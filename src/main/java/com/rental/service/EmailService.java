package com.rental.service;

import com.rental.model.Certificate;
import com.rental.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${landlord.email}")
    private String landlordEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${notification.enabled:true}")
    private boolean notificationsEnabled;

    public void sendCertificateExpiryReminder(Property property, Certificate certificate, int daysUntilExpiry) {
        if (!canSend()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(landlordEmail);
            helper.setSubject(certificate.getCertificateType() + " expiring soon - " + property.getAddress());
            helper.setText(buildExpiryHtml(property, certificate, daysUntilExpiry, false), true);

            mailSender.send(message);
            System.out.println("Certificate expiry reminder sent for: " + property.getAddress());
        } catch (MessagingException e) {
            System.err.println("Failed to send certificate reminder: " + e.getMessage());
        }
    }

    public void sendCertificateExpiredAlert(Property property, Certificate certificate, int daysExpired) {
        if (!canSend()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(landlordEmail);
            helper.setSubject(certificate.getCertificateType() + " EXPIRED - " + property.getAddress());
            helper.setText(buildExpiryHtml(property, certificate, daysExpired, true), true);

            mailSender.send(message);
            System.out.println("Certificate expired alert sent for: " + property.getAddress());
        } catch (MessagingException e) {
            System.err.println("Failed to send certificate expired alert: " + e.getMessage());
        }
    }

    private boolean canSend() {
        if (!notificationsEnabled) return false;
        if (landlordEmail == null || landlordEmail.isBlank()) {
            System.out.println("Skipping certificate email - landlord.email not configured");
            return false;
        }
        return true;
    }

    private String buildExpiryHtml(Property property, Certificate certificate, int days, boolean expired) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: ").append(expired ? "#EF4444" : "#F59E0B").append("; color: white; padding: 24px; text-align: center; border-radius: 10px; margin-bottom: 24px; }");
        html.append(".content { background: #f8f9fa; padding: 24px; border-radius: 8px; }");
        html.append(".row { padding: 6px 0; }");
        html.append(".label { font-weight: bold; }");
        html.append(".footer { text-align: center; margin-top: 24px; color: #666; font-size: 13px; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h2>").append(expired ? "\u26A0\uFE0F Certificate Expired" : "\u23F0 Certificate Expiring Soon").append("</h2></div>");
        html.append("<div class='content'>");
        if (expired) {
            html.append("<p><strong>").append(certificate.getCertificateType()).append("</strong> for this property expired <strong>").append(days).append(" day(s) ago</strong>.</p>");
        } else {
            html.append("<p><strong>").append(certificate.getCertificateType()).append("</strong> for this property expires in <strong>").append(days).append(" day(s)</strong>.</p>");
        }
        html.append("<div class='row'><span class='label'>Property:</span> ").append(property.getAddress()).append("</div>");
        html.append("<div class='row'><span class='label'>Certificate:</span> ").append(certificate.getCertificateType()).append("</div>");
        if (certificate.getExpiryDate() != null) {
            html.append("<div class='row'><span class='label'>Expiry Date:</span> ").append(certificate.getExpiryDate()).append("</div>");
        }
        html.append("<p style='margin-top: 16px;'>Please arrange a renewal as soon as possible.</p>");
        html.append("</div>");
        html.append("<div class='footer'>This is an automated message from your Property Management System</div>");
        html.append("</div></body></html>");
        return html.toString();
    }
}
