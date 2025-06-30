// Chart performance optimization utilities
import React from 'react';
import { logger } from './logger';

// Optimized ApexCharts configuration matching version_1.html design with performance balance
export const getOptimizedChartConfig = () => ({
  // Enable lightweight animations to match original design
  animations: {
    enabled: true,
    easing: 'easeinout',
    speed: 400, // Faster than default but still smooth
    animateGradually: {
      enabled: true,
      delay: 50 // Reduced delay for faster rendering
    },
    dynamicAnimation: {
      enabled: true,
      speed: 200 // Fast dynamic updates
    }
  },
  
  // Optimize chart rendering
  chart: {
    redrawOnParentResize: false,
    redrawOnWindowResize: false,
    animations: {
      enabled: false
    },
    toolbar: {
      show: false
    },
    zoom: {
      enabled: false
    },
    selection: {
      enabled: false
    },
    pan: {
      enabled: false
    },
    events: {
      mounted: function(chartContext: any, config: any) {
        // Chart has finished rendering
        if ((window as any).__chartPerformanceStart) {
          const renderTime = performance.now() - (window as any).__chartPerformanceStart;
          logger.debug('Chart render completed', 'CHART_PERFORMANCE', {
            renderTime: Math.round(renderTime),
            chartId: config?.config?.chart?.id || 'unknown'
          });
        }
      },
      beforeMount: function(chartContext: any, config: any) {
        // Chart is about to start rendering
        (window as any).__chartPerformanceStart = performance.now();
      }
    }
  },
  
  // Simple tooltip matching original
  tooltip: {
    enabled: true,
    shared: false,
    intersect: true,
    followCursor: false
  },
  
  // Optimize legend
  legend: {
    show: true,
    floating: false,
    position: 'top',
    horizontalAlign: 'center',
    fontSize: '12px',
    markers: {
      width: 8,
      height: 8
    }
  },
  
  // Simple grid matching original
  grid: {
    show: true,
    borderColor: '#e0e0e0',
    strokeDashArray: 0,
    position: 'back',
    xaxis: {
      lines: {
        show: false
      }
    },
    yaxis: {
      lines: {
        show: true
      }
    }
  },
  
  // Optimize responsive behavior
  responsive: [{
    breakpoint: 768,
    options: {
      chart: {
        height: 250
      },
      legend: {
        position: 'bottom'
      }
    }
  }]
});

// Performance-optimized chart options for different chart types
export const getOptimizedBarChartConfig = () => ({
  ...getOptimizedChartConfig(),
  chart: {
    ...getOptimizedChartConfig().chart,
    type: 'bar',
    height: 300,
    stacked: true,
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: false,
      columnWidth: '55%'
    }
  },
  fill: {
    opacity: 1
  },
  legend: {
    position: 'top'
  }
});

export const getOptimizedLineChartConfig = () => ({
  ...getOptimizedChartConfig(),
  chart: {
    ...getOptimizedChartConfig().chart,
    type: 'line',
    height: 300,
    toolbar: {
      show: false
    }
  },
  stroke: {
    curve: 'smooth', // Match original design
    width: 3 // Match original design
  },
  markers: {
    size: 0,
    hover: {
      size: 4
    }
  },
  dataLabels: {
    enabled: false
  },
  tooltip: {
    x: {
      show: true
    }
  }
});

// Chart performance monitoring
export class ChartPerformanceMonitor {
  private renderTimes: Map<string, number[]> = new Map();
  
  startRender(chartId: string): number {
    const startTime = performance.now();
    logger.debug(`Starting chart render: ${chartId}`, 'CHART_PERFORMANCE');
    return startTime;
  }
  
  endRender(chartId: string, startTime: number): void {
    const endTime = performance.now();
    const renderTime = endTime - startTime;
    
    // Store render time
    if (!this.renderTimes.has(chartId)) {
      this.renderTimes.set(chartId, []);
    }
    this.renderTimes.get(chartId)!.push(renderTime);
    
    // Log performance with adjusted thresholds for animated charts
    if (renderTime > 600) { // Increased threshold to account for animations
      logger.warn(`Slow chart render detected: ${chartId}`, 'CHART_PERFORMANCE', {
        renderTime: Math.round(renderTime),
        threshold: 600
      });
    } else if (renderTime > 400) {
      logger.info(`Chart render took longer than expected: ${chartId}`, 'CHART_PERFORMANCE', {
        renderTime: Math.round(renderTime),
        note: 'This includes animation time'
      });
    } else {
      logger.debug(`Chart render completed: ${chartId}`, 'CHART_PERFORMANCE', {
        renderTime: Math.round(renderTime)
      });
    }
  }
  
  getAverageRenderTime(chartId: string): number {
    const times = this.renderTimes.get(chartId);
    if (!times || times.length === 0) return 0;
    
    const average = times.reduce((sum, time) => sum + time, 0) / times.length;
    return Math.round(average);
  }
  
  getPerformanceReport(): { [chartId: string]: { average: number; samples: number; max: number } } {
    const report: { [chartId: string]: { average: number; samples: number; max: number } } = {};
    
    this.renderTimes.forEach((times, chartId) => {
      if (times.length > 0) {
        const average = times.reduce((sum, time) => sum + time, 0) / times.length;
        const max = Math.max(...times);
        report[chartId] = {
          average: Math.round(average),
          samples: times.length,
          max: Math.round(max)
        };
      }
    });
    
    return report;
  }
  
  clearHistory(): void {
    this.renderTimes.clear();
    logger.info('Chart performance history cleared', 'CHART_PERFORMANCE');
  }
}

// Global chart performance monitor
export const chartPerformanceMonitor = new ChartPerformanceMonitor();

// Debounced resize handler for charts
export const createDebouncedResizeHandler = (callback: () => void, delay: number = 250) => {
  let timeoutId: NodeJS.Timeout;
  
  return () => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      callback();
    }, delay);
  };
};

// Chart data optimization utilities
export const optimizeChartData = (data: any[], maxDataPoints: number = 50): any[] => {
  if (!Array.isArray(data) || data.length <= maxDataPoints) {
    return data;
  }
  
  // Sample data points to reduce rendering load
  const step = Math.ceil(data.length / maxDataPoints);
  const optimizedData = [];
  
  for (let i = 0; i < data.length; i += step) {
    optimizedData.push(data[i]);
  }
  
  // Always include the last data point
  if (optimizedData[optimizedData.length - 1] !== data[data.length - 1]) {
    optimizedData.push(data[data.length - 1]);
  }
  
  logger.debug('Chart data optimized', 'CHART_PERFORMANCE', {
    originalPoints: data.length,
    optimizedPoints: optimizedData.length,
    reduction: `${Math.round((1 - optimizedData.length / data.length) * 100)}%`
  });
  
  return optimizedData;
};

// Lazy loading utility for charts
export const createLazyChart = (
  chartComponent: React.ComponentType<any>
) => {
  return React.lazy(() => Promise.resolve({ default: chartComponent }));
};

// Chart intersection observer for lazy rendering
export const useChartIntersectionObserver = (
  ref: React.RefObject<HTMLElement>,
  options: IntersectionObserverInit = { threshold: 0.1 }
) => {
  const [isVisible, setIsVisible] = React.useState(false);
  
  React.useEffect(() => {
    const element = ref.current;
    if (!element) return;
    
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && !isVisible) {
        setIsVisible(true);
        observer.disconnect(); // Only render once
      }
    }, options);
    
    observer.observe(element);
    
    return () => observer.disconnect();
  }, [ref, isVisible, options]);
  
  return isVisible;
};

// Performance optimization recommendations
export const getChartOptimizationRecommendations = (performanceReport: ReturnType<ChartPerformanceMonitor['getPerformanceReport']>) => {
  const recommendations: string[] = [];

  Object.entries(performanceReport).forEach(([chartId, metrics]) => {
    if (metrics.average > 600) {
      recommendations.push(`${chartId}: Very slow renders detected (avg: ${metrics.average}ms) - consider reducing data points`);
    } else if (metrics.average > 400) {
      recommendations.push(`${chartId}: Slower than optimal (avg: ${metrics.average}ms) - this includes animation time`);
    }

    if (metrics.max > 1000) {
      recommendations.push(`${chartId}: Experiencing very slow renders (max: ${metrics.max}ms) - consider lazy loading`);
    }

    if (metrics.samples > 10 && metrics.average > 300) {
      recommendations.push(`${chartId}: Frequent re-renders detected - check for unnecessary updates`);
    }
  });

  if (recommendations.length === 0) {
    recommendations.push('All charts are performing optimally! 🎉');
  }

  return recommendations;
};

// Make available globally in development
if (process.env.NODE_ENV === 'development') {
  (window as any).chartOptimizer = {
    performanceMonitor: chartPerformanceMonitor,
    getPerformanceReport: () => chartPerformanceMonitor.getPerformanceReport(),
    getRecommendations: () => getChartOptimizationRecommendations(chartPerformanceMonitor.getPerformanceReport()),
    clearHistory: () => chartPerformanceMonitor.clearHistory()
  };
}

export default {
  getOptimizedChartConfig,
  getOptimizedBarChartConfig,
  getOptimizedLineChartConfig,
  chartPerformanceMonitor,
  createDebouncedResizeHandler,
  optimizeChartData,
  createLazyChart,
  useChartIntersectionObserver,
  getChartOptimizationRecommendations
};
