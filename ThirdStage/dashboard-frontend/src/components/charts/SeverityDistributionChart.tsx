import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Chart from 'react-apexcharts';
import { dashboardApi } from '../../services/api';
import type { ChartDataDTO } from '../../services/api';
import { logger } from '../../utils';
import { getOptimizedBarChartConfig } from '../../utils/chartOptimizer';
import { useChartPerformance } from '../../hooks/useChartPerformance';

interface SeverityDistributionChartProps {
  jobFilter?: string;
  timeRange?: string;
}

const SeverityDistributionChart: React.FC<SeverityDistributionChartProps> = React.memo(({ jobFilter = 'All Jobs' }) => {
  const [chartData, setChartData] = useState<ChartDataDTO | null>(null);
  const [loading, setLoading] = useState(true);

  // Performance monitoring
  useChartPerformance('SeverityDistributionChart', loading, !!chartData?.datasets?.length);

  // Memoize data loading function to prevent unnecessary recreations
  const loadSeverityDistributionData = useCallback(async () => {
    try {
      setLoading(true);
      logger.debug('Loading severity distribution data', 'SEVERITY_DISTRIBUTION_CHART', { jobFilter }, {
        component: 'SeverityDistributionChart',
        action: 'loadSeverityDistributionData'
      });

      const apiData = await dashboardApi.getSeverityDistribution(jobFilter, 5);
      logger.debug('Severity distribution data loaded', 'SEVERITY_DISTRIBUTION_CHART', {
        dataPoints: apiData?.labels?.length || 0,
        datasets: apiData?.datasets?.length || 0
      }, { component: 'SeverityDistributionChart', action: 'loadSeverityDistributionData' });

      setChartData(apiData);
    } catch (error) {
      logger.error('Failed to load severity distribution data', 'SEVERITY_DISTRIBUTION_CHART', { error }, {
        component: 'SeverityDistributionChart',
        action: 'loadSeverityDistributionData'
      });
      setChartData(null);
    } finally {
      setLoading(false);
    }
  }, [jobFilter]);

  // Memoize color mapping for severity levels - DISTINCT colors for each level
  const getTemplateColor = useCallback((severityLabel: string): string => {
    const colorMap: { [key: string]: string } = {
      'CRITICAL': '#b91c1c',  // Dark Red - exact match to version_1.html
      'HIGH': '#ef4444',      // Red - exact match to version_1.html
      'MEDIUM': '#f59e0b',    // Orange - exact match to version_1.html
      'WARNING': '#f97316',   // Different orange for WARNING (distinct from MEDIUM)
      'LOW': '#a3e635',       // Green - exact match to version_1.html
    };
    return colorMap[severityLabel.toUpperCase()] || '#6b7280'; // Default gray
  }, []);

  // Memoize severity order from LOW to CRITICAL
  const getSeverityOrder = useCallback((severityLabel: string): number => {
    const orderMap: { [key: string]: number } = {
      'LOW': 1,
      'WARNING': 2,
      'MEDIUM': 3,
      'HIGH': 4,
      'CRITICAL': 5,
    };
    return orderMap[severityLabel.toUpperCase()] || 999; // Unknown severities go last
  }, []);

  // Memoize sorted datasets by severity order (LOW to CRITICAL)
  const sortedDatasets = useMemo(() => {
    if (!chartData?.datasets) return [];

    const sorted = [...chartData.datasets].sort((a, b) =>
      getSeverityOrder(a.label) - getSeverityOrder(b.label)
    );

    logger.debug('Severity datasets sorted with color mapping', 'SEVERITY_DISTRIBUTION_CHART', {
      originalOrder: chartData.datasets.map(d => d.label),
      sortedOrder: sorted.map(d => d.label),
      colorMapping: sorted.map(d => ({
        severity: d.label,
        color: getTemplateColor(d.label),
        data: d.data
      }))
    }, { component: 'SeverityDistributionChart', action: 'sortDatasets' });

    return sorted;
  }, [chartData?.datasets, getSeverityOrder, getTemplateColor]);

  // Memoize chart series to prevent unnecessary recalculations
  const series = useMemo(() => {
    return sortedDatasets.map(dataset => ({
      name: dataset.label,
      data: dataset.data,
    }));
  }, [sortedDatasets]);

  // Memoize chart options - EXACT match to version_1.html
  const options = useMemo(() => {
    return {
      chart: {
        type: 'bar' as const,
        height: 300, // Further increased height for better label accommodation in small segments
        stacked: true,
        toolbar: {
          show: false
        }
      },
      plotOptions: {
        bar: {
          horizontal: false,
          columnWidth: '55%',
          dataLabels: {
            position: 'center', // Ensure labels are centered in segments
            hideOverflowingLabels: false, // Always show labels even in small segments
            maxItems: 100 // Allow all labels to be shown
          }
        }
      },
      xaxis: {
        categories: chartData?.labels || []
      },
      yaxis: {
        title: {
          text: 'Number of Issues'
        },
        min: 0, // Ensure y-axis starts at 0
        forceNiceScale: true, // Use nice scale intervals
        labels: {
          style: {
            fontSize: '12px'
          }
        }
      },
      legend: {
        position: 'top' as const
      },
      fill: {
        opacity: 1
      },
      colors: sortedDatasets.map(dataset => getTemplateColor(dataset.label)),
      dataLabels: {
        enabled: true,
        style: {
          colors: ['#fff'],
          fontSize: '11px', // Slightly smaller font for better fit in small segments
          fontWeight: 'bold'
        },
        formatter: function(val: number) {
          // Show all numbers except zeros
          return val === 0 ? '' : val.toString();
        },
        offsetY: 0, // Center vertically
        textAnchor: 'middle' as const, // Center horizontally
        background: {
          enabled: false // No background to avoid clutter
        },
        dropShadow: {
          enabled: false // No shadow to keep it clean
        }
      }
    };
  }, [chartData?.labels, sortedDatasets, getTemplateColor]);

  useEffect(() => {
    loadSeverityDistributionData();
  }, [jobFilter]); // ✅ FIX: Depend on jobFilter directly, not just the function

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Severity Distribution</h3>
        </div>
        <div className="p-4 flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        </div>
      </div>
    );
  }

  if (!chartData || !chartData.datasets || chartData.datasets.length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Severity Distribution</h3>
        </div>
        <div className="p-4 flex items-center justify-center h-64">
          <div className="text-center text-gray-500 dark:text-gray-400">
            <i className="fas fa-chart-bar text-2xl mb-2"></i>
            <p className="text-sm">No severity data available for "{jobFilter}"</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">Severity Distribution</h3>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
          Filter: {jobFilter} | Builds: {chartData.labels.length} | Severities: {chartData.datasets.length}
        </p>
      </div>
      <div className="p-4">
        <Chart
          options={options}
          series={series}
          type="bar"
          height={300}
        />
      </div>
    </div>
  );
});

// Add display name for better debugging
SeverityDistributionChart.displayName = 'SeverityDistributionChart';

export default SeverityDistributionChart;
