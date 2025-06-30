import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { dashboardApi } from '../services/api';

interface RiskDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedBuild: any; // Build object with conversationId and buildNumber
}

interface RiskScoreData {
  score: number;
  change: number | null;
  riskLevel: string;
  previousScore: number | null;
}

const RiskDetailsModal: React.FC<RiskDetailsModalProps> = ({ isOpen, onClose, selectedBuild }) => {
  const [riskData, setRiskData] = useState<RiskScoreData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [isNotificationFadingOut, setIsNotificationFadingOut] = useState(false);

  useEffect(() => {
    if (isOpen && selectedBuild) {
      fetchRiskData();
    }
  }, [isOpen, selectedBuild]);

  const fetchRiskData = async () => {
    if (!selectedBuild?.conversationId || !selectedBuild?.buildNumber) {
      setError('Missing build information');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await dashboardApi.getRiskScore(selectedBuild.conversationId, selectedBuild.buildNumber);
      if (response.hasData && response.data) {
        setRiskData(response.data);
      } else {
        setError(response.message || 'No risk data available');
      }
    } catch (err) {
      console.error('Failed to fetch risk data:', err);
      setError('Failed to load risk data');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const getRiskLevelInfo = (level: string) => {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return { color: 'text-green-600 dark:text-green-400', bg: 'bg-green-50 dark:bg-green-900', description: 'Minimal issues, good build health' };
      case 'MEDIUM':
        return { color: 'text-yellow-600 dark:text-yellow-400', bg: 'bg-yellow-50 dark:bg-yellow-900', description: 'Moderate issues, some attention needed' };
      case 'HIGH':
        return { color: 'text-orange-600 dark:text-orange-400', bg: 'bg-orange-50 dark:bg-orange-900', description: 'Significant issues, review required' };
      case 'CRITICAL':
        return { color: 'text-red-600 dark:text-red-400', bg: 'bg-red-50 dark:bg-red-900', description: 'Severe issues, immediate action required' };
      default:
        return { color: 'text-gray-600 dark:text-gray-400', bg: 'bg-gray-50 dark:bg-gray-700', description: 'Risk level not determined' };
    }
  };

  const getScoreColor = (score: number) => {
    if (score <= 25) return 'text-green-600 dark:text-green-400';
    if (score <= 50) return 'text-yellow-600 dark:text-yellow-400';
    if (score <= 75) return 'text-orange-600 dark:text-orange-400';
    return 'text-red-600 dark:text-red-400';
  };

  const getRiskFactors = (score: number, level: string) => {
    const factors = [];
    
    if (score >= 75) {
      factors.push({ factor: 'Critical Anomalies', impact: 'High', description: 'Multiple critical severity anomalies detected' });
      factors.push({ factor: 'Security Issues', impact: 'High', description: 'Security vulnerabilities or exposures found' });
    } else if (score >= 50) {
      factors.push({ factor: 'High Severity Anomalies', impact: 'Medium', description: 'High severity anomalies present' });
      factors.push({ factor: 'Performance Issues', impact: 'Medium', description: 'Build performance degradation detected' });
    } else if (score >= 25) {
      factors.push({ factor: 'Medium Severity Anomalies', impact: 'Low', description: 'Some medium severity issues found' });
      factors.push({ factor: 'Configuration Issues', impact: 'Low', description: 'Minor configuration problems detected' });
    } else {
      factors.push({ factor: 'Low Severity Anomalies', impact: 'Minimal', description: 'Only minor issues detected' });
    }

    return factors;
  };

  const getRecommendations = (level: string) => {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return [
          'Proceed with deployment with minimal oversight',
          'Continue monitoring for any emerging patterns',
          'Consider this build safe for production'
        ];
      case 'MEDIUM':
        return [
          'Review identified issues before deployment',
          'Consider additional testing in staging environment',
          'Monitor deployment closely for any issues'
        ];
      case 'HIGH':
        return [
          'Investigate and resolve issues before deployment',
          'Require additional approval for production deployment',
          'Implement additional monitoring and rollback procedures'
        ];
      case 'CRITICAL':
        return [
          'Block deployment until issues are resolved',
          'Conduct thorough investigation of all anomalies',
          'Require security and architecture team review'
        ];
      default:
        return ['Risk assessment not available'];
    }
  };

  const copyRiskDetails = () => {
    if (!riskData) return;

    const content = `
Risk Assessment Details

Build: ${selectedBuild?.conversationId || 'N/A'} #${selectedBuild?.buildNumber || 'N/A'}
Risk Score: ${riskData.score}/100
Risk Level: ${riskData.riskLevel}
Change from Previous: ${riskData.change !== null ? (riskData.change >= 0 ? '+' : '') + riskData.change : 'N/A'}
Previous Score: ${riskData.previousScore || 'N/A'}

Risk Factors:
${getRiskFactors(riskData.score, riskData.riskLevel).map(f => `- ${f.factor}: ${f.description}`).join('\n')}

Recommendations:
${getRecommendations(riskData.riskLevel).map(r => `- ${r}`).join('\n')}

Generated by CI Anomaly Detector AI Analysis
    `.trim();

    navigator.clipboard.writeText(content).then(() => {
      setShowCopyNotification(true);
      setIsNotificationFadingOut(false);

      setTimeout(() => {
        setIsNotificationFadingOut(true);
        setTimeout(() => {
          setShowCopyNotification(false);
          setIsNotificationFadingOut(false);
        }, 300);
      }, 2500);
    }).catch(err => {
      console.error('Failed to copy risk details:', err);
    });
  };

  const riskLevelInfo = riskData ? getRiskLevelInfo(riskData.riskLevel) : null;

  return createPortal(
    <div className="fixed inset-0 flex items-center justify-center z-50 modal-backdrop-enter-active">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-all" onClick={onClose}></div>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-4xl z-10 overflow-hidden relative mx-4 max-h-[90vh] overflow-y-auto modal-content-enter-active gpu-accelerated">
        {/* Header */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Risk Assessment Details</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 btn-icon-animated transition-all"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {loading && (
            <div className="flex items-center justify-center py-8 loading-fade-enter-active">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 gpu-accelerated"></div>
              <span className="ml-3 text-gray-600 dark:text-gray-400 content-fade-enter-active">Loading risk data...</span>
            </div>
          )}

          {error && (
            <div className="bg-red-50 dark:bg-red-900 p-4 rounded-lg">
              <div className="flex items-center">
                <i className="fas fa-exclamation-triangle text-red-500 dark:text-red-400 mr-3"></i>
                <span className="text-red-800 dark:text-red-200">{error}</span>
              </div>
            </div>
          )}

          {riskData && riskLevelInfo && (
            <>
              {/* Risk Score Overview */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Risk Score</h4>
                  <div className="text-center">
                    <div className={`text-6xl font-bold ${getScoreColor(riskData.score)}`}>
                      {riskData.score}
                    </div>
                    <div className="text-sm text-gray-500 dark:text-gray-400 mt-1">out of 100</div>
                    {riskData.change !== null && (
                      <div className={`text-sm mt-2 ${riskData.change >= 0 ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'}`}>
                        {riskData.change >= 0 ? '↑' : '↓'} {Math.abs(riskData.change)} from previous build
                      </div>
                    )}
                  </div>
                </div>

                <div className="space-y-4">
                  <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Risk Level</h4>
                  <div className={`p-4 rounded-lg ${riskLevelInfo.bg}`}>
                    <div className={`text-2xl font-bold ${riskLevelInfo.color} text-center`}>
                      {riskData.riskLevel}
                    </div>
                    <div className="text-sm text-gray-600 dark:text-gray-400 text-center mt-2">
                      {riskLevelInfo.description}
                    </div>
                  </div>
                  {riskData.previousScore !== null && (
                    <div className="text-sm text-gray-600 dark:text-gray-400 text-center">
                      Previous build score: {riskData.previousScore}
                    </div>
                  )}
                </div>
              </div>

              {/* Risk Factors */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Risk Factors Analysis</h4>
                <div className="space-y-3">
                  {getRiskFactors(riskData.score, riskData.riskLevel).map((factor, index) => (
                    <div key={index} className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <div className="font-medium text-gray-900 dark:text-gray-100">{factor.factor}</div>
                          <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">{factor.description}</div>
                        </div>
                        <span className={`text-xs px-2 py-1 rounded ${
                          factor.impact === 'High' ? 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200' :
                          factor.impact === 'Medium' ? 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200' :
                          factor.impact === 'Low' ? 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200' :
                          'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200'
                        }`}>
                          {factor.impact} Impact
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Risk Level Guide */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Risk Level Guide</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
                  <div className="bg-green-50 dark:bg-green-900 p-3 rounded-lg text-center">
                    <div className="text-sm font-medium text-green-800 dark:text-green-200">LOW</div>
                    <div className="text-xs text-green-600 dark:text-green-400 mt-1">0-25 score</div>
                  </div>
                  <div className="bg-yellow-50 dark:bg-yellow-900 p-3 rounded-lg text-center">
                    <div className="text-sm font-medium text-yellow-800 dark:text-yellow-200">MEDIUM</div>
                    <div className="text-xs text-yellow-600 dark:text-yellow-400 mt-1">26-50 score</div>
                  </div>
                  <div className="bg-orange-50 dark:bg-orange-900 p-3 rounded-lg text-center">
                    <div className="text-sm font-medium text-orange-800 dark:text-orange-200">HIGH</div>
                    <div className="text-xs text-orange-600 dark:text-orange-400 mt-1">51-75 score</div>
                  </div>
                  <div className="bg-red-50 dark:bg-red-900 p-3 rounded-lg text-center">
                    <div className="text-sm font-medium text-red-800 dark:text-red-200">CRITICAL</div>
                    <div className="text-xs text-red-600 dark:text-red-400 mt-1">76-100 score</div>
                  </div>
                </div>
              </div>

              {/* Recommendations */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Recommendations</h4>
                <div className={`p-4 rounded-lg ${riskLevelInfo.bg}`}>
                  <div className={`font-medium ${riskLevelInfo.color} mb-3`}>
                    Actions for {riskData.riskLevel} Risk Level:
                  </div>
                  <ul className="space-y-2">
                    {getRecommendations(riskData.riskLevel).map((recommendation, index) => (
                      <li key={index} className="flex items-start">
                        <i className="fas fa-check-circle text-gray-400 mr-2 mt-0.5 text-sm"></i>
                        <span className="text-sm text-gray-700 dark:text-gray-300">{recommendation}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              {/* AI Analysis Info */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">AI Analysis Details</h4>
                <div className="bg-blue-50 dark:bg-blue-900 p-4 rounded-lg">
                  <div className="flex items-start space-x-3">
                    <i className="fas fa-robot text-blue-500 dark:text-blue-400 mt-1"></i>
                    <div className="text-sm text-blue-800 dark:text-blue-200">
                      <p className="font-medium mb-2">AI-Powered Risk Assessment</p>
                      <p>This risk score is calculated by analyzing multiple factors including:</p>
                      <ul className="mt-2 space-y-1 text-xs">
                        <li>• Anomaly severity distribution and count</li>
                        <li>• Security vulnerabilities and exposures</li>
                        <li>• Performance regressions and build quality</li>
                        <li>• Historical patterns and trend analysis</li>
                        <li>• Configuration issues and dependency problems</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
          {riskData && (
            <button
              onClick={copyRiskDetails}
              className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm btn-animated transition-all"
            >
              Copy Assessment
            </button>
          )}
          <button
            onClick={onClose}
            className="bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded text-sm btn-primary-animated transition-all"
          >
            Close
          </button>
        </div>
      </div>

      {/* Copy Success Notification */}
      {showCopyNotification && (
        <div className={`fixed top-4 right-4 z-50 ${
          isNotificationFadingOut ? 'notification-exit-active' : 'notification-enter-active'
        }`}>
          <div className="bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center space-x-2 gpu-accelerated">
            <i className="fas fa-check-circle"></i>
            <span>Risk assessment copied to clipboard!</span>
          </div>
        </div>
      )}
    </div>,
    document.body
  );
};

export default RiskDetailsModal;
