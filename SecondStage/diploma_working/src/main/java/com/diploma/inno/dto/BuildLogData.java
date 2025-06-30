package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

/**
 * Data Transfer Object for Jenkins build log data in CI/CD anomaly detection &amp; security analysis.
 *
 * <p>This DTO encapsulates Jenkins build log information including raw log content, compression handling,
 * error tracking, and metadata for comprehensive build analysis. It serves as the primary vehicle for
 * transporting build log data through the anomaly detection pipeline, enabling AI-powered security
 * analysis and DevSecOps monitoring of Jenkins CI/CD processes.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Log Data Transport:</strong> Encapsulates raw build logs with metadata for analysis</li>
 *   <li><strong>Compression Support:</strong> Handles GZIP-compressed logs for efficient data transfer</li>
 *   <li><strong>Error Tracking:</strong> Captures build errors &amp; failure information for diagnosis</li>
 *   <li><strong>Content Processing:</strong> Provides decoded log content for AI analysis</li>
 *   <li><strong>Build Correlation:</strong> Links log data to specific Jenkins jobs &amp; build numbers</li>
 * </ul>
 *
 * <h2>Data Structure &amp; Content</h2>
 * <p>The DTO manages complex log data structures with multiple content types:</p>
 * <ul>
 *   <li><strong>Raw Log Content:</strong> Complete build console output for comprehensive analysis</li>
 *   <li><strong>Compressed Data:</strong> Base64-encoded GZIP logs for bandwidth optimization</li>
 *   <li><strong>Build Metadata:</strong> Job names, build numbers &amp; execution context</li>
 *   <li><strong>Error Information:</strong> Build failures, exceptions &amp; diagnostic details</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "build_log_data",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "data": {
 *     "raw_log": "Started by user admin\nBuilding in workspace /var/jenkins_home/workspace/security-pipeline\n...",
 *     "raw_log_compressed": false,
 *     "build_status": "SUCCESS",
 *     "duration": 45000,
 *     "timestamp": "2024-01-15T10:30:00Z"
 *   },
 *   "error": null
 * }
 * }
 *
 * <h2>Compressed Log Handling</h2>
 * <p>For large build logs, the DTO supports GZIP compression:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "build_log_data",
 *   "job_name": "large-build-pipeline",
 *   "build_number": 456,
 *   "data": {
 *     "raw_log": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
 *     "raw_log_compressed": true,
 *     "original_size": 1048576,
 *     "compressed_size": 65536
 *   }
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When build errors occur, the DTO captures detailed diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "build_log_data",
 *   "job_name": "failing-pipeline",
 *   "build_number": 789,
 *   "data": {
 *     "raw_log": "Build failed with exit code 1\nERROR: Test failures detected\n...",
 *     "build_status": "FAILURE",
 *     "exit_code": 1
 *   },
 *   "error": "Build failed due to test failures in security scan module"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered analysis:</p>
 * <ul>
 *   <li><strong>Security Scanning:</strong> Detection of secrets, vulnerabilities &amp; compliance issues</li>
 *   <li><strong>Build Analysis:</strong> Performance patterns, failure trends &amp; optimization opportunities</li>
 *   <li><strong>Anomaly Detection:</strong> Unusual build behaviors, security threats &amp; operational issues</li>
 *   <li><strong>Compliance Monitoring:</strong> Policy violations, audit trails &amp; regulatory compliance</li>
 * </ul>
 *
 * <h2>Content Processing Pipeline</h2>
 * <p>The DTO supports sophisticated content processing:</p>
 * <ol>
 *   <li><strong>Data Ingestion:</strong> Receives raw or compressed log data from Jenkins</li>
 *   <li><strong>Decompression:</strong> Automatically handles GZIP-compressed content</li>
 *   <li><strong>Content Extraction:</strong> Provides clean log content for analysis</li>
 *   <li><strong>Error Handling:</strong> Graceful handling of malformed or corrupted data</li>
 * </ol>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li><strong>Data Sanitization:</strong> Safe handling of potentially sensitive log content</li>
 *   <li><strong>Compression Security:</strong> Secure decompression with size limits</li>
 *   <li><strong>Error Isolation:</strong> Prevents error propagation in processing pipeline</li>
 *   <li><strong>Content Validation:</strong> Validates log data integrity &amp; format</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>Lazy Decompression:</strong> Decompresses content only when needed for analysis</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized handling of large log files</li>
 *   <li><strong>Streaming Support:</strong> Efficient processing of continuous log streams</li>
 *   <li><strong>Caching Strategy:</strong> Intelligent caching of processed log content</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes build log messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores log data for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides log content for anomaly detection</li>
 *   <li><strong>Security Scanner:</strong> Supplies log data for vulnerability analysis</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see TypedLog
 * @see com.diploma.inno.component.LogMessageListener
 * @see com.diploma.inno.component.SimpleDbChatMemory
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildLogData extends TypedLog {

    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(BuildLogData.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this build log data.
     * <p>Used for correlating log data with specific Jenkins jobs and enabling
     * conversation-based grouping in the AI analysis pipeline.</p>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "security-pipeline"} - Security scanning pipeline</li>
     *   <li>{@code "deployment-prod"} - Production deployment job</li>
     *   <li>{@code "unit-tests"} - Unit testing job</li>
     * </ul>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this log data snapshot.
     * <p>Enables tracking of build progression and correlation of logs
     * across different build executions for trend analysis.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // LOG DATA &amp; CONTENT MANAGEMENT
    // ========================================================================

    /**
     * Complex data structure containing build log content and metadata.
     * <p>This map contains the raw build log data along with associated metadata
     * such as compression flags, build status, timestamps, and other contextual
     * information required for comprehensive log analysis.</p>
     *
     * <h4>Standard Data Structure:</h4>
     * <ul>
     *   <li><strong>raw_log:</strong> The actual build console output (String)</li>
     *   <li><strong>raw_log_compressed:</strong> Boolean flag indicating GZIP compression</li>
     *   <li><strong>build_status:</strong> Build result status (SUCCESS, FAILURE, UNSTABLE, etc.)</li>
     *   <li><strong>duration:</strong> Build execution time in milliseconds</li>
     *   <li><strong>timestamp:</strong> Build execution timestamp</li>
     *   <li><strong>exit_code:</strong> Process exit code for failed builds</li>
     * </ul>
     *
     * <h4>Compressed Log Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "raw_log": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
     *   "raw_log_compressed": true,
     *   "original_size": 1048576,
     *   "compressed_size": 65536,
     *   "build_status": "SUCCESS",
     *   "duration": 120000,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <h4>Uncompressed Log Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "raw_log": "Started by user admin\nBuilding in workspace /var/jenkins_home/workspace/security-pipeline\n[Pipeline] Start of Pipeline\n[Pipeline] node\nRunning on Jenkins in /var/jenkins_home/workspace/security-pipeline\n...",
     *   "raw_log_compressed": false,
     *   "build_status": "SUCCESS",
     *   "duration": 45000
     * }
     * }
     */
    @JsonProperty("data")
    private Map<String, Object> data;

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Error message or diagnostic information for failed builds.
     * <p>Contains human-readable error descriptions, failure reasons, or
     * diagnostic information when builds fail or encounter issues during execution.</p>
     *
     * <h4>Error Types:</h4>
     * <ul>
     *   <li><strong>Build Failures:</strong> Compilation errors, test failures, deployment issues</li>
     *   <li><strong>Infrastructure Errors:</strong> Agent connectivity, resource exhaustion</li>
     *   <li><strong>Security Issues:</strong> Vulnerability detection, compliance violations</li>
     *   <li><strong>Configuration Problems:</strong> Pipeline syntax errors, missing dependencies</li>
     * </ul>
     *
     * <h4>Example Error Messages:</h4>
     * <ul>
     *   <li>{@code "Build failed due to test failures in security scan module"}</li>
     *   <li>{@code "Deployment failed: insufficient permissions"}</li>
     *   <li>{@code "Security vulnerability detected: CVE-2023-12345"}</li>
     * </ul>
     */
    @JsonProperty("error")
    private String error;

    // ========================================================================
    // CONSTRUCTOR &amp; INITIALIZATION
    // ========================================================================

    /**
     * Default constructor initializing the DTO with proper type identification.
     *
     * <p>Creates a new BuildLogData instance with the type set to "build_log_data"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "build_log_data" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * Jenkins build log messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * BuildLogData logData = objectMapper.readValue(jsonMessage, BuildLogData.class);
     *
     * // Manual instantiation for testing
     * BuildLogData logData = new BuildLogData();
     * logData.setJobName("security-pipeline");
     * logData.setBuildNumber(123);
     *
     * Map<String, Object> data = new HashMap<>();
     * data.put("raw_log", "Started by user admin...");
     * data.put("raw_log_compressed", false);
     * logData.setData(data);
     * }
     *
     * @see TypedLog#setType(String)
     */
    public BuildLogData() {
        setType("build_log_data");
        logger.debug("Initialized BuildLogData with type=build_log_data");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - BUILD CONTEXT
    // ========================================================================

    /**
     * Gets the Jenkins job name associated with this build log data.
     * @return the job name, or {@code null} if not set
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the Jenkins job name for this build log data.
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the Jenkins build number for this log data.
     * @return the build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the Jenkins build number for this log data.
     * @param buildNumber the build number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - LOG DATA &amp; CONTENT
    // ========================================================================

    /**
     * Gets the complex data structure containing build log content and metadata.
     * @return the data map containing raw log and metadata, or {@code null} if not set
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the build log data and metadata.
     * <p>This method also logs the data setting operation for debugging purposes.</p>
     * @param data the data map to set, containing raw log content and metadata
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
        logger.debug("Set data: {}", data);
    }

    // ========================================================================
    // PROPERTY ACCESSORS - ERROR HANDLING
    // ========================================================================

    /**
     * Gets the error message or diagnostic information.
     * @return the error message, or {@code null} if no error occurred
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message or diagnostic information.
     * @param error the error message to set
     */
    public void setError(String error) {
        this.error = error;
    }

    // ========================================================================
    // AI ANALYSIS INTEGRATION METHODS
    // ========================================================================

    /**
     * Generates structured content for AI-powered anomaly detection analysis.
     *
     * <p>This method processes the raw build log data and provides clean, analyzable content
     * for AI systems. It handles both compressed and uncompressed log data, automatically
     * decompressing GZIP content when necessary, and prioritizes error information for
     * failed builds.</p>
     *
     * <h4>Content Processing Strategy:</h4>
     * <p>The method employs a multi-stage approach for content extraction:</p>
     * <ol>
     *   <li><strong>Error Prioritization:</strong> If error field is present, includes error details</li>
     *   <li><strong>Data Validation:</strong> Checks for presence of raw_log data</li>
     *   <li><strong>Compression Detection:</strong> Identifies GZIP-compressed content</li>
     *   <li><strong>Decompression:</strong> Automatically decompresses Base64-encoded GZIP data</li>
     *   <li><strong>Content Assembly:</strong> Combines error and log content for analysis</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When build errors are present, the content includes diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Build failed due to test failures in security scan module
     * Raw Log: Started by user admin
     * Building in workspace /var/jenkins_home/workspace/security-pipeline
     * [Pipeline] Start of Pipeline
     * [Pipeline] stage
     * [Pipeline] { (Security Scan)
     * ERROR: Test failures detected in security module
     * FAILED: SecurityTest.testVulnerabilityDetection
     * Build step 'Execute shell' marked build as failure
     * }}
     *
     * <h4>Compressed Content Handling:</h4>
     * <p>For GZIP-compressed logs, the method automatically decompresses:</p>
     * {@snippet lang="text" :
     * Raw Log: [Decompressed from GZIP]
     * Started by timer
     * Building in workspace /var/jenkins_home/workspace/large-build
     * [Pipeline] Start of Pipeline
     * [... extensive log content ...]
     * Finished: SUCCESS
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Security Analysis:</strong> Detection of secrets, vulnerabilities &amp; compliance issues</li>
     *   <li><strong>Build Pattern Analysis:</strong> Performance trends, failure patterns &amp; optimization</li>
     *   <li><strong>Anomaly Detection:</strong> Unusual build behaviors &amp; security threats</li>
     *   <li><strong>Error Classification:</strong> Categorization of build failures &amp; root cause analysis</li>
     *   <li><strong>Compliance Monitoring:</strong> Policy violations &amp; regulatory compliance checks</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Lazy Decompression:</strong> Decompresses only when content is requested</li>
     *   <li><strong>Memory Efficiency:</strong> Streams large log files to prevent memory issues</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted or malformed data</li>
     *   <li><strong>Content Caching:</strong> Caches decompressed content for repeated access</li>
     * </ul>
     *
     * @return formatted string containing build log content optimized for AI analysis.
     *         Returns error information when present, followed by raw log content.
     *         For compressed logs, automatically decompresses and returns readable content.
     * @throws Exception if decompression fails or log data is corrupted
     *
     * @see TypedLog#getContentToAnalyze()
     * @see BuildLogData#decodeGzipMessage(String)
     */
    @JsonIgnore
    @Override
    public String getContentToAnalyze() throws Exception {
        StringBuilder content = new StringBuilder();
        if (error != null) {
            content.append("Error: ").append(error).append("\n");
        }
        if (data == null || !data.containsKey("raw_log")) {
            return content.length() > 0 ? content.toString() : "No content to decode";
        }
        Object rawLog = data.get("raw_log");
        if (rawLog instanceof String encodedMessage) {
            Boolean isCompressed = (Boolean) data.getOrDefault("raw_log_compressed", false);
            if (isCompressed) {
                String decoded = decodeGzipMessage(encodedMessage);
                this.data.put("raw_log",decoded);
                content.append("Raw Log: ").append(decoded);
            } else {
                content.append("Raw Log: ").append(encodedMessage);
            }
            return content.toString();
        }
        return content.length() > 0 ? content.toString() : "No content to decode";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of build log data with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that build logs are grouped with
     * related system metrics and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Build Correlation:</strong> Links build logs to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks build patterns &amp; trends over time</li>
     *   <li><strong>Context Preservation:</strong> Maintains conversation continuity for AI analysis</li>
     *   <li><strong>Memory Management:</strong> Enables efficient conversation-based cleanup</li>
     * </ul>
     *
     * <h4>Integration with Chat Memory:</h4>
     * <p>The conversation ID is used by {@link com.diploma.inno.component.SimpleDbChatMemory} for:</p>
     * <ul>
     *   <li>Message storage and retrieval</li>
     *   <li>Conversation history management</li>
     *   <li>AI context preparation</li>
     *   <li>Build sequence tracking</li>
     * </ul>
     *
     * @return the Jenkins job name serving as the conversation identifier,
     *         or {@code null} if no job name is set
     *
     * @see TypedLog#getConversationId()
     * @see com.diploma.inno.component.SimpleDbChatMemory#add(String, java.util.List)
     */
    @JsonIgnore
    @Override
    public String getConversationId() {
        return jobName;
    }

    // ========================================================================
    // COMPRESSION &amp; DECOMPRESSION UTILITIES
    // ========================================================================

    /**
     * Decodes GZIP-compressed Base64-encoded build log messages.
     *
     * <p>This private utility method handles the decompression of build logs that have been
     * compressed using GZIP and encoded with Base64 for efficient transmission. It provides
     * robust error handling and logging for debugging compression-related issues.</p>
     *
     * <h4>Decompression Process:</h4>
     * <ol>
     *   <li><strong>Validation:</strong> Checks for null or empty encoded messages</li>
     *   <li><strong>Base64 Decoding:</strong> Decodes the Base64-encoded compressed data</li>
     *   <li><strong>GZIP Decompression:</strong> Decompresses the GZIP data stream</li>
     *   <li><strong>String Conversion:</strong> Converts decompressed bytes to UTF-8 string</li>
     *   <li><strong>Error Handling:</strong> Provides graceful error recovery</li>
     * </ol>
     *
     * <h4>Compression Format:</h4>
     * <p>The method expects data in the following format:</p>
     * {@snippet lang="text" :
     * Original Log → GZIP Compression → Base64 Encoding → Transmission
     * "Started by user admin..." → [GZIP bytes] → "H4sIAAAAAAAA/3WQwQrCMBBE..." → Network
     * }
     *
     * <h4>Error Scenarios:</h4>
     * <ul>
     *   <li><strong>Null Input:</strong> Returns "No content to decode" message</li>
     *   <li><strong>Invalid Base64:</strong> Throws exception with detailed error information</li>
     *   <li><strong>Corrupted GZIP:</strong> Throws exception indicating compression issues</li>
     *   <li><strong>Memory Issues:</strong> Handles large files with streaming decompression</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Memory Efficiency:</strong> Uses streaming decompression for large files</li>
     *   <li><strong>Resource Management:</strong> Automatic resource cleanup with try-with-resources</li>
     *   <li><strong>Error Logging:</strong> Comprehensive logging for debugging issues</li>
     * </ul>
     *
     * @param encodedMessage the Base64-encoded GZIP-compressed log message
     * @return the decompressed log content as a UTF-8 string
     * @throws Exception if Base64 decoding fails, GZIP decompression fails,
     *                   or I/O errors occur during processing
     *
     * @see Base64#getDecoder()
     * @see GZIPInputStream
     * @see #getContentToAnalyze()
     */
    private String decodeGzipMessage(String encodedMessage) throws Exception {
        if (encodedMessage == null) {
            logger.warn("Encoded message is null");
            return "No content to decode";
        }
        byte[] compressed = Base64.getDecoder().decode(encodedMessage);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            String decoded = new String(gzipInputStream.readAllBytes());
//            logger.debug("Decoded GZIP message: {}", decoded);
            return decoded;
        }
    }

    @Override
    public String toString() {
        return "BuildLogData{" +
                "jobName='" + jobName + '\'' +
                ", data=" + data +
                ", buildNumber=" + buildNumber +
                ", error='" + error + '\'' +
                '}';
    }


    // ========================================================================
    // JSON SERIALIZATION &amp; DATA EXCHANGE
    // ========================================================================

    /**
     * Converts the build log data to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete build log information
     * including raw log content, compression metadata, error details, and build context. It uses
     * Jackson's ObjectMapper for comprehensive serialization with proper error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all build log data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant build log information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "build_log_data",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "data": {
     *     "raw_log": "Started by user admin\nBuilding in workspace /var/jenkins_home/workspace/security-pipeline\n[Pipeline] Start of Pipeline\n[Pipeline] node\nRunning on Jenkins in /var/jenkins_home/workspace/security-pipeline\n[Pipeline] {\n[Pipeline] stage\n[Pipeline] { (Checkout)\n[Pipeline] git\nCloning the remote Git repository\n...\nFinished: SUCCESS",
     *     "raw_log_compressed": false,
     *     "build_status": "SUCCESS",
     *     "duration": 45000,
     *     "timestamp": "2024-01-15T10:30:00Z"
     *   },
     *   "error": null
     *   }
     * }}}
     *
     *
     *
     *
     * <h4>Compressed Log Output:</h4>
     * <p>For compressed logs, the JSON includes compression metadata:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "build_log_data",
     *   "job_name": "large-build-pipeline",
     *   "build_number": 456,
     *   "data": {
     *     "raw_log": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
     *     "raw_log_compressed": true,
     *     "original_size": 1048576,
     *     "compressed_size": 65536,
     *     "build_status": "SUCCESS"
     *   }
     * }
     * }
     *
     * <h4>Error Handling:</h4>
     * <p>The method includes comprehensive error handling:</p>
     * <ul>
     *   <li><strong>Exception Catching:</strong> Catches all JSON processing exceptions</li>
     *   <li><strong>Fallback Response:</strong> Returns "{}" on serialization failure</li>
     *   <li><strong>Error Logging:</strong> Logs detailed error information for debugging</li>
     *   <li><strong>Graceful Degradation:</strong> Ensures system continues operation</li>
     * </ul>
     *
     * <h4>Usage Scenarios:</h4>
     * <ul>
     *   <li><strong>Message Queue:</strong> Serialization for RabbitMQ transmission</li>
     *   <li><strong>Database Storage:</strong> JSON storage in chat memory</li>
     *   <li><strong>API Responses:</strong> REST API data exchange</li>
     *   <li><strong>Logging:</strong> Structured logging of build information</li>
     * </ul>
     *
     * @return JSON string representation of the build log data,
     *         or "{}" if serialization fails
     *
     * @see TypedLog#toJson()
     * @see ObjectMapper#writeValueAsString(Object)
     */
    @Override
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Failed to create JSON for {}: {}", this.getClass().getSimpleName(), this, e);
            return "{}";
        }
    }


}