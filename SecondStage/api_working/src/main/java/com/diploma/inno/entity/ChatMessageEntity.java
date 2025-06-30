package com.diploma.inno.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * JPA Entity representing chat messages in the CI Anomaly Detector system.
 *
 * <p>This entity serves as the central data storage mechanism for the entire CI/CD monitoring
 * and AI analysis pipeline. It stores both build logs (USER messages) and AI analysis results
 * (ASSISTANT messages) in a unified conversation-based structure that enables comprehensive
 * build monitoring, anomaly detection, and trend analysis.</p>
 *
 * <p><strong>Core Entity Purpose:</strong></p>
 * <ul>
 *   <li><strong>Build Log Storage:</strong> Stores Jenkins build logs as USER messages</li>
 *   <li><strong>AI Analysis Storage:</strong> Stores AI analysis results as ASSISTANT messages</li>
 *   <li><strong>Conversation Management:</strong> Groups messages by Jenkins job (conversation_id)</li>
 *   <li><strong>Build Tracking:</strong> Associates messages with specific build numbers</li>
 *   <li><strong>JSONB Analytics:</strong> Enables complex queries on structured AI analysis data</li>
 * </ul>
 *
 * <p><strong>Database Table Structure:</strong></p>
 * <pre>
 * Table: chat_messages
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Column          │ Type             │ Description                                         │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ id              │ BIGINT           │ Primary key, auto-generated                         │
 * │ conversation_id │ VARCHAR          │ Jenkins job name (conversation identifier)          │
 * │ build_number    │ INTEGER          │ Jenkins build number                                │
 * │ message_type    │ VARCHAR          │ 'USER' (build logs) or 'ASSISTANT' (AI analysis)    │
 * │ content         │ JSONB            │ Structured data (logs or AI analysis results)       │
 * │ timestamp       │ TIMESTAMPTZ      │ Message creation time (UTC+5)                       │
 * │ created_at      │ TIMESTAMPTZ      │ Entity creation time (UTC+5)                        │
 * │ job_name        │ VARCHAR          │ Duplicate of conversation_id for query optimization │
 * │ metadata        │ JSONB            │ Additional metadata (converted via JsonMapConverter)│
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><strong>Message Type Classification:</strong></p>
 * <ul>
 *   <li><strong>USER Messages:</strong> Build logs from Jenkins, stored as JSONB chunks</li>
 *   <li><strong>ASSISTANT Messages:</strong> AI analysis results with anomalies, risk scores, insights</li>
 * </ul>
 * <hr>
 * <br>
 * <p><strong>JSONB Content Structure:</strong></p>
 *<br>
 *
 *
 * <p><b>USER messages (Build Logs)</b></p>
 * {@snippet lang=json :
 * {
 *   "type": "build_log_data",
 *   "log_chunk": "...",
 *   "chunk_index": 1,
 *   "total_chunks": 14
 * }
 *}
 * <span><b>ASSISTANT messages (AI Analysis Results)</b></span>
 * {@snippet lang=json :
 * {
 *   "anomalies": [
 *     {
 *       "severity": "CRITICAL|HIGH|MEDIUM|WARNING|LOW",
 *       "type": "security|performance|quality|...",
 *       "description": "...",
 *       "details": "...",
 *       "aiAnalysis": "...",
 *       "recommendation": "..."
 *     }
 *   ],
 *   "buildMetadata": {
 *     "status": "SUCCESS|FAILURE|UNSTABLE|ABORTED",
 *     "duration": "...",
 *     "startTime": "...",
 *     "timestamp": "..."
 *   },
 *   "riskScore": {
 *     "score": 75,
 *     "change": 15,
 *     "riskLevel": "HIGH",
 *     "previousScore": 60
 *   },
 *   "insights": {
 *     "summary": "...",
 *     "recommendations": "[...]",
 *     "trends": "{...}"
 *   }
 * }
 * }
 * <p><strong>Data Flow &amp; Lifecycle:</strong></p>
 * <ol>
 *   <li><strong>Build Log Collection:</strong> Jenkins build logs collected and stored as USER messages</li>
 *   <li><strong>AI Analysis:</strong> External AI service analyzes logs for anomalies and patterns</li>
 *   <li><strong>Result Storage:</strong> AI analysis results stored as ASSISTANT messages with JSONB content</li>
 *   <li><strong>Dashboard Queries:</strong> Complex SQL queries extract data for dashboard visualization</li>
 *   <li><strong>Aggregation:</strong> Scheduled processes create materialized views for performance</li>
 *   <li><strong>Cleanup:</strong> Old messages cleaned up via sliding window approach</li>
 * </ol>
 *
 * <p><strong>Query Patterns &amp; Performance:</strong></p>
 * <ul>
 *   <li><strong>Conversation Filtering:</strong> WHERE conversation_id = :jobName</li>
 *   <li><strong>Build Filtering:</strong> WHERE build_number = :buildNumber</li>
 *   <li><strong>Message Type Filtering:</strong> WHERE message_type = 'USER'|'ASSISTANT'</li>
 *   <li><strong>JSONB Operations:</strong> content->'anomalies', content->>'type', etc.</li>
 *   <li><strong>Time-based Queries:</strong> WHERE timestamp >= :startTime</li>
 *   <li><strong>Aggregation Queries:</strong> COUNT(*), jsonb_array_elements(), etc.</li>
 * </ul>
 *
 * <p><strong>Timezone Handling:</strong></p>
 * <ul>
 *   <li><strong>UTC+5 Offset:</strong> All timestamps stored with ZoneOffset.ofHours(5)</li>
 *   <li><strong>Consistent Timezone:</strong> Both timestamp and created_at use same offset</li>
 *   <li><strong>Query Compatibility:</strong> Timezone-aware queries for accurate filtering</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see com.diploma.inno.repository.ChatMessageRepository
 * @see com.diploma.inno.entity.JsonMapConverter
 */
@Getter
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    /**
     * Primary key identifier for the chat message entity.
     *
     * <p>This field serves as the unique identifier for each chat message record
     * in the database. It is automatically generated using the IDENTITY strategy,
     * ensuring unique identification across all message types and conversations.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code id} (BIGINT)</li>
     *   <li><strong>Generation:</strong> Auto-generated using IDENTITY strategy</li>
     *   <li><strong>Constraints:</strong> Primary key, NOT NULL, UNIQUE</li>
     * </ul>
     *
     * <p><strong>Usage:</strong></p>
     * <ul>
     *   <li><strong>Entity Identification:</strong> Unique identifier for JPA operations</li>
     *   <li><strong>Foreign Key References:</strong> Referenced by related entities if needed</li>
     *   <li><strong>Ordering:</strong> Can be used for chronological ordering when timestamp is insufficient</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conversation identifier that groups related messages by Jenkins job.
     *
     * <p>This field serves as the primary grouping mechanism for chat messages,
     * typically containing the Jenkins job name. It enables conversation-based
     * organization of build logs and AI analysis results for specific CI/CD pipelines.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code conversation_id} (VARCHAR)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Indexing:</strong> Indexed for fast conversation-based queries</li>
     * </ul>
     *
     * <p><strong>Value Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Jenkins Job Names:</strong> Typically matches Jenkins job names exactly</li>
     *   <li><strong>Case Sensitivity:</strong> Exact case matching with Jenkins job configuration</li>
     *   <li><strong>Special Characters:</strong> May contain hyphens, underscores, and other valid job name characters</li>
     *   <li><strong>Uniqueness:</strong> Multiple messages can share the same conversation_id</li>
     * </ul>
     *
     * <p><strong>Query Patterns:</strong></p>
     * <ul>
     *   <li><strong>Conversation Filtering:</strong> WHERE conversation_id = :jobName</li>
     *   <li><strong>Job Discovery:</strong> SELECT DISTINCT conversation_id for job listing</li>
     *   <li><strong>Bulk Operations:</strong> DELETE WHERE conversation_id = :jobName for cleanup</li>
     * </ul>
     *
     * <p><strong>Relationship with job_name:</strong></p>
     * <ul>
     *   <li><strong>Duplication:</strong> Value duplicated in job_name field for query optimization</li>
     *   <li><strong>Consistency:</strong> Should always match job_name field value</li>
     *   <li><strong>Primary Usage:</strong> conversation_id is the primary field for grouping logic</li>
     * </ul>
     */
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    /**
     * Jenkins build number associated with this message.
     *
     * <p>This field identifies the specific Jenkins build that this message relates to,
     * enabling precise tracking of build logs and AI analysis results for individual
     * build executions within a Jenkins job.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code build_number} (INTEGER)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Indexing:</strong> Part of composite index (conversation_id, build_number)</li>
     * </ul>
     *
     * <p><strong>Value Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Jenkins Build Numbers:</strong> Matches Jenkins build numbering sequence</li>
     *   <li><strong>Sequential:</strong> Typically incremental within each job</li>
     *   <li><strong>Positive Integers:</strong> Always positive, starting from 1</li>
     *   <li><strong>Build Lifecycle:</strong> Same build_number for all messages in a build</li>
     * </ul>
     *
     * <p><strong>Query Patterns:</strong></p>
     * <ul>
     *   <li><strong>Build-Specific:</strong> WHERE conversation_id = :job AND build_number = :build</li>
     *   <li><strong>Latest Builds:</strong> ORDER BY build_number DESC for recent builds</li>
     *   <li><strong>Build Range:</strong> WHERE build_number BETWEEN :start AND :end</li>
     *   <li><strong>Build Counting:</strong> COUNT(DISTINCT build_number) for build statistics</li>
     * </ul>
     *
     * <p><strong>Message Association:</strong></p>
     * <ul>
     *   <li><strong>USER Messages:</strong> Build logs associated with specific build execution</li>
     *   <li><strong>ASSISTANT Messages:</strong> AI analysis results for the same build</li>
     *   <li><strong>Temporal Grouping:</strong> All messages for a build share the same build_number</li>
     * </ul>
     */
    @Column(name = "build_number",nullable = false)
    private int buildNumber;

    /**
     * Type classification of the chat message.
     *
     * <p>This field categorizes messages into two primary types that represent different
     * stages of the CI/CD monitoring and analysis pipeline. It enables efficient filtering
     * and processing of messages based on their purpose and content structure.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code message_type} (VARCHAR)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Indexing:</strong> Indexed for fast type-based filtering</li>
     * </ul>
     *
     * <p><strong>Message Type Values:</strong></p>
     * <ul>
     *   <li><strong>"USER":</strong> Build logs from Jenkins, raw log data chunks</li>
     *   <li><strong>"ASSISTANT":</strong> AI analysis results, structured anomaly data</li>
     * </ul>
     *
     * <p><strong>USER Message Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Content:</strong> Raw build log data in JSONB format</li>
     *   <li><strong>Purpose:</strong> Store Jenkins build logs for AI analysis</li>
     *   <li><strong>Structure:</strong> Contains log chunks, indices, and metadata</li>
     *   <li><strong>Volume:</strong> Typically 14 messages per build (expected chunks)</li>
     * </ul>
     *
     * <p><strong>ASSISTANT Message Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Content:</strong> Structured AI analysis results in JSONB format</li>
     *   <li><strong>Purpose:</strong> Store AI-generated anomalies, insights, and risk scores</li>
     *   <li><strong>Structure:</strong> Contains anomalies array, risk scores, build metadata</li>
     *   <li><strong>Volume:</strong> Typically 1 message per build (analysis result)</li>
     * </ul>
     *
     * <p><strong>Query Patterns:</strong></p>
     * <ul>
     *   <li><strong>Log Retrieval:</strong> WHERE message_type = 'USER' for build logs</li>
     *   <li><strong>Analysis Retrieval:</strong> WHERE message_type = 'ASSISTANT' for AI results</li>
     *   <li><strong>Log Counting:</strong> COUNT(*) WHERE message_type = 'USER' for progress tracking</li>
     *   <li><strong>Mixed Queries:</strong> CASE statements for different processing logic</li>
     * </ul>
     *
     * <p><strong>Processing Pipeline:</strong></p>
     * <ul>
     *   <li><strong>Step 1:</strong> USER messages collected from Jenkins build logs</li>
     *   <li><strong>Step 2:</strong> AI service processes USER messages for analysis</li>
     *   <li><strong>Step 3:</strong> ASSISTANT messages created with AI analysis results</li>
     *   <li><strong>Step 4:</strong> Dashboard queries extract data from ASSISTANT messages</li>
     * </ul>
     */
    @Column(name = "message_type", nullable = false)
    private String messageType;

    /**
     * JSONB content field containing structured message data.
     *
     * <p>This field serves as the primary data storage mechanism for both build logs
     * and AI analysis results. The JSONB format enables complex queries, indexing,
     * and efficient storage of structured data while maintaining flexibility for
     * different content types and evolving data schemas.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code content} (JSONB)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Indexing:</strong> GIN indexes for efficient JSONB operations</li>
     *   <li><strong>Type Code:</strong> {@code SqlTypes.JSON} for Hibernate mapping</li>
     * </ul>
     *
     * <p><strong>Content Structure for USER Messages:</strong></p>
     * {@snippet lang=json :
     * {
     *   "type": "build_log_data",
     *   "log_chunk": "Jenkins build log content...",
     *   "chunk_index": 1,
     *   "total_chunks": 14,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <p><strong>Content Structure for ASSISTANT Messages:</strong></p>
     * {@snippet lang=json :
     * {
     *   "anomalies": [
     *     {
     *       "severity": "CRITICAL",
     *       "type": "security",
     *       "description": "Potential API key exposure",
     *       "details": "{...}",
     *       "aiAnalysis": "...",
     *       "recommendation": "..."
     *     }
     *   ],
     *   "buildMetadata": {
     *     "status": "SUCCESS",
     *     "duration": "2m 30s",
     *     "startTime": "2024-01-15T10:30:00Z"
     *   },
     *   "riskScore": {
     *     "score": 75,
     *     "change": 15,
     *     "riskLevel": "HIGH",
     *     "previousScore": 60
     *   },
     *   "insights": {
     *     "summary": "Build completed with security concerns",
     *     "recommendations": "[...]",
     *     "trends": "{...}"
     *   }
     * }
     * }
     *
     *
     * <p><strong>JSONB Query Operations:</strong></p>
     * <ul>
     *   <li><strong>Field Extraction:</strong> content->'anomalies' for JSON objects</li>
     *   <li><strong>Text Extraction:</strong> content->>'type' for text values</li>
     *   <li><strong>Array Operations:</strong> jsonb_array_elements(content->'anomalies')</li>
     *   <li><strong>Containment:</strong> content @> '{"type": "security"}' for filtering</li>
     *   <li><strong>Path Queries:</strong> content#>'{riskScore,score}' for nested access</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong></p>
     * <ul>
     *   <li><strong>GIN Indexing:</strong> Enables fast JSONB queries and containment operations</li>
     *   <li><strong>Compression:</strong> JSONB format provides automatic compression</li>
     *   <li><strong>Query Optimization:</strong> PostgreSQL optimizes JSONB operations</li>
     *   <li><strong>Schema Evolution:</strong> Flexible schema allows for content evolution</li>
     * </ul>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private String content;

    /**
     * Timestamp indicating when the message was created or received.
     *
     * <p>This field records the temporal information for message creation, enabling
     * chronological ordering, time-based filtering, and trend analysis across the
     * CI/CD monitoring pipeline. All timestamps use UTC+5 offset for consistency.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code timestamp} (TIMESTAMPTZ)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Timezone:</strong> UTC+5 (ZoneOffset.ofHours(5))</li>
     *   <li><strong>Indexing:</strong> Indexed for time-based queries</li>
     * </ul>
     *
     * <p><strong>Timestamp Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Precision:</strong> Nanosecond precision via OffsetDateTime</li>
     *   <li><strong>Timezone Awareness:</strong> Includes timezone offset information</li>
     *   <li><strong>Consistency:</strong> All messages use the same timezone offset</li>
     *   <li><strong>Automatic Setting:</strong> Set automatically in constructor</li>
     * </ul>
     *
     * <p><strong>Query Patterns:</strong></p>
     * <ul>
     *   <li><strong>Chronological Ordering:</strong> ORDER BY timestamp ASC/DESC</li>
     *   <li><strong>Time Range Filtering:</strong> WHERE timestamp BETWEEN :start AND :end</li>
     *   <li><strong>Recent Messages:</strong> WHERE timestamp >= :cutoff</li>
     *   <li><strong>Daily Grouping:</strong> DATE_TRUNC('day', timestamp) for aggregation</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Message Ordering:</strong> Chronological display of conversation history</li>
     *   <li><strong>Trend Analysis:</strong> Time-series analysis of build patterns</li>
     *   <li><strong>Data Retention:</strong> Cleanup of old messages based on age</li>
     *   <li><strong>Performance Monitoring:</strong> Analysis of processing times</li>
     * </ul>
     */
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Entity creation timestamp for audit and tracking purposes.
     *
     * <p>This field records when the entity was first persisted to the database,
     * providing audit trail information and enabling tracking of data insertion
     * patterns. It differs from timestamp in that it represents database insertion
     * time rather than logical message time.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code created_at} (TIMESTAMPTZ)</li>
     *   <li><strong>Constraints:</strong> NOT NULL, updatable = false, insertable = true</li>
     *   <li><strong>Timezone:</strong> UTC+5 (ZoneOffset.ofHours(5))</li>
     *   <li><strong>Immutability:</strong> Cannot be updated after initial insert</li>
     * </ul>
     *
     * <p><strong>Audit Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Immutable:</strong> Set once during entity creation, never updated</li>
     *   <li><strong>Automatic:</strong> Automatically set in constructor</li>
     *   <li><strong>Precision:</strong> Nanosecond precision for accurate tracking</li>
     *   <li><strong>Consistency:</strong> Matches timestamp field timezone</li>
     * </ul>
     *
     * <p><strong>Audit Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Data Lineage:</strong> Track when data was first inserted</li>
     *   <li><strong>Performance Analysis:</strong> Measure data processing delays</li>
     *   <li><strong>Debugging:</strong> Identify timing issues in data pipeline</li>
     *   <li><strong>Compliance:</strong> Audit trail for regulatory requirements</li>
     * </ul>
     *
     * <p><strong>Relationship with timestamp:</strong></p>
     * <ul>
     *   <li><strong>Usually Equal:</strong> Both set to same value in constructor</li>
     *   <li><strong>Potential Difference:</strong> May differ if timestamp is manually set</li>
     *   <li><strong>Audit Purpose:</strong> created_at provides immutable audit trail</li>
     * </ul>
     */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = true)
    private OffsetDateTime createdAt;

    /**
     * Jenkins job name for query optimization and denormalization.
     *
     * <p>This field duplicates the conversation_id value to provide query optimization
     * for job-specific operations. While functionally equivalent to conversation_id,
     * it may be used in different indexing strategies or query patterns for performance.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code job_name} (VARCHAR)</li>
     *   <li><strong>Constraints:</strong> NOT NULL</li>
     *   <li><strong>Indexing:</strong> May have separate indexing strategy from conversation_id</li>
     *   <li><strong>Denormalization:</strong> Duplicates conversation_id for optimization</li>
     * </ul>
     *
     * <p><strong>Value Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Identical to conversation_id:</strong> Always matches conversation_id value</li>
     *   <li><strong>Jenkins Job Names:</strong> Exact Jenkins job name format</li>
     *   <li><strong>Automatic Setting:</strong> Set to conversation_id in constructor</li>
     *   <li><strong>Consistency:</strong> Must remain synchronized with conversation_id</li>
     * </ul>
     *
     * <p><strong>Query Optimization:</strong></p>
     * <ul>
     *   <li><strong>Alternative Index:</strong> May use different indexing strategy</li>
     *   <li><strong>Join Optimization:</strong> Optimized for specific join patterns</li>
     *   <li><strong>Materialized Views:</strong> May be used in materialized view definitions</li>
     *   <li><strong>Aggregation Queries:</strong> Optimized for job-based aggregations</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>Job Listing:</strong> SELECT DISTINCT job_name for job discovery</li>
     *   <li><strong>Job Filtering:</strong> WHERE job_name = :jobName for job-specific queries</li>
     *   <li><strong>Bulk Operations:</strong> DELETE WHERE job_name IN (:jobNames)</li>
     *   <li><strong>Analytics:</strong> GROUP BY job_name for job-level statistics</li>
     * </ul>
     */
    @Column(name = "job_name",nullable = false)
    private String jobName;

    /**
     * Additional metadata stored as JSONB for extensibility.
     *
     * <p>This field provides a flexible storage mechanism for additional metadata
     * that doesn't fit into the structured fields. It uses the JsonMapConverter
     * to automatically serialize/deserialize Map objects to/from JSONB format.</p>
     *
     * <p><strong>Database Mapping:</strong></p>
     * <ul>
     *   <li><strong>Column:</strong> {@code metadata} (JSONB)</li>
     *   <li><strong>Constraints:</strong> Nullable (optional metadata)</li>
     *   <li><strong>Converter:</strong> JsonMapConverter for Map&lt;String, Object&gt; serialization</li>
     *   <li><strong>Indexing:</strong> May have GIN indexes for JSONB operations</li>
     * </ul>
     *
     * <p><strong>Content Structure:</strong></p>
     * <ul>
     *   <li><strong>Flexible Schema:</strong> Any key-value pairs as needed</li>
     *   <li><strong>Nested Objects:</strong> Supports nested JSON structures</li>
     *   <li><strong>Type Safety:</strong> Map&lt;String, Object&gt; provides type safety in Java</li>
     *   <li><strong>Evolution:</strong> Allows for schema evolution without migration</li>
     * </ul>
     *
     * <p><strong>Potential Use Cases:</strong></p>
     * <ul>
     *   <li><strong>Processing Metadata:</strong> Information about message processing</li>
     *   <li><strong>Source Information:</strong> Details about message origin</li>
     *   <li><strong>Feature Flags:</strong> Configuration flags for message handling</li>
     *   <li><strong>Debug Information:</strong> Additional debugging context</li>
     *   <li><strong>Integration Data:</strong> Data from external system integrations</li>
     * </ul>
     *
     * <p><strong>Converter Integration:</strong></p>
     * <ul>
     *   <li><strong>JsonMapConverter:</strong> Handles Map to JSONB conversion automatically</li>
     *   <li><strong>Null Handling:</strong> Gracefully handles null metadata</li>
     *   <li><strong>Type Preservation:</strong> Maintains Java type information where possible</li>
     *   <li><strong>Performance:</strong> Efficient serialization/deserialization</li>
     * </ul>
     */
    @Column(name = "metadata")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> metadata;

    /**
     * Default no-argument constructor required by JPA.
     *
     * <p>This constructor is required by JPA/Hibernate for entity instantiation
     * during database operations. It creates an uninitialized entity that will
     * be populated by the persistence framework during entity loading.</p>
     *
     * <p><strong>Usage:</strong></p>
     * <ul>
     *   <li><strong>JPA Requirement:</strong> Required by JPA specification</li>
     *   <li><strong>Framework Use:</strong> Used by Hibernate for entity instantiation</li>
     *   <li><strong>Not for Application Use:</strong> Should not be used directly in application code</li>
     * </ul>
     */
    public ChatMessageEntity() {
    }

    /**
     * Constructs a new ChatMessageEntity with the specified parameters.
     *
     * <p>This constructor creates a fully initialized chat message entity ready for
     * persistence. It automatically sets timestamps using UTC+5 timezone and ensures
     * consistency between conversation_id and job_name fields.</p>
     *
     * <p><strong>Parameter Processing:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> Set as both conversation_id and job_name</li>
     *   <li><strong>buildNumber:</strong> Jenkins build number for message association</li>
     *   <li><strong>messageType:</strong> "USER" for logs or "ASSISTANT" for AI analysis</li>
     *   <li><strong>content:</strong> JSONB content string (pre-serialized)</li>
     *   <li><strong>metadata:</strong> Additional metadata map (can be null)</li>
     * </ul>
     *
     * <p><strong>Automatic Field Setting:</strong></p>
     * <ul>
     *   <li><strong>timestamp:</strong> Set to current time with UTC+5 offset</li>
     *   <li><strong>createdAt:</strong> Set to current time with UTC+5 offset</li>
     *   <li><strong>jobName:</strong> Set to same value as conversationId</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     *
     * <span><b>Create USER message for build logs</b></span>
     * {@snippet lang=java :
     * ChatMessageEntity logMessage = new ChatMessageEntity(
     *     "my-web-app",           // conversationId (Jenkins job name)
     *     123,                    // buildNumber
     *     "USER",                 // messageType
     *     "{\"type\":\"build_log_data\",\"log_chunk\":\"...\"}", // content
     *     null                    // metadata (optional)
     * );
     *}
     *
     * <span><b>Create ASSISTANT message for AI analysis</b></span>
     * {@snippet lang=java :
     * ChatMessageEntity analysisMessage = new ChatMessageEntity(
     *     "my-web-app",           // conversationId
     *     123,                    // buildNumber
     *     "ASSISTANT",            // messageType
     *     "{\"anomalies\":[...],\"riskScore\":{...}}", // content
     *     Map.of("source", "ai-service") // metadata
     * );
     * }
     *
     *
     * <p><strong>Validation Considerations:</strong></p>
     * <ul>
     *   <li><strong>conversationId:</strong> Should match valid Jenkins job name</li>
     *   <li><strong>buildNumber:</strong> Should be positive integer</li>
     *   <li><strong>messageType:</strong> Should be "USER" or "ASSISTANT"</li>
     *   <li><strong>content:</strong> Should be valid JSON string</li>
     *   <li><strong>metadata:</strong> Can be null or valid Map</li>
     * </ul>
     *
     * <p><strong>Timezone Handling:</strong></p>
     * <ul>
     *   <li><strong>Consistent Offset:</strong> Both timestamps use UTC+5</li>
     *   <li><strong>Current Time:</strong> Uses OffsetDateTime.now() for accuracy</li>
     *   <li><strong>Timezone Awareness:</strong> Maintains timezone information</li>
     * </ul>
     *
     * @param conversationId the conversation identifier (Jenkins job name)
     * @param buildNumber the Jenkins build number
     * @param messageType the message type ("USER" or "ASSISTANT")
     * @param content the JSONB content as a string
     * @param metadata additional metadata (can be null)
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

    /**
     * Sets the primary key identifier for this entity.
     *
     * <p><strong>Note:</strong> This setter is primarily used by JPA/Hibernate
     * during entity loading from the database. In normal application flow,
     * the ID is auto-generated and should not be manually set.</p>
     *
     * @param id the primary key identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the conversation identifier for this message.
     *
     * <p><strong>Important:</strong> When changing the conversation ID, consider
     * also updating the job_name field to maintain consistency, as they should
     * always have the same value.</p>
     *
     * @param conversationId the conversation identifier (Jenkins job name)
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Sets the message type classification.
     *
     * <p><strong>Valid Values:</strong></p>
     * <ul>
     *   <li><strong>"USER":</strong> For build log messages</li>
     *   <li><strong>"ASSISTANT":</strong> For AI analysis results</li>
     * </ul>
     *
     * @param messageType the message type
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the JSONB content for this message.
     *
     * <p><strong>Content Requirements:</strong></p>
     * <ul>
     *   <li><strong>Valid JSON:</strong> Must be a valid JSON string</li>
     *   <li><strong>Structure:</strong> Should match expected structure for message type</li>
     *   <li><strong>Encoding:</strong> Should be properly encoded for database storage</li>
     * </ul>
     *
     * @param content the JSONB content as a string
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the message timestamp.
     *
     * <p><strong>Timezone Considerations:</strong></p>
     * <ul>
     *   <li><strong>Consistency:</strong> Should use UTC+5 offset for consistency</li>
     *   <li><strong>Precision:</strong> OffsetDateTime provides nanosecond precision</li>
     *   <li><strong>Ordering:</strong> Used for chronological message ordering</li>
     * </ul>
     *
     * @param timestamp the message timestamp
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the entity creation timestamp.
     *
     * <p><strong>Note:</strong> This field is marked as non-updatable in the
     * database schema. This setter is primarily for framework use and should
     * not be called after initial entity creation.</p>
     *
     * @param createdAt the entity creation timestamp
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the additional metadata for this message.
     *
     * <p><strong>Metadata Handling:</strong></p>
     * <ul>
     *   <li><strong>Null Safe:</strong> Can be set to null if no metadata needed</li>
     *   <li><strong>Serialization:</strong> Automatically converted to JSONB via JsonMapConverter</li>
     *   <li><strong>Flexibility:</strong> Supports any Map&lt;String, Object&gt; structure</li>
     * </ul>
     *
     * @param metadata the metadata map (can be null)
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}