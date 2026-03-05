package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.util.I18n;
import com.zakilb.servicemanager.repository.SettingsRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main controller for the application window
 * Manages the tab pane and menu actions
 */
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    // Tabs
    @FXML private Tab aboutTab;
    @FXML private javafx.scene.layout.HBox logoContainer;
    @FXML private Tab ordersTab;
    @FXML private Tab customersTab;
    @FXML private Tab serviceTypesTab;
    @FXML private Tab backupsTab;
    @FXML private Tab settingsTab;
    @FXML private javafx.scene.text.Text appTitleText;
    @FXML private TabPane mainTabPane;
    
    // Status bar
    @FXML private Label statusLabel;
    @FXML private Label dbStatusLabel;
    
    @FXML
    public void initialize() {
        logger.info("Initializing MainController");
        
        // Apply translations to tabs
        applyTranslations();
        
        loadLogoInHeader();
        
        // Load tab content
        loadTabContent();
        
        // Initialize status
        updateStatus("Application started");
        
        // Update database status
        updateDatabaseStatus(true);
    }
    
    /**
     * Apply translations to UI elements
     */
    private void applyTranslations() {
        if (appTitleText != null) appTitleText.setText(I18n.get("app.title"));
        if (ordersTab != null) ordersTab.setText(I18n.get("tab.orders"));
        if (customersTab != null) customersTab.setText(I18n.get("tab.customers"));
        if (serviceTypesTab != null) serviceTypesTab.setText(I18n.get("tab.services"));
        if (backupsTab != null) backupsTab.setText(I18n.get("tab.backups"));
        if (settingsTab != null) settingsTab.setText(I18n.get("tab.settings"));
        if (aboutTab != null) aboutTab.setText(I18n.get("tab.about"));
        if (statusLabel != null) statusLabel.setText(I18n.isPolish() ? "Gotowe" : "Ready");
        if (dbStatusLabel != null) updateDatabaseStatus(true);
    }
    
    /**
     * Load logo in header
     */
    private void loadLogoInHeader() {
        try {
            SettingsRepository settingsRepo =
                new SettingsRepository();
            String logoPath = settingsRepo.get("shop_logo_path", "");
            if (!logoPath.isEmpty() && logoContainer != null) {
                java.io.File logoFile = new java.io.File(logoPath);
                if (logoFile.exists()) {
                    javafx.scene.image.Image logo = new javafx.scene.image.Image(
                        logoFile.toURI().toString(), 120, 0, true, true);
                    javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logo);
                    logoView.setPreserveRatio(true);
                    logoContainer.getChildren().add(logoView);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load logo in header", e);
        }
    }
    
    /**
     * Load FXML content into tabs
     */
    private void loadTabContent() {
        try {
            // Load Orders view
            FXMLLoader ordersLoader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/orders-view.fxml")
            );
            VBox ordersContent = ordersLoader.load();
            ordersTab.setContent(ordersContent);
            
            // Load About view
            VBox aboutContent = FXMLLoader.load(
                getClass().getResource("/com/zakilb/servicemanager/fxml/about-view.fxml")
            );
            aboutTab.setContent(aboutContent);
            logger.info("Loaded orders view");
            
            // Load Customers view
            FXMLLoader customersLoader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/customers-view.fxml")
            );
            VBox customersContent = customersLoader.load();
            customersTab.setContent(customersContent);
            logger.info("Loaded customers view");
            
            // Load Service Types view
            FXMLLoader serviceTypesLoader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/service-types-view.fxml")
            );
            VBox serviceTypesContent = serviceTypesLoader.load();
            serviceTypesTab.setContent(serviceTypesContent);
            logger.info("Loaded service types view");
            
            // Load Backups view
            FXMLLoader backupsLoader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/backup-view.fxml")
            );
            VBox backupsContent = backupsLoader.load();
            backupsTab.setContent(backupsContent);
            logger.info("Loaded backups view");
            
            // Load Settings view
            FXMLLoader settingsLoader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/settings-view.fxml")
            );
            ScrollPane settingsContent = settingsLoader.load();
            settingsTab.setContent(settingsContent);
            logger.info("Loaded settings view");
            
        } catch (IOException e) {
            logger.error("Failed to load tab content", e);
            showError("Error", "Failed to load application views: " + e.getMessage());
        }
    }
    
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            logger.debug("Status updated: {}", message);
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void updateDatabaseStatus(boolean connected) {
        if (dbStatusLabel != null) {
            if (connected) {
                dbStatusLabel.setText(I18n.get("status.database.connected"));
                dbStatusLabel.setStyle("-fx-text-fill: #27ae60;");
            } else {
                dbStatusLabel.setText(I18n.get("status.database.disconnected"));
                dbStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }
}
