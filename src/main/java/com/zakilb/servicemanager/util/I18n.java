package com.zakilb.servicemanager.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalization utility class
 * Handles loading and accessing translations
 */
public class I18n {
    
    private static final String BUNDLE_NAME = "i18n.messages";
    private static ResourceBundle bundle;
    private static Locale currentLocale = Locale.ENGLISH;
    
    static {
        loadBundle();
    }
    
    /**
     * Load the resource bundle for current locale
     */
    private static void loadBundle() {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
        } catch (Exception e) {
            System.err.println("Failed to load resource bundle: " + e.getMessage());
            // Fallback to English
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        }
    }
    
    /**
     * Get translated string by key
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // Return key if translation not found
        }
    }
    
    /**
     * Get translated string with parameters
     */
    public static String get(String key, Object... params) {
        try {
            String pattern = bundle.getString(key);
            return String.format(pattern, params);
        } catch (Exception e) {
            return key;
        }
    }
    
    /**
     * Set the application locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();
    }
    
    /**
     * Set locale by language code (e.g., "pl", "en")
     */
    public static void setLocale(String languageCode) {
        setLocale(new Locale(languageCode));
    }
    
    /**
     * Get current locale
     */
    public static Locale getLocale() {
        return currentLocale;
    }
    
    /**
     * Get current language code
     */
    public static String getLanguageCode() {
        return currentLocale.getLanguage();
    }
    
    /**
     * Check if Polish is current language
     */
    public static boolean isPolish() {
        return "pl".equals(currentLocale.getLanguage());
    }
}
