package com.zakilb.servicemanager.service;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.repository.CustomerRepository;
import com.zakilb.servicemanager.repository.ServiceTypeRepository;
import com.zakilb.servicemanager.repository.SettingsRepository;
import com.zakilb.servicemanager.repository.WorkOrderRepository;
import com.zakilb.servicemanager.repository.WorkOrderItemRepository;
import com.zakilb.servicemanager.util.DatabaseManager;
import com.zakilb.servicemanager.util.OrderNumberGenerator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for WorkOrder business logic
 * Handles order creation, item management, and barcode generation
 */
public class WorkOrderService {
    
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemRepository workOrderItemRepository;
    private final CustomerRepository customerRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    
    public WorkOrderService() {
        this.workOrderRepository = new WorkOrderRepository();
        this.workOrderItemRepository = new WorkOrderItemRepository();
        this.customerRepository = new CustomerRepository();
        this.serviceTypeRepository = new ServiceTypeRepository();
        this.orderNumberGenerator = new OrderNumberGenerator(new SettingsRepository());
    }
    
    public WorkOrderService(WorkOrderRepository workOrderRepository,
                           WorkOrderItemRepository workOrderItemRepository,
                           CustomerRepository customerRepository,
                           ServiceTypeRepository serviceTypeRepository,
                           OrderNumberGenerator orderNumberGenerator) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderItemRepository = workOrderItemRepository;
        this.customerRepository = customerRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    /**
     * Create a new work order with items including item names
     *
     * @param customer The customer
     * @param serviceTypeIds List of service type IDs (one per item)
     * @param itemNotes List of item notes (optional, can be null)
     * @param itemNames List of item names/descriptions (optional, can be null)
     * @param dueDate Due date for the order
     * @param orderNotes Notes for the entire order
     * @return Created WorkOrder with items
     * @throws SQLException if database error occurs
     */
    public WorkOrder createOrder(Customer customer, List<Integer> serviceTypeIds,
                                List<String> itemNotes, List<String> itemNames,
                                LocalDate dueDate, String orderNotes)
            throws SQLException {
        
        // Validate
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Valid customer is required");
        }
        
        if (serviceTypeIds == null || serviceTypeIds.isEmpty()) {
            throw new IllegalArgumentException("At least one service type is required");
        }
        
        // Verify customer exists
        Customer existingCustomer = customerRepository.findById(customer.getId());
        if (existingCustomer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        // Generate order number
        String orderNumber = orderNumberGenerator.nextNumber();
        
        // Create work order
        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderNumber(orderNumber);
        workOrder.setCustomerId(customer.getId());
        workOrder.setDueDate(dueDate);
        workOrder.setStatus(WorkOrder.STATUS_RECEIVED);
        workOrder.setNotes(orderNotes);
        
        // Insert order
        workOrderRepository.insert(workOrder);
        
        // Create items
        List<WorkOrderItem> items = new ArrayList<>();
        for (int i = 0; i < serviceTypeIds.size(); i++) {
            Integer serviceTypeId = serviceTypeIds.get(i);
            
            // Verify service type exists
            ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId);
            if (serviceType == null) {
                throw new IllegalArgumentException("Service type not found: " + serviceTypeId);
            }
            
            // Create item
            WorkOrderItem item = new WorkOrderItem();
            item.setWorkOrderId(workOrder.getId());
            item.setServiceTypeId(serviceTypeId);
            item.setItemNumber(i + 1);
            
            // Generate barcode
            String barcode = OrderNumberGenerator.generateItemBarcode(orderNumber, i + 1);
            item.setBarcode(barcode);
            
            // Set notes if provided
            if (itemNotes != null && i < itemNotes.size()) {
                item.setNotes(itemNotes.get(i));
            }
            
            // Set item name if provided
            if (itemNames != null && i < itemNames.size()) {
                item.setItemName(itemNames.get(i));
            }
            
            items.add(item);
        }
        
        // Insert all items in batch
        workOrderItemRepository.insertBatch(items);
        
        return workOrder;
    }
    
    /**
     * Get all work orders
     */
    public List<WorkOrder> getAllOrders() throws SQLException {
        return workOrderRepository.findAll();
    }
    
    /**
     * Search work orders by order number, status, or notes
     */
    public List<WorkOrder> searchOrders(String searchText) throws SQLException {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllOrders();
        }
        return workOrderRepository.search(searchText.trim());
    }
    

    /**
     * Get orders by status
     */
    public List<WorkOrder> getOrdersByStatus(String status) throws SQLException {
        return workOrderRepository.findByStatus(status);
    }
    
    /**
     * Get orders within date range
     */
    public List<WorkOrder> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return workOrderRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get all items for a work order
     */
    public List<WorkOrderItem> getOrderItems(int workOrderId) throws SQLException {
        return workOrderItemRepository.findByWorkOrderId(workOrderId);
    }

    
    /**
     * Update work order status
     */
    public void updateStatus(int orderId, String newStatus) throws SQLException {
        updateStatus(orderId, newStatus, null);
    }
    
    /**
     * Update work order status with notes
     */
    public void updateStatus(int orderId, String newStatus, String notes) throws SQLException {
        // Validate status
        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        
        // Check if order exists and get old status
        WorkOrder order = workOrderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        
        String oldStatus = order.getStatus();
        
        // Update status
        workOrderRepository.updateStatus(orderId, newStatus);
        
        // Log status change with notes
        logStatusChange(orderId, oldStatus, newStatus, notes);
    }
    
    /**
     * Log status change to history
     */
    private void logStatusChange(int orderId, String oldStatus, String newStatus, String notes) throws SQLException {
        String sql = "INSERT INTO order_status_history (work_order_id, old_status, new_status, notes) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, oldStatus);
            stmt.setString(3, newStatus);
            stmt.setString(4, notes);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get status change history for an order
     * @deprecated Use getStatusHistoryDetailed() instead for better formatting
     */
    public List<String> getStatusHistory(int orderId) throws SQLException {
        List<String> history = new ArrayList<>();
        String sql = "SELECT old_status, new_status, notes, datetime(changed_at, 'localtime') as changed_at FROM order_status_history WHERE work_order_id = ? ORDER BY changed_at DESC";
        
        try (var conn = DatabaseManager.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            var rs = stmt.executeQuery();
            
            while (rs.next()) {
                String oldStatus = rs.getString("old_status");
                String newStatus = rs.getString("new_status");
                String notes = rs.getString("notes");
                String changedAt = rs.getString("changed_at");
                
                String entry = String.format("%s: %s → %s", changedAt, oldStatus, newStatus);
                if (notes != null && !notes.isEmpty()) {
                    entry += "\nNote: " + notes;
                }
                history.add(entry);
            }
        }
        
        return history;
    }
    
    /**
     * Get detailed status change history for an order
     */
    public List<StatusHistoryEntry> getStatusHistoryDetailed(int orderId) throws SQLException {
        List<StatusHistoryEntry> history = new ArrayList<>();
        String sql = "SELECT old_status, new_status, notes, datetime(changed_at, 'localtime') as changed_at FROM order_status_history WHERE work_order_id = ? ORDER BY changed_at DESC";
        
        try (var conn = DatabaseManager.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            var rs = stmt.executeQuery();
            
            while (rs.next()) {
                StatusHistoryEntry entry = new StatusHistoryEntry(
                    rs.getString("old_status"),
                    rs.getString("new_status"),
                    rs.getString("notes"),
                    rs.getString("changed_at")
                );
                history.add(entry);
            }
        }
        
        return history;
    }
    
    /**
     * Status history entry
     */
    public static class StatusHistoryEntry {
        private final String oldStatus;
        private final String newStatus;
        private final String notes;
        private final String changedAt;
        
        public StatusHistoryEntry(String oldStatus, String newStatus, String notes, String changedAt) {
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.notes = notes;
            this.changedAt = changedAt;
        }
        
        public String getOldStatus() {
            return oldStatus;
        }
        
        public String getNewStatus() {
            return newStatus;
        }
        
        public String getNotes() {
            return notes;
        }
        
        public String getChangedAt() {
            return changedAt;
        }
        
        public boolean hasNotes() {
            return notes != null && !notes.trim().isEmpty();
        }
    }
    
    /**
     * Update discount for a work order item
     */
    public void updateItemDiscount(int itemId, double discountPercent) throws SQLException {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        
        WorkOrderItem item = workOrderItemRepository.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Work order item not found");
        }
        
        item.setDiscountPercent(discountPercent);
        workOrderItemRepository.update(item);
    }
    
    /**
     * Update entire work order
     */
    public void updateOrder(WorkOrder workOrder) throws SQLException {
        if (workOrder == null || workOrder.getId() == null) {
            throw new IllegalArgumentException("Valid work order with ID is required");
        }
        
        // Check if order exists
        WorkOrder existing = workOrderRepository.findById(workOrder.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Work order not found");
        }
        
        // Validate status
        if (!isValidStatus(workOrder.getStatus())) {
            throw new IllegalArgumentException("Invalid status: " + workOrder.getStatus());
        }
        
        workOrderRepository.update(workOrder);
    }

    
    /**
     * Validate status value
     */
    private boolean isValidStatus(String status) {
        return WorkOrder.STATUS_RECEIVED.equals(status) ||
               WorkOrder.STATUS_IN_PROGRESS.equals(status) ||
               WorkOrder.STATUS_READY.equals(status) ||
               WorkOrder.STATUS_PICKED_UP.equals(status) ||
               WorkOrder.STATUS_CANCELED.equals(status);
    }
    
    /**
     * Get service type by ID (convenience method for UI)
     */
    public ServiceType getServiceTypeById(int serviceTypeId) throws SQLException {
        return serviceTypeRepository.findById(serviceTypeId);
    }
    
    /**
     * Inner class to hold work order with all details
     */
    public static class WorkOrderWithDetails {
        private final WorkOrder order;
        private final Customer customer;
        private final List<WorkOrderItem> items;
        
        public WorkOrderWithDetails(WorkOrder order, Customer customer, List<WorkOrderItem> items) {
            this.order = order;
            this.customer = customer;
            this.items = items;
        }
        
        public WorkOrder getOrder() {
            return order;
        }
        
        public Customer getCustomer() {
            return customer;
        }
        
        public List<WorkOrderItem> getItems() {
            return items;
        }
    }
    
    /**
     * Inner class to hold item with service type details
     */
    public static class WorkOrderItemWithDetails {
        private final WorkOrderItem item;
        private final ServiceType serviceType;
        
        public WorkOrderItemWithDetails(WorkOrderItem item, ServiceType serviceType) {
            this.item = item;
            this.serviceType = serviceType;
        }
        
        public WorkOrderItem getItem() {
            return item;
        }
        
        public ServiceType getServiceType() {
            return serviceType;
        }
    }
}
