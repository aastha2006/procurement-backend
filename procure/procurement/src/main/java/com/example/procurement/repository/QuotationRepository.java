package com.example.procurement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.procurement.entity.Quotation;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

       @Query("SELECT MAX(q.revisionNo) FROM Quotation q WHERE q.rfq.id = :rfqId AND q.vendor.id = :vendorId")
    Integer findLastRevision(@Param("rfqId") Long rfqId, @Param("vendorId") Long vendorId);

    List<Quotation> findByRfqIdAndVendorIdOrderByRevisionNoAsc(Long rfqId, Long vendorId);

    @Query("""
    SELECT q 
    FROM Quotation q
    WHERE q.rfq.id = :rfqId
    AND q.revisionNo = (
        SELECT MAX(q2.revisionNo)
        FROM Quotation q2
        WHERE q2.rfq.id = :rfqId AND q2.vendor.id = q.vendor.id
    )
""")
List<Quotation> getLatestVendorQuotes(@Param("rfqId") Long rfqId);

}
