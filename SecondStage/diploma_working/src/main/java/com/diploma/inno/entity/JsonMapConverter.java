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
 * JPA AttributeConverter for bidirectional conversion between Map&lt;String, Object&gt; &amp; JSON strings.
 *
 * <p>This converter provides seamless integration between Java Map objects and JSON string
 * representations for database storage in CI/CD anomaly detection &amp; AI analysis systems.
 * It enables flexible metadata storage while maintaining type safety and providing robust
 * error handling for data integrity in persistent storage operations.</p>
 *
 * <h2>Core Functionality</h2>
 * <ul>
 *   <li><strong>Bidirectional Conversion:</strong> Map&lt;String, Object&gt; ↔ JSON String conversion</li>
 *   <li><strong>Type Safety:</strong> Maintains type information during serialization/deserialization</li>
 *   <li><strong>Error Resilience:</strong> Graceful handling of malformed JSON &amp; serialization failures</li>
 *   <li><strong>Null Safety:</strong> Proper handling of null &amp; empty values</li>
 *   <li><strong>Performance Optimization:</strong> Efficient Jackson-based JSON processing</li>
 *   <li><strong>Logging Integration:</strong> Comprehensive error logging for debugging</li>
 * </ul>
 *
 * <h2>JPA Integration &amp; Usage</h2>
 * <p>The converter integrates seamlessly with JPA entities for metadata storage:</p>
 * {@snippet lang="java" :
 * @Entity
 * @Table(name = "chat_messages")
 * public class ChatMessageEntity {
 *
 *     @Column(name = "metadata")
 *     @Convert(converter = JsonMapConverter.class)
 *     private Map<String, Object> metadata;
 *
 *     // Other entity fields...
 * }
 * }
 *
 * <h2>Conversion Examples</h2>
 * <p>Map to JSON string conversion (Database Storage):</p>
 * {@snippet lang="java" :
 * Map<String, Object> metadata = new HashMap<>();
 * metadata.put("priority", "high");
 * metadata.put("tags", Arrays.asList("security", "vulnerability"));
 * metadata.put("analysis_score", 8.5);
 * metadata.put("processing_flags", Map.of("ai_analyzed", true, "requires_review", false));
 *
 * // Converter automatically serializes to:
 * // {"priority":"high","tags":["security","vulnerability"],"analysis_score":8.5,"processing_flags":{"ai_analyzed":true,"requires_review":false}}
 * }
 *
 * <p>JSON string to Map conversion (Entity Retrieval):</p>
 * {@snippet lang="json" :
 * {
 *   "source_system": "jenkins",
 *   "pipeline_stage": "security_analysis",
 *   "priority": "high",
 *   "tags": ["security", "vulnerability", "compliance"],
 *   "processing_flags": {
 *     "ai_analyzed": true,
 *     "anomaly_detected": false,
 *     "requires_review": true
 *   },
 *   "analysis_results": {
 *     "risk_score": 7.5,
 *     "confidence": 0.92,
 *     "categories": ["security", "performance"]
 *   }
 * }
 * }
 *
 * <h2>Data Type Support</h2>
 * <p>The converter supports comprehensive data types for flexible metadata storage:</p>
 * <ul>
 *   <li><strong>Primitive Types:</strong> String, Integer, Double, Boolean</li>
 *   <li><strong>Collections:</strong> Lists, Arrays, Sets (serialized as JSON arrays)</li>
 *   <li><strong>Nested Objects:</strong> Maps, POJOs (serialized as JSON objects)</li>
 *   <li><strong>Mixed Types:</strong> Heterogeneous collections with multiple data types</li>
 *   <li><strong>Null Values:</strong> Proper null handling &amp; preservation</li>
 * </ul>
 *
 * <h2>Error Handling &amp; Resilience</h2>
 * <p>The converter provides robust error handling for data integrity:</p>
 * <ul>
 *   <li><strong>Serialization Failures:</strong> Throws RuntimeException with detailed error context</li>
 *   <li><strong>Deserialization Failures:</strong> Returns empty Map with error logging</li>
 *   <li><strong>Null Input Handling:</strong> Graceful processing of null &amp; empty values</li>
 *   <li><strong>Malformed JSON:</strong> Safe fallback to empty Map for corrupted data</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <ul>
 *   <li><strong>Jackson Integration:</strong> High-performance JSON processing with ObjectMapper</li>
 *   <li><strong>Memory Efficiency:</strong> Optimized serialization for large metadata objects</li>
 *   <li><strong>Caching Strategy:</strong> Reuses ObjectMapper instance for performance</li>
 *   <li><strong>Lazy Processing:</strong> Conversion occurs only during database operations</li>
 * </ul>
 *
 * <h2>Use Cases in CI/CD Analysis</h2>
 * <p>The converter enables flexible metadata storage for various analysis scenarios:</p>
 * <ul>
 *   <li><strong>Processing Flags:</strong> Track analysis status &amp; processing state</li>
 *   <li><strong>AI Analysis Results:</strong> Store ML model outputs &amp; confidence scores</li>
 *   <li><strong>Contextual Tags:</strong> Categorization &amp; classification metadata</li>
 *   <li><strong>System Information:</strong> Source system &amp; pipeline context</li>
 *   <li><strong>Custom Extensions:</strong> Application-specific metadata requirements</li>
 * </ul>
 *
 * <h2>Database Compatibility</h2>
 * <p>The converter works with various database systems &amp; column types:</p>
 * <ul>
 *   <li><strong>PostgreSQL:</strong> TEXT, VARCHAR, JSONB columns</li>
 *   <li><strong>MySQL:</strong> TEXT, LONGTEXT, JSON columns</li>
 *   <li><strong>Oracle:</strong> CLOB, VARCHAR2 columns</li>
 *   <li><strong>SQL Server:</strong> NVARCHAR(MAX), TEXT columns</li>
 * </ul>
 *
 * <h2>Thread Safety &amp; Concurrency</h2>
 * <p>The converter is designed for safe concurrent usage:</p>
 * <ul>
 *   <li><strong>Stateless Design:</strong> No mutable state between conversion operations</li>
 *   <li><strong>ObjectMapper Safety:</strong> Jackson ObjectMapper is thread-safe</li>
 *   <li><strong>Concurrent Access:</strong> Safe for use in multi-threaded environments</li>
 *   <li><strong>JPA Integration:</strong> Compatible with JPA's concurrent entity operations</li>
 * </ul>
 *
 * <h2>Integration Points</h2>
 * <p>This converter integrates with multiple system components:</p>
 * <ul>
 *   <li><strong>ChatMessageEntity:</strong> Primary usage for metadata field conversion</li>
 *   <li><strong>JPA Framework:</strong> Automatic conversion during entity persistence</li>
 *   <li><strong>Jackson ObjectMapper:</strong> JSON serialization &amp; deserialization engine</li>
 *   <li><strong>Database Layer:</strong> Seamless integration with various database systems</li>
 * </ul>
 *
 * @author Khasan Abdurakhmanov
 * @version 1.0
 * @since 1.0
 * @see AttributeConverter
 * @see ChatMessageEntity
 * @see ObjectMapper
 * @see JsonProcessingException
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    // ========================================================================
    // CONSTANTS &amp; DEPENDENCIES
    // ========================================================================

    /**
     * Logger instance for error reporting &amp; debugging during conversion operations.
     * <p>Provides comprehensive logging for serialization failures, deserialization errors,
     * and other conversion-related issues to facilitate debugging and monitoring.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(JsonMapConverter.class);

    /**
     * Jackson ObjectMapper for JSON serialization &amp; deserialization operations.
     * <p>Configured with default settings for optimal performance and compatibility.
     * Thread-safe and reused across all conversion operations for efficiency.</p>
     *
     * <h4>ObjectMapper Configuration:</h4>
     * <ul>
     *   <li><strong>Default Settings:</strong> Standard Jackson configuration for broad compatibility</li>
     *   <li><strong>Type Handling:</strong> Automatic type inference for Map&lt;String, Object&gt;</li>
     *   <li><strong>Thread Safety:</strong> Safe for concurrent access in multi-threaded environments</li>
     *   <li><strong>Performance:</strong> Reused instance for optimal conversion performance</li>
     * </ul>
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new instance of the class with default initial values.
     * This constructor initializes the object to its default state.
     */
    public JsonMapConverter() {
    }


    // ========================================================================
    // CONVERSION METHODS - MAP TO JSON STRING
    // ========================================================================

    /**
     * Converts a Map&lt;String, Object&gt; to JSON string for database storage.
     *
     * <p>This method serializes Java Map objects into JSON string format for persistent
     * storage in database columns. It handles complex nested data structures, maintains
     * type information, and provides robust error handling for serialization failures.</p>
     *
     * <h4>Conversion Process:</h4>
     * <ol>
     *   <li><strong>Null/Empty Check:</strong> Returns null for null or empty input maps</li>
     *   <li><strong>JSON Serialization:</strong> Uses Jackson ObjectMapper for conversion</li>
     *   <li><strong>Error Handling:</strong> Throws RuntimeException on serialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ol>
     *
     * <h4>Input Examples:</h4>
     * <p>Simple metadata map:</p>
     * {@snippet lang="java" :
     * Map<String, Object> metadata = Map.of(
     *     "priority", "high",
     *     "source_system", "jenkins",
     *     "analysis_score", 8.5,
     *     "ai_analyzed", true
     * );
     * }
     *
     * <p>Complex nested metadata:</p>
     * {@snippet lang="java" :
     * Map<String, Object> metadata = Map.of(
     *     "tags", Arrays.asList("security", "vulnerability"),
     *     "processing_flags", Map.of(
     *         "ai_analyzed", true,
     *         "requires_review", false
     *     ),
     *     "analysis_results", Map.of(
     *         "risk_score", 7.5,
     *         "confidence", 0.92
     *     )
     * );
     * }
     *
     * <h4>Output JSON Examples:</h4>
     * <p>Simple metadata JSON output:</p>
     * {@snippet lang="json" :
     * {
     *   "priority": "high",
     *   "source_system": "jenkins",
     *   "analysis_score": 8.5,
     *   "ai_analyzed": true
     * }
     * }
     *
     * <p>Complex nested JSON output:</p>
     * {@snippet lang="json" :
     * {
     *   "tags": ["security", "vulnerability"],
     *   "processing_flags": {
     *     "ai_analyzed": true,
     *     "requires_review": false
     *   },
     *   "analysis_results": {
     *     "risk_score": 7.5,
     *     "confidence": 0.92
     *   }
     * }
     * }
     *
     * <h4>Error Handling:</h4>
     * <ul>
     *   <li><strong>Null Input:</strong> Returns null for null input maps</li>
     *   <li><strong>Empty Input:</strong> Returns null for empty input maps</li>
     *   <li><strong>Serialization Failure:</strong> Throws RuntimeException with detailed context</li>
     *   <li><strong>Error Logging:</strong> Logs complete error details for debugging</li>
     * </ul>
     *
     * <h4>Performance Considerations:</h4>
     * <ul>
     *   <li><strong>Jackson Efficiency:</strong> Optimized JSON serialization performance</li>
     *   <li><strong>Memory Usage:</strong> Efficient handling of large metadata objects</li>
     *   <li><strong>Type Preservation:</strong> Maintains type information during conversion</li>
     *   <li><strong>Thread Safety:</strong> Safe for concurrent conversion operations</li>
     * </ul>
     *
     * @param attribute the Map&lt;String, Object&gt; to convert to JSON string,
     *                  may be null or empty
     * @return JSON string representation of the map, or null if input is null/empty
     * @throws RuntimeException if JSON serialization fails due to unsupported types
     *                          or other Jackson processing errors
     *
     * @see ObjectMapper#writeValueAsString(Object)
     * @see JsonProcessingException
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

    // ========================================================================
    // CONVERSION METHODS - JSON STRING TO MAP
    // ========================================================================

    /**
     * Converts JSON string from database to Map&lt;String, Object&gt; for entity attribute.
     *
     * <p>This method deserializes JSON string data retrieved from database columns into
     * Java Map objects for use in entity attributes. It provides robust error handling,
     * graceful fallback for malformed data, and maintains type safety during conversion.</p>
     *
     * <h4>Conversion Process:</h4>
     * <ol>
     *   <li><strong>Null/Empty Check:</strong> Returns empty Map for null or empty input</li>
     *   <li><strong>JSON Deserialization:</strong> Uses Jackson ObjectMapper for parsing</li>
     *   <li><strong>Error Recovery:</strong> Returns empty Map on deserialization failure</li>
     *   <li><strong>Logging:</strong> Comprehensive error logging for debugging</li>
     * </ol>
     *
     * <h4>Input JSON Examples:</h4>
     * <p>Simple metadata JSON input:</p>
     * {@snippet lang="json" :
     * {
     *   "priority": "high",
     *   "source_system": "jenkins",
     *   "analysis_score": 8.5,
     *   "ai_analyzed": true
     * }
     * }
     *
     * <p>Complex nested JSON input:</p>
     * {@snippet lang="json" :
     * {
     *   "tags": ["security", "vulnerability", "compliance"],
     *   "processing_flags": {
     *     "ai_analyzed": true,
     *     "anomaly_detected": false,
     *     "requires_review": true
     *   },
     *   "analysis_results": {
     *     "risk_score": 7.5,
     *     "confidence": 0.92,
     *     "categories": ["security", "performance"]
     *   }
     * }
     * }
     *
     * <h4>Output Map Examples:</h4>
     * <p>Resulting Map structure for simple metadata:</p>
     * {@snippet lang="java" :
     * Map<String, Object> result = Map.of(
     *     "priority", "high",
     *     "source_system", "jenkins",
     *     "analysis_score", 8.5,
     *     "ai_analyzed", true
     * );
     * }
     *
     * <p>Resulting Map structure for complex nested data:</p>
     * {@snippet lang="java" :
     * Map<String, Object> result = Map.of(
     *     "tags", List.of("security", "vulnerability", "compliance"),
     *     "processing_flags", Map.of(
     *         "ai_analyzed", true,
     *         "anomaly_detected", false,
     *         "requires_review", true
     *     ),
     *     "analysis_results", Map.of(
     *         "risk_score", 7.5,
     *         "confidence", 0.92,
     *         "categories", List.of("security", "performance")
     *     )
     * );
     * }
     *
     * <h4>Error Handling &amp; Resilience:</h4>
     * <ul>
     *   <li><strong>Null Input:</strong> Returns empty Map for null database values</li>
     *   <li><strong>Empty Input:</strong> Returns empty Map for empty string values</li>
     *   <li><strong>Malformed JSON:</strong> Returns empty Map with error logging</li>
     *   <li><strong>Graceful Degradation:</strong> Never throws exceptions, always returns valid Map</li>
     * </ul>
     *
     * <h4>Type Handling:</h4>
     * <ul>
     *   <li><strong>Primitive Types:</strong> String, Integer, Double, Boolean preserved</li>
     *   <li><strong>Collections:</strong> JSON arrays converted to Java Lists</li>
     *   <li><strong>Nested Objects:</strong> JSON objects converted to nested Maps</li>
     *   <li><strong>Type Inference:</strong> Automatic type detection from JSON content</li>
     * </ul>
     *
     * <h4>Performance Characteristics:</h4>
     * <ul>
     *   <li><strong>Jackson Performance:</strong> Optimized JSON parsing with ObjectMapper</li>
     *   <li><strong>Memory Efficiency:</strong> Efficient handling of large JSON documents</li>
     *   <li><strong>Error Recovery:</strong> Fast fallback to empty Map on parsing errors</li>
     *   <li><strong>Thread Safety:</strong> Safe for concurrent deserialization operations</li>
     * </ul>
     *
     * @param dbData the JSON string retrieved from database column,
     *               may be null, empty, or malformed
     * @return Map&lt;String, Object&gt; representation of the JSON data,
     *         or empty Map if input is null/empty/malformed
     *
     * @see ObjectMapper#readValue(String, Class)
     * @see Collections#emptyMap()
     * @see JsonProcessingException
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