package com.example.cinema_management.gate.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Decodes a QR code from an uploaded PNG/JPG and returns the embedded text payload.
 */
@Service
public class QrDecodeService {

    public String decode(MultipartFile file) {
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) throw new IllegalArgumentException("Unsupported or empty image");
            var source = new BufferedImageLuminanceSource(img);
            var bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result r = new MultiFormatReader().decode(bitmap);
            if (r == null || r.getText() == null || r.getText().isBlank()) {
                throw new IllegalArgumentException("No QR code found in image");
            }
            return r.getText().trim();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read QR from image", e);
        }
    }
}

