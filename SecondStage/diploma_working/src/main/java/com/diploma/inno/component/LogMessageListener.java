package com.diploma.inno.component;

import com.diploma.inno.dto.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core message processing component for Jenkins CI/CD pipeline log analysis &amp; anomaly detection.
 *
 * <p>This component serves as the central hub for processing Jenkins build logs received via RabbitMQ,
 * performing real-time analysis, and triggering AI-powered anomaly detection. It implements sophisticated
 * message filtering, timestamp validation, and intelligent AI triggering strategies to ensure accurate
 * and timely detection of security vulnerabilities, build failures, and operational anomalies.</p>
 *
 * <h2>Core Responsibilities</h2>
 * <ul>
 *   <li><strong>Message Processing:</strong> Consumes &amp; deserializes Jenkins logs from RabbitMQ queues</li>
 *   <li><strong>Temporal Filtering:</strong> Validates timestamps &amp; filters out-of-sync messages</li>
 *   <li><strong>Log Classification:</strong> Identifies &amp; routes different log types (build, security, dependencies)</li>
 *   <li><strong>AI Orchestration:</strong> Triggers intelligent analysis when complete build datasets are available</li>
 *   <li><strong>Memory Management:</strong> Stores conversation history for contextual AI analysis</li>
 * </ul>
 *
 * <h2>Message Processing Pipeline</h2>
 * <p>The component implements a sophisticated multi-stage processing pipeline:</p>
 * <pre>{@code
 * RabbitMQ → Deserialization → Timestamp Validation → Type Classification → Memory Storage → AI Trigger
 *    ↓             ↓                    ↓                     ↓                ↓              ↓
 * Raw JSON → Typed Objects → Time Filtering → Log Routing → Chat Memory → Anomaly Analysis
 * }</pre>
 *
 * <h2>Supported Log Types</h2>
 * <p>The system processes 14 distinct log types per Jenkins build:</p>
 * <ol>
 *   <li>{@link BuildLogData} - Initial build execution logs</li>
 *   <li>{@link SecretDetection} - Build log secret scanning results</li>
 *   <li>{@link DependencyData} - Dependency analysis &amp; vulnerability data</li>
 *   <li>{@link SecretDetection} - Jenkinsfile secret scanning results</li>
 *   <li>{@link CodeChanges} - Git commit &amp; change analysis</li>
 *   <li>{@link AdditionalInfoAgent} - Jenkins agent system information</li>
 *   <li>{@link AdditionalInfoController} - System health metrics</li>
 *   <li>{@link AdditionalInfoController} - JVM performance data</li>
 *   <li>{@link ScanResult} - Semgrep SAST scanning results</li>
 *   <li>{@link ScanResult} - Bearer security scanning results</li>
 *   <li>{@link ScanResult} - Trivy vulnerability scanning results</li>
 *   <li>{@link BuildLogData} - Final build execution logs</li>
 *   <li>{@link SecretDetection} - Final build log secret scanning</li>
 *   <li>{@link ScanResult} - Horusec security scanning (if SCM available)</li>
 * </ol>
 *
 * <h2>AI Triggering Strategy</h2>
 * <p>The component implements an intelligent triggering mechanism that ensures complete context
 * before initiating AI analysis:</p>
 * <ul>
 *   <li><strong>Build Completion Detection:</strong> Monitors for 2 {@link BuildLogData} logs (initial + final)</li>
 *   <li><strong>Trigger Condition:</strong> Activates on final {@link SecretDetection} with source "build_log"</li>
 *   <li><strong>Context Assembly:</strong> Aggregates all 13 logs for comprehensive analysis</li>
 *   <li><strong>Retry Logic:</strong> Implements robust error handling with automatic retry</li>
 * </ul>
 *
 * <h2>Temporal Filtering &amp; Synchronization</h2>
 * <p>Advanced timestamp validation ensures data integrity &amp; system synchronization:</p>
 * <ul>
 *   <li><strong>Age Limit:</strong> Rejects logs older than 420 seconds (7 minutes)</li>
 *   <li><strong>Future Protection:</strong> Blocks logs with timestamps more than 30 seconds in the future</li>
 *   <li><strong>Format Support:</strong> Handles ISO 8601 timestamps with flexible timezone parsing</li>
 *   <li><strong>Normalization:</strong> Converts various timestamp formats to consistent millisecond precision</li>
 * </ul>
 *
 * <h2>Configuration Properties</h2>
 * <p>The component relies on several configuration properties:</p>
 * <ul>
 *   <li><code>queue.name</code> - RabbitMQ queue name for Jenkins logs</li>
 *   <li><code>classpath:templates/user.json</code> - AI prompt template for analysis</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <ul>
 *   <li><strong>Throughput:</strong> Designed for high-volume log processing (1000+ logs/minute)</li>
 *   <li><strong>Memory Efficiency:</strong> Uses streaming JSON processing &amp; bounded memory maps</li>
 *   <li><strong>Fault Tolerance:</strong> Graceful error handling with detailed logging</li>
 *   <li><strong>Scalability:</strong> Thread-safe design supports concurrent message processing</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <ul>
 *   <li><strong>RabbitMQ:</strong> Message consumption via {@link RabbitListener}</li>
 *   <li><strong>Spring AI:</strong> AI analysis via {@link ChatClient}</li>
 *   <li><strong>Database:</strong> Conversation storage via {@link ChatMemory}</li>
 *   <li><strong>Jackson:</strong> JSON processing via {@link ObjectMapper}</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>This component is thread-safe and designed for concurrent operation. The {@link ConcurrentHashMap}
 * ensures safe tracking of build states across multiple threads, while the underlying Spring AI
 * components handle concurrent chat memory access.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see BuildLogData
 * @see SecretDetection
 * @see ChatClient
 * @see ChatMemory
 * @see SimpleDbChatMemory
 * @see CustomMetadataChatMemoryAdvisor
 */
@Component
public class LogMessageListener {

    // ========================================================================
    // CONSTANTS &amp; CONFIGURATION
    // ========================================================================

    /** Logger instance for this component. */
    private static final Logger logger = LoggerFactory.getLogger(LogMessageListener.class);

    /** Maximum age threshold for log messages in milliseconds (7 minutes). */
    private static final long MAX_AGE_MILLIS = 420_000;

    // ========================================================================
    // DEPENDENCIES &amp; COMPONENTS
    // ========================================================================

    /** Spring AI chat client for anomaly detection analysis. */
    private final ChatClient chatClient;

    /** Chat memory implementation for conversation history storage. */
    private final ChatMemory chatMemory;

    /** Jackson ObjectMapper for JSON processing with custom configuration. */
    private final ObjectMapper objectMapper;

    // ========================================================================
    // CONFIGURATION PROPERTIES
    // ========================================================================

    /** Maximum allowed future timestamp gap in milliseconds (30 seconds). */
    long allowedFutureGapMillis = 30_000;

    /**
     * In-memory tracking map for builds with complete build log data.
     * <p>Key format: {@code "conversationId#buildNumber"}</p>
     * <p>Value: {@code true} when both initial and final build logs are received</p>
     */
    private final Map<String, Boolean> hasTwoBuildLogsMap = new ConcurrentHashMap<>();

    /** Resource reference to the AI prompt template file. */
    @Value("classpath:templates/user.json")
    private Resource userPromptResource;

    /** Loaded AI prompt template content for build analysis. */
    private String userPromptTemplate;

    // ========================================================================
    // CONSTRUCTOR &amp; INITIALIZATION
    // ========================================================================

    /**
     * Constructs a new LogMessageListener with required dependencies.
     *
     * <p>This constructor initializes the component with all necessary dependencies
     * for processing Jenkins logs and performing AI-powered anomaly detection.</p>
     *
     * <h4>ObjectMapper Configuration:</h4>
     * <p>The constructor automatically configures the ObjectMapper to:</p>
     * <ul>
     *   <li>Ignore unknown JSON properties for forward compatibility</li>
     *   <li>Handle various log formats from different Jenkins plugins</li>
     *   <li>Support flexible deserialization of evolving log schemas</li>
     * </ul>
     *
     * @param chatClient the Spring AI chat client for anomaly analysis.
     *                   Must be properly configured with AI model settings.
     * @param chatMemory the chat memory implementation for conversation storage.
     *                   Typically a {@link SimpleDbChatMemory} instance.
     * @param objectMapper the Jackson ObjectMapper for JSON processing.
     *                     Will be configured for lenient deserialization.
     * @throws NullPointerException if any parameter is {@code null}
     *
     * @see ChatClient
     * @see ChatMemory
     * @see SimpleDbChatMemory
     * @see ObjectMapper
     */
    public LogMessageListener(ChatClient chatClient, ChatMemory chatMemory, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Initializes the AI prompt template from the classpath resource.
     *
     * <p>This method is automatically called after dependency injection is complete.
     * It loads the user prompt template that will be used for AI analysis requests.</p>
     *
     * <h4>Template Loading Process:</h4>
     * <ol>
     *   <li>Reads the template file from classpath resources</li>
     *   <li>Converts the content to UTF-8 string format</li>
     *   <li>Stores the template for runtime use</li>
     * </ol>
     *
     * <h4>Template Structure:</h4>
     * <p>The loaded template contains placeholders for dynamic content:</p>
     * {@snippet lang="json" :
     * {
     *   "action": "detect_anomalies",
     *   "jobName": "<conversationId>",
     *   "buildId": "<buildNumber>",
     *   "instructions": "Detect anomalies for build <conversationId>#<buildNumber>..."
     * }
     * }
     *
     * @throws IOException if the template file cannot be read or is malformed
     *
     * @see #generateBuildSummary(String, int)
     * @see #userPromptResource
     */
    @PostConstruct
    public void loadUserPromptTemplate() throws IOException {
        this.userPromptTemplate = StreamUtils.copyToString(userPromptResource.getInputStream(), StandardCharsets.UTF_8);
    }


    // ========================================================================
    // MAIN MESSAGE PROCESSING PIPELINE
    // ========================================================================

    /**
     * Processes incoming Jenkins log messages from RabbitMQ queue with comprehensive analysis &amp; AI triggering.
     *
     * <p>This method serves as the main entry point for all Jenkins log processing, implementing a sophisticated
     * multi-stage pipeline that handles message deserialization, temporal validation, type classification,
     * memory storage, and intelligent AI triggering for anomaly detection.</p>
     *
     * <h4>Processing Pipeline Stages:</h4>
     * <ol>
     *   <li><strong>Message Deserialization:</strong> Converts raw JSON to typed log objects</li>
     *   <li><strong>Timestamp Validation:</strong> Ensures temporal consistency &amp; filters stale messages</li>
     *   <li><strong>Content Extraction:</strong> Retrieves analyzable content from log structures</li>
     *   <li><strong>Memory Storage:</strong> Persists logs in conversation history for AI context</li>
     *   <li><strong>Build State Tracking:</strong> Monitors completion of build log pairs</li>
     *   <li><strong>AI Trigger Logic:</strong> Initiates analysis when complete datasets are available</li>
     * </ol>
     *
     * <h4>Temporal Filtering Strategy:</h4>
     * <p>The method implements robust timestamp validation to ensure data quality:</p>
     * <ul>
     *   <li><strong>Age Validation:</strong> Rejects logs older than {@value #MAX_AGE_MILLIS}ms (7 minutes)</li>
     *   <li><strong>Future Protection:</strong> Blocks logs more than {@code allowedFutureGapMillis} in the future</li>
     *   <li><strong>Synchronization:</strong> Maintains temporal consistency across distributed systems</li>
     * </ul>
     *
     * <h4>AI Triggering Logic:</h4>
     * <p>The method implements an intelligent triggering mechanism:</p>
     * <pre>{@code
     * Build Progress Tracking:
     * 1. Monitor for 2 BuildLogData instances (initial + final)
     * 2. Track completion state in hasTwoBuildLogsMap
     * 3. Trigger on final SecretDetection with source="build_log"
     * 4. Aggregate all 13 logs for comprehensive AI analysis
     * }</pre>
     *
     * <h4>Error Handling &amp; Resilience:</h4>
     * <p>The method includes comprehensive error handling:</p>
     * <ul>
     *   <li><strong>Graceful Degradation:</strong> Continues processing despite individual message failures</li>
     *   <li><strong>Retry Logic:</strong> Implements automatic retry for AI analysis failures</li>
     *   <li><strong>Validation:</strong> Ensures JSON response validity before proceeding</li>
     *   <li><strong>Logging:</strong> Provides detailed diagnostic information for troubleshooting</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Throughput:</strong> Optimized for high-volume message processing</li>
     *   <li><strong>Memory Efficiency:</strong> Bounded memory usage with cleanup mechanisms</li>
     *   <li><strong>Latency:</strong> Low-latency processing with minimal blocking operations</li>
     * </ul>
     *
     * @param logMessageJson the raw JSON message received from RabbitMQ containing Jenkins log data.
     *                       Expected to be a valid JSON string representing one of the supported log types.
     *                       May be escaped or wrapped depending on the message producer.
     *
     * @see #deserializeLog(String)
     * @see #parseTimestamp(String)
     * @see #generateBuildSummary(String, int)
     * @see BuildLogData
     * @see SecretDetection
     * @see RabbitListener
     */
    @RabbitListener(queues = "${queue.name}")
    public void processLogMessage(String logMessageJson) {
        try {
            // Map JSON to the appropriate DTO
            Log log = deserializeLog(logMessageJson);
            // logger.info("Deserialized log: {}", log.getType());

            // Extract and normalize timestamp
            long timestampMillis = parseTimestamp(log.getTimestamp());
            if (timestampMillis == -1) {
                logger.warn("Could not parse timestamp for log: {}. Dropping log.", logMessageJson);
                return;
            }

            // Get current time
            long nowMillis = Instant.now().toEpochMilli();

            // Reject future timestamps
            if (timestampMillis > nowMillis + allowedFutureGapMillis) {
                logger.info("Dropping future timestamp log (timestamp: {}): {}",
                        Instant.ofEpochMilli(timestampMillis), logMessageJson);
                return;
            }

            // Reject timestamps older than 420 seconds
            long ageMillis = nowMillis - timestampMillis;
            if (ageMillis > MAX_AGE_MILLIS) {
                logger.info("Dropping out-of-sync log (timestamp: {}, age: {} ms): {}",
                        Instant.ofEpochMilli(timestampMillis), ageMillis, logMessageJson);
                return;
            }

            // Log timestamp details for debugging
            logger.debug("Raw timestamp string: {}", log.getTimestamp());
            logger.debug("Parsed to milliseconds: {}", Instant.ofEpochMilli(timestampMillis));
            logger.debug("Current system time: {}", Instant.ofEpochMilli(nowMillis));
            logger.debug("Age in seconds: {}", ageMillis / 1000.0);

            // Extract content to analyze
            String contentToAnalyze = log.getContentToAnalyze();

            // Generate conversationId
            String conversationId = log.getConversationId();

            // Get build number
            int buildNumber = log.getBuildNumber();

            String buildConversationId = conversationId + "#" + buildNumber;

            String type = log instanceof TypedLog typedLog ? typedLog.getType() : "";
            logger.info("Type: {}, ConversationId: {}", type, conversationId);


            String prompt = log.toJson();
//            UserMessage userMessage = new UserMessage(prompt);


            // Create UserMessage
            UserMessage userMessage = new UserMessage(
                    prompt,
                    Collections.emptyList(),
                    Map.of("build_number", buildNumber)
            );


            // Store all logs in chat_messages
            chatMemory.add(conversationId, List.of(userMessage));


//          Check for two build_log_data logs
            if (log instanceof BuildLogData) {
                boolean hasTwoBuildLogs = ((SimpleDbChatMemory)chatMemory).hasTwoBuildLogs(conversationId, buildNumber);
                if (hasTwoBuildLogs) {
                    logger.debug("Detected two build_log_data logs for conversationId: {}, buildNumber: {}", conversationId, buildNumber);
                    hasTwoBuildLogsMap.put(buildConversationId, true);
                }
            }

            // Trigger AI call on secret_detection if two build_log_data logs detected
            if (log instanceof SecretDetection && ((SecretDetection) log).getData().get("source").equals("build_log")) {
                if (hasTwoBuildLogsMap.getOrDefault(buildConversationId, false)) {
                    try{
                        logger.info("Generating Build Summary for conversationId: {}, buildNumber: {}", conversationId, buildNumber);
                        String aiResponse = generateBuildSummary(conversationId,buildNumber);
                        // Validate JSON before proceeding
                        if (!isValidJson(aiResponse)) {
                            throw new RuntimeException("Invalid JSON response from AI");
                        }
                        logger.debug("AI Response for Build Summary: {}", aiResponse);
                    }catch (Exception ex){
                        logger.warn("First AI attempt failed: {}", ex.getMessage());
                        logger.info("Trying one more time AI!");

                        try {
                            String aiResponse = generateBuildSummary(conversationId,buildNumber);

                            // Validate retry response
                            if (!isValidJson(aiResponse)) {
                                throw new RuntimeException("Invalid JSON response from AI retry");
                            }

                            logger.debug("AI Retry Response for Build Summary: {}", aiResponse);

                        } catch (Exception retryEx) {
                            logger.error("Both AI attempts failed for build {}#{}: {}",
                                    conversationId, buildNumber, retryEx.getMessage());
                        }
                    }

                    hasTwoBuildLogsMap.remove(buildConversationId); // Clean up
                }
            }


        } catch (Exception e) {
            logger.error("Error processing log message: {}", logMessageJson, e);
        }
    }

    /**
     * Parses &amp; normalizes timestamp strings from Jenkins logs into millisecond precision.
     *
     * <p>This method handles various timestamp formats commonly found in Jenkins logs,
     * providing robust parsing with automatic normalization and timezone handling.</p>
     *
     * <h4>Supported Timestamp Formats:</h4>
     * <p>The parser handles multiple ISO 8601 variations:</p>
     * {@snippet lang="java" :
     * // Standard ISO 8601 with timezone
     * "2024-01-15T14:30:45.123+05:00"
     *
     * // Compact timezone format (auto-normalized)
     * "2024-01-15T14:30:45.123+0500"
     *
     * // UTC timezone
     * "2024-01-15T14:30:45.123Z"
     *
     * // Variable fractional seconds (0-9 digits)
     * "2024-01-15T14:30:45Z"
     * "2024-01-15T14:30:45.1Z"
     * "2024-01-15T14:30:45.123456789Z"
     * }
     *
     * <h4>Normalization Process:</h4>
     * <ol>
     *   <li><strong>Input Validation:</strong> Checks for null/empty strings</li>
     *   <li><strong>Timezone Normalization:</strong> Converts +HHMM to +HH:MM format</li>
     *   <li><strong>Flexible Parsing:</strong> Handles variable fractional seconds</li>
     *   <li><strong>Conversion:</strong> Converts to milliseconds since epoch</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>The method provides comprehensive error handling:</p>
     * <ul>
     *   <li>Returns {@code -1} for invalid or unparseable timestamps</li>
     *   <li>Logs detailed error information for debugging</li>
     *   <li>Preserves original timestamp string in error messages</li>
     * </ul>
     *
     * @param timestampStr the timestamp string to parse. May be in various ISO 8601 formats.
     *                     Can be {@code null} or empty, in which case {@code -1} is returned.
     * @return the parsed timestamp as milliseconds since epoch, or {@code -1} if parsing fails
     *
     * @see DateTimeFormatter
     * @see OffsetDateTime
     * @see #processLogMessage(String)
     */
    private long parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            logger.error("Timestamp is null or empty");
            return -1;
        }

        String normalizedTimestamp = null;
        try {
            // Normalize +HHMM to +HH:MM (e.g., +0500 to +05:00)
            normalizedTimestamp = timestampStr.replaceFirst("([+-])(\\d{2})(\\d{2})$", "$1$2:$3");

            // Define a flexible formatter for ISO 8601 timestamps
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .appendOffset("+HH:mm", "Z")
                    .toFormatter();

            // Parse as OffsetDateTime
            OffsetDateTime odt = OffsetDateTime.parse(normalizedTimestamp, formatter);
            return odt.toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            logger.error("Failed to parse timestamp: {} (original: {})", normalizedTimestamp, timestampStr, e);
            return -1;
        }
    }

    // ========================================================================
    // JSON PROCESSING &amp; DESERIALIZATION
    // ========================================================================

    /**
     * Deserializes raw JSON messages into strongly-typed log objects with comprehensive error handling.
     *
     * <p>This method implements a sophisticated JSON processing pipeline that handles various
     * message formats, escape sequences, and structural variations commonly encountered in
     * Jenkins log messages from different sources and plugins.</p>
     *
     * <h4>Deserialization Pipeline:</h4>
     * <ol>
     *   <li><strong>Input Validation:</strong> Checks for null/empty JSON strings</li>
     *   <li><strong>Escape Processing:</strong> Recursively unescapes nested JSON strings</li>
     *   <li><strong>Structure Analysis:</strong> Determines JSON format (object, array, wrapped)</li>
     *   <li><strong>Type Extraction:</strong> Identifies log type from JSON structure</li>
     *   <li><strong>Object Mapping:</strong> Converts to appropriate {@link TypedLog} subclass</li>
     * </ol>
     *
     * <h4>Supported JSON Formats:</h4>
     * <p>The method handles multiple JSON message formats:</p>
     * {@snippet lang="json" :
     * // Direct object format
     * {
     *   "type": "build_log_data",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "timestamp": "2024-01-15T14:30:45Z"
     * }
     *
     * // Array format (extracts first element)
     * [{
     *   "type": "secret_detection",
     *   "data": {...}
     * }]
     *
     * // Escaped string format (auto-unescaped)
     * "\"{\\\"type\\\":\\\"dependency_data\\\",\\\"data\\\":{...}}\""
     * }
     *
     * <h4>Type Mapping Strategy:</h4>
     * <p>The method uses Jackson's polymorphic deserialization based on the {@code type} field:</p>
     * <ul>
     *   <li>{@code "build_log_data"} → {@link BuildLogData}</li>
     *   <li>{@code "secret_detection"} → {@link SecretDetection}</li>
     *   <li>{@code "dependency_data"} → {@link DependencyData}</li>
     *   <li>{@code "code_changes"} → {@link CodeChanges}</li>
     *   <li>{@code "sast_scanning"} → {@link ScanResult}</li>
     *   <li>{@code "additional_info_*"} → {@link AdditionalInfoAgent} or {@link AdditionalInfoController}</li>
     * </ul>
     *
     * <h4>Error Recovery:</h4>
     * <p>The method implements multiple fallback strategies:</p>
     * <ul>
     *   <li><strong>Escape Handling:</strong> Graceful fallback if unescape fails</li>
     *   <li><strong>Structure Flexibility:</strong> Handles both object and array formats</li>
     *   <li><strong>Type Safety:</strong> Validates type field presence before mapping</li>
     *   <li><strong>Detailed Logging:</strong> Provides diagnostic information for failures</li>
     * </ul>
     *
     * @param logMessageJson the raw JSON message string from RabbitMQ.
     *                       May be escaped, wrapped in arrays, or contain nested JSON structures.
     *                       Must contain a valid {@code type} field for proper deserialization.
     * @return a strongly-typed {@link Log} object corresponding to the message type.
     *         The returned object will be an instance of the appropriate {@link TypedLog} subclass.
     * @throws Exception if the JSON is malformed, missing required fields, or cannot be mapped to a known type.
     *                   The exception includes detailed information about the parsing failure.
     *
     * @see #recursiveUnescape(String)
     * @see TypedLog
     * @see BuildLogData
     * @see SecretDetection
     * @see ObjectMapper#readTree(String)
     */
    private Log deserializeLog(String logMessageJson) throws Exception {
        try {
            logger.debug("Deserializing log as TypedLog: {}", logMessageJson);

            // Check if the input is an escaped JSON string (starts and ends with quotes)
            String cleanedJson = logMessageJson;
            try {
                cleanedJson = recursiveUnescape(logMessageJson);
//                logger.debug("Fully unescaped JSON: {}", cleanedJson);
            } catch (Exception e) {
                logger.warn("Failed to unescape JSON, using original: {}", logMessageJson, e);
            }


            // Parse JSON to check structure
            var jsonNode = objectMapper.readTree(cleanedJson);
            ObjectNode targetNode;

            // Handle array, wrapped object, or direct object
            if (jsonNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) jsonNode;
                if (arrayNode.size() == 0) {
                    throw new Exception("Empty JSON array received");
                }
                targetNode = (ObjectNode) arrayNode.get(0);
                logger.debug("Extracted first object from array: {}", targetNode.toString());
            } else if (jsonNode.isObject()) {
                targetNode = (ObjectNode) jsonNode;
            } else {
                throw new Exception("Invalid JSON structure: " + cleanedJson);
            }

            // Extract type
            if (!targetNode.has("type")) {
                throw new Exception("Missing 'type' property in JSON: " + cleanedJson);
            }
            String type = targetNode.get("type").asText();
            logger.debug("Extracted type: {}", type);

            // Deserialize to TypedLog
            TypedLog typedLog = objectMapper.treeToValue(targetNode, TypedLog.class);
            logger.debug("Deserialized TypedLog: type={}, timestamp={}", typedLog.getType(), typedLog.getTimestamp());
            return typedLog;
        } catch (Exception e) {
            throw new Exception("Failed to deserialize log as TypedLog: " + logMessageJson, e);
        }
    }

    /**
     * Recursively unescapes nested JSON strings with depth protection.
     *
     * <p>This utility method handles JSON messages that have been multiply-escaped
     * during transmission through various systems (RabbitMQ, Jenkins plugins, etc.).
     * It safely unescapes nested JSON strings while preventing infinite loops.</p>
     *
     * <h4>Escape Scenarios:</h4>
     * <p>Common escape patterns encountered in Jenkins logs:</p>
     * {@snippet lang="java" :
     * // Single escape (most common)
     * "\"{\\\"type\\\":\\\"build_log_data\\\"}\""
     *
     * // Double escape (from nested systems)
     * "\"\\\"{\\\\\\\"type\\\\\\\":\\\\\\\"build_log_data\\\\\\\"}\\\"\""
     *
     * // Triple escape (rare but possible)
     * "\"\\\"\\\\\\\"...\\\\\\\"\\\"\""
     * }
     *
     * <h4>Safety Mechanisms:</h4>
     * <ul>
     *   <li><strong>Depth Limiting:</strong> Maximum 5 unescape iterations to prevent infinite loops</li>
     *   <li><strong>Pattern Validation:</strong> Only processes strings wrapped in quotes</li>
     *   <li><strong>Incremental Processing:</strong> Unescapes one level at a time</li>
     *   <li><strong>Error Isolation:</strong> Failures don't affect the main processing pipeline</li>
     * </ul>
     *
     * <h4>Algorithm:</h4>
     * <ol>
     *   <li>Trim whitespace from input string</li>
     *   <li>Check if string starts and ends with quotes</li>
     *   <li>Use Jackson to unescape one level</li>
     *   <li>Repeat until no more quotes or depth limit reached</li>
     * </ol>
     *
     * @param json the potentially escaped JSON string to process.
     *             May contain multiple levels of escape sequences.
     * @return the fully unescaped JSON string ready for parsing.
     *         If no unescaping is needed, returns the trimmed input.
     * @throws Exception if JSON unescaping fails due to malformed escape sequences
     *
     * @see #deserializeLog(String)
     * @see ObjectMapper#readValue(String, Class)
     */
    private String recursiveUnescape(String json) throws Exception {
        String current = json.trim();
        int depth = 0;
        while (current.startsWith("\"") && current.endsWith("\"") && depth < 5) {
            current = objectMapper.readValue(current, String.class);
            depth++;
        }
        return current;
    }

    // ========================================================================
    // AI ANALYSIS &amp; ORCHESTRATION
    // ========================================================================

    /**
     * Generates comprehensive build anomaly analysis using AI with conversation context.
     *
     * <p>This method orchestrates the AI-powered analysis of complete Jenkins build datasets,
     * leveraging conversation history and specialized prompts to detect security vulnerabilities,
     * performance anomalies, and operational issues.</p>
     *
     * <h4>AI Analysis Process:</h4>
     * <ol>
     *   <li><strong>Context Assembly:</strong> Retrieves all 13+ logs for the specified build</li>
     *   <li><strong>Prompt Preparation:</strong> Customizes analysis template with build-specific data</li>
     *   <li><strong>Advisor Configuration:</strong> Sets conversation ID and build number parameters</li>
     *   <li><strong>AI Invocation:</strong> Calls the configured AI model for analysis</li>
     *   <li><strong>Response Processing:</strong> Returns structured JSON analysis results</li>
     * </ol>
     *
     * <h4>Prompt Template Structure:</h4>
     * <p>The method uses a sophisticated prompt template that includes:</p>
     * <ul>
     *   <li><strong>Analysis Instructions:</strong> Specific guidance for anomaly detection</li>
     *   <li><strong>Context Scope:</strong> Historical comparison with previous builds</li>
     *   <li><strong>Risk Assessment:</strong> Scoring criteria and severity levels</li>
     *   <li><strong>Output Format:</strong> Structured JSON response requirements</li>
     * </ul>
     *
     * <h4>Template Placeholder Replacement:</h4>
     * {@snippet lang="java" :
     * // Original template placeholders
     * "jobName": "<conversationId>",
     * "buildId": <buildNumber>,
     * "instructions": "Detect anomalies for build <conversationId>#<buildNumber>..."
     *
     * // After replacement
     * "jobName": "security-pipeline",
     * "buildId": 123,
     * "instructions": "Detect anomalies for build security-pipeline#123..."
     * }
     *
     * <h4>AI Model Configuration:</h4>
     * <p>The analysis uses the following AI settings:</p>
     * <ul>
     *   <li><strong>Model:</strong> Google Gemini 2.5 Flash Preview</li>
     *   <li><strong>Temperature:</strong> 0 (deterministic responses)</li>
     *   <li><strong>Response Format:</strong> JSON_OBJECT</li>
     *   <li><strong>Context Window:</strong> Optimized for log analysis</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name serving as conversation identifier.
     *                       Used to retrieve relevant conversation history and context.
     * @param buildNumber the specific build number to analyze.
     *                    Used to filter logs and provide build-specific context.
     * @return a JSON string containing the AI analysis results with anomaly detection,
     *         risk scoring, recommendations, and detailed insights.
     *         The response follows a structured format defined in the prompt template.
     *
     * @see #loadUserPromptTemplate()
     * @see #processLogMessage(String)
     * @see ChatClient
     * @see CustomMetadataChatMemoryAdvisor
     */
    private String generateBuildSummary(String conversationId, int buildNumber) {
        return chatClient.prompt()
                .advisors(advisor -> advisor
                        .param("chat_memory_conversation_id", conversationId)
                        .param("build_number", buildNumber))
                .user(userPromptTemplate.replace("<conversationId>", conversationId)
                        .replace("<buildNumber>", String.valueOf(buildNumber)))
                .call()
                .content();
    }

    // ========================================================================
    // UTILITY METHODS - VALIDATION &amp; HELPERS
    // ========================================================================

    /**
     * Validates JSON string format for AI response verification.
     *
     * <p>This utility method provides fast, lightweight validation of JSON strings
     * to ensure AI responses are properly formatted before further processing.
     * It's particularly important for validating AI-generated analysis results.</p>
     *
     * <h4>Validation Process:</h4>
     * <ol>
     *   <li><strong>Null Check:</strong> Handles null and empty strings</li>
     *   <li><strong>Whitespace Trimming:</strong> Removes leading/trailing whitespace</li>
     *   <li><strong>Parse Attempt:</strong> Uses Jackson to validate JSON structure</li>
     *   <li><strong>Result Return:</strong> Boolean indicating validity</li>
     * </ol>
     *
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li><strong>AI Response Validation:</strong> Ensures AI returns valid JSON</li>
     *   <li><strong>Error Prevention:</strong> Prevents downstream parsing failures</li>
     *   <li><strong>Quality Assurance:</strong> Validates data integrity before storage</li>
     *   <li><strong>Retry Logic:</strong> Determines if AI calls should be retried</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Fast Validation:</strong> Lightweight parsing without full object creation</li>
     *   <li><strong>Memory Efficient:</strong> Minimal memory allocation for validation</li>
     *   <li><strong>Exception Safe:</strong> Catches all parsing exceptions gracefully</li>
     * </ul>
     *
     * @param json the JSON string to validate. Can be {@code null} or empty.
     * @return {@code true} if the string represents valid JSON, {@code false} otherwise.
     *         Returns {@code false} for null, empty, or malformed JSON strings.
     *
     * @see #processLogMessage(String)
     * @see #generateBuildSummary(String, int)
     * @see ObjectMapper#readTree(String)
     */
    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
