package com.zakilb.servicemanager.model;

/**
 * Settings entity model (key-value pair)
 */
public class Settings {
    
    private String key;
    private String value;
    
    public Settings() {
    }
    
    public Settings(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    // Getters and Setters
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "Settings{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
