package com.zakilb.servicemanager.repository;

import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for WorkOrderItem CRUD operations
 */
public class WorkOrderItemRepository {
    
    private final DatabaseManager dbManager;
    
    public WorkOrderItemRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Find all items for a specific work order
     */
    public List<WorkOrderItem> findByWorkOrderId(int workOrderId) throws SQLException {
        List<WorkOrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM work_order_item WHERE work_order_id = ? ORDER BY item_number";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workOrderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToWorkOrderItem(rs));
                }
            }
        }
        
        return items;
    }
    
    /**
     * Find item by ID
     */
    public WorkOrderItem findById(int id) throws SQLException {
        String sql = "SELECT * FROM work_order_item WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWorkOrderItem(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find item by barcode
     */
    public WorkOrderItem findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM work_order_item WHERE barcode = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, barcode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWorkOrderItem(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Insert new work order item
     */
    public void insert(WorkOrderItem item) throws SQLException {
        String sql = "INSERT INTO work_order_item (work_order_id, service_type_id, item_number, barcode, notes, discount_percent, item_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, item.getWorkOrderId());
            pstmt.setInt(2, item.getServiceTypeId());
            pstmt.setInt(3, item.getItemNumber());
            pstmt.setString(4, item.getBarcode());
            pstmt.setString(5, item.getNotes());
            pstmt.setDouble(6, item.getDiscountPercent());
            pstmt.setString(7, item.getItemName());
            
            pstmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
     * Update existing work order item
     */
    public void update(WorkOrderItem item) throws SQLException {
        String sql = "UPDATE work_order_item SET service_type_id = ?, notes = ?, discount_percent = ?, item_name = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, item.getServiceTypeId());
            pstmt.setString(2, item.getNotes());
            pstmt.setDouble(3, item.getDiscountPercent());
            pstmt.setString(4, item.getItemName());
            pstmt.setInt(5, item.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete work order item by ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM work_order_item WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }


    /**
     * Insert multiple items in a transaction
     */
    public void insertBatch(List<WorkOrderItem> items) throws SQLException {
        String sql = "INSERT INTO work_order_item (work_order_id, service_type_id, item_number, barcode, notes, discount_percent, item_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (WorkOrderItem item : items) {
                    pstmt.setInt(1, item.getWorkOrderId());
                    pstmt.setInt(2, item.getServiceTypeId());
                    pstmt.setInt(3, item.getItemNumber());
                    pstmt.setString(4, item.getBarcode());
                    pstmt.setString(5, item.getNotes());
                    pstmt.setDouble(6, item.getDiscountPercent());
                    pstmt.setString(7, item.getItemName());
                    pstmt.addBatch();
                }
                
                pstmt.executeBatch();
                
                // Get generated IDs
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    int index = 0;
                    while (generatedKeys.next() && index < items.size()) {
                        items.get(index).setId(generatedKeys.getInt(1));
                        index++;
                    }
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Map ResultSet to WorkOrderItem object
     */
    private WorkOrderItem mapResultSetToWorkOrderItem(ResultSet rs) throws SQLException {
        WorkOrderItem item = new WorkOrderItem();
        item.setId(rs.getInt("id"));
        item.setWorkOrderId(rs.getInt("work_order_id"));
        item.setServiceTypeId(rs.getInt("service_type_id"));
        item.setItemNumber(rs.getInt("item_number"));
        item.setBarcode(rs.getString("barcode"));
        item.setNotes(rs.getString("notes"));
        item.setDiscountPercent(rs.getDouble("discount_percent"));
        item.setItemName(rs.getString("item_name"));
        
        return item;
    }
}
