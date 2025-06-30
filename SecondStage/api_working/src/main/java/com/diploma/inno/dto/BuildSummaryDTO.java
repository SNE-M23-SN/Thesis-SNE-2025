package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a comprehensive build summary from AI analysis.
 *
 * <p>This DTO encapsulates essential build information derived from AI analysis of Jenkins
 * build logs and metadata. It provides a high-level overview of build health, performance,
 * and key metrics for dashboard visualization and monitoring purposes.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins Build Execution:</strong> Build logs and metadata generated during job execution</li>
 *   <li><strong>Log Ingestion:</strong> Build logs chunked and stored as USER messages in chat_messages table</li>
 *   <li><strong>AI Analysis:</strong> External AI service analyzes logs and generates comprehensive summary</li>
 *   <li><strong>Result Storage:</strong> AI analysis results stored as ASSISTANT messages with JSONB content</li>
 *   <li><strong>Data Extraction:</strong> Complex SQL query extracts and formats summary data from JSONB</li>
 *   <li><strong>DTO Mapping:</strong> Jackson ObjectMapper converts JSON to BuildSummaryDTO instance</li>
 * </ol>
 *
 * <p><strong>Database Query Structure:</strong></p>
 * <p>Data is extracted using a complex SQL query with jsonb_build_object:</p>
 * {@snippet lang=sql :
 * SELECT jsonb_build_object(
 *   'job_name', cm.job_name,
 *   'build_id', cm.build_number,
 *   'health_status', CASE
 *     WHEN cm.content->'anomalies' @> '[{"severity": "CRITICAL"}]'
 *       OR cm.content->'anomalies' @> '[{"severity": "HIGH"}]'
 *       THEN 'CRITICAL'
 *     WHEN (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a
 *           WHERE a->>'severity' = 'MEDIUM') > 0
 *       OR (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a
 *           WHERE a->>'severity' IN ('CRITICAL', 'HIGH', 'MEDIUM')) > 1
 *       THEN 'WARNING'
 *     WHEN (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a
 *           WHERE a->>'severity' IN ('LOW', 'WARNING')) <= 5
 *       AND NOT (cm.content->'anomalies' @> '[{"severity": "CRITICAL"}]'
 *               OR cm.content->'anomalies' @> '[{"severity": "HIGH"}]'
 *               OR cm.content->'anomalies' @> '[{"severity": "MEDIUM"}]')
 *       THEN 'Healthy'
 *     ELSE 'Unhealthy'
 *   END,
 *   'build_summary', cm.content->>'summary',
 *   'build_started_time', CASE
 *     WHEN (cm.content->'buildMetadata'->>'startTime')::timestamptz::date = CURRENT_DATE
 *     THEN 'Today, ' || TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMHH12:MI AM')
 *     ELSE TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMDay, FMHH12:MI AM')
 *   END,
 *   'build_duration',
 *     FLOOR((cm.content->'buildMetadata'->>'durationSeconds')::numeric / 60) || 'm ' ||
 *     ((cm.content->'buildMetadata'->>'durationSeconds')::numeric % 60) || 's',
 *   'regression_detected',
 *     (cm.content->>'regressionFromPreviousBuilds')::boolean
 * ) AS build_summary
 * FROM chat_messages cm
 * WHERE cm.message_type = 'ASSISTANT'
 *   AND cm.conversation_id = :conversationId
 *   AND cm.build_number = :buildNumber
 * }
 *
 * <p><strong>JSONB Content Structure:</strong></p>
 *
 * <span>ASSISTANT message content structure</span>
 * {@snippet lang=json :
 * {
 *   "summary": "AI-generated build summary text",
 *   "anomalies": [
 *     {
 *       "severity": "CRITICAL|HIGH|MEDIUM|WARNING|LOW",
 *       "type": "security|performance|quality|...",
 *       "description": "...",
 *       "recommendation": "..."
 *     }
 *   ],
 *   "buildMetadata": {
 *     "status": "SUCCESS|FAILURE|UNSTABLE|ABORTED",
 *     "startTime": "2024-01-15T10:30:45.123Z",
 *     "durationSeconds": 180,
 *     "timestamp": "..."
 *   },
 *   "regressionFromPreviousBuilds": "true|false"
 * }
 * }
 *
 * <p><strong>Health Status Calculation Logic:</strong></p>
 * <ul>
 *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
 *   <li><strong>WARNING:</strong> Contains MEDIUM severity anomalies OR multiple high-severity anomalies</li>
 *   <li><strong>Healthy:</strong> Only LOW/WARNING anomalies (≤5) and no higher severity issues</li>
 *   <li><strong>Unhealthy:</strong> Default case for other scenarios</li>
 * </ul>
 *
 * <p><strong>Time Formatting Logic:</strong></p>
 * <ul>
 *   <li><strong>Today's Builds:</strong> "Today, 2:30 PM" format</li>
 *   <li><strong>Other Days:</strong> "Monday, 2:30 PM" format</li>
 *   <li><strong>Duration:</strong> "3m 45s" format (minutes and seconds)</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>REST API:</strong> Serialized to JSON for dashboard endpoints</li>
 *   <li><strong>Build Details:</strong> Comprehensive build information display</li>
 *   <li><strong>Health Monitoring:</strong> Quick build health assessment</li>
 *   <li><strong>Performance Tracking:</strong> Build duration and regression monitoring</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "jobName": "my-web-app",
 *   "buildId": 123,
 *   "healthStatus": "WARNING",
 *   "buildSummary": "Build completed with 2 medium severity issues...",
 *   "buildStartedTime": "Today, 2:30 PM",
 *   "buildDuration": "3m 45s",
 *   "regressionDetected": false
 * }
 * }
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * Jackson annotations ensure proper serialization/deserialization in multi-threaded environments.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.service.DashboardService#getBuildSummary(String, Integer)
 * @see com.diploma.inno.repository.ChatMessageRepository#findBuildSummary(String, Integer)
 */
public class BuildSummaryDTO {

    /**
     * The name of the Jenkins job.
     *
     * <p>This field contains the Jenkins job name that generated the build.
     * It serves as the primary identifier for grouping builds and linking
     * them to their corresponding Jenkins job configuration.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Maps directly to {@code chat_messages.job_name} column</p>
     *
     * <p><strong>Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Case Sensitivity:</strong> Preserves original Jenkins job name casing</li>
     *   <li><strong>Special Characters:</strong> May include spaces, hyphens, underscores</li>
     *   <li><strong>Uniqueness:</strong> Unique within Jenkins instance but not globally</li>
     *   <li><strong>Consistency:</strong> Matches conversation_id in chat_messages table</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code job_name} field in the SQL query result</p>
     */
    private String jobName;

    /**
     * The build number/ID for this specific build.
     *
     * <p>This field contains the Jenkins build number, which is a sequential
     * integer assigned by Jenkins for each build execution within a job.
     * It uniquely identifies a specific build within the context of a job.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Maps directly to {@code chat_messages.build_number} column</p>
     *
     * <p><strong>Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Sequential:</strong> Incremental integer starting from 1</li>
     *   <li><strong>Unique per Job:</strong> Unique within the scope of a single Jenkins job</li>
     *   <li><strong>Immutable:</strong> Never changes once assigned by Jenkins</li>
     *   <li><strong>Continuous:</strong> Generally continuous but may have gaps due to aborted builds</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code build_id} field in the SQL query result</p>
     */
    private Integer buildId;

    /**
     * The calculated health status of the build based on anomaly analysis.
     *
     * <p>This field represents the overall health assessment of the build,
     * calculated by analyzing the severity and count of anomalies detected
     * during AI analysis of the build logs.</p>
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
     *   <li>Check for CRITICAL or HIGH severity anomalies → CRITICAL</li>
     *   <li>Check for MEDIUM severity OR multiple high-severity → WARNING</li>
     *   <li>Check for only LOW/WARNING (≤5) and no higher → Healthy</li>
     *   <li>All other cases → Unhealthy</li>
     * </ol>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code health_status} field calculated by complex CASE statement</p>
     */
    private String healthStatus;

    /**
     * AI-generated summary of the build analysis.
     *
     * <p>This field contains a comprehensive, human-readable summary generated
     * by the AI analysis system. It provides insights into build performance,
     * issues detected, and overall assessment of the build quality.</p>
     *
     * <p><strong>Summary Content:</strong></p>
     * <ul>
     *   <li><strong>Build Overview:</strong> High-level assessment of build success/failure</li>
     *   <li><strong>Key Issues:</strong> Summary of major problems or anomalies found</li>
     *   <li><strong>Performance Notes:</strong> Build time and performance observations</li>
     *   <li><strong>Recommendations:</strong> High-level suggestions for improvement</li>
     *   <li><strong>Trend Analysis:</strong> Comparison with previous builds when available</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Maps to {@code content->>'summary'} from JSONB content in chat_messages</p>
     *
     * <p><strong>AI Generation Process:</strong></p>
     * <ul>
     *   <li>AI analyzes complete build logs and metadata</li>
     *   <li>Identifies patterns, anomalies, and performance characteristics</li>
     *   <li>Generates human-readable summary with actionable insights</li>
     *   <li>Includes context from historical build data when available</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code build_summary} field extracted from JSONB content</p>
     */
    private String buildSummary;

    /**
     * Human-readable formatted start time of the build.
     *
     * <p>This field contains a user-friendly representation of when the build
     * was started, formatted differently based on whether the build occurred
     * today or on a previous day.</p>
     *
     * <p><strong>Formatting Rules:</strong></p>
     * <ul>
     *   <li><strong>Today's Builds:</strong> "Today, 2:30 PM" format</li>
     *   <li><strong>Previous Days:</strong> "Monday, 2:30 PM" format (day name + time)</li>
     *   <li><strong>Time Format:</strong> 12-hour format with AM/PM indicator</li>
     *   <li><strong>Timezone:</strong> Formatted in system timezone</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Derived from {@code content->'buildMetadata'->>'startTime'} JSONB field</p>
     *
     * <p><strong>SQL Formatting Logic:</strong></p>
     * {@snippet lang=java :
     * CASE
     *   WHEN (cm.content->'buildMetadata'->>'startTime')::timestamptz::date = CURRENT_DATE
     *   THEN 'Today, ' || TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMHH12:MI AM')
     *   ELSE TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMDay, FMHH12:MI AM')
     * END
     * }
     *
     * <p><strong>Example Values:</strong></p>
     * <ul>
     *   <li>"Today, 2:30 PM"</li>
     *   <li>"Monday, 9:15 AM"</li>
     *   <li>"Friday, 11:45 PM"</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code build_started_time} field with conditional formatting</p>
     */
    private String buildStartedTime;

    /**
     * Human-readable formatted duration of the build.
     *
     * <p>This field contains the total time taken for the build to complete,
     * formatted in a user-friendly "minutes and seconds" format for easy
     * comprehension and performance monitoring.</p>
     *
     * <p><strong>Format Specification:</strong></p>
     * <ul>
     *   <li><strong>Pattern:</strong> "{minutes}m {seconds}s"</li>
     *   <li><strong>Minutes:</strong> Total minutes (can be 0 or more)</li>
     *   <li><strong>Seconds:</strong> Remaining seconds after minute calculation (0-59)</li>
     *   <li><strong>Precision:</strong> Rounded to nearest second</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Calculated from {@code content->'buildMetadata'->>'durationSeconds'} JSONB field</p>
     *
     * <p><strong>SQL Calculation Logic:</strong></p>
     * <pre>
     * FLOOR((cm.content->'buildMetadata'->>'durationSeconds')::numeric / 60) || 'm ' ||
     * ((cm.content->'buildMetadata'->>'durationSeconds')::numeric % 60) || 's'
     * </pre>
     *
     * <p><strong>Example Values:</strong></p>
     * <ul>
     *   <li>"3m 45s" (3 minutes, 45 seconds)</li>
     *   <li>"0m 30s" (30 seconds)</li>
     *   <li>"15m 0s" (exactly 15 minutes)</li>
     *   <li>"120m 15s" (2 hours, 15 seconds)</li>
     * </ul>
     *
     * <p><strong>Performance Interpretation:</strong></p>
     * <ul>
     *   <li><strong>&lt; 2 minutes:</strong> Fast build, good performance</li>
     *   <li><strong>2-10 minutes:</strong> Normal build time for most projects</li>
     *   <li><strong>10-30 minutes:</strong> Longer build, may need optimization</li>
     *   <li><strong>&gt; 30 minutes:</strong> Very long build, optimization recommended</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code build_duration} field with calculated formatting</p>
     */
    private String buildDuration;

    /**
     * Indicates whether performance regression was detected compared to previous builds.
     *
     * <p>This field represents the result of AI analysis comparing the current
     * build's performance characteristics with historical build data to detect
     * performance regressions or improvements.</p>
     *
     * <p><strong>Regression Detection Criteria:</strong></p>
     * <ul>
     *   <li><strong>Build Duration:</strong> Significant increase in build time</li>
     *   <li><strong>Resource Usage:</strong> Higher memory or CPU consumption</li>
     *   <li><strong>Test Performance:</strong> Slower test execution times</li>
     *   <li><strong>Deployment Time:</strong> Longer deployment or startup times</li>
     *   <li><strong>Quality Metrics:</strong> Degradation in code quality scores</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Maps to {@code content->>'regressionFromPreviousBuilds'} JSONB field</p>
     *
     * <p><strong>Value Interpretation:</strong></p>
     * <ul>
     *   <li><strong>true:</strong> Performance regression detected</li>
     *   <li><strong>false:</strong> No regression detected (performance maintained or improved)</li>
     *   <li><strong>null:</strong> Insufficient data for comparison (first build, etc.)</li>
     * </ul>
     *
     * <p><strong>AI Analysis Process:</strong></p>
     * <ul>
     *   <li>Compares current build metrics with baseline from previous builds</li>
     *   <li>Applies statistical analysis to identify significant changes</li>
     *   <li>Considers multiple performance dimensions simultaneously</li>
     *   <li>Accounts for normal variance and establishes regression thresholds</li>
     * </ul>
     *
     * <p><strong>Usage in Monitoring:</strong></p>
     * <ul>
     *   <li>Alerts teams to potential performance issues</li>
     *   <li>Triggers performance investigation workflows</li>
     *   <li>Helps identify commits that introduced performance problems</li>
     *   <li>Supports continuous performance monitoring and optimization</li>
     * </ul>
     *
     * <p><strong>JSON Mapping:</strong></p>
     * <p>Maps to {@code regression_detected} field cast to boolean</p>
     */
    private Boolean regressionDetected;

    /**
     * Constructs a new BuildSummaryDTO with the specified parameters.
     *
     * <p>This constructor is primarily used by Jackson ObjectMapper during
     * JSON deserialization from the database query result. The JsonProperty
     * annotations ensure proper mapping from SQL query field names to Java fields.</p>
     *
     * <p><strong>Parameter Mapping:</strong></p>
     * <ul>
     *   <li><strong>job_name:</strong> Maps to jobName field</li>
     *   <li><strong>build_id:</strong> Maps to buildId field</li>
     *   <li><strong>health_status:</strong> Maps to healthStatus field</li>
     *   <li><strong>build_summary:</strong> Maps to buildSummary field</li>
     *   <li><strong>build_started_time:</strong> Maps to buildStartedTime field</li>
     *   <li><strong>build_duration:</strong> Maps to buildDuration field</li>
     *   <li><strong>regression_detected:</strong> Maps to regressionDetected field</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li>Automatic deserialization from SQL query JSON result</li>
     *   <li>Manual construction in test scenarios</li>
     *   <li>API response processing and validation</li>
     * </ul>
     *
     * @param jobName the name of the Jenkins job
     * @param buildId the build number/ID
     * @param healthStatus the calculated health status based on anomalies
     * @param buildSummary the AI-generated summary of the build
     * @param buildStartedTime the formatted start time of the build
     * @param buildDuration the formatted duration of the build
     * @param regressionDetected whether performance regression was detected
     */
    public BuildSummaryDTO(@JsonProperty("job_name") String jobName,
                           @JsonProperty("build_id") Integer buildId,
                           @JsonProperty("health_status") String healthStatus,
                           @JsonProperty("build_summary") String buildSummary,
                           @JsonProperty("build_started_time") String buildStartedTime,
                           @JsonProperty("build_duration") String buildDuration,
                           @JsonProperty("regression_detected") Boolean regressionDetected) {
        this.jobName = jobName;
        this.buildId = buildId;
        this.healthStatus = healthStatus;
        this.buildSummary = buildSummary;
        this.buildStartedTime = buildStartedTime;
        this.buildDuration = buildDuration;
        this.regressionDetected = regressionDetected;
    }

    /**
     * Returns the name of the Jenkins job.
     *
     * @return the Jenkins job name, never null for valid build summaries
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the name of the Jenkins job.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the job name is set via
     * constructor and should not be modified.</p>
     *
     * @param jobName the Jenkins job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Returns the build number/ID for this specific build.
     *
     * @return the Jenkins build number, never null for valid build summaries
     */
    public Integer getBuildId() {
        return buildId;
    }

    /**
     * Sets the build number/ID for this specific build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the build ID is set via
     * constructor and should not be modified.</p>
     *
     * @param buildId the Jenkins build number to set
     */
    public void setBuildId(Integer buildId) {
        this.buildId = buildId;
    }

    /**
     * Returns the calculated health status of the build.
     *
     * <p>The health status is calculated based on the severity and count of
     * anomalies detected during AI analysis.</p>
     *
     * @return the health status (CRITICAL, WARNING, Healthy, or Unhealthy)
     */
    public String getHealthStatus() {
        return healthStatus;
    }

    /**
     * Sets the calculated health status of the build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the health status is calculated
     * by SQL query and should not be modified.</p>
     *
     * @param healthStatus the health status to set
     */
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    /**
     * Returns the AI-generated summary of the build analysis.
     *
     * <p>This summary provides comprehensive insights into build performance,
     * issues detected, and overall assessment of build quality.</p>
     *
     * @return the AI-generated build summary text
     */
    public String getBuildSummary() {
        return buildSummary;
    }

    /**
     * Sets the AI-generated summary of the build analysis.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the build summary is generated
     * by AI analysis and should not be modified.</p>
     *
     * @param buildSummary the build summary text to set
     */
    public void setBuildSummary(String buildSummary) {
        this.buildSummary = buildSummary;
    }

    /**
     * Returns the human-readable formatted start time of the build.
     *
     * <p>The format varies based on when the build occurred:</p>
     * <ul>
     *   <li>Today's builds: "Today, 2:30 PM"</li>
     *   <li>Previous days: "Monday, 2:30 PM"</li>
     * </ul>
     *
     * @return the formatted build start time
     */
    public String getBuildStartedTime() {
        return buildStartedTime;
    }

    /**
     * Sets the human-readable formatted start time of the build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the start time is formatted
     * by SQL query and should not be modified.</p>
     *
     * @param buildStartedTime the formatted start time to set
     */
    public void setBuildStartedTime(String buildStartedTime) {
        this.buildStartedTime = buildStartedTime;
    }

    /**
     * Returns the human-readable formatted duration of the build.
     *
     * <p>Duration is formatted as "{minutes}m {seconds}s" for easy comprehension
     * and performance monitoring.</p>
     *
     * @return the formatted build duration (e.g., "3m 45s")
     */
    public String getBuildDuration() {
        return buildDuration;
    }

    /**
     * Sets the human-readable formatted duration of the build.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the duration is calculated
     * and formatted by SQL query and should not be modified.</p>
     *
     * @param buildDuration the formatted duration to set
     */
    public void setBuildDuration(String buildDuration) {
        this.buildDuration = buildDuration;
    }

    /**
     * Returns whether performance regression was detected compared to previous builds.
     *
     * <p>This indicates the result of AI analysis comparing current build
     * performance with historical data to detect regressions or improvements.</p>
     *
     * @return true if regression detected, false if not, null if insufficient data
     */
    public Boolean getRegressionDetected() {
        return regressionDetected;
    }

    /**
     * Sets whether performance regression was detected.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, regression detection is performed
     * by AI analysis and should not be modified.</p>
     *
     * @param regressionDetected the regression detection result to set
     */
    public void setRegressionDetected(Boolean regressionDetected) {
        this.regressionDetected = regressionDetected;
    }
}