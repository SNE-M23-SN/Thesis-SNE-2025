import React, { useState } from 'react';
import { createPortal } from 'react-dom';
import type { RecentJobBuildDTO } from '../services/api';

interface BuildDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  build: RecentJobBuildDTO | null; // Build object from Recent Job Builds
}

const BuildDetailsModal: React.FC<BuildDetailsModalProps> = ({ isOpen, onClose, build }) => {
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [isNotificationFadingOut, setIsNotificationFadingOut] = useState(false);

  if (!isOpen || !build) return null;

  const getStatusBadge = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'success':
        return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200';
      case 'failure':
      case 'failed':
        return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200';
      case 'unstable':
        return 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200';
      case 'running':
      case 'in_progress':
        return 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200';
      case 'aborted':
        return 'bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-gray-200';
      default:
        return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200';
    }
  };

  const copyBuildDetails = () => {
    if (!build) return;

    const buildContent = `
Build Details

Job Name: ${build.jobName || 'N/A'}
Original Job Name: ${build.originalJobName || 'N/A'}
Build ID: ${build.buildId || 'N/A'}
Health Status: ${build.healthStatus || 'N/A'}
Anomaly Count: ${build.anomalyCount || 0}
Time Ago: ${build.timeAgo || 'N/A'}
Raw Timestamp: ${build.rawTimestamp || 'N/A'}
Computed At: ${build.computedAt || 'N/A'}

This build information is sourced from the recent_job_builds materialized view
and represents the latest available data for this Jenkins job build.
    `.trim();

    navigator.clipboard.writeText(buildContent).then(() => {
      // Show success notification
      setShowCopyNotification(true);
      setIsNotificationFadingOut(false);

      // Start fade-out animation after 2.5 seconds
      setTimeout(() => {
        setIsNotificationFadingOut(true);
        // Remove notification after fade-out animation completes
        setTimeout(() => {
          setShowCopyNotification(false);
          setIsNotificationFadingOut(false);
        }, 300);
      }, 2500);
    }).catch(err => {
      console.error('Failed to copy build details to clipboard:', err);
    });
  };

  return createPortal(
    <div className="fixed inset-0 flex items-center justify-center z-50 modal-backdrop-enter-active">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-all" onClick={onClose}></div>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-4xl z-10 overflow-hidden relative mx-4 max-h-[90vh] overflow-y-auto modal-content-enter-active gpu-accelerated">
        {/* Header */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Build Details</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 btn-icon-animated transition-all"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Build Overview */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Build Information</h4>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Job Name:</span>
                  <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{build.jobName || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Original Job Name:</span>
                  <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{build.originalJobName || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Build ID:</span>
                  <span className="text-sm font-medium text-gray-900 dark:text-gray-100">#{build.buildId || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Health Status:</span>
                  <span className={`text-xs px-2 py-1 rounded ${getStatusBadge(build.healthStatus)}`}>
                    {build.healthStatus || 'Unknown'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Time Ago:</span>
                  <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{build.timeAgo || 'N/A'}</span>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Anomaly Information</h4>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Anomaly Count:</span>
                  <span className={`text-sm font-bold ${
                    (build.anomalyCount || 0) === 0
                      ? 'text-green-600 dark:text-green-400'
                      : (build.anomalyCount || 0) < 5
                        ? 'text-yellow-600 dark:text-yellow-400'
                        : 'text-red-600 dark:text-red-400'
                  }`}>
                    {build.anomalyCount || 0}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Raw Timestamp:</span>
                  <span className="text-sm font-mono text-gray-900 dark:text-gray-100">
                    {build.rawTimestamp ? new Date(build.rawTimestamp).toLocaleString() : 'N/A'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500 dark:text-gray-400">Computed At:</span>
                  <span className="text-sm font-mono text-gray-900 dark:text-gray-100">
                    {build.computedAt ? new Date(build.computedAt).toLocaleString() : 'N/A'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Data Source Information */}
          <div className="space-y-4">
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Data Source</h4>
            <div className="bg-blue-50 dark:bg-blue-900 p-4 rounded-lg">
              <div className="flex items-start space-x-3">
                <i className="fas fa-info-circle text-blue-500 dark:text-blue-400 mt-1"></i>
                <div className="text-sm text-blue-800 dark:text-blue-200">
                  <p className="font-medium mb-2">Recent Job Builds Materialized View</p>
                  <p>This build information is sourced from the <code className="bg-blue-100 dark:bg-blue-800 px-1 rounded">recent_job_builds</code> materialized view,
                  which provides aggregated build data with health status calculations and anomaly counts.</p>
                  <p className="mt-2">
                    <strong>Data Freshness:</strong> Updated every 15 minutes via scheduled sync<br/>
                    <strong>Last Computed:</strong> {build.computedAt ? new Date(build.computedAt).toLocaleString() : 'N/A'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Anomaly Severity Guide */}
          <div className="space-y-4">
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Anomaly Severity Levels</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3">
              <div className="bg-blue-50 dark:bg-blue-900 p-3 rounded-lg text-center">
                <div className="text-sm font-medium text-blue-800 dark:text-blue-200">LOW</div>
                <div className="text-xs text-blue-600 dark:text-blue-400 mt-1">Minor issues</div>
              </div>
              <div className="bg-yellow-50 dark:bg-yellow-900 p-3 rounded-lg text-center">
                <div className="text-sm font-medium text-yellow-800 dark:text-yellow-200">WARNING</div>
                <div className="text-xs text-yellow-600 dark:text-yellow-400 mt-1">Needs attention</div>
              </div>
              <div className="bg-orange-50 dark:bg-orange-900 p-3 rounded-lg text-center">
                <div className="text-sm font-medium text-orange-800 dark:text-orange-200">MEDIUM</div>
                <div className="text-xs text-orange-600 dark:text-orange-400 mt-1">Moderate risk</div>
              </div>
              <div className="bg-red-50 dark:bg-red-900 p-3 rounded-lg text-center">
                <div className="text-sm font-medium text-red-800 dark:text-red-200">HIGH</div>
                <div className="text-xs text-red-600 dark:text-red-400 mt-1">Serious issues</div>
              </div>
              <div className="bg-purple-50 dark:bg-purple-900 p-3 rounded-lg text-center">
                <div className="text-sm font-medium text-purple-800 dark:text-purple-200">CRITICAL</div>
                <div className="text-xs text-purple-600 dark:text-purple-400 mt-1">Urgent action</div>
              </div>
            </div>
            <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                <strong>Build Health Status</strong> is calculated based on the total count and severity of anomalies detected.
                Higher anomaly counts and more severe anomalies result in worse health status.
              </p>
            </div>
          </div>

          {/* Additional Actions */}
          <div className="space-y-4">
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Additional Information</h4>
            <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                For detailed build logs, test results, and security scan information, use the "View All Logs" button
                in the Build Summary section of the dashboard.
              </p>
              <div className="flex flex-wrap gap-2">
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200">
                  <i className="fas fa-file-alt mr-1"></i>
                  Build Logs Available
                </span>
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200">
                  <i className="fas fa-shield-alt mr-1"></i>
                  Security Scans
                </span>
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200">
                  <i className="fas fa-chart-line mr-1"></i>
                  Anomaly Detection
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
          <button
            onClick={copyBuildDetails}
            className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm btn-animated transition-all"
          >
            Copy Details
          </button>
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
            <span>Build details copied to clipboard!</span>
          </div>
        </div>
      )}
    </div>,
    document.body
  );
};

export default BuildDetailsModal;
