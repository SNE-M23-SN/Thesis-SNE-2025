package com.diploma.inno.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Data Transfer Object for Jenkins controller system information &amp; resource monitoring in CI/CD anomaly detection.
 *
 * <p>This DTO captures comprehensive Jenkins controller (master) runtime information including memory management,
 * system performance, thread utilization, disk space, and operational health metrics. It serves as a critical
 * component for monitoring Jenkins controller infrastructure and detecting system-level anomalies that could
 * impact CI/CD pipeline performance, stability, and security.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Memory Management:</strong> Heap, non-heap, PermGen &amp; physical memory monitoring</li>
 *   <li><strong>System Performance:</strong> CPU load, thread counts &amp; system load averages</li>
 *   <li><strong>Resource Monitoring:</strong> Disk space, swap usage &amp; processor availability</li>
 *   <li><strong>Session Management:</strong> Active HTTP sessions &amp; connection tracking</li>
 *   <li><strong>Infrastructure Health:</strong> System status, error detection &amp; diagnostic information</li>
 * </ul>
 *
 * <h2>Data Collection Sources</h2>
 * <p>This DTO aggregates information from multiple Jenkins controller subsystems:</p>
 * <ul>
 *   <li><strong>JVM Memory Management:</strong> Heap, non-heap, PermGen &amp; garbage collection metrics</li>
 *   <li><strong>Operating System:</strong> CPU utilization, load averages &amp; physical memory usage</li>
 *   <li><strong>Jenkins Controller:</strong> HTTP sessions, thread pools &amp; server configuration</li>
 *   <li><strong>Storage Systems:</strong> Disk space monitoring &amp; swap space utilization</li>
 *   <li><strong>Error Handling:</strong> Exception tracking, stack traces &amp; failure diagnostics</li>
 * </ul>
 *
 * <h2>JSON Structure &amp; Serialization</h2>
 * <p>The DTO supports comprehensive JSON serialization for data exchange:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "additional_info_controller",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "usedMemory": "512MB",
 *   "maxMemory": "2048MB",
 *   "usedPermGen": "128MB",
 *   "maxPermGen": "256MB",
 *   "usedNonHeap": "256MB",
 *   "usedPhysicalMemory": "4096MB",
 *   "usedSwapSpace": "0MB",
 *   "sessionsCount": 15,
 *   "activeHttpThreadsCount": 8,
 *   "threadsCount": 45,
 *   "systemLoadAverage": 1.25,
 *   "systemCpuLoad": 0.45,
 *   "availableProcessors": 8,
 *   "host": "jenkins-controller.company.com",
 *   "os": "Linux 5.4.0-74-generic",
 *   "javaVersion": "17.0.2",
 *   "jvmVersion": "OpenJDK 64-Bit Server VM",
 *   "pid": "1234",
 *   "serverInfo": "Jenkins/2.401.3",
 *   "contextPath": "/jenkins",
 *   "startDate": "2024-01-15T08:00:00Z",
 *   "freeDiskSpaceInJenkinsDirMb": 50240,
 *   "status": "healthy"
 * }
 * }
 *
 * <h2>Error Handling &amp; Diagnostics</h2>
 * <p>When controller errors occur, the DTO captures detailed diagnostic information:</p>
 * {@snippet lang="json" :
 * {
 *   "type": "additional_info_controller",
 *   "job_name": "security-pipeline",
 *   "build_number": 123,
 *   "status": "error",
 *   "message": "OutOfMemoryError: Java heap space",
 *   "stacktrace": [
 *     "java.lang.OutOfMemoryError: Java heap space",
 *     "at java.util.Arrays.copyOf(Arrays.java:3332)",
 *     "at hudson.model.Queue.maintain(Queue.java:1456)"
 *   ]
 * }
 * }
 *
 * <h2>AI Analysis Integration</h2>
 * <p>The DTO provides structured content for AI-powered anomaly detection:</p>
 * <ul>
 *   <li><strong>Memory Analysis:</strong> Heap utilization patterns &amp; memory leak detection</li>
 *   <li><strong>Performance Monitoring:</strong> CPU load trends &amp; system bottleneck identification</li>
 *   <li><strong>Capacity Planning:</strong> Resource usage forecasting &amp; scaling recommendations</li>
 *   <li><strong>Health Assessment:</strong> System stability analysis &amp; failure prediction</li>
 * </ul>
 *
 * <h2>Monitoring Use Cases</h2>
 * <ul>
 *   <li><strong>Infrastructure Monitoring:</strong> Controller health &amp; resource utilization tracking</li>
 *   <li><strong>Performance Optimization:</strong> Memory tuning &amp; thread pool optimization</li>
 *   <li><strong>Capacity Planning:</strong> Resource scaling decisions &amp; hardware requirements</li>
 *   <li><strong>Security Analysis:</strong> Anomalous resource consumption &amp; potential attacks</li>
 *   <li><strong>Troubleshooting:</strong> System diagnostics &amp; root cause analysis</li>
 * </ul>
 *
 * <h2>Memory Management Insights</h2>
 * <p>The DTO provides comprehensive memory monitoring capabilities:</p>
 * <ul>
 *   <li><strong>Heap Memory:</strong> Used vs. maximum heap allocation for garbage collection analysis</li>
 *   <li><strong>PermGen Space:</strong> Permanent generation usage for class loading monitoring</li>
 *   <li><strong>Non-Heap Memory:</strong> Method area, code cache &amp; compressed class space</li>
 *   <li><strong>Physical Memory:</strong> System-level memory utilization &amp; availability</li>
 *   <li><strong>Swap Space:</strong> Virtual memory usage indicating memory pressure</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This DTO integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>LogMessageListener:</strong> Receives &amp; processes controller information messages</li>
 *   <li><strong>SimpleDbChatMemory:</strong> Stores controller metrics for historical analysis</li>
 *   <li><strong>AI Analysis Engine:</strong> Provides structured data for anomaly detection</li>
 *   <li><strong>Monitoring Dashboard:</strong> Supplies real-time controller health metrics</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see TypedLog
 * @see com.diploma.inno.component.LogMessageListener
 * @see com.diploma.inno.component.SimpleDbChatMemory
 * @see AdditionalInfoAgent
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalInfoController extends TypedLog {
    // ========================================================================
    // CONSTANTS &amp; UTILITIES
    // ========================================================================

    /** Logger instance for this DTO class. */
    private static final Logger logger = LoggerFactory.getLogger(AdditionalInfoController.class);

    /** Jackson ObjectMapper for JSON serialization operations. */
    private static final ObjectMapper mapper = new ObjectMapper();

    // ========================================================================
    // JENKINS BUILD CONTEXT FIELDS
    // ========================================================================

    /**
     * Jenkins job name associated with this controller information.
     * <p>Used for correlating controller metrics with specific Jenkins jobs and builds.</p>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Jenkins build number for this controller information snapshot.
     * <p>Enables tracking of controller performance across different build executions.</p>
     */
    @JsonProperty("build_number")
    private int buildNumber;

    // ========================================================================
    // MEMORY MANAGEMENT METRICS
    // ========================================================================

    /**
     * Current heap memory usage in human-readable format.
     * <p>Indicates the amount of heap memory currently being used by the Jenkins controller JVM.
     * Critical for memory leak detection and garbage collection analysis.</p>
     *
     * <h4>Example Values:</h4>
     * <ul>
     *   <li>{@code "512MB"} - Moderate heap usage</li>
     *   <li>{@code "1.5GB"} - High heap usage requiring monitoring</li>
     *   <li>{@code "3.8GB"} - Near-maximum heap usage, potential memory pressure</li>
     * </ul>
     */
    @JsonProperty("usedMemory")
    private String usedMemory;

    /**
     * Maximum heap memory allocation in human-readable format.
     * <p>Represents the maximum heap size configured for the Jenkins controller JVM.
     * Used for calculating heap utilization percentages and memory pressure analysis.</p>
     */
    @JsonProperty("maxMemory")
    private String maxMemory;

    /**
     * Current PermGen (Permanent Generation) memory usage.
     * <p>Indicates memory used for storing class metadata and constant pool information.
     * High PermGen usage can indicate class loading issues or memory leaks in older JVM versions.</p>
     *
     * <h4>Note:</h4>
     * <p>PermGen was replaced by Metaspace in Java 8+, but this field may still be populated
     * for compatibility with older Jenkins installations.</p>
     */
    @JsonProperty("usedPermGen")
    private String usedPermGen;

    /**
     * Maximum PermGen memory allocation.
     * <p>Maximum size allocated for permanent generation space in older JVM versions.</p>
     */
    @JsonProperty("maxPermGen")
    private String maxPermGen;

    /**
     * Current non-heap memory usage.
     * <p>Memory used for method area, code cache, compressed class space, and other
     * non-heap regions. Important for monitoring JVM internal memory structures.</p>
     */
    @JsonProperty("usedNonHeap")
    private String usedNonHeap;

    /**
     * Current physical memory usage by the Jenkins controller process.
     * <p>Total physical RAM being used by the Jenkins controller, including heap,
     * non-heap, and native memory allocations.</p>
     */
    @JsonProperty("usedPhysicalMemory")
    private String usedPhysicalMemory;

    /**
     * Current swap space usage.
     * <p>Amount of virtual memory (swap) being used by the system. Non-zero values
     * may indicate memory pressure and potential performance degradation.</p>
     */
    @JsonProperty("usedSwapSpace")
    private String usedSwapSpace;

    // ========================================================================
    // SESSION &amp; CONNECTION MANAGEMENT
    // ========================================================================

    /**
     * Number of active HTTP sessions on the Jenkins controller.
     * <p>Indicates current user activity and connection load for capacity planning
     * and performance monitoring.</p>
     */
    @JsonProperty("sessionsCount")
    private Integer sessionsCount;

    /**
     * Number of active HTTP threads handling requests.
     * <p>Critical metric for monitoring web server performance and detecting
     * thread pool exhaustion that could impact user experience.</p>
     */
    @JsonProperty("activeHttpThreadsCount")
    private Integer activeHttpThreadsCount;

    /**
     * Total number of threads in the Jenkins controller JVM.
     * <p>Includes all threads (HTTP, build executors, background tasks) for
     * comprehensive thread pool monitoring and concurrency analysis.</p>
     */
    @JsonProperty("threadsCount")
    private Integer threadsCount;

    // ========================================================================
    // SYSTEM PERFORMANCE METRICS
    // ========================================================================

    /**
     * System load average over the last minute.
     * <p>Unix-style load average indicating overall system utilization. Values above
     * the number of available processors suggest system overload.</p>
     *
     * <h4>Interpretation Guidelines:</h4>
     * <ul>
     *   <li><strong>0.0 - 1.0:</strong> Low to moderate system load</li>
     *   <li><strong>1.0 - 2.0:</strong> High load, monitor performance closely</li>
     *   <li><strong>&gt; 2.0:</strong> System overload, investigate immediately</li>
     * </ul>
     */
    @JsonProperty("systemLoadAverage")
    private Double systemLoadAverage;

    /**
     * Current system CPU utilization as a percentage (0.0 to 1.0).
     * <p>Real-time CPU usage measurement for performance monitoring and capacity planning.
     * Values approaching 1.0 indicate CPU saturation and potential bottlenecks.</p>
     */
    @JsonProperty("systemCpuLoad")
    private Double systemCpuLoad;

    /**
     * Number of CPU cores/processors available to the JVM.
     * <p>Hardware capacity metric used for load balancing decisions and performance
     * baseline calculations. Essential for understanding system scaling capabilities.</p>
     */
    @JsonProperty("availableProcessors")
    private Integer availableProcessors;

    // ========================================================================
    // INFRASTRUCTURE INFORMATION
    // ========================================================================

    /**
     * Fully qualified domain name or IP address of the controller host.
     * <p>Network identifier for the Jenkins controller server, essential for
     * infrastructure mapping and network troubleshooting.</p>
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

    /**
     * Java runtime version information.
     * <p>Java version details for compatibility analysis and security vulnerability assessment.</p>
     */
    @JsonProperty("javaVersion")
    private String javaVersion;

    /**
     * Java Virtual Machine implementation details.
     * <p>JVM vendor and version information for performance tuning and compatibility analysis.</p>
     */
    @JsonProperty("jvmVersion")
    private String jvmVersion;

    /**
     * Process identifier for the Jenkins controller JVM.
     * <p>Operating system process ID for system administration and debugging purposes.</p>
     */
    @JsonProperty("pid")
    private String pid;

    /**
     * Jenkins server version and implementation details.
     * <p>Jenkins version information for compatibility tracking and security analysis.</p>
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
     * Jenkins controller startup timestamp.
     * <p>ISO-8601 formatted timestamp indicating when the controller was started.
     * Used for uptime calculations and restart detection.</p>
     */
    @JsonProperty("startDate")
    private String startDate;

    // ========================================================================
    // STORAGE &amp; DISK MANAGEMENT
    // ========================================================================

    /**
     * Available disk space in Jenkins home directory (in megabytes).
     * <p>Critical metric for monitoring disk space availability and preventing
     * build failures due to insufficient storage. Low values require immediate attention.</p>
     *
     * <h4>Monitoring Thresholds:</h4>
     * <ul>
     *   <li><strong>&gt; 10GB (10240MB):</strong> Healthy disk space</li>
     *   <li><strong>5-10GB (5120-10240MB):</strong> Monitor closely</li>
     *   <li><strong>&lt; 5GB (5120MB):</strong> Critical - cleanup required</li>
     * </ul>
     */
    @JsonProperty("freeDiskSpaceInJenkinsDirMb")
    private Long freeDiskSpaceInJenkinsDirMb;

    // ========================================================================
    // ERROR HANDLING &amp; DIAGNOSTICS
    // ========================================================================

    /**
     * Controller health status indicator.
     * <p>Overall health status of the Jenkins controller for quick health assessment.</p>
     *
     * <h4>Possible Values:</h4>
     * <ul>
     *   <li><strong>healthy:</strong> Controller operating normally</li>
     *   <li><strong>warning:</strong> Minor issues detected, monitor closely</li>
     *   <li><strong>error:</strong> Critical problems requiring immediate attention</li>
     *   <li><strong>degraded:</strong> Performance issues affecting operations</li>
     * </ul>
     */
    @JsonProperty("status")
    private String status;

    /**
     * Human-readable status or error message.
     * <p>Descriptive message providing context for the current controller status,
     * particularly useful when status indicates problems or warnings.</p>
     */
    @JsonProperty("message")
    private String message;

    /**
     * Exception stack trace information for error diagnosis.
     * <p>Detailed stack trace data when errors occur, enabling root cause analysis
     * and debugging of controller-related issues.</p>
     *
     * <h4>Usage Example:</h4>
     * {@snippet lang="json" :
     * [
     *   "java.lang.OutOfMemoryError: Java heap space",
     *   "at java.util.Arrays.copyOf(Arrays.java:3332)",
     *   "at hudson.model.Queue.maintain(Queue.java:1456)",
     *   "at hudson.model.Queue$MaintainTask.run(Queue.java:1234)"
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
     * <p>Creates a new AdditionalInfoController instance with the type set to "additional_info_controller"
     * for proper message routing and processing in the Jenkins anomaly detection pipeline.</p>
     *
     * <h4>Initialization Process:</h4>
     * <ol>
     *   <li>Sets the log type to "additional_info_controller" for message classification</li>
     *   <li>Logs initialization for debugging and audit purposes</li>
     *   <li>Prepares the instance for data population and serialization</li>
     * </ol>
     *
     * <h4>Usage in Pipeline:</h4>
     * <p>This constructor is typically called during JSON deserialization when
     * Jenkins controller information messages are received via RabbitMQ:</p>
     *
     * {@snippet lang="java" :
     * // Automatic instantiation during JSON deserialization
     * AdditionalInfoController controllerInfo = objectMapper.readValue(jsonMessage, AdditionalInfoController.class);
     *
     * // Manual instantiation for testing
     * AdditionalInfoController controllerInfo = new AdditionalInfoController();
     * controllerInfo.setJobName("security-pipeline");
     * controllerInfo.setBuildNumber(123);
     * controllerInfo.setUsedMemory("512MB");
     * controllerInfo.setMaxMemory("2048MB");
     * }
     *
     * @see TypedLog#setType(String)
     */
    public AdditionalInfoController() {
        setType("additional_info_controller");
        logger.debug("Initialized AdditionalInfoController with type=additional_info_controller");
    }

    // ========================================================================
    // PROPERTY ACCESSORS - MEMORY MANAGEMENT
    // ========================================================================

    /**
     * Gets the current heap memory usage in human-readable format.
     * @return the used heap memory (e.g., "512MB"), or {@code null} if not available
     */
    public String getUsedMemory() {
        return usedMemory;
    }

    /**
     * Sets the current heap memory usage.
     * @param usedMemory the used memory in human-readable format
     */
    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    /**
     * Gets the maximum heap memory allocation.
     * @return the maximum heap memory (e.g., "2048MB"), or {@code null} if not available
     */
    public String getMaxMemory() {
        return maxMemory;
    }

    /**
     * Sets the maximum heap memory allocation.
     * @param maxMemory the maximum memory in human-readable format
     */
    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    /**
     * Gets the current PermGen memory usage.
     * @return the used PermGen memory, or {@code null} if not available
     */
    public String getUsedPermGen() {
        return usedPermGen;
    }

    /**
     * Sets the current PermGen memory usage.
     * @param usedPermGen the used PermGen memory
     */
    public void setUsedPermGen(String usedPermGen) {
        this.usedPermGen = usedPermGen;
    }

    /**
     * Gets the maximum PermGen memory allocation.
     * @return the maximum PermGen memory, or {@code null} if not available
     */
    public String getMaxPermGen() {
        return maxPermGen;
    }

    /**
     * Sets the maximum PermGen memory allocation.
     * @param maxPermGen the maximum PermGen memory
     */
    public void setMaxPermGen(String maxPermGen) {
        this.maxPermGen = maxPermGen;
    }

    /**
     * Gets the current non-heap memory usage.
     * @return the used non-heap memory, or {@code null} if not available
     */
    public String getUsedNonHeap() {
        return usedNonHeap;
    }

    /**
     * Sets the current non-heap memory usage.
     * @param usedNonHeap the used non-heap memory
     */
    public void setUsedNonHeap(String usedNonHeap) {
        this.usedNonHeap = usedNonHeap;
    }

    /**
     * Gets the current physical memory usage.
     * @return the used physical memory, or {@code null} if not available
     */
    public String getUsedPhysicalMemory() {
        return usedPhysicalMemory;
    }

    /**
     * Sets the current physical memory usage.
     * @param usedPhysicalMemory the used physical memory
     */
    public void setUsedPhysicalMemory(String usedPhysicalMemory) {
        this.usedPhysicalMemory = usedPhysicalMemory;
    }

    /**
     * Gets the current swap space usage.
     * @return the used swap space, or {@code null} if not available
     */
    public String getUsedSwapSpace() {
        return usedSwapSpace;
    }

    /**
     * Sets the current swap space usage.
     * @param usedSwapSpace the used swap space
     */
    public void setUsedSwapSpace(String usedSwapSpace) {
        this.usedSwapSpace = usedSwapSpace;
    }

    /**
     * Gets the current sessions count.
     * @return the sessions count
     */
    public Integer getSessionsCount() {
        return sessionsCount;
    }

    /**
     * Sets the current sessions count.
     * @param sessionsCount the sessions count
     */
    public void setSessionsCount(Integer sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    /**
     * Gets the current Active Http Threads Count.
     * @return Active Http Threads Count
     */
    public Integer getActiveHttpThreadsCount() {
        return activeHttpThreadsCount;
    }

    /**
     * Sets the current Active Http Threads Count.
     * @param activeHttpThreadsCount the Active Http Threads Count
     */
    public void setActiveHttpThreadsCount(Integer activeHttpThreadsCount) {
        this.activeHttpThreadsCount = activeHttpThreadsCount;
    }

    /**
     * Gets the current active HTTP threads count.
     * @return the active HTTP threads count
     */
    public Integer getThreadsCount() {
        return threadsCount;
    }

    /**
     * Sets the current Threads Count.
     * @param threadsCount the Threads Count
     */
    public void setThreadsCount(Integer threadsCount) {
        this.threadsCount = threadsCount;
    }

    /**
     * Gets the current system load average.
     * @return the system load average
     */
    public Double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    /**
     * Sets the current System Load Average.
     * @param systemLoadAverage the System Load Average
     */
    public void setSystemLoadAverage(Double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    /**
     * Gets the current system CPU load.
     * @return the system CPU load
     */
    public Double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    /**
     * Sets the current System Cpu Load.
     * @param systemCpuLoad the  System Cpu Load
     */
    public void setSystemCpuLoad(Double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }


    /**
     * Gets the number of available processors.
     * @return the number of available processors
     */
    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    /**
     * Sets the current Available Processors.
     * @param availableProcessors the Available Processors
     */
    public void setAvailableProcessors(Integer availableProcessors) {
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
     * Sets the current Host.
     * @param host the Host
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
     * Sets the current OS.
     * @param os the OS
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
     * Gets the free disk space in Jenkins directory in megabytes.
     * @return the free disk space in Jenkins directory in MB
     */
    public Long getFreeDiskSpaceInJenkinsDirMb() {
        return freeDiskSpaceInJenkinsDirMb;
    }

    /**
     * Sets the free disk space in Jenkins directory in megabytes.
     * @param freeDiskSpaceInJenkinsDirMb the free disk space in Jenkins directory in MB
     */
    public void setFreeDiskSpaceInJenkinsDirMb(Long freeDiskSpaceInJenkinsDirMb) {
        this.freeDiskSpaceInJenkinsDirMb = freeDiskSpaceInJenkinsDirMb;
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
     * <p>This method transforms the raw controller metrics into a human-readable format
     * optimized for AI analysis. It prioritizes error information when present and
     * provides comprehensive system metrics for normal operation analysis.</p>
     *
     * <h4>Content Generation Strategy:</h4>
     * <p>The method employs a two-tier approach based on controller status:</p>
     * <ol>
     *   <li><strong>Error Mode:</strong> When status is "error", prioritizes error details &amp; stack traces</li>
     *   <li><strong>Normal Mode:</strong> Provides comprehensive system metrics &amp; performance data</li>
     * </ol>
     *
     * <h4>Error Content Format:</h4>
     * <p>When errors are detected, the content focuses on diagnostic information:</p>
     * {@snippet lang="text" :
     * Error: OutOfMemoryError: Java heap space
     * Stacktrace:
     * - java.lang.OutOfMemoryError: Java heap space
     * - at java.util.Arrays.copyOf(Arrays.java:3332)
     * - at hudson.model.Queue.maintain(Queue.java:1456)
     * }
     *
     * <h4>Normal Content Format:</h4>
     * <p>For healthy controllers, provides comprehensive system overview:</p>
     * {@snippet lang="text" :
     * Used Memory: 512MB
     * Max Memory: 2048MB
     * Used PermGen: 128MB
     * Max PermGen: 256MB
     * Used Non-Heap: 256MB
     * Used Physical Memory: 4096MB
     * Used Swap Space: 0MB
     * Sessions Count: 15
     * Active HTTP Threads Count: 8
     * Threads Count: 45
     * System Load Average: 1.25
     * System CPU Load: 0.45
     * Available Processors: 8
     * Host: jenkins-controller.company.com
     * OS: Linux 5.4.0-74-generic
     * Java Version: 17.0.2
     * JVM Version: OpenJDK 64-Bit Server VM
     * Free Disk Space in Jenkins Dir (MB): 50240
     * }
     *
     * <h4>AI Analysis Applications:</h4>
     * <ul>
     *   <li><strong>Memory Analysis:</strong> Heap utilization patterns &amp; memory leak detection</li>
     *   <li><strong>Performance Monitoring:</strong> CPU &amp; system load trend analysis</li>
     *   <li><strong>Capacity Planning:</strong> Resource usage forecasting &amp; scaling decisions</li>
     *   <li><strong>Error Detection:</strong> Exception patterns &amp; failure correlation</li>
     *   <li><strong>Infrastructure Health:</strong> Controller stability &amp; operational status</li>
     * </ul>
     *
     * @return formatted string containing controller information optimized for AI analysis.
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

        if (usedMemory != null) content.append("Used Memory: ").append(usedMemory).append("\n");
        if (maxMemory != null) content.append("Max Memory: ").append(maxMemory).append("\n");
        if (usedPermGen != null) content.append("Used PermGen: ").append(usedPermGen).append("\n");
        if (maxPermGen != null) content.append("Max PermGen: ").append(maxPermGen).append("\n");
        if (usedNonHeap != null) content.append("Used Non-Heap: ").append(usedNonHeap).append("\n");
        if (usedPhysicalMemory != null) content.append("Used Physical Memory: ").append(usedPhysicalMemory).append("\n");
        if (usedSwapSpace != null) content.append("Used Swap Space: ").append(usedSwapSpace).append("\n");
        if (sessionsCount != null) content.append("Sessions Count: ").append(sessionsCount).append("\n");
        if (activeHttpThreadsCount != null) content.append("Active HTTP Threads Count: ").append(activeHttpThreadsCount).append("\n");
        if (threadsCount != null) content.append("Threads Count: ").append(threadsCount).append("\n");
        if (systemLoadAverage != null) content.append("System Load Average: ").append(systemLoadAverage).append("\n");
        if (systemCpuLoad != null) content.append("System CPU Load: ").append(systemCpuLoad).append("\n");
        if (availableProcessors != null) content.append("Available Processors: ").append(availableProcessors).append("\n");
        if (host != null) content.append("Host: ").append(host).append("\n");
        if (os != null) content.append("OS: ").append(os).append("\n");
        if (javaVersion != null) content.append("Java Version: ").append(javaVersion).append("\n");
        if (jvmVersion != null) content.append("JVM Version: ").append(jvmVersion).append("\n");
        if (pid != null) content.append("PID: ").append(pid).append("\n");
        if (serverInfo != null) content.append("Server Info: ").append(serverInfo).append("\n");
        if (contextPath != null) content.append("Context Path: ").append(contextPath).append("\n");
        if (startDate != null) content.append("Start Date: ").append(startDate).append("\n");
        if (freeDiskSpaceInJenkinsDirMb != null) content.append("Free Disk Space in Jenkins Dir (MB): ").append(freeDiskSpaceInJenkinsDirMb).append("\n");

        return content.length() > 0 ? content.toString() : "No content available";
    }

    /**
     * Provides the conversation identifier for chat memory correlation.
     *
     * <p>Returns the Jenkins job name as the conversation identifier, enabling
     * proper correlation of controller metrics with specific Jenkins jobs in the
     * AI analysis pipeline. This ensures that controller information is grouped
     * with related build logs and analysis results.</p>
     *
     * <h4>Conversation Grouping Strategy:</h4>
     * <p>Using job name as conversation ID enables:</p>
     * <ul>
     *   <li><strong>Build Correlation:</strong> Links controller metrics to specific Jenkins jobs</li>
     *   <li><strong>Historical Analysis:</strong> Tracks controller performance trends per job</li>
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
     * @see com.diploma.inno.component.SimpleDbChatMemory#add(String, List)
     */
    @JsonIgnore
    @Override
    public String getConversationId() {
        return jobName;
    }

    @Override
    public String toString() {
        return "AdditionalInfoController{" +
                "jobName='" + jobName + '\'' +
                ", buildNumber=" + buildNumber +
                ", usedMemory='" + usedMemory + '\'' +
                ", maxMemory='" + maxMemory + '\'' +
                ", usedPermGen='" + usedPermGen + '\'' +
                ", maxPermGen='" + maxPermGen + '\'' +
                ", usedNonHeap='" + usedNonHeap + '\'' +
                ", usedPhysicalMemory='" + usedPhysicalMemory + '\'' +
                ", usedSwapSpace='" + usedSwapSpace + '\'' +
                ", sessionsCount=" + sessionsCount +
                ", activeHttpThreadsCount=" + activeHttpThreadsCount +
                ", threadsCount=" + threadsCount +
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
                ", freeDiskSpaceInJenkinsDirMb=" + freeDiskSpaceInJenkinsDirMb +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", stacktrace=" + stacktrace +
                '}';
    }


    // ========================================================================
    // JSON SERIALIZATION &amp; DATA EXCHANGE
    // ========================================================================

    /**
     * Converts the controller information to JSON format for data exchange &amp; storage.
     *
     * <p>This method provides robust JSON serialization of the complete controller information
     * including all system metrics, memory details, error information, and metadata. It uses
     * Jackson's ObjectMapper for comprehensive serialization with proper error handling.</p>
     *
     * <h4>Serialization Features:</h4>
     * <ul>
     *   <li><strong>Complete Data:</strong> Serializes all controller metrics &amp; system information</li>
     *   <li><strong>Null Handling:</strong> Excludes null values for cleaner JSON output</li>
     *   <li><strong>Error Recovery:</strong> Returns empty JSON object on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ul>
     *
     * <h4>Output Format:</h4>
     * <p>The JSON output includes all relevant controller information:</p>
     * {@snippet lang="json" :
     * {
     *   "type": "additional_info_controller",
     *   "job_name": "security-pipeline",
     *   "build_number": 123,
     *   "usedMemory": "512MB",
     *   "maxMemory": "2048MB",
     *   "usedPermGen": "128MB",
     *   "maxPermGen": "256MB",
     *   "usedNonHeap": "256MB",
     *   "usedPhysicalMemory": "4096MB",
     *   "usedSwapSpace": "0MB",
     *   "sessionsCount": 15,
     *   "activeHttpThreadsCount": 8,
     *   "threadsCount": 45,
     *   "systemLoadAverage": 1.25,
     *   "systemCpuLoad": 0.45,
     *   "availableProcessors": 8,
     *   "host": "jenkins-controller.company.com",
     *   "os": "Linux 5.4.0-74-generic",
     *   "javaVersion": "17.0.2",
     *   "jvmVersion": "OpenJDK 64-Bit Server VM",
     *   "pid": "1234",
     *   "serverInfo": "Jenkins/2.401.3",
     *   "contextPath": "/jenkins",
     *   "startDate": "2024-01-15T08:00:00Z",
     *   "freeDiskSpaceInJenkinsDirMb": 50240,
     *   "status": "healthy"
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
     *   <li><strong>Logging:</strong> Structured logging of controller information</li>
     * </ul>
     *
     * @return JSON string representation of the controller information,
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