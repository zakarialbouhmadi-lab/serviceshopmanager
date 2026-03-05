package com.zakilb.servicemanager.service;

import com.google.zxing.WriterException;
import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.repository.CustomerRepository;
import com.zakilb.servicemanager.repository.ServiceTypeRepository;
import com.zakilb.servicemanager.repository.SettingsRepository;
import com.zakilb.servicemanager.util.DatabaseManager;
import com.zakilb.servicemanager.util.PdfGenerator;
import com.zakilb.servicemanager.util.StickerTemplate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for printing stickers and A4 documents
 * Supports separate printer configuration for A4/PDF documents and label printers
 */
public class PrintingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrintingService.class);
    
    private final BarcodeService barcodeService;
    private final CustomerRepository customerRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final SettingsRepository settingsRepository;
    private final PdfGenerator pdfGenerator;
    private final StickerTemplate stickerTemplate;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // DPI for label printers (configurable - Brady uses 300, DYMO uses 203)
    private static final double DEFAULT_LABEL_DPI = 203.0;
    private static final double MM_PER_INCH = 25.4;
    
    public PrintingService() {
        this.barcodeService = new BarcodeService();
        this.customerRepository = new CustomerRepository();
        this.serviceTypeRepository = new ServiceTypeRepository();
        this.settingsRepository = new SettingsRepository();
        this.pdfGenerator = new PdfGenerator();
        this.stickerTemplate = new StickerTemplate();
    }
    
    public PrintingService(BarcodeService barcodeService,
                          CustomerRepository customerRepository,
                          ServiceTypeRepository serviceTypeRepository,
                          SettingsRepository settingsRepository) {
        this.barcodeService = barcodeService;
        this.customerRepository = customerRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.settingsRepository = settingsRepository;
        this.pdfGenerator = new PdfGenerator();
        this.stickerTemplate = new StickerTemplate();
    }
    
    // ============================================
    // PRINTER CONFIGURATION METHODS
    // ============================================
    
    /**
     * Get configured label printer DPI (default 203 for DYMO, 300 for Brady)
     */
    public double getLabelPrinterDPI() {
        try {
            return settingsRepository.getDouble("label.printer.dpi", DEFAULT_LABEL_DPI);
        } catch (SQLException e) {
            logger.error("Failed to get label printer DPI", e);
            return DEFAULT_LABEL_DPI;
        }
    }
    
    /**
     * Get printer by name
     * @return PrintService or null if not found
     */
    public PrintService getPrinterByName(String printerName) {
        if (printerName == null || printerName.isEmpty()) {
            return null;
        }
        
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equals(printerName)) {
                return service;
            }
        }
        return null;
    }
    
    /**
     * Get configured A4/PDF printer
     */
    public PrintService getA4Printer() {
        try {
            String printerName = settingsRepository.get("printer.a4", "");
            PrintService printer = getPrinterByName(printerName);
            if (printer != null) {
                return printer;
            }
        } catch (SQLException e) {
            logger.error("Failed to get A4 printer setting", e);
        }
        // Fall back to default printer
        return getDefaultPrinter();
    }
    
    /**
     * Get configured label printer
     */
    public PrintService getLabelPrinter() {
        try {
            String printerName = settingsRepository.get("printer.label", "");
            PrintService printer = getPrinterByName(printerName);
            if (printer != null) {
                return printer;
            }
        } catch (SQLException e) {
            logger.error("Failed to get label printer setting", e);
        }
        return null; // No default for labels - must be configured
    }
    
    /**
     * Get configured label dimensions in mm
     * Returns LANDSCAPE format: [width, height] where width > height
     * For Brady BBP11-34L: 89mm wide x 36mm tall (landscape for 2-column layout)
     */
    public int[] getLabelDimensions() {
        try {
            int dim1 = settingsRepository.getInt("label.width.mm", 89);
            int dim2 = settingsRepository.getInt("label.height.mm", 36);
            
            // Ensure LANDSCAPE format (width > height)
            if (dim1 > dim2) {
                return new int[]{dim1, dim2};
            } else {
                return new int[]{dim2, dim1};
            }
        } catch (SQLException e) {
            logger.error("Failed to get label dimensions", e);
            return new int[]{89, 36}; // Default Brady BBP11-34L landscape
        }
    }
    
    /**
     * Get configured number of PDF copies to print (default 1)
     */
    public int getPdfCopies() {
        try {
            return settingsRepository.getInt("pdf.copies", 1);
        } catch (SQLException e) {
            logger.error("Failed to get PDF copies setting", e);
            return 1;
        }
    }
    
    /**
     * Convert mm to pixels at label printer DPI (configurable)
     */
    public int mmToPixels(int mm) {
        double dpi = getLabelPrinterDPI();
        return (int) Math.round((mm / MM_PER_INCH) * dpi);
    }
    
    /**
     * Convert mm to pixels at specific DPI
     */
    public static int mmToPixelsAtDPI(int mm, double dpi) {
        return (int) Math.round((mm / MM_PER_INCH) * dpi);
    }
    
    /**
     * Convert mm to points (1/72 inch) for page setup
     */
    public double mmToPoints(int mm) {
        return (mm / MM_PER_INCH) * 72.0;
    }
    
    // ============================================
    // STICKER PRINTING METHODS
    // ============================================
    
    /**
     * Print a single sticker using the configured label printer
     * Uses LANDSCAPE image (89x36mm) rotated for PORTRAIT label feed
     * Generates image at 300 DPI for best quality
     */
    public void printSticker(WorkOrderItem item, WorkOrder order, ServiceType serviceType, 
                            Customer customer, int totalItems) throws PrinterException, WriterException {
        
        PrintService labelPrinter = getLabelPrinter();
        if (labelPrinter == null) {
            logger.warn("No label printer configured - skipping sticker print");
            throw new PrinterException("No label printer configured. Please configure a label printer in Settings.");
        }
        
        int[] dims = getLabelDimensions();
        int widthMm = dims[0];  // 89mm (image width - longer side)
        int heightMm = dims[1]; // 36mm (image height - shorter side)
        
        // Generate image at 300 DPI for quality - LANDSCAPE
        int widthPx = mmToPixelsAtDPI(widthMm, 300);
        int heightPx = mmToPixelsAtDPI(heightMm, 300);
        
        logger.info("Generating LANDSCAPE sticker: {}x{} mm -> {}x{} px at 300 DPI (item {}/{})", 
                   widthMm, heightMm, widthPx, heightPx, item.getItemNumber(), totalItems);
        
        // Generate sticker image in landscape (width > height)
        BufferedImage stickerImage = stickerTemplate.generateStickerWithTime(
            item, order.getOrderNumber(), customer.getName(), customer.getPhone(),
            serviceType, totalItems, order.getDueDate(), order.getCreatedAt(),
            widthPx, heightPx
        );
        
        // Create printer job
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(labelPrinter);
        
        // Set page format for PORTRAIT label (Brady feeds 36mm wide x 89mm tall)
        // Image will be rotated 90° in ImagePrintable
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        
        // Convert mm to points (72 points per inch)
        // For PORTRAIT: width=36mm (shorter), height=89mm (longer)
        double paperWidthPoints = (heightMm / MM_PER_INCH) * 72.0;  // 36mm
        double paperHeightPoints = (widthMm / MM_PER_INCH) * 72.0;  // 89mm
        
        paper.setSize(paperWidthPoints, paperHeightPoints);
        paper.setImageableArea(0, 0, paperWidthPoints, paperHeightPoints);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        
        logger.info("Page format: {}x{} points, PORTRAIT (label feed orientation)", paperWidthPoints, paperHeightPoints);
        
        printerJob.setPrintable(new ImagePrintable(stickerImage), pageFormat);
        printerJob.print();
        
        logger.info("Sticker printed on {} for item {}", labelPrinter.getName(), item.getBarcode());
    }
    
    /**
     * Print stickers for all items in a work order
     */
    public void printStickers(List<WorkOrderItem> items, WorkOrder order) 
            throws PrinterException, WriterException, SQLException {
        
        PrintService labelPrinter = getLabelPrinter();
        if (labelPrinter == null) {
            throw new PrinterException("No label printer configured. Please configure a label printer in Settings.");
        }
        
        Customer customer = customerRepository.findById(order.getCustomerId());
        int totalItems = items.size();
        
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            printSticker(item, order, serviceType, customer, totalItems);
        }
    }
    
    // ============================================
    // A4 DOCUMENT PRINTING METHODS
    // ============================================
    
    /**
     * Print A4 document using configured A4 printer
     * Uses the same PdfGenerator as savePdfToFile for consistent output
     */
    public void printA4Document(WorkOrder order, List<WorkOrderItem> items, Customer customer) 
            throws Exception {
        
        PrintService a4Printer = getA4Printer();
        if (a4Printer == null) {
            logger.warn("No A4 printer available - skipping document print");
            throw new PrinterException("No A4 printer available.");
        }
        
        // Generate PDF to temp file using same template as savePdfToFile
        List<Map.Entry<WorkOrderItem, ServiceType>> itemsWithTypes = new ArrayList<>();
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            itemsWithTypes.add(new AbstractMap.SimpleEntry<>(item, serviceType));
        }
        
        // Create temp file for PDF
        String tempDir = System.getProperty("java.io.tmpdir");
        String tempPath = tempDir + File.separator + "print_" + order.getOrderNumber() + "_" + System.currentTimeMillis() + ".pdf";
        
        File pdfFile = pdfGenerator.generateA4Document(order, customer, itemsWithTypes, tempPath);
        logger.info("Generated temp PDF for printing: {}", pdfFile.getAbsolutePath());
        
        // Print the PDF file with configured number of copies
        int copies = getPdfCopies();
        try {
            printPdfFileWithPrinter(pdfFile, a4Printer, copies);
            logger.info("A4 document printed on {} for order {} ({} copies)", a4Printer.getName(), order.getOrderNumber(), copies);
        } finally {
            // Clean up temp file after printing (with delay to allow print spooler to read it)
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds for print spooler
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                        logger.debug("Cleaned up temp PDF file: {}", tempPath);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    /**
     * Print a PDF file to a specific printer using PDFBox (silent, no external app)
     */
    private void printPdfFileWithPrinter(File pdfFile, PrintService printer, int copies) throws Exception {
        logger.info("Printing PDF {} to printer {} ({} copies)", pdfFile.getName(), printer.getName(), copies);
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(printer);
            printerJob.setPageable(new PDFPageable(document));
            
            // Set number of copies
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Copies(copies));
            
            printerJob.print(attributes);
            
            logger.info("PDF printed successfully to {} ({} copies)", printer.getName(), copies);
        }
    }
    
    // ============================================
    // UTILITY METHODS
    // ============================================
    
    public PrintService[] getAvailablePrinters() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }
    
    public PrintService getDefaultPrinter() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }
    
    // ============================================
    // ENHANCED PRINTING METHODS
    // ============================================

    
    /**
     * Print all stickers for an order using enhanced template
     */
    public List<String> printStickersEnhanced(List<WorkOrderItem> items, WorkOrder order, 
                                             Customer customer) throws Exception {
        
        logger.info("Printing enhanced stickers for order {}", order.getOrderNumber());
        
        List<String> stickerPaths = new ArrayList<>();
        int totalItems = items.size();
        
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            String path = printSingleSticker(item, order, serviceType, customer, totalItems);
            stickerPaths.add(path);
        }
        
        logger.info("All {} stickers printed successfully", stickerPaths.size());
        return stickerPaths;
    }
    
    /**
     * Print a single sticker
     * Uses LANDSCAPE image rotated for PORTRAIT label feed
     */
    private String printSingleSticker(WorkOrderItem item, WorkOrder order, 
                                      ServiceType serviceType, Customer customer,
                                      int totalItems) throws Exception {
        
        logger.info("Printing sticker for item {} of order {}", 
                   item.getItemNumber(), order.getOrderNumber());
        
        int[] dims = getLabelDimensions();
        int widthMm = dims[0];  // 89mm (image width)
        int heightMm = dims[1]; // 36mm (image height)
        
        // Generate image at 300 DPI - LANDSCAPE
        int widthPx = mmToPixelsAtDPI(widthMm, 300);
        int heightPx = mmToPixelsAtDPI(heightMm, 300);
        
        // Generate sticker in landscape (width > height)
        BufferedImage stickerImage = stickerTemplate.generateStickerWithTime(
            item, order.getOrderNumber(), customer.getName(), customer.getPhone(), 
            serviceType, totalItems, order.getDueDate(), order.getCreatedAt(),
            widthPx, heightPx
        );
        
        // Save sticker to file
        String defaultStickerDir = DatabaseManager.getAppDataDirectory().resolve("stickers").toString();
        String outputDir = settingsRepository.get("sticker_output_directory", defaultStickerDir);
        
        String filename = String.format("sticker_%s.png", item.getBarcode());
        String outputPath = outputDir + "/" + filename;
        
        File stickerFile = stickerTemplate.saveStickerToFile(stickerImage, outputPath);
        logger.info("Sticker saved to: {}", stickerFile.getAbsolutePath());
        
        // Print using configured label printer
        PrintService labelPrinter = getLabelPrinter();
        if (labelPrinter == null) {
            logger.warn("No label printer configured - sticker saved but not printed");
            return stickerFile.getAbsolutePath();
        }
        
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(labelPrinter);
        
        // Set page format - PORTRAIT (label feeds 36mm wide x 89mm tall)
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        double paperWidthPoints = (heightMm / MM_PER_INCH) * 72.0;  // 36mm
        double paperHeightPoints = (widthMm / MM_PER_INCH) * 72.0;  // 89mm
        paper.setSize(paperWidthPoints, paperHeightPoints);
        paper.setImageableArea(0, 0, paperWidthPoints, paperHeightPoints);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        
        printerJob.setPrintable(new ImagePrintable(stickerImage), pageFormat);
        
        boolean showDialog = settingsRepository.getBoolean("show_print_dialog", false);
        if (showDialog) {
            if (printerJob.printDialog()) {
                printerJob.print();
            }
        } else {
            printerJob.print();
        }
        
        return stickerFile.getAbsolutePath();
    }

    public List<String> saveStickersToDirectory(List<WorkOrderItem> items, WorkOrder order, 
                                                Customer customer, String directoryPath) throws Exception {
        
        logger.info("Saving stickers to directory: {}", directoryPath);
        
        List<String> savedPaths = new ArrayList<>();
        int totalItems = items.size();
        
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        int[] dims = getLabelDimensions();
        // Use 300 DPI for high quality stickers
        int widthPx = mmToPixelsAtDPI(dims[0], 300);
        int heightPx = mmToPixelsAtDPI(dims[1], 300);
        
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            
            // Use generateStickerWithTime for full datetime support
            BufferedImage stickerImage = stickerTemplate.generateStickerWithTime(
                item, order.getOrderNumber(), customer.getName(), customer.getPhone(),
                serviceType, totalItems, order.getDueDate(), order.getCreatedAt(),
                widthPx, heightPx
            );
            
            String filename = String.format("sticker_%s.png", item.getBarcode());
            String outputPath = directoryPath + File.separator + filename;
            
            File stickerFile = stickerTemplate.saveStickerToFile(stickerImage, outputPath);
            savedPaths.add(stickerFile.getAbsolutePath());
            
            logger.info("Saved sticker: {}", filename);
        }
        
        logger.info("All {} stickers saved successfully", savedPaths.size());
        return savedPaths;
    }
    
    public String savePdfToFile(WorkOrder order, List<WorkOrderItem> items, 
                               Customer customer, String filePath) throws Exception {
        
        logger.info("Saving PDF to: {}", filePath);
        
        List<Map.Entry<WorkOrderItem, ServiceType>> itemsWithTypes = new ArrayList<>();
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            itemsWithTypes.add(new AbstractMap.SimpleEntry<>(item, serviceType));
        }
        
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }
        
        File pdfFile = pdfGenerator.generateA4Document(order, customer, itemsWithTypes, filePath);
        logger.info("PDF saved successfully: {}", pdfFile.getAbsolutePath());
        
        return pdfFile.getAbsolutePath();
    }
    
    /**
     * Helper class to print BufferedImage
     * Rotates image 90° clockwise for Brady thermal printers that feed labels in portrait
     */
    private static class ImagePrintable implements Printable {
        private final BufferedImage image;
        
        public ImagePrintable(BufferedImage image) {
            this.image = image;
        }
        
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            
            // High quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Rotate image 90° clockwise for Brady printers
            // Input: landscape image (width > height, e.g., 1051x425 px for 89x36mm)
            // Output: portrait image (height > width) that prints correctly on portrait label
            BufferedImage rotated = rotateClockwise90(image);
            
            // Get imageable area
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            double pageX = pageFormat.getImageableX();
            double pageY = pageFormat.getImageableY();
            
            // Scale rotated image to fit page maintaining aspect ratio
            double scaleX = pageWidth / rotated.getWidth();
            double scaleY = pageHeight / rotated.getHeight();
            double scale = Math.min(scaleX, scaleY);
            
            double drawWidth = rotated.getWidth() * scale;
            double drawHeight = rotated.getHeight() * scale;
            double x = pageX + (pageWidth - drawWidth) / 2;
            double y = pageY + (pageHeight - drawHeight) / 2;
            
            g2d.drawImage(rotated, (int)x, (int)y, (int)drawWidth, (int)drawHeight, null);
            
            return PAGE_EXISTS;
        }
        
        /**
         * Rotate image 90 degrees clockwise
         */
        private BufferedImage rotateClockwise90(BufferedImage src) {
            int w = src.getWidth();
            int h = src.getHeight();
            BufferedImage rotated = new BufferedImage(h, w, src.getType());
            Graphics2D g2d = rotated.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Rotate 90° clockwise: translate to new position, then rotate
            g2d.translate(h, 0);
            g2d.rotate(Math.PI / 2);
            g2d.drawImage(src, 0, 0, null);
            g2d.dispose();
            return rotated;
        }
    }
    
    /**
     * Result class for complete order printing
     */
    /**
     * Complete order printing workflow
     */
    public OrderPrintResult printCompleteOrder(WorkOrder order, List<WorkOrderItem> items,
                                              Customer customer) {
        
        logger.info("Starting complete print workflow for order {}", order.getOrderNumber());
        
        OrderPrintResult result = new OrderPrintResult();
        result.orderNumber = order.getOrderNumber();
        
        try {
            result.stickerPaths = printStickersEnhanced(items, order, customer);
            result.stickersSuccess = true;
            logger.info("Stickers printed successfully");
        } catch (Exception e) {
            logger.error("Failed to print stickers", e);
            result.stickersSuccess = false;
            result.errorMessage = "Sticker printing failed: " + e.getMessage();
        }
        
        try {
            result.documentPath = printA4DocumentEnhanced(order, items, customer);
            result.documentSuccess = true;
            logger.info("A4 document printed successfully");
        } catch (Exception e) {
            logger.error("Failed to print A4 document", e);
            result.documentSuccess = false;
            if (result.errorMessage != null) {
                result.errorMessage += "; ";
            } else {
                result.errorMessage = "";
            }
            result.errorMessage += "A4 printing failed: " + e.getMessage();
        }
        
        result.overallSuccess = result.stickersSuccess && result.documentSuccess;
        logger.info("Complete print workflow finished. Success: {}", result.overallSuccess);
        return result;
    }
    
    /**
     * Print A4 document using PdfGenerator utility
     */
    public String printA4DocumentEnhanced(WorkOrder order, List<WorkOrderItem> items, 
                                         Customer customer) throws Exception {
        
        logger.info("Generating enhanced A4 PDF for order {}", order.getOrderNumber());
        
        List<Map.Entry<WorkOrderItem, ServiceType>> itemsWithTypes = new ArrayList<>();
        for (WorkOrderItem item : items) {
            ServiceType serviceType = serviceTypeRepository.findById(item.getServiceTypeId());
            itemsWithTypes.add(new AbstractMap.SimpleEntry<>(item, serviceType));
        }
        
        String defaultDocDir = DatabaseManager.getAppDataDirectory().resolve("documents").toString();
        String outputDir = settingsRepository.get("pdf_output_directory", defaultDocDir);
        String filename = String.format("order_%s.pdf", order.getOrderNumber());
        String outputPath = outputDir + "/" + filename;
        
        File pdfFile = pdfGenerator.generateA4Document(order, customer, itemsWithTypes, outputPath);
        logger.info("PDF document generated: {}", pdfFile.getAbsolutePath());
        
        boolean autoPrint = settingsRepository.getBoolean("auto_print_a4", true);
        PrintService a4Printer = getA4Printer();
        
        if (autoPrint && a4Printer != null) {
            int copies = getPdfCopies();
            printPdfFileWithPrinter(pdfFile, a4Printer, copies);
        } else if (autoPrint && a4Printer == null) {
            logger.warn("Auto-print enabled but no A4 printer available - PDF saved only");
        }
        
        return pdfFile.getAbsolutePath();
    }
    
    /**
     * Print sticker image using system print dialog
     * Uses PORTRAIT page format for thermal label printers
     */
    public void printStickerWithDialog(BufferedImage stickerImage, int widthMm, int heightMm) 
            throws PrinterException {
        
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        
        // PORTRAIT page format (label feeds 36mm wide x 89mm tall)
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        double paperWidthPoints = (heightMm / MM_PER_INCH) * 72.0;  // shorter dimension
        double paperHeightPoints = (widthMm / MM_PER_INCH) * 72.0;  // longer dimension
        paper.setSize(paperWidthPoints, paperHeightPoints);
        paper.setImageableArea(0, 0, paperWidthPoints, paperHeightPoints);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        
        printerJob.setPrintable(new ImagePrintable(stickerImage), pageFormat);
        
        // Show print dialog to let user configure printer
        if (printerJob.printDialog()) {
            printerJob.print();
            logger.info("Sticker printed via dialog");
        }
    }
    
    public static class OrderPrintResult {
        public String orderNumber;
        public List<String> stickerPaths;
        public String documentPath;
        public boolean stickersSuccess;
        public boolean documentSuccess;
        public boolean overallSuccess;
        public String errorMessage;
        
        @Override
        public String toString() {
            return String.format("OrderPrintResult{order=%s, stickers=%s, document=%s, success=%s}",
                orderNumber, stickersSuccess, documentSuccess, overallSuccess);
        }
    }
}
