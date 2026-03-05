package com.zakilb.servicemanager.ui.viewmodels;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.WorkOrder;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.format.DateTimeFormatter;

/**
 * View model for WorkOrder data binding in JavaFX tables
 */
public class WorkOrderViewModel {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final IntegerProperty id;
    private final StringProperty orderNumber;
    private final StringProperty customerName;
    private final ObjectProperty<Customer> customer;
    private final StringProperty dueDate;
    private final StringProperty status;
    private final StringProperty notes;
    private final StringProperty createdAt;
    private final ObservableList<WorkOrderItemViewModel> items;
    private final IntegerProperty totalPrice;
    private final IntegerProperty amountPaid;
    private final IntegerProperty remaining;
    
    // Keep reference to original work order
    private final WorkOrder workOrder;
    
    public WorkOrderViewModel(WorkOrder workOrder, Customer customer) {
        this.workOrder = workOrder;
        this.id = new SimpleIntegerProperty(workOrder.getId());
        this.orderNumber = new SimpleStringProperty(workOrder.getOrderNumber());
        this.customer = new SimpleObjectProperty<>(customer);
        this.customerName = new SimpleStringProperty(customer != null ? customer.getName() : "Unknown");
        this.dueDate = new SimpleStringProperty(
            workOrder.getDueDate() != null ? workOrder.getDueDate().format(DATE_FORMATTER) : ""
        );
        this.status = new SimpleStringProperty(workOrder.getStatus());
        this.notes = new SimpleStringProperty(workOrder.getNotes() != null ? workOrder.getNotes() : "");
        this.createdAt = new SimpleStringProperty(
            workOrder.getCreatedAt() != null ? workOrder.getCreatedAt().format(DATETIME_FORMATTER) : ""
        );
        this.items = FXCollections.observableArrayList();
        this.totalPrice = new SimpleIntegerProperty(0);
        this.amountPaid = new SimpleIntegerProperty(workOrder.getAmountPaid() != null ? workOrder.getAmountPaid() : 0);
        this.remaining = new SimpleIntegerProperty(0);
    }
    
    public void setTotalPrice(int cents) {
        this.totalPrice.set(cents);
        this.remaining.set(cents - this.amountPaid.get());
    }

    public StringProperty totalPriceProperty() {
        return new SimpleStringProperty(String.format("%.2f PLN", totalPrice.get() / 100.0));
    }
    
    public StringProperty amountPaidProperty() {
        return new SimpleStringProperty(String.format("%.2f PLN", amountPaid.get() / 100.0));
    }
    
    public StringProperty remainingProperty() {
        return new SimpleStringProperty(String.format("%.2f PLN", remaining.get() / 100.0));
    }

    public StringProperty orderNumberProperty() {
        return orderNumber;
    }
    
    public StringProperty customerNameProperty() {
        return customerName;
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public StringProperty notesProperty() {
        return notes;
    }
    
    public StringProperty createdAtProperty() {
        return createdAt;
    }
    
    public ObservableList<WorkOrderItemViewModel> getItems() {
        return items;
    }
    
    // Getters for values
    public int getId() {
        return id.get();
    }
    
    public String getOrderNumber() {
        return orderNumber.get();
    }
    
    public String getCustomerName() {
        return customerName.get();
    }
    
    public Customer getCustomer() {
        return customer.get();
    }

    public String getStatus() {
        return status.get();
    }
    
    public String getNotes() {
        return notes.get();
    }
    
    public String getCreatedAt() {
        return createdAt.get();
    }
    
    public WorkOrder getWorkOrder() {
        return workOrder;
    }
    
    // Setter for status
    public void setStatus(String newStatus) {
        this.status.set(newStatus);
        this.workOrder.setStatus(newStatus);
    }

}
