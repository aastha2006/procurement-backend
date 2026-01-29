package com.example.procurement.DTO;

import java.util.List;

import lombok.Data;

@Data
public class RFQRequest {
    private Long prId; // Purchase Request ID

    // IDs of onboarded vendors
    private List<Long> vendorIds;

    // For unregistered suppliers — we send them invite emails
    private List<InviteSupplierRequest> inviteSuppliers;
}

