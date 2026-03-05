package com.zakilb.servicemanager.model;

/**
 * Work Order Item entity model
 */
public class WorkOrderItem {
    
    private Integer id;
    private Integer workOrderId;
    private Integer serviceTypeId;
    private int itemNumber;
    private String barcode;
    private String notes;
    private double discountPercent;
    private String itemName;      // Name/description of serviced item (e.g., "Red Trek Bike")

    public WorkOrderItem() {
        this.discountPercent = 0.0;
    }

    public WorkOrderItem(Integer id, Integer workOrderId, Integer serviceTypeId,
                         int itemNumber, String barcode, String notes) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.serviceTypeId = serviceTypeId;
        this.itemNumber = itemNumber;
        this.barcode = barcode;
        this.notes = notes;
        this.discountPercent = 0.0;
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getWorkOrderId() {
        return workOrderId;
    }
    
    public void setWorkOrderId(Integer workOrderId) {
        this.workOrderId = workOrderId;
    }
    
    public Integer getServiceTypeId() {
        return serviceTypeId;
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }
    
    public int getItemNumber() {
        return itemNumber;
    }
    
    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public double getDiscountPercent() {
        return discountPercent;
    }
    
    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    @Override
    public String toString() {
        return "WorkOrderItem{" +
                "id=" + id +
                ", workOrderId=" + workOrderId +
                ", serviceTypeId=" + serviceTypeId +
                ", itemNumber=" + itemNumber +
                ", barcode='" + barcode + '\'' +
                '}';
    }
}
