package com.zakilb.servicemanager.util;

import com.zakilb.servicemanager.model.Customer;
import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrder;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.repository.SettingsRepository;
import com.zakilb.servicemanager.service.BarcodeService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF generator for A4 customer documents.
 * Generates professional service order documents with shop header,
 * customer information, items list, and order barcode at bottom.
 */
public class PdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
    
    private final BarcodeService barcodeService;
    private final SettingsRepository settingsRepository;
    
    // PDF styling constants
    private static final int MARGIN = 40;
    private static final int FOOTER_HEIGHT = 100; // Height reserved for footer with barcode
    private static final String ENCODING = "Cp1250"; // Central European encoding for Polish characters
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, ENCODING, 17);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, ENCODING, 13);
    private static final Font FONT_SUBHEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, ENCODING, 10);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, ENCODING, 9);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, ENCODING, 8);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, ENCODING, 9);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    public PdfGenerator() {
        this.barcodeService = new BarcodeService();
        this.settingsRepository = new SettingsRepository();
    }
    
    /**
     * Footer event handler to add barcode on every page
     */
    private class BarcodeFooter extends PdfPageEventHelper {
        private final String orderNumber;
        private Image barcodeImage;
        
        public BarcodeFooter(String orderNumber) {
            this.orderNumber = orderNumber;
            try {
                BufferedImage barcode = barcodeService.generateBarcode(orderNumber, 300, 80);
                this.barcodeImage = convertToITextImage(barcode);
            } catch (Exception e) {
                logger.warn("Failed to generate barcode for footer", e);
                this.barcodeImage = null;
            }
        }
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                
                float pageWidth = document.getPageSize().getWidth();
                float footerY = document.bottomMargin() - FOOTER_HEIGHT + 20;
                
                cb.setLineWidth(0.5f);
                cb.moveTo(document.leftMargin(), footerY + 85);
                cb.lineTo(pageWidth - document.rightMargin(), footerY + 85);
                cb.stroke();
                
                if (barcodeImage != null) {
                    float barcodeWidth = 300;
                    float barcodeHeight = 80;
                    float x = (pageWidth - barcodeWidth) / 2;
                    float y = footerY;
                    
                    barcodeImage.setAbsolutePosition(x, y);
                    barcodeImage.scaleToFit(barcodeWidth, barcodeHeight);
                    cb.addImage(barcodeImage);
                    
                    cb.beginText();
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, ENCODING, BaseFont.EMBEDDED), 10);
                    float textWidth = cb.getEffectiveStringWidth(orderNumber, false);
                    cb.setTextMatrix((pageWidth - textWidth) / 2, y - 15);
                    cb.showText(orderNumber);
                    cb.endText();
                } else {
                    cb.beginText();
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, ENCODING, BaseFont.EMBEDDED), 12);
                    String text = I18n.get("pdf.order") + " " + orderNumber;
                    float textWidth = cb.getEffectiveStringWidth(text, false);
                    cb.setTextMatrix((pageWidth - textWidth) / 2, footerY + 30);
                    cb.showText(text);
                    cb.endText();
                }
                
            } catch (Exception e) {
                logger.error("Failed to add footer", e);
            }
        }
    }
    
    /**
     * Generate A4 PDF document for a work order
     * 
     * @param order The work order
     * @param customer The customer
     * @param items List of work order items with service types
     * @param outputPath Path where to save the PDF
     * @return File object of the generated PDF
     */
    public File generateA4Document(WorkOrder order, Customer customer, 
                                   List<Map.Entry<WorkOrderItem, ServiceType>> items, 
                                   String outputPath) throws DocumentException, IOException, SQLException {
        
        logger.info("Generating A4 PDF document for order {}", order.getOrderNumber());
        
        // Create document with extra bottom margin for footer
        Document document = new Document(PageSize.A4, MARGIN, MARGIN, MARGIN, MARGIN + FOOTER_HEIGHT);
        
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs(); // Ensure directory exists
        
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        
        // Set page event handler for footer with barcode
        writer.setPageEvent(new BarcodeFooter(order.getOrderNumber()));
        
        document.open();
        
        try {
            // Add header with logo
            addHeader(document);
            document.add(new Paragraph("\n"));
            
            // Add order info section
            addOrderInfo(document, order);
            document.add(new Paragraph("\n"));
            
            // Add customer info section
            addCustomerInfo(document, customer);
            document.add(new Paragraph("\n"));
            
            // Add items table
            addItemsTable(document, items);
            document.add(new Paragraph("\n"));
            
            // Add totals
            addTotals(document, items, order);
            document.add(new Paragraph("\n"));
            
            // Add order notes if present
            if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                addOrderNotes(document, order);
                document.add(new Paragraph("\n"));
            }
            
            // Barcode is now automatically added as footer on every page
            
            logger.info("PDF document generated successfully: {}", outputPath);
            
        } finally {
            document.close();
        }
        
        return outputFile;
    }
    
    /**
     * Add shop header with logo (200x58px) and shop details
     */
    private void addHeader(Document document) throws DocumentException, SQLException, IOException {
        // Try to load logo from multiple locations
        String logoPath = settingsRepository.get("shop_logo_path");
        boolean logoAdded = false;

        // Try configured path first
        if (logoPath != null && !logoPath.isEmpty()) {
            logoAdded = tryAddLogo(document, logoPath);
        }

        // Try default locations if not found
        if (!logoAdded) {
            String[] defaultPaths = {
                "resources/logo.png",
                "src/main/resources/logo.png",
                "logo.png",
                "./logo.png",
                "../logo.png",
                System.getProperty("user.dir") + "/logo.png",
                System.getProperty("user.dir") + "/resources/logo.png"
            };

            for (String path : defaultPaths) {
                if (tryAddLogo(document, path)) {
                    logoAdded = true;
                    logger.info("Logo loaded from: {}", path);
                    break;
                }
            }
        }

        // Fall back to text header if no logo found
        if (!logoAdded) {
            logger.info("No logo found, using text header");
            addTextHeader(document);
        }

        // Shop details
        String shopAddress = settingsRepository.get("shop_address");
        String shopPhone = settingsRepository.get("shop_phone");
        String shopEmail = settingsRepository.get("shop_email");

        if (shopAddress != null || shopPhone != null || shopEmail != null) {
            StringBuilder details = new StringBuilder();
            if (shopAddress != null) details.append(shopAddress).append("  |  ");
            if (shopPhone != null) details.append(I18n.get("dialog.phone")).append(" ").append(shopPhone).append("  |  ");
            if (shopEmail != null) details.append(I18n.get("dialog.email")).append(" ").append(shopEmail);

            Paragraph shopDetails = new Paragraph(details.toString(), FONT_SMALL);
            shopDetails.setAlignment(Element.ALIGN_CENTER);
            document.add(shopDetails);
        }

        // Separator line
        document.add(new Paragraph("\n", FONT_SMALL));
        LineSeparator line = new LineSeparator();
        document.add(new Chunk(line));
    }

    /**
     * Try to add logo from a specific path
     * Returns true if successful, false otherwise
     */
    private boolean tryAddLogo(Document document, String logoPath) {
        try {
            File logoFile = new File(logoPath);
            if (logoFile.exists() && logoFile.isFile()) {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(200, 58);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
                document.add(new Paragraph("\n", FONT_SMALL));
                return true;
            }
        } catch (Exception e) {
            logger.debug("Could not load logo from: {} - {}", logoPath, e.getMessage());
        }
        return false;
    }
    
    /**
     * Add text-based header if logo is not available
     */
    private void addTextHeader(Document document) throws DocumentException, SQLException {
        String shopName = settingsRepository.get("shop_name");
        if (shopName == null) shopName = "Service Manager";
        
        Paragraph title = new Paragraph(shopName, FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
    }
    
    /**
     * Add order information section (removed status)
     */
    private void addOrderInfo(Document document, WorkOrder order) throws DocumentException {
        // Title
        Paragraph sectionTitle = new Paragraph(I18n.get("pdf.serviceorder"), FONT_HEADER);
        sectionTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));
        
        // Order details in 2-column layout
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 1});
        
        // Order Number
        addInfoRow(table, I18n.get("pdf.ordernumber"), order.getOrderNumber());
        
        // Created date
        addInfoRow(table, I18n.get("pdf.created"), order.getCreatedAt().format(DATETIME_FORMATTER));
        
        // Due date
        if (order.getDueDate() != null) {
            addInfoRow(table, I18n.get("pdf.duedate"), order.getDueDate().format(DATE_FORMATTER));
        }
        
        document.add(table);
    }
    
    /**
     * Add customer information section
     */
    private void addCustomerInfo(Document document, Customer customer) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(I18n.get("pdf.customerinfo"), FONT_SUBHEADER);
        document.add(sectionTitle);
        document.add(new Paragraph("\n", FONT_SMALL));
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 2});
        
        addInfoRow(table, I18n.get("pdf.name"), customer.getName());
        
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            addInfoRow(table, I18n.get("pdf.phone"), customer.getPhone());
        }
        
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            addInfoRow(table, I18n.get("pdf.email"), customer.getEmail());
        }
        
        document.add(table);
    }
    
    /**
     * Add items table with discount support and item name
     */
    private void addItemsTable(Document document, List<Map.Entry<WorkOrderItem, ServiceType>> items)
            throws DocumentException {

        Paragraph sectionTitle = new Paragraph(I18n.get("pdf.serviceitems"), FONT_SUBHEADER);
        document.add(sectionTitle);
        document.add(new Paragraph("\n", FONT_SMALL));

        // Check if any items have discounts
        boolean hasDiscounts = items.stream()
            .anyMatch(entry -> entry.getKey().getDiscountPercent() > 0);
        boolean hasItemNames = items.stream()
            .anyMatch(entry -> entry.getKey().getItemName() != null && !entry.getKey().getItemName().isEmpty());

        // Create table with appropriate columns
        PdfPTable table;
        if (hasDiscounts) {
            // 6 or 7 columns depending on item names
            int columnCount = 6 + (hasItemNames ? 1 : 0);
            table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);
            
            // Build column widths dynamically
            java.util.List<Float> widths = new java.util.ArrayList<>();
            widths.add(0.4f); // #
            widths.add(1.5f); // Service
            if (hasItemNames) widths.add(1.2f); // Item Name
            widths.add(1.0f); // Notes
            widths.add(0.8f); // Original
            widths.add(0.6f); // Discount
            widths.add(0.8f); // Final Price
            
            float[] widthArray = new float[widths.size()];
            for (int i = 0; i < widths.size(); i++) widthArray[i] = widths.get(i);
            table.setWidths(widthArray);

            // Header row
            addTableHeader(table, I18n.get("pdf.itemnumber"));
            addTableHeader(table, I18n.get("pdf.service"));
            if (hasItemNames) addTableHeader(table, I18n.get("pdf.itemname"));
            addTableHeader(table, I18n.get("pdf.notes"));
            addTableHeader(table, I18n.get("pdf.original"));
            addTableHeader(table, I18n.get("pdf.discount"));
            addTableHeader(table, I18n.get("pdf.finalprice"));
        } else {
            // 4 or 5 columns without discount
            int columnCount = 4 + (hasItemNames ? 1 : 0);
            table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);
            
            java.util.List<Float> widths = new java.util.ArrayList<>();
            widths.add(0.4f); // #
            widths.add(2.0f); // Service
            if (hasItemNames) widths.add(1.5f); // Item Name
            widths.add(1.5f); // Notes
            widths.add(0.8f); // Price
            
            float[] widthArray = new float[widths.size()];
            for (int i = 0; i < widths.size(); i++) widthArray[i] = widths.get(i);
            table.setWidths(widthArray);

            // Header row
            addTableHeader(table, I18n.get("pdf.itemnumber"));
            addTableHeader(table, I18n.get("pdf.service"));
            if (hasItemNames) addTableHeader(table, I18n.get("pdf.itemname"));
            addTableHeader(table, I18n.get("pdf.notes"));
            addTableHeader(table, I18n.get("pdf.price"));
        }

        // Data rows
        int itemNumber = 1;
        for (Map.Entry<WorkOrderItem, ServiceType> entry : items) {
            WorkOrderItem item = entry.getKey();
            ServiceType serviceType = entry.getValue();

            // Item number
            addTableCell(table, String.valueOf(itemNumber++));

            // Service name
            addTableCell(table, serviceType.getName());

            // Item name (if column present)
            if (hasItemNames) {
                String itemName = item.getItemName();
                addTableCell(table, itemName != null && !itemName.isEmpty() ? itemName : "-");
            }

            // Notes
            String notes = item.getNotes() != null && !item.getNotes().isEmpty() ? item.getNotes() : "-";
            addTableCell(table, notes);

            if (hasDiscounts) {
                // Original price
                addTableCell(table, formatPrice(serviceType.getPriceCents()));

                // Discount
                double discountPercent = item.getDiscountPercent();
                if (discountPercent > 0) {
                    PdfPCell discountCell = new PdfPCell(new Phrase(String.format("%.1f%%", discountPercent), FONT_BOLD));
                    discountCell.setPadding(5);
                    discountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    discountCell.setBackgroundColor(new Color(255, 235, 235));
                    table.addCell(discountCell);
                } else {
                    addTableCell(table, "-");
                }

                // Final price
                int originalCents = serviceType.getPriceCents();
                int finalCents = (int) (originalCents * (1 - discountPercent / 100.0));
                PdfPCell finalPriceCell = new PdfPCell(new Phrase(formatPrice(finalCents), FONT_BOLD));
                finalPriceCell.setPadding(5);
                finalPriceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                if (discountPercent > 0) {
                    finalPriceCell.setBackgroundColor(new Color(235, 255, 235));
                }
                table.addCell(finalPriceCell);
            } else {
                // Just price (no discounts)
                addTableCell(table, formatPrice(serviceType.getPriceCents()));
            }
        }

        document.add(table);
    }

    /**
     * Add totals section with discount support and payment information
     */
    private void addTotals(Document document, List<Map.Entry<WorkOrderItem, ServiceType>> items, WorkOrder order)
            throws DocumentException {

        // Calculate totals
        int totalOriginalCents = 0;
        int totalFinalCents = 0;

        for (Map.Entry<WorkOrderItem, ServiceType> entry : items) {
            int originalCents = entry.getValue().getPriceCents();
            double discountPercent = entry.getKey().getDiscountPercent();
            int finalCents = (int) (originalCents * (1 - discountPercent / 100.0));

            totalOriginalCents += originalCents;
            totalFinalCents += finalCents;
        }

        int totalDiscountCents = totalOriginalCents - totalFinalCents;
        boolean hasDiscount = totalDiscountCents > 0;

        // Create right-aligned totals table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new int[]{2, 1});

        if (hasDiscount) {
            // Subtotal (original prices)
            addTotalRow(table,I18n.get("pdf.subtotal") , formatPrice(totalOriginalCents), false, false);

            // Discount
            PdfPCell discountLabelCell = new PdfPCell(new Phrase(I18n.get("pdf.discount"), FONT_BOLD));
            discountLabelCell.setBorder(Rectangle.NO_BORDER);
            discountLabelCell.setPadding(5);
            discountLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            discountLabelCell.setBackgroundColor(new Color(255, 235, 235));
            table.addCell(discountLabelCell);

            PdfPCell discountValueCell = new PdfPCell(new Phrase("-" + formatPrice(totalDiscountCents), FONT_BOLD));
            discountValueCell.setBorder(Rectangle.NO_BORDER);
            discountValueCell.setPadding(5);
            discountValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            discountValueCell.setBackgroundColor(new Color(255, 235, 235));
            table.addCell(discountValueCell);
        }

        // Total row (with top border)
        addTotalRow(table, I18n.get("pdf.total"), formatPrice(totalFinalCents), true, hasDiscount);

        // Payment information
        int paidCents = order.getAmountPaid() != null ? order.getAmountPaid() : 0;
        int remainingCents = totalFinalCents - paidCents;

        if (paidCents > 0) {
            // Paid amount
            PdfPCell paidLabelCell = new PdfPCell(new Phrase(I18n.get("pdf.paid"), FONT_BOLD));
            paidLabelCell.setBorder(Rectangle.NO_BORDER);
            paidLabelCell.setPadding(5);
            paidLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidLabelCell.setBackgroundColor(new Color(230, 245, 255));
            table.addCell(paidLabelCell);

            PdfPCell paidValueCell = new PdfPCell(new Phrase(formatPrice(paidCents), FONT_BOLD));
            paidValueCell.setBorder(Rectangle.NO_BORDER);
            paidValueCell.setPadding(5);
            paidValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidValueCell.setBackgroundColor(new Color(230, 245, 255));
            table.addCell(paidValueCell);
        }

        if (remainingCents > 0) {
            // Remaining balance
            PdfPCell balanceLabelCell = new PdfPCell(new Phrase(I18n.get("pdf.remaining"), FONT_BOLD));
            balanceLabelCell.setBorder(Rectangle.TOP);
            balanceLabelCell.setPadding(5);
            balanceLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            balanceLabelCell.setBackgroundColor(new Color(255, 240, 240));
            table.addCell(balanceLabelCell);

            PdfPCell balanceValueCell = new PdfPCell(new Phrase(formatPrice(remainingCents), FONT_BOLD));
            balanceValueCell.setBorder(Rectangle.TOP);
            balanceValueCell.setPadding(5);
            balanceValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            balanceValueCell.setBackgroundColor(new Color(255, 240, 240));
            table.addCell(balanceValueCell);
        } else if (paidCents >= totalFinalCents && paidCents > 0) {
            // Paid in full indicator
            PdfPCell paidFullLabelCell = new PdfPCell(new Phrase(I18n.get("pdf.paidinfull"), FONT_BOLD));
            paidFullLabelCell.setBorder(Rectangle.TOP);
            paidFullLabelCell.setPadding(5);
            paidFullLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidFullLabelCell.setBackgroundColor(new Color(235, 255, 235));
            table.addCell(paidFullLabelCell);

            PdfPCell paidFullValueCell = new PdfPCell(new Phrase("", FONT_BOLD));
            paidFullValueCell.setBorder(Rectangle.TOP);
            paidFullValueCell.setPadding(5);
            paidFullValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidFullValueCell.setBackgroundColor(new Color(235, 255, 235));
            table.addCell(paidFullValueCell);
        }

        document.add(table);
    }

    /**
     * Helper to add a total row
     */
    private void addTotalRow(PdfPTable table, String label, String value, boolean topBorder, boolean highlight) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_BOLD));
        labelCell.setBorder(topBorder ? Rectangle.TOP : Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (highlight) {
            labelCell.setBackgroundColor(new Color(235, 255, 235));
        }
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_BOLD));
        valueCell.setBorder(topBorder ? Rectangle.TOP : Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (highlight) {
            valueCell.setBackgroundColor(new Color(235, 255, 235));
        }
        table.addCell(valueCell);
    }

    /**
     * Add order notes section
     */
    private void addOrderNotes(Document document, WorkOrder order) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(I18n.get("pdf.ordernotes"), FONT_SUBHEADER);
        document.add(sectionTitle);
        document.add(new Paragraph("\n", FONT_SMALL));

        Paragraph notes = new Paragraph(order.getNotes(), FONT_NORMAL);
        notes.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(notes);
    }
    
    // Helper methods
    
    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_BOLD));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }
    
    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD));
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL));
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private String formatPrice(int priceCents) {
        double price = priceCents / 100.0;
        return String.format("%.2f PLN", price);
    }
    
    /**
     * Convert BufferedImage to iText Image
     */
    private Image convertToITextImage(BufferedImage bufferedImage) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "PNG", baos);
        return Image.getInstance(baos.toByteArray());
    }
}
