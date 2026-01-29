package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="po_id")
    private PurchaseOrder po;

    @ManyToOne
    @JoinColumn(name="vendor_id")
    private Vendor vendor;

    private String invoiceNumber;
    private LocalDate invoiceDate;
    private Double amount;
    private Double gst;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.SUBMITTED;

    private Instant createdAt;
}
