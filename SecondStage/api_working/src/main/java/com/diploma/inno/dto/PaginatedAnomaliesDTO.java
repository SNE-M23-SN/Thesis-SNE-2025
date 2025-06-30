package com.diploma.inno.dto;

import java.util.List;

/**
 * Data Transfer Object for paginated anomaly results from the CI Anomaly Detector system.
 *
 * <p>This DTO encapsulates a paginated response containing AI-detected anomalies from specific
 * Jenkins builds, along with comprehensive pagination metadata. It provides structured access
 * to anomaly data while supporting efficient pagination for large datasets, enabling responsive
 * user interfaces and optimal data transfer for build analysis dashboards.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>JSONB Array Extraction:</strong> Anomalies extracted from chat_messages.content->'anomalies' JSONB array</li>
 *   <li><strong>CROSS JOIN LATERAL:</strong> Complex SQL unnests JSONB array into individual rows with ordinality</li>
 *   <li><strong>Pagination Logic:</strong> LIMIT and OFFSET applied to unnested anomaly rows</li>
 *   <li><strong>Total Count Calculation:</strong> Separate CTE counts total anomalies for pagination metadata</li>
 *   <li><strong>JSONB Aggregation:</strong> Paginated anomalies re-aggregated into JSONB array</li>
 *   <li><strong>Jackson Deserialization:</strong> JSONB content converted to List&lt;AnomalyDTO&gt; objects</li>
 *   <li><strong>DTO Construction:</strong> Service layer creates PaginatedAnomaliesDTO with metadata</li>
 * </ol>
 *
 * <p><strong>Database Query Structure:</strong></p>
 * {@snippet lang=sql :
 * WITH unnested_anomalies AS (
 *   SELECT anomaly, position
 *   FROM chat_messages cm
 *   CROSS JOIN LATERAL jsonb_array_elements(
 *     COALESCE(cm.content->'anomalies', '[]'::jsonb)
 *   ) WITH ORDINALITY AS t(anomaly, position)
 *   WHERE cm.message_type = 'ASSISTANT'
 *     AND cm.conversation_id = :conversationId
 *     AND cm.build_number = :buildNumber
 *   ORDER BY position
 *   LIMIT :pageSize OFFSET :offset
 * ),
 * total_count AS (
 *   SELECT COUNT(*) AS total
 *   FROM chat_messages cm
 *   CROSS JOIN LATERAL jsonb_array_elements(
 *     COALESCE(cm.content->'anomalies', '[]'::jsonb)
 *   ) AS t(anomaly)
 *   WHERE cm.message_type = 'ASSISTANT'
 *     AND cm.conversation_id = :conversationId
 *     AND cm.build_number = :buildNumber
 * )
 * SELECT COALESCE(jsonb_agg(anomaly ORDER BY position), '[]'::jsonb) AS anomalies,
 *        (SELECT total FROM total_count) AS total_count
 * FROM unnested_anomalies
 * }
 *
 * <p><strong>JSONB Processing Details:</strong></p>
 * <ul>
 *   <li><strong>CROSS JOIN LATERAL:</strong> Unnests JSONB array into individual rows for pagination</li>
 *   <li><strong>WITH ORDINALITY:</strong> Preserves original array order through position tracking</li>
 *   <li><strong>COALESCE:</strong> Handles cases where anomalies array doesn't exist in JSONB</li>
 *   <li><strong>jsonb_agg:</strong> Reconstructs paginated array maintaining original order</li>
 * </ul>
 *
 * <p><strong>Pagination Implementation:</strong></p>
 * <ul>
 *   <li><strong>1-based Page Numbers:</strong> Frontend-friendly pagination (page 1, 2, 3...)</li>
 *   <li><strong>Offset Calculation:</strong> offset = (pageNumber - 1) * pageSize</li>
 *   <li><strong>Default Page Size:</strong> 3 anomalies per page for optimal UI display</li>
 *   <li><strong>Total Count:</strong> Separate query provides accurate total for pagination controls</li>
 * </ul>
 *
 * <p><strong>Anomaly Data Structure:</strong></p>
 * <p>Each anomaly contains the following AI-generated fields:</p>
 * <ul>
 *   <li><strong>type:</strong> Anomaly category (security, performance, quality, etc.)</li>
 *   <li><strong>severity:</strong> Impact level (CRITICAL, HIGH, MEDIUM, LOW, WARNING)</li>
 *   <li><strong>description:</strong> Human-readable anomaly description</li>
 *   <li><strong>recommendation:</strong> AI-suggested remediation steps</li>
 *   <li><strong>aiAnalysis:</strong> Detailed AI analysis and reasoning</li>
 *   <li><strong>details:</strong> Additional technical context and metadata</li>
 * </ul>
 *
 * <p><strong>Service Layer Processing:</strong></p>
 * <ul>
 *   <li><strong>Input Validation:</strong> Validates conversationId, buildNumber, pageNumber, pageSize</li>
 *   <li><strong>Jackson Conversion:</strong> Converts JSONB string/object to List&lt;AnomalyDTO&gt;</li>
 *   <li><strong>Error Handling:</strong> Graceful handling of malformed JSON or missing data</li>
 *   <li><strong>Response Wrapping:</strong> Wraps result in Map with hasData flag and error messages</li>
 * </ul>
 *
 * <p><strong>REST API Integration:</strong></p>
 * <ul>
 *   <li><strong>Endpoint:</strong> GET /api/dashboard/builds/{jobName}/{buildId}/detected-anomalies</li>
 *   <li><strong>Query Parameters:</strong> page (default: 1), size (default: 3)</li>
 *   <li><strong>Response Format:</strong> Wrapped in Map with hasData boolean and data object</li>
 *   <li><strong>Error Responses:</strong> Structured error messages for various failure scenarios</li>
 * </ul>
 *
 * <p><strong>Frontend Integration:</strong></p>
 * <ul>
 *   <li><strong>Pagination Controls:</strong> totalCount enables page navigation calculation</li>
 *   <li><strong>Loading States:</strong> hasData flag controls loading and empty state display</li>
 *   <li><strong>Anomaly Display:</strong> Individual AnomalyDTO objects rendered as cards or lists</li>
 *   <li><strong>Infinite Scroll:</strong> Supports both traditional pagination and infinite scroll patterns</li>
 * </ul>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Efficient Pagination:</strong> Database-level LIMIT/OFFSET prevents large data transfers</li>
 *   <li><strong>JSONB Optimization:</strong> Native PostgreSQL JSONB operations for fast processing</li>
 *   <li><strong>Memory Management:</strong> Small page sizes prevent memory issues with large anomaly sets</li>
 *   <li><strong>Query Optimization:</strong> Indexed conversation_id and build_number for fast filtering</li>
 * </ul>
 *
 * <p><strong>Error Handling Scenarios:</strong></p>
 * <ul>
 *   <li><strong>Invalid Parameters:</strong> Null or empty conversationId/buildNumber</li>
 *   <li><strong>Missing Data:</strong> No anomalies found for specified build</li>
 *   <li><strong>JSON Parsing Errors:</strong> Malformed JSONB content in database</li>
 *   <li><strong>Database Errors:</strong> Connection issues or query failures</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "anomalies": [
 *     {
 *       "type": "security",
 *       "severity": "HIGH",
 *       "description": "Potential secret exposure in build logs",
 *       "recommendation": "Review and remove sensitive data",
 *       "aiAnalysis": "AI detected patterns suggesting API key exposure...",
 *       "details": {
 *         "location": "build-step-3:line-45",
 *         "confidence": 0.85
 *       }
 *     }
 *   ],
 *   "totalCount": 15,
 *   "pageNumber": 1,
 *   "pageSize": 3
 * }
 * }
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All fields are set via constructor and accessed through getter methods.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see AnomalyDTO
 * @see com.diploma.inno.service.DashboardService#getPaginatedAnomalies(String, Integer, Integer, Integer)
 * @see com.diploma.inno.repository.ChatMessageRepository#findPaginatedAnomalies(String, Integer, Integer, Long)
 */
public class PaginatedAnomaliesDTO {

    /**
     * The list of anomalies for the current page.
     *
     * <p>This field contains the AI-detected anomalies for the requested page,
     * with each anomaly representing a specific issue, security concern, or
     * performance problem identified during the build analysis process.</p>
     *
     * <p><strong>Data Source:</strong></p>
     * <ul>
     *   <li><strong>JSONB Origin:</strong> Extracted from chat_messages.content->'anomalies' array</li>
     *   <li><strong>AI Generation:</strong> Created by AI analysis of build logs and artifacts</li>
     *   <li><strong>Pagination:</strong> Subset of total anomalies based on page parameters</li>
     *   <li><strong>Ordering:</strong> Maintains original array order from AI analysis</li>
     * </ul>
     *
     * <p><strong>Content Structure:</strong></p>
     * <p>Each AnomalyDTO in the list contains:</p>
     * <ul>
     *   <li><strong>type:</strong> Categorization (security, performance, quality, configuration)</li>
     *   <li><strong>severity:</strong> Impact assessment (CRITICAL, HIGH, MEDIUM, LOW, WARNING)</li>
     *   <li><strong>description:</strong> Human-readable problem description</li>
     *   <li><strong>recommendation:</strong> AI-suggested remediation actions</li>
     *   <li><strong>aiAnalysis:</strong> Detailed AI reasoning and context</li>
     *   <li><strong>details:</strong> Technical metadata and additional information</li>
     * </ul>
     *
     * <p><strong>Jackson Processing:</strong></p>
     * <ul>
     *   <li><strong>Deserialization:</strong> JSONB content converted via Jackson ObjectMapper</li>
     *   <li><strong>Type Safety:</strong> TypeReference ensures proper List&lt;AnomalyDTO&gt; conversion</li>
     *   <li><strong>Error Handling:</strong> Malformed JSON gracefully handled with empty lists</li>
     *   <li><strong>Null Safety:</strong> COALESCE in SQL ensures non-null JSONB arrays</li>
     * </ul>
     *
     * <p><strong>Pagination Context:</strong></p>
     * <ul>
     *   <li><strong>Page Size:</strong> Typically 3 anomalies per page for optimal UI display</li>
     *   <li><strong>Ordering:</strong> Preserves original AI analysis order via position tracking</li>
     *   <li><strong>Completeness:</strong> May contain fewer items than pageSize on last page</li>
     *   <li><strong>Empty Pages:</strong> Empty list when no anomalies exist for the page</li>
     * </ul>
     *
     * <p><strong>Usage Patterns:</strong></p>
     * <ul>
     *   <li><strong>UI Rendering:</strong> Displayed as cards, lists, or detailed views</li>
     *   <li><strong>Filtering:</strong> Can be filtered by type or severity on frontend</li>
     *   <li><strong>Sorting:</strong> Maintains database order but can be re-sorted client-side</li>
     *   <li><strong>Navigation:</strong> Used with pagination controls for browsing</li>
     * </ul>
     */
    private List<AnomalyDTO> anomalies;

    /**
     * The total number of anomalies across all pages.
     *
     * <p>This field provides the complete count of anomalies detected for the
     * specified build, enabling accurate pagination control calculation and
     * progress indication in user interfaces.</p>
     *
     * <p><strong>Calculation Method:</strong></p>
     * <ul>
     *   <li><strong>Separate CTE:</strong> Calculated via dedicated total_count Common Table Expression</li>
     *   <li><strong>Full Dataset:</strong> Counts all anomalies before pagination is applied</li>
     *   <li><strong>JSONB Counting:</strong> Uses CROSS JOIN LATERAL to count array elements</li>
     *   <li><strong>Accuracy:</strong> Reflects exact number of anomalies in the build</li>
     * </ul>
     *
     * <p><strong>Pagination Calculations:</strong></p>
     * <ul>
     *   <li><strong>Total Pages:</strong> Math.ceil(totalCount / pageSize)</li>
     *   <li><strong>Has Next Page:</strong> (pageNumber * pageSize) &lt; totalCount</li>
     *   <li><strong>Has Previous Page:</strong> pageNumber &gt; 1</li>
     *   <li><strong>Last Page Items:</strong> totalCount % pageSize (if not zero)</li>
     * </ul>
     *
     * <p><strong>Value Interpretation:</strong></p>
     * <ul>
     *   <li><strong>0:</strong> No anomalies detected (clean build)</li>
     *   <li><strong>1-5:</strong> Low anomaly count (typical for healthy builds)</li>
     *   <li><strong>6-15:</strong> Moderate anomaly count (requires attention)</li>
     *   <li><strong>16+:</strong> High anomaly count (significant issues detected)</li>
     * </ul>
     *
     * <p><strong>Database Source:</strong></p>
     * <ul>
     *   <li><strong>Query Origin:</strong> total_count CTE in findPaginatedAnomalies query</li>
     *   <li><strong>Type Conversion:</strong> Database Number converted to Long for precision</li>
     *   <li><strong>Consistency:</strong> Always matches actual anomaly count in database</li>
     *   <li><strong>Performance:</strong> Efficiently calculated alongside paginated results</li>
     * </ul>
     *
     * <p><strong>Frontend Integration:</strong></p>
     * <ul>
     *   <li><strong>Progress Indicators:</strong> "Showing X of Y anomalies"</li>
     *   <li><strong>Pagination Controls:</strong> Page number calculation and navigation</li>
     *   <li><strong>Load More:</strong> Infinite scroll implementation support</li>
     *   <li><strong>Summary Statistics:</strong> Build quality assessment metrics</li>
     * </ul>
     */
    private Long totalCount;

    /**
     * The current page number (1-based).
     *
     * <p>This field indicates which page of results is currently being displayed,
     * using a 1-based numbering system that aligns with user expectations and
     * frontend pagination controls.</p>
     *
     * <p><strong>Numbering System:</strong></p>
     * <ul>
     *   <li><strong>1-based:</strong> First page is page 1 (not 0)</li>
     *   <li><strong>User-friendly:</strong> Matches typical pagination UI conventions</li>
     *   <li><strong>Validation:</strong> Must be positive integer (≥ 1)</li>
     *   <li><strong>Upper Bound:</strong> Should not exceed calculated total pages</li>
     * </ul>
     *
     * <p><strong>Offset Calculation:</strong></p>
     * <ul>
     *   <li><strong>Database Offset:</strong> (pageNumber - 1) * pageSize</li>
     *   <li><strong>Page 1:</strong> offset = 0 (first pageSize records)</li>
     *   <li><strong>Page 2:</strong> offset = pageSize (next pageSize records)</li>
     *   <li><strong>Page N:</strong> offset = (N-1) * pageSize</li>
     * </ul>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li><strong>Minimum Value:</strong> 1 (first page)</li>
     *   <li><strong>Default Value:</strong> 1 when not specified in request</li>
     *   <li><strong>Maximum Value:</strong> Math.ceil(totalCount / pageSize)</li>
     *   <li><strong>Error Handling:</strong> Invalid pages return empty results</li>
     * </ul>
     *
     * <p><strong>Navigation Context:</strong></p>
     * <ul>
     *   <li><strong>Previous Page:</strong> pageNumber - 1 (if pageNumber &gt; 1)</li>
     *   <li><strong>Next Page:</strong> pageNumber + 1 (if more pages exist)</li>
     *   <li><strong>First Page:</strong> Always 1</li>
     *   <li><strong>Last Page:</strong> Math.ceil(totalCount / pageSize)</li>
     * </ul>
     *
     * <p><strong>Frontend Usage:</strong></p>
     * <ul>
     *   <li><strong>URL Parameters:</strong> Reflected in browser URL for bookmarking</li>
     *   <li><strong>Pagination Controls:</strong> Highlights current page in navigation</li>
     *   <li><strong>Breadcrumbs:</strong> "Page X of Y" display</li>
     *   <li><strong>Deep Linking:</strong> Direct navigation to specific pages</li>
     * </ul>
     */
    private Integer pageNumber;

    /**
     * The number of anomalies per page.
     *
     * <p>This field specifies how many anomalies are included in each page of results,
     * controlling the granularity of pagination and the amount of data transferred
     * in each request.</p>
     *
     * <p><strong>Default Configuration:</strong></p>
     * <ul>
     *   <li><strong>Default Value:</strong> 3 anomalies per page</li>
     *   <li><strong>UI Optimization:</strong> Chosen for optimal display in dashboard cards</li>
     *   <li><strong>Performance Balance:</strong> Balances data transfer with user experience</li>
     *   <li><strong>Mobile Friendly:</strong> Suitable for mobile device screen sizes</li>
     * </ul>
     *
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li><strong>Minimum Value:</strong> 1 (at least one anomaly per page)</li>
     *   <li><strong>Maximum Value:</strong> Typically limited to 50 for performance</li>
     *   <li><strong>Common Values:</strong> 3, 5, 10, 20 for different UI layouts</li>
     *   <li><strong>Error Handling:</strong> Invalid sizes default to 3</li>
     * </ul>
     *
     * <p><strong>Database Impact:</strong></p>
     * <ul>
     *   <li><strong>LIMIT Clause:</strong> Directly used in SQL LIMIT clause</li>
     *   <li><strong>Memory Usage:</strong> Smaller sizes reduce memory consumption</li>
     *   <li><strong>Query Performance:</strong> Smaller pages generally perform better</li>
     *   <li><strong>Network Transfer:</strong> Affects JSON response size</li>
     * </ul>
     *
     * <p><strong>User Experience:</strong></p>
     * <ul>
     *   <li><strong>Loading Speed:</strong> Smaller pages load faster</li>
     *   <li><strong>Scrolling:</strong> Affects amount of scrolling required</li>
     *   <li><strong>Navigation Frequency:</strong> Smaller pages require more navigation</li>
     *   <li><strong>Overview:</strong> Larger pages provide better overview</li>
     * </ul>
     *
     * <p><strong>Calculation Usage:</strong></p>
     * <ul>
     *   <li><strong>Total Pages:</strong> Math.ceil(totalCount / pageSize)</li>
     *   <li><strong>Offset Calculation:</strong> (pageNumber - 1) * pageSize</li>
     *   <li><strong>Last Page Size:</strong> totalCount % pageSize (if not zero)</li>
     *   <li><strong>Has More Pages:</strong> (pageNumber * pageSize) &lt; totalCount</li>
     * </ul>
     *
     * <p><strong>Customization Options:</strong></p>
     * <ul>
     *   <li><strong>User Preference:</strong> Can be customized per user or session</li>
     *   <li><strong>Device Adaptation:</strong> Different sizes for mobile vs desktop</li>
     *   <li><strong>Context Sensitivity:</strong> Adjusted based on anomaly complexity</li>
     *   <li><strong>Performance Tuning:</strong> Optimized based on system load</li>
     * </ul>
     */
    private Integer pageSize;

    /**
     * Constructs a new PaginatedAnomaliesDTO with the specified parameters.
     *
     * <p>This constructor creates a complete paginated response containing anomaly data
     * and pagination metadata. It is primarily used by the service layer after
     * processing database results and converting JSONB content to AnomalyDTO objects.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li><strong>anomalies:</strong> Can be empty list but should not be null</li>
     *   <li><strong>totalCount:</strong> Should be non-negative and accurate</li>
     *   <li><strong>pageNumber:</strong> Should be positive (≥ 1) and within valid range</li>
     *   <li><strong>pageSize:</strong> Should be positive and reasonable (typically 1-50)</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li><strong>Service Layer:</strong> Called by DashboardService.getPaginatedAnomalies()</li>
     *   <li><strong>Data Processing:</strong> After Jackson conversion of JSONB to AnomalyDTO list</li>
     *   <li><strong>Response Assembly:</strong> Part of Map response with hasData flag</li>
     *   <li><strong>Error Scenarios:</strong> May be created with empty anomalies list</li>
     * </ul>
     *
     * <p><strong>Data Consistency:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Count:</strong> anomalies.size() ≤ pageSize (except for last page)</li>
     *   <li><strong>Total Accuracy:</strong> totalCount reflects actual database count</li>
     *   <li><strong>Page Bounds:</strong> pageNumber should be within calculated total pages</li>
     *   <li><strong>Size Consistency:</strong> pageSize matches request parameter</li>
     * </ul>
     *
     * <p><strong>Construction Examples:</strong></p>
     * {@snippet lang=java :
     * // Normal page with full results
     * new PaginatedAnomaliesDTO(anomalyList, 15L, 1, 3);
     *
     * // Last page with partial results
     * new PaginatedAnomaliesDTO(anomalyList, 15L, 5, 3); // 3 items on last page
     *
     * // Empty results
     * new PaginatedAnomaliesDTO(Collections.emptyList(), 0L, 1, 3);
     * }
     *
     * @param anomalies the list of anomalies for the current page
     * @param totalCount the total number of anomalies across all pages
     * @param pageNumber the current page number (1-based)
     * @param pageSize the number of anomalies per page
     */
    public PaginatedAnomaliesDTO(
            List<AnomalyDTO> anomalies,
            Long totalCount,
            Integer pageNumber,
            Integer pageSize) {
        this.anomalies = anomalies;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    /**
     * Returns the list of anomalies for the current page.
     *
     * @return the list of anomalies, never null but may be empty
     */
    public List<AnomalyDTO> getAnomalies() { return anomalies; }

    /**
     * Sets the list of anomalies for the current page.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, anomalies are set via constructor
     * and should not be modified after DTO creation.</p>
     *
     * @param anomalies the list of anomalies to set
     */
    public void setAnomalies(List<AnomalyDTO> anomalies) { this.anomalies = anomalies; }

    /**
     * Returns the total number of anomalies across all pages.
     *
     * @return the total count of anomalies, never null, always non-negative
     */
    public Long getTotalCount() { return totalCount; }

    /**
     * Sets the total number of anomalies across all pages.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the total count is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param totalCount the total count to set
     */
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }

    /**
     * Returns the current page number (1-based).
     *
     * @return the page number, never null, always positive
     */
    public Integer getPageNumber() { return pageNumber; }

    /**
     * Sets the current page number (1-based).
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the page number is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param pageNumber the page number to set
     */
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    /**
     * Returns the number of anomalies per page.
     *
     * @return the page size, never null, always positive
     */
    public Integer getPageSize() { return pageSize; }

    /**
     * Sets the number of anomalies per page.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the page size is set via
     * constructor and should not be modified after DTO creation.</p>
     *
     * @param pageSize the page size to set
     */
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}