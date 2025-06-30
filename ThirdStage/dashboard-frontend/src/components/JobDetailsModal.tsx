import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { dashboardApi } from '../services/api';

interface JobDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobName: string;
}

interface JobDetails {
  jobName: string;
  latestBuildId: number;
  status: string;
  healthStatus: string;
  lastBuildTime: string;
  anomalyCount: number;
  description?: string;
  url?: string;
  buildable?: boolean;
  inQueue?: boolean;
}

const JobDetailsModal: React.FC<JobDetailsModalProps> = ({ isOpen, onClose, jobName }) => {
  const [jobDetails, setJobDetails] = useState<JobDetails | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showCopyNotification, setShowCopyNotification] = useState(false);
  const [isNotificationFadingOut, setIsNotificationFadingOut] = useState(false);

  useEffect(() => {
    if (isOpen && jobName) {
      fetchJobDetails();
    }
  }, [isOpen, jobName]);

  const fetchJobDetails = async () => {
    setLoading(true);
    setError(null);

    try {
      // For now, we'll use the job explorer data and enhance it
      // In a real implementation, you might have a dedicated job details endpoint
      const jobs = await dashboardApi.getJobExplorer('all');
      const job = jobs.find(j => j.job_name === jobName);
      
      if (job) {
        // Parse the last_build string to extract build ID and time
        const buildMatch = job.last_build.match(/(\d+)\s*-\s*(.+)/);
        const buildId = buildMatch ? parseInt(buildMatch[1]) : 0;
        const timeAgo = buildMatch ? buildMatch[2] : job.last_build;

        setJobDetails({
          jobName: job.job_name,
          latestBuildId: buildId,
          status: job.status,
          healthStatus: getHealthStatus(job.anomalies),
          lastBuildTime: timeAgo,
          anomalyCount: job.anomalies,
          description: `Jenkins job for ${job.job_name}`,
          url: `http://jenkins/job/${encodeURIComponent(job.job_name)}/`,
          buildable: job.status !== 'RUNNING',
          inQueue: job.status === 'RUNNING'
        });
      } else {
        setError('Job not found');
      }
    } catch (err) {
      console.error('Failed to fetch job details:', err);
      setError('Failed to load job details');
    } finally {
      setLoading(false);
    }
  };

  const getHealthStatus = (anomalies: number): string => {
    if (anomalies === 0) return 'Healthy';
    if (anomalies <= 2) return 'Warning';
    if (anomalies <= 5) return 'Unhealthy';
    return 'Critical';
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200';
      case 'FAILED':
        return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200';
      case 'RUNNING':
        return 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200';
      default:
        return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200';
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

  const copyJobDetails = () => {
    if (!jobDetails) return;

    const content = `
Job Details

Job Name: ${jobDetails.jobName}
Latest Build: #${jobDetails.latestBuildId}
Status: ${jobDetails.status}
Health Status: ${jobDetails.healthStatus}
Last Build: ${jobDetails.lastBuildTime}
Anomaly Count: ${jobDetails.anomalyCount}
Description: ${jobDetails.description || 'N/A'}
Jenkins URL: ${jobDetails.url || 'N/A'}
Buildable: ${jobDetails.buildable ? 'Yes' : 'No'}
In Queue: ${jobDetails.inQueue ? 'Yes' : 'No'}

Generated from Job Explorer
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
      console.error('Failed to copy job details:', err);
    });
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="fixed inset-0 bg-black bg-opacity-50" onClick={onClose}></div>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-3xl z-10 overflow-hidden relative mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100">Job Details</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {loading && (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
              <span className="ml-3 text-gray-600 dark:text-gray-400">Loading job details...</span>
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

          {jobDetails && (
            <>
              {/* Job Overview */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Job Information</h4>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Job Name:</span>
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{jobDetails.jobName}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Latest Build:</span>
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">#{jobDetails.latestBuildId}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Status:</span>
                      <span className={`text-xs px-2 py-1 rounded ${getStatusBadge(jobDetails.status)}`}>
                        {jobDetails.status}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Health Status:</span>
                      <span className={`text-xs px-2 py-1 rounded ${getHealthBadge(jobDetails.healthStatus)}`}>
                        {jobDetails.healthStatus}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Last Build:</span>
                      <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{jobDetails.lastBuildTime}</span>
                    </div>
                  </div>
                </div>

                <div className="space-y-4">
                  <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Build Status</h4>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Anomaly Count:</span>
                      <span className={`text-sm font-bold ${
                        jobDetails.anomalyCount === 0 
                          ? 'text-green-600 dark:text-green-400' 
                          : jobDetails.anomalyCount <= 2 
                            ? 'text-yellow-600 dark:text-yellow-400' 
                            : 'text-red-600 dark:text-red-400'
                      }`}>
                        {jobDetails.anomalyCount}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Buildable:</span>
                      <span className={`text-sm font-medium ${
                        jobDetails.buildable 
                          ? 'text-green-600 dark:text-green-400' 
                          : 'text-red-600 dark:text-red-400'
                      }`}>
                        {jobDetails.buildable ? 'Yes' : 'No'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">In Queue:</span>
                      <span className={`text-sm font-medium ${
                        jobDetails.inQueue 
                          ? 'text-blue-600 dark:text-blue-400' 
                          : 'text-gray-600 dark:text-gray-400'
                      }`}>
                        {jobDetails.inQueue ? 'Yes' : 'No'}
                      </span>
                    </div>
                    {jobDetails.url && (
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-500 dark:text-gray-400">Jenkins URL:</span>
                        <a 
                          href={jobDetails.url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-sm text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
                        >
                          Open in Jenkins
                        </a>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Description */}
              {jobDetails.description && (
                <div className="space-y-4">
                  <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Description</h4>
                  <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                    <p className="text-sm text-gray-700 dark:text-gray-300">{jobDetails.description}</p>
                  </div>
                </div>
              )}

              {/* Quick Actions */}
              <div className="space-y-4">
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Quick Actions</h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  <button className="bg-blue-50 dark:bg-blue-900 hover:bg-blue-100 dark:hover:bg-blue-800 text-blue-700 dark:text-blue-300 p-3 rounded-lg text-sm transition">
                    <i className="fas fa-play mr-2"></i>
                    Trigger Build
                  </button>
                  <button className="bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 p-3 rounded-lg text-sm transition">
                    <i className="fas fa-history mr-2"></i>
                    View History
                  </button>
                  <button className="bg-green-50 dark:bg-green-900 hover:bg-green-100 dark:hover:bg-green-800 text-green-700 dark:text-green-300 p-3 rounded-lg text-sm transition">
                    <i className="fas fa-chart-line mr-2"></i>
                    View Analytics
                  </button>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end space-x-3">
          {jobDetails && (
            <button
              onClick={copyJobDetails}
              className="bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 py-2 px-4 rounded text-sm transition"
            >
              Copy Details
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
            <span>Job details copied to clipboard!</span>
          </div>
        </div>
      )}
    </div>,
    document.body
  );
};

export default JobDetailsModal;
