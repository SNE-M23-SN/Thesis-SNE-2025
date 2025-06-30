import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { dashboardApi } from '../services/api';
import { LOG_SANITIZATION } from '../constants/dashboard';
import type { RecentJobBuildDTO } from '../services/api';
import { logger } from '../utils';

// Based on actual Java DTO classes from dto/ directory - TypedLog base class + 7 concrete implementations

// Base interface matching TypedLog.java
interface BaseTypedLog {
  type: string;
  timestamp?: string;
  buildNumber: number;
}

// BuildLogData.java - Jenkins build console output & execution logs
interface BuildLogDataEntry extends BaseTypedLog {
  type: 'build_log_data';
  jobName: string;
  data: { [key: string]: any }; // Contains raw_log, raw_log_compressed, build_info, etc.
  error?: string;
}

// ScanResult.java - Static Application Security Testing results
interface ScanResultEntry extends BaseTypedLog {
  type: 'sast_scanning';
  jobName: string;
  repoUrl: string;
  branch: string;
  tool: string;
  status: string;
  scanDurationSeconds: number;
  scanResult?: string; // JSON or compressed format
  error?: string;
}

// SecretDetection.java - Credential scanning & secret exposure detection
interface SecretDetectionEntry extends BaseTypedLog {
  type: 'secret_detection';
  jobName: string;
  data: { [key: string]: any }; // Contains source, message, content, secrets, etc.
  error?: string;
}

// DependencyData.java - Build dependencies, artifacts & plugin information
interface DependencyDataEntry extends BaseTypedLog {
  type: 'dependency_data';
  jobName: string;
  data: { [key: string]: any }; // Contains build_file, plugin_info, artifacts, dependencies
  error?: string;
}

// CodeChanges.java - Version control changes & commit information
interface CodeChangesEntry extends BaseTypedLog {
  type: 'code_changes';
  jobName: string;
  data: { [key: string]: any }; // Contains message, changes_compressed, changes, culprits
  error?: string;
}

// AdditionalInfoAgent.java - Jenkins agent system metrics & performance data
interface AdditionalInfoAgentEntry extends BaseTypedLog {
  type: 'additional_info_agent';
  jobName: string;
  node?: string;
  sessionCount?: number;
  activeThreadCount?: number;
  threadCount?: number;
  systemLoadAverage?: number;
  systemCpuLoad?: number;
  availableProcessors?: number;
  host?: string;
  os?: string;
  javaVersion?: string;
  jvmVersion?: string;
  pid?: string;
  serverInfo?: string;
  contextPath?: string;
  startDate?: string;
  memory?: { [key: string]: any };
  threads?: { [key: string]: any };
  status?: string;
  message?: string;
  stacktrace?: string[];
}

// AdditionalInfoController.java - Jenkins controller system metrics & health data
interface AdditionalInfoControllerEntry extends BaseTypedLog {
  type: 'additional_info_controller';
  jobName: string;
  usedMemory?: string;
  maxMemory?: string;
  usedPermGen?: string;
  maxPermGen?: string;
  usedNonHeap?: string;
  usedPhysicalMemory?: string;
  usedSwapSpace?: string;
  sessionsCount?: number;
  activeHttpThreadsCount?: number;
  threadsCount?: number;
  systemLoadAverage?: number;
  systemCpuLoad?: number;
  availableProcessors?: number;
  host?: string;
  os?: string;
  javaVersion?: string;
  jvmVersion?: string;
  pid?: string;
  serverInfo?: string;
  contextPath?: string;
  startDate?: string;
  freeDiskSpaceInJenkinsDirMb?: number;
  status?: string;
  message?: string;
  stacktrace?: string[];
}

// Union type for all possible log entry types based on actual DTOs
type BuildLogEntry =
  | BuildLogDataEntry
  | ScanResultEntry
  | SecretDetectionEntry
  | DependencyDataEntry
  | CodeChangesEntry
  | AdditionalInfoAgentEntry
  | AdditionalInfoControllerEntry;

interface BuildLogsModalProps {
  isOpen: boolean;
  onClose: () => void;
  selectedBuild: RecentJobBuildDTO | null;
}

const BuildLogsModal: React.FC<BuildLogsModalProps> = ({ isOpen, onClose, selectedBuild }) => {
  const [logs, setLogs] = useState<BuildLogEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [groupedLogs, setGroupedLogs] = useState<{ [key: string]: BuildLogEntry[] }>({});

  useEffect(() => {
    if (isOpen && selectedBuild) {
      loadAllLogs();
    }
  }, [isOpen, selectedBuild]);

  const loadAllLogs = async () => {
    if (!selectedBuild) return;

    try {
      setLoading(true);
      setError(null);

      logger.info(`Loading ALL logs for ${selectedBuild.originalJobName} #${selectedBuild.buildId}`, 'BUILD_LOGS_MODAL', {
        jobName: selectedBuild.originalJobName,
        buildId: selectedBuild.buildId
      }, { component: 'BuildLogsModal', action: 'loadAllLogs' });

      // Load all logs at once (no pagination)
      const response = await dashboardApi.getBuildLogs(
        selectedBuild.originalJobName,
        selectedBuild.buildId,
        0,
        1000 // Large page size to get all logs
      );

      logger.debug('Complete logs API response received', 'BUILD_LOGS_MODAL', {
        totalEntries: response.length,
        logTypes: [...new Set(response.map((log: BuildLogEntry) => log.type))]
      }, { component: 'BuildLogsModal', action: 'loadAllLogs' });

      setLogs(response);

      // Group logs by type for better organization
      const grouped = response.reduce((acc: { [key: string]: BuildLogEntry[] }, log: BuildLogEntry) => {
        const type = log.type || 'unknown';
        if (!acc[type]) {
          acc[type] = [];
        }
        acc[type].push(log);
        return acc;
      }, {});

      setGroupedLogs(grouped);
      logger.debug('Logs grouped by type', 'BUILD_LOGS_MODAL', {
        groupedTypes: Object.keys(grouped),
        totalGroups: Object.keys(grouped).length
      }, { component: 'BuildLogsModal', action: 'loadAllLogs' });

    } catch (error) {
      logger.error('Failed to load build logs', 'BUILD_LOGS_MODAL', { error }, {
        component: 'BuildLogsModal',
        action: 'loadAllLogs'
      });
      setError('Failed to load build logs. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  // Sanitize log content to prevent XSS attacks
  const sanitizeLogContent = (content: string): string => {
    if (!content || typeof content !== 'string') {
      return '';
    }

    let sanitized = content;

    // Remove potentially dangerous HTML tags using constants
    LOG_SANITIZATION.DANGEROUS_TAGS.forEach((pattern, index) => {
      const replacements = [
        LOG_SANITIZATION.REPLACEMENT_TEXT.SCRIPT,
        LOG_SANITIZATION.REPLACEMENT_TEXT.IFRAME,
        LOG_SANITIZATION.REPLACEMENT_TEXT.OBJECT,
        LOG_SANITIZATION.REPLACEMENT_TEXT.EMBED,
        LOG_SANITIZATION.REPLACEMENT_TEXT.LINK,
        LOG_SANITIZATION.REPLACEMENT_TEXT.META,
      ];
      const replacement = replacements[index] || '[REMOVED_TAG]';
      sanitized = sanitized.replace(pattern, replacement);
    });

    // Remove dangerous protocols using constants
    LOG_SANITIZATION.DANGEROUS_PROTOCOLS.forEach((pattern, index) => {
      const replacements = [
        LOG_SANITIZATION.REPLACEMENT_TEXT.JAVASCRIPT,
        LOG_SANITIZATION.REPLACEMENT_TEXT.DATA,
        LOG_SANITIZATION.REPLACEMENT_TEXT.VBSCRIPT,
      ];
      const replacement = replacements[index] || '[REMOVED_PROTOCOL]';
      sanitized = sanitized.replace(pattern, replacement);
    });

    // Escape remaining HTML entities
    return sanitized
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#x27;')
      .replace(/\//g, '&#x2F;');
  };

  const formatLogText = (rawLog: string) => {
    if (!rawLog) {
      return '';
    }

    // First sanitize the content
    const sanitized = sanitizeLogContent(rawLog);

    // Then clean up Jenkins log formatting on the sanitized content
    return sanitized
      .replace(/&gt;/g, '>')  // Restore safe greater-than symbols
      .replace(/\n/g, '\n')   // Preserve line breaks
      .trim();
  };

  const getLogTypeInfo = (type: string) => {
    switch (type?.toLowerCase()) {
      case 'build_log_data':
        return { icon: 'fas fa-hammer', color: 'text-blue-500', bgColor: 'bg-blue-100 dark:bg-blue-900', label: 'Build Log' };
      case 'sast_scanning':
        return { icon: 'fas fa-shield-alt', color: 'text-red-500', bgColor: 'bg-red-100 dark:bg-red-900', label: 'SAST Scan' };
      case 'dependency_check':
        return { icon: 'fas fa-cubes', color: 'text-orange-500', bgColor: 'bg-orange-100 dark:bg-orange-900', label: 'Dependency Check' };
      case 'code_quality_check':
        return { icon: 'fas fa-code', color: 'text-purple-500', bgColor: 'bg-purple-100 dark:bg-purple-900', label: 'Code Quality' };
      case 'test_results':
        return { icon: 'fas fa-vial', color: 'text-green-500', bgColor: 'bg-green-100 dark:bg-green-900', label: 'Test Results' };
      case 'deployment_log':
        return { icon: 'fas fa-rocket', color: 'text-indigo-500', bgColor: 'bg-indigo-100 dark:bg-indigo-900', label: 'Deployment' };
      case 'security_scan':
        return { icon: 'fas fa-lock', color: 'text-red-600', bgColor: 'bg-red-100 dark:bg-red-900', label: 'Security Scan' };
      case 'performance_test':
        return { icon: 'fas fa-tachometer-alt', color: 'text-yellow-500', bgColor: 'bg-yellow-100 dark:bg-yellow-900', label: 'Performance Test' };
      case 'docker_build':
        return { icon: 'fab fa-docker', color: 'text-blue-600', bgColor: 'bg-blue-100 dark:bg-blue-900', label: 'Docker Build' };
      case 'git_operations':
        return { icon: 'fab fa-git-alt', color: 'text-gray-600', bgColor: 'bg-gray-100 dark:bg-gray-900', label: 'Git Operations' };
      default:
        return { icon: 'fas fa-file-alt', color: 'text-gray-500', bgColor: 'bg-gray-100 dark:bg-gray-900', label: type || 'Unknown' };
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'success':
      case 'passed':
        return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200';
      case 'failure':
      case 'failed':
      case 'error':
        return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200';
      case 'warning':
      case 'unstable':
        return 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200';
      case 'running':
      case 'in_progress':
        return 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200';
      case 'skipped':
        return 'bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-gray-200';
      default:
        return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200';
    }
  };

  // Type guard functions based on actual DTO types
  const isBuildLogData = (entry: BuildLogEntry): entry is BuildLogDataEntry => entry.type === 'build_log_data';
  const isScanResult = (entry: BuildLogEntry): entry is ScanResultEntry => entry.type === 'sast_scanning';
  const isSecretDetection = (entry: BuildLogEntry): entry is SecretDetectionEntry => entry.type === 'secret_detection';
  const isDependencyData = (entry: BuildLogEntry): entry is DependencyDataEntry => entry.type === 'dependency_data';
  const isCodeChanges = (entry: BuildLogEntry): entry is CodeChangesEntry => entry.type === 'code_changes';
  const isAdditionalInfoAgent = (entry: BuildLogEntry): entry is AdditionalInfoAgentEntry => entry.type === 'additional_info_agent';
  const isAdditionalInfoController = (entry: BuildLogEntry): entry is AdditionalInfoControllerEntry => entry.type === 'additional_info_controller';

  // Helper functions to safely access properties
  const getToolName = (entry: BuildLogEntry): string | undefined => {
    if (isScanResult(entry)) {
      return entry.tool;
    }
    return undefined;
  };

  const getStatus = (entry: BuildLogEntry): string => {
    if (isBuildLogData(entry)) {
      // Access build_info from data object
      const buildInfo = entry.data.build_info;
      return buildInfo?.result || 'UNKNOWN';
    } else if (isScanResult(entry)) {
      return entry.status;
    } else if (isAdditionalInfoAgent(entry) || isAdditionalInfoController(entry)) {
      return entry.status || 'UNKNOWN';
    } else if (isSecretDetection(entry) || isDependencyData(entry) || isCodeChanges(entry)) {
      return 'SUCCESS'; // These types don't have explicit status
    }
    return 'UNKNOWN';
  };

  const isCompressed = (entry: BuildLogEntry): boolean => {
    if (isBuildLogData(entry)) {
      return entry.data.raw_log_compressed || false;
    } else if (isSecretDetection(entry)) {
      return entry.data.content_compressed || false;
    } else if (isCodeChanges(entry)) {
      return entry.data.changes_compressed || false;
    }
    return false;
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 flex items-center justify-center z-50 modal-backdrop-enter-active">
      <div className="fixed inset-0 bg-black bg-opacity-50 transition-all" onClick={onClose}></div>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-6xl h-5/6 z-10 overflow-hidden relative mx-4 flex flex-col modal-content-enter-active gpu-accelerated">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
          <div>
            <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100">
              Build Logs
            </h2>
            {selectedBuild && (
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                {selectedBuild.originalJobName} #{selectedBuild.buildId}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 btn-icon-animated transition-all"
          >
            <i className="fas fa-times text-xl"></i>
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-hidden flex flex-col">
          {error ? (
            <div className="p-6 text-center">
              <i className="fas fa-exclamation-triangle text-red-500 text-3xl mb-4"></i>
              <p className="text-red-600 dark:text-red-400">{error}</p>
              <button
                onClick={() => loadAllLogs()}
                className="mt-4 bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded text-sm btn-primary-animated transition-all"
              >
                Retry
              </button>
            </div>
          ) : (
            <>
              {/* Logs Container */}
              <div className="flex-1 overflow-y-auto p-6 bg-gray-50 dark:bg-gray-900">
                {logs.length === 0 && loading ? (
                  <div className="text-center py-8 loading-fade-enter-active">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto mb-4 gpu-accelerated"></div>
                    <p className="text-gray-500 dark:text-gray-400 content-fade-enter-active">Loading build logs...</p>
                  </div>
                ) : logs.length === 0 ? (
                  <div className="text-center py-8">
                    <i className="fas fa-file-alt text-gray-400 text-3xl mb-4"></i>
                    <p className="text-gray-500 dark:text-gray-400">No logs available for this build</p>
                  </div>
                ) : (
                  <div className="space-y-6">
                    {Object.entries(groupedLogs).map(([logType, logEntries]) => {
                      const typeInfo = getLogTypeInfo(logType);
                      return (
                        <div key={logType} className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 card-hover transition-all">
                          {/* Log Type Header */}
                          <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center space-x-3">
                                <i className={`${typeInfo.icon} ${typeInfo.color} text-lg`}></i>
                                <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
                                  {typeInfo.label}
                                </h3>
                                <span className="text-sm text-gray-500 dark:text-gray-400">
                                  ({logEntries.length} entries)
                                </span>
                              </div>
                            </div>
                          </div>

                          {/* Log Entries for this type */}
                          <div className="divide-y divide-gray-200 dark:divide-gray-700">
                            {logEntries.map((logEntry, index) => (
                              <div
                                key={`${logEntry.buildNumber}-${logEntry.timestamp}-${index}`}
                                className="p-4"
                              >
                        {/* Log Entry Header */}
                        <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-4">
                              {(() => {
                                const typeInfo = getLogTypeInfo(logEntry.type || '');
                                return (
                                  <div className="flex items-center space-x-2">
                                    <i className={`${typeInfo.icon} ${typeInfo.color}`}></i>
                                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
                                      {typeInfo.label}
                                    </span>
                                  </div>
                                );
                              })()}
                              <span className="text-xs text-gray-500 dark:text-gray-400">
                                {logEntry.timestamp ? formatTimestamp(logEntry.timestamp) : 'No timestamp'}
                              </span>
                              {getToolName(logEntry) && (
                                <span className="text-xs bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-300 px-2 py-1 rounded">
                                  {getToolName(logEntry)}
                                </span>
                              )}
                            </div>
                            <div className="flex items-center space-x-2">
                              {isCompressed(logEntry) && (
                                <span className="text-xs bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 px-2 py-1 rounded">
                                  Compressed
                                </span>
                              )}
                              {/* Status from different sources */}
                              {(() => {
                                const status = getStatus(logEntry);
                                return (
                                  <span className={`text-xs px-2 py-1 rounded ${getStatusBadge(status)}`}>
                                    {status}
                                  </span>
                                );
                              })()}
                            </div>
                          </div>
                        </div>

                        {/* Log Content */}
                        <div className="p-4">
                          {(() => {
                            // Handle different log content structures using type guards based on actual Java DTOs
                            if (isBuildLogData(logEntry)) {
                              // BuildLogData.java - Jenkins build console output & execution logs
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Build Log Data</h5>
                                  {logEntry.error && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Error:</strong> {logEntry.error}
                                    </div>
                                  )}
                                  {logEntry.data.raw_log && (
                                    <pre className="text-xs text-gray-800 dark:text-gray-200 font-mono whitespace-pre-wrap overflow-x-auto bg-gray-200 dark:bg-gray-800 p-2 rounded">
                                      {formatLogText(logEntry.data.raw_log)}
                                    </pre>
                                  )}
                                  {logEntry.data.build_info && (
                                    <div className="mt-2">
                                      <strong>Build Info:</strong>
                                      <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                        {JSON.stringify(logEntry.data.build_info, null, 2)}
                                      </pre>
                                    </div>
                                  )}
                                </div>
                              );
                            } else if (isScanResult(logEntry)) {
                              // ScanResult.java - Static Application Security Testing results
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">SAST Scan Results</h5>
                                  {logEntry.error && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Error:</strong> {logEntry.error}
                                    </div>
                                  )}
                                  <div className="grid grid-cols-2 gap-4 text-sm mb-2">
                                    <div><strong>Tool:</strong> {logEntry.tool}</div>
                                    <div><strong>Branch:</strong> {logEntry.branch}</div>
                                    <div><strong>Status:</strong> {logEntry.status}</div>
                                    <div><strong>Duration:</strong> {logEntry.scanDurationSeconds}s</div>
                                  </div>
                                  <div className="mb-2">
                                    <strong>Repository:</strong> {logEntry.repoUrl}
                                  </div>
                                  {logEntry.scanResult && (
                                    <div className="mt-2">
                                      <strong>Scan Results:</strong>
                                      <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                        {logEntry.scanResult}
                                      </pre>
                                    </div>
                                  )}
                                </div>
                              );
                            } else if (isSecretDetection(logEntry)) {
                              // SecretDetection.java - Credential scanning & secret exposure detection
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Secret Detection</h5>
                                  {logEntry.error && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Error:</strong> {logEntry.error}
                                    </div>
                                  )}
                                  <div className="text-sm space-y-2">
                                    <div><strong>Source:</strong> {logEntry.data.source || 'Unknown'}</div>
                                    <div><strong>Message:</strong> {logEntry.data.message || 'No message'}</div>
                                    {logEntry.data.content && (
                                      <div>
                                        <strong>Content:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {logEntry.data.content}
                                        </pre>
                                      </div>
                                    )}
                                    {logEntry.data.secrets && (
                                      <div>
                                        <strong>Secrets Found:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {JSON.stringify(logEntry.data.secrets, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              );
                            } else if (isDependencyData(logEntry)) {
                              // DependencyData.java - Build dependencies, artifacts & plugin information
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Dependency Data</h5>
                                  {logEntry.error && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Error:</strong> {logEntry.error}
                                    </div>
                                  )}
                                  <div className="space-y-2">
                                    {logEntry.data.build_file && (
                                      <div>
                                        <strong>Build File:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {JSON.stringify(logEntry.data.build_file, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                    {logEntry.data.plugin_info && (
                                      <div>
                                        <strong>Plugin Info:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {JSON.stringify(logEntry.data.plugin_info, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                    {logEntry.data.dependencies && (
                                      <div>
                                        <strong>Dependencies:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {JSON.stringify(logEntry.data.dependencies, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                    {logEntry.data.artifacts && (
                                      <div>
                                        <strong>Artifacts:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {JSON.stringify(logEntry.data.artifacts, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              );
                            } else if (isCodeChanges(logEntry)) {
                              // CodeChanges.java - Version control changes & commit information
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Code Changes</h5>
                                  {logEntry.error && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Error:</strong> {logEntry.error}
                                    </div>
                                  )}
                                  <div className="space-y-2">
                                    {logEntry.data.message && (
                                      <div><strong>Message:</strong> {logEntry.data.message}</div>
                                    )}
                                    {logEntry.data.changes && (
                                      <div>
                                        <strong>Changes:</strong>
                                        <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                          {typeof logEntry.data.changes === 'string'
                                            ? logEntry.data.changes
                                            : JSON.stringify(logEntry.data.changes, null, 2)}
                                        </pre>
                                      </div>
                                    )}
                                    {logEntry.data.culprits && (
                                      <div>
                                        <strong>Culprits:</strong> {Array.isArray(logEntry.data.culprits)
                                          ? logEntry.data.culprits.join(', ')
                                          : logEntry.data.culprits}
                                      </div>
                                    )}
                                  </div>
                                </div>
                              );
                            } else if (isAdditionalInfoAgent(logEntry)) {
                              // AdditionalInfoAgent.java - Jenkins agent system metrics & performance data
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Agent System Info</h5>
                                  {logEntry.message && (
                                    <div className="mb-2 text-blue-600 dark:text-blue-400">
                                      <strong>Message:</strong> {logEntry.message}
                                    </div>
                                  )}
                                  {logEntry.stacktrace && logEntry.stacktrace.length > 0 && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Stack Trace:</strong>
                                      <pre className="text-xs mt-1 bg-red-50 dark:bg-red-900 p-2 rounded overflow-x-auto">
                                        {logEntry.stacktrace.join('\n')}
                                      </pre>
                                    </div>
                                  )}
                                  <div className="grid grid-cols-2 gap-4 text-sm">
                                    {logEntry.node && <div><strong>Node:</strong> {logEntry.node}</div>}
                                    {logEntry.host && <div><strong>Host:</strong> {logEntry.host}</div>}
                                    {logEntry.os && <div><strong>OS:</strong> {logEntry.os}</div>}
                                    {logEntry.javaVersion && <div><strong>Java:</strong> {logEntry.javaVersion}</div>}
                                    {logEntry.sessionCount !== undefined && <div><strong>Sessions:</strong> {logEntry.sessionCount}</div>}
                                    {logEntry.activeThreadCount !== undefined && <div><strong>Active Threads:</strong> {logEntry.activeThreadCount}</div>}
                                    {logEntry.threadCount !== undefined && <div><strong>Total Threads:</strong> {logEntry.threadCount}</div>}
                                    {logEntry.systemLoadAverage !== undefined && <div><strong>Load Avg:</strong> {logEntry.systemLoadAverage.toFixed(2)}</div>}
                                    {logEntry.systemCpuLoad !== undefined && <div><strong>CPU Load:</strong> {(logEntry.systemCpuLoad * 100).toFixed(1)}%</div>}
                                    {logEntry.availableProcessors !== undefined && <div><strong>Processors:</strong> {logEntry.availableProcessors}</div>}
                                  </div>
                                  {logEntry.memory && (
                                    <div className="mt-2">
                                      <strong>Memory:</strong>
                                      <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                        {JSON.stringify(logEntry.memory, null, 2)}
                                      </pre>
                                    </div>
                                  )}
                                  {logEntry.threads && (
                                    <div className="mt-2">
                                      <strong>Thread Details:</strong>
                                      <pre className="text-xs mt-1 bg-gray-200 dark:bg-gray-800 p-2 rounded overflow-x-auto">
                                        {JSON.stringify(logEntry.threads, null, 2)}
                                      </pre>
                                    </div>
                                  )}
                                </div>
                              );
                            } else if (isAdditionalInfoController(logEntry)) {
                              // AdditionalInfoController.java - Jenkins controller system metrics & health data
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Controller System Info</h5>
                                  {logEntry.message && (
                                    <div className="mb-2 text-blue-600 dark:text-blue-400">
                                      <strong>Message:</strong> {logEntry.message}
                                    </div>
                                  )}
                                  {logEntry.stacktrace && logEntry.stacktrace.length > 0 && (
                                    <div className="mb-2 text-red-600 dark:text-red-400">
                                      <strong>Stack Trace:</strong>
                                      <pre className="text-xs mt-1 bg-red-50 dark:bg-red-900 p-2 rounded overflow-x-auto">
                                        {logEntry.stacktrace.join('\n')}
                                      </pre>
                                    </div>
                                  )}
                                  <div className="grid grid-cols-2 gap-4 text-sm">
                                    {logEntry.host && <div><strong>Host:</strong> {logEntry.host}</div>}
                                    {logEntry.os && <div><strong>OS:</strong> {logEntry.os}</div>}
                                    {logEntry.javaVersion && <div><strong>Java:</strong> {logEntry.javaVersion}</div>}
                                    {logEntry.usedMemory && <div><strong>Used Memory:</strong> {logEntry.usedMemory}</div>}
                                    {logEntry.maxMemory && <div><strong>Max Memory:</strong> {logEntry.maxMemory}</div>}
                                    {logEntry.sessionsCount !== undefined && <div><strong>Sessions:</strong> {logEntry.sessionsCount}</div>}
                                    {logEntry.activeHttpThreadsCount !== undefined && <div><strong>HTTP Threads:</strong> {logEntry.activeHttpThreadsCount}</div>}
                                    {logEntry.threadsCount !== undefined && <div><strong>Total Threads:</strong> {logEntry.threadsCount}</div>}
                                    {logEntry.systemLoadAverage !== undefined && <div><strong>Load Avg:</strong> {logEntry.systemLoadAverage.toFixed(2)}</div>}
                                    {logEntry.systemCpuLoad !== undefined && <div><strong>CPU Load:</strong> {(logEntry.systemCpuLoad * 100).toFixed(1)}%</div>}
                                    {logEntry.freeDiskSpaceInJenkinsDirMb !== undefined && <div><strong>Free Disk:</strong> {logEntry.freeDiskSpaceInJenkinsDirMb}MB</div>}
                                  </div>
                                </div>
                              );
                            } else {
                              // Fallback for unknown log types (should not happen with proper typing)
                              const unknownEntry = logEntry as any;
                              return (
                                <div className="bg-gray-100 dark:bg-gray-900 p-3 rounded border">
                                  <h5 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">Unknown Log Type</h5>
                                  <div className="mb-2 text-orange-600 dark:text-orange-400">
                                    <strong>Type:</strong> {unknownEntry.type || 'Unknown'}
                                  </div>
                                  <pre className="text-xs text-gray-800 dark:text-gray-200 font-mono whitespace-pre-wrap overflow-x-auto">
                                    {JSON.stringify(unknownEntry, null, 2)}
                                  </pre>
                                </div>
                              );
                            }
                          })()}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-500 dark:text-gray-400">
              {logs.length > 0 && (
                <>
                  Showing {logs.length} log entries across {Object.keys(groupedLogs).length} log types
                </>
              )}
            </div>
            <button
              onClick={onClose}
              className="bg-gray-500 hover:bg-gray-600 text-white py-2 px-4 rounded text-sm btn-animated transition-all"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default BuildLogsModal;
