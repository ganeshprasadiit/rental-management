package com.rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.model.Certificate;
import com.rental.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${landlord.email}")
    private String landlordEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.from.email:}")
    private String brevoFromEmail;

    @Value("${brevo.from.name:Property Manager}")
    private String brevoFromName;

    @Value("${notification.enabled:true}")
    private boolean notificationsEnabled;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    // ============================================================
    // CERTIFICATE EXPIRY REMINDER
    // ============================================================

    public boolean sendCertificateExpiryReminder(
            Property property,
            Certificate certificate,
            int daysUntilExpiry) {

        if (!canSend()) {
            return false;
        }

        String subject =
                certificate.getCertificateType()
                        + " expiring soon - "
                        + property.getAddress();

        String html =
                buildExpiryHtml(
                        property,
                        certificate,
                        daysUntilExpiry,
                        false
                );

        boolean sent =
                sendViaBrevo(
                        getLandlordEmails(),
                        subject,
                        html
                );

        if (sent) {
            System.out.println(
                    "Certificate expiry reminder sent for: "
                            + property.getAddress()
            );
        }

        return sent;
    }


    // ============================================================
    // CERTIFICATE EXPIRED ALERT
    // ============================================================

    public boolean sendCertificateExpiredAlert(
            Property property,
            Certificate certificate,
            int daysExpired) {

        if (!canSend()) {
            return false;
        }

        String subject =
                certificate.getCertificateType()
                        + " EXPIRED - "
                        + property.getAddress();

        String html =
                buildExpiryHtml(
                        property,
                        certificate,
                        daysExpired,
                        true
                );

        boolean sent =
                sendViaBrevo(
                        getLandlordEmails(),
                        subject,
                        html
                );

        if (sent) {
            System.out.println(
                    "Certificate expired alert sent for: "
                            + property.getAddress()
            );
        }

        return sent;
    }


    // ============================================================
    // DAILY RENT PENDING REPORT
    // ============================================================

    public boolean sendRentPendingDailyReport(
            List<Property> pendingProperties) {

        if (!canSend() || pendingProperties.isEmpty()) {
            return false;
        }

        String subject =
                "Daily Rent Pending Report - "
                        + java.time.LocalDate.now();

        String html =
                buildDailyReportHtml(pendingProperties);

        boolean sent =
                sendViaBrevo(
                        getLandlordEmails(),
                        subject,
                        html
                );

        if (sent) {
            System.out.println(
                    "Daily rent pending report sent to "
                            + landlordEmail
            );
        }

        return sent;
    }


    // ============================================================
    // SINGLE PROPERTY RENT PENDING NOTIFICATION
    // ============================================================

    public boolean sendRentPendingNotification(
            Property property,
            int daysOverdue) {

        if (!canSend()) {
            return false;
        }

        String subject =
                "Rent Pending Alert - "
                        + property.getAddress();

        String html =
                buildSinglePropertyReminderHtml(
                        property,
                        daysOverdue
                );

        boolean sent =
                sendViaBrevo(
                        getLandlordEmails(),
                        subject,
                        html
                );

        if (sent) {
            System.out.println(
                    "Rent pending notification sent for: "
                            + property.getAddress()
            );
        }

        return sent;
    }


    // ============================================================
    // BREVO EMAIL SENDING
    // ============================================================

    /**
     * Sends email through Brevo's HTTPS API.
     *
     * Railway Hobby blocks outbound SMTP, so this uses
     * Brevo's HTTPS API instead.
     *
     * Returns true only for a successful 2xx response.
     * Network/API failures return false rather than
     * crashing the calling application.
     */
    private boolean sendViaBrevo(
            String[] toAddresses,
            String subject,
            String htmlBody) {

        try {

            // ----------------------------------------------------
            // Build recipient list
            // ----------------------------------------------------

            List<Map<String, String>> recipients =
                    Arrays.stream(toAddresses)
                            .map(String::trim)
                            .filter(email -> !email.isEmpty())
                            .map(email -> Map.of("email", email))
                            .toList();


            if (recipients.isEmpty()) {
                System.err.println(
                        "Brevo email not sent - no recipients"
                );
                return false;
            }


            // ----------------------------------------------------
            // Build Brevo request payload
            // ----------------------------------------------------

            Map<String, Object> payload =
                    new LinkedHashMap<>();

            payload.put(
                    "sender",
                    Map.of(
                            "name",
                            brevoFromName,
                            "email",
                            brevoFromEmail
                    )
            );

            payload.put(
                    "to",
                    recipients
            );

            payload.put(
                    "subject",
                    subject
            );

            payload.put(
                    "htmlContent",
                    htmlBody
            );


            // ----------------------------------------------------
            // Convert payload to JSON
            // ----------------------------------------------------

            String jsonBody =
                    objectMapper.writeValueAsString(payload);


            // ----------------------------------------------------
            // Build HTTPS request
            // ----------------------------------------------------

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://api.brevo.com/v3/smtp/email"
                                    )
                            )
                            .header(
                                    "api-key",
                                    brevoApiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .timeout(
                                    Duration.ofSeconds(15)
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(jsonBody)
                            )
                            .build();


            // ----------------------------------------------------
            // Send request
            // ----------------------------------------------------

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );


            // ----------------------------------------------------
            // Handle response
            // ----------------------------------------------------

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                System.out.println(
                        "Email sent successfully via Brevo to "
                                + String.join(
                                ", ",
                                toAddresses
                        )
                );

                return true;
            }


            // ----------------------------------------------------
            // API error
            // ----------------------------------------------------

            System.err.println(
                    "Brevo API error ("
                            + response.statusCode()
                            + "): "
                            + response.body()
            );

            return false;

        } catch (Exception e) {

            // ----------------------------------------------------
            // Network / unexpected error
            // ----------------------------------------------------

            System.err.println(
                    "Failed to send email via Brevo: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // ============================================================
    // GET LANDLORD EMAIL ADDRESSES
    // ============================================================

    private String[] getLandlordEmails() {

        return Arrays.stream(
                        landlordEmail.split(",")
                )
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toArray(String[]::new);
    }


    // ============================================================
    // DAILY RENT REPORT HTML
    // ============================================================

    private String buildDailyReportHtml(
            List<Property> properties) {

        double totalPending =
                properties.stream()
                        .mapToDouble(
                                p -> p.getRentAmount() != null
                                        ? p.getRentAmount()
                                        .doubleValue()
                                        : 0
                        )
                        .sum();


        StringBuilder html =
                new StringBuilder();

        html.append(
                "<!DOCTYPE html><html><head><style>"
        );

        html.append(
                "body { font-family: Arial, sans-serif; "
                        + "line-height: 1.6; color: #333; }"
        );

        html.append(
                ".container { max-width: 700px; "
                        + "margin: 0 auto; padding: 20px; }"
        );

        html.append(
                ".header { background: #6366F1; "
                        + "color: white; padding: 24px; "
                        + "text-align: center; border-radius: 10px; "
                        + "margin-bottom: 24px; }"
        );

        html.append(
                ".summary { background: #f8f9fa; "
                        + "padding: 16px; border-radius: 8px; "
                        + "margin-bottom: 20px; "
                        + "text-align: center; }"
        );

        html.append(
                ".property-row { background: #f8f9fa; "
                        + "padding: 16px; border-radius: 8px; "
                        + "margin-bottom: 12px; "
                        + "border-left: 4px solid #F59E0B; }"
        );

        html.append(
                ".property-row.overdue { "
                        + "border-left-color: #EF4444; }"
        );

        html.append(
                ".label { font-weight: bold; }"
        );

        html.append(
                ".footer { text-align: center; "
                        + "margin-top: 24px; color: #666; "
                        + "font-size: 13px; }"
        );

        html.append(
                "</style></head><body>"
        );

        html.append(
                "<div class='container'>"
        );

        html.append(
                "<div class='header'>"
                        + "<h2>🏠 Daily Rent Pending Report</h2>"
                        + "<p>"
                        + java.time.LocalDate.now()
                        + "</p></div>"
        );

        html.append(
                "<div class='summary'>"
                        + "<strong>"
                        + properties.size()
                        + "</strong> propert"
                        + (
                        properties.size() == 1
                                ? "y"
                                : "ies"
                )
                        + " with pending rent "
                        + "&mdash; total owed: "
                        + "<strong>£"
                        + String.format(
                        "%.2f",
                        totalPending
                )
                        + "</strong></div>"
        );


        for (Property p : properties) {

            html.append(
                    "<div class='property-row overdue'>"
            );

            html.append(
                    "<div class='label'>"
                            + p.getAddress()
                            + "</div>"
            );

            html.append(
                    "<div>Tenant: "
                            + (
                            p.getTenantName() != null
                                    ? p.getTenantName()
                                    : "N/A"
                    )
                            + "</div>"
            );

            html.append(
                    "<div>Amount: £"
                            + (
                            p.getRentAmount() != null
                                    ? String.format(
                                    "%.2f",
                                    p.getRentAmount()
                            )
                                    : "N/A"
                    )
                            + "</div>"
            );


            if (p.getTenantPhone() != null
                    && !p.getTenantPhone().isBlank()) {

                html.append(
                        "<div>Phone: "
                                + p.getTenantPhone()
                                + "</div>"
                );
            }

            html.append("</div>");
        }


        html.append(
                "<div class='footer'>"
                        + "This is an automated message from "
                        + "your Property Management System"
                        + "</div>"
        );

        html.append(
                "</div></body></html>"
        );

        return html.toString();
    }


    // ============================================================
    // SINGLE PROPERTY RENT REMINDER HTML
    // ============================================================

    private String buildSinglePropertyReminderHtml(
            Property property,
            int daysOverdue) {

        boolean overdue = daysOverdue > 0;

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<!DOCTYPE html><html><head><style>"
        );

        html.append(
                "body { font-family: Arial, sans-serif; "
                        + "line-height: 1.6; color: #333; }"
        );

        html.append(
                ".container { max-width: 600px; "
                        + "margin: 0 auto; padding: 20px; }"
        );

        html.append(
                ".header { background: "
                        + (
                        overdue
                                ? "#EF4444"
                                : "#F59E0B"
                )
                        + "; color: white; padding: 24px; "
                        + "text-align: center; border-radius: 10px; "
                        + "margin-bottom: 24px; }"
        );

        html.append(
                ".content { background: #f8f9fa; "
                        + "padding: 24px; border-radius: 8px; }"
        );

        html.append(
                ".row { padding: 6px 0; }"
        );

        html.append(
                ".label { font-weight: bold; }"
        );

        html.append(
                ".footer { text-align: center; "
                        + "margin-top: 24px; color: #666; "
                        + "font-size: 13px; }"
        );

        html.append(
                "</style></head><body>"
        );

        html.append(
                "<div class='container'>"
        );

        html.append(
                "<div class='header'><h2>"
                        + (
                        overdue
                                ? "⚠️ Rent Overdue"
                                : "⏰ Rent Pending"
                )
                        + "</h2></div>"
        );

        html.append(
                "<div class='content'>"
        );


        if (overdue) {

            html.append(
                    "<p>Rent for this property is "
                            + "<strong>"
                            + daysOverdue
                            + " day(s) overdue"
                            + "</strong>.</p>"
            );

        } else {

            html.append(
                    "<p>Rent for this property is "
                            + "currently pending.</p>"
            );
        }


        html.append(
                "<div class='row'>"
                        + "<span class='label'>Property:</span> "
                        + property.getAddress()
                        + "</div>"
        );

        html.append(
                "<div class='row'>"
                        + "<span class='label'>Tenant:</span> "
                        + (
                        property.getTenantName() != null
                                ? property.getTenantName()
                                : "N/A"
                )
                        + "</div>"
        );

        html.append(
                "<div class='row'>"
                        + "<span class='label'>Amount:</span> £"
                        + (
                        property.getRentAmount() != null
                                ? String.format(
                                "%.2f",
                                property.getRentAmount()
                        )
                                : "N/A"
                )
                        + "</div>"
        );


        if (property.getTenantPhone() != null
                && !property.getTenantPhone().isBlank()) {

            html.append(
                    "<div class='row'>"
                            + "<span class='label'>Phone:</span> "
                            + property.getTenantPhone()
                            + "</div>"
            );
        }


        html.append("</div>");

        html.append(
                "<div class='footer'>"
                        + "This is an automated message from "
                        + "your Property Management System"
                        + "</div>"
        );

        html.append(
                "</div></body></html>"
        );

        return html.toString();
    }


    // ============================================================
    // CAN SEND?
    // ============================================================

    private boolean canSend() {

        if (!notificationsEnabled) {
            return false;
        }


        if (landlordEmail == null
                || landlordEmail.isBlank()) {

            System.out.println(
                    "Skipping email - "
                            + "landlord.email not configured"
            );

            return false;
        }


        if (brevoApiKey == null
                || brevoApiKey.isBlank()) {

            System.out.println(
                    "Skipping email - "
                            + "BREVO_API_KEY not configured"
            );

            return false;
        }


        if (brevoFromEmail == null
                || brevoFromEmail.isBlank()) {

            System.out.println(
                    "Skipping email - "
                            + "BREVO_FROM_EMAIL not configured"
            );

            return false;
        }


        return true;
    }


    // ============================================================
    // CERTIFICATE EXPIRY HTML
    // ============================================================

    private String buildExpiryHtml(
            Property property,
            Certificate certificate,
            int days,
            boolean expired) {

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<!DOCTYPE html><html><head><style>"
        );

        html.append(
                "body { font-family: Arial, sans-serif; "
                        + "line-height: 1.6; color: #333; }"
        );

        html.append(
                ".container { max-width: 600px; "
                        + "margin: 0 auto; padding: 20px; }"
        );

        html.append(
                ".header { background: "
                        + (
                        expired
                                ? "#EF4444"
                                : "#F59E0B"
                )
                        + "; color: white; padding: 24px; "
                        + "text-align: center; border-radius: 10px; "
                        + "margin-bottom: 24px; }"
        );

        html.append(
                ".content { background: #f8f9fa; "
                        + "padding: 24px; border-radius: 8px; }"
        );

        html.append(
                ".row { padding: 6px 0; }"
        );

        html.append(
                ".label { font-weight: bold; }"
        );

        html.append(
                ".footer { text-align: center; "
                        + "margin-top: 24px; color: #666; "
                        + "font-size: 13px; }"
        );

        html.append(
                "</style></head><body>"
        );

        html.append(
                "<div class='container'>"
        );

        html.append(
                "<div class='header'><h2>"
                        + (
                        expired
                                ? "⚠️ Certificate Expired"
                                : "⏰ Certificate Expiring Soon"
                )
                        + "</h2></div>"
        );

        html.append(
                "<div class='content'>"
        );


        if (expired) {

            html.append(
                    "<p><strong>"
                            + certificate.getCertificateType()
                            + "</strong> for this property "
                            + "expired <strong>"
                            + days
                            + " day(s) ago"
                            + "</strong>.</p>"
            );

        } else {

            html.append(
                    "<p><strong>"
                            + certificate.getCertificateType()
                            + "</strong> for this property "
                            + "expires in <strong>"
                            + days
                            + " day(s)"
                            + "</strong>.</p>"
            );
        }


        html.append(
                "<div class='row'>"
                        + "<span class='label'>Property:</span> "
                        + property.getAddress()
                        + "</div>"
        );

        html.append(
                "<div class='row'>"
                        + "<span class='label'>Certificate:</span> "
                        + certificate.getCertificateType()
                        + "</div>"
        );


        if (certificate.getExpiryDate() != null) {

            html.append(
                    "<div class='row'>"
                            + "<span class='label'>Expiry Date:</span> "
                            + certificate.getExpiryDate()
                            + "</div>"
            );
        }


        html.append(
                "<p style='margin-top: 16px;'>"
                        + "Please arrange a renewal as soon as possible."
                        + "</p>"
        );

        html.append("</div>");

        html.append(
                "<div class='footer'>"
                        + "This is an automated message from "
                        + "your Property Management System"
                        + "</div>"
        );

        html.append(
                "</div></body></html>"
        );

        return html.toString();
    }
}