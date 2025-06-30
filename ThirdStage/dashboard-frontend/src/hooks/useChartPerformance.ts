// React hook for monitoring chart performance
import { useEffect, useRef } from 'react';
import { chartPerformanceMonitor } from '../utils/chartOptimizer';
import { logger } from '../utils';

export const useChartPerformance = (
  chartId: string,
  isLoading: boolean,
  hasData: boolean
) => {
  const startTimeRef = useRef<number | null>(null);
  const mountedRef = useRef(false);

  // Start performance monitoring when chart begins to render
  useEffect(() => {
    if (!isLoading && hasData && !mountedRef.current) {
      startTimeRef.current = chartPerformanceMonitor.startRender(chartId);
      mountedRef.current = true;
      
      logger.debug(`Starting performance monitoring for ${chartId}`, 'CHART_PERFORMANCE');
    }
  }, [isLoading, hasData, chartId]);

  // End performance monitoring after a longer delay (allowing chart animations to complete)
  useEffect(() => {
    if (!isLoading && hasData && mountedRef.current && startTimeRef.current) {
      const timeoutId = setTimeout(() => {
        if (startTimeRef.current) {
          chartPerformanceMonitor.endRender(chartId, startTimeRef.current);
          startTimeRef.current = null;
        }
      }, 500); // 500ms delay to account for animations and full rendering

      return () => clearTimeout(timeoutId);
    }
  }, [isLoading, hasData, chartId]);

  // Reset when component unmounts or data changes
  useEffect(() => {
    return () => {
      mountedRef.current = false;
      startTimeRef.current = null;
    };
  }, []);

  return {
    isMonitoring: mountedRef.current && startTimeRef.current !== null
  };
};

export default useChartPerformance;
