package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "rfq")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RFQ {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private String rfqNumber;

   @ManyToOne
   @JoinColumn(name = "pr_id")
   private PurchaseRequisition pr;

   @JsonManagedReference
   @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
   @ToString.Exclude
   @Builder.Default
   private List<RFQVendor> vendors = new ArrayList<>();

   private String status;
   private LocalDateTime createdAt;
   private Long selectedVendor;
   private Long selectedBy;
   private LocalDateTime selectedOn;

}
