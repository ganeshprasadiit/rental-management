package com.rental.scheduler;

import com.rental.model.Certificate;
import com.rental.model.Property;
import com.rental.service.CertificateService;
import com.rental.service.EmailService;
import com.rental.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class CertificateExpiryScheduler {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private EmailService emailService;

    @Value("${notification.enabled:true}")
    private boolean notificationsEnabled;

    // Runs daily at 8:30 AM - checks all certificates for upcoming/passed expiry
    @Scheduled(cron = "0 30 8 * * ?")
    public void checkCertificateExpiries() {
        if (!notificationsEnabled) return;

        LocalDate today = LocalDate.now();
        List<Certificate> allCertificates = certificateService.getAllCertificates();

        for (Certificate certificate : allCertificates) {
            if (certificate.getExpiryDate() == null || certificate.getExpiryDate().isBlank()) {
                continue;
            }

            // Already handled today - avoids duplicate sends if the job somehow runs twice
            if (today.toString().equals(certificate.getLastReminderMilestone())) {
                continue;
            }

            LocalDate expiryDate;
            try {
                expiryDate = LocalDate.parse(certificate.getExpiryDate());
            } catch (Exception e) {
                continue; // skip malformed dates rather than fail the whole job
            }

            long daysUntil = ChronoUnit.DAYS.between(today, expiryDate);
            boolean shouldSend = shouldSendReminder(daysUntil);

            if (!shouldSend) continue;

            Optional<Property> propertyOpt = propertyService.getPropertyById(certificate.getPropertyId());
            if (propertyOpt.isEmpty()) continue;

            if (daysUntil >= 0) {
                emailService.sendCertificateExpiryReminder(propertyOpt.get(), certificate, (int) daysUntil);
            } else {
                emailService.sendCertificateExpiredAlert(propertyOpt.get(), certificate, (int) -daysUntil);
            }

            certificateService.markReminderSent(certificate.getId(), today.toString());
        }
    }

    private boolean shouldSendReminder(long daysUntil) {
        if (daysUntil == 30 || daysUntil == 14 || daysUntil == 7 || daysUntil == 3 || daysUntil == 1 || daysUntil == 0) {
            return true; // upcoming expiry milestones
        }
        // After expiry, remind weekly so it doesn't get forgotten but doesn't spam daily either
        return daysUntil < 0 && (-daysUntil) % 7 == 0;
    }
}
