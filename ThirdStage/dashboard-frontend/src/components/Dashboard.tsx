import React, { useState, useEffect, useRef, useCallback } from 'react';
import type {RecentJobBuildDTO, SecurityAnomalyCountDTO, ActiveBuildCountDTO, JobCountDTO } from '../services/api';
import { dashboardApi } from '../services/api';
import ErrorBoundary from './ErrorBoundary';
import {
  REFRESH_INTERVALS,
  TIME_RANGE_MAP,
  SECURITY_TIME_RANGE_MAP,
  ERROR_MESSAGES,
  SUCCESS_MESSAGES
} from '../constants/dashboard';
import { ARIA_LABELS, ARIA_DESCRIPTIONS, logger } from '../utils';
import { runComprehensiveTests } from '../utils/testRunner';
import { debugDOMElements } from '../utils/domDebugger';
import PerformanceMonitor from './PerformanceMonitor';
import SummaryStats from './SummaryStats';
import RecentJobBuilds from './RecentJobBuilds';
import BuildDetails from './BuildDetails';
import AnomaliesTable from './AnomaliesTable';
import JobExplorer from './JobExplorer';
import RiskScoreGauge from './charts/RiskScoreGauge';
import AnomaliesTrendChart from './charts/AnomaliesTrendChart';
import SeverityDistributionChart from './charts/SeverityDistributionChart';
import AIInsightsPanel from './AIInsightsPanel';
import NotificationContainer from './NotificationContainer';
import { useNotifications } from '../hooks/useNotifications';

const Dashboard: React.FC = () => {
  // Notification system
  const { notifications, removeNotification, showSuccess, showError } = useNotifications();

  // State management
  const [darkMode, setDarkMode] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Data state
  const [recentBuilds, setRecentBuilds] = useState<RecentJobBuildDTO[]>([]);
  const [securityAnomalies, setSecurityAnomalies] = useState<SecurityAnomalyCountDTO | null>(null);
  const [activeBuilds, setActiveBuilds] = useState<ActiveBuildCountDTO | null>(null);
  const [totalJobs, setTotalJobs] = useState<JobCountDTO | null>(null);
  const [selectedBuild, setSelectedBuild] = useState<RecentJobBuildDTO | null>(null);
  const [availableJobs, setAvailableJobs] = useState<string[]>([]);
  const [jobExplorerData, setJobExplorerData] = useState<{ job_name: string; last_build: string; status: string; anomalies: number }[]>([]);


  // Filters
  const [timeRange, setTimeRange] = useState('7 days');
  const [jobFilter, setJobFilter] = useState('all');

  // Performance monitoring (development only)
  const [performanceMonitorVisible, setPerformanceMonitorVisible] = useState(false);

  // Smooth refresh system state
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastRefreshTime, setLastRefreshTime] = useState<Date>(new Date());
  const [userInteracting, setUserInteracting] = useState(false);
  const refreshIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const userInteractionTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const scrollPositionRef = useRef<number>(0);

  // User interaction detection
  const handleUserInteraction = useCallback(() => {
    setUserInteracting(true);

    // Clear existing timeout
    if (userInteractionTimeoutRef.current) {
      clearTimeout(userInteractionTimeoutRef.current);
    }

    // Set timeout to mark user as not interacting after configured timeout
    userInteractionTimeoutRef.current = setTimeout(() => {
      setUserInteracting(false);
    }, REFRESH_INTERVALS.USER_INTERACTION_TIMEOUT);
  }, []);

  // Save scroll position
  const saveScrollPosition = useCallback(() => {
    scrollPositionRef.current = window.scrollY;
  }, []);

  // Restore scroll position
  const restoreScrollPosition = useCallback(() => {
    if (scrollPositionRef.current > 0) {
      window.scrollTo({ top: scrollPositionRef.current, behavior: 'instant' });
    }
  }, []);

  // Load available job names
  const loadJobNames = async () => {
    try {
      const jobNames = await dashboardApi.getAllJobNames();
      logger.debug('Available job names loaded', 'DASHBOARD', { jobNames }, { component: 'Dashboard', action: 'loadJobNames' });
      setAvailableJobs(jobNames);
    } catch (error) {
      logger.error('Failed to load job names', 'DASHBOARD', { error }, { component: 'Dashboard', action: 'loadJobNames' });
    }
  };

  // Core refresh logic - shared between auto and manual refresh
  const performRefresh = async (isManual: boolean = false): Promise<void> => {
    const refreshType = isManual ? 'Manual' : 'Auto';

    try {
      setIsRefreshing(true);
      if (!isManual) {
        saveScrollPosition();
      }

      // Convert timeRange to API format using constants
      const apiTimeRange = TIME_RANGE_MAP[timeRange as keyof typeof TIME_RANGE_MAP] || TIME_RANGE_MAP['7 days'];
      const securityTimeRange = SECURITY_TIME_RANGE_MAP[timeRange as keyof typeof SECURITY_TIME_RANGE_MAP] || SECURITY_TIME_RANGE_MAP['7 days'];

      logger.info(`${refreshType} refresh started`, 'DASHBOARD', {
        refreshType,
        jobFilter,
        timeRange
      }, { component: 'Dashboard', action: 'performRefresh' });

      // Execute API calls with individual error handling
      const apiCalls = [
        (jobFilter === 'all'
          ? dashboardApi.getRecentJobBuilds()
          : dashboardApi.getRecentJobBuildsByName(jobFilter)
        ).catch(err => {
          logger.error('Failed to fetch recent builds', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          throw new Error('Recent builds data unavailable');
        }),
        (jobFilter === 'all'
          ? dashboardApi.getSecurityAnomalies()
          : dashboardApi.getSecurityAnomaliesByFilter(jobFilter, securityTimeRange)
        ).catch(err => {
          logger.error('Failed to fetch security anomalies', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          throw new Error('Security anomalies data unavailable');
        }),
        (jobFilter === 'all'
          ? dashboardApi.getActiveBuilds()
          : dashboardApi.getActiveBuildsByFilter(jobFilter)
        ).catch(err => {
          logger.error('Failed to fetch active builds', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          throw new Error('Active builds data unavailable');
        }),
        dashboardApi.getTotalJobs(apiTimeRange).catch(err => {
          logger.error('Failed to fetch total jobs', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          throw new Error('Total jobs data unavailable');
        }),
        // ✅ FIX: Include job names in auto-refresh to keep dropdown up-to-date
        dashboardApi.getAllJobNames().catch(err => {
          logger.error('Failed to fetch job names', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          // Don't throw error for job names - use existing data as fallback
          return availableJobs; // Return current job names if API fails
        }),
        // ✅ FIX: Include JobExplorer data in auto-refresh cycle
        dashboardApi.getJobExplorer('all').catch(err => {
          logger.error('Failed to fetch job explorer data', 'DASHBOARD', { error: err }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
          // Don't throw error for job explorer - use existing data as fallback
          return jobExplorerData; // Return current job explorer data if API fails
        })
      ];

      const [buildsData, securityData, activeData, jobsData, jobNamesData, jobExplorerApiData] = await Promise.all(apiCalls);

      // Update state - always update data to keep it fresh every 30 seconds
      // Use smooth updates that don't disrupt user experience
      setRecentBuilds(buildsData as RecentJobBuildDTO[]);
      setSecurityAnomalies(securityData as SecurityAnomalyCountDTO);
      setActiveBuilds(activeData as ActiveBuildCountDTO);
      setTotalJobs(jobsData as JobCountDTO);

      // ✅ FIX: Update job names to keep dropdown current
      if (Array.isArray(jobNamesData) && jobNamesData.length > 0) {
        // Ensure jobNamesData is string[] (from API) not RecentJobBuildDTO[] (fallback)
        const jobNames = jobNamesData.filter((item): item is string => typeof item === 'string');
        if (jobNames.length > 0) {
          setAvailableJobs(jobNames);
          logger.debug('Job names updated during refresh', 'DASHBOARD', {
            newJobCount: jobNames.length,
            jobNames: jobNames
          }, { component: 'Dashboard', action: 'performRefresh' });
        }
      }

      // ✅ FIX: Update JobExplorer data to keep job status current
      if (Array.isArray(jobExplorerApiData)) {
        // Type guard to ensure we have the correct structure
        const isValidJobExplorerData = (data: any[]): data is { job_name: string; last_build: string; status: string; anomalies: number }[] => {
          return data.every(item =>
            typeof item === 'object' &&
            'job_name' in item && typeof item.job_name === 'string' &&
            'last_build' in item && typeof item.last_build === 'string' &&
            'status' in item && typeof item.status === 'string' &&
            'anomalies' in item && typeof item.anomalies === 'number'
          );
        };

        if (isValidJobExplorerData(jobExplorerApiData)) {
          setJobExplorerData(jobExplorerApiData);
          logger.debug('JobExplorer data updated during refresh', 'DASHBOARD', {
            jobCount: jobExplorerApiData.length,
            jobs: jobExplorerApiData.map(job => ({ name: job.job_name, status: job.status, anomalies: job.anomalies }))
          }, { component: 'Dashboard', action: 'performRefresh' });
        } else {
          logger.warn('Invalid JobExplorer data structure received', 'DASHBOARD', { jobExplorerApiData }, {
            component: 'Dashboard',
            action: 'performRefresh'
          });
        }
      }

      // Log update type for debugging
      logger.debug(`Dashboard data updated (${isManual ? 'manual' : 'auto'})`, 'DASHBOARD', {
        buildsCount: Array.isArray(buildsData) ? buildsData.length : 0,
        securityAnomalies: (securityData as SecurityAnomalyCountDTO)?.anomalyCount || 0,
        activeBuilds: (activeData as ActiveBuildCountDTO)?.activeBuilds || 0,
        totalJobs: (jobsData as JobCountDTO)?.totalJobs || 0,
        availableJobsCount: availableJobs.length,
        jobExplorerJobsCount: jobExplorerData.length,
        userInteracting
      }, { component: 'Dashboard', action: 'performRefresh' });

      setLastRefreshTime(new Date());
      setError(null); // Clear any previous errors

      // Restore scroll position after a brief delay (only for auto refresh)
      if (!isManual) {
        setTimeout(restoreScrollPosition, REFRESH_INTERVALS.SCROLL_RESTORE_DELAY);
      }

      logger.info(`${refreshType} refresh completed successfully`, 'DASHBOARD', undefined, {
        component: 'Dashboard',
        action: 'performRefresh'
      });

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      logger.error(`Failed to ${refreshType.toLowerCase()} refresh dashboard data`, 'DASHBOARD', {
        error: err,
        errorMessage
      }, { component: 'Dashboard', action: 'performRefresh' });

      // Set error state for UI feedback
      setError(`Failed to refresh dashboard: ${errorMessage}`);

      // For manual refresh, show user notification
      if (isManual) {
        throw err; // Re-throw to be handled by manualRefreshDashboardData
      }
    } finally {
      setIsRefreshing(false);
    }
  };

  // Auto refresh version - always updates every 30 seconds regardless of user interaction
  const refreshDashboardData = async () => {
    logger.debug('Auto-refresh triggered', 'DASHBOARD', {
      userInteracting,
      interval: '30 seconds'
    }, {
      component: 'Dashboard',
      action: 'refreshDashboardData'
    });

    // Always perform refresh - user interaction no longer blocks updates
    await performRefresh(false);
  };

  // Manual refresh version - always works regardless of user interaction
  const manualRefreshDashboardData = async () => {
    try {
      logger.info('Manual refresh triggered by user', 'DASHBOARD', undefined, { component: 'Dashboard', action: 'manualRefresh' });
      await performRefresh(true);
      showSuccess('Dashboard Refreshed', SUCCESS_MESSAGES.DASHBOARD_REFRESHED);
    } catch (error) {
      logger.error('Manual refresh failed', 'DASHBOARD', { error }, { component: 'Dashboard', action: 'manualRefresh' });
      showError('Refresh Failed', ERROR_MESSAGES.REFRESH_FAILED);

      // Re-enable the button by ensuring isRefreshing is false
      setIsRefreshing(false);
    }
  };

  // Load dashboard data (initial load)
  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Convert timeRange to API format using constants
      const apiTimeRange = TIME_RANGE_MAP[timeRange as keyof typeof TIME_RANGE_MAP] || TIME_RANGE_MAP['7 days'];
      const securityTimeRange = SECURITY_TIME_RANGE_MAP[timeRange as keyof typeof SECURITY_TIME_RANGE_MAP] || SECURITY_TIME_RANGE_MAP['7 days'];

      const [buildsData, securityData, activeData, jobsData, initialJobExplorerData] = await Promise.all([
        jobFilter === 'all'
          ? dashboardApi.getRecentJobBuilds()
          : dashboardApi.getRecentJobBuildsByName(jobFilter),
        jobFilter === 'all'
          ? dashboardApi.getSecurityAnomalies()
          : dashboardApi.getSecurityAnomaliesByFilter(jobFilter, securityTimeRange),
        jobFilter === 'all'
          ? dashboardApi.getActiveBuilds()
          : dashboardApi.getActiveBuildsByFilter(jobFilter),
        dashboardApi.getTotalJobs(apiTimeRange), // Total jobs uses the selected time range
        dashboardApi.getJobExplorer('all') // ✅ FIX: Load JobExplorer data on initial load
      ]);

      logger.debug('Raw API responses loaded', 'DASHBOARD', {
        buildsData: buildsData?.length || 0,
        securityData,
        activeData,
        jobsData
      }, { component: 'Dashboard', action: 'loadDashboardData' });

      setRecentBuilds(buildsData as RecentJobBuildDTO[]);
      setSecurityAnomalies(securityData as SecurityAnomalyCountDTO);
      setActiveBuilds(activeData as ActiveBuildCountDTO);
      setTotalJobs(jobsData as JobCountDTO);

      // ✅ FIX: Set initial JobExplorer data
      if (Array.isArray(initialJobExplorerData)) {
        setJobExplorerData(initialJobExplorerData as { job_name: string; last_build: string; status: string; anomalies: number }[]);
      }

      // Auto-select the latest build if available and no build is currently selected
      if (buildsData && buildsData.length > 0 && !selectedBuild) {
        // Find the latest build (highest buildId)
        const latestBuild = buildsData.reduce((latest, current) => {
          return current.buildId > latest.buildId ? current : latest;
        });

        logger.debug('Auto-selecting latest build', 'DASHBOARD', {
          buildId: latestBuild.buildId,
          jobName: latestBuild.originalJobName
        }, { component: 'Dashboard', action: 'autoSelectBuild' });
        setSelectedBuild(latestBuild);
      }
      setLastRefreshTime(new Date());
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      logger.error('Failed to load dashboard data', 'DASHBOARD', {
        error: err,
        errorMessage
      }, { component: 'Dashboard', action: 'loadDashboardData' });
      setError(`Failed to load dashboard data: ${errorMessage}`);
      showError('Load Failed', 'Failed to load dashboard data. Please check your API connection.');
    } finally {
      setLoading(false);
    }
  };

  // Initialize dashboard - Load job names once on mount
  useEffect(() => {
    loadJobNames();
  }, []);

  // Handle data loading and auto-refresh with proper cleanup
  useEffect(() => {
    // Clear any existing interval
    if (refreshIntervalRef.current) {
      clearInterval(refreshIntervalRef.current);
      refreshIntervalRef.current = null;
    }

    // Load initial data
    loadDashboardData();

    // Set up auto-refresh interval
    refreshIntervalRef.current = setInterval(() => {
      refreshDashboardData();
    }, REFRESH_INTERVALS.AUTO_REFRESH);

    // Cleanup function
    return () => {
      if (refreshIntervalRef.current) {
        clearInterval(refreshIntervalRef.current);
        refreshIntervalRef.current = null;
      }
    };
  }, [jobFilter, timeRange]); // Restart when filters change

  // Cleanup on component unmount
  useEffect(() => {
    return () => {
      if (refreshIntervalRef.current) {
        clearInterval(refreshIntervalRef.current);
      }
      if (userInteractionTimeoutRef.current) {
        clearTimeout(userInteractionTimeoutRef.current);
      }
    };
  }, []);

  // Set up user interaction listeners
  useEffect(() => {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

    events.forEach(event => {
      document.addEventListener(event, handleUserInteraction, { passive: true });
    });

    return () => {
      events.forEach(event => {
        document.removeEventListener(event, handleUserInteraction);
      });
    };
  }, [handleUserInteraction]);

  // Theme management
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [darkMode]);

  const toggleTheme = () => {
    setDarkMode(!darkMode);
  };

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center loading-fade-enter-active">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto gpu-accelerated"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400 content-fade-enter-active">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <i className="fas fa-exclamation-triangle text-red-500 text-5xl mx-auto"></i>
          <p className="mt-4 text-red-600 dark:text-red-400">{error}</p>
          <button
            onClick={loadDashboardData}
            className="mt-4 bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded text-sm btn-primary-animated transition-all"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100 page-transition-enter-active" data-component="Dashboard">
      {/* Header */}
      <header
        className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700 h-16 flex items-center justify-between px-4 md:px-6"
        role="banner"
        aria-label={ARIA_LABELS.MAIN_NAVIGATION}
      >
        <div className="flex items-center">
          <button
            onClick={toggleSidebar}
            className="mr-2 md:hidden text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 focus-visible:focus"
            aria-label={ARIA_LABELS.SIDEBAR_TOGGLE}
            aria-expanded={sidebarOpen}
            aria-controls="main-sidebar"
          >
            {sidebarOpen ? <i className="fas fa-times" aria-hidden="true"></i> : <i className="fas fa-bars" aria-hidden="true"></i>}
          </button>
          <div className="flex items-center">
            <i className="fas fa-shield-alt text-blue-500 text-xl mr-2" aria-hidden="true"></i>
            <h1 className="text-xl font-bold">DevSecOps AI</h1>
            {isRefreshing && (
              <div className="ml-3 flex items-center text-xs text-gray-500 dark:text-gray-400" role="status" aria-live="polite">
                <div className="animate-spin rounded-full h-3 w-3 border border-blue-500 border-t-transparent mr-1" aria-hidden="true"></div>
                <span>Updating...</span>
              </div>
            )}
          </div>
        </div>
        
        <div className="flex items-center space-x-4">
          <div className="hidden md:flex items-center space-x-4">
            <div className="relative">
              <label htmlFor="time-range-select" className="sr-only">
                {ARIA_LABELS.TIME_RANGE_FILTER}
              </label>
              <select
                id="time-range-select"
                value={timeRange}
                onChange={(e) => setTimeRange(e.target.value)}
                className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-md py-1 pl-3 pr-8 text-sm focus:outline-none focus-visible:focus"
                aria-label={ARIA_LABELS.TIME_RANGE_FILTER}
              >
                <option value="7 days">Last 7 days</option>
                <option value="14 days">Last 14 days</option>
                <option value="30 days">Last 30 days</option>
                <option value="60 days">Last 60 days</option>
                <option value="180 days">Last 180 days</option>
                <option value="all time">All time</option>
              </select>
            </div>

            <div className="relative">
              <label htmlFor="job-filter-select" className="sr-only">
                {ARIA_LABELS.JOB_FILTER}
              </label>
              <select
                id="job-filter-select"
                value={jobFilter}
                onChange={(e) => setJobFilter(e.target.value)}
                className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-md py-1 pl-3 pr-8 text-sm focus:outline-none focus-visible:focus filter-transition"
                aria-label={ARIA_LABELS.JOB_FILTER}
              >
                <option value="all">All Jobs</option>
                {availableJobs.map((jobName) => (
                  <option key={jobName} value={jobName}>
                    {jobName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="relative">
            <label htmlFor="search-input" className="sr-only">
              {ARIA_LABELS.SEARCH_INPUT}
            </label>
            <input
              id="search-input"
              type="text"
              placeholder="Search builds or jobs..."
              className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-md py-1 pl-8 pr-4 text-sm w-48 md:w-64 focus:outline-none focus:ring-2 focus:ring-blue-500 focus-visible:focus transition-all"
              aria-label={ARIA_LABELS.SEARCH_INPUT}
            />
            <i className="fas fa-search text-gray-400 absolute left-2 top-1/2 transform -translate-y-1/2" aria-hidden="true"></i>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={manualRefreshDashboardData}
              disabled={isRefreshing}
              className="bg-gray-100 dark:bg-gray-700 p-2 rounded-full text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-100 disabled:opacity-50 btn-icon-animated transition-all focus-visible:focus"
              aria-label={ARIA_LABELS.REFRESH_BUTTON}
              title={`Click to refresh now. Last updated: ${lastRefreshTime.toLocaleTimeString()}`}
            >
              <i className={`fas fa-sync-alt ${isRefreshing ? 'animate-spin' : ''} gpu-accelerated`} aria-hidden="true"></i>
              <span className="sr-only">
                {isRefreshing ? 'Refreshing dashboard data' : 'Refresh dashboard data'}
              </span>
            </button>

            <button
              onClick={toggleTheme}
              className="bg-gray-100 dark:bg-gray-700 p-1 rounded-full text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-100 btn-icon-animated transition-all focus-visible:focus"
              aria-label={ARIA_LABELS.THEME_TOGGLE}
              aria-pressed={darkMode}
            >
              {darkMode ? <i className="fas fa-sun transition-all" aria-hidden="true"></i> : <i className="fas fa-moon transition-all" aria-hidden="true"></i>}
              <span className="sr-only">
                {darkMode ? 'Switch to light mode' : 'Switch to dark mode'}
              </span>
            </button>

            {/* Development Testing Panel */}
            {process.env.NODE_ENV === 'development' && (
              <div className="relative">
                <button
                  onClick={async () => {
                    try {
                      logger.info('🧪 Running comprehensive test suite...', 'DASHBOARD');
                      const testResults = await runComprehensiveTests();
                      console.log('🎯 Test Results:', testResults);

                      if (testResults.overallPassed) {
                        showSuccess('Tests Passed', `All ${testResults.summary.totalTests} tests passed! 🎉`);
                      } else {
                        showError('Tests Failed', `${testResults.summary.failedTests} of ${testResults.summary.totalTests} tests failed.`);
                      }
                    } catch (error) {
                      logger.error('Test suite failed', 'DASHBOARD', { error });
                      showError('Test Error', 'Failed to run test suite');
                    }
                  }}
                  className="bg-green-100 dark:bg-green-800 p-1 rounded-full text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-100 focus-visible:focus"
                  title="Run comprehensive test suite (Development only)"
                >
                  <i className="fas fa-flask" aria-hidden="true"></i>
                  <span className="sr-only">Run tests</span>
                </button>

                <button
                  onClick={() => {
                    debugDOMElements();
                    showSuccess('Debug Complete', 'Check browser console for DOM debug report');
                  }}
                  className="bg-blue-100 dark:bg-blue-800 p-1 rounded-full text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-100 focus-visible:focus ml-2"
                  title="Debug DOM elements (Development only)"
                >
                  <i className="fas fa-bug" aria-hidden="true"></i>
                  <span className="sr-only">Debug DOM</span>
                </button>

                <button
                  onClick={() => setPerformanceMonitorVisible(!performanceMonitorVisible)}
                  className={`p-1 rounded-full focus-visible:focus ml-2 ${
                    performanceMonitorVisible
                      ? 'bg-orange-100 dark:bg-orange-800 text-orange-600 dark:text-orange-400'
                      : 'bg-purple-100 dark:bg-purple-800 text-purple-600 dark:text-purple-400'
                  } hover:text-purple-900 dark:hover:text-purple-100`}
                  title="Toggle chart performance monitor (Development only)"
                >
                  <i className="fas fa-chart-line" aria-hidden="true"></i>
                  <span className="sr-only">Performance Monitor</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </header>
      
      <div className="flex">
        {/* Sidebar */}
        <aside
          id="main-sidebar"
          className={`sidebar ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0 fixed md:static inset-y-0 left-0 z-50 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 sidebar-transition py-4 px-2`}
          role="navigation"
          aria-label="Main navigation"
        >
          <nav className="space-y-1" role="list">
            <a
              href="#"
              className="flex items-center px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 dark:bg-blue-900 dark:text-blue-200 rounded-md focus-visible:focus"
              role="listitem"
              aria-current="page"
            >
              <i className="fas fa-chart-line w-5 h-5 mr-3" aria-hidden="true"></i>
              <span>Dashboard</span>
            </a>
            <a href="#" className="flex items-center px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-md transition-all hover-lift">
              <i className="fas fa-folder w-5 h-5 mr-3"></i>
              <span>All Jobs</span>
            </a>
            <a href="#" className="flex items-center px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-md transition-all hover-lift">
              <i className="fas fa-list-alt w-5 h-5 mr-3"></i>
              <span>Builds</span>
            </a>
            <a href="#" className="flex items-center px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-md transition-all hover-lift">
              <i className="fas fa-brain w-5 h-5 mr-3"></i>
              <span>AI Insights</span>
            </a>
            <a href="#" className="flex items-center px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-md transition-all hover-lift">
              <i className="fas fa-cog w-5 h-5 mr-3"></i>
              <span>Settings</span>
            </a>
          </nav>
          
          <div className="mt-8 px-2">
            <h3 className="px-4 mb-2 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
              Recent Jobs
            </h3>
            <div className="space-y-1">
              {(() => {
                logger.debug('Processing recent builds for sidebar', 'DASHBOARD', {
                  totalBuilds: recentBuilds.length
                }, { component: 'Dashboard', action: 'processSidebarBuilds' });

                // No filtering needed since all entries have job_name = 'all'
                // Just deduplicate by originalJobName and keep latest build
                const unique = recentBuilds.reduce((unique: typeof recentBuilds, build) => {
                  // Only keep the latest build per unique original_job_name
                  const existingJob = unique.find(u => u.originalJobName === build.originalJobName);

                  if (!existingJob || build.buildId > existingJob.buildId) {
                    const result = [...unique.filter(u => u.originalJobName !== build.originalJobName), build];
                    return result;
                  }
                  return unique;
                }, []);

                const final = unique.slice(0, 4);
                logger.debug('Sidebar builds processed', 'DASHBOARD', {
                  uniqueJobs: unique.length,
                  finalCount: final.length
                }, { component: 'Dashboard', action: 'processSidebarBuilds' });

                return final.map((build) => (
                <a
                  key={`${build.originalJobName}-${build.buildId}`}
                  href="#"
                  className="flex items-center px-4 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-md transition-all hover-lift"
                >
                  <span className={`w-2 h-2 rounded-full mr-3 ${
                    build.healthStatus === 'Healthy' ? 'bg-green-500' :
                    build.healthStatus === 'WARNING' ? 'bg-yellow-500' :
                    'bg-red-500'
                  }`}></span>
                  <span className="truncate">{build.originalJobName}</span>
                </a>
              ));
              })()}
            </div>
          </div>
        </aside>
        
        {/* Main Content */}
        <main
          className="main-content p-4 md:p-6 bg-gray-50 dark:bg-gray-900"
          role="main"
          aria-label="Dashboard content"
          aria-describedby="dashboard-description"
          data-component="DashboardMain"
        >
          <div className="mb-6">
            <div className="flex justify-between items-start">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100">Dashboard Overview</h2>
                <p id="dashboard-description" className="text-sm text-gray-500 dark:text-gray-400">
                  {ARIA_DESCRIPTIONS.DASHBOARD_OVERVIEW}
                </p>
              </div>
              <div className="text-right">
                <div className="flex items-center justify-end space-x-3 mb-2">
                  <button
                    onClick={manualRefreshDashboardData}
                    disabled={isRefreshing}
                    className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white px-3 py-1 rounded text-xs btn-primary-animated transition-all flex items-center space-x-1"
                    title="Click to refresh all dashboard data now"
                  >
                    <i className={`fas fa-sync-alt ${isRefreshing ? 'animate-spin' : ''} gpu-accelerated`}></i>
                    <span>{isRefreshing ? 'Refreshing...' : 'Refresh Now'}</span>
                  </button>
                </div>
                <div className="text-xs text-gray-400 dark:text-gray-500">
                  Last updated: {lastRefreshTime.toLocaleTimeString()}
                </div>
                <div className="text-xs text-green-500 dark:text-green-400 flex items-center justify-end mt-1">
                  <div className="w-2 h-2 bg-green-500 rounded-full mr-1 animate-pulse"></div>
                  Auto-refresh every 30s
                  {userInteracting && (
                    <span className="ml-2 text-blue-400 dark:text-blue-300">
                      (user active)
                    </span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Summary Stats */}
          <SummaryStats
            totalJobs={totalJobs}
            activeBuilds={activeBuilds}
            securityAnomalies={securityAnomalies}
            jobFilter={jobFilter}
            timeRange={timeRange}
          />

          {/* Recent Job Builds */}
          <RecentJobBuilds
            builds={recentBuilds}
            onBuildSelect={setSelectedBuild}
            jobFilter={jobFilter}
            timeRange={timeRange}
          />

          {/* Build Details and Risk Score */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6 items-stretch">
            <div className="lg:col-span-2 flex">
              <BuildDetails
                selectedBuild={selectedBuild}
                hasBuilds={recentBuilds.length > 0}
                onShowNotification={showSuccess}
                onShowError={showError}
              />
            </div>
            <div className="flex">
              <div className="chart-container filter-transition">
                <RiskScoreGauge
                  score={0}
                  previousScore={0}
                  selectedBuild={selectedBuild}
                  hasBuilds={recentBuilds.length > 0}
                />
              </div>
            </div>
          </div>

          {/* Anomalies Table */}
          <AnomaliesTable
            selectedBuild={selectedBuild}
            hasBuilds={recentBuilds.length > 0}
          />

          {/* Trends & Charts */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="chart-container filter-transition">
              <AnomaliesTrendChart
                jobFilter={jobFilter}
                timeRange={timeRange}
              />
            </div>
            <div className="chart-container filter-transition">
              <SeverityDistributionChart
                jobFilter={jobFilter}
                timeRange={timeRange}
              />
            </div>
          </div>

          {/* AI Insights Panel */}
          <AIInsightsPanel
            selectedBuild={selectedBuild}
            hasBuilds={recentBuilds.length > 0}
          />

          {/* Job Explorer */}
          <JobExplorer
            jobs={jobExplorerData}
            loading={isRefreshing}
          />
        </main>
      </div>
      
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 md:hidden modal-backdrop-enter-active transition-all"
          onClick={() => setSidebarOpen(false)}
          role="button"
          aria-label="Close sidebar"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Escape' || e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              setSidebarOpen(false);
            }
          }}
        ></div>
      )}

      {/* Notification Container */}
      <NotificationContainer
        notifications={notifications}
        onClose={removeNotification}
      />

      {/* Performance Monitor (Development only) */}
      {process.env.NODE_ENV === 'development' && (
        <PerformanceMonitor
          isVisible={performanceMonitorVisible}
          onClose={() => setPerformanceMonitorVisible(false)}
        />
      )}
      </div>
    </ErrorBoundary>
  );
};

export default Dashboard;

