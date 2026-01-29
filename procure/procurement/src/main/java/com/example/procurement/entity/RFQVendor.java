package com.example.procurement.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rfq_vendor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RFQVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "rfq_id")
    private RFQ rfq;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private LocalDateTime invitedOn = LocalDateTime.now();
}

