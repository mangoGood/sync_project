package com.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retry utility class for handling transient failures
 */
public class RetryUtil {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    
    /**
     * Execute operation with retry
     * 
     * @param operation operation to execute
     * @param maxRetries maximum number of retries
     * @param initialDelayMs initial delay in milliseconds
     * @param maxDelayMs maximum delay in milliseconds
     * @param <T> return type
     * @return operation result
     * @throws Exception if all retries fail
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, 
                                         int maxRetries, 
                                         long initialDelayMs, 
                                         long maxDelayMs) throws Exception {
        return executeWithRetry(operation, maxRetries, initialDelayMs, maxDelayMs, null);
    }
    
    /**
     * Execute operation with retry and custom exception handler
     * 
     * @param operation operation to execute
     * @param maxRetries maximum number of retries
     * @param initialDelayMs initial delay in milliseconds
     * @param maxDelayMs maximum delay in milliseconds
     * @param errorHandler custom error handler
     * @param <T> return type
     * @return operation result
     * @throws Exception if all retries fail
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, 
                                         int maxRetries, 
                                         long initialDelayMs, 
                                         long maxDelayMs,
                                         ErrorHandler errorHandler) throws Exception {
        Exception lastException = null;
        long delay = initialDelayMs;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxRetries) {
                    if (errorHandler != null) {
                        errorHandler.handleError(e, attempt + 1, maxRetries);
                    } else {
                        logger.warn("Operation failed (attempt {}/{}), retrying in {} ms. Error: {}", 
                            attempt + 1, maxRetries, delay, e.getMessage());
                    }
                    
                    // Sleep before retry
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Retry interrupted", ie);
                    }
                    
                    // Exponential backoff with jitter
                    delay = Math.min((long) (delay * 1.5), maxDelayMs);
                }
            }
        }
        
        logger.error("Operation failed after {} attempts", maxRetries + 1);
        throw lastException;
    }
    
    /**
     * Execute operation with retry (no return value)
     * 
     * @param operation operation to execute
     * @param maxRetries maximum number of retries
     * @param initialDelayMs initial delay in milliseconds
     * @param maxDelayMs maximum delay in milliseconds
     * @throws Exception if all retries fail
     */
    public static void executeWithRetry(RetryableVoidOperation operation, 
                                        int maxRetries, 
                                        long initialDelayMs, 
                                        long maxDelayMs) throws Exception {
        executeWithRetry(() -> {
            operation.execute();
            return null;
        }, maxRetries, initialDelayMs, maxDelayMs);
    }
    
    /**
     * Functional interface for retryable operations
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Functional interface for retryable void operations
     */
    @FunctionalInterface
    public interface RetryableVoidOperation {
        void execute() throws Exception;
    }
    
    /**
     * Functional interface for custom error handling
     */
    @FunctionalInterface
    public interface ErrorHandler {
        void handleError(Exception e, int attempt, int maxRetries);
    }
}
