package com.diploma.inno.config;

import com.cdancy.jenkins.rest.JenkinsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jenkins REST API client configuration for the CI Anomaly Detector application.
 *
 * <p>This configuration class is responsible for creating and configuring a {@link JenkinsClient}
 * bean that enables the application to interact with Jenkins CI/CD server through REST API calls.
 * The client is used throughout the application to fetch build information, job details, and
 * trigger various Jenkins operations for anomaly detection and monitoring purposes.</p>
 *
 * <p><strong>Configuration Properties:</strong></p>
 * <p>This class relies on the following application properties for Jenkins connectivity:</p>
 * <ul>
 *   <li><strong>jenkins.url:</strong> The base URL of the Jenkins server (e.g., http://jenkins.company.com:8080)</li>
 *   <li><strong>jenkins.username:</strong> Username for Jenkins authentication</li>
 *   <li><strong>jenkins.password:</strong> Password or API token for Jenkins authentication</li>
 * </ul>
 *
 * <p><strong>Default Configuration (from application.properties):</strong></p>
 * {@snippet lang=text :
 * jenkins.url=${JENKINS_URL:http://130.193.49.138:8080}
 * jenkins.username=${JENKINS_USERNAME:admin}
 * jenkins.password=${JENKINS_PASSWORD:admin123}
 * }
 *
 * <p><strong>Authentication Methods:</strong></p>
 * <p>The Jenkins client supports multiple authentication mechanisms:</p>
 * <ul>
 *   <li><strong>Basic Authentication:</strong> Username and password (current implementation)</li>
 *   <li><strong>API Token:</strong> Username and API token (recommended for production)</li>
 *   <li><strong>Anonymous Access:</strong> For Jenkins instances with security disabled</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li>⚠️ <strong>WARNING:</strong> Credentials are currently configured via properties files</li>
 *   <li>🔒 <strong>RECOMMENDATION:</strong> Use environment variables or secure vaults in production</li>
 *   <li>🔑 <strong>BEST PRACTICE:</strong> Use Jenkins API tokens instead of passwords</li>
 *   <li>🛡️ <strong>SECURITY:</strong> Ensure Jenkins server uses HTTPS in production environments</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <p>The configured Jenkins client is used by various services in the application:</p>
 * <ul>
 *   <li>{@code JenkinsService} - Primary service for Jenkins operations</li>
 *   <li>{@code DashboardService} - For fetching build data and job information</li>
 *   <li>Scheduled tasks - For periodic synchronization with Jenkins</li>
 *   <li>Anomaly detection workflows - For analyzing build logs and results</li>
 * </ul>
 *
 * <p><strong>Supported Jenkins Operations:</strong></p>
 * <p>Through this client configuration, the application can perform:</p>
 * <ul>
 *   <li>Fetching job lists and job details</li>
 *   <li>Retrieving build information and build logs</li>
 *   <li>Triggering new builds</li>
 *   <li>Monitoring build status and progress</li>
 *   <li>Accessing Jenkins system information</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>Connection failures and authentication errors are handled by the consuming services.
 * Common issues include:</p>
 * <ul>
 *   <li>Network connectivity problems to Jenkins server</li>
 *   <li>Invalid credentials or expired API tokens</li>
 *   <li>Jenkins server downtime or maintenance</li>
 *   <li>Insufficient permissions for requested operations</li>
 * </ul>
 *
 * <p><strong>Production Deployment Notes:</strong></p>
 * <ul>
 *   <li>Ensure Jenkins server is accessible from the application deployment environment</li>
 *   <li>Configure appropriate network security (firewalls, VPNs) if needed</li>
 *   <li>Use connection pooling and timeout configurations for production workloads</li>
 *   <li>Monitor Jenkins API rate limits and implement appropriate retry mechanisms</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see JenkinsClient
 * @see com.diploma.inno.service.JenkinsService
 * @see com.diploma.inno.service.DashboardService
 */
@Configuration
public class JenkinsConfiguration {

    /**
     * The base URL of the Jenkins server.
     *
     * <p>This property is injected from the application configuration and specifies
     * the complete URL where the Jenkins server can be accessed. The URL should include
     * the protocol (http/https), hostname/IP address, and port number.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <ul>
     *   <li>{@code http://localhost:8080} - Local Jenkins instance</li>
     *   <li>{@code https://jenkins.company.com} - Production Jenkins with HTTPS</li>
     *   <li>{@code http://192.168.1.100:8080} - Jenkins on specific IP and port</li>
     * </ul>
     *
     * <p><strong>Configuration:</strong></p>
     * <p>Set via {@code jenkins.url} property in application.properties or environment variable {@code JENKINS_URL}</p>
     *
     * @see Value
     */
    @Value("${jenkins.url}")
    private String jenkinsUrl;

    /**
     * Username for Jenkins authentication.
     *
     * <p>This property contains the username used to authenticate with the Jenkins server.
     * The username should correspond to a valid Jenkins user account with appropriate
     * permissions to perform the required operations (reading jobs, triggering builds, etc.).</p>
     *
     * <p><strong>Permission Requirements:</strong></p>
     * <p>The Jenkins user should have at minimum:</p>
     * <ul>
     *   <li><strong>Overall/Read:</strong> To access Jenkins and view basic information</li>
     *   <li><strong>Job/Read:</strong> To view job configurations and build history</li>
     *   <li><strong>Job/Build:</strong> To trigger new builds (if required)</li>
     *   <li><strong>Job/Workspace:</strong> To access build artifacts and logs</li>
     * </ul>
     *
     * <p><strong>Configuration:</strong></p>
     * <p>Set via {@code jenkins.username} property in application.properties or environment variable {@code JENKINS_USERNAME}</p>
     *
     * @see Value
     */
    @Value("${jenkins.username}")
    private String username;

    /**
     * Password or API token for Jenkins authentication.
     *
     * <p>This property contains the authentication credential used in combination with
     * the username to authenticate with Jenkins. This can be either:</p>
     * <ul>
     *   <li><strong>User Password:</strong> The actual password for the Jenkins user account</li>
     *   <li><strong>API Token:</strong> A Jenkins-generated API token (recommended for security)</li>
     * </ul>
     *
     * <p><strong>API Token vs Password:</strong></p>
     * <ul>
     *   <li><strong>API Token (Recommended):</strong> More secure, can be revoked independently, doesn't expire with password changes</li>
     *   <li><strong>Password:</strong> Less secure, changes when user updates their password</li>
     * </ul>
     *
     * <p><strong>Generating Jenkins API Token:</strong></p>
     * <ol>
     *   <li>Log into Jenkins web interface</li>
     *   <li>Go to User → Configure → API Token</li>
     *   <li>Click "Add new Token" and generate</li>
     *   <li>Use the generated token as the password value</li>
     * </ol>
     *
     * <p><strong>Security Warning:</strong></p>
     * <p>⚠️ This credential provides access to Jenkins operations. Ensure it's stored securely
     * and not exposed in logs, version control, or configuration files in production environments.</p>
     *
     * <p><strong>Configuration:</strong></p>
     * <p>Set via {@code jenkins.password} property in application.properties or environment variable {@code JENKINS_PASSWORD}</p>
     *
     * @see Value
     */
    @Value("${jenkins.password}")
    private String password;

    /**
     * Creates and configures a Jenkins REST API client bean.
     *
     * <p>This method constructs a {@link JenkinsClient} instance using the jenkins-rest library,
     * configured with the Jenkins server endpoint and authentication credentials. The client
     * provides a programmatic interface to interact with Jenkins through its REST API.</p>
     *
     * <p><strong>Client Configuration:</strong></p>
     * <ul>
     *   <li><strong>Endpoint:</strong> Set to the configured Jenkins server URL</li>
     *   <li><strong>Authentication:</strong> Basic authentication using username:password format</li>
     *   <li><strong>Connection:</strong> HTTP client with default timeout and retry settings</li>
     * </ul>
     *
     * <p><strong>Authentication Format:</strong></p>
     * <p>The credentials are formatted as "username:password" following HTTP Basic Authentication
     * standards. This format is compatible with both regular passwords and Jenkins API tokens.</p>
     *
     * <p><strong>Client Capabilities:</strong></p>
     * <p>The returned client provides access to various Jenkins APIs:</p>
     * <ul>
     *   <li><strong>Jobs API:</strong> {@code client.api().jobsApi()} - Job management and build operations</li>
     *   <li><strong>System API:</strong> {@code client.api().systemApi()} - Jenkins system information</li>
     *   <li><strong>Queue API:</strong> {@code client.api().queueApi()} - Build queue management</li>
     *   <li><strong>Plugin API:</strong> {@code client.api().pluginManagerApi()} - Plugin information</li>
     * </ul>
     *
     * <p><strong>Usage Examples:</strong></p>
     * {@snippet lang=TEXT :
     * // Injecting the client in a service
     * {@literal @}Autowired
     * private JenkinsClient jenkinsClient;
     *
     * // Getting all jobs
     * List&lt;Job&gt; jobs = jenkinsClient.api().jobsApi().jobList("").jobs();
     *
     * // Triggering a build
     * jenkinsClient.api().jobsApi().build("", "job-name");
     *
     * // Getting build information
     * BuildInfo buildInfo = jenkinsClient.api().jobsApi().buildInfo("", "job-name", 123);
     * }
     *
     * <p><strong>Error Handling:</strong></p>
     * <p>The client may throw exceptions for various scenarios:</p>
     * <ul>
     *   <li><strong>Connection errors:</strong> Network issues, server unavailable</li>
     *   <li><strong>Authentication errors:</strong> Invalid credentials, insufficient permissions</li>
     *   <li><strong>API errors:</strong> Invalid requests, Jenkins internal errors</li>
     * </ul>
     *
     * <p><strong>Thread Safety:</strong></p>
     * <p>The Jenkins client is thread-safe and can be shared across multiple threads
     * and services within the Spring application context.</p>
     *
     * @return a configured {@link JenkinsClient} instance ready for Jenkins API operations
     *
     * @throws IllegalArgumentException if the Jenkins URL is malformed
     * @throws RuntimeException if client initialization fails due to configuration issues
     *
     * @see JenkinsClient
     * @see JenkinsClient.Builder
     * @see Bean
     */
    @Bean
    public JenkinsClient jenkinsClient() {
        return JenkinsClient.builder()
                .endPoint(jenkinsUrl)
                .credentials(username + ":" + password)
                .build();
    }


    /**
     * Default constructor for JenkinsConfiguration.
     */
    public JenkinsConfiguration() {
    }
}
