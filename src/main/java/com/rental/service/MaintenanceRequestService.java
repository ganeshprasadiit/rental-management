package com.rental.service;

import com.rental.model.MaintenanceRequest;
import com.rental.repository.MaintenanceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceRequestService {

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    public List<MaintenanceRequest> getAllRequests() {
        return maintenanceRequestRepository.findAll();
    }

    public Optional<MaintenanceRequest> getRequestById(Long id) {
        return maintenanceRequestRepository.findById(id);
    }

    public List<MaintenanceRequest> getRequestsByProperty(Long propertyId) {
        return maintenanceRequestRepository.findByPropertyIdOrderByReportedDateDesc(propertyId);
    }

    public List<MaintenanceRequest> getRequestsByStatus(String status) {
        return maintenanceRequestRepository.findByStatus(status);
    }

    public MaintenanceRequest saveRequest(MaintenanceRequest request) {
        return maintenanceRequestRepository.save(request);
    }

    public void deleteRequest(Long id) {
        maintenanceRequestRepository.deleteById(id);
    }

    public MaintenanceRequest resolveRequest(Long id, String resolvedBy, String notes) {
        Optional<MaintenanceRequest> optionalRequest = maintenanceRequestRepository.findById(id);
        if (optionalRequest.isPresent()) {
            MaintenanceRequest request = optionalRequest.get();
            request.setStatus("Resolved");
            request.setResolvedDate(LocalDateTime.now());
            request.setResolvedBy(resolvedBy);
            request.setResolutionNotes(notes);
            return maintenanceRequestRepository.save(request);
        }
        return null;
    }
}