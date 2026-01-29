package com.example.procurement.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class BudgetHead {
    @Id @GeneratedValue private Long id;
    private String name; // Maintenance, Capex, Misc.
    private BigDecimal annualBudget;
    private BigDecimal utilized;
    @ManyToOne private Society society;
}

