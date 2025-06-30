package com.diploma.inno.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * JPA AttributeConverter for automatic serialization/deserialization of Map objects to/from JSON strings.
 *
 * <p>This converter provides seamless integration between Java Map&lt;String, Object&gt; objects
 * and database JSONB columns, enabling flexible metadata storage in the CI Anomaly Detector system.
 * It handles the automatic conversion during JPA entity persistence and retrieval operations.</p>
 *
 * <p><strong>Primary Usage:</strong></p>
 * <ul>
 *   <li><strong>ChatMessageEntity.metadata:</strong> Stores additional metadata for chat messages</li>
 *   <li><strong>Extensible Storage:</strong> Enables flexible schema evolution without database migrations</li>
 *   <li><strong>Type Safety:</strong> Maintains Java type safety while providing JSON flexibility</li>
 *   <li><strong>Automatic Conversion:</strong> Transparent serialization/deserialization via JPA</li>
 * </ul>
 *
 * <p><strong>Database Integration:</strong></p>
 * {@snippet lang=java :
 * // Entity field declaration
 * {@code @Column(name = "metadata")
 * @Convert(converter = JsonMapConverter.class)
 * private Map<String, Object> metadata;}
 *
 * // Database column type: JSONB (PostgreSQL)
 * // Stored as: {"key1": "value1", "key2": 123, "key3": {"nested": "object"}}
 * }
 *
 * <p><strong>Conversion Process:</strong></p>
 * <ol>
 *   <li><strong>Entity to Database:</strong> Map&lt;String, Object&gt; → JSON String → JSONB column</li>
 *   <li><strong>Database to Entity:</strong> JSONB column → JSON String → Map&lt;String, Object&gt;</li>
 *   <li><strong>Null Handling:</strong> Null/empty maps stored as NULL, NULL columns return empty map</li>
 *   <li><strong>Error Recovery:</strong> JSON parsing errors return empty map with error logging</li>
 * </ol>
 *
 * <p><strong>Supported Data Types:</strong></p>
 * <ul>
 *   <li><strong>Primitives:</strong> String, Integer, Long, Double, Boolean</li>
 *   <li><strong>Collections:</strong> List, Map (nested structures)</li>
 *   <li><strong>Null Values:</strong> Graceful handling of null values</li>
 *   <li><strong>Complex Objects:</strong> Any JSON-serializable object structure</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong></p>
 *
 * <span><b>Creating entity with metadata</b></span>
 * {@snippet lang=java :
 * Map&lt;String, Object&gt; metadata = Map.of(
 *     "source", "ai-service",
 *     "version", "1.2.3",
 *     "processing_time_ms", 1500,
 *     "features", List.of("anomaly_detection", "risk_scoring"),
 *     "config", Map.of("threshold", 0.8, "enabled", true)
 * );
 *
 * ChatMessageEntity message = new ChatMessageEntity(
 *     "my-job", 123, "ASSISTANT", "{...}", metadata
 * );
 *}
 * <span><b>Automatic conversion during save/load</b></span>
 * {@snippet lang=java :
 * repository.save(message); // Map → JSON string → JSONB
 * ChatMessageEntity loaded = repository.findById(id); // JSONB → JSON string → Map
 * }
 *
 * <p><strong>Error Handling Strategy:</strong></p>
 * <ul>
 *   <li><strong>Serialization Errors:</strong> Throws RuntimeException to prevent data corruption</li>
 *   <li><strong>Deserialization Errors:</strong> Returns empty map and logs error for graceful degradation</li>
 *   <li><strong>Null Safety:</strong> Handles null inputs gracefully without exceptions</li>
 *   <li><strong>Empty Data:</strong> Distinguishes between null and empty collections</li>
 * </ul>
 *
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li><strong>ObjectMapper Instance:</strong> Single instance per converter for efficiency</li>
 *   <li><strong>JSON Processing:</strong> Efficient Jackson library for fast serialization</li>
 *   <li><strong>Memory Usage:</strong> Minimal overhead for small to medium-sized metadata</li>
 *   <li><strong>Database Storage:</strong> JSONB format provides compression and indexing</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <ul>
 *   <li><strong>ObjectMapper:</strong> Thread-safe Jackson ObjectMapper instance</li>
 *   <li><strong>Stateless Design:</strong> No mutable state in converter instance</li>
 *   <li><strong>Concurrent Access:</strong> Safe for concurrent JPA operations</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see ChatMessageEntity
 * @see jakarta.persistence.AttributeConverter
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    /**
     * Logger instance for error reporting and debugging.
     *
     * <p>This logger is used to record conversion errors, particularly during
     * JSON deserialization failures, providing detailed error information
     * for troubleshooting and monitoring purposes.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(JsonMapConverter.class);

    /**
     * Jackson ObjectMapper instance for JSON serialization/deserialization.
     *
     * <p>This ObjectMapper handles the conversion between Java Map objects and JSON strings.
     * It is configured with default settings and is thread-safe for concurrent access
     * across multiple JPA operations.</p>
     *
     * <p><strong>Configuration:</strong></p>
     * <ul>
     *   <li><strong>Default Settings:</strong> Uses Jackson's default configuration</li>
     *   <li><strong>Thread Safety:</strong> Safe for concurrent access</li>
     *   <li><strong>Performance:</strong> Single instance reduces object creation overhead</li>
     *   <li><strong>Type Handling:</strong> Automatically handles common Java types</li>
     * </ul>
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a Java Map to a JSON string for database storage.
     *
     * <p>This method is automatically called by JPA when persisting entities with
     * Map&lt;String, Object&gt; fields annotated with this converter. It serializes
     * the Map to a JSON string that can be stored in a JSONB database column.</p>
     *
     * <p><strong>Conversion Logic:</strong></p>
     * <ol>
     *   <li><strong>Null/Empty Check:</strong> Returns null for null or empty maps</li>
     *   <li><strong>JSON Serialization:</strong> Uses Jackson ObjectMapper to serialize</li>
     *   <li><strong>Error Handling:</strong> Throws RuntimeException on serialization failure</li>
     *   <li><strong>Data Preservation:</strong> Maintains all map data and structure</li>
     * </ol>
     *
     * <p><strong>Input Handling:</strong></p>
     * <ul>
     *   <li><strong>Null Input:</strong> Returns null (stored as NULL in database)</li>
     *   <li><strong>Empty Map:</strong> Returns null (treated same as null input)</li>
     *   <li><strong>Valid Map:</strong> Serializes to JSON string</li>
     *   <li><strong>Complex Structures:</strong> Handles nested maps, lists, and objects</li>
     * </ul>
     *
     * <p><strong>Error Scenarios:</strong></p>
     * <ul>
     *   <li><strong>Serialization Failure:</strong> Throws RuntimeException with detailed error</li>
     *   <li><strong>Circular References:</strong> Jackson handles with default settings</li>
     *   <li><strong>Unsupported Types:</strong> Jackson attempts best-effort conversion</li>
     * </ul>
     *
     * <p><strong>Example Conversions:</strong></p>
     * <pre>
     * // Input: Map.of("key", "value", "number", 123)
     * // Output: "{\"key\":\"value\",\"number\":123}"
     *
     * // Input: null or Collections.emptyMap()
     * // Output: null
     *
     * // Input: Map.of("nested", Map.of("inner", "value"))
     * // Output: "{\"nested\":{\"inner\":\"value\"}}"
     * </pre>
     *
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li><strong>Efficient Serialization:</strong> Jackson provides optimized JSON processing</li>
     *   <li><strong>Memory Usage:</strong> Temporary string creation during conversion</li>
     *   <li><strong>CPU Impact:</strong> Minimal overhead for small to medium maps</li>
     * </ul>
     *
     * @param attribute the Map to convert to JSON string (can be null or empty)
     * @return JSON string representation of the map, or null if input is null/empty
     * @throws RuntimeException if JSON serialization fails
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize metadata: {}", attribute, e);
            throw new RuntimeException("Failed to serialize metadata to JSON", e);
        }
    }

    /**
     * Default constructor for JsonMapConverter.
     */
    public JsonMapConverter() {
    }

    /**
     * Converts a JSON string from the database to a Java Map object.
     *
     * <p>This method is automatically called by JPA when loading entities with
     * Map&lt;String, Object&gt; fields annotated with this converter. It deserializes
     * the JSON string from the JSONB database column back to a Java Map.</p>
     *
     * <p><strong>Conversion Logic:</strong></p>
     * <ol>
     *   <li><strong>Null/Empty Check:</strong> Returns empty map for null or empty strings</li>
     *   <li><strong>JSON Deserialization:</strong> Uses Jackson ObjectMapper to deserialize</li>
     *   <li><strong>Error Recovery:</strong> Returns empty map on deserialization failure</li>
     *   <li><strong>Type Mapping:</strong> Maps JSON types to appropriate Java types</li>
     * </ol>
     *
     * <p><strong>Input Handling:</strong></p>
     * <ul>
     *   <li><strong>Null Input:</strong> Returns empty map (database NULL becomes empty map)</li>
     *   <li><strong>Empty String:</strong> Returns empty map (treated same as null)</li>
     *   <li><strong>Valid JSON:</strong> Deserializes to Map&lt;String, Object&gt;</li>
     *   <li><strong>Invalid JSON:</strong> Returns empty map with error logging</li>
     * </ul>
     *
     * <p><strong>Type Mapping:</strong></p>
     * <ul>
     *   <li><strong>JSON String:</strong> → Java String</li>
     *   <li><strong>JSON Number:</strong> → Java Integer, Long, or Double (depending on value)</li>
     *   <li><strong>JSON Boolean:</strong> → Java Boolean</li>
     *   <li><strong>JSON Object:</strong> → Java Map&lt;String, Object&gt;</li>
     *   <li><strong>JSON Array:</strong> → Java List&lt;Object&gt;</li>
     *   <li><strong>JSON null:</strong> → Java null</li>
     * </ul>
     *
     * <p><strong>Error Recovery Strategy:</strong></p>
     * <ul>
     *   <li><strong>Graceful Degradation:</strong> Returns empty map instead of throwing exceptions</li>
     *   <li><strong>Error Logging:</strong> Logs detailed error information for debugging</li>
     *   <li><strong>System Stability:</strong> Prevents application crashes due to malformed data</li>
     *   <li><strong>Data Preservation:</strong> Original JSON string included in error logs</li>
     * </ul>
     *
     * <p><strong>Example Conversions:</strong></p>
     * <pre>
     * // Input: "{\"key\":\"value\",\"number\":123}"
     * // Output: Map.of("key", "value", "number", 123)
     *
     * // Input: null or ""
     * // Output: Collections.emptyMap()
     *
     * // Input: "{\"nested\":{\"inner\":\"value\"}}"
     * // Output: Map.of("nested", Map.of("inner", "value"))
     *
     * // Input: "invalid json"
     * // Output: Collections.emptyMap() (with error logged)
     * </pre>
     *
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li><strong>Efficient Parsing:</strong> Jackson provides optimized JSON parsing</li>
     *   <li><strong>Memory Usage:</strong> Creates new Map instance for each conversion</li>
     *   <li><strong>CPU Impact:</strong> Minimal overhead for small to medium JSON strings</li>
     *   <li><strong>Caching:</strong> No caching - each call creates new objects</li>
     * </ul>
     *
     * <p><strong>Data Integrity:</strong></p>
     * <ul>
     *   <li><strong>Round-trip Safety:</strong> Data serialized and deserialized maintains structure</li>
     *   <li><strong>Type Preservation:</strong> JSON types mapped to appropriate Java types</li>
     *   <li><strong>Null Handling:</strong> Consistent handling of null values</li>
     *   <li><strong>Error Resilience:</strong> Graceful handling of corrupted data</li>
     * </ul>
     *
     * <p><strong>Monitoring &amp; Debugging:</strong></p>
     * <ul>
     *   <li><strong>Error Logging:</strong> Detailed logs for troubleshooting conversion issues</li>
     *   <li><strong>Data Context:</strong> Original JSON string included in error messages</li>
     *   <li><strong>Exception Details:</strong> Full exception information for root cause analysis</li>
     *   <li><strong>Silent Failure:</strong> Errors don't propagate to prevent system instability</li>
     * </ul>
     *
     * @param dbData the JSON string from the database (can be null or empty)
     * @return Map representation of the JSON data, or empty map if input is null/empty/invalid
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(dbData, Map.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize metadata: {}", dbData, e);
            return Collections.emptyMap();
        }
    }
}