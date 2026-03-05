package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for ServiceType CRUD operations
 */
public class ServiceTypeRepository {
    
    private final DatabaseManager dbManager;
    
    public ServiceTypeRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Find all service types
     */
    public List<ServiceType> findAll() throws SQLException {
        List<ServiceType> serviceTypes = new ArrayList<>();
        String sql = "SELECT * FROM service_type ORDER BY name";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                serviceTypes.add(mapResultSetToServiceType(rs));
            }
        }
        
        return serviceTypes;
    }
    
    /**
     * Find service type by ID
     */
    public ServiceType findById(int id) throws SQLException {
        String sql = "SELECT * FROM service_type WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToServiceType(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find service type by code
     */
    public ServiceType findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM service_type WHERE code = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToServiceType(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Insert new service type
     */
    public void insert(ServiceType serviceType) throws SQLException {
        String sql = "INSERT INTO service_type (code, name, price_cents, description, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, serviceType.getCode());
            pstmt.setString(2, serviceType.getName());
            pstmt.setInt(3, serviceType.getPriceCents());
            pstmt.setString(4, serviceType.getDescription());
            pstmt.setString(5, serviceType.getCreatedAt().toString());
            
            pstmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    serviceType.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update existing service type
     */
    public void update(ServiceType serviceType) throws SQLException {
        String sql = "UPDATE service_type SET code = ?, name = ?, price_cents = ?, description = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceType.getCode());
            pstmt.setString(2, serviceType.getName());
            pstmt.setInt(3, serviceType.getPriceCents());
            pstmt.setString(4, serviceType.getDescription());
            pstmt.setInt(5, serviceType.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete service type by ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM service_type WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Search service types by code, name, or description
     */
    public List<ServiceType> search(String text) throws SQLException {
        List<ServiceType> serviceTypes = new ArrayList<>();
        String sql = "SELECT * FROM service_type WHERE code LIKE ? OR name LIKE ? OR description LIKE ? ORDER BY name";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + text + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    serviceTypes.add(mapResultSetToServiceType(rs));
                }
            }
        }

        return serviceTypes;
    }

    /**
     * Check if service type code already exists
     */
    public boolean codeExists(String code, Integer excludeId) throws SQLException {
        String sql = excludeId == null 
            ? "SELECT COUNT(*) FROM service_type WHERE code = ?"
            : "SELECT COUNT(*) FROM service_type WHERE code = ? AND id != ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to ServiceType object
     */
    private ServiceType mapResultSetToServiceType(ResultSet rs) throws SQLException {
        ServiceType serviceType = new ServiceType();
        serviceType.setId(rs.getInt("id"));
        serviceType.setCode(rs.getString("code"));
        serviceType.setName(rs.getString("name"));
        serviceType.setPriceCents(rs.getInt("price_cents"));
        serviceType.setDescription(rs.getString("description"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            serviceType.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        return serviceType;
    }
}
