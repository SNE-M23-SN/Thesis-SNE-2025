package com.diploma.inno.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-Origin Resource Sharing (CORS) configuration for the CI Anomaly Detector API.
 *
 * <p>This configuration class implements {@link WebMvcConfigurer} to customize CORS settings
 * for the Spring Boot application. CORS is a security feature implemented by web browsers
 * to restrict cross-origin HTTP requests that are initiated from scripts running in the browser.</p>
 *
 * <p><strong>Current Configuration:</strong></p>
 * <ul>
 *   <li><strong>Allowed Origins:</strong> All origins (*) - permits requests from any domain</li>
 *   <li><strong>Allowed Methods:</strong> GET, POST, PUT, DELETE, OPTIONS - covers all standard HTTP methods</li>
 *   <li><strong>Allowed Headers:</strong> All headers (*) - permits any request headers</li>
 *   <li><strong>Path Mapping:</strong> All endpoints (/**) - applies to entire API surface</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li>⚠️ <strong>WARNING:</strong> Current configuration allows all origins, which may pose security risks in production</li>
 *   <li>Consider restricting {@code allowedOrigins} to specific domains in production environments</li>
 *   <li>Evaluate if all HTTP methods are necessary for your use case</li>
 *   <li>Consider implementing more granular CORS policies for different API endpoints</li>
 * </ul>
 *
 * <p><strong>Usage Context:</strong></p>
 * <p>This configuration is particularly relevant for:</p>
 * <ul>
 *   <li>Frontend applications running on different domains/ports accessing the API</li>
 *   <li>Development environments where frontend and backend run on different ports</li>
 *   <li>Integration with external dashboards or monitoring tools</li>
 *   <li>Third-party applications consuming the Jenkins anomaly detection API</li>
 * </ul>
 *
 * <p><strong>Production Recommendations:</strong></p>
 * {@snippet lang=java :
 * // Example of more secure configuration:
 * registry.addMapping("/api/**")
 *         .allowedOrigins("https://your-frontend-domain.com", "https://dashboard.company.com")
 *         .allowedMethods("GET", "POST")
 *         .allowedHeaders("Content-Type", "Authorization")
 *         .allowCredentials(true)
 *         .maxAge(3600);
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see WebMvcConfigurer
 * @see CorsRegistry
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * The allowed origins for CORS requests, configurable via application properties.
     * <p>Default value allows all origins ("*") if not specified in configuration.</p>
     *
     * @see #addCorsMappings(CorsRegistry)
     */
    @Value("${cors.allowedOrigins:*}")
    private String[] allowedOrigins;

    /**
     * Configures Cross-Origin Resource Sharing (CORS) mappings for the application.
     *
     * <p>This method defines which cross-origin requests are allowed by configuring:</p>
     * <ul>
     *   <li><strong>Path patterns</strong> that the CORS configuration applies to</li>
     *   <li><strong>Allowed origins</strong> that can make cross-origin requests</li>
     *   <li><strong>HTTP methods</strong> that are permitted for cross-origin requests</li>
     *   <li><strong>Headers</strong> that can be used during the actual request</li>
     * </ul>
     *
     * <p><strong>Current Implementation Details:</strong></p>
     * <ul>
     *   <li>{@code addMapping("/**")} - Applies CORS settings to all API endpoints</li>
     *   <li>{@code allowedOrigins("*")} - Permits requests from any origin (domain/port)</li>
     *   <li>{@code allowedMethods(...)} - Allows standard REST API HTTP methods</li>
     *   <li>{@code allowedHeaders("*")} - Permits any request headers</li>
     * </ul>
     *
     * <p><strong>Method-Specific Behavior:</strong></p>
     * <ul>
     *   <li><strong>GET:</strong> Used for retrieving dashboard data, job information, and anomaly reports</li>
     *   <li><strong>POST:</strong> Used for triggering builds or submitting analysis requests</li>
     *   <li><strong>PUT:</strong> Used for updating configuration or job settings</li>
     *   <li><strong>DELETE:</strong> Used for removing jobs or clearing data</li>
     *   <li><strong>OPTIONS:</strong> Preflight requests sent by browsers for complex CORS requests</li>
     * </ul>
     *
     * <p><strong>Browser Preflight Requests:</strong></p>
     * <p>For certain cross-origin requests, browsers automatically send an OPTIONS request
     * (preflight) to check if the actual request is allowed. This configuration handles
     * those preflight requests by including OPTIONS in the allowed methods.</p>
     *
     * @param registry the {@link CorsRegistry} to configure CORS mappings on
     *
     * This method is called automatically by Spring during application startup
     *           to configure CORS settings for the embedded web server
     *
     * @see CorsRegistry#addMapping(String)
     * @see org.springframework.web.cors.CorsConfiguration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * Default constructor for CorsConfig.
     */
    public CorsConfig() {
    }
}
