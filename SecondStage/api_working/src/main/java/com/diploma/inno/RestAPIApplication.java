package com.diploma.inno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the CI Anomaly Detector system.
 *
 * <p>This class serves as the entry point for a comprehensive Jenkins CI/CD monitoring and AI-powered
 * anomaly detection system. It bootstraps the entire application ecosystem including REST APIs,
 * database connections, Jenkins integrations, scheduled tasks, and AI analysis pipelines.</p>
 *
 * <p><strong>System Architecture Overview:</strong></p>
 * <ul>
 *   <li><strong>Application Type:</strong> Spring Boot REST API microservice</li>
 *   <li><strong>Primary Purpose:</strong> Jenkins CI/CD monitoring with AI-powered anomaly detection</li>
 *   <li><strong>Data Sources:</strong> Jenkins REST API, PostgreSQL database with JSONB storage</li>
 *   <li><strong>AI Integration:</strong> External AI service for build log analysis and anomaly detection</li>
 *   <li><strong>Frontend Integration:</strong> REST API backend for dashboard UI components</li>
 * </ul>
 *
 * <p><strong>Spring Boot Configuration:</strong></p>
 * <ul>
 *   <li><strong>@SpringBootApplication:</strong> Enables auto-configuration, component scanning, and configuration</li>
 *   <li><strong>@EnableScheduling:</strong> Activates scheduled task execution for Jenkins synchronization</li>
 *   <li><strong>Component Scanning:</strong> Automatically discovers and registers beans in com.diploma.inno package</li>
 *   <li><strong>Auto-Configuration:</strong> Automatically configures Spring Data JPA, Web MVC, Jackson, etc.</li>
 * </ul>
 *
 * <p><strong>Application Properties Configuration:</strong></p>
 * <pre>
 * # Server Configuration
 * server.port=8282
 * spring.application.name=ci-anomaly-detector-api
 *
 * # Jenkins Integration
 * jenkins.url=${JENKINS_URL:http://130.193.49.138:8080}
 * jenkins.username=${JENKINS_USERNAME:admin}
 * jenkins.password=${JENKINS_PASSWORD:admin123}
 *
 * # Database Configuration (PostgreSQL)
 * spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/postgres
 * spring.datasource.username=postgres
 * spring.datasource.password=postgres
 * spring.jpa.hibernate.ddl-auto=update
 * spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
 *
 * # Logging Configuration
 * logging.level.com.diploma.inno=DEBUG
 * logging.level.org.springframework.ai=DEBUG
 * </pre>
 *
 * <p><strong>Core System Components:</strong></p>
 * <ul>
 *   <li><strong>Controllers:</strong> DashboardController - REST API endpoints for dashboard functionality</li>
 *   <li><strong>Services:</strong> DashboardService, JenkinsService - Business logic and external integrations</li>
 *   <li><strong>Repositories:</strong> ChatMessageRepository - Data access layer with complex JSONB queries</li>
 *   <li><strong>Entities:</strong> ChatMessageEntity - JPA entity for storing build logs and AI analysis</li>
 *   <li><strong>Configurations:</strong> JenkinsConfiguration - Jenkins REST client configuration</li>
 *   <li><strong>DTOs:</strong> Various DTOs for type-safe data transfer and API responses</li>
 * </ul>
 *
 * <p><strong>Database Schema:</strong></p>
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
 * <p><strong>Scheduled Operations:</strong></p>
 * <ul>
 *   <li><strong>Jenkins Synchronization:</strong> @Scheduled(cron = "0 0/15 * * * ?") - Every 15 minutes</li>
 *   <li><strong>Job Discovery:</strong> Automatic detection of new Jenkins jobs</li>
 *   <li><strong>Orphan Cleanup:</strong> Removal of database entries for deleted Jenkins jobs</li>
 *   <li><strong>Data Consistency:</strong> Maintains synchronization between Jenkins and database</li>
 * </ul>
 *
 * <p><strong>REST API Endpoints:</strong></p>
 * <ul>
 *   <li><strong>Base URL:</strong> http://localhost:8282/api/dashboard</li>
 *   <li><strong>Build Details:</strong> /builds/{jobName}/{buildId}/* - Build-specific information</li>
 *   <li><strong>Analytics:</strong> /anomaly-trend, /severity-distribution - Chart data for visualizations</li>
 *   <li><strong>Monitoring:</strong> /activeBuilds, /totalJobs - Real-time system metrics</li>
 *   <li><strong>Job Management:</strong> /jobs, /job-explorer - Job discovery and management</li>
 * </ul>
 *
 * <p><strong>External Dependencies:</strong></p>
 * <ul>
 *   <li><strong>Jenkins REST API:</strong> jenkins-rest library (io.github.cdancy:jenkins-rest:1.0.2)</li>
 *   <li><strong>PostgreSQL Database:</strong> Primary data storage with JSONB support</li>
 *   <li><strong>Spring Boot:</strong> Framework foundation (version 3.3.0)</li>
 *   <li><strong>Jackson:</strong> JSON processing for JSONB operations</li>
 *   <li><strong>JPA/Hibernate:</strong> ORM for database operations</li>
 * </ul>
 *
 * <p><strong>AI Analysis Pipeline:</strong></p>
 * <ol>
 *   <li><strong>Log Collection:</strong> Jenkins build logs collected and stored as USER messages</li>
 *   <li><strong>AI Processing:</strong> External AI service analyzes logs for anomalies and patterns</li>
 *   <li><strong>Result Storage:</strong> AI analysis results stored as ASSISTANT messages with JSONB content</li>
 *   <li><strong>Dashboard Queries:</strong> Complex SQL queries extract data for dashboard visualization</li>
 *   <li><strong>Real-time Updates:</strong> Scheduled synchronization ensures data freshness</li>
 * </ol>
 *
 * <p><strong>Performance Optimizations:</strong></p>
 * <ul>
 *   <li><strong>JSONB Indexing:</strong> GIN indexes for efficient JSONB operations</li>
 *   <li><strong>Materialized Views:</strong> Pre-computed aggregations for fast dashboard loading</li>
 *   <li><strong>Caching Strategy:</strong> 15-second TTL cache in JenkinsService for API optimization</li>
 *   <li><strong>Pagination Support:</strong> Efficient handling of large datasets</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li><strong>Jenkins Authentication:</strong> Username/password or API token authentication</li>
 *   <li><strong>Database Security:</strong> PostgreSQL connection with credentials</li>
 *   <li><strong>Environment Variables:</strong> Sensitive configuration via environment variables</li>
 *   <li><strong>Input Validation:</strong> Robust validation and error handling throughout</li>
 * </ul>
 *
 * <p><strong>Monitoring &amp; Logging:</strong></p>
 * <ul>
 *   <li><strong>Application Logging:</strong> DEBUG level for com.diploma.inno package</li>
 *   <li><strong>Spring AI Logging:</strong> DEBUG level for AI integration troubleshooting</li>
 *   <li><strong>Console Pattern:</strong> Structured logging with timestamps and thread information</li>
 *   <li><strong>Error Tracking:</strong> Comprehensive error logging for system monitoring</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see com.diploma.inno.controller.DashboardController
 * @see com.diploma.inno.service.DashboardService
 * @see com.diploma.inno.service.JenkinsService
 * @see com.diploma.inno.repository.ChatMessageRepository
 * @see com.diploma.inno.config.JenkinsConfiguration
 */
@SpringBootApplication
@EnableScheduling
public class RestAPIApplication {

    /**
     * Main entry point for the CI Anomaly Detector Spring Boot application.
     *
     * <p>This method bootstraps the entire CI Anomaly Detector system by initializing the Spring
     * application context and starting the embedded web server. It orchestrates the startup of
     * all system components including database connections, Jenkins integrations, scheduled tasks,
     * and REST API endpoints.</p>
     *
     * <p><strong>Application Startup Process:</strong></p>
     * <ol>
     *   <li><strong>Spring Context Initialization:</strong> Creates and configures the Spring application context</li>
     *   <li><strong>Auto-Configuration:</strong> Automatically configures Spring Boot components based on classpath</li>
     *   <li><strong>Component Scanning:</strong> Discovers and registers all @Component, @Service, @Repository, @Controller beans</li>
     *   <li><strong>Database Connection:</strong> Establishes PostgreSQL connection and initializes JPA/Hibernate</li>
     *   <li><strong>Jenkins Client Setup:</strong> Configures Jenkins REST API client via JenkinsConfiguration</li>
     *   <li><strong>Scheduled Tasks:</strong> Enables and starts @Scheduled methods (15-minute Jenkins sync)</li>
     *   <li><strong>Web Server Startup:</strong> Starts embedded Tomcat server on port 8282</li>
     *   <li><strong>REST Endpoints:</strong> Activates all /api/dashboard/* endpoints for client access</li>
     * </ol>
     *
     * <p><strong>System Components Initialized:</strong></p>
     * <ul>
     *   <li><strong>DashboardController:</strong> REST API endpoints for dashboard functionality</li>
     *   <li><strong>DashboardService:</strong> Business logic for analytics and data processing</li>
     *   <li><strong>JenkinsService:</strong> Jenkins REST API integration with caching</li>
     *   <li><strong>ChatMessageRepository:</strong> Data access layer with complex JSONB queries</li>
     *   <li><strong>JenkinsConfiguration:</strong> Jenkins client configuration and authentication</li>
     *   <li><strong>JsonMapConverter:</strong> JPA converter for Map to JSONB serialization</li>
     * </ul>
     *
     * <p><strong>Database Initialization:</strong></p>
     * <ul>
     *   <li><strong>Connection Pool:</strong> PostgreSQL connection pool initialization</li>
     *   <li><strong>Schema Update:</strong> Hibernate DDL auto-update for table structure</li>
     *   <li><strong>Entity Mapping:</strong> JPA entity mapping for ChatMessageEntity</li>
     *   <li><strong>JSONB Support:</strong> PostgreSQL JSONB column type configuration</li>
     * </ul>
     *
     * <p><strong>Scheduled Task Activation:</strong></p>
     * <ul>
     *   <li><strong>Jenkins Sync:</strong> DashboardService.syncJenkinsJobsWithDatabase() every 15 minutes</li>
     *   <li><strong>Task Scheduler:</strong> Spring's @EnableScheduling activates scheduled method execution</li>
     *   <li><strong>Background Processing:</strong> Non-blocking execution of maintenance tasks</li>
     * </ul>
     *
     * <p><strong>Web Server Configuration:</strong></p>
     * <ul>
     *   <li><strong>Port:</strong> 8282 (configured via server.port property)</li>
     *   <li><strong>Context Path:</strong> Root context ("/") with API base at "/api/dashboard"</li>
     *   <li><strong>JSON Processing:</strong> Jackson configured for JSONB serialization</li>
     *   <li><strong>Error Handling:</strong> Global exception handling for REST endpoints</li>
     * </ul>
     *
     * <p><strong>Environment Configuration:</strong></p>
     * <ul>
     *   <li><strong>Properties Loading:</strong> application.properties and environment variables</li>
     *   <li><strong>Profile Support:</strong> Spring profiles for different environments</li>
     *   <li><strong>External Configuration:</strong> Jenkins URL, credentials, database settings</li>
     * </ul>
     *
     * <p><strong>Logging Initialization:</strong></p>
     * <ul>
     *   <li><strong>Log Levels:</strong> DEBUG for com.diploma.inno, INFO for root</li>
     *   <li><strong>Console Output:</strong> Structured logging with timestamps and thread info</li>
     *   <li><strong>AI Logging:</strong> DEBUG level for Spring AI integration</li>
     * </ul>
     *
     * <p><strong>Health Checks &amp; Monitoring:</strong></p>
     * <ul>
     *   <li><strong>Application Ready:</strong> Spring Boot actuator endpoints (if enabled)</li>
     *   <li><strong>Database Health:</strong> Connection pool monitoring</li>
     *   <li><strong>Jenkins Connectivity:</strong> REST client health validation</li>
     * </ul>
     *
     * <p><strong>Graceful Shutdown:</strong></p>
     * <ul>
     *   <li><strong>Signal Handling:</strong> SIGTERM and SIGINT signal processing</li>
     *   <li><strong>Resource Cleanup:</strong> Database connections, scheduled tasks, web server</li>
     *   <li><strong>Context Destruction:</strong> Proper Spring context shutdown</li>
     * </ul>
     *
     * <p><strong>Command Line Arguments:</strong></p>
     * <ul>
     *   <li><strong>Property Overrides:</strong> --server.port=8080, --spring.profiles.active=prod</li>
     *   <li><strong>Configuration Files:</strong> --spring.config.location=classpath:/custom.properties</li>
     *   <li><strong>Debug Options:</strong> --debug for Spring Boot debug mode</li>
     * </ul>
     *
     * @param args command line arguments passed to the application for configuration overrides
     * @see org.springframework.boot.SpringApplication#run(Class, String...)
     * @see org.springframework.boot.autoconfigure.SpringBootApplication
     * @see org.springframework.scheduling.annotation.EnableScheduling
     */
    public static void main(String[] args) {
        SpringApplication.run(RestAPIApplication.class, args);
    }

    /**
     * Default constructor for RestApiApplication.
     */
    public RestAPIApplication() {
    }
}