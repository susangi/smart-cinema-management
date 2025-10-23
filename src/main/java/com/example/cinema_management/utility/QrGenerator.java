package com.example.cinema_management.utility;

import org.springframework.stereotype.Component;

@Component
public class QrGenerator {
    public String pngBase64(String payload, int size) {
        try {
            var matrix = new com.google.zxing.qrcode.QRCodeWriter()
                    .encode(payload, com.google.zxing.BarcodeFormat.QR_CODE, size, size);
            var img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < size; y++)
                for (int x = 0; x < size; x++)
                    img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            var baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", baos);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}