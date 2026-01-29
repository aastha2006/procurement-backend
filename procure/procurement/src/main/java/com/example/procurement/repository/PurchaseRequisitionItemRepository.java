package com.example.procurement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.procurement.entity.PRItem;

public interface PurchaseRequisitionItemRepository extends JpaRepository<PRItem, Long> {
    
   
}
