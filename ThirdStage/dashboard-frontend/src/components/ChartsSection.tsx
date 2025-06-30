import React, { useState, useEffect } from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, ArcElement } from 'chart.js';
import { Line, Doughnut } from 'react-chartjs-2';
import type { ChartDataDTO, RiskScoreDTO } from '../services/api';
import { dashboardApi} from '../services/api';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);

interface ChartsSectionProps {
  jobFilter: string;
}

const ChartsSection: React.FC<ChartsSectionProps> = ({ jobFilter }) => {
  const [anomalyTrendData, setAnomalyTrendData] = useState<ChartDataDTO | null>(null);
  const [severityData, setSeverityData] = useState<ChartDataDTO | null>(null);
  const [riskScore, setRiskScore] = useState<RiskScoreDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadChartsData();
  }, [jobFilter]);

  const loadChartsData = async () => {
    try {
      setLoading(true);
      console.log('📊 Loading charts data for jobFilter:', jobFilter);

      const [trendData, severityDistribution] = await Promise.all([
        dashboardApi.getAnomalyTrend(jobFilter, 10),
        dashboardApi.getSeverityDistribution(jobFilter)
      ]);

      console.log('📈 Anomaly trend data received:', trendData);
      console.log('🍩 Severity distribution data received:', severityDistribution);

      setAnomalyTrendData(trendData);
      setSeverityData(severityDistribution);

      // Mock risk score data - replace with actual API call when available
      setRiskScore({
        score: 82,
        change: 17,
        riskLevel: 'Critical',
        previousScore: 65
      });
    } catch (error) {
      console.error('❌ Failed to load charts data:', error);
      // Set fallback data to prevent empty charts
      setAnomalyTrendData(null);
      setSeverityData(null);
    } finally {
      setLoading(false);
    }
  };

  const lineChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
      },
    },
  };

  const getRiskScoreColor = (score: number) => {
    if (score >= 76) return 'text-red-600';
    if (score >= 51) return 'text-orange-600';
    if (score >= 26) return 'text-yellow-600';
    return 'text-green-600';
  };

  const getRiskScoreBackground = (score: number) => {
    if (score >= 76) return 'from-red-500 to-red-600';
    if (score >= 51) return 'from-orange-500 to-orange-600';
    if (score >= 26) return 'from-yellow-500 to-yellow-600';
    return 'from-green-500 to-green-600';
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="card p-4">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-4"></div>
            <div className="h-32 bg-gray-200 dark:bg-gray-700 rounded"></div>
          </div>
        </div>
        <div className="card p-4">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-4"></div>
            <div className="h-32 bg-gray-200 dark:bg-gray-700 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Risk Score Gauge */}
      {riskScore && (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Score</h3>
          </div>
          <div className="p-4 flex flex-col items-center">
            <div className="relative w-32 h-32 mb-4">
              <div className="absolute inset-0 rounded-full bg-gray-200 dark:bg-gray-700"></div>
              <div 
                className={`absolute inset-0 rounded-full bg-gradient-to-r ${getRiskScoreBackground(riskScore.score)}`}
                style={{
                  background: `conic-gradient(from 0deg, transparent ${360 - (riskScore.score / 100) * 360}deg, currentColor 0deg)`,
                }}
              ></div>
              <div className="absolute inset-2 rounded-full bg-white dark:bg-gray-800 flex items-center justify-center">
                <span className={`text-2xl font-bold ${getRiskScoreColor(riskScore.score)}`}>
                  {riskScore.score}
                </span>
              </div>
            </div>
            
            <div className="w-full space-y-2">
              <div className="flex justify-between text-xs">
                <span className="text-gray-500 dark:text-gray-400">Previous Score</span>
                <span className="font-medium text-gray-900 dark:text-gray-100">{riskScore.previousScore}</span>
              </div>
              <div className="flex justify-between text-xs">
                <span className="text-gray-500 dark:text-gray-400">Change</span>
                <span className={`font-medium ${riskScore.change > 0 ? 'text-red-600' : 'text-green-600'}`}>
                  {riskScore.change > 0 ? '+' : ''}{riskScore.change}
                </span>
              </div>
              <div className="flex justify-between text-xs">
                <span className="text-gray-500 dark:text-gray-400">Risk Level</span>
                <span className={`font-medium ${getRiskScoreColor(riskScore.score)}`}>
                  {riskScore.riskLevel}
                </span>
              </div>
            </div>
            
            <button className="w-full mt-4 btn-secondary text-sm">
              View Risk Details
            </button>
          </div>
        </div>
      )}

      {/* Anomalies Trend Chart */}
      {anomalyTrendData ? (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomalies Trend</h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Filter: {jobFilter} | Data points: {anomalyTrendData.labels.length}
            </p>
          </div>
          <div className="p-4">
            <div style={{ height: '200px' }}>
              <Line data={anomalyTrendData} options={lineChartOptions} />
            </div>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomalies Trend</h3>
          </div>
          <div className="p-4 flex items-center justify-center h-48">
            <div className="text-center text-gray-500 dark:text-gray-400">
              <i className="fas fa-chart-line text-2xl mb-2"></i>
              <p className="text-sm">No trend data available for "{jobFilter}"</p>
            </div>
          </div>
        </div>
      )}

      {/* Severity Distribution Chart */}
      {severityData ? (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Severity Distribution</h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Filter: {jobFilter} | Categories: {severityData.labels.length}
            </p>
          </div>
          <div className="p-4">
            <div style={{ height: '200px' }}>
              <Doughnut data={severityData} options={doughnutOptions} />
            </div>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Severity Distribution</h3>
          </div>
          <div className="p-4 flex items-center justify-center h-48">
            <div className="text-center text-gray-500 dark:text-gray-400">
              <i className="fas fa-chart-pie text-2xl mb-2"></i>
              <p className="text-sm">No severity data available for "{jobFilter}"</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChartsSection;
