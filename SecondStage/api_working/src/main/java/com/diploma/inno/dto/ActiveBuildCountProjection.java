package com.diploma.inno.dto;


import java.time.Instant;

/**
 * Spring Data JPA projection interface for active build count data from the CI Anomaly Detector system.
 *
 * <p>This projection interface provides a type-safe, performance-optimized way to retrieve
 * active build count statistics from the {@code active_build_counts} database table. It serves
 * as an intermediate data mapping layer between the database and the {@link ActiveBuildCountDTO},
 * enabling efficient data transfer with minimal memory overhead.</p>
 *
 * <p><strong>Database Query Integration:</strong></p>
 * <p>This projection is used by the following repository method:</p>
 * {@snippet lang=java :
 * @Query(value = "SELECT job_filter AS jobFilter, " +
 *                "       active_builds AS activeBuilds, " +
 *                "       computed_at AS computedAt " +
 *                "FROM active_build_counts " +
 *                "WHERE job_filter = :jobFilter",
 *                nativeQuery = true)
 * ActiveBuildCountProjection findActiveBuildCountByJobFilter(@Param("jobFilter") String jobFilter);
 * }
 *
 * <p><strong>Data Source &amp; Table Structure:</strong></p>
 * <pre>
 * Table: active_build_counts
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Database Column │ Projection Method│ Description                                         │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ job_filter      │ getJobFilter()   │ Job name or "all" for system-wide aggregation       │
 * │ active_builds   │ getActiveBuilds()│ Count of currently running/queued builds            │
 * │ computed_at     │ getComputedAt()  │ Timestamp when the count was last calculated        │
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Data Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins API Integration:</strong> Real-time build status fetched via jenkins-rest library</li>
 *   <li><strong>Data Aggregation:</strong> Build counts aggregated by job filter and stored in database table</li>
 *   <li><strong>Database Query:</strong> Native SQL query retrieves data with column aliasing</li>
 *   <li><strong>Projection Mapping:</strong> Spring Data JPA maps columns to interface methods</li>
 *   <li><strong>Service Layer Processing:</strong> DashboardService converts projection to DTO</li>
 * </ol>
 *
 * <p><strong>Active Build Definition:</strong></p>
 * <p>The active build count includes builds in the following states:</p>
 * <ul>
 *   <li><strong>Currently Executing:</strong> Builds actively running build steps</li>
 *   <li><strong>Queue Waiting:</strong> Builds waiting in the Jenkins build queue</li>
 *   <li><strong>Paused/Manual:</strong> Builds paused for manual intervention or approval</li>
 *   <li><strong>Starting Up:</strong> Builds that are initializing or starting up</li>
 * </ul>
 *
 * <p><strong>Job Filter Patterns:</strong></p>
 * <ul>
 *   <li><strong>"all":</strong> System-wide count across all Jenkins jobs</li>
 *   <li><strong>Specific Job Name:</strong> Count for a single job (e.g., "my-web-app")</li>
 *   <li><strong>Pattern Matching:</strong> May support wildcards or regex (implementation dependent)</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Memory Efficient:</strong> Interface-based projection minimizes object creation</li>
 *   <li><strong>Type Safe:</strong> Compile-time type checking for database column mapping</li>
 *   <li><strong>Query Optimized:</strong> Only retrieves required columns from database</li>
 *   <li><strong>Caching Friendly:</strong> Immutable data suitable for caching strategies</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Synchronization:</strong></p>
 * <ul>
 *   <li><strong>Update Frequency:</strong> Data refreshed every 15-30 seconds via scheduled tasks</li>
 *   <li><strong>Jenkins Sync:</strong> Synchronized with Jenkins server every 15 minutes</li>
 *   <li><strong>Computed Timestamp:</strong> Indicates when the aggregation was last calculated</li>
 *   <li><strong>Cache Strategy:</strong> Balance between real-time accuracy and system performance</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>Service Layer:</strong> {@code DashboardService.getTotalActiveBuildCount()}</li>
 *   <li><strong>Service Layer:</strong> {@code DashboardService.getActiveBuildCountByJobFilter(String)}</li>
 *   <li><strong>Repository Layer:</strong> {@code ChatMessageRepository.findActiveBuildCountByJobFilter(String)}</li>
 *   <li><strong>REST API:</strong> Converted to {@code ActiveBuildCountDTO} for JSON serialization</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li><strong>Null Results:</strong> Repository method returns null when no matching records found</li>
 *   <li><strong>Service Validation:</strong> Service layer handles null projections with fallback DTOs</li>
 *   <li><strong>Parameter Validation:</strong> Invalid job filters result in error DTOs</li>
 *   <li><strong>Database Errors:</strong> Handled by Spring Data JPA exception translation</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This projection interface is thread-safe as it represents immutable data retrieved
 * from the database. Multiple threads can safely access projection instances concurrently.</p>
 *
 * <p><strong>Example Usage:</strong></p>
 *
 *
 * <span>Repository layer</span>
 * {@snippet lang=java :
 * ActiveBuildCountProjection projection = repository.findActiveBuildCountByJobFilter("all");
 * }
 *
 * <span>Service layer conversion</span>
 * {@snippet lang=java :
 * if (projection != null) {
 *     ActiveBuildCountDTO dto = new ActiveBuildCountDTO(
 *         projection.getJobFilter(),
 *         projection.getActiveBuilds(),
 *         projection.getComputedAt()
 *     );
 * }
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see ActiveBuildCountDTO
 * @see com.diploma.inno.service.DashboardService#getTotalActiveBuildCount()
 * @see com.diploma.inno.service.DashboardService#getActiveBuildCountByJobFilter(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findActiveBuildCountByJobFilter(String)
 */
public interface ActiveBuildCountProjection {

    /**
     * Returns the job filter used to aggregate active build counts.
     *
     * <p>This method retrieves the job filter scope that was used to calculate
     * the active build count. The filter determines which Jenkins jobs are
     * included in the aggregation.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code active_build_counts.job_filter} column via SQL alias {@code jobFilter}</p>
     *
     * <p><strong>Possible Values:</strong></p>
     * <ul>
     *   <li><strong>"all":</strong> System-wide count across all Jenkins jobs</li>
     *   <li><strong>Job Name:</strong> Specific job name for targeted monitoring (e.g., "my-web-app")</li>
     *   <li><strong>Pattern:</strong> Job name pattern or filter expression</li>
     * </ul>
     *
     * <p><strong>Data Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Preserves original case from database</li>
     *   <li><strong>Null Safety:</strong> Should never be null for valid database records</li>
     *   <li><strong>Trimming:</strong> Database values are typically trimmed of whitespace</li>
     * </ul>
     *
     * @return the job filter string used for aggregation, never null for valid records
     */
    String getJobFilter();

    /**
     * Returns the count of currently active builds matching the job filter.
     *
     * <p>This method retrieves the total number of builds that are currently
     * in an active state (running, queued, paused, or starting) within the
     * scope defined by the job filter.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code active_build_counts.active_builds} column via SQL alias {@code activeBuilds}</p>
     *
     * <p><strong>Value Interpretation:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No active builds (normal state for idle systems)</li>
     *   <li><strong>1-5:</strong> Normal activity level for small to medium Jenkins instances</li>
     *   <li><strong>6-15:</strong> High activity, monitor for potential bottlenecks</li>
     *   <li><strong>15+:</strong> Very high activity, potential capacity or performance issues</li>
     * </ul>
     *
     * <p><strong>Calculation Logic:</strong></p>
     * <ul>
     *   <li><strong>Real-time Data:</strong> Aggregated from Jenkins API build status queries</li>
     *   <li><strong>Included States:</strong> Running, queued, paused, and starting builds</li>
     *   <li><strong>Excluded States:</strong> Completed, failed, aborted, or cancelled builds</li>
     *   <li><strong>Update Frequency:</strong> Refreshed periodically via scheduled synchronization</li>
     * </ul>
     *
     * <p><strong>Data Quality:</strong></p>
     * <ul>
     *   <li><strong>Non-negative:</strong> Count should always be zero or positive</li>
     *   <li><strong>Consistency:</strong> Reflects Jenkins server state at computation time</li>
     *   <li><strong>Accuracy:</strong> Depends on Jenkins API availability and sync frequency</li>
     * </ul>
     *
     * @return the number of active builds, never null, always non-negative
     */
    Integer getActiveBuilds();

    /**
     * Returns the timestamp when the active build count was last computed.
     *
     * <p>This method retrieves the exact timestamp when the active build count
     * aggregation was calculated and stored in the database. This timestamp is
     * crucial for determining data freshness and reliability.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code active_build_counts.computed_at} column via SQL alias {@code computedAt}</p>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>UTC Format:</strong> All timestamps stored in UTC for consistency across time zones</li>
     *   <li><strong>Precision:</strong> Nanosecond precision for accurate tracking and ordering</li>
     *   <li><strong>Monotonic:</strong> Newer computations have later timestamps</li>
     *   <li><strong>Immutable:</strong> Timestamp represents historical computation time</li>
     * </ul>
     *
     * <p><strong>Data Freshness Assessment:</strong></p>
     * <ul>
     *   <li><strong>&lt; 1 minute:</strong> Fresh, real-time data suitable for immediate use</li>
     *   <li><strong>1-5 minutes:</strong> Slightly stale but generally acceptable for dashboards</li>
     *   <li><strong>5-15 minutes:</strong> Moderately stale, may indicate sync delays</li>
     *   <li><strong>&gt; 15 minutes:</strong> Potentially stale, may indicate system issues</li>
     * </ul>
     *
     * <p><strong>Synchronization Context:</strong></p>
     * <ul>
     *   <li><strong>Scheduled Updates:</strong> Typically updated every 15-30 seconds</li>
     *   <li><strong>Jenkins Sync:</strong> Coordinated with 15-minute Jenkins job synchronization</li>
     *   <li><strong>Error Recovery:</strong> May have older timestamps during Jenkins API failures</li>
     *   <li><strong>Cache Invalidation:</strong> Used to determine when to refresh cached data</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     * {@snippet lang=java :
     * // Check data freshness
     * Instant now = Instant.now();
     * Duration age = Duration.between(projection.getComputedAt(), now);
     * boolean isFresh = age.toMinutes() < 5;
     *
     * // Cache invalidation logic
     * if (age.toSeconds() > 30) {
     *     // Trigger refresh
     * }
     * }
     *
     * @return the computation timestamp in UTC, never null for valid records
     */
    Instant getComputedAt();
}
