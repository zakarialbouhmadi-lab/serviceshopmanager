package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.service.ServiceTypeService;
import com.zakilb.servicemanager.ui.viewmodels.ServiceTypeViewModel;
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
 * Controller for the Service Types view
 */
public class ServiceTypesController {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceTypesController.class);

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    @FXML private TableView<ServiceTypeViewModel> serviceTypesTable;
    @FXML private TableColumn<ServiceTypeViewModel, Integer> idColumn;
    @FXML private TableColumn<ServiceTypeViewModel, String> codeColumn;
    @FXML private TableColumn<ServiceTypeViewModel, String> nameColumn;
    @FXML private TableColumn<ServiceTypeViewModel, String> priceColumn;
    @FXML private TableColumn<ServiceTypeViewModel, String> descriptionColumn;
    @FXML private TableColumn<ServiceTypeViewModel, String> createdColumn;
    
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    
    private final ServiceTypeService serviceTypeService;
    private final ObservableList<ServiceTypeViewModel> serviceTypeData;
    
    public ServiceTypesController() {
        this.serviceTypeService = new ServiceTypeService();
        this.serviceTypeData = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing ServiceTypesController");
        
        // Apply translations first
        applyTranslations();
        
        // Set up table columns
        setupTableColumns();
        
        // Set up selection listener
        setupSelectionListener();
        
        // Load service types
        loadServiceTypes();

        // Set up search on Enter
        searchField.setOnAction(e -> handleSearch());
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        titleLabel.setText(I18n.get("servicetypes.title"));
        searchField.setPromptText(I18n.get("servicetypes.search"));
        searchButton.setText(I18n.get("servicetypes.search.button")); // Reuse text for search
        addButton.setText("+ " + I18n.get("servicetypes.new"));
        editButton.setText(I18n.get("servicetypes.edit"));
        deleteButton.setText(I18n.get("servicetypes.delete")); // Reuse
        refreshButton.setText(I18n.get("servicetypes.refresh"));
        
        // Table headers
        idColumn.setText("ID");
        codeColumn.setText(I18n.get("servicetypes.column.code"));
        nameColumn.setText(I18n.get("servicetypes.column.name")); // Reuse
        priceColumn.setText(I18n.get("servicetypes.column.price"));
        descriptionColumn.setText(I18n.get("servicetypes.column.description"));
        createdColumn.setText(I18n.get("orders.column.date")); // Reuse
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        codeColumn.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        createdColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        
        serviceTypesTable.setItems(serviceTypeData);
    }
    
    private void setupSelectionListener() {
        serviceTypesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue != null;
                editButton.setDisable(!hasSelection);
                deleteButton.setDisable(!hasSelection);
            }
        );
    }
    
    private void loadServiceTypes() {
        try {
            List<ServiceType> serviceTypes = serviceTypeService.getAllServiceTypes();
            serviceTypeData.clear();
            
            for (ServiceType serviceType : serviceTypes) {
                serviceTypeData.add(new ServiceTypeViewModel(serviceType));
            }
            
            updateStatus(MessageFormat.format(I18n.get("dialog.msg.servicetypesloaded"), serviceTypes.size()));
            countLabel.setText(MessageFormat.format(I18n.get("msg.total"), serviceTypes.size()));
            
            logger.info("Loaded {} service types", serviceTypes.size());
        } catch (Exception e) {
            logger.error("Failed to load service types", e);
            showError(I18n.get("dialog.error"), "Failed to load service types: " + e.getMessage());
            updateStatus("Error loading service types");
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadServiceTypes();
            return;
        }
        
        try {
            List<ServiceType> serviceTypes = serviceTypeService.searchServiceTypes(searchText);
            serviceTypeData.clear();
            
            for (ServiceType serviceType : serviceTypes) {
                serviceTypeData.add(new ServiceTypeViewModel(serviceType));
            }
            
            updateStatus("Found " + serviceTypes.size() + " service type(s)");
            countLabel.setText("Total: " + serviceTypes.size());
            
            logger.info("Search '{}' found {} service types", searchText, serviceTypes.size());
        } catch (Exception e) {
            logger.error("Failed to search service types", e);
            showError(I18n.get("dialog.error"), "Failed to search service types: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAdd() {
        logger.info("Add service type requested");
        try {
            boolean saved = showServiceTypeDialog(null);
            if (saved) {
                loadServiceTypes();
            }
        } catch (Exception e) {
            logger.error("Failed to open add service type dialog", e);
            showError(I18n.get("dialog.error"), "Failed to open add service type dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEdit() {
        ServiceTypeViewModel selected = serviceTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18n.get("dialog.noselection"), I18n.get("dialog.msg.selectservicetoedit"));
            return;
        }
        
        logger.info("Edit service type requested: {}", selected.getName());
        try {
            boolean saved = showServiceTypeDialog(selected.getServiceType());
            if (saved) {
                loadServiceTypes();
            }
        } catch (Exception e) {
            logger.error("Failed to open edit service type dialog", e);
            showError(I18n.get("dialog.error"), "Failed to open edit service type dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        ServiceTypeViewModel selected = serviceTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(I18n.get("dialog.noselection"), I18n.get("dialog.deleteservice.noselection"));
            return;
        }
        
        // Confirm deletion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(I18n.get("dialog.title.confirmdeletion"));
        confirmation.setHeaderText(I18n.get("dialog.deleteservice.header"));
        confirmation.setContentText(MessageFormat.format(I18n.get("dialog.deleteservice.confirm"), selected.getName()) + 
                "\n\n" + I18n.get("dialog.deleteservice.note"));
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceTypeService.deleteServiceType(selected.getId());
                loadServiceTypes();
                updateStatus(MessageFormat.format(I18n.get("dialog.deleteservice.deleted"), selected.getName()));
                logger.info("Deleted service type: {}", selected.getName());
            } catch (Exception e) {
                logger.error("Failed to delete service type", e);
                showError(I18n.get("dialog.deleteservice.deletefailed"), 
                    MessageFormat.format(I18n.get("dialog.deleteservice.error"), e.getMessage()) + 
                    "\n\n" + I18n.get("dialog.msg.servicehasitems"));
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        logger.info("Refresh requested");
        searchField.clear();
        loadServiceTypes();
    }
    
    /**
     * Show service type edit dialog
     * @param serviceType Service type to edit, or null for new service type
     * @return true if saved, false if cancelled
     */
    private boolean showServiceTypeDialog(ServiceType serviceType) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/zakilb/servicemanager/fxml/service-type-edit-dialog.fxml")
        );
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle(serviceType == null ? I18n.get("servicetypes.add") : I18n.get("servicetypes.edit"));
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(serviceTypesTable.getScene().getWindow());
        dialogStage.setScene(new Scene(loader.load()));
        
        ServiceTypeEditDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setServiceType(serviceType);
        
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
