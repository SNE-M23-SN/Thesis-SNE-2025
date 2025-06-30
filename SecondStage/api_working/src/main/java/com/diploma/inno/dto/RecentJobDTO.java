package com.diploma.inno.dto;


/**
 * Data Transfer Object for recent Jenkins job information with caching support in the CI Anomaly Detector system.
 *
 * <p>This DTO represents a lightweight view of Jenkins job status information, designed for
 * high-frequency access patterns with built-in caching mechanisms. It provides essential
 * job metadata including current status, visual indicators, and temporal information
 * optimized for dashboard display and real-time monitoring.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins API:</strong> Data sourced from Jenkins REST API via jenkins-rest library</li>
 *   <li><strong>Job Discovery:</strong> {@code JenkinsClient.api().jobsApi().jobList(folderPath)} retrieves job list</li>
 *   <li><strong>Status Mapping:</strong> Jenkins BallColor enum mapped to Tailwind CSS classes</li>
 *   <li><strong>Progress Detection:</strong> Build status analysis determines if job is currently running</li>
 *   <li><strong>Timestamp Processing:</strong> Build timestamps converted to epoch milliseconds</li>
 *   <li><strong>DTO Construction:</strong> Service layer creates cached DTO instances</li>
 * </ol>
 *
 * <p><strong>Caching Architecture:</strong></p>
 * <p>This DTO is part of a sophisticated caching system in {@code JenkinsService}:</p>
 * {@snippet lang=java :
 * // Cache configuration
 * private List&lt;RecentJobDTO&gt; cachedJobs = null;
 * private long cacheTimestamp = 0;
 * private static final long CACHE_TTL_MS = 15000; // 15 seconds
 *
 * // Cache validation logic
 * if (cachedJobs != null && (System.currentTimeMillis() - cacheTimestamp) &lt; CACHE_TTL_MS) {
 *     return cachedJobs; // Return cached data
 * }
 * }
 *
 * <p><strong>Jenkins BallColor to CSS Mapping:</strong></p>
 * <p>Status colors are mapped from Jenkins BallColor enum to Tailwind CSS classes:</p>
 * {@snippet lang=TEXT :
 * STATUS_COLOR_MAP = {
 *   "RED"      → "bg-red-500"    (Failed builds)
 *   "YELLOW"   → "bg-yellow-500" (Unstable builds)
 *   "BLUE"     → "bg-green-500"  (Successful builds)
 *   "GREY"     → "bg-gray-500"   (Disabled/Never built)
 *   "DISABLED" → "bg-gray-500"   (Disabled jobs)
 *   "ABORTED"  → "bg-gray-700"   (Aborted builds)
 *   "NOTBUILT" → "bg-gray-300"   (Not yet built)
 * }
 * }
 *
 * <p><strong>Build Progress Detection:</strong></p>
 * <p>The {@code inProgress} field is determined by analyzing Jenkins job status:</p>
 * <ul>
 *   <li><strong>Running Builds:</strong> Jobs with "_ANIME" suffix in BallColor (e.g., "RED_ANIME")</li>
 *   <li><strong>Queue Status:</strong> Jobs waiting in build queue</li>
 *   <li><strong>Active Executors:</strong> Jobs currently being executed</li>
 *   <li><strong>Build History:</strong> Analysis of recent build completion status</li>
 * </ul>
 *
 * <p><strong>Timestamp Processing:</strong></p>
 * <ul>
 *   <li><strong>Source:</strong> Jenkins build timestamp from most recent build</li>
 *   <li><strong>Format:</strong> Epoch milliseconds (long) for efficient sorting and comparison</li>
 *   <li><strong>Precision:</strong> Millisecond precision for accurate chronological ordering</li>
 *   <li><strong>Time Zone:</strong> UTC-based timestamps for consistency across deployments</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>15-second Cache TTL:</strong> Balances data freshness with API call efficiency</li>
 *   <li><strong>Immutable Design:</strong> Thread-safe final fields prevent concurrent modification</li>
 *   <li><strong>Lightweight Structure:</strong> Minimal memory footprint for high-volume caching</li>
 *   <li><strong>Fast Serialization:</strong> Optimized for JSON serialization in REST responses</li>
 * </ul>
 *
 * <p><strong>Jenkins API Integration:</strong></p>
 * <ul>
 *   <li><strong>Job Discovery:</strong> {@code jenkinsClient.api().jobsApi().jobList("")} for job enumeration</li>
 *   <li><strong>Build Information:</strong> {@code jenkinsClient.api().jobsApi().buildInfo()} for latest build data</li>
 *   <li><strong>Queue Status:</strong> {@code jenkinsClient.api().queueApi()} for pending builds</li>
 *   <li><strong>Error Handling:</strong> Graceful degradation when Jenkins API is unavailable</li>
 * </ul>
 *
 * <p><strong>Dashboard Integration:</strong></p>
 * <ul>
 *   <li><strong>Job Cards:</strong> Visual representation with status-based color coding</li>
 *   <li><strong>Progress Indicators:</strong> Real-time build progress visualization</li>
 *   <li><strong>Status Badges:</strong> Color-coded status indicators using Tailwind CSS</li>
 *   <li><strong>Timestamp Display:</strong> Relative time formatting for user-friendly display</li>
 * </ul>
 *
 * <p><strong>Concurrent Access Patterns:</strong></p>
 * <ul>
 *   <li><strong>Cache Synchronization:</strong> Thread-safe cache access with timestamp validation</li>
 *   <li><strong>API Rate Limiting:</strong> Cache prevents excessive Jenkins API calls</li>
 *   <li><strong>Bulk Operations:</strong> Efficient batch processing of multiple jobs</li>
 *   <li><strong>Memory Management:</strong> Automatic cache invalidation prevents memory leaks</li>
 * </ul>
 *
 * <p><strong>Error Handling &amp; Resilience:</strong></p>
 * <ul>
 *   <li><strong>Jenkins Unavailable:</strong> Returns empty list when Jenkins API fails</li>
 *   <li><strong>Network Timeouts:</strong> Graceful handling of connection timeouts</li>
 *   <li><strong>Authentication Errors:</strong> Proper error logging for credential issues</li>
 *   <li><strong>Malformed Data:</strong> Defensive programming for unexpected API responses</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "name": "my-web-app",
 *   "status": "SUCCESS",
 *   "inProgress": false,
 *   "colorClass": "bg-green-500",
 *   "timestamp": 1705329045123
 * }
 * }
 *
 * <p><strong>Usage Patterns:</strong></p>
 * <ul>
 *   <li><strong>Dashboard Widgets:</strong> Real-time job status display</li>
 *   <li><strong>Monitoring Dashboards:</strong> High-level CI/CD pipeline health</li>
 *   <li><strong>Status Aggregation:</strong> Summary views of multiple job statuses</li>
 *   <li><strong>Alert Systems:</strong> Trigger notifications based on job status changes</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All fields are final and set via constructor, ensuring safe sharing across threads.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.service.JenkinsService
 * @see com.cdancy.jenkins.rest.JenkinsClient
 * @see com.cdancy.jenkins.rest.domain.job.Job
 */
public class RecentJobDTO {

    /**
     * The name of the Jenkins job.
     *
     * <p>This field contains the exact job name as it appears in Jenkins,
     * serving as the primary identifier for the job across the CI/CD system.
     * It is used for job identification, API calls, and user display purposes.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Retrieved from {@code Job.name()} via Jenkins REST API</p>
     *
     * <p><strong>Name Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Uniqueness:</strong> Unique identifier within Jenkins instance</li>
     *   <li><strong>Case Sensitivity:</strong> Preserves exact casing from Jenkins configuration</li>
     *   <li><strong>Special Characters:</strong> May contain spaces, hyphens, underscores</li>
     *   <li><strong>Immutable:</strong> Never changes once set via constructor</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>Job Identification:</strong> Primary key for job-specific operations</li>
     *   <li><strong>API Calls:</strong> Used in Jenkins API calls for job operations</li>
     *   <li><strong>Display:</strong> Shown in dashboard job cards and lists</li>
     *   <li><strong>Navigation:</strong> Links to job-specific pages and builds</li>
     * </ul>
     *
     * <p><strong>Validation:</strong></p>
     * <ul>
     *   <li><strong>Non-null:</strong> Should never be null for valid job instances</li>
     *   <li><strong>Non-empty:</strong> Should contain meaningful job identifier</li>
     *   <li><strong>Valid Characters:</strong> Must be valid Jenkins job name</li>
     * </ul>
     */
    private final String name;

    /**
     * The current status of the Jenkins job.
     *
     * <p>This field represents the outcome of the most recent build execution,
     * providing immediate insight into the job's current state. The status
     * is derived from Jenkins build results and mapped to standardized values.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Derived from Jenkins BallColor enum via {@code Job.color()} API</p>
     *
     * <p><strong>Status Values:</strong></p>
     * <ul>
     *   <li><strong>SUCCESS:</strong> Last build completed successfully (BLUE)</li>
     *   <li><strong>FAILURE:</strong> Last build failed (RED)</li>
     *   <li><strong>UNSTABLE:</strong> Last build was unstable (YELLOW)</li>
     *   <li><strong>ABORTED:</strong> Last build was aborted (ABORTED)</li>
     *   <li><strong>DISABLED:</strong> Job is disabled (DISABLED)</li>
     *   <li><strong>NOT_BUILT:</strong> Job has never been built (NOTBUILT)</li>
     *   <li><strong>UNKNOWN:</strong> Status could not be determined (GREY)</li>
     * </ul>
     *
     * <p><strong>Status Mapping Logic:</strong></p>
     * <p>Jenkins BallColor enum values are mapped to human-readable status strings:</p>
     * <ul>
     *   <li><strong>BLUE/BLUE_ANIME:</strong> SUCCESS</li>
     *   <li><strong>RED/RED_ANIME:</strong> FAILURE</li>
     *   <li><strong>YELLOW/YELLOW_ANIME:</strong> UNSTABLE</li>
     *   <li><strong>GREY:</strong> UNKNOWN or never built</li>
     *   <li><strong>DISABLED:</strong> DISABLED</li>
     *   <li><strong>ABORTED:</strong> ABORTED</li>
     * </ul>
     *
     * <p><strong>Dashboard Integration:</strong></p>
     * <ul>
     *   <li><strong>Status Badges:</strong> Color-coded visual indicators</li>
     *   <li><strong>Filtering:</strong> Enables status-based job filtering</li>
     *   <li><strong>Sorting:</strong> Allows sorting by build outcome</li>
     *   <li><strong>Alerting:</strong> Triggers notifications for failure states</li>
     * </ul>
     */
    private final String status;

    /**
     * Indicates whether the job is currently executing a build.
     *
     * <p>This boolean flag provides real-time information about the job's
     * execution state, enabling dynamic UI updates and progress tracking
     * for active builds.</p>
     *
     * <p><strong>Detection Logic:</strong></p>
     * <p>Progress status is determined by analyzing Jenkins job indicators:</p>
     * <ul>
     *   <li><strong>BallColor Animation:</strong> "_ANIME" suffix indicates running build</li>
     *   <li><strong>Build Queue:</strong> Job waiting in Jenkins build queue</li>
     *   <li><strong>Executor Status:</strong> Job currently assigned to executor</li>
     *   <li><strong>Build History:</strong> Analysis of recent build completion</li>
     * </ul>
     *
     * <p><strong>Animation Indicators:</strong></p>
     * <ul>
     *   <li><strong>BLUE_ANIME:</strong> Successful build in progress</li>
     *   <li><strong>RED_ANIME:</strong> Failing build in progress</li>
     *   <li><strong>YELLOW_ANIME:</strong> Unstable build in progress</li>
     *   <li><strong>GREY_ANIME:</strong> Unknown status build in progress</li>
     * </ul>
     *
     * <p><strong>UI Integration:</strong></p>
     * <ul>
     *   <li><strong>Progress Indicators:</strong> Animated spinners for active builds</li>
     *   <li><strong>Status Updates:</strong> Real-time status refresh for running jobs</li>
     *   <li><strong>Action Availability:</strong> Disable certain actions during builds</li>
     *   <li><strong>Polling Frequency:</strong> Increased refresh rate for active jobs</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li><strong>Cache Invalidation:</strong> Running jobs may require more frequent cache refresh</li>
     *   <li><strong>API Polling:</strong> Active builds may trigger additional API calls</li>
     *   <li><strong>Resource Usage:</strong> Monitor system resources during active builds</li>
     * </ul>
     */
    private final boolean inProgress;

    /**
     * The Tailwind CSS class for visual status representation.
     *
     * <p>This field contains the appropriate Tailwind CSS background color class
     * that corresponds to the job's current status, enabling consistent visual
     * representation across the dashboard interface.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Mapped from Jenkins BallColor via {@code STATUS_COLOR_MAP} in {@code JenkinsService}</p>
     *
     * <p><strong>CSS Class Mapping:</strong></p>
     * <p>Jenkins status colors are mapped to Tailwind CSS background classes:</p>
     * <ul>
     *   <li><strong>"bg-green-500":</strong> Successful builds (BLUE)</li>
     *   <li><strong>"bg-red-500":</strong> Failed builds (RED)</li>
     *   <li><strong>"bg-yellow-500":</strong> Unstable builds (YELLOW)</li>
     *   <li><strong>"bg-gray-500":</strong> Disabled or unknown status (GREY, DISABLED)</li>
     *   <li><strong>"bg-gray-700":</strong> Aborted builds (ABORTED)</li>
     *   <li><strong>"bg-gray-300":</strong> Never built jobs (NOTBUILT)</li>
     * </ul>
     *
     * <p><strong>Color Semantics:</strong></p>
     * <ul>
     *   <li><strong>Green:</strong> Success, healthy state, builds passing</li>
     *   <li><strong>Red:</strong> Failure, critical issues, builds failing</li>
     *   <li><strong>Yellow:</strong> Warning, unstable state, tests failing</li>
     *   <li><strong>Gray (Dark):</strong> Aborted, user intervention, cancelled builds</li>
     *   <li><strong>Gray (Medium):</strong> Disabled, inactive, configuration issues</li>
     *   <li><strong>Gray (Light):</strong> Not built, new jobs, no history</li>
     * </ul>
     *
     * <p><strong>UI Integration:</strong></p>
     * <ul>
     *   <li><strong>Status Badges:</strong> Applied to job status indicators</li>
     *   <li><strong>Job Cards:</strong> Background color for job cards</li>
     *   <li><strong>Progress Bars:</strong> Color coding for build progress</li>
     *   <li><strong>Dashboard Widgets:</strong> Consistent color scheme across components</li>
     * </ul>
     *
     * <p><strong>Accessibility:</strong></p>
     * <ul>
     *   <li><strong>Color Contrast:</strong> Tailwind classes ensure WCAG compliance</li>
     *   <li><strong>Semantic Meaning:</strong> Colors follow standard conventions</li>
     *   <li><strong>Alternative Indicators:</strong> Used alongside text and icons</li>
     * </ul>
     *
     * <p><strong>Responsive Design:</strong></p>
     * <ul>
     *   <li><strong>Consistent Rendering:</strong> Same appearance across devices</li>
     *   <li><strong>Theme Support:</strong> Compatible with light/dark themes</li>
     *   <li><strong>Scalability:</strong> Maintains visual hierarchy at different sizes</li>
     * </ul>
     */
    private final String colorClass;

    /**
     * The timestamp of the most recent build in epoch milliseconds.
     *
     * <p>This field represents the exact moment when the most recent build
     * for this job was started, stored as milliseconds since Unix epoch
     * for efficient sorting, comparison, and temporal operations.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Retrieved from Jenkins build information via {@code BuildInfo.timestamp()}</p>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Format:</strong> Epoch milliseconds (long) for precision and efficiency</li>
     *   <li><strong>Precision:</strong> Millisecond precision for accurate ordering</li>
     *   <li><strong>Time Zone:</strong> UTC-based for consistency across deployments</li>
     *   <li><strong>Range:</strong> Supports dates from 1970 to far future</li>
     * </ul>
     *
     * <p><strong>Temporal Operations:</strong></p>
     * <ul>
     *   <li><strong>Sorting:</strong> Natural ordering for chronological display</li>
     *   <li><strong>Comparison:</strong> Efficient comparison for recency analysis</li>
     *   <li><strong>Filtering:</strong> Time-based filtering for date ranges</li>
     *   <li><strong>Calculation:</strong> Duration and interval calculations</li>
     * </ul>
     *
     * <p><strong>Display Formatting:</strong></p>
     * <p>Client-side formatting options for user display:</p>
     * <ul>
     *   <li><strong>Relative Time:</strong> "2 hours ago", "3 days ago"</li>
     *   <li><strong>Absolute Time:</strong> "2024-01-15 14:30:45"</li>
     *   <li><strong>Localized Format:</strong> User's locale-specific formatting</li>
     *   <li><strong>ISO Format:</strong> Standard ISO 8601 representation</li>
     * </ul>
     *
     * <p><strong>Performance Benefits:</strong></p>
     * <ul>
     *   <li><strong>Efficient Storage:</strong> 8-byte long vs string representation</li>
     *   <li><strong>Fast Comparison:</strong> Numeric comparison vs string parsing</li>
     *   <li><strong>Database Indexing:</strong> Optimal for database timestamp indexes</li>
     *   <li><strong>JSON Serialization:</strong> Compact representation in API responses</li>
     * </ul>
     *
     * <p><strong>Edge Cases:</strong></p>
     * <ul>
     *   <li><strong>Never Built:</strong> May be 0 or very old timestamp for new jobs</li>
     *   <li><strong>Build Failures:</strong> Timestamp represents start time, not completion</li>
     *   <li><strong>Time Zones:</strong> Always stored in UTC, converted for display</li>
     *   <li><strong>Clock Skew:</strong> May reflect Jenkins server time differences</li>
     * </ul>
     */
    private final long timestamp;

    /**
     * Constructs a new RecentJobDTO with the specified parameters.
     *
     * <p>This constructor creates a complete job status information object ready for
     * caching and JSON serialization. It ensures all required fields are properly
     * initialized and provides a consistent way to create DTO instances from
     * Jenkins API data.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li><strong>name:</strong> Should be non-null and represent valid Jenkins job name</li>
     *   <li><strong>status:</strong> Should match one of the defined status values</li>
     *   <li><strong>inProgress:</strong> Should accurately reflect current build state</li>
     *   <li><strong>colorClass:</strong> Should be valid Tailwind CSS class</li>
     *   <li><strong>timestamp:</strong> Should be valid epoch milliseconds</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Service Layer:</strong> Called by JenkinsService during job data processing</li>
     *   <li><strong>API Mapping:</strong> Parameters derived from Jenkins REST API responses</li>
     *   <li><strong>Cache Population:</strong> Used to populate 15-second TTL cache</li>
     *   <li><strong>Response Assembly:</strong> Part of REST API response construction</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Status Alignment:</strong> status and colorClass should be consistent</li>
     *   <li><strong>Progress Correlation:</strong> inProgress should match status animation</li>
     *   <li><strong>Timestamp Validity:</strong> timestamp should represent recent build time</li>
     *   <li><strong>Name Uniqueness:</strong> name should be unique within Jenkins instance</li>
     * </ul>
     *
     * <p><strong>Construction Examples:</strong></p>
     * {@snippet lang=java :
     * // Successful job with recent build
     * new RecentJobDTO("my-web-app", "SUCCESS", false, "bg-green-500", 1705329045123L);
     *
     * // Failed job currently building
     * new RecentJobDTO("backend-api", "FAILURE", true, "bg-red-500", 1705329045123L);
     *
     * // New job never built
     * new RecentJobDTO("new-service", "NOT_BUILT", false, "bg-gray-300", 0L);
     * }
     *
     * @param name the name of the Jenkins job
     * @param status the current status of the job
     * @param inProgress whether the job is currently executing a build
     * @param colorClass the Tailwind CSS class for visual representation
     * @param timestamp the timestamp of the most recent build in epoch milliseconds
     */
    public RecentJobDTO(String name, String status, boolean inProgress, String colorClass, long timestamp) {
        this.name = name;
        this.status = status;
        this.inProgress = inProgress;
        this.colorClass = colorClass;
        this.timestamp = timestamp;
    }

    /**
     * Returns the name of the Jenkins job.
     *
     * @return the job name, never null for valid instances
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current status of the Jenkins job.
     *
     * @return the job status (SUCCESS, FAILURE, UNSTABLE, etc.), never null
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns whether the job is currently executing a build.
     *
     * @return true if the job is currently building, false otherwise
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * Returns the Tailwind CSS class for visual status representation.
     *
     * @return the CSS class (e.g., "bg-green-500"), never null
     */
    public String getColorClass() {
        return colorClass;
    }

    /**
     * Returns the timestamp of the most recent build.
     *
     * @return the timestamp in epoch milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}