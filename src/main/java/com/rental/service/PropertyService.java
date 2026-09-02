package com.rental.service;

import com.rental.model.Property;
import com.rental.model.TenantHistory;
import com.rental.repository.PropertyRepository;
import com.rental.repository.TenantHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Mirrors the frontend's isRentPending/getDaysOverdue logic (index.html) - calculates
     * from the FIRST unpaid due date after the last payment, not just the current month's
     * due date, so a tenant who missed multiple months shows the correct total days overdue.
     */
    public boolean isRentPending(Property property) {
        if (!"Rented".equals(property.getStatus()) || property.getRentDueDay() == null) {
            return false;
        }

        String lastPaidStr = property.getLastPaidDate();
        if (lastPaidStr == null || lastPaidStr.isBlank()) {
            return true;
        }

        LocalDate firstUnpaidDueDate = firstUnpaidDueDateAfter(LocalDate.parse(lastPaidStr), property.getRentDueDay());
        return !firstUnpaidDueDate.isAfter(LocalDate.now());
    }

    public int getDaysOverdue(Property property) {
        if (property.getRentDueDay() == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        String lastPaidStr = property.getLastPaidDate();

        LocalDate firstUnpaidDueDate;
        if (lastPaidStr == null || lastPaidStr.isBlank()) {
            firstUnpaidDueDate = dueDateInMonth(today, property.getRentDueDay());
            if (firstUnpaidDueDate.isAfter(today)) {
                firstUnpaidDueDate = firstUnpaidDueDate.minusMonths(1);
            }
        } else {
            firstUnpaidDueDate = firstUnpaidDueDateAfter(LocalDate.parse(lastPaidStr), property.getRentDueDay());
        }

        if (firstUnpaidDueDate.isAfter(today)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(firstUnpaidDueDate, today);
    }

    public List<Property> getPendingRentProperties() {
        return getPropertiesByStatus("Rented").stream()
                .filter(this::isRentPending)
                .collect(Collectors.toList());
    }

    private LocalDate firstUnpaidDueDateAfter(LocalDate lastPaid, int rentDueDay) {
        LocalDate dueDateInPaymentMonth = dueDateInMonth(lastPaid, rentDueDay);
        if (lastPaid.getDayOfMonth() >= rentDueDay) {
            return dueDateInPaymentMonth.plusMonths(1);
        }
        return dueDateInPaymentMonth;
    }

    // Clamps the due day to the last day of the month for short months (e.g. due day 31 in February)
    private LocalDate dueDateInMonth(LocalDate referenceDate, int dueDay) {
        int clampedDay = Math.min(dueDay, referenceDate.lengthOfMonth());
        return LocalDate.of(referenceDate.getYear(), referenceDate.getMonth(), clampedDay);
    }
}