export type KeyStatus = "active" | "inactive" | "expired";
export type ApiUserType = "PREMIUM" | "STANDARD" | "ANONYMOUS";

export interface ApiKey {
  id: string;
  name: string;
  key: string;
  description?: string;
  status: KeyStatus;
  userType?: ApiUserType;
  createdAt: string;
  lastUsed?: string;
  expiresAt?: string;
  rateLimit: {
    capacity: number;
    refillRate: number;
    algorithm: string;
  };
  usageStats: {
    totalRequests: number;
    successfulRequests: number;
    rateLimitedRequests: number;
  };
  ipWhitelist?: string[];
  ipBlacklist?: string[];
}

export interface ApiKeyCreateInput {
  name: string;
  key?: string;
  description?: string;
  expiresAt?: Date;
  useDefaultLimits: boolean;
  customLimits?: {
    capacity: number;
    refillRate: number;
    algorithm: string;
  };
  ipWhitelist?: string[];
}

export interface KeyAccessLog {
  id: string;
  timestamp: string;
  ipAddress: string;
  endpoint: string;
  statusCode: number;
  responseTime: number;
}
