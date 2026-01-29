package com.example.procurement.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private Double amount;
    private String paymentMode;
    private String reference;
    private Instant paidOn;
    private String invoiceNumber;
}
