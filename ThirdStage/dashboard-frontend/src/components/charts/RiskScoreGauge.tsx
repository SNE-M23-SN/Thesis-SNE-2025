import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Chart from 'react-apexcharts';
import { dashboardApi } from '../../services/api';
import type { RecentJobBuildDTO, RiskScoreDTO } from '../../services/api';
import RiskDetailsModal from '../RiskDetailsModal';
import { logger } from '../../utils';

interface RiskScoreGaugeProps {
  score: number;
  previousScore?: number;
  riskLevel?: string;
  change?: number;
  selectedBuild?: RecentJobBuildDTO | null;
  hasBuilds?: boolean;
}

const RiskScoreGauge: React.FC<RiskScoreGaugeProps> = React.memo(({
  score,
  previousScore = 0,
  riskLevel,
  change,
  selectedBuild,
  hasBuilds = true
}) => {
  const [riskScoreData, setRiskScoreData] = useState<RiskScoreDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = useCallback(() => setIsModalOpen(true), []);
  const closeModal = useCallback(() => setIsModalOpen(false), []);

  // Fetch risk score when a build is selected
  useEffect(() => {
    const fetchRiskScore = async () => {
      if (selectedBuild) {
        try {
          setLoading(true);
          logger.debug('Fetching risk score for build', 'RISK_SCORE_GAUGE', {
            jobName: selectedBuild.jobName,
            buildId: selectedBuild.buildId,
            originalJobName: selectedBuild.originalJobName
          }, { component: 'RiskScoreGauge', action: 'fetchRiskScore' });

          // Try with originalJobName first since that's what the API might expect
          const apiResponse = await dashboardApi.getRiskScore(selectedBuild.originalJobName, selectedBuild.buildId);
          logger.debug('Risk score API response received', 'RISK_SCORE_GAUGE', {
            responseKeys: Object.keys(apiResponse || {}),
            hasData: apiResponse?.hasData
          }, { component: 'RiskScoreGauge', action: 'fetchRiskScore' });

          // Check if the response has the expected structure
          if (apiResponse && typeof apiResponse === 'object') {
            // The API returns { hasData: boolean, data?: RiskScoreDTO }
            if (apiResponse.hasData && apiResponse.data) {
              logger.debug('Valid risk score data found', 'RISK_SCORE_GAUGE', {
                riskScore: apiResponse.data.score,
                riskLevel: apiResponse.data.riskLevel
              }, { component: 'RiskScoreGauge', action: 'fetchRiskScore' });
              setRiskScoreData(apiResponse.data);
            } else {
              logger.debug('No risk score data available', 'RISK_SCORE_GAUGE', undefined, {
                component: 'RiskScoreGauge',
                action: 'fetchRiskScore'
              });
              setRiskScoreData(null);
            }
          } else {
            logger.warn('Invalid risk score response structure', 'RISK_SCORE_GAUGE', { apiResponse }, {
              component: 'RiskScoreGauge',
              action: 'fetchRiskScore'
            });
            setRiskScoreData(null);
          }
        } catch (error) {
          logger.error('Failed to fetch risk score', 'RISK_SCORE_GAUGE', { error }, {
            component: 'RiskScoreGauge',
            action: 'fetchRiskScore'
          });

          setRiskScoreData(null);
        } finally {
          setLoading(false);
        }
      } else {
        logger.debug('No build selected, clearing risk score data', 'RISK_SCORE_GAUGE', undefined, {
          component: 'RiskScoreGauge',
          action: 'fetchRiskScore'
        });
        setRiskScoreData(null);
      }
    };

    fetchRiskScore();
  }, [selectedBuild]);

  // Memoize display values to prevent unnecessary recalculations
  const displayValues = useMemo(() => {
    const displayScore = riskScoreData?.score || score;
    const displayPreviousScore = riskScoreData?.previousScore || previousScore;
    const displayChange = riskScoreData?.change || change || (displayScore - displayPreviousScore);
    const displayRiskLevel = riskScoreData?.riskLevel || riskLevel;

    // Debug logging for display values
    logger.debug('Risk score display values calculated', 'RISK_SCORE_GAUGE', {
      hasRiskScoreData: !!riskScoreData,
      displayScore,
      displayPreviousScore,
      displayChange,
      displayRiskLevel
    }, { component: 'RiskScoreGauge', action: 'calculateDisplayValues' });

    return {
      displayScore,
      displayPreviousScore,
      displayChange,
      displayRiskLevel
    };
  }, [riskScoreData, score, previousScore, change, riskLevel]);

  // Memoize chart options to prevent recreation
  const options = useMemo(() => ({
    chart: {
      height: 180,
      type: 'radialBar' as const,
      offsetY: -10,
    },
    plotOptions: {
      radialBar: {
        startAngle: -135,
        endAngle: 135,
        hollow: {
          margin: 0,
          size: '70%',
        },
        track: {
          background: '#e7e7e7',
          strokeWidth: '67%',
          margin: 0,
        },
        dataLabels: {
          show: false,
        },
      },
    },
    fill: {
      type: 'gradient' as const,
      gradient: {
        shade: 'dark' as const,
        type: 'horizontal' as const,
        shadeIntensity: 0.5,
        gradientToColors: ['#ef4444'],
        inverseColors: true,
        opacityFrom: 1,
        opacityTo: 1,
        stops: [0, 100],
      },
    },
    stroke: {
      lineCap: 'round' as const,
    },
    labels: ['Score'],
  }), []);

  // Memoize risk level calculation
  const getRiskLevel = useCallback((score: number, apiRiskLevel?: string) => {
    // Use API risk level if available, otherwise calculate from score
    if (apiRiskLevel) {
      const level = apiRiskLevel.toLowerCase();
      if (level === 'critical') return { level: 'Critical', color: 'text-red-600' };
      if (level === 'high') return { level: 'High', color: 'text-orange-600' };
      if (level === 'medium') return { level: 'Medium', color: 'text-yellow-600' };
      return { level: 'Low', color: 'text-green-600' };
    }

    // Fallback to score-based calculation
    if (score >= 80) return { level: 'Critical', color: 'text-red-600' };
    if (score >= 60) return { level: 'High', color: 'text-orange-600' };
    if (score >= 40) return { level: 'Medium', color: 'text-yellow-600' };
    return { level: 'Low', color: 'text-green-600' };
  }, []);

  // Memoize risk level info
  const riskLevelInfo = useMemo(() =>
    getRiskLevel(displayValues.displayScore, displayValues.displayRiskLevel),
    [getRiskLevel, displayValues.displayScore, displayValues.displayRiskLevel]
  );

  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow h-full flex flex-col">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Score</h3>
        </div>
        <div className="p-4 flex flex-col items-center justify-center flex-1">
          <i className="fas fa-spinner fa-spin text-2xl text-gray-400 mb-2"></i>
          <span className="text-sm text-gray-500">Loading risk score...</span>
        </div>
      </div>
    );
  }

  if (!selectedBuild && !riskScoreData) {
    if (!hasBuilds) {
      // Show empty state when no builds are available
      return (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow h-full flex flex-col">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Score</h3>
          </div>
          <div className="p-4 flex flex-col items-center justify-center flex-1 text-center">
            <i className="fas fa-shield-alt text-4xl text-gray-400 mb-4"></i>
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No Risk Data</h4>
            <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm">
              Risk scores will appear here after you trigger your first build in Jenkins.
            </p>
          </div>
        </div>
      );
    } else {
      // Show selection prompt when builds are available but none selected
      return (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow h-full flex flex-col">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Score</h3>
          </div>
          <div className="p-4 flex flex-col items-center justify-center flex-1">
            <i className="fas fa-chart-pie text-2xl text-gray-400 mb-2"></i>
            <span className="text-sm text-gray-500">Select a build to view risk score</span>
          </div>
        </div>
      );
    }
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow h-full flex flex-col card-hover transition-all">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Score</h3>
        {selectedBuild && (
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            {selectedBuild.originalJobName} #{selectedBuild.buildId}
          </p>
        )}
      </div>
      <div className="p-4 flex flex-col items-center flex-1">
        <div className="gauge-container mb-4 relative flex-shrink-0 chart-container filter-transition">
          <Chart options={options} series={[displayValues.displayScore]} type="radialBar" height={180} />
          <div className="gauge-value text-gray-900 dark:text-gray-100 transition-all">{displayValues.displayScore}</div>
        </div>

        <div className="w-full flex-1 flex flex-col justify-between">
          <div className="space-y-3 mb-4">
            <div className="flex justify-between">
              <span className="text-xs text-gray-500 dark:text-gray-400">Previous Score</span>
              <span className="text-xs font-medium text-gray-900 dark:text-gray-100">{displayValues.displayPreviousScore}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-xs text-gray-500 dark:text-gray-400">Change</span>
              <span className={`text-xs font-medium ${displayValues.displayChange >= 0 ? 'text-red-600' : 'text-green-600'}`}>
                {displayValues.displayChange >= 0 ? '+' : ''}{displayValues.displayChange}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-xs text-gray-500 dark:text-gray-400">Risk Level</span>
              <span className={`text-xs font-medium ${riskLevelInfo.color}`}>{riskLevelInfo.level}</span>
            </div>
          </div>

          <button
            onClick={openModal}
            className="w-full bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm btn-animated transition-all mt-auto"
          >
            View Risk Details
          </button>
        </div>
      </div>

      {/* Risk Details Modal */}
      <RiskDetailsModal
        isOpen={isModalOpen}
        onClose={closeModal}
        selectedBuild={selectedBuild ? {
          conversationId: selectedBuild.originalJobName,
          buildNumber: selectedBuild.buildId
        } : null}
      />
    </div>
  );
});

// Add display name for better debugging
RiskScoreGauge.displayName = 'RiskScoreGauge';

export default RiskScoreGauge;
