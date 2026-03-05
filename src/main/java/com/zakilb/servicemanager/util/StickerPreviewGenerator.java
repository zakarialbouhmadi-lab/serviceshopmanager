package com.zakilb.servicemanager.util;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrderItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utility to generate sticker previews without printing.
 * Run the main method to generate sample stickers in the user's home directory.
 */
public class StickerPreviewGenerator {
    
    public static void main(String[] args) {
        System.out.println("=== Sticker Preview Generator (LANDSCAPE 2-column layout) ===\n");
        
        // Output directory
        String homeDir = System.getProperty("user.home");
        String outputDir = homeDir + File.separator + "sticker_previews";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        System.out.println("Output directory: " + outputDir + "\n");
        
        // Create sample data
        ServiceType sampleService = createSampleService();
        
        String orderNumber = "2025-00042";
        String customerName = "Jan Kowalski";
        String customerPhone = "+48 123 456 789";
        LocalDate dueDate = LocalDate.now().plusDays(3);
        LocalDateTime orderDateTime = LocalDateTime.now();
        
        StickerTemplate template = new StickerTemplate();
        
        // ============================================
        // LANDSCAPE STICKERS (width > height)
        // ============================================
        System.out.println("--- LANDSCAPE stickers (2-column layout) ---\n");
        
        WorkOrderItem sampleItem = createSampleItem("Rower Trek Marlin 7");
        
        // LANDSCAPE sizes: width x height (width > height)
        int[][] sizes = {
            {89, 36, 1, 2},   // Brady BBP11-34L (recommended)
            {89, 28, 1, 1},   // DYMO 99010
            {89, 36, 2, 2},   // Brady - item 2 of 2
            {57, 32, 1, 1},   // DYMO 11354
        };
        
        String[] names = {
            "LANDSCAPE_Brady_89x36_1of2",
            "LANDSCAPE_DYMO_89x28",
            "LANDSCAPE_Brady_89x36_2of2",
            "LANDSCAPE_DYMO_57x32",
        };
        
        for (int i = 0; i < sizes.length; i++) {
            int widthMm = sizes[i][0];
            int heightMm = sizes[i][1];
            int itemNum = sizes[i][2];
            int totalItems = sizes[i][3];
            
            // Generate at 300 DPI
            int widthPx = StickerTemplate.mmToPixels(widthMm, 300);
            int heightPx = StickerTemplate.mmToPixels(heightMm, 300);
            
            System.out.printf("Generating: %s (%dmm x %dmm = %dpx x %dpx)%n", 
                            names[i], widthMm, heightMm, widthPx, heightPx);
            
            try {
                WorkOrderItem item = createSampleItem("Rower Trek Marlin 7");
                item.setItemNumber(itemNum);
                
                BufferedImage sticker = template.generateStickerWithTime(
                    item, orderNumber, customerName, customerPhone,
                    sampleService, totalItems, dueDate, orderDateTime,
                    widthPx, heightPx
                );
                
                String filename = String.format("sticker_%s.png", names[i]);
                File outputFile = new File(outputDir, filename);
                ImageIO.write(sticker, "PNG", outputFile);
                
                System.out.println("  -> Saved: " + outputFile.getName());
                
            } catch (Exception e) {
                System.err.println("  -> ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // ============================================
        // Long text test
        // ============================================
        System.out.println("\n--- Long text truncation test ---\n");
        
        String longCustomerName = "Aleksander Maksymilian Kowalczyk-Wiśniewski";
        String longItemName = "Rower górski Trek Marlin 7 2024 czarny mat XL";
        
        ServiceType longService = new ServiceType();
        longService.setId(3);
        longService.setCode("FULL");
        longService.setName("Kompleksowy serwis roweru z wymianą części");
        longService.setPriceCents(35000);
        
        WorkOrderItem longItem = createSampleItem(longItemName);
        
        int widthMm = 89;
        int heightMm = 36;
        int widthPx = StickerTemplate.mmToPixels(widthMm, 300);
        int heightPx = StickerTemplate.mmToPixels(heightMm, 300);
        
        System.out.printf("Generating: LongText_89x36 (%dmm x %dmm = %dpx x %dpx)%n", 
                        widthMm, heightMm, widthPx, heightPx);
        
        try {
            BufferedImage sticker = template.generateStickerWithTime(
                longItem, orderNumber, longCustomerName, "+48 111 222 333",
                longService, 1, dueDate, orderDateTime,
                widthPx, heightPx
            );
            
            File outputFile = new File(outputDir, "sticker_LongText_89x36.png");
            ImageIO.write(sticker, "PNG", outputFile);
            System.out.println("  -> Saved: " + outputFile.getName());
            
        } catch (Exception e) {
            System.err.println("  -> ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Done! ===");
        System.out.println("Generated sticker previews in: " + outputDir);
        
        // Try to open the folder
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(dir);
            }
        } catch (Exception e) {
            // Ignore - user can open manually
        }
    }
    
    private static WorkOrderItem createSampleItem(String itemName) {
        WorkOrderItem item = new WorkOrderItem();
        item.setId(1);
        item.setWorkOrderId(42);
        item.setServiceTypeId(1);
        item.setItemNumber(1);
        item.setBarcode("2025-00042-01");
        item.setNotes("Sample item");
        item.setItemName(itemName);
        return item;
    }
    
    private static ServiceType createSampleService() {
        ServiceType service = new ServiceType();
        service.setId(1);
        service.setCode("SGOLD");
        service.setName("Serwis Diamant");
        service.setPriceCents(15000);
        service.setDescription("Full service");
        service.setCreatedAt(LocalDateTime.now());
        return service;
    }
}
