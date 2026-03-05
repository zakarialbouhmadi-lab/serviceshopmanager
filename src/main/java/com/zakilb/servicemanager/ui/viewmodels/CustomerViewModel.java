package com.zakilb.servicemanager.ui.viewmodels;

import com.zakilb.servicemanager.model.Customer;
import javafx.beans.property.*;

import java.time.format.DateTimeFormatter;

/**
 * View model for Customer data binding in JavaFX tables
 */
public class CustomerViewModel {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty phone;
    private final StringProperty email;
    private final StringProperty notes;
    private final IntegerProperty orderCount;
    private final StringProperty createdAt;
    
    // Keep reference to original customer for editing
    private final Customer customer;
    
    public CustomerViewModel(Customer customer, int orderCount) {
        this.customer = customer;
        this.id = new SimpleIntegerProperty(customer.getId());
        this.name = new SimpleStringProperty(customer.getName());
        this.phone = new SimpleStringProperty(customer.getPhone() != null ? customer.getPhone() : "");
        this.email = new SimpleStringProperty(customer.getEmail() != null ? customer.getEmail() : "");
        this.notes = new SimpleStringProperty(customer.getNotes() != null ? customer.getNotes() : "");
        this.orderCount = new SimpleIntegerProperty(orderCount);
        this.createdAt = new SimpleStringProperty(
            customer.getCreatedAt() != null ? customer.getCreatedAt().format(DATE_FORMATTER) : ""
        );
    }
    
    // Getters for properties
    public IntegerProperty idProperty() {
        return id;
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public StringProperty phoneProperty() {
        return phone;
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public StringProperty notesProperty() {
        return notes;
    }
    
    public IntegerProperty orderCountProperty() {
        return orderCount;
    }
    
    public StringProperty createdAtProperty() {
        return createdAt;
    }
    
    // Getters for values
    public int getId() {
        return id.get();
    }
    
    public String getName() {
        return name.get();
    }
    
    public String getPhone() {
        return phone.get();
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public String getNotes() {
        return notes.get();
    }
    
    public int getOrderCount() {
        return orderCount.get();
    }
    
    public String getCreatedAt() {
        return createdAt.get();
    }
    
    public Customer getCustomer() {
        return customer;
    }
}
