import { useState, useEffect } from "react";
import { GlobalConfigCard } from "@/components/configuration/GlobalConfigCard";
import { toast } from "sonner";
import { rateLimiterApi } from "@/services/rateLimiterApi";
import {
  GlobalConfig,
  ConfigAlgorithm,
} from "@/types/configuration";

const Configuration = () => {
      const normalizeAlgorithm = (algorithm?: string): ConfigAlgorithm => {
        const normalized = (algorithm || "TOKEN_BUCKET").toLowerCase().replaceAll("_", "-");
        const valid: ConfigAlgorithm[] = ["token-bucket", "sliding-window", "fixed-window", "leaky-bucket"];
        return valid.includes(normalized as ConfigAlgorithm)
          ? (normalized as ConfigAlgorithm)
          : "token-bucket";
      };

      const toBackendAlgorithm = (algorithm: ConfigAlgorithm): string =>
        algorithm.toUpperCase().replaceAll("-", "_");

  const [globalConfig, setGlobalConfig] = useState<GlobalConfig>({
    defaultCapacity: 10,
    defaultRefillRate: 2,
    cleanupInterval: 300,
    algorithm: "token-bucket",
  });


  // Load configuration from API
  useEffect(() => {
    const loadConfig = async () => {
      try {
        const config = await rateLimiterApi.getConfig();
        
        setGlobalConfig({
          defaultCapacity: config.capacity,
          defaultRefillRate: config.refillRate,
          cleanupInterval: config.cleanupIntervalMs / 1000,
          algorithm: normalizeAlgorithm(config.algorithm),
        });

      } catch (error) {
        console.error('Failed to load configuration:', error);
        toast.error('Failed to load configuration from backend');
      }
    };

    loadConfig();
  }, []);

  const handleUpdateGlobalConfig = async (config: GlobalConfig) => {
    try {
      await rateLimiterApi.updateDefaultConfig({
        capacity: config.defaultCapacity,
        refillRate: config.defaultRefillRate,
        cleanupIntervalMs: config.cleanupInterval * 1000,
        algorithm: toBackendAlgorithm(config.algorithm),
      });
      setGlobalConfig(config);
      toast.success("Global configuration updated successfully");
    } catch (error) {
      toast.error("Failed to update global configuration");
      console.error(error);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-3xl font-bold tracking-tight text-foreground">
          Configuration Management
        </h2>
        <p className="text-muted-foreground">
          Manage global default rate limiting settings
        </p>
      </div>

      <GlobalConfigCard config={globalConfig} onUpdate={handleUpdateGlobalConfig} />
    </div>
  );
};

export default Configuration;
