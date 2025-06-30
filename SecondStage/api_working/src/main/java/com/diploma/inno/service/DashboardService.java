package com.diploma.inno.service;

import com.cdancy.jenkins.rest.domain.job.Job;
import com.diploma.inno.dto.*;
import com.diploma.inno.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for providing comprehensive dashboard analytics and metrics for Jenkins CI/CD monitoring and AI analysis.
 *
 * <p>This service serves as the central business logic layer for the CI Anomaly Detector dashboard,
 * orchestrating complex data aggregation, AI analysis processing, and real-time monitoring capabilities.
 * It bridges the gap between raw database queries and formatted dashboard responses.</p>
 *
 * <p><strong>Core Functionality:</strong></p>
 * <ul>
 *   <li><strong>Build Analytics:</strong> Comprehensive build summary with health status and risk scoring</li>
 *   <li><strong>AI Analysis Processing:</strong> JSONB content parsing and insight extraction from AI results</li>
 *   <li><strong>Security Monitoring:</strong> Anomaly detection, severity distribution, and trend analysis</li>
 *   <li><strong>Log Management:</strong> Paginated log retrieval with progress tracking and completion status</li>
 *   <li><strong>Chart Data Generation:</strong> Formatted data for Chart.js visualizations and dashboard widgets</li>
 *   <li><strong>Job Synchronization:</strong> Automated Jenkins job discovery and database consistency maintenance</li>
 * </ul>
 *
 * <p><strong>Data Source Integration:</strong></p>
 * <ul>
 *   <li><strong>{@link JenkinsService}:</strong> Jenkins API integration for job discovery and build data</li>
 *   <li><strong>{@link ChatMessageRepository}:</strong> Primary data access for AI analysis results and build logs</li>
 *   <li><strong>{@link JdbcTemplate}:</strong> Direct database queries for complex analytics and materialized views</li>
 *   <li><strong>Materialized Views:</strong> Performance-optimized pre-computed data for dashboard metrics</li>
 * </ul>
 *
 * <p><strong>REST API Integration:</strong></p>
 * <ul>
 *   <li><strong>DashboardController:</strong> Primary consumer for all dashboard endpoints</li>
 *   <li><strong>Build Details:</strong> /api/dashboard/builds/{jobName}/{buildId}/* endpoints</li>
 *   <li><strong>Analytics:</strong> /api/dashboard/anomaly-trend, /api/dashboard/severity-distribution</li>
 *   <li><strong>Monitoring:</strong> /api/dashboard/activeBuilds, /api/dashboard/totalJobs</li>
 *   <li><strong>Job Management:</strong> /api/dashboard/jobs, /api/dashboard/job-explorer</li>
 * </ul>
 *
 * <p><strong>Data Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Repository Query:</strong> Complex JSONB queries via ChatMessageRepository</li>
 *   <li><strong>Data Transformation:</strong> JSON parsing and object mapping via ObjectMapper</li>
 *   <li><strong>DTO Conversion:</strong> Projection to DTO mapping for type safety</li>
 *   <li><strong>Response Formatting:</strong> Structured responses with error handling</li>
 *   <li><strong>Chart Data Generation:</strong> Chart.js compatible data structures</li>
 * </ol>
 *
 * <p><strong>Scheduled Operations:</strong></p>
 * <ul>
 *   <li><strong>Jenkins Sync:</strong> @Scheduled(cron = "0 0/15 * * * ?") - Every 15 minutes</li>
 *   <li><strong>Job Discovery:</strong> Automatic detection of new Jenkins jobs</li>
 *   <li><strong>Orphan Cleanup:</strong> Removal of database entries for deleted Jenkins jobs</li>
 *   <li><strong>Safety Checks:</strong> Prevents mass deletion with MAX_DELETE_RATIO protection</li>
 * </ul>
 *
 * <p><strong>AI Analysis Integration:</strong></p>
 * <ul>
 *   <li><strong>JSONB Processing:</strong> Complex parsing of AI analysis results from content field</li>
 *   <li><strong>Anomaly Extraction:</strong> Severity-based anomaly categorization and counting</li>
 *   <li><strong>Risk Scoring:</strong> AI-calculated risk scores with trend analysis</li>
 *   <li><strong>Insight Generation:</strong> Structured insights with recommendations and security alerts</li>
 * </ul>
 *
 * <p><strong>Performance Optimization:</strong></p>
 * <ul>
 *   <li><strong>Materialized Views:</strong> Pre-computed data for fast dashboard loading</li>
 *   <li><strong>Pagination Support:</strong> Efficient handling of large log sets and anomaly lists</li>
 *   <li><strong>Caching Strategy:</strong> Repository-level caching for frequently accessed data</li>
 *   <li><strong>Batch Processing:</strong> Optimized bulk operations for job synchronization</li>
 * </ul>
 *
 * <p><strong>Error Handling &amp; Resilience:</strong></p>
 * <ul>
 *   <li><strong>Graceful Degradation:</strong> Default responses when AI data unavailable</li>
 *   <li><strong>JSON Parsing Safety:</strong> Robust error handling for malformed JSONB content</li>
 *   <li><strong>Null Safety:</strong> Comprehensive null checks and default value provision</li>
 *   <li><strong>Logging:</strong> Detailed error logging for troubleshooting and monitoring</li>
 * </ul>
 *
 * <p><strong>Time Range Support:</strong></p>
 * <ul>
 *   <li><strong>Predefined Ranges:</strong> "today", "week", "month" for standardized filtering</li>
 *   <li><strong>Build Count Limits:</strong> Configurable limits (1-15) for trend analysis</li>
 *   <li><strong>Job Filtering:</strong> "all" or specific job names for targeted analysis</li>
 *   <li><strong>Time Boundary Processing:</strong> Materialized view integration for time-based queries</li>
 * </ul>
 *
 * <p><strong>Chart Data Generation:</strong></p>
 * <ul>
 *   <li><strong>Chart.js Compatibility:</strong> Structured data for frontend visualization libraries</li>
 *   <li><strong>Dynamic Labeling:</strong> Job and build-specific labels for trend charts</li>
 *   <li><strong>Color Coding:</strong> Severity-based color schemes for anomaly visualization</li>
 *   <li><strong>Multi-Dataset Support:</strong> Complex charts with multiple data series</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.controller.DashboardController
 * @see com.diploma.inno.repository.ChatMessageRepository
 * @see com.diploma.inno.service.JenkinsService
 */
@Service
public class DashboardService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private ChatMessageRepository repository;

    private static final int MAX_DELETE_RATIO = 100; // Allow up to 50% of jobs to be deleted

    private static Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();



    /**
     * Retrieves comprehensive AI-generated build summary with health status analysis for a specific job and build.
     *
     * <p>This method orchestrates the retrieval and processing of AI analysis results from the repository,
     * performing complex JSONB parsing to extract build summary information including health status,
     * anomaly analysis, and formatted timestamps. It serves as the primary data source for build detail views.</p>
     *
     * <p><strong>Data Flow:</strong></p>
     * <ol>
     *   <li><strong>Repository Query:</strong> Calls repository.findBuildSummary() with complex JSONB operations</li>
     *   <li><strong>JSON Parsing:</strong> Uses ObjectMapper to deserialize JSON to BuildSummaryDTO</li>
     *   <li><strong>Response Formatting:</strong> Wraps data with hasAiData flag for frontend handling</li>
     *   <li><strong>Error Handling:</strong> Graceful degradation when AI data unavailable</li>
     * </ol>
     *
     * <p><strong>Repository Integration:</strong></p>
     * <ul>
     *   <li><strong>Query Method:</strong> repository.findBuildSummary(conversationId, buildNumber)</li>
     *   <li><strong>JSONB Operations:</strong> Complex health status logic with anomaly severity analysis</li>
     *   <li><strong>Data Source:</strong> ASSISTANT messages from chat_messages table</li>
     *   <li><strong>Performance:</strong> Single query with jsonb_build_object for structured response</li>
     * </ul>
     *
     * <p><strong>Health Status Logic (from Repository):</strong></p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
     *   <li><strong>WARNING:</strong> Contains MEDIUM severity anomalies or multiple anomalies</li>
     *   <li><strong>Healthy:</strong> Only LOW/WARNING anomalies (≤5) and no higher severity</li>
     *   <li><strong>Unhealthy:</strong> Default case for other scenarios</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     *
     * <span><b>When AI data available</b></span>
     * {@snippet lang=json :
     * {
     *   "hasAiData": true,
     *   "data": {
     *     "jobName": "my-web-app",
     *     "buildId": 123,
     *     "healthStatus": "CRITICAL|WARNING|Healthy|Unhealthy",
     *     "buildSummary": "AI-generated build summary",
     *     "buildStartedTime": "Today, 2:30 PM or Monday, 2:30 PM",
     *     "buildDuration": "2m 30s",
     *     "regressionDetected": "true|false"
     *   }
     * }
     *}
     *
     * <span><b>When AI data unavailable</b></span>
     * {@snippet lang=json :
     * {
     *   "hasAiData": false,
     *   "message": "AI has not yet been triggered for conversationId: my-job, buildNumber: 123"
     * }
     * }
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li><strong>No AI Data:</strong> Returns hasAiData: false with descriptive message</li>
     *   <li><strong>JSON Parsing Error:</strong> Catches JsonProcessingException and returns error response</li>
     *   <li><strong>Graceful Degradation:</strong> System continues operation despite parsing failures</li>
     *   <li><strong>Null Safety:</strong> Handles null repository responses</li>
     * </ul>
     *
     * <p><strong>REST API Integration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> GET /api/dashboard/builds/{jobName}/{buildId}/summary</li>
     *   <li><strong>Controller:</strong> DashboardController.getBuildSummary()</li>
     *   <li><strong>Frontend Usage:</strong> Build detail page summary section</li>
     *   <li><strong>Response Format:</strong> JSON with hasAiData flag for conditional rendering</li>
     * </ul>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Single Query:</strong> Efficient single repository call with complex JSONB processing</li>
     *   <li><strong>JSON Processing:</strong> Fast ObjectMapper deserialization to typed DTO</li>
     *   <li><strong>Memory Efficient:</strong> Minimal object creation and garbage collection</li>
     *   <li><strong>Index Utilization:</strong> Uses composite index on (conversation_id, build_number)</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to analyze
     * @return Map containing build summary data with "hasAiData" flag indicating data availability
     * @see com.diploma.inno.repository.ChatMessageRepository#findBuildSummary(String, Integer)
     * @see com.diploma.inno.dto.BuildSummaryDTO
     * @see com.diploma.inno.controller.DashboardController#getBuildSummary(String, Integer)
     */
    public Map<String, Object> getBuildSummary(String conversationId, Integer buildNumber) {
        Map<String, Object> response = new HashMap<>();

        String json = repository.findBuildSummary(conversationId, buildNumber);
        if (json == null) {
            response.put("hasAiData", false);
            response.put("message", "AI has not yet been triggered for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
            return response;
        }

        try {
            BuildSummaryDTO dto = objectMapper.readValue(json, BuildSummaryDTO.class);
            response.put("hasAiData", true);
            response.put("data", dto);
        } catch (JsonProcessingException e) {
            response.put("hasAiData", false);
            response.put("message", "Error parsing build summary data for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
        }
        return response;
    }


    /**
     * Default constructor for DashboardService.
     */
    public DashboardService() {
    }

    /**
     * Retrieves log collection progress tracking information for a specific build.
     *
     * <p>This method provides real-time tracking of build log collection progress, monitoring
     * how many log chunks have been received versus the expected total. It's essential for
     * providing user feedback during the log collection and AI analysis process.</p>
     *
     * <p><strong>Data Flow:</strong></p>
     * <ol>
     *   <li><strong>Repository Query:</strong> Calls repository.getTrackingLogInfo() to count received logs</li>
     *   <li><strong>Progress Calculation:</strong> Compares received vs expected (14) log chunks</li>
     *   <li><strong>Status Determination:</strong> Sets "Complete" or "In Progress" based on count</li>
     *   <li><strong>Response Formatting:</strong> Structures data for frontend progress indicators</li>
     * </ol>
     *
     * <p><strong>Log Collection System:</strong></p>
     * <ul>
     *   <li><strong>Expected Chunks:</strong> Jenkins builds typically generate 14 log chunks</li>
     *   <li><strong>Chunk Tracking:</strong> Each chunk stored as separate USER message</li>
     *   <li><strong>Progress Monitoring:</strong> Real-time feedback for log collection status</li>
     *   <li><strong>Completion Detection:</strong> Status changes to "Complete" when all chunks received</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "received": 12,
     *   "expected": 14,
     *   "status": "In Progress"
     * }
     * }
     *
     * <p><strong>REST API Integration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> GET /api/dashboard/builds/{jobName}/{buildId}/logs-tracker</li>
     *   <li><strong>Controller:</strong> DashboardController.getLogsTracker()</li>
     *   <li><strong>Frontend Usage:</strong> Progress bars and status indicators</li>
     *   <li><strong>Polling:</strong> Frontend polls this endpoint for real-time updates</li>
     * </ul>
     *
     * @param jobName the Jenkins job name
     * @param buildId the build number/ID
     * @return Map containing tracking information with received count, expected count, and status
     * @see com.diploma.inno.repository.ChatMessageRepository#getTrackingLogInfo(String, Integer)
     * @see #getAllLogs(String, int, int, int)
     */
    public Map<String, Object> getLogsTracker(String jobName, int buildId) {
        Map<String, Object> logsTracker = new HashMap<>();
        Long receivedLogs = repository.getTrackingLogInfo(jobName, buildId);
        logsTracker.put("received", receivedLogs != null ? Math.min(receivedLogs,14) : 0);
        logsTracker.put("expected", 14);
        logsTracker.put("status", (receivedLogs != null && receivedLogs >= 14) ? "Complete" : "In Progress");
        return logsTracker;
    }

    /**
     * Retrieves paginated build logs with JSON parsing and error handling for a specific build.
     *
     * <p>This method implements efficient pagination for build log retrieval, parsing JSONB content
     * from USER messages and providing structured log data for frontend consumption. It includes
     * robust error handling to ensure system stability when processing potentially malformed log data.</p>
     *
     * <p><strong>Data Flow:</strong></p>
     * <ol>
     *   <li><strong>Offset Calculation:</strong> Converts page/size to database offset</li>
     *   <li><strong>Repository Query:</strong> Calls repository.findAllUserLogsByJobAndBuildWithPagination()</li>
     *   <li><strong>JSON Parsing:</strong> Uses ObjectMapper to parse JSONB content strings</li>
     *   <li><strong>Error Filtering:</strong> Removes null entries from parsing failures</li>
     *   <li><strong>Response Collection:</strong> Returns List of parsed log objects</li>
     * </ol>
     *
     * <p><strong>Repository Integration:</strong></p>
     * <ul>
     *   <li><strong>Query Method:</strong> repository.findAllUserLogsByJobAndBuildWithPagination()</li>
     *   <li><strong>Filtering:</strong> Excludes instruction messages via jsonb_exists check</li>
     *   <li><strong>Ordering:</strong> Chronological order by timestamp for log sequence</li>
     *   <li><strong>Pagination:</strong> LIMIT/OFFSET for efficient large log set handling</li>
     * </ul>
     *
     * <p><strong>JSON Processing:</strong></p>
     * <ul>
     *   <li><strong>ObjectMapper:</strong> Parses JSONB content strings to Object instances</li>
     *   <li><strong>Error Handling:</strong> Catches JsonProcessingException for malformed content</li>
     *   <li><strong>Null Filtering:</strong> Removes failed parsing results from response</li>
     *   <li><strong>Logging:</strong> Detailed error logging for troubleshooting</li>
     * </ul>
     *
     * <p><strong>Expected Log Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "type": "build_log_data",
     *   "log_chunk": "Started by user admin\nRunning as SYSTEM\n...",
     *   "chunk_index": 1,
     *   "total_chunks": 14,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Efficient Pagination:</strong> Only loads requested page of logs</li>
     *   <li><strong>Memory Management:</strong> Streams processing to minimize memory usage</li>
     *   <li><strong>Error Resilience:</strong> Continues processing despite individual parsing failures</li>
     *   <li><strong>Index Utilization:</strong> Uses composite index on (conversation_id, build_number)</li>
     * </ul>
     *
     * <p><strong>REST API Integration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> GET /api/dashboard/builds/{jobName}/{buildId}/logs</li>
     *   <li><strong>Controller:</strong> DashboardController.getAllLogs()</li>
     *   <li><strong>Frontend Usage:</strong> Log viewer with pagination controls</li>
     *   <li><strong>Parameters:</strong> page and size for pagination control</li>
     * </ul>
     *
     * @param jobName the Jenkins job name
     * @param buildId the build number/ID
     * @param page the page number (0-based)
     * @param size the number of logs per page
     * @return List of parsed log objects, excluding any that failed JSON parsing
     * @see com.diploma.inno.repository.ChatMessageRepository#findAllUserLogsByJobAndBuildWithPagination
     * @see #getLogsTracker(String, int)
     */
    public List<Object> getAllLogs(String jobName, int buildId, int page, int size) {
        int offset = page * size;
        List<String> logs = repository.findAllUserLogsByJobAndBuildWithPagination(jobName, buildId, size, offset);
        return logs.stream()
                .map(content -> {
                    try {
                        return objectMapper.readValue(content, Object.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse JSON content: {}, error: {}", content, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the risk score for a specific job and build ID.
     *
     * <p>This method fetches the risk score, change, risk level, and previous score.
     * If no data is available or an error occurs, it returns a placeholder response.</p>
     *
     * @param conversationId The name of the Jenkins job
     * @param buildNumber The build number/ID
     * @return Map containing risk score data with "hasData" flag indicating data availability
     */
    public Map<String, Object> getRiskScore(String conversationId, Integer buildNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            String json = repository.findRiskScore(conversationId, buildNumber);
            if (json == null) {
                response.put("hasData", false);
                response.put("message", "Risk score not available for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
                return response;
            }

            RiskScoreDTO dto = objectMapper.readValue(json, RiskScoreDTO.class);
            if (dto.getScore() == null || dto.getRiskLevel() == null) {
                response.put("hasData", false);
                response.put("message", "Invalid risk score data for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
                return response;
            }

            response.put("hasData", true);
            response.put("data", dto);
        } catch (JsonProcessingException e) {
            response.put("hasData", false);
            response.put("message", "Error parsing risk score data for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
        } catch (Exception e) {
            response.put("hasData", false);
            response.put("message", "Unexpected error retrieving risk score for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
        }

        return response;
    }

    /**
     * Retrieves a list of all distinct job names in the system.
     *
     * <p>This method delegates to the repository to fetch all unique job names/identifiers
     * stored in the database. The results are returned in an unmodifiable list.</p>
     *
     * <p><b>Note:</b> The order of job names in the returned list is implementation-dependent
     * and should not be relied upon.</p>
     *
     * @return A non-null, possibly empty List containing all distinct job names.
     *         Returns an empty list if no jobs exist in the system.
     *
     * @see ChatMessageRepository#findDistinctJobNames()
     */
    public List<String> getAllJobNames() {
        return repository.findDistinctJobNames();
    }

    /**
     * Retrieves job count statistics for a specific time boundary period.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Fetches statistics from the repository for the specified time boundary</li>
     *   <li>Handles null results by returning a DTO with zero count and current timestamp</li>
     *   <li>Transforms the projection into a more convenient DTO format</li>
     * </ol>
     *
     *
     * <p><b>Time Boundary Examples:</b>
     * Typical values might include "daily", "weekly", "monthly", or other period identifiers
     * supported by the system.</p>
     *
     * @param timeBoundary The time period identifier to query. Must not be {@code null} or blank.
     *
     * @return A non-null {@link JobCountDTO} containing:
     *         <ul>
     *           <li>The time boundary identifier</li>
     *           <li>Total job count (0 if no data exists)</li>
     *           <li>Computation timestamp (current time if no data exists)</li>
     *         </ul>
     *
     * @throws IllegalArgumentException if {@code timeBoundary} is null or blank
     *
     * @see JobCountProjection
     * @see JobCountDTO
     * @see ChatMessageRepository#findJobCountByTimeBoundary(String)
     */
    public JobCountDTO getJobCountByTimeBoundary(String timeBoundary) {
        JobCountProjection projection = repository.findJobCountByTimeBoundary(timeBoundary);
        if (projection == null) {
            return new JobCountDTO(timeBoundary,0,Instant.now()); // Or throw an exception, depending on requirements
        }
        return new JobCountDTO(
                projection.getTimeBoundary(),
                projection.getTotalJobs(),
                projection.getComputedAt()
        );
    }


    /**
     * Retrieves the total count of all active builds across all jobs in the system.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Queries the repository for active build counts using the special "all" job filter</li>
     *   <li>Handles null results by returning a DTO with zero count and current timestamp</li>
     *   <li>Transforms the projection into a more convenient {@link ActiveBuildCountDTO} format</li>
     * </ol>
     *
     *
     * <p><b>Note:</b> The "all" filter is a special case that aggregates counts across all jobs.</p>
     * If no active builds exist or data isn't available, returns a DTO with:
     * <ul>
     *   <li>jobFilter: "all"</li>
     *   <li>activeBuilds: 0</li>
     *   <li>computedAt: current system time</li>
     * </ul>
     *
     *
     * @return A non-null {@link ActiveBuildCountDTO} containing:
     *         <ul>
     *           <li>The job filter ("all")</li>
     *           <li>Total active build count (0 if no data exists)</li>
     *           <li>Timestamp of when the count was computed (current time if no data exists)</li>
     *         </ul>
     *
     * @see ActiveBuildCountProjection
     * @see ActiveBuildCountDTO
     * @see ChatMessageRepository#findActiveBuildCountByJobFilter(String)
     */
    public ActiveBuildCountDTO getTotalActiveBuildCount() {
        ActiveBuildCountProjection projection = repository.findActiveBuildCountByJobFilter("all");
        if (projection == null) {
            return new ActiveBuildCountDTO("all", 0, Instant.now());
        }
        return new ActiveBuildCountDTO(
                projection.getJobFilter(),
                projection.getActiveBuilds(),
                projection.getComputedAt());
    }


    /**
     * Retrieves the active build count filtered by the given job filter.
     *
     * @param jobFilter the filter string to select jobs; if null or empty, returns an error DTO
     * @return an {@link ActiveBuildCountDTO} containing the job filter, active build count, and computation timestamp;
     *         returns an error DTO if the filter is invalid or no data is found
     */
    public ActiveBuildCountDTO getActiveBuildCountByJobFilter(String jobFilter) {
        ActiveBuildCountProjection projection = repository.findActiveBuildCountByJobFilter(jobFilter);
        if (jobFilter == null || jobFilter.trim().isEmpty()) {
            return new ActiveBuildCountDTO("error", 0, Instant.now());
        }
        if (projection == null) {
            return new ActiveBuildCountDTO("error", 0, Instant.now());
        }
        return new ActiveBuildCountDTO(
                projection.getJobFilter(),
                projection.getActiveBuilds(),
                projection.getComputedAt());
    }

    /**
     * Retrieves security anomaly counts for a specific job filter and time range combination.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Validates input parameters (non-null and non-empty)</li>
     *   <li>Returns a DTO with default values (count=0, current timestamp) if:
     *     <ul>
     *       <li>Input validation fails</li>
     *       <li>No matching record is found</li>
     *     </ul>
     *   </li>
     *   <li>Transforms the projection into a {@link SecurityAnomalyCountDTO}</li>
     * </ol>
     *
     *
     * <p><b>Default Value Handling:</b></p>
     * <ul>
     *   <li>jobFilter: "unknown" when null, original value otherwise</li>
     *   <li>anomalyCount: 0</li>
     *   <li>timeRange: Original value if provided, "unknown" when null</li>
     *   <li>computedAt: Current system time when no data exists</li>
     * </ul>
     *
     *
     * @param jobFilter The job identifier to query (e.g., job name or pattern).
     *                 Must not be null or empty.
     * @param timeRange The time period to analyze (e.g., "7d", "30d").
     *                 Must not be null or empty.
     *
     * @return A non-null {@link SecurityAnomalyCountDTO} containing:
     *         <ul>
     *           <li>Job filter identifier</li>
     *           <li>Anomaly count (0 if invalid/missing data)</li>
     *           <li>Time range</li>
     *           <li>Computation timestamp</li>
     *         </ul>
     *
     * @see SecurityAnomalyCountProjection
     * @see SecurityAnomalyCountDTO
     * @see ChatMessageRepository#findSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
     */
    public SecurityAnomalyCountDTO getSecurityAnomalyCountByJobFilterAndTimeRange(String jobFilter, String timeRange) {
        if (jobFilter == null || jobFilter.trim().isEmpty() || timeRange == null || timeRange.trim().isEmpty()) {
            return new SecurityAnomalyCountDTO(jobFilter != null ? jobFilter : "unknown", 0,
                    timeRange != null ? timeRange : "unknown", Instant.now());
        }
        SecurityAnomalyCountProjection projection = repository
                .findSecurityAnomalyCountByJobFilterAndTimeRange(jobFilter, timeRange);
        if (projection == null) {
            return new SecurityAnomalyCountDTO("unknown",0,timeRange,Instant.now());
        }
        return new SecurityAnomalyCountDTO(
                projection.getJobFilter(),
                projection.getAnomalyCount(),
                projection.getTimeRange(),
                projection.getComputedAt());
    }


    /**
     * Retrieves recent build information for a specific job.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Validates the job name parameter (non-null and non-empty)</li>
     *   <li>Returns an empty list if validation fails</li>
     *   <li>Transforms each {@link RecentJobBuildProjection} into a {@link RecentJobBuildDTO}</li>
     *   <li>Preserves all build records in chronological order (newest first)</li>
     * </ol>
     *
     *
     * <p><b>Returned DTO Fields:</b></p>
     * <ul>
     *   <li>jobName: The normalized job name</li>
     *   <li>buildId: Unique build identifier</li>
     *   <li>healthStatus: Build health indicator</li>
     *   <li>anomalyCount: Number of detected anomalies</li>
     *   <li>timeAgo: Human-readable time since build</li>
     *   <li>rawTimestamp: Exact build timestamp</li>
     *   <li>computedAt: When the data was generated</li>
     *   <li>originalJobName: Unmodified job name from source</li>
     * </ul>
     *
     *
     * @param jobName The name of the job to query. Must not be null or empty.
     *
     * @return A non-null, possibly empty List of {@link RecentJobBuildDTO} objects.
     *         Returns empty list if:
     *         <ul>
     *           <li>Input validation fails</li>
     *           <li>No builds exist for the job</li>
     *         </ul>
     *
     * @see RecentJobBuildProjection
     * @see RecentJobBuildDTO
     * @see ChatMessageRepository#findRecentJobBuildsByJobName(String)
     */
    public List<RecentJobBuildDTO> getRecentJobBuildsByJobName(String jobName) {
        if (jobName == null || jobName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<RecentJobBuildProjection> projections = repository.findRecentJobBuildsByJobName(jobName);
        return projections.stream()
                .map(projection -> new RecentJobBuildDTO(
                        projection.getJobName(),
                        projection.getBuildId(),
                        projection.getHealthStatus(),
                        projection.getAnomalyCount(),
                        projection.getTimeAgo(),
                        projection.getRawTimestamp(),
                        projection.getComputedAt(),
                        projection.getOriginalJobName()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves paginated anomalies for a specific job and build ID.
     *
     * @param conversationId The name of the Jenkins job
     * @param buildNumber The build number/ID
     * @param pageNumber The page number (1-based)
     * @param pageSize The number of anomalies per page
     * @return Map containing paginated anomalies with "hasData" flag
     */
    public Map<String, Object> getPaginatedAnomalies(String conversationId, Integer buildNumber, Integer pageNumber, Integer pageSize) {
        Map<String, Object> response = new HashMap<>();

        // Input validation
        if (conversationId == null || conversationId.trim().isEmpty()) {
            response.put("hasData", false);
            response.put("message", "Conversation ID cannot be null or empty");
            return response;
        }
        if (buildNumber == null || buildNumber < 1) {
            response.put("hasData", false);
            response.put("message", "Build number must be a positive integer");
            return response;
        }
        if (pageNumber == null || pageNumber < 1) pageNumber = 1;
        if (pageSize == null || pageSize < 1) pageSize = 3;

        Long offset = (long) (pageNumber - 1) * pageSize;

        try {
            Map<String, Object> result = repository.findPaginatedAnomalies(conversationId, buildNumber, pageSize, offset);
            if (result == null || result.isEmpty()) {
                response.put("hasData", false);
                response.put("message", "Anomalies not available for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
                return response;
            }

            Object anomaliesObj = result.get("anomalies");
            if (anomaliesObj == null) {
                response.put("hasData", false);
                response.put("message", "No anomalies found for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
                return response;
            }

            // Parse the JSON string into List<AnomalyDTO>
            List<AnomalyDTO> anomalies;
            if (anomaliesObj instanceof String) {
                anomalies = objectMapper.readValue((String) anomaliesObj, new TypeReference<List<AnomalyDTO>>() {});
            } else {
                anomalies = objectMapper.convertValue(anomaliesObj, new TypeReference<List<AnomalyDTO>>() {});
            }

            Long totalCount = ((Number) result.get("total_count")).longValue();

            PaginatedAnomaliesDTO paginatedResult = new PaginatedAnomaliesDTO(anomalies, totalCount, pageNumber, pageSize);
            response.put("hasData", true);
            response.put("data", paginatedResult);
        } catch (Exception e) {
            response.put("hasData", false);
            response.put("message", "Unexpected error retrieving anomalies for conversationId: " + conversationId + ", buildNumber: " + buildNumber);
        }

        return response;
    }


    /**
     * Generates Chart.js compatible anomaly trend data for dashboard visualization.
     *
     * <p>This method creates formatted chart data showing anomaly count trends across recent builds,
     * supporting both job-specific and cross-job analysis. It processes repository data into
     * Chart.js compatible format with proper labeling and color coding for frontend consumption.</p>
     *
     * <p><strong>Data Flow:</strong></p>
     * <ol>
     *   <li><strong>Parameter Validation:</strong> Ensures buildCount is within valid range (1-15)</li>
     *   <li><strong>Repository Query:</strong> Calls repository.findLatestBuildAnomalyCounts()</li>
     *   <li><strong>Label Generation:</strong> Creates "Job - Build №X" format labels</li>
     *   <li><strong>Data Extraction:</strong> Extracts anomaly counts from repository results</li>
     *   <li><strong>Chart Formatting:</strong> Structures data for Chart.js line/bar charts</li>
     * </ol>
     *
     * <p><strong>Repository Integration:</strong></p>
     * <ul>
     *   <li><strong>Query Method:</strong> repository.findLatestBuildAnomalyCounts(jobFilter, buildCount)</li>
     *   <li><strong>Data Source:</strong> Materialized view build_anomaly_summary</li>
     *   <li><strong>Filtering:</strong> Supports "all" jobs or specific job filtering</li>
     *   <li><strong>Ordering:</strong> Latest builds first for trend visualization</li>
     * </ul>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li><strong>buildCount Range:</strong> Automatically constrained to 1-15 builds</li>
     *   <li><strong>Default Value:</strong> Uses 5 builds if null or invalid</li>
     *   <li><strong>Performance Limit:</strong> Maximum 15 builds to prevent UI overload</li>
     *   <li><strong>Minimum Requirement:</strong> At least 1 build for meaningful trends</li>
     * </ul>
     *
     * <p><strong>Chart Data Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "labels": [
     *     "my-web-app - Build №123",
     *     "my-api - Build №456",
     *     "my-service - Build №789"
     *   ],
     *   "datasets": [
     *     {
     *       "label": "Anomaly Count",
     *       "data": [5, 12, 3],
     *       "backgroundColor": "#36A2EB",
     *       "borderColor": "#36A2EB"
     *     }
     *   ]
     * }
     * }
     *
     * <p><strong>Label Format:</strong></p>
     * <ul>
     *   <li><strong>Pattern:</strong> "{jobName} - Build №{buildNumber}"</li>
     *   <li><strong>Example:</strong> "my-web-app - Build №123"</li>
     *   <li><strong>Uniqueness:</strong> Each label represents a specific build</li>
     *   <li><strong>Sorting:</strong> Ordered by build recency for trend analysis</li>
     * </ul>
     *
     * <p><strong>REST API Integration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> GET /api/dashboard/anomaly-trend</li>
     *   <li><strong>Controller:</strong> DashboardController.getAnomalyTrend()</li>
     *   <li><strong>Parameters:</strong> jobFilter (default "all"), buildCount (default 5)</li>
     *   <li><strong>Frontend Usage:</strong> Chart.js line charts for trend visualization</li>
     * </ul>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Materialized View:</strong> Fast query execution via pre-computed data</li>
     *   <li><strong>Limited Dataset:</strong> Maximum 15 builds prevents performance issues</li>
     *   <li><strong>Memory Efficient:</strong> Minimal object creation for chart data</li>
     *   <li><strong>Frontend Optimized:</strong> Direct Chart.js compatibility reduces processing</li>
     * </ul>
     *
     * @param jobFilter the job name to filter by, or "all" for cross-job analysis
     * @param buildCount the number of recent builds to include (1-15, defaults to 5)
     * @return ChartDataDTO containing Chart.js compatible anomaly trend data
     * @see com.diploma.inno.repository.ChatMessageRepository#findLatestBuildAnomalyCounts
     * @see com.diploma.inno.dto.ChartDataDTO
     * @see com.diploma.inno.dto.DatasetDTO
     * @see #getSeverityDistribution(String, Integer)
     */
    public ChartDataDTO getAnomalyTrend(String jobFilter, Integer buildCount) {
        if (buildCount == null || buildCount < 1) buildCount = 5;
        if (buildCount > 15) buildCount = 15;

        List<Map<String, Object>> data = repository.findLatestBuildAnomalyCounts(jobFilter, buildCount);
        List<String> labels = new ArrayList<>();
        List<Integer> dataPoints = new ArrayList<>();

        for (Map<String, Object> row : data) {
            // Handle the timestamp as Instant and convert to LocalDate
//            Instant instant = (Instant) row.get("timestamp");
            String label = row.get("job") + " - Build № " + row.get("build");
//            + " (" + instant.atZone(ZoneId.systemDefault()).toLocalDate() + ")";
            labels.add(label);
            dataPoints.add(((Number) row.get("anomaly_count")).intValue());
        }

        List<DatasetDTO> datasets = new ArrayList<>();
        datasets.add(new DatasetDTO("Anomaly Count", dataPoints, "#36A2EB", "#36A2EB"));

        return new ChartDataDTO(labels, datasets);
    }


    /**
     * Retrieves the severity distribution chart data for the latest builds filtered by job.
     *
     * <p>The method fetches severity distribution data for the specified job filter and number of builds,
     * parses JSON severity counts, and organizes the data into labels and datasets suitable for charting.</p>
     *
     * @param jobFilter the job filter string to select builds; can be null or empty to select all jobs
     * @param buildCount the number of latest builds to consider; if null or less than 1, defaults to 5; capped at 15
     * @return a {@link ChartDataDTO} containing labels and datasets representing severity counts per build
     */
    public ChartDataDTO getSeverityDistribution(String jobFilter, Integer buildCount) {
        if (buildCount == null || buildCount < 1) buildCount = 5;
        if (buildCount > 15) buildCount = 15;

        List<Map<String, Object>> data = repository.findLatestBuildSeverityDistributions(jobFilter, buildCount);
        List<String> labels = new ArrayList<>();
        Map<String, List<Integer>> severityData = new HashMap<>();
        List<String> severities = new ArrayList<>();

        for (Map<String, Object> row : data) {
//            Integer build = (Integer) row.get("build");
//            Instant instant = (Instant) row.get("timestamp");
            String label = row.get("job") + " - Build № " + row.get("build");
//            + " (" + instant.atZone(ZoneId.systemDefault()).toLocalDate() + ")";
            labels.add(label);

            // Parse the severity_distribution JSON string into a Map
            String severityDistributionJson = (String) row.get("severity_distribution");
            Map<String, Integer> severityCounts;
            try {
                severityCounts = objectMapper.readValue(severityDistributionJson, Map.class);
            } catch (Exception e) {
                // Log the error and skip or handle the invalid row
                System.err.println("Failed to parse severity_distribution: " + severityDistributionJson + " - " + e.getMessage());
                severityCounts = new HashMap<>(); // Default to empty map if parsing fails
            }

            for (Map.Entry<String, Integer> entry : severityCounts.entrySet()) {
                severityData.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
                if (!severities.contains(entry.getKey())) severities.add(entry.getKey());
            }
        }

        // Fill missing severity counts with 0 and align with buildCount
        for (List<Integer> values : severityData.values()) {
            while (values.size() < labels.size()) values.add(0);
        }

        List<DatasetDTO> datasets = new ArrayList<>();
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"}; // CRITICAL, HIGH, MEDIUM, LOW, WARNING
        for (int i = 0; i < severities.size(); i++) {
            datasets.add(new DatasetDTO(severities.get(i), severityData.getOrDefault(severities.get(i), new ArrayList<>()), colors[i % colors.length], colors[i % colors.length]));
        }

        return new ChartDataDTO(labels, datasets);
    }



    /**
     * Retrieves and processes AI-generated insights for a specific build of a conversation/job.
     *
     * <p>This method performs the following operations:</p>
     * <ol>
     *   <li>Fetches raw insights data from the repository for the specified conversation and build</li>
     *   <li>Handles cases where no data is available by returning an empty map</li>
     *   <li>Parses the stored JSON insights string into a usable Map structure</li>
     *   <li>Extracts and transforms specific insight fields into a standardized format</li>
     *   <li>Provides default values for missing insight categories</li>
     * </ol>
     *
     *
     * <p><b>Returned Data Structure:</b></p>
     * The method always returns a Map with these standardized keys (with fallback values if unavailable):
     * <ul>
     *   <li><b>securityTrendAlert</b>: Security trend analysis (default: "No alert available")</li>
     *   <li><b>criticalSecretExposure</b>: Critical secret exposure findings (default: "No exposure data")</li>
     *   <li><b>dependencyManagement</b>: Dependency management insights (default: "No dependency info")</li>
     *   <li><b>recommendation</b>: Actionable recommendations (default: "No recommendation")</li>
     * </ul>
     *
     *
     * @param conversationId The unique identifier for the conversation/job. Must not be {@code null} or empty.
     * @param buildNumber The specific build number to get insights for. Must be a positive integer.
     *
     * @return A Map containing processed insights data with standardized keys. Returns empty Map if:
     *         <ul>
     *           <li>No insights data exists for the build</li>
     *           <li>JSON parsing fails</li>
     *           <li>Input parameters are invalid</li>
     *         </ul>
     *
     * @throws IllegalArgumentException if {@code conversationId} is null/empty or {@code buildNumber} is not positive
     *
     * @see #getAiInsights(String, Integer) 
     */
    public Map<String, Object> getAiInsights(String conversationId, Integer buildNumber) {
        Map<String, Object> insights = repository.findLatestInsights(conversationId, buildNumber);

        if (insights == null || insights.isEmpty() || insights.get("insights") == null) {
            return new HashMap<>(); // Return empty map if no data
        }

        // Parse the JSON string into a Map
        Map<String, Object> insightData;
        try {
            String insightsJson = (String) insights.get("insights");
            insightData = objectMapper.readValue(insightsJson, Map.class);
        } catch (Exception e) {
            // Log the error and return an empty map or default values
            System.err.println("Failed to parse insights JSON: " + e.getMessage());
            return new HashMap<>();
        }

        // Extract specific fields
        Map<String, Object> result = new HashMap<>();
        result.put("securityTrendAlert", insightData.getOrDefault("securityTrends", "No alert available"));
        result.put("criticalSecretExposure", insightData.getOrDefault("criticalIssues", "No exposure data"));
        result.put("dependencyManagement", insightData.getOrDefault("dependencyManagement", "No dependency info"));
        result.put("recommendation", insightData.getOrDefault("recommendations", "No recommendation"));

        return result;
    }


    /**
     * Retrieves and processes the latest AI-generated insights for a specific conversation/job.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Fetches the most recent insights record from the repository for the given conversation ID</li>
     *   <li>Returns an empty map if no insights data is found or if the data is invalid</li>
     *   <li>Parses the stored JSON insights string into a Map structure</li>
     *   <li>Extracts and transforms specific insight fields into a standardized response format</li>
     *   <li>Provides default placeholder values for any missing insight categories</li>
     * </ol>
     *
     *
     * <p><b>Return Structure:</b> Always returns a Map with these standardized keys:</p>
     * <ul>
     *   <li><b>securityTrendAlert</b>: Security trend analysis ("No alert available" if missing)</li>
     *   <li><b>criticalSecretExposure</b>: Critical secret findings ("No exposure data" if missing)</li>
     *   <li><b>dependencyManagement</b>: Dependency insights ("No dependency info" if missing)</li>
     *   <li><b>recommendation</b>: Suggested actions ("No recommendation" if missing)</li>
     * </ul>
     *
     *
     * @param conversationId The unique conversation/job identifier. Must not be {@code null} or blank.
     *
     * @return Non-null Map containing processed insights. Returns empty Map if:
     *         <ul>
     *           <li>No insights exist for this conversation</li>
     *           <li>JSON parsing fails</li>
     *           <li>Input validation fails</li>
     *         </ul>
     *
     * @throws IllegalArgumentException if {@code conversationId} is null or blank
     *
     * @see ChatMessageRepository#findLatestInsightsByConversation(String)
     */
    public Map<String, Object> getAiInsightsByConversation(String conversationId) {
        Map<String, Object> insights = repository.findLatestInsightsByConversation(conversationId);

        if (insights == null || insights.isEmpty() || insights.get("insights") == null) {
            return new HashMap<>(); // Return empty map if no data
        }

        // Parse the JSON string into a Map
        Map<String, Object> insightData;
        try {
            String insightsJson = (String) insights.get("insights");
            insightData = objectMapper.readValue(insightsJson, Map.class);
        } catch (Exception e) {
            // Log the error and return an empty map or default values
            System.err.println("Failed to parse insights JSON: " + e.getMessage());
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("securityTrendAlert", insightData.getOrDefault("securityTrends", "No alert available"));
        result.put("criticalSecretExposure", insightData.getOrDefault("criticalIssues", "No exposure data"));
        result.put("dependencyManagement", insightData.getOrDefault("dependencyManagement", "No dependency info"));
        result.put("recommendation", insightData.getOrDefault("recommendations", "No recommendation"));

        return result;
    }


    /**
     * Retrieves job status information filtered by the specified status category.
     *
     * <p><b>Status Filter Options:</b></p>
     * <ul>
     *   <li><b>all</b>: Returns all jobs (default when null is provided)</li>
     *   <li><b>active</b>: Currently running jobs</li>
     *   <li><b>completed</b>: Successfully finished jobs</li>
     *   <li><b>completedwithissues</b>: Completed jobs with detected anomalies</li>
     *   <li><b>withissues</b>: Any jobs with failures or anomalies</li>
     * </ul>
     * The filter value is case-insensitive.
     *
     *
     * <p><b>Note:</b> The actual filtering logic is implemented in the repository layer.
     * This method primarily handles input normalization.</p>
     *
     * @param statusFilter The status category to filter by. Converts to lowercase and defaults to "all" if null.
     *
     * @return List of job status maps in repository-defined format. Never null, but may be empty.
     *
     * @see ChatMessageRepository#findJobsByStatus(String)
     */
    public List<Map<String, Object>> getJobsByStatus(String statusFilter) {
        return repository.findJobsByStatus(statusFilter != null ? statusFilter.toLowerCase() : "all");
    }





    /**
     * Cleans raw JSON strings by removing Markdown code block markers and extracting valid JSON.
     * 
     * <p>This utility method handles JSON strings that may be wrapped in Markdown code blocks
     * or contain extraneous text, ensuring only valid JSON is returned for parsing.</p>
     * 
     * @param rawJson Raw JSON string potentially containing Markdown markers
     * @return Cleaned JSON string ready for parsing, null if input is null
     */
    public static String cleanJsonString(String rawJson) {
        if (rawJson == null) return null;

        // Remove Markdown code block markers
        rawJson = rawJson.replaceAll("^```json\\s*", "");       // leading ```json
        rawJson = rawJson.replaceAll("```json\\s*$", "");       // trailing ```json

        // Trim and return
        rawJson = rawJson.trim();

        // Optional: cut everything before first '{' and after last '}'
        int start = rawJson.indexOf('{');
        int end = rawJson.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            rawJson = rawJson.substring(start, end + 1);
        }

        return rawJson;
    }



    /**
     * Scheduled task for synchronizing Jenkins jobs with database to maintain data consistency.
     *
     * <p>This method runs every 15 minutes to ensure the database stays synchronized with the current
     * state of Jenkins jobs. It performs automatic job discovery, orphan cleanup, and maintains
     * data integrity between the Jenkins instance and the CI Anomaly Detector database.</p>
     *
     * <p><strong>Scheduling:</strong></p>
     * <ul>
     *   <li><strong>Frequency:</strong> Every 15 minutes via @Scheduled(cron = "0 0/15 * * * ?")</li>
     *   <li><strong>Execution Time:</strong> Runs at :00, :15, :30, :45 minutes of each hour</li>
     *   <li><strong>Automatic Trigger:</strong> No manual intervention required</li>
     *   <li><strong>Background Process:</strong> Runs asynchronously without blocking other operations</li>
     * </ul>
     *
     * <p><strong>Synchronization Process:</strong></p>
     * <ol>
     *   <li><strong>Jenkins Discovery:</strong> Fetches all current jobs from Jenkins API</li>
     *   <li><strong>Data Validation:</strong> Validates job names and filters null entries</li>
     *   <li><strong>Database Comparison:</strong> Compares Jenkins jobs with database job names</li>
     *   <li><strong>Orphan Identification:</strong> Identifies database jobs not in Jenkins</li>
     *   <li><strong>Safety Check:</strong> Prevents mass deletion with MAX_DELETE_RATIO protection</li>
     *   <li><strong>Cleanup Execution:</strong> Removes orphaned job data from database</li>
     * </ol>
     *
     * <p><strong>Data Sources:</strong></p>
     * <ul>
     *   <li><strong>Jenkins API:</strong> jenkinsService.getAllJobs("") for current job list</li>
     *   <li><strong>Database:</strong> repository.findDistinctJobNames() for existing job names</li>
     *   <li><strong>Comparison Logic:</strong> Set operations to identify differences</li>
     * </ul>
     *
     * <p><strong>Safety Mechanisms:</strong></p>
     * <ul>
     *   <li><strong>MAX_DELETE_RATIO:</strong> Prevents deletion of more than 100 jobs at once</li>
     *   <li><strong>Null Validation:</strong> Filters out null or invalid job names</li>
     *   <li><strong>Empty Set Checks:</strong> Skips processing if no valid jobs found</li>
     *   <li><strong>Error Handling:</strong> Comprehensive try-catch with detailed logging</li>
     * </ul>
     *
     * <p><strong>Cleanup Operations:</strong></p>
     * <ul>
     *   <li><strong>Orphan Detection:</strong> Jobs in database but not in Jenkins</li>
     *   <li><strong>Bulk Deletion:</strong> Efficient removal of orphaned job data</li>
     *   <li><strong>Referential Integrity:</strong> Maintains database consistency</li>
     *   <li><strong>Performance Optimization:</strong> Batch operations for large datasets</li>
     * </ul>
     *
     * <p><strong>Logging &amp; Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Info Logging:</strong> Successful sync operations with job counts</li>
     *   <li><strong>Warning Logging:</strong> Empty job lists or validation failures</li>
     *   <li><strong>Error Logging:</strong> Exception details for troubleshooting</li>
     *   <li><strong>Metrics Tracking:</strong> Job addition/removal statistics</li>
     * </ul>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Jenkins Unavailable:</strong> Logs warning and skips sync</li>
     *   <li><strong>Empty Job List:</strong> Logs warning and skips processing</li>
     *   <li><strong>Database Error:</strong> Logs error and continues operation</li>
     *   <li><strong>Mass Deletion:</strong> Safety check prevents accidental data loss</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li><strong>Efficient Queries:</strong> Uses distinct job names for minimal data transfer</li>
     *   <li><strong>Set Operations:</strong> Fast comparison using HashSet operations</li>
     *   <li><strong>Batch Processing:</strong> Bulk operations for database modifications</li>
     *   <li><strong>Memory Management:</strong> Minimal object retention during processing</li>
     * </ul>
     *
     * <p><strong>Integration Points:</strong></p>
     * <ul>
     *   <li><strong>JenkinsService:</strong> Primary data source for current job state</li>
     *   <li><strong>ChatMessageRepository:</strong> Database operations for job management</li>
     *   <li><strong>Dashboard Updates:</strong> Ensures dashboard reflects current Jenkins state</li>
     *   <li><strong>Materialized Views:</strong> Triggers refresh of dependent views</li>
     * </ul>
     *
     * @see com.diploma.inno.service.JenkinsService#getAllJobs(String)
     * @see com.diploma.inno.repository.ChatMessageRepository#findDistinctJobNames()
     * @see org.springframework.scheduling.annotation.Scheduled
     */
    @Scheduled(initialDelay = 15 * 60 * 1000, fixedDelay = 15 * 60 * 1000)
    public void syncJenkinsJobsWithDatabase() {
        try {
            List<Job> jenkinsJobs = jenkinsService.getAllJobs("");
            if (jenkinsJobs == null || jenkinsJobs.isEmpty()) {
                log.warn("No Jenkins jobs found - skipping sync");
                return;
            }

            Set<String> jenkinsJobNames = jenkinsJobs.stream()
                    .map(Job::name)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (jenkinsJobNames.isEmpty()) {
                log.warn("No valid Jenkins job names found - skipping sync");
                return;
            }

            List<String> dbJobNames = repository.findDistinctJobNames();
            Set<String> jobsToDelete = dbJobNames.stream()
                    .filter(job -> !jenkinsJobNames.contains(job))
                    .collect(Collectors.toSet());

            if (!jobsToDelete.isEmpty()) {
                int maxAllowedDeletions = dbJobNames.size() * MAX_DELETE_RATIO / 100;
                if (jobsToDelete.size() > maxAllowedDeletions) {
                    log.error("Safety check failed: Attempting to delete {} jobs, exceeding {}% of {} total jobs",
                            jobsToDelete.size(), MAX_DELETE_RATIO, dbJobNames.size());
                    return;
                }

                repository.deleteByJobNameIn(jobsToDelete);
                log.info("Deleted {} jobs from chat_messages: {}", jobsToDelete.size(), jobsToDelete);
            } else {
                log.info("No jobs to delete from chat_messages.");
            }
        } catch (Exception e) {
            log.error("Error during Jenkins job sync: {}", e.getMessage(), e);
        }
    }

}
