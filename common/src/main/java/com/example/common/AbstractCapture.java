package com.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Abstract base class for Capture implementations
 * 
 * @param <T> the type of data captured
 */
public abstract class AbstractCapture<T> implements Capture<T> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected Properties props;
    protected String currentPosition;
    protected boolean running = false;
    
    @Override
    public void initialize(Properties props) throws Exception {
        this.props = props;
        logger.info("Initializing {} with properties", getClass().getSimpleName());
        doInitialize();
    }
    
    @Override
    public void start() throws Exception {
        logger.info("Starting {}", getClass().getSimpleName());
        running = true;
        doStart();
    }
    
    @Override
    public void stop() throws Exception {
        logger.info("Stopping {}", getClass().getSimpleName());
        running = false;
        doStop();
    }
    
    @Override
    public String getCurrentPosition() {
        return currentPosition;
    }
    
    @Override
    public void setPosition(String position) throws Exception {
        this.currentPosition = position;
        logger.info("Set position to: {}", position);
    }
    
    /**
     * Subclasses should implement this method to perform initialization
     * 
     * @throws Exception if initialization fails
     */
    protected abstract void doInitialize() throws Exception;
    
    /**
     * Subclasses should implement this method to start capturing
     * 
     * @throws Exception if start fails
     */
    protected abstract void doStart() throws Exception;
    
    /**
     * Subclasses should implement this method to stop capturing
     * 
     * @throws Exception if stop fails
     */
    protected abstract void doStop() throws Exception;
    
    /**
     * Check if capture is running
     * 
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}
