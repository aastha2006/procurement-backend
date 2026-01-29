package com.example.procurement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.procurement.entity.Vendor;
import com.example.procurement.entity.VendorStatus;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByName(String name);
    List<Vendor> findByStatus(VendorStatus status);
    Optional<Vendor> findByEmail(String email);
    Page<Vendor> findByStatus(VendorStatus sts, Pageable pageable);

     @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'APPROVED'")
    long countActiveVendors();

    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = 'PENDING'")
    long countOnboardingVendors();
}
