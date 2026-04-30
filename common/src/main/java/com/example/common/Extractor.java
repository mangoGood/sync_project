package com.example.common;

import java.util.Properties;

/**
 * Extractor interface for parsing captured data and generating THL events
 * 
 * @param <T> the type of input data
 * @param <E> the type of output event
 */
public interface Extractor<T, E> {
    
    /**
     * Initialize the extractor with configuration
     * 
     * @param props configuration properties
     * @throws Exception if initialization fails
     */
    void initialize(Properties props) throws Exception;
    
    /**
     * Extract events from input data
     * 
     * @param input input data
     * @return extracted event
     * @throws Exception if extraction fails
     */
    E extract(T input) throws Exception;
    
    /**
     * Get the last extracted position
     * 
     * @return last extracted position
     */
    String getLastExtractedPosition();
    
    /**
     * Set the position to start extracting from
     * 
     * @param position starting position
     * @throws Exception if setting position fails
     */
    void setPosition(String position) throws Exception;
}
