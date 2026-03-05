package com.zakilb.servicemanager;

import com.zakilb.servicemanager.service.BackupService;

/**
 * Launcher class to start the JavaFX application
 * This class is needed for some packaging scenarios where the main class
 * extends Application, which can cause issues with module system
 */
public class Launcher {
    public static void main(String[] args) {
        // Check for pending restore BEFORE any database access
        // This solves Windows file locking issues
        BackupService.performPendingRestore();
        
        // Launch the application
        ServiceManagerApplication.main(args);
    }
}
