package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeStatusDialogController {

    private static final Logger logger = LoggerFactory.getLogger(ChangeStatusDialogController.class);

    @FXML private Label headerLabel;
    @FXML private Label selectStatusLabel;
    @FXML private Label orderInfoLabel;
    
    @FXML private Label receivedLabel;
    @FXML private Label receivedDescLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label inProgressDescLabel;
    @FXML private Label readyLabel;
    @FXML private Label readyDescLabel;
    @FXML private Label pickedUpLabel;
    @FXML private Label pickedUpDescLabel;
    @FXML private Label canceledLabel;
    @FXML private Label canceledDescLabel;
    
    @FXML private Label addNoteLabel;
    @FXML private Label addNoteHelpLabel;
    
    @FXML private RadioButton receivedRadio;
    @FXML private RadioButton inProgressRadio;
    @FXML private RadioButton readyRadio;
    @FXML private RadioButton pickedUpRadio;
    @FXML private RadioButton canceledRadio;

    @FXML private HBox receivedBox;
    @FXML private HBox inProgressBox;
    @FXML private HBox readyBox;
    @FXML private HBox pickedUpBox;
    @FXML private HBox canceledBox;
    
    @FXML private TextArea notesTextArea;
    @FXML private Button cancelButton;
    @FXML private Button updateButton;

    private Stage dialogStage;
    private String newStatus;
    private String notes;
    private boolean updated = false;
    private ToggleGroup statusGroup;

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        if (headerLabel != null) headerLabel.setText(I18n.get("dialog.changestatus.header"));
        if (selectStatusLabel != null) selectStatusLabel.setText(I18n.get("dialog.changestatus.selectstatus"));
        if (addNoteLabel != null) addNoteLabel.setText(I18n.get("dialog.changestatus.addnote"));
        if (addNoteHelpLabel != null) addNoteHelpLabel.setText(I18n.get("dialog.changestatus.addnotehelp"));
        
        if (receivedLabel != null) receivedLabel.setText(I18n.get("dialog.changestatus.received"));
        if (receivedDescLabel != null) receivedDescLabel.setText(I18n.get("dialog.changestatus.received.desc"));
        
        if (inProgressLabel != null) inProgressLabel.setText(I18n.get("dialog.changestatus.inprogress"));
        if (inProgressDescLabel != null) inProgressDescLabel.setText(I18n.get("dialog.changestatus.inprogress.desc"));
        
        if (readyLabel != null) readyLabel.setText(I18n.get("dialog.changestatus.ready"));
        if (readyDescLabel != null) readyDescLabel.setText(I18n.get("dialog.changestatus.ready.desc"));
        
        if (pickedUpLabel != null) pickedUpLabel.setText(I18n.get("dialog.changestatus.pickedup"));
        if (pickedUpDescLabel != null) pickedUpDescLabel.setText(I18n.get("dialog.changestatus.pickedup.desc"));
        
        if (canceledLabel != null) canceledLabel.setText(I18n.get("dialog.changestatus.canceled"));
        if (canceledDescLabel != null) canceledDescLabel.setText(I18n.get("dialog.changestatus.canceled.desc"));
        
        if (cancelButton != null) cancelButton.setText(I18n.get("dialog.cancel"));
        if (updateButton != null) updateButton.setText(I18n.get("dialog.changestatus.update"));
    }

    /**
     * Apply all translations to UI elements
     */

    @FXML
    public void initialize() {
        // Apply translations first
        applyTranslations();
        
        statusGroup = new ToggleGroup();
        receivedRadio.setToggleGroup(statusGroup);
        inProgressRadio.setToggleGroup(statusGroup);
        readyRadio.setToggleGroup(statusGroup);
        pickedUpRadio.setToggleGroup(statusGroup);
        canceledRadio.setToggleGroup(statusGroup);

        // Add hover effects
        setupHoverEffects();

        // Add listener to update card styling when radio changes
        statusGroup.selectedToggleProperty().addListener((obs, old, newVal) -> updateCardStyles());
    }

    private void setupHoverEffects() {
        addHoverEffect(receivedBox);
        addHoverEffect(inProgressBox);
        addHoverEffect(readyBox);
        addHoverEffect(pickedUpBox);
        addHoverEffect(canceledBox);
    }

    private void addHoverEffect(HBox box) {
        box.setOnMouseEntered(e -> {
            if (!isBoxSelected(box)) {
                box.setStyle(box.getStyle() + "-fx-background-color: #d5dbdb;");
            }
        });
        box.setOnMouseExited(e -> {
            if (!isBoxSelected(box)) {
                box.setStyle(box.getStyle().replace("-fx-background-color: #d5dbdb;", "-fx-background-color: #ecf0f1;"));
            }
        });
    }

    private boolean isBoxSelected(HBox box) {
        if (box == receivedBox) return receivedRadio.isSelected();
        if (box == inProgressBox) return inProgressRadio.isSelected();
        if (box == readyBox) return readyRadio.isSelected();
        if (box == pickedUpBox) return pickedUpRadio.isSelected();
        if (box == canceledBox) return canceledRadio.isSelected();
        return false;
    }

    private void updateCardStyles() {
        // Reset all boxes
        receivedBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        inProgressBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        readyBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        pickedUpBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");
        canceledBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand;");

        // Highlight selected
        if (receivedRadio.isSelected()) {
            receivedBox.setStyle("-fx-background-color: #d6eaf8; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (inProgressRadio.isSelected()) {
            inProgressBox.setStyle("-fx-background-color: #fef5e7; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #f39c12; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (readyRadio.isSelected()) {
            readyBox.setStyle("-fx-background-color: #d5f4e6; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (pickedUpRadio.isSelected()) {
            pickedUpBox.setStyle("-fx-background-color: #e8eaf6; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #95a5a6; -fx-border-width: 2; -fx-border-radius: 8;");
        } else if (canceledRadio.isSelected()) {
            canceledBox.setStyle("-fx-background-color: #fadbd8; -fx-background-radius: 8; -fx-padding: 15; -fx-cursor: hand; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setWorkOrder(WorkOrder workOrder, String customerName) {
        orderInfoLabel.setText(String.format("Order: %s | Customer: %s | Current Status: %s",
            workOrder.getOrderNumber(), customerName, getStatusDisplay(workOrder.getStatus())));

        // Select current status
        switch (workOrder.getStatus()) {
            case "received":
                receivedRadio.setSelected(true);
                break;
            case "in_progress":
                inProgressRadio.setSelected(true);
                break;
            case "ready":
                readyRadio.setSelected(true);
                break;
            case "picked_up":
                pickedUpRadio.setSelected(true);
                break;
            case "canceled":
                canceledRadio.setSelected(true);
                break;
        }
        updateCardStyles();
    }

    @FXML
    private void handleReceivedClick(MouseEvent event) {
        receivedRadio.setSelected(true);
    }

    @FXML
    private void handleInProgressClick(MouseEvent event) {
        inProgressRadio.setSelected(true);
    }

    @FXML
    private void handleReadyClick(MouseEvent event) {
        readyRadio.setSelected(true);
    }

    @FXML
    private void handlePickedUpClick(MouseEvent event) {
        pickedUpRadio.setSelected(true);
    }

    @FXML
    private void handleCanceledClick(MouseEvent event) {
        canceledRadio.setSelected(true);
    }

    @FXML
    private void handleUpdate() {
        if (receivedRadio.isSelected()) {
            newStatus = "received";
        } else if (inProgressRadio.isSelected()) {
            newStatus = "in_progress";
        } else if (readyRadio.isSelected()) {
            newStatus = "ready";
        } else if (pickedUpRadio.isSelected()) {
            newStatus = "picked_up";
        } else if (canceledRadio.isSelected()) {
            newStatus = "canceled";
        }
        
        notes = notesTextArea.getText();
        if (notes != null && notes.trim().isEmpty()) {
            notes = null;
        }

        updated = true;
        logger.info("Status updated to: {} with notes: {}", newStatus, notes != null ? "yes" : "no");
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        logger.info("Status change cancelled");
        dialogStage.close();
    }

    public boolean isUpdated() {
        return updated;
    }

    public String getNewStatus() {
        return newStatus;
    }
    
    public String getNotes() {
        return notes;
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
}
