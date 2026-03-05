package com.zakilb.servicemanager.ui.viewmodels;

import com.zakilb.servicemanager.model.ServiceType;
import javafx.beans.property.*;

import java.time.format.DateTimeFormatter;

/**
 * View model for ServiceType data binding in JavaFX tables
 */
public class ServiceTypeViewModel {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final IntegerProperty id;
    private final StringProperty code;
    private final StringProperty name;
    private final StringProperty price; // Formatted as PLN
    private final StringProperty description;
    private final StringProperty createdAt;
    
    // Keep reference to original service type for editing
    private final ServiceType serviceType;
    
    public ServiceTypeViewModel(ServiceType serviceType) {
        this.serviceType = serviceType;
        this.id = new SimpleIntegerProperty(serviceType.getId());
        this.code = new SimpleStringProperty(serviceType.getCode());
        this.name = new SimpleStringProperty(serviceType.getName());
        this.price = new SimpleStringProperty(formatPrice(serviceType.getPriceCents()));
        this.description = new SimpleStringProperty(
            serviceType.getDescription() != null ? serviceType.getDescription() : ""
        );
        this.createdAt = new SimpleStringProperty(
            serviceType.getCreatedAt() != null ? serviceType.getCreatedAt().format(DATE_FORMATTER) : ""
        );
    }
    
    /**
     * Format price from cents to PLN string
     */
    private String formatPrice(int cents) {
        double pln = cents / 100.0;
        return String.format("%.2f", pln);
    }
    
    // Getters for properties
    public IntegerProperty idProperty() {
        return id;
    }
    
    public StringProperty codeProperty() {
        return code;
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public StringProperty priceProperty() {
        return price;
    }

    public StringProperty descriptionProperty() {
        return description;
    }
    
    public StringProperty createdAtProperty() {
        return createdAt;
    }
    
    // Getters for values
    public int getId() {
        return id.get();
    }
    
    public String getCode() {
        return code.get();
    }
    
    public String getName() {
        return name.get();
    }
    
    public String getPrice() {
        return price.get();
    }

    public String getDescription() {
        return description.get();
    }
    
    public String getCreatedAt() {
        return createdAt.get();
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
}
