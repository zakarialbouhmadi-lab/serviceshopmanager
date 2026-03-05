package com.zakilb.servicemanager.model;

import java.time.LocalDateTime;

/**
 * Service Type entity model
 */
public class ServiceType {
    
    private Integer id;
    private String code;
    private String name;
    private int priceCents;
    private String description;
    private LocalDateTime createdAt;
    
    public ServiceType() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ServiceType(Integer id, String code, String name, int priceCents, String description, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.priceCents = priceCents;
        this.description = description;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPriceCents() {
        return priceCents;
    }
    
    public void setPriceCents(int priceCents) {
        this.priceCents = priceCents;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get price in PLN (złoty)
     */
    public double getPriceInPLN() {
        return priceCents / 100.0;
    }
    
    /**
     * Set price from PLN
     */
    public void setPriceFromPLN(double pricePLN) {
        this.priceCents = (int) Math.round(pricePLN * 100);
    }
    
    @Override
    public String toString() {
        return "ServiceType{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", priceCents=" + priceCents +
                '}';
    }
}
