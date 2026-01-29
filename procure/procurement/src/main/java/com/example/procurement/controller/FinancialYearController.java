package com.example.procurement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.entity.FinancialYear;
import com.example.procurement.service.FinancialYearService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/financial-years")
@RequiredArgsConstructor
public class FinancialYearController {

    private final FinancialYearService service;

    /**
     * ➕ Add Financial Year
     */
    @PostMapping
    public ResponseEntity<FinancialYear> create(
            @RequestBody FinancialYear fy) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(fy));
    }

    /**
     * 📋 Get All Financial Years
     */
    @GetMapping
    public ResponseEntity<List<FinancialYear>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * 🔍 Get Financial Year By ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FinancialYear> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * ⭐ Set Active Financial Year
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<FinancialYear> setActive(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.setActive(id));
    }

    /**
     * 🔒 Close Financial Year
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<FinancialYear> closeYear(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.closeYear(id));
    }
}

