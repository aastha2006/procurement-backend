package com.example.procurement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Department {
    @Id @GeneratedValue private Long id;
    private String name; // e.g. STP, Security, Maintenance
    @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code; 
       @Column(name = "department_head", length = 100)
    private String departmentHead;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;
    @ManyToOne private Society society;
}

