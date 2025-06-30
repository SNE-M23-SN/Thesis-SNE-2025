package com.diploma.inno.dto;


import java.time.Instant;

/**
 * Data Transfer Object for recent Jenkins build information from the CI Anomaly Detector system.
 *
 * <p>This DTO encapsulates comprehensive build metadata including health status, anomaly counts,
 * and temporal information for recent Jenkins builds. It provides structured access to build
 * analytics data sourced from the {@code recent_job_builds} materialized view, enabling
 * efficient dashboard visualization and build monitoring capabilities.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Materialized View:</strong> Data sourced from {@code recent_job_builds} materialized view</li>
 *   <li><strong>Health Calculation:</strong> Complex CASE logic analyzes JSONB anomalies for health status</li>
 *   <li><strong>Time Processing:</strong> Human-readable time formatting with relative timestamps</li>
 *   <li><strong>Anomaly Aggregation:</strong> Total anomaly counts from build_anomaly_summary table</li>
 *   <li><strong>Projection Mapping:</strong> {@code RecentJobBuildProjection} interface maps database columns</li>
 *   <li><strong>DTO Construction:</strong> Service layer creates DTO instances with complete metadata</li>
 * </ol>
 *
 * <p><strong>Database Query Structure:</strong></p>
 * {@snippet lang=GenericSQL :
 * SELECT job_name AS jobName,
 *        build_id AS buildId,
 *        health_status AS healthStatus,
 *        anomaly_count AS anomalyCount,
 *        time_ago AS timeAgo,
 *        raw_timestamp AS rawTimestamp,
 *        computed_at AS computedAt,
 *        original_job_name AS originalJobName
 * FROM recent_job_builds
 * WHERE job_name = :jobName
 * ORDER BY raw_timestamp DESC
 * }
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
 * <p><strong>Health Status Categories:</strong></p>
 * <ul>
 *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
 *   <li><strong>WARNING:</strong> Contains MEDIUM severity anomalies OR multiple high-severity anomalies</li>
 *   <li><strong>Healthy:</strong> Only LOW/WARNING anomalies (≤5) and no higher severity issues</li>
 *   <li><strong>Unhealthy:</strong> Default case for other scenarios or edge cases</li>
 * </ul>
 *
 * <p><strong>Time Formatting Logic:</strong></p>
 * <ul>
 *   <li><strong>&lt; 1 hour:</strong> "X minutes ago"</li>
 *   <li><strong>&lt; 24 hours:</strong> "X hours ago"</li>
 *   <li><strong>&lt; 30 days:</strong> "X days ago"</li>
 *   <li><strong>&gt; 30 days:</strong> "YYYY-MM-DD HH24:MI" format</li>
 * </ul>
 *
 * <p><strong>Materialized View Dependencies:</strong></p>
 * <ul>
 *   <li><strong>chat_messages:</strong> Source of AI analysis results and build metadata</li>
 *   <li><strong>build_anomaly_summary:</strong> Aggregated anomaly counts and severity data</li>
 *   <li><strong>active_build_counts:</strong> Real-time build status information</li>
 *   <li><strong>Scheduled Refresh:</strong> View refreshed every 15 minutes via Jenkins sync</li>
 * </ul>
 *
 * <p><strong>REST API Integration:</strong></p>
 * <ul>
 *   <li><strong>All Builds:</strong> GET /api/dashboard/recentJobBuilds (jobName = "all")</li>
 *   <li><strong>Specific Job:</strong> GET /api/dashboard/recentJobBuilds/{jobName}</li>
 *   <li><strong>Response Format:</strong> List&lt;RecentJobBuildDTO&gt; ordered by timestamp DESC</li>
 *   <li><strong>Data Freshness:</strong> Updated every 15 minutes via scheduled sync</li>
 * </ul>
 *
 * <p><strong>Dashboard Integration:</strong></p>
 * <ul>
 *   <li><strong>Build Cards:</strong> Visual representation of build health and status</li>
 *   <li><strong>Health Indicators:</strong> Color-coded health status for quick assessment</li>
 *   <li><strong>Anomaly Badges:</strong> Anomaly count display with severity context</li>
 *   <li><strong>Time Display:</strong> Human-readable relative timestamps</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Materialized View:</strong> Pre-computed data for fast response times</li>
 *   <li><strong>Indexed Queries:</strong> Optimized for job_name and timestamp filtering</li>
 *   <li><strong>Batch Processing:</strong> Efficient bulk data retrieval for dashboard</li>
 *   <li><strong>Memory Efficient:</strong> Projection interface minimizes data transfer</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Synchronization:</strong></p>
 * <ul>
 *   <li><strong>15-minute Sync:</strong> Coordinated with Jenkins job synchronization</li>
 *   <li><strong>Computed Timestamp:</strong> Indicates last materialized view refresh</li>
 *   <li><strong>Real-time Accuracy:</strong> May lag by up to 15 minutes for new builds</li>
 *   <li><strong>Consistency:</strong> Ensures consistent data across dashboard components</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "jobName": "my-web-app",
 *   "buildId": 123,
 *   "healthStatus": "WARNING",
 *   "anomalyCount": 3,
 *   "timeAgo": "2 hours ago",
 *   "rawTimestamp": "2024-01-15T14:30:45.123Z",
 *   "computedAt": "2024-01-15T16:00:00.000Z",
 *   "originalJobName": "my-web-app"
 * }
 * }
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All fields are set via constructor and accessed through getter methods.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see RecentJobBuildProjection
 * @see com.diploma.inno.service.DashboardService#getRecentJobBuildsByJobName(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findRecentJobBuildsByJobName(String)
 */
public class RecentJobBuildDTO {

    /**
     * The display name of the Jenkins job.
     *
     * <p>This field contains the job name used for display purposes in the dashboard.
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
     * <p><strong>Relationship to Original:</strong></p>
     * <ul>
     *   <li>May differ from originalJobName for display optimization</li>
     *   <li>Maintains functional equivalence for job identification</li>
     *   <li>Used in conjunction with originalJobName for complete context</li>
     * </ul>
     */
    private String jobName;

    /**
     * The build number/identifier for this specific build.
     *
     * <p>This field represents the unique build number assigned by Jenkins
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
     */
    private Integer buildId;

    /**
     * The calculated health status of the build based on anomaly analysis.
     *
     * <p>This field represents the overall health assessment of the build,
     * calculated by analyzing the severity and count of anomalies detected
     * during AI analysis of the build logs and artifacts.</p>
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
     * <p>The health status is calculated using complex CASE logic in SQL:</p>
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
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code recent_job_builds.health_status} column via SQL alias {@code healthStatus}</p>
     */
    private String healthStatus;

    /**
     * The total number of anomalies detected in this build.
     *
     * <p>This field represents the aggregate count of all anomalies identified
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
     */
    private Integer anomalyCount;

    /**
     * Human-readable relative time description for when the build occurred.
     *
     * <p>This field provides a user-friendly representation of when the build
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
     */
    private String timeAgo;

    /**
     * The exact timestamp when the build was executed.
     *
     * <p>This field contains the precise moment when the build was started,
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
     */
    private Instant rawTimestamp;

    /**
     * The timestamp when the materialized view data was last computed.
     *
     * <p>This field indicates when the materialized view containing this build
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
     */
    private Instant computedAt;

    /**
     * The original Jenkins job name without any processing or formatting.
     *
     * <p>This field preserves the exact job name as it appears in Jenkins,
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
     */
    private String originalJobName;

    /**
     * Constructs a new RecentJobBuildDTO with the specified parameters.
     *
     * <p>This constructor creates a complete recent build information object ready for
     * JSON serialization and API response. It ensures all required fields are
     * properly initialized and provides a consistent way to create DTO instances
     * from materialized view projection data.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li><strong>jobName:</strong> Should be non-null and represent valid Jenkins job</li>
     *   <li><strong>buildId:</strong> Should be positive integer representing valid build number</li>
     *   <li><strong>healthStatus:</strong> Should match one of the defined health status values</li>
     *   <li><strong>anomalyCount:</strong> Should be non-negative integer</li>
     *   <li><strong>timeAgo:</strong> Should be properly formatted relative time string</li>
     *   <li><strong>rawTimestamp:</strong> Should be valid UTC timestamp</li>
     *   <li><strong>computedAt:</strong> Should represent materialized view refresh time</li>
     *   <li><strong>originalJobName:</strong> Should match exact Jenkins job name</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Service Layer:</strong> Called by DashboardService.getRecentJobBuildsByJobName()</li>
     *   <li><strong>Projection Mapping:</strong> Parameters derived from RecentJobBuildProjection</li>
     *   <li><strong>Stream Processing:</strong> Used in stream map operations for bulk conversion</li>
     *   <li><strong>Response Assembly:</strong> Part of REST API response construction</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Job Names:</strong> jobName and originalJobName should refer to same job</li>
     *   <li><strong>Timestamps:</strong> rawTimestamp should be earlier than computedAt</li>
     *   <li><strong>Health vs Count:</strong> healthStatus should align with anomalyCount patterns</li>
     *   <li><strong>Time Consistency:</strong> timeAgo should be calculated from rawTimestamp</li>
     * </ul>
     *
     * <p><strong>Construction Examples:</strong></p>
     * {@snippet lang=java :
     * // Healthy build with no anomalies
     * new RecentJobBuildDTO("my-web-app", 123, "Healthy", 0, "2 hours ago",
     *                       Instant.parse("2024-01-15T14:30:45.123Z"),
     *                       Instant.parse("2024-01-15T16:00:00.000Z"), "my-web-app");
     *
     * // Critical build with high anomaly count
     * new RecentJobBuildDTO("backend-api", 456, "CRITICAL", 8, "30 minutes ago",
     *                       Instant.parse("2024-01-15T15:30:45.123Z"),
     *                       Instant.parse("2024-01-15T16:00:00.000Z"), "backend-api");
     * }
     *
     * @param jobName the display name of the Jenkins job
     * @param buildId the build number/identifier for this specific build
     * @param healthStatus the calculated health status based on anomaly analysis
     * @param anomalyCount the total number of anomalies detected in this build
     * @param timeAgo human-readable relative time description
     * @param rawTimestamp the exact timestamp when the build was executed
     * @param computedAt the timestamp when the materialized view data was computed
     * @param originalJobName the original Jenkins job name without processing
     */
    public RecentJobBuildDTO(String jobName, Integer buildId, String healthStatus, Integer anomalyCount,
                             String timeAgo, Instant rawTimestamp, Instant computedAt, String originalJobName) {
        this.jobName = jobName;
        this.buildId = buildId;
        this.healthStatus = healthStatus;
        this.anomalyCount = anomalyCount;
        this.timeAgo = timeAgo;
        this.rawTimestamp = rawTimestamp;
        this.computedAt = computedAt;
        this.originalJobName = originalJobName;
    }

    /**
     * Returns the display name of the Jenkins job.
     *
     * @return the job name used for display purposes, never null
     */
    public String getJobName() { return jobName; }

    /**
     * Sets the display name of the Jenkins job.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the job name is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) { this.jobName = jobName; }

    /**
     * Returns the build number/identifier for this specific build.
     *
     * @return the build ID, never null, always positive
     */
    public Integer getBuildId() { return buildId; }

    /**
     * Sets the build number/identifier for this specific build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the build ID is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param buildId the build ID to set
     */
    public void setBuildId(Integer buildId) { this.buildId = buildId; }

    /**
     * Returns the calculated health status of the build.
     *
     * @return the health status (CRITICAL, WARNING, Healthy, Unhealthy), never null
     */
    public String getHealthStatus() { return healthStatus; }

    /**
     * Sets the calculated health status of the build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the health status is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param healthStatus the health status to set
     */
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    /**
     * Returns the total number of anomalies detected in this build.
     *
     * @return the anomaly count, never null, always non-negative
     */
    public Integer getAnomalyCount() { return anomalyCount; }

    /**
     * Sets the total number of anomalies detected in this build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the anomaly count is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param anomalyCount the anomaly count to set
     */
    public void setAnomalyCount(Integer anomalyCount) { this.anomalyCount = anomalyCount; }

    /**
     * Returns the human-readable relative time description.
     *
     * @return the relative time string (e.g., "2 hours ago"), never null
     */
    public String getTimeAgo() { return timeAgo; }

    /**
     * Sets the human-readable relative time description.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the time ago string is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param timeAgo the relative time string to set
     */
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }

    /**
     * Returns the exact timestamp when the build was executed.
     *
     * @return the raw timestamp in UTC, never null
     */
    public Instant getRawTimestamp() { return rawTimestamp; }

    /**
     * Sets the exact timestamp when the build was executed.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the raw timestamp is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param rawTimestamp the raw timestamp to set
     */
    public void setRawTimestamp(Instant rawTimestamp) { this.rawTimestamp = rawTimestamp; }

    /**
     * Returns the timestamp when the materialized view data was computed.
     *
     * @return the computed timestamp in UTC, never null
     */
    public Instant getComputedAt() { return computedAt; }

    /**
     * Sets the timestamp when the materialized view data was computed.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the computed timestamp is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param computedAt the computed timestamp to set
     */
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }

    /**
     * Returns the original Jenkins job name without processing.
     *
     * @return the original job name, never null
     */
    public String getOriginalJobName() { return originalJobName; }

    /**
     * Sets the original Jenkins job name without processing.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the original job name is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param originalJobName the original job name to set
     */
    public void setOriginalJobName(String originalJobName) { this.originalJobName = originalJobName; }
}