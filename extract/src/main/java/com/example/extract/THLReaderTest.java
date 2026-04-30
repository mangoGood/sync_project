package com.example.extract;

import com.example.thl.THLFileReader;
import com.example.thl.THLEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Test THL file reader
 */
public class THLReaderTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: THLReaderTest <thl-file-or-directory>");
            System.out.println("Examples:");
            System.out.println("  THLReaderTest ../output/thl/thl-1775204948353.thl");
            System.out.println("  THLReaderTest ../output/thl");
            System.exit(1);
        }
        
        String path = args[0];
        File file = new File(path);
        
        if (file.isDirectory()) {
            File[] thlFiles = file.listFiles((dir, name) -> name.endsWith(".thl"));
            if (thlFiles == null || thlFiles.length == 0) {
                System.out.println("No THL files found in directory: " + path);
                System.exit(1);
            }
            
            Arrays.sort(thlFiles, Comparator.comparingLong(File::lastModified).reversed());
            file = thlFiles[0];
            System.out.println("Found " + thlFiles.length + " THL files, using latest: " + file.getName());
        }
        
        if (!file.exists()) {
            System.out.println("THL file not found: " + path);
            System.exit(1);
        }
        
        System.out.println("Reading THL file: " + file.getAbsolutePath());
        
        try (THLFileReader reader = new THLFileReader(file.getAbsolutePath())) {
            int count = 0;
            THLEvent event;
            
            while ((event = reader.readEvent()) != null) {
                count++;
                System.out.println("Event #" + count);
                System.out.println("  Seqno: " + event.getSeqno());
                System.out.println("  EventId: " + event.getEventId());
                System.out.println("  SourceId: " + event.getSourceId());
                System.out.println("  Timestamp: " + event.getSourceTstamp());
                String eventType = (String) event.getMetadata().get("event_type");
                System.out.println("  Event Type: " + eventType);
                System.out.println("  Binlog File: " + event.getMetadata().get("binlog_file"));
                System.out.println("  Binlog Position: " + event.getMetadata().get("binlog_position"));
                
                // Print key metadata for debugging
                if ("QUERY".equals(eventType)) {
                    System.out.println("  >>> database_name = " + event.getMetadata().get("database_name"));
                    System.out.println("  >>> sql = " + event.getMetadata().get("sql"));
                }
                
                System.out.println();
            }
            
            System.out.println("Total events: " + count);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
