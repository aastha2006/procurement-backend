package com.example.procurement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Expense {
    @Id @GeneratedValue private Long id;
    private LocalDate date;
    private String reference; // PO / Invoice No.
    private BigDecimal amount;
    @ManyToOne private BudgetHead budgetHead;
    private String narration;
}

