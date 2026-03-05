package com.zakilb.servicemanager.service;

import com.zakilb.servicemanager.model.BackupLog;
import com.zakilb.servicemanager.repository.BackupLogRepository;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for database backup management
 * Handles automatic daily backups and manual backups
 */
public class BackupService {
    
    private final BackupLogRepository backupLogRepository;
    private final DatabaseManager databaseManager;
    
    private final Path backupDir;
    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public BackupService() {
        this.backupLogRepository = new BackupLogRepository();
        this.databaseManager = DatabaseManager.getInstance();
        this.backupDir = DatabaseManager.getAppDataDirectory().resolve("backups");
    }
    
    public BackupService(BackupLogRepository backupLogRepository,
                        DatabaseManager databaseManager) {
        this.backupLogRepository = backupLogRepository;
        this.databaseManager = databaseManager;
        this.backupDir = DatabaseManager.getAppDataDirectory().resolve("backups");
    }
    
    /**
     * Create manual backup
     */
    public String createManualBackup() throws SQLException, IOException {
        return createBackup(BackupLog.METHOD_MANUAL);
    }
    
    /**
     * Create backup file
     */
    private String createBackup(String method) throws SQLException, IOException {
        // Ensure backup directory exists
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }
        
        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
        String filename = String.format("service_shop_backup_%s.db", timestamp);
        Path backupPath = backupDir.resolve(filename);
        
        // Get database file path
        String dbPath = databaseManager.getDbPath();
        Path sourcePath = Paths.get(dbPath);
        
        // Copy database file
        Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Log backup
        BackupLog log = new BackupLog();
        log.setFilename(filename);
        log.setMethod(method);
        log.setCreatedAt(LocalDateTime.now());
        backupLogRepository.addLog(log);
        
        System.out.println("Backup created: " + filename);
        
        return filename;
    }
    
    /**
     * Get all backup logs - scans actual files and syncs with database
     */
    public List<BackupLog> getAllBackups() throws SQLException, IOException {
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            return new ArrayList<>();
        }
        
        // Get all actual backup files
        Map<String, BackupLog> logsMap = new HashMap<>();
        List<BackupLog> dbLogs = backupLogRepository.findAll();
        for (BackupLog log : dbLogs) {
            logsMap.put(log.getFilename(), log);
        }
        
        // Scan directory for actual files
        try (Stream<Path> files = Files.list(backupDir)) {
            List<Path> backupFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .collect(Collectors.toList());
            
            // Add missing files to database log
            for (Path file : backupFiles) {
                String filename = file.getFileName().toString();
                if (!logsMap.containsKey(filename)) {
                    BackupLog newLog = new BackupLog();
                    newLog.setFilename(filename);
                    newLog.setMethod(BackupLog.METHOD_MANUAL);
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file).toInstant(),
                            java.time.ZoneId.systemDefault()
                        );
                        newLog.setCreatedAt(fileTime);
                    } catch (IOException e) {
                        newLog.setCreatedAt(LocalDateTime.now());
                    }
                    backupLogRepository.addLog(newLog);
                    logsMap.put(filename, newLog);
                }
            }
        }
        
        // Return all logs sorted by date
        List<BackupLog> allLogs = new ArrayList<>(logsMap.values());
        allLogs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return allLogs;
    }
    
    /**
     * Get latest backup
     */
    public BackupLog getLatestBackup() throws SQLException {
        return backupLogRepository.findLatest();
    }
    
    /**
     * Restore from backup file
     * Schedules restore for next app startup (avoids Windows file locking issues)
     */
    public void restoreFromBackup(String backupFilename) throws IOException {
        Path backupPath = backupDir.resolve(backupFilename);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found: " + backupFilename);
        }

        // Schedule restore for next startup (works reliably on all platforms including Windows)
        Path restoreFlagFile = DatabaseManager.getAppDataDirectory().resolve(".pending_restore");
        Files.writeString(restoreFlagFile, backupPath.toString());

        System.out.println("Restore scheduled for: " + backupFilename);
        System.out.println("Restart the application to complete restore.");
    }

    /**
     * Check if there's a pending restore and perform it
     * Called at application startup BEFORE opening the database
     */
    public static void performPendingRestore() {
        try {
            Path restoreFlagFile = DatabaseManager.getAppDataDirectory().resolve(".pending_restore");

            if (Files.exists(restoreFlagFile)) {
                System.out.println("Pending restore detected...");

                String backupPathStr = Files.readString(restoreFlagFile).trim();
                Path backupPath = Paths.get(backupPathStr);

                if (Files.exists(backupPath)) {
                    Path dbPath = DatabaseManager.getAppDataDirectory().resolve("service_shop_db.db");

                    // Create safety backup
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    Path safetyBackup = DatabaseManager.getAppDataDirectory()
                            .resolve("backups")
                            .resolve("pre_restore_" + timestamp + ".db");

                    // Ensure backups directory exists
                    Files.createDirectories(safetyBackup.getParent());

                    // Backup current database if it exists
                    if (Files.exists(dbPath)) {
                        Files.copy(dbPath, safetyBackup, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Safety backup created: " + safetyBackup.getFileName());
                    }

                    // Perform restore
                    Files.copy(backupPath, dbPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Database restored successfully from: " + backupPath.getFileName());
                } else {
                    System.err.println("Backup file not found: " + backupPath);
                }

                // Delete the flag file
                Files.deleteIfExists(restoreFlagFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to perform pending restore: " + e.getMessage());
            // Delete flag to prevent infinite loop
            try {
                Files.deleteIfExists(DatabaseManager.getAppDataDirectory().resolve(".pending_restore"));
            } catch (IOException ignored) {}
        }
    }
    
    /**
     * Get backup file path
     */
    public Path getBackupFilePath(String filename) {
        return backupDir.resolve(filename);
    }
    
    /**
     * Check if backup file exists
     */
    public boolean backupFileExists(String filename) {
        return Files.exists(getBackupFilePath(filename));
    }
    
    /**
     * Get backup file size in bytes
     */
    public long getBackupFileSize(String filename) throws IOException {
        Path path = getBackupFilePath(filename);
        if (Files.exists(path)) {
            return Files.size(path);
        }
        return 0;
    }
    
    /**
     * Get total size of all backups in MB
     */
    public double getTotalBackupSize() throws IOException {
        if (!Files.exists(backupDir)) {
            return 0;
        }
        
        try (Stream<Path> files = Files.list(backupDir)) {
            long totalBytes = files
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            
            return totalBytes / (1024.0 * 1024.0); // Convert to MB
        }
    }
    
    /**
     * Delete specific backup
     */
    public void deleteBackup(String filename) throws IOException, SQLException {
        Path backupPath = getBackupFilePath(filename);
        
        if (Files.exists(backupPath)) {
            Files.delete(backupPath);
            System.out.println("Deleted backup: " + filename);
        }
        
        // Also remove from log (find by filename)
        List<BackupLog> allLogs = backupLogRepository.findAll();
        for (BackupLog log : allLogs) {
            if (log.getFilename().equals(filename)) {
                backupLogRepository.delete(log.getId());
                break;
            }
        }
    }
}
