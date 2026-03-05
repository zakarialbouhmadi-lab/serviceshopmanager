package com.zakilb.servicemanager.ui.viewmodels;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrderItem;
import javafx.beans.property.*;

/**
 * View model for WorkOrderItem data binding
 */
public class WorkOrderItemViewModel {
    
    private final IntegerProperty itemNumber;
    private final ObjectProperty<ServiceType> serviceType;
    private final StringProperty serviceTypeName;
    private final StringProperty price;
    private final IntegerProperty priceCents;
    private final DoubleProperty discountPercent;
    private final StringProperty finalPrice;
    private final IntegerProperty finalPriceCents;
    private final StringProperty barcode;
    private final StringProperty notes;
    private final StringProperty itemName;        // Name/description of serviced item


    /**
     * Constructor for existing work order item
     */
    public WorkOrderItemViewModel(WorkOrderItem item, ServiceType serviceType) {
        this.itemNumber = new SimpleIntegerProperty(item.getItemNumber());
        this.serviceType = new SimpleObjectProperty<>(serviceType);
        this.serviceTypeName = new SimpleStringProperty(serviceType != null ? serviceType.getName() : "");
        this.priceCents = new SimpleIntegerProperty(serviceType != null ? serviceType.getPriceCents() : 0);
        this.price = new SimpleStringProperty(formatPrice(serviceType != null ? serviceType.getPriceCents() : 0));
        this.discountPercent = new SimpleDoubleProperty(item.getDiscountPercent());
        this.finalPriceCents = new SimpleIntegerProperty(calculateFinalPrice(
            serviceType != null ? serviceType.getPriceCents() : 0, item.getDiscountPercent()));
        this.finalPrice = new SimpleStringProperty(formatPrice(finalPriceCents.get()));
        this.barcode = new SimpleStringProperty(item.getBarcode() != null ? item.getBarcode() : "");
        this.notes = new SimpleStringProperty(item.getNotes() != null ? item.getNotes() : "");
        this.itemName = new SimpleStringProperty(item.getItemName() != null ? item.getItemName() : "");

        // Update final price when discount changes
        discountPercent.addListener((obs, oldVal, newVal) -> updateFinalPrice());
    }

    /**
     * Constructor for new item (during order creation)
     */
    public WorkOrderItemViewModel(int itemNumber, ServiceType serviceType, String notes) {
        this.itemNumber = new SimpleIntegerProperty(itemNumber);
        this.serviceType = new SimpleObjectProperty<>(serviceType);
        this.serviceTypeName = new SimpleStringProperty(serviceType != null ? serviceType.getName() : "");
        this.priceCents = new SimpleIntegerProperty(serviceType != null ? serviceType.getPriceCents() : 0);
        this.price = new SimpleStringProperty(formatPrice(serviceType != null ? serviceType.getPriceCents() : 0));
        this.discountPercent = new SimpleDoubleProperty(0.0);
        this.finalPriceCents = new SimpleIntegerProperty(serviceType != null ? serviceType.getPriceCents() : 0);
        this.finalPrice = new SimpleStringProperty(formatPrice(finalPriceCents.get()));
        this.barcode = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty(notes != null ? notes : "");
        this.itemName = new SimpleStringProperty("");

        // Update final price when discount changes
        discountPercent.addListener((obs, oldVal, newVal) -> updateFinalPrice());
    }

    /**
     * Constructor for new item with discount
     */
    public WorkOrderItemViewModel(int itemNumber, ServiceType serviceType, String notes, double discountPercent) {
        this.itemNumber = new SimpleIntegerProperty(itemNumber);
        this.serviceType = new SimpleObjectProperty<>(serviceType);
        this.serviceTypeName = new SimpleStringProperty(serviceType != null ? serviceType.getName() : "");
        this.priceCents = new SimpleIntegerProperty(serviceType != null ? serviceType.getPriceCents() : 0);
        this.price = new SimpleStringProperty(formatPrice(serviceType != null ? serviceType.getPriceCents() : 0));
        this.discountPercent = new SimpleDoubleProperty(discountPercent);
        this.finalPriceCents = new SimpleIntegerProperty(calculateFinalPrice(
            serviceType != null ? serviceType.getPriceCents() : 0, discountPercent));
        this.finalPrice = new SimpleStringProperty(formatPrice(finalPriceCents.get()));
        this.barcode = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty(notes != null ? notes : "");
        this.itemName = new SimpleStringProperty("");

        // Update final price when discount changes
        this.discountPercent.addListener((obs, oldVal, newVal) -> updateFinalPrice());
    }

    /**
     * Constructor for new item with all fields
     */
    public WorkOrderItemViewModel(int itemNumber, ServiceType serviceType, String notes,
                                  double discountPercent, String itemName) {
        this.itemNumber = new SimpleIntegerProperty(itemNumber);
        this.serviceType = new SimpleObjectProperty<>(serviceType);
        this.serviceTypeName = new SimpleStringProperty(serviceType != null ? serviceType.getName() : "");
        this.priceCents = new SimpleIntegerProperty(serviceType != null ? serviceType.getPriceCents() : 0);
        this.price = new SimpleStringProperty(formatPrice(serviceType != null ? serviceType.getPriceCents() : 0));
        this.discountPercent = new SimpleDoubleProperty(discountPercent);
        this.finalPriceCents = new SimpleIntegerProperty(calculateFinalPrice(
            serviceType != null ? serviceType.getPriceCents() : 0, discountPercent));
        this.finalPrice = new SimpleStringProperty(formatPrice(finalPriceCents.get()));
        this.barcode = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty(notes != null ? notes : "");
        this.itemName = new SimpleStringProperty(itemName != null ? itemName : "");

        // Update final price when discount changes
        this.discountPercent.addListener((obs, oldVal, newVal) -> updateFinalPrice());
    }

    /**
     * Format price from cents to PLN string
     */
    private String formatPrice(int cents) {
        double pln = cents / 100.0;
        return String.format("%.2f PLN", pln);
    }

    /**
     * Calculate final price after discount
     */
    private int calculateFinalPrice(int originalCents, double discountPercent) {
        return (int) (originalCents * (1 - discountPercent / 100.0));
    }

    /**
     * Update final price based on current discount
     */
    private void updateFinalPrice() {
        int finalCents = calculateFinalPrice(priceCents.get(), discountPercent.get());
        finalPriceCents.set(finalCents);
        finalPrice.set(formatPrice(finalCents));
    }

    // Getters for properties
    public IntegerProperty itemNumberProperty() {
        return itemNumber;
    }

    public ObjectProperty<ServiceType> serviceTypeProperty() {
        return serviceType;
    }

    public StringProperty serviceTypeNameProperty() {
        return serviceTypeName;
    }

    public StringProperty priceProperty() {
        return price;
    }

    public DoubleProperty discountPercentProperty() {
        return discountPercent;
    }

    public StringProperty finalPriceProperty() {
        return finalPrice;
    }


    public StringProperty notesProperty() {
        return notes;
    }



    public ServiceType getServiceType() {
        return serviceType.get();
    }

    public String getPrice() {
        return price.get();
    }

    public int getPriceCents() {
        return priceCents.get();
    }

    public double getDiscountPercent() {
        return discountPercent.get();
    }

    public String getFinalPrice() {
        return finalPrice.get();
    }

    public int getFinalPriceCents() {
        return finalPriceCents.get();
    }

    public String getBarcode() {
        return barcode.get();
    }

    public String getNotes() {
        return notes.get();
    }

    // Setters
    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public void setDiscountPercent(double discount) {
        this.discountPercent.set(discount);
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType.set(serviceType);
        this.serviceTypeName.set(serviceType != null ? serviceType.getName() : "");
        this.priceCents.set(serviceType != null ? serviceType.getPriceCents() : 0);
        this.price.set(formatPrice(serviceType != null ? serviceType.getPriceCents() : 0));
        updateFinalPrice();
    }

    // Item Name getters/setters
    public StringProperty itemNameProperty() {
        return itemName;
    }

    public String getItemName() {
        return itemName.get();
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName != null ? itemName : "");
    }
}
