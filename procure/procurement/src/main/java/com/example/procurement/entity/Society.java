package com.example.procurement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Society {
    @Id 
    @GeneratedValue private Long id;
    private String name;
    private String address;
    private String registrationNumber;
    private String gstNumber;
    private String bankAccount;
}

