package com.rental.controller;

import com.rental.model.Certificate;
import com.rental.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/properties/{propertyId}/certificates")
    public List<Certificate> getCertificatesForProperty(@PathVariable Long propertyId) {
        return certificateService.getCertificatesByProperty(propertyId);
    }

    @PostMapping("/properties/{propertyId}/certificates")
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("certificateType") String certificateType,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "notes", required = false) String notes) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file provided"));
        }

        try {
            Certificate saved = certificateService.uploadCertificate(
                    propertyId, file, certificateType, issueDate, expiryDate, notes);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/certificates/{id}/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long id) {
        String url = certificateService.getDownloadUrl(id);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        boolean deleted = certificateService.deleteCertificate(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
