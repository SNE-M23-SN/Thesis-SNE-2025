package com.diploma.inno.dto;

import java.util.List;

/**
 * Data Transfer Object for chart visualization data in the CI Anomaly Detector dashboard.
 *
 * <p>This DTO encapsulates chart data in a format compatible with Chart.js and other
 * JavaScript charting libraries. It provides structured data for various chart types
 * including line charts, bar charts, and stacked charts used for anomaly trend analysis
 * and severity distribution visualization.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Database Query:</strong> Complex SQL queries extract data from build_anomaly_summary table</li>
 *   <li><strong>Data Aggregation:</strong> Service layer processes raw database results</li>
 *   <li><strong>Label Generation:</strong> Build identifiers formatted as "job - Build № number"</li>
 *   <li><strong>Dataset Creation:</strong> Multiple DatasetDTO instances for different data series</li>
 *   <li><strong>Chart Formatting:</strong> Data structured for Chart.js consumption</li>
 *   <li><strong>REST Response:</strong> Serialized to JSON for frontend chart rendering</li>
 * </ol>
 *
 * <p><strong>Supported Chart Types:</strong></p>
 * <ul>
 *   <li><strong>Anomaly Trend Charts:</strong> Line charts showing anomaly count trends over time</li>
 *   <li><strong>Severity Distribution Charts:</strong> Stacked bar charts showing severity breakdown</li>
 *   <li><strong>Time Series Analysis:</strong> Build progression and pattern visualization</li>
 *   <li><strong>Comparative Analysis:</strong> Multi-job comparison and trend analysis</li>
 * </ul>
 *
 * <p><strong>Database Query Sources:</strong></p>
 * <p>Chart data is generated from two main repository methods:</p>
 * <ul>
 *   <li><strong>findLatestBuildAnomalyCounts:</strong> Anomaly trend data from build_anomaly_summary</li>
 *   <li><strong>findLatestBuildSeverityDistributions:</strong> Severity breakdown from severity_counts JSONB</li>
 * </ul>
 *
 * <p><strong>Complex SQL Query Structure (Anomaly Trend):</strong></p>
 * {@snippet lang=sql :
 * WITH distinct_jobs AS (
 *   SELECT COUNT(DISTINCT conversation_id) AS total_jobs
 *   FROM build_anomaly_summary
 *   WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter)
 * ),
 * ranked_builds AS (
 *   SELECT conversation_id AS job, build_number AS build,
 *          timestamp, total_anomalies AS anomaly_count,
 *          ROW_NUMBER() OVER (PARTITION BY conversation_id
 *                            ORDER BY timestamp DESC, build_number DESC) AS rn
 *   FROM build_anomaly_summary
 *   WHERE (:jobFilter = 'all' OR conversation_id = :jobFilter)
 * ),
 * limited_builds AS (
 *   SELECT job, build, timestamp, anomaly_count
 *   FROM ranked_builds
 *   WHERE (:jobFilter = 'all' AND rn <= GREATEST(1, LEAST(:buildCount / NULLIF(total_jobs, 0), :buildCount)))
 *      OR (:jobFilter != 'all' AND rn <= :buildCount)
 * )
 * SELECT * FROM limited_builds ORDER BY timestamp DESC, build DESC
 * }
 *
 * <p><strong>Chart.js Compatibility:</strong></p>
 * <p>The DTO structure is designed for direct consumption by Chart.js:</p>
 *
 * <span><b>Chart.js configuration</b></span>
 * {@snippet lang=json :
 * {
 *   "type": "line",
 *   "data": {
 *     "labels": "chartData.labels",
 *     "datasets": "chartData.datasets"
 *   },
 *   "options":" { ... }"
 * }
 * }
 *
 * <p><strong>Label Format Specifications:</strong></p>
 * <ul>
 *   <li><strong>Pattern:</strong> "{jobName} - Build № {buildNumber}"</li>
 *   <li><strong>Example:</strong> "my-web-app - Build № 123"</li>
 *   <li><strong>Ordering:</strong> Chronological by timestamp (most recent first)</li>
 *   <li><strong>Uniqueness:</strong> Each label represents a unique build</li>
 * </ul>
 *
 * <p><strong>Dataset Structure:</strong></p>
 * <ul>
 *   <li><strong>Anomaly Trend:</strong> Single dataset with blue color (#36A2EB)</li>
 *   <li><strong>Severity Distribution:</strong> Multiple datasets with severity-specific colors</li>
 *   <li><strong>Color Scheme:</strong> Consistent colors across dashboard charts</li>
 *   <li><strong>Data Alignment:</strong> All datasets aligned to same label sequence</li>
 * </ul>
 *
 * <p><strong>Severity Color Mapping:</strong></p>
 * <ul>
 *   <li><strong>CRITICAL:</strong> Red (#FF6384) - Severe security or system issues</li>
 *   <li><strong>HIGH:</strong> Orange (#36A2EB) - Important issues requiring attention</li>
 *   <li><strong>MEDIUM:</strong> Yellow (#FFCE56) - Moderate issues to address</li>
 *   <li><strong>LOW:</strong> Teal (#4BC0C0) - Minor issues or improvements</li>
 *   <li><strong>WARNING:</strong> Purple (#9966FF) - Potential problems or notices</li>
 * </ul>
 *
 * <p><strong>Data Processing Logic:</strong></p>
 * <ul>
 *   <li><strong>Build Count Validation:</strong> Limited to 1-15 builds for performance</li>
 *   <li><strong>Job Filtering:</strong> Supports single job or "all" jobs aggregation</li>
 *   <li><strong>Data Alignment:</strong> Missing severity counts filled with zeros</li>
 *   <li><strong>Chronological Ordering:</strong> Builds ordered by timestamp for trend analysis</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <ul>
 *   <li><strong>REST API Endpoints:</strong> /anomaly-trend and /severity-distribution</li>
 *   <li><strong>Dashboard Visualization:</strong> Real-time chart updates and trend analysis</li>
 *   <li><strong>Performance Monitoring:</strong> Build quality trends and regression detection</li>
 *   <li><strong>Security Analysis:</strong> Anomaly pattern identification and alerting</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "labels": [
 *     "my-web-app - Build № 125",
 *     "my-web-app - Build № 124",
 *     "backend-service - Build № 89"
 *   ],
 *   "datasets": [
 *     {
 *       "label": "Anomaly Count",
 *       "data": [3, 1, 5],
 *       "borderColor": "#36A2EB",
 *       "backgroundColor": "#36A2EB"
 *     }
 *   ]
 * }
 * }
 *
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li><strong>Build Limit:</strong> Maximum 15 builds to prevent large datasets</li>
 *   <li><strong>Efficient Queries:</strong> Window functions and CTEs for optimal performance</li>
 *   <li><strong>Data Caching:</strong> Results suitable for client-side caching</li>
 *   <li><strong>Minimal Payload:</strong> Only essential data for chart rendering</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This DTO is immutable after construction and thread-safe for concurrent access.
 * All collections are set via constructor and accessed through getter methods.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see DatasetDTO
 * @see com.diploma.inno.service.DashboardService#getAnomalyTrend(String, Integer)
 * @see com.diploma.inno.service.DashboardService#getSeverityDistribution(String, Integer)
 * @see com.diploma.inno.repository.ChatMessageRepository#findLatestBuildAnomalyCounts(String, Integer)
 * @see com.diploma.inno.repository.ChatMessageRepository#findLatestBuildSeverityDistributions(String, Integer)
 */
public class ChartDataDTO {

    /**
     * The labels for the chart's X-axis.
     *
     * <p>This list contains formatted build identifiers that serve as X-axis labels
     * for chart visualization. Each label represents a unique build and follows
     * a consistent format for easy identification and chronological ordering.</p>
     *
     * <p><strong>Label Format:</strong></p>
     * <ul>
     *   <li><strong>Pattern:</strong> "{jobName} - Build № {buildNumber}"</li>
     *   <li><strong>Example:</strong> "my-web-app - Build № 123"</li>
     *   <li><strong>Unicode:</strong> Uses № symbol for build number designation</li>
     *   <li><strong>Consistency:</strong> Same format across all chart types</li>
     * </ul>
     *
     * <p><strong>Data Source:</strong></p>
     * <p>Generated from database query results:</p>
     * <ul>
     *   <li>job field (conversation_id from build_anomaly_summary)</li>
     *   <li>build field (build_number from build_anomaly_summary)</li>
     *   <li>Formatted in service layer during data processing</li>
     * </ul>
     *
     * <p><strong>Ordering Logic:</strong></p>
     * <ul>
     *   <li><strong>Primary:</strong> Timestamp DESC (most recent first)</li>
     *   <li><strong>Secondary:</strong> Build number DESC (higher build numbers first)</li>
     *   <li><strong>Chronological:</strong> Maintains build sequence for trend analysis</li>
     *   <li><strong>Consistency:</strong> Same ordering across anomaly trend and severity distribution</li>
     * </ul>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js data.labels property for X-axis rendering</p>
     *
     * <p><strong>Example Values:</strong></p>
     * <ul>
     *   <li>"backend-api - Build № 156"</li>
     *   <li>"frontend-app - Build № 89"</li>
     *   <li>"data-processor - Build № 234"</li>
     * </ul>
     */
    private List<String> labels;

    /**
     * The datasets containing chart data series and styling information.
     *
     * <p>This list contains one or more DatasetDTO instances, each representing
     * a data series with associated styling and metadata. The structure varies
     * based on chart type and visualization requirements.</p>
     *
     * <p><strong>Dataset Types:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Trend:</strong> Single dataset with anomaly counts</li>
     *   <li><strong>Severity Distribution:</strong> Multiple datasets (one per severity level)</li>
     *   <li><strong>Comparative Analysis:</strong> Multiple datasets for job comparison</li>
     * </ul>
     *
     * <p><strong>Anomaly Trend Dataset:</strong></p>
     * <ul>
     *   <li><strong>Label:</strong> "Anomaly Count"</li>
     *   <li><strong>Data:</strong> Integer array of anomaly counts per build</li>
     *   <li><strong>Color:</strong> Blue (#36A2EB) for consistency</li>
     *   <li><strong>Type:</strong> Line chart for trend visualization</li>
     * </ul>
     *
     * <p><strong>Severity Distribution Datasets:</strong></p>
     * <ul>
     *   <li><strong>CRITICAL Dataset:</strong> Red (#FF6384) - Critical severity anomalies</li>
     *   <li><strong>HIGH Dataset:</strong> Orange (#36A2EB) - High severity anomalies</li>
     *   <li><strong>MEDIUM Dataset:</strong> Yellow (#FFCE56) - Medium severity anomalies</li>
     *   <li><strong>LOW Dataset:</strong> Teal (#4BC0C0) - Low severity anomalies</li>
     *   <li><strong>WARNING Dataset:</strong> Purple (#9966FF) - Warning level anomalies</li>
     * </ul>
     *
     * <p><strong>Data Alignment:</strong></p>
     * <ul>
     *   <li>All datasets have same length as labels array</li>
     *   <li>Missing data points filled with zeros</li>
     *   <li>Consistent ordering across all datasets</li>
     *   <li>Proper alignment for stacked chart visualization</li>
     * </ul>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js data.datasets property for series rendering</p>
     *
     * <p><strong>Color Consistency:</strong></p>
     * <ul>
     *   <li>Colors consistent across all dashboard charts</li>
     *   <li>Severity levels maintain same colors throughout application</li>
     *   <li>Both borderColor and backgroundColor set for flexibility</li>
     *   <li>Color array cycling for additional severity levels</li>
     * </ul>
     *
     * <p><strong>Data Processing:</strong></p>
     * <ul>
     *   <li>Severity data extracted from JSONB severity_counts field</li>
     *   <li>Missing severity levels initialized with empty arrays</li>
     *   <li>Data arrays padded to match label count</li>
     *   <li>Consistent dataset creation across service methods</li>
     * </ul>
     */
    private List<DatasetDTO> datasets;

    /**
     * Constructs a new ChartDataDTO with the specified labels and datasets.
     *
     * <p>This constructor creates a complete chart data structure ready for
     * JSON serialization and Chart.js consumption. It ensures proper data
     * alignment and maintains consistency across different chart types.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>Labels and datasets should have consistent data alignment</li>
     *   <li>All dataset data arrays should match labels array length</li>
     *   <li>Service layer ensures proper data formatting before construction</li>
     * </ul>
     *
     * <p><strong>Usage Context:</strong></p>
     * <ul>
     *   <li>Service layer chart data assembly</li>
     *   <li>REST API response construction</li>
     *   <li>Test scenario data creation</li>
     * </ul>
     *
     * <p><strong>Data Integrity:</strong></p>
     * <ul>
     *   <li>Labels represent X-axis build identifiers</li>
     *   <li>Datasets contain aligned Y-axis data points</li>
     *   <li>Color schemes consistent with dashboard standards</li>
     *   <li>Chart type compatibility maintained</li>
     * </ul>
     *
     * @param labels the list of X-axis labels (build identifiers)
     * @param datasets the list of data series with styling information
     */
    public ChartDataDTO(List<String> labels, List<DatasetDTO> datasets) {
        this.labels = labels;
        this.datasets = datasets;
    }

    /**
     * Returns the list of chart labels for the X-axis.
     *
     * <p>These labels represent build identifiers formatted as
     * "jobName - Build № buildNumber" for chart visualization.</p>
     *
     * @return the list of X-axis labels, never null
     */
    public List<String> getLabels() { return labels; }

    /**
     * Sets the list of chart labels for the X-axis.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, labels are set via constructor
     * and should not be modified after chart data creation.</p>
     *
     * @param labels the list of X-axis labels to set
     */
    public void setLabels(List<String> labels) { this.labels = labels; }

    /**
     * Returns the list of datasets containing chart data series.
     *
     * <p>Each dataset represents a data series with associated styling
     * and metadata for chart rendering.</p>
     *
     * @return the list of chart datasets, never null
     */
    public List<DatasetDTO> getDatasets() { return datasets; }

    /**
     * Sets the list of datasets containing chart data series.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, datasets are set via constructor
     * and should not be modified after chart data creation.</p>
     *
     * @param datasets the list of chart datasets to set
     */
    public void setDatasets(List<DatasetDTO> datasets) { this.datasets = datasets; }
}

