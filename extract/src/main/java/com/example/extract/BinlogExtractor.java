package com.example.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

import com.example.thl.THLEvent;
import com.example.thl.THLFileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinlogExtractor {
    private static final Logger logger = LoggerFactory.getLogger(BinlogExtractor.class);

    private String inputDir;
    private String outputDir;

    public BinlogExtractor(Properties props) {
        this.inputDir = props.getProperty("input.dir", "./output");
        this.outputDir = props.getProperty("output.dir", "./thl");
    }

    public void start() throws Exception {
        logger.info("Starting binlog extraction...");

        // Create output directory if it doesn't exist
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        // Get list of binlog files
        File inputDirFile = new File(inputDir);
        File[] binlogFiles = inputDirFile.listFiles((dir, name) -> name.startsWith("binlog-") && name.endsWith(".bin"));

        if (binlogFiles == null || binlogFiles.length == 0) {
            logger.warn("No binlog files found in directory: {}", inputDir);
            return;
        }

        // Create THL file with timestamp in filename
        String outputFileName = "thl-" + System.currentTimeMillis() + ".thl";
        File thlFile = new File(outputDir, outputFileName);
        THLFileWriter thlWriter = new THLFileWriter(thlFile.getAbsolutePath());

        try {
            long seqno = 1;
            for (File binlogFile : binlogFiles) {
                logger.info("Processing binlog file: {}", binlogFile.getName());
                seqno = processBinlogFile(binlogFile, thlWriter, seqno);
            }
        } finally {
            thlWriter.close();
        }

        logger.info("Binlog extraction completed - THL file: {}", thlFile.getAbsolutePath());
    }

    private long processBinlogFile(File binlogFile, THLFileWriter thlWriter, long seqno) throws Exception {
        try (FileInputStream fis = new FileInputStream(binlogFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Parse binlog event (simplified for demonstration)
                THLEvent event = parseBinlogEvent(buffer, 0, bytesRead, seqno);
                if (event != null) {
                    thlWriter.writeEvent(event);
                    seqno++;
                }
            }
        }
        return seqno;
    }

    private THLEvent parseBinlogEvent(byte[] buffer, int offset, int length, long seqno) {
        // Simplified binlog event parsing
        // In a real implementation, you would use a proper binlog parser
        THLEvent event = new THLEvent();
        event.setSeqno(seqno);
        event.setSourceTstamp(new Timestamp(System.currentTimeMillis()));
        event.setEventId("event-" + seqno);
        event.setSourceId("localhost");
        event.addMetadata("binlog_file", "test-binlog");
        event.addMetadata("binlog_position", "12345");
        event.setData(new byte[length]);
        System.arraycopy(buffer, offset, event.getData(), 0, length);
        return event;
    }
}
