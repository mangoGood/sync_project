package com.example.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MonitorUtil
 */
@DisplayName("MonitorUtil Tests")
class MonitorUtilTest {
    
    @BeforeEach
    void setUp() {
        MonitorUtil.reset();
    }
    
    @AfterEach
    void tearDown() {
        MonitorUtil.reset();
    }
    
    @Test
    @DisplayName("Should increment counter")
    void testIncrementCounter() {
        MonitorUtil.incrementCounter("test_counter");
        assertThat(MonitorUtil.getCounter("test_counter")).isEqualTo(1);
        
        MonitorUtil.incrementCounter("test_counter");
        assertThat(MonitorUtil.getCounter("test_counter")).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should increment counter by value")
    void testIncrementCounterByValue() {
        MonitorUtil.incrementCounter("test_counter", 5);
        assertThat(MonitorUtil.getCounter("test_counter")).isEqualTo(5);
        
        MonitorUtil.incrementCounter("test_counter", 3);
        assertThat(MonitorUtil.getCounter("test_counter")).isEqualTo(8);
    }
    
    @Test
    @DisplayName("Should return 0 for non-existent counter")
    void testNonExistentCounter() {
        assertThat(MonitorUtil.getCounter("non_existent")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should record start and end time")
    void testRecordTime() {
        MonitorUtil.recordStartTime("test_operation");
        
        // Simulate some work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long duration = MonitorUtil.recordEndTime("test_operation");
        
        assertThat(duration).isGreaterThanOrEqualTo(100);
        assertThat(MonitorUtil.getMetric("test_operation_duration")).isEqualTo(duration);
    }
    
    @Test
    @DisplayName("Should return -1 for missing start time")
    void testMissingStartTime() {
        long duration = MonitorUtil.recordEndTime("non_existent_operation");
        assertThat(duration).isEqualTo(-1);
    }
    
    @Test
    @DisplayName("Should set and get metric")
    void testSetAndGetMetric() {
        MonitorUtil.setMetric("test_metric", "test_value");
        assertThat(MonitorUtil.getMetric("test_metric")).isEqualTo("test_value");
        
        MonitorUtil.setMetric("test_metric_int", 42);
        assertThat(MonitorUtil.getMetric("test_metric_int")).isEqualTo(42);
    }
    
    @Test
    @DisplayName("Should calculate throughput")
    void testCalculateThroughput() {
        MonitorUtil.recordStartTime("test_operation");
        
        // Simulate some work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        double throughput = MonitorUtil.calculateThroughput("test_operation", 1000);
        
        // Should be approximately 10000 events/second (1000 events / 0.1 seconds)
        assertThat(throughput).isGreaterThan(5000.0);
    }
    
    @Test
    @DisplayName("Should return 0 throughput for missing start time")
    void testThroughputMissingStartTime() {
        double throughput = MonitorUtil.calculateThroughput("non_existent", 1000);
        assertThat(throughput).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should return 0 throughput for zero count")
    void testThroughputZeroCount() {
        MonitorUtil.recordStartTime("test_operation");
        double throughput = MonitorUtil.calculateThroughput("test_operation", 0);
        assertThat(throughput).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should reset all statistics")
    void testReset() {
        MonitorUtil.incrementCounter("test_counter");
        MonitorUtil.setMetric("test_metric", "test_value");
        MonitorUtil.recordStartTime("test_operation");
        
        MonitorUtil.reset();
        
        assertThat(MonitorUtil.getCounter("test_counter")).isEqualTo(0);
        assertThat(MonitorUtil.getMetric("test_metric")).isNull();
        assertThat(MonitorUtil.getAllCounters()).isEmpty();
        assertThat(MonitorUtil.getAllMetrics()).isEmpty();
    }
    
    @Test
    @DisplayName("Should get all counters")
    void testGetAllCounters() {
        MonitorUtil.incrementCounter("counter1", 10);
        MonitorUtil.incrementCounter("counter2", 20);
        
        java.util.Map<String, Long> counters = MonitorUtil.getAllCounters();
        
        assertThat(counters).hasSize(2);
        assertThat(counters.get("counter1")).isEqualTo(10);
        assertThat(counters.get("counter2")).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should get all metrics")
    void testGetAllMetrics() {
        MonitorUtil.setMetric("metric1", "value1");
        MonitorUtil.setMetric("metric2", 42);
        
        java.util.Map<String, Object> metrics = MonitorUtil.getAllMetrics();
        
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get("metric1")).isEqualTo("value1");
        assertThat(metrics.get("metric2")).isEqualTo(42);
    }
}
