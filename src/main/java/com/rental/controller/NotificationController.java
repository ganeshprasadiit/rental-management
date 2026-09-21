package com.rental.controller;

import com.rental.model.Property;
import com.rental.service.EmailService;
import com.rental.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-daily-report")
    public ResponseEntity<Map<String, Object>> sendDailyReport() {
        List<Property> pending = propertyService.getPendingRentProperties();

        Map<String, Object> response = new HashMap<>();
        response.put("count", pending.size());

        if (pending.isEmpty()) {
            response.put("success", true);
            response.put("emailSent", false);
            return ResponseEntity.ok(response);
        }

        boolean sent = emailService.sendRentPendingDailyReport(pending);
        response.put("success", sent);
        response.put("emailSent", sent);
        if (!sent) {
            response.put("message", "Could not send email - check landlord.email and mail credentials are configured correctly, and check server logs for details");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/property/{propertyId}/send-reminder")
    public ResponseEntity<Map<String, Object>> sendPropertyReminder(@PathVariable Long propertyId) {
        Optional<Property> propertyOpt = propertyService.getPropertyById(propertyId);
        Map<String, Object> response = new HashMap<>();

        if (propertyOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Property not found");
            return ResponseEntity.status(404).body(response);
        }

        Property property = propertyOpt.get();
        int daysOverdue = propertyService.getDaysOverdue(property);
        boolean sent = emailService.sendRentPendingNotification(property, daysOverdue);

        response.put("success", sent);
        if (!sent) {
            response.put("message", "Could not send email - check landlord.email and mail credentials are configured correctly, and check server logs for details");
        }
        return ResponseEntity.ok(response);
    }
}
