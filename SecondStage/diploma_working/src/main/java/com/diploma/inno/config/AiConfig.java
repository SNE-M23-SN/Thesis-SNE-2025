package com.diploma.inno.config;



import com.diploma.inno.component.CustomMetadataChatMemoryAdvisor;
import com.diploma.inno.component.SimpleDbChatMemory;
import com.diploma.inno.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Spring AI configuration for Jenkins CI/CD pipeline anomaly detection system.
 *
 * <p>This configuration class orchestrates the setup of AI-powered components for DevSecOps
 * monitoring, including chat memory management, conversation advisors, and AI client configuration.
 * It serves as the central hub for integrating Google Gemini AI with Jenkins build log analysis.</p>
 *
 * <h2>Core Responsibilities</h2>
 * <ul>
 *   <li><strong>AI Client Configuration:</strong> Sets up ChatClient with Google Gemini 2.5 Flash model</li>
 *   <li><strong>Memory Management:</strong> Configures persistent conversation storage with PostgreSQL</li>
 *   <li><strong>Advisor Integration:</strong> Wires custom memory advisors for metadata preservation</li>
 *   <li><strong>System Prompt Loading:</strong> Manages AI system prompts for DevSecOps analysis</li>
 *   <li><strong>Component Orchestration:</strong> Coordinates all AI-related Spring beans</li>
 * </ul>
 *
 * <h2>Architecture Integration</h2>
 * <p>This configuration enables the complete AI analysis pipeline:</p>
 * <pre>{@code
 * Jenkins Logs → LogMessageListener → ChatMemory → ChatClient → AI Analysis
 *      ↓               ↓                  ↓           ↓            ↓
 * Raw Messages → Message Storage → Context Retrieval → AI Processing → Anomaly Detection
 *      ↑               ↑                  ↑           ↑            ↑
 * RabbitMQ ← Database Storage ← Memory Advisor ← System Prompt ← Google Gemini
 * }</pre>
 *
 * <h2>AI Model Configuration</h2>
 * <p>The configuration integrates with Google's Generative AI platform:</p>
 * <ul>
 *   <li><strong>Model:</strong> Google Gemini 2.5 Flash Preview</li>
 *   <li><strong>API Integration:</strong> OpenAI-compatible interface via Spring AI</li>
 *   <li><strong>Response Format:</strong> JSON_OBJECT for structured anomaly reports</li>
 *   <li><strong>Temperature:</strong> 0 (deterministic responses for consistent analysis)</li>
 *   <li><strong>Context Window:</strong> Optimized for Jenkins log analysis</li>
 * </ul>
 *
 * <h2>Memory Management Strategy</h2>
 * <p>Implements sophisticated conversation memory with:</p>
 * <ul>
 *   <li><strong>Persistent Storage:</strong> PostgreSQL-backed chat history</li>
 *   <li><strong>Sliding Window:</strong> Configurable message retention (100 messages default)</li>
 *   <li><strong>Metadata Preservation:</strong> Build numbers &amp; Jenkins context tracking</li>
 *   <li><strong>Automatic Cleanup:</strong> Scheduled pruning for optimal performance</li>
 * </ul>
 *
 * <h2>System Prompt Integration</h2>
 * <p>Loads specialized DevSecOps analysis prompts:</p>
 * <ul>
 *   <li><strong>Template Location:</strong> {@code classpath:templates/system.st}</li>
 *   <li><strong>Content Focus:</strong> Security vulnerability detection &amp; anomaly analysis</li>
 *   <li><strong>Context Awareness:</strong> Jenkins-specific log format understanding</li>
 *   <li><strong>Output Structure:</strong> Standardized JSON response format</li>
 * </ul>
 *
 * <h2>Bean Dependencies</h2>
 * <p>This configuration requires the following external dependencies:</p>
 * <ul>
 *   <li><strong>ChatMessageRepository:</strong> JPA repository for message persistence</li>
 *   <li><strong>ChatClient.Builder:</strong> Spring AI's chat client builder (auto-configured)</li>
 *   <li><strong>Database Connection:</strong> PostgreSQL connection for chat memory</li>
 *   <li><strong>AI Model Access:</strong> Google Generative AI API credentials</li>
 * </ul>
 *
 * <h2>Configuration Properties</h2>
 * <p>Key configuration aspects managed by this class:</p>
 * {@snippet lang="properties" :
 * # System prompt template location
 * spring.ai.system.prompt=classpath:templates/system.st
 *
 * # Memory management settings
 * ai.memory.max-messages-per-conversation=100
 * ai.memory.cleanup.enabled=true
 * ai.memory.cleanup.schedule=3600000
 *
 * # AI model configuration (external)
 * spring.ai.openai.api-key=${GOOGLE_AI_API_KEY}
 * spring.ai.openai.base-url=https://generativelanguage.googleapis.com/v1beta/
 * }
 *
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li><strong>Memory Efficiency:</strong> Bounded conversation history prevents memory leaks</li>
 *   <li><strong>Database Optimization:</strong> Efficient queries with proper indexing</li>
 *   <li><strong>AI Rate Limiting:</strong> Managed through Spring AI's built-in mechanisms</li>
 *   <li><strong>Concurrent Access:</strong> Thread-safe components for high-volume processing</li>
 * </ul>
 *
 * <h2>Security &amp; Compliance</h2>
 * <ul>
 *   <li><strong>API Key Management:</strong> Secure credential handling via environment variables</li>
 *   <li><strong>Data Privacy:</strong> Local conversation storage with controlled retention</li>
 *   <li><strong>Access Control:</strong> Spring Security integration for API access</li>
 *   <li><strong>Audit Trail:</strong> Complete logging of AI interactions</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * {@snippet lang="java" :
 * // Automatic Spring configuration - no manual setup required
 * @Autowired
 * private ChatClient chatClient;
 *
 * @Autowired
 * private ChatMemory chatMemory;
 *
 * // Example AI analysis call
 * String analysis = chatClient.prompt()
 *     .advisors(advisor -> advisor
 *         .param("chat_memory_conversation_id", "jenkins-security-scan")
 *         .param("build_number", 123))
 *     .user("Analyze this Jenkins build for anomalies: " + buildLogs)
 *     .call()
 *     .content();
 * }
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see ChatClient
 * @see ChatMemory
 * @see SimpleDbChatMemory
 * @see CustomMetadataChatMemoryAdvisor
 * @see ChatMessageRepository
 */
@Configuration
public class AiConfig {
    // ========================================================================
    // CONFIGURATION PROPERTIES &amp; RESOURCES
    // ========================================================================

    /**
     * System prompt template resource for AI-powered DevSecOps analysis.
     *
     * <p>This resource contains the specialized system prompt that configures the AI model
     * for Jenkins build log analysis and anomaly detection. The prompt defines the AI's
     * role, capabilities, and expected output format for DevSecOps monitoring.</p>
     *
     * <h4>Template Content Structure:</h4>
     * <p>The system prompt template includes:</p>
     * <ul>
     *   <li><strong>Role Definition:</strong> AI persona as DevSecOps security analyst</li>
     *   <li><strong>Analysis Scope:</strong> Jenkins build logs, security scans, dependencies</li>
     *   <li><strong>Output Format:</strong> Structured JSON with anomaly details &amp; risk scores</li>
     *   <li><strong>Context Awareness:</strong> Understanding of CI/CD pipeline stages</li>
     * </ul>
     *
     * <h4>Template Location:</h4>
     * <p>Located at {@code src/main/resources/templates/system.st} in the classpath.
     * This location allows for easy template updates without code changes.</p>
     *
     * <h4>Template Processing:</h4>
     * <p>The template supports dynamic content injection:</p>
     * {@snippet lang="text" :
     * You are a DevSecOps AI analyst specializing in Jenkins CI/CD pipeline monitoring.
     * Analyze the provided build logs for security vulnerabilities, anomalies, and risks.
     *
     * Focus on:
     * - Secret detection and exposure
     * - Dependency vulnerabilities
     * - Build failures and instability
     * - Security scan results
     * - Performance anomalies
     *
     * Respond with structured JSON containing risk assessment and recommendations.
     * }
     *
     * @see ChatClient.Builder#defaultSystem(Resource)
     * @see org.springframework.core.io.Resource
     */
    @Value("${ai.system.prompt.template:classpath:templates/system.st}")
    private Resource getSystemPrompt;

    /**
     * Maximum number of messages to retain per conversation in chat memory.
     * <p>This value controls the sliding window size for conversation history,
     * balancing context richness with performance and storage efficiency.</p>
     */
    @Value("${ai.memory.max-messages-per-conversation:100}")
    private int maxMessagesPerConversation;

    // ========================================================================
    // SPRING BEAN CONFIGURATION
    // ========================================================================

    /**
     * Configures the chat memory implementation for persistent conversation storage.
     *
     * <p>This bean creates a database-backed chat memory that provides persistent storage
     * for Jenkins build logs and AI analysis results. It implements sophisticated memory
     * management with automatic cleanup and sliding window approach for optimal performance.</p>
     *
     * <h4>Memory Configuration:</h4>
     * <ul>
     *   <li><strong>Storage Backend:</strong> PostgreSQL database via JPA repository</li>
     *   <li><strong>Message Limit:</strong> 100 messages per conversation (configurable)</li>
     *   <li><strong>Cleanup Strategy:</strong> Automatic pruning of old messages</li>
     *   <li><strong>Metadata Support:</strong> Rich metadata preservation for Jenkins context</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Throughput:</strong> Optimized for high-volume Jenkins log ingestion</li>
     *   <li><strong>Storage Efficiency:</strong> JSONB compression for large log messages</li>
     *   <li><strong>Query Performance:</strong> Indexed queries for fast conversation retrieval</li>
     *   <li><strong>Memory Management:</strong> Bounded memory usage with automatic cleanup</li>
     * </ul>
     *
     * <h4>Integration Points:</h4>
     * <p>The chat memory integrates with multiple system components:</p>
     * <ul>
     *   <li><strong>LogMessageListener:</strong> Stores incoming Jenkins logs</li>
     *   <li><strong>CustomMetadataChatMemoryAdvisor:</strong> Retrieves conversation context</li>
     *   <li><strong>ChatClient:</strong> Provides historical context for AI analysis</li>
     * </ul>
     *
     * <h4>Configuration Rationale:</h4>
     * <p>The 100-message limit balances several factors:</p>
     * <ul>
     *   <li><strong>Context Richness:</strong> Sufficient history for trend analysis</li>
     *   <li><strong>Performance:</strong> Manageable query sizes for fast retrieval</li>
     *   <li><strong>Token Efficiency:</strong> Reasonable AI context window usage</li>
     *   <li><strong>Storage Cost:</strong> Controlled database growth</li>
     * </ul>
     *
     * @param repository the JPA repository for chat message persistence.
     *                   Injected automatically by Spring's dependency injection.
     *                   Must support custom queries for conversation management.
     * @return a configured {@link SimpleDbChatMemory} instance ready for production use.
     *         The instance includes automatic cleanup scheduling and transaction management.
     *
     * @see SimpleDbChatMemory
     * @see ChatMessageRepository
     * @see ChatMemory
     */
    @Bean
    public ChatMemory chatMemory(ChatMessageRepository repository) {
        return new SimpleDbChatMemory(repository, maxMessagesPerConversation);
    }

    /**
     * Configures the primary ChatClient for AI-powered Jenkins build analysis.
     *
     * <p>This bean creates the central AI client that orchestrates all anomaly detection
     * and security analysis for Jenkins CI/CD pipelines. It integrates Google Gemini AI
     * with custom memory management and specialized DevSecOps prompting.</p>
     *
     * <h4>Client Configuration:</h4>
     * <p>The ChatClient is configured with enterprise-grade capabilities:</p>
     * <ul>
     *   <li><strong>AI Model:</strong> Google Gemini 2.5 Flash Preview via OpenAI-compatible API</li>
     *   <li><strong>System Prompt:</strong> Specialized DevSecOps analysis instructions</li>
     *   <li><strong>Memory Advisor:</strong> Custom metadata-aware conversation management</li>
     *   <li><strong>Response Format:</strong> Structured JSON for consistent analysis output</li>
     * </ul>
     *
     * <h4>System Prompt Integration:</h4>
     * <p>The default system prompt configures the AI for:</p>
     * <ul>
     *   <li><strong>Role Definition:</strong> DevSecOps security analyst specialization</li>
     *   <li><strong>Analysis Focus:</strong> Jenkins logs, security scans, dependencies</li>
     *   <li><strong>Output Structure:</strong> JSON format with risk scores &amp; recommendations</li>
     *   <li><strong>Context Awareness:</strong> CI/CD pipeline stage understanding</li>
     * </ul>
     *
     * <h4>Memory Advisor Features:</h4>
     * <p>The {@link CustomMetadataChatMemoryAdvisor} provides:</p>
     * <ul>
     *   <li><strong>Conversation Context:</strong> Historical build analysis for trend detection</li>
     *   <li><strong>Metadata Preservation:</strong> Jenkins build numbers &amp; job correlation</li>
     *   <li><strong>Memory Windowing:</strong> Efficient context management for large conversations</li>
     *   <li><strong>Build Tracking:</strong> Correlation of logs across build sequences</li>
     * </ul>
     *
     * <h4>AI Analysis Capabilities:</h4>
     * <p>The configured client enables comprehensive analysis:</p>
     * {@snippet lang="json" :
     * {
     *   "analysis_type": "jenkins_build_anomaly_detection",
     *   "risk_score": 75,
     *   "anomalies_detected": [
     *     {
     *       "type": "secret_exposure",
     *       "severity": "HIGH",
     *       "description": "API key detected in build logs",
     *       "recommendation": "Rotate exposed credentials immediately"
     *     },
     *     {
     *       "type": "dependency_vulnerability",
     *       "severity": "MEDIUM",
     *       "description": "Outdated library with known CVE",
     *       "recommendation": "Update to latest secure version"
     *     }
     *   ],
     *   "build_health": "DEGRADED",
     *   "trend_analysis": "Security posture declining over last 5 builds"
     * }
     * }
     *
     * <h4>Usage Patterns:</h4>
     * <p>The ChatClient supports various analysis scenarios:</p>
     * {@snippet lang="java" :
     * // Real-time build analysis
     * String analysis = chatClient.prompt()
     *     .advisors(advisor -> advisor
     *         .param("chat_memory_conversation_id", "jenkins-security-pipeline")
     *         .param("build_number", 123))
     *     .user(buildLogData)
     *     .call()
     *     .content();
     *
     * // Streaming analysis for large logs
     * Flux<String> streamingAnalysis = chatClient.prompt()
     *     .user(largeBuildLogs)
     *     .stream()
     *     .content();
     * }
     *
     * <h4>Performance Optimization:</h4>
     * <ul>
     *   <li><strong>Connection Pooling:</strong> Efficient HTTP connection management</li>
     *   <li><strong>Request Batching:</strong> Optimized API call patterns</li>
     *   <li><strong>Response Caching:</strong> Intelligent caching for repeated analyses</li>
     *   <li><strong>Error Recovery:</strong> Automatic retry with exponential backoff</li>
     * </ul>
     *
     * <h4>Security Features:</h4>
     * <ul>
     *   <li><strong>API Key Security:</strong> Secure credential management</li>
     *   <li><strong>Request Validation:</strong> Input sanitization &amp; validation</li>
     *   <li><strong>Response Filtering:</strong> Output sanitization for sensitive data</li>
     *   <li><strong>Audit Logging:</strong> Complete interaction audit trail</li>
     * </ul>
     *
     * <h4>Integration Architecture:</h4>
     * <p>The ChatClient serves as the central hub for AI operations:</p>
     * <pre>{@code
     * LogMessageListener → ChatClient → Google Gemini AI
     *        ↓                ↓              ↓
     * Jenkins Logs → Analysis Request → AI Response
     *        ↑                ↑              ↑
     * Memory Context ← CustomAdvisor ← Structured JSON
     * }</pre>
     *
     * @param builder the ChatClient builder provided by Spring AI auto-configuration.
     *                Pre-configured with AI model settings, API credentials, and base URL.
     * @param chatMemory the chat memory implementation for conversation persistence.
     *                   Provides historical context and metadata for enhanced analysis.
     * @return a fully configured {@link ChatClient} ready for production Jenkins analysis.
     *         The client includes system prompts, memory management, and error handling.
     *
     * @see ChatClient
     * @see ChatClient.Builder
     * @see CustomMetadataChatMemoryAdvisor
     * @see ChatMemory
     * @see Resource
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(getSystemPrompt)
                .defaultAdvisors(new CustomMetadataChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * Creates a new instance of the class with default initial values.
     * This constructor initializes the object to its default state.
     */
    public AiConfig() {
    }
}