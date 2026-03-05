package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Customer CRUD operations
 */
public class CustomerRepository {
    
    private final DatabaseManager dbManager;
    
    public CustomerRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Find all customers
     */
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customer ORDER BY name";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        
        return customers;
    }
    
    /**
     * Find customer by ID
     */
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customer WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Search customers by name, email, or phone (partial match)
     */
    public List<Customer> searchByName(String text) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR notes LIKE ? ORDER BY name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + text + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        }
        
        return customers;
    }
    
    /**
     * Insert new customer
     */
    public void insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (name, phone, email, notes, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getNotes());
            pstmt.setString(5, customer.getCreatedAt().toString());
            
            pstmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update existing customer
     */
    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customer SET name = ?, phone = ?, email = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getNotes());
            pstmt.setInt(5, customer.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete customer by ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM customer WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get count of orders for a customer
     */
    public int getOrderCount(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM work_order WHERE customer_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to Customer object
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setName(rs.getString("name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setNotes(rs.getString("notes"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            customer.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        return customer;
    }
}
