package com.rental.repository;

import com.rental.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByPropertyIdOrderByExpiryDateAsc(Long propertyId);
}
