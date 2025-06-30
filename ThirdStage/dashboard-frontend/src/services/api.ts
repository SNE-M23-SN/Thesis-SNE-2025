import axios from 'axios';
import {
  apiValidators,
  safeApiCall,
  validators
} from '../utils/validation';
import { logger } from '../utils/logger';

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8282/api/dashboard';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for environment-aware logging
api.interceptors.request.use(
  (config) => {
    const startTime = Date.now();
    (config as any).metadata = { startTime }; // Store start time for duration calculation

    // Only log detailed request info in development
    if (process.env.NODE_ENV === 'development') {
      logger.debug('API Request initiated', 'API', {
        method: config.method?.toUpperCase(),
        url: config.url,
        baseURL: config.baseURL,
        timeout: config.timeout,
        headers: config.headers
      }, { component: 'ApiService', action: 'request' });
    } else {
      // In production, only log essential info
      logger.info('API Request', 'API', {
        method: config.method?.toUpperCase(),
        endpoint: config.url?.split('?')[0] // Remove query params for privacy
      }, { component: 'ApiService', action: 'request' });
    }

    return config;
  },
  (error) => {
    logger.error('API Request Error', 'API', {
      error: error.message,
      method: error.config?.method,
      url: error.config?.url
    }, { component: 'ApiService', action: 'requestError' });
    return Promise.reject(error);
  }
);

// Response interceptor for environment-aware error handling
api.interceptors.response.use(
  (response) => {
    const duration = (response.config as any).metadata?.startTime
      ? Date.now() - (response.config as any).metadata.startTime
      : undefined;

    // Log successful API operations
    logger.logApiOperation(
      response.config.method?.toUpperCase() || 'UNKNOWN',
      response.config.url || 'unknown',
      duration,
      true
    );

    // In development, log response details for debugging
    if (process.env.NODE_ENV === 'development' && duration && duration > 2000) {
      logger.warn('Slow API response detected', 'API', {
        method: response.config.method?.toUpperCase(),
        url: response.config.url,
        duration,
        status: response.status
      }, { component: 'ApiService', action: 'slowResponse' });
    }

    return response;
  },
  (error) => {
    const duration = (error.config as any)?.metadata?.startTime
      ? Date.now() - (error.config as any).metadata.startTime
      : undefined;

    const status = error.response?.status;
    const statusText = error.response?.statusText;
    const method = error.config?.method?.toUpperCase() || 'UNKNOWN';
    const url = error.config?.url || 'unknown';

    // Log failed API operations
    const errorData = process.env.NODE_ENV === 'development'
      ? {
          status,
          statusText,
          errorMessage: error.message,
          responseData: error.response?.data,
          requestConfig: error.config
        }
      : {
          status,
          statusText,
          errorMessage: error.message
        };

    logger.logApiOperation(method, url, duration, false, errorData);

    // Enhanced error categorization with production-safe logging
    if (status === 404) {
      logger.warn('Resource not found', 'API', {
        endpoint: url?.split('?')[0], // Remove query params
        method
      }, { component: 'ApiService', action: 'responseError' });
    } else if (status >= 500) {
      logger.error('Server error occurred', 'API', {
        status,
        statusText,
        endpoint: url?.split('?')[0],
        method,
        duration
      }, { component: 'ApiService', action: 'responseError' });
    } else if (status >= 400) {
      logger.warn('Client error occurred', 'API', {
        status,
        statusText,
        endpoint: url?.split('?')[0],
        method
      }, { component: 'ApiService', action: 'responseError' });
    } else if (!status) {
      // Network error or timeout
      logger.error('Network error occurred', 'API', {
        errorMessage: error.message,
        endpoint: url?.split('?')[0],
        method,
        duration
      }, { component: 'ApiService', action: 'networkError' });
    }

    return Promise.reject(error);
  }
);

// Type definitions based on your Spring Boot DTOs
export interface RecentJobBuildDTO {
  jobName: string;
  buildId: number;
  healthStatus: string;
  anomalyCount: number;
  timeAgo: string;
  rawTimestamp: string;
  computedAt: string;
  originalJobName: string;
}

export interface SecurityAnomalyCountDTO {
  jobFilter: string;
  anomalyCount: number;
  timeRange: string;
  computedAt: string;
}

export interface ActiveBuildCountDTO {
  jobFilter: string;
  activeBuilds: number;
  computedAt: string;
}

export interface JobCountDTO {
  timeBoundary: string;
  totalJobs: number;
  computedAt: string;
}

export interface BuildSummaryDTO {
  job_name: string;
  build_id: number;
  health_status: string;
  build_summary: string;
  build_started_time: string;
  build_duration: string;
  regression_detected: boolean;
}

export interface RiskScoreDTO {
  score: number;
  change: number;
  riskLevel: string;
  previousScore: number;
}

export interface AnomalyDTO {
  type: string;
  severity: string;
  description: string;
  recommendation: string;
  aiAnalysis: string;
  details: any; // Generic object to match backend Object type - can contain various structures
}

export interface AIInsightsDTO {
  // Updated to match actual API response format from backend
  summary?: string;
  recommendations?: string[];
  trends?: {
    pattern?: string;
    direction?: string;
  };

  // Legacy format support (for backward compatibility)
  criticalSecretExposure?: string;
  dependencyManagement?: string;
  securityTrendAlert?: string;
  recommendation?: string | string[]; // ✅ FIX: Can be either string or array
}

export interface ChartDataDTO {
  labels: string[];
  datasets: DatasetDTO[];
}

export interface DatasetDTO {
  label: string;
  data: number[];
  backgroundColor?: string[];
  borderColor?: string;
  borderWidth?: number;
}

// API Service Functions
// Note: These endpoints have been aligned with the backend Spring Boot controller
// Backend base path: /api/dashboard
export const dashboardApi = {
  // Recent Job Builds
  async getRecentJobBuilds(): Promise<RecentJobBuildDTO[]> {
    return safeApiCall(
      () => api.get('/recentJobBuilds').then(response => response.data),
      (data): data is RecentJobBuildDTO[] =>
        Array.isArray(data) && data.every(apiValidators.recentJobBuild),
      'recentJobBuilds',
      [] // fallback to empty array
    );
  },

  async getRecentJobBuildsByName(jobName: string): Promise<RecentJobBuildDTO[]> {
    return safeApiCall(
      () => api.get(`/recentJobBuilds/${encodeURIComponent(jobName)}`).then(response => response.data),
      (data): data is RecentJobBuildDTO[] =>
        Array.isArray(data) && data.every(apiValidators.recentJobBuild),
      `recentJobBuilds for ${jobName}`,
      [] // fallback to empty array
    );
  },

  // Security Anomalies
  async getSecurityAnomalies(): Promise<SecurityAnomalyCountDTO> {
    return safeApiCall(
      () => api.get('/securityAnomalies').then(response => response.data),
      apiValidators.securityAnomalyCount,
      'securityAnomalies',
      { jobFilter: 'all', anomalyCount: 0, timeRange: '7 days', computedAt: new Date().toISOString() } as SecurityAnomalyCountDTO
    );
  },

  async getSecurityAnomaliesByFilter(jobFilter: string, timeRange: string): Promise<SecurityAnomalyCountDTO> {
    return safeApiCall(
      () => api.get(`/securityAnomalies/${encodeURIComponent(jobFilter)}?timeRange=${encodeURIComponent(timeRange)}`).then(response => response.data),
      apiValidators.securityAnomalyCount,
      `securityAnomalies for ${jobFilter}`,
      { jobFilter, anomalyCount: 0, timeRange: '7 days', computedAt: new Date().toISOString() } as SecurityAnomalyCountDTO
    );
  },

  // Active Builds
  async getActiveBuilds(): Promise<ActiveBuildCountDTO> {
    return safeApiCall(
      () => api.get('/activeBuilds').then(response => response.data),
      apiValidators.activeBuildCount,
      'activeBuilds',
      { jobFilter: 'all', activeBuilds: 0, computedAt: new Date().toISOString() } as ActiveBuildCountDTO
    );
  },

  async getActiveBuildsByFilter(jobFilter: string): Promise<ActiveBuildCountDTO> {
    return safeApiCall(
      () => api.get(`/activeBuilds/${encodeURIComponent(jobFilter)}`).then(response => response.data),
      apiValidators.activeBuildCount,
      `activeBuilds for ${jobFilter}`,
      { jobFilter, activeBuilds: 0, computedAt: new Date().toISOString() } as ActiveBuildCountDTO
    );
  },

  // Total Jobs
  async getTotalJobs(timeBoundary: string): Promise<JobCountDTO> {
    return safeApiCall(
      () => api.get(`/totalJobs/${encodeURIComponent(timeBoundary)}`).then(response => response.data),
      apiValidators.jobCount,
      `totalJobs for ${timeBoundary}`,
      { timeBoundary, totalJobs: 0, computedAt: new Date().toISOString() } as JobCountDTO
    );
  },

  // Build Details
  async getBuildSummary(conversationId: string, buildNumber: number): Promise<{ hasAiData: boolean; data?: BuildSummaryDTO; message?: string }> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(conversationId)}/${buildNumber}`).then(response => response.data),
      apiValidators.buildSummaryWrapper(apiValidators.buildSummary),
      `buildSummary for ${conversationId}/${buildNumber}`,
      { hasAiData: false, message: 'No build summary available' }
    );
  },

  async getRiskScore(conversationId: string, buildNumber: number): Promise<{ hasData: boolean; data?: RiskScoreDTO; message?: string }> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(conversationId)}/${buildNumber}/risk-score`).then(response => response.data),
      apiValidators.responseWrapper(apiValidators.riskScore),
      `riskScore for ${conversationId}/${buildNumber}`,
      { hasData: false, message: 'No risk score available' }
    );
  },

  async getLogsTracker(jobName: string, buildId: number): Promise<{ received: number; expected: number; status: string }> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(jobName)}/${buildId}/logs-tracker`).then(response => response.data),
      (data): data is { received: number; expected: number; status: string } => {
        return validators.isObject(data) &&
               validators.isNumber((data as any).received) &&
               validators.isNumber((data as any).expected) &&
               validators.isString((data as any).status);
      },
      `logsTracker for ${jobName}/${buildId}`,
      { received: 0, expected: 0, status: 'unknown' }
    );
  },

  async getBuildLogs(jobName: string, buildId: number, page: number = 0, size: number = 100): Promise<any[]> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(jobName)}/${buildId}/logs?page=${page}&size=${size}`).then(response => response.data),
      validators.isArray,
      `buildLogs for ${jobName}/${buildId}`,
      []
    );
  },

  // Charts and Analytics
  async getAnomalyTrend(jobFilter: string, buildCount: number = 10): Promise<ChartDataDTO> {
    return safeApiCall(
      () => api.get(`/anomaly-trend?jobFilter=${encodeURIComponent(jobFilter)}&buildCount=${buildCount}`).then(response => response.data),
      apiValidators.chartData,
      `anomalyTrend for ${jobFilter}`,
      { labels: [], datasets: [] }
    );
  },

  async getSeverityDistribution(jobFilter: string, buildCount: number = 5): Promise<ChartDataDTO> {
    return safeApiCall(
      () => api.get(`/severity-distribution?jobFilter=${encodeURIComponent(jobFilter)}&buildCount=${buildCount}`).then(response => response.data),
      apiValidators.chartData,
      `severityDistribution for ${jobFilter}`,
      { labels: [], datasets: [] }
    );
  },

  // Build-specific anomalies (replaces the general getAnomalies method)
  // Note: Backend only provides anomalies for specific builds, not general job-level anomalies
  async getDetectedAnomalies(jobName: string, buildId: number, page: number = 1, size: number = 3): Promise<{ hasData: boolean; data?: { anomalies: AnomalyDTO[]; totalCount: number; pageNumber: number; pageSize: number; totalPages?: number }; message?: string }> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(jobName)}/${buildId}/detected-anomalies?page=${page}&size=${size}`).then(response => response.data),
      (data: any): data is { hasData: boolean; data?: { anomalies: AnomalyDTO[]; totalCount: number; pageNumber: number; pageSize: number; totalPages?: number }; message?: string } => {
        return validators.isObject(data) && validators.isBoolean((data as any).hasData);
      },
      `detectedAnomalies for ${jobName}/${buildId}`,
      { hasData: false, message: 'No anomalies available' }
    );
  },

  async getAIInsights(jobName: string, buildId: number): Promise<AIInsightsDTO | { hasData: boolean; data?: AIInsightsDTO; message?: string }> {
    return safeApiCall(
      () => api.get(`/builds/${encodeURIComponent(jobName)}/${buildId}/ai-insights`).then(response => response.data),
      (data: any): data is AIInsightsDTO | { hasData: boolean; data?: AIInsightsDTO; message?: string } => {
        // Handle empty map (no data case)
        if (validators.isObject(data) && Object.keys(data).length === 0) {
          return true; // Empty object is valid (will be handled by component)
        }

        // Handle wrapped response format (if backend returns it)
        if (validators.isObject(data) && 'hasData' in data) {
          return apiValidators.responseWrapper(apiValidators.aiInsights)(data);
        }

        // Handle direct AIInsightsDTO format
        return apiValidators.aiInsights(data);
      },
      `aiInsights for ${jobName}/${buildId}`,
      { hasData: false, message: 'No AI insights available' } // Return proper fallback structure
    );
  },

  // @deprecated - Use getDetectedAnomalies(jobName, buildId, page, size) instead
  // The backend doesn't support general anomalies by job filter, only build-specific anomalies
  async getAnomalies(jobName: string, buildId: number, page: number = 1, size: number = 3): Promise<{ hasData: boolean; data?: { anomalies: AnomalyDTO[]; totalCount: number; pageNumber: number; pageSize: number; totalPages?: number }; message?: string }> {
    logger.warn('getAnomalies is deprecated. Use getDetectedAnomalies instead.', 'API', {
      jobName,
      buildId,
      page,
      size
    }, { component: 'ApiService', action: 'getAnomalies' });
    return this.getDetectedAnomalies(jobName, buildId, page, size);
  },

  // Job Management
  async rerunBuild(jobName: string, buildId: number): Promise<{ message: string }> {
    return safeApiCall(
      () => api.post(`/builds/${encodeURIComponent(jobName)}/${buildId}/rerun`).then(response => response.data),
      (data): data is { message: string } => {
        return validators.isObject(data) && validators.isString((data as any).message);
      },
      `rerunBuild for ${jobName}/${buildId}`,
      { message: 'Build rerun failed' }
    );
  },

  async getJobExplorer(tab: string = 'all'): Promise<{ job_name: string; last_build: string; status: string; anomalies: number }[]> {
    return safeApiCall(
      () => api.get(`/job-explorer${tab !== 'all' ? `?tab=${encodeURIComponent(tab)}` : ''}`).then(response => response.data),
      (data): data is { job_name: string; last_build: string; status: string; anomalies: number }[] =>
        validators.isArray(data) && data.every(apiValidators.jobExplorer),
      `jobExplorer for tab ${tab}`,
      []
    );
  },

  // Additional method to get all job names
  async getAllJobNames(): Promise<string[]> {
    return safeApiCall(
      () => api.get('/jobs').then(response => response.data),
      apiValidators.stringArray,
      'allJobNames',
      []
    );
  },
};

export default api;
