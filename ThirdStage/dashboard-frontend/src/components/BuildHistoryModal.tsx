import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { dashboardApi } from '../services/api';
import type { RecentJobBuildDTO } from '../services/api';

interface BuildHistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobName: string;
}

const BuildHistoryModal: React.FC<BuildHistoryModalProps> = ({ isOpen, onClose, jobName }) => {
  const [buildHistory, setBuildHistory] = useState<RecentJobBuildDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [isNotificationFadingOut, setIsNotificationFadingOut] = useState(false);

  useEffect(() => {
    if (isOpen && jobName) {
      fetchBuildHistory();
    }
  }, [isOpen, jobName]);

  const fetchBuildHistory = async () => {
    setLoading(true);
    setError(null);

    try {
      // Use the recent job builds API to get build history for this specific job
      const builds = await dashboardApi.getRecentJobBuildsByName(jobName);
      setBuildHistory(builds);
    } catch (err) {
      console.error('Failed to fetch build history:', err);
      setError('Failed to load build history');
      // Mock data for demonstration
      setBuildHistory([
        {
          jobName: jobName,
          buildId: 135,
          healthStatus: 'CRITICAL',
          anomalyCount: 8,
          timeAgo: '2 hours ago',
          rawTimestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
          computedAt: new Date().toISOString(),
          originalJobName: jobName
        },
        {
          jobName: jobName,
          buildId: 134,
          healthStatus: 'WARNING',
          anomalyCount: 3,
          timeAgo: '6 hours ago',
          rawTimestamp: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
          computedAt: new Date().toISOString(),
          originalJobName: jobName
        },
        {
          jobName: jobName,
          buildId: 133,
          healthStatus: 'Healthy',
          anomalyCount: 0,
          timeAgo: '1 day ago',
          rawTimestamp: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          computedAt: new Date().toISOString(),
          originalJobName: jobName
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const getHealthBadge = (health: string) => {
    switch (health?.toLowerCase()) {
      case 'healthy':
        return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200';
      case 'warning':
        return 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200';
      case 'unhealthy':
        return 'bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200';
      case 'critical':
        return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200';
      default:
        return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200';
    }
  };

  const getAnomalyColor = (count: number) => {
    if (count === 0) return 'text-green-600 dark:text-green-400';
    if (count <= 2) return 'text-yellow-600 dark:text-yellow-400';
    if (count <= 5) return 'text-orange-600 dark:text-orange-400';
    return 'text-red-600 dark:text-red-400';
  };

  const copyBuildHistory = () => {
    const content = `
Build History for ${jobName}

${buildHistory.map(build => `
Build #${build.buildId}
Health Status: ${build.healthStatus}
Anomaly Count: ${build.anomalyCount}
Time: ${build.timeAgo}
Timestamp: ${build.rawTimestamp}
`).join('\n')}

Total Builds: ${buildHistory.length}
Generated from Build History
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
      console.error('Failed to copy build history:', err);
    });
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="fixed inset-0 bg-black bg-opacity-50" onClick={onClose}></div>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-4xl z-10 overflow-hidden relative mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <div>
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Build History</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Recent builds for {jobName}</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {loading && (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              <span className="ml-3 text-gray-600 dark:text-gray-400">Loading build history...</span>
            </div>
          )}

          {error && (
            <div className="bg-red-50 dark:bg-red-900 p-4 rounded-lg mb-6">
              <div className="flex items-center">
                <i className="fas fa-exclamation-triangle text-red-500 dark:text-red-400 mr-3"></i>
                <span className="text-red-800 dark:text-red-200">{error}</span>
              </div>
            </div>
          )}

          {buildHistory.length > 0 && (
            <>
              {/* Summary Stats */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg text-center">
                  <div className="text-2xl font-bold text-gray-900 dark:text-gray-100">{buildHistory.length}</div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">Total Builds</div>
                </div>
                <div className="bg-green-50 dark:bg-green-900 p-4 rounded-lg text-center">
                  <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                    {buildHistory.filter(b => b.healthStatus?.toLowerCase() === 'healthy').length}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">Healthy</div>
                </div>
                <div className="bg-yellow-50 dark:bg-yellow-900 p-4 rounded-lg text-center">
                  <div className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">
                    {buildHistory.filter(b => b.healthStatus?.toLowerCase() === 'warning').length}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">Warning</div>
                </div>
                <div className="bg-red-50 dark:bg-red-900 p-4 rounded-lg text-center">
                  <div className="text-2xl font-bold text-red-600 dark:text-red-400">
                    {buildHistory.filter(b => b.healthStatus?.toLowerCase() === 'critical').length}
                  </div>
                  <div className="text-sm text-gray-500 dark:text-gray-400">Critical</div>
                </div>
              </div>

              {/* Build History Table */}
              <div className="overflow-x-auto">
                <table className="min-w-full">
                  <thead>
                    <tr className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider border-b border-gray-200 dark:border-gray-700">
                      <th className="px-4 py-3">Build #</th>
                      <th className="px-4 py-3">Health Status</th>
                      <th className="px-4 py-3">Anomalies</th>
                      <th className="px-4 py-3">Time</th>
                      <th className="px-4 py-3">Timestamp</th>
                      <th className="px-4 py-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                    {buildHistory.map((build, index) => (
                      <tr key={`${build.buildId}-${index}`} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td className="px-4 py-3 text-sm font-medium text-gray-900 dark:text-gray-100">
                          #{build.buildId}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`text-xs px-2 py-1 rounded ${getHealthBadge(build.healthStatus)}`}>
                            {build.healthStatus}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <span className={`text-sm font-bold ${getAnomalyColor(build.anomalyCount)}`}>
                            {build.anomalyCount}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-800 dark:text-gray-200">
                          {build.timeAgo}
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-600 dark:text-gray-400 font-mono">
                          {build.rawTimestamp ? new Date(build.rawTimestamp).toLocaleString() : 'N/A'}
                        </td>
                        <td className="px-4 py-3 text-sm">
                          <div className="flex space-x-2">
                            <button 
                              className="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
                              title="View build details"
                            >
                              <i className="fas fa-eye text-sm"></i>
                            </button>
                            <button 
                              className="text-green-500 hover:text-green-700 dark:text-green-400 dark:hover:text-green-300"
                              title="View logs"
                            >
                              <i className="fas fa-file-alt text-sm"></i>
                            </button>
                            <button 
                              className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
                              title="Rerun build"
                            >
                              <i className="fas fa-redo text-sm"></i>
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Build Trend Analysis */}
              <div className="mt-6 space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Build Trend Analysis</h4>
                <div className="bg-blue-50 dark:bg-blue-900 p-4 rounded-lg">
                  <div className="flex items-start space-x-3">
                    <i className="fas fa-chart-line text-blue-500 dark:text-blue-400 mt-1"></i>
                    <div className="text-sm text-blue-800 dark:text-blue-200">
                      <p className="font-medium mb-2">Recent Build Trends</p>
                      <ul className="space-y-1 text-xs">
                        <li>• Latest build: #{buildHistory[0]?.buildId} ({buildHistory[0]?.healthStatus})</li>
                        <li>• Average anomalies: {(buildHistory.reduce((sum, b) => sum + b.anomalyCount, 0) / buildHistory.length).toFixed(1)}</li>
                        <li>• Health trend: {buildHistory.length >= 2 && buildHistory[0].anomalyCount < buildHistory[1].anomalyCount ? 'Improving' : 'Stable'}</li>
                        <li>• Total builds analyzed: {buildHistory.length}</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}

          {!loading && !error && buildHistory.length === 0 && (
            <div className="text-center py-8">
              <i className="fas fa-history text-4xl text-gray-300 dark:text-gray-600 mb-4"></i>
              <p className="text-gray-500 dark:text-gray-400">No build history available for this job.</p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
          {buildHistory.length > 0 && (
            <button
              onClick={copyBuildHistory}
              className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm transition"
            >
              Copy History
            </button>
          )}
          <button
            onClick={onClose}
            className="bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded text-sm transition"
          >
            Close
          </button>
        </div>
      </div>

      {/* Copy Success Notification */}
      {showCopyNotification && (
        <div className={`fixed top-4 right-4 z-50 transition-all duration-300 ${
          isNotificationFadingOut ? 'opacity-0 transform translate-x-2' : 'opacity-100 transform translate-x-0'
        }`}>
          <div className="bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center space-x-2">
            <i className="fas fa-check-circle"></i>
            <span>Build history copied to clipboard!</span>
          </div>
        </div>
      )}
    </div>,
    document.body
  );
};

export default BuildHistoryModal;
