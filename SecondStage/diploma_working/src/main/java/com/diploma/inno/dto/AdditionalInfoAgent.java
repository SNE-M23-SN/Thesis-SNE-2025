package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for Jenkins agent system information &amp; performance metrics in CI/CD anomaly detection.
 *
 * <p>This DTO captures comprehensive Jenkins agent runtime information including system performance,
 * memory utilization, thread management, and error conditions. It serves as a critical component
 * for monitoring Jenkins agent health and detecting infrastructure-related anomalies that could
 * impact CI/CD pipeline reliability and security.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>System Monitoring:</strong> CPU load, memory usage, thread counts &amp; system metrics</li>
 *   <li><strong>Agent Health:</strong> Session management, active connections &amp; resource utilization</li>
 *   <li><strong>Error Detection:</strong> Exception tracking, stack traces &amp; failure analysis</li>
 *   <li><strong>Performance Analysis:</strong> Load averages, processor utilization &amp; capacity metrics</li>
 *   <li><strong>Infrastructure Context:</strong> Host information, OS details &amp; runtime environment</li>
 * </ul>
 *
 * <h2>Data Collection Sources</h2>
 * <p>This DTO aggregates information from multiple Jenkins agent subsystems:</p>
 * <ul>
 *   <li><strong>JVM Runtime:</strong> Memory management, thread pools &amp; garbage collection</li>
 *   <li><strong>Operating System:</strong> CPU metrics, load averages &amp; system resources</li>
 *   <li><strong>Jenkins Agent:</strong> Session counts, active builds &amp; connection status</li>
 *   <li><strong>Error Handling:</strong> Exception details, stack traces &amp; failure contexts</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "additional_info_agent",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "node": "jenkins-agent-01",
 *   "sessionCount": 5,
 *   "activeThreadCount": 12,
 *   "threadCount": 25,
 *   "systemLoadAverage": 1.45,
 *   "systemCpuLoad": 0.67,
 *   "availableProcessors": 8,
 *   "host": "jenkins-agent-01.company.com",
 *   "os": "Linux 5.4.0-74-generic",
 *   "javaVersion": "17.0.2",
 *   "jvmVersion": "OpenJDK 64-Bit Server VM",
 *   "pid": "12345",
 *   "serverInfo": "Jenkins/2.401.3",
 *   "contextPath": "/jenkins",
 *   "startDate": "2024-01-15T08:30:00Z",
 *   "memory": {
 *     "heapUsed": 512000000,
 *     "heapMax": 2048000000,
 *     "nonHeapUsed": 128000000,
 *     "nonHeapMax": 256000000
 *   },
 *   "threads": {
 *     "totalThreads": 25,
 *     "deadlockedThreads": 0,
 *     "deadlockedThreadData": []
 *   },
 *   "status": "healthy",
 *   "message": null,
 *   "stacktrace": null
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When agent errors occur, the DTO captures detailed diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "additional_info_agent",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "status": "error",
 *   "message": "Agent connection lost during build execution",
 *   "stacktrace": [
 *     "java.net.SocketException: Connection reset",
 *     "at java.net.SocketInputStream.read(SocketInputStream.java:186)",
 *     "at hudson.remoting.Channel$ReaderThread.run(Channel.java:1234)"
 *   ]
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered anomaly detection:</p>
 * <ul>
 *   <li><strong>Performance Baselines:</strong> Historical comparison of system metrics</li>
 *   <li><strong>Resource Utilization:</strong> Memory &amp; CPU usage pattern analysis</li>
 *   <li><strong>Thread Management:</strong> Deadlock detection &amp; concurrency issues</li>
 *   <li><strong>Error Correlation:</strong> Exception patterns &amp; failure trend analysis</li>
 * </ul>
 *
 * <h2>Monitoring Use Cases</h2>
 * <ul>
 *   <li><strong>Capacity Planning:</strong> Resource utilization trends &amp; scaling decisions</li>
 *   <li><strong>Performance Optimization:</strong> Bottleneck identification &amp; tuning</li>
 *   <li><strong>Reliability Monitoring:</strong> Agent stability &amp; connection health</li>
 *   <li><strong>Security Analysis:</strong> Anomalous resource usage &amp; potential threats</li>
 *   <li><strong>Troubleshooting:</strong> Error diagnosis &amp; root cause analysis</li>
 * </ul>
 *
 * <h2>Data Validation &amp; Quality</h2>
 * <p>The DTO includes comprehensive validation &amp; error handling:</p>
 * <ul>
 *   <li><strong>Null Safety:</strong> Graceful handling of missing or unavailable metrics</li>
 *   <li><strong>Type Safety:</strong> Strong typing for numeric metrics &amp; system properties</li>
 *   <li><strong>JSON Validation:</strong> Robust serialization with error recovery</li>
 *   <li><strong>Content Analysis:</strong> Intelligent content extraction for AI processing</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes agent information messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores agent metrics for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides structured data for anomaly detection</li>
 *   <li><strong>Monitoring Dashboard:</strong> Supplies real-time agent health metrics</li>
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
public class AdditionalInfoAgent extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(AdditionalInfoAgent.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this agent information.
     * <p>Used for correlating agent metrics with specific Jenkins jobs and builds.</p>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this agent information snapshot.
     * <p>Enables tracking of agent performance across different build executions.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // AGENT IDENTIFICATION &amp; NETWORK INFORMATION
    // ========================================================================

    /**
     * Jenkins agent node identifier.
     * <p>Unique identifier for the Jenkins agent node providing this system information.
     * Used for agent-specific monitoring and resource allocation decisions.</p>
     */
    @JsonProperty("node")
    private String node;

    /**
     * Fully qualified domain name or IP address of the agent host.
     * <p>Network identifier for the physical or virtual machine running the Jenkins agent.
     * Essential for network troubleshooting and infrastructure mapping.</p>
     */
    @JsonProperty("host")
    private String host;

    /**
     * Operating system information including version and architecture.
     * <p>Complete OS details for compatibility analysis and platform-specific optimizations.</p>
     *
     * <h4>Example Values:</h4>
     * <ul>
     *   <li>{@code "Linux 5.4.0-74-generic"} - Ubuntu/Debian systems</li>
     *   <li>{@code "Windows Server 2019 10.0.17763"} - Windows environments</li>
     *   <li>{@code "macOS 12.6 (21G115)"} - macOS systems</li>
     * </ul>
     */
    @JsonProperty("os")
    private String os;

    // ========================================================================
    // SESSION &amp; CONNECTION MANAGEMENT
    // ========================================================================

    /**
     * Number of active Jenkins sessions on this agent.
     * <p>Indicates the current workload and connection count for capacity planning
     * and load balancing decisions.</p>
     */
    @JsonProperty("sessionCount")
    private int sessionCount;

    // ========================================================================
    // THREAD MANAGEMENT &amp; CONCURRENCY METRICS
    // ========================================================================

    /**
     * Number of currently active threads in the Jenkins agent JVM.
     * <p>Critical metric for monitoring concurrency and detecting thread pool exhaustion
     * or deadlock conditions that could impact build performance.</p>
     */
    @JsonProperty("activeThreadCount")
    private int activeThreadCount;

    /**
     * Total number of threads in the Jenkins agent JVM.
     * <p>Includes both active and idle threads, providing insight into thread pool
     * sizing and resource allocation efficiency.</p>
     */
    @JsonProperty("threadCount")
    private int threadCount;

    /**
     * Detailed thread information including deadlock detection data.
     * <p>Complex object containing thread pool statistics, deadlock information,
     * and thread state analysis for advanced concurrency monitoring.</p>
     *
     * <h4>Structure Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "totalThreads": 25,
     *   "deadlockedThreads": 0,
     *   "deadlockedThreadData": [
     *     {
     *       "id": 123,
     *       "name": "Build-Executor-Thread",
     *       "state": "BLOCKED",
     *       "stackTrace": ["java.lang.Thread.sleep(Native Method)", "..."]
     *     }
     *   ]
     * }
     * }
     */
    @JsonProperty("threads")
    private Map<String, Object> threads;

    // ========================================================================
    // SYSTEM PERFORMANCE METRICS
    // ========================================================================

    /**
     * System load average over the last minute.
     * <p>Unix-style load average indicating system utilization. Values above the number
     * of available processors suggest system overload and potential performance issues.</p>
     *
     * <h4>Interpretation Guidelines:</h4>
     * <ul>
     *   <li><strong>0.0 - 1.0:</strong> Low to moderate load</li>
     *   <li><strong>1.0 - 2.0:</strong> High load, monitor closely</li>
     *   <li><strong>&gt; 2.0:</strong> System overload, investigate immediately</li>
     * </ul>
     */
    @JsonProperty("systemLoadAverage")
    private double systemLoadAverage;

    /**
     * Current system CPU utilization as a percentage (0.0 to 1.0).
     * <p>Real-time CPU usage measurement for performance monitoring and capacity planning.
     * Values approaching 1.0 indicate CPU saturation and potential bottlenecks.</p>
     */
    @JsonProperty("systemCpuLoad")
    private double systemCpuLoad;

    /**
     * Number of CPU cores/processors available to the JVM.
     * <p>Hardware capacity metric used for load balancing decisions and performance
     * baseline calculations. Critical for understanding system scaling capabilities.</p>
     */
    @JsonProperty("availableProcessors")
    private int availableProcessors;

    // ========================================================================
    // JAVA RUNTIME ENVIRONMENT
    // ========================================================================

    /**
     * Java runtime version information.
     * <p>Java version details for compatibility analysis and security vulnerability assessment.</p>
     *
     * <h4>Example Values:</h4>
     * <ul>
     *   <li>{@code "17.0.2"} - Java 17 LTS</li>
     *   <li>{@code "11.0.16"} - Java 11 LTS</li>
     *   <li>{@code "1.8.0_345"} - Java 8</li>
     * </ul>
     */
    @JsonProperty("javaVersion")
    private String javaVersion;

    /**
     * Java Virtual Machine implementation details.
     * <p>JVM vendor and version information for performance tuning and compatibility analysis.</p>
     *
     * <h4>Example Values:</h4>
     * <ul>
     *   <li>{@code "OpenJDK 64-Bit Server VM"} - OpenJDK implementation</li>
     *   <li>{@code "Eclipse OpenJ9 VM"} - IBM J9 implementation</li>
     *   <li>{@code "Oracle HotSpot VM"} - Oracle JVM</li>
     * </ul>
     */
    @JsonProperty("jvmVersion")
    private String jvmVersion;

    /**
     * Process identifier for the Jenkins agent JVM.
     * <p>Operating system process ID for system administration and debugging purposes.</p>
     */
    @JsonProperty("pid")
    private String pid;

    // ========================================================================
    // JENKINS SERVER INFORMATION
    // ========================================================================

    /**
     * Jenkins server version and implementation details.
     * <p>Jenkins version information for compatibility tracking and security analysis.</p>
     *
     * <h4>Example Values:</h4>
     * <ul>
     *   <li>{@code "Jenkins/2.401.3"} - Jenkins LTS version</li>
     *   <li>{@code "Jenkins/2.420"} - Jenkins weekly release</li>
     * </ul>
     */
    @JsonProperty("serverInfo")
    private String serverInfo;

    /**
     * Jenkins web application context path.
     * <p>URL context path for the Jenkins installation, used for web interface access
     * and API endpoint construction.</p>
     */
    @JsonProperty("contextPath")
    private String contextPath;

    /**
     * Jenkins agent startup timestamp.
     * <p>ISO-8601 formatted timestamp indicating when the agent was started.
     * Used for uptime calculations and restart detection.</p>
     */
    @JsonProperty("startDate")
    private String startDate;

    // ========================================================================
    // MEMORY MANAGEMENT METRICS
    // ========================================================================

    /**
     * Comprehensive JVM memory utilization information.
     * <p>Detailed memory statistics including heap and non-heap usage for memory
     * leak detection and garbage collection analysis.</p>
     *
     * <h4>Structure Example:</h4>
     * {@snippet lang="json" :
     * {
     *   "heapUsed": 512000000,
     *   "heapMax": 2048000000,
     *   "heapCommitted": 1024000000,
     *   "nonHeapUsed": 128000000,
     *   "nonHeapMax": 256000000,
     *   "nonHeapCommitted": 192000000,
     *   "gcCollections": 45,
     *   "gcTime": 1250
     * }
     * }
     */
    @JsonProperty("memory")
    private Map<String, Object> memory;

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Agent health status indicator.
     * <p>Overall health status of the Jenkins agent for quick health assessment.</p>
     *
     * <h4>Possible Values:</h4>
     * <ul>
     *   <li><strong>healthy:</strong> Agent operating normally</li>
     *   <li><strong>warning:</strong> Minor issues detected</li>
     *   <li><strong>error:</strong> Critical problems requiring attention</li>
     *   <li><strong>offline:</strong> Agent disconnected or unavailable</li>
     * </ul>
     */
    @JsonProperty("status")
    private String status;

    /**
     * Human-readable status or error message.
     * <p>Descriptive message providing context for the current agent status,
     * particularly useful when status indicates problems or warnings.</p>
     */
    @JsonProperty("message")
    private String message;

    /**
     * Exception stack trace information for error diagnosis.
     * <p>Detailed stack trace data when errors occur, enabling root cause analysis
     * and debugging of agent-related issues.</p>
     *
     * <h4>Usage Example:</h4>
     * {@snippet lang="json" :
     * [
     *   "java.net.SocketException: Connection reset",
     *   "at java.net.SocketInputStream.read(SocketInputStream.java:186)",
     *   "at hudson.remoting.Channel$ReaderThread.run(Channel.java:1234)",
     *   "at java.lang.Thread.run(Thread.java:748)"
     * ]
     * }
     */
    @JsonProperty("stacktrace")
    private List<String> stacktrace;

    // ========================================================================
    // CONSTRUCTOR &amp; INITIALIZATION
    // ========================================================================

    /**
     * Default constructor initializing the DTO with proper type identification.
     *
     * <p>Creates a new AdditionalInfoAgent instance with the type set to "additional_info_agent"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "additional_info_agent" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * Jenkins agent information messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * AdditionalInfoAgent agentInfo = objectMapper.readValue(jsonMessage, AdditionalInfoAgent.class);
     *
     * // Manual instantiation for testing
     * AdditionalInfoAgent agentInfo = new AdditionalInfoAgent();
     * agentInfo.setJobName("security-pipeline");
     * agentInfo.setBuildNumber(123);
     * }
     *
     * @see TypedLog#setType(String)
     */
    public AdditionalInfoAgent() {
        setType("additional_info_agent");
        logger.debug("Initialized AdditionalInfoAgent with type=additional_info_agent");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - AGENT IDENTIFICATION
    // ========================================================================

    /**
     * Gets the Jenkins agent node identifier.
     * @return the agent node name, or {@code null} if not set
     */
    public String getNode() {
        return node;
    }

    /**
     * Sets the Jenkins agent node identifier.
     * @param node the agent node name
     */
    public void setNode(String node) {
        this.node = node;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - SESSION &amp; CONNECTION MANAGEMENT
    // ========================================================================

    /**
     * Gets the number of active Jenkins sessions on this agent.
     * @return the current session count
     */
    public int getSessionCount() {
        return sessionCount;
    }

    /**
     * Sets the number of active Jenkins sessions.
     * @param sessionCount the session count to set
     */
    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - THREAD MANAGEMENT
    // ========================================================================

    /**
     * Gets the number of currently active threads in the agent JVM.
     * @return the active thread count
     */
    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    /**
     * Sets the number of active threads.
     * @param activeThreadCount the active thread count to set
     */
    public void setActiveThreadCount(int activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    /**
     * Gets the total number of threads in the agent JVM.
     * @return the total thread count
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Sets the total thread count.
     * @param threadCount the thread count to set
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    // ========================================================================
    // PROPERTY ACCESSORS - SYSTEM PERFORMANCE METRICS
    // ========================================================================

    /**
     * Gets the system load average over the last minute.
     * @return the system load average, or 0.0 if not available
     */
    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    /**
     * Sets the system load average.
     * @param systemLoadAverage the load average to set
     */
    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    /**
     * Gets the current system CPU utilization (0.0 to 1.0).
     * @return the CPU load percentage, or 0.0 if not available
     */
    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    /**
     * Sets the system CPU load.
     * @param systemCpuLoad the CPU load to set (0.0 to 1.0)
     */
    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    /**
     * Gets the number of available processors.
     * @return the number of available processors
     */
    public int getAvailableProcessors() {
        return availableProcessors;
    }

    /**
     * Sets the number of available processors.
     * @param availableProcessors the number of available processors
     */
    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    /**
     * Gets the host name.
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name.
     * @param host the host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the operating system name.
     * @return the operating system name
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets the operating system name.
     * @param os the operating system name
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Gets the Java version.
     * @return the Java version
     */
    public String getJavaVersion() {
        return javaVersion;
    }

    /**
     * Sets the Java version.
     * @param javaVersion the Java version
     */
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    /**
     * Gets the JVM version.
     * @return the JVM version
     */
    public String getJvmVersion() {
        return jvmVersion;
    }

    /**
     * Sets the JVM version.
     * @param jvmVersion the JVM version
     */
    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    /**
     * Gets the process ID.
     * @return the process ID
     */
    public String getPid() {
        return pid;
    }

    /**
     * Sets the process ID.
     * @param pid the process ID
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * Gets the server information.
     * @return the server information
     */
    public String getServerInfo() {
        return serverInfo;
    }

    /**
     * Sets the server information.
     * @param serverInfo the server information
     */
    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Gets the context path.
     * @return the context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path.
     * @param contextPath the context path
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Gets the start date.
     * @return the start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     * @param startDate the start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the memory information map.
     * @return the memory information as a map of string keys to object values
     */
    public Map<String, Object> getMemory() {
        return memory;
    }

    /**
     * Sets the memory information map.
     * @param memory the memory information as a map of string keys to object values
     */
    public void setMemory(Map<String, Object> memory) {
        this.memory = memory;
    }

    /**
     * Gets the threads information map.
     * @return the threads information as a map of string keys to object values
     */
    public Map<String, Object> getThreads() {
        return threads;
    }

    /**
     * Sets the threads information map.
     * @param threads the threads information as a map of string keys to object values
     */
    public void setThreads(Map<String, Object> threads) {
        this.threads = threads;
    }

    /**
     * Gets the job name.
     * @return the job name
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the job name.
     * @param jobName the job name
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the current status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the stack trace.
     * @return the stack trace as a list of strings
     */
    public List<String> getStacktrace() {
        return stacktrace;
    }

    /**
     * Sets the stack trace.
     * @param stacktrace the stack trace as a list of strings
     */
    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    /**
     * Gets the build number.
     * @return the build number
     */
    @Override
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * Sets the build number.
     * @param buildNumber the build number
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    // ========================================================================
    // AI ANALYSIS INTEGRATION METHODS
    // ========================================================================

    /**
     * Generates structured content for AI-powered anomaly detection analysis.
     *
     * <p>This method transforms the raw agent metrics into a human-readable format
     * optimized for AI analysis. It prioritizes error information when present and
     * provides comprehensive system metrics for normal operation analysis.</p>
     *
     * <h4>Content Generation Strategy:</h4>
     * <p>The method employs a two-tier approach based on agent status:</p>
     * <ol>
     *   <li><strong>Error Mode:</strong> When status is "error", prioritizes error details &amp; stack traces</li>
     *   <li><strong>Normal Mode:</strong> Provides comprehensive system metrics &amp; performance data</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When errors are detected, the content focuses on diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: Agent connection lost during build execution
     * Stacktrace:
     * - java.net.SocketException: Connection reset
     * - at java.net.SocketInputStream.read(SocketInputStream.java:186)
     * - at hudson.remoting.Channel$ReaderThread.run(Channel.java:1234)
     * }
     *
     * <h4>Normal Content Format:</h4>
     * <p>For healthy agents, provides comprehensive system overview:</p>
     * {@snippet lang="text" :
     * Node: jenkins-agent-01
     * Session Count: 5
     * Active Thread Count: 12
     * Thread Count: 25
     * System Load Average: 1.45
     * System CPU Load: 0.67
     * Available Processors: 8
     * Host: jenkins-agent-01.company.com
     * OS: Linux 5.4.0-74-generic
     * Java Version: 17.0.2
     * JVM Version: OpenJDK 64-Bit Server VM
     * Memory:
     * - heapUsed: 512000000
     * - heapMax: 2048000000
     * - nonHeapUsed: 128000000
     * Threads:
     * - Total Threads: 25
     * - Deadlocked Threads: 0
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Performance Monitoring:</strong> CPU &amp; memory utilization trends</li>
     *   <li><strong>Capacity Planning:</strong> Resource usage patterns &amp; scaling needs</li>
     *   <li><strong>Error Detection:</strong> Exception patterns &amp; failure correlation</li>
     *   <li><strong>Thread Analysis:</strong> Deadlock detection &amp; concurrency issues</li>
     *   <li><strong>Infrastructure Health:</strong> Agent stability &amp; connection reliability</li>
     * </ul>
     *
     * @return formatted string containing agent information optimized for AI analysis.
     *         Returns error-focused content when status indicates problems,
     *         or comprehensive system metrics for normal operation.
     * @throws Exception if content generation fails due to data processing errors
     *
     * @see TypedLog#getContentToAnalyze()
     */
    @JsonIgnore
    @Override
    public String getContentToAnalyze() throws Exception {
        StringBuilder content = new StringBuilder();
        if (status != null && "error".equals(status)) {
            content.append("Error: ").append(message != null ? message : "N/A").append("\n");
            if (stacktrace != null && !stacktrace.isEmpty()) {
                content.append("Stacktrace:\n");
                for (String line : stacktrace) {
                    content.append("- ").append(line).append("\n");
                }
            }
            return content.toString();
        }

        content.append("Node: ").append(node != null ? node : "N/A").append("\n");
        content.append("Session Count: ").append(sessionCount).append("\n");
        content.append("Active Thread Count: ").append(activeThreadCount).append("\n");
        content.append("Thread Count: ").append(threadCount).append("\n");
        content.append("System Load Average: ").append(systemLoadAverage).append("\n");
        content.append("System CPU Load: ").append(systemCpuLoad).append("\n");
        content.append("Available Processors: ").append(availableProcessors).append("\n");
        content.append("Host: ").append(host != null ? host : "N/A").append("\n");
        content.append("OS: ").append(os != null ? os : "N/A").append("\n");
        content.append("Java Version: ").append(javaVersion != null ? javaVersion : "N/A").append("\n");
        content.append("JVM Version: ").append(jvmVersion != null ? jvmVersion : "N/A").append("\n");
        content.append("PID: ").append(pid != null ? pid : "N/A").append("\n");
        content.append("Server Info: ").append(serverInfo != null ? serverInfo : "N/A").append("\n");
        content.append("Context Path: ").append(contextPath != null ? contextPath : "N/A").append("\n");
        content.append("Start Date: ").append(startDate != null ? startDate : "N/A").append("\n");

        if (memory != null) {
            content.append("Memory:\n");
            for (Map.Entry<String, Object> entry : memory.entrySet()) {
                content.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        if (threads != null) {
            content.append("Threads:\n");
            content.append("- Total Threads: ").append(threads.getOrDefault("totalThreads", 0)).append("\n");
            content.append("- Deadlocked Threads: ").append(threads.getOrDefault("deadlockedThreads", 0)).append("\n");
            List<Map<String, Object>> deadlockedThreadData = (List<Map<String, Object>>) threads.getOrDefault("deadlockedThreadData", Collections.emptyList());
            if (!deadlockedThreadData.isEmpty()) {
                content.append("Deadlocked Thread Data:\n");
                for (Map<String, Object> thread : deadlockedThreadData) {
                    content.append("  - ID: ").append(thread.getOrDefault("id", "N/A")).append("\n");
                    content.append("    Name: ").append(thread.getOrDefault("name", "N/A")).append("\n");
                    content.append("    State: ").append(thread.getOrDefault("state", "N/A")).append("\n");
                    List<String> stackTrace = (List<String>) thread.getOrDefault("stackTrace", Collections.emptyList());
                    if (!stackTrace.isEmpty()) {
                        content.append("    Stack Trace:\n");
                        for (String line : stackTrace) {
                            content.append("      - ").append(line).append("\n");
                        }
                    }
                }
            }
        }

        return content.length() > 0 ? content.toString() : "No content available";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of agent metrics with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that agent information is grouped
     * with related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Build Correlation:</strong> Links agent metrics to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks agent performance trends per job</li>
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
     *   <li>Build sequence tracking</li>
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

    @Override
    public String toString() {
        return "AdditionalInfoAgent{" +
                "jobName='" + jobName + '\'' +
                ", buildNumber=" + buildNumber +
                ", node='" + node + '\'' +
                ", sessionCount=" + sessionCount +
                ", activeThreadCount=" + activeThreadCount +
                ", threadCount=" + threadCount +
                ", systemLoadAverage=" + systemLoadAverage +
                ", systemCpuLoad=" + systemCpuLoad +
                ", availableProcessors=" + availableProcessors +
                ", host='" + host + '\'' +
                ", os='" + os + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", jvmVersion='" + jvmVersion + '\'' +
                ", pid='" + pid + '\'' +
                ", serverInfo='" + serverInfo + '\'' +
                ", contextPath='" + contextPath + '\'' +
                ", startDate='" + startDate + '\'' +
                ", memory=" + memory +
                ", threads=" + threads +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", stacktrace=" + stacktrace +
                '}';
    }

    // ========================================================================
    // JSON SERIALIZATION &amp; DATA EXCHANGE
    // ========================================================================

    /**
     * Converts the agent information to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete agent information
     * including all system metrics, error details, and metadata. It uses Jackson's
     * ObjectMapper for comprehensive serialization with proper error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all agent metrics &amp; system information</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant agent information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "additional_info_agent",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "node": "jenkins-agent-01",
     *   "sessionCount": 5,
     *   "activeThreadCount": 12,
     *   "threadCount": 25,
     *   "systemLoadAverage": 1.45,
     *   "systemCpuLoad": 0.67,
     *   "availableProcessors": 8,
     *   "host": "jenkins-agent-01.company.com",
     *   "os": "Linux 5.4.0-74-generic",
     *   "javaVersion": "17.0.2",
     *   "jvmVersion": "OpenJDK 64-Bit Server VM",
     *   "pid": "12345",
     *   "serverInfo": "Jenkins/2.401.3",
     *   "contextPath": "/jenkins",
     *   "startDate": "2024-01-15T08:30:00Z",
     *   "memory": {
     *     "heapUsed": 512000000,
     *     "heapMax": 2048000000
     *   },
     *   "threads": {
     *     "totalThreads": 25,
     *     "deadlockedThreads": 0
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
     *   <li><strong>Logging:</strong> Structured logging of agent information</li>
     * </ul>
     *
     * @return JSON string representation of the agent information,
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