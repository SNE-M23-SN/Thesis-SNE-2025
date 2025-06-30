// Strict type definitions for enhanced type safety

// Utility types for better type safety
export type NonEmptyArray<T> = [T, ...T[]];

export type RequiredKeys<T, K extends keyof T> = T & Required<Pick<T, K>>;

export type OptionalKeys<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

export type StrictRecord<K extends string | number | symbol, V> = {
  [P in K]: V;
};

// API Response types with strict validation
export interface StrictApiResponse<T> {
  readonly data: T;
  readonly success: true;
  readonly timestamp: string;
}

export interface StrictApiError {
  readonly success: false;
  readonly error: {
    readonly code: string;
    readonly message: string;
    readonly details?: unknown;
  };
  readonly timestamp: string;
}

export type StrictApiResult<T> = StrictApiResponse<T> | StrictApiError;

// Component prop types with strict validation
export interface StrictComponentProps {
  readonly className?: string;
  readonly 'data-testid'?: string;
}

// Event handler types
export type StrictEventHandler<T = Event> = (event: T) => void;
export type StrictAsyncEventHandler<T = Event> = (event: T) => Promise<void>;

// Form types with strict validation
export interface StrictFormField<T> {
  readonly value: T;
  readonly error?: string;
  readonly touched: boolean;
  readonly required: boolean;
}

export interface StrictFormState<T extends Record<string, any>> {
  readonly fields: {
    readonly [K in keyof T]: StrictFormField<T[K]>;
  };
  readonly isValid: boolean;
  readonly isSubmitting: boolean;
  readonly submitCount: number;
}

// Async operation states with strict typing
export interface StrictAsyncState<T, E = Error> {
  readonly data: T | null;
  readonly loading: boolean;
  readonly error: E | null;
  readonly lastUpdated: Date | null;
}

// Chart data types with strict validation
export interface StrictChartDataset {
  readonly label: string;
  readonly data: readonly number[];
  readonly backgroundColor?: readonly string[];
  readonly borderColor?: string;
  readonly borderWidth?: number;
}

export interface StrictChartData {
  readonly labels: readonly string[];
  readonly datasets: readonly StrictChartDataset[];
}

// Dashboard specific strict types
export interface StrictDashboardFilters {
  readonly jobFilter: string;
  readonly timeRange: '7 days' | '14 days' | '30 days';
  readonly refreshInterval: number;
}

export interface StrictBuildInfo {
  readonly jobName: string;
  readonly buildId: number;
  readonly status: 'SUCCESS' | 'FAILURE' | 'UNSTABLE' | 'ABORTED' | 'NOT_BUILT';
  readonly timestamp: string;
  readonly duration: number;
}

export interface StrictAnomalyInfo {
  readonly id: string;
  readonly type: string;
  readonly severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  readonly description: string;
  readonly recommendation?: string;
  readonly buildInfo: StrictBuildInfo;
}

// Validation result types
export interface StrictValidationResult<T> {
  readonly isValid: boolean;
  readonly data: T | null;
  readonly errors: readonly string[];
}

// Logger types with strict validation
export interface StrictLogEntry {
  readonly timestamp: string;
  readonly level: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
  readonly message: string;
  readonly context?: string;
  readonly data?: unknown;
  readonly component?: string;
  readonly action?: string;
}

// Performance monitoring types
export interface StrictPerformanceMetric {
  readonly name: string;
  readonly value: number;
  readonly unit: 'ms' | 'bytes' | 'count' | 'percentage';
  readonly timestamp: Date;
  readonly threshold?: number;
}

// Error types with strict categorization
export interface StrictErrorInfo {
  readonly type: 'NETWORK' | 'VALIDATION' | 'API' | 'COMPONENT' | 'UNKNOWN';
  readonly severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  readonly message: string;
  readonly stack?: string;
  readonly context?: string;
  readonly timestamp: Date;
  readonly retryable: boolean;
}

// Configuration types
export interface StrictAppConfig {
  readonly apiBaseUrl: string;
  readonly environment: 'development' | 'staging' | 'production';
  readonly logLevel: 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
  readonly features: {
    readonly enableAnalytics: boolean;
    readonly enableErrorReporting: boolean;
    readonly enablePerformanceMonitoring: boolean;
  };
}

// Type guards for runtime validation
export const isNonEmptyArray = <T>(value: T[]): value is NonEmptyArray<T> => {
  return Array.isArray(value) && value.length > 0;
};

export const isStrictApiResponse = <T>(value: any): value is StrictApiResponse<T> => {
  return (
    typeof value === 'object' &&
    value !== null &&
    'data' in value &&
    'success' in value &&
    value.success === true &&
    'timestamp' in value &&
    typeof value.timestamp === 'string'
  );
};

export const isStrictApiError = (value: any): value is StrictApiError => {
  return (
    typeof value === 'object' &&
    value !== null &&
    'success' in value &&
    value.success === false &&
    'error' in value &&
    typeof value.error === 'object' &&
    'timestamp' in value &&
    typeof value.timestamp === 'string'
  );
};

export const isValidBuildStatus = (status: string): status is StrictBuildInfo['status'] => {
  return ['SUCCESS', 'FAILURE', 'UNSTABLE', 'ABORTED', 'NOT_BUILT'].includes(status);
};

export const isValidSeverity = (severity: string): severity is StrictAnomalyInfo['severity'] => {
  return ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(severity);
};

export const isValidLogLevel = (level: string): level is StrictLogEntry['level'] => {
  return ['DEBUG', 'INFO', 'WARN', 'ERROR'].includes(level);
};

// Utility functions for strict type handling
export const assertNever = (value: never): never => {
  throw new Error(`Unexpected value: ${value}`);
};

export const exhaustiveCheck = (value: never): never => {
  throw new Error(`Exhaustive check failed. Received: ${value}`);
};

// Type-safe object key iteration
export const typedKeys = <T extends Record<string, any>>(obj: T): (keyof T)[] => {
  return Object.keys(obj) as (keyof T)[];
};

export const typedEntries = <T extends Record<string, any>>(obj: T): [keyof T, T[keyof T]][] => {
  return Object.entries(obj) as [keyof T, T[keyof T]][];
};

// Safe array access with bounds checking
export const safeArrayAccess = <T>(array: readonly T[], index: number): T | undefined => {
  return index >= 0 && index < array.length ? array[index] : undefined;
};

// Type-safe environment variable access
export const getEnvVar = (key: string, defaultValue?: string): string => {
  const value = import.meta.env[key];
  if (value === undefined) {
    if (defaultValue !== undefined) {
      return defaultValue;
    }
    throw new Error(`Environment variable ${key} is not defined`);
  }
  return value;
};

// Branded types for additional type safety
export type BrandedString<T> = string & { readonly __brand: T };
export type JobName = BrandedString<'JobName'>;
export type BuildId = BrandedString<'BuildId'>;
export type UserId = BrandedString<'UserId'>;

export const createJobName = (value: string): JobName => value as JobName;
export const createBuildId = (value: string): BuildId => value as BuildId;
export const createUserId = (value: string): UserId => value as UserId;

export default {
  isNonEmptyArray,
  isStrictApiResponse,
  isStrictApiError,
  isValidBuildStatus,
  isValidSeverity,
  isValidLogLevel,
  assertNever,
  exhaustiveCheck,
  typedKeys,
  typedEntries,
  safeArrayAccess,
  getEnvVar,
  createJobName,
  createBuildId,
  createUserId
};
