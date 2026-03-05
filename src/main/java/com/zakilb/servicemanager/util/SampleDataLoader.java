package com.zakilb.servicemanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SampleDataLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);
    
    public static void loadSampleData() {
        logger.info("Loading sample data...");
        
        try {
            // Try to read from data directory first
            Path sampleDataPath = Paths.get("data/sample_data.sql");
            String sql;
            
            if (Files.exists(sampleDataPath)) {
                logger.info("Reading sample data from: {}", sampleDataPath.toAbsolutePath());
                sql = Files.readString(sampleDataPath);
            } else {
                logger.warn("Sample data file not found at: {}", sampleDataPath.toAbsolutePath());
                logger.info("Attempting to read from classpath...");
                
                // Try reading from classpath as fallback
                InputStream is = SampleDataLoader.class.getResourceAsStream("/db/sample_data.sql");
                if (is == null) {
                    logger.error("Sample data file not found in classpath either");
                    return;
                }
                sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));
            }
            
            // Execute SQL
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Split by semicolon and execute each statement
                String[] statements = sql.split(";");
                
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && 
                        !trimmed.startsWith("--") && 
                        !trimmed.toLowerCase().startsWith("select")) {
                        
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception e) {
                            // Log but continue - some statements might fail if data exists
                            logger.debug("Statement failed (might be expected): {}", e.getMessage());
                        }
                    }
                }
                
                logger.info("Sample data loaded successfully!");
                
            } catch (Exception e) {
                logger.error("Failed to execute sample data SQL", e);
                throw e;
            }
            
        } catch (Exception e) {
            logger.error("Failed to load sample data", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Loading sample data into database...");
        loadSampleData();
        System.out.println("Done! Check logs for details.");
    }
}
