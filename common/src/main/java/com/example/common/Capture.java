package com.example.common;

import java.util.Properties;

/**
 * Capture interface for reading data from source database
 * 
 * @param <T> the type of data captured
 */
public interface Capture<T> {
    
    /**
     * Initialize the capture with configuration
     * 
     * @param props configuration properties
     * @throws Exception if initialization fails
     */
    void initialize(Properties props) throws Exception;
    
    /**
     * Start capturing data from source
     * 
     * @throws Exception if capture fails
     */
    void start() throws Exception;
    
    /**
     * Stop capturing data
     * 
     * @throws Exception if stop fails
     */
    void stop() throws Exception;
    
    /**
     * Get the current position in the source
     * 
     * @return current position
     */
    String getCurrentPosition();
    
    /**
     * Set the position to start capturing from
     * 
     * @param position starting position
     * @throws Exception if setting position fails
     */
    void setPosition(String position) throws Exception;
}
