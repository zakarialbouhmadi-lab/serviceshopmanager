package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.BackupLog;
import com.zakilb.servicemanager.service.BackupService;
import com.zakilb.servicemanager.util.DatabaseManager;
import com.zakilb.servicemanager.util.I18n;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Backup management view
 * Handles backup creation, restoration, deletion and viewing
 */
public class BackupController {
    
    @FXML private TableView<BackupLog> backupTable;
    @FXML private TableColumn<BackupLog, String> filenameColumn;
    @FXML private TableColumn<BackupLog, String> methodColumn;
    @FXML private TableColumn<BackupLog, String> dateColumn;
    @FXML private TableColumn<BackupLog, String> sizeColumn;
    @FXML private TableColumn<BackupLog, Void> actionsColumn;
    
    @FXML private Label headerLabel;
    @FXML private Label lastBackupLabelTitle;
    @FXML private Label lastBackupLabel;
    @FXML private Label totalBackupsLabelTitle;
    @FXML private Label totalBackupsLabel;
    @FXML private Label totalSizeLabelTitle;
    @FXML private Label totalSizeLabel;
    @FXML private Label backupHistoryLabel;
    @FXML private Label noBackupsLabel;
    @FXML private Label statusLabel;
    
    @FXML private Button refreshButton;
    @FXML private Button manualBackupButton;
    @FXML private Button restoreButton;
    @FXML private Button deleteButton;
    @FXML private Button openFolderButton;
    @FXML private ProgressIndicator progressIndicator;
    
    private final BackupService backupService;
    private final ObservableList<BackupLog> backupList;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat SIZE_FORMATTER = new DecimalFormat("#,##0.00");
    
    public BackupController() {
        this.backupService = new BackupService();
        this.backupList = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        // Apply translations first
        applyTranslations();
        
        setupTable();
        setupTableSelection();
        loadBackups();
        updateStatusInfo();
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        headerLabel.setText(I18n.get("backups.title"));
        lastBackupLabelTitle.setText(I18n.get("backups.lastbackup"));
        totalBackupsLabelTitle.setText(I18n.get("backups.totalbackups"));
        totalSizeLabelTitle.setText(I18n.get("backups.totalsize"));
        backupHistoryLabel.setText(I18n.get("backups.history"));
        noBackupsLabel.setText(I18n.get("backups.nobackups"));
        
        refreshButton.setText(I18n.get("orders.refresh"));
        manualBackupButton.setText(I18n.get("backups.new"));
        restoreButton.setText(I18n.get("backups.restore"));
        deleteButton.setText(I18n.get("backups.delete"));
        openFolderButton.setText(I18n.get("backups.openfolder"));
        
        filenameColumn.setText(I18n.get("backups.column.filename"));
        methodColumn.setText(I18n.get("backups.column.type"));
        dateColumn.setText(I18n.get("backups.column.date"));
        sizeColumn.setText(I18n.get("backups.column.size"));
        actionsColumn.setText(I18n.get("backups.column.actions"));
    }
    
    /**
     * Setup table columns and cell factories
     */
    private void setupTable() {
        // Filename column
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        
        // Method column
        methodColumn.setCellValueFactory(cellData -> {
            String displayMethod = "Manual";
            return new SimpleStringProperty(displayMethod);
        });
        
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getCreatedAt().format(DATE_FORMATTER);
            return new SimpleStringProperty(dateStr);
        });
        
        // Size column
        sizeColumn.setCellValueFactory(cellData -> {
            try {
                String filename = cellData.getValue().getFilename();
                long sizeBytes = backupService.getBackupFileSize(filename);
                double sizeMB = sizeBytes / (1024.0 * 1024.0);
                return new SimpleStringProperty(SIZE_FORMATTER.format(sizeMB) + " MB");
            } catch (IOException e) {
                return new SimpleStringProperty("N/A");
            }
        });
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button(I18n.get("button.view"));
            private final HBox buttonsBox = new HBox(5);
            
            {
                viewButton.getStyleClass().add("small-button");
                viewButton.setOnAction(event -> {
                    BackupLog backup = getTableView().getItems().get(getIndex());
                    handleViewBackup(backup);
                });
                
                buttonsBox.getChildren().add(viewButton);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
        
        backupTable.setItems(backupList);
    }
    
    /**
     * Setup table selection listener
     */
    private void setupTableSelection() {
        backupTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean hasSelection = newValue != null;
                    restoreButton.setDisable(!hasSelection);
                    deleteButton.setDisable(!hasSelection);
                }
        );
    }
    
    /**
     * Load backups from database
     */
    private void loadBackups() {
        Task<List<BackupLog>> task = new Task<>() {
            @Override
            protected List<BackupLog> call() throws Exception {
                return backupService.getAllBackups();
            }
            
            @Override
            protected void succeeded() {
                backupList.clear();
                backupList.addAll(getValue());
                updateStatusInfo();
                setStatus(I18n.get("status.backupsloaded"));
            }
            
            @Override
            protected void failed() {
                showError(I18n.get("dialog.error"), getException().getMessage());
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Update status information labels
     */
    private void updateStatusInfo() {
        Task<Void> task = new Task<>() {
            private String lastBackup = "-";
            private int totalBackups = 0;
            private double totalSize = 0.0;
            
            @Override
            protected Void call() throws Exception {
                // Get latest backup
                BackupLog latest = backupService.getLatestBackup();
                if (latest != null) {
                    lastBackup = latest.getCreatedAt().format(DATE_FORMATTER);
                }
                
                // Get total backups count
                totalBackups = backupList.size();
                
                // Get total size
                totalSize = backupService.getTotalBackupSize();
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                lastBackupLabel.setText(lastBackup);
                totalBackupsLabel.setText(String.valueOf(totalBackups));
                totalSizeLabel.setText(SIZE_FORMATTER.format(totalSize) + " MB");
            }
            
            @Override
            protected void failed() {
                System.err.println("Failed to update status info: " + getException().getMessage());
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        loadBackups();
    }
    
    /**
     * Handle manual backup button click
     */
    @FXML
    private void handleManualBackup() {
        // Confirm action
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(I18n.get("dialog.backup.createmanual.title"));
        confirmAlert.setHeaderText(I18n.get("dialog.backup.createmanual.header"));
        confirmAlert.setContentText(I18n.get("dialog.backup.createmanual.content"));
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return backupService.createManualBackup();
            }
            
            @Override
            protected void succeeded() {
                String filename = getValue();
                loadBackups();
                showSuccess(MessageFormat.format(I18n.get("dialog.backup.createmanual.success"), filename));
            }
            
            @Override
            protected void failed() {
                showError(I18n.get("dialog.backup.createmanual.failed"), 
                    MessageFormat.format(I18n.get("dialog.backup.createmanual.error"), getException().getMessage()));
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Handle restore button click
     */
    @FXML
    private void handleRestore() {
        BackupLog selectedBackup = backupTable.getSelectionModel().getSelectedItem();
        if (selectedBackup == null) {
            return;
        }
        
        // Confirmation dialog with warning
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle(I18n.get("dialog.backup.restore.title"));
        confirmAlert.setHeaderText(I18n.get("dialog.backup.restore.header"));
        confirmAlert.setContentText(
                MessageFormat.format(I18n.get("dialog.backup.restore.content"), 
                    selectedBackup.getFilename(), 
                    selectedBackup.getCreatedAt().format(DATE_FORMATTER))
        );
        
        ButtonType continueButton = new ButtonType(I18n.get("dialog.backup.restore.continue"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(I18n.get("dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(continueButton, cancelButton);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != continueButton) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                backupService.restoreFromBackup(selectedBackup.getFilename());
                return null;
            }
            
            @Override
            protected void succeeded() {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle(I18n.get("dialog.backup.restore.success.title"));
                infoAlert.setHeaderText(I18n.get("dialog.backup.restore.success.header"));
                infoAlert.setContentText(I18n.get("dialog.backup.restore.success.content"));
                infoAlert.showAndWait();
                
                // Close application
                Platform.exit();
            }
            
            @Override
            protected void failed() {
                showError(I18n.get("dialog.backup.restore.failed"), 
                    MessageFormat.format(I18n.get("dialog.backup.restore.error"), getException().getMessage()));
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Handle delete button click
     */
    @FXML
    private void handleDelete() {
        BackupLog selectedBackup = backupTable.getSelectionModel().getSelectedItem();
        if (selectedBackup == null) {
            return;
        }
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(I18n.get("dialog.title.deletebackup"));
        confirmAlert.setHeaderText(I18n.get("dialog.deletebackup.header"));
        confirmAlert.setContentText(
                I18n.get("dialog.deletebackup.confirm") + "\n\n" +
                MessageFormat.format(I18n.get("dialog.deletebackup.filename"), selectedBackup.getFilename()) + "\n" +
                MessageFormat.format(I18n.get("dialog.deletebackup.date"), selectedBackup.getCreatedAt().format(DATE_FORMATTER))
        );
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                backupService.deleteBackup(selectedBackup.getFilename());
                return null;
            }
            
            @Override
            protected void succeeded() {
                loadBackups();
                showSuccess(I18n.get("dialog.deletebackup.deleted"));
            }
            
            @Override
            protected void failed() {
                showError(I18n.get("dialog.deletebackup.deletefailed"), 
                    I18n.get("dialog.deletebackup.error") + " " + getException().getMessage());
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Handle open folder button click
     */
    @FXML
    private void handleOpenFolder() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File backupDir = DatabaseManager.getAppDataDirectory().resolve("backups").toFile();
                
                // Create directory if it doesn't exist
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                // Open in file manager
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(backupDir);
                } else {
                    throw new UnsupportedOperationException(I18n.get("dialog.backup.openfolder.unsupported"));
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                setStatus(I18n.get("dialog.backup.openfolder.success"));
            }
            
            @Override
            protected void failed() {
                showError(I18n.get("dialog.backup.openfolder.failed"), 
                    MessageFormat.format(I18n.get("dialog.backup.openfolder.error"), getException().getMessage()));
            }
        };
        
        executeTask(task);
    }
    
    /**
     * Handle view backup action
     */
    private void handleViewBackup(BackupLog backup) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("dialog.backup.view.title"));
        alert.setHeaderText(I18n.get("dialog.backup.view.header"));
        
        try {
            long sizeBytes = backupService.getBackupFileSize(backup.getFilename());
            double sizeMB = sizeBytes / (1024.0 * 1024.0);
            String fileExists = backupService.backupFileExists(backup.getFilename()) ? 
                I18n.get("common.yes") : I18n.get("common.no");
            
            String content = MessageFormat.format(
                    I18n.get("dialog.backup.view.content"),
                    backup.getFilename(),
                    backup.getCreatedAt().format(DATE_FORMATTER),
                    SIZE_FORMATTER.format(sizeMB),
                    fileExists
            );
            
            alert.setContentText(content);
        } catch (IOException e) {
            alert.setContentText(MessageFormat.format(I18n.get("dialog.backup.view.error"), e.getMessage()));
        }
        
        alert.showAndWait();
    }
    
    /**
     * Execute task with progress indication
     */
    private void executeTask(Task<?> task) {
        progressIndicator.setVisible(true);
        manualBackupButton.setDisable(true);
        refreshButton.setDisable(true);
        
        task.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            manualBackupButton.setDisable(false);
            refreshButton.setDisable(false);
        });
        
        task.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            manualBackupButton.setDisable(false);
            refreshButton.setDisable(false);
        });
        
        new Thread(task).start();
    }
    
    /**
     * Set status message
     */
    private void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.get("dialog.success"));
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            setStatus(message);
        });
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            setStatus(MessageFormat.format(I18n.get("status.error"), title));
        });
    }
}
