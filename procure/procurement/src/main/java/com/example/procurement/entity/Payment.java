package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="invoice_id")
    private Invoice invoice;

    private Double amount;
    private Instant paidOn;
    private String paymentMode;
    private String reference;
}
