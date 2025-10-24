package com.example.cinema_management.gate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class QrTokenSigner {

    private static final String ALG = "HmacSHA256";

    private final byte[] secret;

    public QrTokenSigner(@Value("${app.qr.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String signTicketId(Long ticketId) {
        String payload = "TICKET:" + ticketId;
        String mac = hmac(payload);
        return payload + ":" + mac;
    }

    public Long verifyAndExtractTicketId(String token) {
        String[] parts = token == null ? new String[0] : token.split(":");
        if (parts.length != 3 || !parts[0].equals("TICKET")) {
            throw new IllegalArgumentException("Invalid QR format");
        }
        String payload = parts[0] + ":" + parts[1];
        String expectedMac = hmac(payload);
        if (!expectedMac.equals(parts[2])) {
            throw new IllegalArgumentException("Invalid QR signature");
        }
        return Long.valueOf(parts[1]);
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance(ALG);
            mac.init(new SecretKeySpec(secret, ALG));
            byte[] out = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC error", e);
        }
    }
}

