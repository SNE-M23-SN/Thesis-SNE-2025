package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

/**
 * Data Transfer Object for Static Application Security Testing (SAST) scan results in CI/CD security analysis.
 *
 * <p>This DTO encapsulates comprehensive SAST scanning results including vulnerability findings, scan metadata,
 * performance metrics, and error information for Jenkins builds. It serves as a critical component for tracking
 * security scan outcomes, identifying vulnerabilities, and enabling AI-powered analysis of security posture
 * across CI/CD pipelines.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Vulnerability Tracking:</strong> Captures detailed security scan results &amp; findings</li>
 *   <li><strong>Tool Integration:</strong> Supports multiple SAST tools (SonarQube, Checkmarx, Veracode, etc.)</li>
 *   <li><strong>Performance Monitoring:</strong> Tracks scan duration &amp; performance metrics</li>
 *   <li><strong>Repository Context:</strong> Links scan results to specific repositories &amp; branches</li>
 *   <li><strong>Compression Support:</strong> Handles GZIP-compressed scan results for large reports</li>
 *   <li><strong>Error Handling:</strong> Comprehensive error tracking &amp; diagnostic information</li>
 * </ul>
 *
 * <h2>SAST Tool Integration</h2>
 * <p>The DTO supports integration with various Static Application Security Testing tools:</p>
 * <ul>
 *   <li><strong>SonarQube:</strong> Code quality &amp; security vulnerability analysis</li>
 *   <li><strong>Checkmarx:</strong> Static code analysis for security vulnerabilities</li>
 *   <li><strong>Veracode:</strong> Application security testing &amp; compliance</li>
 *   <li><strong>Fortify:</strong> Static application security testing</li>
 *   <li><strong>CodeQL:</strong> Semantic code analysis for security issues</li>
 *   <li><strong>Semgrep:</strong> Static analysis for finding bugs &amp; security issues</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "sast_scanning",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "repoUrl": "https://github.com/company/security-app.git",
 *   "branch": "main",
 *   "tool": "SonarQube",
 *   "status": "SUCCESS",
 *   "scanDurationSeconds": 45.7,
 *   "scanResult": "{\"vulnerabilities\":[{\"severity\":\"HIGH\",\"type\":\"SQL_INJECTION\",\"file\":\"UserController.java\",\"line\":42}]}",
 *   "error": null,
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 * }
 *
 * <h2>Compressed Scan Results</h2>
 * <p>For large scan reports, the DTO supports GZIP compression:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "sast_scanning",
 *   "job_name": "enterprise-security-scan",
 *   "build_number": 456,
 *   "repoUrl": "https://github.com/company/large-enterprise-app.git",
 *   "branch": "develop",
 *   "tool": "Checkmarx",
 *   "status": "SUCCESS",
 *   "scanDurationSeconds": 180.5,
 *   "scanResult": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
 *   "error": null
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When scan errors occur, the DTO captures detailed diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "sast_scanning",
 *   "job_name": "failing-security-scan",
 *   "build_number": 789,
 *   "repoUrl": "https://github.com/company/problematic-app.git",
 *   "branch": "feature/security-fix",
 *   "tool": "SonarQube",
 *   "status": "FAILURE",
 *   "scanDurationSeconds": 12.3,
 *   "scanResult": null,
 *   "error": "Authentication failed: Invalid SonarQube token"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered security analysis:</p>
 * <ul>
 *   <li><strong>Vulnerability Assessment:</strong> Analysis of security findings &amp; risk levels</li>
 *   <li><strong>Trend Analysis:</strong> Historical vulnerability tracking &amp; improvement trends</li>
 *   <li><strong>Risk Prioritization:</strong> AI-driven vulnerability prioritization &amp; remediation guidance</li>
 *   <li><strong>Compliance Monitoring:</strong> Automated compliance checking &amp; reporting</li>
 *   <li><strong>False Positive Detection:</strong> Machine learning-based false positive identification</li>
 * </ul>
 *
 * <h2>Security Metrics &amp; KPIs</h2>
 * <p>The DTO enables comprehensive security metrics tracking:</p>
 * <ul>
 *   <li><strong>Vulnerability Counts:</strong> Critical, High, Medium, Low severity counts</li>
 *   <li><strong>Scan Performance:</strong> Duration trends &amp; performance optimization</li>
 *   <li><strong>Tool Effectiveness:</strong> Comparative analysis across different SAST tools</li>
 *   <li><strong>Remediation Tracking:</strong> Time-to-fix metrics &amp; remediation effectiveness</li>
 * </ul>
 *
 * <h2>Repository &amp; Branch Context</h2>
 * <p>The DTO maintains crucial repository context for security tracking:</p>
 * <ul>
 *   <li><strong>Repository Identification:</strong> Full repository URL for source tracking</li>
 *   <li><strong>Branch Correlation:</strong> Branch-specific security analysis &amp; comparison</li>
 *   <li><strong>Commit Correlation:</strong> Links security findings to specific code changes</li>
 *   <li><strong>Multi-Repository Support:</strong> Handles complex multi-repo security scanning</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>Compression Support:</strong> GZIP compression for large scan reports</li>
 *   <li><strong>Lazy Decompression:</strong> Decompresses content only when needed for analysis</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized handling of large security reports</li>
 *   <li><strong>Streaming Processing:</strong> Efficient processing of continuous scan results</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes SAST scan result messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores scan results for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides scan data for security analysis</li>
 *   <li><strong>Security Dashboard:</strong> Supplies real-time security metrics &amp; trends</li>
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
public class ScanResult extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(ScanResult.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this SAST scan result.
     * <p>Used for correlating scan results with specific Jenkins jobs and enabling
     * conversation-based grouping in the AI analysis pipeline.</p>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "security-pipeline"} - Dedicated security scanning pipeline</li>
     *   <li>{@code "main-branch-scan"} - Main branch security validation</li>
     *   <li>{@code "pr-security-check"} - Pull request security verification</li>
     * </ul>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this SAST scan result.
     * <p>Enables tracking of security scan results across different build executions
     * and correlation with build outcomes for trend analysis.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // REPOSITORY &amp; SOURCE CONTEXT
    // ========================================================================

    /**
     * Repository URL for the scanned codebase.
     * <p>Provides the complete repository URL that was scanned, enabling source code
     * correlation and multi-repository security tracking.</p>
     *
     * <h4>Supported Repository Types:</h4>
     * <ul>
     *   <li><strong>GitHub:</strong> {@code https://github.com/company/security-app.git}</li>
     *   <li><strong>GitLab:</strong> {@code https://gitlab.com/company/security-app.git}</li>
     *   <li><strong>Bitbucket:</strong> {@code https://bitbucket.org/company/security-app.git}</li>
     *   <li><strong>Azure DevOps:</strong> {@code https://dev.azure.com/company/project/_git/security-app}</li>
     * </ul>
     */
    @JsonProperty("repoUrl")
    private String repoUrl;

    /**
     * Git branch that was scanned for security vulnerabilities.
     * <p>Identifies the specific branch analyzed, enabling branch-specific security
     * tracking and comparison across different development streams.</p>
     *
     * <h4>Common Branch Patterns:</h4>
     * <ul>
     *   <li><strong>main/master:</strong> Production branch security validation</li>
     *   <li><strong>develop:</strong> Development branch continuous security scanning</li>
     *   <li><strong>feature/*:</strong> Feature branch security verification</li>
     *   <li><strong>release/*:</strong> Release candidate security validation</li>
     * </ul>
     */
    @JsonProperty("branch")
    private String branch;

    // ========================================================================
    // SCAN EXECUTION &amp; RESULTS
    // ========================================================================

    /**
     * SAST tool used for the security scan.
     * <p>Identifies the specific Static Application Security Testing tool that performed
     * the scan, enabling tool-specific result processing and comparative analysis.</p>
     *
     * <h4>Supported SAST Tools:</h4>
     * <ul>
     *   <li><strong>SonarQube:</strong> Code quality &amp; security analysis platform</li>
     *   <li><strong>Checkmarx:</strong> Static application security testing solution</li>
     *   <li><strong>Veracode:</strong> Application security testing platform</li>
     *   <li><strong>Fortify:</strong> Static application security testing</li>
     *   <li><strong>CodeQL:</strong> Semantic code analysis engine</li>
     *   <li><strong>Semgrep:</strong> Static analysis for finding bugs &amp; security issues</li>
     * </ul>
     */
    @JsonProperty("tool")
    private String tool;

    /**
     * Comprehensive SAST scan results in JSON or compressed format.
     * <p>Contains the complete security scan findings including vulnerabilities,
     * code quality issues, and detailed analysis results. May be GZIP-compressed
     * and Base64-encoded for large reports.</p>
     *
     * <h4>Result Format Examples:</h4>
     * <p>Uncompressed JSON results:</p>
     * {@snippet lang="json" :
     * {
     *   "vulnerabilities": [
     *     {
     *       "severity": "HIGH",
     *       "type": "SQL_INJECTION",
     *       "file": "src/main/java/UserController.java",
     *       "line": 42,
     *       "description": "Potential SQL injection vulnerability",
     *       "cwe": "CWE-89"
     *     },
     *     {
     *       "severity": "MEDIUM",
     *       "type": "XSS",
     *       "file": "src/main/java/WebController.java",
     *       "line": 78,
     *       "description": "Cross-site scripting vulnerability",
     *       "cwe": "CWE-79"
     *     }
     *   ],
     *   "summary": {
     *     "total": 2,
     *     "high": 1,
     *     "medium": 1,
     *     "low": 0
     *   }
     * }
     * }
     *
     * <p>For large reports, results may be GZIP-compressed and Base64-encoded.</p>
     */
    @JsonProperty("scanResult")
    private String scanResult;

    /**
     * Duration of the security scan in seconds.
     * <p>Tracks the execution time of the SAST scan for performance monitoring,
     * optimization, and capacity planning purposes.</p>
     *
     * <h4>Performance Benchmarks:</h4>
     * <ul>
     *   <li><strong>&lt; 60 seconds:</strong> Small to medium codebases</li>
     *   <li><strong>60-300 seconds:</strong> Large codebases or comprehensive scans</li>
     *   <li><strong>&gt; 300 seconds:</strong> Enterprise applications or deep analysis</li>
     * </ul>
     */
    @JsonProperty("scanDurationSeconds")
    private double scanDurationSeconds;

    // ========================================================================
    // STATUS &amp; ERROR HANDLING
    // ========================================================================

    /**
     * Overall status of the SAST scan execution.
     * <p>Indicates the final outcome of the security scan process, enabling
     * quick assessment of scan success and appropriate follow-up actions.</p>
     *
     * <h4>Status Values:</h4>
     * <ul>
     *   <li><strong>SUCCESS:</strong> Scan completed successfully with results</li>
     *   <li><strong>FAILURE:</strong> Scan failed due to errors or configuration issues</li>
     *   <li><strong>WARNING:</strong> Scan completed with warnings or partial results</li>
     *   <li><strong>TIMEOUT:</strong> Scan exceeded maximum execution time</li>
     *   <li><strong>CANCELLED:</strong> Scan was cancelled by user or system</li>
     * </ul>
     */
    @JsonProperty("status")
    private String status;

    /**
     * Error message or diagnostic information for failed scans.
     * <p>Contains human-readable error descriptions, failure reasons, or
     * diagnostic information when security scans fail or encounter issues.</p>
     *
     * <h4>Error Categories:</h4>
     * <ul>
     *   <li><strong>Authentication Errors:</strong> Invalid credentials or API tokens</li>
     *   <li><strong>Configuration Issues:</strong> Misconfigured scan parameters or rules</li>
     *   <li><strong>Resource Constraints:</strong> Insufficient memory or timeout issues</li>
     *   <li><strong>Network Problems:</strong> Connectivity issues with SAST services</li>
     *   <li><strong>Repository Access:</strong> Source code access or permission problems</li>
     * </ul>
     *
     * <h4>Example Error Messages:</h4>
     * <ul>
     *   <li>{@code "Authentication failed: Invalid SonarQube token"}</li>
     *   <li>{@code "Scan timeout: Analysis exceeded 600 seconds limit"}</li>
     *   <li>{@code "Repository access denied: Insufficient permissions"}</li>
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
     * <p>Creates a new ScanResult instance with the type set to "sast_scanning"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "sast_scanning" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * SAST scan result messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * ScanResult scanResult = objectMapper.readValue(jsonMessage, ScanResult.class);
     *
     * // Manual instantiation for testing
     * ScanResult scanResult = new ScanResult();
     * scanResult.setJobName("security-pipeline");
     * scanResult.setBuildNumber(123);
     * scanResult.setTool("SonarQube");
     * scanResult.setStatus("SUCCESS");
     * scanResult.setScanResult("{\"vulnerabilities\":[...]}");
     * }
     *
     * @see TypedLog#setType(String)
     */
    public ScanResult() {
        setType("sast_scanning");
        logger.debug("Initialized ScanResult with type=sast_scanning");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - REPOSITORY &amp; SOURCE CONTEXT
    // ========================================================================

    /**
     * Gets the repository URL for the scanned codebase.
     * @return the repository URL, or {@code null} if not set
     */
    public String getRepoUrl() {
        return repoUrl;
    }

    /**
     * Sets the repository URL for the scanned codebase.
     * @param repoUrl the repository URL to set
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    /**
     * Gets the Git branch that was scanned.
     * @return the branch name, or {@code null} if not set
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Sets the Git branch that was scanned.
     * @param branch the branch name to set
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - JENKINS BUILD CONTEXT
    // ========================================================================

    /**
     * Gets the Jenkins job name associated with this scan result.
     * @return the job name, or {@code null} if not set
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the Jenkins job name for this scan result.
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the Jenkins build number for this scan result.
     * @return the build number
     */
    @Override
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the Jenkins build number for this scan result.
     * @param buildNumber the build number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - SCAN EXECUTION &amp; RESULTS
    // ========================================================================

    /**
     * Gets the SAST tool used for the security scan.
     * @return the tool name, or {@code null} if not set
     */
    public String getTool() {
        return tool;
    }

    /**
     * Sets the SAST tool used for the security scan.
     * @param tool the tool name to set
     */
    public void setTool(String tool) {
        this.tool = tool;
    }

    /**
     * Gets the comprehensive SAST scan results.
     * @return the scan results in JSON or compressed format, or {@code null} if not available
     */
    public String getScanResult() {
        return scanResult;
    }

    /**
     * Sets the comprehensive SAST scan results.
     * @param scanResult the scan results to set, may be JSON or compressed format
     */
    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    /**
     * Gets the duration of the security scan in seconds.
     * @return the scan duration in seconds
     */
    public double getScanDurationSeconds() {
        return scanDurationSeconds;
    }

    /**
     * Sets the duration of the security scan in seconds.
     * @param scanDurationSeconds the scan duration to set
     */
    public void setScanDurationSeconds(double scanDurationSeconds) {
        this.scanDurationSeconds = scanDurationSeconds;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - STATUS &amp; ERROR HANDLING
    // ========================================================================

    /**
     * Gets the overall status of the SAST scan execution.
     * @return the scan status, or {@code null} if not set
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the overall status of the SAST scan execution.
     * @param status the scan status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

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
     * Generates structured content for AI-powered security analysis &amp; vulnerability assessment.
     *
     * <p>This method processes the raw SAST scan results and provides clean, analyzable content
     * for AI systems. It handles both compressed and uncompressed scan results, automatically
     * decompressing GZIP content when necessary, and formats security findings for optimal
     * AI analysis and risk assessment.</p>
     *
     * <h4>Content Processing Strategy:</h4>
     * <p>The method employs a streamlined approach for content extraction:</p>
     * <ol>
     *   <li><strong>Error Prioritization:</strong> If error field is present, includes error details</li>
     *   <li><strong>Status Information:</strong> Scan execution status for context</li>
     *   <li><strong>Result Processing:</strong> Detects compression and decompresses scan findings</li>
     *   <li><strong>Content Assembly:</strong> Combines all information for analysis</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When scan errors are present, the content includes diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Authentication failed: Invalid SonarQube token
     * Status: FAILURE
     * }
     *
     * <h4>Successful Scan Content Format:</h4>
     * <p>For successful scans, provides comprehensive security analysis data:</p>
     * {@snippet lang="text" :
     * Status: SUCCESS
     * Scan Result: {"vulnerabilities":[{"severity":"HIGH","type":"SQL_INJECTION","file":"UserController.java","line":42,"description":"Potential SQL injection vulnerability","cwe":"CWE-89"}],"summary":{"total":1,"high":1,"medium":0,"low":0}}
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Vulnerability Assessment:</strong> Analysis of security findings &amp; risk levels</li>
     *   <li><strong>Trend Analysis:</strong> Historical vulnerability tracking &amp; improvement trends</li>
     *   <li><strong>Risk Prioritization:</strong> AI-driven vulnerability prioritization &amp; remediation guidance</li>
     *   <li><strong>Compliance Monitoring:</strong> Automated compliance checking &amp; reporting</li>
     *   <li><strong>False Positive Detection:</strong> Machine learning-based false positive identification</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Compression Detection:</strong> Automatic detection of Base64-encoded GZIP content</li>
     *   <li><strong>Lazy Decompression:</strong> Decompresses only when content is requested</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted or malformed data</li>
     *   <li><strong>Memory Efficiency:</strong> Optimized processing of large security reports</li>
     * </ul>
     *
     * @return formatted string containing SAST scan information optimized for AI analysis.
     *         Returns error information when present, followed by scan status and
     *         detailed security findings, or "No content available" if no data is present.
     * @throws Exception if decompression fails, scan data is corrupted, or processing errors occur
     *
     * @see TypedLog#getContentToAnalyze()
     * @see #decodeGzipMessage(String)
     * @see #isBase64Encoded(String)
     */
    @JsonIgnore
    @Override
    public String getContentToAnalyze() throws Exception {
        StringBuilder content = new StringBuilder();
        if (error != null) {
            content.append("Error: ").append(error).append("\n");
        }
        if (status != null) {
            content.append("Status: ").append(status).append("\n");
        }
        if (scanResult != null && !scanResult.isEmpty()) {
            // Attempt to detect if scanResult is Base64-encoded
            String resultContent;
            if (isBase64Encoded(scanResult)) {
                try {
                    resultContent = decodeGzipMessage(scanResult);
                    this.setScanResult(resultContent);
                } catch (Exception e) {
                    logger.warn("Failed to decode scanResult as GZIP: {}. Treating as uncompressed.", scanResult, e);
                    resultContent = scanResult;
                }
            } else {
                resultContent = scanResult;
            }
            content.append("Scan Result: ").append(resultContent).append("\n");
        }
        return content.length() > 0 ? content.toString() : "No content available";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of SAST scan results with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that scan results are grouped with
     * related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Security Correlation:</strong> Links scan results to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks security trends &amp; improvements over time</li>
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
     *   <li>Security scan sequence tracking</li>
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
     * Decodes GZIP-compressed Base64-encoded SAST scan result messages.
     *
     * <p>This private utility method handles the decompression of SAST scan results that have been
     * compressed using GZIP and encoded with Base64 for efficient transmission. It provides
     * robust error handling and logging for debugging compression-related issues.</p>
     *
     * <h4>Decompression Process:</h4>
     * <ol>
     *   <li><strong>Validation:</strong> Checks for null or empty encoded messages</li>
     *   <li><strong>Base64 Decoding:</strong> Decodes the Base64-encoded compressed data</li>
     *   <li><strong>GZIP Decompression:</strong> Decompresses the GZIP data stream</li>
     *   <li><strong>String Conversion:</strong> Converts decompressed bytes to UTF-8 string</li>
     *   <li><strong>Error Handling:</strong> Provides comprehensive error reporting</li>
     * </ol>
     *
     * <h4>Compression Format:</h4>
     * <p>The method expects data in the following format:</p>
     * {@snippet lang="text" :
     * Original Scan Results → GZIP Compression → Base64 Encoding → Transmission
     * {"vulnerabilities":[...]} → [GZIP bytes] → "H4sIAAAAAAAA/62QwQrCMBBE..." → Network
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
     *   <li><strong>Memory Efficiency:</strong> Uses streaming decompression for large reports</li>
     *   <li><strong>Resource Management:</strong> Automatic resource cleanup with try-with-resources</li>
     *   <li><strong>Error Logging:</strong> Comprehensive logging for debugging issues</li>
     *   <li><strong>Exception Propagation:</strong> Re-throws exceptions for proper error handling</li>
     * </ul>
     *
     * @param encodedMessage the Base64-encoded GZIP-compressed scan result message
     * @return the decompressed scan result content as a UTF-8 string,
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
            throw e;
        }
    }


    /**
     * Determines if a string is Base64-encoded content.
     *
     * <p>This utility method performs validation to detect whether scan result content
     * is Base64-encoded (and potentially GZIP-compressed). It uses multiple validation
     * criteria to accurately identify Base64 content and avoid false positives.</p>
     *
     * <h4>Validation Criteria:</h4>
     * <ol>
     *   <li><strong>Null/Empty Check:</strong> Returns false for null or empty strings</li>
     *   <li><strong>Length Validation:</strong> Base64 strings must have length divisible by 4</li>
     *   <li><strong>Character Validation:</strong> Attempts actual Base64 decoding</li>
     *   <li><strong>Exception Handling:</strong> Catches decoding exceptions for invalid content</li>
     * </ol>
     *
     * <h4>Base64 Format Requirements:</h4>
     * <ul>
     *   <li><strong>Character Set:</strong> A-Z, a-z, 0-9, +, /, = (padding)</li>
     *   <li><strong>Length:</strong> Must be multiple of 4 characters</li>
     *   <li><strong>Padding:</strong> Proper padding with = characters</li>
     *   <li><strong>Structure:</strong> Valid Base64 encoding structure</li>
     * </ul>
     *
     * <h4>Usage Scenarios:</h4>
     * <ul>
     *   <li><strong>Compression Detection:</strong> Identifies compressed scan results</li>
     *   <li><strong>Content Processing:</strong> Determines appropriate processing method</li>
     *   <li><strong>Error Prevention:</strong> Avoids unnecessary decompression attempts</li>
     *   <li><strong>Performance Optimization:</strong> Skips processing for uncompressed content</li>
     * </ul>
     *
     * <h4>False Positive Prevention:</h4>
     * <p>The method uses actual Base64 decoding to prevent false positives from:</p>
     * <ul>
     *   <li>JSON strings that happen to have valid Base64 length</li>
     *   <li>Regular text content with Base64-like characters</li>
     *   <li>Partial Base64 content or corrupted data</li>
     * </ul>
     *
     * @param str the string to check for Base64 encoding
     * @return {@code true} if the string is valid Base64-encoded content,
     *         {@code false} if null, empty, or not valid Base64
     *
     * @see Base64#getDecoder()
     * @see #decodeGzipMessage(String)
     * @see #getContentToAnalyze()
     */
    private boolean isBase64Encoded(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // Check if string is valid Base64 (length divisible by 4, valid characters)
        if (str.length() % 4 != 0) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ========================================================================
    // OBJECT REPRESENTATION &amp; SERIALIZATION
    // ========================================================================

    /**
     * Returns a string representation of this ScanResult object.
     *
     * <p>Provides a human-readable representation of the SAST scan result including
     * job context, repository information, scan metadata, and results for debugging
     * and logging purposes.</p>
     *
     * @return string representation of the ScanResult object
     */
    @Override
    public String toString() {
        return "ScanResult{" +
                "jobName='" + jobName + '\'' +
                ", buildNumber=" + buildNumber +
                ", repoUrl='" + repoUrl + '\'' +
                ", scanResult='" + scanResult + '\'' +
                ", branch='" + branch + '\'' +
                ", scanDurationSeconds=" + scanDurationSeconds +
                ", status='" + status + '\'' +
                ", error='" + error + '\'' +
                ", tool='" + tool + '\'' +
                '}';
    }

    /**
     * Converts the SAST scan result to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete SAST scan information
     * including repository context, scan results, performance metrics, error information, and
     * metadata. It uses Jackson's ObjectMapper for comprehensive serialization with proper
     * error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all scan result data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant SAST scan information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "sast_scanning",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "repoUrl": "https://github.com/company/security-app.git",
     *   "branch": "main",
     *   "tool": "SonarQube",
     *   "status": "SUCCESS",
     *   "scanDurationSeconds": 45.7,
     *   "scanResult": "{\"vulnerabilities\":[{\"severity\":\"HIGH\",\"type\":\"SQL_INJECTION\",\"file\":\"UserController.java\",\"line\":42}]}",
     *   "error": null,
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     * }
     *
     * <h4>Compressed Results Output:</h4>
     * <p>For compressed scan results, the JSON includes Base64-encoded content:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "sast_scanning",
     *   "job_name": "enterprise-security-scan",
     *   "build_number": 456,
     *   "repoUrl": "https://github.com/company/large-enterprise-app.git",
     *   "branch": "develop",
     *   "tool": "Checkmarx",
     *   "status": "SUCCESS",
     *   "scanDurationSeconds": 180.5,
     *   "scanResult": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
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
     *   <li><strong>Security Reporting:</strong> Structured logging of scan results</li>
     * </ul>
     *
     * @return JSON string representation of the SAST scan result,
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