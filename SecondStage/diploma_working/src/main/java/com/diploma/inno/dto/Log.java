package com.diploma.inno.dto;

/**
 * Core interface for Jenkins log data objects in CI/CD anomaly detection &amp; security analysis.
 *
 * <p>This interface defines the fundamental contract for all log data types within the Jenkins
 * anomaly detection pipeline. It establishes a unified API for log content processing, conversation
 * management, temporal tracking, and data serialization across diverse log sources including build
 * logs, system metrics, dependency information, and code changes.</p>
 *
 * <h2>Core Responsibilities</h2>
 * <ul>
 *   <li><strong>Content Analysis:</strong> Provides structured content for AI-powered anomaly detection</li>
 *   <li><strong>Conversation Management:</strong> Enables grouping of related log entries for context</li>
 *   <li><strong>Temporal Tracking:</strong> Maintains chronological ordering of log events</li>
 *   <li><strong>Type Classification:</strong> Categorizes log entries for proper routing &amp; processing</li>
 *   <li><strong>Build Correlation:</strong> Links log entries to specific Jenkins build executions</li>
 *   <li><strong>Data Serialization:</strong> Supports JSON-based data exchange &amp; storage</li>
 * </ul>
 *
 * <h2>Implementation Architecture</h2>
 * <p>The Log interface serves as the foundation for a comprehensive log processing hierarchy:</p>
 * <pre>{@code
 * Log (Interface)
 *  ├── TypedLog (Abstract Base Class)
 *  │    ├── BuildLogData (Build Console Output)
 *  │    ├── CodeChanges (Version Control Information)
 *  │    ├── DependencyData (Artifact &amp; Dependency Details)
 *  │    ├── AdditionalInfoAgent (Agent System Metrics)
 *  │    └── AdditionalInfoController (Controller System Metrics)
 *  └── Custom Implementations (Extensible)
 * }</pre>
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The interface enables sophisticated AI-powered analysis through structured content provision:</p>
 * <ul>
 *   <li><strong>Security Analysis:</strong> Detection of vulnerabilities, secrets &amp; compliance violations</li>
 *   <li><strong>Anomaly Detection:</strong> Identification of unusual patterns &amp; behaviors</li>
 *   <li><strong>Performance Monitoring:</strong> Analysis of system performance &amp; resource utilization</li>
 *   <li><strong>Trend Analysis:</strong> Historical pattern recognition &amp; forecasting</li>
 *   <li><strong>Risk Assessment:</strong> Evaluation of security &amp; operational risks</li>
 * </ul>
 *
 * <h2>Conversation-Based Processing</h2>
 * <p>The interface supports conversation-based log grouping for enhanced context:</p>
 * {@snippet lang="json" :
 * {
 *   "conversation_id": "security-pipeline",
 *   "messages": [
 *     {
 *       "type": "build_log_data",
 *       "timestamp": "2024-01-15T10:30:00Z",
 *       "build_number": 123,
 *       "content": "Build started for security analysis..."
 *     },
 *     {
 *       "type": "code_changes",
 *       "timestamp": "2024-01-15T10:31:00Z",
 *       "build_number": 123,
 *       "content": "Security fix in authentication module..."
 *     },
 *     {
 *       "type": "dependency_data",
 *       "timestamp": "2024-01-15T10:32:00Z",
 *       "build_number": 123,
 *       "content": "Updated Spring Security to 5.7.2..."
 *     }
 *   ]
 * }
 * }
 *
 * <h2>Data Flow &amp; Processing Pipeline</h2>
 * <p>Log implementations participate in a comprehensive data processing pipeline:</p>
 * <ol>
 *   <li><strong>Data Ingestion:</strong> Jenkins events → RabbitMQ → LogMessageListener</li>
 *   <li><strong>Deserialization:</strong> JSON messages → Log implementations</li>
 *   <li><strong>Content Processing:</strong> Raw data → Structured analysis content</li>
 *   <li><strong>Conversation Grouping:</strong> Related logs → Conversation contexts</li>
 *   <li><strong>AI Analysis:</strong> Structured content → Anomaly detection results</li>
 *   <li><strong>Storage &amp; Retrieval:</strong> Processed data → Database persistence</li>
 * </ol>
 *
 * <h2>Type System &amp; Classification</h2>
 * <p>The interface supports a comprehensive type system for log classification:</p>
 * <ul>
 *   <li><strong>build_log_data:</strong> Jenkins build console output &amp; execution logs</li>
 *   <li><strong>code_changes:</strong> Version control changes &amp; commit information</li>
 *   <li><strong>dependency_data:</strong> Build dependencies, artifacts &amp; plugin information</li>
 *   <li><strong>additional_info_agent:</strong> Jenkins agent system metrics &amp; performance data</li>
 *   <li><strong>additional_info_controller:</strong> Jenkins controller system metrics &amp; health data</li>
 * </ul>
 *
 * <h2>Temporal Coordination &amp; Sequencing</h2>
 * <p>The interface enables precise temporal coordination of log events:</p>
 * <ul>
 *   <li><strong>Chronological Ordering:</strong> Timestamp-based event sequencing</li>
 *   <li><strong>Build Correlation:</strong> Build number-based event grouping</li>
 *   <li><strong>Causal Analysis:</strong> Event dependency &amp; causation tracking</li>
 *   <li><strong>Timeline Reconstruction:</strong> Complete build execution timeline assembly</li>
 * </ul>
 *
 * <h2>Serialization &amp; Data Exchange</h2>
 * <p>The interface mandates JSON serialization for consistent data exchange:</p>
 * <ul>
 *   <li><strong>Message Queue Integration:</strong> RabbitMQ message serialization</li>
 *   <li><strong>Database Storage:</strong> Persistent log data storage</li>
 *   <li><strong>API Communication:</strong> REST API data exchange</li>
 *   <li><strong>Inter-Service Communication:</strong> Microservice data sharing</li>
 * </ul>
 *
 * <h2>Error Handling &amp; Resilience</h2>
 * <p>Implementations must provide robust error handling:</p>
 * <ul>
 *   <li><strong>Content Processing Errors:</strong> Graceful handling of malformed data</li>
 *   <li><strong>Serialization Failures:</strong> Fallback mechanisms for JSON processing</li>
 *   <li><strong>Resource Constraints:</strong> Memory &amp; performance optimization</li>
 *   <li><strong>Network Issues:</strong> Resilient data transmission &amp; retry logic</li>
 * </ul>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li><strong>Data Sanitization:</strong> Safe handling of potentially sensitive log content</li>
 *   <li><strong>Access Control:</strong> Secure log data access &amp; processing</li>
 *   <li><strong>Privacy Protection:</strong> Careful handling of personal &amp; confidential information</li>
 *   <li><strong>Audit Compliance:</strong> Comprehensive logging &amp; audit trail maintenance</li>
 * </ul>
 *
 * <h2>Performance &amp; Scalability</h2>
 * <ul>
 *   <li><strong>Memory Efficiency:</strong> Optimized memory usage for large log volumes</li>
 *   <li><strong>Processing Speed:</strong> Fast content analysis &amp; serialization</li>
 *   <li><strong>Concurrent Access:</strong> Thread-safe implementations for high-volume processing</li>
 *   <li><strong>Resource Management:</strong> Efficient resource allocation &amp; cleanup</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This interface integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes log messages from RabbitMQ</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores log data for conversation management</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides structured content for anomaly detection</li>
 *   <li><strong>Monitoring Dashboard:</strong> Supplies real-time log data for visualization</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see TypedLog
 * @see BuildLogData
 * @see CodeChanges
 * @see DependencyData
 * @see AdditionalInfoAgent
 * @see AdditionalInfoController
 * @see com.diploma.inno.component.LogMessageListener
 * @see com.diploma.inno.component.SimpleDbChatMemory
 */
public interface Log {

    // ========================================================================
    // CONTENT ANALYSIS &amp; AI INTEGRATION
    // ========================================================================

    /**
     * Generates structured content optimized for AI-powered anomaly detection analysis.
     *
     * <p>This method is the cornerstone of the AI analysis pipeline, transforming raw log data
     * into clean, structured content that can be effectively processed by machine learning
     * models for security analysis, anomaly detection, and pattern recognition.</p>
     *
     * <h4>Content Processing Requirements:</h4>
     * <ul>
     *   <li><strong>Data Cleaning:</strong> Remove noise, normalize formatting &amp; handle encoding issues</li>
     *   <li><strong>Structure Preservation:</strong> Maintain logical structure &amp; hierarchical relationships</li>
     *   <li><strong>Context Enrichment:</strong> Include relevant metadata &amp; contextual information</li>
     *   <li><strong>Error Handling:</strong> Graceful processing of malformed or incomplete data</li>
     * </ul>
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Security Analysis:</strong> Detection of vulnerabilities, secrets &amp; threats</li>
     *   <li><strong>Anomaly Detection:</strong> Identification of unusual patterns &amp; behaviors</li>
     *   <li><strong>Performance Monitoring:</strong> Analysis of system performance metrics</li>
     *   <li><strong>Compliance Checking:</strong> Verification against security policies &amp; standards</li>
     *   <li><strong>Trend Analysis:</strong> Historical pattern recognition &amp; forecasting</li>
     * </ul>
     *
     * <h4>Content Format Examples:</h4>
     * <p>Build log content for security analysis:</p>
     * {@snippet lang="text" :
     * Build File Content: <project><groupId>com.company</groupId><artifactId>security-app</artifactId>...
     * Dependencies (Type: maven):
     * - Group: org.springframework.security, Artifact: spring-security-core, Version: 5.7.2
     * - Group: org.apache.commons, Artifact: commons-lang3, Version: 3.12.0
     * Build Status: SUCCESS
     * Security Scan Results: No vulnerabilities detected
     * }
     *
     * <p>System metrics content for performance analysis:</p>
     * {@snippet lang="text" :
     * Used Memory: 512MB
     * Max Memory: 2048MB
     * System CPU Load: 0.45
     * Active Thread Count: 12
     * System Load Average: 1.25
     * Free Disk Space: 50240MB
     * }
     *
     * <h4>Error Handling:</h4>
     * <p>Implementations must handle various error scenarios:</p>
     * <ul>
     *   <li><strong>Data Corruption:</strong> Graceful handling of corrupted log data</li>
     *   <li><strong>Missing Information:</strong> Appropriate defaults for missing fields</li>
     *   <li><strong>Processing Failures:</strong> Fallback mechanisms for content generation</li>
     *   <li><strong>Resource Constraints:</strong> Memory-efficient processing of large datasets</li>
     * </ul>
     *
     * @return structured string content optimized for AI analysis, containing cleaned
     *         and formatted log information ready for machine learning processing
     * @throws Exception if content processing fails due to data corruption, resource
     *                   constraints, or other critical errors that cannot be handled gracefully
     *
     * @see TypedLog#getContentToAnalyze()
     */
    String getContentToAnalyze() throws Exception;

    // ========================================================================
    // CONVERSATION MANAGEMENT &amp; GROUPING
    // ========================================================================

    /**
     * Provides the conversation identifier for grouping related log entries.
     *
     * <p>This method returns a unique identifier that enables the grouping of related log
     * entries into logical conversations. This is essential for maintaining context across
     * multiple log events within the same Jenkins job execution or related build processes.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>The conversation ID typically corresponds to the Jenkins job name, enabling:</p>
     * <ul>
     *   <li><strong>Context Preservation:</strong> Maintains logical relationships between log entries</li>
     *   <li><strong>Historical Analysis:</strong> Enables trend analysis across job executions</li>
     *   <li><strong>Memory Management:</strong> Facilitates efficient conversation-based cleanup</li>
     *   <li><strong>AI Context:</strong> Provides rich context for machine learning analysis</li>
     * </ul>
     *
     * <h4>Usage in Chat Memory:</h4>
     * <p>The conversation ID is used by {@link com.diploma.inno.component.SimpleDbChatMemory} for:</p>
     * <ul>
     *   <li>Message storage and retrieval</li>
     *   <li>Conversation history management</li>
     *   <li>Context preparation for AI analysis</li>
     *   <li>Automatic cleanup and archival</li>
     * </ul>
     *
     * <h4>Conversation Examples:</h4>
     * {@snippet lang="json" :
     * {
     *   "conversation_id": "security-pipeline",
     *   "participants": ["build_log_data", "code_changes", "dependency_data"],
     *   "message_count": 15,
     *   "last_activity": "2024-01-15T10:35:00Z"
     * }
     * }
     *
     * @return the conversation identifier, typically the Jenkins job name,
     *         or {@code null} if no conversation context is available
     *
     * @see com.diploma.inno.component.SimpleDbChatMemory#add(String, java.util.List)
     * @see com.diploma.inno.component.SimpleDbChatMemory#get(String, int)
     */
    String getConversationId();

    // ========================================================================
    // TEMPORAL TRACKING &amp; CHRONOLOGICAL ORDERING
    // ========================================================================

    /**
     * Provides the timestamp for chronological ordering of log events.
     *
     * <p>This method returns a timestamp that enables precise chronological ordering of log
     * events within the CI/CD pipeline. Accurate temporal information is crucial for causal
     * analysis, timeline reconstruction, and temporal pattern detection in anomaly analysis.</p>
     *
     * <h4>Timestamp Format &amp; Standards:</h4>
     * <ul>
     *   <li><strong>ISO-8601 Format:</strong> Standard timestamp format for interoperability</li>
     *   <li><strong>UTC Timezone:</strong> Consistent timezone handling across distributed systems</li>
     *   <li><strong>Millisecond Precision:</strong> High-resolution timing for precise ordering</li>
     *   <li><strong>Sortable Format:</strong> Lexicographically sortable for efficient processing</li>
     * </ul>
     *
     * <h4>Temporal Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Event Sequencing:</strong> Precise ordering of build pipeline events</li>
     *   <li><strong>Causal Analysis:</strong> Identification of cause-and-effect relationships</li>
     *   <li><strong>Performance Metrics:</strong> Duration calculations &amp; timing analysis</li>
     *   <li><strong>Anomaly Detection:</strong> Temporal pattern recognition &amp; deviation detection</li>
     * </ul>
     *
     * <h4>Timeline Reconstruction:</h4>
     * <p>Timestamps enable comprehensive timeline reconstruction:</p>
     * {@snippet lang="json" :
     * {
     *   "timeline": [
     *     {
     *       "timestamp": "2024-01-15T10:30:00.000Z",
     *       "event": "build_started",
     *       "type": "build_log_data"
     *     },
     *     {
     *       "timestamp": "2024-01-15T10:31:15.234Z",
     *       "event": "code_analysis",
     *       "type": "code_changes"
     *     },
     *     {
     *       "timestamp": "2024-01-15T10:32:45.567Z",
     *       "event": "dependency_resolution",
     *       "type": "dependency_data"
     *     }
     *   ]
     * }
     * }
     *
     * @return ISO-8601 formatted timestamp string in UTC timezone,
     *         or {@code null} if timestamp information is not available
     *
     * @see java.time.Instant
     * @see java.time.format.DateTimeFormatter#ISO_INSTANT
     */
    String getTimestamp();

    // ========================================================================
    // TYPE CLASSIFICATION &amp; ROUTING
    // ========================================================================

    /**
     * Provides the log type identifier for classification &amp; routing.
     *
     * <p>This method returns a type identifier that enables proper classification and routing
     * of log entries within the anomaly detection pipeline. The type determines how the log
     * data is processed, analyzed, and stored by various system components.</p>
     *
     * <h4>Supported Log Types:</h4>
     * <ul>
     *   <li><strong>build_log_data:</strong> Jenkins build console output &amp; execution logs</li>
     *   <li><strong>code_changes:</strong> Version control changes &amp; commit information</li>
     *   <li><strong>dependency_data:</strong> Build dependencies, artifacts &amp; plugin information</li>
     *   <li><strong>additional_info_agent:</strong> Jenkins agent system metrics &amp; performance</li>
     *   <li><strong>additional_info_controller:</strong> Jenkins controller system metrics &amp; health</li>
     * </ul>
     *
     * <h4>Type-Based Processing:</h4>
     * <p>Different log types receive specialized processing:</p>
     * <ul>
     *   <li><strong>Content Analysis:</strong> Type-specific content extraction &amp; formatting</li>
     *   <li><strong>Security Scanning:</strong> Type-appropriate security analysis techniques</li>
     *   <li><strong>Performance Monitoring:</strong> Type-specific performance metrics &amp; thresholds</li>
     *   <li><strong>Storage Optimization:</strong> Type-optimized storage &amp; indexing strategies</li>
     * </ul>
     *
     * <h4>Routing &amp; Classification:</h4>
     * {@snippet lang="json" :
     * {
     *   "message_routing": {
     *     "build_log_data": {
     *       "processor": "BuildLogProcessor",
     *       "analyzer": "SecurityLogAnalyzer",
     *       "storage": "BuildLogStorage"
     *     },
     *     "dependency_data": {
     *       "processor": "DependencyProcessor",
     *       "analyzer": "VulnerabilityAnalyzer",
     *       "storage": "DependencyStorage"
     *     }
     *   }
     * }
     * }
     *
     * @return the log type identifier string, never {@code null}
     *
     * @see TypedLog#setType(String)
     * @see com.diploma.inno.component.LogMessageListener
     */
    String getType();

    // ========================================================================
    // BUILD CORRELATION &amp; TRACKING
    // ========================================================================

    /**
     * Provides the Jenkins build number for correlation &amp; tracking.
     *
     * <p>This method returns the Jenkins build number that enables correlation of log entries
     * with specific build executions. Build numbers provide a crucial link between different
     * types of log data generated during the same build process.</p>
     *
     * <h4>Build Correlation Benefits:</h4>
     * <ul>
     *   <li><strong>Event Correlation:</strong> Links related events across different log types</li>
     *   <li><strong>Build Tracking:</strong> Enables complete build execution monitoring</li>
     *   <li><strong>Trend Analysis:</strong> Facilitates build-over-build comparison &amp; analysis</li>
     *   <li><strong>Debugging Support:</strong> Provides context for troubleshooting build issues</li>
     * </ul>
     *
     * <h4>Cross-Log Correlation:</h4>
     * <p>Build numbers enable correlation across different log types:</p>
     * {@snippet lang="json" :
     * {
     *   "build_123": {
     *     "build_log_data": {
     *       "status": "SUCCESS",
     *       "duration": "2m 15s"
     *     },
     *     "code_changes": {
     *       "commits": 3,
     *       "files_changed": 12
     *     },
     *     "dependency_data": {
     *       "dependencies": 45,
     *       "vulnerabilities": 0
     *     }
     *   }
     * }
     * }
     *
     * <h4>Build Progression Tracking:</h4>
     * <ul>
     *   <li><strong>Sequential Analysis:</strong> Track changes across consecutive builds</li>
     *   <li><strong>Regression Detection:</strong> Identify when issues are introduced</li>
     *   <li><strong>Performance Trends:</strong> Monitor build performance over time</li>
     *   <li><strong>Quality Metrics:</strong> Track code quality &amp; security metrics progression</li>
     * </ul>
     *
     * @return the Jenkins build number as a positive integer,
     *         or 0 if build number information is not available
     *
     * @see com.cdancy.jenkins.rest.JenkinsClient
     * @see com.cdancy.jenkins.rest.JenkinsApi
     */
    int getBuildNumber();

    // ========================================================================
    // SERIALIZATION &amp; DATA EXCHANGE
    // ========================================================================

    /**
     * Converts the log data to JSON format for serialization &amp; data exchange.
     *
     * <p>This method provides JSON serialization of the complete log data structure,
     * enabling consistent data exchange across system components, persistent storage,
     * and inter-service communication within the anomaly detection pipeline.</p>
     *
     * <h4>Serialization Requirements:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serialize all relevant log data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Graceful handling of null &amp; missing values</li>
     *   <li><strong>Error Recovery:</strong> Robust error handling with fallback mechanisms</li>
     *   <li><strong>Format Consistency:</strong> Consistent JSON structure across implementations</li>
     * </ul>
     *
     * <h4>JSON Structure Standards:</h4>
     * <p>All implementations should follow a consistent JSON structure:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "build_log_data",
     *   "timestamp": "2024-01-15T10:30:00.000Z",
     *   "conversation_id": "security-pipeline",
     *   "build_number": 123,
     *   "data": {
     *     "content": "...",
     *     "metadata": "{...}"
     *   },
     *   "error": null
     * }
     * }
     *
     * <h4>Usage Scenarios:</h4>
     * <ul>
     *   <li><strong>Message Queue:</strong> RabbitMQ message serialization for inter-service communication</li>
     *   <li><strong>Database Storage:</strong> Persistent storage in chat memory &amp; audit logs</li>
     *   <li><strong>API Communication:</strong> REST API request/response data exchange</li>
     *   <li><strong>Monitoring &amp; Logging:</strong> Structured logging for debugging &amp; monitoring</li>
     * </ul>
     *
     * <h4>Error Handling:</h4>
     * <p>Implementations must provide robust error handling:</p>
     * <ul>
     *   <li><strong>Serialization Failures:</strong> Return fallback JSON on processing errors</li>
     *   <li><strong>Large Data Handling:</strong> Efficient processing of large log datasets</li>
     *   <li><strong>Encoding Issues:</strong> Proper handling of character encoding problems</li>
     *   <li><strong>Memory Constraints:</strong> Memory-efficient serialization for large objects</li>
     * </ul>
     *
     * @return JSON string representation of the log data,
     *         or a fallback JSON object (e.g., "{}") if serialization fails
     *
     * @see com.fasterxml.jackson.databind.ObjectMapper
     * @see com.fasterxml.jackson.core.JsonProcessingException
     */
    String toJson();
}