package com.rental.service;

import com.rental.model.Certificate;
import com.rental.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private R2StorageService r2StorageService;

    public List<Certificate> getCertificatesByProperty(Long propertyId) {
        return certificateRepository.findByPropertyIdOrderByExpiryDateAsc(propertyId);
    }

    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    public Optional<Certificate> getCertificateById(Long id) {
        return certificateRepository.findById(id);
    }

    public Certificate uploadCertificate(Long propertyId, MultipartFile file, String certificateType,
                                          String issueDate, String expiryDate, String notes) throws IOException {
        String key = r2StorageService.uploadFile(propertyId, file);

        Certificate certificate = new Certificate();
        certificate.setPropertyId(propertyId);
        certificate.setCertificateType(certificateType);
        certificate.setFileName(file.getOriginalFilename());
        certificate.setFileKey(key);
        certificate.setFileSize(file.getSize());
        certificate.setIssueDate(issueDate);
        certificate.setExpiryDate(expiryDate);
        certificate.setNotes(notes);
        certificate.setUploadedDate(LocalDate.now().toString());

        return certificateRepository.save(certificate);
    }

    public String getDownloadUrl(Long certificateId) {
        Optional<Certificate> certOpt = certificateRepository.findById(certificateId);
        if (certOpt.isEmpty()) {
            return null;
        }
        // Link valid for 15 minutes - plenty for a user to open/download it
        return r2StorageService.generatePresignedDownloadUrl(certOpt.get().getFileKey(), Duration.ofMinutes(15));
    }

    public void markReminderSent(Long certificateId, String dateSent) {
        certificateRepository.findById(certificateId).ifPresent(cert -> {
            cert.setLastReminderMilestone(dateSent);
            certificateRepository.save(cert);
        });
    }

    public boolean deleteCertificate(Long certificateId) {
        Optional<Certificate> certOpt = certificateRepository.findById(certificateId);
        if (certOpt.isEmpty()) {
            return false;
        }

        r2StorageService.deleteFile(certOpt.get().getFileKey());
        certificateRepository.deleteById(certificateId);
        return true;
    }
}
