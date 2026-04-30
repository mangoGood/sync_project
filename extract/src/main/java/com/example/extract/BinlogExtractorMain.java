package com.example.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Main class for Binlog Extractor module
 */
public class BinlogExtractorMain {
    
    private static final Logger logger = LoggerFactory.getLogger(BinlogExtractorMain.class);
    
    public static void main(String[] args) {
        logger.info("Starting Binlog Extractor Module...");
        
        try {
            // Load configuration
            Properties props = new Properties();
            InputStream input = BinlogExtractorMain.class.getClassLoader().getResourceAsStream("extract.properties");
            if (input == null) {
                throw new RuntimeException("extract.properties not found in classpath");
            }
            props.load(input);
            input.close();
            
            // Create extractor
            BinlogExtractor extractor = new BinlogExtractor(props);
            
            // Start extraction
            extractor.start();
            
            logger.info("Binlog Extractor Module completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in Binlog Extractor Module", e);
            System.exit(1);
        }
    }
}
