package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="goods_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceipt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="po_id")
    private PurchaseOrder po;

    private String receivedBy;
    private Instant receivedOn;
    private String note;

    @Enumerated(EnumType.STRING)
    private GRNStatus status = GRNStatus.RECEIVED;
}
