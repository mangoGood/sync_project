package com.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Async batch processor for high-throughput event processing
 */
public class AsyncBatchProcessor<T> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncBatchProcessor.class);
    
    private final BlockingQueue<T> queue;
    private final BatchHandler<T> handler;
    private final int batchSize;
    private final long batchTimeoutMs;
    private final int numWorkers;
    
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong batchCount = new AtomicLong(0);
    
    /**
     * Create async batch processor
     * 
     * @param handler batch handler
     * @param batchSize batch size
     * @param batchTimeoutMs batch timeout in milliseconds
     * @param numWorkers number of worker threads
     * @param queueCapacity queue capacity
     */
    public AsyncBatchProcessor(BatchHandler<T> handler, 
                               int batchSize, 
                               long batchTimeoutMs, 
                               int numWorkers,
                               int queueCapacity) {
        this.handler = handler;
        this.batchSize = batchSize;
        this.batchTimeoutMs = batchTimeoutMs;
        this.numWorkers = numWorkers;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.executor = Executors.newFixedThreadPool(numWorkers);
    }
    
    /**
     * Start the processor
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting AsyncBatchProcessor with {} workers, batch size: {}", 
                numWorkers, batchSize);
            
            for (int i = 0; i < numWorkers; i++) {
                executor.submit(this::processBatch);
            }
            
            MonitorUtil.setMetric("batch_processor_status", "running");
        }
    }
    
    /**
     * Stop the processor
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping AsyncBatchProcessor...");
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("AsyncBatchProcessor stopped. Processed: {}, Batches: {}", 
                processedCount.get(), batchCount.get());
            
            MonitorUtil.setMetric("batch_processor_status", "stopped");
            MonitorUtil.setMetric("total_processed", processedCount.get());
            MonitorUtil.setMetric("total_batches", batchCount.get());
        }
    }
    
    /**
     * Submit item for processing
     * 
     * @param item item to process
     * @return true if submitted successfully
     */
    public boolean submit(T item) {
        if (!running.get()) {
            logger.warn("Processor is not running, cannot submit item");
            return false;
        }
        
        try {
            return queue.offer(item, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while submitting item", e);
            return false;
        }
    }
    
    /**
     * Submit items for processing
     * 
     * @param items items to process
     * @return number of items submitted successfully
     */
    public int submitAll(List<T> items) {
        int count = 0;
        for (T item : items) {
            if (submit(item)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Process batches
     */
    private void processBatch() {
        List<T> batch = new ArrayList<>(batchSize);
        long lastBatchTime = System.currentTimeMillis();
        
        while (running.get() || !queue.isEmpty()) {
            try {
                // Wait for item with timeout
                T item = queue.poll(100, TimeUnit.MILLISECONDS);
                
                if (item != null) {
                    batch.add(item);
                }
                
                // Check if batch is ready
                long currentTime = System.currentTimeMillis();
                boolean batchFull = batch.size() >= batchSize;
                boolean timeoutReached = (currentTime - lastBatchTime) >= batchTimeoutMs;
                
                if (!batch.isEmpty() && (batchFull || timeoutReached)) {
                    // Process batch
                    try {
                        handler.handleBatch(batch);
                        
                        long count = processedCount.addAndGet(batch.size());
                        batchCount.incrementAndGet();
                        
                        MonitorUtil.incrementCounter("batches_processed");
                        MonitorUtil.incrementCounter("items_processed", batch.size());
                        
                        if (batchCount.get() % 100 == 0) {
                            logger.info("Processed {} batches, {} items total", 
                                batchCount.get(), count);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing batch", e);
                        MonitorUtil.incrementCounter("batch_errors");
                    }
                    
                    batch.clear();
                    lastBatchTime = currentTime;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Batch processor interrupted");
                break;
            }
        }
        
        // Process remaining items
        if (!batch.isEmpty()) {
            try {
                handler.handleBatch(batch);
                processedCount.addAndGet(batch.size());
                batchCount.incrementAndGet();
                logger.info("Processed final batch with {} items", batch.size());
            } catch (Exception e) {
                logger.error("Error processing final batch", e);
            }
        }
    }
    
    /**
     * Get queue size
     * 
     * @return queue size
     */
    public int getQueueSize() {
        return queue.size();
    }
    
    /**
     * Get processed count
     * 
     * @return processed count
     */
    public long getProcessedCount() {
        return processedCount.get();
    }
    
    /**
     * Get batch count
     * 
     * @return batch count
     */
    public long getBatchCount() {
        return batchCount.get();
    }
    
    /**
     * Check if processor is running
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Functional interface for batch handler
     */
    @FunctionalInterface
    public interface BatchHandler<T> {
        void handleBatch(List<T> batch) throws Exception;
    }
}
