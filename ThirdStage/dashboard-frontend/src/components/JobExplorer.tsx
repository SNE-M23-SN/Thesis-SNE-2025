import React, { useState, useEffect } from 'react';
import { EyeIcon, ClockIcon } from '@heroicons/react/24/outline';
import { dashboardApi} from '../services/api';
import JobDetailsModal from './JobDetailsModal';
import BuildHistoryModal from './BuildHistoryModal';

// Define the correct interface for job explorer data
interface JobExplorerData {
  job_name: string;
  last_build: string;
  status: string;
  anomalies: number;
}

interface JobExplorerProps {
  jobs?: JobExplorerData[];
  loading?: boolean;
}

const JobExplorer: React.FC<JobExplorerProps> = ({
  jobs: propJobs = [],
  loading: propLoading = false
}) => {
  const [jobs, setJobs] = useState<JobExplorerData[]>(propJobs);
  const [loading, setLoading] = useState(propLoading);
  const [activeTab, setActiveTab] = useState('all');

  // Modal states
  const [isJobDetailsModalOpen, setIsJobDetailsModalOpen] = useState(false);
  const [isBuildHistoryModalOpen, setIsBuildHistoryModalOpen] = useState(false);
  const [selectedJobName, setSelectedJobName] = useState<string>('');

  const openJobDetailsModal = (jobName: string) => {
    setSelectedJobName(jobName);
    setIsJobDetailsModalOpen(true);
  };

  const openBuildHistoryModal = (jobName: string) => {
    setSelectedJobName(jobName);
    setIsBuildHistoryModalOpen(true);
  };

  const closeJobDetailsModal = () => {
    setIsJobDetailsModalOpen(false);
    setSelectedJobName('');
  };

  const closeBuildHistoryModal = () => {
    setIsBuildHistoryModalOpen(false);
    setSelectedJobName('');
  };

  // Sync with props data when available
  useEffect(() => {
    loadJobs();
  }, [propJobs, propLoading, activeTab]);

  const loadJobs = async () => {
    // If we have props data, filter it locally instead of making API call
    if (propJobs.length > 0) {
      setLoading(propLoading);
      const filteredJobs = filterJobsByTab(propJobs, activeTab);
      setJobs(filteredJobs);
      return;
    }

    // Fallback to API call if no props data
    try {
      setLoading(true);
      const jobsData = await dashboardApi.getJobExplorer(activeTab);
      setJobs(jobsData);
    } catch (error) {
      console.error('Failed to load jobs:', error);
      // Mock data for demonstration
      setJobs([
        {
          job_name: 'Backend-Deploy',
          last_build: '12930 - 8 hours ago',
          status: 'FAILED',
          anomalies: 14
        },
        {
          job_name: 'API-Integration',
          last_build: '12933 - 4 hours ago',
          status: 'COMPLETED',
          anomalies: 8
        },
        {
          job_name: 'Frontend-CI',
          last_build: '12934 - 2 hours ago',
          status: 'COMPLETED',
          anomalies: 2
        },
        {
          job_name: 'Mobile-Build',
          last_build: '12935 - 1 hour ago',
          status: 'COMPLETED',
          anomalies: 0
        },
        {
          job_name: 'Database-Migration',
          last_build: '12936 - 30 minutes ago',
          status: 'RUNNING',
          anomalies: 1
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  // Helper function to filter jobs by tab
  const filterJobsByTab = (jobsData: JobExplorerData[], tab: string): JobExplorerData[] => {
    switch (tab) {
      case 'active':
        return jobsData.filter(job => job.status === 'RUNNING');
      case 'withIssues':
        return jobsData.filter(job => job.anomalies > 0 || job.status === 'FAILED');
      case 'completed':
        return jobsData.filter(job => job.status === 'COMPLETED');
      case 'all':
      default:
        return jobsData;
    }
  };

  const getStatusBadge = (status: string) => {
    if (!status) return 'badge badge-secondary';

    switch (status.toUpperCase()) {
      case 'RUNNING':
        return 'badge badge-info';
      case 'COMPLETED':
        return 'badge badge-success';
      case 'FAILED':
        return 'badge badge-error';
      default:
        return 'badge badge-secondary';
    }
  };

  const getStatusText = (status: string) => {
    if (!status) return 'Unknown';

    switch (status.toUpperCase()) {
      case 'RUNNING':
        return 'Running';
      case 'COMPLETED':
        return 'Completed';
      case 'FAILED':
        return 'Failed';
      default:
        return status;
    }
  };

  const tabs = [
    { id: 'all', label: 'All Jobs' },
    { id: 'active', label: 'Active' },
    { id: 'withIssues', label: 'With Issues' },
    { id: 'completed', label: 'Completed' }
  ];

  return (
    <div className="card card-hover transition-all">
      <div className="p-4 border-b border-gray-100 dark:border-gray-700">
        <h3 className="font-semibold text-gray-900 dark:text-gray-100">Job Explorer</h3>
      </div>
      <div className="p-4">
        <div className="mb-4 flex flex-wrap border-b border-gray-200 dark:border-gray-700">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-all hover-lift ${
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        
        {loading ? (
          <div className="animate-pulse space-y-4 loading-fade-enter-active">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded transition-all"></div>
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto content-fade-enter-active">
            <table className="min-w-full">
              <thead>
                <tr className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  <th className="px-4 py-3">Job Name</th>
                  <th className="px-4 py-3">Last Build</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Anomalies</th>
                  <th className="px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {jobs.map((job, index) => (
                  <tr key={`${job.job_name}-${index}`} className="hover:bg-gray-50 dark:hover:bg-gray-700 table-row-enter-active transition-all">
                    <td className="px-4 py-3 text-sm font-medium text-gray-900 dark:text-gray-100">
                      {job.job_name}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-800 dark:text-gray-200">
                      {job.last_build}
                    </td>
                    <td className="px-4 py-3">
                      <span className={getStatusBadge(job.status)}>
                        {getStatusText(job.status)}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-800 dark:text-gray-200">
                      {job.anomalies}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => openJobDetailsModal(job.job_name)}
                          className="text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 btn-icon-animated transition-all"
                          title="View job details"
                        >
                          <EyeIcon className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => openBuildHistoryModal(job.job_name)}
                          className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300 btn-icon-animated transition-all"
                          title="View build history"
                        >
                          <ClockIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Job Details Modal */}
      <JobDetailsModal
        isOpen={isJobDetailsModalOpen}
        onClose={closeJobDetailsModal}
        jobName={selectedJobName}
      />

      {/* Build History Modal */}
      <BuildHistoryModal
        isOpen={isBuildHistoryModalOpen}
        onClose={closeBuildHistoryModal}
        jobName={selectedJobName}
      />
    </div>
  );
};

export default JobExplorer;
