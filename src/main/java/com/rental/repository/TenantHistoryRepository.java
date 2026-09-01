package com.rental.repository;

import com.rental.model.TenantHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TenantHistoryRepository extends JpaRepository<TenantHistory, Long> {
    List<TenantHistory> findByPropertyIdOrderByMoveOutDateDesc(Long propertyId);
}
