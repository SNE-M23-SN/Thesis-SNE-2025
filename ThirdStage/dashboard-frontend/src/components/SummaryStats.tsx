import React, { useState, useEffect, useMemo, useCallback } from 'react';
import type { JobCountDTO, ActiveBuildCountDTO, SecurityAnomalyCountDTO } from '../services/api';
import { dashboardApi } from '../services/api';
import { logger } from '../utils';
import { TIME_RANGE_MAP } from '../constants/dashboard';

interface SummaryStatsProps {
  totalJobs: JobCountDTO | null;
  activeBuilds: ActiveBuildCountDTO | null;
  securityAnomalies: SecurityAnomalyCountDTO | null;
  jobFilter: string;
  timeRange: string;
}

const SummaryStats: React.FC<SummaryStatsProps> = React.memo(({ totalJobs, activeBuilds, securityAnomalies, jobFilter, timeRange }) => {
  const [totalJobsChange, setTotalJobsChange] = useState<{ percentage: number; type: 'increase' | 'decrease' } | null>(null);
  const [securityAnomaliesChange, setSecurityAnomaliesChange] = useState<{ percentage: number; type: 'increase' | 'decrease' } | null>(null);

  // Memoize time range mappings for comparison periods
  const comparisonTimeRanges = useMemo(() => ({
    // For Total Jobs API (underscore format) - get double the period for comparison
    totalJobs: {
      '7 days': '14_days',
      '14 days': '30_days',
      '30 days': '60_days',
      '60 days': '180_days',
      '180 days': 'all_time',
      'all time': 'all_time'
    },
    // For Security Anomalies API (space format) - get double the period for comparison
    securityAnomalies: {
      '7 days': '14 days',
      '14 days': '30 days',
      '30 days': '60 days',
      '60 days': '180 days',
      '180 days': 'all time',
      'all time': 'all time'
    }
  }), []);

  // Generate dynamic change text based on selected time range
  const changeText = useMemo(() => {
    const timeRangeNumber = timeRange.split(' ')[0];
    return `from last ${timeRangeNumber} ${timeRange.split(' ')[1]}`;
  }, [timeRange]);

  // Memoized calculation function to prevent recreation
  const calculateChanges = useCallback(async () => {
      try {
        // Calculate Total Jobs change (current period vs double period)
        if (totalJobs?.totalJobs) {
          const currentTimeRange = TIME_RANGE_MAP[timeRange as keyof typeof TIME_RANGE_MAP] || TIME_RANGE_MAP['7 days'];
          const comparisonTimeRange = comparisonTimeRanges.totalJobs[timeRange as keyof typeof comparisonTimeRanges.totalJobs] || '14_days';

          logger.debug('Calculating total jobs change', 'SUMMARY_STATS', {
            currentTimeRange,
            comparisonTimeRange,
            currentJobs: totalJobs.totalJobs
          }, { component: 'SummaryStats', action: 'calculateTotalJobsChange' });

          const comparisonPeriodJobs = await dashboardApi.getTotalJobs(comparisonTimeRange);
          const currentPeriodJobs = totalJobs.totalJobs;
          const comparisonPeriodCount = comparisonPeriodJobs?.totalJobs || 0;

          // Calculate previous period by subtracting current from comparison period
          const previousPeriodJobs = Math.max(0, comparisonPeriodCount - currentPeriodJobs);

          logger.debug('Total jobs calculation details', 'SUMMARY_STATS', {
            currentPeriod: currentPeriodJobs,
            comparisonPeriod: comparisonPeriodCount,
            previousPeriod: previousPeriodJobs,
            currentTimeRange,
            comparisonTimeRange
          }, { component: 'SummaryStats', action: 'calculateTotalJobsChange' });

          if (previousPeriodJobs > 0) {
            const change = ((currentPeriodJobs - previousPeriodJobs) / previousPeriodJobs) * 100;
            setTotalJobsChange({
              percentage: Math.abs(Math.round(change)),
              type: change >= 0 ? 'increase' : 'decrease'
            });

            logger.debug('Total jobs change calculated', 'SUMMARY_STATS', {
              change,
              changeState: { percentage: Math.abs(Math.round(change)), type: change >= 0 ? 'increase' : 'decrease' }
            }, { component: 'SummaryStats', action: 'calculateTotalJobsChange' });
          } else if (previousPeriodJobs === 0 && currentPeriodJobs === 0) {
            // Both periods have 0 jobs (0% change)
            setTotalJobsChange({
              percentage: 0,
              type: 'increase' // Default to increase for 0%
            });
            logger.debug('No jobs in both periods (0%)', 'SUMMARY_STATS', undefined, {
              component: 'SummaryStats',
              action: 'calculateTotalJobsChange'
            });
          } else if (previousPeriodJobs === 0 && currentPeriodJobs > 0) {
            // New jobs appeared (100% increase)
            setTotalJobsChange({
              percentage: 100,
              type: 'increase'
            });
            logger.debug('New jobs appeared (100% increase)', 'SUMMARY_STATS', undefined, {
              component: 'SummaryStats',
              action: 'calculateTotalJobsChange'
            });
          } else if (comparisonPeriodCount === currentPeriodJobs && currentPeriodJobs > 0) {
            // Same number of jobs in both periods (0% change)
            setTotalJobsChange({
              percentage: 0,
              type: 'increase' // Default to increase for 0%
            });
            logger.debug('Same jobs in both periods (0%)', 'SUMMARY_STATS', {
              comparisonPeriodCount,
              currentPeriodJobs
            }, { component: 'SummaryStats', action: 'calculateTotalJobsChange' });
          } else {
            logger.debug('No change calculated for total jobs', 'SUMMARY_STATS', {
              previousPeriodJobs,
              comparisonPeriodCount,
              currentPeriodJobs,
              reason: 'Insufficient or invalid data'
            }, { component: 'SummaryStats', action: 'calculateTotalJobsChange' });
          }
        }

        // Calculate Security Anomalies change (current period vs double period)
        if (securityAnomalies?.anomalyCount !== undefined) {
          const comparisonTimeRange = comparisonTimeRanges.securityAnomalies[timeRange as keyof typeof comparisonTimeRanges.securityAnomalies] || '14 days';
          const filterToUse = jobFilter === 'all' ? 'all' : jobFilter;

          logger.debug('Calculating security anomalies change', 'SUMMARY_STATS', {
            currentData: securityAnomalies,
            jobFilter,
            timeRange,
            comparisonTimeRange
          }, { component: 'SummaryStats', action: 'calculateSecurityAnomaliesChange' });

          const comparisonPeriodAnomalies = await dashboardApi.getSecurityAnomaliesByFilter(filterToUse, comparisonTimeRange);
          logger.debug('Comparison period anomalies loaded', 'SUMMARY_STATS', {
            comparisonPeriodAnomalies,
            comparisonTimeRange
          }, { component: 'SummaryStats', action: 'calculateSecurityAnomaliesChange' });

          const currentPeriodAnomalies = securityAnomalies.anomalyCount;
          const comparisonPeriodCount = comparisonPeriodAnomalies?.anomalyCount || 0;

          // Calculate previous period by subtracting current from comparison period
          const previousPeriodAnomalies = Math.max(0, comparisonPeriodCount - currentPeriodAnomalies);

          logger.debug('Anomalies change calculation', 'SUMMARY_STATS', {
            currentPeriod: currentPeriodAnomalies,
            comparisonPeriod: comparisonPeriodCount,
            previousPeriod: previousPeriodAnomalies
          }, { component: 'SummaryStats', action: 'calculateSecurityAnomaliesChange' });

          if (previousPeriodAnomalies > 0) {
            const change = ((currentPeriodAnomalies - previousPeriodAnomalies) / previousPeriodAnomalies) * 100;
            const changeState = {
              percentage: Math.abs(Math.round(change)),
              type: change >= 0 ? 'increase' as const : 'decrease' as const
            };

            setSecurityAnomaliesChange(changeState);
            logger.debug('Security anomalies change calculated', 'SUMMARY_STATS', {
              change,
              changeState
            }, { component: 'SummaryStats', action: 'calculateSecurityAnomaliesChange' });
          } else if (previousPeriodAnomalies === 0 && currentPeriodAnomalies === 0) {
            // Special case: no anomalies in both periods (0%)
            setSecurityAnomaliesChange({
              percentage: 0,
              type: 'increase' // Default to increase for 0%
            });
            logger.debug('No anomalies in both periods (0%)', 'SUMMARY_STATS', undefined, {
              component: 'SummaryStats',
              action: 'calculateSecurityAnomaliesChange'
            });
          } else if (previousPeriodAnomalies === 0 && currentPeriodAnomalies > 0) {
            // New anomalies appeared (100% increase)
            setSecurityAnomaliesChange({
              percentage: 100,
              type: 'increase'
            });
            logger.debug('New anomalies appeared (100% increase)', 'SUMMARY_STATS', undefined, {
              component: 'SummaryStats',
              action: 'calculateSecurityAnomaliesChange'
            });
          } else {
            logger.debug('No change calculated (insufficient data)', 'SUMMARY_STATS', {
              previousPeriodAnomalies,
              currentPeriodAnomalies
            }, { component: 'SummaryStats', action: 'calculateSecurityAnomaliesChange' });
          }
        } else {
          logger.debug('No current security anomalies data available', 'SUMMARY_STATS', undefined, {
            component: 'SummaryStats',
            action: 'calculateSecurityAnomaliesChange'
          });
        }
      } catch (error) {
        logger.error('Failed to calculate percentage changes', 'SUMMARY_STATS', { error }, {
          component: 'SummaryStats',
          action: 'calculateChanges'
        });
      }
    }, [comparisonTimeRanges, jobFilter, timeRange, totalJobs, securityAnomalies]);

  // Calculate percentage changes with memory leak prevention
  useEffect(() => {
    let isMounted = true;

    const executeCalculation = async () => {
      if (totalJobs || securityAnomalies) {
        logger.debug('Starting percentage calculation', 'SUMMARY_STATS', {
          totalJobs: totalJobs?.totalJobs,
          securityAnomalies: securityAnomalies?.anomalyCount,
          jobFilter,
          timeRange
        }, { component: 'SummaryStats', action: 'executeCalculation' });

        try {
          await calculateChanges();
        } catch (error) {
          if (isMounted) {
            logger.error('Failed to execute percentage calculation', 'SUMMARY_STATS', { error }, {
              component: 'SummaryStats',
              action: 'executeCalculation'
            });
          }
        }
      } else {
        logger.debug('No data available for percentage calculation', 'SUMMARY_STATS', {
          totalJobs,
          securityAnomalies
        }, { component: 'SummaryStats', action: 'executeCalculation' });
      }
    };

    executeCalculation();

    return () => {
      isMounted = false;
    };
  }, [totalJobs, securityAnomalies, calculateChanges, jobFilter, timeRange]);

  // Debug log for state
  useEffect(() => {
    logger.debug('SummaryStats state update', 'SUMMARY_STATS', {
      totalJobsChange,
      securityAnomaliesChange,
      totalJobsValue: totalJobs?.totalJobs,
      securityAnomaliesValue: securityAnomalies?.anomalyCount
    }, { component: 'SummaryStats', action: 'stateUpdate' });
  }, [totalJobsChange, securityAnomaliesChange, totalJobs, securityAnomalies]);

  // Memoize stats calculation to prevent unnecessary recalculations
  const stats = useMemo(() => [
    {
      title: 'Total Jobs',
      value: totalJobs?.totalJobs || 0,
      icon: 'fas fa-folder',
      iconBg: 'bg-blue-100 dark:bg-blue-900/20',
      iconColor: 'text-blue-500',
      change: totalJobsChange ? `${totalJobsChange.percentage === 0 ? '' : totalJobsChange.type === 'increase' ? '+' : '-'}${totalJobsChange.percentage}%` : 'calculating...',
      changeType: totalJobsChange?.type || 'increase',
      changeText: changeText,
      showChange: true,
      // For Total Jobs: increase = good (green), decrease = bad (red)
      changeColorClass: totalJobsChange ? (totalJobsChange.type === 'increase' ? 'text-green-500' : 'text-red-500') : 'text-gray-400',
      arrowIcon: totalJobsChange ? (totalJobsChange.type === 'increase' ? 'fas fa-arrow-up' : 'fas fa-arrow-down') : 'fas fa-spinner fa-spin'
    },
    {
      title: 'Active Builds',
      value: activeBuilds?.activeBuilds || 0,
      icon: 'fas fa-cogs',
      iconBg: 'bg-green-100 dark:bg-green-900/20',
      iconColor: 'text-green-500',
      change: null,
      changeType: 'increase' as const,
      changeText: 'current active',
      showChange: false, // No percentage change for active builds
      changeColorClass: 'text-gray-500',
      arrowIcon: 'fas fa-play-circle'
    },
    {
      title: 'Security Anomalies',
      value: securityAnomalies?.anomalyCount || 0,
      icon: 'fas fa-exclamation-triangle',
      iconBg: 'bg-red-100 dark:bg-red-900/20',
      iconColor: 'text-red-500',
      change: securityAnomaliesChange !== null ? `${securityAnomaliesChange.percentage === 0 ? '' : securityAnomaliesChange.type === 'increase' ? '+' : '-'}${securityAnomaliesChange.percentage}%` : null,
      changeType: securityAnomaliesChange?.type || 'increase',
      changeText: changeText,
      showChange: true,
      // For Security Anomalies: increase = bad (red), decrease = good (green)
      changeColorClass: securityAnomaliesChange?.type === 'increase' ? 'text-red-500' : 'text-green-500',
      arrowIcon: securityAnomaliesChange?.type === 'increase' ? 'fas fa-arrow-up' : 'fas fa-arrow-down'
    }
  ], [totalJobs, activeBuilds, securityAnomalies, totalJobsChange, securityAnomaliesChange, changeText]);

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      {stats.map((stat, index) => (
        <div key={index} className="card p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">{stat.title}</p>
              <h4 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {stat.value.toLocaleString()}
              </h4>
            </div>
            <div className={`p-3 rounded-full ${stat.iconBg}`}>
              <i className={`${stat.icon} ${stat.iconColor}`}></i>
            </div>
          </div>
          <div className="mt-2 flex items-center text-sm">
            {stat.showChange ? (
              <>
                <span className={`flex items-center ${stat.changeColorClass}`}>
                  <i className={`${stat.arrowIcon} mr-1`}></i>
                  {stat.change}
                </span>
                <span className="text-gray-500 dark:text-gray-400 ml-2">{stat.changeText}</span>
              </>
            ) : (
              <span className={`flex items-center text-gray-500 dark:text-gray-400`}>
                {stat.title === 'Active Builds' ? (
                  <>
                    <i className={`fas ${
                      stat.value === 0 ? 'fa-pause-circle' :
                      stat.value <= 2 ? 'fa-play-circle' :
                      'fa-spinner fa-spin'
                    } mr-1`}></i>
                    {stat.changeText}
                  </>
                ) : (
                  stat.changeText
                )}
              </span>
            )}
          </div>
        </div>
      ))}
    </div>
  );
});

// Add display name for better debugging
SummaryStats.displayName = 'SummaryStats';

export default SummaryStats;
