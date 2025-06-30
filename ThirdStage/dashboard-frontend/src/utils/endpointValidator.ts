// Comprehensive endpoint validation and testing utility
import { dashboardApi } from '../services/api';
import { logger } from './logger';

export interface EndpointTestResult {
  endpoint: string;
  method: string;
  success: boolean;
  duration: number;
  statusCode?: number;
  error?: string;
  dataReceived: boolean;
  dataSize?: number;
}

export interface ValidationSuite {
  totalEndpoints: number;
  passedEndpoints: number;
  failedEndpoints: number;
  results: EndpointTestResult[];
  overallHealth: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
  totalDuration: number;
  recommendations: string[];
}

// Test individual endpoint with detailed validation
const testEndpoint = async (
  name: string,
  method: string,
  testFn: () => Promise<any>,
  expectedDataStructure?: (data: any) => boolean
): Promise<EndpointTestResult> => {
  const startTime = Date.now();
  
  try {
    logger.info(`🧪 Testing endpoint: ${name}`, 'ENDPOINT_VALIDATOR');
    const response = await testFn();
    const duration = Date.now() - startTime;
    
    // Validate data structure if provided
    let dataValid = true;
    if (expectedDataStructure && response) {
      dataValid = expectedDataStructure(response);
    }
    
    const dataSize = response ? JSON.stringify(response).length : 0;
    const dataReceived = !!response && (Array.isArray(response) ? response.length > 0 : Object.keys(response).length > 0);
    
    logger.info(`✅ Endpoint test passed: ${name}`, 'ENDPOINT_VALIDATOR', {
      duration,
      dataReceived,
      dataSize,
      dataValid
    });
    
    return {
      endpoint: name,
      method,
      success: true,
      duration,
      dataReceived,
      dataSize,
      statusCode: 200
    };
  } catch (error: any) {
    const duration = Date.now() - startTime;
    const errorMessage = error?.message || 'Unknown error';
    const statusCode = error?.response?.status || 0;
    
    logger.error(`❌ Endpoint test failed: ${name}`, 'ENDPOINT_VALIDATOR', {
      duration,
      error: errorMessage,
      statusCode
    });
    
    return {
      endpoint: name,
      method,
      success: false,
      duration,
      error: errorMessage,
      statusCode,
      dataReceived: false
    };
  }
};

// Comprehensive endpoint validation suite
export const validateAllEndpoints = async (): Promise<ValidationSuite> => {
  const startTime = Date.now();
  logger.info('🚀 Starting comprehensive endpoint validation suite', 'ENDPOINT_VALIDATOR');
  
  const endpointTests = [
    // Core Dashboard Endpoints
    {
      name: 'Recent Job Builds',
      method: 'GET',
      testFn: () => dashboardApi.getRecentJobBuilds(),
      validator: (data: any) => Array.isArray(data) && data.every(item => 
        item.jobName && item.buildId && item.status
      )
    },
    {
      name: 'Security Anomalies',
      method: 'GET', 
      testFn: () => dashboardApi.getSecurityAnomalies(),
      validator: (data: any) => data && typeof data.anomalyCount === 'number'
    },
    {
      name: 'Active Builds',
      method: 'GET',
      testFn: () => dashboardApi.getActiveBuilds(),
      validator: (data: any) => data && typeof data.activeBuilds === 'number'
    },
    {
      name: 'Total Jobs (7 days)',
      method: 'GET',
      testFn: () => dashboardApi.getTotalJobs('7_days'),
      validator: (data: any) => data && typeof data.totalJobs === 'number'
    },
    {
      name: 'All Job Names',
      method: 'GET',
      testFn: () => dashboardApi.getAllJobNames(),
      validator: (data: any) => Array.isArray(data) && data.every(item => typeof item === 'string')
    },
    
    // Chart Data Endpoints
    {
      name: 'Anomaly Trend Chart',
      method: 'GET',
      testFn: () => dashboardApi.getAnomalyTrend('All Jobs', 10),
      validator: (data: any) => data && Array.isArray(data.labels) && Array.isArray(data.datasets)
    },
    {
      name: 'Severity Distribution Chart',
      method: 'GET',
      testFn: () => dashboardApi.getSeverityDistribution('All Jobs', 5),
      validator: (data: any) => data && Array.isArray(data.labels) && Array.isArray(data.datasets)
    },
    
    // Filtered Endpoints
    {
      name: 'Security Anomalies by Filter',
      method: 'GET',
      testFn: () => dashboardApi.getSecurityAnomaliesByFilter('all', '7 days'),
      validator: (data: any) => data && typeof data.anomalyCount === 'number'
    },
    {
      name: 'Active Builds by Filter',
      method: 'GET',
      testFn: () => dashboardApi.getActiveBuildsByFilter('all'),
      validator: (data: any) => data && typeof data.activeBuilds === 'number'
    },
    {
      name: 'Total Jobs (14 days)',
      method: 'GET',
      testFn: () => dashboardApi.getTotalJobs('14_days'),
      validator: (data: any) => data && typeof data.totalJobs === 'number'
    },
    {
      name: 'Total Jobs (30 days)',
      method: 'GET',
      testFn: () => dashboardApi.getTotalJobs('30_days'),
      validator: (data: any) => data && typeof data.totalJobs === 'number'
    },
    
    // Job Explorer
    {
      name: 'Job Explorer',
      method: 'GET',
      testFn: () => dashboardApi.getJobExplorer(),
      validator: (data: any) => Array.isArray(data)
    }
  ];
  
  // Execute all tests
  const results: EndpointTestResult[] = [];
  
  for (const test of endpointTests) {
    const result = await testEndpoint(test.name, test.method, test.testFn, test.validator);
    results.push(result);
    
    // Small delay between tests to avoid overwhelming the server
    await new Promise(resolve => setTimeout(resolve, 150));
  }
  
  // Calculate health metrics
  const passedEndpoints = results.filter(r => r.success).length;
  const failedEndpoints = results.filter(r => !r.success).length;
  const totalDuration = Date.now() - startTime;
  const successRate = (passedEndpoints / results.length) * 100;
  
  // Determine overall health
  let overallHealth: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
  if (successRate >= 90) {
    overallHealth = 'HEALTHY';
  } else if (successRate >= 70) {
    overallHealth = 'DEGRADED';
  } else {
    overallHealth = 'UNHEALTHY';
  }
  
  // Generate recommendations
  const recommendations: string[] = [];
  const slowEndpoints = results.filter(r => r.success && r.duration > 2000);
  const failedEndpoints_list = results.filter(r => !r.success);
  
  if (slowEndpoints.length > 0) {
    recommendations.push(`${slowEndpoints.length} endpoints are responding slowly (>2s). Consider optimization.`);
  }
  
  if (failedEndpoints_list.length > 0) {
    recommendations.push(`${failedEndpoints_list.length} endpoints are failing. Check API connectivity and server status.`);
  }
  
  if (successRate < 100) {
    recommendations.push('Some endpoints are not functioning properly. Review error logs for details.');
  }
  
  if (recommendations.length === 0) {
    recommendations.push('All endpoints are functioning optimally! 🎉');
  }
  
  const validationSuite: ValidationSuite = {
    totalEndpoints: results.length,
    passedEndpoints,
    failedEndpoints,
    results,
    overallHealth,
    totalDuration,
    recommendations
  };
  
  // Log comprehensive summary
  logger.info('🏁 Endpoint validation suite completed', 'ENDPOINT_VALIDATOR', {
    totalEndpoints: validationSuite.totalEndpoints,
    passed: validationSuite.passedEndpoints,
    failed: validationSuite.failedEndpoints,
    successRate: `${Math.round(successRate)}%`,
    overallHealth: validationSuite.overallHealth,
    totalDuration: validationSuite.totalDuration,
    recommendations: validationSuite.recommendations
  });
  
  return validationSuite;
};

// Test build-specific endpoints (requires valid build data)
export const validateBuildEndpoints = async (jobName: string, buildId: number): Promise<EndpointTestResult[]> => {
  logger.info(`🔧 Validating build-specific endpoints for ${jobName}#${buildId}`, 'ENDPOINT_VALIDATOR');
  
  const buildTests = [
    {
      name: `Build Summary - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getBuildSummary(jobName, buildId),
      validator: (data: any) => data && data.jobName && data.buildId
    },
    {
      name: `Risk Score - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getRiskScore(jobName, buildId),
      validator: (data: any) => data && typeof data.riskScore === 'number'
    },
    {
      name: `Detected Anomalies - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getDetectedAnomalies(jobName, buildId, 1, 3),
      validator: (data: any) => data && Array.isArray(data.anomalies)
    },
    {
      name: `AI Insights - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getAIInsights(jobName, buildId),
      validator: (data: any) => data && (data.summary || data.recommendation || data.recommendations)
    },
    {
      name: `Logs Tracker - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getLogsTracker(jobName, buildId),
      validator: (data: any) => Array.isArray(data)
    },
    {
      name: `Build Logs - ${jobName}#${buildId}`,
      method: 'GET',
      testFn: () => dashboardApi.getBuildLogs(jobName, buildId, 0, 10),
      validator: (data: any) => Array.isArray(data)
    }
  ];
  
  const results: EndpointTestResult[] = [];
  
  for (const test of buildTests) {
    const result = await testEndpoint(test.name, test.method, test.testFn, test.validator);
    results.push(result);
    
    // Small delay between tests
    await new Promise(resolve => setTimeout(resolve, 200));
  }
  
  return results;
};

// Quick health check for critical endpoints only
export const quickHealthCheck = async (): Promise<{
  healthy: boolean;
  criticalEndpointsStatus: { [key: string]: boolean };
  responseTime: number;
}> => {
  const startTime = Date.now();
  logger.info('⚡ Running quick health check on critical endpoints', 'ENDPOINT_VALIDATOR');
  
  const criticalEndpoints = {
    'Recent Job Builds': () => dashboardApi.getRecentJobBuilds(),
    'Security Anomalies': () => dashboardApi.getSecurityAnomalies(),
    'Active Builds': () => dashboardApi.getActiveBuilds(),
    'Job Names': () => dashboardApi.getAllJobNames()
  };
  
  const status: { [key: string]: boolean } = {};
  
  try {
    const results = await Promise.allSettled(
      Object.entries(criticalEndpoints).map(async ([name, fn]) => {
        try {
          await fn();
          status[name] = true;
          return true;
        } catch (error) {
          status[name] = false;
          logger.warn(`Critical endpoint failed: ${name}`, 'ENDPOINT_VALIDATOR', {
            error: error instanceof Error ? error.message : error
          });
          return false;
        }
      })
    );
    
    const healthyCount = results.filter(r => r.status === 'fulfilled' && r.value).length;
    const healthy = healthyCount === results.length;
    const responseTime = Date.now() - startTime;
    
    logger.info(`⚡ Quick health check completed`, 'ENDPOINT_VALIDATOR', {
      healthy,
      healthyEndpoints: healthyCount,
      totalEndpoints: results.length,
      responseTime
    });
    
    return {
      healthy,
      criticalEndpointsStatus: status,
      responseTime
    };
  } catch (error) {
    logger.error('❌ Quick health check failed', 'ENDPOINT_VALIDATOR', {
      error: error instanceof Error ? error.message : error
    });
    
    return {
      healthy: false,
      criticalEndpointsStatus: status,
      responseTime: Date.now() - startTime
    };
  }
};

// Make available globally in development for manual testing
if (process.env.NODE_ENV === 'development') {
  (window as any).endpointValidator = {
    validateAllEndpoints,
    validateBuildEndpoints,
    quickHealthCheck
  };
}

export default {
  validateAllEndpoints,
  validateBuildEndpoints,
  quickHealthCheck
};
