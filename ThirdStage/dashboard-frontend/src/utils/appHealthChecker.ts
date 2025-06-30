// Comprehensive application health checker
import { validateAllEndpoints, quickHealthCheck } from './endpointValidator';
import { logger } from './logger';

export interface ComponentHealthCheck {
  component: string;
  healthy: boolean;
  issues: string[];
  recommendations: string[];
}

export interface AppHealthReport {
  overallHealth: 'HEALTHY' | 'DEGRADED' | 'CRITICAL';
  timestamp: string;
  components: ComponentHealthCheck[];
  apiHealth: {
    healthy: boolean;
    endpointsPassed: number;
    endpointsTotal: number;
    responseTime: number;
  };
  performance: {
    memoryUsage?: number;
    renderTime?: number;
    bundleSize?: number;
  };
  recommendations: string[];
  score: number; // 0-100
}

// Check individual component health
const checkComponentHealth = (componentName: string): ComponentHealthCheck => {
  const issues: string[] = [];
  const recommendations: string[] = [];
  
  try {
    // Check if component exists in DOM
    const elements = document.querySelectorAll(`[data-component="${componentName}"]`);
    if (elements.length === 0) {
      issues.push(`Component ${componentName} not found in DOM`);
      recommendations.push(`Ensure ${componentName} component is properly rendered`);
    }
    
    // Check for error boundaries
    const errorElements = document.querySelectorAll('.error-boundary');
    if (errorElements.length > 0) {
      issues.push('Error boundaries detected');
      recommendations.push('Check console for React errors');
    }
    
    // Check for loading states
    const loadingElements = document.querySelectorAll('.loading, .spinner');
    if (loadingElements.length > 5) {
      issues.push('Multiple loading states detected');
      recommendations.push('Check for stuck loading states');
    }
    
    return {
      component: componentName,
      healthy: issues.length === 0,
      issues,
      recommendations
    };
  } catch (error) {
    return {
      component: componentName,
      healthy: false,
      issues: [`Failed to check component: ${error}`],
      recommendations: ['Investigate component health check failure']
    };
  }
};

// Check performance metrics
const checkPerformanceMetrics = (): AppHealthReport['performance'] => {
  const performance: AppHealthReport['performance'] = {};
  
  try {
    // Memory usage (if available)
    if ('memory' in performance && (performance as any).memory) {
      const memInfo = (performance as any).memory;
      performance.memoryUsage = memInfo.usedJSHeapSize / (1024 * 1024); // MB
    }
    
    // Render time from performance API
    const navigationTiming = window.performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
    if (navigationTiming) {
      performance.renderTime = navigationTiming.loadEventEnd - navigationTiming.navigationStart;
    }
    
    // Bundle size estimation (rough)
    const scripts = document.querySelectorAll('script[src]');
    let estimatedBundleSize = 0;
    scripts.forEach(script => {
      // This is a rough estimation - in real apps you'd get this from build tools
      estimatedBundleSize += 100; // KB per script (rough estimate)
    });
    performance.bundleSize = estimatedBundleSize;
    
  } catch (error) {
    logger.warn('Failed to collect performance metrics', 'APP_HEALTH_CHECKER', { error });
  }
  
  return performance;
};

// Comprehensive application health check
export const runAppHealthCheck = async (): Promise<AppHealthReport> => {
  const startTime = Date.now();
  logger.info('🏥 Starting comprehensive application health check', 'APP_HEALTH_CHECKER');
  
  // Check API health
  const apiHealthResult = await quickHealthCheck();
  
  // Check component health
  const criticalComponents = [
    'Dashboard',
    'SummaryStats', 
    'RecentJobBuilds',
    'SeverityDistributionChart',
    'AnomaliesTrendChart'
  ];
  
  const componentChecks = criticalComponents.map(checkComponentHealth);
  
  // Check performance
  const performanceMetrics = checkPerformanceMetrics();
  
  // Calculate overall health score
  let score = 100;
  
  // API health impact (40% of score)
  if (!apiHealthResult.healthy) {
    const apiFailureRate = 1 - (Object.values(apiHealthResult.criticalEndpointsStatus).filter(Boolean).length / Object.keys(apiHealthResult.criticalEndpointsStatus).length);
    score -= apiFailureRate * 40;
  }
  
  // Component health impact (30% of score)
  const unhealthyComponents = componentChecks.filter(c => !c.healthy).length;
  if (unhealthyComponents > 0) {
    score -= (unhealthyComponents / componentChecks.length) * 30;
  }
  
  // Performance impact (30% of score)
  if (performanceMetrics.renderTime && performanceMetrics.renderTime > 3000) {
    score -= 15; // Slow render time
  }
  if (performanceMetrics.memoryUsage && performanceMetrics.memoryUsage > 100) {
    score -= 15; // High memory usage
  }
  
  score = Math.max(0, Math.round(score));
  
  // Determine overall health
  let overallHealth: AppHealthReport['overallHealth'];
  if (score >= 90) {
    overallHealth = 'HEALTHY';
  } else if (score >= 70) {
    overallHealth = 'DEGRADED';
  } else {
    overallHealth = 'CRITICAL';
  }
  
  // Generate recommendations
  const recommendations: string[] = [];
  
  if (!apiHealthResult.healthy) {
    recommendations.push('API connectivity issues detected. Check backend services.');
  }
  
  if (unhealthyComponents > 0) {
    recommendations.push(`${unhealthyComponents} components have issues. Check component rendering.`);
  }
  
  if (performanceMetrics.renderTime && performanceMetrics.renderTime > 3000) {
    recommendations.push('Application is loading slowly. Consider performance optimization.');
  }
  
  if (performanceMetrics.memoryUsage && performanceMetrics.memoryUsage > 100) {
    recommendations.push('High memory usage detected. Check for memory leaks.');
  }
  
  if (score === 100) {
    recommendations.push('Application is running optimally! 🎉');
  }
  
  const healthReport: AppHealthReport = {
    overallHealth,
    timestamp: new Date().toISOString(),
    components: componentChecks,
    apiHealth: {
      healthy: apiHealthResult.healthy,
      endpointsPassed: Object.values(apiHealthResult.criticalEndpointsStatus).filter(Boolean).length,
      endpointsTotal: Object.keys(apiHealthResult.criticalEndpointsStatus).length,
      responseTime: apiHealthResult.responseTime
    },
    performance: performanceMetrics,
    recommendations,
    score
  };
  
  const duration = Date.now() - startTime;
  
  // Log comprehensive health report
  logger.info('🏥 Application health check completed', 'APP_HEALTH_CHECKER', {
    overallHealth: healthReport.overallHealth,
    score: healthReport.score,
    apiHealthy: healthReport.apiHealth.healthy,
    componentsHealthy: componentChecks.filter(c => c.healthy).length,
    totalComponents: componentChecks.length,
    duration,
    recommendations: healthReport.recommendations
  });
  
  return healthReport;
};

// Continuous health monitoring
export class AppHealthMonitor {
  private intervalId: NodeJS.Timeout | null = null;
  private isMonitoring = false;
  
  start(intervalMs: number = 30000) { // Default: 30 seconds
    if (this.isMonitoring) {
      logger.warn('Health monitor is already running', 'APP_HEALTH_MONITOR');
      return;
    }
    
    logger.info('🔄 Starting continuous health monitoring', 'APP_HEALTH_MONITOR', { intervalMs });
    
    this.isMonitoring = true;
    this.intervalId = setInterval(async () => {
      try {
        const healthReport = await runAppHealthCheck();
        
        // Alert on critical issues
        if (healthReport.overallHealth === 'CRITICAL') {
          logger.error('🚨 CRITICAL: Application health is critical!', 'APP_HEALTH_MONITOR', {
            score: healthReport.score,
            issues: healthReport.components.filter(c => !c.healthy).map(c => c.component)
          });
        } else if (healthReport.overallHealth === 'DEGRADED') {
          logger.warn('⚠️ WARNING: Application health is degraded', 'APP_HEALTH_MONITOR', {
            score: healthReport.score,
            recommendations: healthReport.recommendations
          });
        }
        
      } catch (error) {
        logger.error('Failed to run health check', 'APP_HEALTH_MONITOR', { error });
      }
    }, intervalMs);
  }
  
  stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      this.isMonitoring = false;
      logger.info('🛑 Stopped continuous health monitoring', 'APP_HEALTH_MONITOR');
    }
  }
  
  isRunning(): boolean {
    return this.isMonitoring;
  }
}

// Create global health monitor instance
export const appHealthMonitor = new AppHealthMonitor();

// Make available globally in development
if (process.env.NODE_ENV === 'development') {
  (window as any).appHealthChecker = {
    runAppHealthCheck,
    appHealthMonitor,
    startMonitoring: (interval?: number) => appHealthMonitor.start(interval),
    stopMonitoring: () => appHealthMonitor.stop()
  };
}

export default {
  runAppHealthCheck,
  AppHealthMonitor,
  appHealthMonitor
};
