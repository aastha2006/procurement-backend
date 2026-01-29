package com.example.procurement.DTO;

import lombok.Data;

@Data
public class SelectVendorRequest {
    private Long vendorId;
    private Long selectedBy;   // who selected the vendor (user/employee)
}

