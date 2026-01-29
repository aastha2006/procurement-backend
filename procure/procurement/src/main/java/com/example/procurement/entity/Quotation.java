package com.example.procurement.entity;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="quotation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quotation {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
 @JsonIgnore
  @JoinColumn(name="rfq_id")
  private RFQ rfq;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name="vendor_id")
  private Vendor vendor;

  @ManyToOne
    @JoinColumn(name = "item_id")
    private PRItem item;
    
  private Double price;
  private String deliveryTerms;
  private String warranty;
  private String paymentTerms;
 private Double qty;
 private Double total;

    private Integer revisionNo;
  private Instant createdAt;
}
