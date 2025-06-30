import React, { useState, useMemo, useCallback } from 'react';
import type { RecentJobBuildDTO } from '../services/api';
import BuildDetailsModal from './BuildDetailsModal';
import { logger } from '../utils';

interface RecentJobBuildsProps {
  builds: RecentJobBuildDTO[];
  onBuildSelect: (build: RecentJobBuildDTO) => void;
  jobFilter: string;
  timeRange: string;
}

const RecentJobBuilds: React.FC<RecentJobBuildsProps> = React.memo(({ builds, onBuildSelect, jobFilter, timeRange }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBuildForModal, setSelectedBuildForModal] = useState<RecentJobBuildDTO | null>(null);

  const openModal = useCallback((build: RecentJobBuildDTO) => {
    setSelectedBuildForModal(build);
    setIsModalOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsModalOpen(false);
    setSelectedBuildForModal(null);
  }, []);

  const handleViewDetails = useCallback((build: RecentJobBuildDTO) => {
    // Keep the existing functionality for the Build Details section
    onBuildSelect(build);
    // Also open the modal for detailed view
    openModal(build);
  }, [onBuildSelect, openModal]);

  // Memoize health status functions to prevent recreation
  const getHealthStatusBadge = useCallback((status: string) => {
    switch (status.toLowerCase()) {
      case 'healthy':
        return 'badge badge-success';
      case 'warning':
        return 'badge badge-warning';
      case 'critical':
        return 'badge badge-critical';
      default:
        return 'badge badge-danger';
    }
  }, []);

  const getHealthStatusText = useCallback((status: string) => {
    switch (status.toLowerCase()) {
      case 'healthy':
        return 'Healthy';
      case 'warning':
        return 'Warning';
      case 'critical':
        return 'Critical';
      default:
        return 'Unhealthy';
    }
  }, []);

  // Memoize filtered builds to prevent unnecessary recalculations
  const filteredBuilds = useMemo(() => {
    logger.debug('Filtering recent job builds', 'RECENT_JOB_BUILDS', {
      totalBuilds: builds.length,
      jobFilter,
      timeRange
    }, { component: 'RecentJobBuilds', action: 'filterBuilds' });

    if (jobFilter === 'all') {
      // Show latest build from each unique job (3 different jobs)
      logger.debug('Filtering for all jobs (latest build per job)', 'RECENT_JOB_BUILDS', undefined, {
        component: 'RecentJobBuilds',
        action: 'filterAllJobs'
      });

      const unique = builds.reduce((unique: typeof builds, build) => {
        // Only keep the latest build per original job name
        const existingJob = unique.find(u => u.originalJobName === build.originalJobName);

        if (!existingJob || build.buildId > existingJob.buildId) {
          const result = [...unique.filter(u => u.originalJobName !== build.originalJobName), build];
          return result;
        }
        return unique;
      }, []);

      const result = unique.slice(0, 3); // Show 3 different jobs
      logger.debug('Filtered unique jobs', 'RECENT_JOB_BUILDS', {
        uniqueJobs: unique.length,
        finalCount: result.length
      }, { component: 'RecentJobBuilds', action: 'filterAllJobs' });
      return result;

    } else {
      // Show latest 3 builds from the specific job
      logger.debug('Filtering for specific job', 'RECENT_JOB_BUILDS', { jobFilter }, {
        component: 'RecentJobBuilds',
        action: 'filterSpecificJob'
      });

      const jobSpecificBuilds = builds
        .filter(build => build.originalJobName === jobFilter)
        .sort((a, b) => b.buildId - a.buildId) // Sort by buildId descending (latest first)
        .slice(0, 3); // Take latest 3 builds

      logger.debug('Filtered specific job builds', 'RECENT_JOB_BUILDS', {
        jobFilter,
        filteredCount: jobSpecificBuilds.length
      }, { component: 'RecentJobBuilds', action: 'filterSpecificJob' });
      return jobSpecificBuilds;
    }
  }, [builds, jobFilter, timeRange]);

  return (
    <div className="mb-6">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-3">Recent Job Builds</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredBuilds.map((build) => (
          <div key={`${build.originalJobName}-${build.buildId}`} className="card card-hover transition-all">
            <div className="p-4 border-b border-gray-100 dark:border-gray-700">
              <div className="flex justify-between items-center">
                <h4 className="font-medium text-gray-900 dark:text-gray-100 truncate">
                  {build.originalJobName}
                </h4>
                <span className={getHealthStatusBadge(build.healthStatus)}>
                  {getHealthStatusText(build.healthStatus)}
                </span>
              </div>
            </div>
            <div className="p-4">
              <div className="flex justify-between mb-2">
                <span className="text-sm text-gray-500 dark:text-gray-400">Build ID</span>
                <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  #{build.buildId}
                </span>
              </div>
              <div className="flex justify-between mb-2">
                <span className="text-sm text-gray-500 dark:text-gray-400">Anomalies</span>
                <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {build.anomalyCount}
                </span>
              </div>
              <div className="flex justify-between mb-3">
                <span className="text-sm text-gray-500 dark:text-gray-400">Last Build</span>
                <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {build.timeAgo}
                </span>
              </div>
              <button
                onClick={() => handleViewDetails(build)}
                className="w-full btn-primary text-sm btn-primary-animated transition-all"
              >
                View Details
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Build Details Modal */}
      <BuildDetailsModal
        isOpen={isModalOpen}
        onClose={closeModal}
        build={selectedBuildForModal}
      />
    </div>
  );
});

// Add display name for better debugging
RecentJobBuilds.displayName = 'RecentJobBuilds';

export default RecentJobBuilds;
