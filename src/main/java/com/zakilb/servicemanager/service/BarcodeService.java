package com.zakilb.servicemanager.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.awt.image.BufferedImage;

/**
 * Service for generating Code-128 barcodes using ZXing
 */
public class BarcodeService {

    /**
     * Generate Code-128 barcode with custom dimensions
     * 
     * @param content The text to encode
     * @param width Width in pixels
     * @param height Height in pixels
     * @return BufferedImage of the barcode
     */
    public BufferedImage generateBarcode(String content, int width, int height) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Barcode content cannot be empty");
        }
        
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(content, BarcodeFormat.CODE_128, width, height);
        
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
