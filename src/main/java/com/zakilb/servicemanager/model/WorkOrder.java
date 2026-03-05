package com.zakilb.servicemanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Work Order entity model
 */
public class WorkOrder {
    
    private Integer id;
    private String orderNumber;
    private Integer customerId;
    private LocalDate dueDate;
    private String status;
    private String notes;
    private Integer amountPaid; // Amount paid in cents
    private LocalDateTime createdAt;
    
    // Status constants
    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_PICKED_UP = "picked_up";
    public static final String STATUS_CANCELED = "canceled";
    
    public WorkOrder() {
        this.createdAt = LocalDateTime.now();
        this.status = STATUS_RECEIVED;
        this.amountPaid = 0;
    }
    
    public WorkOrder(Integer id, String orderNumber, Integer customerId, LocalDate dueDate, 
                     String status, String notes, Integer amountPaid, LocalDateTime createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.dueDate = dueDate;
        this.status = status;
        this.notes = notes;
        this.amountPaid = amountPaid != null ? amountPaid : 0;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(Integer amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    @Override
    public String toString() {
        return "WorkOrder{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId=" + customerId +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}
