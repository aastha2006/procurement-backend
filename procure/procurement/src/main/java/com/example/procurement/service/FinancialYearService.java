package com.example.procurement.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.procurement.entity.FinancialYear;
import com.example.procurement.repository.FinancialYearRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialYearService {

    private final FinancialYearRepository repo;

    /**
     * ➕ Create Financial Year
     */
    public FinancialYear create(FinancialYear fy) {

        if (repo.existsByYear(fy.getYear())) {
            throw new IllegalArgumentException("Financial year already exists");
        }

        fy.setId(null);
        fy.setActive(false);
        fy.setClosed(false);

        return repo.save(fy);
    }

    /**
     * 📋 Get All Financial Years
     */
    public List<FinancialYear> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "startDate"));
    }

    /**
     * 🔍 Get By ID
     */
    public FinancialYear getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Financial year not found"));
    }

    /**
     * ⭐ Set Active Financial Year
     */
    public FinancialYear setActive(Long id) {

        FinancialYear fy = getById(id);

        if (fy.getClosed()) {
            throw new IllegalStateException("Closed financial year cannot be activated");
        }

        // Deactivate existing active year
        repo.findByActiveTrue()
                .ifPresent(activeFy -> {
                    activeFy.setActive(false);
                    repo.save(activeFy);
                });

        fy.setActive(true);
        return repo.save(fy);
    }

    /**
     * 🔒 Close Financial Year
     */
    public FinancialYear closeYear(Long id) {

        FinancialYear fy = getById(id);

        fy.setClosed(true);
        fy.setActive(false);

        return repo.save(fy);
    }
}

