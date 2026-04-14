import { useState, useEffect, useMemo } from "react";
import { KeysOverview } from "@/components/apikeys/KeysOverview";
import { KeysTable } from "@/components/apikeys/KeysTable";
import { KeyDetailsPanel } from "@/components/apikeys/KeyDetailsPanel";
import { ApiHealthCheck } from "@/components/ApiHealthCheck";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";
import { ManagedApiKey, rateLimiterApi } from "@/services/rateLimiterApi";
import { ApiKey } from "@/types/apiKeys";
import { generateMockAccessLogs } from "@/utils/mockApiKeys";

const ApiKeys = () => {
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [selectedKeyForDetails, setSelectedKeyForDetails] = useState<ApiKey | null>(null);
  const [keyToDelete, setKeyToDelete] = useState<string | null>(null);
  const accessLogs = generateMockAccessLogs();

  const mapManagedKey = (key: ManagedApiKey): ApiKey => ({
    id: key.key,
    name: key.displayName || key.key,
    key: key.key,
    description: key.description,
    status: key.active ? "active" : "inactive",
    userType: key.userType === "PREMIUM" ? "PREMIUM" : "STANDARD",
    createdAt: new Date().toISOString(),
    lastUsed: key.lastUsedAt > 0 ? new Date(key.lastUsedAt).toISOString() : undefined,
    rateLimit: {
      capacity: key.capacity,
      refillRate: key.refillRate,
      algorithm: key.algorithm.toLowerCase().replace("_", "-"),
    },
    usageStats: {
      totalRequests: key.usage.totalRequests,
      successfulRequests: key.usage.successfulRequests,
      rateLimitedRequests: key.usage.rateLimitedRequests,
    },
  });

  const loadKeys = async () => {
    try {
      const response = await rateLimiterApi.getApiKeys();
      setKeys(response.keys.map(mapManagedKey));
    } catch (error) {
      console.error("Failed to load API keys:", error);
      toast.error("Failed to load API keys from backend");
    }
  };

  useEffect(() => {
    loadKeys();
    
    // Refresh keys every 10 seconds
    const interval = setInterval(loadKeys, 10000);
    return () => clearInterval(interval);
  }, []);

  const selectedKey = useMemo(
    () => keys.find((k) => k.id === keyToDelete),
    [keys, keyToDelete]
  );

  const handleDeactivateKey = async (id: string) => {
    try {
      await rateLimiterApi.deactivateApiKey(id);
      await loadKeys();
      setKeyToDelete(null);
      toast.success("API key deactivated");
    } catch (error) {
      toast.error("Failed to deactivate API key");
    }
  };

  const handleToggleActive = async (key: ApiKey) => {
    try {
      if (key.status === "active") {
        await rateLimiterApi.deactivateApiKey(key.key);
        toast.success(`Deactivated ${key.name}`);
      } else {
        await rateLimiterApi.activateApiKey(key.key);
        toast.success(`Activated ${key.name}`);
      }
      await loadKeys();
    } catch (error) {
      toast.error("Failed to update key status");
    }
  };

  const handleRegenerateKey = () => {
    if (selectedKeyForDetails) {
      const newKey = `rl_${Array.from({ length: 32 }, () =>
        Math.random().toString(36)[2]
      ).join("")}`;
      setKeys(
        keys.map((k) => (k.id === selectedKeyForDetails.id ? { ...k, key: newKey } : k))
      );
      toast.success("API key regenerated successfully");
      setSelectedKeyForDetails(null);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-3xl font-bold tracking-tight text-foreground">API Keys</h2>
        <p className="text-muted-foreground">
          Manage API keys for authentication and access control
        </p>
      </div>

      <ApiHealthCheck />

      <KeysOverview keys={keys} />

      <KeysTable
        keys={keys}
        selectedKeys={selectedKeys}
        onSelectionChange={setSelectedKeys}
        onView={setSelectedKeyForDetails}
        onEdit={handleToggleActive}
        onDelete={(id) => setKeyToDelete(id)}
      />
      <KeyDetailsPanel
        keyData={selectedKeyForDetails}
        accessLogs={accessLogs}
        open={!!selectedKeyForDetails}
        onClose={() => setSelectedKeyForDetails(null)}
        onEdit={() => toast.info("Edit functionality coming soon")}
        onDelete={() => {
          if (selectedKeyForDetails) {
            handleDeactivateKey(selectedKeyForDetails.id);
            setSelectedKeyForDetails(null);
          }
        }}
        onRegenerate={handleRegenerateKey}
      />


      <AlertDialog open={!!keyToDelete} onOpenChange={() => setKeyToDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete API Key</AlertDialogTitle>
            <AlertDialogDescription>
              {selectedKey?.status === "active"
                ? "Deactivate this API key? Requests using this key will be rejected until it is reactivated."
                : "This API key is already inactive."}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => keyToDelete && handleDeactivateKey(keyToDelete)}
              className="bg-destructive"
              disabled={selectedKey?.status !== "active"}
            >
              Deactivate
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default ApiKeys;
