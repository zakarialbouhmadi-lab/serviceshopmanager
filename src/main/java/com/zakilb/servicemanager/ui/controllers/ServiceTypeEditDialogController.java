package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.service.ServiceTypeService;
import com.zakilb.servicemanager.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Service Type Edit Dialog
 */
public class ServiceTypeEditDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceTypeEditDialogController.class);
    
    @FXML private Label codeLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label requiredFieldsLabel;
    @FXML private Label codeHelpLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;

    
    private Stage dialogStage;
    private ServiceType serviceType;
    private boolean saved = false;
    
    private final ServiceTypeService serviceTypeService;
    
    public ServiceTypeEditDialogController() {
        this.serviceTypeService = new ServiceTypeService();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing ServiceTypeEditDialogController");
        
        // Apply translations first
        applyTranslations();
        
        // Add validation listeners
        codeField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        priceField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        
        // Format price field to accept only numbers and decimal point
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d{0,2}")) {
                priceField.setText(oldVal);
            }
        });
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        if (codeLabel != null) codeLabel.setText(I18n.get("dialog.service.code"));
        if (nameLabel != null) nameLabel.setText(I18n.get("dialog.service.name"));
        if (priceLabel != null) priceLabel.setText(I18n.get("dialog.service.price"));
        if (descriptionLabel != null) descriptionLabel.setText(I18n.get("dialog.service.description"));
        if (requiredFieldsLabel != null) requiredFieldsLabel.setText(I18n.get("dialog.msg.requiredfields"));
        if (codeHelpLabel != null) codeHelpLabel.setText(I18n.get("dialog.service.codehelp"));
        if (saveButton != null) saveButton.setText(I18n.get("dialog.save"));
        if (cancelButton != null) cancelButton.setText(I18n.get("dialog.cancel"));
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        
        if (serviceType != null) {
            // Edit mode - populate fields
            codeField.setText(serviceType.getCode());
            codeField.setDisable(true); // Code cannot be changed in edit mode
            nameField.setText(serviceType.getName());
            priceField.setText(String.format("%.2f", serviceType.getPriceCents() / 100.0));
            descriptionArea.setText(serviceType.getDescription() != null ? serviceType.getDescription() : "");
        } else {
            // Add mode - fields are empty
            codeField.clear();
            codeField.setDisable(false);
            nameField.clear();
            priceField.clear();
            descriptionArea.clear();
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                if (serviceType == null) {
                    // Create new service type
                    serviceType = new ServiceType();
                    serviceType.setCode(codeField.getText().trim().toUpperCase());
                }
                
                // Update service type data
                serviceType.setName(nameField.getText().trim());
                
                // Convert price from PLN to cents
                double pricePln = Double.parseDouble(priceField.getText().trim());
                int priceCents = (int) Math.round(pricePln * 100);
                serviceType.setPriceCents(priceCents);
                
                serviceType.setDescription(
                    descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim()
                );
                
                // Save to database
                serviceTypeService.saveServiceType(serviceType);
                
                saved = true;
                logger.info("Service type saved: {}", serviceType.getName());
                dialogStage.close();
                
            } catch (Exception e) {
                logger.error("Failed to save service type", e);
                showError("Failed to save service type: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        logger.info("Service type edit cancelled");
        saved = false;
        dialogStage.close();
    }
    
    private boolean validateInput() {
        clearError();
        
        // Validate code (required, alphanumeric)
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            showError("Code is required.");
            codeField.requestFocus();
            return false;
        }
        
        if (!code.matches("[A-Za-z0-9_-]+")) {
            showError("Code must contain only letters, numbers, dashes, and underscores.");
            codeField.requestFocus();
            return false;
        }
        
        // Check code uniqueness (only for new service types)
        if (serviceType == null) {
            try {
                if (!serviceTypeService.isCodeAvailable(code.toUpperCase(), null)) {
                    showError("This code is already in use. Please choose a different code.");
                    codeField.requestFocus();
                    return false;
                }
            } catch (Exception e) {
                logger.error("Failed to check code availability", e);
                showError("Failed to validate code: " + e.getMessage());
                return false;
            }
        }
        
        // Validate name (required)
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("Name is required.");
            nameField.requestFocus();
            return false;
        }
        
        // Validate price (required, must be positive)
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showError("Price is required.");
            priceField.requestFocus();
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                showError("Price must be a positive number.");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            priceField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
