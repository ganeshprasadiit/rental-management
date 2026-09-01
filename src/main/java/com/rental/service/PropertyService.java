package com.rental.service;

import com.rental.model.Property;
import com.rental.model.TenantHistory;
import com.rental.repository.PropertyRepository;
import com.rental.repository.TenantHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findById(id);
    }

    public Property saveProperty(Property property) {
        archiveOutgoingTenantIfChanged(property);
        return propertyRepository.save(property);
    }

    public List<TenantHistory> getTenantHistory(Long propertyId) {
        return tenantHistoryRepository.findByPropertyIdOrderByMoveOutDateDesc(propertyId);
    }

    /**
     * If this is an update to an existing property and it previously had a tenant
     * who is now gone (status changed away from Rented, tenant name cleared, or
     * replaced with a different tenant), archive that outgoing tenant into
     * tenant_history before the new data overwrites it.
     */
    private void archiveOutgoingTenantIfChanged(Property incoming) {
        if (incoming.getId() == null) {
            return; // brand new property, nothing to archive
        }

        Optional<Property> existingOpt = propertyRepository.findById(incoming.getId());
        if (existingOpt.isEmpty()) {
            return;
        }

        Property existing = existingOpt.get();
        String oldTenantName = existing.getTenantName();

        if (oldTenantName == null || oldTenantName.trim().isEmpty()) {
            return; // no previous tenant to archive
        }

        boolean statusChangedAwayFromRented = !"Rented".equals(incoming.getStatus());
        boolean tenantCleared = incoming.getTenantName() == null || incoming.getTenantName().trim().isEmpty();
        boolean tenantReplaced = !tenantCleared && !oldTenantName.equals(incoming.getTenantName());

        if (statusChangedAwayFromRented || tenantCleared || tenantReplaced) {
            TenantHistory history = new TenantHistory();
            history.setPropertyId(existing.getId());
            history.setTenantName(existing.getTenantName());
            history.setTenantEmail(existing.getTenantEmail());
            history.setTenantPhone(existing.getTenantPhone());
            history.setRentAmount(existing.getRentAmount());
            history.setMoveInDate(existing.getTenantMoveInDate());
            history.setMoveOutDate(LocalDate.now().toString());
            tenantHistoryRepository.save(history);
        }
    }

    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }

    public List<Property> getPropertiesByStatus(String status) {
        return propertyRepository.findByStatus(status);
    }

    public List<Property> getPropertiesByType(String type) {
        return propertyRepository.findByType(type);
    }
}