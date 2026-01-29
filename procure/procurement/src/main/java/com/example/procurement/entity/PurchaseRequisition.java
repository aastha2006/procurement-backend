package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "purchase_requisition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PurchaseRequisition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String prNumber;

  private String department;

  private Long requestedBy;

  private String description; // Header description

  @Enumerated(EnumType.STRING)
  private PRStatus status = PRStatus.RAISED;

  private String approvedBy;

  private LocalDateTime approvedOn;

  private String budgetHead;

  private Instant createdAt = Instant.now();

  @JsonManagedReference
  @OneToMany(mappedBy = "pr", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private java.util.Set<PRItem> items;
}
