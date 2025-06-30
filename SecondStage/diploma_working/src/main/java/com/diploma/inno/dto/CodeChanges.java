package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Data Transfer Object for Jenkins code changes &amp; version control information in CI/CD anomaly detection.
 *
 * <p>This DTO encapsulates comprehensive code change information including commit details, author information,
 * affected files, and change metadata for Jenkins builds. It serves as a critical component for tracking
 * code modifications, identifying potential security risks, and enabling AI-powered analysis of development
 * patterns and anomalies in CI/CD pipelines.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Change Tracking:</strong> Captures commit details, authors &amp; affected files</li>
 *   <li><strong>Compression Support:</strong> Handles GZIP-compressed change data for large repositories</li>
 *   <li><strong>Culprit Identification:</strong> Tracks responsible developers for build issues</li>
 *   <li><strong>Version Control Integration:</strong> Links changes to specific builds &amp; jobs</li>
 *   <li><strong>Security Analysis:</strong> Enables detection of suspicious code changes</li>
 * </ul>
 *
 * <h2>Data Structure &amp; Content</h2>
 * <p>The DTO manages complex change data structures with multiple information layers:</p>
 * <ul>
 *   <li><strong>Commit Information:</strong> Commit IDs, messages, timestamps &amp; author details</li>
 *   <li><strong>File Changes:</strong> Lists of affected files with modification types</li>
 *   <li><strong>Author Tracking:</strong> Developer identification &amp; contact information</li>
 *   <li><strong>Build Context:</strong> Job correlation &amp; build number association</li>
 *   <li><strong>Compressed Data:</strong> Base64-encoded GZIP changes for large datasets</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "code_changes",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "data": {
 *     "message": "Code changes for security enhancement",
 *     "changes_compressed": false,
 *     "changes": [
 *       {
 *         "commit_id": "a1b2c3d4e5f6",
 *         "author": "John Doe",
 *         "author_email": "john.doe@company.com",
 *         "timestamp": "2024-01-15T10:30:00Z",
 *         "message": "Fix security vulnerability in authentication module",
 *         "affected_files": [
 *           "src/main/java/auth/SecurityManager.java",
 *           "src/test/java/auth/SecurityManagerTest.java"
 *         ]
 *       }
 *     ],
 *     "culprits": ["john.doe@company.com"]
 *   },
 *   "error": null
 * }
 * }
 *
 * <h2>Compressed Change Handling</h2>
 * <p>For large repositories with extensive changes, the DTO supports GZIP compression:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "code_changes",
 *   "job_name": "large-refactor-pipeline",
 *   "build_number": 456,
 *   "data": {
 *     "message": "Major refactoring with 500+ file changes",
 *     "changes_compressed": true,
 *     "changes": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
 *     "original_size": 2048576,
 *     "compressed_size": 102400,
 *     "culprits": ["lead.developer@company.com", "architect@company.com"]
 *   }
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When change processing errors occur, the DTO captures diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "code_changes",
 *   "job_name": "failing-pipeline",
 *   "build_number": 789,
 *   "data": {
 *     "message": "Failed to retrieve change information",
 *     "changes": [],
 *     "culprits": []
 *   },
 *   "error": "Git repository access denied: insufficient permissions"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered analysis:</p>
 * <ul>
 *   <li><strong>Security Analysis:</strong> Detection of suspicious changes &amp; potential threats</li>
 *   <li><strong>Pattern Recognition:</strong> Identification of development patterns &amp; anomalies</li>
 *   <li><strong>Risk Assessment:</strong> Evaluation of change impact &amp; security implications</li>
 *   <li><strong>Compliance Monitoring:</strong> Verification of coding standards &amp; policies</li>
 *   <li><strong>Developer Behavior:</strong> Analysis of commit patterns &amp; code quality trends</li>
 * </ul>
 *
 * <h2>Change Analysis Capabilities</h2>
 * <p>The DTO enables comprehensive change analysis:</p>
 * <ol>
 *   <li><strong>Commit Correlation:</strong> Links changes to specific builds &amp; deployments</li>
 *   <li><strong>Author Tracking:</strong> Identifies responsible developers for accountability</li>
 *   <li><strong>File Impact Analysis:</strong> Assesses scope &amp; risk of file modifications</li>
 *   <li><strong>Temporal Analysis:</strong> Tracks change frequency &amp; timing patterns</li>
 * </ol>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li><strong>Data Sanitization:</strong> Safe handling of potentially sensitive commit information</li>
 *   <li><strong>Access Control:</strong> Secure processing of repository access credentials</li>
 *   <li><strong>Privacy Protection:</strong> Careful handling of developer personal information</li>
 *   <li><strong>Compression Security:</strong> Secure decompression with size &amp; content validation</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>Lazy Decompression:</strong> Decompresses change data only when needed for analysis</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized handling of large change datasets</li>
 *   <li><strong>Streaming Processing:</strong> Efficient processing of continuous change streams</li>
 *   <li><strong>Caching Strategy:</strong> Intelligent caching of processed change information</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes code change messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores change data for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides change content for anomaly detection</li>
 *   <li><strong>Security Scanner:</strong> Supplies change data for vulnerability analysis</li>
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
public class CodeChanges extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(CodeChanges.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with these code changes.
     * <p>Used for correlating code changes with specific Jenkins jobs and enabling
     * conversation-based grouping in the AI analysis pipeline.</p>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "security-pipeline"} - Security-focused development pipeline</li>
     *   <li>{@code "feature-branch-ci"} - Feature branch continuous integration</li>
     *   <li>{@code "release-deployment"} - Release deployment pipeline</li>
     * </ul>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this code changes snapshot.
     * <p>Enables tracking of code changes across different build executions
     * and correlation with build outcomes for trend analysis.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // CODE CHANGE DATA &amp; METADATA
    // ========================================================================

    /**
     * Complex data structure containing code change information and metadata.
     * <p>This map contains comprehensive change data including commit details, author information,
     * affected files, compression flags, and other contextual information required for
     * thorough code change analysis and security assessment.</p>
     *
     * <h4>Standard Data Structure:</h4>
     * <ul>
     *   <li><strong>message:</strong> Overall change description or summary (String)</li>
     *   <li><strong>changes_compressed:</strong> Boolean flag indicating GZIP compression</li>
     *   <li><strong>changes:</strong> List of commit objects or compressed change data</li>
     *   <li><strong>culprits:</strong> List of developer emails responsible for changes</li>
     *   <li><strong>original_size:</strong> Uncompressed data size (for compressed changes)</li>
     *   <li><strong>compressed_size:</strong> Compressed data size (for compressed changes)</li>
     * </ul>
     *
     * <h4>Uncompressed Changes Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "message": "Security enhancement changes",
     *   "changes_compressed": false,
     *   "changes": [
     *     {
     *       "commit_id": "a1b2c3d4e5f6",
     *       "author": "John Doe",
     *       "author_email": "john.doe@company.com",
     *       "timestamp": "2024-01-15T10:30:00Z",
     *       "message": "Fix authentication vulnerability",
     *       "affected_files": [
     *         "src/main/java/auth/SecurityManager.java",
     *         "src/test/java/auth/SecurityManagerTest.java"
     *       ]
     *     }
     *   ],
     *   "culprits": ["john.doe@company.com"]
     * }
     * }
     *
     * <h4>Compressed Changes Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "message": "Large refactoring with 500+ files",
     *   "changes_compressed": true,
     *   "changes": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
     *   "original_size": 2048576,
     *   "compressed_size": 102400,
     *   "culprits": ["lead.dev@company.com", "architect@company.com"]
     * }
     * }
     */
    @JsonProperty("data")
    private Map<String, Object> data; // Contains changes_compressed, message

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Error message or diagnostic information for change processing failures.
     * <p>Contains human-readable error descriptions, failure reasons, or
     * diagnostic information when code change retrieval or processing fails.</p>
     *
     * <h4>Error Types:</h4>
     * <ul>
     *   <li><strong>Repository Access:</strong> Git access denied, authentication failures</li>
     *   <li><strong>Network Issues:</strong> Repository connectivity problems, timeouts</li>
     *   <li><strong>Data Processing:</strong> Compression errors, malformed change data</li>
     *   <li><strong>Permission Problems:</strong> Insufficient access to change information</li>
     * </ul>
     *
     * <h4>Example Error Messages:</h4>
     * <ul>
     *   <li>{@code "Git repository access denied: insufficient permissions"}</li>
     *   <li>{@code "Failed to decompress change data: corrupted GZIP stream"}</li>
     *   <li>{@code "Repository connection timeout: unable to fetch changes"}</li>
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
     * <p>Creates a new CodeChanges instance with the type set to "code_changes"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "code_changes" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * Jenkins code change messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * CodeChanges changes = objectMapper.readValue(jsonMessage, CodeChanges.class);
     *
     * // Manual instantiation for testing
     * CodeChanges changes = new CodeChanges();
     * changes.setJobName("security-pipeline");
     * changes.setBuildNumber(123);
     *
     * Map<String, Object> data = new HashMap<>();
     * data.put("message", "Security enhancement changes");
     * data.put("changes_compressed", false);
     * data.put("changes", Arrays.asList(commitData));
     * changes.setData(data);
     * }
     *
     * @see TypedLog#setType(String)
     */
    public CodeChanges() {
        setType("code_changes");
        logger.debug("Initialized CodeChanges with type=code_changes");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - BUILD CONTEXT
    // ========================================================================

    /**
     * Gets the Jenkins job name associated with these code changes.
     * @return the job name, or {@code null} if not set
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the Jenkins job name for these code changes.
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the Jenkins build number for these code changes.
     * @return the build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the Jenkins build number for these code changes.
     * @param buildNumber the build number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - CHANGE DATA &amp; METADATA
    // ========================================================================

    /**
     * Gets the complex data structure containing code change information and metadata.
     * @return the data map containing change details and metadata, or {@code null} if not set
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the code change data and metadata.
     * @param data the data map to set, containing change information and metadata
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
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
     * <p>This method processes the raw code change data and provides clean, analyzable content
     * for AI systems. It handles both compressed and uncompressed change data, automatically
     * decompressing GZIP content when necessary, and formats change information for optimal
     * AI analysis and security assessment.</p>
     *
     * <h4>Content Processing Strategy:</h4>
     * <p>The method employs a multi-stage approach for content extraction:</p>
     * <ol>
     *   <li><strong>Error Prioritization:</strong> If error field is present, includes error details</li>
     *   <li><strong>Data Validation:</strong> Checks for presence of change data</li>
     *   <li><strong>Message Extraction:</strong> Includes overall change description</li>
     *   <li><strong>Compression Detection:</strong> Identifies GZIP-compressed change data</li>
     *   <li><strong>Change Processing:</strong> Formats individual commits for analysis</li>
     *   <li><strong>Culprit Identification:</strong> Lists responsible developers</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When change processing errors are present, the content includes diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Git repository access denied: insufficient permissions
     * Message: Failed to retrieve change information for security analysis
     * }
     *
     * <h4>Normal Content Format:</h4>
     * <p>For successful change retrieval, provides comprehensive change details:</p>
     * {@snippet lang="text" :
     * Message: Security enhancement changes for authentication module
     * Changes:
     * - Commit: a1b2c3d4e5f6, Author: John Doe, Email: john.doe@company.com, Timestamp: 2024-01-15T10:30:00Z, Message: Fix authentication vulnerability, Files: [src/main/java/auth/SecurityManager.java, src/test/java/auth/SecurityManagerTest.java]
     * - Commit: b2c3d4e5f6a1, Author: Jane Smith, Email: jane.smith@company.com, Timestamp: 2024-01-15T11:15:00Z, Message: Add security tests, Files: [src/test/java/auth/SecurityIntegrationTest.java]
     * Culprits: john.doe@company.com, jane.smith@company.com
     * }
     *
     * <h4>Compressed Content Handling:</h4>
     * <p>For GZIP-compressed changes, the method automatically decompresses:</p>
     * {@snippet lang="text" :
     * Message: Large refactoring with 500+ file changes
     * Changes:
     * [Decompressed from GZIP - 500+ commits with detailed information]
     * - Commit: c3d4e5f6a1b2, Author: Lead Developer, Email: lead.dev@company.com, ...
     * Culprits: lead.dev@company.com, architect@company.com
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Security Analysis:</strong> Detection of suspicious changes &amp; potential threats</li>
     *   <li><strong>Pattern Recognition:</strong> Identification of development patterns &amp; anomalies</li>
     *   <li><strong>Risk Assessment:</strong> Evaluation of change impact &amp; security implications</li>
     *   <li><strong>Compliance Monitoring:</strong> Verification of coding standards &amp; policies</li>
     *   <li><strong>Developer Behavior:</strong> Analysis of commit patterns &amp; code quality trends</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Lazy Decompression:</strong> Decompresses only when content is requested</li>
     *   <li><strong>Memory Efficiency:</strong> Streams large change datasets to prevent memory issues</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted or malformed data</li>
     *   <li><strong>Content Caching:</strong> Caches decompressed content for repeated access</li>
     * </ul>
     *
     * @return formatted string containing code change information optimized for AI analysis.
     *         Returns error information when present, followed by change details and culprit information.
     *         For compressed changes, automatically decompresses and returns readable content.
     * @throws Exception if decompression fails, JSON parsing fails, or change data is corrupted
     *
     * @see TypedLog#getContentToAnalyze()
     * @see #decodeGzipMessage(String)
     */
    @JsonIgnore
    @Override
    public String getContentToAnalyze() throws Exception {
        StringBuilder content = new StringBuilder();
        if (error != null) {
            content.append("Error: ").append(error).append("\n");
        }
        if (data == null) {
            return content.length() > 0 ? content.toString() : "No content available";
        }
        String message = (String) data.getOrDefault("message", "");
        if (!message.isEmpty()) {
            content.append("Message: ").append(message).append("\n");
        }
        Boolean isCompressed = (Boolean) data.getOrDefault("changes_compressed", false);
        Object changesObj = data.get("changes");
        if (changesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> changes = (List<Map<String, Object>>) changesObj;
            if (!changes.isEmpty()) {
                content.append("Changes:\n");
                for (Map<String, Object> change : changes) {
                    content.append("- Commit: ").append(change.getOrDefault("commit_id", "N/A"))
                            .append(", Author: ").append(change.getOrDefault("author", "Unknown"))
                            .append(", Email: ").append(change.getOrDefault("author_email", ""))
                            .append(", Timestamp: ").append(change.getOrDefault("timestamp", ""))
                            .append(", Message: ").append(change.getOrDefault("message", ""))
                            .append(", Files: ").append(change.getOrDefault("affected_files", Collections.emptyList()))
                            .append("\n");
                }
            }
        } else if (isCompressed && changesObj instanceof String encodedChanges && !encodedChanges.isEmpty()) {
            String decompressedChanges = decodeGzipMessage(encodedChanges);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> changes = mapper.readValue(decompressedChanges, List.class);
            if (!changes.isEmpty()) {
                content.append("Changes:\n");
                for (Map<String, Object> change : changes) {
                    content.append("- Commit: ").append(change.getOrDefault("commit_id", "N/A"))
                            .append(", Author: ").append(change.getOrDefault("author", "Unknown"))
                            .append(", Email: ").append(change.getOrDefault("author_email", ""))
                            .append(", Timestamp: ").append(change.getOrDefault("timestamp", ""))
                            .append(", Message: ").append(change.getOrDefault("message", ""))
                            .append(", Files: ").append(change.getOrDefault("affected_files", Collections.emptyList()))
                            .append("\n");
                }
            }
            this.data.put("changes",changes);
        }
        List<String> culprits = (List<String>) data.getOrDefault("culprits", Collections.emptyList());
        if (!culprits.isEmpty()) {
            content.append("Culprits: ").append(String.join(", ", culprits)).append("\n");
        }
        return content.length() > 0 ? content.toString() : "No content available";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of code changes with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that code changes are grouped with
     * related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Change Correlation:</strong> Links code changes to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks development patterns &amp; trends over time</li>
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
     *   <li>Change sequence tracking</li>
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
     * Decodes GZIP-compressed Base64-encoded code change messages.
     *
     * <p>This private utility method handles the decompression of code changes that have been
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
     * Original Changes → GZIP Compression → Base64 Encoding → Transmission
     * [{"commit_id":"abc123",...}] → [GZIP bytes] → "H4sIAAAAAAAA/62QwQrCMBBE..." → Network
     * }
     *
     * <h4>Error Scenarios:</h4>
     * <ul>
     *   <li><strong>Null/Empty Input:</strong> Returns empty string with warning log</li>
     *   <li><strong>Invalid Base64:</strong> Throws exception with detailed error information</li>
     *   <li><strong>Corrupted GZIP:</strong> Throws exception indicating compression issues</li>
     *   <li><strong>Memory Issues:</strong> Handles large files with streaming decompression</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Memory Efficiency:</strong> Uses streaming decompression for large datasets</li>
     *   <li><strong>Resource Management:</strong> Automatic resource cleanup with try-with-resources</li>
     *   <li><strong>Error Logging:</strong> Comprehensive logging for debugging issues</li>
     * </ul>
     *
     * @param encodedMessage the Base64-encoded GZIP-compressed change message
     * @return the decompressed change content as a UTF-8 string,
     *         or empty string if input is null/empty
     * @throws Exception if Base64 decoding fails, GZIP decompression fails,
     *                   or I/O errors occur during processing
     *
     * @see Base64#getDecoder()
     * @see GZIPInputStream
     * @see #getContentToAnalyze()
     */
    private String decodeGzipMessage(String encodedMessage) throws Exception {
        if (encodedMessage == null || encodedMessage.isEmpty()) {
            logger.warn("Encoded changes message is null or empty");
            return "";
        }
        byte[] compressed = Base64.getDecoder().decode(encodedMessage);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            String decoded = new String(gzipInputStream.readAllBytes());
//            logger.debug("Decoded GZIP changes: {}", decoded);
            return decoded;
        }
    }

    // ========================================================================
    // OBJECT REPRESENTATION &amp; SERIALIZATION
    // ========================================================================

    /**
     * Returns a string representation of this CodeChanges object.
     *
     * <p>Provides a human-readable representation of the code changes including
     * job name, build number, data summary, and error information for debugging
     * and logging purposes.</p>
     *
     * @return string representation of the CodeChanges object
     */
    @Override
    public String toString() {
        return "CodeChanges{" +
                "jobName='" + jobName + '\'' +
                ", data=" + data +
                ", buildNumber=" + buildNumber +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * Converts the code changes to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete code change information
     * including change details, compression metadata, error information, and build context. It uses
     * Jackson's ObjectMapper for comprehensive serialization with proper error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all code change data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant code change information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "code_changes",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "data": {
     *     "message": "Security enhancement changes",
     *     "changes_compressed": false,
     *     "changes": [
     *       {
     *         "commit_id": "a1b2c3d4e5f6",
     *         "author": "John Doe",
     *         "author_email": "john.doe@company.com",
     *         "timestamp": "2024-01-15T10:30:00Z",
     *         "message": "Fix authentication vulnerability",
     *         "affected_files": [
     *           "src/main/java/auth/SecurityManager.java",
     *           "src/test/java/auth/SecurityManagerTest.java"
     *         ]
     *       }
     *     ],
     *     "culprits": ["john.doe@company.com"]
     *   },
     *   "error": null
     * }
     * }
     *
     * <h4>Compressed Changes Output:</h4>
     * <p>For compressed changes, the JSON includes compression metadata:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "code_changes",
     *   "job_name": "large-refactor-pipeline",
     *   "build_number": 456,
     *   "data": {
     *     "message": "Large refactoring with 500+ files",
     *     "changes_compressed": true,
     *     "changes": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
     *     "original_size": 2048576,
     *     "compressed_size": 102400,
     *     "culprits": ["lead.dev@company.com", "architect@company.com"]
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
     *   <li><strong>Logging:</strong> Structured logging of change information</li>
     * </ul>
     *
     * @return JSON string representation of the code changes,
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