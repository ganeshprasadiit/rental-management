//package com.rental.service;
//
//import com.rental.request.BrevoEmailRequest;
//import com.rental.request.BrevoRecipient;
//import com.rental.request.BrevoSender;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.util.List;
//
//@Service
//public class BrevoEmailService implements EmailService{
//
//
//        private final RestClient restClient;
//
//        private final String apiUrl;
//        private final String apiKey;
//        private final String senderEmail;
//        private final String senderName;
//
//        public BrevoEmailService(
//                @Value("${brevo.api.url}") String apiUrl,
//                @Value("${brevo.api.key}") String apiKey,
//                @Value("${brevo.sender.email}") String senderEmail,
//                @Value("${brevo.sender.name}") String senderName) {
//
//            this.apiUrl = apiUrl;
//            this.apiKey = apiKey;
//            this.senderEmail = senderEmail;
//            this.senderName = senderName;
//
//            this.restClient = RestClient.builder()
//                    .build();
//        }
//
//        @Override
//        public void sendEmail(
//                List<String> recipients,
//                String subject,
//                String htmlContent) {
//
//            List<BrevoRecipient> to = recipients.stream()
//                    .map(email -> new BrevoRecipient(email, null))
//                    .toList();
//
//            BrevoEmailRequest request = new BrevoEmailRequest();
//
//            request.setSender(
//                    new BrevoSender(senderName, senderEmail)
//            );
//
//            request.setTo(to);
//            request.setSubject(subject);
//            request.setHtmlContent(htmlContent);
//
//            restClient.post()
//                    .uri(apiUrl)
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                    .header("api-key", apiKey)
//                    .body(request)
//                    .retrieve()
//                    .toBodilessEntity();
//        }
//    }
//
