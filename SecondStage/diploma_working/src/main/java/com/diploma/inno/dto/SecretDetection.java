package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

/**
 * Data Transfer Object for secret detection &amp; credential scanning results in CI/CD security analysis.
 *
 * <p>This DTO encapsulates comprehensive secret detection results including exposed credentials,
 * API keys, tokens, passwords, and other sensitive information found in source code, configuration
 * files, and build artifacts. It serves as a critical component for preventing credential leaks,
 * ensuring security compliance, and enabling AI-powered analysis of secret exposure risks
 * across CI/CD pipelines.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Secret Detection:</strong> Identifies exposed credentials, API keys &amp; sensitive data</li>
 *   <li><strong>Multi-Source Scanning:</strong> Scans source code, configs, logs &amp; build artifacts</li>
 *   <li><strong>Pattern Recognition:</strong> Advanced regex &amp; ML-based secret pattern detection</li>
 *   <li><strong>Content Analysis:</strong> Deep analysis of file contents &amp; commit history</li>
 *   <li><strong>Compression Support:</strong> Handles GZIP-compressed content for large datasets</li>
 *   <li><strong>Risk Assessment:</strong> Categorizes secrets by type &amp; exposure risk level</li>
 * </ul>
 *
 * <h2>Secret Types &amp; Detection</h2>
 * <p>The DTO supports detection of various types of sensitive information:</p>
 * <ul>
 *   <li><strong>API Keys:</strong> AWS, Google Cloud, Azure, GitHub, Slack API keys</li>
 *   <li><strong>Database Credentials:</strong> MySQL, PostgreSQL, MongoDB connection strings</li>
 *   <li><strong>Authentication Tokens:</strong> JWT tokens, OAuth tokens, session tokens</li>
 *   <li><strong>Private Keys:</strong> SSH keys, SSL certificates, PGP private keys</li>
 *   <li><strong>Passwords:</strong> Hardcoded passwords, service account credentials</li>
 *   <li><strong>Cloud Secrets:</strong> Docker registry tokens, Kubernetes secrets</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "secret_detection",
 *   "job_name": "security-scan-pipeline",
 *   "build_number": 123,
 *   "data": {
 *     "source": "src/main/resources/application.properties",
 *     "message": "Secret detection scan completed",
 *     "content_compressed": false,
 *     "content": "database.password=mySecretPassword123\napi.key=sk-1234567890abcdef",
 *     "secrets_compressed": false,
 *     "secrets": {
 *       "database_password": ["mySecretPassword123"],
 *       "api_key": ["sk-1234567890abcdef"],
 *       "aws_access_key": ["AKIAIOSFODNN7EXAMPLE"]
 *     }
 *   },
 *   "error": null,
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * }
 *
 * <h2>Compressed Content Handling</h2>
 * <p>For large files or extensive secret scans, the DTO supports GZIP compression:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "secret_detection",
 *   "job_name": "enterprise-secret-scan",
 *   "build_number": 456,
 *   "data": {
 *     "source": "entire_repository_scan",
 *     "message": "Full repository secret scan with 500+ files",
 *     "content_compressed": true,
 *     "content": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
 *     "secrets_compressed": true,
 *     "secrets": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ..."
 *   },
 *   "error": null
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When secret detection errors occur, the DTO captures diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "secret_detection",
 *   "job_name": "failing-secret-scan",
 *   "build_number": 789,
 *   "data": {
 *     "source": "protected_repository",
 *     "message": "Access denied to private repository",
 *     "content": "",
 *     "secrets": {}
 *   },
 *   "error": "Repository access denied: Insufficient permissions for secret scanning"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered security analysis:</p>
 * <ul>
 *   <li><strong>Risk Assessment:</strong> AI-driven evaluation of secret exposure severity</li>
 *   <li><strong>False Positive Reduction:</strong> ML-based filtering of test/dummy credentials</li>
 *   <li><strong>Pattern Learning:</strong> Adaptive pattern recognition for new secret types</li>
 *   <li><strong>Compliance Monitoring:</strong> Automated compliance checking &amp; reporting</li>
 *   <li><strong>Remediation Guidance:</strong> AI-generated recommendations for secret management</li>
 * </ul>
 *
 * <h2>Security Metrics &amp; KPIs</h2>
 * <p>The DTO enables comprehensive security metrics tracking:</p>
 * <ul>
 *   <li><strong>Secret Counts:</strong> Total secrets found by type &amp; severity</li>
 *   <li><strong>Exposure Trends:</strong> Historical secret detection trends &amp; improvements</li>
 *   <li><strong>Source Analysis:</strong> Secret distribution across files &amp; repositories</li>
 *   <li><strong>Remediation Tracking:</strong> Time-to-fix metrics &amp; resolution effectiveness</li>
 * </ul>
 *
 * <h2>Source Context &amp; Traceability</h2>
 * <p>The DTO maintains crucial source context for secret tracking:</p>
 * <ul>
 *   <li><strong>File Identification:</strong> Specific files &amp; locations containing secrets</li>
 *   <li><strong>Repository Context:</strong> Repository-wide secret scanning &amp; analysis</li>
 *   <li><strong>Commit Correlation:</strong> Links secret findings to specific code changes</li>
 *   <li><strong>Multi-Source Support:</strong> Handles secrets across multiple sources &amp; formats</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>Compression Support:</strong> GZIP compression for large content &amp; secret datasets</li>
 *   <li><strong>Lazy Decompression:</strong> Decompresses content only when needed for analysis</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized handling of large file scans</li>
 *   <li><strong>Streaming Processing:</strong> Efficient processing of continuous secret detection</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes secret detection messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores secret detection results for analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides secret data for security analysis</li>
 *   <li><strong>Security Dashboard:</strong> Supplies real-time secret exposure metrics</li>
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
public class SecretDetection extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(SecretDetection.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this secret detection result.
     * <p>Used for correlating secret detection results with specific Jenkins jobs and enabling
     * conversation-based grouping in the AI analysis pipeline.</p>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "security-scan-pipeline"} - Dedicated security scanning pipeline</li>
     *   <li>{@code "secret-detection-job"} - Specialized secret detection job</li>
     *   <li>{@code "compliance-check"} - Compliance verification with secret scanning</li>
     * </ul>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this secret detection result.
     * <p>Enables tracking of secret detection results across different build executions
     * and correlation with build outcomes for trend analysis.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // SECRET DETECTION DATA &amp; METADATA
    // ========================================================================

    /**
     * Complex data structure containing comprehensive secret detection information and metadata.
     * <p>This map contains detailed secret detection data including source information, scan content,
     * detected secrets, compression flags, and other contextual information required for thorough
     * secret analysis and security assessment.</p>
     *
     * <h4>Standard Data Structure:</h4>
     * <ul>
     *   <li><strong>source:</strong> Source file or location where secrets were detected</li>
     *   <li><strong>message:</strong> Descriptive message about the secret detection process</li>
     *   <li><strong>content:</strong> Scanned content (may be compressed)</li>
     *   <li><strong>content_compressed:</strong> Boolean flag indicating content compression</li>
     *   <li><strong>secrets:</strong> Detected secrets organized by type (may be compressed)</li>
     *   <li><strong>secrets_compressed:</strong> Boolean flag indicating secrets compression</li>
     * </ul>
     *
     * <h4>Source Information Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "source": "src/main/resources/application.properties",
     *   "message": "Configuration file secret scan completed"
     * }
     * }
     *
     * <h4>Content Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "content_compressed": false,
     *   "content": "database.password=mySecretPassword123\napi.key=sk-1234567890abcdef\naws.access.key=AKIAIOSFODNN7EXAMPLE"
     * }
     * }
     *
     * <h4>Secrets Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "secrets_compressed": false,
     *   "secrets": {
     *     "database_password": ["mySecretPassword123"],
     *     "api_key": ["sk-1234567890abcdef"],
     *     "aws_access_key": ["AKIAIOSFODNN7EXAMPLE"],
     *     "github_token": ["ghp_1234567890abcdefghijklmnopqrstuvwxyz"],
     *     "private_key": ["-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEA..."]
     *   }
     * }
     * }
     *
     * <h4>Compressed Data Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "source": "entire_repository_scan",
     *   "message": "Full repository secret scan with 500+ files",
     *   "content_compressed": true,
     *   "content": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwQMf4MmTJ...",
     *   "secrets_compressed": true,
     *   "secrets": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwQMf4MmTJ..."
     * }
     * }
     */
    @JsonProperty("data")
    private Map<String, Object> data; // Contains content_compressed, source, secrets_compressed, message, content, secrets

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Error message or diagnostic information for failed secret detection scans.
     * <p>Contains human-readable error descriptions, failure reasons, or
     * diagnostic information when secret detection fails or encounters issues.</p>
     *
     * <h4>Error Categories:</h4>
     * <ul>
     *   <li><strong>Access Errors:</strong> Repository or file access permission issues</li>
     *   <li><strong>Scanning Failures:</strong> Secret detection engine errors or timeouts</li>
     *   <li><strong>Content Issues:</strong> Malformed files or unsupported formats</li>
     *   <li><strong>Resource Constraints:</strong> Memory or processing limitations</li>
     *   <li><strong>Configuration Problems:</strong> Invalid scan parameters or rules</li>
     * </ul>
     *
     * <h4>Example Error Messages:</h4>
     * <ul>
     *   <li>{@code "Repository access denied: Insufficient permissions for secret scanning"}</li>
     *   <li>{@code "Secret detection timeout: Scan exceeded 300 seconds limit"}</li>
     *   <li>{@code "Content parsing failed: Binary file format not supported"}</li>
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
     * <p>Creates a new SecretDetection instance with the type set to "secret_detection"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "secret_detection" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * secret detection result messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * SecretDetection secretDetection = objectMapper.readValue(jsonMessage, SecretDetection.class);
     *
     * // Manual instantiation for testing
     * SecretDetection secretDetection = new SecretDetection();
     * secretDetection.setJobName("security-scan-pipeline");
     * secretDetection.setBuildNumber(123);
     *
     * Map<String, Object> data = new HashMap<>();
     * data.put("source", "src/main/resources/application.properties");
     * data.put("content", "database.password=secret123");
     * Map<String, List<String>> secrets = new HashMap<>();
     * secrets.put("database_password", Arrays.asList("secret123"));
     * data.put("secrets", secrets);
     * secretDetection.setData(data);
     * }
     *
     * @see TypedLog#setType(String)
     */
    public SecretDetection() {
        setType("secret_detection");
        logger.debug("Initialized SecretDetection with type=secret_detection");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - JENKINS BUILD CONTEXT
    // ========================================================================

    /**
     * Gets the Jenkins job name associated with this secret detection result.
     * @return the job name, or {@code null} if not set
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the Jenkins job name for this secret detection result.
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the Jenkins build number for this secret detection result.
     * @return the build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the Jenkins build number for this secret detection result.
     * @param buildNumber the build number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - SECRET DETECTION DATA &amp; METADATA
    // ========================================================================

    /**
     * Gets the complex data structure containing secret detection information and metadata.
     * @return the data map containing secret detection details and metadata, or {@code null} if not set
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the secret detection data and metadata.
     * @param data the data map to set, containing secret detection information and metadata
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
     * Generates structured content for AI-powered secret detection analysis &amp; security assessment.
     *
     * <p>This method processes the raw secret detection data and provides clean, analyzable content
     * for AI systems. It handles both compressed and uncompressed content and secrets, automatically
     * decompressing GZIP content when necessary, and formats secret findings for optimal
     * AI analysis and risk assessment.</p>
     *
     * <h4>Content Processing Strategy:</h4>
     * <p>The method employs a multi-stage approach for content extraction:</p>
     * <ol>
     *   <li><strong>Error Prioritization:</strong> If error field is present, includes error details</li>
     *   <li><strong>Data Validation:</strong> Checks for presence of secret detection data</li>
     *   <li><strong>Source Information:</strong> Extracts source file or location details</li>
     *   <li><strong>Message Processing:</strong> Includes descriptive scan messages</li>
     *   <li><strong>Content Decompression:</strong> Handles compressed scan content</li>
     *   <li><strong>Secret Extraction:</strong> Processes and formats detected secrets</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When secret detection errors are present, the content includes diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Repository access denied: Insufficient permissions for secret scanning
     * Source: protected_repository
     * Message: Access denied to private repository
     * }
     *
     * <h4>Successful Detection Content Format:</h4>
     * <p>For successful secret detection, provides comprehensive security analysis data:</p>
     * {@snippet lang="text" :
     * Source: src/main/resources/application.properties
     * Message: Configuration file secret scan completed
     * Content: database.password=mySecretPassword123
     * api.key=sk-1234567890abcdef
     * aws.access.key=AKIAIOSFODNN7EXAMPLE
     * Secrets:
     * - Type: database_password, Values: [mySecretPassword123]
     * - Type: api_key, Values: [sk-1234567890abcdef]
     * - Type: aws_access_key, Values: [AKIAIOSFODNN7EXAMPLE]
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Risk Assessment:</strong> AI-driven evaluation of secret exposure severity</li>
     *   <li><strong>False Positive Reduction:</strong> ML-based filtering of test/dummy credentials</li>
     *   <li><strong>Pattern Learning:</strong> Adaptive pattern recognition for new secret types</li>
     *   <li><strong>Compliance Monitoring:</strong> Automated compliance checking &amp; reporting</li>
     *   <li><strong>Remediation Guidance:</strong> AI-generated recommendations for secret management</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Compression Detection:</strong> Automatic detection of compressed content &amp; secrets</li>
     *   <li><strong>Lazy Decompression:</strong> Decompresses only when content is requested</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted or malformed data</li>
     *   <li><strong>Memory Efficiency:</strong> Optimized processing of large secret datasets</li>
     * </ul>
     *
     * @return formatted string containing secret detection information optimized for AI analysis.
     *         Returns error information when present, followed by source context, scan content,
     *         and detailed secret findings, or "No content available" if no data is present.
     * @throws Exception if decompression fails, secret data is corrupted, or processing errors occur
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

        // Source
        String source = (String) data.getOrDefault("source", "");
        if (!source.isEmpty()) {
            content.append("Source: ").append(source).append("\n");
        }

        // Message
        String message = (String) data.getOrDefault("message", "");
        if (!message.isEmpty()) {
            content.append("Message: ").append(message).append("\n");
        }

        // Content
        String contentData = "";
        if (Boolean.TRUE.equals(data.get("content_compressed"))) {
            String encodedContent = (String) data.get("content");
            contentData = decodeGzipMessage(encodedContent);
            this.data.put("content",contentData);
        } else {
            contentData = (String) data.getOrDefault("content", "");
        }
        if (!contentData.isEmpty()) {
            content.append("Content: ").append(contentData).append("\n");
        }

        // Secrets
        Map<String, List<String>> secrets;
        if (Boolean.TRUE.equals(data.get("secrets_compressed"))) {
            String encodedSecrets = (String) data.get("secrets");
            String decodedSecrets = decodeGzipMessage(encodedSecrets);
            secrets = decodedSecrets.isEmpty() ? Collections.emptyMap() : mapper.readValue(decodedSecrets, Map.class);
            this.data.put("secrets",secrets);
        } else {
            secrets = (Map<String, List<String>>) data.getOrDefault("secrets", Collections.emptyMap());
        }
        if (!secrets.isEmpty()) {
            content.append("Secrets:\n");
            for (Map.Entry<String, List<String>> entry : secrets.entrySet()) {
                String secretType = entry.getKey();
                List<String> values = entry.getValue();
                content.append("- Type: ").append(secretType).append(", Values: ").append(values).append("\n");
            }
        }

        return content.length() > 0 ? content.toString() : "No content available";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of secret detection results with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that secret detection results are grouped with
     * related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Security Correlation:</strong> Links secret detection results to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks secret exposure trends &amp; remediation over time</li>
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
     *   <li>Secret detection sequence tracking</li>
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
     * Decodes GZIP-compressed Base64-encoded secret detection content &amp; secret data.
     *
     * <p>This private utility method handles the decompression of secret detection content and
     * secret data that has been compressed using GZIP and encoded with Base64 for efficient
     * transmission. It provides robust error handling and logging for debugging compression-related
     * issues in secret detection workflows.</p>
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
     * Original Secret Data → GZIP Compression → Base64 Encoding → Transmission
     * {"database_password":["secret123"],...} → [GZIP bytes] → "H4sIAAAAAAAA/62QwQrCMBBE..." → Network
     * }
     *
     * <h4>Error Scenarios:</h4>
     * <ul>
     *   <li><strong>Null/Empty Input:</strong> Returns empty string with warning log</li>
     *   <li><strong>Invalid Base64:</strong> Logs warning and returns empty string</li>
     *   <li><strong>Corrupted GZIP:</strong> Logs warning and returns empty string</li>
     *   <li><strong>Memory Issues:</strong> Handles large files with streaming decompression</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Memory Efficiency:</strong> Uses streaming decompression for large datasets</li>
     *   <li><strong>Resource Management:</strong> Automatic resource cleanup with try-with-resources</li>
     *   <li><strong>Error Logging:</strong> Comprehensive logging for debugging issues</li>
     *   <li><strong>Graceful Degradation:</strong> Returns empty string on errors to prevent failures</li>
     * </ul>
     *
     * @param encodedMessage the Base64-encoded GZIP-compressed secret detection message
     * @return the decompressed secret detection content as a UTF-8 string,
     *         or empty string if input is null/empty or decompression fails
     * @throws Exception if critical decompression errors occur that cannot be handled gracefully
     *
     * @see Base64#getDecoder()
     * @see GZIPInputStream
     * @see #getContentToAnalyze()
     */
    private String decodeGzipMessage(String encodedMessage) throws Exception {
        if (encodedMessage == null || encodedMessage.isEmpty()) {
            logger.warn("Encoded message is null or empty");
            return "";
        }
        try {
            byte[] compressed = Base64.getDecoder().decode(encodedMessage);
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                String decoded = new String(gzipInputStream.readAllBytes());
//                logger.debug("Decoded GZIP message: {}", decoded);
                return decoded;
            }
        } catch (Exception e) {
            logger.warn("Failed to decode GZIP message: {}", encodedMessage, e);
            return "";
        }
    }

    // ========================================================================
    // OBJECT REPRESENTATION &amp; SERIALIZATION
    // ========================================================================

    /**
     * Returns a string representation of this SecretDetection object.
     *
     * <p>Provides a human-readable representation of the secret detection result including
     * job context, detection data, build number, and error information for debugging
     * and logging purposes.</p>
     *
     * @return string representation of the SecretDetection object
     */
    @Override
    public String toString() {
        return "SecretDetection{" +
                "jobName='" + jobName + '\'' +
                ", data=" + data +
                ", buildNumber=" + buildNumber +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * Converts the secret detection result to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete secret detection information
     * including source context, scan content, detected secrets, compression metadata, error information,
     * and other relevant data. It uses Jackson's ObjectMapper for comprehensive serialization with
     * proper error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all secret detection data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant secret detection information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "secret_detection",
     *   "job_name": "security-scan-pipeline",
     *   "build_number": 123,
     *   "data": {
     *     "source": "src/main/resources/application.properties",
     *     "message": "Configuration file secret scan completed",
     *     "content_compressed": false,
     *     "content": "database.password=mySecretPassword123\napi.key=sk-1234567890abcdef",
     *     "secrets_compressed": false,
     *     "secrets": {
     *       "database_password": ["mySecretPassword123"],
     *       "api_key": ["sk-1234567890abcdef"],
     *       "aws_access_key": ["AKIAIOSFODNN7EXAMPLE"]
     *     }
     *   },
     *   "error": null,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <h4>Compressed Content Output:</h4>
     * <p>For compressed secret detection data, the JSON includes Base64-encoded content:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "secret_detection",
     *   "job_name": "enterprise-secret-scan",
     *   "build_number": 456,
     *   "data": {
     *     "source": "entire_repository_scan",
     *     "message": "Full repository secret scan with 500+ files",
     *     "content_compressed": true,
     *     "content": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwQMf4MmTJ...",
     *     "secrets_compressed": true,
     *     "secrets": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwQMf4MmTJ..."
     *   },
     *   "error": null
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
     *   <li><strong>Security Reporting:</strong> Structured logging of secret detection results</li>
     * </ul>
     *
     * @return JSON string representation of the secret detection result,
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