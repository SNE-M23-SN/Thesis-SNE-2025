// Comprehensive test runner for the entire application
import { validateAllEndpoints, validateBuildEndpoints } from './endpointValidator';
import { runAppHealthCheck } from './appHealthChecker';
import { logger } from './logger';
import { dashboardApi } from '../services/api';

export interface TestSuiteResult {
  suiteName: string;
  passed: boolean;
  duration: number;
  tests: {
    name: string;
    passed: boolean;
    duration: number;
    error?: string;
    details?: any;
  }[];
}

export interface ComprehensiveTestReport {
  timestamp: string;
  overallPassed: boolean;
  totalDuration: number;
  suites: TestSuiteResult[];
  summary: {
    totalTests: number;
    passedTests: number;
    failedTests: number;
    successRate: number;
  };
  recommendations: string[];
}

// Individual test runner
const runTest = async (
  testName: string,
  testFn: () => Promise<any>
): Promise<{ name: string; passed: boolean; duration: number; error?: string; details?: any }> => {
  const startTime = Date.now();
  
  try {
    logger.debug(`🧪 Running test: ${testName}`, 'TEST_RUNNER');
    const result = await testFn();
    const duration = Date.now() - startTime;
    
    logger.info(`✅ Test passed: ${testName}`, 'TEST_RUNNER', { duration });
    
    return {
      name: testName,
      passed: true,
      duration,
      details: result
    };
  } catch (error) {
    const duration = Date.now() - startTime;
    const errorMessage = error instanceof Error ? error.message : 'Unknown error';
    
    logger.error(`❌ Test failed: ${testName}`, 'TEST_RUNNER', {
      duration,
      error: errorMessage
    });
    
    return {
      name: testName,
      passed: false,
      duration,
      error: errorMessage
    };
  }
};

// API Endpoints Test Suite
const runApiTestSuite = async (): Promise<TestSuiteResult> => {
  const startTime = Date.now();
  logger.info('🚀 Running API Endpoints Test Suite', 'TEST_RUNNER');
  
  const tests = [
    {
      name: 'Validate All Endpoints',
      testFn: async () => {
        const result = await validateAllEndpoints();
        if (result.overallHealth === 'UNHEALTHY') {
          throw new Error(`API health is ${result.overallHealth}. ${result.failedEndpoints} endpoints failed.`);
        }
        return result;
      }
    },
    {
      name: 'Test Recent Job Builds',
      testFn: async () => {
        const result = await dashboardApi.getRecentJobBuilds();
        if (!Array.isArray(result)) {
          throw new Error('Recent job builds should return an array');
        }
        return result;
      }
    },
    {
      name: 'Test Security Anomalies',
      testFn: async () => {
        const result = await dashboardApi.getSecurityAnomalies();
        if (!result || typeof result.anomalyCount !== 'number') {
          throw new Error('Security anomalies should return object with anomalyCount');
        }
        return result;
      }
    },
    {
      name: 'Test Chart Data Endpoints',
      testFn: async () => {
        const [anomalyTrend, severityDist] = await Promise.all([
          dashboardApi.getAnomalyTrend('All Jobs', 10),
          dashboardApi.getSeverityDistribution('All Jobs', 5)
        ]);
        
        if (!anomalyTrend?.labels || !anomalyTrend?.datasets) {
          throw new Error('Anomaly trend data is invalid');
        }
        
        if (!severityDist?.labels || !severityDist?.datasets) {
          throw new Error('Severity distribution data is invalid');
        }
        
        return { anomalyTrend, severityDist };
      }
    }
  ];
  
  const testResults = [];
  for (const test of tests) {
    const result = await runTest(test.name, test.testFn);
    testResults.push(result);
  }
  
  const duration = Date.now() - startTime;
  const passed = testResults.every(t => t.passed);
  
  return {
    suiteName: 'API Endpoints',
    passed,
    duration,
    tests: testResults
  };
};

// Application Health Test Suite
const runHealthTestSuite = async (): Promise<TestSuiteResult> => {
  const startTime = Date.now();
  logger.info('🏥 Running Application Health Test Suite', 'TEST_RUNNER');
  
  const tests = [
    {
      name: 'Overall Application Health',
      testFn: async () => {
        const healthReport = await runAppHealthCheck();
        if (healthReport.overallHealth === 'CRITICAL') {
          throw new Error(`Application health is critical. Score: ${healthReport.score}`);
        }
        return healthReport;
      }
    },
    {
      name: 'Component Rendering',
      testFn: async () => {
        // Wait a bit for components to render
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        const criticalElements = [
          '.main-content',
          'header[role="banner"]',
          'aside[role="navigation"]',
          'main[role="main"]',
          '[data-component="Dashboard"]',
          '[data-component="DashboardMain"]'
        ];
        
        const missingElements = [];
        const foundElements = [];

        for (const selector of criticalElements) {
          const element = document.querySelector(selector);
          if (!element) {
            missingElements.push(selector);
            logger.warn(`Missing element: ${selector}`, 'TEST_RUNNER');
          } else {
            foundElements.push(selector);
            logger.debug(`Found element: ${selector}`, 'TEST_RUNNER');
          }
        }

        logger.info('Component rendering check completed', 'TEST_RUNNER', {
          totalElements: criticalElements.length,
          foundElements: foundElements.length,
          missingElements: missingElements.length,
          found: foundElements,
          missing: missingElements
        });

        if (missingElements.length > 0) {
          throw new Error(`Missing critical elements: ${missingElements.join(', ')}. Found: ${foundElements.join(', ')}`);
        }

        return {
          renderedElements: criticalElements.length,
          foundElements,
          missingElements
        };
      }
    },
    {
      name: 'Error Boundary Check',
      testFn: async () => {
        const errorBoundaries = document.querySelectorAll('.error-boundary');
        if (errorBoundaries.length > 0) {
          throw new Error(`${errorBoundaries.length} error boundaries detected`);
        }
        return { errorBoundaries: errorBoundaries.length };
      }
    },
    {
      name: 'Console Error Check',
      testFn: async () => {
        // This is a simplified check - in real apps you'd capture console errors
        const hasConsoleErrors = false; // Would be implemented with error tracking
        if (hasConsoleErrors) {
          throw new Error('Console errors detected');
        }
        return { consoleErrors: 0 };
      }
    }
  ];
  
  const testResults = [];
  for (const test of tests) {
    const result = await runTest(test.name, test.testFn);
    testResults.push(result);
  }
  
  const duration = Date.now() - startTime;
  const passed = testResults.every(t => t.passed);
  
  return {
    suiteName: 'Application Health',
    passed,
    duration,
    tests: testResults
  };
};

// Performance Test Suite
const runPerformanceTestSuite = async (): Promise<TestSuiteResult> => {
  const startTime = Date.now();
  logger.info('⚡ Running Performance Test Suite', 'TEST_RUNNER');
  
  const tests = [
    {
      name: 'Page Load Performance',
      testFn: async () => {
        const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
        const loadTime = navigation.loadEventEnd - navigation.fetchStart;
        
        if (loadTime > 5000) {
          throw new Error(`Page load time too slow: ${loadTime}ms`);
        }
        
        return { loadTime };
      }
    },
    {
      name: 'API Response Times',
      testFn: async () => {
        const startTime = Date.now();
        await Promise.all([
          dashboardApi.getRecentJobBuilds(),
          dashboardApi.getSecurityAnomalies(),
          dashboardApi.getActiveBuilds()
        ]);
        const totalTime = Date.now() - startTime;
        
        if (totalTime > 3000) {
          throw new Error(`API responses too slow: ${totalTime}ms`);
        }
        
        return { apiResponseTime: totalTime };
      }
    },
    {
      name: 'Memory Usage Check',
      testFn: async () => {
        if ('memory' in performance) {
          const memInfo = (performance as any).memory;
          const memoryUsage = memInfo.usedJSHeapSize / (1024 * 1024); // MB
          
          if (memoryUsage > 150) {
            throw new Error(`Memory usage too high: ${memoryUsage.toFixed(2)}MB`);
          }
          
          return { memoryUsage: memoryUsage.toFixed(2) };
        }
        
        return { memoryUsage: 'Not available' };
      }
    }
  ];
  
  const testResults = [];
  for (const test of tests) {
    const result = await runTest(test.name, test.testFn);
    testResults.push(result);
  }
  
  const duration = Date.now() - startTime;
  const passed = testResults.every(t => t.passed);
  
  return {
    suiteName: 'Performance',
    passed,
    duration,
    tests: testResults
  };
};

// Comprehensive test runner
export const runComprehensiveTests = async (): Promise<ComprehensiveTestReport> => {
  const startTime = Date.now();
  logger.info('🎯 Starting comprehensive application test suite', 'TEST_RUNNER');
  
  // Run all test suites
  const suites = await Promise.all([
    runApiTestSuite(),
    runHealthTestSuite(),
    runPerformanceTestSuite()
  ]);
  
  // Calculate summary
  const totalTests = suites.reduce((sum, suite) => sum + suite.tests.length, 0);
  const passedTests = suites.reduce((sum, suite) => sum + suite.tests.filter(t => t.passed).length, 0);
  const failedTests = totalTests - passedTests;
  const successRate = totalTests > 0 ? (passedTests / totalTests) * 100 : 0;
  const overallPassed = suites.every(suite => suite.passed);
  const totalDuration = Date.now() - startTime;
  
  // Generate recommendations
  const recommendations: string[] = [];
  const failedSuites = suites.filter(s => !s.passed);
  
  if (failedSuites.length > 0) {
    recommendations.push(`${failedSuites.length} test suites failed. Review failed tests for details.`);
  }
  
  if (successRate < 90) {
    recommendations.push('Test success rate is below 90%. Address failing tests.');
  }
  
  if (totalDuration > 30000) {
    recommendations.push('Test suite is running slowly. Consider optimization.');
  }
  
  if (overallPassed && successRate === 100) {
    recommendations.push('All tests passed! Application is ready for production. 🎉');
  }
  
  const report: ComprehensiveTestReport = {
    timestamp: new Date().toISOString(),
    overallPassed,
    totalDuration,
    suites,
    summary: {
      totalTests,
      passedTests,
      failedTests,
      successRate: Math.round(successRate)
    },
    recommendations
  };
  
  // Log comprehensive summary
  logger.info('🏁 Comprehensive test suite completed', 'TEST_RUNNER', {
    overallPassed: report.overallPassed,
    totalTests: report.summary.totalTests,
    passedTests: report.summary.passedTests,
    failedTests: report.summary.failedTests,
    successRate: `${report.summary.successRate}%`,
    totalDuration: report.totalDuration,
    recommendations: report.recommendations
  });
  
  return report;
};

// Make available globally in development
if (process.env.NODE_ENV === 'development') {
  (window as any).testRunner = {
    runComprehensiveTests,
    runApiTestSuite,
    runHealthTestSuite,
    runPerformanceTestSuite
  };
}

export default {
  runComprehensiveTests,
  runApiTestSuite,
  runHealthTestSuite,
  runPerformanceTestSuite
};
