import { Card } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Slider } from "@/components/ui/slider";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Settings } from "lucide-react";
import { LoadTestConfig, RequestPattern } from "@/types/loadTesting";

interface TestConfigPanelProps {
  config: LoadTestConfig;
  onChange: (config: LoadTestConfig) => void;
}

const durationOptions = [
  { label: "30 seconds", value: 30 },
  { label: "1 minute", value: 60 },
  { label: "5 minutes", value: 300 },
  { label: "10 minutes", value: 600 },
];

export const TestConfigPanel = ({ config, onChange }: TestConfigPanelProps) => {
  return (
    <Card className="p-6 shadow-elevated backdrop-blur-sm bg-gradient-to-br from-card to-card/50">
      <div className="mb-6 flex items-center gap-2">
        <Settings className="h-5 w-5 text-primary" />
        <h3 className="text-lg font-semibold text-foreground">Test Configuration</h3>
      </div>

      <div className="space-y-6">
        <div className="space-y-2">
          <Label htmlFor="standard-api-key">Standard User API Key</Label>
          <Input
            id="standard-api-key"
            placeholder="api-key-1"
            value={config.standardApiKey}
            onChange={(e) => onChange({ ...config, standardApiKey: e.target.value })}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="premium-api-key">Premium User API Key</Label>
          <Input
            id="premium-api-key"
            placeholder="premium-key-123"
            value={config.premiumApiKey}
            onChange={(e) => onChange({ ...config, premiumApiKey: e.target.value })}
          />
          <p className="text-xs text-muted-foreground">
            Load tests compare how standard and premium keys are throttled.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="target-key">Fallback Key Prefix</Label>
          <Input
            id="target-key"
            placeholder="rl_prod_user123"
            value={config.targetKey}
            onChange={(e) => onChange({ ...config, targetKey: e.target.value })}
          />
          <p className="text-xs text-muted-foreground">
            Used only when standard/premium keys are not provided
          </p>
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <Label>Request Rate (req/s)</Label>
            <span className="text-sm font-medium text-foreground">{config.requestRate}</span>
          </div>
          <Slider
            value={[config.requestRate]}
            onValueChange={([value]) => onChange({ ...config, requestRate: value })}
            min={1}
            max={1000}
            step={10}
            className="py-4"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="duration">Test Duration</Label>
          <Select
            value={config.duration.toString()}
            onValueChange={(value) => onChange({ ...config, duration: parseInt(value) })}
          >
            <SelectTrigger id="duration">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {durationOptions.map((opt) => (
                <SelectItem key={opt.value} value={opt.value.toString()}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <Label>Concurrency Level</Label>
            <span className="text-sm font-medium text-foreground">{config.concurrency}</span>
          </div>
          <Slider
            value={[config.concurrency]}
            onValueChange={([value]) => onChange({ ...config, concurrency: value })}
            min={1}
            max={100}
            step={1}
            className="py-4"
          />
          <p className="text-xs text-muted-foreground">
            Number of concurrent clients
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="pattern">Request Pattern</Label>
          <Select
            value={config.pattern}
            onValueChange={(value) => onChange({ ...config, pattern: value as RequestPattern })}
          >
            <SelectTrigger id="pattern">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="constant">Constant Rate</SelectItem>
              <SelectItem value="ramp-up">Ramp Up (gradually increase)</SelectItem>
              <SelectItem value="spike">Spike Test (sudden load)</SelectItem>
              <SelectItem value="step-load">Step Load (incremental steps)</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </Card>
  );
};
