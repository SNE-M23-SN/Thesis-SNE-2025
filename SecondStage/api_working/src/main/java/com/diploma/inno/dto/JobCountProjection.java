package com.diploma.inno.dto;

import java.time.Instant;

/**
 * Spring Data JPA projection interface for job count statistics from the CI Anomaly Detector system.
 *
 * <p>This projection interface provides a type-safe, performance-optimized way to retrieve
 * aggregated job count statistics from the {@code job_counts} database table. It serves
 * as an intermediate data mapping layer between the database and the {@link JobCountDTO},
 * enabling efficient data transfer with minimal memory overhead for time-bounded job analytics.</p>
 *
 * <p><strong>Database Query Integration:</strong></p>
 * <p>This projection is used by the following repository method:</p>
 * {@snippet lang=java :
 * @Query(value = "SELECT time_boundary AS timeBoundary, " +
 *                "       total_jobs AS totalJobs, " +
 *                "       computed_at AS computedAt " +
 *                "FROM job_counts " +
 *                "WHERE time_boundary = :timeBoundary",
 *                nativeQuery = true)
 * JobCountProjection findJobCountByTimeBoundary(@Param("timeBoundary") String timeBoundary);
 * }
 *
 * <p><strong>Data Source &amp; Table Structure:</strong></p>
 * <pre>
 * Table: job_counts
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Database Column │ Projection Method│ Description                                         │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ time_boundary   │ getTimeBoundary()│ Time period identifier (today, week, month, etc.)   │
 * │ total_jobs      │ getTotalJobs()   │ Count of jobs active within the time boundary       │
 * │ computed_at     │ getComputedAt()  │ Timestamp when the count was last calculated        │
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Data Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins Job Discovery:</strong> Jobs identified via jenkins-rest library API calls</li>
 *   <li><strong>Activity Analysis:</strong> Job activity patterns analyzed from chat_messages table</li>
 *   <li><strong>Time Boundary Calculation:</strong> Dynamic time windows calculated relative to current timestamp</li>
 *   <li><strong>Data Aggregation:</strong> Job counts aggregated by time boundary and stored in job_counts table</li>
 *   <li><strong>Scheduled Updates:</strong> Periodic refresh coordinated with 15-minute Jenkins synchronization</li>
 *   <li><strong>Projection Mapping:</strong> Type-safe data retrieval via Spring Data JPA projection</li>
 * </ol>
 *
 * <p><strong>Time Boundary Categories:</strong></p>
 * <ul>
 *   <li><strong>"today":</strong> Jobs with activity in the last 24 hours</li>
 *   <li><strong>"week":</strong> Jobs with activity in the last 7 days</li>
 *   <li><strong>"month":</strong> Jobs with activity in the last 30 days</li>
 *   <li><strong>"quarter":</strong> Jobs with activity in the last 90 days</li>
 *   <li><strong>"year":</strong> Jobs with activity in the last 365 days</li>
 *   <li><strong>"all":</strong> All jobs regardless of activity period</li>
 * </ul>
 *
 * <p><strong>Job Activity Definition:</strong></p>
 * <p>A job is considered "active" within a time boundary if it has:</p>
 * <ul>
 *   <li><strong>Build Execution:</strong> At least one build started within the time period</li>
 *   <li><strong>Configuration Changes:</strong> Job configuration modified within the period</li>
 *   <li><strong>Queue Activity:</strong> Builds queued or scheduled within the period</li>
 *   <li><strong>Manual Triggers:</strong> Manual build triggers or interventions</li>
 * </ul>
 *
 * <p><strong>Aggregation Logic:</strong></p>
 * <ul>
 *   <li><strong>Source Analysis:</strong> Analyzes chat_messages table for job activity patterns</li>
 *   <li><strong>DISTINCT Counting:</strong> Each job counted only once regardless of activity frequency</li>
 *   <li><strong>Time Window Filtering:</strong> Activity must occur within the specified time boundary</li>
 *   <li><strong>Jenkins Integration:</strong> Coordinated with real-time job discovery via Jenkins API</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Memory Efficient:</strong> Interface-based projection minimizes object creation</li>
 *   <li><strong>Type Safe:</strong> Compile-time type checking for database column mapping</li>
 *   <li><strong>Fast Retrieval:</strong> Pre-computed aggregations enable sub-second response times</li>
 *   <li><strong>Minimal Overhead:</strong> Direct column mapping without intermediate object creation</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Caching:</strong></p>
 * <ul>
 *   <li><strong>Update Frequency:</strong> Data refreshed hourly or daily depending on time boundary</li>
 *   <li><strong>Cache Strategy:</strong> Pre-computed aggregations stored in job_counts table</li>
 *   <li><strong>Computed Timestamp:</strong> {@code computed_at} field indicates data freshness</li>
 *   <li><strong>Scheduled Refresh:</strong> Coordinated with Jenkins synchronization processes</li>
 * </ul>
 *
 * <p><strong>Usage Flow:</strong></p>
 * <ul>
 *   <li><strong>Controller Layer:</strong> {@code DashboardController.getTotalJobs(String)}</li>
 *   <li><strong>Service Layer:</strong> {@code DashboardService.getJobCountByTimeBoundary(String)}</li>
 *   <li><strong>Repository Layer:</strong> {@code ChatMessageRepository.findJobCountByTimeBoundary(String)}</li>
 *   <li><strong>REST API:</strong> Converted to {@code JobCountDTO} for JSON serialization</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li><strong>Null Results:</strong> Repository method returns null when no matching records found</li>
 *   <li><strong>Service Validation:</strong> Service layer handles null projections with fallback DTOs</li>
 *   <li><strong>Parameter Validation:</strong> Invalid time boundaries result in fallback responses</li>
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
 * JobCountProjection projection = repository.findJobCountByTimeBoundary("week");
 *
 * // Service layer conversion
 * if (projection != null) {
 *     JobCountDTO dto = new JobCountDTO(
 *         projection.getTimeBoundary(),
 *         projection.getTotalJobs(),
 *         projection.getComputedAt()
 *     );
 * }
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see JobCountDTO
 * @see com.diploma.inno.service.DashboardService#getJobCountByTimeBoundary(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findJobCountByTimeBoundary(String)
 */
public interface JobCountProjection {

    /**
     * Returns the time boundary identifier for job count aggregation.
     *
     * <p>This method retrieves the time period identifier that was used to calculate
     * the job count statistics. The time boundary determines the temporal scope
     * within which job activity is analyzed and counted.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code job_counts.time_boundary} column via SQL alias {@code timeBoundary}</p>
     *
     * <p><strong>Supported Values:</strong></p>
     * <ul>
     *   <li><strong>"today":</strong> Jobs active in the last 24 hours</li>
     *   <li><strong>"week":</strong> Jobs active in the last 7 days</li>
     *   <li><strong>"month":</strong> Jobs active in the last 30 days</li>
     *   <li><strong>"quarter":</strong> Jobs active in the last 90 days</li>
     *   <li><strong>"year":</strong> Jobs active in the last 365 days</li>
     *   <li><strong>"all":</strong> All jobs regardless of activity period</li>
     * </ul>
     *
     * <p><strong>Time Boundary Calculation:</strong></p>
     * <ul>
     *   <li><strong>Dynamic Windows:</strong> Time boundaries calculated relative to current timestamp</li>
     *   <li><strong>Inclusive Periods:</strong> Boundaries include the start and end of the period</li>
     *   <li><strong>UTC Consistency:</strong> All time calculations performed in UTC</li>
     *   <li><strong>Standardized Periods:</strong> Consistent period definitions across the system</li>
     * </ul>
     *
     * <p><strong>Data Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Preserves original case from database</li>
     *   <li><strong>Null Safety:</strong> Should never be null for valid database records</li>
     *   <li><strong>Trimming:</strong> Database values are typically trimmed of whitespace</li>
     *   <li><strong>Validation:</strong> Values should match supported time boundary identifiers</li>
     * </ul>
     *
     * @return the time boundary identifier used for aggregation, never null for valid records
     */
    String getTimeBoundary();

    /**
     * Returns the total count of jobs active within the time boundary.
     *
     * <p>This method retrieves the number of Jenkins jobs that had some form of
     * activity (builds, configuration changes, queue activity) within the
     * specified time boundary period.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code job_counts.total_jobs} column via SQL alias {@code totalJobs}</p>
     *
     * <p><strong>Count Calculation Logic:</strong></p>
     * <ul>
     *   <li><strong>Activity Detection:</strong> Jobs with builds, configuration changes, or queue activity</li>
     *   <li><strong>Unique Jobs:</strong> Each job counted only once regardless of activity frequency</li>
     *   <li><strong>Time Window:</strong> Activity must occur within the specified time boundary</li>
     *   <li><strong>Active Status:</strong> Includes both enabled and disabled jobs with activity</li>
     * </ul>
     *
     * <p><strong>Data Source Analysis:</strong></p>
     * <ul>
     *   <li><strong>Primary Source:</strong> chat_messages table for build activity tracking</li>
     *   <li><strong>Jenkins Integration:</strong> Real-time job discovery via Jenkins API</li>
     *   <li><strong>Activity Patterns:</strong> Analysis of message timestamps and job names</li>
     *   <li><strong>Aggregation Logic:</strong> DISTINCT job counting within time boundaries</li>
     * </ul>
     *
     * <p><strong>Value Interpretation:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No jobs active in the time period (rare for "all" boundary)</li>
     *   <li><strong>1-10:</strong> Small Jenkins instance or specific time period</li>
     *   <li><strong>10-50:</strong> Medium-sized Jenkins deployment</li>
     *   <li><strong>50+:</strong> Large-scale Jenkins environment with high activity</li>
     * </ul>
     *
     * <p><strong>Quality Assurance:</strong></p>
     * <ul>
     *   <li><strong>Non-negative:</strong> Count should always be zero or positive</li>
     *   <li><strong>Consistency:</strong> Longer time boundaries should have equal or higher counts</li>
     *   <li><strong>Accuracy:</strong> Reflects actual Jenkins job activity within the period</li>
     *   <li><strong>Freshness:</strong> Updated regularly to maintain current statistics</li>
     * </ul>
     *
     * @return the number of active jobs, never null, always non-negative
     */
    Integer getTotalJobs();

    /**
     * Returns the timestamp when the job count was computed.
     *
     * <p>This method retrieves the exact moment when the aggregation calculation
     * was performed for the specified time boundary. It provides crucial information
     * about data freshness and helps determine the reliability of the job count statistics.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps to {@code job_counts.computed_at} column via SQL alias {@code computedAt}</p>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>UTC Format:</strong> All timestamps stored in UTC for consistency across time zones</li>
     *   <li><strong>Precision:</strong> Nanosecond precision for accurate tracking and ordering</li>
     *   <li><strong>Aggregation Time:</strong> Represents when the count calculation was completed</li>
     *   <li><strong>Immutable:</strong> Timestamp represents historical computation time</li>
     * </ul>
     *
     * <p><strong>Data Freshness Assessment:</strong></p>
     * <ul>
     *   <li><strong>&lt; 1 hour:</strong> Fresh data suitable for real-time dashboards</li>
     *   <li><strong>1-6 hours:</strong> Acceptable for most monitoring and reporting purposes</li>
     *   <li><strong>6-24 hours:</strong> Moderately stale, may indicate aggregation delays</li>
     *   <li><strong>&gt; 24 hours:</strong> Potentially stale, may indicate system issues</li>
     * </ul>
     *
     * <p><strong>Aggregation Context:</strong></p>
     * <ul>
     *   <li><strong>Scheduled Updates:</strong> Typically updated hourly or daily based on time boundary</li>
     *   <li><strong>Jenkins Sync:</strong> Coordinated with 15-minute Jenkins job synchronization</li>
     *   <li><strong>Batch Processing:</strong> Part of larger aggregation batch for multiple time boundaries</li>
     *   <li><strong>Error Recovery:</strong> May have older timestamps during system maintenance</li>
     * </ul>
     *
     * <p><strong>Usage in Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Cache Invalidation:</strong> Used to determine when to refresh cached data</li>
     *   <li><strong>Data Quality:</strong> Helps identify stale or outdated statistics</li>
     *   <li><strong>System Health:</strong> Indicates proper functioning of aggregation processes</li>
     *   <li><strong>Troubleshooting:</strong> Assists in diagnosing data pipeline issues</li>
     * </ul>
     *
     * @return the computation timestamp in UTC, never null for valid records
     */
    Instant getComputedAt();
}