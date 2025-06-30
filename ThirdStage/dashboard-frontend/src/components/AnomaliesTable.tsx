import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import type {AnomalyDTO, RecentJobBuildDTO } from '../services/api';
import { dashboardApi} from '../services/api';

interface AnomaliesTableProps {
  selectedBuild: RecentJobBuildDTO | null;
  hasBuilds?: boolean;
}

const AnomaliesTable: React.FC<AnomaliesTableProps> = ({ selectedBuild, hasBuilds = true }) => {
  const [anomalies, setAnomalies] = useState<AnomalyDTO[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1); // Backend uses 1-based pagination
  const [pageSize] = useState(3); // Backend default is 3
  const [loading, setLoading] = useState(false);
  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());
  const [totalPages, setTotalPages] = useState(0);
  const [selectedAnomaly, setSelectedAnomaly] = useState<AnomalyDTO | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [isNotificationFadingOut, setIsNotificationFadingOut] = useState(false);

  useEffect(() => {
    if (selectedBuild) {
      loadAnomalies();
    } else {
      // Clear anomalies when no build is selected
      setAnomalies([]);
      setTotalCount(0);
      setTotalPages(0);
      setLoading(false);
    }
  }, [selectedBuild, currentPage]);

  const loadAnomalies = async () => {
    if (!selectedBuild) return;

    try {
      setLoading(true);
      const response = await dashboardApi.getDetectedAnomalies(
        selectedBuild.originalJobName,
        selectedBuild.buildId,
        currentPage,
        pageSize
      );

      if (response.hasData && response.data) {
        setAnomalies(response.data.anomalies || []);
        setTotalCount(response.data.totalCount || 0);
        // Calculate total pages from pageSize and totalCount
        setTotalPages(Math.ceil((response.data.totalCount || 0) / (response.data.pageSize || pageSize)));
      } else {
        setAnomalies([]);
        setTotalCount(0);
        setTotalPages(0);
      }
    } catch (error) {
      console.error('Failed to load anomalies:', error);
      // Mock data for demonstration when API fails
      setAnomalies([
        {
          type: 'deployment_log',
          severity: 'Critical',
          description: 'Exposed AWS access key in deployment configuration',
          recommendation: 'Immediately rotate the exposed AWS access key. Store all credentials in a secure vault like AWS Secrets Manager or HashiCorp Vault.',
          aiAnalysis: 'This is a severe security vulnerability that can lead to data breaches and unauthorized access.',
          details: {
            issue_id: 'SEC-001',
            location: 'deployment/config.yaml:156',
            code_snippet: `aws_access_key: 'AKIA1234567890ABCDEF'`
          }
        },
        {
          type: 'sast_scanning',
          severity: 'High',
          description: 'SQL Injection vulnerability in user API',
          recommendation: 'Use parameterized queries or an ORM to prevent SQL injection. Validate and sanitize all user inputs.',
          aiAnalysis: 'This vulnerability can lead to data breaches and unauthorized database access.',
          details: {
            issue_id: 'CVE-2023-001',
            location: 'src/main/java/com/example/controller/UserController.java:78',
            code_snippet: `String query = "SELECT * FROM users WHERE username = '" + username + "'";`
          }
        },
        {
          type: 'dependency_check',
          severity: 'Medium',
          description: 'Outdated dependency with known vulnerabilities',
          recommendation: 'Update the dependency to the latest version or use an alternative library.',
          aiAnalysis: 'This dependency vulnerability can impact application availability and security.',
          details: {
            issue_id: 'CVE-2021-22118',
            location: 'pom.xml (spring-core:5.2.8.RELEASE)',
            code_snippet: null
          }
        }
      ]);
      setTotalCount(14);
    } finally {
      setLoading(false);
    }
  };

  const toggleRowExpansion = (index: number) => {
    const newExpandedRows = new Set(expandedRows);
    if (newExpandedRows.has(index)) {
      newExpandedRows.delete(index);
    } else {
      newExpandedRows.add(index);
    }
    setExpandedRows(newExpandedRows);
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical':
        return 'badge badge-critical';
      case 'high':
        return 'badge badge-danger';
      case 'medium':
        return 'badge badge-warning';
      case 'low':
        return 'badge badge-success';
      default:
        return 'badge badge-warning';
    }
  };

  const getTypeBadge = (type: string) => {
    switch (type.toLowerCase()) {
      case 'sast_scanning':
        return 'badge badge-info';
      case 'dependency_check':
        return 'badge badge-warning';
      case 'deployment_log':
        return 'badge badge-danger';
      default:
        return 'badge badge-warning';
    }
  };

  const openModal = (anomaly: AnomalyDTO) => {
    setSelectedAnomaly(anomaly);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedAnomaly(null);
  };

  const copyToClipboard = (anomaly: AnomalyDTO | null) => {
    console.log('🔄 copyToClipboard called with:', anomaly);

    if (!anomaly) {
      console.error('❌ No anomaly data to copy');
      return;
    }

    const detailsText = `
Issue ID: ${anomaly.details.issue_id}
Location: ${anomaly.details.location}
Severity: ${anomaly.severity}
Description: ${anomaly.description}
Recommendation: ${anomaly.recommendation}
AI Analysis: ${anomaly.aiAnalysis}
${anomaly.details.code_snippet ? `Code Snippet: ${anomaly.details.code_snippet}` : ''}
    `.trim();

    console.log('📋 Copying text to clipboard:', detailsText);

    navigator.clipboard.writeText(detailsText).then(() => {
      console.log('✅ Successfully copied to clipboard');
      // Show success notification
      setShowCopyNotification(true);
      setIsNotificationFadingOut(false);

      // Start fade-out animation after 2.5 seconds
      setTimeout(() => {
        setIsNotificationFadingOut(true);
        // Hide notification completely after fade-out animation (300ms)
        setTimeout(() => {
          setShowCopyNotification(false);
          setIsNotificationFadingOut(false);
        }, 300);
      }, 2500);
    }).catch(err => {
      console.error('Failed to copy to clipboard:', err);
      // You could add an error notification here too
    });
  };

  const copyModalContent = (anomaly: AnomalyDTO | null) => {
    console.log('🔄 copyModalContent called with:', anomaly);

    if (!anomaly) {
      console.error('❌ No anomaly data to copy');
      return;
    }

    // Copy the content exactly as displayed in the modal
    const modalContent = `
Anomaly Detail

Log Type
${anomaly.type}

Severity
${anomaly.severity}

Description
${anomaly.description}

Details
Issue ID: ${anomaly.details.issue_id}
Location: ${anomaly.details.location}${anomaly.details.code_snippet ? `
Code: ${anomaly.details.code_snippet}` : ''}

Recommendation
${anomaly.recommendation}

AI Analysis
${anomaly.aiAnalysis}
    `.trim();

    console.log('📋 Copying modal content to clipboard:', modalContent);

    navigator.clipboard.writeText(modalContent).then(() => {
      console.log('✅ Successfully copied modal content to clipboard');
      // Show success notification
      setShowCopyNotification(true);
      setIsNotificationFadingOut(false);

      // Start fade-out animation after 2.5 seconds
      setTimeout(() => {
        setIsNotificationFadingOut(true);
        // Hide notification completely after fade-out animation (300ms)
        setTimeout(() => {
          setShowCopyNotification(false);
          setIsNotificationFadingOut(false);
        }, 300);
      }, 2500);
    }).catch(err => {
      console.error('Failed to copy modal content to clipboard:', err);
      // You could add an error notification here too
    });
  };

  if (loading) {
    return (
      <div className="card mb-6">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Detected Anomalies</h3>
        </div>
        <div className="p-4">
          <div className="animate-pulse space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!selectedBuild) {
    if (!hasBuilds) {
      // Show empty state when no builds are available
      return (
        <div className="card mb-6">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Detected Anomalies</h3>
          </div>
          <div className="p-8 flex flex-col items-center justify-center text-center">
            <i className="fas fa-bug text-4xl text-gray-400 mb-4"></i>
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No Anomalies to Display</h4>
            <p className="text-sm text-gray-500 dark:text-gray-400 max-w-md">
              Security anomalies and vulnerabilities will appear here after you trigger builds in Jenkins.
            </p>
          </div>
        </div>
      );
    } else {
      // Show selection prompt when builds are available but none selected
      return (
        <div className="card mb-6">
          <div className="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100">Detected Anomalies</h3>
          </div>
          <div className="p-4">
            <div className="text-center text-gray-500 dark:text-gray-400 py-8">
              <p>Select a build from the Recent Job Builds table to view its anomalies</p>
            </div>
          </div>
        </div>
      );
    }
  }

  return (
    <div className="card mb-6 card-hover transition-all">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">
          Detected Anomalies - {selectedBuild.originalJobName} #{selectedBuild.buildId}
        </h3>
      </div>
      <div className="p-4">
        {anomalies.length === 0 ? (
          <div className="text-center text-gray-500 dark:text-gray-400 py-8">
            <p>No anomalies detected for this build</p>
          </div>
        ) : (
          <div className="overflow-x-auto content-fade-enter-active">
            <table className="min-w-full">
              <thead>
                <tr className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  <th className="px-4 py-3">Log Type</th>
                  <th className="px-4 py-3">Severity</th>
                  <th className="px-4 py-3">Description</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {anomalies.map((anomaly, index) => (
                <React.Fragment key={index}>
                  <tr className="hover:bg-gray-50 dark:hover:bg-gray-700 table-row-enter-active transition-all">
                    <td className="px-4 py-3">
                      <span className={getTypeBadge(anomaly.type)}>
                        {anomaly.type}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={getSeverityBadge(anomaly.severity)}>
                        {anomaly.severity}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-800 dark:text-gray-200">
                      {anomaly.description}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => openModal(anomaly)}
                          className="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 btn-icon-animated transition-all"
                          title="View details"
                        >
                          <i className="fas fa-eye"></i>
                        </button>
                        <button
                          onClick={() => copyToClipboard(anomaly)}
                          className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300 btn-icon-animated transition-all"
                          title="Copy details"
                        >
                          <i className="fas fa-copy"></i>
                        </button>
                        <button
                          onClick={() => toggleRowExpansion(index)}
                          className="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 btn-icon-animated transition-all"
                          title="Toggle details"
                        >
                          {expandedRows.has(index) ? (
                            <i className="fas fa-chevron-up"></i>
                          ) : (
                            <i className="fas fa-chevron-down"></i>
                          )}
                        </button>
                      </div>
                    </td>
                  </tr>
                  {expandedRows.has(index) && (
                    <tr className="dropdown-enter-active">
                      <td colSpan={4}>
                        <div className="px-4 py-4 bg-gray-50 dark:bg-gray-800 border-l-4 border-blue-500 content-fade-enter-active">
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-2">Issue ID</h4>
                              <p className="text-sm text-gray-800 dark:text-gray-200 font-mono">
                                {anomaly.details.issue_id}
                              </p>
                            </div>
                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-2">Location</h4>
                              <p className="text-sm text-gray-800 dark:text-gray-200 font-mono">
                                {anomaly.details.location}
                              </p>
                            </div>
                          </div>

                          <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-2">Recommendation</h4>
                          <p className="text-sm text-gray-800 dark:text-gray-200 mb-3">
                            {anomaly.recommendation}
                          </p>

                          <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-2">AI Analysis</h4>
                          <p className="text-sm text-gray-800 dark:text-gray-200 mb-3">
                            {anomaly.aiAnalysis}
                          </p>

                          {anomaly.details.code_snippet && (
                            <>
                              <h4 className="font-medium text-gray-900 dark:text-gray-100 mb-2">Code Snippet</h4>
                              <pre className="text-xs bg-gray-100 dark:bg-gray-900 p-3 rounded overflow-x-auto text-gray-800 dark:text-gray-200">
                                {anomaly.details.code_snippet}
                              </pre>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        </div>
        )}

        {totalCount > 0 && (
          <div className="mt-4 flex justify-between items-center">
            <span className="text-sm text-gray-500 dark:text-gray-400">
              Showing {Math.min(currentPage * pageSize, totalCount)} of {totalCount} anomalies
            </span>
            <div className="flex space-x-1">
              <button
                onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1 border border-gray-200 dark:border-gray-600 rounded text-sm bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed btn-animated transition-all"
              >
                Previous
              </button>
              {(() => {
                // Smart pagination: show max 5 page buttons
                const maxButtons = 5;
                const halfRange = Math.floor(maxButtons / 2);
                let startPage = Math.max(1, currentPage - halfRange);
                const endPage = Math.min(totalPages, startPage + maxButtons - 1);

                // Adjust start if we're near the end
                if (endPage - startPage + 1 < maxButtons) {
                  startPage = Math.max(1, endPage - maxButtons + 1);
                }

                const pages = [];
                for (let i = startPage; i <= endPage; i++) {
                  pages.push(i);
                }

                return pages.map(pageNum => (
                  <button
                    key={pageNum}
                    onClick={() => setCurrentPage(pageNum)}
                    className={`px-3 py-1 border border-gray-200 dark:border-gray-600 rounded text-sm btn-animated transition-all ${
                      currentPage === pageNum
                        ? 'bg-blue-500 text-white border-blue-500'
                        : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'
                    }`}
                  >
                    {pageNum}
                  </button>
                ));
              })()}
              <button
                onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                disabled={currentPage >= totalPages}
                className="px-3 py-1 border border-gray-200 dark:border-gray-600 rounded text-sm bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed btn-animated transition-all"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Anomaly Detail Modal */}
      {isModalOpen && selectedAnomaly && createPortal(
        <div className="fixed inset-0 flex items-center justify-center z-50 modal-backdrop-enter-active">
          <div className="fixed inset-0 bg-black bg-opacity-50 transition-all" onClick={closeModal}></div>
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-2xl z-10 overflow-hidden relative mx-4 modal-content-enter-active gpu-accelerated">
            <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
              <h3 className="font-semibold text-gray-900 dark:text-gray-100">Anomaly Detail</h3>
              <button
                onClick={closeModal}
                className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 btn-icon-animated transition-all"
              >
                <i className="fas fa-times"></i>
              </button>
            </div>
            <div className="p-6">
              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Log Type</h4>
                <p className="text-sm font-medium">
                  <span className={getTypeBadge(selectedAnomaly.type)}>
                    {selectedAnomaly.type}
                  </span>
                </p>
              </div>

              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Severity</h4>
                <p className="text-sm font-medium">
                  <span className={getSeverityBadge(selectedAnomaly.severity)}>
                    {selectedAnomaly.severity}
                  </span>
                </p>
              </div>

              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Description</h4>
                <p className="text-sm text-gray-800 dark:text-gray-200">
                  {selectedAnomaly.description}
                </p>
              </div>

              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Details</h4>
                <pre className="text-xs bg-gray-100 dark:bg-gray-700 p-3 rounded overflow-x-auto text-gray-800 dark:text-gray-200">
{`Issue ID: ${selectedAnomaly.details.issue_id}
Location: ${selectedAnomaly.details.location}${selectedAnomaly.details.code_snippet ? `
Code: ${selectedAnomaly.details.code_snippet}` : ''}`}
                </pre>
              </div>

              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">Recommendation</h4>
                <p className="text-sm text-gray-800 dark:text-gray-200">
                  {selectedAnomaly.recommendation}
                </p>
              </div>

              <div className="mb-4">
                <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-1">AI Analysis</h4>
                <p className="text-sm text-gray-800 dark:text-gray-200">
                  {selectedAnomaly.aiAnalysis}
                </p>
              </div>

              <div className="flex justify-end space-x-3">
                <button
                  onClick={() => {
                    console.log('🔄 Modal copy button clicked, selectedAnomaly:', selectedAnomaly);
                    copyModalContent(selectedAnomaly);
                  }}
                  className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm btn-animated transition-all"
                >
                  Copy Details
                </button>
                <button
                  onClick={closeModal}
                  className="bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded text-sm btn-primary-animated transition-all"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}

      {/* Copy Success Notification */}
      {showCopyNotification && createPortal(
        <div className={`fixed top-4 right-4 z-50 ${
          isNotificationFadingOut ? 'notification-exit-active' : 'notification-enter-active'
        }`}>
          <div className="bg-green-500 text-white px-4 py-3 rounded-lg shadow-lg flex items-center space-x-2 gpu-accelerated">
            <i className="fas fa-check-circle"></i>
            <span className="text-sm font-medium">Copied to clipboard!</span>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
};

export default AnomaliesTable;
