package com.rental.controller;

import com.rental.model.MaintenanceRequest;
import com.rental.service.MaintenanceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceRequestController {

    @Autowired
    private MaintenanceRequestService maintenanceRequestService;

    @GetMapping
    public List<MaintenanceRequest> getAllRequests() {
        return maintenanceRequestService.getAllRequests();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> getRequestById(@PathVariable Long id) {
        return maintenanceRequestService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/property/{propertyId}")
    public List<MaintenanceRequest> getRequestsByProperty(@PathVariable Long propertyId) {
        return maintenanceRequestService.getRequestsByProperty(propertyId);
    }

    @GetMapping("/status/{status}")
    public List<MaintenanceRequest> getRequestsByStatus(@PathVariable String status) {
        return maintenanceRequestService.getRequestsByStatus(status);
    }

    @PostMapping
    public MaintenanceRequest createRequest(@RequestBody MaintenanceRequest request) {
        return maintenanceRequestService.saveRequest(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> updateRequest(@PathVariable Long id, @RequestBody MaintenanceRequest request) {
        return maintenanceRequestService.getRequestById(id)
                .map(existingRequest -> {
                    request.setId(id);
                    return ResponseEntity.ok(maintenanceRequestService.saveRequest(request));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<MaintenanceRequest> resolveRequest(
            @PathVariable Long id,
            @RequestParam String resolvedBy,
            @RequestParam(required = false) String notes) {
        MaintenanceRequest resolved = maintenanceRequestService.resolveRequest(id, resolvedBy, notes);
        if (resolved != null) {
            return ResponseEntity.ok(resolved);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        if (maintenanceRequestService.getRequestById(id).isPresent()) {
            maintenanceRequestService.deleteRequest(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}