package com.example.extract;

import com.example.common.AbstractExtractor;
import com.example.thl.THLEvent;

import java.io.*;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * MongoDB Oplog Extractor implementation
 * Parses oplog data and generates THL events
 */
public class MongoOplogExtractor extends AbstractExtractor<byte[], THLEvent> {
    
    private String inputDir;
    private String outputDir;
    private long seqno = 1;
    
    @Override
    protected void doInitialize() throws Exception {
        inputDir = props.getProperty("input.dir", "./output");
        outputDir = props.getProperty("output.dir", "./thl");
        
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
        
        logger.info("MongoDB Oplog Extractor initialized - input: {}, output: {}", inputDir, outputDir);
    }
    
    @Override
    protected THLEvent doExtract(byte[] input) throws Exception {
        // TODO: Implement MongoDB oplog parsing
        // This is a placeholder implementation
        
        THLEvent thlEvent = new THLEvent();
        thlEvent.setSeqno(seqno++);
        thlEvent.setEventId("oplog-" + System.currentTimeMillis());
        thlEvent.setSourceId("mongodb");
        thlEvent.setSourceTstamp(new Timestamp(System.currentTimeMillis()));
        thlEvent.addMetadata("oplog_type", "placeholder");
        thlEvent.setData(input);
        
        lastExtractedPosition = thlEvent.getEventId();
        
        logger.debug("Extracted THL event: seqno={}, eventId={}", thlEvent.getSeqno(), thlEvent.getEventId());
        
        return thlEvent;
    }
    
    /**
     * Process all oplog files in the input directory
     * 
     * @throws Exception if processing fails
     */
    public void processAllFiles() throws Exception {
        File inputDirFile = new File(inputDir);
        File[] oplogFiles = inputDirFile.listFiles((dir, name) -> 
            name.startsWith("oplog-") && name.endsWith(".bin"));
        
        if (oplogFiles == null || oplogFiles.length == 0) {
            logger.warn("No oplog files found in directory: {}", inputDir);
            return;
        }
        
        logger.info("Found {} oplog files to process", oplogFiles.length);
        
        // Create THL output file
        String outputFileName = "thl-" + System.currentTimeMillis() + ".thl";
        File outputFile = new File(outputDir, outputFileName);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            for (File oplogFile : oplogFiles) {
                logger.info("Processing oplog file: {}", oplogFile.getName());
                processOplogFile(oplogFile, oos);
            }
        }
        
        logger.info("THL file created: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Process a single oplog file
     * 
     * @param oplogFile oplog file
     * @param oos object output stream for THL events
     * @throws Exception if processing fails
     */
    private void processOplogFile(File oplogFile, ObjectOutputStream oos) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(oplogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                byte[] eventBytes = line.getBytes("UTF-8");
                THLEvent event = extract(eventBytes);
                if (event != null) {
                    oos.writeObject(event);
                }
            }
        }
    }
}
