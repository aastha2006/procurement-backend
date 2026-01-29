package com.example.procurement.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.procurement.entity.RFQ;

public interface RFQRepository extends JpaRepository<RFQ, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendors" })
    @Query("""
                SELECT rfq FROM RFQ rfq
                ORDER BY rfq.createdAt DESC
            """)
    List<RFQ> findRecentRFQs(Pageable pageable);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendors" })
    org.springframework.data.domain.Page<RFQ> findAll(Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendors" })
    @Query("SELECT rfq FROM RFQ rfq WHERE rfq.id = :id")
    java.util.Optional<RFQ> findByIdWithItemsAndVendors(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendors" })
    @Query("SELECT r FROM RFQ r JOIN r.vendors v WHERE v.vendor.id = :vendorId")
    List<RFQ> findRfqsByVendorId(Long vendorId);
}
