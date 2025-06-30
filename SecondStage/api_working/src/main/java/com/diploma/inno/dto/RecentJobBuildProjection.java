package com.diploma.inno.dto;

import java.time.Instant;

/**
 * Spring Data JPA projection interface for recent Jenkins build data from the CI Anomaly Detector system.
 *
 * <p>This projection interface provides a type-safe, performance-optimized way to retrieve
 * recent build information from the {@code recent_job_builds} materialized view. It serves
 * as an intermediate data mapping layer between the database and the {@link RecentJobBuildDTO},
 * enabling efficient data transfer with minimal memory overhead for build monitoring dashboards.</p>
 *
 * <p><strong>Database Query Integration:</strong></p>
 * <p>This projection is used by the following repository method:</p>
 * {@snippet lang=java :
 * @Query(value = "SELECT job_name AS jobName, " +
 *                "       build_id AS buildId, " +
 *                "       health_status AS healthStatus, " +
 *                "       anomaly_count AS anomalyCount, " +
 *                "       time_ago AS timeAgo, " +
 *                "       raw_timestamp AS rawTimestamp, " +
 *                "       computed_at AS computedAt, " +
 *                "       original_job_name AS originalJobName " +
 *                "FROM recent_job_builds " +
 *                "WHERE job_name = :jobName " +
 *                "ORDER BY raw_timestamp DESC",
 *                nativeQuery = true)
 * List<RecentJobBuildProjection> findRecentJobBuildsByJobName(@Param("jobName") String jobName);
 * }
 *
 * <p><strong>Materialized View Structure:</strong></p>
 * <pre>
 * Table: recent_job_builds (Materialized View)
 * ┌─────────────────────┬──────────────────────┬─────────────────────────────────────────────────────┐
 * │ Database Column     │ Projection Method    │ Description                                         │
 * ├─────────────────────┼──────────────────────┼─────────────────────────────────────────────────────┤
 * │ job_name            │ getJobName()         │ Display name of the Jenkins job                     │
 * │ build_id            │ getBuildId()         │ Build number/identifier for the specific build      │
 * │ health_status       │ getHealthStatus()    │ Calculated health status based on anomaly analysis  │
 * │ anomaly_count       │ getAnomalyCount()    │ Total number of anomalies detected in the build     │
 * │ time_ago            │ getTimeAgo()         │ Human-readable relative time description            │
 * │ raw_timestamp       │ getRawTimestamp()    │ Exact timestamp when the build was executed         │
 * │ computed_at         │ getComputedAt()      │ Timestamp when materialized view was last computed  │
 * │ original_job_name   │ getOriginalJobName() │ Original Jenkins job name without processing        │
 * └─────────────────────┴──────────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Data Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Source Tables:</strong> chat_messages, build_anomaly_summary, active_build_counts</li>
 *   <li><strong>Health Calculation:</strong> Complex CASE logic analyzes JSONB anomalies for health status</li>
 *   <li><strong>Time Processing:</strong> EXTRACT(EPOCH) calculations for human-readable relative time</li>
 *   <li><strong>Anomaly Aggregation:</strong> Total anomaly counts from build_anomaly_summary table</li>
 *   <li><strong>Materialized View:</strong> Pre-computed data stored in recent_job_builds view</li>
 *   <li><strong>Projection Mapping:</strong> Type-safe data retrieval via Spring Data JPA projection</li>
 * </ol>
 *
 * <p><strong>Health Status Calculation Logic:</strong></p>
 * <p>Health status is calculated using complex CASE logic analyzing JSONB anomalies:</p>
 * {@snippet lang=GenericSQL :
 * CASE
 *   WHEN content->'anomalies' @> '[{"severity": "CRITICAL"}]'
 *     OR content->'anomalies' @> '[{"severity": "HIGH"}]'
 *     THEN 'CRITICAL'
 *   WHEN (COUNT of MEDIUM severity anomalies) > 0
 *     OR (COUNT of CRITICAL/HIGH/MEDIUM anomalies) > 1
 *     THEN 'WARNING'
 *   WHEN (COUNT of LOW/WARNING anomalies) <= 5
 *     AND NOT (CRITICAL OR HIGH OR MEDIUM anomalies exist)
 *     THEN 'Healthy'
 *   ELSE 'Unhealthy'
 * END
 * }
 *
 * <p><strong>Time Formatting Logic:</strong></p>
 * <p>Relative time calculated using EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - timestamp)):</p>
 * <ul>
 *   <li><strong>&lt; 1 hour:</strong> "X minutes ago" format</li>
 *   <li><strong>&lt; 24 hours:</strong> "X hours ago" format</li>
 *   <li><strong>&lt; 30 days:</strong> "X days ago" format</li>
 *   <li><strong>&gt; 30 days:</strong> "YYYY-MM-DD HH24:MI" absolute format</li>
 * </ul>
 *
 * <p><strong>Materialized View Dependencies:</strong></p>
 * <ul>
 *   <li><strong>chat_messages:</strong> Source of AI analysis results and build metadata in JSONB format</li>
 *   <li><strong>build_anomaly_summary:</strong> Aggregated anomaly counts and severity distribution data</li>
 *   <li><strong>active_build_counts:</strong> Real-time active build statistics by job filter</li>
 *   <li><strong>Scheduled Refresh:</strong> View refreshed every 15 minutes via Jenkins synchronization</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Memory Efficient:</strong> Interface-based projection minimizes object creation overhead</li>
 *   <li><strong>Type Safe:</strong> Compile-time type checking for database column mapping</li>
 *   <li><strong>Fast Retrieval:</strong> Pre-computed materialized view enables sub-second response times</li>
 *   <li><strong>Minimal Overhead:</strong> Direct column mapping without intermediate object creation</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Synchronization:</strong></p>
 * <ul>
 *   <li><strong>15-minute Refresh:</strong> Materialized view refreshed every 15 minutes</li>
 *   <li><strong>Jenkins Coordination:</strong> Synchronized with Jenkins job discovery and status updates</li>
 *   <li><strong>Computed Timestamp:</strong> {@code computed_at} field indicates data freshness</li>
 *   <li><strong>Scheduled Process:</strong> Automated refresh via @Scheduled(cron = "0 0/15 * * * ?")</li>
 * </ul>
 *
 * <p><strong>Usage Flow:</strong></p>
 * <ul>
 *   <li><strong>Controller Layer:</strong> {@code DashboardController.getAllRecentBuilds()} and {@code getRecentBuildsByJobName(String)}</li>
 *   <li><strong>Service Layer:</strong> {@code DashboardService.getRecentJobBuildsByJobName(String)}</li>
 *   <li><strong>Repository Layer:</strong> {@code ChatMessageRepository.findRecentJobBuildsByJobName(String)}</li>
 *   <li><strong>REST API:</strong> Converted to {@code RecentJobBuildDTO} for JSON serialization</li>
 * </ul>
 *
 * <p><strong>Query Filtering:</strong></p>
 * <ul>
 *   <li><strong>Job-Specific:</strong> WHERE job_name = :jobName for specific job filtering</li>
 *   <li><strong>All Jobs:</strong> jobName = "all" parameter includes all jobs in results</li>
 *   <li><strong>Chronological Order:</strong> ORDER BY raw_timestamp DESC for most recent builds first</li>
 *   <li><strong>Null Safety:</strong> Handles missing or null data gracefully</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li><strong>Empty Results:</strong> Repository method returns empty list when no builds found</li>
 *   <li><strong>Service Validation:</strong> Service layer handles null or empty job names</li>
 *   <li><strong>Parameter Validation:</strong> Invalid job names result in empty responses</li>
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
 * List&lt;RecentJobBuildProjection&gt; projections = repository.findRecentJobBuildsByJobName("my-web-app");
 *
 * // Service layer conversion
 * List&lt;RecentJobBuildDTO&gt; dtos = projections.stream()
 *     .map(projection -&gt; new RecentJobBuildDTO(
 *         projection.getJobName(),
 *         projection.getBuildId(),
 *         projection.getHealthStatus(),
 *         projection.getAnomalyCount(),
 *         projection.getTimeAgo(),
 *         projection.getRawTimestamp(),
 *         projection.getComputedAt(),
 *         projection.getOriginalJobName()))
 *     .collect(Collectors.toList());
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see RecentJobBuildDTO
 * @see com.diploma.inno.service.DashboardService#getRecentJobBuildsByJobName(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findRecentJobBuildsByJobName(String)
 */
public interface RecentJobBuildProjection {

    /**
     * Returns the display name of the Jenkins job.
     *
     * <p>This method retrieves the job name used for display purposes in the dashboard.
     * It may be processed or formatted for better user experience while maintaining
     * the essential job identification information.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.job_name} column via SQL alias {@code jobName}</p>
     *
     * <p><strong>Name Processing:</strong></p>
     * <ul>
     *   <li><strong>Display Format:</strong> Optimized for dashboard presentation</li>
     *   <li><strong>Case Preservation:</strong> Maintains original Jenkins job name casing</li>
     *   <li><strong>Special Characters:</strong> Preserves spaces, hyphens, and underscores</li>
     *   <li><strong>Uniqueness:</strong> Unique identifier within Jenkins instance</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Cards:</strong> Primary job identification in build cards</li>
     *   <li><strong>Navigation:</strong> Links to job-specific build history</li>
     *   <li><strong>Filtering:</strong> Used for job-specific data filtering</li>
     *   <li><strong>Grouping:</strong> Groups builds by job for organization</li>
     * </ul>
     *
     * <p><strong>Data Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Non-null:</strong> Should never be null for valid database records</li>
     *   <li><strong>Trimmed:</strong> Database values are typically trimmed of whitespace</li>
     *   <li><strong>Consistent:</strong> Consistent formatting across all build records</li>
     *   <li><strong>Relationship:</strong> May differ from originalJobName for display optimization</li>
     * </ul>
     *
     * @return the display name of the Jenkins job, never null for valid records
     */
    String getJobName();

    /**
     * Returns the build number/identifier for this specific build.
     *
     * <p>This method retrieves the unique build number assigned by Jenkins
     * for this specific build execution. Build numbers are sequential and
     * auto-incrementing within each job.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.build_id} column via SQL alias {@code buildId}</p>
     *
     * <p><strong>Build Number Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Sequential:</strong> Auto-incrementing integers starting from 1</li>
     *   <li><strong>Job-Specific:</strong> Unique within each Jenkins job</li>
     *   <li><strong>Immutable:</strong> Never changes once assigned to a build</li>
     *   <li><strong>Chronological:</strong> Higher numbers indicate more recent builds</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>Build Identification:</strong> Primary key for build-specific operations</li>
     *   <li><strong>URL Construction:</strong> Used in Jenkins URLs and API calls</li>
     *   <li><strong>Sorting:</strong> Natural ordering for build chronology</li>
     *   <li><strong>Navigation:</strong> Links to detailed build analysis pages</li>
     * </ul>
     *
     * <p><strong>Value Range:</strong></p>
     * <ul>
     *   <li><strong>Minimum:</strong> 1 (first build in job)</li>
     *   <li><strong>Typical Range:</strong> 1-10,000 for most jobs</li>
     *   <li><strong>High Activity:</strong> Can exceed 100,000 for frequently built jobs</li>
     *   <li><strong>Data Type:</strong> Integer for efficient storage and comparison</li>
     * </ul>
     *
     * @return the build number/identifier, never null, always positive
     */
    Integer getBuildId();

    /**
     * Returns the calculated health status of the build based on anomaly analysis.
     *
     * <p>This method retrieves the overall health assessment of the build,
     * calculated by analyzing the severity and count of anomalies detected
     * during AI analysis of the build logs and artifacts.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.health_status} column via SQL alias {@code healthStatus}</p>
     *
     * <p><strong>Health Status Values:</strong></p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
     *   <li><strong>WARNING:</strong> Contains MEDIUM severity anomalies OR multiple high-severity anomalies</li>
     *   <li><strong>Healthy:</strong> Only LOW/WARNING anomalies (≤5) and no higher severity issues</li>
     *   <li><strong>Unhealthy:</strong> Default case for other scenarios or edge cases</li>
     * </ul>
     *
     * <p><strong>Calculation Logic:</strong></p>
     * <p>The health status is calculated using complex CASE logic in the materialized view:</p>
     * <ol>
     *   <li><strong>CRITICAL Check:</strong> JSONB @> operator checks for CRITICAL/HIGH severity</li>
     *   <li><strong>WARNING Check:</strong> Counts MEDIUM severity OR multiple high-severity anomalies</li>
     *   <li><strong>Healthy Check:</strong> Only LOW/WARNING (≤5) and no higher severity</li>
     *   <li><strong>Default:</strong> All other cases marked as Unhealthy</li>
     * </ol>
     *
     * <p><strong>JSONB Analysis Process:</strong></p>
     * <ul>
     *   <li><strong>Severity Detection:</strong> Uses JSONB @> operator for efficient severity matching</li>
     *   <li><strong>Count Aggregation:</strong> jsonb_array_elements() for detailed counting</li>
     *   <li><strong>Complex Logic:</strong> Multiple conditions evaluated in priority order</li>
     *   <li><strong>Performance:</strong> Optimized JSONB operations for fast calculation</li>
     * </ul>
     *
     * <p><strong>Dashboard Integration:</strong></p>
     * <ul>
     *   <li><strong>Color Coding:</strong> Red (CRITICAL), Yellow (WARNING), Green (Healthy), Gray (Unhealthy)</li>
     *   <li><strong>Status Icons:</strong> Visual indicators for quick health assessment</li>
     *   <li><strong>Filtering:</strong> Enables health-based build filtering</li>
     *   <li><strong>Alerting:</strong> Triggers notifications for CRITICAL builds</li>
     * </ul>
     *
     * @return the health status (CRITICAL, WARNING, Healthy, Unhealthy), never null for valid records
     */
    String getHealthStatus();

    /**
     * Returns the total number of anomalies detected in this build.
     *
     * <p>This method retrieves the aggregate count of all anomalies identified
     * by AI analysis during the build process, regardless of severity level.
     * It provides a quick quantitative assessment of build quality and issues.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.anomaly_count} column via SQL alias {@code anomalyCount}</p>
     *
     * <p><strong>Count Calculation:</strong></p>
     * <ul>
     *   <li><strong>Source Data:</strong> Aggregated from build_anomaly_summary.total_anomalies</li>
     *   <li><strong>All Severities:</strong> Includes CRITICAL, HIGH, MEDIUM, WARNING, and LOW anomalies</li>
     *   <li><strong>Unique Count:</strong> Each distinct anomaly counted once</li>
     *   <li><strong>Build-Specific:</strong> Count specific to this build execution</li>
     * </ul>
     *
     * <p><strong>Value Interpretation:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> Clean build with no detected anomalies</li>
     *   <li><strong>1-3:</strong> Low anomaly count, typically acceptable</li>
     *   <li><strong>4-10:</strong> Moderate anomaly count, requires attention</li>
     *   <li><strong>11+:</strong> High anomaly count, significant issues detected</li>
     * </ul>
     *
     * <p><strong>Relationship to Health Status:</strong></p>
     * <ul>
     *   <li><strong>Health Calculation:</strong> Used in conjunction with severity analysis</li>
     *   <li><strong>Threshold Logic:</strong> Count thresholds influence health status determination</li>
     *   <li><strong>Quality Indicator:</strong> Higher counts generally indicate lower build quality</li>
     *   <li><strong>Trend Analysis:</strong> Count trends help identify build quality patterns</li>
     * </ul>
     *
     * <p><strong>Dashboard Usage:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Badges:</strong> Displayed as numbered badges on build cards</li>
     *   <li><strong>Sorting:</strong> Enables sorting builds by anomaly count</li>
     *   <li><strong>Filtering:</strong> Supports filtering by anomaly count ranges</li>
     *   <li><strong>Metrics:</strong> Used in build quality metrics and reporting</li>
     * </ul>
     *
     * @return the total number of anomalies detected, never null, always non-negative
     */
    Integer getAnomalyCount();

    /**
     * Returns the human-readable relative time description for when the build occurred.
     *
     * <p>This method retrieves a user-friendly representation of when the build
     * was executed, formatted as relative time for better user experience
     * and quick temporal understanding.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.time_ago} column via SQL alias {@code timeAgo}</p>
     *
     * <p><strong>Time Formatting Logic:</strong></p>
     * <p>Calculated using EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - timestamp)):</p>
     * <ul>
     *   <li><strong>&lt; 1 hour:</strong> "X minutes ago" (e.g., "15 minutes ago")</li>
     *   <li><strong>&lt; 24 hours:</strong> "X hours ago" (e.g., "3 hours ago")</li>
     *   <li><strong>&lt; 30 days:</strong> "X days ago" (e.g., "5 days ago")</li>
     *   <li><strong>&gt; 30 days:</strong> "YYYY-MM-DD HH24:MI" format (e.g., "2024-01-15 14:30")</li>
     * </ul>
     *
     * <p><strong>Calculation Examples:</strong></p>
     * <ul>
     *   <li><strong>Recent:</strong> "5 minutes ago", "45 minutes ago"</li>
     *   <li><strong>Today:</strong> "2 hours ago", "8 hours ago"</li>
     *   <li><strong>This Week:</strong> "2 days ago", "6 days ago"</li>
     *   <li><strong>Older:</strong> "2024-01-10 09:15", "2023-12-25 16:45"</li>
     * </ul>
     *
     * <p><strong>Localization Considerations:</strong></p>
     * <ul>
     *   <li><strong>Language:</strong> Currently English-only format</li>
     *   <li><strong>Time Zone:</strong> Calculated relative to server time zone</li>
     *   <li><strong>Precision:</strong> Rounded to appropriate time units</li>
     *   <li><strong>Consistency:</strong> Same format used across all dashboard components</li>
     * </ul>
     *
     * <p><strong>User Experience:</strong></p>
     * <ul>
     *   <li><strong>Quick Scanning:</strong> Enables rapid assessment of build recency</li>
     *   <li><strong>Intuitive:</strong> Natural language format familiar to users</li>
     *   <li><strong>Contextual:</strong> Provides immediate temporal context</li>
     *   <li><strong>Complementary:</strong> Used alongside rawTimestamp for complete information</li>
     * </ul>
     *
     * @return the relative time string (e.g., "2 hours ago"), never null for valid records
     */
    String getTimeAgo();

    /**
     * Returns the exact timestamp when the build was executed.
     *
     * <p>This method retrieves the precise moment when the build was started,
     * providing accurate temporal information for sorting, filtering, and
     * detailed analysis purposes.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.raw_timestamp} column via SQL alias {@code rawTimestamp}</p>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Precision:</strong> Nanosecond precision for accurate ordering</li>
     *   <li><strong>Time Zone:</strong> UTC format for consistency across deployments</li>
     *   <li><strong>Immutable:</strong> Represents historical build execution time</li>
     *   <li><strong>Sortable:</strong> Enables precise chronological ordering</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <ul>
     *   <li><strong>Build Metadata:</strong> Extracted from Jenkins build metadata</li>
     *   <li><strong>Start Time:</strong> Represents build start time, not completion</li>
     *   <li><strong>Accuracy:</strong> Reflects actual Jenkins build execution time</li>
     *   <li><strong>Consistency:</strong> Synchronized with Jenkins server time</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>Sorting:</strong> Primary sort key for chronological ordering</li>
     *   <li><strong>Filtering:</strong> Date range filtering for build history</li>
     *   <li><strong>Analysis:</strong> Temporal analysis and trend identification</li>
     *   <li><strong>Comparison:</strong> Precise time comparison between builds</li>
     * </ul>
     *
     * <p><strong>Relationship to timeAgo:</strong></p>
     * <ul>
     *   <li><strong>Precision vs Readability:</strong> rawTimestamp provides precision, timeAgo provides readability</li>
     *   <li><strong>Complementary:</strong> Both fields serve different user interface needs</li>
     *   <li><strong>Source:</strong> timeAgo calculated from rawTimestamp</li>
     *   <li><strong>Consistency:</strong> Both represent the same temporal moment</li>
     * </ul>
     *
     * @return the exact timestamp when the build was executed, never null for valid records
     */
    Instant getRawTimestamp();

    /**
     * Returns the timestamp when the materialized view data was last computed.
     *
     * <p>This method retrieves when the materialized view containing this build
     * information was last refreshed, providing insight into data freshness
     * and helping determine the reliability of the information.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.computed_at} column via SQL alias {@code computedAt}</p>
     *
     * <p><strong>Refresh Schedule:</strong></p>
     * <ul>
     *   <li><strong>15-minute Sync:</strong> Materialized view refreshed every 15 minutes</li>
     *   <li><strong>Jenkins Coordination:</strong> Synchronized with Jenkins job discovery</li>
     *   <li><strong>Scheduled Process:</strong> Automated refresh via @Scheduled annotation</li>
     *   <li><strong>Consistency:</strong> All builds in response have same computedAt value</li>
     * </ul>
     *
     * <p><strong>Data Freshness Assessment:</strong></p>
     * <ul>
     *   <li><strong>&lt; 15 minutes:</strong> Fresh data, recently computed</li>
     *   <li><strong>15-30 minutes:</strong> Normal staleness, within expected range</li>
     *   <li><strong>30-60 minutes:</strong> Moderately stale, may indicate sync delays</li>
     *   <li><strong>&gt; 1 hour:</strong> Potentially stale, may indicate system issues</li>
     * </ul>
     *
     * <p><strong>System Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Health Check:</strong> Indicates proper functioning of sync processes</li>
     *   <li><strong>Troubleshooting:</strong> Helps diagnose data pipeline issues</li>
     *   <li><strong>Performance:</strong> Tracks materialized view refresh performance</li>
     *   <li><strong>Alerting:</strong> Can trigger alerts for stale data conditions</li>
     * </ul>
     *
     * <p><strong>User Interface:</strong></p>
     * <ul>
     *   <li><strong>Data Freshness:</strong> May be displayed to indicate data currency</li>
     *   <li><strong>Cache Invalidation:</strong> Used for client-side cache management</li>
     *   <li><strong>Refresh Indicators:</strong> Helps users understand data recency</li>
     *   <li><strong>Debug Information:</strong> Available for troubleshooting purposes</li>
     * </ul>
     *
     * @return the timestamp when materialized view was computed, never null for valid records
     */
    Instant getComputedAt();

    /**
     * Returns the original Jenkins job name without any processing or formatting.
     *
     * <p>This method retrieves the exact job name as it appears in Jenkins,
     * providing a reference to the original identifier for cases where
     * the display name (jobName) has been processed or modified.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.original_job_name} column via SQL alias {@code originalJobName}</p>
     *
     * <p><strong>Name Preservation:</strong></p>
     * <ul>
     *   <li><strong>Exact Match:</strong> Identical to Jenkins job configuration name</li>
     *   <li><strong>Case Sensitive:</strong> Preserves original casing from Jenkins</li>
     *   <li><strong>Special Characters:</strong> Includes all original special characters</li>
     *   <li><strong>Immutable:</strong> Never modified from original Jenkins value</li>
     * </ul>
     *
     * <p><strong>Relationship to jobName:</strong></p>
     * <ul>
     *   <li><strong>Source vs Display:</strong> originalJobName is source, jobName is display</li>
     *   <li><strong>Processing:</strong> jobName may be processed for better UX</li>
     *   <li><strong>Fallback:</strong> originalJobName used when exact matching required</li>
     *   <li><strong>Consistency:</strong> Both refer to the same Jenkins job</li>
     * </ul>
     *
     * <p><strong>Usage Scenarios:</strong></p>
     * <ul>
     *   <li><strong>API Calls:</strong> Used for Jenkins API interactions requiring exact names</li>
     *   <li><strong>URL Construction:</strong> Building Jenkins URLs with proper encoding</li>
     *   <li><strong>Database Queries:</strong> Exact matching in database operations</li>
     *   <li><strong>Integration:</strong> Third-party integrations requiring precise names</li>
     * </ul>
     *
     * <p><strong>Data Integrity:</strong></p>
     * <ul>
     *   <li><strong>Audit Trail:</strong> Maintains original job identification</li>
     *   <li><strong>Traceability:</strong> Enables tracing back to original Jenkins job</li>
     *   <li><strong>Validation:</strong> Used for validating job existence in Jenkins</li>
     *   <li><strong>Synchronization:</strong> Key field for Jenkins-database synchronization</li>
     * </ul>
     *
     * @return the original Jenkins job name without processing, never null for valid records
     */
    String getOriginalJobName();
}
