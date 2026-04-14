import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Download, Save } from "lucide-react";
import { LoadTestResult } from "@/types/loadTesting";
import { toast } from "sonner";

interface TestResultsSummaryProps {
  result: LoadTestResult;
  onSaveConfig: () => void;
}

export const TestResultsSummary = ({
  result,
  onSaveConfig,
}: TestResultsSummaryProps) => {
  const handleDownloadReport = () => {
    const reportData = {
      testId: result.id,
      startedAt: result.startedAt,
      completedAt: result.completedAt,
      duration: result.duration,
      config: result.config,
      metrics: result.metrics,
    };

    const blob = new Blob([JSON.stringify(reportData, null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `load-test-${result.id}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success("Report downloaded successfully");
  };

  return (
    <Card className="p-6 shadow-elevated backdrop-blur-sm bg-gradient-to-br from-card to-card/50 animate-fade-in">
      <div className="mb-6 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-foreground">Test Results Summary</h3>
        <Badge variant={result.metrics.successRate > 95 ? "default" : "destructive"}>
          {result.metrics.successRate > 95 ? "Passed" : "Issues Detected"}
        </Badge>
      </div>

      <div className="space-y-6">
        <div className="grid gap-6 md:grid-cols-3">
          <div className="rounded-lg border border-border bg-background/50 p-4">
            <p className="text-sm text-muted-foreground">Total Requests</p>
            <p className="mt-2 text-3xl font-bold text-foreground">
              {result.metrics.requestsSent.toLocaleString()}
            </p>
          </div>

          <div className="rounded-lg border border-border bg-background/50 p-4">
            <p className="text-sm text-muted-foreground">Success Rate</p>
            <p className="mt-2 text-3xl font-bold text-green-600">
              {result.metrics.successRate.toFixed(2)}%
            </p>
          </div>

          <div className="rounded-lg border border-border bg-background/50 p-4">
            <p className="text-sm text-muted-foreground">Avg Response Time</p>
            <p className="mt-2 text-3xl font-bold text-foreground">
              {result.metrics.avgResponseTime.toFixed(2)}ms
            </p>
          </div>

          <div className="rounded-lg border border-border bg-background/50 p-4">
            <p className="text-sm text-muted-foreground">Backend</p>
            <p className="mt-2 text-2xl font-bold text-foreground">
              {result.backendUsed || "IN_MEMORY"}
            </p>
          </div>
        </div>

        <div className="rounded-lg bg-muted/50 p-4">
          <h4 className="mb-3 font-semibold text-foreground">Performance Breakdown</h4>
          <div className="grid gap-3 md:grid-cols-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Successful Requests</span>
              <span className="font-medium text-foreground">
                {result.metrics.successful.toLocaleString()}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Rate Limited</span>
              <span className="font-medium text-red-600">
                {result.metrics.rateLimited.toLocaleString()}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Failed Requests</span>
              <span className="font-medium text-foreground">
                {result.metrics.failed.toLocaleString()}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Test Duration</span>
              <span className="font-medium text-foreground">{result.duration}s</span>
            </div>
          </div>
        </div>

        {result.perKeyResults && Object.keys(result.perKeyResults).length > 0 && (
          <div className="rounded-lg bg-muted/50 p-4">
            <h4 className="mb-3 font-semibold text-foreground">User Type Comparison</h4>
            <div className="space-y-3">
              {Object.entries(result.perKeyResults).map(([apiKey, stats]) => (
                <div key={apiKey} className="rounded-md border border-border bg-background p-3">
                  <div className="mb-2 flex items-center justify-between">
                    <span className="font-medium text-foreground">{stats.userType}</span>
                    <code className="text-xs text-muted-foreground">{apiKey}</code>
                  </div>
                  <div className="grid gap-2 md:grid-cols-4 text-sm">
                    <div className="flex items-center justify-between md:block">
                      <span className="text-muted-foreground">Success</span>
                      <p className="font-medium text-green-600">{stats.successfulRequests.toLocaleString()}</p>
                    </div>
                    <div className="flex items-center justify-between md:block">
                      <span className="text-muted-foreground">Throttled</span>
                      <p className="font-medium text-red-600">{stats.throttledRequests.toLocaleString()}</p>
                    </div>
                    <div className="flex items-center justify-between md:block">
                      <span className="text-muted-foreground">Errors</span>
                      <p className="font-medium text-foreground">{stats.errorRequests.toLocaleString()}</p>
                    </div>
                    <div className="flex items-center justify-between md:block">
                      <span className="text-muted-foreground">Success Rate</span>
                      <p className="font-medium text-foreground">{stats.successRate.toFixed(2)}%</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="rounded-lg bg-muted/50 p-4">
          <h4 className="mb-3 font-semibold text-foreground">Response Time Percentiles</h4>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">P50 (Median)</span>
              <span className="font-medium text-foreground">
                {result.metrics.p50ResponseTime.toFixed(2)}ms
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">P95</span>
              <span className="font-medium text-foreground">
                {result.metrics.p95ResponseTime.toFixed(2)}ms
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">P99</span>
              <span className="font-medium text-foreground">
                {result.metrics.p99ResponseTime.toFixed(2)}ms
              </span>
            </div>
          </div>
        </div>

        <div className="flex gap-3">
          <Button onClick={handleDownloadReport} variant="outline" className="gap-2">
            <Download className="h-4 w-4" />
            Download Report
          </Button>
          <Button onClick={onSaveConfig} variant="outline" className="gap-2">
            <Save className="h-4 w-4" />
            Save Configuration
          </Button>
        </div>
      </div>
    </Card>
  );
};
