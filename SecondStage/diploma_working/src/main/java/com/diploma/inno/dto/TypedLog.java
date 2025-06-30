package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Abstract base class for typed log data objects in CI/CD anomaly detection &amp; security analysis.
 *
 * <p>This abstract class serves as the foundation for all log data types within the Jenkins
 * anomaly detection pipeline. It provides common functionality, type-based polymorphic
 * serialization, and establishes the contract for specialized log implementations including
 * build logs, security scans, dependency analysis, code changes, and system metrics.</p>
 *
 * <h2>Core Architecture &amp; Design</h2>
 * <p>TypedLog implements a sophisticated type-based polymorphic system that enables:</p>
 * <ul>
 *   <li><strong>Type Safety:</strong> Compile-time type checking for log data structures</li>
 *   <li><strong>Polymorphic Serialization:</strong> Jackson-based type-aware JSON processing</li>
 *   <li><strong>Extensible Design:</strong> Easy addition of new log types without breaking changes</li>
 *   <li><strong>Common Interface:</strong> Unified API for all log data operations</li>
 *   <li><strong>Message Routing:</strong> Type-based routing for specialized processing</li>
 * </ul>
 *
 * <h2>Supported Log Types &amp; Implementations</h2>
 * <p>The class hierarchy supports comprehensive log data types for CI/CD analysis:</p>
 * <pre>{@code
 * TypedLog (Abstract Base)
 *  ├── BuildLogData ("build_log_data")
 *  │    └── Jenkins build console output & execution logs
 *  ├── SecretDetection ("secret_detection")
 *  │    └── Credential scanning & secret exposure detection
 *  ├── DependencyData ("dependency_data")
 *  │    └── Build dependencies, artifacts & plugin information
 *  ├── CodeChanges ("code_changes")
 *  │    └── Version control changes & commit information
 *  ├── AdditionalInfoAgent ("additional_info_agent")
 *  │    └── Jenkins agent system metrics & performance data
 *  ├── AdditionalInfoController ("additional_info_controller")
 *  │    └── Jenkins controller system metrics & health data
 *  └── ScanResult ("sast_scanning")
 *       └── Static Application Security Testing results
 * }</pre>
 *
 * <h2>Jackson Polymorphic Serialization</h2>
 * <p>The class uses Jackson annotations for sophisticated type-aware serialization:</p>
 * <ul>
 *   <li><strong>@JsonTypeInfo:</strong> Configures type information inclusion in JSON</li>
 *   <li><strong>@JsonSubTypes:</strong> Maps type names to concrete implementations</li>
 *   <li><strong>Property-Based:</strong> Uses "type" property for type discrimination</li>
 *   <li><strong>Automatic Resolution:</strong> Automatic deserialization to correct subtype</li>
 * </ul>
 *
 * <h3>JSON Serialization Examples:</h3>
 * <p>Build log data serialization:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "build_log_data",
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "buildNumber": 123,
 *   "job_name": "security-pipeline",
 *   "data": {
 *     "content": "Build started...",
 *     "status": "SUCCESS"
 *   }
 * }
 * }
 *
 * <p>Secret detection serialization:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "secret_detection",
 *   "timestamp": "2024-01-15T10:31:00Z",
 *   "buildNumber": 123,
 *   "job_name": "security-scan-pipeline",
 *   "data": {
 *     "source": "src/main/resources/application.properties",
 *     "secrets": {
 *       "database_password": ["secret123"],
 *       "api_key": ["sk-1234567890abcdef"]
 *     }
 *   }
 * }
 * }
 *
 * <p>SAST scanning results serialization:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "sast_scanning",
 *   "timestamp": "2024-01-15T10:32:00Z",
 *   "buildNumber": 123,
 *   "repoUrl": "https://github.com/company/security-app.git",
 *   "tool": "SonarQube",
 *   "status": "SUCCESS",
 *   "scanResult": "{\"vulnerabilities\":[...]}"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>TypedLog provides the foundation for AI-powered analysis across all log types:</p>
 * <ul>
 *   <li><strong>Content Standardization:</strong> Uniform content extraction for AI processing</li>
 *   <li><strong>Context Preservation:</strong> Maintains conversation context across log types</li>
 *   <li><strong>Type-Specific Analysis:</strong> Enables specialized analysis per log type</li>
 *   <li><strong>Cross-Type Correlation:</strong> Facilitates analysis across different log sources</li>
 * </ul>
 *
 * <h2>Temporal Coordination &amp; Sequencing</h2>
 * <p>The class provides temporal coordination capabilities:</p>
 * <ul>
 *   <li><strong>Timestamp Management:</strong> ISO-8601 formatted timestamps for precise ordering</li>
 *   <li><strong>Build Correlation:</strong> Build number-based event correlation</li>
 *   <li><strong>Chronological Analysis:</strong> Time-based pattern recognition &amp; analysis</li>
 *   <li><strong>Event Sequencing:</strong> Proper ordering of related log events</li>
 * </ul>
 *
 * <h2>Message Routing &amp; Processing</h2>
 * <p>Type information enables sophisticated message routing:</p>
 * <ul>
 *   <li><strong>Type-Based Routing:</strong> Automatic routing to specialized processors</li>
 *   <li><strong>Pipeline Integration:</strong> Seamless integration with processing pipelines</li>
 *   <li><strong>Load Balancing:</strong> Type-aware load distribution across processors</li>
 *   <li><strong>Error Handling:</strong> Type-specific error handling &amp; recovery</li>
 * </ul>
 *
 * <h2>Extensibility &amp; Future Growth</h2>
 * <p>The design supports easy extension for new log types:</p>
 * <ul>
 *   <li><strong>New Type Addition:</strong> Simple addition via @JsonSubTypes annotation</li>
 *   <li><strong>Backward Compatibility:</strong> Existing types remain unaffected</li>
 *   <li><strong>Interface Compliance:</strong> All new types must implement Log interface</li>
 *   <li><strong>Consistent Behavior:</strong> Inherited common functionality from TypedLog</li>
 * </ul>
 *
 * <h2>Performance &amp; Optimization</h2>
 * <ul>
 *   <li><strong>Efficient Serialization:</strong> Optimized Jackson-based JSON processing</li>
 *   <li><strong>Type Caching:</strong> Jackson caches type information for performance</li>
 *   <li><strong>Memory Efficiency:</strong> Shared base functionality reduces memory overhead</li>
 *   <li><strong>Processing Speed:</strong> Fast type resolution &amp; method dispatch</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This abstract class integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; deserializes typed log messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores typed log data for conversation management</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides typed content for specialized analysis</li>
 *   <li><strong>Message Queue:</strong> Enables type-aware message processing via RabbitMQ</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see Log
 * @see BuildLogData
 * @see SecretDetection
 * @see DependencyData
 * @see CodeChanges
 * @see AdditionalInfoAgent
 * @see AdditionalInfoController
 * @see ScanResult
 * @see com.diploma.inno.component.LogMessageListener
 * @see com.diploma.inno.component.SimpleDbChatMemory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuildLogData.class, name = "build_log_data"),
        @JsonSubTypes.Type(value = SecretDetection.class, name = "secret_detection"),
        @JsonSubTypes.Type(value = DependencyData.class, name = "dependency_data"),
        @JsonSubTypes.Type(value = CodeChanges.class, name = "code_changes"),
        @JsonSubTypes.Type(value = AdditionalInfoAgent.class, name = "additional_info_agent"),
        @JsonSubTypes.Type(value = AdditionalInfoController.class, name = "additional_info_controller"),
        @JsonSubTypes.Type(value = ScanResult.class, name = "sast_scanning")
})
public abstract class TypedLog implements Log {

    // ========================================================================
    // CORE FIELDS &amp; PROPERTIES
    // ========================================================================

    /**
     * Log type identifier for polymorphic serialization &amp; message routing.
     * <p>This field serves as the discriminator for Jackson's polymorphic serialization,
     * enabling automatic deserialization to the correct concrete subtype. It also
     * facilitates type-based message routing and specialized processing.</p>
     *
     * <h4>Supported Type Values:</h4>
     * <ul>
     *   <li><strong>build_log_data:</strong> Jenkins build console output &amp; execution logs</li>
     *   <li><strong>secret_detection:</strong> Credential scanning &amp; secret exposure detection</li>
     *   <li><strong>dependency_data:</strong> Build dependencies, artifacts &amp; plugin information</li>
     *   <li><strong>code_changes:</strong> Version control changes &amp; commit information</li>
     *   <li><strong>additional_info_agent:</strong> Jenkins agent system metrics &amp; performance</li>
     *   <li><strong>additional_info_controller:</strong> Jenkins controller system metrics &amp; health</li>
     *   <li><strong>sast_scanning:</strong> Static Application Security Testing results</li>
     * </ul>
     */
    private String type;

    /**
     * ISO-8601 formatted timestamp for chronological ordering &amp; temporal analysis.
     * <p>Provides precise temporal information for log events, enabling chronological
     * ordering, time-based analysis, and correlation of events across different log types.</p>
     *
     * <h4>Format Specification:</h4>
     * <ul>
     *   <li><strong>Standard:</strong> ISO-8601 format (e.g., "2024-01-15T10:30:00Z")</li>
     *   <li><strong>Timezone:</strong> UTC timezone for consistency across distributed systems</li>
     *   <li><strong>Precision:</strong> Millisecond precision for high-resolution timing</li>
     *   <li><strong>Sortability:</strong> Lexicographically sortable for efficient processing</li>
     * </ul>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "2024-01-15T10:30:00.000Z"} - Build start timestamp</li>
     *   <li>{@code "2024-01-15T10:31:15.234Z"} - Security scan completion</li>
     *   <li>{@code "2024-01-15T10:32:45.567Z"} - Dependency analysis result</li>
     * </ul>
     */
    private String timestamp;

    /**
     * Jenkins build number for correlation &amp; tracking across log types.
     * <p>Enables correlation of different log types generated during the same Jenkins
     * build execution, facilitating comprehensive build analysis and cross-log insights.</p>
     *
     * <h4>Correlation Benefits:</h4>
     * <ul>
     *   <li><strong>Build Tracking:</strong> Links all log types to specific build executions</li>
     *   <li><strong>Cross-Log Analysis:</strong> Enables analysis across different log sources</li>
     *   <li><strong>Trend Analysis:</strong> Facilitates build-over-build comparison</li>
     *   <li><strong>Debugging Support:</strong> Provides context for troubleshooting</li>
     * </ul>
     */
    private int buildNumber;

    /**
     * Creates a new instance of the class with default initial values.
     * This constructor initializes the object to its default state.
     */
    public TypedLog() {
    }


    // ========================================================================
    // PROPERTY ACCESSORS - TYPE MANAGEMENT
    // ========================================================================

    /**
     * Gets the log type identifier for polymorphic serialization &amp; routing.
     * @return the log type identifier, never {@code null} for properly initialized instances
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the log type identifier for polymorphic serialization &amp; routing.
     * <p>This method is typically called during object initialization to establish
     * the correct type for Jackson serialization and message routing.</p>
     *
     * @param type the log type identifier to set, should match one of the supported types
     */
    public void setType(String type) {
        this.type = type;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - TEMPORAL COORDINATION
    // ========================================================================

    /**
     * Gets the ISO-8601 formatted timestamp for chronological ordering.
     * @return the timestamp string, or {@code null} if not set
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the ISO-8601 formatted timestamp for chronological ordering.
     * <p>Should be set to the current time when the log event occurs, using
     * UTC timezone for consistency across distributed systems.</p>
     *
     * @param timestamp the ISO-8601 formatted timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - BUILD CORRELATION
    // ========================================================================

    /**
     * Gets the Jenkins build number for correlation &amp; tracking.
     * @return the build number, or 0 if not set
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    // ========================================================================
    // ABSTRACT METHOD CONTRACTS
    // ========================================================================

    /**
     * Generates structured content optimized for AI-powered analysis &amp; anomaly detection.
     *
     * <p>This abstract method defines the contract for content extraction across all log types.
     * Each concrete implementation must provide type-specific content processing that transforms
     * raw log data into clean, structured content suitable for AI analysis, security assessment,
     * and anomaly detection.</p>
     *
     * <h4>Implementation Requirements:</h4>
     * <ul>
     *   <li><strong>Content Cleaning:</strong> Remove noise, normalize formatting &amp; handle encoding</li>
     *   <li><strong>Structure Preservation:</strong> Maintain logical structure &amp; relationships</li>
     *   <li><strong>Context Enrichment:</strong> Include relevant metadata &amp; contextual information</li>
     *   <li><strong>Error Handling:</strong> Graceful processing of malformed or incomplete data</li>
     *   <li><strong>Compression Support:</strong> Handle GZIP-compressed content when applicable</li>
     * </ul>
     *
     * <h4>Type-Specific Processing Examples:</h4>
     * <p>Build log content processing:</p>
     * {@snippet lang="text" :
     * Build Status: SUCCESS
     * Build Duration: 2m 15s
     * Console Output: Started by user admin
     * [Pipeline] Start of Pipeline
     * [Pipeline] node
     * Running on Jenkins in /var/jenkins_home/workspace/security-pipeline
     * }
     *
     * <p>Secret detection content processing:</p>
     * {@snippet lang="text" :
     * Source: src/main/resources/application.properties
     * Message: Configuration file secret scan completed
     * Content: database.password=mySecretPassword123
     * Secrets:
     * - Type: database_password, Values: [mySecretPassword123]
     * - Type: api_key, Values: [sk-1234567890abcdef]
     * }
     *
     * <p>SAST scanning content processing:</p>
     * {@snippet lang="text" :
     * Repository URL: https://github.com/company/security-app.git
     * Branch: main
     * Tool: SonarQube
     * Status: SUCCESS
     * Scan Duration (seconds): 45.7
     * Scan Result: {"vulnerabilities":[{"severity":"HIGH","type":"SQL_INJECTION"}]}
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Security Analysis:</strong> Detection of vulnerabilities, secrets &amp; threats</li>
     *   <li><strong>Anomaly Detection:</strong> Identification of unusual patterns &amp; behaviors</li>
     *   <li><strong>Performance Monitoring:</strong> Analysis of system performance metrics</li>
     *   <li><strong>Compliance Checking:</strong> Verification against security policies</li>
     *   <li><strong>Trend Analysis:</strong> Historical pattern recognition &amp; forecasting</li>
     * </ul>
     *
     * @return structured string content optimized for AI analysis, containing cleaned
     *         and formatted log information ready for machine learning processing
     * @throws Exception if content processing fails due to data corruption, resource
     *                   constraints, or other critical errors that cannot be handled gracefully
     *
     * @see Log#getContentToAnalyze()
     */
    @Override
    public abstract String getContentToAnalyze() throws Exception;

    /**
     * Provides the conversation identifier for grouping related log entries.
     *
     * <p>This abstract method defines the contract for conversation identification across
     * all log types. Each concrete implementation must provide a conversation identifier
     * that enables logical grouping of related log entries for context preservation
     * and comprehensive analysis.</p>
     *
     * <h4>Implementation Requirements:</h4>
     * <ul>
     *   <li><strong>Consistency:</strong> Same identifier for related log entries</li>
     *   <li><strong>Uniqueness:</strong> Distinct identifiers for different conversations</li>
     *   <li><strong>Persistence:</strong> Stable identifiers across system restarts</li>
     *   <li><strong>Meaningful Names:</strong> Human-readable identifiers when possible</li>
     * </ul>
     *
     * <h4>Common Implementation Patterns:</h4>
     * <ul>
     *   <li><strong>Job-Based:</strong> Jenkins job name for job-specific conversations</li>
     *   <li><strong>Repository-Based:</strong> Repository URL for repository-specific analysis</li>
     *   <li><strong>Build-Based:</strong> Build identifier for build-specific grouping</li>
     *   <li><strong>Custom Logic:</strong> Application-specific conversation logic</li>
     * </ul>
     *
     * <h4>Conversation Grouping Benefits:</h4>
     * <ul>
     *   <li><strong>Context Preservation:</strong> Maintains logical relationships between entries</li>
     *   <li><strong>Historical Analysis:</strong> Enables trend analysis across conversations</li>
     *   <li><strong>Memory Management:</strong> Facilitates efficient conversation-based cleanup</li>
     *   <li><strong>AI Context:</strong> Provides rich context for machine learning analysis</li>
     * </ul>
     *
     * <h4>Integration with Chat Memory:</h4>
     * <p>The conversation ID is used by {@link com.diploma.inno.component.SimpleDbChatMemory} for:</p>
     * <ul>
     *   <li>Message storage and retrieval</li>
     *   <li>Conversation history management</li>
     *   <li>Context preparation for AI analysis</li>
     *   <li>Automatic cleanup and archival</li>
     * </ul>
     *
     * @return the conversation identifier for grouping related log entries,
     *         or {@code null} if no conversation context is available
     *
     * @see Log#getConversationId()
     * @see com.diploma.inno.component.SimpleDbChatMemory#add(String, List)
     * @see com.diploma.inno.component.SimpleDbChatMemory#get(String, int)
     */
    @Override
    public abstract String getConversationId();

}