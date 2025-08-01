package com.diploma.inno.dto;


import java.time.Instant;

/**
 * Spring Data JPA projection interface for security anomaly count statistics from the CI Anomaly Detector system.
 *
 * <p>This projection interface provides a type-safe, performance-optimized way to retrieve
 * aggregated security anomaly count statistics from the {@code security_anomaly_counts} database table.
 * It serves as an intermediate data mapping layer between the database and the {@link SecurityAnomalyCountDTO},
 * enabling efficient data transfer with minimal memory overhead for security-focused monitoring.</p>
 *
 * <p><strong>Database Query Integration:</strong></p>
 * <p>This projection is used by the following repository method:</p>
 * {@snippet lang=java :
 * @Query(value = "SELECT job_filter AS jobFilter, " +
 *                "       anomaly_count AS anomalyCount, " +
 *                "       time_range AS timeRange, " +
 *                "       computed_at AS computedAt " +
 *                "FROM security_anomaly_counts " +
 *                "WHERE job_filter = :jobFilter AND time_range = :timeRange",
 *                nativeQuery = true)
 * SecurityAnomalyCountProjection findSecurityAnomalyCountByJobFilterAndTimeRange(
 *     @Param("jobFilter") String jobFilter,
 *     @Param("timeRange") String timeRange);
 * }
 *
 * <p><strong>Security Anomaly Counts Table Structure:</strong></p>
 * <pre>
 * Table: security_anomaly_counts (Pre-computed Aggregation Table)
 * ┌─────────────────────┬──────────────────────┬─────────────────────────────────────────────────────┐
 * │ Database Column     │ Projection Method    │ Description                                         │
 * ├─────────────────────┼──────────────────────┼─────────────────────────────────────────────────────┤
 * │ job_filter          │ getJobFilter()       │ Job scope filter ("all" or specific job name)       │
 * │ anomaly_count       │ getAnomalyCount()    │ Total count of security-specific anomalies          │
 * │ time_range          │ getTimeRange()       │ Time boundary specification (e.g., "7 days")        │
 * │ computed_at         │ getComputedAt()      │ Timestamp when aggregation was last computed        │
 * └─────────────────────┴──────────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Data Aggregation Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Source Data:</strong> AI analysis results from chat_messages table</li>
 *   <li><strong>JSONB Parsing:</strong> Extract anomalies array from ASSISTANT message content</li>
 *   <li><strong>Security Filtering:</strong> Filter anomalies WHERE type = 'security'</li>
 *   <li><strong>Time Filtering:</strong> Apply time range constraints on message timestamps</li>
 *   <li><strong>Job Filtering:</strong> Apply job filter constraints on conversation_id</li>
 *   <li><strong>Count Aggregation:</strong> Sum total security anomalies by filter criteria</li>
 *   <li><strong>Storage:</strong> Store computed metrics in security_anomaly_counts table</li>
 *   <li><strong>Projection Mapping:</strong> Type-safe data retrieval via Spring Data JPA projection</li>
 * </ol>
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
 * <p><strong>Aggregation Process Details:</strong></p>
 * <p>Security anomaly counts are computed through complex JSONB operations:</p>
 * {@snippet lang=sql :
 * SELECT
 *   CASE
 *     WHEN :jobFilter = 'all' THEN 'all'
 *     ELSE conversation_id
 *   END AS job_filter,
 *   COUNT(*) FILTER (
 *     WHERE jsonb_array_elements(content->'anomalies')->>'type' = 'security'
 *   ) AS anomaly_count,
 *   :timeRange AS time_range,
 *   CURRENT_TIMESTAMP AS computed_at
 * FROM chat_messages
 * WHERE message_type = 'ASSISTANT'
 *   AND timestamp >= (CURRENT_TIMESTAMP - INTERVAL :timeRange)
 *   AND (:jobFilter = 'all' OR conversation_id = :jobFilter)
 * GROUP BY job_filter, time_range
 * }
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Pre-computed Data:</strong> Fast response times from aggregated table</li>
 *   <li><strong>Memory Efficient:</strong> Interface-based projection minimizes object creation overhead</li>
 *   <li><strong>Type Safe:</strong> Compile-time type checking for database column mapping</li>
 *   <li><strong>Indexed Queries:</strong> Optimized for job_filter and time_range filtering</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Synchronization:</strong></p>
 * <ul>
 *   <li><strong>Scheduled Refresh:</strong> Aggregations updated periodically (likely 15-minute intervals)</li>
 *   <li><strong>Jenkins Coordination:</strong> Synchronized with Jenkins job discovery and AI analysis</li>
 *   <li><strong>Computed Timestamp:</strong> {@code computed_at} field indicates data freshness</li>
 *   <li><strong>AI Analysis Dependency:</strong> Updates after AI analysis completion</li>
 * </ul>
 *
 * <p><strong>Usage Flow:</strong></p>
 * <ul>
 *   <li><strong>Controller Layer:</strong> {@code DashboardController.getAllSecurityAnomalies()} and {@code getSecurityAnomaliesByJobFilterAndTimeRange()}</li>
 *   <li><strong>Service Layer:</strong> {@code DashboardService.getSecurityAnomalyCountByJobFilterAndTimeRange(String, String)}</li>
 *   <li><strong>Repository Layer:</strong> {@code ChatMessageRepository.findSecurityAnomalyCountByJobFilterAndTimeRange(String, String)}</li>
 *   <li><strong>REST API:</strong> Converted to {@code SecurityAnomalyCountDTO} for JSON serialization</li>
 * </ul>
 *
 * <p><strong>Query Filtering:</strong></p>
 * <ul>
 *   <li><strong>Job-Specific:</strong> WHERE job_filter = :jobFilter for specific job filtering</li>
 *   <li><strong>All Jobs:</strong> jobFilter = "all" parameter includes all jobs in results</li>
 *   <li><strong>Time-Bounded:</strong> WHERE time_range = :timeRange for temporal filtering</li>
 *   <li><strong>Null Safety:</strong> Handles missing or null data gracefully</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li><strong>Empty Results:</strong> Repository method returns null when no matching records found</li>
 *   <li><strong>Service Validation:</strong> Service layer handles null projections with fallback DTOs</li>
 *   <li><strong>Parameter Validation:</strong> Invalid job filters or time ranges result in fallback responses</li>
 *   <li><strong>Database Errors:</strong> Handled by Spring Data JPA exception translation</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This projection interface is thread-safe as it represents immutable data retrieved
 * from the database. Multiple threads can safely access projection instances concurrently.</p>
 *
 * <p><strong>Example Usage:</strong></p>
 * {@snippet lang=java :
 * // Repository layer
 * SecurityAnomalyCountProjection projection = repository
 *     .findSecurityAnomalyCountByJobFilterAndTimeRange("my-web-app", "7 days");
 *
 * // Service layer conversion
 * if (projection != null) {
 *     SecurityAnomalyCountDTO dto = new SecurityAnomalyCountDTO(
 *         projection.getJobFilter(),
 *         projection.getAnomalyCount(),
 *         projection.getTimeRange(),
 *         projection.getComputedAt());
 * } else {
 *     // Fallback DTO for missing data
 *     SecurityAnomalyCountDTO dto = new SecurityAnomalyCountDTO(
 *         "unknown", 0, "7 days", Instant.now());
 * }
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see SecurityAnomalyCountDTO
 * @see com.diploma.inno.service.DashboardService#getSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findSecurityAnomalyCountByJobFilterAndTimeRange(String, String)
 */
public interface SecurityAnomalyCountProjection {

    /**
     * Returns the job filter criteria used for security anomaly aggregation.
     *
     * <p>This method retrieves the job scope filter that determines which Jenkins jobs
     * are included in the security anomaly count calculation. The value indicates
     * whether the count represents security anomalies from all jobs or a specific job subset.</p>
     *
     * <p><strong>Database Column Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.job_filter} column via Spring Data JPA projection</p>
     *
     * <p><strong>Return Values:</strong></p>
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
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Exact match with Jenkins job names</li>
     *   <li><strong>Validation:</strong> Service layer validates against known job names</li>
     *   <li><strong>Synchronization:</strong> Coordinated with Jenkins job discovery</li>
     *   <li><strong>Fallback Handling:</strong> Graceful degradation for invalid filters</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Widgets:</strong> Global security health overview</li>
     *   <li><strong>Job Monitoring:</strong> Security-focused job analysis</li>
     *   <li><strong>Team Dashboards:</strong> Security metrics for specific teams</li>
     *   <li><strong>Executive Reporting:</strong> High-level security posture metrics</li>
     * </ul>
     *
     * @return the job filter criteria ("all", specific job name, or "unknown"), never null
     */
    String getJobFilter();

    /**
     * Returns the total count of security-specific anomalies within the specified criteria.
     *
     * <p>This method retrieves the aggregated number of security anomalies identified
     * by AI analysis within the specified job filter and time range. It provides
     * a quantitative measure of security issues detected in the CI/CD pipeline.</p>
     *
     * <p><strong>Database Column Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.anomaly_count} column via Spring Data JPA projection</p>
     *
     * <p><strong>Count Calculation Process:</strong></p>
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
     * <p><strong>Aggregation Logic:</strong></p>
     * <ul>
     *   <li><strong>JSONB Operations:</strong> Complex queries extract security anomalies from JSONB arrays</li>
     *   <li><strong>Pre-computation:</strong> Counts calculated during scheduled aggregation processes</li>
     *   <li><strong>Performance Optimization:</strong> Avoids real-time JSONB parsing for fast response times</li>
     *   <li><strong>Data Consistency:</strong> Ensures accurate counts across time ranges and job filters</li>
     * </ul>
     *
     * @return the total count of security anomalies, never null, always non-negative
     */
    Integer getAnomalyCount();

    /**
     * Returns the time range specification for security anomaly aggregation.
     *
     * <p>This method retrieves the temporal boundary that defines how far back in time
     * the security anomaly counting extends. It enables time-bounded security analysis
     * and trend monitoring for CI/CD pipeline security assessment.</p>
     *
     * <p><strong>Database Column Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.time_range} column via Spring Data JPA projection</p>
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
     *
     * @return the time range specification (e.g., "7 days", "30 days"), never null
     */
    String getTimeRange();

    /**
     * Returns the timestamp when the security anomaly count was computed.
     *
     * <p>This method retrieves the timestamp indicating when the aggregated security
     * anomaly metrics were last calculated and stored in the database. It provides
     * insight into data freshness and helps determine the reliability of the security metrics.</p>
     *
     * <p><strong>Database Column Mapping:</strong></p>
     * <p>Maps to {@code security_anomaly_counts.computed_at} column via Spring Data JPA projection</p>
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
     *
     * @return the computation timestamp in UTC, never null
     */
    Instant getComputedAt();
}

