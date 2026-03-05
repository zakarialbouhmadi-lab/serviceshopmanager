package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.service.CustomerService;
import com.zakilb.servicemanager.service.PrintingService;
import com.zakilb.servicemanager.service.WorkOrderService;
import com.zakilb.servicemanager.ui.viewmodels.WorkOrderViewModel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Orders view
 */
public class OrdersController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private DatePicker dueDateFromPicker;
    @FXML private DatePicker dueDateToPicker;
    
    // FXML fields for translation
    @FXML private Label workOrdersTitleLabel;
    @FXML private Label filterByLabel;
    @FXML private Label statusFilterLabel;
    @FXML private Label createdFromLabel;
    @FXML private Label toLabel1;
    @FXML private Label dueFromLabel;
    @FXML private Label toLabel2;
    @FXML private Button newOrderButton;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button changeStatusButton;
    @FXML private Button statusHistoryButton;
    
    @FXML private TableView<WorkOrderViewModel> ordersTable;
    @FXML private TableColumn<WorkOrderViewModel, String> orderNumberColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> customerColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> statusColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> dueDateColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> createdColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> notesColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> totalPriceColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> amountPaidColumn;
    @FXML private TableColumn<WorkOrderViewModel, String> remainingColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    @FXML private Button printButton;

    private ObservableList<String> statusFilterDbValues;

    
    private final WorkOrderService workOrderService;
    private final CustomerService customerService;
    private final PrintingService printingService;
    private final ObservableList<WorkOrderViewModel> orderData;
    
    public OrdersController() {
        this.workOrderService = new WorkOrderService();
        this.customerService = new CustomerService();
        this.printingService = new PrintingService();
        this.orderData = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing OrdersController");
        
        // Apply translations
        applyTranslations();
        
        // Set up table columns
        setupTableColumns();
        
        // Set up selection listener
        setupSelectionListener();
        
        // Set up status filter
        setupStatusFilter();
        
        // Load orders
        loadOrders();
        
        // Set up search on Enter
        searchField.setOnAction(e -> handleSearch());
    }
    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        // Title and buttons
        workOrdersTitleLabel.setText(I18n.get("orders.title"));
        searchField.setPromptText(I18n.get("orders.search"));
        searchButton.setText(I18n.get("orders.search.button")); // Reuse text for search
        newOrderButton.setText("+ " + I18n.get("orders.new"));
        
        // Filter labels
        filterByLabel.setText(I18n.get("filter.status").split(":")[0] + ":");
        statusFilterLabel.setText(I18n.get("filter.status").split(":")[1].trim() + ":");
        createdFromLabel.setText(I18n.get("filter.created.from") + ":");
        toLabel1.setText(I18n.get("filter.created.to") + ":");
        dueFromLabel.setText(I18n.get("filter.due.from") + ":");
        toLabel2.setText(I18n.get("filter.due.to") + ":");
        
        // Filter buttons
        filterButton.setText(I18n.get("filter.apply"));
        clearFilterButton.setText(I18n.get("filter.clear"));
        refreshButton.setText(I18n.get("orders.refresh") != null ? I18n.get("orders.refresh") : "Refresh");
        
        // Table column headers
        orderNumberColumn.setText(I18n.get("orders.column.number"));
        customerColumn.setText(I18n.get("orders.column.customer"));
        statusColumn.setText(I18n.get("orders.column.status"));
        dueDateColumn.setText(I18n.get("orders.column.due"));
        createdColumn.setText(I18n.get("orders.column.date"));
        totalPriceColumn.setText(I18n.get("orders.column.total"));
        amountPaidColumn.setText(I18n.get("orders.column.paid"));
        remainingColumn.setText(I18n.get("orders.column.remaining"));
        notesColumn.setText(I18n.get("orders.column.notes"));
        
        // Action buttons
        viewDetailsButton.setText(I18n.get("orders.viewdetails"));
        changeStatusButton.setText(I18n.get("orders.changestatus"));
        statusHistoryButton.setText(I18n.get("orders.statushistory"));
        printButton.setText(I18n.get("orders.print"));
        
        // Status labels (will be updated after loading)
        statusLabel.setText("");
        countLabel.setText("");
    }
    
    private void setupTableColumns() {
        orderNumberColumn.setCellValueFactory(cellData -> cellData.getValue().orderNumberProperty());
        customerColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        createdColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        notesColumn.setCellValueFactory(cellData -> cellData.getValue().notesProperty());
        totalPriceColumn.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty());
        amountPaidColumn.setCellValueFactory(cellData -> cellData.getValue().amountPaidProperty());
        remainingColumn.setCellValueFactory(cellData -> cellData.getValue().remainingProperty());
        
        // Custom cell factory for status column with colors and click handler
        statusColumn.setCellFactory(column -> new TableCell<WorkOrderViewModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    setOnMouseClicked(null);
                } else {
                    setText(getStatusDisplay(status));
                    
                    // Apply color based on status
                    String color = switch (status) {
                        case "received" -> "-fx-background-color: #d6eaf8; -fx-text-fill: #1f618d;";
                        case "in_progress" -> "-fx-background-color: #fef5e7; -fx-text-fill: #b9770e;";
                        case "ready" -> "-fx-background-color: #d5f4e6; -fx-text-fill: #196f3d;";
                        case "picked_up" -> "-fx-background-color: #e8eaf6; -fx-text-fill: #5d6d7e;";
                        case "canceled" -> "-fx-background-color: #fadbd8; -fx-text-fill: #922b21;";
                        default -> "";
                    };
                    setStyle(color + " -fx-padding: 5; -fx-background-radius: 3; -fx-font-weight: bold; -fx-cursor: hand;");
                    

                }
            }
        });
        
        ordersTable.setItems(orderData);
        ordersTable.setEditable(true);
    }
    
    private void setupSelectionListener() {
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue != null;
                viewDetailsButton.setDisable(!hasSelection);
                changeStatusButton.setDisable(!hasSelection);
                printButton.setDisable(!hasSelection);
            }
        );
    }
    
    private void setupStatusFilter() {
        // Store database status values, but display translations
        ObservableList<String> dbStatuses = FXCollections.observableArrayList(
            "",  // All statuses (empty = no filter)
            "received",
            "in_progress",
            "ready",
            "picked_up",
            "canceled"
        );
        
        // Create display list with translations
        ObservableList<String> displayStatuses = FXCollections.observableArrayList(
            I18n.get("status.all"),
            I18n.get("status.received"),
            I18n.get("status.inprogress"),
            I18n.get("status.ready"),
            I18n.get("status.pickedup"),
            I18n.get("status.canceled")
        );
        
        statusFilter.setItems(displayStatuses);
        statusFilter.getSelectionModel().select(0);
        
        // Store reference to database statuses for lookup
        statusFilterDbValues = dbStatuses;
    }
    
    private void loadOrders() {
        try {
            List<WorkOrder> orders = workOrderService.getAllOrders();
            orderData.clear();
            
            for (WorkOrder order : orders) {
                Customer customer = customerService.getCustomerById(order.getCustomerId());
                WorkOrderViewModel viewModel = new WorkOrderViewModel(order, customer);
                
                List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
                int totalCents = 0;
                for (WorkOrderItem item : items) {
                    ServiceType serviceType = workOrderService.getServiceTypeById(item.getServiceTypeId());
                    int originalCents = serviceType.getPriceCents();
                    int finalCents = (int) (originalCents * (1 - item.getDiscountPercent() / 100.0));
                    totalCents += finalCents;
                }
                viewModel.setTotalPrice(totalCents);
                
                orderData.add(viewModel);
            }
            
            updateStatus(MessageFormat.format(I18n.get("status.ordersloaded"), orders.size()));
            countLabel.setText(MessageFormat.format(I18n.get("msg.total"), orders.size()));
            
            logger.info("Loaded {} orders", orders.size());
        } catch (Exception e) {
            logger.error("Failed to load orders", e);
            showError("Error", "Failed to load orders: " + e.getMessage());
            updateStatus("Error loading orders");
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadOrders();
            return;
        }
        
        try {
            List<WorkOrder> orders = workOrderService.searchOrders(searchText);
            orderData.clear();
            
            for (WorkOrder order : orders) {
                Customer customer = customerService.getCustomerById(order.getCustomerId());
                WorkOrderViewModel viewModel = new WorkOrderViewModel(order, customer);
                
                List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
                int totalCents = 0;
                for (WorkOrderItem item : items) {
                    ServiceType serviceType = workOrderService.getServiceTypeById(item.getServiceTypeId());
                    int originalCents = serviceType.getPriceCents();
                    int finalCents = (int) (originalCents * (1 - item.getDiscountPercent() / 100.0));
                    totalCents += finalCents;
                }
                viewModel.setTotalPrice(totalCents);
                
                orderData.add(viewModel);
            }
            
            updateStatus("Found " + orders.size() + " order(s)");
            countLabel.setText("Total: " + orders.size());
            
            logger.info("Search '{}' found {} orders", searchText, orders.size());
        } catch (Exception e) {
            logger.error("Failed to search orders", e);
            showError("Error", "Failed to search orders: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNewOrder() {
        logger.info("New order requested");
        try {
            boolean created = showNewOrderDialog();
            if (created) {
                loadOrders();
            }
        } catch (Exception e) {
            logger.error("Failed to open new order dialog", e);
            showError("Error", "Failed to open new order dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFilter() {
        try {
            String statusValue = statusFilter.getValue();
            String dbStatus = getStatusValue(statusValue);
            LocalDate from = dateFromPicker.getValue();
            LocalDate to = dateToPicker.getValue();
            LocalDate dueFrom = dueDateFromPicker.getValue();
            LocalDate dueTo = dueDateToPicker.getValue();
            
            List<WorkOrder> orders;
            
            // Apply status filter
            if (dbStatus != null && !dbStatus.isEmpty()) {
                orders = workOrderService.getOrdersByStatus(dbStatus);
            } else {
                orders = workOrderService.getAllOrders();
            }
            
            // Apply date range filter if specified
            if (from != null || to != null) {
                LocalDate fromDate = from != null ? from : LocalDate.of(2000, 1, 1);
                LocalDate toDate = to != null ? to : LocalDate.now().plusYears(10);
                orders = workOrderService.getOrdersByDateRange(fromDate, toDate);
            }
            
            // Filter by due date range if specified
            if (dueFrom != null || dueTo != null) {
                LocalDate dueFromDate = dueFrom != null ? dueFrom : LocalDate.of(2000, 1, 1);
                LocalDate dueToDate = dueTo != null ? dueTo : LocalDate.now().plusYears(10);
                orders = orders.stream()
                    .filter(o -> o.getDueDate() != null && 
                           !o.getDueDate().isBefore(dueFromDate) && 
                           !o.getDueDate().isAfter(dueToDate))
                    .toList();
            }
            
            orderData.clear();
            for (WorkOrder order : orders) {
                Customer customer = customerService.getCustomerById(order.getCustomerId());
                WorkOrderViewModel viewModel = new WorkOrderViewModel(order, customer);
                
                List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
                int totalCents = 0;
                for (WorkOrderItem item : items) {
                    ServiceType serviceType = workOrderService.getServiceTypeById(item.getServiceTypeId());
                    int originalCents = serviceType.getPriceCents();
                    int finalCents = (int) (originalCents * (1 - item.getDiscountPercent() / 100.0));
                    totalCents += finalCents;
                }
                viewModel.setTotalPrice(totalCents);
                
                orderData.add(viewModel);
            }
            
            updateStatus(MessageFormat.format(I18n.get("status.filterapplied"), orders.size()));
            countLabel.setText(MessageFormat.format(I18n.get("msg.total"), orders.size()));
            
            logger.info("Filter applied, found {} orders", orders.size());
        } catch (Exception e) {
            logger.error("Failed to apply filter", e);
            showError(I18n.get("dialog.error"), "Failed to apply filter: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFilter() {
        statusFilter.getSelectionModel().select(0);
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        dueDateFromPicker.setValue(null);
        dueDateToPicker.setValue(null);
        loadOrders();
    }
    
    @FXML
    private void handleRefresh() {
        logger.info("Refresh requested");
        loadOrders();
    }
    
    @FXML
    private void handleChangeStatusButton() {
        WorkOrderViewModel selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an order to change status.");
            return;
        }
        
        handleChangeStatus(selected);
    }
    
    @FXML
    private void handleViewDetails() {
        WorkOrderViewModel selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an order to view details.");
            return;
        }
        
        logger.info("View details requested for order: {}", selected.getOrderNumber());
        
        try {
            WorkOrder order = selected.getWorkOrder();
            List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
            Customer customer = customerService.getCustomerById(order.getCustomerId());
            
            // Create a custom dialog with better styling
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(I18n.get("dialog.title.orderdetails"));
            dialog.setHeaderText(null);
            
            // Create main content
            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");
            
            // Order header with order number
            Label orderHeader = new Label(I18n.get("dialog.orderdetails.order") + order.getOrderNumber());
            orderHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            content.getChildren().add(orderHeader);
            
            // Separator
            Separator sep1 = new Separator();
            content.getChildren().add(sep1);
            
            // Order Information Section
            VBox orderInfoBox = createInfoSection(I18n.get("dialog.orderdetails.orderinfo"),
                I18n.get("pdf.ordernumber") + " " + order.getOrderNumber(),
                I18n.get("orders.column.status") + " " + getStatusDisplay(order.getStatus()),
                I18n.get("pdf.created") + " " + order.getCreatedAt(),
                I18n.get("pdf.duedate") + " " + (order.getDueDate() != null ? order.getDueDate() : "Not set")
            );
            content.getChildren().add(orderInfoBox);
            
            // Customer Information Section
            List<String> customerInfo = new ArrayList<>();
            customerInfo.add(I18n.get("dialog.orderdetails.name") + " " + customer.getName());
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                customerInfo.add(I18n.get("dialog.orderdetails.phone") + " " + customer.getPhone());
            }
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                customerInfo.add(I18n.get("dialog.orderdetails.email") + " " + customer.getEmail());
            }
            VBox customerInfoBox = createInfoSection(I18n.get("dialog.orderdetails.customerinfo"), 
                customerInfo.toArray(new String[0]));
            content.getChildren().add(customerInfoBox);
            
            // Service Items Section
            VBox itemsBox = new VBox(10);
            itemsBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
            
            Label itemsTitle = new Label(I18n.get("dialog.orderdetails.serviceitems") + " (" + items.size() + ")");
            itemsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            itemsBox.getChildren().add(itemsTitle);
            
            double totalOriginalPrice = 0;
            double totalFinalPrice = 0;
            for (int i = 0; i < items.size(); i++) {
                WorkOrderItem item = items.get(i);
                ServiceType serviceType =
                    workOrderService.getServiceTypeById(item.getServiceTypeId());
                
                VBox itemBox = new VBox(5);
                itemBox.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 3; -fx-border-color: #dee2e6; -fx-border-radius: 3;");
                
                // Item title
                StringBuilder titleText = new StringBuilder(I18n.get("dialog.orderdetails.item") + " " + (i + 1));
                Label itemTitle = new Label(titleText.toString());
                itemTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
                
                Label serviceLabel = new Label(I18n.get("dialog.orderdetails.service") + " " + serviceType.getName() + " (" + serviceType.getCode() + ")");
                
                itemBox.getChildren().addAll(itemTitle, serviceLabel);
                
                // Item name if present
                if (item.getItemName() != null && !item.getItemName().isEmpty()) {
                    Label itemNameLabel = new Label(I18n.get("dialog.orderdetails.itemname") + " " + item.getItemName());
                    itemNameLabel.setStyle("-fx-text-fill: #5d6d7e;");
                    itemBox.getChildren().add(itemNameLabel);
                }
                
                // Price handling with discount
                double originalPrice = serviceType.getPriceInPLN();
                double discountPercent = item.getDiscountPercent();
                double finalPrice = originalPrice * (1 - discountPercent / 100.0);
                
                VBox priceBox = new VBox(2);
                if (discountPercent > 0) {
                    Label originalPriceLabel = new Label(I18n.get("dialog.orderdetails.originalprice") + " " + String.format("%.2f PLN", originalPrice));
                    originalPriceLabel.setStyle("-fx-text-fill: #6c757d; -fx-strikethrough: true;");
                    
                    Label discountLabel = new Label(I18n.get("dialog.orderdetails.discount") + " " + String.format("%.1f%%", discountPercent));
                    discountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    
                    Label finalPriceLabel = new Label(I18n.get("dialog.orderdetails.finalprice") + " " + String.format("%.2f PLN", finalPrice));
                    finalPriceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745; -fx-font-size: 13px;");
                    
                    priceBox.getChildren().addAll(originalPriceLabel, discountLabel, finalPriceLabel);
                } else {
                    Label priceLabel = new Label(I18n.get("dialog.orderdetails.price") + " " + String.format("%.2f PLN", originalPrice));
                    priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
                    priceBox.getChildren().add(priceLabel);
                }
                
                Label barcodeLabel = new Label(I18n.get("dialog.orderdetails.barcode") + " " + item.getBarcode());
                barcodeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
                
                itemBox.getChildren().addAll(priceBox, barcodeLabel);
                
                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    Label notesLabel = new Label(I18n.get("dialog.orderdetails.notes") + " " + item.getNotes());
                    notesLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #6c757d;");
                    itemBox.getChildren().add(notesLabel);
                }
                
                itemsBox.getChildren().add(itemBox);
                totalOriginalPrice += originalPrice;
                totalFinalPrice += finalPrice;
            }
            
            // Total with savings
            double totalSavings = totalOriginalPrice - totalFinalPrice;
            if (totalSavings > 0) {
                Label savingsLabel = new Label(I18n.get("dialog.orderdetails.discounttotal") + " " + String.format("%.2f PLN", totalSavings));
                savingsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 5 0 0 0;");
                itemsBox.getChildren().add(savingsLabel);
            }
            
            Label totalLabel = new Label(I18n.get("dialog.orderdetails.total") + " " + String.format("%.2f PLN", totalFinalPrice));
            totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745; -fx-padding: 10 0 0 0;");
            itemsBox.getChildren().add(totalLabel);
            
            double paidAmount = order.getAmountPaid() != null ? order.getAmountPaid() / 100.0 : 0.0;
            double remainingBalance = totalFinalPrice - paidAmount;
            
            Label paidLabel = new Label(I18n.get("dialog.orderdetails.paid") + " " + String.format("%.2f PLN", paidAmount));
            paidLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #17a2b8; -fx-padding: 5 0 0 0;");
            itemsBox.getChildren().add(paidLabel);
            
            if (remainingBalance > 0.01) {
                Label balanceLabel = new Label(I18n.get("dialog.orderdetails.remaining") + " " + String.format("%.2f PLN", remainingBalance));
                balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545; -fx-padding: 5 0 0 0;");
                itemsBox.getChildren().add(balanceLabel);
            } else if (paidAmount > 0.01) {
                Label paidFullLabel = new Label(I18n.get("dialog.orderdetails.paidinfull"));
                paidFullLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745; -fx-padding: 5 0 0 0;");
                itemsBox.getChildren().add(paidFullLabel);
            }
            
            content.getChildren().add(itemsBox);
            
            // Order Notes if present
            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                VBox notesBox = new VBox(5);
                notesBox.setStyle("-fx-padding: 15; -fx-background-color: #fff3cd; -fx-background-radius: 5; -fx-border-color: #ffc107; -fx-border-radius: 5;");
                
                Label notesTitle = new Label(I18n.get("dialog.orderdetails.ordernotes"));
                notesTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #856404;");
                
                Label notesContent = new Label(order.getNotes());
                notesContent.setWrapText(true);
                notesContent.setStyle("-fx-text-fill: #856404;");
                
                notesBox.getChildren().addAll(notesTitle, notesContent);
                content.getChildren().add(notesBox);
            }
            
            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportWidth(700);
            scrollPane.setPrefViewportHeight(600);
            scrollPane.setStyle("-fx-background-color: white;");
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(750, 650);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Failed to load order details", e);
            showError("Error", "Failed to load order details: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create info section
     */
    private VBox createInfoSection(String title, String... items) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        box.getChildren().add(titleLabel);
        
        for (String item : items) {
            Label itemLabel = new Label(item);
            itemLabel.setStyle("-fx-text-fill: #495057;");
            box.getChildren().add(itemLabel);
        }
        
        return box;
    }
    
    @FXML
    private void handlePrint() {
        WorkOrderViewModel selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an order to print.");
            return;
        }
        
        try {
            WorkOrder order = selected.getWorkOrder();
            List<WorkOrderItem> items = workOrderService.getOrderItems(order.getId());
            Customer customer = customerService.getCustomerById(order.getCustomerId());

            // Show options dialog with separate buttons for stickers and PDF
            Alert optionsDialog = new Alert(Alert.AlertType.CONFIRMATION);
            optionsDialog.setTitle(I18n.get("dialog.print.title"));
            optionsDialog.setHeaderText(I18n.get("dialog.print.header") + ": " + order.getOrderNumber());

            // Build info text showing configured printers
            StringBuilder infoText = new StringBuilder();
            infoText.append(I18n.get("dialog.print.selectoption")).append("\n\n");

            javax.print.PrintService labelPrinter = printingService.getLabelPrinter();
            javax.print.PrintService a4Printer = printingService.getA4Printer();

            String noPrinterText = I18n.get("dialog.print.notconfigured") != null ?
                I18n.get("dialog.print.notconfigured") : "(not configured)";

            infoText.append("🏷 ").append(I18n.get("settings.printing.labelprinter")).append(" ");
            infoText.append(labelPrinter != null ? labelPrinter.getName() : noPrinterText).append("\n");

            infoText.append("📄 ").append(I18n.get("settings.printing.a4printer")).append(" ");
            infoText.append(a4Printer != null ? a4Printer.getName() : noPrinterText);

            optionsDialog.setContentText(infoText.toString());

            ButtonType printStickersButton = new ButtonType(I18n.get("dialog.print.printstickers"));
            ButtonType saveStickersButton = new ButtonType(I18n.get("dialog.print.savestickers"));
            ButtonType printPdfButton = new ButtonType(I18n.get("dialog.print.printpdf"));
            ButtonType savePdfButton = new ButtonType(I18n.get("dialog.savepdf.button"));
            ButtonType cancelButton = new ButtonType(I18n.get("dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            optionsDialog.getButtonTypes().setAll(printStickersButton, saveStickersButton, printPdfButton, savePdfButton, cancelButton);

            optionsDialog.showAndWait().ifPresent(response -> {
                try {
                    if (response == printStickersButton) {
                        // Print stickers using configured label printer
                        javax.print.PrintService configuredLabelPrinter = printingService.getLabelPrinter();
                        if (configuredLabelPrinter == null) {
                            showWarning(I18n.get("dialog.print.noprinter"),
                                I18n.get("settings.printing.labelprinter") + " " + I18n.get("dialog.print.noprintermsg"));
                            return;
                        }
                        printingService.printStickers(items, order);
                        showInfo(I18n.get("dialog.print.printstickers"),
                            I18n.get("dialog.print.sent") + "\n" + configuredLabelPrinter.getName());
                        logger.info("Printed stickers for order: {} on {}", order.getOrderNumber(), configuredLabelPrinter.getName());

                    } else if (response == saveStickersButton) {
                        // Save stickers to chosen location
                        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
                        dirChooser.setTitle(I18n.get("dialog.print.choosefolder"));
                        dirChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));

                        java.io.File selectedDir = dirChooser.showDialog(ordersTable.getScene().getWindow());
                        if (selectedDir != null) {
                            List<String> savedPaths = printingService.saveStickersToDirectory(items, order, customer, selectedDir.getAbsolutePath());
                            showInfo(I18n.get("dialog.print.stickerssaved"),
                                    savedPaths.size() + " " + I18n.get("dialog.print.stickerssavedto") + "\n" + selectedDir.getAbsolutePath() +
                                    "\n\n" + I18n.get("dialog.print.files") + "\n" + String.join("\n", savedPaths.stream().map(p -> "- " + new java.io.File(p).getName()).toList()));
                            logger.info("Saved {} stickers for order: {}", savedPaths.size(), order.getOrderNumber());
                        }

                    } else if (response == printPdfButton) {
                        // Print A4 document using configured A4 printer
                        javax.print.PrintService configuredA4Printer = printingService.getA4Printer();
                        if (configuredA4Printer == null) {
                            showWarning(I18n.get("msg.noprinter"),
                                I18n.get("settings.printing.a4printer") + " " + I18n.get("dialog.savepdf.usealt"));
                            return;
                        }
                        printingService.printA4Document(order, items, customer);
                        showInfo(I18n.get("dialog.print.printpdf"),
                            I18n.get("dialog.print.pdfsent") + "\n" + configuredA4Printer.getName());
                        logger.info("Printed PDF for order: {} on {}", order.getOrderNumber(), configuredA4Printer.getName());
                        
                    } else if (response == savePdfButton) {
                        // Save PDF to chosen location
                        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                        fileChooser.setTitle(I18n.get("dialog.title.savepdf"));
                        fileChooser.setInitialFileName("order_" + order.getOrderNumber() + ".pdf");
                        fileChooser.getExtensionFilters().add(
                            new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                        );
                        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
                        
                        java.io.File selectedFile = fileChooser.showSaveDialog(ordersTable.getScene().getWindow());
                        if (selectedFile != null) {
                            String pdfPath = printingService.savePdfToFile(order, items, customer, selectedFile.getAbsolutePath());
                            showInfo(I18n.get("dialog.print.stickerssaved"), I18n.get("dialog.print.pdfsavedto") + "\n" + pdfPath);
                            logger.info("Generated PDF for order: {}", order.getOrderNumber());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to print/save documents", e);
                    showError(I18n.get("dialog.error"), I18n.get("dialog.print.error") + " " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to prepare documents", e);
            showError(I18n.get("dialog.error"), I18n.get("dialog.print.prepareerror") + " " + e.getMessage());
        }
    }

    

    
    private boolean showNewOrderDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/zakilb/servicemanager/fxml/new-order-dialog.fxml")
        );
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle(I18n.get("dialog.title.createneworder"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(ordersTable.getScene().getWindow());
        dialogStage.setScene(new Scene(loader.load()));
        
        NewOrderDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        
        dialogStage.showAndWait();
        
        return controller.isCreated();
    }
    
    private String getStatusDisplay(String dbStatus) {
        switch (dbStatus) {
            case "received": return I18n.get("status.received");
            case "in_progress": return I18n.get("status.inprogress");
            case "ready": return I18n.get("status.ready");
            case "picked_up": return I18n.get("status.pickedup");
            case "canceled": return I18n.get("status.canceled");
            default: return dbStatus;
        }
    }
    
    private String getStatusValue(String displayStatus) {
        // Get the index of the selected display status
        int index = statusFilter.getItems().indexOf(displayStatus);
        if (index >= 0 && index < statusFilterDbValues.size()) {
            return statusFilterDbValues.get(index);
        }
        return "";
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
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("dialog.success"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleViewStatusHistory() {
        WorkOrderViewModel selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18n.get("dialog.noselection"), I18n.get("dialog.statushistory.selectorder"));
            return;
        }
        
        try {
            List<WorkOrderService.StatusHistoryEntry> history = workOrderService.getStatusHistoryDetailed(selected.getId());
            
            // Create custom dialog for better display
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(I18n.get("dialog.title.statushistory"));
            dialog.setHeaderText(null);
            
            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");
            
            // Header with order info
            Label headerLabel = new Label(I18n.get("dialog.orderdetails.order") + selected.getOrderNumber());
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            content.getChildren().add(headerLabel);
            
            Label subHeaderLabel = new Label(I18n.get("dialog.orderdetails.customer") + " " + selected.getCustomerName());
            subHeaderLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            content.getChildren().add(subHeaderLabel);
            
            Separator separator = new Separator();
            content.getChildren().add(separator);
            
            if (history.isEmpty()) {
                Label noHistoryLabel = new Label(I18n.get("dialog.statushistory.nohistory"));
                noHistoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
                content.getChildren().add(noHistoryLabel);
            } else {
                // Timeline-style display
                VBox timelineBox = new VBox(12);
                
                for (WorkOrderService.StatusHistoryEntry entry : history) {
                    VBox entryBox = new VBox(8);
                    entryBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;");
                    
                    // Date/time
                    Label dateLabel = new Label(entry.getChangedAt());
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");
                    entryBox.getChildren().add(dateLabel);
                    
                    // Status change with colored badges
                    javafx.scene.layout.HBox statusChangeBox = new javafx.scene.layout.HBox(10);
                    statusChangeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label oldStatusLabel = createStatusBadge(entry.getOldStatus());
                    Label arrowLabel = new Label("→");
                    arrowLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");
                    Label newStatusLabel = createStatusBadge(entry.getNewStatus());
                    
                    statusChangeBox.getChildren().addAll(oldStatusLabel, arrowLabel, newStatusLabel);
                    entryBox.getChildren().add(statusChangeBox);
                    
                    // Notes if present
                    if (entry.hasNotes()) {
                        VBox notesBox = new VBox(5);
                        notesBox.setStyle("-fx-padding: 10; -fx-background-color: #fff3cd; -fx-background-radius: 5; -fx-border-color: #ffc107; -fx-border-width: 1; -fx-border-radius: 5;");
                        
                        Label notesTitle = new Label(I18n.get("dialog.statushistory.note"));
                        notesTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #856404;");
                        
                        Label notesContent = new Label(entry.getNotes());
                        notesContent.setWrapText(true);
                        notesContent.setStyle("-fx-font-size: 12px; -fx-text-fill: #856404;");
                        
                        notesBox.getChildren().addAll(notesTitle, notesContent);
                        entryBox.getChildren().add(notesBox);
                    }
                    
                    timelineBox.getChildren().add(entryBox);
                }
                
                ScrollPane scrollPane = new ScrollPane(timelineBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(400);
                scrollPane.setStyle("-fx-background-color: transparent;");
                
                content.getChildren().add(scrollPane);
            }
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(600, 500);
            
            dialog.showAndWait();
        } catch (Exception e) {
            logger.error("Failed to load status history", e);
            showError(I18n.get("dialog.error"), I18n.get("dialog.statushistory.error") + ": " + e.getMessage());
        }
    }
    
    /**
     * Create a colored status badge label
     */
    private Label createStatusBadge(String status) {
        Label badge = new Label(getStatusDisplay(status));
        
        String color;
        switch (status) {
            case "received":
                color = "-fx-background-color: #d6eaf8; -fx-text-fill: #1f618d;";
                break;
            case "in_progress":
                color = "-fx-background-color: #fef5e7; -fx-text-fill: #b9770e;";
                break;
            case "ready":
                color = "-fx-background-color: #d5f4e6; -fx-text-fill: #196f3d;";
                break;
            case "picked_up":
                color = "-fx-background-color: #e8eaf6; -fx-text-fill: #5d6d7e;";
                break;
            case "canceled":
                color = "-fx-background-color: #fadbd8; -fx-text-fill: #922b21;";
                break;
            default:
                color = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;";
        }
        
        badge.setStyle(color + " -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        return badge;
    }
    
    /**
     * Handle status change by opening the change status dialog
     */
    private void handleChangeStatus(WorkOrderViewModel orderViewModel) {
        try {
            WorkOrder order = orderViewModel.getWorkOrder();
            Customer customer = customerService.getCustomerById(order.getCustomerId());
            
            // Load the change status dialog
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/change-status-dialog.fxml")
            );
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Order Status");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(ordersTable.getScene().getWindow());
            dialogStage.setScene(new Scene(loader.load()));
            
            ChangeStatusDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setWorkOrder(order, customer.getName());
            
            dialogStage.showAndWait();
            
            // If status was updated, refresh the order
            if (controller.isUpdated()) {
                String newStatus = controller.getNewStatus();
                String notes = controller.getNotes();
                workOrderService.updateStatus(order.getId(), newStatus, notes);
                orderViewModel.setStatus(newStatus);
                ordersTable.refresh();
                logger.info("Updated order {} status to {} with notes: {}", order.getOrderNumber(), newStatus, notes != null ? "yes" : "no");
                showSuccess("Status updated successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to change status", e);
            showError("Error", "Failed to change status: " + e.getMessage());
        }
    }
}
