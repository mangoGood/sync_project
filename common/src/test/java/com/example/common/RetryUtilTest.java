package com.example.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for RetryUtil
 */
@DisplayName("RetryUtil Tests")
class RetryUtilTest {
    
    @BeforeEach
    void setUp() {
        // Reset any state if needed
    }
    
    @Test
    @DisplayName("Should succeed on first attempt")
    void testSuccessOnFirstAttempt() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = RetryUtil.executeWithRetry(() -> {
            attempts.incrementAndGet();
            return "success";
        }, 3, 100, 1000);
        
        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should retry on failure and eventually succeed")
    void testRetryOnFailure() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = RetryUtil.executeWithRetry(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        }, 3, 100, 1000);
        
        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Should fail after max retries")
    void testFailAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Exception exception = assertThrows(Exception.class, () -> {
            RetryUtil.executeWithRetry(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Permanent failure");
            }, 3, 100, 1000);
        });
        
        assertThat(exception.getMessage()).isEqualTo("Permanent failure");
        assertThat(attempts.get()).isEqualTo(4); // Initial attempt + 3 retries
    }
    
    @Test
    @DisplayName("Should call error handler on failure")
    void testErrorHandler() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicInteger errorhandlerCalls = new AtomicInteger(0);
        
        String result = RetryUtil.executeWithRetry(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        }, 3, 100, 1000, (e, attempt, maxRetries) -> {
            errorhandlerCalls.incrementAndGet();
        });
        
        assertThat(result).isEqualTo("success");
        assertThat(errorhandlerCalls.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should execute void operation with retry")
    void testVoidOperation() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        RetryUtil.executeWithRetry(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 2) {
                throw new RuntimeException("Temporary failure");
            }
        }, 3, 100, 1000);
        
        assertThat(attempts.get()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should apply exponential backoff")
    void testExponentialBackoff() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        RetryUtil.executeWithRetry(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        }, 3, 100, 1000);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Should have at least 100ms + 150ms = 250ms delay
        assertThat(duration).isGreaterThanOrEqualTo(200);
        assertThat(attempts.get()).isEqualTo(3);
    }
}
