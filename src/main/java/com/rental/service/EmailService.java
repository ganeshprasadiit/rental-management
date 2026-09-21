package com.rental.service;

import com.rental.model.Certificate;
import com.rental.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

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

    public boolean sendCertificateExpiryReminder(Property property, Certificate certificate, int daysUntilExpiry) {
        if (!canSend()) return false;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(getLandlordEmails());
            helper.setSubject(certificate.getCertificateType() + " expiring soon - " + property.getAddress());
            helper.setText(buildExpiryHtml(property, certificate, daysUntilExpiry, false), true);

            mailSender.send(message);
            System.out.println("Certificate expiry reminder sent for: " + property.getAddress());
            return true;
        } catch (Exception e) {
            // mailSender.send() throws Spring's unchecked MailException (auth failures,
            // connection issues) - not jakarta.mail.MessagingException - so this must
            // catch Exception broadly or send failures crash the calling request.
            System.err.println("Failed to send certificate reminder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendCertificateExpiredAlert(Property property, Certificate certificate, int daysExpired) {
        if (!canSend()) return false;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(getLandlordEmails());
            helper.setSubject(certificate.getCertificateType() + " EXPIRED - " + property.getAddress());
            helper.setText(buildExpiryHtml(property, certificate, daysExpired, true), true);

            mailSender.send(message);
            System.out.println("Certificate expired alert sent for: " + property.getAddress());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send certificate expired alert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendRentPendingDailyReport(List<Property> pendingProperties) {
        if (!canSend() || pendingProperties.isEmpty()) return false;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(getLandlordEmails());
            helper.setSubject("Daily Rent Pending Report - " + java.time.LocalDate.now());
            helper.setText(buildDailyReportHtml(pendingProperties), true);

            mailSender.send(message);
            System.out.println("Daily rent pending report sent to " + landlordEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send daily rent report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendRentPendingNotification(Property property, int daysOverdue) {
        if (!canSend()) return false;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(getLandlordEmails());
            helper.setSubject("Rent Pending Alert - " + property.getAddress());
            helper.setText(buildSinglePropertyReminderHtml(property, daysOverdue), true);

            mailSender.send(message);
            System.out.println("Rent pending notification sent for: " + property.getAddress());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send rent notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildDailyReportHtml(List<Property> properties) {
        double totalPending = properties.stream()
                .mapToDouble(p -> p.getRentAmount() != null ? p.getRentAmount().doubleValue() : 0)
                .sum();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 700px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #6366F1; color: white; padding: 24px; text-align: center; border-radius: 10px; margin-bottom: 24px; }");
        html.append(".summary { background: #f8f9fa; padding: 16px; border-radius: 8px; margin-bottom: 20px; text-align: center; }");
        html.append(".property-row { background: #f8f9fa; padding: 16px; border-radius: 8px; margin-bottom: 12px; border-left: 4px solid #F59E0B; }");
        html.append(".property-row.overdue { border-left-color: #EF4444; }");
        html.append(".label { font-weight: bold; }");
        html.append(".footer { text-align: center; margin-top: 24px; color: #666; font-size: 13px; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h2>\uD83C\uDFE0 Daily Rent Pending Report</h2><p>").append(java.time.LocalDate.now()).append("</p></div>");
        html.append("<div class='summary'><strong>").append(properties.size()).append("</strong> propert").append(properties.size() == 1 ? "y" : "ies")
                .append(" with pending rent &mdash; total owed: <strong>\u00A3").append(String.format("%.2f", totalPending)).append("</strong></div>");

        for (Property p : properties) {
            html.append("<div class='property-row overdue'>");
            html.append("<div class='label'>").append(p.getAddress()).append("</div>");
            html.append("<div>Tenant: ").append(p.getTenantName() != null ? p.getTenantName() : "N/A").append("</div>");
            html.append("<div>Amount: \u00A3").append(p.getRentAmount() != null ? String.format("%.2f", p.getRentAmount()) : "N/A").append("</div>");
            if (p.getTenantPhone() != null && !p.getTenantPhone().isBlank()) {
                html.append("<div>Phone: ").append(p.getTenantPhone()).append("</div>");
            }
            html.append("</div>");
        }

        html.append("<div class='footer'>This is an automated message from your Property Management System</div>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private String buildSinglePropertyReminderHtml(Property property, int daysOverdue) {
        boolean overdue = daysOverdue > 0;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: ").append(overdue ? "#EF4444" : "#F59E0B").append("; color: white; padding: 24px; text-align: center; border-radius: 10px; margin-bottom: 24px; }");
        html.append(".content { background: #f8f9fa; padding: 24px; border-radius: 8px; }");
        html.append(".row { padding: 6px 0; }");
        html.append(".label { font-weight: bold; }");
        html.append(".footer { text-align: center; margin-top: 24px; color: #666; font-size: 13px; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h2>").append(overdue ? "\u26A0\uFE0F Rent Overdue" : "\u23F0 Rent Pending").append("</h2></div>");
        html.append("<div class='content'>");
        if (overdue) {
            html.append("<p>Rent for this property is <strong>").append(daysOverdue).append(" day(s) overdue</strong>.</p>");
        } else {
            html.append("<p>Rent for this property is currently pending.</p>");
        }
        html.append("<div class='row'><span class='label'>Property:</span> ").append(property.getAddress()).append("</div>");
        html.append("<div class='row'><span class='label'>Tenant:</span> ").append(property.getTenantName() != null ? property.getTenantName() : "N/A").append("</div>");
        html.append("<div class='row'><span class='label'>Amount:</span> \u00A3").append(property.getRentAmount() != null ? String.format("%.2f", property.getRentAmount()) : "N/A").append("</div>");
        if (property.getTenantPhone() != null && !property.getTenantPhone().isBlank()) {
            html.append("<div class='row'><span class='label'>Phone:</span> ").append(property.getTenantPhone()).append("</div>");
        }
        html.append("</div>");
        html.append("<div class='footer'>This is an automated message from your Property Management System</div>");
        html.append("</div></body></html>");
        return html.toString();
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

    private String[] getLandlordEmails() {
        return java.util.Arrays.stream(landlordEmail.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toArray(String[]::new);
    }
}