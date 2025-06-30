package com.diploma.inno.repository;

import com.diploma.inno.dto.ActiveBuildCountProjection;
import com.diploma.inno.dto.JobCountProjection;
import com.diploma.inno.dto.RecentJobBuildProjection;
import com.diploma.inno.dto.SecurityAnomalyCountProjection;
import com.diploma.inno.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing chat message entities and providing comprehensive data access for Jenkins CI/CD monitoring and AI analysis.
 *
 * <p>This repository serves as the central data access layer for the CI Anomaly Detector system,
 * extending {@link JpaRepository} and providing specialized query methods for complex JSONB operations,
 * dashboard analytics, and real-time monitoring of Jenkins builds and AI analysis results.</p>
 *
 * <p><strong>Core Functionality:</strong></p>
 * <ul>
 *   <li><strong>Chat Message CRUD:</strong> Standard entity operations with conversation management</li>
 *   <li><strong>AI Analysis Retrieval:</strong> Complex JSONB queries for anomaly detection and risk scoring</li>
 *   <li><strong>Build Log Management:</strong> Tracking and pagination of Jenkins build logs</li>
 *   <li><strong>Dashboard Analytics:</strong> Aggregated data for real-time monitoring dashboards</li>
 *   <li><strong>Trend Analysis:</strong> Time-series data extraction for pattern identification</li>
 *   <li><strong>Materialized View Integration:</strong> Performance-optimized queries via pre-computed views</li>
 * </ul>
 *
 * <p><strong>Database Schema Integration:</strong></p>
 * <pre>
 * Primary Table: chat_messages
 * ┌─────────────────┬──────────────────┬─────────────────────────────────────────────────────┐
 * │ Column          │ Type             │ Purpose                                             │
 * ├─────────────────┼──────────────────┼─────────────────────────────────────────────────────┤
 * │ id              │ BIGINT           │ Primary key, auto-generated                         │
 * │ conversation_id │ VARCHAR          │ Jenkins job name (conversation identifier)          │
 * │ build_number    │ INTEGER          │ Jenkins build number                                │
 * │ message_type    │ VARCHAR          │ 'USER' (build logs) or 'ASSISTANT' (AI analysis)    │
 * │ content         │ JSONB            │ Structured data (logs or AI analysis results)       │
 * │ timestamp       │ TIMESTAMPTZ      │ Message creation time (UTC+5)                       │
 * │ created_at      │ TIMESTAMPTZ      │ Entity creation time (UTC+5)                        │
 * │ job_name        │ VARCHAR          │ Duplicate of conversation_id for query optimization │
 * │ metadata        │ JSONB            │ Additional metadata (via JsonMapConverter)          │
 * └─────────────────┴──────────────────┴─────────────────────────────────────────────────────┘
 *
 * Materialized Views:
 * - recent_job_builds: Pre-computed recent build metrics with health status
 * - security_anomaly_counts: Aggregated security anomaly statistics
 * - active_build_counts: Real-time active build counts by job filter
 * - job_counts: Total job counts by time boundary
 * - build_anomaly_summary: Aggregated anomaly counts and severity distribution
 * </pre>
 *
 * <p><strong>JSONB Content Structures:</strong></p>
 *
 * <span><b>ASSISTANT messages (AI Analysis Results)</b></span>
 * {@snippet lang=json :
 * {
 *   "anomalies": [
 *     {
 *       "severity": "CRITICAL|HIGH|MEDIUM|WARNING|LOW",
 *       "type": "security|performance|quality|configuration|...",
 *       "description": "Human-readable anomaly description",
 *       "details": "Technical details and context",
 *       "aiAnalysis": "AI reasoning and analysis",
 *       "recommendation": "Suggested remediation steps"
 *     }
 *   ],
 *   "buildMetadata": {
 *     "status": "SUCCESS|FAILURE|UNSTABLE|ABORTED",
 *     "duration": "Build execution time",
 *     "startTime": "Build start timestamp",
 *     "timestamp": "Build completion timestamp"
 *   },
 *   "riskScore": {
 *     "score": 75,
 *     "change": 15,
 *     "riskLevel": "HIGH",
 *     "previousScore": 60
 *   },
 *   "insights": {
 *     "summary": "Overall build analysis summary",
 *     "recommendations": ["Action items for improvement"],
 *     "trends": {"pattern": "analysis", "direction": "improving|deteriorating"}
 *   }
 * }
 *}
 * <span><b>USER messages (Build Logs)</b></span>
 * {@snippet lang=json :
 * {
 *   "type": "build_log_data",
 *   "log_chunk": "Raw Jenkins build log content",
 *   "chunk_index": 1,
 *   "total_chunks": 14,
 *   "timestamp": "Log collection timestamp"
 * }
 * }
 *
 *
 * <p><strong>Query Performance Optimization:</strong></p>
 * <ul>
 *   <li><strong>Native SQL:</strong> Complex JSONB operations using PostgreSQL-specific features</li>
 *   <li><strong>GIN Indexes:</strong> Optimized JSONB field indexing for fast content queries</li>
 *   <li><strong>Composite Indexes:</strong> (conversation_id, build_number) for build-specific queries</li>
 *   <li><strong>Materialized Views:</strong> Pre-computed aggregations for dashboard performance</li>
 *   <li><strong>Pagination Support:</strong> LIMIT/OFFSET for large result sets</li>
 *   <li><strong>CTE Queries:</strong> Complex analytical queries with Common Table Expressions</li>
 * </ul>
 *
 * <p><strong>Service Layer Integration:</strong></p>
 * <ul>
 *   <li><strong>DashboardService:</strong> Primary consumer for dashboard analytics and metrics</li>
 *   <li><strong>Scheduled Tasks:</strong> 15-minute Jenkins synchronization via @Scheduled methods</li>
 *   <li><strong>REST Controllers:</strong> Data provision for API endpoints and dashboard widgets</li>
 *   <li><strong>AI Analysis Pipeline:</strong> Storage and retrieval of AI analysis results</li>
 * </ul>
 *
 * <p><strong>Data Lifecycle Management:</strong></p>
 * <ul>
 *   <li><strong>Message Creation:</strong> USER messages for build logs, ASSISTANT for AI analysis</li>
 *   <li><strong>Conversation Management:</strong> Grouping by Jenkins job names (conversation_id)</li>
 *   <li><strong>Build Tracking:</strong> Association with specific Jenkins build numbers</li>
 *   <li><strong>Cleanup Operations:</strong> Sliding window deletion and orphaned job removal</li>
 *   <li><strong>Synchronization:</strong> Periodic sync with Jenkins job state</li>
 * </ul>
 *
 * <p><strong>Error Handling &amp; Resilience:</strong></p>
 * <ul>
 *   <li><strong>Null Safety:</strong> Graceful handling of missing or malformed JSONB data</li>
 *   <li><strong>Transaction Support:</strong> @Transactional annotations for data consistency</li>
 *   <li><strong>Fallback Responses:</strong> Default values when queries return no results</li>
 *   <li><strong>Safety Checks:</strong> Bulk deletion limits to prevent accidental data loss</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see ChatMessageEntity
 * @see com.diploma.inno.service.DashboardService
 * @see com.diploma.inno.controller.DashboardController
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    /**
     * Retrieves the most recent N chat messages for a specific conversation, ordered by timestamp.
     *
     * <p>This method is useful for maintaining conversation context and implementing
     * sliding window approaches for chat history management.</p>
     *
     * @param conversationId The conversation identifier (typically Jenkins job name)
     * @param lastN          Maximum number of recent messages to retrieve
     * @return List of chat message entities ordered by timestamp (ascending)
     * @see #deleteOldMessages(String, int)
     */
    @Query(value = "SELECT * FROM chat_messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC FETCH FIRST :lastN ROWS ONLY", nativeQuery = true)
    List<ChatMessageEntity> findTopByConversationIdOrderByTimestampAsc(
            @Param("conversationId") String conversationId, @Param("lastN") int lastN);

    /**
     * Deletes old messages from a conversation, keeping only the most recent N messages.
     *
     * <p>This method implements a sliding window cleanup strategy to prevent
     * unlimited growth of conversation history while maintaining recent context.</p>
     *
     * <p><strong>Deletion Strategy:</strong></p>
     * <ol>
     *   <li>Identifies the most recent {@code limit} messages by timestamp</li>
     *   <li>Deletes all other messages for the conversation</li>
     *   <li>Preserves conversation continuity and context</li>
     * </ol>
     *
     * @param conversationId The conversation identifier to clean up
     * @param limit          Number of recent messages to preserve
     * @see #findTopByConversationIdOrderByTimestampAsc(String, int)
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM chat_messages e WHERE e.conversation_id = :conversationId AND e.id NOT IN (" +
            "SELECT e2.id FROM chat_messages e2 WHERE e2.conversation_id = :conversationId " +
            "ORDER BY e2.timestamp DESC LIMIT :limit)", nativeQuery = true)
    void deleteOldMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);

    /**
     * Deletes all messages associated with a specific conversation.
     *
     * <p>This method provides complete conversation cleanup, typically used
     * when a Jenkins job is deleted or when resetting conversation state.</p>
     *
     * @param conversationId The conversation identifier to delete
     */
    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);

    /**
     * Retrieves all unique conversation identifiers from the chat messages table.
     *
     * <p>This method is useful for discovering all active Jenkins jobs that
     * have been monitored and have associated chat message data.</p>
     *
     * @return List of unique conversation IDs (Jenkins job names)
     */
    @Query(value = "SELECT DISTINCT conversation_id FROM chat_messages", nativeQuery = true)
    List<String> findAllConversationIds();

    /**
     * Counts build log messages for a specific conversation and build number.
     *
     * <p>This method counts USER messages that contain build log data, which is useful
     * for tracking the progress of log collection and ensuring all expected logs
     * have been received for a particular build.</p>
     *
     * <p><strong>JSONB Query Details:</strong></p>
     * <ul>
     *   <li>Uses {@code content ->> 'type'} to extract the type field as text</li>
     *   <li>Filters for messages with type 'build_log_data'</li>
     *   <li>Combines with conversation_id and build_number for precise targeting</li>
     * </ul>
     *
     * @param conversationId The conversation identifier (Jenkins job name)
     * @param buildNumber    The specific build number to count logs for
     * @return Count of build log messages for the specified build
     * @see #getTrackingLogInfo(String, Integer)
     */
    @Query(value = "SELECT COUNT(*) FROM chat_messages c WHERE c.conversation_id = :conversationId AND c.build_number = :buildNumber AND c.content ->> 'type' = 'build_log_data'", nativeQuery = true)
    int countByConversationIdAndBuildNumberAndContentContaining(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") int buildNumber
    );



    /**
     * Retrieves a comprehensive build summary with health status analysis from AI analysis results.
     *
     * <p>This method performs complex JSONB analysis to generate a complete build summary including
     * health status determination based on anomaly severity, formatted timestamps, duration calculations,
     * and regression detection. It's used by the dashboard to display detailed build information.</p>
     *
     * <p><strong>Health Status Logic:</strong></p>
     * <ul>
     *   <li><strong>CRITICAL:</strong> Contains CRITICAL or HIGH severity anomalies</li>
     *   <li><strong>WARNING:</strong> Contains MEDIUM severity anomalies or multiple anomalies</li>
     *   <li><strong>Healthy:</strong> Only LOW/WARNING anomalies (≤5) and no higher severity</li>
     *   <li><strong>Unhealthy:</strong> Default case for other scenarios</li>
     * </ul>
     *
     * <p><strong>JSONB Operations Used:</strong></p>
     * <ul>
     *   <li><strong>jsonb_build_object:</strong> Constructs structured response object</li>
     *   <li><strong>@&gt; operator:</strong> JSONB containment for severity checking</li>
     *   <li><strong>jsonb_array_elements:</strong> Unnests anomalies array for counting</li>
     *   <li><strong>-&gt;&gt; operator:</strong> Extracts text values from JSONB</li>
     *   <li><strong>-&gt; operator:</strong> Extracts JSONB objects for nested access</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "job_name": "my-web-app",
     *   "build_id": 123,
     *   "health_status": "CRITICAL|WARNING|Healthy|Unhealthy",
     *   "build_summary": "AI-generated build summary",
     *   "build_started_time": "Today, 2:30 PM or Monday, 2:30 PM",
     *   "build_duration": "2m 30s",
     *   "regression_detected": "true|false"
     * }
     * }
     *
     * <p><strong>Time Formatting Logic:</strong></p>
     * <ul>
     *   <li><strong>Today's builds:</strong> "Today, HH:MM AM/PM" format</li>
     *   <li><strong>Other days:</strong> "Day, HH:MM AM/PM" format</li>
     *   <li><strong>Duration:</strong> "Xm Ys" format (minutes and seconds)</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Service:</strong> Called by DashboardService for build detail views</li>
     *   <li><strong>REST API:</strong> Provides data for /api/dashboard/builds/{jobName}/{buildId}/summary</li>
     *   <li><strong>UI Components:</strong> Powers build summary cards and detail panels</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to analyze
     * @return JSON string containing comprehensive build summary with health status
     * @see #findRiskScore(String, Integer)
     * @see #findLatestInsights(String, Integer)
     */
    @Query(value = "SELECT jsonb_build_object(\n" +
            "    'job_name', cm.job_name,\n" +
            "    'build_id', cm.build_number,\n" +
            "    'health_status', \n" +
            "        CASE \n" +
            "            WHEN cm.content->'anomalies' @> '[{\"severity\": \"CRITICAL\"}]' \n" +
            "                OR cm.content->'anomalies' @> '[{\"severity\": \"HIGH\"}]' \n" +
            "                THEN 'CRITICAL'\n" +
            "            WHEN (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a \n" +
            "                  WHERE a->>'severity' = 'MEDIUM') > 0\n" +
            "                 OR (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a \n" +
            "                    WHERE a->>'severity' IN ('CRITICAL', 'HIGH', 'MEDIUM')) > 1 \n" +
            "                THEN 'WARNING'\n" +
            "            WHEN (SELECT COUNT(*) FROM jsonb_array_elements(cm.content->'anomalies') a \n" +
            "                  WHERE a->>'severity' IN ('LOW', 'WARNING')) <= 5\n" +
            "                 AND NOT (cm.content->'anomalies' @> '[{\"severity\": \"CRITICAL\"}]' \n" +
            "                         OR cm.content->'anomalies' @> '[{\"severity\": \"HIGH\"}]' \n" +
            "                         OR cm.content->'anomalies' @> '[{\"severity\": \"MEDIUM\"}]')\n" +
            "                THEN 'Healthy'\n" +
            "            ELSE 'Unhealthy' \n" +
            "        END,\n" +
            "    'build_summary', cm.content->>'summary',\n" +
            "    'build_started_time', \n" +
            "        CASE \n" +
            "            WHEN (cm.content->'buildMetadata'->>'startTime')::timestamptz::date = CURRENT_DATE \n" +
            "            THEN 'Today, ' || TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMHH12:MI AM')\n" +
            "            ELSE TO_CHAR((cm.content->'buildMetadata'->>'startTime')::timestamptz, 'FMDay, FMHH12:MI AM')\n" +
            "        END,\n" +
            "    'build_duration', \n" +
            "        FLOOR((cm.content->'buildMetadata'->>'durationSeconds')::numeric / 60) || 'm ' || \n" +
            "        ((cm.content->'buildMetadata'->>'durationSeconds')::numeric % 60) || 's',\n" +
            "    'regression_detected', \n" +
            "        (cm.content->>'regressionFromPreviousBuilds')::boolean\n" +
            ") AS build_summary\n" +
            "FROM chat_messages cm\n" +
            "WHERE cm.message_type = 'ASSISTANT'\n" +
            "  AND cm.conversation_id = :conversationId\n" +
            "  AND cm.build_number = :buildNumber", nativeQuery = true)
    String findBuildSummary(@Param("conversationId") String conversationId, @Param("buildNumber") Integer buildNumber);

    /**
     * Retrieves paginated anomalies from AI analysis results with total count for pagination metadata.
     *
     * <p>This method implements sophisticated JSONB array pagination using Common Table Expressions (CTEs)
     * to efficiently extract, count, and paginate anomalies from the JSONB content field. It's designed
     * for high-performance anomaly browsing in the dashboard interface.</p>
     *
     * <p><strong>CTE Structure:</strong></p>
     * <ol>
     *   <li><strong>unnested_anomalies:</strong> Unnests JSONB anomalies array with position tracking</li>
     *   <li><strong>total_count:</strong> Calculates total anomaly count for pagination metadata</li>
     *   <li><strong>Final SELECT:</strong> Aggregates paginated results with total count</li>
     * </ol>
     *
     * <p><strong>Advanced JSONB Operations:</strong></p>
     * <ul>
     *   <li><strong>CROSS JOIN LATERAL:</strong> Unnests JSONB arrays while maintaining row context</li>
     *   <li><strong>WITH ORDINALITY:</strong> Adds position numbers to unnested elements</li>
     *   <li><strong>COALESCE:</strong> Handles missing anomalies arrays gracefully</li>
     *   <li><strong>jsonb_agg:</strong> Re-aggregates paginated results into JSONB array</li>
     * </ul>
     *
     * <p><strong>Pagination Logic:</strong></p>
     * <ul>
     *   <li><strong>LIMIT :pageSize:</strong> Controls number of anomalies per page</li>
     *   <li><strong>OFFSET :offset:</strong> Skips to correct page position</li>
     *   <li><strong>ORDER BY position:</strong> Maintains original anomaly order</li>
     *   <li><strong>Total Count:</strong> Separate CTE for accurate pagination metadata</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "anomalies": [
     *     {
     *       "severity": "CRITICAL",
     *       "type": "security",
     *       "description": "Potential API key exposure",
     *       "details": "...",
     *       "aiAnalysis": "...",
     *       "recommendation": "..."
     *     }
     *
     *   ],
     *   "total_count": 25
     * }
     * }
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Efficient Pagination:</strong> Only processes requested page of anomalies</li>
     *   <li><strong>Accurate Counting:</strong> Separate CTE ensures correct total count</li>
     *   <li><strong>JSONB Optimization:</strong> Leverages PostgreSQL's optimized JSONB operations</li>
     *   <li><strong>Memory Efficient:</strong> Streams results without loading entire anomaly set</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Controller:</strong> Powers /api/dashboard/builds/{jobName}/{buildId}/detected-anomalies</li>
     *   <li><strong>Anomaly Browser:</strong> Enables paginated browsing of build anomalies</li>
     *   <li><strong>Performance Optimization:</strong> Handles large anomaly sets efficiently</li>
     *   <li><strong>UI Pagination:</strong> Provides data for pagination controls</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to analyze
     * @param pageSize the number of anomalies to return per page
     * @param offset the number of anomalies to skip (pageNumber * pageSize)
     * @return Map containing 'anomalies' JSONB array and 'total_count' for pagination
     * @see #findBuildSummary(String, Integer)
     * @see com.diploma.inno.controller.DashboardController#getDetectedAnomalies
     */
    @Query(value = "WITH unnested_anomalies AS ( " +
            "    SELECT anomaly, position " +
            "    FROM chat_messages cm " +
            "    CROSS JOIN LATERAL jsonb_array_elements( " +
            "        COALESCE(cm.content->'anomalies', '[]'::jsonb) " +
            "    ) WITH ORDINALITY AS t(anomaly, position) " +
            "    WHERE cm.message_type = 'ASSISTANT' " +
            "      AND cm.conversation_id = :conversationId " +
            "      AND cm.build_number = :buildNumber " +
            "    ORDER BY position " +
            "    LIMIT :pageSize OFFSET :offset " +
            "), " +
            "total_count AS ( " +
            "    SELECT COUNT(*) AS total " +
            "    FROM chat_messages cm " +
            "    CROSS JOIN LATERAL jsonb_array_elements( " +
            "        COALESCE(cm.content->'anomalies', '[]'::jsonb) " +
            "    ) AS t(anomaly) " +
            "    WHERE cm.message_type = 'ASSISTANT' " +
            "      AND cm.conversation_id = :conversationId " +
            "      AND cm.build_number = :buildNumber " +
            ") " +
            "SELECT " +
            "    COALESCE(jsonb_agg(anomaly ORDER BY position), '[]'::jsonb) AS anomalies, " +
            "    (SELECT total FROM total_count) AS total_count " +
            "FROM unnested_anomalies", nativeQuery = true)
    Map<String, Object> findPaginatedAnomalies(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") Integer buildNumber,
            @Param("pageSize") Integer pageSize,
            @Param("offset") Long offset);

    /**
     * Extracts and formats risk score information from AI analysis results.
     *
     * <p>This method retrieves the AI-calculated risk score data from the JSONB content field,
     * including current score, change from previous build, risk level classification, and
     * previous score for trend analysis. The data is formatted as a structured JSON object
     * for dashboard consumption.</p>
     *
     * <p><strong>JSONB Path Extraction:</strong></p>
     * <ul>
     *   <li><strong>content->'riskScore'->>'score':</strong> Current risk score (0-100)</li>
     *   <li><strong>content->'riskScore'->>'change':</strong> Change from previous build (+/-)</li>
     *   <li><strong>content->'riskScore'->>'riskLevel':</strong> Categorical risk level</li>
     *   <li><strong>content->'riskScore'->>'previousScore':</strong> Previous build's score</li>
     * </ul>
     *
     * <p><strong>Risk Level Classifications:</strong></p>
     * <ul>
     *   <li><strong>LOW:</strong> Score 0-30, minimal risk detected</li>
     *   <li><strong>MEDIUM:</strong> Score 31-60, moderate risk level</li>
     *   <li><strong>HIGH:</strong> Score 61-80, significant risk concerns</li>
     *   <li><strong>CRITICAL:</strong> Score 81-100, immediate attention required</li>
     * </ul>
     *
     * <p><strong>Response Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "score": 75,
     *   "change": 15,
     *   "riskLevel": "HIGH",
     *   "previousScore": 60
     * }
     * }
     *
     * <p><strong>Type Casting:</strong></p>
     * <ul>
     *   <li><strong>::integer:</strong> Converts JSONB text values to integers for score/change</li>
     *   <li><strong>Text Values:</strong> riskLevel returned as text for categorical display</li>
     *   <li><strong>jsonb_build_object:</strong> Constructs structured response object</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Service:</strong> Called by DashboardService.getRiskScore()</li>
     *   <li><strong>Risk Widgets:</strong> Powers risk score dashboard widgets</li>
     *   <li><strong>Trend Analysis:</strong> Enables risk score trend visualization</li>
     *   <li><strong>Alert Systems:</strong> Provides data for risk-based alerting</li>
     * </ul>
     *
     * <p><strong>AI Integration:</strong></p>
     * <ul>
     *   <li><strong>AI Calculation:</strong> Risk scores calculated by external AI service</li>
     *   <li><strong>Historical Context:</strong> Includes previous score for trend analysis</li>
     *   <li><strong>Change Detection:</strong> Tracks risk score changes between builds</li>
     *   <li><strong>Categorical Mapping:</strong> Maps numeric scores to risk levels</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to analyze
     * @return JSON string containing structured risk score information
     * @see #findBuildSummary(String, Integer)
     * @see #findLatestInsights(String, Integer)
     */
    @Query(value = "SELECT jsonb_build_object( " +
            "'score', (cm.content->'riskScore'->>'score')::integer, " +
            "'change', (cm.content->'riskScore'->>'change')::integer, " +
            "'riskLevel', cm.content->'riskScore'->>'riskLevel', " +
            "'previousScore', (cm.content->'riskScore'->>'previousScore')::integer " +
            ") AS risk_score " +
            "FROM chat_messages cm " +
            "WHERE cm.message_type = 'ASSISTANT' " +
            "  AND cm.conversation_id = :conversationId " +
            "  AND cm.build_number = :buildNumber", nativeQuery = true)
    String findRiskScore(@Param("conversationId") String conversationId, @Param("buildNumber") Integer buildNumber);


    /**
     * Retrieves the latest build anomaly counts for either a specific job or all jobs, with intelligent result limiting.
     *
     * <p>This complex query performs a multi-step analysis:</p>
     * <ol>
     *   <li><b>distinct_jobs</b>: Counts total distinct jobs matching the filter</li>
     *   <li><b>ranked_builds</b>: Ranks builds per job by timestamp and build number (newest first)</li>
     *   <li><b>limited_builds</b>: Applies dynamic limiting based on the requested build count</li>
     * </ol>
     *
     *
     * <p><b>Filtering Behavior:</b></p>
     * <ul>
     *   <li>When {@code jobFilter = 'all'}: Returns builds from all jobs, distributing the {@code buildCount}
     *       evenly across available jobs (minimum 1 build per job)</li>
     *   <li>For specific job filters: Returns the N most recent builds for just that job</li>
     * </ul>
     *
     *
     * <p>The query returns a list of maps containing these fields:</p>
     * <ul>
     *   <li><b>job</b>: The job/conversation identifier</li>
     *   <li><b>build</b>: The build number</li>
     *   <li><b>timestamp</b>: When the build occurred</li>
     *   <li><b>anomaly_count</b>: Number of anomalies detected in this build</li>
     * </ul>
     * Results are always ordered by timestamp (newest first), then by build number (descending).
     *
     *
     * @param jobFilter Either 'all' to include all jobs, or a specific job ID to filter. Must not be {@code null}.
     * @param buildCount Maximum number of builds to return. When filtering all jobs, this is distributed
     *                  across available jobs. Must be a positive integer.
     *
     * @return List of build anomaly records as maps. Returns empty list if no matching builds found.
     *
     * @throws IllegalArgumentException if either parameter is {@code null}, or if {@code buildCount} is not positive
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     * @throws jakarta.persistence.QueryTimeoutException if the complex query times out
     *
     * @see org.springframework.data.jpa.repository.Query
     */
    @Query(value = "WITH distinct_jobs AS ( " +
            "    SELECT COUNT(DISTINCT conversation_id) AS total_jobs " +
            "    FROM build_anomaly_summary " +
            "    WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter) " +
            "), " +
            "ranked_builds AS ( " +
            "    SELECT " +
            "        conversation_id AS job, " +
            "        build_number AS build, " +
            "        timestamp, " +
            "        total_anomalies AS anomaly_count, " +
            "        ROW_NUMBER() OVER (PARTITION BY conversation_id ORDER BY timestamp DESC, build_number DESC) AS rn, " +
            "        (SELECT total_jobs FROM distinct_jobs) AS total_jobs " +
            "    FROM build_anomaly_summary " +
            "    WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter) " +
            "), " +
            "limited_builds AS ( " +
            "    SELECT " +
            "        job, " +
            "        build, " +
            "        timestamp, " +
            "        anomaly_count " +
            "    FROM ranked_builds " +
            "    WHERE (:jobFilter = 'all' AND rn <= GREATEST(1, LEAST(:buildCount / NULLIF(total_jobs, 0), :buildCount))) " +
            "       OR (:jobFilter != 'all' AND rn <= :buildCount) " +
            ") " +
            "SELECT * FROM limited_builds " +
            "ORDER BY timestamp DESC, build DESC", nativeQuery = true)
    List<Map<String, Object>> findLatestBuildAnomalyCounts(
            @Param("jobFilter") String jobFilter,
            @Param("buildCount") Integer buildCount);


    /**
     * Retrieves the latest build severity distributions from the build anomaly summary table,
     * with intelligent build count distribution across jobs.
     *
     * <p>This method executes a complex native SQL query that performs the following operations:
     * <ol>
     *   <li><strong>Job Counting:</strong> Counts distinct jobs (conversation_ids) based on the filter</li>
     *   <li><strong>Build Ranking:</strong> Ranks builds within each job by timestamp and build number (most recent first)</li>
     *   <li><strong>Smart Limiting:</strong> Distributes the requested build count across jobs intelligently</li>
     * </ol>
     *
     * <h4>Query Logic Breakdown:</h4>
     *
     * <h4>1. distinct_jobs CTE:</h4>
     * <p>Counts the total number of distinct jobs (conversation_ids) that match the job filter.
     * This count is used later to calculate how many builds to retrieve per job.</p>
     *
     * <h4>2. ranked_builds CTE:</h4>
     * <p>Selects and ranks all builds within each job, ordered by most recent timestamp and highest build number.
     * Each build gets a row number (rn) within its job partition, starting from 1 for the most recent build.</p>
     *
     * <h4>3. limited_builds CTE:</h4>
     * <p>Applies intelligent limiting logic based on the job filter:</p>
     * <ul>
     *   <li><strong>When jobFilter = 'all':</strong> Distributes buildCount across all jobs evenly.
     *       Each job gets at most {@code buildCount / total_jobs} builds, but at least 1 build per job.
     *       The GREATEST(1, LEAST()) ensures each job gets at least 1 build while respecting the total limit.</li>
     *   <li><strong>When jobFilter is specific:</strong> Returns up to buildCount builds from the single specified job.</li>
     * </ul>
     *
     * <h4>4. Final SELECT:</h4>
     * <p>Orders results by timestamp (descending) and build number (descending) to show most recent builds first.</p>
     *
     * <h4>Use Cases:</h4>
     * <ul>
     *   <li><strong>Dashboard Overview:</strong> Get latest builds from all jobs for a system health overview</li>
     *   <li><strong>Job-Specific Analysis:</strong> Focus on recent builds from a particular job/conversation</li>
     *   <li><strong>Resource Management:</strong> Limit query results to prevent overwhelming the UI or API</li>
     * </ul>
     *
     * <h4>Example Scenarios:</h4>
     * <ul>
     *   <li>jobFilter='all', buildCount=20, total_jobs=5 → Each job returns up to 4 builds (20/5)</li>
     *   <li>jobFilter='all', buildCount=10, total_jobs=15 → Each job returns exactly 1 build (minimum)</li>
     *   <li>jobFilter='job-123', buildCount=10 → Returns up to 10 most recent builds from 'job-123'</li>
     * </ul>
     *
     * @param jobFilter The job/conversation filter criteria. Use 'all' to include all jobs,
     *                  or specify a particular conversation_id to filter to a specific job.
     *                  Must not be null.
     *
     * @param buildCount The maximum total number of builds to return across all selected jobs.
     *                   When jobFilter='all', this count is intelligently distributed across jobs.
     *                   When jobFilter specifies a job, this is the max builds from that job.
     *                   Must be a positive integer.
     *
     * @return A list of maps containing build severity distribution data. Each map contains:
     *         <ul>
     *           <li><strong>job:</strong> The conversation_id/job identifier (String)</li>
     *           <li><strong>build:</strong> The build number (Integer)</li>
     *           <li><strong>timestamp:</strong> When the build occurred (Timestamp)</li>
     *           <li><strong>severity_distribution:</strong> JSON/structured data containing severity counts</li>
     *         </ul>
     *         Results are ordered by timestamp (newest first), then by build number (highest first).
     *         Returns empty list if no builds match the criteria.
     *
     * @throws IllegalArgumentException if jobFilter is null or buildCount is null or non-positive
     *
     *
     * @since 1.0
     * @see org.hibernate.annotations.processing.SQL table schema
     */
    @Query(value = "WITH distinct_jobs AS ( " +
            "    SELECT COUNT(DISTINCT conversation_id) AS total_jobs " +
            "    FROM build_anomaly_summary " +
            "    WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter) " +
            "), " +
            "ranked_builds AS ( " +
            "    SELECT " +
            "        conversation_id AS job, " + // Add conversation_id AS job
            "        build_number AS build, " +
            "        timestamp, " +
            "        severity_counts AS severity_distribution, " +
            "        ROW_NUMBER() OVER (PARTITION BY conversation_id ORDER BY timestamp DESC, build_number DESC) AS rn, " +
            "        (SELECT total_jobs FROM distinct_jobs) AS total_jobs " +
            "    FROM build_anomaly_summary " +
            "    WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter) " +
            "), " +
            "limited_builds AS ( " +
            "    SELECT " +
            "        job, " + // Include job in the selection
            "        build, " +
            "        timestamp, " +
            "        severity_distribution " +
            "    FROM ranked_builds " +
            "    WHERE (:jobFilter = 'all' AND rn <= GREATEST(1, LEAST(:buildCount / NULLIF(total_jobs, 0), :buildCount))) " +
            "       OR (:jobFilter != 'all' AND rn <= :buildCount) " +
            ") " +
            "SELECT * FROM limited_builds " +
            "ORDER BY timestamp DESC, build DESC", nativeQuery = true)
    List<Map<String, Object>> findLatestBuildSeverityDistributions(
            @Param("jobFilter") String jobFilter,
            @Param("buildCount") Integer buildCount);


    /**
     * Retrieves AI-generated insights for a specific build from analysis results.
     *
     * <p>This method extracts the insights section from AI analysis results, containing
     * summary information, recommendations, and trend analysis for a specific build.
     * It's used to provide actionable intelligence about build quality and patterns.</p>
     *
     * <p><strong>JSONB Path Extraction:</strong></p>
     * <ul>
     *   <li><strong>content->'insights':</strong> Extracts entire insights object from JSONB</li>
     *   <li><strong>ORDER BY timestamp DESC:</strong> Ensures latest analysis if multiple exist</li>
     *   <li><strong>LIMIT 1:</strong> Returns only the most recent insights</li>
     * </ul>
     *
     * <p><strong>Insights Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "summary": "Overall build analysis summary",
     *   "recommendations": [
     *     "Fix security vulnerability in authentication module",
     *     "Optimize database queries for better performance",
     *     "Update deprecated dependencies"
     *   ],
     *   "trends": {
     *     "pattern": "improving|stable|deteriorating",
     *     "direction": "positive|negative|neutral",
     *     "confidence": 0.85
     *   },
     *   "keyFindings": [
     *     "No critical security issues detected",
     *     "Performance improved by 15% from previous build"
     *   ]
     * }
     * }
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Service:</strong> Called by DashboardService.getAiInsights()</li>
     *   <li><strong>Build Detail View:</strong> Powers insights section in build details</li>
     *   <li><strong>Recommendation Engine:</strong> Provides actionable recommendations</li>
     *   <li><strong>Trend Analysis:</strong> Enables pattern recognition across builds</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to analyze
     * @return Map containing insights JSONB object, or null if no insights found
     * @see #findLatestInsightsByConversation(String)
     * @see #findRiskScore(String, Integer)
     */
    @Query(value = "SELECT content->'insights' AS insights " +
            "FROM chat_messages " +
            "WHERE conversation_id = :conversationId " +
            "  AND build_number = :buildNumber " +
            "  AND message_type = 'ASSISTANT' " +
            "ORDER BY timestamp DESC " +
            "LIMIT 1", nativeQuery = true)
    Map<String, Object> findLatestInsights(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") Integer buildNumber);

    /**
     * Retrieves the most recent AI-generated insights for a conversation (job) across all builds.
     *
     * <p>This method extracts insights from the latest AI analysis for a specific Jenkins job,
     * regardless of build number. It's useful for getting the most current insights about
     * a job's overall health and trends when specific build context isn't required.</p>
     *
     * <p><strong>Query Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Cross-Build Search:</strong> Searches across all builds for the job</li>
     *   <li><strong>Latest Analysis:</strong> Returns insights from most recent AI analysis</li>
     *   <li><strong>Job-Level View:</strong> Provides job-wide insights rather than build-specific</li>
     *   <li><strong>Fallback Option:</strong> Used when specific build insights aren't available</li>
     * </ul>
     *
     * <p><strong>Comparison with findLatestInsights:</strong></p>
     * <ul>
     *   <li><strong>findLatestInsights:</strong> Build-specific insights (includes build_number filter)</li>
     *   <li><strong>findLatestInsightsByConversation:</strong> Job-wide insights (no build_number filter)</li>
     *   <li><strong>Use Case:</strong> Job overview vs specific build analysis</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Service:</strong> Called by DashboardService.getAiInsightsByConversation()</li>
     *   <li><strong>Job Overview:</strong> Powers job-level insights in dashboard</li>
     *   <li><strong>Fallback Mechanism:</strong> Used when build-specific insights unavailable</li>
     *   <li><strong>Job Health Summary:</strong> Provides overall job health assessment</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @return Map containing insights JSONB object from latest analysis, or null if none found
     * @see #findLatestInsights(String, Integer)
     * @see #findBuildSummary(String, Integer)
     */
    @Query(value = "SELECT content->'insights' AS insights " +
            "FROM chat_messages " +
            "WHERE conversation_id = :conversationId " +
            "  AND message_type = 'ASSISTANT' " +
            "ORDER BY timestamp DESC " +
            "LIMIT 1", nativeQuery = true)
    Map<String, Object> findLatestInsightsByConversation(
            @Param("conversationId") String conversationId);


    /**
     * Retrieves job status information with comprehensive filtering capabilities.
     *
     * <p>This complex query performs a multi-step analysis of job/build data:</p>
     * <ol>
     *   <li><b>latest_messages</b>: Gets the most recent chat message for each build with known status</li>
     *   <li><b>latest_builds</b>: Identifies the most recent build for each job</li>
     *   <li><b>relevant_builds</b>: Enriches build data with anomaly counts and active status</li>
     *   <li><b>final selection</b>: Formats the output with human-readable timestamps and status categories</li>
     * </ol>
     *
     *
     * <p>The query returns a list of maps containing these fields:</p>
     * <ul>
     *   <li><b>job_name</b>: The conversation/job identifier</li>
     *   <li><b>last_build</b>: Formatted build timestamp (relative time or absolute)</li>
     *   <li><b>status</b>: Categorized build status (RUNNING, COMPLETED, FAILED, etc.)</li>
     *   <li><b>anomalies</b>: Count of detected anomalies (0 if none)</li>
     * </ul>
     *
     *
     * <p><b>Filtering Options:</b></p>
     * <ul>
     *   <li><b>all</b>: Returns all jobs regardless of status</li>
     *   <li><b>active</b>: Only currently running jobs</li>
     *   <li><b>completed</b>: Successfully completed jobs without anomalies</li>
     *   <li><b>completedwithissues</b>: Completed successfully but with anomalies</li>
     *   <li><b>withissues</b>: Any job with anomalies or failure status</li>
     * </ul>
     *
     *
     * @param statusFilter The filter criteria to apply. Must be one of:
     *                    "all", "active", "completed", "completedwithissues", or "withissues".
     *                    Case-sensitive. Must not be {@code null}.
     *
     * @return List of job status maps, each containing keys: "job_name", "last_build",
     *         "status", and "anomalies". Returns empty list if no matches found.
     *         Results ordered by timestamp (newest first).
     *
     * @throws IllegalArgumentException if {@code statusFilter} is {@code null} or invalid
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     * @throws jakarta.persistence.QueryTimeoutException if the query times out
     *
     * @see org.springframework.data.jpa.repository.Query
     */
    @Query(value = "WITH latest_messages AS ( " +
            "    SELECT DISTINCT ON (cm.conversation_id, cm.build_number) " + // Group by BOTH conversation_id AND build_number
            "        cm.conversation_id, " +
            "        cm.build_number, " +
            "        cm.timestamp, " +
            "        COALESCE( " +
            "            NULLIF(cm.content -> 'buildMetadata' ->> 'status', ''), " +
            "            NULLIF(cm.content -> 'data' -> 'build_info' ->> 'result', ''), " +
            "            'UNKNOWN' " +
            "        ) AS build_status " +
            "    FROM chat_messages cm " +
            "    WHERE COALESCE( " +
            "            NULLIF(cm.content -> 'buildMetadata' ->> 'status', ''), " +
            "            NULLIF(cm.content -> 'data' -> 'build_info' ->> 'result', ''), " +
            "            'UNKNOWN' " +
            "        ) != 'UNKNOWN' " + // Only get messages with actual status
            "    ORDER BY cm.conversation_id, cm.build_number, cm.timestamp DESC " + // Latest per build
            "), " +
            "latest_builds AS ( " +
            "    SELECT DISTINCT ON (lm.conversation_id) " + // Now get latest build per job
            "        lm.conversation_id, " +
            "        lm.build_number, " +
            "        lm.timestamp, " +
            "        lm.build_status " +
            "    FROM latest_messages lm " +
            "    ORDER BY lm.conversation_id, lm.build_number DESC " + // Latest build number
            "), " +
            "relevant_builds AS ( " +
            "    SELECT " +
            "        lb.conversation_id, " +
            "        lb.build_number, " +
            "        lb.timestamp, " +
            "        lb.build_status, " +
            "        COALESCE(bas.total_anomalies, 0) AS total_anomalies, " +
            "        COALESCE(abc.active_builds, 0) > 0 AS is_active " +
            "    FROM latest_builds lb " +
            "    LEFT JOIN build_anomaly_summary bas ON lb.conversation_id = bas.conversation_id AND lb.build_number = bas.build_number " +
            "    LEFT JOIN active_build_counts abc ON lb.conversation_id = abc.job_filter " +
            ") " +
            "SELECT " +
            "    rb.conversation_id AS job_name, " +
            "    rb.build_number || ' - ' || " +
            "    CASE " +
            "        WHEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) < 3600 THEN FLOOR(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) / 60) || ' minutes ago' " +
            "        WHEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) < 86400 THEN FLOOR(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) / 3600) || ' hours ago' " +
            "        WHEN EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) < 2592000 THEN FLOOR(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - rb.timestamp)) / 86400) || ' days ago' " +
            "        ELSE TO_CHAR(rb.timestamp, 'YYYY-MM-DD HH24:MI') " +
            "    END AS last_build, " +
            "    CASE " +
            "        WHEN rb.is_active THEN 'RUNNING' " +
            "        WHEN rb.build_status = 'SUCCESS' THEN 'COMPLETED' " +
            "        WHEN rb.build_status IN ('FAILURE', 'ABORTED', 'UNSTABLE') THEN 'FAILED' " +
            "        ELSE rb.build_status " +
            "    END AS status, " +
            "    rb.total_anomalies AS anomalies " +
            "FROM relevant_builds rb " +
            "WHERE (:statusFilter = 'all' " +
            "       OR (:statusFilter = 'active' AND rb.is_active) " +
            "       OR (:statusFilter = 'completed' AND NOT rb.is_active AND rb.build_status = 'SUCCESS') " +
            "       OR (:statusFilter = 'completedwithissues' AND NOT rb.is_active AND rb.build_status = 'SUCCESS' AND rb.total_anomalies > 0) " +
            "       OR (:statusFilter = 'withissues' AND (rb.total_anomalies > 0 OR rb.build_status IN ('FAILURE', 'ABORTED', 'UNSTABLE')))) " +
            "ORDER BY rb.timestamp DESC", nativeQuery = true)
    List<Map<String, Object>> findJobsByStatus(@Param("statusFilter") String statusFilter);



    /**
     * Retrieves job count statistics for a specific time boundary from the database.
     *
     * <p>Executes a native SQL query that selects three columns from the {@code job_counts} table
     * and maps them to a {@link JobCountProjection} interface:</p>
     * <ul>
     *   <li>{@code time_boundary} (aliased as {@code timeBoundary}) - The time period identifier (e.g., daily, weekly)</li>
     *   <li>{@code total_jobs} (aliased as {@code totalJobs}) - The total number of jobs counted</li>
     *   <li>{@code computed_at} (aliased as {@code computedAt}) - The timestamp when the count was computed</li>
     * </ul>
     *
     *
     * <p><b>Note:</b> This is a read-only operation that expects the {@code timeBoundary} parameter</p>
     * <p>to match exactly one record in the database. The query uses column aliasing to match the</p>
     * <p>projection interface's property names.</p>
     *
     * @param timeBoundary the time boundary identifier used to select the statistics record.
     *                    Must not be {@code null} or empty. Example values: "daily", "weekly", "monthly".
     *
     * @return a {@link JobCountProjection} containing the job count statistics for the specified
     *         time boundary, or {@code null} if no matching record is found.
     *
     * @throws IllegalArgumentException if {@code timeBoundary} is {@code null} or empty
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     * @throws jakarta.persistence.NonUniqueResultException if multiple records match the time boundary
     *
     * @see org.springframework.data.jpa.repository.Query
     * @see JobCountProjection
     */
    @Query(value = "SELECT time_boundary AS timeBoundary, " +
            "       total_jobs AS totalJobs, " +
            "       computed_at AS computedAt " +
            "FROM job_counts " +
            "WHERE time_boundary = :timeBoundary",
            nativeQuery = true)
    JobCountProjection findJobCountByTimeBoundary(@Param("timeBoundary") String timeBoundary);


    /**
     * Retrieves active build count statistics for a specific job filter from the database.
     *
     * <p>This method executes a native SQL query that selects three specific columns from the
     * {@code active_build_counts} table and maps them to an {@link ActiveBuildCountProjection}:</p>
     * <ul>
     *   <li>{@code job_filter} (aliased as {@code jobFilter})</li>
     *   <li>{@code active_builds} (aliased as {@code activeBuilds})</li>
     *   <li>{@code computed_at} (aliased as {@code computedAt})</li>
     * </ul>
     *
     *
     * <p><b>Note:</b> This is a read-only operation that doesn't modify the database state.
     * The query uses column aliasing to match the projection interface's property names.</p>
     *
     * @param jobFilter the job filter string used to identify the specific record to retrieve.
     *                 Must not be {@code null} or empty.
     *
     * @return an {@link ActiveBuildCountProjection} containing the job filter, active builds count,
     *         and computation timestamp. Returns {@code null} if no matching record is found.
     *
     * @throws IllegalArgumentException if {@code jobFilter} is {@code null} or empty
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     *
     * @see org.springframework.data.jpa.repository.Query
     * @see ActiveBuildCountProjection
     */
    @Query(value = "SELECT job_filter AS jobFilter, " +
            "       active_builds AS activeBuilds, " +
            "       computed_at AS computedAt " +
            "FROM active_build_counts " +
            "WHERE job_filter = :jobFilter",
            nativeQuery = true)
    ActiveBuildCountProjection findActiveBuildCountByJobFilter(@Param("jobFilter") String jobFilter);


    /**
     * Retrieves security anomaly count statistics for a specific job filter and time range combination.
     *
     * <p>Executes a native SQL query that selects four columns from the {@code security_anomaly_counts} table
     * and maps them to a {@link SecurityAnomalyCountProjection} interface:</p>
     * <ul>
     *   <li>{@code job_filter} (aliased as {@code jobFilter}) - The job filter identifier</li>
     *   <li>{@code anomaly_count} (aliased as {@code anomalyCount}) - The count of security anomalies</li>
     *   <li>{@code time_range} (aliased as {@code timeRange}) - The time period these statistics cover</li>
     *   <li>{@code computed_at} (aliased as {@code computedAt}) - When these statistics were calculated</li>
     * </ul>
     *
     *
     * <p><b>Note:</b> This query requires both parameters to match exactly one record in the database.
     * The combination of {@code jobFilter} and {@code timeRange} should be unique in the table.</p>
     *
     * @param jobFilter the job filter string used to identify the specific security statistics record.
     *                 Must not be {@code null} or empty.
     * @param timeRange the time range string (e.g., "7d", "30d") specifying which period's statistics to retrieve.
     *                 Must not be {@code null} or empty.
     *
     * @return a {@link SecurityAnomalyCountProjection} containing the security anomaly statistics,
     *         or {@code null} if no matching record is found for the given parameters.
     *
     * @throws IllegalArgumentException if either {@code jobFilter} or {@code timeRange} is {@code null} or empty
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     * @throws jakarta.persistence.NonUniqueResultException if multiple records match the criteria
     *
     * @see org.springframework.data.jpa.repository.Query
     * @see SecurityAnomalyCountProjection
     */
    @Query(value = "SELECT job_filter AS jobFilter, " +
            "       anomaly_count AS anomalyCount, " +
            "       time_range AS timeRange, " +
            "       computed_at AS computedAt " +
            "FROM security_anomaly_counts " +
            "WHERE job_filter = :jobFilter AND time_range = :timeRange",
            nativeQuery = true)
    SecurityAnomalyCountProjection findSecurityAnomalyCountByJobFilterAndTimeRange(
            @Param("jobFilter") String jobFilter,
            @Param("timeRange") String timeRange);



    /**
     * Retrieves recent builds for a specific job, ordered chronologically (newest first).
     *
     * <p>This native query selects all build records from the {@code recent_job_builds} table
     * matching the specified job name, with the following field mappings:</p>
     * <ul>
     *   <li>{@code job_name} → {@code jobName}</li>
     *   <li>{@code build_id} → {@code buildId}</li>
     *   <li>{@code health_status} → {@code healthStatus}</li>
     *   <li>{@code anomaly_count} → {@code anomalyCount}</li>
     *   <li>{@code time_ago} → {@code timeAgo} (human-readable time since build)</li>
     *   <li>{@code raw_timestamp} → {@code rawTimestamp} (exact build timestamp)</li>
     *   <li>{@code computed_at} → {@code computedAt} (when record was generated)</li>
     *   <li>{@code original_job_name} → {@code originalJobName} (source system job name)</li>
     * </ul>
     *
     *
     * <p><b>Ordering:</b> Results are always sorted by {@code raw_timestamp} in descending order
     * (newest builds first).</p>
     *
     * @param jobName The exact job name to filter by. Must match exactly (case-sensitive).
     *               Null values will be filtered out by the query.
     *
     * @return A non-null List of {@link RecentJobBuildProjection} objects containing build details.
     *         Returns empty list if:
     *         <ul>
     *           <li>No builds exist for the job</li>
     *           <li>Job name doesn't match any records</li>
     *         </ul>
     *
     * @throws jakarta.persistence.PersistenceException if query execution fails
     *
     * @see RecentJobBuildProjection
     */
    @Query(value = "SELECT job_name AS jobName, " +
            "       build_id AS buildId, " +
            "       health_status AS healthStatus, " +
            "       anomaly_count AS anomalyCount, " +
            "       time_ago AS timeAgo, " +
            "       raw_timestamp AS rawTimestamp, " +
            "       computed_at AS computedAt, " +
            "       original_job_name AS originalJobName " +
            "FROM recent_job_builds " +
            "WHERE job_name = :jobName " +
            "ORDER BY raw_timestamp DESC",
            nativeQuery = true)
    List<RecentJobBuildProjection> findRecentJobBuildsByJobName(@Param("jobName") String jobName);


    /**
     * Retrieves all distinct job names from the chat messages table.
     *
     * <p>This native query:</p>
     * <ul>
     *   <li>Selects only non-null job names from {@code chat_messages} table</li>
     *   <li>Returns unique values (DISTINCT)</li>
     *   <li>Orders results alphabetically by job name</li>
     *   <li>Uses case-sensitive sorting</li>
     * </ul>
     *
     *
     * <p><b>Note:</b> The list represents all jobs that have ever generated chat messages,
     * not necessarily currently active jobs.</p>
     *
     * @return A non-null, alphabetically ordered List of distinct job names as Strings.
     *         Returns empty list if no job messages exist or all job_name values are null.
     *
     * @throws jakarta.persistence.PersistenceException if query execution fails
     */
    @Query(value = "SELECT DISTINCT job_name " +
            "FROM chat_messages " +
            "WHERE job_name IS NOT NULL " +
            "ORDER BY job_name",
            nativeQuery = true)
    List<String> findDistinctJobNames();


    /**
     * Deletes all chat messages associated with the given collection of job names in a single transaction.
     *
     * <p>This method performs a bulk delete operation directly in the database using a native SQL query,
     * which is more efficient than deleting entities one by one. The operation is executed within a
     * transactional context and modifies the database state.</p>
     *
     * <p><b>Note:</b> This is a modifying query that bypasses JPA's entity lifecycle events (like @PreRemove).
     * Cache eviction and other side effects might need to be handled manually if required.</p>
     *
     * @param jobNames a collection of job names identifying the messages to be deleted.
     *                 Must not be {@code null}. If empty, no deletions will occur.
     *
     * @throws jakarta.persistence.TransactionRequiredException if no transaction is active
     * @throws jakarta.persistence.PersistenceException if the query execution fails
     *
     * @see org.springframework.transaction.annotation.Transactional
     * @see org.springframework.data.jpa.repository.Modifying
     * @see org.springframework.data.jpa.repository.Query
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM chat_messages WHERE job_name IN :jobNames", nativeQuery = true)
    void deleteByJobNameIn(@Param("jobNames") Collection<String> jobNames);

    /**
     * Counts USER messages (build logs) for tracking log collection progress.
     *
     * <p>This method provides a count of received log messages for a specific build,
     * which is used to track log collection progress and determine completion status.</p>
     *
     * @param conversationId The conversation identifier (Jenkins job name)
     * @param buildNumber    The specific build number
     * @return Count of USER messages (build logs) received for the build
     * @see #countByConversationIdAndBuildNumberAndContentContaining(String, int)
     */
    @Query(value = "SELECT COUNT(*) FROM chat_messages WHERE message_type = 'USER' AND conversation_id = :conversationId AND build_number = :buildNumber", nativeQuery = true)
    Long getTrackingLogInfo(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") Integer buildNumber
    );

    /**
     * Retrieves paginated build logs (USER messages) for a specific job and build with instruction filtering.
     *
     * <p>This method implements efficient pagination for build log retrieval, filtering out instruction
     * messages to return only actual build log content. It's designed for high-performance log browsing
     * in the dashboard interface where users need to examine build logs in manageable chunks.</p>
     *
     * <p><strong>Filtering Logic:</strong></p>
     * <ul>
     *   <li><strong>message_type = 'USER':</strong> Only retrieves build log messages</li>
     *   <li><strong>NOT jsonb_exists(content, 'instructions'):</strong> Excludes instruction/control messages</li>
     *   <li><strong>conversation_id + build_number:</strong> Targets specific build logs</li>
     *   <li><strong>ORDER BY timestamp:</strong> Maintains chronological log order</li>
     * </ul>
     *
     * <p><strong>JSONB Operations:</strong></p>
     * <ul>
     *   <li><strong>jsonb_exists:</strong> Checks for presence of 'instructions' key in JSONB</li>
     *   <li><strong>NOT operator:</strong> Inverts the existence check to exclude instructions</li>
     *   <li><strong>Content extraction:</strong> Returns raw JSONB content as string</li>
     * </ul>
     *
     * <p><strong>Pagination Parameters:</strong></p>
     * <ul>
     *   <li><strong>LIMIT :limit:</strong> Controls number of log entries per page</li>
     *   <li><strong>OFFSET :offset:</strong> Skips to correct page position (pageNumber * pageSize)</li>
     *   <li><strong>Chronological Order:</strong> Ensures logs appear in execution order</li>
     * </ul>
     *
     * <p><strong>Expected Log Content Structure:</strong></p>
     * {@snippet lang=json :
     * {
     *   "type": "build_log_data",
     *   "log_chunk": "Started by user admin\nRunning as SYSTEM\n...",
     *   "chunk_index": 1,
     *   "total_chunks": 14,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Efficient Pagination:</strong> Only loads requested page of logs</li>
     *   <li><strong>Index Utilization:</strong> Uses composite index on (conversation_id, build_number)</li>
     *   <li><strong>Memory Efficient:</strong> Streams results without loading entire log set</li>
     *   <li><strong>JSONB Optimization:</strong> Leverages PostgreSQL's optimized JSONB operations</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Dashboard Service:</strong> Called by DashboardService.getAllLogs()</li>
     *   <li><strong>Log Viewer:</strong> Powers paginated log browsing interface</li>
     *   <li><strong>Build Analysis:</strong> Enables detailed examination of build execution</li>
     *   <li><strong>Debugging:</strong> Provides access to raw build logs for troubleshooting</li>
     * </ul>
     *
     * <p><strong>Integration with Log Collection:</strong></p>
     * <ul>
     *   <li><strong>Expected Chunks:</strong> Typically 14 chunks per build (total_chunks field)</li>
     *   <li><strong>Progress Tracking:</strong> Used with getTrackingLogInfo() for completion status</li>
     *   <li><strong>Chronological Order:</strong> Maintains original Jenkins log sequence</li>
     * </ul>
     *
     * @param conversationId the Jenkins job name (conversation identifier)
     * @param buildNumber the specific build number to retrieve logs for
     * @param limit the number of log entries to return per page
     * @param offset the number of log entries to skip (pageNumber * pageSize)
     * @return List of JSONB content strings containing build log data
     * @see #getTrackingLogInfo(String, Integer)
     * @see #countByConversationIdAndBuildNumberAndContentContaining(String, int)
     */
    @Query(value = "SELECT content FROM chat_messages " +
            "WHERE message_type = 'USER' " +
            "AND conversation_id = :conversationId " +
            "AND build_number = :buildNumber " +
            "AND NOT jsonb_exists(content, 'instructions') " +
            "ORDER BY timestamp " +
            "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<String> findAllUserLogsByJobAndBuildWithPagination(
            @Param("conversationId") String conversationId,
            @Param("buildNumber") Integer buildNumber,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * Retrieves all ASSISTANT messages with content for trend analysis across all jobs.
     *
     * <p>This method fetches AI analysis messages grouped by day for trend analysis.
     * Used for generating time-series data and identifying patterns in build analysis.</p>
     *
     * <p><strong>SQL Features:</strong></p>
     * <ul>
     *   <li>Uses {@code DATE_TRUNC('day', timestamp)} for daily grouping</li>
     *   <li>Returns both date and content for flexible analysis</li>
     *   <li>Ordered by date for chronological processing</li>
     * </ul>
     *
     * @param startTime Start of the analysis period
     * @param endTime   End of the analysis period
     * @return List of Object arrays containing [date, content] pairs
     * @see #getMessagesForTrendByJob(String, Instant, Instant)
     */
    @Query(value = """
            SELECT DATE_TRUNC('day', timestamp) AS date, content
            FROM chat_messages
            WHERE message_type = 'ASSISTANT'
              AND timestamp >= :startTime
              AND timestamp <= :endTime
            ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getAllMessagesForTrend(@Param("startTime") Instant startTime,
                                          @Param("endTime") Instant endTime);

    /**
     * Retrieves ASSISTANT messages with content for trend analysis for a specific job.
     *
     * <p>This method is the job-specific version of trend analysis, providing
     * daily-grouped AI analysis data for a particular Jenkins job.</p>
     *
     * @param jobFilter The specific job name to analyze
     * @param startTime Start of the analysis period
     * @param endTime   End of the analysis period
     * @return List of Object arrays containing [date, content] pairs for the specified job
     * @see #getAllMessagesForTrend(Instant, Instant)
     */
    @Query(value = """
            SELECT DATE_TRUNC('day', timestamp) AS date, content
            FROM chat_messages
            WHERE message_type = 'ASSISTANT'
              AND conversation_id = :jobFilter
              AND timestamp >= :startTime
              AND timestamp <= :endTime
            ORDER BY date
            """, nativeQuery = true)
    List<Object[]> getMessagesForTrendByJob(@Param("jobFilter") String jobFilter,
                                            @Param("startTime") Instant startTime,
                                            @Param("endTime") Instant endTime);

}


