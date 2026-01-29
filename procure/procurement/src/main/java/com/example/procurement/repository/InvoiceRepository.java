package com.example.procurement.repository;

import com.example.procurement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<com.example.procurement.entity.Invoice, Long> {

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "po", "po.pr", "po.pr.items", "vendor" })
    java.util.List<com.example.procurement.entity.Invoice> findAll();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "po", "po.pr", "po.pr.items", "vendor",
            "po.vendor" })
    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i WHERE i.id = :id")
    java.util.Optional<com.example.procurement.entity.Invoice> findByIdWithRelations(Long id);
}
