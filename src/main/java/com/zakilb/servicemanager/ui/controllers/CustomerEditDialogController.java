package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.service.CustomerService;
import com.zakilb.servicemanager.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Customer Edit Dialog
 */
public class CustomerEditDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerEditDialogController.class);
    
    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label notesLabel;
    @FXML private Label requiredFieldsLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;
    
    private Stage dialogStage;
    private Customer customer;
    private boolean saved = false;
    
    private final CustomerService customerService;
    
    public CustomerEditDialogController() {
        this.customerService = new CustomerService();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing CustomerEditDialogController");
        
        // Apply translations first
        applyTranslations();
        
        // Add validation listeners
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        if (nameLabel != null) nameLabel.setText(I18n.get("dialog.customer.name"));
        if (phoneLabel != null) phoneLabel.setText(I18n.get("dialog.customer.phone"));
        if (emailLabel != null) emailLabel.setText(I18n.get("dialog.customer.email"));
        if (notesLabel != null) notesLabel.setText(I18n.get("dialog.customer.notes"));
        if (requiredFieldsLabel != null) requiredFieldsLabel.setText(I18n.get("dialog.msg.requiredfields"));
        if (saveButton != null) saveButton.setText(I18n.get("dialog.save"));
        if (cancelButton != null) cancelButton.setText(I18n.get("dialog.cancel"));
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        
        if (customer != null) {
            // Edit mode - populate fields
            nameField.setText(customer.getName());
            phoneField.setText(customer.getPhone() != null ? customer.getPhone() : "");
            emailField.setText(customer.getEmail() != null ? customer.getEmail() : "");
            notesArea.setText(customer.getNotes() != null ? customer.getNotes() : "");
        } else {
            // Add mode - fields are empty
            nameField.clear();
            phoneField.clear();
            emailField.clear();
            notesArea.clear();
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                if (customer == null) {
                    // Create new customer
                    customer = new Customer();
                }
                
                // Update customer data
                customer.setName(nameField.getText().trim());
                customer.setPhone(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                customer.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                customer.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());
                
                // Save to database
                customerService.saveCustomer(customer);
                
                saved = true;
                logger.info("Customer saved: {}", customer.getName());
                dialogStage.close();
                
            } catch (Exception e) {
                logger.error("Failed to save customer", e);
                showError("Failed to save customer: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        logger.info("Customer edit cancelled");
        saved = false;
        dialogStage.close();
    }
    
    private boolean validateInput() {
        clearError();
        
        // Validate name (required)
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("Name is required.");
            nameField.requestFocus();
            return false;
        }
        
        // Validate email format (if provided)
        String email = emailField.getText().trim();
        if (!email.isEmpty()) {
            if (!isValidEmail(email)) {
                showError("Email format is invalid.");
                emailField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
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
