package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for Settings (key-value store)
 * FIXED: Proper connection management - don't close singleton connection
 */
public class SettingsRepository {
    
    private final DatabaseManager dbManager;
    
    public SettingsRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Get value by key
     */
    public String get(String key) throws SQLException {
        String sql = "SELECT value FROM settings WHERE key = ?";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return null;
    }
    
    /**
     * Get value by key with default value
     */
    public String get(String key, String defaultValue) throws SQLException {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get integer value by key with default value
     */
    public int getInt(String key, int defaultValue) throws SQLException {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get boolean value by key with default value
     */
    public boolean getBoolean(String key, boolean defaultValue) throws SQLException {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get double value by key with default value
     */
    public double getDouble(String key, double defaultValue) throws SQLException {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Set or update a setting
     */
    public void set(String key, String value) throws SQLException {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Delete a setting
     */
    public void delete(String key) throws SQLException {
        String sql = "DELETE FROM settings WHERE key = ?";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Get all settings as a map
     */
    public Map<String, String> getAll() throws SQLException {
        Map<String, String> settingsMap = new HashMap<>();
        String sql = "SELECT key, value FROM settings";
        
        Connection conn = dbManager.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                settingsMap.put(rs.getString("key"), rs.getString("value"));
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        
        return settingsMap;
    }
    
    /**
     * Check if a key exists
     */
    public boolean exists(String key) throws SQLException {
        String sql = "SELECT 1 FROM settings WHERE key = ?";
        
        Connection conn = dbManager.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            
            rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Get count of settings
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM settings";
        
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
}
