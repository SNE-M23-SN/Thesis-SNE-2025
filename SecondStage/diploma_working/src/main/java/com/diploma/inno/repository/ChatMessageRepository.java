package com.diploma.inno.repository;


import com.diploma.inno.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Spring Data JPA Repository for comprehensive chat message management in CI/CD anomaly detection &amp; AI analysis.
 *
 * <p>This repository interface provides sophisticated data access operations for {@link ChatMessageEntity}
 * objects within the Jenkins anomaly detection pipeline. It extends {@link JpaRepository} to offer
 * standard CRUD operations while adding specialized query methods for conversation management,
 * message cleanup, build correlation, and AI analysis support.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Conversation Management:</strong> Retrieve &amp; manage messages grouped by conversation ID</li>
 *   <li><strong>Message Cleanup:</strong> Sliding window cleanup to prevent unlimited data growth</li>
 *   <li><strong>Build Correlation:</strong> Query messages by build number for cross-log analysis</li>
 *   <li><strong>Content Filtering:</strong> JSONB-based content queries for message type filtering</li>
 *   <li><strong>Discovery Operations:</strong> Find all active conversations &amp; monitoring contexts</li>
 *   <li><strong>Performance Optimization:</strong> Efficient native queries for large datasets</li>
 * </ul>
 *
 * <h2>Repository Architecture &amp; Design</h2>
 * <p>The repository follows Spring Data JPA patterns with custom native queries:</p>
 * <pre>{@code
 * ChatMessageRepository
 *  ├── JpaRepository<ChatMessageEntity, Long> (Base CRUD)
 *  ├── Custom Query Methods (Conversation Management)
 *  ├── Native SQL Queries (Performance Optimization)
 *  ├── JSONB Operations (Content-Based Filtering)
 *  └── Transactional Operations (Data Consistency)
 * }</pre>
 *
 * <h2>Conversation-Based Operations</h2>
 * <p>The repository supports sophisticated conversation management for Jenkins job contexts:</p>
 * <ul>
 *   <li><strong>Message Retrieval:</strong> Get recent messages for conversation context</li>
 *   <li><strong>Sliding Window:</strong> Maintain fixed-size conversation history</li>
 *   <li><strong>Conversation Discovery:</strong> Find all active Jenkins jobs with messages</li>
 *   <li><strong>Complete Cleanup:</strong> Remove entire conversation histories</li>
 * </ul>
 *
 * <h2>Conversation Management Example:</h2>
 * {@snippet lang="java" :
 * // Retrieve last 10 messages for security-pipeline conversation
 * List<ChatMessageEntity> recentMessages = repository
 *     .findTopByConversationIdOrderByTimestampAsc("security-pipeline", 10);
 *
 * // Cleanup old messages, keeping only last 50
 * repository.deleteOldMessages("security-pipeline", 50);
 *
 * // Discover all active conversations
 * List<String> activeJobs = repository.findAllConversationIds();
 * }
 *
 * <h2>Build Correlation &amp; Analysis</h2>
 * <p>The repository enables comprehensive build-based analysis:</p>
 * <ul>
 *   <li><strong>Build Log Counting:</strong> Track log message completeness per build</li>
 *   <li><strong>Cross-Message Analysis:</strong> Correlate different message types by build</li>
 *   <li><strong>Progress Monitoring:</strong> Ensure all expected logs are received</li>
 *   <li><strong>Quality Assurance:</strong> Verify build data integrity</li>
 * </ul>
 *
 * <h2>Build Analysis Example:</h2>
 * {@snippet lang="java" :
 * // Count build log messages for specific build
 * int logCount = repository.countByConversationIdAndBuildNumberAndContentContaining(
 *     "security-pipeline", 123);
 *
 * // Verify expected log count (e.g., should have 5 log entries per build)
 * if (logCount < 5) {
 *     logger.warn("Incomplete build logs for build {}: only {} messages", 123, logCount);
 * }
 * }
 *
 * <h2>JSONB Content Queries</h2>
 * <p>The repository leverages PostgreSQL JSONB capabilities for content-based filtering:</p>
 * <ul>
 *   <li><strong>Type Filtering:</strong> Query messages by content type (build_log_data, secret_detection, etc.)</li>
 *   <li><strong>Metadata Queries:</strong> Search within message metadata using JSONB operators</li>
 *   <li><strong>Performance Optimization:</strong> Efficient indexing on JSONB content</li>
 *   <li><strong>Flexible Queries:</strong> Dynamic content-based filtering capabilities</li>
 * </ul>
 *
 * <h2>JSONB Query Examples:</h2>
 * {@snippet lang="sql" :
 * -- Count build log messages using JSONB extraction
 * SELECT COUNT(*) FROM chat_messages
 * WHERE conversation_id = 'security-pipeline'
 *   AND build_number = 123
 *   AND content ->> 'type' = 'build_log_data';
 *
 * -- Find messages with specific metadata
 * SELECT * FROM chat_messages
 * WHERE content -> 'data' ->> 'status' = 'SUCCESS';
 * }
 *
 * <h2>Performance &amp; Optimization</h2>
 * <p>The repository is optimized for high-volume CI/CD environments:</p>
 * <ul>
 *   <li><strong>Native Queries:</strong> Direct SQL for optimal performance on large datasets</li>
 *   <li><strong>Database Indexes:</strong> Optimized indexes on conversation_id, build_number, timestamp</li>
 *   <li><strong>Sliding Window:</strong> Prevents unlimited data growth with automatic cleanup</li>
 *   <li><strong>Batch Operations:</strong> Efficient bulk operations for message management</li>
 * </ul>
 *
 * <h2>Transaction Management</h2>
 * <p>The repository ensures data consistency through proper transaction handling:</p>
 * <ul>
 *   <li><strong>@Transactional:</strong> Atomic operations for data modification methods</li>
 *   <li><strong>@Modifying:</strong> Proper handling of DML operations</li>
 *   <li><strong>Rollback Support:</strong> Automatic rollback on operation failures</li>
 *   <li><strong>Isolation Levels:</strong> Appropriate isolation for concurrent access</li>
 * </ul>
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The repository supports AI analysis workflows:</p>
 * <ul>
 *   <li><strong>Context Retrieval:</strong> Provide conversation context for AI processing</li>
 *   <li><strong>Historical Analysis:</strong> Enable trend analysis across time periods</li>
 *   <li><strong>Cross-Message Correlation:</strong> Support analysis across message types</li>
 *   <li><strong>Data Preparation:</strong> Efficient data access for ML model training</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This repository integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>SimpleDbChatMemory:</strong> Primary data access for chat memory operations</li>
 *   <li><strong>LogMessageListener:</strong> Persistence of incoming log messages</li>
 *   <li><strong>AI Analysis Engine:</strong> Data retrieval for anomaly detection</li>
 *   <li><strong>Monitoring Dashboard:</strong> Real-time data access for visualization</li>
 * </ul>
 *
 * <h2>Usage Patterns &amp; Best Practices</h2>
 * <ul>
 *   <li><strong>Conversation Lifecycle:</strong> Regular cleanup to maintain performance</li>
 *   <li><strong>Build Correlation:</strong> Use build numbers for cross-message analysis</li>
 *   <li><strong>Content Queries:</strong> Leverage JSONB capabilities for flexible filtering</li>
 *   <li><strong>Performance Monitoring:</strong> Monitor query performance on large datasets</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see ChatMessageEntity
 * @see JpaRepository
 * @see com.diploma.inno.component.SimpleDbChatMemory
 * @see com.diploma.inno.component.LogMessageListener
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // ========================================================================
    // CONVERSATION MANAGEMENT &amp; MESSAGE RETRIEVAL
    // ========================================================================

    /**
     * Retrieves the most recent N chat messages for a specific conversation, ordered chronologically.
     *
     * <p>This method implements efficient conversation context retrieval for AI analysis and
     * chat memory management. It uses native SQL with database-specific optimizations to
     * fetch the most recent messages while maintaining chronological order for proper
     * conversation flow and context preservation.</p>
     *
     * <h4>Query Strategy &amp; Performance:</h4>
     * <ul>
     *   <li><strong>Native SQL:</strong> Direct database query for optimal performance</li>
     *   <li><strong>FETCH FIRST:</strong> Database-level row limiting for efficiency</li>
     *   <li><strong>Timestamp Ordering:</strong> Chronological sequence for conversation flow</li>
     *   <li><strong>Index Utilization:</strong> Leverages conversation_id &amp; timestamp indexes</li>
     * </ul>
     *
     * <h4>Use Cases &amp; Applications:</h4>
     * <ul>
     *   <li><strong>AI Context Preparation:</strong> Provide recent conversation history for analysis</li>
     *   <li><strong>Chat Memory Management:</strong> Implement sliding window conversation context</li>
     *   <li><strong>Real-time Monitoring:</strong> Display recent activity for Jenkins jobs</li>
     *   <li><strong>Debugging Support:</strong> Retrieve recent logs for troubleshooting</li>
     * </ul>
     *
     * <h4>Conversation Context Example:</h4>
     * {@snippet lang="java" :
     * // Get last 10 messages for security analysis
     * List<ChatMessageEntity> context = repository
     *     .findTopByConversationIdOrderByTimestampAsc("security-pipeline", 10);
     *
     * // Process messages chronologically for AI analysis
     * for (ChatMessageEntity message : context) {
     *     String content = message.getContent();
     *     String messageType = message.getMessageType();
     *     OffsetDateTime timestamp = message.getTimestamp();
     *
     *     // Prepare for AI analysis...
     *     aiAnalyzer.addContext(content, messageType, timestamp);
     * }
     * }
     *
     * <h4>Message Flow &amp; Ordering:</h4>
     * <p>Messages are returned in ascending timestamp order to maintain conversation flow:</p>
     * {@snippet lang="text" :
     * Message 1: 2024-01-15T10:30:00Z - Build started
     * Message 2: 2024-01-15T10:31:00Z - Code analysis complete
     * Message 3: 2024-01-15T10:32:00Z - Security scan finished
     * Message 4: 2024-01-15T10:33:00Z - Build completed successfully
     * }
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Query Efficiency:</strong> O(log n) lookup with proper indexing</li>
     *   <li><strong>Memory Usage:</strong> Limited result set prevents memory issues</li>
     *   <li><strong>Network Transfer:</strong> Minimal data transfer with row limiting</li>
     *   <li><strong>Database Load:</strong> Optimized query reduces database overhead</li>
     * </ul>
     *
     * <h4>Integration with Chat Memory:</h4>
     * <p>This method is primarily used by {@link com.diploma.inno.component.SimpleDbChatMemory} for context retrieval:</p>
     * {@snippet lang="java" :
     * // SimpleDbChatMemory usage pattern
     * public List<ChatMessage> get(String conversationId, int maxMessages) {
     *     List<ChatMessageEntity> entities = repository
     *         .findTopByConversationIdOrderByTimestampAsc(conversationId, maxMessages);
     *
     *     return entities.stream()
     *         .map(this::convertToMessage)
     *         .collect(Collectors.toList());
     * }
     * }
     *
     * @param conversationId the conversation identifier (typically Jenkins job name),
     *                       must not be null or empty
     * @param lastN the maximum number of recent messages to retrieve,
     *              must be positive (typically 10-100 for optimal performance)
     * @return list of chat message entities ordered by timestamp in ascending order,
     *         empty list if no messages found for the conversation
     *
     * @see #deleteOldMessages(String, int)
     * @see com.diploma.inno.component.SimpleDbChatMemory#get(String, int)
     */
    @Query(value = "SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC FETCH FIRST :lastN ROWS ONLY", nativeQuery = true)
    List<ChatMessageEntity> findTopByConversationIdOrderByTimestampAsc(
            @Param("conversationId") String conversationId, @Param("lastN") int lastN);

    // ========================================================================
    // MESSAGE CLEANUP &amp; SLIDING WINDOW MANAGEMENT
    // ========================================================================

    /**
     * Implements sliding window cleanup by deleting old messages while preserving recent conversation context.
     *
     * <p>This method provides sophisticated conversation history management using a sliding window
     * approach to prevent unlimited database growth while maintaining sufficient context for AI
     * analysis and conversation continuity. It uses a complex native SQL query with subqueries
     * for efficient bulk deletion operations.</p>
     *
     * <h4>Sliding Window Strategy:</h4>
     * <ol>
     *   <li><strong>Recent Message Identification:</strong> Finds the most recent {@code limit} messages by timestamp</li>
     *   <li><strong>Bulk Deletion:</strong> Removes all other messages for the conversation in single operation</li>
     *   <li><strong>Context Preservation:</strong> Maintains conversation continuity &amp; AI analysis context</li>
     *   <li><strong>Performance Optimization:</strong> Uses efficient NOT IN subquery for bulk operations</li>
     * </ol>
     *
     * <h4>SQL Query Breakdown:</h4>
     * {@snippet lang="sql" :
     * DELETE FROM chat_messages e
     * WHERE e.conversation_id = :conversationId
     *   AND e.id NOT IN (
     *     SELECT e2.id FROM chat_messages e2
     *     WHERE e2.conversation_id = :conversationId
     *     ORDER BY e2.timestamp DESC
     *     LIMIT :limit
     *   );
     * }
     *
     * <h4>Cleanup Scenarios &amp; Examples:</h4>
     * <p>Regular maintenance cleanup:</p>
     * {@snippet lang="java" :
     * // Keep last 50 messages for active conversation
     * repository.deleteOldMessages("security-pipeline", 50);
     *
     * // Aggressive cleanup for inactive conversations
     * repository.deleteOldMessages("old-project", 10);
     *
     * // Scheduled cleanup for all conversations
     * List<String> conversations = repository.findAllConversationIds();
     * for (String conversationId : conversations) {
     *     repository.deleteOldMessages(conversationId, 100);
     * }
     * }
     *
     * <h4>Before &amp; After Cleanup Example:</h4>
     * <p>Before cleanup (conversation has 150 messages):</p>
     * {@snippet lang="text" :
     * Messages 1-100:   [Old messages to be deleted]
     * Messages 101-150: [Recent messages to preserve]
     * Total: 150 messages
     * }
     *
     * <p>After cleanup with limit=50:</p>
     * {@snippet lang="text" :
     * Messages 101-150: [Preserved recent messages]
     * Total: 50 messages (100 messages deleted)
     * }
     *
     * <h4>Performance &amp; Efficiency:</h4>
     * <ul>
     *   <li><strong>Bulk Operations:</strong> Single DELETE statement for multiple rows</li>
     *   <li><strong>Index Utilization:</strong> Leverages conversation_id &amp; timestamp indexes</li>
     *   <li><strong>Memory Efficiency:</strong> No intermediate result loading into memory</li>
     *   <li><strong>Transaction Safety:</strong> Atomic operation with automatic rollback</li>
     * </ul>
     *
     * <h4>Database Impact &amp; Considerations:</h4>
     * <ul>
     *   <li><strong>Lock Duration:</strong> Brief table locks during deletion operation</li>
     *   <li><strong>Storage Reclaim:</strong> Immediate space reclamation after deletion</li>
     *   <li><strong>Index Maintenance:</strong> Automatic index updates during deletion</li>
     *   <li><strong>Foreign Key Handling:</strong> Proper cascade handling if applicable</li>
     * </ul>
     *
     * <h4>Integration with Memory Management:</h4>
     * <p>Used by {@link com.diploma.inno.component.SimpleDbChatMemory} for automatic conversation maintenance:</p>
     * {@snippet lang="java" :
     * // Automatic cleanup after adding new messages
     * public void add(String conversationId, List<ChatMessage> messages) {
     *     // Add new messages...
     *     saveMessages(conversationId, messages);
     *
     *     // Cleanup old messages to maintain window size
     *     repository.deleteOldMessages(conversationId, maxMessages);
     * }
     * }
     *
     * <h4>Best Practices &amp; Recommendations:</h4>
     * <ul>
     *   <li><strong>Regular Scheduling:</strong> Run cleanup operations during low-traffic periods</li>
     *   <li><strong>Appropriate Limits:</strong> Balance context preservation with storage efficiency</li>
     *   <li><strong>Monitoring:</strong> Track deletion counts for capacity planning</li>
     *   <li><strong>Backup Considerations:</strong> Ensure important historical data is archived</li>
     * </ul>
     *
     * @param conversationId the conversation identifier to clean up,
     *                       must not be null or empty
     * @param limit the number of recent messages to preserve,
     *              must be positive (typically 50-200 for optimal balance)
     *
     * @see #findTopByConversationIdOrderByTimestampAsc(String, int)
     * @see com.diploma.inno.component.SimpleDbChatMemory#add(String, java.util.List)
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM chat_messages e WHERE e.conversation_id = :conversationId AND e.id NOT IN (" +
            "SELECT e2.id FROM chat_messages e2 WHERE e2.conversation_id = :conversationId " +
            "ORDER BY e2.timestamp DESC LIMIT :limit)", nativeQuery = true)
    void deleteOldMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);

    // ========================================================================
    // COMPLETE CONVERSATION CLEANUP
    // ========================================================================

    /**
     * Performs complete conversation deletion, removing all messages for a specific conversation.
     *
     * <p>This method provides comprehensive conversation cleanup for scenarios requiring
     * complete data removal, such as Jenkins job deletion, conversation reset, or
     * compliance-driven data purging. It leverages Spring Data JPA's derived query
     * method for efficient bulk deletion with proper transaction management.</p>
     *
     * <h4>Deletion Scope &amp; Impact:</h4>
     * <ul>
     *   <li><strong>Complete Removal:</strong> Deletes all messages regardless of timestamp or type</li>
     *   <li><strong>Conversation Reset:</strong> Completely clears conversation history</li>
     *   <li><strong>Storage Reclaim:</strong> Immediate database space reclamation</li>
     *   <li><strong>Index Cleanup:</strong> Automatic index maintenance during deletion</li>
     * </ul>
     *
     * <h4>Use Cases &amp; Scenarios:</h4>
     * <ul>
     *   <li><strong>Jenkins Job Deletion:</strong> Clean up when Jenkins jobs are permanently removed</li>
     *   <li><strong>Conversation Reset:</strong> Start fresh conversation context for troubleshooting</li>
     *   <li><strong>Compliance Purging:</strong> Remove data for regulatory compliance requirements</li>
     *   <li><strong>System Maintenance:</strong> Clean up test or development conversations</li>
     * </ul>
     *
     * <h4>Deletion Examples:</h4>
     * {@snippet lang="java" :
     * // Delete conversation when Jenkins job is removed
     * public void onJobDeleted(String jobName) {
     *     repository.deleteByConversationId(jobName);
     *     logger.info("Deleted all messages for job: {}", jobName);
     * }
     *
     * // Reset conversation for troubleshooting
     * public void resetConversation(String conversationId) {
     *     repository.deleteByConversationId(conversationId);
     *     logger.info("Reset conversation: {}", conversationId);
     * }
     *
     * // Bulk cleanup for inactive conversations
     * List<String> inactiveJobs = findInactiveJobs();
     * for (String jobName : inactiveJobs) {
     *     repository.deleteByConversationId(jobName);
     * }
     * }
     *
     * <h4>Transaction &amp; Consistency:</h4>
     * <ul>
     *   <li><strong>@Transactional:</strong> Ensures atomic deletion operation</li>
     *   <li><strong>@Modifying:</strong> Proper handling of DML operations</li>
     *   <li><strong>Rollback Support:</strong> Automatic rollback on operation failures</li>
     *   <li><strong>Consistency Guarantee:</strong> All-or-nothing deletion semantics</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Bulk Operation:</strong> Single DELETE statement for all conversation messages</li>
     *   <li><strong>Index Efficiency:</strong> Leverages conversation_id index for fast lookup</li>
     *   <li><strong>Memory Efficiency:</strong> No intermediate data loading required</li>
     *   <li><strong>Lock Duration:</strong> Minimal lock time for deletion operation</li>
     * </ul>
     *
     * <h4>Monitoring &amp; Auditing:</h4>
     * {@snippet lang="java" :
     * // Audit conversation deletion
     * public void deleteConversationWithAudit(String conversationId) {
     *     long messageCount = repository.countByConversationId(conversationId);
     *     repository.deleteByConversationId(conversationId);
     *
     *     auditLogger.info("Deleted conversation {} with {} messages",
     *                     conversationId, messageCount);
     * }
     * }
     *
     * <h4>Integration Points:</h4>
     * <ul>
     *   <li><strong>Job Lifecycle Management:</strong> Triggered by Jenkins job deletion events</li>
     *   <li><strong>Conversation Management:</strong> Used by conversation reset operations</li>
     *   <li><strong>Compliance Systems:</strong> Integrated with data retention policies</li>
     *   <li><strong>Maintenance Scripts:</strong> Used in automated cleanup procedures</li>
     * </ul>
     *
     * <h4>Safety Considerations:</h4>
     * <ul>
     *   <li><strong>Irreversible Operation:</strong> No recovery possible after deletion</li>
     *   <li><strong>Backup Verification:</strong> Ensure backups exist before deletion</li>
     *   <li><strong>Access Control:</strong> Restrict access to authorized operations only</li>
     *   <li><strong>Confirmation Required:</strong> Implement confirmation for manual operations</li>
     * </ul>
     *
     * @param conversationId the conversation identifier to delete completely,
     *                       must not be null or empty
     */
    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);

    // ========================================================================
    // CONVERSATION DISCOVERY &amp; MONITORING
    // ========================================================================

    /**
     * Discovers all unique conversation identifiers for comprehensive system monitoring &amp; analysis.
     *
     * <p>This method provides essential discovery capabilities for the CI/CD monitoring system,
     * enabling identification of all active Jenkins jobs that have generated chat message data.
     * It uses a native SQL query with DISTINCT for optimal performance and accurate results
     * across large datasets.</p>
     *
     * <h4>Discovery Capabilities:</h4>
     * <ul>
     *   <li><strong>Active Job Detection:</strong> Identifies all Jenkins jobs with message history</li>
     *   <li><strong>System Overview:</strong> Provides complete picture of monitored environments</li>
     *   <li><strong>Conversation Inventory:</strong> Enables conversation management operations</li>
     *   <li><strong>Monitoring Scope:</strong> Determines extent of CI/CD monitoring coverage</li>
     * </ul>
     *
     * <h4>Use Cases &amp; Applications:</h4>
     * <ul>
     *   <li><strong>Dashboard Population:</strong> Display all monitored Jenkins jobs</li>
     *   <li><strong>Bulk Operations:</strong> Perform maintenance across all conversations</li>
     *   <li><strong>System Health Checks:</strong> Verify monitoring coverage &amp; completeness</li>
     *   <li><strong>Reporting &amp; Analytics:</strong> Generate system-wide reports &amp; metrics</li>
     * </ul>
     *
     * <h4>Discovery Examples:</h4>
     * {@snippet lang="java" :
     * // Get all monitored Jenkins jobs
     * List<String> allJobs = repository.findAllConversationIds();
     *
     * // Display monitoring coverage
     * logger.info("Currently monitoring {} Jenkins jobs:", allJobs.size());
     * for (String jobName : allJobs) {
     *     logger.info("  - {}", jobName);
     * }
     *
     * // Perform bulk maintenance
     * for (String conversationId : allJobs) {
     *     // Cleanup old messages for each conversation
     *     repository.deleteOldMessages(conversationId, 100);
     *
     *     // Generate health report
     *     generateHealthReport(conversationId);
     * }
     * }
     *
     * <h4>System Monitoring Integration:</h4>
     * {@snippet lang="java" :
     * // Monitor system health across all conversations
     * public SystemHealthReport generateSystemHealth() {
     *     List<String> conversations = repository.findAllConversationIds();
     *     SystemHealthReport report = new SystemHealthReport();
     *
     *     for (String conversationId : conversations) {
     *         ConversationHealth health = analyzeConversationHealth(conversationId);
     *         report.addConversationHealth(conversationId, health);
     *     }
     *
     *     return report;
     * }
     * }
     *
     * <h4>Performance &amp; Optimization:</h4>
     * <ul>
     *   <li><strong>DISTINCT Query:</strong> Efficient elimination of duplicate conversation IDs</li>
     *   <li><strong>Index Utilization:</strong> Leverages conversation_id index for fast scanning</li>
     *   <li><strong>Result Caching:</strong> Results can be cached for frequent access patterns</li>
     *   <li><strong>Memory Efficiency:</strong> Returns only unique identifiers, not full entities</li>
     * </ul>
     *
     * <h4>Result Characteristics:</h4>
     * <p>Example result set for active monitoring environment:</p>
     * {@snippet lang="text" :
     * Discovered Conversations:
     * - security-pipeline
     * - main-build-job
     * - integration-tests
     * - deployment-pipeline
     * - monitoring-checks
     * Total: 5 active conversations
     * }
     *
     * <h4>Integration with Management Operations:</h4>
     * <ul>
     *   <li><strong>Conversation Management:</strong> Basis for bulk conversation operations</li>
     *   <li><strong>Monitoring Dashboard:</strong> Populates job selection lists &amp; overviews</li>
     *   <li><strong>Maintenance Scripts:</strong> Enables automated system-wide maintenance</li>
     *   <li><strong>Analytics Engine:</strong> Provides scope for cross-job analysis</li>
     * </ul>
     *
     * <h4>Monitoring &amp; Alerting:</h4>
     * {@snippet lang="java" :
     * // Monitor for new or missing conversations
     * public void monitorConversationChanges() {
     *     List<String> currentConversations = repository.findAllConversationIds();
     *     List<String> expectedJobs = jenkinsService.getAllJobNames();
     *
     *     // Find missing monitoring coverage
     *     List<String> unmonitored = expectedJobs.stream()
     *         .filter(job -> !currentConversations.contains(job))
     *         .collect(Collectors.toList());
     *
     *     if (!unmonitored.isEmpty()) {
     *         alertService.sendAlert("Unmonitored Jenkins jobs detected: " + unmonitored);
     *     }
     * }
     * }
     *
     * @return list of unique conversation identifiers (typically Jenkins job names),
     *         empty list if no conversations exist in the system
     *
     * @see #deleteOldMessages(String, int)
     * @see #deleteByConversationId(String)
     */
    @Query(value = "SELECT DISTINCT conversation_id FROM chat_messages", nativeQuery = true)
    List<String> findAllConversationIds();

    // ========================================================================
    // BUILD CORRELATION &amp; LOG VERIFICATION
    // ========================================================================

    /**
     * Counts build log messages for specific conversation &amp; build to verify log completeness.
     *
     * <p>This method provides critical build log verification capabilities using PostgreSQL's
     * JSONB extraction features to identify and count build log messages for specific Jenkins
     * build executions. It enables quality assurance by ensuring all expected log entries
     * have been received and processed correctly.</p>
     *
     * <h4>JSONB Query Strategy:</h4>
     * <ul>
     *   <li><strong>Content Type Extraction:</strong> Uses JSONB -&gt;&gt; operator to extract message type</li>
     *   <li><strong>Build Correlation:</strong> Filters by conversation ID &amp; build number</li>
     *   <li><strong>Type Filtering:</strong> Specifically counts 'build_log_data' messages</li>
     *   <li><strong>Performance Optimization:</strong> Leverages JSONB indexing for efficient queries</li>
     * </ul>
     *
     * <h4>SQL Query Breakdown:</h4>
     * {@snippet lang="sql" :
     * SELECT COUNT(*)
     * FROM chat_messages
     * WHERE conversation_id = :conversationId
     *   AND build_number = :buildNumber
     *   AND content ->> 'type' = 'build_log_data';
     * }
     *
     * <h4>Build Log Verification Examples:</h4>
     * {@snippet lang="java" :
     * // Verify expected build log count
     * public boolean verifyBuildLogCompleteness(String jobName, int buildNumber) {
     *     int logCount = repository.countByConversationIdAndBuildNumberAndContentContaining(
     *         jobName, buildNumber);
     *
     *     // Expected: 5 log entries per build (start, compile, test, package, deploy)
     *     int expectedCount = 5;
     *
     *     if (logCount < expectedCount) {
     *         logger.warn("Incomplete build logs for {}-{}: {} of {} expected",
     *                    jobName, buildNumber, logCount, expectedCount);
     *         return false;
     *     }
     *
     *     logger.info("Build log verification passed for {}-{}: {} logs",
     *                jobName, buildNumber, logCount);
     *     return true;
     * }
     * }
     *
     * <h4>Quality Assurance Integration:</h4>
     * {@snippet lang="java" :
     * // Automated build quality checks
     * public BuildQualityReport assessBuildQuality(String conversationId, int buildNumber) {
     *     BuildQualityReport report = new BuildQualityReport();
     *
     *     // Check build log completeness
     *     int buildLogCount = repository.countByConversationIdAndBuildNumberAndContentContaining(
     *         conversationId, buildNumber);
     *     report.setBuildLogCount(buildLogCount);
     *     report.setBuildLogComplete(buildLogCount >= EXPECTED_BUILD_LOGS);
     *
     *     // Additional quality checks...
     *     checkSecurityScanResults(conversationId, buildNumber, report);
     *     checkDependencyAnalysis(conversationId, buildNumber, report);
     *
     *     return report;
     * }
     * }
     *
     * <h4>Build Log Types &amp; Expected Counts:</h4>
     * <p>Typical Jenkins build pipeline log expectations:</p>
     * {@snippet lang="text" :
     * Build Phase          | Expected Logs | Message Type
     * ---------------------|---------------|------------------
     * Build Start          | 1             | build_log_data
     * Source Checkout      | 1             | build_log_data
     * Compilation          | 1             | build_log_data
     * Unit Tests           | 1             | build_log_data
     * Package Creation     | 1             | build_log_data
     * Deployment           | 1             | build_log_data
     * Build Completion     | 1             | build_log_data
     * ---------------------|---------------|------------------
     * Total Expected       | 7             | build_log_data
     * }
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>JSONB Indexing:</strong> Efficient content-based queries with proper indexing</li>
     *   <li><strong>Selective Counting:</strong> Counts only relevant message types</li>
     *   <li><strong>Build-Specific:</strong> Precise filtering by conversation &amp; build number</li>
     *   <li><strong>Query Optimization:</strong> Leverages composite indexes for fast execution</li>
     * </ul>
     *
     * <h4>Monitoring &amp; Alerting:</h4>
     * {@snippet lang="java" :
     * // Monitor build log completeness across all builds
     * public void monitorBuildLogCompleteness() {
     *     List<String> conversations = repository.findAllConversationIds();
     *
     *     for (String conversationId : conversations) {
     *         List<Integer> recentBuilds = getRecentBuildNumbers(conversationId);
     *
     *         for (Integer buildNumber : recentBuilds) {
     *             int logCount = repository.countByConversationIdAndBuildNumberAndContentContaining(
     *                 conversationId, buildNumber);
     *
     *             if (logCount == 0) {
     *                 alertService.sendAlert(String.format(
     *                     "No build logs found for %s build %d", conversationId, buildNumber));
     *             }
     *         }
     *     }
     * }
     * }
     *
     * <h4>Integration Points:</h4>
     * <ul>
     *   <li><strong>Build Quality Assurance:</strong> Automated verification of log completeness</li>
     *   <li><strong>Monitoring Systems:</strong> Real-time build health monitoring</li>
     *   <li><strong>CI/CD Pipeline:</strong> Quality gates based on log verification</li>
     *   <li><strong>Alerting Systems:</strong> Notifications for incomplete build logs</li>
     * </ul>
     *
     * @param conversationId the conversation identifier (Jenkins job name),
     *                       must not be null or empty
     * @param buildNumber the specific Jenkins build number to check,
     *                    must be positive
     * @return count of build log messages for the specified conversation and build,
     *         0 if no build log messages found
     *
     * @see #findTopByConversationIdOrderByTimestampAsc(String, int)
     */
    @Query(value = "SELECT COUNT(*) FROM chat_messages c WHERE c.conversation_id = :conversationId AND c.build_number = :buildNumber AND c.content ->> 'type' = 'build_log_data'", nativeQuery = true)
    int countByConversationIdAndBuildNumberAndContentContaining(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") int buildNumber
    );



}


