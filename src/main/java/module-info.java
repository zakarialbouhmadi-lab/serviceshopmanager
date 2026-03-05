module com.zakilb.servicemanager {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    
    // Database
    requires java.sql;
    
    // Barcode generation
    requires com.google.zxing;
    requires com.google.zxing.javase;
    
    // PDF generation
    requires com.github.librepdf.openpdf;
    
    // Google Drive API
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.services.drive;
    
    // Logging
    requires org.slf4j;
    requires java.desktop;
    requires com.google.api.client.extensions.java6.auth;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.jetty.auth;
    requires org.apache.pdfbox;

    // Export main packages
    exports com.zakilb.servicemanager;
    exports com.zakilb.servicemanager.model;
    exports com.zakilb.servicemanager.service;
    exports com.zakilb.servicemanager.repository;
    exports com.zakilb.servicemanager.ui.controllers;
    exports com.zakilb.servicemanager.ui.viewmodels;
    exports com.zakilb.servicemanager.util;
    
    // Open packages for JavaFX reflection
    opens com.zakilb.servicemanager to javafx.fxml;
    opens com.zakilb.servicemanager.ui.controllers to javafx.fxml;
    opens com.zakilb.servicemanager.model to javafx.base;
}