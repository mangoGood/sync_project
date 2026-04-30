package com.example.capture;

import java.io.FileInputStream;
import java.util.Properties;

public class BinlogCaptureMain {
    public static void main(String[] args) {
        try {
            // Load configuration from properties file
            Properties props = new Properties();
            if (args.length > 0) {
                props.load(new FileInputStream(args[0]));
            } else {
                // Default configuration file from classpath
                props.load(BinlogCaptureMain.class.getClassLoader().getResourceAsStream("capture.properties"));
            }

            // Create and start MySQL binlog capture
            MySQLBinlogCapture capture = new MySQLBinlogCapture();
            capture.initialize(props);
            capture.start();
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down capture...");
                    capture.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
