package com.diploma.inno.controller;

import com.diploma.inno.dto.*;
import com.diploma.inno.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for the CI Anomaly Detector dashboard functionality.
 *
 * <p>This controller serves as the primary REST interface for a comprehensive Jenkins CI/CD monitoring
 * and anomaly detection system. It provides endpoints for accessing real-time build data, AI-powered
 * analysis results, security anomaly detection, and dashboard analytics.</p>
 *
 * <p><strong>Base URL:</strong> {@code /api/dashboard}</p>
 *
 * <p><strong>System Architecture Overview:</strong></p>
 * <ul>
 *   <li><strong>Data Sources:</strong> Jenkins REST API, PostgreSQL database with JSONB storage</li>
 *   <li><strong>AI Integration:</strong> Machine learning analysis stored in chat_messages table</li>
 *   <li><strong>Caching Strategy:</strong> Materialized views and computed metrics tables</li>
 *   <li><strong>Sync Process:</strong> Scheduled 15-minute Jenkins job synchronization</li>
 * </ul>
 *
 * <p><strong>Database Schema Dependencies:</strong></p>
 * <ul>
 *   <li><strong>chat_messages:</strong> Core table storing AI analysis results and build logs in JSONB format</li>
 *   <li><strong>recent_job_builds:</strong> Materialized view for recent build metrics and health status</li>
 *   <li><strong>security_anomaly_counts:</strong> Aggregated security anomaly statistics by job and time range</li>
 *   <li><strong>active_build_counts:</strong> Real-time active build statistics by job filter</li>
 *   <li><strong>job_counts:</strong> Job activity metrics grouped by time boundaries</li>
 *   <li><strong>build_anomaly_summary:</strong> Aggregated anomaly data for trend analysis</li>
 * </ul>
 *
 * <p><strong>Core Functionality Categories:</strong></p>
 * <ul>
 *   <li><strong>Build Monitoring:</strong> Real-time build status, recent builds, active build tracking</li>
 *   <li><strong>Anomaly Detection:</strong> AI-powered security anomaly identification and classification</li>
 *   <li><strong>Analytics &amp; Visualization:</strong> Chart data for trends, severity distributions, metrics</li>
 *   <li><strong>Job Management:</strong> Job discovery, status filtering, build triggering</li>
 *   <li><strong>AI Insights:</strong> Machine learning analysis, recommendations, risk scoring</li>
 *   <li><strong>Log Analysis:</strong> Paginated build log retrieval and processing status</li>
 * </ul>
 *
 * <p><strong>Data Processing Flow:</strong></p>
 * <ol>
 *   <li><strong>Jenkins Integration:</strong> Real-time data fetching via jenkins-rest library</li>
 *   <li><strong>AI Analysis:</strong> Build logs processed and results stored as JSONB in chat_messages</li>
 *   <li><strong>Data Aggregation:</strong> Complex SQL queries with CTEs for dashboard metrics</li>
 *   <li><strong>Caching Layer:</strong> Materialized views and computed tables for performance</li>
 *   <li><strong>API Response:</strong> DTO mapping and JSON serialization for frontend consumption</li>
 * </ol>
 *
 * <p><strong>Response Format Standards:</strong></p>
 * <ul>
 *   <li><strong>Structured DTOs:</strong> Type-safe data transfer objects with computed timestamps</li>
 *   <li><strong>Chart Data:</strong> Standardized format for Chart.js and similar visualization libraries</li>
 *   <li><strong>Paginated Results:</strong> Consistent pagination with total counts and metadata</li>
 *   <li><strong>Error Responses:</strong> Structured error information with context</li>
 * </ul>
 *
 * <p><strong>Performance Optimizations:</strong></p>
 * <ul>
 *   <li><strong>Database Projections:</strong> Custom projection interfaces to minimize data transfer</li>
 *   <li><strong>Query Optimization:</strong> Complex CTEs with window functions for efficient aggregation</li>
 *   <li><strong>JSONB Operations:</strong> Native PostgreSQL JSONB queries for AI analysis data</li>
 *   <li><strong>Pagination:</strong> LIMIT/OFFSET for large datasets with configurable page sizes</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li>⚠️ <strong>CRITICAL:</strong> No authentication/authorization currently implemented</li>
 *   <li>🔒 <strong>RECOMMENDATION:</strong> Implement Spring Security with role-based access control</li>
 *   <li>🛡️ <strong>INPUT VALIDATION:</strong> Add @Valid annotations and parameter validation</li>
 *   <li>📝 <strong>AUDIT LOGGING:</strong> Consider implementing audit trails for sensitive operations</li>
 * </ul>
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li><strong>404 Not Found:</strong> When requested data doesn't exist in database</li>
 *   <li><strong>500 Internal Server Error:</strong> For JSON processing failures and database errors</li>
 *   <li><strong>400 Bad Request:</strong> For invalid parameters or malformed requests</li>
 *   <li><strong>Graceful Degradation:</strong> Empty responses instead of exceptions where appropriate</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see DashboardService
 * @see JenkinsService
 * @see com.diploma.inno.repository.ChatMessageRepository
 * @see com.diploma.inno.dto
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /**
     * Service for dashboard-related business logic and data aggregation.
     *
     * <p>This service handles the core dashboard functionality including:</p>
     * <ul>
     *   <li><strong>Data Aggregation:</strong> Complex SQL queries with CTEs and window functions</li>
     *   <li><strong>AI Analysis Processing:</strong> JSONB content parsing from chat_messages table</li>
     *   <li><strong>Chart Data Generation:</strong> Formatted data for Chart.js visualizations</li>
     *   <li><strong>Risk Score Calculations:</strong> AI-powered risk assessment and trending</li>
     *   <li><strong>Database Query Execution:</strong> Custom projections and result mapping</li>
     *   <li><strong>Scheduled Synchronization:</strong> 15-minute Jenkins job sync with safety checks</li>
     * </ul>
     *
     * <p><strong>Database Dependencies:</strong></p>
     * <ul>
     *   <li>ChatMessageRepository for AI analysis results and build logs</li>
     *   <li>JdbcTemplate for direct database queries when needed</li>
     *   <li>Materialized views for performance-optimized data access</li>
     * </ul>
     *
     * @see DashboardService
     * @see com.diploma.inno.repository.ChatMessageRepository
     */
    @Autowired
    private DashboardService dashboardService;

    /**
     * Service for Jenkins-specific operations and integrations.
     *
     * <p>This service provides direct integration with Jenkins server for:</p>
     * <ul>
     *   <li><strong>Build Triggering:</strong> REST API calls to trigger new builds</li>
     *   <li><strong>Job Information:</strong> Real-time job discovery and metadata retrieval</li>
     *   <li><strong>Status Monitoring:</strong> Active build tracking and health checks</li>
     *   <li><strong>API Communication:</strong> jenkins-rest library integration with error handling</li>
     * </ul>
     *
     * <p><strong>Caching Strategy:</strong></p>
     * <ul>
     *   <li>15-second TTL cache for recent jobs to reduce Jenkins API load</li>
     *   <li>Active builds cache with timestamp-based invalidation</li>
     * </ul>
     *
     * @see JenkinsService
     * @see com.cdancy.jenkins.rest.JenkinsClient
     */
    @Autowired
    private JenkinsService jenkinsService;

    /**
     * Default constructor for DashboardController.
     */
    public DashboardController() {
    }

    /**
     * Retrieves recent build information for all Jenkins jobs.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code recent_job_builds} materialized view</li>
     *   <li>Filters by jobName = "all" to include all jobs</li>
     *   <li>Orders by raw_timestamp DESC for most recent builds first</li>
     *   <li>Maps to RecentJobBuildProjection interface</li>
     *   <li>Converts to RecentJobBuildDTO with computed timestamps</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/recentJobBuilds}</p>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <p>Each build entry includes:</p>
     * <ul>
     *   <li><strong>jobName:</strong> Display name of the Jenkins job</li>
     *   <li><strong>buildId:</strong> Unique build number from Jenkins</li>
     *   <li><strong>healthStatus:</strong> Computed status (SUCCESS, FAILURE, WARNING, CRITICAL)</li>
     *   <li><strong>anomalyCount:</strong> Number of AI-detected anomalies in the build</li>
     *   <li><strong>timeAgo:</strong> Human-readable relative time (e.g., "2 hours ago")</li>
     *   <li><strong>rawTimestamp:</strong> ISO timestamp for precise sorting</li>
     *   <li><strong>computedAt:</strong> When the materialized view was last refreshed</li>
     *   <li><strong>originalJobName:</strong> Original Jenkins job name for API calls</li>
     * </ul>
     *
     * <p><strong>Health Status Calculation:</strong></p>
     * <p>Status is computed based on anomaly severity in the AI analysis:</p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
     *   <li><strong>WARNING:</strong> Contains MEDIUM severity or multiple anomalies</li>
     *   <li><strong>SUCCESS:</strong> No significant anomalies detected</li>
     * </ul>
     *
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li>Data served from materialized view for fast response times</li>
     *   <li>View refreshed periodically via scheduled tasks</li>
     *   <li>Typical response includes 10-50 recent builds across all jobs</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Dashboard overview displaying recent build activity</li>
     *   <li>Build health monitoring across all projects</li>
     *   <li>Quick identification of failed or problematic builds</li>
     *   <li>Anomaly detection summary for recent builds</li>
     * </ul>
     *
     * @return {@link ResponseEntity} containing a list of {@link RecentJobBuildDTO} objects
     *         representing recent builds across all jobs, with HTTP 200 OK status
     *
     * @see RecentJobBuildDTO
     * @see DashboardService#getRecentJobBuildsByJobName(String)
     * @see com.diploma.inno.dto.RecentJobBuildProjection
     */
    @GetMapping("/recentJobBuilds")
    public ResponseEntity<List<RecentJobBuildDTO>> getAllRecentBuilds() {
        List<RecentJobBuildDTO> builds = dashboardService.getRecentJobBuildsByJobName("all");
        return ResponseEntity.ok(builds);
    }

    /**
     * Retrieves recent build information for a specific Jenkins job.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code recent_job_builds} materialized view</li>
     *   <li>Filters WHERE job_name = :jobName for specific job</li>
     *   <li>Orders by raw_timestamp DESC for chronological order</li>
     *   <li>Returns List&lt;RecentJobBuildProjection&gt; mapped to DTOs</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/recentJobBuilds/{jobName}}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> The exact name of the Jenkins job to query (case-sensitive)</li>
     * </ul>
     *
     * <p><strong>Job Name Handling:</strong></p>
     * <ul>
     *   <li>Must match the original Jenkins job name exactly</li>
     *   <li>Special characters and spaces are preserved</li>
     *   <li>URL encoding is handled automatically by Spring</li>
     *   <li>Empty or null jobName returns empty list</li>
     * </ul>
     *
     * <p><strong>Response Data:</strong></p>
     * <p>Returns the same structure as {@link #getAllRecentBuilds()} but filtered
     * to only include builds from the specified job. Typical response includes
     * 5-20 recent builds for the job depending on build frequency.</p>
     *
     * <p><strong>Data Freshness:</strong></p>
     * <ul>
     *   <li>Data sourced from materialized view refreshed every 15 minutes</li>
     *   <li>computedAt timestamp indicates last refresh time</li>
     *   <li>May not include builds triggered in the last few minutes</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Job-specific build history analysis</li>
     *   <li>Monitoring build trends for a particular project</li>
     *   <li>Detailed anomaly tracking for specific jobs</li>
     *   <li>Project-focused dashboard views</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=TEXT :
     * GET /api/dashboard/recentJobBuilds/my-web-app
     * GET /api/dashboard/recentJobBuilds/backend-service
     * GET /api/dashboard/recentJobBuilds/My%20Project%20With%20Spaces
     * }
     *
     * @param jobName the name of the Jenkins job to retrieve recent builds for
     * @return {@link ResponseEntity} containing a list of {@link RecentJobBuildDTO} objects
     *         for the specified job, with HTTP 200 OK status
     *
     * @see RecentJobBuildDTO
     * @see DashboardService#getRecentJobBuildsByJobName(String)
     * @see com.diploma.inno.dto.RecentJobBuildProjection
     */
    @GetMapping("/recentJobBuilds/{jobName}")
    public ResponseEntity<List<RecentJobBuildDTO>> getRecentBuildsByJobName(@PathVariable String jobName) {
        List<RecentJobBuildDTO> builds = dashboardService.getRecentJobBuildsByJobName(jobName);
        return ResponseEntity.ok(builds);
    }

    /**
     * Retrieves security anomaly count summary for all jobs with default time range.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code security_anomaly_counts} aggregated table</li>
     *   <li>Filters WHERE job_filter = 'all' AND time_range = '7 days'</li>
     *   <li>Returns SecurityAnomalyCountProjection mapped to DTO</li>
     *   <li>Includes computed timestamp from last aggregation run</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/securityAnomalies}</p>
     *
     * <p><strong>Default Parameters:</strong></p>
     * <ul>
     *   <li><strong>Job Filter:</strong> "all" (includes all jobs)</li>
     *   <li><strong>Time Range:</strong> "7 days" (last week)</li>
     * </ul>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> The filter applied ("all")</li>
     *   <li><strong>anomalyCount:</strong> Total number of security anomalies detected</li>
     *   <li><strong>timeRange:</strong> The time period analyzed ("7 days")</li>
     *   <li><strong>computedAt:</strong> Timestamp when the aggregation was last calculated</li>
     * </ul>
     *
     * <p><strong>Security Anomaly Classification:</strong></p>
     * <p>Includes various types of security-related issues detected by AI analysis:</p>
     * <ul>
     *   <li><strong>Dependency Vulnerabilities:</strong> Known CVEs in project dependencies</li>
     *   <li><strong>Security Scan Failures:</strong> Failed security scans or policy violations</li>
     *   <li><strong>Authentication Issues:</strong> Authentication/authorization problems</li>
     *   <li><strong>Suspicious Patterns:</strong> Unusual build patterns or behaviors</li>
     *   <li><strong>Code Quality Security:</strong> Security-related code quality violations</li>
     *   <li><strong>Secret Exposure:</strong> Potential secrets or credentials in logs</li>
     * </ul>
     *
     * <p><strong>Data Aggregation Process:</strong></p>
     * <ul>
     *   <li>AI analysis results from chat_messages table are processed</li>
     *   <li>JSONB content is parsed for anomaly severity and type</li>
     *   <li>Security-specific anomalies are filtered and counted</li>
     *   <li>Results are aggregated by job filter and time range</li>
     *   <li>Computed metrics are stored in security_anomaly_counts table</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Security dashboard overview widgets</li>
     *   <li>High-level security health monitoring</li>
     *   <li>Trend analysis for security improvements</li>
     *   <li>Executive reporting on security posture</li>
     * </ul>
     *
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li>Data served from pre-computed aggregation table</li>
     *   <li>Aggregations refreshed periodically via scheduled tasks</li>
     *   <li>Fast response time due to materialized data</li>
     * </ul>
     *
     * @return {@link ResponseEntity} containing {@link SecurityAnomalyCountDTO} with
     *         anomaly count data, or HTTP 404 Not Found if no data is available
     *
     * @see SecurityAnomalyCountDTO
     * @see DashboardService#getSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
     * @see com.diploma.inno.dto.SecurityAnomalyCountProjection
     */
    @GetMapping("/securityAnomalies")
    public ResponseEntity<SecurityAnomalyCountDTO> getAllSecurityAnomalies() {
        SecurityAnomalyCountDTO anomalies = dashboardService.getSecurityAnomalyCountByJobFilterAndTimeRange("all","7 days");
        if (anomalies == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(anomalies);
    }

    /**
     * Retrieves security anomaly count for specific job filter and time range.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code security_anomaly_counts} aggregated table</li>
     *   <li>Filters WHERE job_filter = :jobFilter AND time_range = :timeRange</li>
     *   <li>Returns SecurityAnomalyCountProjection with computed metrics</li>
     *   <li>Handles null/empty parameters with validation</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/securityAnomalies/{jobFilter}}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> Job name or "all" for all jobs</li>
     * </ul>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>timeRange:</strong> Time period to analyze (required)</li>
     * </ul>
     *
     * <p><strong>Supported Time Ranges:</strong></p>
     * <ul>
     *   <li><strong>"7 days":</strong> Last week of build activity</li>
     *   <li><strong>"14 days":</strong> Last two weeks</li>
     *   <li><strong>"30 days":</strong> Last month</li>
     *   <li><strong>"90 days":</strong> Last quarter</li>
     *   <li><strong>"1 year":</strong> Last year</li>
     * </ul>
     *
     * <p><strong>Job Filter Options:</strong></p>
     * <ul>
     *   <li><strong>"all":</strong> Include all Jenkins jobs in the analysis</li>
     *   <li><strong>Specific job name:</strong> Filter to a single job (exact match)</li>
     *   <li><strong>Job pattern:</strong> May support pattern matching (implementation dependent)</li>
     * </ul>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>Empty or null jobFilter defaults to "unknown" with 0 count</li>
     *   <li>Empty or null timeRange defaults to "unknown" with 0 count</li>
     *   <li>Invalid combinations return default DTO with error indicators</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/securityAnomalies/my-web-app?timeRange=30%20days
     * GET /api/dashboard/securityAnomalies/all?timeRange=7%20days
     * GET /api/dashboard/securityAnomalies/backend-service?timeRange=90%20days
     * }
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Project-specific security analysis</li>
     *   <li>Historical security trend analysis</li>
     *   <li>Comparative security assessment across time periods</li>
     *   <li>Focused security monitoring for critical projects</li>
     * </ul>
     *
     * <p><strong>Response Handling:</strong></p>
     * <ul>
     *   <li>Always returns HTTP 200 OK (no 404 for this endpoint)</li>
     *   <li>Missing data returns DTO with 0 count and error indicators</li>
     *   <li>Graceful degradation for invalid parameters</li>
     * </ul>
     *
     * @param jobFilter the job name to filter by, or "all" for all jobs
     * @param timeRange the time period to analyze (e.g., "7 days", "30 days")
     * @return {@link ResponseEntity} containing {@link SecurityAnomalyCountDTO} with
     *         filtered anomaly count data, with HTTP 200 OK status
     *
     * @see SecurityAnomalyCountDTO
     * @see DashboardService#getSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
     * @see com.diploma.inno.dto.SecurityAnomalyCountProjection
     */
    @GetMapping("/securityAnomalies/{jobFilter}")
    public ResponseEntity<SecurityAnomalyCountDTO> getSecurityAnomaliesByJobFilterAndTimeRange(
            @PathVariable String jobFilter,
            @RequestParam String timeRange) {
        SecurityAnomalyCountDTO anomaly = dashboardService.getSecurityAnomalyCountByJobFilterAndTimeRange(jobFilter, timeRange);
        return ResponseEntity.ok(anomaly);
    }

    /**
     * Retrieves the total count of currently active builds across all Jenkins jobs.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code active_build_counts} aggregated table</li>
     *   <li>Filters WHERE job_filter = 'all' for system-wide count</li>
     *   <li>Returns ActiveBuildCountProjection mapped to DTO</li>
     *   <li>Includes computed timestamp from last aggregation run</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/activeBuilds}</p>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> "all" (indicates all jobs are included)</li>
     *   <li><strong>activeBuilds:</strong> Total number of builds currently running</li>
     *   <li><strong>computedAt:</strong> Timestamp when the count was calculated</li>
     * </ul>
     *
     * <p><strong>Active Build Definition:</strong></p>
     * <p>Includes builds in the following states:</p>
     * <ul>
     *   <li><strong>Currently Executing:</strong> Builds actively running build steps</li>
     *   <li><strong>Queue Waiting:</strong> Builds waiting in the Jenkins build queue</li>
     *   <li><strong>Paused/Manual:</strong> Builds paused for manual intervention</li>
     *   <li><strong>Starting Up:</strong> Builds that are initializing or starting</li>
     * </ul>
     *
     * <p><strong>Data Aggregation Process:</strong></p>
     * <ul>
     *   <li>Real-time Jenkins API calls to check build status</li>
     *   <li>Build queue analysis for pending builds</li>
     *   <li>Job status evaluation across all configured jobs</li>
     *   <li>Results cached in active_build_counts table</li>
     *   <li>Periodic refresh to maintain accuracy</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>System load monitoring and capacity planning</li>
     *   <li>Dashboard widgets showing current activity</li>
     *   <li>Performance analysis and bottleneck identification</li>
     *   <li>Resource utilization tracking</li>
     * </ul>
     *
     * <p><strong>Data Freshness:</strong></p>
     * <ul>
     *   <li>Data typically cached with short TTL (15-30 seconds)</li>
     *   <li>Balance between real-time accuracy and system performance</li>
     *   <li>computedAt timestamp indicates last refresh time</li>
     * </ul>
     *
     * @return {@link ResponseEntity} containing {@link ActiveBuildCountDTO} with
     *         total active build count, or HTTP 404 Not Found if no data is available
     *
     * @see ActiveBuildCountDTO
     * @see DashboardService#getTotalActiveBuildCount()
     * @see com.diploma.inno.dto.ActiveBuildCountProjection
     */
    @GetMapping("/activeBuilds")
    public ResponseEntity<ActiveBuildCountDTO> getAllActiveBuilds() {
        ActiveBuildCountDTO activeBuild = dashboardService.getTotalActiveBuildCount();
        if (activeBuild == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(activeBuild);
    }

    /**
     * Retrieves the count of active builds filtered by job name or pattern.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code active_build_counts} aggregated table</li>
     *   <li>Filters WHERE job_filter = :jobFilter for specific job/pattern</li>
     *   <li>Returns ActiveBuildCountProjection with filtered metrics</li>
     *   <li>Handles validation and error cases gracefully</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/activeBuilds/{jobFilter}}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> Job name, pattern, or "all" for all jobs</li>
     * </ul>
     *
     * <p><strong>Job Filter Options:</strong></p>
     * <ul>
     *   <li><strong>Specific job name:</strong> Count active builds for a single job</li>
     *   <li><strong>"all":</strong> Count active builds across all jobs (same as {@link #getAllActiveBuilds()})</li>
     *   <li><strong>Pattern matching:</strong> May support wildcards or regex (implementation dependent)</li>
     * </ul>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> The filter that was applied</li>
     *   <li><strong>activeBuilds:</strong> Number of active builds matching the filter</li>
     *   <li><strong>computedAt:</strong> Timestamp when the count was calculated</li>
     * </ul>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>Null or empty jobFilter returns DTO with "error" filter and 0 count</li>
     *   <li>Invalid job names return DTO with "error" filter and 0 count</li>
     *   <li>Missing data returns DTO with "error" filter and 0 count</li>
     *   <li>Graceful degradation prevents exceptions</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/activeBuilds/my-web-app
     * GET /api/dashboard/activeBuilds/backend-*
     * GET /api/dashboard/activeBuilds/all
     * }
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Project-specific build monitoring</li>
     *   <li>Team-focused dashboard views</li>
     *   <li>Load balancing and resource allocation</li>
     *   <li>Build queue analysis for specific projects</li>
     * </ul>
     *
     * @param jobFilter the job name, pattern, or "all" to filter active builds by
     * @return {@link ResponseEntity} containing {@link ActiveBuildCountDTO} with
     *         filtered active build count, or HTTP 404 Not Found if no data is available
     *
     * @see ActiveBuildCountDTO
     * @see DashboardService#getActiveBuildCountByJobFilter(String)
     * @see com.diploma.inno.dto.ActiveBuildCountProjection
     */
    @GetMapping("/activeBuilds/{jobFilter}")
    public ResponseEntity<ActiveBuildCountDTO> getActiveBuildsByJobFilter(@PathVariable String jobFilter) {
        ActiveBuildCountDTO activeBuild = dashboardService.getActiveBuildCountByJobFilter(jobFilter);
        if (activeBuild == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(activeBuild);
    }

    /**
     * Retrieves the total count of Jenkins jobs within a specified time boundary.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code job_counts} aggregated table</li>
     *   <li>Filters WHERE time_boundary = :timeBoundary for specific period</li>
     *   <li>Returns JobCountProjection mapped to DTO</li>
     *   <li>Includes computed timestamp from last aggregation run</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/totalJobs/{timeBoundary}}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>timeBoundary:</strong> The time period to analyze job activity within</li>
     * </ul>
     *
     * <p><strong>Supported Time Boundaries:</strong></p>
     * <ul>
     *   <li><strong>"today":</strong> Jobs that had activity today (last 24 hours)</li>
     *   <li><strong>"week":</strong> Jobs active in the last 7 days</li>
     *   <li><strong>"month":</strong> Jobs active in the last 30 days</li>
     *   <li><strong>"quarter":</strong> Jobs active in the last 90 days</li>
     *   <li><strong>"year":</strong> Jobs active in the last 365 days</li>
     *   <li><strong>"all":</strong> All jobs regardless of activity</li>
     * </ul>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <ul>
     *   <li><strong>timeBoundary:</strong> The time period that was analyzed</li>
     *   <li><strong>totalJobs:</strong> Number of jobs with activity in the specified period</li>
     *   <li><strong>computedAt:</strong> Timestamp when the count was calculated</li>
     * </ul>
     *
     * <p><strong>Job Activity Definition:</strong></p>
     * <p>A job is considered "active" within a time boundary if it has:</p>
     * <ul>
     *   <li><strong>Build Execution:</strong> Executed at least one build in the period</li>
     *   <li><strong>Configuration Changes:</strong> Been modified or reconfigured</li>
     *   <li><strong>Trigger Events:</strong> Had any interaction or trigger event</li>
     *   <li><strong>Status Updates:</strong> Any status or metadata changes</li>
     * </ul>
     *
     * <p><strong>Data Aggregation Process:</strong></p>
     * <ul>
     *   <li>Analysis of chat_messages table for job activity</li>
     *   <li>Jenkins API calls for job metadata and build history</li>
     *   <li>Time-based filtering using timestamp comparisons</li>
     *   <li>Results cached in job_counts table by time boundary</li>
     *   <li>Periodic refresh to maintain accuracy</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Infrastructure utilization analysis</li>
     *   <li>Growth tracking and capacity planning</li>
     *   <li>Identifying dormant or unused jobs</li>
     *   <li>Executive reporting on CI/CD adoption</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/totalJobs/today
     * GET /api/dashboard/totalJobs/week
     * GET /api/dashboard/totalJobs/month
     * }
     *
     * @param timeBoundary the time period to count job activity within
     * @return {@link ResponseEntity} containing {@link JobCountDTO} with
     *         job count data, or HTTP 404 Not Found if no data is available
     *
     * @see JobCountDTO
     * @see DashboardService#getJobCountByTimeBoundary(String)
     * @see com.diploma.inno.dto.JobCountProjection
     */
    @GetMapping("/totalJobs/{timeBoundary}")
    public ResponseEntity<JobCountDTO> getTotalJobs(@PathVariable String timeBoundary) {
        JobCountDTO totalJobs = dashboardService.getJobCountByTimeBoundary(timeBoundary);
        if (totalJobs == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(totalJobs);
    }

    /**
     * Retrieves comprehensive build summary information for a specific build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for AI analysis results</li>
     *   <li>Filters WHERE conversation_id = :conversationId AND build_number = :buildNumber</li>
     *   <li>Searches for message_type = 'ASSISTANT' with build summary data</li>
     *   <li>Parses JSONB content field for structured build information</li>
     *   <li>Maps to BuildSummaryDTO or returns status information</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{conversationId}/{buildNumber}}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> The Jenkins job name (used as conversation identifier)</li>
     *   <li><strong>buildNumber:</strong> The specific build number to retrieve</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * <p>Returns a Map containing:</p>
     * <ul>
     *   <li><strong>hasAiData:</strong> Boolean indicating if AI analysis is available</li>
     *   <li><strong>data:</strong> {@link BuildSummaryDTO} with build details (if AI data exists)</li>
     *   <li><strong>message:</strong> Error or status message (if AI data doesn't exist)</li>
     * </ul>
     *
     * <p><strong>Build Summary Data (when available):</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> Name of the Jenkins job</li>
     *   <li><strong>buildId:</strong> Build number</li>
     *   <li><strong>healthStatus:</strong> SUCCESS, FAILURE, UNSTABLE, or ABORTED</li>
     *   <li><strong>buildSummary:</strong> AI-generated summary of the build</li>
     *   <li><strong>buildStartedTime:</strong> When the build was initiated</li>
     *   <li><strong>buildDuration:</strong> How long the build took</li>
     *   <li><strong>regressionDetected:</strong> Whether performance regression was found</li>
     * </ul>
     *
     * <p><strong>AI Analysis Integration:</strong></p>
     * <ul>
     *   <li>AI analysis is triggered asynchronously after build completion</li>
     *   <li>Results stored as JSONB in chat_messages.content field</li>
     *   <li>Analysis includes log parsing, anomaly detection, and summary generation</li>
     *   <li>If hasAiData is false, analysis may still be in progress</li>
     * </ul>
     *
     * <p><strong>JSONB Content Structure:</strong></p>
     * <p>The AI analysis content includes:</p>
     * <ul>
     *   <li>Build metadata and timing information</li>
     *   <li>Anomaly detection results with severity levels</li>
     *   <li>Performance regression analysis</li>
     *   <li>Security vulnerability assessments</li>
     *   <li>Human-readable build summary</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/builds/my-web-app/123
     * GET /api/dashboard/builds/backend-service/456
     * }
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Detailed build analysis and reporting</li>
     *   <li>Build summary display in dashboards</li>
     *   <li>AI-powered build insights and recommendations</li>
     *   <li>Build performance and regression analysis</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to retrieve summary for
     * @return {@link Map} containing build summary data with AI analysis results,
     *         or status information if data is not available
     *
     * @see BuildSummaryDTO
     * @see DashboardService#getBuildSummary(String, Integer)
     */
    @GetMapping("/builds/{conversationId}/{buildNumber}")
    public Map<String, Object> getBuildSummary(
            @PathVariable String conversationId,
            @PathVariable Integer buildNumber) {
        return dashboardService.getBuildSummary(conversationId, buildNumber);
    }

    /**
     * Retrieves build log tracking information for a specific build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for log processing status</li>
     *   <li>Counts USER messages WHERE conversation_id = :jobName AND build_number = :buildId</li>
     *   <li>Filters for messages without 'instructions' field in JSONB content</li>
     *   <li>Calculates received vs expected log count (expected = 14)</li>
     *   <li>Returns processing status and completion percentage</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{jobName}/{buildId}/logs-tracker}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> The name of the Jenkins job</li>
     *   <li><strong>buildId:</strong> The specific build number to track logs for</li>
     * </ul>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <p>Returns a Map containing log tracking information:</p>
     * <ul>
     *   <li><strong>received:</strong> Number of log entries processed (max 14)</li>
     *   <li><strong>expected:</strong> Expected number of log entries (always 14)</li>
     *   <li><strong>status:</strong> "Complete" if received >= 14, "In Progress" otherwise</li>
     * </ul>
     *
     * <p><strong>Log Processing Pipeline:</strong></p>
     * <ul>
     *   <li><strong>Log Ingestion:</strong> Build logs are fetched from Jenkins API</li>
     *   <li><strong>Chunking:</strong> Logs are split into manageable chunks</li>
     *   <li><strong>Storage:</strong> Each chunk stored as USER message in chat_messages</li>
     *   <li><strong>AI Processing:</strong> Logs are analyzed for anomalies and insights</li>
     *   <li><strong>Status Tracking:</strong> Progress tracked via message count</li>
     * </ul>
     *
     * <p><strong>Expected Log Count Logic:</strong></p>
     * <ul>
     *   <li>System expects 14 log chunks per build for complete analysis</li>
     *   <li>Each chunk represents a portion of the build log</li>
     *   <li>Fewer chunks may indicate incomplete log processing</li>
     *   <li>Status "Complete" when all expected chunks are received</li>
     * </ul>
     *
     * <p><strong>JSONB Content Filtering:</strong></p>
     * <ul>
     *   <li>Excludes messages with 'instructions' field (system messages)</li>
     *   <li>Counts only actual log content messages</li>
     *   <li>Ensures accurate tracking of log processing progress</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Monitoring log analysis progress</li>
     *   <li>Debugging log processing issues</li>
     *   <li>Performance analysis of log processing workflows</li>
     *   <li>Build log health monitoring</li>
     * </ul>
     *
     * <p><strong>Example Response:</strong></p>
     * {@snippet lang=json :
     * {
     *   "received": 14,
     *   "expected": 14,
     *   "status": "Complete"
     * }
     * }
     *
     * @param jobName the name of the Jenkins job
     * @param buildId the build number to track logs for
     * @return {@link Map} containing log tracking information and metadata
     *
     * @see DashboardService#getLogsTracker(String, int)
     */
    @GetMapping("/builds/{jobName}/{buildId}/logs-tracker")
    public Map<String, Object> getLogsTracker(
            @PathVariable String jobName,
            @PathVariable int buildId) {
        return dashboardService.getLogsTracker(jobName, buildId);
    }

    /**
     * Retrieves AI-calculated risk score information for a specific build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for AI risk analysis</li>
     *   <li>Filters WHERE message_type = 'ASSISTANT' AND conversation_id = :conversationId AND build_number = :buildNumber</li>
     *   <li>Extracts JSONB content->'riskScore' object using jsonb_build_object</li>
     *   <li>Parses risk score, change, level, and previous score</li>
     *   <li>Maps to RiskScoreDTO or returns error information</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{conversationId}/{buildNumber}/risk-score}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> The Jenkins job name (conversation identifier)</li>
     *   <li><strong>buildNumber:</strong> The specific build number to get risk score for</li>
     * </ul>
     *
     * <p><strong>Response Data Structure:</strong></p>
     * <p>Returns a Map containing risk assessment information:</p>
     * <ul>
     *   <li><strong>hasData:</strong> Boolean indicating if risk score data is available</li>
     *   <li><strong>data:</strong> {@link RiskScoreDTO} with risk metrics (if available)</li>
     *   <li><strong>message:</strong> Error or status message (if data unavailable)</li>
     * </ul>
     *
     * <p><strong>Risk Score Data (when available):</strong></p>
     * <ul>
     *   <li><strong>score:</strong> Numerical risk rating (typically 0-100)</li>
     *   <li><strong>change:</strong> Risk score change from previous build</li>
     *   <li><strong>riskLevel:</strong> Categorical risk level (LOW, MEDIUM, HIGH, CRITICAL)</li>
     *   <li><strong>previousScore:</strong> Risk score of the previous build</li>
     * </ul>
     *
     * <p><strong>Risk Assessment Factors:</strong></p>
     * <ul>
     *   <li><strong>Security Vulnerabilities:</strong> Known CVEs and security anomalies</li>
     *   <li><strong>Build Failure Patterns:</strong> Historical failure rates and patterns</li>
     *   <li><strong>Code Quality Metrics:</strong> Technical debt and quality violations</li>
     *   <li><strong>Test Coverage:</strong> Test failure rates and coverage metrics</li>
     *   <li><strong>Dependency Issues:</strong> Outdated or vulnerable dependencies</li>
     *   <li><strong>Performance Regressions:</strong> Build time and performance issues</li>
     * </ul>
     *
     * <p><strong>Risk Level Classification:</strong></p>
     * <ul>
     *   <li><strong>LOW (0-25):</strong> Minimal risk, good build health</li>
     *   <li><strong>MEDIUM (26-50):</strong> Moderate risk, some issues to address</li>
     *   <li><strong>HIGH (51-75):</strong> Significant risk, requires attention</li>
     *   <li><strong>CRITICAL (76-100):</strong> Severe risk, immediate action needed</li>
     * </ul>
     *
     * <p><strong>JSONB Query Structure:</strong></p>
     * {@snippet lang=GenericSQL :
     * SELECT jsonb_build_object(
     *   'score', (content->'riskScore'->>'score')::integer,
     *   'change', (content->'riskScore'->>'change')::integer,
     *   'riskLevel', content->'riskScore'->>'riskLevel',
     *   'previousScore', (content->'riskScore'->>'previousScore')::integer
     * ) FROM chat_messages WHERE ...
     * }
     *
     * <p><strong>Error Handling:</strong></p>
     * <ul>
     *   <li><strong>JsonProcessingException:</strong> Error parsing risk score data</li>
     *   <li><strong>Missing Data:</strong> No AI analysis available for the build</li>
     *   <li><strong>Invalid Format:</strong> Malformed JSONB content structure</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Build quality assessment and gating</li>
     *   <li>Security risk monitoring</li>
     *   <li>Trend analysis for build health</li>
     *   <li>Automated decision making for deployments</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to get risk score for
     * @return {@link Map} containing risk score data and assessment details
     * @throws JsonProcessingException if there's an error processing the risk score data
     *
     * @see DashboardService#getRiskScore(String, Integer)
     * @see RiskScoreDTO
     */
    @GetMapping("/builds/{conversationId}/{buildNumber}/risk-score")
    public Map<String, Object> getRiskScore(
            @PathVariable String conversationId,
            @PathVariable Integer buildNumber) throws JsonProcessingException {
        return dashboardService.getRiskScore(conversationId, buildNumber);
    }

    /**
     * Retrieves paginated build logs for a specific Jenkins build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for build log content</li>
     *   <li>Filters WHERE message_type = 'USER' AND conversation_id = :jobName AND build_number = :buildId</li>
     *   <li>Excludes messages with 'instructions' field using NOT jsonb_exists(content, 'instructions')</li>
     *   <li>Orders by timestamp for chronological log sequence</li>
     *   <li>Applies LIMIT :size OFFSET :offset for pagination</li>
     *   <li>Parses JSONB content field to Object using ObjectMapper</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{jobName}/{buildId}/logs}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> The name of the Jenkins job</li>
     *   <li><strong>buildId:</strong> The specific build number to retrieve logs for</li>
     * </ul>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>page:</strong> Page number for pagination (default: 0, 0-based)</li>
     *   <li><strong>size:</strong> Number of log entries per page (default: 100)</li>
     * </ul>
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns a List of Objects containing log entries with:</p>
     * <ul>
     *   <li><strong>Log Content:</strong> Parsed JSONB content from chat_messages</li>
     *   <li><strong>Timestamp Information:</strong> When the log entry was created</li>
     *   <li><strong>Structured Data:</strong> JSON objects with log metadata</li>
     *   <li><strong>Build Context:</strong> Information about the build step or phase</li>
     * </ul>
     *
     * <p><strong>Pagination Implementation:</strong></p>
     * <ul>
     *   <li><strong>Offset Calculation:</strong> offset = page * size</li>
     *   <li><strong>Page Size Limits:</strong> Recommended max size: 1000 for performance</li>
     *   <li><strong>Zero-based Indexing:</strong> First page is page=0</li>
     *   <li><strong>Chronological Order:</strong> Logs ordered by timestamp ASC</li>
     * </ul>
     *
     * <p><strong>JSONB Content Processing:</strong></p>
     * <ul>
     *   <li>Each log entry stored as JSONB in chat_messages.content field</li>
     *   <li>ObjectMapper.readValue() converts JSONB to Java Object</li>
     *   <li>Malformed JSON entries are filtered out (null values removed)</li>
     *   <li>Error logging for failed JSON parsing attempts</li>
     * </ul>
     *
     * <p><strong>Log Entry Structure:</strong></p>
     * <p>Typical log entry contains:</p>
     * <ul>
     *   <li>Build step information and phase</li>
     *   <li>Console output and command results</li>
     *   <li>Timestamps and duration data</li>
     *   <li>Error messages and stack traces</li>
     *   <li>Plugin output and tool results</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li>Large log files are automatically chunked during ingestion</li>
     *   <li>Pagination prevents memory issues with large builds</li>
     *   <li>JSONB indexing optimizes query performance</li>
     *   <li>Failed JSON parsing is logged but doesn't break the response</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Build log viewing and analysis</li>
     *   <li>Debugging build failures and issues</li>
     *   <li>Log search and filtering operations</li>
     *   <li>Automated log processing and analysis</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/builds/my-web-app/123/logs?page=0&size=50
     * GET /api/dashboard/builds/backend-service/456/logs?page=2&size=200
     * }
     *
     * @param jobName the name of the Jenkins job
     * @param buildId the build number to retrieve logs for
     * @param page the page number for pagination (0-based)
     * @param size the number of log entries per page
     * @return {@link List} of Objects containing paginated log entries
     *
     * @see DashboardService#getAllLogs(String, int, int, int)
     */
    @GetMapping(value = "/builds/{jobName}/{buildId}/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Object> getAllLogs(
            @PathVariable String jobName,
            @PathVariable int buildId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return dashboardService.getAllLogs(jobName, buildId, page, size);
    }

    /**
     * Triggers a new build for the specified Jenkins job.
     *
     * <p><strong>Jenkins API Integration Process:</strong></p>
     * <ol>
     *   <li>Calls JenkinsService.rerunBuild() with job name and build ID</li>
     *   <li>Uses jenkins-rest library to make API call to Jenkins server</li>
     *   <li>Executes jenkinsClient.api().jobsApi().build("", jobName)</li>
     *   <li>Queues new build with current job configuration</li>
     *   <li>Returns confirmation message with job name</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code POST /api/dashboard/builds/{jobName}/{buildId}/rerun}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> The name of the Jenkins job to trigger</li>
     *   <li><strong>buildId:</strong> The build number (used for reference, new build will get next available number)</li>
     * </ul>
     *
     * <p><strong>Build Trigger Process:</strong></p>
     * <ol>
     *   <li><strong>Validation:</strong> Checks if job exists and is accessible</li>
     *   <li><strong>Permission Check:</strong> Verifies Jenkins user has build permissions</li>
     *   <li><strong>Queue Submission:</strong> Submits build request to Jenkins queue</li>
     *   <li><strong>Response Generation:</strong> Returns confirmation message</li>
     * </ol>
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns a Map containing:</p>
     * <ul>
     *   <li><strong>message:</strong> Confirmation message indicating build was triggered</li>
     * </ul>
     *
     * <p><strong>Build Behavior:</strong></p>
     * <ul>
     *   <li><strong>Configuration:</strong> New build uses the current job configuration</li>
     *   <li><strong>Parameters:</strong> Build parameters are inherited from job defaults</li>
     *   <li><strong>Queuing:</strong> Build is queued and will start when resources are available</li>
     *   <li><strong>Independence:</strong> Original build data and logs remain unchanged</li>
     *   <li><strong>Build Number:</strong> New build gets the next sequential build number</li>
     * </ul>
     *
     * <p><strong>Jenkins API Call Details:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> POST /job/{jobName}/build</li>
     *   <li><strong>Authentication:</strong> Uses configured Jenkins credentials</li>
     *   <li><strong>Folder Path:</strong> Empty string "" for root-level jobs</li>
     *   <li><strong>Error Handling:</strong> Exceptions logged but don't prevent response</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Retrying failed builds after fixing issues</li>
     *   <li>Manual build triggering for testing</li>
     *   <li>Re-running builds with updated dependencies</li>
     *   <li>Emergency builds for hotfixes</li>
     * </ul>
     *
     * <p><strong>Security Considerations:</strong></p>
     * <ul>
     *   <li>⚠️ <strong>WARNING:</strong> No authentication/authorization currently implemented</li>
     *   <li>🔒 <strong>RECOMMENDATION:</strong> Add proper access controls for build triggering</li>
     *   <li>📝 <strong>AUDIT:</strong> Consider logging build trigger events for audit trails</li>
     *   <li>🛡️ <strong>PERMISSIONS:</strong> Ensure Jenkins user has minimal required permissions</li>
     * </ul>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Job Not Found:</strong> Jenkins returns 404 for non-existent jobs</li>
     *   <li><strong>Permission Denied:</strong> Jenkins returns 403 for insufficient permissions</li>
     *   <li><strong>Server Unavailable:</strong> Network or Jenkins server issues</li>
     *   <li><strong>Queue Full:</strong> Jenkins build queue at capacity</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>
     * POST /api/dashboard/builds/my-web-app/123/rerun
     * POST /api/dashboard/builds/backend-service/456/rerun
     * </pre>
     *
     * @param jobName the name of the Jenkins job to trigger a new build for
     * @param buildId the build number (for reference, new build gets next available number)
     * @return {@link Map} containing confirmation message about the triggered build
     *
     * @see JenkinsService#rerunBuild(String, int)
     * @see com.cdancy.jenkins.rest.JenkinsClient
     */
    @PostMapping("/builds/{jobName}/{buildId}/rerun")
    public Map<String, String> rerunBuild(
            @PathVariable String jobName,
            @PathVariable int buildId) {
        jenkinsService.rerunBuild(jobName, buildId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "New build triggered for " + jobName);
        return response;
    }

    /**
     * Retrieves a list of all Jenkins job names available in the system.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for distinct job names</li>
     *   <li>Executes SELECT DISTINCT job_name WHERE job_name IS NOT NULL</li>
     *   <li>Orders results alphabetically for consistent presentation</li>
     *   <li>Returns List&lt;String&gt; of unique job names</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/jobs}</p>
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns a List of Strings containing:</p>
     * <ul>
     *   <li><strong>Active Job Names:</strong> Jobs that have had recent activity</li>
     *   <li><strong>Historical Jobs:</strong> Jobs with stored analysis data</li>
     *   <li><strong>Alphabetical Order:</strong> Sorted for consistent UI presentation</li>
     *   <li><strong>Non-null Values:</strong> Filters out any null or empty job names</li>
     * </ul>
     *
     * <p><strong>Data Source Analysis:</strong></p>
     * <ul>
     *   <li><strong>Primary Source:</strong> chat_messages table job_name column</li>
     *   <li><strong>Data Freshness:</strong> Reflects jobs with stored AI analysis results</li>
     *   <li><strong>Synchronization:</strong> Updated via 15-minute scheduled Jenkins sync</li>
     *   <li><strong>Cleanup:</strong> Orphaned jobs removed during sync process</li>
     * </ul>
     *
     * <p><strong>Job Name Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Preserves original Jenkins job name casing</li>
     *   <li><strong>Special Characters:</strong> Includes spaces, hyphens, underscores</li>
     *   <li><strong>Folder Structure:</strong> May include folder paths if applicable</li>
     *   <li><strong>Uniqueness:</strong> DISTINCT ensures no duplicate entries</li>
     * </ul>
     *
     * <p><strong>Synchronization Process:</strong></p>
     * <ul>
     *   <li>Scheduled task runs every 15 minutes (@Scheduled annotation)</li>
     *   <li>Fetches current jobs from Jenkins API via JenkinsService</li>
     *   <li>Compares with database job names for consistency</li>
     *   <li>Removes orphaned jobs (safety check prevents mass deletion)</li>
     *   <li>Maintains data integrity between Jenkins and database</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Populating job selection dropdowns in UI</li>
     *   <li>System administration and job management</li>
     *   <li>Automated job discovery and monitoring setup</li>
     *   <li>Integration with external tools and dashboards</li>
     * </ul>
     *
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li>Fast response due to simple DISTINCT query</li>
     *   <li>Minimal data transfer (only job names)</li>
     *   <li>Cached at database level for repeated queries</li>
     *   <li>Typical response includes 10-100 job names</li>
     * </ul>
     *
     * @return {@link ResponseEntity} containing a list of all Jenkins job names
     *         with HTTP 200 OK status
     *
     * @see DashboardService#getAllJobNames()
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<String>> getJobNames() {
        List<String> jobNames = dashboardService.getAllJobNames();
        return ResponseEntity.ok(jobNames);
    }

    /**
     * Retrieves paginated anomalies detected in a specific build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for AI analysis results</li>
     *   <li>Uses complex CTE with CROSS JOIN LATERAL to unnest JSONB anomalies array</li>
     *   <li>Filters WHERE message_type = 'ASSISTANT' AND conversation_id = :jobName AND build_number = :buildId</li>
     *   <li>Applies LIMIT :pageSize OFFSET :offset for pagination</li>
     *   <li>Counts total anomalies for pagination metadata</li>
     *   <li>Returns structured response with anomalies and pagination info</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{jobName}/{buildId}/detected-anomalies}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> The name of the Jenkins job</li>
     *   <li><strong>buildId:</strong> The specific build number to retrieve anomalies for</li>
     * </ul>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>page:</strong> Page number for pagination (default: 1, 1-based)</li>
     *   <li><strong>size:</strong> Number of anomalies per page (default: 3)</li>
     * </ul>
     *
     * <p><strong>Complex SQL Query Structure:</strong></p>
     * {@snippet lang=GenericSQL :
     * WITH unnested_anomalies AS (
     *   SELECT anomaly, position
     *   FROM chat_messages cm
     *   CROSS JOIN LATERAL jsonb_array_elements(
     *     COALESCE(cm.content->'anomalies', '[]'::jsonb)
     *   ) WITH ORDINALITY AS t(anomaly, position)
     *   WHERE cm.message_type = 'ASSISTANT'
     *     AND cm.conversation_id = :conversationId
     *     AND cm.build_number = :buildNumber
     *   ORDER BY position
     *   LIMIT :pageSize OFFSET :offset
     * ),
     * total_count AS (
     *   SELECT COUNT(*) AS total FROM ..
     * )
     * SELECT COALESCE(jsonb_agg(anomaly ORDER BY position), '[]'::jsonb) AS anomalies,
     *        (SELECT total FROM total_count) AS total_count
     * FROM unnested_anomalies
     * }
     *
     * <p><strong>Response Structure:</strong></p>
     * <p>Returns a Map containing:</p>
     * <ul>
     *   <li><strong>hasData:</strong> Boolean indicating if anomalies are available</li>
     *   <li><strong>anomalies:</strong> List of {@link AnomalyDTO} objects for the current page</li>
     *   <li><strong>totalCount:</strong> Total number of anomalies found</li>
     *   <li><strong>pageNumber:</strong> Current page number (1-based)</li>
     *   <li><strong>pageSize:</strong> Number of items per page</li>
     *   <li><strong>totalPages:</strong> Total number of pages available</li>
     *   <li><strong>message:</strong> Error message if no data available</li>
     * </ul>
     *
     * <p><strong>Anomaly Information Structure:</strong></p>
     * <p>Each anomaly includes:</p>
     * <ul>
     *   <li><strong>type:</strong> Category of the anomaly (security, performance, quality, etc.)</li>
     *   <li><strong>severity:</strong> CRITICAL, HIGH, MEDIUM, WARNING, or LOW</li>
     *   <li><strong>description:</strong> Human-readable description of the issue</li>
     *   <li><strong>recommendation:</strong> Suggested actions to resolve the issue</li>
     *   <li><strong>aiAnalysis:</strong> Detailed AI-generated analysis</li>
     *   <li><strong>details:</strong> Additional technical details and context</li>
     * </ul>
     *
     * <p><strong>JSONB Array Processing:</strong></p>
     * <ul>
     *   <li>CROSS JOIN LATERAL unnests JSONB array into individual rows</li>
     *   <li>WITH ORDINALITY preserves original array order</li>
     *   <li>COALESCE handles cases where anomalies array doesn't exist</li>
     *   <li>jsonb_agg reconstructs paginated array for response</li>
     * </ul>
     *
     * <p><strong>Pagination Implementation:</strong></p>
     * <ul>
     *   <li><strong>1-based Page Numbers:</strong> Frontend-friendly pagination</li>
     *   <li><strong>Offset Calculation:</strong> (page - 1) * size</li>
     *   <li><strong>Total Count:</strong> Separate CTE for accurate pagination metadata</li>
     *   <li><strong>Small Page Size:</strong> Default 3 items for detailed anomaly display</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Detailed build analysis and issue investigation</li>
     *   <li>Security vulnerability assessment</li>
     *   <li>Quality assurance and code review processes</li>
     *   <li>Automated issue tracking and reporting</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/builds/my-web-app/123/detected-anomalies?page=1&size=5
     * GET /api/dashboard/builds/backend-service/456/detected-anomalies?page=2&size=10
     * }
     *
     * @param jobName the name of the Jenkins job
     * @param buildId the build number to retrieve anomalies for
     * @param page the page number for pagination (1-based)
     * @param size the number of anomalies per page
     * @return {@link Map} containing paginated anomaly data and metadata
     *
     * @see AnomalyDTO
     * @see PaginatedAnomaliesDTO
     * @see DashboardService#getPaginatedAnomalies(String, Integer, Integer, Integer)
     */
    @GetMapping("/builds/{jobName}/{buildId}/detected-anomalies")
    public Map<String, Object> getDetectedAnomalies(
            @PathVariable String jobName,
            @PathVariable int buildId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size) {
        return dashboardService.getPaginatedAnomalies(jobName,buildId,page,size);
    }


    /**
     * Retrieves anomaly trend data for chart visualization.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code build_anomaly_summary} table for trend analysis</li>
     *   <li>Uses complex CTE with ROW_NUMBER() window function for latest builds</li>
     *   <li>Filters by job (WHERE :jobFilter = 'all' OR conversation_id = :jobFilter)</li>
     *   <li>Partitions by conversation_id and orders by timestamp DESC, build_number DESC</li>
     *   <li>Limits to specified buildCount per job for trend analysis</li>
     *   <li>Returns List&lt;Map&gt; with job, build, timestamp, and anomaly_count</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/anomaly-trend}</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> Job name or "all" for all jobs (default: "all")</li>
     *   <li><strong>buildCount:</strong> Number of recent builds to analyze (default: 5, max: 15)</li>
     * </ul>
     *
     * <p><strong>Complex SQL Query Structure:</strong></p>
     * {@snippet lang=GenericSQL :
     * WITH distinct_jobs AS (
     *   SELECT COUNT(DISTINCT conversation_id) AS total_jobs
     *   FROM build_anomaly_summary
     *   WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter)
     * ),
     * ranked_builds AS (
     *   SELECT conversation_id AS job, build_number AS build,
     *          timestamp, total_anomalies AS anomaly_count,
     *          ROW_NUMBER() OVER (PARTITION BY conversation_id
     *                            ORDER BY timestamp DESC, build_number DESC) AS rn
     *   FROM build_anomaly_summary
     *   WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter)
     * ),
     * limited_builds AS (
     *   SELECT job, build, timestamp, anomaly_count
     *   FROM ranked_builds WHERE rn <= :buildCount
     * )
     * SELECT * FROM limited_builds ORDER BY timestamp
     * }
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns {@link ChartDataDTO} containing:</p>
     * <ul>
     *   <li><strong>labels:</strong> Build identifiers for X-axis (e.g., "my-web-app - Build № 123")</li>
     *   <li><strong>datasets:</strong> Array with single dataset for anomaly count trends</li>
     * </ul>
     *
     * <p><strong>Chart Data Structure:</strong></p>
     * <ul>
     *   <li><strong>X-axis (labels):</strong> Job name + build number combinations</li>
     *   <li><strong>Y-axis (data):</strong> Anomaly counts for each build</li>
     *   <li><strong>Series:</strong> Single dataset "Anomaly Count" with blue color (#36A2EB)</li>
     *   <li><strong>Ordering:</strong> Chronological by timestamp for trend visualization</li>
     * </ul>
     *
     * <p><strong>Build Count Validation:</strong></p>
     * <ul>
     *   <li>Null or &lt; 1 defaults to 5 builds</li>
     *   <li>Maximum capped at 15 builds for performance</li>
     *   <li>Applied per job when jobFilter = "all"</li>
     *   <li>Ensures manageable chart data size</li>
     * </ul>
     *
     * <p><strong>Label Generation:</strong></p>
     * <ul>
     *   <li>Format: "{job} - Build № {build}"</li>
     *   <li>Preserves job name and build number for identification</li>
     *   <li>Chronological ordering for trend analysis</li>
     *   <li>Consistent formatting across all chart endpoints</li>
     * </ul>
     *
     * <p><strong>Trend Analysis Capabilities:</strong></p>
     * <ul>
     *   <li>Shows anomaly count changes over time</li>
     *   <li>Helps identify patterns and improvement trends</li>
     *   <li>Useful for tracking security posture over time</li>
     *   <li>Enables comparison between different time periods</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Dashboard trend charts and visualizations</li>
     *   <li>Historical analysis of build quality</li>
     *   <li>Performance tracking and improvement monitoring</li>
     *   <li>Executive reporting on security trends</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=TEXT :
     * GET /api/dashboard/anomaly-trend?jobFilter=my-web-app&buildCount=10
     * GET /api/dashboard/anomaly-trend?jobFilter=all&buildCount=7
     * }
     *
     * @param jobFilter the job name to filter by, or "all" for all jobs
     * @param buildCount the number of recent builds to include in the trend analysis
     * @return {@link ChartDataDTO} containing formatted data for anomaly trend visualization
     *
     * @see ChartDataDTO
     * @see DatasetDTO
     * @see DashboardService#getAnomalyTrend(String, Integer)
     */
    @GetMapping("/anomaly-trend")
    public ChartDataDTO getAnomalyTrend(
            @RequestParam(defaultValue = "all") String jobFilter,
            @RequestParam(defaultValue = "5") Integer buildCount) {
        return dashboardService.getAnomalyTrend(jobFilter, buildCount);
    }

    /**
     * Retrieves anomaly severity distribution data for chart visualization.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code build_anomaly_summary} table for severity analysis</li>
     *   <li>Uses same CTE structure as anomaly trend for build selection</li>
     *   <li>Extracts severity_counts JSONB field containing severity distribution</li>
     *   <li>Processes severity data for each build in the result set</li>
     *   <li>Aggregates severity counts across selected builds</li>
     *   <li>Returns structured data for stacked chart visualization</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/severity-distribution}</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> Job name or "all" for all jobs (default: "all")</li>
     *   <li><strong>buildCount:</strong> Number of recent builds to analyze (default: 5, max: 15)</li>
     * </ul>
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns {@link ChartDataDTO} containing:</p>
     * <ul>
     *   <li><strong>labels:</strong> Build identifiers for X-axis</li>
     *   <li><strong>datasets:</strong> Multiple data series, one for each severity level</li>
     * </ul>
     *
     * <p><strong>Severity Levels &amp; Colors:</strong></p>
     * <p>Data is broken down by severity categories with consistent colors:</p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Severe security or system issues (red #FF6384)</li>
     *   <li><strong>HIGH:</strong> Important issues requiring attention (orange #FF9F40)</li>
     *   <li><strong>MEDIUM:</strong> Moderate issues to be addressed (yellow #FFCD56)</li>
     *   <li><strong>WARNING:</strong> Minor issues or potential problems (blue #36A2EB)</li>
     *   <li><strong>LOW:</strong> Informational or low-priority issues (purple #9966FF)</li>
     * </ul>
     *
     * <p><strong>JSONB Severity Processing:</strong></p>
     * <ul>
     *   <li>severity_counts field contains JSONB object with severity breakdown</li>
     *   <li>Each build's severity data is parsed and aggregated</li>
     *   <li>Missing severity levels default to 0 count</li>
     *   <li>Data structured for stacked bar chart visualization</li>
     * </ul>
     *
     * <p><strong>Chart Visualization:</strong></p>
     * <ul>
     *   <li><strong>Stacked Bar Chart:</strong> Shows severity breakdown per build</li>
     *   <li><strong>Color-coded Series:</strong> Easy severity identification</li>
     *   <li><strong>Consistent Colors:</strong> Same color scheme across dashboard</li>
     *   <li><strong>Multiple Views:</strong> Supports both absolute counts and percentage views</li>
     * </ul>
     *
     * <p><strong>Analysis Insights:</strong></p>
     * <ul>
     *   <li>Identify builds with high-severity issues</li>
     *   <li>Track improvement in security posture over time</li>
     *   <li>Compare severity patterns across different jobs</li>
     *   <li>Prioritize remediation efforts based on severity trends</li>
     * </ul>
     *
     * <p><strong>Data Processing Logic:</strong></p>
     * <ul>
     *   <li>Iterates through build data to extract severity information</li>
     *   <li>Maintains separate data arrays for each severity level</li>
     *   <li>Ensures consistent ordering across all severity datasets</li>
     *   <li>Handles missing or null severity data gracefully</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Security dashboard severity breakdown charts</li>
     *   <li>Quality assurance trend analysis</li>
     *   <li>Risk assessment and prioritization</li>
     *   <li>Team performance and improvement tracking</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/severity-distribution?jobFilter=backend-service&buildCount=8
     * GET /api/dashboard/severity-distribution?jobFilter=all&buildCount=12
     * }
     *
     * @param jobFilter the job name to filter by, or "all" for all jobs
     * @param buildCount the number of recent builds to include in the distribution analysis
     * @return {@link ChartDataDTO} containing formatted data for severity distribution visualization
     *
     * @see ChartDataDTO
     * @see DatasetDTO
     * @see DashboardService#getSeverityDistribution(String, Integer)
     */
    @GetMapping("/severity-distribution")
    public ChartDataDTO getSeverityDistribution(
            @RequestParam(defaultValue = "all") String jobFilter,
            @RequestParam(defaultValue = "5") Integer buildCount) {
        return dashboardService.getSeverityDistribution(jobFilter, buildCount);
    }

    /**
     * Retrieves AI-generated insights for a specific build.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for AI analysis results</li>
     *   <li>Filters WHERE message_type = 'ASSISTANT' AND conversation_id = :conversationId AND build_number = :buildNumber</li>
     *   <li>Extracts JSONB content field containing comprehensive AI analysis</li>
     *   <li>Parses insights, recommendations, and analysis data</li>
     *   <li>Returns structured response with AI insights or error information</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{conversationId}/{buildNumber}/ai-insights}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> The Jenkins job name (conversation identifier)</li>
     *   <li><strong>buildNumber:</strong> The specific build number to get insights for</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * <p>Returns a Map containing AI insights such as:</p>
     * <ul>
     *   <li><strong>hasData:</strong> Boolean indicating if AI insights are available</li>
     *   <li><strong>data:</strong> Comprehensive AI analysis results (if available)</li>
     *   <li><strong>message:</strong> Error or status message (if data unavailable)</li>
     * </ul>
     *
     * <p><strong>AI Insights Data Structure (when available):</strong></p>
     * <ul>
     *   <li><strong>Build Analysis:</strong> Overall build health and performance assessment</li>
     *   <li><strong>Quality Metrics:</strong> Code quality, test coverage, and technical debt analysis</li>
     *   <li><strong>Security Assessment:</strong> Vulnerability analysis and security recommendations</li>
     *   <li><strong>Performance Insights:</strong> Build time analysis and optimization suggestions</li>
     *   <li><strong>Recommendations:</strong> Actionable steps for improvement</li>
     *   <li><strong>Trend Analysis:</strong> Comparison with previous builds and historical data</li>
     * </ul>
     *
     * <p><strong>AI Analysis Components:</strong></p>
     * <ul>
     *   <li><strong>Log Pattern Analysis:</strong> Automated log parsing and anomaly detection</li>
     *   <li><strong>Test Result Evaluation:</strong> Test failure analysis and coverage assessment</li>
     *   <li><strong>Dependency Scanning:</strong> Security vulnerability scanning results</li>
     *   <li><strong>Code Quality Metrics:</strong> Static analysis and quality violations</li>
     *   <li><strong>Performance Regression:</strong> Build time and performance trend detection</li>
     *   <li><strong>Best Practice Compliance:</strong> Industry standard compliance assessment</li>
     * </ul>
     *
     * <p><strong>JSONB Content Processing:</strong></p>
     * <ul>
     *   <li>AI analysis stored as structured JSONB in content field</li>
     *   <li>Complex nested objects with insights, metrics, and recommendations</li>
     *   <li>Graceful handling of missing or malformed AI data</li>
     *   <li>Error logging for failed content parsing attempts</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Detailed build analysis and troubleshooting</li>
     *   <li>Quality assurance and code review processes</li>
     *   <li>Performance optimization and improvement planning</li>
     *   <li>Security assessment and vulnerability management</li>
     * </ul>
     *
     * <p><strong>Data Availability:</strong></p>
     * <ul>
     *   <li>AI insights are generated asynchronously after build completion</li>
     *   <li>Analysis may take several minutes for complex builds</li>
     *   <li>If hasData is false, analysis may still be in progress</li>
     *   <li>Retry mechanism recommended for pending analyses</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to get AI insights for
     * @return {@link Map} containing comprehensive AI-generated insights and analysis
     *
     * @see DashboardService#getAiInsights(String, Integer)
     */
    @GetMapping("/builds/{conversationId}/{buildNumber}/ai-insights")
    public Map<String, Object> getAiInsights(
            @PathVariable String conversationId,
            @PathVariable Integer buildNumber) {
        return dashboardService.getAiInsights(conversationId, buildNumber);
    }

    /**
     * Retrieves the latest AI insights for all builds in a conversation (job).
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for latest AI analysis</li>
     *   <li>Filters WHERE message_type = 'ASSISTANT' AND conversation_id = :conversationId</li>
     *   <li>Orders by build_number DESC to get most recent analysis</li>
     *   <li>Aggregates insights from recent builds for job-level overview</li>
     *   <li>Returns comprehensive job health assessment</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/builds/{conversationId}/ai-insights}</p>
     *
     * <p><strong>Path Parameters:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> The Jenkins job name (conversation identifier)</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * <p>Returns a Map containing aggregated AI insights such as:</p>
     * <ul>
     *   <li><strong>hasData:</strong> Boolean indicating if AI insights are available</li>
     *   <li><strong>data:</strong> Aggregated AI analysis results (if available)</li>
     *   <li><strong>message:</strong> Error or status message (if data unavailable)</li>
     * </ul>
     *
     * <p><strong>Aggregated Insights Data (when available):</strong></p>
     * <ul>
     *   <li><strong>Overall Job Health:</strong> Summary of job performance and stability</li>
     *   <li><strong>Recent Trends:</strong> Analysis of recent build patterns and changes</li>
     *   <li><strong>Quality Trends:</strong> Code quality evolution over recent builds</li>
     *   <li><strong>Security Posture:</strong> Overall security assessment and trends</li>
     *   <li><strong>Performance Analysis:</strong> Build time trends and optimization opportunities</li>
     *   <li><strong>Recommendations:</strong> Job-level improvement suggestions</li>
     * </ul>
     *
     * <p><strong>Aggregation Logic:</strong></p>
     * <ul>
     *   <li><strong>Recent Build Analysis:</strong> Combines insights from the most recent builds</li>
     *   <li><strong>Pattern Recognition:</strong> Identifies trends and patterns across multiple builds</li>
     *   <li><strong>Historical Context:</strong> Provides job-level recommendations based on historical data</li>
     *   <li><strong>Change Detection:</strong> Highlights significant changes or regressions</li>
     * </ul>
     *
     * <p><strong>Data Processing:</strong></p>
     * <ul>
     *   <li>Analyzes multiple recent builds for comprehensive job assessment</li>
     *   <li>Weights recent builds more heavily in trend analysis</li>
     *   <li>Aggregates metrics and recommendations across builds</li>
     *   <li>Provides job-level health score and status</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Job-level health monitoring and assessment</li>
     *   <li>Long-term trend analysis and planning</li>
     *   <li>Team performance evaluation and improvement</li>
     *   <li>Strategic decision making for project development</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/builds/my-web-app/ai-insights
     * GET /api/dashboard/builds/backend-service/ai-insights
     * }
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @return {@link Map} containing aggregated AI insights for the job
     *
     * @see DashboardService#getAiInsightsByConversation(String)
     */
    @GetMapping("/builds/{conversationId}/ai-insights")
    public Map<String, Object> getAiInsightsByConversation(
            @PathVariable String conversationId) {
        return dashboardService.getAiInsightsByConversation(conversationId);
    }

    /**
     * Retrieves Jenkins jobs filtered by status for the job explorer interface.
     *
     * <p><strong>Database Query Process:</strong></p>
     * <ol>
     *   <li>Queries {@code chat_messages} table for job status information</li>
     *   <li>Uses complex CTE to get latest build status per job</li>
     *   <li>Filters by status using CASE WHEN logic for status classification</li>
     *   <li>Joins with Jenkins API data for real-time job information</li>
     *   <li>Returns comprehensive job list with metadata</li>
     * </ol>
     *
     * <p><strong>Endpoint:</strong> {@code GET /api/dashboard/job-explorer}</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><strong>tab:</strong> Status filter for jobs (optional, default: "all")</li>
     * </ul>
     *
     * <p><strong>Supported Status Filters:</strong></p>
     * <ul>
     *   <li><strong>"all":</strong> All jobs regardless of status</li>
     *   <li><strong>"success":</strong> Jobs with successful recent builds</li>
     *   <li><strong>"failure":</strong> Jobs with failed recent builds</li>
     *   <li><strong>"unstable":</strong> Jobs with unstable recent builds</li>
     *   <li><strong>"aborted":</strong> Jobs with aborted recent builds</li>
     *   <li><strong>"disabled":</strong> Disabled or inactive jobs</li>
     * </ul>
     *
     * <p><strong>Status Classification Logic:</strong></p>
     * <ul>
     *   <li><strong>Success:</strong> Latest build completed without significant anomalies</li>
     *   <li><strong>Failure:</strong> Latest build failed or has critical anomalies</li>
     *   <li><strong>Unstable:</strong> Build succeeded but has warnings or medium anomalies</li>
     *   <li><strong>Aborted:</strong> Build was manually stopped or cancelled</li>
     *   <li><strong>Disabled:</strong> Job is disabled in Jenkins or has no recent activity</li>
     * </ul>
     *
     * <p><strong>Response Format:</strong></p>
     * <p>Returns a List of Maps, each containing job information such as:</p>
     * <ul>
     *   <li><strong>jobName:</strong> Name of the Jenkins job</li>
     *   <li><strong>status:</strong> Current build status and health classification</li>
     *   <li><strong>lastBuild:</strong> Information about the most recent build</li>
     *   <li><strong>buildHistory:</strong> Summary of recent build results</li>
     *   <li><strong>healthScore:</strong> Overall job health assessment (0-100)</li>
     *   <li><strong>anomalyCount:</strong> Number of anomalies in recent builds</li>
     *   <li><strong>lastActivity:</strong> Timestamp of last build activity</li>
     * </ul>
     *
     * <p><strong>Data Integration:</strong></p>
     * <ul>
     *   <li><strong>Database Analysis:</strong> AI analysis results and anomaly data</li>
     *   <li><strong>Jenkins API:</strong> Real-time job status and configuration</li>
     *   <li><strong>Computed Metrics:</strong> Health scores and trend analysis</li>
     *   <li><strong>Status Mapping:</strong> Consistent status classification across sources</li>
     * </ul>
     *
     * <p><strong>Job Explorer Features:</strong></p>
     * <ul>
     *   <li><strong>Tabbed Interface:</strong> Filter jobs by status categories</li>
     *   <li><strong>Health Indicators:</strong> Visual health status and trend information</li>
     *   <li><strong>Quick Actions:</strong> Direct links to job details and build history</li>
     *   <li><strong>Bulk Operations:</strong> Support for bulk job management operations</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Job management and administration interfaces</li>
     *   <li>System health monitoring and overview</li>
     *   <li>Troubleshooting and issue identification</li>
     *   <li>Team productivity and performance analysis</li>
     * </ul>
     *
     * <p><strong>Example Usage:</strong></p>
     * {@snippet lang=text :
     * GET /api/dashboard/job-explorer?tab=failure
     * GET /api/dashboard/job-explorer?tab=success
     * GET /api/dashboard/job-explorer
     * }
     *
     * @param tab the status filter to apply (optional, defaults to "all")
     * @return {@link List} of Maps containing job information filtered by status
     *
     * @see DashboardService#getJobsByStatus(String)
     */
    @GetMapping("/job-explorer")
    public List<Map<String, Object>> getJobs(
            @RequestParam(required = false) String tab) {
        String statusFilter = tab != null ? tab.toLowerCase() : "all";
        return dashboardService.getJobsByStatus(statusFilter);
    }



}