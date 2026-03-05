package com.zakilb.servicemanager.util;

import com.zakilb.servicemanager.repository.SettingsRepository;

import java.sql.SQLException;
import java.time.Year;

/**
 * Generates unique order numbers in format YYYY-NNNNN
 * Counter resets each year
 */
public class OrderNumberGenerator {
    
    private final SettingsRepository settingsRepository;
    private static final String COUNTER_KEY_PREFIX = "order_counter_";
    
    public OrderNumberGenerator(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }
    
    /**
     * Generate next order number for current year
     * Format: YYYY-NNNNN (e.g., 2025-00042)
     * 
     * @return Next order number
     * @throws SQLException if database error occurs
     */
    public synchronized String nextNumber() throws SQLException {
        int currentYear = Year.now().getValue();
        String counterKey = COUNTER_KEY_PREFIX + currentYear;
        
        // Get current counter for this year (default to 0)
        String counterStr = settingsRepository.get(counterKey, "0");
        int counter = Integer.parseInt(counterStr);
        
        // Increment counter
        counter++;
        
        // Save new counter value
        settingsRepository.set(counterKey, String.valueOf(counter));
        
        // Format: YYYY-NNNNN
        return String.format("%d-%05d", currentYear, counter);
    }
    
    /**
     * Get current counter value for a specific year without incrementing
     * 
     * @param year The year to check
     * @return Current counter value
     * @throws SQLException if database error occurs
     */
    public int getCurrentCounter(int year) throws SQLException {
        String counterKey = COUNTER_KEY_PREFIX + year;
        String counterStr = settingsRepository.get(counterKey, "0");
        return Integer.parseInt(counterStr);
    }
    
    /**
     * Reset counter for a specific year (use with caution!)
     * 
     * @param year The year to reset
     * @throws SQLException if database error occurs
     */
    public void resetCounter(int year) throws SQLException {
        String counterKey = COUNTER_KEY_PREFIX + year;
        settingsRepository.set(counterKey, "0");
    }
    
    /**
     * Generate barcode for a work order item
     * Format: orderNumber-itemNumber (e.g., 2025-00042-01)
     * 
     * @param orderNumber The work order number
     * @param itemNumber The item sequence number (1, 2, 3...)
     * @return Barcode string
     */
    public static String generateItemBarcode(String orderNumber, int itemNumber) {
        return String.format("%s-%02d", orderNumber, itemNumber);
    }
}
