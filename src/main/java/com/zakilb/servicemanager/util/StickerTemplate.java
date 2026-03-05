package com.zakilb.servicemanager.util;

import com.zakilb.servicemanager.model.ServiceType;
import com.zakilb.servicemanager.model.WorkOrderItem;
import com.zakilb.servicemanager.repository.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Generates sticker/label templates for work order items.
 * Creates printable stickers with LANDSCAPE layout (89x36mm) for thermal label printers.
 * Uses 2-column layout to fit all information.
 */
public class StickerTemplate {
    private static final Logger logger = LoggerFactory.getLogger(StickerTemplate.class);

    private static final double MM_PER_INCH = 25.4;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final SettingsRepository settingsRepository;
    
    public StickerTemplate() {
        this.settingsRepository = new SettingsRepository();
    }
    
    public StickerTemplate(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * Convert mm to pixels at specified DPI
     */
    public static int mmToPixels(int mm, double dpi) {
        return (int) Math.round((mm / MM_PER_INCH) * dpi);
    }
    
    /**
     * Get company name from settings
     */
    private String getCompanyName() {
        try {
            String companyName = settingsRepository.get("shop_name", "");
            return companyName != null && !companyName.isEmpty() ? companyName : "";
        } catch (SQLException e) {
            logger.warn("Failed to get company name from settings", e);
            return "";
        }
    }
    
    /**
     * Format price in PLN with currency
     */
    private String formatPrice(int priceCents) {
        double pricePLN = priceCents / 100.0;
        return String.format("%.0f zł", pricePLN);
    }

    /**
     * Generate sticker - delegates to generateStickerWithTime
     */
    public BufferedImage generateSticker(WorkOrderItem item, String orderNumber,
                                        String customerName, String customerPhone,
                                        ServiceType serviceType,
                                        int totalItems, LocalDate dueDate, LocalDate orderDate,
                                        int width, int height) {
        LocalDateTime orderDateTime = orderDate != null ? orderDate.atStartOfDay() : LocalDateTime.now();
        return generateStickerWithTime(item, orderNumber, customerName, customerPhone, 
                                       serviceType, totalItems, dueDate, orderDateTime, width, height);
    }

    public BufferedImage generateStickerWithTime(
            WorkOrderItem item,
            String orderNumber,
            String customerName,
            String customerPhone,
            ServiceType serviceType,
            int totalItems,
            LocalDate dueDate,
            LocalDateTime orderDateTime,
            int width,
            int height) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ===== BACKGROUND =====
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // ===== BORDER =====
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(1, 1, width - 3, height - 3);

        int marginX = 32;
        int marginY = 2;
        int contentWidth = width - marginX * 2;

        // ===== GRID =====
        int headerHeight = height / 6;
        int bodyTop = marginY + headerHeight + 4;
        int bodyHeight = height - bodyTop - marginY;

// row weight distribution (sum = 8)
        int r1 = bodyHeight * 2 / 8; // name+phone
        int r2 = bodyHeight * 2 / 8; // service
        int r3 = bodyHeight * 2 / 8; // item + price
        int r4 = bodyHeight * 2 / 8; // dates
        int rowY = bodyTop;

        int[] rows = { r1, r2, r3, r4 };


        // ===== FONTS (HARD CAPPED) =====
        Font headerFont = new Font("SansSerif", Font.BOLD, Math.min(32, height / 5));
        Font mainFont = new Font("Serif", Font.BOLD, Math.min(48, height / 5));
        Font dueDateFont = new Font("SansSerif", Font.BOLD, Math.min(40, height / 5));
        Font receivedDateFont = new Font("SansSerif", Font.BOLD, Math.min(32, height / 5));

        // ===== HEADER =====
        g2d.setFont(headerFont);
        FontMetrics fm = g2d.getFontMetrics();
        int headerTextY = centerTextY(marginY, headerHeight, fm);

        String left = orderNumber != null ? orderNumber : "";
        String center = getCompanyName();
        String right = (item != null ? item.getItemNumber() : 1) + "/" + totalItems;

        g2d.drawString(left, marginX, headerTextY);
        g2d.drawString(center,
                marginX + (contentWidth - fm.stringWidth(center)) / 2,
                headerTextY);
        g2d.drawString(right,
                width - marginX - fm.stringWidth(right),
                headerTextY);

        // separator
        g2d.drawLine(marginX, marginY + headerHeight, width - marginX, marginY + headerHeight);

        // ===== ROW 1 — NAME and PHONE (2 columns) =====
        g2d.setFont(mainFont);
        fm = g2d.getFontMetrics();
        int y1 = centerTextY(rowY, rows[0], fm);
        String name = customerName != null ? customerName : "";
        String phone = customerPhone != null ? customerPhone : "";
        
        g2d.drawString(
                truncate(name, contentWidth / 2 - 10, fm),
                marginX,
                y1
        );
        g2d.drawString(
                phone,
                width - marginX - fm.stringWidth(phone),
                y1
        );
        rowY += rows[0];

        // ===== ROW 2 — SERVICE TYPE =====
        g2d.setFont(mainFont);
        fm = g2d.getFontMetrics();
        String service = serviceType != null ? serviceType.getName() : "-";

        g2d.drawString(
                service,
                marginX,
                centerTextY(rowY, rows[1], fm)
        );
        rowY += rows[1];


        // ===== ROW 3 — ITEM + PRICE =====
        g2d.setFont(mainFont);
        fm = g2d.getFontMetrics();
        int y4 = centerTextY(rowY, rows[2], fm);
        String itemName = item != null ? item.getItemName() : "";
        String price = serviceType != null ? formatPrice(serviceType.getPriceCents()) : "";

        g2d.drawString(
                truncate(itemName, contentWidth - 80, fm),
                marginX,
                y4
        );

        g2d.setFont(mainFont);
        fm = g2d.getFontMetrics();
        g2d.drawString(
                price,
                width - marginX - fm.stringWidth(price),
                y4
        );
        rowY += rows[2];

        // ===== ROW 4 — DATES (2 columns) =====
        String received = orderDateTime != null
                ? "Przyjęto: " + orderDateTime.format(DATETIME_FORMATTER)
                : "Przyjęto: -";

        String due = dueDate != null
                ? "Termin Wydania: " + dueDate.format(DATE_FORMATTER)
                : "Termin Wydania: -";
        g2d.setFont(receivedDateFont);
        fm = g2d.getFontMetrics();
        int y5 = centerTextY(rowY, rows[3], fm);
        
        g2d.drawString(
                received,
                marginX,
                y5
        );
        g2d.setFont(dueDateFont);
        fm = g2d.getFontMetrics();
        g2d.drawString(
                due,
                width - marginX - fm.stringWidth(due),
                y5
        );

        g2d.dispose();
        return image;
    }


    private int centerTextY(int rowTop, int rowHeight, FontMetrics fm) {
        return rowTop + (rowHeight + fm.getAscent() - fm.getDescent()) / 2;
    }

    
    /**
     * Truncate text with ellipsis if it exceeds maxWidth
     */
    private String truncate(String text, int maxWidth, FontMetrics fm) {
        if (text == null) return "";
        if (fm.stringWidth(text) <= maxWidth) return text;
        
        String ellipsis = "..";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        
        for (int i = text.length() - 1; i > 0; i--) {
            if (fm.stringWidth(text.substring(0, i)) + ellipsisWidth <= maxWidth) {
                return text.substring(0, i) + ellipsis;
            }
        }
        return ellipsis;
    }
    
    /**
     * Save sticker image to file
     */
    public File saveStickerToFile(BufferedImage sticker, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        ImageIO.write(sticker, "PNG", outputFile);
        logger.info("Sticker saved: {} ({}x{} px)", outputPath, sticker.getWidth(), sticker.getHeight());
        return outputFile;
    }
    
    /**
     * Convert sticker image to Base64 string
     */
    public String stickerToBase64(BufferedImage sticker) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(sticker, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
