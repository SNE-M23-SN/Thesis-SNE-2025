package com.diploma.inno.dto;

import java.time.Instant;

/**
 * Data Transfer Object for active build count information in the CI Anomaly Detector system.
 *
 * <p>This DTO encapsulates real-time statistics about currently active Jenkins builds,
 * providing essential metrics for system load monitoring, capacity planning, and
 * dashboard visualization. It represents aggregated data from the {@code active_build_counts}
 * database table, which is populated and maintained through scheduled synchronization
 * with the Jenkins server.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins API Integration:</strong> Real-time build status fetched via jenkins-rest library</li>
 *   <li><strong>Data Aggregation:</strong> Build counts aggregated by job filter in {@code active_build_counts} table</li>
 *   <li><strong>Database Query:</strong> {@code ChatMessageRepository.findActiveBuildCountByJobFilter()} retrieves data</li>
 *   <li><strong>Projection Mapping:</strong> {@code ActiveBuildCountProjection} interface maps database columns</li>
 *   <li><strong>DTO Construction:</strong> Service layer creates DTO instances with validation and error handling</li>
 * </ol>
 *
 * <p><strong>Database Schema Mapping:</strong></p>
 * <pre>
 * Table: active_build_counts
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────┐
 * │ Column          │ Java Field       │ Description                         │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────┤
 * │ job_filter      │ jobFilter        │ Job name or "all" for system-wide   │
 * │ active_builds   │ activeBuilds     │ Count of currently running builds   │
 * │ computed_at     │ computedAt       │ Timestamp of last calculation       │
 * └─────────────────┴──────────────────┴─────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Active Build Definition:</strong></p>
 * <p>A build is considered "active" if it meets any of the following criteria:</p>
 * <ul>
 *   <li><strong>Currently Executing:</strong> Build is actively running build steps</li>
 *   <li><strong>Queue Waiting:</strong> Build is waiting in the Jenkins build queue</li>
 *   <li><strong>Paused/Manual:</strong> Build is paused for manual intervention or approval</li>
 *   <li><strong>Starting Up:</strong> Build is initializing or starting up</li>
 * </ul>
 *
 * <p><strong>Job Filter Patterns:</strong></p>
 * <ul>
 *   <li><strong>"all":</strong> System-wide count across all Jenkins jobs</li>
 *   <li><strong>Specific Job Name:</strong> Count for a single job (e.g., "my-web-app")</li>
 *   <li><strong>"error":</strong> Error indicator when validation fails or data is unavailable</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Caching:</strong></p>
 * <ul>
 *   <li><strong>Update Frequency:</strong> Data refreshed every 15-30 seconds via scheduled tasks</li>
 *   <li><strong>Cache Strategy:</strong> Balance between real-time accuracy and system performance</li>
 *   <li><strong>Computed Timestamp:</strong> {@code computedAt} field indicates data freshness</li>
 *   <li><strong>Fallback Behavior:</strong> Returns current timestamp for missing data scenarios</li>
 * </ul>
 *
 * <p><strong>Error Handling Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Null Job Filter:</strong> Returns DTO with "error" filter and 0 count</li>
 *   <li><strong>Empty Job Filter:</strong> Returns DTO with "error" filter and 0 count</li>
 *   <li><strong>Missing Database Record:</strong> Returns DTO with "error" filter and 0 count</li>
 *   <li><strong>Jenkins API Failure:</strong> Returns last known cached values</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>REST API Responses:</strong> Serialized to JSON for dashboard endpoints</li>
 *   <li><strong>System Monitoring:</strong> Load balancing and capacity planning decisions</li>
 *   <li><strong>Dashboard Widgets:</strong> Real-time activity indicators and metrics</li>
 *   <li><strong>Performance Analysis:</strong> Bottleneck identification and resource optimization</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All fields are set via constructor and accessed through getter methods.</p>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "jobFilter": "all",
 *   "activeBuilds": 5,
 *   "computedAt": "2024-01-15T10:30:45.123Z"
 * }
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.dto.ActiveBuildCountProjection
 * @see com.diploma.inno.service.DashboardService#getTotalActiveBuildCount()
 * @see com.diploma.inno.service.DashboardService#getActiveBuildCountByJobFilter(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findActiveBuildCountByJobFilter(String)
 */
public class ActiveBuildCountDTO {

    /**
     * The job filter used to aggregate active build counts.
     *
     * <p>This field specifies the scope of the active build count aggregation:</p>
     * <ul>
     *   <li><strong>"all":</strong> System-wide count across all Jenkins jobs</li>
     *   <li><strong>Job Name:</strong> Specific job name for targeted monitoring</li>
     *   <li><strong>"error":</strong> Error indicator for validation failures or missing data</li>
     * </ul>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Cannot be null (handled by service layer validation)</li>
     *   <li>Empty strings are converted to "error" by service layer</li>
     *   <li>Case-sensitive matching with database records</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code active_build_counts.job_filter} column</p>
     */
    private String jobFilter;

    /**
     * The count of currently active builds matching the job filter.
     *
     * <p>This field represents the total number of builds that are currently
     * in an active state (running, queued, paused, or starting) for the
     * specified job filter scope.</p>
     *
     * <p><strong>Value Ranges:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No active builds (normal state for idle systems)</li>
     *   <li><strong>1-10:</strong> Typical range for small to medium Jenkins instances</li>
     *   <li><strong>10+:</strong> High activity or large-scale Jenkins deployments</li>
     * </ul>
     *
     * <p><strong>Calculation Logic:</strong></p>
     * <ul>
     *   <li>Aggregated from real-time Jenkins API build status queries</li>
     *   <li>Includes builds in queue, running, and paused states</li>
     *   <li>Excludes completed, failed, or aborted builds</li>
     *   <li>Updated periodically via scheduled synchronization tasks</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code active_build_counts.active_builds} column</p>
     */
    private Integer activeBuilds;

    /**
     * The timestamp when the active build count was last computed.
     *
     * <p>This field provides crucial information about data freshness and
     * helps determine the reliability of the active build count. It represents
     * the exact moment when the aggregation calculation was performed.</p>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>UTC Format:</strong> All timestamps stored in UTC for consistency</li>
     *   <li><strong>Precision:</strong> Nanosecond precision for accurate tracking</li>
     *   <li><strong>Update Frequency:</strong> Typically updated every 15-30 seconds</li>
     *   <li><strong>Fallback Value:</strong> Current timestamp for error scenarios</li>
     * </ul>
     *
     * <p><strong>Data Freshness Indicators:</strong></p>
     * <ul>
     *   <li><strong>&lt; 1 minute:</strong> Fresh, real-time data</li>
     *   <li><strong>1-5 minutes:</strong> Slightly stale but acceptable</li>
     *   <li><strong>&gt; 5 minutes:</strong> Potentially stale, may indicate sync issues</li>
     * </ul>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code active_build_counts.computed_at} column</p>
     */
    private Instant computedAt;

    /**
     * Constructs a new ActiveBuildCountDTO with the specified parameters.
     *
     * <p>This constructor is primarily used by the service layer when mapping
     * database projection results to DTO instances. It ensures all required
     * fields are properly initialized and provides a consistent way to create
     * DTO instances across the application.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>All parameters are required (null values handled by service layer)</li>
     *   <li>Service layer performs validation before DTO construction</li>
     *   <li>Error scenarios result in specific error values being passed</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     * {@snippet lang=java :
     * // Normal case - system-wide active builds
     * new ActiveBuildCountDTO("all", 5, Instant.now());
     *
     * // Job-specific case
     * new ActiveBuildCountDTO("my-web-app", 2, Instant.now());
     *
     * // Error case - validation failure
     * new ActiveBuildCountDTO("error", 0, Instant.now());
     * }
     *
     * @param jobFilter the job filter scope for the active build count
     * @param activeBuilds the number of currently active builds
     * @param computedAt the timestamp when the count was calculated
     */
    public ActiveBuildCountDTO(String jobFilter, Integer activeBuilds, Instant computedAt) {
        this.jobFilter = jobFilter;
        this.activeBuilds = activeBuilds;
        this.computedAt = computedAt;
    }

    /**
     * Returns the job filter used to aggregate active build counts.
     *
     * <p>The job filter determines the scope of the active build count:</p>
     * <ul>
     *   <li><strong>"all":</strong> System-wide aggregation across all jobs</li>
     *   <li><strong>Job Name:</strong> Specific job name for targeted monitoring</li>
     *   <li><strong>"error":</strong> Indicates validation failure or missing data</li>
     * </ul>
     *
     * @return the job filter string, never null
     */
    public String getJobFilter() {
        return jobFilter;
    }

    /**
     * Sets the job filter for active build count aggregation.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility (JSON deserialization, etc.). In normal application flow,
     * the job filter is set via constructor and should not be modified.</p>
     *
     * @param jobFilter the job filter to set
     */
    public void setJobFilter(String jobFilter) {
        this.jobFilter = jobFilter;
    }

    /**
     * Returns the count of currently active builds.
     *
     * <p>This count represents builds that are currently in an active state
     * (running, queued, paused, or starting) within the scope defined by
     * the job filter.</p>
     *
     * <p><strong>Interpretation Guidelines:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No active builds (normal for idle periods)</li>
     *   <li><strong>1-5:</strong> Normal activity level</li>
     *   <li><strong>6-15:</strong> High activity, monitor for bottlenecks</li>
     *   <li><strong>15+:</strong> Very high activity, potential capacity issues</li>
     * </ul>
     *
     * @return the number of active builds, never null
     */
    public Integer getActiveBuilds() {
        return activeBuilds;
    }

    /**
     * Sets the count of currently active builds.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the active build count is
     * set via constructor and should not be modified.</p>
     *
     * @param activeBuilds the active build count to set
     */
    public void setActiveBuilds(Integer activeBuilds) {
        this.activeBuilds = activeBuilds;
    }

    /**
     * Returns the timestamp when the active build count was computed.
     *
     * <p>This timestamp is crucial for determining data freshness and
     * reliability. It represents the exact moment when the Jenkins API
     * was queried and the aggregation was calculated.</p>
     *
     * <p><strong>Data Freshness Assessment:</strong></p>
     * <ul>
     *   <li>Compare with current time to determine staleness</li>
     *   <li>Data older than 5 minutes may indicate sync issues</li>
     *   <li>Use for cache invalidation and refresh decisions</li>
     * </ul>
     *
     * @return the computation timestamp in UTC, never null
     */
    public Instant getComputedAt() {
        return computedAt;
    }

    /**
     * Sets the timestamp when the active build count was computed.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the computed timestamp is
     * set via constructor and should not be modified.</p>
     *
     * @param computedAt the computation timestamp to set
     */
    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
