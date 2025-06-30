import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Chart from 'react-apexcharts';
import { dashboardApi } from '../../services/api';
import type { ChartDataDTO } from '../../services/api';
import { logger } from '../../utils';
import { getOptimizedLineChartConfig } from '../../utils/chartOptimizer';
import { useChartPerformance } from '../../hooks/useChartPerformance';

interface AnomaliesTrendChartProps {
  jobFilter?: string;
  timeRange?: string;
}

const AnomaliesTrendChart: React.FC<AnomaliesTrendChartProps> = React.memo(({ jobFilter = 'All Jobs' }) => {
  const [chartData, setChartData] = useState<ChartDataDTO | null>(null);
  const [loading, setLoading] = useState(true);

  // Performance monitoring
  useChartPerformance('AnomaliesTrendChart', loading, !!chartData?.datasets?.length);

  // Memoize data loading function to prevent unnecessary recreations
  const loadAnomalyTrendData = useCallback(async () => {
    try {
      setLoading(true);
      logger.debug('Loading anomaly trend data', 'ANOMALY_TREND_CHART', { jobFilter }, {
        component: 'AnomaliesTrendChart',
        action: 'loadAnomalyTrendData'
      });

      const apiData = await dashboardApi.getAnomalyTrend(jobFilter, 10);
      logger.debug('Anomaly trend data loaded', 'ANOMALY_TREND_CHART', {
        dataPoints: apiData?.labels?.length || 0,
        datasets: apiData?.datasets?.length || 0
      }, { component: 'AnomaliesTrendChart', action: 'loadAnomalyTrendData' });

      setChartData(apiData);
    } catch (error) {
      logger.error('Failed to load anomaly trend data', 'ANOMALY_TREND_CHART', { error }, {
        component: 'AnomaliesTrendChart',
        action: 'loadAnomalyTrendData'
      });
      setChartData(null);
    } finally {
      setLoading(false);
    }
  }, [jobFilter]); // Add jobFilter as dependency

  // Memoize chart series to prevent unnecessary recalculations
  const series = useMemo(() => {
    if (!chartData?.datasets) return [];

    return chartData.datasets.map(dataset => ({
      name: dataset.label,
      data: dataset.data,
    }));
  }, [chartData?.datasets]);

  // Memoize chart options with performance optimizations
  const options = useMemo(() => {
    const baseConfig = getOptimizedLineChartConfig();

    return {
      ...baseConfig,
      xaxis: {
        ...baseConfig.xaxis,
        categories: chartData?.labels || [],
      },
      yaxis: {
        ...baseConfig.yaxis,
        title: {
          text: 'Number of Anomalies',
        },
      },
      colors: ['#3b82f6'], // Match version_1.html exact color
      theme: {
        mode: 'light' as const,
      },
    };
  }, [chartData?.labels, chartData?.datasets]);

  useEffect(() => {
    loadAnomalyTrendData();
  }, [jobFilter]); // ✅ FIX: Depend on jobFilter directly, not just the function

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomalies Trend</h3>
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
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomalies Trend</h3>
        </div>
        <div className="p-4 flex items-center justify-center h-64">
          <div className="text-center text-gray-500 dark:text-gray-400">
            <i className="fas fa-chart-line text-2xl mb-2"></i>
            <p className="text-sm">No trend data available for "{jobFilter}"</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomalies Trend</h3>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
          Filter: {jobFilter} | Data points: {chartData.labels.length}
        </p>
      </div>
      <div className="p-4">
        <Chart
          options={options}
          series={series}
          type="line"
          height={300}
        />
      </div>
    </div>
  );
});

// Add display name for better debugging
AnomaliesTrendChart.displayName = 'AnomaliesTrendChart';

export default AnomaliesTrendChart;
