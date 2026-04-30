package com.example.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Abstract base class for Extractor implementations
 * 
 * @param <T> the type of input data
 * @param <E> the type of output event
 */
public abstract class AbstractExtractor<T, E> implements Extractor<T, E> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected Properties props;
    protected String lastExtractedPosition;
    
    @Override
    public void initialize(Properties props) throws Exception {
        this.props = props;
        logger.info("Initializing {} with properties", getClass().getSimpleName());
        doInitialize();
    }
    
    @Override
    public E extract(T input) throws Exception {
        logger.debug("Extracting from input: {}", input);
        return doExtract(input);
    }
    
    @Override
    public String getLastExtractedPosition() {
        return lastExtractedPosition;
    }
    
    @Override
    public void setPosition(String position) throws Exception {
        this.lastExtractedPosition = position;
        logger.info("Set position to: {}", position);
    }
    
    /**
     * Subclasses should implement this method to perform initialization
     * 
     * @throws Exception if initialization fails
     */
    protected abstract void doInitialize() throws Exception;
    
    /**
     * Subclasses should implement this method to perform extraction
     * 
     * @param input input data
     * @return extracted event
     * @throws Exception if extraction fails
     */
    protected abstract E doExtract(T input) throws Exception;
}
