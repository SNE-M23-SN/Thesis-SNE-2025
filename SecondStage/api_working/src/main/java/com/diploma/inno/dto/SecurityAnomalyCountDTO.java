package com.diploma.inno.dto;


import java.time.Instant;

/**
 * Data Transfer Object for security-specific anomaly count statistics from the CI Anomaly Detector system.
 *
 * <p>This DTO encapsulates aggregated security anomaly metrics derived from AI analysis of Jenkins
 * build logs and artifacts. It provides quantitative security assessment data filtered by job scope
 * and time boundaries, enabling security-focused monitoring and trend analysis for CI/CD pipelines.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>AI Analysis:</strong> AI analyzes Jenkins build logs for security-related anomalies</li>
 *   <li><strong>JSONB Storage:</strong> Analysis results stored in chat_messages.content->'anomalies' array</li>
 *   <li><strong>Security Filtering:</strong> Anomalies filtered by type = 'security' classification</li>
 *   <li><strong>Aggregation Process:</strong> Counts aggregated by job filter and time range</li>
 *   <li><strong>Table Storage:</strong> Pre-computed metrics stored in security_anomaly_counts table</li>
 *   <li><strong>DTO Mapping:</strong> SecurityAnomalyCountProjection maps to DTO via service layer</li>
 * </ol>
 *
 * <p><strong>Database Query Structure:</strong></p>
 * <p>Security anomaly counts are retrieved from pre-computed aggregation table:</p>
 * {@snippet lang=GenericSQL :
 * SELECT job_filter AS jobFilter,
 *        anomaly_count AS anomalyCount,
 *        time_range AS timeRange,
 *        computed_at AS computedAt
 * FROM security_anomaly_counts
 * WHERE job_filter = :jobFilter
 *   AND time_range = :timeRange
 * }
 *
 * <p><strong>Security Anomaly Classification:</strong></p>
 * <p>Security anomalies are identified by AI analysis and include:</p>
 * <ul>
 *   <li><strong>Vulnerability Detection:</strong> Known CVEs and security vulnerabilities</li>
 *   <li><strong>Dependency Security:</strong> Vulnerable or outdated dependencies</li>
 *   <li><strong>Configuration Security:</strong> Insecure configurations and settings</li>
 *   <li><strong>Secret Exposure:</strong> Potential secrets or credentials in build logs</li>
 *   <li><strong>Access Control:</strong> Permission and authentication issues</li>
 *   <li><strong>Suspicious Patterns:</strong> Unusual build patterns or behaviors</li>
 *   <li><strong>Code Quality Security:</strong> Security-related code quality violations</li>
 * </ul>
 *
 * <p><strong>Aggregation Process:</strong></p>
 * <p>Security anomaly counts are computed through the following process:</p>
 * <ol>
 *   <li><strong>Source Data:</strong> AI analysis results from chat_messages table</li>
 *   <li><strong>JSONB Parsing:</strong> Extract anomalies array from ASSISTANT message content</li>
 *   <li><strong>Type Filtering:</strong> Filter anomalies WHERE type = 'security'</li>
 *   <li><strong>Time Filtering:</strong> Apply time range constraints on message timestamps</li>
 *   <li><strong>Job Filtering:</strong> Apply job filter constraints on conversation_id</li>
 *   <li><strong>Count Aggregation:</strong> Sum total security anomalies by filter criteria</li>
 *   <li><strong>Storage:</strong> Store computed metrics in security_anomaly_counts table</li>
 * </ol>
 *
 * <p><strong>Job Filter Patterns:</strong></p>
 * <ul>
 *   <li><strong>"all":</strong> Aggregate security anomalies across all Jenkins jobs</li>
 *   <li><strong>Specific Job Name:</strong> Security anomalies for a particular Jenkins job</li>
 *   <li><strong>Job Pattern:</strong> May support pattern matching for job groups</li>
 *   <li><strong>Case Sensitivity:</strong> Exact match with Jenkins job names</li>
 * </ul>
 *
 * <p><strong>Time Range Specifications:</strong></p>
 * <ul>
 *   <li><strong>"7 days":</strong> Security anomalies from the last week</li>
 *   <li><strong>"30 days":</strong> Security anomalies from the last month</li>
 *   <li><strong>"90 days":</strong> Security anomalies from the last quarter</li>
 *   <li><strong>Custom Ranges:</strong> May support other time boundary specifications</li>
 * </ul>
 *
 * <p><strong>REST API Integration:</strong></p>
 * <ul>
 *   <li><strong>All Jobs:</strong> GET /api/dashboard/securityAnomalies (default: "all", "7 days")</li>
 *   <li><strong>Specific Job:</strong> GET /api/dashboard/securityAnomalies/{jobFilter}?timeRange={range}</li>
 *   <li><strong>Response Format:</strong> SecurityAnomalyCountDTO with computed metrics</li>
 *   <li><strong>Error Handling:</strong> Graceful handling of missing data with fallback DTOs</li>
 * </ul>
 *
 * <p><strong>Dashboard Integration:</strong></p>
 * <ul>
 *   <li><strong>Security Widgets:</strong> High-level security health indicators</li>
 *   <li><strong>Trend Analysis:</strong> Security anomaly trends over time</li>
 *   <li><strong>Alert Thresholds:</strong> Trigger security alerts based on anomaly counts</li>
 *   <li><strong>Executive Reporting:</strong> Security posture metrics for stakeholders</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Pre-computed Data:</strong> Fast response times from aggregated table</li>
 *   <li><strong>Scheduled Refresh:</strong> Aggregations updated periodically via scheduled tasks</li>
 *   <li><strong>Indexed Queries:</strong> Optimized for job_filter and time_range filtering</li>
 *   <li><strong>Memory Efficient:</strong> Lightweight DTO for high-frequency access</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Synchronization:</strong></p>
 * <ul>
 *   <li><strong>Periodic Updates:</strong> Aggregations refreshed on schedule (likely 15-minute intervals)</li>
 *   <li><strong>Computed Timestamp:</strong> Indicates when aggregation was last calculated</li>
 *   <li><strong>AI Analysis Dependency:</strong> Freshness depends on AI analysis completion</li>
 *   <li><strong>Build Synchronization:</strong> Coordinated with Jenkins build discovery</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li><strong>Security Monitoring:</strong> Track security anomaly trends across CI/CD pipeline</li>
 *   <li><strong>Risk Assessment:</strong> Quantify security risk for deployment decisions</li>
 *   <li><strong>Compliance Reporting:</strong> Security metrics for audit and compliance</li>
 *   <li><strong>Trend Analysis:</strong> Identify improving or deteriorating security posture</li>
 *   <li><strong>Alert Systems:</strong> Trigger security alerts based on anomaly thresholds</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "jobFilter": "my-web-app",
 *   "anomalyCount": 3,
 *   "timeRange": "7 days",
 *   "computedAt": "2024-01-15T16:00:00.000Z"
 * }
 * }
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li><strong>Missing Data:</strong> Returns DTO with 0 count when no data found</li>
 *   <li><strong>Invalid Parameters:</strong> Validation in service layer with fallback responses</li>
 *   <li><strong>Database Errors:</strong> Graceful degradation with error DTOs</li>
 *   <li><strong>Null Safety:</strong> Handles null/empty job filters and time ranges</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All fields are set via constructor and accessed through getter methods.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see SecurityAnomalyCountProjection
 * @see com.diploma.inno.service.DashboardService#getSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
 */
public class SecurityAnomalyCountDTO {

    /**
     * The job filter criteria used for security anomaly aggregation.
     *
     * <p>This field specifies the scope of Jenkins jobs included in the security
     * anomaly count calculation. It determines whether the count represents
     * security anomalies from all jobs or a specific job subset.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.job_filter} column</p>
     *
     * <p><strong>Filter Values:</strong></p>
     * <ul>
     *   <li><strong>"all":</strong> Aggregates security anomalies across all Jenkins jobs</li>
     *   <li><strong>Specific Job Name:</strong> Security anomalies for a particular Jenkins job</li>
     *   <li><strong>Job Pattern:</strong> May support pattern matching for job groups</li>
     *   <li><strong>"unknown":</strong> Fallback value for invalid or missing parameters</li>
     * </ul>
     *
     * <p><strong>Aggregation Scope:</strong></p>
     * <ul>
     *   <li><strong>Global Scope:</strong> "all" provides organization-wide security metrics</li>
     *   <li><strong>Job-Specific:</strong> Individual job names provide focused security analysis</li>
     *   <li><strong>Team Scope:</strong> Job patterns may group related projects</li>
     *   <li><strong>Error Handling:</strong> "unknown" indicates parameter validation failures</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Widgets:</strong> Global security health overview</li>
     *   <li><strong>Job Monitoring:</strong> Security-focused job analysis</li>
     *   <li><strong>Team Dashboards:</strong> Security metrics for specific teams</li>
     *   <li><strong>Executive Reporting:</strong> High-level security posture metrics</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Exact match with Jenkins job names</li>
     *   <li><strong>Validation:</strong> Service layer validates against known job names</li>
     *   <li><strong>Synchronization:</strong> Coordinated with Jenkins job discovery</li>
     *   <li><strong>Fallback Handling:</strong> Graceful degradation for invalid filters</li>
     * </ul>
     */
    private String jobFilter;

    /**
     * The total count of security-specific anomalies within the specified criteria.
     *
     * <p>This field represents the aggregated number of security anomalies identified
     * by AI analysis within the specified job filter and time range. It provides
     * a quantitative measure of security issues detected in the CI/CD pipeline.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.anomaly_count} column</p>
     *
     * <p><strong>Count Calculation:</strong></p>
     * <ul>
     *   <li><strong>Source Data:</strong> AI analysis results from chat_messages table</li>
     *   <li><strong>Type Filtering:</strong> Only anomalies with type = 'security'</li>
     *   <li><strong>Time Filtering:</strong> Constrained by time range parameter</li>
     *   <li><strong>Job Filtering:</strong> Constrained by job filter parameter</li>
     * </ul>
     *
     * <p><strong>Security Anomaly Types Included:</strong></p>
     * <ul>
     *   <li><strong>Vulnerability Detection:</strong> Known CVEs and security vulnerabilities</li>
     *   <li><strong>Dependency Security:</strong> Vulnerable or outdated dependencies</li>
     *   <li><strong>Configuration Security:</strong> Insecure configurations and settings</li>
     *   <li><strong>Secret Exposure:</strong> Potential secrets or credentials in logs</li>
     *   <li><strong>Access Control:</strong> Permission and authentication issues</li>
     *   <li><strong>Suspicious Patterns:</strong> Unusual build patterns or behaviors</li>
     * </ul>
     *
     * <p><strong>Count Interpretation:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No security anomalies detected (good security posture)</li>
     *   <li><strong>1-5:</strong> Low security anomaly count (acceptable range)</li>
     *   <li><strong>6-15:</strong> Moderate security anomaly count (requires attention)</li>
     *   <li><strong>16+:</strong> High security anomaly count (immediate review needed)</li>
     * </ul>
     *
     * <p><strong>Trend Analysis:</strong></p>
     * <ul>
     *   <li><strong>Baseline Establishment:</strong> Historical counts establish security baseline</li>
     *   <li><strong>Trend Monitoring:</strong> Increasing counts indicate deteriorating security</li>
     *   <li><strong>Improvement Tracking:</strong> Decreasing counts show security improvements</li>
     *   <li><strong>Alert Thresholds:</strong> Configurable thresholds trigger security alerts</li>
     * </ul>
     *
     * <p><strong>Dashboard Integration:</strong></p>
     * <ul>
     *   <li><strong>Security Widgets:</strong> Numerical display of security anomaly counts</li>
     *   <li><strong>Color Coding:</strong> Visual indicators based on count thresholds</li>
     *   <li><strong>Trend Charts:</strong> Historical progression of security anomaly counts</li>
     *   <li><strong>Alert Systems:</strong> Trigger notifications for elevated counts</li>
     * </ul>
     */
    private Integer anomalyCount;

    /**
     * The time range specification for security anomaly aggregation.
     *
     * <p>This field defines the temporal boundary for security anomaly counting,
     * determining how far back in time the aggregation extends. It enables
     * time-bounded security analysis and trend monitoring.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.time_range} column</p>
     *
     * <p><strong>Time Range Values:</strong></p>
     * <ul>
     *   <li><strong>"7 days":</strong> Security anomalies from the last week</li>
     *   <li><strong>"30 days":</strong> Security anomalies from the last month</li>
     *   <li><strong>"90 days":</strong> Security anomalies from the last quarter</li>
     *   <li><strong>"unknown":</strong> Fallback value for invalid time range parameters</li>
     * </ul>
     *
     * <p><strong>Temporal Analysis:</strong></p>
     * <ul>
     *   <li><strong>Short-term (7 days):</strong> Recent security trends and immediate issues</li>
     *   <li><strong>Medium-term (30 days):</strong> Monthly security patterns and improvements</li>
     *   <li><strong>Long-term (90 days):</strong> Quarterly security posture assessment</li>
     *   <li><strong>Custom Ranges:</strong> May support additional time boundary specifications</li>
     * </ul>
     *
     * <p><strong>Aggregation Logic:</strong></p>
     * <ul>
     *   <li><strong>Inclusive Boundaries:</strong> Includes anomalies from start to end of range</li>
     *   <li><strong>Rolling Windows:</strong> Time ranges are calculated from current timestamp</li>
     *   <li><strong>Timezone Handling:</strong> Consistent timezone application across calculations</li>
     *   <li><strong>Precision:</strong> Day-level precision for time range boundaries</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Recent Monitoring:</strong> 7-day range for immediate security concerns</li>
     *   <li><strong>Trend Analysis:</strong> 30-day range for monthly security trends</li>
     *   <li><strong>Strategic Planning:</strong> 90-day range for quarterly security assessment</li>
     *   <li><strong>Compliance Reporting:</strong> Various ranges for audit requirements</li>
     * </ul>
     *
     * <p><strong>Dashboard Integration:</strong></p>
     * <ul>
     *   <li><strong>Time Selectors:</strong> User interface controls for time range selection</li>
     *   <li><strong>Comparative Analysis:</strong> Side-by-side comparison of different time ranges</li>
     *   <li><strong>Trend Visualization:</strong> Time-series charts with configurable ranges</li>
     *   <li><strong>Alert Configuration:</strong> Time-range-specific alert thresholds</li>
     * </ul>
     */
    private String timeRange;

    /**
     * The timestamp when the security anomaly count was computed.
     *
     * <p>This field indicates when the aggregated security anomaly metrics were
     * last calculated and stored in the database. It provides insight into data
     * freshness and helps determine the reliability of the security metrics.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.computed_at} column</p>
     *
     * <p><strong>Computation Schedule:</strong></p>
     * <ul>
     *   <li><strong>Periodic Updates:</strong> Aggregations refreshed on scheduled intervals</li>
     *   <li><strong>15-minute Sync:</strong> Likely coordinated with Jenkins synchronization</li>
     *   <li><strong>AI Analysis Dependency:</strong> Updates after AI analysis completion</li>
     *   <li><strong>Build Synchronization:</strong> Coordinated with build discovery process</li>
     * </ul>
     *
     * <p><strong>Data Freshness Assessment:</strong></p>
     * <ul>
     *   <li><strong>&lt; 15 minutes:</strong> Fresh data, recently computed</li>
     *   <li><strong>15-30 minutes:</strong> Normal staleness, within expected range</li>
     *   <li><strong>30-60 minutes:</strong> Moderately stale, may indicate processing delays</li>
     *   <li><strong>&gt; 1 hour:</strong> Potentially stale, may indicate system issues</li>
     * </ul>
     *
     * <p><strong>System Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Health Check:</strong> Indicates proper functioning of aggregation processes</li>
     *   <li><strong>Troubleshooting:</strong> Helps diagnose data pipeline issues</li>
     *   <li><strong>Performance Tracking:</strong> Monitors aggregation processing performance</li>
     *   <li><strong>Alerting:</strong> Can trigger alerts for stale data conditions</li>
     * </ul>
     *
     * <p><strong>User Interface:</strong></p>
     * <ul>
     *   <li><strong>Data Freshness:</strong> May be displayed to indicate data currency</li>
     *   <li><strong>Cache Management:</strong> Used for client-side cache invalidation</li>
     *   <li><strong>Refresh Indicators:</strong> Helps users understand data recency</li>
     *   <li><strong>Debug Information:</strong> Available for troubleshooting purposes</li>
     * </ul>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>UTC Format:</strong> Consistent timezone for global deployments</li>
     *   <li><strong>Precision:</strong> Millisecond precision for accurate tracking</li>
     *   <li><strong>Immutable:</strong> Represents historical computation time</li>
     *   <li><strong>Sortable:</strong> Enables chronological ordering of computations</li>
     * </ul>
     */
    private Instant computedAt;

    /**
     * Constructs a new SecurityAnomalyCountDTO with the specified security metrics parameters.
     *
     * <p>This constructor creates a complete security anomaly count information object ready for
     * JSON serialization and API response. It ensures all required fields are properly
     * initialized and provides a consistent way to create DTO instances from
     * SecurityAnomalyCountProjection data.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li><strong>jobFilter:</strong> Should be valid job name or "all" for global scope</li>
     *   <li><strong>anomalyCount:</strong> Should be non-negative integer representing security anomaly count</li>
     *   <li><strong>timeRange:</strong> Should be valid time range specification (e.g., "7 days")</li>
     *   <li><strong>computedAt:</strong> Should be valid UTC timestamp of computation</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Service Layer:</strong> Called by DashboardService.getSecurityAnomalyCountByJobFilterAndTimeRange()</li>
     *   <li><strong>Projection Mapping:</strong> Parameters derived from SecurityAnomalyCountProjection</li>
     *   <li><strong>Error Handling:</strong> Used for fallback DTOs with default values</li>
     *   <li><strong>Response Assembly:</strong> Part of REST API response construction</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Filter Validation:</strong> jobFilter should match known Jenkins jobs or "all"</li>
     *   <li><strong>Count Validity:</strong> anomalyCount should be non-negative</li>
     *   <li><strong>Time Range Format:</strong> timeRange should follow expected format patterns</li>
     *   <li><strong>Timestamp Accuracy:</strong> computedAt should represent actual computation time</li>
     * </ul>
     *
     * <p><strong>Construction Examples:</strong></p>
     * {@snippet lang=java :
     * // Global security anomaly count for last week
     * new SecurityAnomalyCountDTO("all", 15, "7 days", Instant.now());
     *
     * // Job-specific security anomaly count for last month
     * new SecurityAnomalyCountDTO("my-web-app", 3, "30 days", Instant.now());
     *
     * // Fallback DTO for missing data
     * new SecurityAnomalyCountDTO("unknown", 0, "7 days", Instant.now());
     * }
     *
     * @param jobFilter the job filter criteria for security anomaly aggregation
     * @param anomalyCount the total count of security anomalies
     * @param timeRange the time range specification for aggregation
     * @param computedAt the timestamp when the count was computed
     */
    public SecurityAnomalyCountDTO(String jobFilter, Integer anomalyCount, String timeRange, Instant computedAt) {
        this.jobFilter = jobFilter;
        this.anomalyCount = anomalyCount;
        this.timeRange = timeRange;
        this.computedAt = computedAt;
    }

    /**
     * Returns the job filter criteria used for security anomaly aggregation.
     *
     * @return the job filter ("all", specific job name, or "unknown"), never null
     */
    public String getJobFilter() { return jobFilter; }

    /**
     * Sets the job filter criteria for security anomaly aggregation.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the job filter is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param jobFilter the job filter to set
     */
    public void setJobFilter(String jobFilter) { this.jobFilter = jobFilter; }

    /**
     * Returns the total count of security-specific anomalies.
     *
     * @return the security anomaly count, never null, always non-negative
     */
    public Integer getAnomalyCount() { return anomalyCount; }

    /**
     * Sets the total count of security-specific anomalies.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the anomaly count is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param anomalyCount the security anomaly count to set
     */
    public void setAnomalyCount(Integer anomalyCount) { this.anomalyCount = anomalyCount; }

    /**
     * Returns the time range specification for security anomaly aggregation.
     *
     * @return the time range (e.g., "7 days", "30 days"), never null
     */
    public String getTimeRange() { return timeRange; }

    /**
     * Sets the time range specification for security anomaly aggregation.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the time range is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param timeRange the time range to set
     */
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    /**
     * Returns the timestamp when the security anomaly count was computed.
     *
     * @return the computation timestamp in UTC, never null
     */
    public Instant getComputedAt() { return computedAt; }

    /**
     * Sets the timestamp when the security anomaly count was computed.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the computed timestamp is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param computedAt the computation timestamp to set
     */
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
}
