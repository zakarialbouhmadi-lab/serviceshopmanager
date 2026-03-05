package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.service.CustomerService;
import com.zakilb.servicemanager.service.PrintingService;
import com.zakilb.servicemanager.service.ServiceTypeService;
import com.zakilb.servicemanager.service.WorkOrderService;
import com.zakilb.servicemanager.ui.viewmodels.WorkOrderItemViewModel;
import com.zakilb.servicemanager.util.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the New Order Dialog
 */
public class NewOrderDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(NewOrderDialogController.class);
    
    @FXML private Label headerLabel;
    @FXML private Label customerLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label notesLabel;
    @FXML private Label amountPaidLabel;
    @FXML private Label amountPaidHelpLabel;
    @FXML private Label itemsLabel;
    @FXML private Label totalLabelTitle;
    @FXML private Label requiredFieldsLabel;
    @FXML private Button newCustomerButton;
    @FXML private Button addItemButton;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextArea notesArea;
    @FXML private TextField amountPaidField;

    @FXML private TableView<WorkOrderItemViewModel> itemsTable;
    @FXML private TableColumn<WorkOrderItemViewModel, Integer> itemNumberColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, String> serviceTypeColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, String> itemNameColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, String> priceColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, Double> discountColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, String> finalPriceColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, String> itemNotesColumn;
    @FXML private TableColumn<WorkOrderItemViewModel, Void> actionsColumn;
    
    @FXML private Label totalLabel;
    @FXML private Label savingsLabel;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private boolean created = false;
    private boolean isUpdatingCustomerComboBox = false;
    
    private final CustomerService customerService;
    private final ServiceTypeService serviceTypeService;
    private final WorkOrderService workOrderService;
    private final PrintingService printingService;
    
    private final ObservableList<WorkOrderItemViewModel> items;
    
    public NewOrderDialogController() {
        this.customerService = new CustomerService();
        this.serviceTypeService = new ServiceTypeService();
        this.workOrderService = new WorkOrderService();
        this.printingService = new PrintingService();
        this.items = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing NewOrderDialogController");
        
        // Apply translations first
        applyTranslations();
        
        // Setup customer autocomplete
        setupCustomerComboBox();
        
        // Setup items table
        setupItemsTable();
        
        // Set default due date (7 days from now)
        dueDatePicker.setValue(LocalDate.now().plusDays(7));
        
        // Update total when items change
        items.addListener((javafx.collections.ListChangeListener<WorkOrderItemViewModel>) c -> updateTotal());
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        if (headerLabel != null) headerLabel.setText(I18n.get("dialog.neworder.header"));
        if (customerLabel != null) customerLabel.setText(I18n.get("dialog.neworder.customer"));
        if (dueDateLabel != null) dueDateLabel.setText(I18n.get("dialog.neworder.duedate"));
        if (notesLabel != null) notesLabel.setText(I18n.get("dialog.neworder.notes"));
        if (amountPaidLabel != null) amountPaidLabel.setText(I18n.get("dialog.neworder.amountpaid"));
        if (amountPaidHelpLabel != null) amountPaidHelpLabel.setText(I18n.get("dialog.neworder.amountpaidhelp"));
        if (itemsLabel != null) itemsLabel.setText(I18n.get("dialog.neworder.items"));
        if (totalLabelTitle != null) totalLabelTitle.setText(I18n.get("dialog.neworder.total"));
        if (requiredFieldsLabel != null) requiredFieldsLabel.setText(I18n.get("dialog.neworder.requiredfields"));
        
        if (newCustomerButton != null) newCustomerButton.setText(I18n.get("dialog.neworder.newcustomer"));
        if (addItemButton != null) addItemButton.setText(I18n.get("dialog.neworder.additem"));
        if (createButton != null) createButton.setText(I18n.get("dialog.neworder.create"));
        if (cancelButton != null) cancelButton.setText(I18n.get("dialog.cancel"));
        
        // Table columns
        if (itemNumberColumn != null) itemNumberColumn.setText(I18n.get("dialog.neworder.itemcolumns.number"));
        if (serviceTypeColumn != null) serviceTypeColumn.setText(I18n.get("dialog.neworder.itemcolumns.servicetype"));
        if (itemNameColumn != null) itemNameColumn.setText(I18n.get("dialog.neworder.itemcolumns.itemname"));
        if (priceColumn != null) priceColumn.setText(I18n.get("dialog.neworder.itemcolumns.price"));
        if (discountColumn != null) discountColumn.setText(I18n.get("dialog.neworder.itemcolumns.discount"));
        if (finalPriceColumn != null) finalPriceColumn.setText(I18n.get("dialog.neworder.itemcolumns.finalprice"));
        if (itemNotesColumn != null) itemNotesColumn.setText(I18n.get("dialog.neworder.itemcolumns.notes"));
        if (actionsColumn != null) actionsColumn.setText(I18n.get("dialog.neworder.itemcolumns.actions"));
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isCreated() {
        return created;
    }
    
    private void setupCustomerComboBox() {
        try {
            List<Customer> allCustomers = customerService.getAllCustomers();
            ObservableList<Customer> filteredCustomers = FXCollections.observableArrayList(allCustomers);
            customerComboBox.setItems(filteredCustomers);
            
            // Custom string converter for display
            customerComboBox.setConverter(new StringConverter<Customer>() {
                @Override
                public String toString(Customer customer) {
                    return customer != null ? customer.getName() : "";
                }
                
                @Override
                public Customer fromString(String string) {
                    // Try to find existing customer by name
                    return customerComboBox.getItems().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
                }
            });
            
            // Enable filtering
            customerComboBox.setEditable(true);
            
            // Handle selection from dropdown first
            customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !isUpdatingCustomerComboBox) {
                    isUpdatingCustomerComboBox = true;
                    customerComboBox.getEditor().setText(newVal.getName());
                    customerComboBox.hide();
                    isUpdatingCustomerComboBox = false;
                }
            });
            
            // Add filter listener to the editor
            customerComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                if (isUpdatingCustomerComboBox) {
                    return; // Skip filtering when updating programmatically
                }
                
                final Customer selectedCustomer = customerComboBox.getSelectionModel().getSelectedItem();
                
                if (newValue == null || newValue.isEmpty()) {
                    // Show all customers when empty
                    filteredCustomers.setAll(allCustomers);
                    customerComboBox.hide();
                } else {
                    // Don't filter if the text matches the selected customer
                    if (selectedCustomer != null && newValue.equals(selectedCustomer.getName())) {
                        return;
                    }
                    
                    // Filter customers by name
                    String filterText = newValue.toLowerCase();
                    List<Customer> filtered = allCustomers.stream()
                        .filter(c -> c.getName().toLowerCase().contains(filterText))
                        .toList();
                    
                    filteredCustomers.setAll(filtered);
                    
                    // Show dropdown if there are matches
                    if (!filtered.isEmpty() && !customerComboBox.isShowing()) {
                        customerComboBox.show();
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to load customers", e);
            showError("Failed to load customers: " + e.getMessage());
        }
    }
    
    private void setupItemsTable() {
        itemNumberColumn.setCellValueFactory(cellData -> cellData.getValue().itemNumberProperty().asObject());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeNameProperty());
        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        discountColumn.setCellValueFactory(cellData -> cellData.getValue().discountPercentProperty().asObject());
        finalPriceColumn.setCellValueFactory(cellData -> cellData.getValue().finalPriceProperty());
        itemNotesColumn.setCellValueFactory(cellData -> cellData.getValue().notesProperty());
        
        // Format discount column
        discountColumn.setCellFactory(col -> new TableCell<WorkOrderItemViewModel, Double>() {
            @Override
            protected void updateItem(Double discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty || discount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", discount));
                }
            }
        });
        
        // Add delete button in actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button(I18n.get("dialog.additem.delete"));
            
            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    WorkOrderItemViewModel item = getTableView().getItems().get(getIndex());
                    items.remove(item);
                    // Renumber items
                    renumberItems();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        itemsTable.setItems(items);
    }
    
    @FXML
    private void handleNewCustomer() {
        logger.info("New customer requested from order dialog");
        try {
            boolean saved = showCustomerDialog();
            if (saved) {
                // Reload customers
                setupCustomerComboBox();
            }
        } catch (Exception e) {
            logger.error("Failed to open customer dialog", e);
            showError("Failed to open customer dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddItem() {
        logger.info("Add item requested");
        try {
            WorkOrderItemViewModel newItem = showAddItemDialog();
            if (newItem != null) {
                items.add(newItem);
                renumberItems();
            }
        } catch (Exception e) {
            logger.error("Failed to add item", e);
            showError("Failed to add item: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreate() {
        if (validateInput()) {
            try {
                Customer customer = customerComboBox.getValue();
                LocalDate dueDate = dueDatePicker.getValue();
                String notes = notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim();
                
                // Parse paid amount
                Integer amountPaidCents = 0;
                if (amountPaidField != null && amountPaidField.getText() != null && !amountPaidField.getText().trim().isEmpty()) {
                    try {
                        double amountPaid = Double.parseDouble(amountPaidField.getText().trim());
                        amountPaidCents = (int) (amountPaid * 100);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid amount paid format: {}", amountPaidField.getText());
                        showError(I18n.get("dialog.neworder.error.invalidamount"));
                        return;
                    }
                }
                
                // Create lists for order items
                List<Integer> serviceTypesIds = new ArrayList<>();
                List<String> itemNotes = new ArrayList<>();
                List<Double> discounts = new ArrayList<>();
                List<String> itemNames = new ArrayList<>();
                
                for (WorkOrderItemViewModel item : items) {
                    serviceTypesIds.add(item.getServiceType().getId());
                    itemNotes.add(item.getNotes());
                    discounts.add(item.getDiscountPercent());
                    itemNames.add(item.getItemName());
                }
                
                // Create the order
                WorkOrder order = workOrderService.createOrder(customer, serviceTypesIds, itemNotes, 
                                                               itemNames, dueDate, notes);
                
                // Update paid amount if specified
                if (amountPaidCents > 0) {
                    order.setAmountPaid(amountPaidCents);
                    workOrderService.updateOrder(order);
                    logger.info("Updated order {} with paid amount: {} cents", order.getOrderNumber(), amountPaidCents);
                }
                
                // Update discounts on created items
                List<WorkOrderItem> createdItems = workOrderService.getOrderItems(order.getId());
                for (int i = 0; i < createdItems.size() && i < discounts.size(); i++) {
                    WorkOrderItem item = createdItems.get(i);
                    item.setDiscountPercent(discounts.get(i));
                    workOrderService.updateItemDiscount(item.getId(), discounts.get(i));
                }
                
                logger.info("Created order: {}", order.getOrderNumber());
                
                showInfo(I18n.get("dialog.success"), I18n.get("dialog.neworder.ordercreated") + ": " + order.getOrderNumber());
                created = true;
                dialogStage.close();
                
            } catch (Exception e) {
                logger.error("Failed to create order", e);
                showError("Failed to create order: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        logger.info("Order creation cancelled");
        created = false;
        dialogStage.close();
    }
    
    private boolean validateInput() {
        clearError();
        
        // Validate customer
        if (customerComboBox.getValue() == null) {
            showError(I18n.get("dialog.neworder.error.selectcustomer"));
            customerComboBox.requestFocus();
            return false;
        }
        
        // Validate due date
        if (dueDatePicker.getValue() == null) {
            showError(I18n.get("dialog.neworder.error.selectduedate"));
            dueDatePicker.requestFocus();
            return false;
        }
        
        // Validate items
        if (items.isEmpty()) {
            showError(I18n.get("dialog.neworder.error.addatleastoneitem"));
            return false;
        }
        
        return true;
    }
    
    private boolean showCustomerDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/zakilb/servicemanager/fxml/customer-edit-dialog.fxml")
        );
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle(I18n.get("dialog.title.addcustomer"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(this.dialogStage);
        dialogStage.setScene(new Scene(loader.load()));
        
        CustomerEditDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setCustomer(null);
        
        dialogStage.showAndWait();
        
        return controller.isSaved();
    }
    
    private WorkOrderItemViewModel showAddItemDialog(){
        // Create a simple dialog to select service type and add notes
        Dialog<WorkOrderItemViewModel> dialog = new Dialog<>();
        dialog.setTitle(I18n.get("dialog.additem.title"));
        dialog.setHeaderText(I18n.get("dialog.additem.header"));
        
        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));
        
        // Service Type
        Label serviceLabel = new Label(I18n.get("dialog.additem.servicetype"));
        ComboBox<ServiceType> serviceComboBox = new ComboBox<>();
        try {
            List<ServiceType> serviceTypes = serviceTypeService.getAllServiceTypes();
            serviceComboBox.setItems(FXCollections.observableArrayList(serviceTypes));
            serviceComboBox.setConverter(new StringConverter<ServiceType>() {
                @Override
                public String toString(ServiceType st) {
                    return st != null ? st.getName() + " (" + String.format("%.2f PLN", st.getPriceCents() / 100.0) + ")" : "";
                }
                
                @Override
                public ServiceType fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load service types", e);
        }
        
        // Item Name (description of serviced item)
        Label itemNameLabel = new Label(I18n.get("dialog.additem.itemname"));
        TextField itemNameField = new TextField();
        itemNameField.setPromptText(I18n.get("dialog.additem.itemnameprompt"));
        
        // Discount
        Label discountLabel = new Label(I18n.get("dialog.additem.discount"));
        Spinner<Double> discountSpinner = new Spinner<>(0.0, 100.0, 0.0, 1.0);
        discountSpinner.setEditable(true);
        discountSpinner.setPrefWidth(150);
        
        // Notes
        Label notesLabel = new Label(I18n.get("dialog.additem.notes"));
        TextArea itemNotesArea = new TextArea();
        itemNotesArea.setPromptText(I18n.get("dialog.additem.notesprompt"));
        itemNotesArea.setPrefRowCount(3);
        itemNotesArea.setWrapText(true);
        
        content.getChildren().addAll(
            serviceLabel, serviceComboBox, 
            itemNameLabel, itemNameField,
            discountLabel, discountSpinner, 
            notesLabel, itemNotesArea
        );
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType addButtonType = new ButtonType(I18n.get("dialog.additem.add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                ServiceType selected = serviceComboBox.getValue();
                if (selected != null) {
                    int itemNum = items.size() + 1;
                    String notes = itemNotesArea.getText().trim().isEmpty() ? null : itemNotesArea.getText().trim();
                    double discount = discountSpinner.getValue();
                    String itemName = itemNameField.getText().trim().isEmpty() ? null : itemNameField.getText().trim();
                    return new WorkOrderItemViewModel(itemNum, selected, notes, discount, itemName);
                }
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    private void renumberItems() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).itemNumberProperty().set(i + 1);
        }
    }
    
    private void updateTotal() {
        int totalOriginalCents = 0;
        int totalFinalCents = 0;
        
        for (WorkOrderItemViewModel item : items) {
            totalOriginalCents += item.getPriceCents();
            totalFinalCents += item.getFinalPriceCents();
        }
        
        double totalFinal = totalFinalCents / 100.0;
        totalLabel.setText(String.format("%.2f PLN", totalFinal));
        
        // Show savings if there are any discounts
        int savingsCents = totalOriginalCents - totalFinalCents;
        if (savingsCents > 0) {
            double savings = savingsCents / 100.0;
            savingsLabel.setText(String.format("Discount: %.2f PLN", savings));
            savingsLabel.setVisible(true);
            savingsLabel.setManaged(true);
        } else {
            savingsLabel.setVisible(false);
            savingsLabel.setManaged(false);
        }
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
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
