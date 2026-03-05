package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.repository.SettingsRepository;
import com.zakilb.servicemanager.service.PrintingService;
import com.zakilb.servicemanager.util.DatabaseManager;
import com.zakilb.servicemanager.util.I18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.PrintService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for Settings view
 * Manages application settings including separate printer configuration for A4 and labels
 */
public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    // Header
    @FXML private Label headerLabel;
    
    // Shop Information
    @FXML private Label shopInfoTitle;
    @FXML private Label logoLabel;
    @FXML private javafx.scene.image.ImageView logoPreview;
    @FXML private Button uploadLogoButton;
    @FXML private Button removeLogoButton;
    @FXML private Label logoHelpLabel;
    @FXML private Label shopNameLabel;
    @FXML private TextField shopNameField;
    @FXML private Label shopAddressLabel;
    @FXML private TextArea shopAddressField;
    @FXML private Label shopPhoneLabel;
    @FXML private TextField shopPhoneField;
    @FXML private Label shopEmailLabel;
    @FXML private TextField shopEmailField;

    // Language
    @FXML private Label languageTitle;
    @FXML private Label languageSelectLabel;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label languageRestartLabel;
    
    // Printing Settings - Separate printers
    @FXML private Label printingTitle;
    @FXML private Label a4PrinterLabel;
    @FXML private ComboBox<String> a4PrinterComboBox;
    @FXML private Label a4PrinterHelpLabel;
    @FXML private Label pdfCopiesLabel;
    @FXML private Spinner<Integer> pdfCopiesSpinner;
    @FXML private Label pdfCopiesHelpLabel;
    @FXML private Label labelPrinterLabel;
    @FXML private ComboBox<String> labelPrinterComboBox;
    @FXML private Label labelPrinterHelpLabel;
    @FXML private Button refreshPrintersButton;
    @FXML private Label stickerSizeLabel;
    @FXML private ComboBox<String> stickerSizeComboBox;
    
    // Custom size fields
    @FXML private VBox customSizeBox;
    @FXML private Label customSizeLabel;
    @FXML private Label widthLabel;
    @FXML private TextField customWidthField;
    @FXML private Label heightLabel;
    @FXML private TextField customHeightField;
    
    @FXML private CheckBox useZPLPrintingCheckBox;
    @FXML private Label zplHelpLabel;
    
    // Printer DPI Setting
    @FXML private Label printerDpiLabel;
    @FXML private ComboBox<String> printerDpiComboBox;
    @FXML private Label printerDpiHelpLabel;
    
    // Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastSavedLabel;
    
    private final SettingsRepository settingsRepository;
    private final PrintingService printingService;

    // Track if settings have been modified
    private boolean settingsModified = false;
    
    // No printer option
    private static final String NO_PRINTER = "---";
    private static final String CUSTOM_SIZE = "Custom";
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SettingsController() {
        this.settingsRepository = new SettingsRepository();
        this.printingService = new PrintingService();
    }
    
    @FXML
    public void initialize() {
        logger.info("Initializing SettingsController");
        
        applyTranslations();
        setupComboBoxes();
        setupChangeListeners();
        loadSettings();
        logger.info("Settings loaded successfully");
    }

    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        headerLabel.setText(I18n.get("settings.title"));
        
        // Shop Info Section
        shopInfoTitle.setText(I18n.get("settings.shop.title"));
        logoLabel.setText(I18n.get("settings.shop.logo"));
        logoHelpLabel.setText(I18n.get("settings.shop.logohelp"));
        uploadLogoButton.setText(I18n.get("settings.upload"));
        removeLogoButton.setText(I18n.get("settings.remove"));
        shopNameLabel.setText(I18n.get("settings.shop.name"));
        shopAddressLabel.setText(I18n.get("settings.shop.address"));
        shopPhoneLabel.setText(I18n.get("settings.shop.phone"));
        shopEmailLabel.setText(I18n.get("settings.shop.email"));
        
        // Language Section
        languageTitle.setText(I18n.get("settings.language.title"));
        languageSelectLabel.setText(I18n.get("settings.language.select"));
        languageRestartLabel.setText(I18n.get("settings.language.restart"));
        
        // Printing Section
        printingTitle.setText(I18n.get("settings.printing.title"));
        a4PrinterLabel.setText(I18n.get("settings.printing.a4printer"));
        a4PrinterHelpLabel.setText(I18n.get("settings.printing.a4printer.help"));
        if (pdfCopiesLabel != null) pdfCopiesLabel.setText(I18n.get("settings.printing.pdfcopies"));
        if (pdfCopiesHelpLabel != null) pdfCopiesHelpLabel.setText(I18n.get("settings.printing.pdfcopies.help"));
        labelPrinterLabel.setText(I18n.get("settings.printing.labelprinter"));
        labelPrinterHelpLabel.setText(I18n.get("settings.printing.labelprinter.help"));
        refreshPrintersButton.setText(I18n.get("settings.printing.refresh"));
        stickerSizeLabel.setText(I18n.get("settings.printing.stickersize"));
        customSizeLabel.setText(I18n.get("settings.printing.customsize"));
        widthLabel.setText(I18n.get("settings.printing.width"));
        heightLabel.setText(I18n.get("settings.printing.height"));
        useZPLPrintingCheckBox.setText(I18n.get("settings.printing.usezpl"));
        zplHelpLabel.setText(I18n.get("settings.printing.zplhelp"));
        
        // DPI Setting translations
        if (printerDpiLabel != null) {
            printerDpiLabel.setText(I18n.get("settings.printing.dpi"));
        }
        if (printerDpiHelpLabel != null) {
            printerDpiHelpLabel.setText(I18n.get("settings.printing.dpihelp"));
        }
        
        // Buttons
        saveButton.setText(I18n.get("settings.save"));
        cancelButton.setText(I18n.get("settings.cancel"));
        
        statusLabel.setText(I18n.get("settings.ready"));
    }
    
    /**
     * Setup combo boxes with default values
     */
    private void setupComboBoxes() {
        // Language combo box
        if (languageComboBox != null) {
            languageComboBox.setItems(FXCollections.observableArrayList("Polski", "English"));
            if (I18n.isPolish()) {
                languageComboBox.getSelectionModel().select("Polski");
            } else {
                languageComboBox.getSelectionModel().select("English");
            }
        }
        
        // PDF copies spinner (1-5 copies)
        if (pdfCopiesSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1);
            pdfCopiesSpinner.setValueFactory(valueFactory);
            pdfCopiesSpinner.setEditable(false);
        }
        
        // Sticker size combo box - PORTRAIT sizes (width x height, width < height)
        if (stickerSizeComboBox != null) {
            stickerSizeComboBox.setItems(FXCollections.observableArrayList(
                    "36mm x 89mm (Brady BBP11-34L)",
                    "32mm x 57mm (DYMO 11354)",
                    "28mm x 89mm (DYMO 99010)",
                    "36mm x 89mm (DYMO 99012)",
                    "25mm x 54mm (DYMO 30336)",
                    "29mm x 90mm (Brother 29mm)",
                    "38mm x 90mm (Brother 38mm)",
                    CUSTOM_SIZE
            ));
            stickerSizeComboBox.getSelectionModel().select("36mm x 89mm (Brady BBP11-34L)");
            
            // Show/hide custom size fields based on selection
            stickerSizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isCustom = CUSTOM_SIZE.equals(newVal);
                if (customSizeBox != null) {
                    customSizeBox.setVisible(isCustom);
                    customSizeBox.setManaged(isCustom);
                }
            });
        }
        
        // Printer DPI combo box
        if (printerDpiComboBox != null) {
            printerDpiComboBox.setItems(FXCollections.observableArrayList(
                    "203 DPI (DYMO, Zebra)",
                    "300 DPI (Brady, Brother)",
                    "600 DPI (High resolution)"
            ));
            printerDpiComboBox.getSelectionModel().select("203 DPI (DYMO, Zebra)");
        }
        
        // Load available printers
        loadPrinters();
    }
    
    /**
     * Setup change listeners to track modifications
     */
    private void setupChangeListeners() {
        // Text fields
        if (shopNameField != null) shopNameField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (shopAddressField != null) shopAddressField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (shopPhoneField != null) shopPhoneField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (shopEmailField != null) shopEmailField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (customWidthField != null) customWidthField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (customHeightField != null) customHeightField.textProperty().addListener((obs, old, newVal) -> settingsModified = true);
        
        // Check boxes
        if (useZPLPrintingCheckBox != null) useZPLPrintingCheckBox.selectedProperty().addListener((obs, old, newVal) -> settingsModified = true);
        
        // Combo boxes
        if (a4PrinterComboBox != null) a4PrinterComboBox.valueProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (labelPrinterComboBox != null) labelPrinterComboBox.valueProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (stickerSizeComboBox != null) stickerSizeComboBox.valueProperty().addListener((obs, old, newVal) -> settingsModified = true);
        if (printerDpiComboBox != null) printerDpiComboBox.valueProperty().addListener((obs, old, newVal) -> settingsModified = true);
        
        // Spinners
        if (pdfCopiesSpinner != null) pdfCopiesSpinner.valueProperty().addListener((obs, old, newVal) -> settingsModified = true);
    }
    
    /**
     * Load settings from database
     */
    private void loadSettings() {
        try {
            // Shop Information
            if (shopNameField != null) shopNameField.setText(settingsRepository.get("shop_name", "Service"));
            if (shopAddressField != null) shopAddressField.setText(settingsRepository.get("shop_address", ""));
            if (shopPhoneField != null) shopPhoneField.setText(settingsRepository.get("shop_phone", ""));
            if (shopEmailField != null) shopEmailField.setText(settingsRepository.get("shop_email", ""));
            
            // Load logo
            String logoPath = settingsRepository.get("shop_logo_path", "");
            if (!logoPath.isEmpty()) {
                updateLogoPreview(logoPath);
                if (removeLogoButton != null) {
                    removeLogoButton.setVisible(true);
                    removeLogoButton.setManaged(true);
                }
            } else {
                if (logoPreview != null) {
                    logoPreview.setImage(null);
                    logoPreview.setVisible(false);
                }
                if (removeLogoButton != null) {
                    removeLogoButton.setVisible(false);
                    removeLogoButton.setManaged(false);
                }
            }
            
            // Printing Settings - Load saved printers
            String savedA4Printer = settingsRepository.get("printer.a4", "");
            String savedLabelPrinter = settingsRepository.get("printer.label", "");
            
            if (!savedA4Printer.isEmpty() && a4PrinterComboBox != null && a4PrinterComboBox.getItems().contains(savedA4Printer)) {
                a4PrinterComboBox.getSelectionModel().select(savedA4Printer);
            }
            
            if (!savedLabelPrinter.isEmpty() && labelPrinterComboBox != null && labelPrinterComboBox.getItems().contains(savedLabelPrinter)) {
                labelPrinterComboBox.getSelectionModel().select(savedLabelPrinter);
            }
            
            // Load PDF copies
            if (pdfCopiesSpinner != null) {
                int pdfCopies = settingsRepository.getInt("pdf.copies", 1);
                pdfCopiesSpinner.getValueFactory().setValue(Math.max(1, Math.min(5, pdfCopies)));
            }
            
            // Load sticker size
            String stickerSize = settingsRepository.get("sticker_size", "36mm x 89mm (Brady BBP11-34L)");
            if (stickerSizeComboBox != null) {
                if (stickerSizeComboBox.getItems().contains(stickerSize)) {
                    stickerSizeComboBox.getSelectionModel().select(stickerSize);
                } else {
                    stickerSizeComboBox.getSelectionModel().select("36mm x 89mm (Brady BBP11-34L)");
                }
            }
            
            // Load custom dimensions (portrait: width < height)
            String customWidth = settingsRepository.get("label.width.mm", "36");
            String customHeight = settingsRepository.get("label.height.mm", "89");
            if (customWidthField != null) customWidthField.setText(customWidth);
            if (customHeightField != null) customHeightField.setText(customHeight);
            
            // Show custom size box if "Custom" is selected
            if (CUSTOM_SIZE.equals(stickerSize) && customSizeBox != null) {
                customSizeBox.setVisible(true);
                customSizeBox.setManaged(true);
            }
            
            if (useZPLPrintingCheckBox != null) {
                useZPLPrintingCheckBox.setSelected(settingsRepository.getBoolean("use_zpl_printing", false));
            }
            
            // Load DPI setting
            if (printerDpiComboBox != null) {
                double dpi = settingsRepository.getDouble("label.printer.dpi", 203.0);
                if (dpi >= 600) {
                    printerDpiComboBox.getSelectionModel().select("600 DPI (High resolution)");
                } else if (dpi >= 300) {
                    printerDpiComboBox.getSelectionModel().select("300 DPI (Brady, Brother)");
                } else {
                    printerDpiComboBox.getSelectionModel().select("203 DPI (DYMO, Zebra)");
                }
            }
            
            // Last saved time
            String lastSaved = settingsRepository.get("settings_last_saved", "");
            if (!lastSaved.isEmpty() && lastSavedLabel != null) {
                lastSavedLabel.setText(I18n.get("status.lastsaved", lastSaved));
            }
            
            // Reset modified flag after loading
            settingsModified = false;
            
        } catch (SQLException e) {
            logger.error("Failed to load settings", e);
            showError("Load Error", "Failed to load settings: " + e.getMessage());
        }
    }
    
    /**
     * Load available printers into both combo boxes
     */
    private void loadPrinters() {
        try {
            PrintService[] printers = printingService.getAvailablePrinters();
            
            // Clear and add "no printer" option first
            if (a4PrinterComboBox != null) {
                a4PrinterComboBox.getItems().clear();
                a4PrinterComboBox.getItems().add(NO_PRINTER);
            }
            if (labelPrinterComboBox != null) {
                labelPrinterComboBox.getItems().clear();
                labelPrinterComboBox.getItems().add(NO_PRINTER);
            }
            
            // Add all available printers
            for (PrintService printer : printers) {
                String printerName = printer.getName();
                if (a4PrinterComboBox != null) a4PrinterComboBox.getItems().add(printerName);
                if (labelPrinterComboBox != null) labelPrinterComboBox.getItems().add(printerName);
            }
            
            // Select default printer for A4 if nothing was saved
            PrintService defaultPrinter = printingService.getDefaultPrinter();
            if (defaultPrinter != null) {
                String defaultName = defaultPrinter.getName();
                if (a4PrinterComboBox != null && a4PrinterComboBox.getSelectionModel().isEmpty()) {
                    a4PrinterComboBox.getSelectionModel().select(defaultName);
                }
            }
            
            // Default to "no printer" for labels if nothing saved
            if (labelPrinterComboBox != null && labelPrinterComboBox.getSelectionModel().isEmpty()) {
                labelPrinterComboBox.getSelectionModel().select(NO_PRINTER);
            }
            
            logger.info("Loaded {} printers", printers.length);
            
        } catch (Exception e) {
            logger.error("Failed to load printers", e);
        }
    }
    
    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        if (!settingsModified) {
            setStatus(I18n.get("settings.nochanges"));
            return;
        }
        
        // Validate custom dimensions if custom size selected
        if (CUSTOM_SIZE.equals(stickerSizeComboBox.getValue())) {
            if (!validateCustomDimensions()) {
                return;
            }
        }
        
        try {
            // Shop Information
            if (shopNameField != null) settingsRepository.set("shop_name", shopNameField.getText());
            if (shopAddressField != null) settingsRepository.set("shop_address", shopAddressField.getText());
            if (shopPhoneField != null) settingsRepository.set("shop_phone", shopPhoneField.getText());
            if (shopEmailField != null) settingsRepository.set("shop_email", shopEmailField.getText());
            
            // Save A4 Printer
            if (a4PrinterComboBox != null) {
                String selectedA4Printer = a4PrinterComboBox.getSelectionModel().getSelectedItem();
                if (selectedA4Printer != null && !NO_PRINTER.equals(selectedA4Printer)) {
                    settingsRepository.set("printer.a4", selectedA4Printer);
                } else {
                    settingsRepository.set("printer.a4", "");
                }
            }
            
            // Save Label Printer
            if (labelPrinterComboBox != null) {
                String selectedLabelPrinter = labelPrinterComboBox.getSelectionModel().getSelectedItem();
                if (selectedLabelPrinter != null && !NO_PRINTER.equals(selectedLabelPrinter)) {
                    settingsRepository.set("printer.label", selectedLabelPrinter);
                } else {
                    settingsRepository.set("printer.label", "");
                }
            }
            
            // Save PDF copies
            if (pdfCopiesSpinner != null) {
                settingsRepository.set("pdf.copies", String.valueOf(pdfCopiesSpinner.getValue()));
            }

            // Save sticker size
            if (stickerSizeComboBox != null) {
                String selectedSize = stickerSizeComboBox.getSelectionModel().getSelectedItem();
                if (selectedSize != null) {
                    settingsRepository.set("sticker_size", selectedSize);
                    
                    // Parse and save dimensions
                    int[] dims = parseStickerSize(selectedSize);
                    settingsRepository.set("label.width.mm", String.valueOf(dims[0]));
                    settingsRepository.set("label.height.mm", String.valueOf(dims[1]));
                }
            }
            
            if (useZPLPrintingCheckBox != null) {
                settingsRepository.set("use_zpl_printing", String.valueOf(useZPLPrintingCheckBox.isSelected()));
            }
            
            // Save DPI setting
            if (printerDpiComboBox != null) {
                String selectedDpi = printerDpiComboBox.getSelectionModel().getSelectedItem();
                double dpiValue = 203.0; // Default
                if (selectedDpi != null) {
                    if (selectedDpi.startsWith("300")) {
                        dpiValue = 300.0;
                    } else if (selectedDpi.startsWith("600")) {
                        dpiValue = 600.0;
                    }
                }
                settingsRepository.set("label.printer.dpi", String.valueOf(dpiValue));
            }
            
            // Save timestamp
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            settingsRepository.set("settings_last_saved", timestamp);
            if (lastSavedLabel != null) {
                lastSavedLabel.setText(I18n.get("status.lastsaved", timestamp));
            }
            
            settingsModified = false;
            
            showSuccess(I18n.get("msg.saved"));
            logger.info("Settings saved successfully");
            
        } catch (SQLException e) {
            logger.error("Failed to save settings", e);
            showError(I18n.get("dialog.error"), I18n.get("settings.saveerror") + ": " + e.getMessage());
        }
    }
    
    /**
     * Validate custom dimension fields for portrait labels
     * Portrait: width < height
     */
    private boolean validateCustomDimensions() {
        try {
            int width = Integer.parseInt(customWidthField.getText().trim());
            int height = Integer.parseInt(customHeightField.getText().trim());
            
            if (width < 15 || width > 100) {
                showError(I18n.get("dialog.error"), I18n.get("settings.printing.invalidwidth"));
                return false;
            }
            if (height < 30 || height > 200) {
                showError(I18n.get("dialog.error"), I18n.get("settings.printing.invalidheight"));
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError(I18n.get("dialog.error"), I18n.get("settings.printing.invaliddimensions"));
            return false;
        }
    }
    
    /**
     * Parse sticker size string to get dimensions in mm
     * For portrait: returns [width, height] where width < height
     */
    private int[] parseStickerSize(String sizeStr) {
        if (CUSTOM_SIZE.equals(sizeStr)) {
            try {
                int width = Integer.parseInt(customWidthField.getText().trim());
                int height = Integer.parseInt(customHeightField.getText().trim());
                return new int[]{width, height};
            } catch (NumberFormatException e) {
                return new int[]{36, 89}; // Default portrait
            }
        }
        
        // Parse format like "36mm x 89mm (Brady BBP11-34L)" or "36mm x 89mm"
        try {
            String cleaned = sizeStr.split("\\(")[0].trim(); // Remove (DYMO...) part
            String[] parts = cleaned.split("x");
            int width = Integer.parseInt(parts[0].replace("mm", "").trim());
            int height = Integer.parseInt(parts[1].replace("mm", "").trim());
            return new int[]{width, height};
        } catch (Exception e) {
            logger.warn("Could not parse sticker size: {}, using default", sizeStr);
            return new int[]{36, 89}; // Default portrait
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        if (settingsModified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.get("dialog.title.unsavedchanges"));
            alert.setHeaderText(I18n.get("settings.unsavedchanges"));
            alert.setContentText(I18n.get("settings.discardchanges"));
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        loadSettings();
        setStatus(I18n.get("settings.changesdiscarded"));
    }
    
    /**
     * Handle language selection change
     */
    @FXML
    private void handleLanguageChange() {
        if (languageComboBox == null) return;
        
        String selected = languageComboBox.getValue();
        String languageCode = "Polski".equals(selected) ? "pl" : "en";
        
        try {
            settingsRepository.set("app_language", languageCode);
            settingsModified = true;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.get("dialog.success"));
            alert.setHeaderText(null);
            alert.setContentText(I18n.get("settings.language.restart"));
            alert.showAndWait();
            
            logger.info("Language changed to: {}", languageCode);
        } catch (SQLException e) {
            logger.error("Failed to save language setting", e);
            showError(I18n.get("dialog.error"), "Failed to save language: " + e.getMessage());
        }
    }
    
    /**
     * Handle refresh printers
     */
    @FXML
    private void handleRefreshPrinters() {
        // Save current selections
        String savedA4 = a4PrinterComboBox != null ? a4PrinterComboBox.getValue() : null;
        String savedLabel = labelPrinterComboBox != null ? labelPrinterComboBox.getValue() : null;
        
        loadPrinters();
        
        // Restore selections if still available
        if (savedA4 != null && a4PrinterComboBox != null && a4PrinterComboBox.getItems().contains(savedA4)) {
            a4PrinterComboBox.getSelectionModel().select(savedA4);
        }
        if (savedLabel != null && labelPrinterComboBox != null && labelPrinterComboBox.getItems().contains(savedLabel)) {
            labelPrinterComboBox.getSelectionModel().select(savedLabel);
        }
        
        setStatus(I18n.get("settings.printersrefreshed"));
    }
    
    /**
     * Handle upload logo
     */
    @FXML
    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("dialog.title.selectlogo"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(saveButton != null ? saveButton.getScene().getWindow() : null);
        
        if (selectedFile != null) {
            try {
                Image testImage = new Image(selectedFile.toURI().toString());
                
                if (testImage.getWidth() > 500 || testImage.getHeight() > 150) {
                    Alert warning = new Alert(Alert.AlertType.CONFIRMATION);
                    warning.setTitle(I18n.get("dialog.title.logowarning"));
                    warning.setHeaderText("Large Logo Detected");
                    warning.setContentText(
                            String.format("The selected image is %.0fx%.0f pixels.\n" +
                                    "Recommended size is 200x58 pixels.\n\n" +
                                    "Do you want to use this image anyway?",
                                    testImage.getWidth(), testImage.getHeight())
                    );
                    
                    Optional<ButtonType> result = warning.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }
                }
                
                Path logosDir = DatabaseManager.getAppDataDirectory().resolve("logos");
                if (!Files.exists(logosDir)) {
                    Files.createDirectories(logosDir);
                }
                
                String fileExtension = getFileExtension(selectedFile.getName());
                Path targetPath = logosDir.resolve("shop-logo" + fileExtension);
                
                Files.copy(selectedFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                settingsRepository.set("shop_logo_path", targetPath.toString());
                
                updateLogoPreview(targetPath.toString());
                
                if (removeLogoButton != null) {
                    removeLogoButton.setVisible(true);
                    removeLogoButton.setManaged(true);
                }
                
                settingsModified = true;
                showSuccess("Logo uploaded successfully!");
                logger.info("Logo uploaded: {}", targetPath);
                
            } catch (IOException | SQLException e) {
                logger.error("Failed to upload logo", e);
                showError("Upload Error", "Failed to upload logo: " + e.getMessage());
            }
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return ".png";
    }
    
    private void updateLogoPreview(String logoPath) {
        if (logoPreview == null) return;
        
        try {
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                Image logo = new Image(logoFile.toURI().toString());
                logoPreview.setImage(logo);
                logoPreview.setVisible(true);
            }
        } catch (Exception e) {
            logger.error("Failed to load logo preview", e);
        }
    }
    
    @FXML
    private void handleRemoveLogo() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.get("dialog.title.removelogo"));
        alert.setHeaderText("Remove shop logo?");
        alert.setContentText("This will remove the current logo from the application.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String logoPath = settingsRepository.get("shop_logo_path", "");
                
                if (!logoPath.isEmpty()) {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        logoFile.delete();
                    }
                }
                
                settingsRepository.set("shop_logo_path", "");
                
                if (logoPreview != null) {
                    logoPreview.setImage(null);
                    logoPreview.setVisible(false);
                }
                
                if (removeLogoButton != null) {
                    removeLogoButton.setVisible(false);
                    removeLogoButton.setManaged(false);
                }
                
                settingsModified = true;
                showSuccess("Logo removed successfully");
                
            } catch (SQLException e) {
                logger.error("Failed to remove logo", e);
                showError("Error", "Failed to remove logo: " + e.getMessage());
            }
        }
    }
    
    private void setStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
            logger.debug("Status: {}", message);
        });
    }
    
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
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            setStatus("Error: " + title);
        });
    }
}
