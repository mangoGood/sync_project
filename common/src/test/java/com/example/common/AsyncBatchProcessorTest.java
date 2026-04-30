package com.example.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for AsyncBatchProcessor
 */
@DisplayName("AsyncBatchProcessor Tests")
class AsyncBatchProcessorTest {
    
    private AsyncBatchProcessor<String> processor;
    private List<String> processedItems;
    private AtomicInteger batchCount;
    
    @BeforeEach
    void setUp() {
        processedItems = new ArrayList<>();
        batchCount = new AtomicInteger(0);
        
        processor = new AsyncBatchProcessor<>(
            batch -> {
                batchCount.incrementAndGet();
                processedItems.addAll(batch);
            },
            10,     // batch size
            1000,   // batch timeout ms
            2,      // num workers
            100     // queue capacity
        );
    }
    
    @AfterEach
    void tearDown() {
        if (processor != null && processor.isRunning()) {
            processor.stop();
        }
    }
    
    @Test
    @DisplayName("Should start and stop processor")
    void testStartAndStop() {
        assertThat(processor.isRunning()).isFalse();
        
        processor.start();
        assertThat(processor.isRunning()).isTrue();
        
        processor.stop();
        assertThat(processor.isRunning()).isFalse();
    }
    
    @Test
    @DisplayName("Should process items in batch")
    void testProcessBatch() throws InterruptedException {
        processor.start();
        
        // Submit items
        for (int i = 0; i < 15; i++) {
            processor.submit("item-" + i);
        }
        
        // Wait for processing (increased wait time)
        Thread.sleep(5000);
        
        processor.stop();
        
        // Wait for final processing after stop
        Thread.sleep(1000);

        assertThat(processedItems).hasSize(15);
        // Should have at least 1 batch (might be 2 due to timing)
        assertThat(batchCount.get()).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    @DisplayName("Should respect batch size")
    void testBatchSize() throws InterruptedException {
        processor.start();
        
        // Submit exactly batch size items
        for (int i = 0; i < 10; i++) {
            processor.submit("item-" + i);
        }
        
        // Wait for processing (increased wait time)
        Thread.sleep(2000);
        
        processor.stop();
        
        // Should have processed all items
        assertThat(processedItems).hasSize(10);
        // Should have at least 1 batch (batch size = 10)
        assertThat(batchCount.get()).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    @DisplayName("Should handle batch timeout")
    void testBatchTimeout() throws InterruptedException {
        processor.start();
        
        // Submit fewer items than batch size
        for (int i = 0; i < 5; i++) {
            processor.submit("item-" + i);
        }
        
        // Wait for timeout
        Thread.sleep(1500);
        
        processor.stop();
        
        // Should have processed all items even though batch wasn't full
        assertThat(processedItems).hasSize(5);
        assertThat(batchCount.get()).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    @DisplayName("Should reject items when not running")
    void testRejectWhenNotRunning() {
        boolean accepted = processor.submit("item-1");
        assertThat(accepted).isFalse();
    }
    
    @Test
    @DisplayName("Should track processed count")
    void testProcessedCount() throws InterruptedException {
        processor.start();
        
        int itemCount = 25;
        for (int i = 0; i < itemCount; i++) {
            processor.submit("item-" + i);
        }
        
        // Wait for processing
        Thread.sleep(2000);
        
        processor.stop();
        
        assertThat(processor.getProcessedCount()).isEqualTo(itemCount);
    }
    
    @Test
    @DisplayName("Should track batch count")
    void testBatchCount() throws InterruptedException {
        processor.start();
        
        // Submit enough items for multiple batches
        for (int i = 0; i < 25; i++) {
            processor.submit("item-" + i);
        }
        
        // Wait for processing
        Thread.sleep(2000);
        
        processor.stop();
        
        // Should have at least 2 batches (25 items / 10 batch size)
        assertThat(processor.getBatchCount()).isGreaterThanOrEqualTo(2);
    }
    
    @Test
    @DisplayName("Should submit multiple items")
    void testSubmitAll() throws InterruptedException {
        processor.start();
        
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add("item-" + i);
        }
        
        int submitted = processor.submitAll(items);
        assertThat(submitted).isEqualTo(20);
        
        // Wait for processing
        Thread.sleep(2000);
        
        processor.stop();
        
        assertThat(processedItems).hasSize(20);
    }
    
    @Test
    @DisplayName("Should handle empty batch gracefully")
    void testEmptyBatch() throws InterruptedException {
        processor.start();
        
        // Don't submit any items
        
        // Wait for timeout
        Thread.sleep(1500);
        
        processor.stop();
        
        // Should not have processed any items
        assertThat(processedItems).isEmpty();
        assertThat(batchCount.get()).isEqualTo(0);
    }
}
