package com.example.procurement.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "financial_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_label", length = 10, nullable = false, unique = true)
    private String year;            // 2024-25

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;     // 2024-04-01

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;       // 2025-03-31

    @Column(nullable = false)
    private Boolean active = false;  // Only ONE can be active

    @Column(nullable = false)
    private Boolean closed = false;  // Closed year cannot be active
}

