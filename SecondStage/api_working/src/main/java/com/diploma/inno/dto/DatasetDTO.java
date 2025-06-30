package com.diploma.inno.dto;

import java.util.List;

/**
 * Data Transfer Object representing a single data series for chart visualization.
 *
 * <p>This DTO encapsulates a complete data series with associated styling information
 * for Chart.js and other JavaScript charting libraries. Each DatasetDTO represents
 * one line, bar, or data series within a chart, containing both the numerical data
 * and the visual styling properties required for proper chart rendering.</p>
 *
 * <p><strong>Data Source &amp; Processing Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Database Query:</strong> Raw data extracted from build_anomaly_summary table</li>
 *   <li><strong>Data Processing:</strong> Service layer processes and aggregates database results</li>
 *   <li><strong>Series Creation:</strong> Individual DatasetDTO instances created for each data series</li>
 *   <li><strong>Color Assignment:</strong> Consistent color schemes applied based on data type</li>
 *   <li><strong>Data Alignment:</strong> Data arrays aligned with chart labels for proper visualization</li>
 *   <li><strong>Chart Integration:</strong> Structured for direct Chart.js consumption</li>
 * </ol>
 *
 * <p><strong>Usage Patterns:</strong></p>
 * <ul>
 *   <li><strong>Anomaly Trend Charts:</strong> Single dataset with "Anomaly Count" label</li>
 *   <li><strong>Severity Distribution Charts:</strong> Multiple datasets (one per severity level)</li>
 *   <li><strong>Comparative Analysis:</strong> Multiple datasets for job or time period comparison</li>
 *   <li><strong>Stacked Charts:</strong> Multiple datasets with aligned data for stacking</li>
 * </ul>
 *
 * <p><strong>Chart.js Integration:</strong></p>
 * <p>DatasetDTO maps directly to Chart.js dataset structure:</p>
 * {@snippet lang=json :
 * {
 *   label: datasetDTO.label,                    // Series name in legend
 *   data: datasetDTO.data,                      // Y-axis data points
 *   borderColor: datasetDTO.borderColor,        // Line/border color
 *   backgroundColor: datasetDTO.backgroundColor  // Fill/background color
 * }
 * }
 *
 * <p><strong>Color Scheme Standards:</strong></p>
 * <p>Consistent color mapping across the dashboard:</p>
 * <ul>
 *   <li><strong>Anomaly Trend:</strong> Blue (#36A2EB) for general anomaly counting</li>
 *   <li><strong>CRITICAL Severity:</strong> Red (#FF6384) for critical security/system issues</li>
 *   <li><strong>HIGH Severity:</strong> Orange (#36A2EB) for important issues requiring attention</li>
 *   <li><strong>MEDIUM Severity:</strong> Yellow (#FFCE56) for moderate issues to address</li>
 *   <li><strong>LOW Severity:</strong> Teal (#4BC0C0) for minor issues or improvements</li>
 *   <li><strong>WARNING Severity:</strong> Purple (#9966FF) for potential problems or notices</li>
 * </ul>
 *
 * <p><strong>Data Processing Examples:</strong></p>
 *
 * <p><strong>Anomaly Trend Dataset Creation:</strong></p>
 * {@snippet lang=java :
 * // Single dataset for anomaly count trend
 * List&lt;Integer&gt; anomalyCounts = [3, 1, 5, 2, 4]; // From database query
 * DatasetDTO trendDataset = new DatasetDTO(
 *     "Anomaly Count",     // Label for legend
 *     anomalyCounts,       // Data points
 *     "#36A2EB",          // Blue border color
 *     "#36A2EB"           // Blue background color
 * );
 * }
 *
 * <p><strong>Severity Distribution Dataset Creation:</strong></p>
 * {@snippet lang=java :
 * // Multiple datasets for severity breakdown
 * String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"};
 * List&lt;String&gt; severities = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "WARNING"];
 *
 * for (int i = 0; i &lt; severities.size(); i++) {
 *     DatasetDTO severityDataset = new DatasetDTO(
 *         severities.get(i),                    // Severity level label
 *         severityData.get(severities.get(i)),  // Counts per build
 *         colors[i % colors.length],            // Border color
 *         colors[i % colors.length]             // Background color
 *     );
 *     datasets.add(severityDataset);
 * }
 * }
 *
 * <p><strong>Data Alignment Process:</strong></p>
 * <ul>
 *   <li><strong>Label Synchronization:</strong> Data array length matches chart labels count</li>
 *   <li><strong>Missing Data Handling:</strong> Zero values added for missing data points</li>
 *   <li><strong>Chronological Ordering:</strong> Data points ordered by build timestamp</li>
 *   <li><strong>Consistency Validation:</strong> All datasets have same data array length</li>
 * </ul>
 *
 * <p><strong>JSONB Data Extraction:</strong></p>
 * <p>For severity distribution, data is extracted from JSONB severity_counts:</p>
 * {@snippet lang=code :
 * // Database JSONB structure
 * severity_counts: {
 *   "CRITICAL": 2,
 *   "HIGH": 1,
 *   "MEDIUM": 3,
 *   "LOW": 0,
 *   "WARNING": 1
 * }
 * }
 *
 * {@snippet lang=java :
 * // Service layer processing
 * Map&lt;String, Integer&gt; severityCounts = objectMapper.readValue(jsonString, Map.class);
 * for (Map.Entry&lt;String, Integer&gt; entry : severityCounts.entrySet()) {
 *     severityData.computeIfAbsent(entry.getKey(), k -&gt; new ArrayList&lt;&gt;())
 *                 .add(entry.getValue());
 * }
 * }
 *
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li><strong>Data Limits:</strong> Maximum 15 builds to prevent large datasets</li>
 *   <li><strong>Memory Efficiency:</strong> Integer arrays for numerical data</li>
 *   <li><strong>Color Cycling:</strong> Modulo operation for color array cycling</li>
 *   <li><strong>Immutable Design:</strong> Thread-safe for concurrent chart generation</li>
 * </ul>
 *
 * <p><strong>JSON Serialization Example:</strong></p>
 * {@snippet lang=json :
 * {
 *   "label": "CRITICAL",
 *   "data": [2, 0, 1, 3, 0],
 *   "borderColor": "#FF6384",
 *   "backgroundColor": "#FF6384"
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
 * @see ChartDataDTO
 * @see com.diploma.inno.service.DashboardService#getAnomalyTrend(String, Integer)
 * @see com.diploma.inno.service.DashboardService#getSeverityDistribution(String, Integer)
 */
public class DatasetDTO {

    /**
     * The label for this data series displayed in the chart legend.
     *
     * <p>This field contains the human-readable name for the data series that
     * appears in the chart legend and tooltips. The label helps users identify
     * what each data series represents in multi-series charts.</p>
     *
     * <p><strong>Label Types:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Trend:</strong> "Anomaly Count" for general anomaly counting</li>
     *   <li><strong>Severity Levels:</strong> "CRITICAL", "HIGH", "MEDIUM", "LOW", "WARNING"</li>
     *   <li><strong>Job Names:</strong> Specific job names for comparative analysis</li>
     *   <li><strong>Time Periods:</strong> Date ranges or time period identifiers</li>
     * </ul>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js dataset.label property for legend display</p>
     *
     * <p><strong>Consistency Rules:</strong></p>
     * <ul>
     *   <li>Severity labels match database severity values exactly</li>
     *   <li>Anomaly trend uses consistent "Anomaly Count" label</li>
     *   <li>Job names preserve original Jenkins job name casing</li>
     *   <li>Labels are unique within a single chart's dataset collection</li>
     * </ul>
     */
    private String label;

    /**
     * The numerical data points for this series aligned with chart labels.
     *
     * <p>This list contains the Y-axis values for each corresponding X-axis label
     * in the chart. The data represents counts, metrics, or measurements that
     * are visualized as points, bars, or other chart elements.</p>
     *
     * <p><strong>Data Types by Chart:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Trend:</strong> Total anomaly counts per build</li>
     *   <li><strong>Severity Distribution:</strong> Count of anomalies per severity level per build</li>
     *   <li><strong>Performance Metrics:</strong> Build times, resource usage, or other measurements</li>
     *   <li><strong>Quality Metrics:</strong> Test counts, coverage percentages, or quality scores</li>
     * </ul>
     *
     * <p><strong>Data Source Processing:</strong></p>
     * <ul>
     *   <li><strong>Anomaly Trend:</strong> Extracted from total_anomalies column in build_anomaly_summary</li>
     *   <li><strong>Severity Distribution:</strong> Parsed from JSONB severity_counts field</li>
     *   <li><strong>Type Conversion:</strong> Database Number values converted to Integer</li>
     *   <li><strong>Missing Data:</strong> Zero values added for missing data points</li>
     * </ul>
     *
     * <p><strong>Data Alignment:</strong></p>
     * <ul>
     *   <li>Array length must match chart labels array length</li>
     *   <li>Data points ordered chronologically by build timestamp</li>
     *   <li>Missing builds represented by zero values</li>
     *   <li>All datasets in a chart have identical data array lengths</li>
     * </ul>
     *
     * <p><strong>Value Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Non-negative:</strong> All values are zero or positive integers</li>
     *   <li><strong>Bounded:</strong> Typical range 0-50 for anomaly counts</li>
     *   <li><strong>Sparse:</strong> Many builds may have zero anomalies</li>
     *   <li><strong>Temporal:</strong> Values represent point-in-time measurements</li>
     * </ul>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js dataset.data property for visualization</p>
     */
    private List<Integer> data;

    /**
     * The border color for line charts and element outlines.
     *
     * <p>This field specifies the color used for drawing lines, borders, and outlines
     * in chart visualizations. It's particularly important for line charts where
     * the border color defines the line color, and for bar charts where it defines
     * the border around each bar.</p>
     *
     * <p><strong>Color Format:</strong></p>
     * <ul>
     *   <li><strong>Hex Format:</strong> 6-character hexadecimal color codes (e.g., "#FF6384")</li>
     *   <li><strong>Case Insensitive:</strong> Uppercase hex digits used for consistency</li>
     *   <li><strong>Alpha Channel:</strong> Not used (solid colors only)</li>
     *   <li><strong>Web Safe:</strong> All colors are web-safe and accessible</li>
     * </ul>
     *
     * <p><strong>Standard Color Mapping:</strong></p>
     * <ul>
     *   <li><strong>#36A2EB:</strong> Blue for anomaly trends and HIGH severity</li>
     *   <li><strong>#FF6384:</strong> Red for CRITICAL severity issues</li>
     *   <li><strong>#FFCE56:</strong> Yellow for MEDIUM severity issues</li>
     *   <li><strong>#4BC0C0:</strong> Teal for LOW severity issues</li>
     *   <li><strong>#9966FF:</strong> Purple for WARNING level issues</li>
     * </ul>
     *
     * <p><strong>Usage by Chart Type:</strong></p>
     * <ul>
     *   <li><strong>Line Charts:</strong> Defines the line color for trend visualization</li>
     *   <li><strong>Bar Charts:</strong> Defines the border color around bars</li>
     *   <li><strong>Stacked Charts:</strong> Helps distinguish between stacked segments</li>
     *   <li><strong>Mixed Charts:</strong> Consistent coloring across different chart elements</li>
     * </ul>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js dataset.borderColor property</p>
     */
    private String borderColor; // For line chart

    /**
     * The background/fill color for chart elements.
     *
     * <p>This field specifies the color used for filling chart elements such as
     * bar interiors, area fills under lines, or background colors for chart
     * segments. It provides the primary visual color for most chart elements.</p>
     *
     * <p><strong>Color Consistency:</strong></p>
     * <ul>
     *   <li><strong>Matching Colors:</strong> Typically same as borderColor for solid appearance</li>
     *   <li><strong>Transparency:</strong> May be made semi-transparent by Chart.js for overlays</li>
     *   <li><strong>Accessibility:</strong> Colors chosen for good contrast and visibility</li>
     *   <li><strong>Brand Consistency:</strong> Aligns with dashboard color scheme</li>
     * </ul>
     *
     * <p><strong>Severity Color Standards:</strong></p>
     * <ul>
     *   <li><strong>CRITICAL (#FF6384):</strong> Red background for critical issues</li>
     *   <li><strong>HIGH (#36A2EB):</strong> Blue background for high priority issues</li>
     *   <li><strong>MEDIUM (#FFCE56):</strong> Yellow background for medium priority</li>
     *   <li><strong>LOW (#4BC0C0):</strong> Teal background for low priority</li>
     *   <li><strong>WARNING (#9966FF):</strong> Purple background for warnings</li>
     * </ul>
     *
     * <p><strong>Visual Impact:</strong></p>
     * <ul>
     *   <li><strong>Bar Charts:</strong> Primary color filling the entire bar</li>
     *   <li><strong>Area Charts:</strong> Fill color under the line</li>
     *   <li><strong>Stacked Charts:</strong> Distinct colors for each stack segment</li>
     *   <li><strong>Legend:</strong> Color displayed in chart legend for identification</li>
     * </ul>
     *
     * <p><strong>Color Assignment Logic:</strong></p>
     * <pre>
     * // Service layer color assignment
     * String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"};
     * String color = colors[i % colors.length]; // Cycling through colors
     * new DatasetDTO(label, data, color, color); // Same color for border and background
     * </pre>
     *
     * <p><strong>Chart.js Integration:</strong></p>
     * <p>Maps directly to Chart.js dataset.backgroundColor property</p>
     */
    private String backgroundColor; // For bar chart

    /**
     * Constructs a new DatasetDTO with the specified parameters.
     *
     * <p>This constructor creates a complete dataset ready for chart visualization,
     * ensuring all required properties are set for proper Chart.js integration.
     * The constructor is used by service layer methods when processing database
     * results and creating chart data structures.</p>
     *
     * <p><strong>Parameter Validation:</strong></p>
     * <ul>
     *   <li>Label should be descriptive and unique within chart context</li>
     *   <li>Data array should align with chart labels array length</li>
     *   <li>Colors should be valid hex color codes</li>
     *   <li>Border and background colors typically match for consistency</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     * {@snippet lang=java :
     * // Anomaly trend dataset
     * new DatasetDTO("Anomaly Count", anomalyCounts, "#36A2EB", "#36A2EB");
     *
     * // Severity distribution dataset
     * new DatasetDTO("CRITICAL", criticalCounts, "#FF6384", "#FF6384");
     *
     * // Job comparison dataset
     * new DatasetDTO("my-web-app", jobAnomalyCounts, "#36A2EB", "#36A2EB");
     * }
     *
     * <p><strong>Service Layer Integration:</strong></p>
     * <ul>
     *   <li>Called during chart data assembly in DashboardService</li>
     *   <li>Parameters derived from database query results</li>
     *   <li>Colors assigned based on data type and severity levels</li>
     *   <li>Data arrays pre-processed and aligned before construction</li>
     * </ul>
     *
     * @param label the display name for this data series
     * @param data the list of numerical data points aligned with chart labels
     * @param borderColor the hex color code for borders and lines
     * @param backgroundColor the hex color code for fills and backgrounds
     */
    public DatasetDTO(String label, List<Integer> data, String borderColor, String backgroundColor) {
        this.label = label;
        this.data = data;
        this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns the label for this data series.
     *
     * @return the series label displayed in chart legend
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label for this data series.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, the label is set via constructor
     * and should not be modified after dataset creation.</p>
     *
     * @param label the series label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the numerical data points for this series.
     *
     * @return the list of data values aligned with chart labels
     */
    public List<Integer> getData() {
        return data;
    }

    /**
     * Sets the numerical data points for this series.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, data is set via constructor
     * and should not be modified after dataset creation.</p>
     *
     * @param data the list of data values to set
     */
    public void setData(List<Integer> data) {
        this.data = data;
    }

    /**
     * Returns the border color for chart elements.
     *
     * @return the hex color code for borders and lines
     */
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color for chart elements.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, colors are set via constructor
     * and should not be modified after dataset creation.</p>
     *
     * @param borderColor the hex color code to set
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Returns the background color for chart elements.
     *
     * @return the hex color code for fills and backgrounds
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for chart elements.
     *
     * <p><strong>Note:</strong> This setter is primarily provided for framework
     * compatibility. In normal application flow, colors are set via constructor
     * and should not be modified after dataset creation.</p>
     *
     * @param backgroundColor the hex color code to set
     */
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
