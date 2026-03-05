package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for WorkOrder CRUD operations
 */
public class WorkOrderRepository {
    
    private final DatabaseManager dbManager;
    
    public WorkOrderRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Find all work orders
     */
    public List<WorkOrder> findAll() throws SQLException {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM work_order ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToWorkOrder(rs));
            }
        }
        
        return orders;
    }
    
    /**
     * Find work order by ID
     */
    public WorkOrder findById(int id) throws SQLException {
        String sql = "SELECT * FROM work_order WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWorkOrder(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find work order by order number
     */
    public WorkOrder findByOrderNumber(String orderNumber) throws SQLException {
        String sql = "SELECT * FROM work_order WHERE order_number = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, orderNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWorkOrder(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find work orders by customer ID
     */
    public List<WorkOrder> findByCustomerId(int customerId) throws SQLException {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM work_order WHERE customer_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToWorkOrder(rs));
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Find work orders by status
     */
    public List<WorkOrder> findByStatus(String status) throws SQLException {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM work_order WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToWorkOrder(rs));
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Find work orders by date range
     */
    public List<WorkOrder> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM work_order WHERE date(created_at) BETWEEN ? AND ? ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToWorkOrder(rs));
                }
            }
        }
        
        return orders;
    }

    /**
     * Search work orders by order number, status, or notes
     */
    public List<WorkOrder> search(String text) throws SQLException {
        List<WorkOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM work_order WHERE order_number LIKE ? OR status LIKE ? OR notes LIKE ? ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + text + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToWorkOrder(rs));
                }
            }
        }

        return orders;
    }

    /**
     * Insert new work order
     */
    public void insert(WorkOrder workOrder) throws SQLException {
        String sql = "INSERT INTO work_order (order_number, customer_id, due_date, status, notes, amount_paid, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, workOrder.getOrderNumber());
            pstmt.setInt(2, workOrder.getCustomerId());
            pstmt.setString(3, workOrder.getDueDate() != null ? workOrder.getDueDate().toString() : null);
            pstmt.setString(4, workOrder.getStatus());
            pstmt.setString(5, workOrder.getNotes());
            pstmt.setInt(6, workOrder.getAmountPaid() != null ? workOrder.getAmountPaid() : 0);
            pstmt.setString(7, workOrder.getCreatedAt().toString());
            
            pstmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    workOrder.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update existing work order
     */
    public void update(WorkOrder workOrder) throws SQLException {
        String sql = "UPDATE work_order SET customer_id = ?, due_date = ?, status = ?, notes = ?, amount_paid = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workOrder.getCustomerId());
            pstmt.setString(2, workOrder.getDueDate() != null ? workOrder.getDueDate().toString() : null);
            pstmt.setString(3, workOrder.getStatus());
            pstmt.setString(4, workOrder.getNotes());
            pstmt.setInt(5, workOrder.getAmountPaid() != null ? workOrder.getAmountPaid() : 0);
            pstmt.setInt(6, workOrder.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update work order status
     */
    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE work_order SET status = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete work order by ID (cascades to items)
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM work_order WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Map ResultSet to WorkOrder object
     */
    private WorkOrder mapResultSetToWorkOrder(ResultSet rs) throws SQLException {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(rs.getInt("id"));
        workOrder.setOrderNumber(rs.getString("order_number"));
        workOrder.setCustomerId(rs.getInt("customer_id"));
        
        String dueDateStr = rs.getString("due_date");
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            workOrder.setDueDate(LocalDate.parse(dueDateStr));
        }
        
        workOrder.setStatus(rs.getString("status"));
        workOrder.setNotes(rs.getString("notes"));
        workOrder.setAmountPaid(rs.getInt("amount_paid"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            // Replace space with 'T' for ISO format
            workOrder.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        return workOrder;
    }
}
