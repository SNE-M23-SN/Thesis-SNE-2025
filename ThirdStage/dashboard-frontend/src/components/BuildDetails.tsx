import React, { useState, useEffect } from 'react';
import type { RecentJobBuildDTO, BuildSummaryDTO, RiskScoreDTO} from '../services/api';
import {dashboardApi} from '../services/api';
import BuildLogsModal from './BuildLogsModal';


interface BuildDetailsProps {
  selectedBuild: RecentJobBuildDTO | null;
  hasBuilds?: boolean;
  onShowNotification?: (title: string, message: string, duration?: number) => void;
  onShowError?: (title: string, message: string, duration?: number) => void;
}

const BuildDetails: React.FC<BuildDetailsProps> = ({
  selectedBuild,
  hasBuilds = true,
  onShowNotification,
  onShowError
}) => {
  const [buildSummary, setBuildSummary] = useState<BuildSummaryDTO | null>(null);
  const [riskScore, setRiskScore] = useState<RiskScoreDTO | null>(null);
  const [logsTracker, setLogsTracker] = useState<{ received: number; expected: number; status: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const [isLogsModalOpen, setIsLogsModalOpen] = useState(false);

  useEffect(() => {
    if (selectedBuild) {
      loadBuildDetails();
    }
  }, [selectedBuild]);

  const loadBuildDetails = async () => {
    if (!selectedBuild) return;

    try {
      setLoading(true);
      
      const [summaryResponse, riskResponse, trackerResponse] = await Promise.all([
        dashboardApi.getBuildSummary(selectedBuild.originalJobName, selectedBuild.buildId),
        dashboardApi.getRiskScore(selectedBuild.originalJobName, selectedBuild.buildId),
        dashboardApi.getLogsTracker(selectedBuild.originalJobName, selectedBuild.buildId)
      ]);

      if (summaryResponse.hasAiData && summaryResponse.data) {
        setBuildSummary(summaryResponse.data);
      } else {
        setBuildSummary(null);
      }

      if (riskResponse.hasData && riskResponse.data) {
        setRiskScore(riskResponse.data);
      } else {
        setRiskScore(null);
      }

      setLogsTracker(trackerResponse);
    } catch (error) {
      console.error('Failed to load build details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRerunBuild = async () => {
    if (!selectedBuild) return;

    try {
      const response = await dashboardApi.rerunBuild(selectedBuild.originalJobName, selectedBuild.buildId);
      console.log('✅ Build rerun response:', response);

      // Show success notification
      if (onShowNotification) {
        onShowNotification(
          'Build Rerun Triggered',
          `Successfully triggered rerun for ${selectedBuild.originalJobName} #${selectedBuild.buildId}`,
          6000
        );
      }
    } catch (error) {
      console.error('❌ Failed to rerun build:', error);

      // Show error notification
      if (onShowError) {
        let errorMessage = 'Failed to trigger build rerun. Please try again.';

        // Extract more specific error message if available
        if (error && typeof error === 'object' && 'response' in error) {
          const axiosError = error as { response?: { data?: { message?: string } } };
          if (axiosError.response?.data?.message) {
            errorMessage = axiosError.response.data.message;
          }
        } else if (error instanceof Error) {
          errorMessage = error.message;
        }

        onShowError(
          'Build Rerun Failed',
          errorMessage,
          8000
        );
      }
    }
  };

  if (!selectedBuild) {
    if (!hasBuilds) {
      // Show empty state when no builds are available
      return (
        <div className="card">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Build Summary</h3>
          </div>
          <div className="p-8 flex flex-col items-center justify-center text-center">
            <i className="fas fa-hammer text-4xl text-gray-400 mb-4"></i>
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No Builds Available</h4>
            <p className="text-sm text-gray-500 dark:text-gray-400 mb-4 max-w-md">
              To see build summaries and security analysis, you need to trigger a build in Jenkins first.
            </p>
            <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 max-w-md">
              <p className="text-sm text-blue-800 dark:text-blue-200">
                <i className="fas fa-info-circle mr-2"></i>
                Trigger a build in Jenkins to start seeing detailed security analysis and risk scores.
              </p>
            </div>
          </div>
        </div>
      );
    } else {
      // Show selection prompt when builds are available but none selected
      return (
        <div className="card p-6">
          <div className="text-center text-gray-500 dark:text-gray-400">
            <p>Select a build to view details</p>
          </div>
        </div>
      );
    }
  }

  return (
    <div className="card h-full flex flex-col card-hover transition-all">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">Build Summary</h3>
        <div className="flex space-x-2">
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {selectedBuild.originalJobName} #{selectedBuild.buildId}
          </span>
          <span className={`badge ${
            selectedBuild.healthStatus === 'Healthy' ? 'badge-success' :
            selectedBuild.healthStatus === 'WARNING' ? 'badge-warning' :
            'badge-critical'
          }`}>
            {selectedBuild.healthStatus}
          </span>
        </div>
      </div>

      <div className="p-4 flex-1 flex flex-col">
        {loading ? (
          <div className="text-center py-4 loading-fade-enter-active">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto gpu-accelerated"></div>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400 content-fade-enter-active">Loading build details...</p>
          </div>
        ) : (
          <>
            {buildSummary ? (
              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">AI Summary</h4>
                <p className="text-sm text-gray-800 dark:text-gray-200 mb-4">
                  {buildSummary.build_summary}
                </p>
                
                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Build Started</h4>
                    <p className="text-sm text-gray-800 dark:text-gray-200">
                      {buildSummary.build_started_time}
                    </p>
                  </div>
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Build Duration</h4>
                    <p className="text-sm text-gray-800 dark:text-gray-200">{buildSummary.build_duration}</p>
                  </div>
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Regression Detected</h4>
                    <p className={`text-sm font-medium ${
                      buildSummary.regression_detected ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'
                    }`}>
                      {buildSummary.regression_detected ? 'Yes' : 'No'}
                    </p>
                  </div>
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Status</h4>
                    <p className={`text-sm font-medium ${
                      buildSummary.health_status === 'Healthy' ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                    }`}>
                      {buildSummary.health_status}
                    </p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="mb-4 p-4 bg-yellow-50 dark:bg-yellow-900 border-l-4 border-yellow-400 rounded">
                <p className="text-sm text-yellow-800 dark:text-yellow-200">
                  AI analysis not available for this build yet.
                </p>
              </div>
            )}
            
            {logsTracker && (
              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">Logs Tracker</h4>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    Received {logsTracker.received}/{logsTracker.expected} logs
                  </span>
                  <span className={`text-xs font-medium ${
                    logsTracker.status === 'Complete' ? 'text-green-600 dark:text-green-400' : 'text-yellow-600 dark:text-yellow-400'
                  }`}>
                    {logsTracker.status}
                  </span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                  <div 
                    className={`h-2 rounded-full transition-all duration-500 ${
                      logsTracker.status === 'Complete' ? 'bg-green-500' : 'bg-yellow-500'
                    }`}
                    style={{ width: `${(logsTracker.received / logsTracker.expected) * 100}%` }}
                  ></div>
                </div>
              </div>
            )}

            <div className="flex space-x-2 mt-auto">
              <button
                onClick={() => setIsLogsModalOpen(true)}
                className="btn-primary text-sm btn-primary-animated transition-all"
              >
                <i className="fas fa-file-alt mr-2"></i>
                View All Logs
              </button>
              <button
                onClick={handleRerunBuild}
                className="btn-secondary text-sm btn-animated transition-all"
              >
                <i className="fas fa-redo mr-2"></i>
                Rerun Build
              </button>
            </div>
          </>
        )}
      </div>

      {/* Build Logs Modal */}
      <BuildLogsModal
        isOpen={isLogsModalOpen}
        onClose={() => setIsLogsModalOpen(false)}
        selectedBuild={selectedBuild}
      />
    </div>
  );
};

export default BuildDetails;
