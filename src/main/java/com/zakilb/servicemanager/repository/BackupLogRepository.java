package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.model.BackupLog;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for BackupLog operations
 * FIXED: Proper connection management - don't close singleton connection
 */
public class BackupLogRepository {
    
    private final DatabaseManager dbManager;
    
    public BackupLogRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Add a new backup log entry
     */
    public void addLog(BackupLog log) throws SQLException {
        String sql = "INSERT INTO backup_log (filename, created_at, method) VALUES (?, ?, ?)";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, log.getFilename());
            pstmt.setString(2, log.getCreatedAt().toString());
            pstmt.setString(3, log.getMethod());
            
            pstmt.executeUpdate();
            
            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                log.setId(generatedKeys.getInt(1));
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Find all backup logs
     */
    public List<BackupLog> findAll() throws SQLException {
        List<BackupLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM backup_log ORDER BY created_at DESC";
        
        Connection conn = dbManager.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                logs.add(mapResultSetToBackupLog(rs));
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return logs;
    }
    
    /**
     * Find recent backup logs (within N days)
     */
    public List<BackupLog> findRecent(int days) throws SQLException {
        List<BackupLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM backup_log WHERE created_at >= datetime('now', '-' || ? || ' days') " +
                     "ORDER BY created_at DESC";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToBackupLog(rs));
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return logs;
    }
    
    /**
     * Get latest backup log
     */
    public BackupLog findLatest() throws SQLException {
        String sql = "SELECT * FROM backup_log ORDER BY created_at DESC LIMIT 1";
        
        Connection conn = dbManager.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return mapResultSetToBackupLog(rs);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return null;
    }
    

    
    /**
     * Delete backup log by ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM backup_log WHERE id = ?";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Delete old backup logs (older than N days)
     */
    public int deleteOlderThan(int days) throws SQLException {
        String sql = "DELETE FROM backup_log WHERE created_at < datetime('now', '-' || ? || ' days')";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Get count of backup logs
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM backup_log";
        
        Connection conn = dbManager.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to BackupLog object
     */
    private BackupLog mapResultSetToBackupLog(ResultSet rs) throws SQLException {
        BackupLog log = new BackupLog();
        log.setId(rs.getInt("id"));
        log.setFilename(rs.getString("filename"));
        log.setMethod(rs.getString("method"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            log.setCreatedAt(LocalDateTime.parse(createdAtStr));
        }
        
        return log;
    }
}
