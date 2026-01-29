package com.example.procurement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.procurement.entity.FinancialYear;

public interface FinancialYearRepository extends JpaRepository<FinancialYear, Long> {

    Optional<FinancialYear> findByActiveTrue();

    boolean existsByYear(String year);
}

