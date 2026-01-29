package com.example.procurement.repository;

import com.example.procurement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendor" })
    java.util.List<PurchaseOrder> findAll();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "pr", "pr.items", "vendor" })
    @org.springframework.data.jpa.repository.Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id")
    java.util.Optional<PurchaseOrder> findByIdWithRelations(Long id);
}
