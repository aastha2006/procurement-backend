package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="purchase_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poNumber;

    @ManyToOne
    @JoinColumn(name="pr_id")
    private PurchaseRequisition pr;

    @ManyToOne
    @JoinColumn(name="vendor_id")
    private Vendor vendor;

    private Double totalAmount;
    private Double gst;

    @Enumerated(EnumType.STRING)
    private POStatus status = POStatus.ISSUED;

    private Instant createdAt;
}
