package com.example.procurement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class VendorCategory {
    @Id @GeneratedValue private Long id;
    private String name; // e.g. Housekeeping, Electrical, Plumbing
}

