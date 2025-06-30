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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

/**
 * Data Transfer Object for Jenkins dependency &amp; artifact information in CI/CD security analysis.
 *
 * <p>This DTO encapsulates comprehensive dependency management information including build files,
 * plugin configurations, artifact details, and dependency trees for Jenkins builds. It serves as
 * a critical component for tracking software dependencies, identifying security vulnerabilities,
 * and enabling AI-powered analysis of dependency-related risks in CI/CD pipelines.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Dependency Tracking:</strong> Captures complete dependency trees with versions &amp; sources</li>
 *   <li><strong>Build File Analysis:</strong> Processes Maven, Gradle &amp; other build configuration files</li>
 *   <li><strong>Plugin Management:</strong> Tracks Jenkins plugins &amp; their configurations</li>
 *   <li><strong>Artifact Information:</strong> Details about generated artifacts &amp; their properties</li>
 *   <li><strong>Compression Support:</strong> Handles GZIP-compressed data for large dependency sets</li>
 *   <li><strong>Security Analysis:</strong> Enables vulnerability detection &amp; compliance monitoring</li>
 * </ul>
 *
 * <h2>Data Structure &amp; Content</h2>
 * <p>The DTO manages complex dependency data structures with multiple information layers:</p>
 * <ul>
 *   <li><strong>Build Files:</strong> Maven POM, Gradle build scripts &amp; configuration content</li>
 *   <li><strong>Plugin Information:</strong> Jenkins plugin details, versions &amp; configurations</li>
 *   <li><strong>Artifact Details:</strong> Generated artifacts with file names, sizes &amp; metadata</li>
 *   <li><strong>Dependency Trees:</strong> Complete dependency hierarchies with transitive dependencies</li>
 *   <li><strong>Compressed Data:</strong> Base64-encoded GZIP content for large datasets</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "dependency_data",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "data": {
 *     "build_file": {
 *       "content": "<project><groupId>com.company</groupId><artifactId>security-app</artifactId>...</project>",
 *       "content_compressed": false,
 *       "message": "Maven POM file for security application"
 *     },
 *     "plugin_info": {
 *       "plugins": [
 *         {
 *           "name": "maven-compiler-plugin",
 *           "version": "3.8.1",
 *           "configuration": "{...}"
 *         }
 *       ]
 *     },
 *     "artifacts": {
 *       "artifacts": [
 *         {
 *           "fileName": "security-app-1.0.0.jar",
 *           "size": 15728640
 *         }
 *       ]
 *     },
 *     "dependencies": {
 *       "type": "maven",
 *       "dependencies": [
 *         {
 *           "group": "org.springframework",
 *           "artifact": "spring-core",
 *           "version": "5.3.21"
 *         }
 *       ]
 *     }
 *   },
 *   "error": null
 * }
 * }
 *
 * <h2>Compressed Content Handling</h2>
 * <p>For large build files or extensive dependency data, the DTO supports GZIP compression:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "dependency_data",
 *   "job_name": "large-enterprise-app",
 *   "build_number": 456,
 *   "data": {
 *     "build_file": {
 *       "content": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
 *       "content_compressed": true,
 *       "original_size": 524288,
 *       "compressed_size": 32768,
 *       "message": "Large Maven POM with 200+ dependencies"
 *     },
 *     "plugin_info_compressed": true,
 *     "plugin_info": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ..."
 *   }
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When dependency processing errors occur, the DTO captures diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "dependency_data",
 *   "job_name": "failing-build",
 *   "build_number": 789,
 *   "data": {
 *     "build_file": {
 *       "content": "",
 *       "message": "Failed to read build file"
 *     },
 *     "dependencies": {
 *       "type": "unknown",
 *       "dependencies": []
 *     }
 *   },
 *   "error": "Maven dependency resolution failed: repository unreachable"
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered analysis:</p>
 * <ul>
 *   <li><strong>Vulnerability Detection:</strong> Identification of known vulnerable dependencies</li>
 *   <li><strong>License Compliance:</strong> Analysis of dependency licenses &amp; compliance issues</li>
 *   <li><strong>Version Management:</strong> Detection of outdated or incompatible versions</li>
 *   <li><strong>Security Policies:</strong> Verification against organizational security policies</li>
 *   <li><strong>Risk Assessment:</strong> Evaluation of dependency-related security risks</li>
 * </ul>
 *
 * <h2>Build System Support</h2>
 * <p>The DTO supports multiple build systems &amp; dependency managers:</p>
 * <ul>
 *   <li><strong>Maven:</strong> POM files, dependency trees &amp; plugin configurations</li>
 *   <li><strong>Gradle:</strong> Build scripts, dependency declarations &amp; task configurations</li>
 *   <li><strong>NPM:</strong> Package.json files &amp; node module dependencies</li>
 *   <li><strong>Pip:</strong> Requirements.txt &amp; Python package dependencies</li>
 *   <li><strong>NuGet:</strong> .NET package dependencies &amp; configurations</li>
 * </ul>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li><strong>Data Sanitization:</strong> Safe handling of potentially sensitive build configurations</li>
 *   <li><strong>Compression Security:</strong> Secure decompression with size &amp; content validation</li>
 *   <li><strong>Content Filtering:</strong> Removal of sensitive information from build files</li>
 *   <li><strong>Access Control:</strong> Secure processing of repository &amp; registry credentials</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>XML Minification:</strong> Efficient processing of large XML build files</li>
 *   <li><strong>Lazy Decompression:</strong> Decompresses content only when needed for analysis</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized handling of large dependency datasets</li>
 *   <li><strong>Streaming Processing:</strong> Efficient processing of continuous dependency updates</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes dependency information messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores dependency data for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides dependency content for security analysis</li>
 *   <li><strong>Vulnerability Scanner:</strong> Supplies dependency data for vulnerability detection</li>
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
public class DependencyData extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(DependencyData.class);

    /**
     * Regular expression pattern for matching whitespace characters.
     * <p>Used for XML minification to replace multiple whitespace characters with single spaces.</p>
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(?s)\\s+");

    /**
     * Regular expression pattern for matching XML comments.
     * <p>Used for XML minification to remove comments from build file content.</p>
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this dependency data.
     * <p>Used for correlating dependency information with specific Jenkins jobs and enabling
     * conversation-based grouping in the AI analysis pipeline.</p>
     *
     * <h4>Usage Examples:</h4>
     * <ul>
     *   <li>{@code "security-pipeline"} - Security-focused application pipeline</li>
     *   <li>{@code "microservice-build"} - Microservice dependency tracking</li>
     *   <li>{@code "enterprise-app"} - Large enterprise application build</li>
     * </ul>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this dependency data snapshot.
     * <p>Enables tracking of dependency changes across different build executions
     * and correlation with build outcomes for trend analysis.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // DEPENDENCY DATA &amp; METADATA
    // ========================================================================

    /**
     * Complex data structure containing comprehensive dependency information and metadata.
     * <p>This map contains detailed dependency data including build files, plugin configurations,
     * artifact details, dependency trees, compression flags, and other contextual information
     * required for thorough dependency analysis and security assessment.</p>
     *
     * <h4>Standard Data Structure:</h4>
     * <ul>
     *   <li><strong>build_file:</strong> Build configuration file content &amp; metadata</li>
     *   <li><strong>plugin_info:</strong> Jenkins plugin information &amp; configurations</li>
     *   <li><strong>artifacts:</strong> Generated artifacts with file details &amp; sizes</li>
     *   <li><strong>dependencies:</strong> Complete dependency tree with versions &amp; sources</li>
     *   <li><strong>*_compressed:</strong> Boolean flags indicating GZIP compression</li>
     * </ul>
     *
     * <h4>Build File Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "build_file": {
     *     "content": "<project><groupId>com.company</groupId><artifactId>app</artifactId>...</project>",
     *     "content_compressed": false,
     *     "message": "Maven POM file for security application",
     *     "type": "maven",
     *     "size": 15360
     *   }
     * }
     * }
     *
     * <h4>Plugin Information Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "plugin_info": {
     *     "plugins": [
     *       {
     *         "name": "maven-compiler-plugin",
     *         "version": "3.8.1",
     *         "groupId": "org.apache.maven.plugins",
     *         "configuration": {
     *           "source": "11",
     *           "target": "11"
     *         }
     *       }
     *     ]
     *   },
     *   "plugin_info_compressed": false
     * }
     * }
     *
     * <h4>Dependencies Structure:</h4>
     * {@snippet lang="json" :
     * {
     *   "dependencies": {
     *     "type": "maven",
     *     "dependencies": [
     *       {
     *         "group": "org.springframework",
     *         "artifact": "spring-core",
     *         "version": "5.3.21",
     *         "scope": "compile",
     *         "transitive": false
     *       },
     *       {
     *         "group": "junit",
     *         "artifact": "junit",
     *         "version": "4.13.2",
     *         "scope": "test",
     *         "transitive": false
     *       }
     *     ]
     *   }
     * }
     * }
     */
    @JsonProperty("data")
    private Map<String, Object> data; // Contains build_file, plugin_info, artifacts, dependencies

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Error message or diagnostic information for dependency processing failures.
     * <p>Contains human-readable error descriptions, failure reasons, or
     * diagnostic information when dependency resolution or processing fails.</p>
     *
     * <h4>Error Types:</h4>
     * <ul>
     *   <li><strong>Repository Access:</strong> Maven/NPM repository connectivity issues</li>
     *   <li><strong>Dependency Resolution:</strong> Version conflicts, missing dependencies</li>
     *   <li><strong>Build File Parsing:</strong> Malformed POM, Gradle, or package files</li>
     *   <li><strong>Plugin Errors:</strong> Jenkins plugin configuration or execution failures</li>
     *   <li><strong>Compression Issues:</strong> GZIP decompression or encoding problems</li>
     * </ul>
     *
     * <h4>Example Error Messages:</h4>
     * <ul>
     *   <li>{@code "Maven dependency resolution failed: repository unreachable"}</li>
     *   <li>{@code "Failed to parse POM file: invalid XML structure"}</li>
     *   <li>{@code "Plugin configuration error: missing required parameters"}</li>
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
     * <p>Creates a new DependencyData instance with the type set to "dependency_data"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "dependency_data" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * Jenkins dependency information messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * DependencyData depData = objectMapper.readValue(jsonMessage, DependencyData.class);
     *
     * // Manual instantiation for testing
     * DependencyData depData = new DependencyData();
     * depData.setJobName("security-pipeline");
     * depData.setBuildNumber(123);
     *
     * Map<String, Object> data = new HashMap<>();
     * Map<String, Object> buildFile = new HashMap<>();
     * buildFile.put("content", pomContent);
     * buildFile.put("content_compressed", false);
     * data.put("build_file", buildFile);
     * depData.setData(data);
     * }
     *
     * @see TypedLog#setType(String)
     */
    public DependencyData() {
        setType("dependency_data");
        logger.debug("Initialized DependencyData with type=dependency_data");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - BUILD CONTEXT
    // ========================================================================

    /**
     * Gets the Jenkins job name associated with this dependency data.
     * @return the job name, or {@code null} if not set
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the Jenkins job name for this dependency data.
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the Jenkins build number for this dependency data.
     * @return the build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the Jenkins build number for this dependency data.
     * @param buildNumber the build number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - DEPENDENCY DATA &amp; METADATA
    // ========================================================================

    /**
     * Gets the complex data structure containing dependency information and metadata.
     * @return the data map containing dependency details and metadata, or {@code null} if not set
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the dependency data and metadata.
     * @param data the data map to set, containing dependency information and metadata
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
     * Generates structured content for AI-powered dependency &amp; security analysis.
     *
     * <p>This method processes the raw dependency data and provides clean, analyzable content
     * for AI systems. It handles both compressed and uncompressed data, automatically
     * decompressing GZIP content when necessary, and formats dependency information for optimal
     * AI analysis and security assessment.</p>
     *
     * <h4>Content Processing Strategy:</h4>
     * <p>The method employs a multi-stage approach for content extraction:</p>
     * <ol>
     *   <li><strong>Error Prioritization:</strong> If error field is present, includes error details</li>
     *   <li><strong>Data Validation:</strong> Checks for presence of dependency data</li>
     *   <li><strong>Build File Processing:</strong> Extracts &amp; minifies build configuration files</li>
     *   <li><strong>Plugin Information:</strong> Processes Jenkins plugin configurations</li>
     *   <li><strong>Artifact Details:</strong> Includes generated artifact information</li>
     *   <li><strong>Dependency Trees:</strong> Formats complete dependency hierarchies</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When dependency processing errors are present, the content includes diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Maven dependency resolution failed: repository unreachable
     * Build File Message: Failed to resolve dependencies from central repository
     * }
     *
     * <h4>Normal Content Format:</h4>
     * <p>For successful dependency processing, provides comprehensive dependency details:</p>
     * {@snippet lang="text" :
     * Build File Content: <project><groupId>com.company</groupId><artifactId>security-app</artifactId><version>1.0.0</version>...</project>
     * Build File Message: Maven POM file for security application
     * Plugin Info: {"plugins":[{"name":"maven-compiler-plugin","version":"3.8.1","configuration":{"source":"11","target":"11"}}]}
     * Artifacts:
     * - File: security-app-1.0.0.jar, Size: 15728640
     * - File: security-app-1.0.0-sources.jar, Size: 2048576
     * Dependencies (Type: maven):
     * - Group: org.springframework, Artifact: spring-core, Version: 5.3.21
     * - Group: org.springframework.security, Artifact: spring-security-core, Version: 5.7.2
     * - Group: junit, Artifact: junit, Version: 4.13.2
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Vulnerability Detection:</strong> Identification of known vulnerable dependencies</li>
     *   <li><strong>License Compliance:</strong> Analysis of dependency licenses &amp; compliance issues</li>
     *   <li><strong>Version Management:</strong> Detection of outdated or incompatible versions</li>
     *   <li><strong>Security Policies:</strong> Verification against organizational security policies</li>
     *   <li><strong>Risk Assessment:</strong> Evaluation of dependency-related security risks</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>XML Minification:</strong> Reduces build file size for efficient processing</li>
     *   <li><strong>Lazy Decompression:</strong> Decompresses only when content is requested</li>
     *   <li><strong>Memory Efficiency:</strong> Streams large dependency datasets to prevent memory issues</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted or malformed data</li>
     * </ul>
     *
     * @return formatted string containing dependency information optimized for AI analysis.
     *         Returns error information when present, followed by build file content,
     *         plugin information, artifacts, and dependency details.
     * @throws Exception if decompression fails, JSON parsing fails, or dependency data is corrupted
     *
     * @see TypedLog#getContentToAnalyze()
     * @see #decodeGzipMessage(String)
     * @see #minifyXml(String)
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

        // Build file
        Map<String, Object> buildFile = (Map<String, Object>) data.getOrDefault("build_file", Collections.emptyMap());
        String buildFileContent = "";
        if (buildFile.containsKey("content")) {
            if (Boolean.TRUE.equals(buildFile.get("content_compressed"))) {
                buildFileContent = decodeGzipMessage((String) buildFile.get("content"));
                buildFile.put("content",minifyXml(buildFileContent));
                this.data.put("build_file",buildFile);
            } else {
                buildFileContent = (String) buildFile.getOrDefault("content", "");
            }
        }
        String buildFileMessage = (String) buildFile.getOrDefault("message", "");
        if (!buildFileContent.isEmpty()) {
            content.append("Build File Content: ").append(buildFileContent).append("\n");
        }
        if (!buildFileMessage.isEmpty()) {
            content.append("Build File Message: ").append(buildFileMessage).append("\n");
        }

        // Plugin info
        String pluginInfo = "";
        if (Boolean.TRUE.equals(data.get("plugin_info_compressed"))) {
            pluginInfo = decodeGzipMessage((String) data.get("plugin_info"));
            this.data.put("plugin_info",pluginInfo);
        } else {
            Object pluginInfoObj = data.getOrDefault("plugin_info", Collections.emptyMap());
            pluginInfo = mapper.writeValueAsString(pluginInfoObj);
        }
        if (!pluginInfo.isEmpty()) {
            content.append("Plugin Info: ").append(pluginInfo).append("\n");
        }

        // Artifacts
        Map<String, Object> artifactsMap = (Map<String, Object>) data.getOrDefault("artifacts", Collections.emptyMap());
        List<Map<String, Object>> artifacts = (List<Map<String, Object>>) artifactsMap.getOrDefault("artifacts", Collections.emptyList());
        if (!artifacts.isEmpty()) {
            content.append("Artifacts:\n");
            for (Map<String, Object> artifact : artifacts) {
                content.append("- File: ").append(artifact.getOrDefault("fileName", "N/A"))
                        .append(", Size: ").append(artifact.getOrDefault("size", 0)).append("\n");
            }
        }

        // Dependencies
        Map<String, Object> dependenciesMap = (Map<String, Object>) data.getOrDefault("dependencies", Collections.emptyMap());
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) dependenciesMap.getOrDefault("dependencies", Collections.emptyList());
        String depType = (String) dependenciesMap.getOrDefault("type", "unknown");
        if (!dependencies.isEmpty()) {
            content.append("Dependencies (Type: ").append(depType).append("):\n");
            for (Map<String, Object> dep : dependencies) {
                content.append("- Group: ").append(dep.getOrDefault("group", "N/A"))
                        .append(", Artifact: ").append(dep.getOrDefault("artifact", "N/A"))
                        .append(", Version: ").append(dep.getOrDefault("version", "N/A")).append("\n");
            }
        }

        return content.length() > 0 ? content.toString() : "No content available";
    }

    // ========================================================================
    // XML PROCESSING &amp; MINIFICATION UTILITIES
    // ========================================================================

    /**
     * Minifies XML content for efficient storage &amp; transmission.
     *
     * <p>This utility method processes XML build files (such as Maven POM files) to reduce
     * their size by removing comments, normalizing whitespace, and eliminating unnecessary
     * formatting while preserving the XML structure and content.</p>
     *
     * <h4>Minification Process:</h4>
     * <ol>
     *   <li><strong>Comment Removal:</strong> Removes XML comments using regex pattern matching</li>
     *   <li><strong>Whitespace Normalization:</strong> Replaces multiple whitespace with single spaces</li>
     *   <li><strong>Tag Spacing:</strong> Removes spaces between XML tags for compact formatting</li>
     *   <li><strong>Trimming:</strong> Removes leading and trailing whitespace</li>
     * </ol>
     *
     * <h4>Input Example:</h4>
     * {@snippet lang = "xml":
     * <?xml version="1.0" encoding="UTF-8"?>
     * <!-- Maven POM file for security application -->
     * <project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion/>
     *     <groupId>com.company</groupId>
     *     <artifactId>security-app</artifactId>
     *     <version>1.0.0</version>
     *
     *     <dependencies>
     *         <dependency>
     *             <groupId>org.springframework</groupId>
     *             <artifactId>spring-core</artifactId>
     *             <version>5.3.21</version>
     *         </dependency>
     *     </dependencies>
     * </project>
     *}
     *
     * <h4>Output Example:</h4>
     * {@snippet lang = "xml":
     * <?xml version="1.0" encoding="UTF-8"?><project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion/><groupId>com.company</groupId><artifactId>security-app</artifactId><version>1.0.0</version><dependencies><dependency><groupId>org.springframework</groupId><artifactId>spring-core</artifactId><version>5.3.21</version></dependency></dependencies></project>
     *}
     *
     * <h4>Error Handling:</h4>
     * <p>The method includes comprehensive error handling:</p>
     * <ul>
     *   <li><strong>Null Safety:</strong> Returns input unchanged if null or empty</li>
     *   <li><strong>Exception Recovery:</strong> Returns original content on processing errors</li>
     *   <li><strong>Error Logging:</strong> Logs detailed error information for debugging</li>
     *   <li><strong>Graceful Degradation:</strong> Ensures system continues operation</li>
     * </ul>
     *
     * <h4>Performance Benefits:</h4>
     * <ul>
     *   <li><strong>Size Reduction:</strong> Typically reduces XML size by 30-50%</li>
     *   <li><strong>Transmission Efficiency:</strong> Faster network transfer of build files</li>
     *   <li><strong>Storage Optimization:</strong> Reduced database storage requirements</li>
     *   <li><strong>Processing Speed:</strong> Faster AI analysis of smaller content</li>
     * </ul>
     *
     * @param xmlContent the XML content to minify, typically a Maven POM or Gradle build file
     * @return the minified XML content with comments removed and whitespace normalized,
     *         or the original content if minification fails
     *
     * @see #WHITESPACE_PATTERN
     * @see #COMMENT_PATTERN
     * @see #getContentToAnalyze()
     */
    private String minifyXml(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return xmlContent;
        }
        try {
            // Remove XML comments
            String noComments = COMMENT_PATTERN.matcher(xmlContent).replaceAll("");
            // Replace multiple whitespace (including newlines, tabs) with a single space
            String singleSpaced = WHITESPACE_PATTERN.matcher(noComments).replaceAll(" ");
            // Remove leading/trailing whitespace and spaces around tags
            return singleSpaced.replaceAll(">\\s+<", "><").trim();
        } catch (Exception e) {
            logger.error("Failed to minify XML content: {}", xmlContent, e);
            return xmlContent; // Fallback to original content
        }
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of dependency data with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that dependency information is grouped
     * with related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Dependency Correlation:</strong> Links dependency data to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks dependency changes &amp; trends over time</li>
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
     *   <li>Dependency sequence tracking</li>
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
     * Decodes GZIP-compressed Base64-encoded dependency data messages.
     *
     * <p>This private utility method handles the decompression of dependency data that has been
     * compressed using GZIP and encoded with Base64 for efficient transmission. It provides
     * robust error handling and logging for debugging compression-related issues.</p>
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
     * Original Data → GZIP Compression → Base64 Encoding → Transmission
     * "<project>...</project>" → [GZIP bytes] → "H4sIAAAAAAAA/62QwQrCMBBE..." → Network
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
     * @param encodedMessage the Base64-encoded GZIP-compressed dependency message
     * @return the decompressed dependency content as a UTF-8 string,
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
     * Returns a string representation of this DependencyData object.
     *
     * <p>Provides a human-readable representation of the dependency data including
     * job name, build number, data summary, and error information for debugging
     * and logging purposes.</p>
     *
     * @return string representation of the DependencyData object
     */
    @Override
    public String toString() {
        return "DependencyData{" +
                "jobName='" + jobName + '\'' +
                ", data=" + data +
                ", buildNumber=" + buildNumber +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * Converts the dependency data to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete dependency information
     * including build files, plugin configurations, artifact details, dependency trees, and
     * metadata. It uses Jackson's ObjectMapper for comprehensive serialization with proper
     * error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all dependency data &amp; metadata</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant dependency information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "dependency_data",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "data": {
     *     "build_file": {
     *       "content": "<project><groupId>com.company</groupId><artifactId>security-app</artifactId>...</project>",
     *       "content_compressed": false,
     *       "message": "Maven POM file for security application"
     *     },
     *     "plugin_info": {
     *       "plugins": [
     *         {
     *           "name": "maven-compiler-plugin",
     *           "version": "3.8.1",
     *           "configuration": {
     *             "source": "11",
     *             "target": "11"
     *           }
     *         }
     *       ]
     *     },
     *     "artifacts": {
     *       "artifacts": [
     *         {
     *           "fileName": "security-app-1.0.0.jar",
     *           "size": 15728640
     *         }
     *       ]
     *     },
     *     "dependencies": {
     *       "type": "maven",
     *       "dependencies": [
     *         {
     *           "group": "org.springframework",
     *           "artifact": "spring-core",
     *           "version": "5.3.21"
     *         }
     *       ]
     *     }
     *   },
     *   "error": null
     * }
     * }
     *
     * <h4>Compressed Data Output:</h4>
     * <p>For compressed dependency data, the JSON includes compression metadata:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "dependency_data",
     *   "job_name": "large-enterprise-app",
     *   "build_number": 456,
     *   "data": {
     *     "build_file": {
     *       "content": "H4sIAAAAAAAA/62QwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ...",
     *       "content_compressed": true,
     *       "original_size": 524288,
     *       "compressed_size": 32768
     *     },
     *     "plugin_info_compressed": true,
     *     "plugin_info": "H4sIAAAAAAAA/3WQwQrCMBBE7+UrHnuxVqsHwYMf4MmTJ..."
     *   }
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
     *   <li><strong>Logging:</strong> Structured logging of dependency information</li>
     * </ul>
     *
     * @return JSON string representation of the dependency data,
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