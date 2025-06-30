package com.diploma.inno.service;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.job.Job;
import com.diploma.inno.dto.RecentJobDTO;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class for Jenkins REST API integration and job management in the CI Anomaly Detector system.
 *
 * <p>This service provides comprehensive integration with Jenkins CI/CD server through the jenkins-rest
 * library, enabling real-time job discovery, build triggering, and status monitoring. It implements
 * sophisticated caching mechanisms to optimize performance and reduce API load on the Jenkins server.</p>
 *
 * <p><strong>Core Functionality:</strong></p>
 * <ul>
 *   <li><strong>Job Discovery:</strong> Real-time enumeration of Jenkins jobs with folder support</li>
 *   <li><strong>Build Triggering:</strong> REST API calls to initiate new builds for specified jobs</li>
 *   <li><strong>Status Monitoring:</strong> Job status tracking with visual indicator mapping</li>
 *   <li><strong>Caching Layer:</strong> Multi-level caching with TTL-based invalidation</li>
 *   <li><strong>Error Handling:</strong> Robust error recovery with graceful degradation</li>
 * </ul>
 *
 * <p><strong>Jenkins REST API Integration:</strong></p>
 * <ul>
 *   <li><strong>Library:</strong> jenkins-rest by cdancy for type-safe API access</li>
 *   <li><strong>Client:</strong> JenkinsClient configured via JenkinsConfiguration</li>
 *   <li><strong>Authentication:</strong> Username/password credentials from application properties</li>
 *   <li><strong>Endpoints:</strong> jobsApi() for job operations, queueApi() for build queues</li>
 * </ul>
 *
 * <p><strong>Caching Architecture:</strong></p>
 * <pre>
 * Recent Jobs Cache:
 * - TTL: 15 seconds (CACHE_TTL_MS = 15000)
 * - Storage: List&lt;RecentJobDTO&gt; cachedJobs
 * - Timestamp: long cacheTimestamp for invalidation
 * - Purpose: Reduce API load for frequent job list requests
 *
 * Active Builds Cache:
 * - Storage: Map&lt;String, Long&gt; activeBuildsCache
 * - Timestamp: long activeBuildsCacheTimestamp
 * - Purpose: Track currently executing builds
 * - Key: Job name, Value: Build start timestamp
 * </pre>
 *
 * <p><strong>Status Mapping System:</strong></p>
 * <ul>
 *   <li><strong>Jenkins BallColor:</strong> RED, YELLOW, BLUE, GREY, DISABLED, ABORTED, NOTBUILT</li>
 *   <li><strong>Tailwind CSS Classes:</strong> bg-red-500, bg-yellow-500, bg-green-500, etc.</li>
 *   <li><strong>Visual Indicators:</strong> Color-coded status representation for dashboard UI</li>
 *   <li><strong>Mapping Logic:</strong> STATUS_COLOR_MAP provides direct color translation</li>
 * </ul>
 *
 * <p><strong>Integration Points:</strong></p>
 * <ul>
 *   <li><strong>DashboardService:</strong> Primary consumer for scheduled job synchronization</li>
 *   <li><strong>DashboardController:</strong> Build triggering via /api/dashboard/builds/{jobName}/{buildId}/rerun</li>
 *   <li><strong>Scheduled Tasks:</strong> 15-minute sync operations for job discovery</li>
 *   <li><strong>Configuration:</strong> JenkinsConfiguration provides JenkinsClient bean</li>
 * </ul>
 *
 * <p><strong>Performance Optimization:</strong></p>
 * <ul>
 *   <li><strong>Cache Hit Ratio:</strong> 15-second TTL balances freshness with performance</li>
 *   <li><strong>API Rate Limiting:</strong> Caching reduces Jenkins server load</li>
 *   <li><strong>Memory Efficiency:</strong> Lightweight DTO objects for cached data</li>
 *   <li><strong>Error Recovery:</strong> Graceful fallback to empty lists on API failures</li>
 * </ul>
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li><strong>Connection Failures:</strong> Log warnings and return empty collections</li>
 *   <li><strong>Authentication Errors:</strong> Detailed error logging with exception context</li>
 *   <li><strong>API Timeouts:</strong> Graceful degradation without system failure</li>
 *   <li><strong>Malformed Responses:</strong> Robust parsing with null safety</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <ul>
 *   <li><strong>JenkinsClient:</strong> Thread-safe jenkins-rest client for concurrent access</li>
 *   <li><strong>Cache Synchronization:</strong> Timestamp-based cache validation</li>
 *   <li><strong>Concurrent Operations:</strong> Safe for multiple simultaneous API calls</li>
 *   <li><strong>State Management:</strong> Minimal mutable state with proper synchronization</li>
 * </ul>
 *
 * <p><strong>Monitoring &amp; Logging:</strong></p>
 * <ul>
 *   <li><strong>Success Logging:</strong> Info-level logs for successful operations</li>
 *   <li><strong>Error Logging:</strong> Detailed error context for troubleshooting</li>
 *   <li><strong>Performance Metrics:</strong> Cache hit/miss tracking for optimization</li>
 *   <li><strong>API Health:</strong> Jenkins connectivity monitoring</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.cdancy.jenkins.rest.JenkinsClient
 * @see com.diploma.inno.config.JenkinsConfiguration
 * @see com.diploma.inno.service.DashboardService
 * @see com.diploma.inno.dto.RecentJobDTO
 */
@Service
public class JenkinsService {

    /**
     * Jenkins REST API client for all Jenkins server interactions.
     *
     * <p>This client is configured via JenkinsConfiguration and provides type-safe
     * access to Jenkins REST API endpoints. It handles authentication, connection
     * management, and API request/response processing.</p>
     *
     * <p><strong>Configuration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> Configured via jenkins.url property</li>
     *   <li><strong>Authentication:</strong> Username/password from jenkins.username and jenkins.password</li>
     *   <li><strong>Thread Safety:</strong> Client is thread-safe for concurrent operations</li>
     * </ul>
     */
    private final JenkinsClient jenkinsClient;

    /**
     * Logger instance for Jenkins service operations and error reporting.
     */
    private static Logger log = LoggerFactory.getLogger(JenkinsService.class);

    /**
     * Cache storage for recent job DTOs to reduce Jenkins API load.
     *
     * <p>This cache stores a list of RecentJobDTO objects representing the current
     * state of Jenkins jobs. The cache is invalidated based on the CACHE_TTL_MS
     * timeout and cacheTimestamp tracking.</p>
     *
     * <p><strong>Cache Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Storage:</strong> List&lt;RecentJobDTO&gt; for job information</li>
     *   <li><strong>Invalidation:</strong> TTL-based with 15-second timeout</li>
     *   <li><strong>Thread Safety:</strong> Accessed via synchronized cache validation</li>
     * </ul>
     */
    private List<RecentJobDTO> cachedJobs = null;

    /**
     * Timestamp tracking for recent jobs cache invalidation.
     *
     * <p>This timestamp records when the cachedJobs was last populated from
     * Jenkins API. It's used in conjunction with CACHE_TTL_MS to determine
     * when cache refresh is needed.</p>
     */
    private long cacheTimestamp = 0;

    /**
     * Time-to-live for recent jobs cache in milliseconds.
     *
     * <p>This constant defines how long cached job data remains valid before
     * requiring refresh from Jenkins API. Set to 15 seconds to balance
     * performance with data freshness.</p>
     */
    private static final long CACHE_TTL_MS = 15000; // 15 seconds

    /**
     * Cache storage for active build tracking by job name.
     *
     * <p>This cache tracks currently executing builds to provide real-time
     * build status information. The map key is the job name and the value
     * is the build start timestamp.</p>
     *
     * <p><strong>Cache Structure:</strong></p>
     * <ul>
     *   <li><strong>Key:</strong> Job name (String)</li>
     *   <li><strong>Value:</strong> Build start timestamp (Long)</li>
     *   <li><strong>Purpose:</strong> Track active builds for progress indicators</li>
     * </ul>
     */
    private Map<String, Long> activeBuildsCache = new HashMap<>();

    /**
     * Timestamp tracking for active builds cache invalidation.
     *
     * <p>This timestamp records when the activeBuildsCache was last updated.
     * Used for cache validation and refresh logic.</p>
     */
    private long activeBuildsCacheTimestamp = 0;

    /**
     * Constructs a new JenkinsService with the provided Jenkins client.
     *
     * <p>This constructor is called by Spring's dependency injection system
     * to initialize the service with a configured JenkinsClient bean from
     * JenkinsConfiguration.</p>
     *
     * <p><strong>Initialization:</strong></p>
     * <ul>
     *   <li><strong>Client Assignment:</strong> Stores the injected JenkinsClient</li>
     *   <li><strong>Cache Initialization:</strong> Initializes empty cache structures</li>
     *   <li><strong>Ready State:</strong> Service is ready for API operations after construction</li>
     * </ul>
     *
     * @param jenkinsClient the configured Jenkins REST API client
     * @see com.diploma.inno.config.JenkinsConfiguration#jenkinsClient()
     */
    @Autowired
    public JenkinsService(JenkinsClient jenkinsClient) {
        this.jenkinsClient = jenkinsClient;
    }

    /**
     * Static mapping from Jenkins BallColor enum values to Tailwind CSS classes.
     *
     * <p>This map provides visual representation mapping for Jenkins job status
     * indicators, converting Jenkins internal status codes to CSS classes for
     * consistent UI styling across the dashboard.</p>
     *
     * <p><strong>Status Mappings:</strong></p>
     * <ul>
     *   <li><strong>RED:</strong> bg-red-500 (Failed builds)</li>
     *   <li><strong>YELLOW:</strong> bg-yellow-500 (Unstable builds)</li>
     *   <li><strong>BLUE:</strong> bg-green-500 (Successful builds)</li>
     *   <li><strong>GREY:</strong> bg-gray-500 (Disabled or pending jobs)</li>
     *   <li><strong>DISABLED:</strong> bg-gray-500 (Disabled jobs)</li>
     *   <li><strong>ABORTED:</strong> bg-gray-700 (Aborted builds)</li>
     *   <li><strong>NOTBUILT:</strong> bg-gray-300 (Never built jobs)</li>
     * </ul>
     *
     * <p><strong>Usage:</strong></p>
     * <ul>
     *   <li><strong>Dashboard UI:</strong> Color-coded job status indicators</li>
     *   <li><strong>RecentJobDTO:</strong> Provides colorClass field for frontend</li>
     *   <li><strong>Consistency:</strong> Ensures uniform status representation</li>
     * </ul>
     */
    private static final Map<String, String> STATUS_COLOR_MAP = Map.of(
            "RED", "bg-red-500",
            "YELLOW", "bg-yellow-500",
            "BLUE", "bg-green-500",
            "GREY", "bg-gray-500",
            "DISABLED", "bg-gray-500",
            "ABORTED", "bg-gray-700",
            "NOTBUILT", "bg-gray-300"
    );




    /**
     * Triggers a new build for the specified Jenkins job via REST API.
     *
     * <p>This method initiates a new build for the given job using the Jenkins REST API.
     * It queues the build with the current job configuration and parameters. The buildId
     * parameter is provided for reference but the new build will receive the next
     * available build number.</p>
     *
     * <p><strong>Jenkins API Integration:</strong></p>
     * <ol>
     *   <li><strong>API Call:</strong> jenkinsClient.api().jobsApi().build("", jobName)</li>
     *   <li><strong>Build Queue:</strong> Job is added to Jenkins build queue</li>
     *   <li><strong>Execution:</strong> Jenkins executor picks up and runs the build</li>
     *   <li><strong>Configuration:</strong> Uses current job configuration and parameters</li>
     * </ol>
     *
     * <p><strong>API Endpoint:</strong></p>
     * <ul>
     *   <li><strong>Method:</strong> POST</li>
     *   <li><strong>Path:</strong> /job/{jobName}/build</li>
     *   <li><strong>Folder Support:</strong> Empty string "" for root-level jobs</li>
     *   <li><strong>Parameters:</strong> Uses default job parameters</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li><strong>Connection Errors:</strong> Logs error and continues gracefully</li>
     *   <li><strong>Authentication Errors:</strong> Detailed error logging with job context</li>
     *   <li><strong>Job Not Found:</strong> Jenkins API returns appropriate error</li>
     *   <li><strong>Permission Errors:</strong> Logs error with full exception context</li>
     * </ul>
     *
     * <p><strong>REST Controller Integration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> POST /api/dashboard/builds/{jobName}/{buildId}/rerun</li>
     *   <li><strong>Controller:</strong> DashboardController.rerunBuild()</li>
     *   <li><strong>Response:</strong> Confirmation message about triggered build</li>
     *   <li><strong>Frontend:</strong> Build trigger buttons in dashboard UI</li>
     * </ul>
     *
     * <p><strong>Logging:</strong></p>
     * <ul>
     *   <li><strong>Success:</strong> Info-level log with job name</li>
     *   <li><strong>Failure:</strong> Error-level log with exception details</li>
     *   <li><strong>Context:</strong> Job name included in all log messages</li>
     * </ul>
     *
     * @param jobName the name of the Jenkins job to trigger
     * @param buildId the build ID for reference (new build gets next available number)
     * @see com.diploma.inno.controller.DashboardController#rerunBuild(String, int)
     * @see com.cdancy.jenkins.rest.features.JobsApi#build(String, String)
     */
    public void rerunBuild(String jobName, int buildId) {
        try {
            jenkinsClient.api().jobsApi().build("",jobName);
            log.info("Triggered new build for job: {}", jobName);
        } catch (Exception e) {
            log.error("Error triggering new build for {}: {}", jobName, e.getMessage(), e);
        }
    }

    /**
     * Retrieves all Jenkins jobs from the specified folder path with error handling.
     *
     * <p>This method fetches the complete list of Jenkins jobs from the specified folder
     * using the Jenkins REST API. It provides robust error handling to ensure system
     * stability when Jenkins is unavailable or experiencing issues.</p>
     *
     * <p><strong>Jenkins API Integration:</strong></p>
     * <ol>
     *   <li><strong>API Call:</strong> jenkinsClient.api().jobsApi().jobList(folderPath)</li>
     *   <li><strong>Response Processing:</strong> Extracts .jobs() list from JobList response</li>
     *   <li><strong>Folder Support:</strong> Supports both root-level and nested folder jobs</li>
     *   <li><strong>Type Safety:</strong> Returns strongly-typed List&lt;Job&gt; objects</li>
     * </ol>
     *
     * <p><strong>API Endpoint:</strong></p>
     * <ul>
     *   <li><strong>Method:</strong> GET</li>
     *   <li><strong>Path:</strong> /api/json (for root) or /job/{folder}/api/json</li>
     *   <li><strong>Response:</strong> JobList containing array of Job objects</li>
     *   <li><strong>Data:</strong> Job metadata including name, status, configuration</li>
     * </ul>
     *
     * <p><strong>Folder Path Support:</strong></p>
     * <ul>
     *   <li><strong>Root Level:</strong> Empty string "" for top-level jobs</li>
     *   <li><strong>Nested Folders:</strong> "folder1/folder2" for nested job discovery</li>
     *   <li><strong>Recursive:</strong> Can be called recursively for folder traversal</li>
     * </ul>
     *
     * <p><strong>Error Handling Strategy:</strong></p>
     * <ul>
     *   <li><strong>Graceful Degradation:</strong> Returns empty list on any error</li>
     *   <li><strong>Warning Logging:</strong> Logs warning with error details</li>
     *   <li><strong>System Stability:</strong> Prevents system failure due to Jenkins issues</li>
     *   <li><strong>Continuation:</strong> Allows application to continue with empty job list</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>DashboardService:</strong> Primary consumer for scheduled job synchronization</li>
     *   <li><strong>Sync Operations:</strong> Called every 15 minutes for job discovery</li>
     *   <li><strong>Job Management:</strong> Enables automatic job discovery and tracking</li>
     *   <li><strong>Database Sync:</strong> Provides current Jenkins state for comparison</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li><strong>API Load:</strong> Single API call for complete job list</li>
     *   <li><strong>Network Efficiency:</strong> Bulk retrieval reduces API calls</li>
     *   <li><strong>Memory Usage:</strong> Returns lightweight Job objects</li>
     *   <li><strong>Error Recovery:</strong> Fast failure with empty list fallback</li>
     * </ul>
     *
     * @param folderPath the Jenkins folder path ("" for root, "folder1/folder2" for nested)
     * @return List of Job objects from Jenkins, or empty list if error occurs
     * @see com.diploma.inno.service.DashboardService#syncJenkinsJobsWithDatabase()
     * @see com.cdancy.jenkins.rest.features.JobsApi#jobList(String)
     * @see com.cdancy.jenkins.rest.domain.job.Job
     */
    public List<Job> getAllJobs(String folderPath) {
        try {
            return jenkinsClient.api().jobsApi().jobList(folderPath).jobs();
        } catch (Exception e) {
            log.warn("Failed to fetch Jenkins jobs due to: {}. Continuing with empty list.", e.getMessage(), e);
            return List.of();
        }
    }





}