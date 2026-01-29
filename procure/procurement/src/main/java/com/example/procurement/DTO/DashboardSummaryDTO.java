package com.example.procurement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {

    private long pendingRequisitions;
    private long pendingRequisitionsToday;

    private long activeVendors;
    private long onboardingVendors;
}

