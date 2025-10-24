package com.example.cinema_management.gate.controller;

import com.example.cinema_management.gate.service.GateValidationService;
import com.example.cinema_management.gate.service.QrDecodeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Gatekeeper UI for validating QR tickets via pasted text or uploaded image.
 */
@Controller
@RequestMapping("/gatekeeper")
public class GateController {

    private final GateValidationService validator;
    private final QrDecodeService decoder;

    public GateController(GateValidationService validator, QrDecodeService decoder) {
        this.validator = validator;
        this.decoder = decoder;
    }

    @GetMapping
    public String form() {
        return "gate/scan";
    }

    @PostMapping("/validate")
    public String validate(@RequestParam("qr") String qr, Model model) {
        try {
            var result = validator.validate(qr);
            model.addAttribute("result", result);
            model.addAttribute("qrValue", qr);
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            model.addAttribute("result", GateValidationService.Result.invalid(ex.getMessage()));
            model.addAttribute("qrValue", qr);
        }
        return "gate/scan";
    }

    /** Validate by uploading a QR image (PNG/JPG). */
    @PostMapping(path = "/validate-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String validateImage(@RequestParam("file") MultipartFile file, Model model) {
        try {
            if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please choose an image file");
            String ct = file.getContentType();
            if (ct == null || !(ct.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE) ||
                    ct.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE))) {
                throw new IllegalArgumentException("Only PNG or JPG images are allowed");
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new IllegalArgumentException("Image too large (max 2MB)");
            }

            String payload = decoder.decode(file);
            var result = validator.validate(payload);
            model.addAttribute("result", result);
            model.addAttribute("qrValue", payload);
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            model.addAttribute("result", GateValidationService.Result.invalid(ex.getMessage()));
        }
        return "gate/scan";
    }
}
