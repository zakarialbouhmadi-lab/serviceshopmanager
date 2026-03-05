package com.zakilb.servicemanager.ui.controllers;

import com.zakilb.servicemanager.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the About view
 */
public class AboutController {
    
    private static final Logger logger = LoggerFactory.getLogger(AboutController.class);
    
    @FXML private Text appNameText;
    @FXML private Text versionText;
    @FXML private Text developedByLabel;
    @FXML private Text authorNameText;
    @FXML private Text descriptionLine1;
    @FXML private Text descriptionLine2;
    @FXML private Text featuresLabel;
    @FXML private Text feature1;
    @FXML private Text feature2;
    @FXML private Text feature3;
    @FXML private Text feature4;
    @FXML private Text feature5;
    @FXML private Text copyrightText;
    
    @FXML
    public void initialize() {
        logger.info("Initializing AboutController");
        applyTranslations();
    }
    
    /**
     * Apply all translations to UI elements
     */
    private void applyTranslations() {
        appNameText.setText(I18n.get("about.title"));
        versionText.setText(I18n.get("about.version"));
        developedByLabel.setText(I18n.get("about.developedby"));
        authorNameText.setText(I18n.get("about.author"));
        descriptionLine1.setText(I18n.get("about.description.line1"));
        descriptionLine2.setText(I18n.get("about.description.line2"));
        featuresLabel.setText(I18n.get("about.features"));
        feature1.setText(I18n.get("about.feature1"));
        feature2.setText(I18n.get("about.feature2"));
        feature3.setText(I18n.get("about.feature3"));
        feature4.setText(I18n.get("about.feature4"));
        feature5.setText(I18n.get("about.feature5"));
        copyrightText.setText(I18n.get("about.copyright"));
    }
}
