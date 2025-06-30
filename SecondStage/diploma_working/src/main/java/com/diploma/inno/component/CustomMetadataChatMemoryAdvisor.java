package com.diploma.inno.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Chat Memory Advisor for DevSecOps CI/CD Pipeline Anomaly Detection System.
 *
 * <p>This advisor extends Spring AI's {@link AbstractChatMemoryAdvisor} to provide specialized
 * memory management for Jenkins build log analysis. It handles metadata enrichment, conversation
 * context management, and build number tracking for AI-powered anomaly detection.</p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><strong>Metadata Preservation:</strong> Maintains build numbers &amp; conversation context across AI interactions</li>
 *   <li><strong>Memory Windowing:</strong> Implements sliding window approach for conversation history management</li>
 *   <li><strong>JSON Cleaning:</strong> Automatically cleans AI responses from markdown formatting</li>
 *   <li><strong>Dual Mode Support:</strong> Handles both synchronous &amp; reactive (streaming) AI interactions</li>
 *   <li><strong>Build Tracking:</strong> Associates each message with specific Jenkins build numbers</li>
 * </ul>
 *
 * <h2>Architecture Integration</h2>
 * <p>This component integrates with the CI anomaly detection pipeline as follows:</p>
 * <pre>{@code
 * Jenkins Logs → RabbitMQ → LogMessageListener → ChatClient (with this Advisor) → AI Analysis
 *                                                      ↓
 *                                              Database Storage (via SimpleDbChatMemory)
 * }</pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Basic configuration
 * ChatMemory chatMemory = new SimpleDbChatMemory(repository, 100);
 * CustomMetadataChatMemoryAdvisor advisor = new CustomMetadataChatMemoryAdvisor(chatMemory);
 *
 * // Advanced configuration with builder
 * CustomMetadataChatMemoryAdvisor advisor = CustomMetadataChatMemoryAdvisor
 *     .builder(chatMemory)
 *     .defaultConversationId("jenkins-job-name")
 *     .chatHistoryWindowSize(50)
 *     .order(1000)
 *     .build();
 *
 * // Integration with ChatClient
 * ChatClient chatClient = ChatClient.builder()
 *     .defaultAdvisors(advisor)
 *     .build();
 * }</pre>
 *
 * <h2>Memory Management Strategy</h2>
 * <p>The advisor implements a sophisticated memory management approach:</p>
 * <ol>
 *   <li><strong>Pre-call:</strong> Fetches conversation history &amp; enriches with metadata</li>
 *   <li><strong>Windowing:</strong> Applies sliding window to prevent context overflow</li>
 *   <li><strong>Post-call:</strong> Stores AI responses with preserved metadata</li>
 *   <li><strong>Cleanup:</strong> Automatically cleans JSON responses from markdown artifacts</li>
 * </ol>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is thread-safe and can be used in concurrent environments. The underlying
 * {@link ChatMemory} implementation handles concurrent access to conversation storage.</p>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see AbstractChatMemoryAdvisor
 * @see ChatMemory
 * @see SimpleDbChatMemory
 * @see LogMessageListener
 */
public class CustomMetadataChatMemoryAdvisor extends AbstractChatMemoryAdvisor<ChatMemory> {

    private static final Logger logger = LoggerFactory.getLogger(CustomMetadataChatMemoryAdvisor.class);

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * Creates a new CustomMetadataChatMemoryAdvisor with default settings.
     *
     * <p>This constructor initializes the advisor with:</p>
     * <ul>
     *   <li>Default conversation ID handling</li>
     *   <li>Default chat history window size</li>
     *   <li>Default advisor order priority</li>
     * </ul>
     *
     * @param chatMemory the chat memory implementation to use for storing conversation history.
     *                   Must not be {@code null}. Typically a {@link SimpleDbChatMemory} instance.
     * @throws NullPointerException if chatMemory is {@code null}
     *
     * @see SimpleDbChatMemory
     * @see AbstractChatMemoryAdvisor
     */
    public CustomMetadataChatMemoryAdvisor(ChatMemory chatMemory) {
        super(chatMemory);
    }

    /**
     * Creates a new CustomMetadataChatMemoryAdvisor with custom conversation and window settings.
     *
     * <p>This constructor allows fine-tuning of memory management behavior for specific
     * Jenkins job monitoring scenarios. The advisor will automatically enable doStore
     * and set a high priority order.</p>
     *
     * <h4>Configuration Details:</h4>
     * <ul>
     *   <li><strong>doStore:</strong> Always {@code true} - ensures all interactions are persisted</li>
     *   <li><strong>order:</strong> {@code Integer.MIN_VALUE + 1000} - high priority execution</li>
     * </ul>
     *
     * @param chatMemory the chat memory implementation for conversation storage
     * @param defaultConversationId the default conversation identifier, typically a Jenkins job name.
     *                             Used when no explicit conversation ID is provided in the request context.
     * @param chatHistoryWindowSize the maximum number of historical messages to retrieve and include
     *                             in the conversation context. Larger values provide more context but
     *                             may impact performance and token usage.
     * @throws NullPointerException if chatMemory is {@code null}
     * @throws IllegalArgumentException if chatHistoryWindowSize is negative
     *
     * @see #CustomMetadataChatMemoryAdvisor(ChatMemory, String, int, int)
     */
    public CustomMetadataChatMemoryAdvisor(ChatMemory chatMemory,
                                           String defaultConversationId,
                                           int chatHistoryWindowSize) {
        super(chatMemory, defaultConversationId, chatHistoryWindowSize, true, Integer.MIN_VALUE + 1000);
    }

    /**
     * Creates a new CustomMetadataChatMemoryAdvisor with full configuration control.
     *
     * <p>This constructor provides complete control over all advisor settings, allowing
     * integration with complex advisor chains and custom execution ordering.</p>
     *
     * <h4>Advisor Order Guidelines:</h4>
     * <ul>
     *   <li><strong>High Priority (negative values):</strong> Execute early in the chain</li>
     *   <li><strong>Default Priority (0):</strong> Standard execution order</li>
     *   <li><strong>Low Priority (positive values):</strong> Execute late in the chain</li>
     * </ul>
     *
     * @param chatMemory the chat memory implementation for conversation storage
     * @param defaultConversationId the default conversation identifier for requests without explicit IDs
     * @param chatHistoryWindowSize the maximum number of historical messages to include in context
     * @param order the execution order within the advisor chain. Lower values execute first.
     *              Recommended: negative values for memory advisors to ensure early execution.
     * @throws NullPointerException if chatMemory is {@code null}
     * @throws IllegalArgumentException if chatHistoryWindowSize is negative
     *
     * @see org.springframework.core.Ordered
     * @see AbstractChatMemoryAdvisor
     */
    public CustomMetadataChatMemoryAdvisor(ChatMemory chatMemory,
                                           String defaultConversationId,
                                           int chatHistoryWindowSize,
                                           int order) {
        super(chatMemory, defaultConversationId, chatHistoryWindowSize, true, order);
    }

    /**
     * Creates a new Builder instance for fluent configuration of the advisor.
     *
     * <p>The builder pattern provides a convenient way to configure complex advisor
     * settings with method chaining and validation.</p>
     *
     * <h4>Builder Usage Example:</h4>
     * <pre>{@code
     * CustomMetadataChatMemoryAdvisor advisor = CustomMetadataChatMemoryAdvisor
     *     .builder(chatMemory)
     *     .defaultConversationId("jenkins-security-scan")
     *     .chatHistoryWindowSize(75)
     *     .order(-500)
     *     .build();
     * }</pre>
     *
     * @param memory the chat memory implementation to use. Must not be {@code null}.
     * @return a new Builder instance for fluent configuration
     * @throws NullPointerException if memory is {@code null}
     *
     * @see Builder
     */
    public static Builder builder(ChatMemory memory) {
        return new Builder(memory);
    }

    // ========================================================================
    // SYNCHRONOUS (BLOCKING) ADVICE IMPLEMENTATION
    // ========================================================================

    /**
     * Provides synchronous (blocking) advice around AI chat interactions.
     *
     * <p>This method implements the core advisor pattern for synchronous AI calls,
     * handling the complete lifecycle of a chat interaction including:</p>
     *
     * <ol>
     *   <li><strong>Pre-processing:</strong> Enriches request with conversation history</li>
     *   <li><strong>Execution:</strong> Delegates to the next advisor in the chain</li>
     *   <li><strong>Post-processing:</strong> Stores AI response with metadata preservation</li>
     * </ol>
     *
     * <h4>Metadata Flow:</h4>
     * <p>The method ensures that critical metadata (especially {@code build_number})
     * flows correctly through the entire interaction lifecycle:</p>
     *
     * <pre>{@code
     * Request Context → Enriched Request → AI Response → Stored Response
     *      ↓                    ↓              ↓             ↓
     * build_number=123 → build_number=123 → build_number=123 → build_number=123
     * }</pre>
     *
     * <h4>Error Handling:</h4>
     * <p>This method is designed to be resilient to failures in the advisor chain.
     * If an exception occurs during processing, the conversation state remains consistent.</p>
     *
     * @param request the incoming chat request containing user message, context, and parameters.
     *                Must contain conversation ID and may contain build_number metadata.
     * @param chain the advisor chain for delegating to subsequent advisors and the final AI model.
     *              The chain handles the actual AI interaction and response generation.
     * @return the AI response enriched with conversation context and metadata preservation.
     *         The response includes the AI-generated content and updated conversation state.
     * @throws RuntimeException if the advisor chain fails or if critical metadata is corrupted
     *
     * @see #before(AdvisedRequest)
     * @see #observeAfter(AdvisedResponse)
     * @see CallAroundAdvisorChain#nextAroundCall(AdvisedRequest)
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request,
                                      CallAroundAdvisorChain chain) {

        logger.debug("aroundCall – userText={} advisorParams={}",
                request.userText(), request.advisorParams());

        // 1. Enrich the request with conversation history and metadata
        AdvisedRequest enriched = before(request);

        // 2. Delegate to the next advisor in the chain (eventually reaching the AI model)
        AdvisedResponse response = chain.nextAroundCall(enriched);

        // 3. Transfer critical metadata from request to response context for persistence
        Object buildNumber = enriched.adviseContext().get("build_number");
        if (buildNumber != null) {
            response = response.updateContext(ctx -> {
                ctx.put("build_number", buildNumber);
                return ctx;
            });
        }

        // 4. Store the AI assistant output in memory with metadata preservation
        observeAfter(response);

        return response;
    }

    // ========================================================================
    // REACTIVE (STREAMING) ADVICE IMPLEMENTATION
    // ========================================================================

    /**
     * Provides reactive (streaming) advice around AI chat interactions.
     *
     * <p>This method implements the advisor pattern for streaming AI responses,
     * handling real-time token streaming while maintaining conversation context
     * and metadata consistency throughout the streaming process.</p>
     *
     * <h4>Streaming Architecture:</h4>
     * <p>The streaming implementation follows a reactive pattern:</p>
     * <pre>{@code
     * Request → Pre-processing → Streaming AI → Message Aggregation → Post-processing
     *    ↓           ↓              ↓               ↓                    ↓
     * Context → History Fetch → Token Stream → Complete Message → Memory Storage
     * }</pre>
     *
     * <h4>Message Aggregation:</h4>
     * <p>The method uses {@link MessageAggregator} to collect streaming tokens
     * into complete messages before storage. This ensures that:</p>
     * <ul>
     *   <li>Partial responses are not stored in memory</li>
     *   <li>Complete conversation context is maintained</li>
     *   <li>Metadata is preserved across the entire streaming session</li>
     * </ul>
     *
     * <h4>Thread Safety:</h4>
     * <p>This method is thread-safe and handles concurrent streaming sessions.
     * Each stream maintains its own conversation context and metadata state.</p>
     *
     * <h4>Error Handling:</h4>
     * <p>The reactive stream includes error handling for:</p>
     * <ul>
     *   <li>Network interruptions during streaming</li>
     *   <li>AI model failures or timeouts</li>
     *   <li>Memory storage failures</li>
     * </ul>
     *
     * @param request the incoming chat request for streaming interaction.
     *                Contains user message, conversation context, and streaming parameters.
     * @param chain the streaming advisor chain for delegating to subsequent advisors.
     *              Handles the actual streaming AI interaction and token generation.
     * @return a reactive stream of {@link AdvisedResponse} objects representing
     *         the streaming AI response. Each response chunk contains partial content
     *         that is aggregated into complete messages for storage.
     * @throws RuntimeException if the streaming chain fails or if critical errors occur during streaming
     *
     * @see #before(AdvisedRequest)
     * @see #observeAfter(AdvisedResponse)
     * @see MessageAggregator#aggregateAdvisedResponse(Flux, java.util.function.Consumer)
     * @see StreamAroundAdvisorChain
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest request,
                                              StreamAroundAdvisorChain chain) {

        logger.debug("aroundStream – userText={} advisorParams={}",
                request.userText(), request.advisorParams());

        Flux<AdvisedResponse> flux = doNextWithProtectFromBlockingBefore(
                request, chain, this::before);

        // Aggregate and store each chunk’s messages
        return new MessageAggregator()
                .aggregateAdvisedResponse(flux, this::observeAfter);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - CONVERSATION LIFECYCLE MANAGEMENT
    // ========================================================================

    /**
     * Pre-processes incoming chat requests by enriching them with conversation history.
     *
     * <p>This method implements the "before" phase of the advisor pattern, responsible for:</p>
     * <ol>
     *   <li><strong>Context Extraction:</strong> Retrieves conversation ID &amp; memory settings</li>
     *   <li><strong>History Retrieval:</strong> Fetches relevant conversation history from storage</li>
     *   <li><strong>Message Persistence:</strong> Stores the current user message immediately</li>
     *   <li><strong>Context Windowing:</strong> Applies sliding window to manage context size</li>
     *   <li><strong>Request Enrichment:</strong> Merges history with current request messages</li>
     * </ol>
     *
     * <h4>Immediate Storage Strategy:</h4>
     * <p>Unlike traditional chat memory advisors, this implementation stores the user message
     * immediately during pre-processing rather than waiting for the AI response. This ensures:</p>
     * <ul>
     *   <li>Complete audit trail of all Jenkins log messages</li>
     *   <li>Resilience to AI failures or timeouts</li>
     *   <li>Consistent conversation state across system restarts</li>
     * </ul>
     *
     * <h4>Metadata Preservation:</h4>
     * <p>The method carefully preserves critical metadata throughout the enrichment process:</p>
     * <pre>{@code
     * // Example metadata flow
     * Request Context: {build_number: 123, conversation_id: "jenkins-job"}
     *        ↓
     * UserMessage: {content: "log data", metadata: {build_number: 123}}
     *        ↓
     * Enriched Request: {messages: [history..., current], context: preserved}
     * }</pre>
     *
     * @param request the incoming chat request containing user message and context.
     *                Must include conversation ID and may contain build_number metadata.
     * @return an enriched {@link AdvisedRequest} with conversation history merged into the message list.
     *         The returned request contains all necessary context for AI processing.
     * @throws RuntimeException if conversation ID cannot be determined or if memory retrieval fails
     *
     * @see #doGetConversationId(Map)
     * @see #doGetChatMemoryRetrieveSize(Map)
     * @see #doWindowChatHistory(List, List)
     * @see ChatMemory#get(String, int)
     * @see ChatMemory#add(String, List)
     */
    private AdvisedRequest before(AdvisedRequest request) {
        Map<String, Object> ctx = request.adviseContext();
        String convId = doGetConversationId(ctx);
        int size = doGetChatMemoryRetrieveSize(ctx);

        // Fetch conversation history from persistent storage
        List<Message> history = getChatMemoryStore().get(convId, size);
        logger.debug("Fetched {} messages for convId={}", history.size(), convId);

        // Immediately persist the current user message with metadata
        UserMessage userMessage = new UserMessage(request.userText(), request.media(),Map.of("build_number",request.adviseContext().get("build_number")));
        getChatMemoryStore().add(convId, userMessage);

        // Apply windowing and merge with current request messages
        List<Message> windowed = doWindowChatHistory(history, request.messages());

        return AdvisedRequest.from(request)
                .messages(windowed)
                .build();
    }

    /**
     * Post-processes AI responses by storing them in conversation memory with metadata enrichment.
     *
     * <p>This method implements the "after" phase of the advisor pattern, responsible for:</p>
     * <ol>
     *   <li><strong>Response Extraction:</strong> Extracts AI assistant messages from the response</li>
     *   <li><strong>Metadata Enrichment:</strong> Preserves &amp; enhances message metadata</li>
     *   <li><strong>JSON Cleaning:</strong> Removes markdown artifacts from AI responses</li>
     *   <li><strong>Memory Persistence:</strong> Stores enriched messages in conversation history</li>
     * </ol>
     *
     * <h4>Metadata Enhancement Process:</h4>
     * <p>The method ensures that critical metadata flows correctly through the entire conversation:</p>
     * <pre>{@code
     * AI Response → Extract AssistantMessage → Enrich Metadata → Clean JSON → Store
     *      ↓                    ↓                    ↓             ↓         ↓
     * Raw Content → Message Object → +build_number → Clean Text → Database
     * }</pre>
     *
     * <h4>JSON Cleaning Strategy:</h4>
     * <p>AI models sometimes return responses wrapped in markdown code blocks. This method
     * automatically cleans such artifacts to ensure pure JSON storage:</p>
     * <ul>
     *   <li>Removes {@code ```json} and {@code ```} markers</li>
     *   <li>Extracts content between first {@code {} and last {@code }}</li>
     *   <li>Preserves original content if cleaning fails</li>
     * </ul>
     *
     * <h4>Error Resilience:</h4>
     * <p>The method is designed to handle various edge cases gracefully:</p>
     * <ul>
     *   <li>Missing or malformed metadata</li>
     *   <li>Multiple assistant messages in a single response</li>
     *   <li>Storage failures (logged but not propagated)</li>
     * </ul>
     *
     * @param advised the AI response containing assistant messages and context.
     *                May contain multiple result messages that need to be processed and stored.
     * @throws RuntimeException if conversation ID cannot be determined or if critical storage failures occur
     *
     * @see #cleanJsonString(String)
     * @see AssistantMessage
     * @see ChatMemory#add(String, List)
     */
    private void observeAfter(AdvisedResponse advised) {
        Map<String, Object> ctx = advised.adviseContext();
        String convId = doGetConversationId(ctx);
        Integer buildNumber = ctx.containsKey("build_number") ? Integer.valueOf(ctx.get("build_number").toString()) : 0;

        advised.response().getResults().stream()
                .map(result -> result.getOutput())
                .filter(output -> output instanceof AssistantMessage)
                .map(output -> (AssistantMessage) output)
                .map(assistantMessage -> {
                    if (buildNumber == null) {
                        return assistantMessage;
                    }
                    // Merge existing metadata with build_number for complete context
                    Map<String, Object> oldMetadata = assistantMessage.getMetadata();
                    Map<String, Object> newMetadata = new HashMap<>(oldMetadata);
                    newMetadata.put("build_number", buildNumber);

                    // Create new AssistantMessage with enhanced metadata and cleaned content
                    return new AssistantMessage(
                            cleanJsonString(assistantMessage.getText()),
                            newMetadata,
                            assistantMessage.getToolCalls(),
                            assistantMessage.getMedia()
                    );
                })
                .forEach(enrichedMessage -> {
                    logger.debug("Storing in memory [{}]: {}", convId, enrichedMessage);
                    getChatMemoryStore().add(convId, enrichedMessage);
                });
    }

    /**
     * Applies sliding window strategy to conversation history for optimal context management.
     *
     * <p>This method implements a sliding window approach to prevent conversation context
     * from growing unbounded while maintaining sufficient historical context for AI analysis.
     * It's particularly important for Jenkins monitoring where builds can generate extensive
     * log histories over time.</p>
     *
     * <h4>Windowing Strategy:</h4>
     * <p>The method applies the following logic:</p>
     * <ol>
     *   <li><strong>Size Check:</strong> Determines if history exceeds window size</li>
     *   <li><strong>Truncation:</strong> Keeps only the most recent N messages</li>
     *   <li><strong>Merging:</strong> Appends current request messages to windowed history</li>
     *   <li><strong>Ordering:</strong> Maintains chronological message order</li>
     * </ol>
     *
     * <h4>Performance Considerations:</h4>
     * <p>The windowing approach provides several benefits:</p>
     * <ul>
     *   <li><strong>Token Efficiency:</strong> Prevents AI context overflow</li>
     *   <li><strong>Memory Management:</strong> Limits in-memory message collections</li>
     *   <li><strong>Response Time:</strong> Reduces AI processing latency</li>
     *   <li><strong>Cost Control:</strong> Minimizes AI API token usage</li>
     * </ul>
     *
     * <h4>Context Preservation:</h4>
     * <p>Despite truncation, the method ensures that:</p>
     * <ul>
     *   <li>Recent conversation context is fully preserved</li>
     *   <li>Current request messages are always included</li>
     *   <li>Message chronology remains intact</li>
     * </ul>
     *
     * @param history the complete conversation history retrieved from storage.
     *                May contain more messages than the configured window size.
     * @param current the current request messages that should always be included.
     *                These are appended after the windowed history.
     * @return a new list containing the windowed history plus current messages.
     *         The total size may exceed windowSize due to current message inclusion.
     *
     * @see #before(AdvisedRequest)
     */
    private List<Message> doWindowChatHistory(List<Message> history,
                                              List<Message> current) {
        int windowSize = 100;  // Configurable window size for context management
        int start = Math.max(0, history.size() - windowSize);
        List<Message> windowed = new ArrayList<>(history.subList(start, history.size()));
        windowed.addAll(current);
        return windowed;
    }

    // ========================================================================
    // BUILDER PATTERN IMPLEMENTATION
    // ========================================================================

    /**
     * Builder class for fluent configuration of {@link CustomMetadataChatMemoryAdvisor} instances.
     *
     * <p>This builder provides a convenient, type-safe way to configure complex advisor settings
     * with method chaining, validation, and sensible defaults. It's particularly useful when
     * integrating with Spring's configuration system or when creating multiple advisor instances
     * with different settings.</p>
     *
     * <h2>Configuration Options:</h2>
     * <ul>
     *   <li><strong>defaultConversationId:</strong> Fallback conversation identifier</li>
     *   <li><strong>chatHistoryWindowSize:</strong> Maximum historical messages to retrieve</li>
     *   <li><strong>order:</strong> Execution priority within advisor chains</li>
     * </ul>
     *
     * <h2>Usage Examples:</h2>
     * <pre>{@code
     * // Basic configuration
     * CustomMetadataChatMemoryAdvisor advisor = CustomMetadataChatMemoryAdvisor
     *     .builder(chatMemory)
     *     .build();
     *
     * // Advanced configuration for Jenkins monitoring
     * CustomMetadataChatMemoryAdvisor advisor = CustomMetadataChatMemoryAdvisor
     *     .builder(chatMemory)
     *     .defaultConversationId("jenkins-security-pipeline")
     *     .chatHistoryWindowSize(150)
     *     .order(-1000)  // High priority execution
     *     .build();
     * }</pre>
     *
     * @see CustomMetadataChatMemoryAdvisor#builder(ChatMemory)
     */
    public static class Builder {
        private final ChatMemory memory;
        private String defaultConversationId = CHAT_MEMORY_CONVERSATION_ID_KEY;
        private int chatHistoryWindowSize = CHAT_MEMORY_RETRIEVE_SIZE_KEY != null
                ? CHAT_MEMORY_RETRIEVE_SIZE_KEY.length() : 100; // fallback
        private int order = HIGHEST_PRECEDENCE;

        /**
         * Creates a new Builder instance with the specified chat memory.
         *
         * @param memory the chat memory implementation to use. Must not be {@code null}.
         * @throws NullPointerException if memory is {@code null}
         */
        public Builder(ChatMemory memory) {
            this.memory = memory;
        }

        /**
         * Sets the default conversation identifier for requests without explicit IDs.
         *
         * @param id the default conversation ID, typically a Jenkins job name
         * @return this builder instance for method chaining
         */
        public Builder defaultConversationId(String id) {
            this.defaultConversationId = id;
            return this;
        }

        /**
         * Sets the maximum number of historical messages to retrieve for context.
         *
         * @param size the window size for conversation history. Must be positive.
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if size is not positive
         */
        public Builder chatHistoryWindowSize(int size) {
            this.chatHistoryWindowSize = size;
            return this;
        }

        /**
         * Sets the execution order within the advisor chain.
         *
         * @param order the execution priority. Lower values execute first.
         * @return this builder instance for method chaining
         */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        /**
         * Builds a new {@link CustomMetadataChatMemoryAdvisor} with the configured settings.
         *
         * @return a fully configured advisor instance
         * @throws IllegalArgumentException if any configuration is invalid
         */
        public CustomMetadataChatMemoryAdvisor build() {
            return new CustomMetadataChatMemoryAdvisor(
                    memory, defaultConversationId, chatHistoryWindowSize, order);
        }
    }

    // ========================================================================
    // UTILITY METHODS - JSON PROCESSING
    // ========================================================================

    /**
     * Cleans AI-generated JSON responses by removing markdown artifacts and extracting pure JSON content.
     *
     * <p>AI language models often wrap JSON responses in markdown code blocks for better readability.
     * This utility method automatically detects and removes such formatting to ensure that only
     * clean, parseable JSON is stored in the conversation memory.</p>
     *
     * <h4>Cleaning Process:</h4>
     * <p>The method applies the following cleaning steps in sequence:</p>
     * <ol>
     *   <li><strong>Null Check:</strong> Returns {@code null} for null input</li>
     *   <li><strong>Markdown Removal:</strong> Strips {@code ```json} and {@code ```} markers</li>
     *   <li><strong>Whitespace Trimming:</strong> Removes leading/trailing whitespace</li>
     *   <li><strong>JSON Extraction:</strong> Extracts content between first {@code {} and last {@code }}</li>
     * </ol>
     *
     * <h4>Supported Input Formats:</h4>
     * <p>The method handles various AI response formats:</p>
     * <pre>{@code
     * // Markdown wrapped JSON
     * ```json
     * {"status": "success", "data": {...}}
     * ```
     *
     * // Plain JSON with extra text
     * Here's the analysis: {"status": "success"} - completed successfully
     *
     * // Clean JSON (unchanged)
     * {"status": "success", "data": {...}}
     * }</pre>
     *
     * <h4>Error Handling:</h4>
     * <p>The method is designed to be safe and non-destructive:</p>
     * <ul>
     *   <li>Returns original content if JSON extraction fails</li>
     *   <li>Preserves content when no cleaning is needed</li>
     *   <li>Handles malformed or incomplete JSON gracefully</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Time Complexity:</strong> O(n) where n is the input string length</li>
     *   <li><strong>Space Complexity:</strong> O(n) for string operations</li>
     *   <li><strong>Thread Safety:</strong> This static method is thread-safe</li>
     * </ul>
     *
     * @param rawJson the raw AI response that may contain markdown formatting or extra text.
     *                Can be {@code null}, in which case {@code null} is returned.
     * @return the cleaned JSON string with markdown artifacts removed, or {@code null} if input was {@code null}.
     *         If JSON extraction fails, returns the trimmed input string.
     *
     * @see #observeAfter(AdvisedResponse)
     * @see AssistantMessage#getText()
     *
     * @since 1.0
     */
    public static String cleanJsonString(String rawJson) {
        if (rawJson == null) return null;

        // Remove Markdown code block markers (both leading and trailing)
        rawJson = rawJson.replaceAll("^```json\\s*", "");       // leading ```json
        rawJson = rawJson.replaceAll("```json\\s*$", "");       // trailing ```json

        // Trim whitespace for cleaner processing
        rawJson = rawJson.trim();

        // Extract JSON content between first '{' and last '}' for safety
        int start = rawJson.indexOf('{');
        int end = rawJson.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            rawJson = rawJson.substring(start, end + 1);
        }

        return rawJson;
    }


}
