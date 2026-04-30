package com.example.increment;

import java.io.FileInputStream;
import java.util.Properties;

public class THLToSqlConverterMain {
    public static void main(String[] args) {
        try {
            // Load configuration from properties file
            Properties props = new Properties();
            if (args.length > 0) {
                props.load(new FileInputStream(args[0]));
            } else {
                // Default configuration file from classpath
                props.load(THLToSqlConverterMain.class.getClassLoader().getResourceAsStream("increment.properties"));
            }

            // Create and start THL to SQL converter
            THLToSqlConverter converter = new THLToSqlConverter(props);
            converter.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
