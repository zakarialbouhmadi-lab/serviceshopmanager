package com.zakilb.servicemanager;

import com.zakilb.servicemanager.util.I18n;
import com.zakilb.servicemanager.repository.SettingsRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Main Application class for Service Manager
 * Desktop application for managing bike/ski/snowboard service orders
 */
public class ServiceManagerApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceManagerApplication.class);
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting the Service Manager...");
        
        try {
            // Load saved language preference
            loadLanguagePreference();
            
            // Load main view FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/zakilb/servicemanager/fxml/main-view.fxml")
            );
            
            BorderPane root = loader.load();
            
            Scene scene = new Scene(root);
            // Add application CSS
            scene.getStylesheets().add(
                getClass().getResource("/com/zakilb/servicemanager/css/application.css").toExternalForm()
            );
            
            // Set the stage title
            primaryStage.setTitle(I18n.get("app.title"));
            primaryStage.setScene(scene);
            
            // Load app icon if available
            try {
                InputStream iconStream = getClass().getResourceAsStream("/icons/app-icon.png");
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                }
            } catch (Exception e) {
                logger.warn("Could not load app icon", e);
            }
            
            primaryStage.setMaximized(true);
            primaryStage.show();
            
            // Lock window size after showing
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            primaryStage.setMinWidth(bounds.getWidth());
            primaryStage.setMinHeight(bounds.getHeight());
            primaryStage.setMaxWidth(bounds.getWidth());
            primaryStage.setMaxHeight(bounds.getHeight());
            
            logger.info("Application started successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load main view", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }
    
    /**
     * Load language preference from settings
     */
    private void loadLanguagePreference() {
        try {
            SettingsRepository settings = new SettingsRepository();
            String language = settings.get("app_language", "pl"); // Default to Polish
            I18n.setLocale(language);
            logger.info("Language set to: {}", language);
        } catch (Exception e) {
            logger.warn("Could not load language preference, using Polish", e);
            I18n.setLocale("pl");
        }
    }
    
    @Override
    public void stop() {
        logger.info("Shutting down the Service Manager...");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
