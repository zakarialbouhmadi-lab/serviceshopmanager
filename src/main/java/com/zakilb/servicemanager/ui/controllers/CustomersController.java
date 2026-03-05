package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.service.CustomerService;
import com.zakilb.servicemanager.ui.viewmodels.CustomerViewModel;
import com.zakilb.servicemanager.util.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Customers view
 */
public class CustomersController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomersController.class);
    
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    @FXML private TableView<CustomerViewModel> customersTable;
    @FXML private TableColumn<CustomerViewModel, Integer> idColumn;
    @FXML private TableColumn<CustomerViewModel, String> nameColumn;
    @FXML private TableColumn<CustomerViewModel, String> phoneColumn;
    @FXML private TableColumn<CustomerViewModel, String> emailColumn;
    @FXML private TableColumn<CustomerViewModel, String> notesColumn;
    @FXML private TableColumn<CustomerViewModel, Integer> ordersColumn;
    @FXML private TableColumn<CustomerViewModel, String> createdColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    
    private final CustomerService customerService;
    private final ObservableList<CustomerViewModel> customerData;
    
    public CustomersController() {
        this.customerService = new CustomerService();
        this.customerData = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing CustomersController");
        
        // Apply translations first
        applyTranslations();
        
        // Set up table columns
        setupTableColumns();
        
        // Set up selection listener
        setupSelectionListener();
        
        // Load customers
        loadCustomers();
        
        // Set up search on Enter
        searchField.setOnAction(e -> handleSearch());
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        ordersColumn.setCellValueFactory(cellData -> cellData.getValue().orderCountProperty().asObject());
        createdColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        notesColumn.setCellValueFactory(customerViewModelStringCellDataFeatures -> customerViewModelStringCellDataFeatures.getValue().notesProperty());
        customersTable.setItems(customerData);
    }
    
    private void setupSelectionListener() {
        customersTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue != null;
                editButton.setDisable(!hasSelection);
                deleteButton.setDisable(!hasSelection);
            }
        );
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        searchField.setPromptText(I18n.get("customers.search"));
        searchButton.setText(I18n.get("customers.search.button")); // Reuse edit text for search button label
        addButton.setText("+ " + I18n.get("customers.new"));
        editButton.setText(I18n.get("customers.edit"));
        deleteButton.setText(I18n.get("customers.delete"));
        refreshButton.setText(I18n.get("orders.refresh")); // Reuse from orders
        
        // Table headers
        idColumn.setText("ID");
        nameColumn.setText(I18n.get("customers.column.name"));
        phoneColumn.setText(I18n.get("customers.column.phone"));
        emailColumn.setText(I18n.get("customers.column.email"));
        ordersColumn.setText(I18n.get("customers.column.orders"));
        createdColumn.setText(I18n.get("orders.column.date")); // Reuse date label
        notesColumn.setText("Notes");
    }
    
    private void loadCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            customerData.clear();
            
            for (Customer customer : customers) {
                int orderCount = customerService.getCustomerOrderCount(customer.getId());
                customerData.add(new CustomerViewModel(customer, orderCount));
            }
            
            updateStatus(MessageFormat.format(I18n.get("dialog.msg.customerloaded"), customers.size()));
            countLabel.setText(MessageFormat.format(I18n.get("msg.total"), customers.size()));
            
            logger.info("Loaded {} customers", customers.size());
        } catch (Exception e) {
            logger.error("Failed to load customers", e);
            showError("Error", "Failed to load customers: " + e.getMessage());
            updateStatus("Error loading customers");
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadCustomers();
            return;
        }
        
        try {
            List<Customer> customers = customerService.searchCustomers(searchText);
            customerData.clear();
            
            for (Customer customer : customers) {
                int orderCount = customerService.getCustomerOrderCount(customer.getId());
                customerData.add(new CustomerViewModel(customer, orderCount));
            }
            
            updateStatus("Found " + customers.size() + " customer(s)");
            countLabel.setText("Total: " + customers.size());
            
            logger.info("Search '{}' found {} customers", searchText, customers.size());
        } catch (Exception e) {
            logger.error("Failed to search customers", e);
            showError("Error", "Failed to search customers: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAdd() {
        logger.info("Add customer requested");
        try {
            boolean saved = showCustomerDialog(null);
            if (saved) {
                loadCustomers();
            }
        } catch (Exception e) {
            logger.error("Failed to open add customer dialog", e);
            showError("Error", "Failed to open add customer dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEdit() {
        CustomerViewModel selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a customer to edit.");
            return;
        }
        
        logger.info("Edit customer requested: {}", selected.getName());
        try {
            boolean saved = showCustomerDialog(selected.getCustomer());
            if (saved) {
                loadCustomers();
            }
        } catch (Exception e) {
            logger.error("Failed to open edit customer dialog", e);
            showError("Error", "Failed to open edit customer dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        CustomerViewModel selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18n.get("dialog.noselection"), I18n.get("dialog.deletecustomer.noselection"));
            return;
        }
        
        // Check if customer has orders
        if (selected.getOrderCount() > 0) {
            showWarning(I18n.get("dialog.deletecustomer.cannotdelete"), 
                MessageFormat.format(I18n.get("dialog.deletecustomer.hasorders"), selected.getOrderCount()));
            return;
        }
        
        // Confirm deletion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(I18n.get("dialog.title.confirmdeletion"));
        confirmation.setHeaderText(I18n.get("dialog.title.deletecustomer"));
        confirmation.setContentText(MessageFormat.format(I18n.get("dialog.deletecustomer.confirm"), selected.getName()));
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(selected.getId());
                loadCustomers();
                updateStatus(MessageFormat.format(I18n.get("dialog.deletecustomer.deleted"), selected.getName()));
                logger.info("Deleted customer: {}", selected.getName());
            } catch (Exception e) {
                logger.error("Failed to delete customer", e);
                showError(I18n.get("dialog.error"), I18n.get("dialog.deletecustomer.error") + " " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        logger.info("Refresh requested");
        searchField.clear();
        loadCustomers();
    }
    
    /**
     * Show customer edit dialog
     * @param customer Customer to edit, or null for new customer
     * @return true if saved, false if cancelled
     */
    private boolean showCustomerDialog(Customer customer) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/zakilb/servicemanager/fxml/customer-edit-dialog.fxml")
        );
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle(customer == null ? I18n.get("dialog.title.addcustomer") : I18n.get("dialog.title.editcustomer"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(customersTable.getScene().getWindow());
        dialogStage.setScene(new Scene(loader.load()));
        
        CustomerEditDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setCustomer(customer);
        
        dialogStage.showAndWait();
        
        return controller.isSaved();
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
