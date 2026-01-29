package com.example.procurement.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.procurement.DTO.DashboardSummaryDTO;
import com.example.procurement.repository.PurchaseRequisitionRepository;
import com.example.procurement.repository.VendorRepository;

@Service
public class DashboardService {

    @Autowired
    private PurchaseRequisitionRepository prRepo;

    @Autowired
    private VendorRepository vendorRepo;

    public DashboardSummaryDTO getDashboardSummary() {

       // Count of pending PRs
        long pendingRequisitions = prRepo.countPendingPRs();

        // Today's date range
      ZoneId zone = ZoneId.systemDefault(); // or ZoneId.of("Asia/Kolkata")

    LocalDate today = LocalDate.now();

    Instant startOfDay = today.atStartOfDay(zone).toInstant();
    Instant endOfDay = today.atTime(LocalTime.MAX).atZone(zone).toInstant();


        // Count of PRs created today
        long pendingToday = prRepo.countTodayPendingPRs(startOfDay, endOfDay);

        // Vendors
        long activeVendors = vendorRepo.countActiveVendors();
        long onboardingVendors = vendorRepo.countOnboardingVendors();

        return new DashboardSummaryDTO(
                pendingRequisitions,
                pendingToday,
                activeVendors,
                onboardingVendors
        );
    }
}

