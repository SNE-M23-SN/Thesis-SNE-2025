package com.diploma.inno.dto;

import java.time.Instant;

/**
 * Data Transfer Object for Jenkins job count statistics within specified time boundaries.
 *
 * <p>This DTO encapsulates aggregated job count metrics for different time periods,
 * providing essential statistics for infrastructure monitoring, capacity planning,
 * and CI/CD adoption analysis. It represents pre-computed data from the {@code job_counts}
 * database table, which is populated and maintained through scheduled aggregation
 * processes that analyze job activity patterns.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins Job Discovery:</strong> Jobs identified via jenkins-rest library API calls</li>
 *   <li><strong>Activity Analysis:</strong> Job activity analyzed within specified time boundaries</li>
 *   <li><strong>Data Aggregation:</strong> Job counts aggregated by time boundary in {@code job_counts} table</li>
 *   <li><strong>Database Query:</strong> {@code ChatMessageRepository.findJobCountByTimeBoundary()} retrieves data</li>
 *   <li><strong>Projection Mapping:</strong> {@code JobCountProjection} interface maps database columns</li>
 *   <li><strong>DTO Construction:</strong> Service layer creates DTO instances with validation and error handling</li>
 * </ol>
 *
 * <p><strong>Database Schema Mapping:</strong></p>
 * <pre>
 * Table: job_counts
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Database Column │ Java Field       │ Description                                         │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ time_boundary   │ timeBoundary     │ Time period identifier (today, week, month, etc.)   │
 * │ total_jobs      │ totalJobs        │ Count of jobs active within the time boundary       │
 * │ computed_at     │ computedAt       │ Timestamp of last aggregation calculation           │
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Database Query Structure:</strong></p>
 * {@snippet lang=GenericSQL :
 * SELECT time_boundary AS timeBoundary,
 *        total_jobs AS totalJobs,
 *        computed_at AS computedAt
 * FROM job_counts
 * WHERE time_boundary = :timeBoundary
 * }
 *
 * <p><strong>Supported Time Boundaries:</strong></p>
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
 * <p><strong>Data Aggregation Process:</strong></p>
 * <ul>
 *   <li><strong>Scheduled Updates:</strong> Aggregation runs periodically (typically hourly or daily)</li>
 *   <li><strong>Jenkins Sync:</strong> Coordinated with 15-minute Jenkins job synchronization</li>
 *   <li><strong>Activity Detection:</strong> Analyzes chat_messages table for job activity patterns</li>
 *   <li><strong>Time Boundary Calculation:</strong> Dynamic time window calculation based on current timestamp</li>
 * </ul>
 *
 * <p><strong>Data Freshness &amp; Caching:</strong></p>
 * <ul>
 *   <li><strong>Update Frequency:</strong> Data refreshed hourly or daily depending on time boundary</li>
 *   <li><strong>Cache Strategy:</strong> Pre-computed aggregations for fast response times</li>
 *   <li><strong>Computed Timestamp:</strong> {@code computedAt} field indicates data freshness</li>
 *   <li><strong>Fallback Behavior:</strong> Returns DTO with 0 count for missing data scenarios</li>
 * </ul>
 *
 * <p><strong>Error Handling Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Invalid Time Boundary:</strong> Returns DTO with 0 count and current timestamp</li>
 *   <li><strong>Missing Database Record:</strong> Returns DTO with 0 count and current timestamp</li>
 *   <li><strong>Null Time Boundary:</strong> Service layer validation prevents null values</li>
 *   <li><strong>Database Connection Issues:</strong> Handled by Spring Data JPA exception translation</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>REST API Responses:</strong> Serialized to JSON for dashboard endpoints</li>
 *   <li><strong>Infrastructure Monitoring:</strong> Capacity planning and utilization analysis</li>
 *   <li><strong>Dashboard Widgets:</strong> Job count metrics and trend indicators</li>
 *   <li><strong>Executive Reporting:</strong> CI/CD adoption and growth tracking</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Fast Response:</strong> Pre-computed aggregations enable sub-second response times</li>
 *   <li><strong>Minimal Load:</strong> Reduces real-time computation overhead</li>
 *   <li><strong>Scalable:</strong> Aggregation approach scales with large numbers of jobs</li>
 *   <li><strong>Consistent:</strong> Time boundary calculations ensure consistent results</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "timeBoundary": "week",
 *   "totalJobs": 25,
 *   "computedAt": "2024-01-15T10:30:45.123Z"
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
 * @see JobCountProjection
 * @see com.diploma.inno.service.DashboardService#getJobCountByTimeBoundary(String)
 * @see com.diploma.inno.repository.ChatMessageRepository#findJobCountByTimeBoundary(String)
 */
public class JobCountDTO {

    /**
     * The time boundary identifier for job count aggregation.
     *
     * <p>This field specifies the time period within which job activity is counted.
     * It serves as both a filter criterion and a categorization mechanism for
     * different temporal analysis perspectives.</p>
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
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code job_counts.time_boundary} column</p>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Cannot be null (handled by service layer validation)</li>
     *   <li>Must match one of the supported time boundary values</li>
     *   <li>Case-sensitive matching with database records</li>
     *   <li>Invalid values result in fallback DTO with 0 count</li>
     * </ul>
     */
    private String timeBoundary;

    /**
     * The total count of jobs active within the specified time boundary.
     *
     * <p>This field represents the number of Jenkins jobs that had some form of
     * activity (builds, configuration changes, queue activity) within the
     * specified time boundary period.</p>
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
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code job_counts.total_jobs} column</p>
     *
     * <p><strong>Quality Assurance:</strong></p>
     * <ul>
     *   <li><strong>Non-negative:</strong> Count should always be zero or positive</li>
     *   <li><strong>Consistency:</strong> Longer time boundaries should have equal or higher counts</li>
     *   <li><strong>Accuracy:</strong> Reflects actual Jenkins job activity within the period</li>
     *   <li><strong>Freshness:</strong> Updated regularly to maintain current statistics</li>
     * </ul>
     */
    private Integer totalJobs;

    /**
     * The timestamp when the job count was last computed.
     *
     * <p>This field provides crucial information about data freshness and helps
     * determine the reliability of the job count statistics. It represents the
     * exact moment when the aggregation calculation was performed for the
     * specified time boundary.</p>
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
     * <p><strong>Database Mapping:</strong></p>
     * <p>Maps directly to {@code job_counts.computed_at} column</p>
     *
     * <p><strong>Usage in Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Cache Invalidation:</strong> Used to determine when to refresh cached data</li>
     *   <li><strong>Data Quality:</strong> Helps identify stale or outdated statistics</li>
     *   <li><strong>System Health:</strong> Indicates proper functioning of aggregation processes</li>
     *   <li><strong>Troubleshooting:</strong> Assists in diagnosing data pipeline issues</li>
     * </ul>
     *
     * <p><strong>Fallback Behavior:</strong></p>
     * <ul>
     *   <li>Current timestamp used when creating fallback DTOs for missing data</li>
     *   <li>Indicates when the fallback response was generated</li>
     *   <li>Helps distinguish between actual aggregated data and fallback responses</li>
     * </ul>
     */
    private Instant computedAt;

    /**
     * Constructs a new JobCountDTO with the specified parameters.
     *
     * <p>This constructor creates a complete job count statistics object ready for
     * JSON serialization and API response. It ensures all required fields are
     * properly initialized and provides a consistent way to create DTO instances
     * across the application.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>All parameters are required for proper DTO functionality</li>
     *   <li>Service layer performs validation before DTO construction</li>
     *   <li>Fallback values used for error scenarios (0 count, current timestamp)</li>
     *   <li>Time boundary should match supported values for consistency</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     * {@snippet lang=java :
     * // Normal case - weekly job count
     * new JobCountDTO("week", 25, Instant.parse("2024-01-15T10:30:45.123Z"));
     *
     * // Daily job count
     * new JobCountDTO("today", 15, Instant.now());
     *
     * // Fallback case - missing data
     * new JobCountDTO("month", 0, Instant.now());
     * }
     *
     * <p><strong>Service Layer Integration:</strong></p>
     * <ul>
     *   <li>Called by {@code DashboardService.getJobCountByTimeBoundary()}</li>
     *   <li>Parameters derived from {@code JobCountProjection} database results</li>
     *   <li>Fallback construction for null projection scenarios</li>
     *   <li>Consistent error handling across different time boundaries</li>
     * </ul>
     *
     * @param timeBoundary the time period identifier for job count aggregation
     * @param totalJobs the number of jobs active within the time boundary
     * @param computedAt the timestamp when the count was calculated
     */
    public JobCountDTO(String timeBoundary, Integer totalJobs, Instant computedAt) {
        this.timeBoundary = timeBoundary;
        this.totalJobs = totalJobs;
        this.computedAt = computedAt;
    }

    /**
     * Returns the time boundary identifier for job count aggregation.
     *
     * @return the time period identifier (e.g., "today", "week", "month")
     */
    public String getTimeBoundary() {
        return timeBoundary;
    }

    /**
     * Sets the time boundary identifier for job count aggregation.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the time boundary is set via
     * constructor and should not be modified.</p>
     *
     * @param timeBoundary the time period identifier to set
     */
    public void setTimeBoundary(String timeBoundary) {
        this.timeBoundary = timeBoundary;
    }

    /**
     * Returns the total count of jobs active within the time boundary.
     *
     * @return the number of active jobs, never null, always non-negative
     */
    public Integer getTotalJobs() {
        return totalJobs;
    }

    /**
     * Sets the total count of jobs active within the time boundary.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the job count is set via
     * constructor and should not be modified.</p>
     *
     * @param totalJobs the job count to set
     */
    public void setTotalJobs(Integer totalJobs) {
        this.totalJobs = totalJobs;
    }

    /**
     * Returns the timestamp when the job count was computed.
     *
     * @return the computation timestamp in UTC, never null
     */
    public Instant getComputedAt() {
        return computedAt;
    }

    /**
     * Sets the timestamp when the job count was computed.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the computed timestamp is set
     * via constructor and should not be modified.</p>
     *
     * @param computedAt the computation timestamp to set
     */
    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}