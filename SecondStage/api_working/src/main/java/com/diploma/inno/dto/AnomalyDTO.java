package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing an anomaly detected during CI/CD build analysis.
 *
 * <p>This DTO encapsulates comprehensive information about anomalies identified by AI analysis
 * of Jenkins build logs and processes. Anomalies represent potential issues, security vulnerabilities,
 * performance problems, or quality concerns discovered during automated build analysis.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Jenkins Build Execution:</strong> Build logs generated during Jenkins job execution</li>
 *   <li><strong>Log Ingestion:</strong> Build logs chunked and stored as USER messages in chat_messages table</li>
 *   <li><strong>AI Analysis:</strong> External AI service analyzes logs for anomalies and patterns</li>
 *   <li><strong>Result Storage:</strong> AI analysis results stored as ASSISTANT messages with JSONB content</li>
 *   <li><strong>Data Extraction:</strong> Complex SQL queries extract anomalies from JSONB arrays</li>
 *   <li><strong>DTO Mapping:</strong> Jackson ObjectMapper converts JSONB to AnomalyDTO instances</li>
 * </ol>
 *
 * <p><strong>Database Storage Structure:</strong></p>
 * <pre>
 * Table: chat_messages
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Column          │ Content          │ Anomaly Data Location                               │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ message_type    │ 'ASSISTANT'      │ Indicates AI analysis result                        │
 * │ content (JSONB) │ {anomalies: [..]}│ Array of anomaly objects in JSONB format            │
 * │ conversation_id │ job_name         │ Jenkins job identifier                              │
 * │ build_number    │ build_id         │ Specific build number                               │
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>JSONB Anomaly Structure:</strong></p>
 * {@snippet lang=json :
 * {
 *   "anomalies": [
 *     {
 *       "type": "security|performance|quality|dependency|configuration",
 *       "severity": "CRITICAL|HIGH|MEDIUM|WARNING|LOW",
 *       "description": "Human-readable description of the issue",
 *       "recommendation": "Actionable steps to resolve the issue",
 *       "aiAnalysis": "Detailed AI-generated analysis and context",
 *       "details": {
 *         "location": "file:line or build step",
 *         "impact": "potential impact assessment",
 *         "confidence": "AI confidence level",
 *         "relatedIssues": ["array of related anomaly IDs"]
 *       }
 *     }
 *   ]
 * }
 * }
 *
 * <p><strong>Anomaly Categories:</strong></p>
 * <ul>
 *   <li><strong>Security:</strong> Vulnerabilities, exposed secrets, insecure configurations</li>
 *   <li><strong>Performance:</strong> Slow builds, resource bottlenecks, regression detection</li>
 *   <li><strong>Quality:</strong> Code quality violations, test failures, coverage issues</li>
 *   <li><strong>Dependency:</strong> Outdated packages, security advisories, license issues</li>
 *   <li><strong>Configuration:</strong> Misconfigurations, deprecated settings, best practice violations</li>
 * </ul>
 *
 * <p><strong>Severity Levels:</strong></p>
 * <ul>
 *   <li><strong>CRITICAL:</strong> Severe security vulnerabilities or system-breaking issues</li>
 *   <li><strong>HIGH:</strong> Important issues requiring immediate attention</li>
 *   <li><strong>MEDIUM:</strong> Moderate issues that should be addressed soon</li>
 *   <li><strong>WARNING:</strong> Minor issues or potential problems</li>
 *   <li><strong>LOW:</strong> Informational or low-priority improvements</li>
 * </ul>
 *
 * <p><strong>Data Extraction Process:</strong></p>
 * <p>Anomalies are extracted using complex SQL queries with JSONB operations:</p>
 * {@snippet lang=sql :
 * WITH unnested_anomalies AS (
 *   SELECT anomaly, position
 *   FROM chat_messages cm
 *   CROSS JOIN LATERAL jsonb_array_elements(
 *     COALESCE(cm.content->'anomalies', '[]'::jsonb)
 *   ) WITH ORDINALITY AS t(anomaly, position)
 *   WHERE cm.message_type = 'ASSISTANT'
 *     AND cm.conversation_id = :conversationId
 *     AND cm.build_number = :buildNumber
 * )
 *
 * SELECT COALESCE(jsonb_agg(anomaly ORDER BY position), '[]'::jsonb) AS anomalies
 * FROM unnested_anomalies
 * }
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>REST API:</strong> Serialized to JSON for dashboard endpoints</li>
 *   <li><strong>Pagination:</strong> Used in {@link PaginatedAnomaliesDTO} for paginated responses</li>
 *   <li><strong>Chart Data:</strong> Aggregated for trend analysis and severity distribution</li>
 *   <li><strong>Security Analysis:</strong> Filtered for security-specific anomaly reporting</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "type": "security",
 *   "severity": "HIGH",
 *   "description": "Potential secret exposure in build logs",
 *   "recommendation": "Review and remove sensitive data from logs",
 *   "aiAnalysis": "AI detected patterns suggesting API key exposure...",
 *   "details": {
 *     "location": "build-step-3:line-45",
 *     "confidence": 0.85,
 *     "impact": "Potential unauthorized access"
 *   }
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
 * @see PaginatedAnomaliesDTO
 * @see com.diploma.inno.service.DashboardService#getPaginatedAnomalies(String, Integer, Integer, Integer)
 * @see com.diploma.inno.repository.ChatMessageRepository#findPaginatedAnomalies(String, Integer, Integer, Long)
 */
public class AnomalyDTO {

    /**
     * The category or type of the anomaly.
     *
     * <p>This field classifies the anomaly into broad categories to help with
     * filtering, prioritization, and routing to appropriate teams or processes.</p>
     *
     * <p><strong>Common Types:</strong></p>
     * <ul>
     *   <li><strong>security:</strong> Security vulnerabilities, exposed secrets, insecure configurations</li>
     *   <li><strong>performance:</strong> Performance issues, slow builds, resource bottlenecks</li>
     *   <li><strong>quality:</strong> Code quality violations, test failures, coverage issues</li>
     *   <li><strong>dependency:</strong> Package vulnerabilities, outdated dependencies, license issues</li>
     *   <li><strong>configuration:</strong> Build configuration issues, deprecated settings</li>
     *   <li><strong>infrastructure:</strong> Environment or infrastructure-related problems</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->>'type'} in chat_messages table</p>
     */
    private String type;

    /**
     * The severity level of the anomaly.
     *
     * <p>This field indicates the urgency and impact level of the anomaly,
     * helping teams prioritize remediation efforts and understand risk levels.</p>
     *
     * <p><strong>Severity Levels:</strong></p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Severe security vulnerabilities or system-breaking issues</li>
     *   <li><strong>HIGH:</strong> Important issues requiring immediate attention</li>
     *   <li><strong>MEDIUM:</strong> Moderate issues that should be addressed soon</li>
     *   <li><strong>WARNING:</strong> Minor issues or potential problems</li>
     *   <li><strong>LOW:</strong> Informational or low-priority improvements</li>
     * </ul>
     *
     * <p><strong>Impact on System Health:</strong></p>
     * <ul>
     *   <li>CRITICAL/HIGH anomalies mark builds as "CRITICAL" or "WARNING" health status</li>
     *   <li>Multiple MEDIUM anomalies can escalate overall build health status</li>
     *   <li>LOW/WARNING anomalies typically don't affect overall health assessment</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->>'severity'} in chat_messages table</p>
     */
    private String severity;

    /**
     * Human-readable description of the anomaly.
     *
     * <p>This field provides a clear, concise explanation of what the anomaly
     * represents, making it accessible to both technical and non-technical stakeholders.</p>
     *
     * <p><strong>Description Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Clarity:</strong> Written in plain language for broad understanding</li>
     *   <li><strong>Specificity:</strong> Includes specific details about the issue</li>
     *   <li><strong>Context:</strong> Provides context about where and why the issue occurred</li>
     *   <li><strong>Impact:</strong> May include information about potential impact</li>
     * </ul>
     *
     * <p><strong>Example Descriptions:</strong></p>
     * <ul>
     *   <li>"Potential API key exposed in build logs"</li>
     *   <li>"Build time increased by 40% compared to baseline"</li>
     *   <li>"Critical security vulnerability in dependency package"</li>
     *   <li>"Test coverage dropped below 80% threshold"</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->>'description'} in chat_messages table</p>
     */
    private String description;

    /**
     * Actionable recommendations for resolving the anomaly.
     *
     * <p>This field provides specific, actionable steps that development teams
     * can take to address and resolve the identified anomaly.</p>
     *
     * <p><strong>Recommendation Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Actionable:</strong> Specific steps that can be implemented</li>
     *   <li><strong>Prioritized:</strong> Steps ordered by importance or dependency</li>
     *   <li><strong>Contextual:</strong> Tailored to the specific anomaly and environment</li>
     *   <li><strong>Feasible:</strong> Realistic and achievable within normal development workflows</li>
     * </ul>
     *
     * <p><strong>Example Recommendations:</strong></p>
     * <ul>
     *   <li>"Remove API key from logs and use environment variables instead"</li>
     *   <li>"Optimize database queries in the user service module"</li>
     *   <li>"Update package to version 2.1.4 or higher to fix vulnerability"</li>
     *   <li>"Add unit tests for the authentication module to improve coverage"</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->>'recommendation'} in chat_messages table</p>
     */
    private String recommendation;

    /**
     * Detailed AI-generated analysis and context for the anomaly.
     *
     * <p>This field contains comprehensive analysis provided by the AI system,
     * including technical details, patterns detected, and additional context
     * that supports the anomaly identification.</p>
     *
     * <p><strong>AI Analysis Components:</strong></p>
     * <ul>
     *   <li><strong>Pattern Recognition:</strong> Specific patterns or signatures detected</li>
     *   <li><strong>Confidence Level:</strong> AI confidence in the anomaly detection</li>
     *   <li><strong>Historical Context:</strong> Comparison with previous builds or baselines</li>
     *   <li><strong>Technical Details:</strong> Deep technical analysis and reasoning</li>
     *   <li><strong>Related Issues:</strong> Connections to other anomalies or known issues</li>
     * </ul>
     *
     * <p><strong>Analysis Depth:</strong></p>
     * <ul>
     *   <li>Detailed technical explanations for complex issues</li>
     *   <li>Root cause analysis when patterns are identified</li>
     *   <li>Risk assessment and potential impact scenarios</li>
     *   <li>Correlation with industry best practices and standards</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->>'aiAnalysis'} in chat_messages table</p>
     */
    private String aiAnalysis;

    /**
     * Additional structured details about the anomaly.
     *
     * <p>This field contains supplementary information that doesn't fit into
     * the standard string fields. It can hold complex nested objects with
     * technical details, metadata, and extended information.</p>
     *
     * <p><strong>Common Detail Types:</strong></p>
     * <ul>
     *   <li><strong>Location Information:</strong> File paths, line numbers, build steps</li>
     *   <li><strong>Metrics:</strong> Performance numbers, thresholds, comparisons</li>
     *   <li><strong>Confidence Scores:</strong> AI confidence levels and reliability indicators</li>
     *   <li><strong>Related Data:</strong> Links to other anomalies, tickets, or documentation</li>
     *   <li><strong>Temporal Data:</strong> Timestamps, durations, trend information</li>
     * </ul>
     *
     * <p><strong>Example Detail Structures:</strong></p>
     *
     * <span><b>Security anomaly details</b></span>
     * {@snippet lang=json :
     * {
     *   "location": "build-step-3:line-45",
     *   "confidence": 0.85,
     *   "impact": "Potential unauthorized access",
     *   "cve": "CVE-2023-12345"
     * }
     * }
     *
     * <span><b>Performance anomaly details</span></b>
     * {@snippet lang=json :
     * {
     *   "baseline": "2.3 minutes",
     *   "current": "3.2 minutes",
     *   "increase": "39%",
     *   "threshold": "20%"
     * }
     * }
     *
     * <p><strong>Data Types:</strong></p>
     * <ul>
     *   <li>Can be a Map&lt;String, Object&gt; for structured data</li>
     *   <li>Can be a String for simple additional information</li>
     *   <li>Can be a List for multiple related items</li>
     *   <li>Can be null if no additional details are available</li>
     * </ul>
     *
     * <p><strong>JSONB Mapping:</strong></p>
     * <p>Maps to {@code content->'anomalies'[n]->'details'} in chat_messages table</p>
     */
    private Object details; // Can be a nested JSON object

    /**
     * Constructs a new AnomalyDTO with the specified parameters.
     *
     * <p>This constructor is primarily used by Jackson ObjectMapper during
     * JSON deserialization from database JSONB content. The JsonProperty
     * annotations ensure proper mapping from JSON field names to Java fields.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>All parameters can be null (handled gracefully by the system)</li>
     *   <li>Jackson handles type conversion and validation during deserialization</li>
     *   <li>Invalid JSON structures are logged and handled by service layer</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li>Automatic deserialization from JSONB database content</li>
     *   <li>Manual construction in test scenarios</li>
     *   <li>API request/response processing</li>
     * </ul>
     *
     * @param type the category or type of the anomaly
     * @param severity the severity level (CRITICAL, HIGH, MEDIUM, WARNING, LOW)
     * @param description human-readable description of the issue
     * @param recommendation actionable steps to resolve the anomaly
     * @param aiAnalysis detailed AI-generated analysis and context
     * @param details additional structured information about the anomaly
     */
    public AnomalyDTO(
            @JsonProperty("type") String type,
            @JsonProperty("severity") String severity,
            @JsonProperty("description") String description,
            @JsonProperty("recommendation") String recommendation,
            @JsonProperty("aiAnalysis") String aiAnalysis,
            @JsonProperty("details") Object details) {
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.recommendation = recommendation;
        this.aiAnalysis = aiAnalysis;
        this.details = details;
    }

    /**
     * Returns the category or type of the anomaly.
     *
     * @return the anomaly type (e.g., "security", "performance", "quality")
     */
    public String getType() { return type; }

    /**
     * Sets the category or type of the anomaly.
     *
     * @param type the anomaly type to set
     */
    public void setType(String type) { this.type = type; }

    /**
     * Returns the severity level of the anomaly.
     *
     * @return the severity level (CRITICAL, HIGH, MEDIUM, WARNING, LOW)
     */
    public String getSeverity() { return severity; }

    /**
     * Sets the severity level of the anomaly.
     *
     * @param severity the severity level to set
     */
    public void setSeverity(String severity) { this.severity = severity; }

    /**
     * Returns the human-readable description of the anomaly.
     *
     * @return the anomaly description
     */
    public String getDescription() { return description; }

    /**
     * Sets the human-readable description of the anomaly.
     *
     * @param description the description to set
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns the actionable recommendations for resolving the anomaly.
     *
     * @return the recommendations for fixing the issue
     */
    public String getRecommendation() { return recommendation; }

    /**
     * Sets the actionable recommendations for resolving the anomaly.
     *
     * @param recommendation the recommendations to set
     */
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    /**
     * Returns the detailed AI-generated analysis and context.
     *
     * @return the AI analysis of the anomaly
     */
    public String getAiAnalysis() { return aiAnalysis; }

    /**
     * Sets the detailed AI-generated analysis and context.
     *
     * @param aiAnalysis the AI analysis to set
     */
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }

    /**
     * Returns additional structured details about the anomaly.
     *
     * @return the additional details object (can be Map, String, List, or null)
     */
    public Object getDetails() { return details; }


    /**
     * Sets additional structured details about the anomaly.
     *
     * @param details the additional details object (can be Map, String, List, or null)
     */
    public void setDetails(Object details) { this.details = details; }
}
