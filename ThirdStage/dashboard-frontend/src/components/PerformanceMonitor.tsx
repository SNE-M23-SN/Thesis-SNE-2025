// Performance monitoring component for development
import React, { useState, useEffect } from 'react';
import { chartPerformanceMonitor, getChartOptimizationRecommendations } from '../utils/chartOptimizer';
import { logger } from '../utils';

interface PerformanceMonitorProps {
  isVisible: boolean;
  onClose: () => void;
}

const PerformanceMonitor: React.FC<PerformanceMonitorProps> = ({ isVisible, onClose }) => {
  const [performanceData, setPerformanceData] = useState<any>({});
  const [recommendations, setRecommendations] = useState<string[]>([]);

  useEffect(() => {
    if (isVisible) {
      const updateData = () => {
        const report = chartPerformanceMonitor.getPerformanceReport();
        const recs = getChartOptimizationRecommendations(report);
        setPerformanceData(report);
        setRecommendations(recs);
      };

      updateData();
      const interval = setInterval(updateData, 2000); // Update every 2 seconds

      return () => clearInterval(interval);
    }
  }, [isVisible]);

  if (!isVisible) return null;

  const clearHistory = () => {
    chartPerformanceMonitor.clearHistory();
    setPerformanceData({});
    setRecommendations(['Performance history cleared']);
    logger.info('Chart performance history cleared', 'PERFORMANCE_MONITOR');
  };

  return (
    <div className="fixed top-4 right-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-4 max-w-md z-50">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-gray-900 dark:text-gray-100">
          📊 Chart Performance Monitor
        </h3>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <i className="fas fa-times"></i>
        </button>
      </div>

      <div className="space-y-3">
        {/* Performance Metrics */}
        <div>
          <h4 className="text-xs font-medium text-gray-700 dark:text-gray-300 mb-2">
            Render Times (ms)
          </h4>
          {Object.keys(performanceData).length === 0 ? (
            <p className="text-xs text-gray-500 dark:text-gray-400">
              No performance data yet. Charts will be monitored as they render.
            </p>
          ) : (
            <div className="space-y-1">
              {Object.entries(performanceData).map(([chartId, metrics]: [string, any]) => (
                <div key={chartId} className="flex justify-between text-xs">
                  <span className="text-gray-600 dark:text-gray-400 truncate">
                    {chartId}:
                  </span>
                  <span className={`font-mono ${
                    metrics.average > 600 ? 'text-red-600' :
                    metrics.average > 400 ? 'text-yellow-600' : 'text-green-600'
                  }`}>
                    {metrics.average}ms (×{metrics.samples})
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recommendations */}
        <div>
          <h4 className="text-xs font-medium text-gray-700 dark:text-gray-300 mb-2">
            Recommendations
          </h4>
          <div className="space-y-1">
            {recommendations.map((rec, index) => (
              <p key={index} className={`text-xs ${
                rec.includes('optimally') ? 'text-green-600' : 'text-yellow-600'
              }`}>
                {rec}
              </p>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="flex space-x-2 pt-2 border-t border-gray-200 dark:border-gray-700">
          <button
            onClick={clearHistory}
            className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 px-2 py-1 rounded hover:bg-gray-200 dark:hover:bg-gray-600"
          >
            Clear History
          </button>
          <button
            onClick={() => {
              console.log('📊 Chart Performance Report:', performanceData);
              console.log('💡 Recommendations:', recommendations);
            }}
            className="text-xs bg-blue-100 dark:bg-blue-800 text-blue-700 dark:text-blue-300 px-2 py-1 rounded hover:bg-blue-200 dark:hover:bg-blue-700"
          >
            Log to Console
          </button>
        </div>
      </div>
    </div>
  );
};

export default PerformanceMonitor;
