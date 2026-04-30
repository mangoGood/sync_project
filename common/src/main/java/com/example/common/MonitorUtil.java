package com.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitoring and statistics utility class
 */
public class MonitorUtil {
    private static final Logger logger = LoggerFactory.getLogger(MonitorUtil.class);
    
    private static final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private static final Map<String, Long> timestamps = new ConcurrentHashMap<>();
    private static final Map<String, Object> metrics = new ConcurrentHashMap<>();
    
    /**
     * Increment counter
     * 
     * @param name counter name
     */
    public static void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Increment counter by value
     * 
     * @param name counter name
     * @param value increment value
     */
    public static void incrementCounter(String name, long value) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).addAndGet(value);
    }
    
    /**
     * Get counter value
     * 
     * @param name counter name
     * @return counter value
     */
    public static long getCounter(String name) {
        AtomicLong counter = counters.get(name);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Record start time
     * 
     * @param operation operation name
     */
    public static void recordStartTime(String operation) {
        timestamps.put(operation + "_start", System.currentTimeMillis());
    }
    
    /**
     * Record end time and calculate duration
     * 
     * @param operation operation name
     * @return duration in milliseconds
     */
    public static long recordEndTime(String operation) {
        Long startTime = timestamps.get(operation + "_start");
        if (startTime == null) {
            return -1;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        metrics.put(operation + "_duration", duration);
        
        return duration;
    }
    
    /**
     * Set metric value
     * 
     * @param name metric name
     * @param value metric value
     */
    public static void setMetric(String name, Object value) {
        metrics.put(name, value);
    }
    
    /**
     * Get metric value
     * 
     * @param name metric name
     * @return metric value
     */
    public static Object getMetric(String name) {
        return metrics.get(name);
    }
    
    /**
     * Log statistics
     */
    public static void logStatistics() {
        logger.info("=== Monitoring Statistics ===");
        
        // Log counters
        if (!counters.isEmpty()) {
            logger.info("Counters:");
            counters.forEach((name, counter) -> 
                logger.info("  {}: {}", name, counter.get()));
        }
        
        // Log metrics
        if (!metrics.isEmpty()) {
            logger.info("Metrics:");
            metrics.forEach((name, value) -> 
                logger.info("  {}: {}", name, value));
        }
        
        logger.info("=============================");
    }
    
    /**
     * Reset all statistics
     */
    public static void reset() {
        counters.clear();
        timestamps.clear();
        metrics.clear();
        logger.info("Monitoring statistics reset");
    }
    
    /**
     * Get all counters
     * 
     * @return map of counters
     */
    public static Map<String, Long> getAllCounters() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        counters.forEach((name, counter) -> result.put(name, counter.get()));
        return result;
    }
    
    /**
     * Get all metrics
     * 
     * @return map of metrics
     */
    public static Map<String, Object> getAllMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }
    
    /**
     * Calculate throughput
     * 
     * @param operation operation name
     * @param count event count
     * @return throughput (events per second)
     */
    public static double calculateThroughput(String operation, long count) {
        Long startTime = timestamps.get(operation + "_start");
        if (startTime == null || count == 0) {
            return 0.0;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        if (duration == 0) {
            return 0.0;
        }
        
        return (count * 1000.0) / duration;
    }
    
    /**
     * Log throughput
     * 
     * @param operation operation name
     * @param count event count
     */
    public static void logThroughput(String operation, long count) {
        double throughput = calculateThroughput(operation, count);
        logger.info("Operation '{}' throughput: {:.2f} events/second", operation, throughput);
    }
}
