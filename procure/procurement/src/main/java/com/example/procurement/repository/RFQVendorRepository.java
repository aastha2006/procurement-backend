package com.example.procurement.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.procurement.entity.RFQ;
import com.example.procurement.entity.RFQVendor;

public interface RFQVendorRepository extends JpaRepository<RFQVendor, Long> {

@Query("SELECT rv.rfq FROM RFQVendor rv WHERE rv.vendor.id = :vendorId")
    List<RFQ> findRfqsByVendorId(@Param("vendorId") Long vendorId);

boolean existsByRfqIdAndVendorId(Long rfqId, Long vendorId);

    

}
