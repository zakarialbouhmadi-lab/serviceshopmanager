package com.zakilb.servicemanager.model;

import java.time.LocalDateTime;

/**
 * Backup Log entity model
 */
public class BackupLog {
    
    private Integer id;
    private String filename;
    private LocalDateTime createdAt;
    private String method;
    
    // Method constants
    public static final String METHOD_MANUAL = "manual";
    
    public BackupLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    public BackupLog(Integer id, String filename, LocalDateTime createdAt, String method) {
        this.id = id;
        this.filename = filename;
        this.createdAt = createdAt;
        this.method = method;
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    @Override
    public String toString() {
        return "BackupLog{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", createdAt=" + createdAt +
                ", method='" + method + '\'' +
                '}';
    }
}
