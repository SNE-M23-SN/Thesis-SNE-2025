package com.diploma.inno.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * JPA Entity for persistent storage of chat messages in CI/CD anomaly detection &amp; AI analysis system.
 *
 * <p>This entity represents individual chat messages within conversation contexts for the Jenkins
 * anomaly detection pipeline. It provides persistent storage for log data, AI analysis results,
 * conversation history, and metadata required for comprehensive security analysis and anomaly
 * detection across CI/CD workflows.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Message Persistence:</strong> Durable storage of chat messages &amp; conversation data</li>
 *   <li><strong>Conversation Management:</strong> Grouping of related messages by conversation ID</li>
 *   <li><strong>Build Correlation:</strong> Links messages to specific Jenkins build executions</li>
 *   <li><strong>Type Classification:</strong> Categorizes messages by type for specialized processing</li>
 *   <li><strong>JSON Content Storage:</strong> Flexible storage of structured log &amp; analysis data</li>
 *   <li><strong>Temporal Tracking:</strong> Precise timestamp management for chronological analysis</li>
 * </ul>
 *
 * <h2>Database Schema &amp; Mapping</h2>
 * <p>The entity maps to the {@code chat_messages} table with the following structure:</p>
 * <pre>{@code
 * Table: chat_messages
 * ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
 * ├── conversation_id (VARCHAR, NOT NULL, INDEX)
 * ├── build_number (INTEGER, NOT NULL, INDEX)
 * ├── message_type (VARCHAR, NOT NULL, INDEX)
 * ├── content (JSONB, NOT NULL)
 * ├── timestamp (TIMESTAMP WITH TIME ZONE, NOT NULL)
 * ├── created_at (TIMESTAMP WITH TIME ZONE, NOT NULL)
 * ├── job_name (VARCHAR, NOT NULL, INDEX)
 * └── metadata (JSONB, NULLABLE)
 * }</pre>
 *
 * <h2>Message Types &amp; Content Structure</h2>
 * <p>The entity supports various message types corresponding to different log data sources:</p>
 * <ul>
 *   <li><strong>build_log_data:</strong> Jenkins build console output &amp; execution logs</li>
 *   <li><strong>secret_detection:</strong> Credential scanning &amp; secret exposure detection results</li>
 *   <li><strong>dependency_data:</strong> Build dependencies, artifacts &amp; plugin information</li>
 *   <li><strong>code_changes:</strong> Version control changes &amp; commit information</li>
 *   <li><strong>additional_info_agent:</strong> Jenkins agent system metrics &amp; performance data</li>
 *   <li><strong>additional_info_controller:</strong> Jenkins controller system metrics &amp; health data</li>
 *   <li><strong>sast_scanning:</strong> Static Application Security Testing results</li>
 * </ul>
 *
 * <h2>JSON Content Examples</h2>
 * <p>Build log data content structure:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "build_log_data",
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "buildNumber": 123,
 *   "job_name": "security-pipeline",
 *   "data": {
 *     "content": "Build started for security analysis...",
 *     "status": "SUCCESS",
 *     "duration": "2m 15s"
 *   }
 * }
 * }
 *
 * <p>Secret detection content structure:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "secret_detection",
 *   "timestamp": "2024-01-15T10:31:00Z",
 *   "buildNumber": 123,
 *   "job_name": "security-scan-pipeline",
 *   "data": {
 *     "source": "src/main/resources/application.properties",
 *     "secrets": {
 *       "database_password": ["mySecretPassword123"],
 *       "api_key": ["sk-1234567890abcdef"]
 *     }
 *   }
 * }
 * }
 *
 * <p>SAST scanning content structure:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "sast_scanning",
 *   "timestamp": "2024-01-15T10:32:00Z",
 *   "buildNumber": 123,
 *   "repoUrl": "https://github.com/company/security-app.git",
 *   "tool": "SonarQube",
 *   "status": "SUCCESS",
 *   "scanResult": "{\"vulnerabilities\":[{\"severity\":\"HIGH\",\"type\":\"SQL_INJECTION\"}]}"
 * }
 * }
 *
 * <h2>Conversation Context &amp; Grouping</h2>
 * <p>Messages are organized into conversations for contextual analysis:</p>
 * <ul>
 *   <li><strong>Conversation ID:</strong> Groups related messages (typically Jenkins job name)</li>
 *   <li><strong>Build Correlation:</strong> Links messages to specific build executions</li>
 *   <li><strong>Temporal Ordering:</strong> Chronological sequence within conversations</li>
 *   <li><strong>Type Diversity:</strong> Multiple message types within single conversations</li>
 * </ul>
 *
 * <h2>Metadata &amp; Extensibility</h2>
 * <p>The metadata field provides flexible storage for additional information:</p>
 * {@snippet lang="json" :
 * {
 *   "metadata": {
 *     "source_system": "jenkins",
 *     "pipeline_stage": "security_analysis",
 *     "priority": "high",
 *     "tags": ["security", "vulnerability", "compliance"],
 *     "processing_flags": {
 *       "ai_analyzed": true,
 *       "anomaly_detected": false,
 *       "requires_review": true
 *     }
 *   }
 * }
 * }
 *
 * <h2>Temporal Management &amp; Timezone Handling</h2>
 * <p>The entity uses sophisticated temporal management:</p>
 * <ul>
 *   <li><strong>OffsetDateTime:</strong> Timezone-aware timestamp storage</li>
 *   <li><strong>UTC+5 Timezone:</strong> Consistent timezone for system operations</li>
 *   <li><strong>Dual Timestamps:</strong> Message timestamp &amp; creation timestamp</li>
 *   <li><strong>Immutable Creation:</strong> Creation timestamp cannot be updated</li>
 * </ul>
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The entity supports comprehensive AI analysis workflows:</p>
 * <ul>
 *   <li><strong>Content Storage:</strong> Structured storage of analysis-ready content</li>
 *   <li><strong>Context Preservation:</strong> Maintains conversation context for AI processing</li>
 *   <li><strong>Historical Analysis:</strong> Enables trend analysis across time periods</li>
 *   <li><strong>Cross-Message Correlation:</strong> Facilitates analysis across message types</li>
 * </ul>
 *
 * <h2>Performance &amp; Optimization</h2>
 * <ul>
 *   <li><strong>Database Indexing:</strong> Optimized indexes on conversation_id, build_number, message_type</li>
 *   <li><strong>JSONB Storage:</strong> Efficient JSON storage with query capabilities</li>
 *   <li><strong>Lombok Integration:</strong> Automatic getter generation for reduced boilerplate</li>
 *   <li><strong>JPA Optimization:</strong> Efficient ORM mapping with minimal overhead</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This entity integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>SimpleDbChatMemory:</strong> Primary storage mechanism for chat memory</li>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; persists log messages</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides persistent data for analysis</li>
 *   <li><strong>Conversation Management:</strong> Enables conversation-based operations</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.component.SimpleDbChatMemory
 * @see com.diploma.inno.component.LogMessageListener
 * @see JsonMapConverter
 */
@Getter
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    // ========================================================================
    // PRIMARY KEY &amp; IDENTITY
    // ========================================================================

    /**
     * Primary key identifier for the chat message entity.
     * <p>Auto-generated unique identifier using database identity strategy.
     * Provides efficient primary key management and referential integrity.</p>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code id}</li>
     *   <li><strong>Type:</strong> {@code BIGINT}</li>
     *   <li><strong>Strategy:</strong> {@code AUTO_INCREMENT}</li>
     *   <li><strong>Constraints:</strong> {@code PRIMARY KEY, NOT NULL}</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // CONVERSATION &amp; GROUPING FIELDS
    // ========================================================================

    /**
     * Conversation identifier for grouping related chat messages.
     * <p>Groups related messages into logical conversations, typically corresponding
     * to Jenkins job names. Enables contextual analysis and conversation-based
     * operations for AI processing and anomaly detection.</p>
     *
     * <h4>Usage Patterns:</h4>
     * <ul>
     *   <li><strong>Job-Based:</strong> Jenkins job name (e.g., "security-pipeline")</li>
     *   <li><strong>Repository-Based:</strong> Repository identifier for multi-repo analysis</li>
     *   <li><strong>Custom Context:</strong> Application-specific conversation identifiers</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code conversation_id}</li>
     *   <li><strong>Type:</strong> {@code VARCHAR}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL, INDEX}</li>
     * </ul>
     */
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    /**
     * Jenkins job name associated with this chat message.
     * <p>Provides direct correlation with Jenkins job execution context.
     * Typically mirrors the conversation ID for job-based conversations.</p>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code job_name}</li>
     *   <li><strong>Type:</strong> {@code VARCHAR}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL, INDEX}</li>
     * </ul>
     */
    @Column(name = "job_name",nullable = false)
    private String jobName;

    // ========================================================================
    // BUILD CORRELATION &amp; TRACKING
    // ========================================================================

    /**
     * Jenkins build number for correlation &amp; tracking across message types.
     * <p>Enables correlation of different message types generated during the same
     * Jenkins build execution, facilitating comprehensive build analysis and
     * cross-message insights for anomaly detection.</p>
     *
     * <h4>Correlation Benefits:</h4>
     * <ul>
     *   <li><strong>Build Tracking:</strong> Links all messages to specific build executions</li>
     *   <li><strong>Cross-Message Analysis:</strong> Enables analysis across different message types</li>
     *   <li><strong>Trend Analysis:</strong> Facilitates build-over-build comparison</li>
     *   <li><strong>Debugging Support:</strong> Provides context for troubleshooting</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code build_number}</li>
     *   <li><strong>Type:</strong> {@code INTEGER}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL, INDEX}</li>
     * </ul>
     */
    @Column(name = "build_number",nullable = false)
    private int buildNumber;

    // ========================================================================
    // MESSAGE CLASSIFICATION &amp; CONTENT
    // ========================================================================

    /**
     * Message type identifier for classification &amp; specialized processing.
     * <p>Categorizes messages by their source and content type, enabling
     * type-specific processing, routing, and analysis within the anomaly
     * detection pipeline.</p>
     *
     * <h4>Supported Message Types:</h4>
     * <ul>
     *   <li><strong>build_log_data:</strong> Jenkins build console output &amp; execution logs</li>
     *   <li><strong>secret_detection:</strong> Credential scanning &amp; secret exposure detection</li>
     *   <li><strong>dependency_data:</strong> Build dependencies, artifacts &amp; plugin information</li>
     *   <li><strong>code_changes:</strong> Version control changes &amp; commit information</li>
     *   <li><strong>additional_info_agent:</strong> Jenkins agent system metrics &amp; performance</li>
     *   <li><strong>additional_info_controller:</strong> Jenkins controller system metrics &amp; health</li>
     *   <li><strong>sast_scanning:</strong> Static Application Security Testing results</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code message_type}</li>
     *   <li><strong>Type:</strong> {@code VARCHAR}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL, INDEX}</li>
     * </ul>
     */
    @Column(name = "message_type", nullable = false)
    private String messageType;

    /**
     * JSON content containing the structured message data.
     * <p>Stores the complete message content in JSON format, providing flexible
     * storage for diverse log data types while maintaining queryability through
     * PostgreSQL's JSONB support.</p>
     *
     * <h4>Content Structure Examples:</h4>
     * <p>Build log data content:</p>
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
     * <p>Secret detection content:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "secret_detection",
     *   "job_name": "security-scan-pipeline",
     *   "data": {
     *     "source": "application.properties",
     *     "secrets": {
     *       "database_password": ["secret123"]
     *     }
     *   }
     * }
     * }
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code content}</li>
     *   <li><strong>Type:</strong> {@code JSONB}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL}</li>
     *   <li><strong>Features:</strong> Full JSON query support, indexing, operators</li>
     * </ul>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private String content;

    // ========================================================================
    // TEMPORAL TRACKING &amp; CHRONOLOGICAL ORDERING
    // ========================================================================

    /**
     * Message timestamp for chronological ordering &amp; temporal analysis.
     * <p>Represents the logical timestamp when the message event occurred,
     * enabling precise chronological ordering and temporal analysis of
     * events within conversations and across the entire system.</p>
     *
     * <h4>Temporal Characteristics:</h4>
     * <ul>
     *   <li><strong>Timezone Aware:</strong> OffsetDateTime with UTC+5 offset</li>
     *   <li><strong>Event Time:</strong> Represents when the logged event actually occurred</li>
     *   <li><strong>Chronological Ordering:</strong> Enables proper event sequencing</li>
     *   <li><strong>Analysis Support:</strong> Facilitates time-based pattern recognition</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code timestamp}</li>
     *   <li><strong>Type:</strong> {@code TIMESTAMP WITH TIME ZONE}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL}</li>
     * </ul>
     */
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Entity creation timestamp for audit &amp; persistence tracking.
     * <p>Immutable timestamp indicating when this entity was first persisted
     * to the database. Provides audit trail and enables tracking of data
     * ingestion patterns and system performance.</p>
     *
     * <h4>Audit Characteristics:</h4>
     * <ul>
     *   <li><strong>Immutable:</strong> Cannot be updated after initial creation</li>
     *   <li><strong>System Time:</strong> Represents database insertion time</li>
     *   <li><strong>Audit Trail:</strong> Enables tracking of data ingestion</li>
     *   <li><strong>Performance Monitoring:</strong> Facilitates system performance analysis</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code created_at}</li>
     *   <li><strong>Type:</strong> {@code TIMESTAMP WITH TIME ZONE}</li>
     *   <li><strong>Constraints:</strong> {@code NOT NULL, IMMUTABLE}</li>
     * </ul>
     */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = true)
    private OffsetDateTime createdAt;

    // ========================================================================
    // METADATA &amp; EXTENSIBILITY
    // ========================================================================

    /**
     * Flexible metadata storage for additional message information &amp; context.
     * <p>Provides extensible storage for additional message metadata, processing
     * flags, analysis results, and contextual information that doesn't fit into
     * the structured fields. Stored as JSON for flexibility and queryability.</p>
     *
     * <h4>Common Metadata Examples:</h4>
     * {@snippet lang="json" :
     * {
     *   "source_system": "jenkins",
     *   "pipeline_stage": "security_analysis",
     *   "priority": "high",
     *   "tags": ["security", "vulnerability", "compliance"],
     *   "processing_flags": {
     *     "ai_analyzed": true,
     *     "anomaly_detected": false,
     *     "requires_review": true
     *   },
     *   "analysis_results": {
     *     "risk_score": 7.5,
     *     "confidence": 0.92,
     *     "categories": ["security", "performance"]
     *   }
     * }
     * }
     *
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li><strong>Processing Flags:</strong> Track analysis status &amp; processing state</li>
     *   <li><strong>Analysis Results:</strong> Store AI analysis outcomes &amp; scores</li>
     *   <li><strong>Contextual Tags:</strong> Categorization &amp; classification metadata</li>
     *   <li><strong>System Information:</strong> Source system &amp; pipeline context</li>
     *   <li><strong>Custom Extensions:</strong> Application-specific metadata</li>
     * </ul>
     *
     * <h4>Database Mapping:</h4>
     * <ul>
     *   <li><strong>Column:</strong> {@code metadata}</li>
     *   <li><strong>Type:</strong> {@code JSONB} (via JsonMapConverter)</li>
     *   <li><strong>Constraints:</strong> {@code NULLABLE}</li>
     *   <li><strong>Converter:</strong> {@link JsonMapConverter} for Map&lt;String, Object&gt; serialization</li>
     * </ul>
     */
    @Column(name = "metadata")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> metadata;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * Default no-argument constructor for JPA entity instantiation.
     * <p>Required by JPA specification for entity instantiation during
     * database operations and ORM framework usage.</p>
     */
    public ChatMessageEntity() {
    }

    /**
     * Parameterized constructor for creating chat message entities with full context.
     * <p>Creates a new chat message entity with all required fields and metadata.
     * Automatically sets timestamps to current time with UTC+5 timezone offset.</p>
     *
     * <h4>Constructor Behavior:</h4>
     * <ul>
     *   <li><strong>Conversation Setup:</strong> Sets conversation ID and mirrors to job name</li>
     *   <li><strong>Build Correlation:</strong> Associates message with specific build number</li>
     *   <li><strong>Type Classification:</strong> Sets message type for proper routing</li>
     *   <li><strong>Content Storage:</strong> Stores JSON content for analysis</li>
     *   <li><strong>Timestamp Generation:</strong> Sets both message and creation timestamps</li>
     *   <li><strong>Metadata Assignment:</strong> Stores additional contextual information</li>
     * </ul>
     *
     * <h4>Timezone Handling:</h4>
     * <p>Both timestamp and createdAt are set to current time with UTC+5 offset
     * for consistent temporal coordination across the system.</p>
     *
     * <h4>Usage Example:</h4>
     * {@snippet lang="java" :
     * Map<String, Object> metadata = new HashMap<>();
     * metadata.put("priority", "high");
     * metadata.put("source_system", "jenkins");
     *
     * ChatMessageEntity message = new ChatMessageEntity(
     *     "security-pipeline",     // conversationId
     *     123,                     // buildNumber
     *     "build_log_data",        // messageType
     *     "{\"status\":\"SUCCESS\"}", // content (JSON)
     *     metadata                 // metadata
     * );
     * }
     *
     * @param conversationId the conversation identifier for grouping related messages
     * @param buildNumber the Jenkins build number for correlation
     * @param messageType the message type for classification and routing
     * @param content the JSON content containing the structured message data
     * @param metadata additional metadata for extensibility and context
     */
    public ChatMessageEntity(String conversationId,int buildNumber, String messageType, String content, Map<String, Object> metadata) {
        this.conversationId = conversationId;
        this.buildNumber = buildNumber;
        this.jobName = conversationId;
        this.messageType = messageType;
        this.content = content;
        this.timestamp = OffsetDateTime.now(ZoneOffset.ofHours(5));
        this.createdAt = OffsetDateTime.now(ZoneOffset.ofHours(5));
        this.metadata = metadata;
    }

    // ========================================================================
    // PROPERTY SETTERS - ENTITY MANAGEMENT
    // ========================================================================

    /**
     * Sets the primary key identifier for the chat message entity.
     * <p>Typically used by JPA framework during entity persistence and retrieval.
     * Should not be manually set in application code.</p>
     *
     * @param id the primary key identifier to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the conversation identifier for grouping related messages.
     * <p>Updates the conversation context for this message, enabling
     * proper grouping and contextual analysis.</p>
     *
     * @param conversationId the conversation identifier to set
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Sets the message type for classification and routing.
     * <p>Updates the message type, which determines how the message
     * is processed and analyzed within the system.</p>
     *
     * @param messageType the message type identifier to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the JSON content containing the structured message data.
     * <p>Updates the message content with new JSON data. The content
     * should be valid JSON for proper storage and querying.</p>
     *
     * @param content the JSON content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the message timestamp for chronological ordering.
     * <p>Updates the logical timestamp when the message event occurred.
     * Should use timezone-aware OffsetDateTime for consistency.</p>
     *
     * @param timestamp the message timestamp to set
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the entity creation timestamp for audit tracking.
     * <p>Updates the creation timestamp. Note that this field is typically
     * immutable and should only be set during initial entity creation.</p>
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the metadata for additional message information and context.
     * <p>Updates the flexible metadata storage with new contextual information,
     * processing flags, or analysis results.</p>
     *
     * @param metadata the metadata map to set
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}