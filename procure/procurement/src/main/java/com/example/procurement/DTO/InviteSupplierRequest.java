package com.example.procurement.DTO;

import lombok.Data;

@Data
public class InviteSupplierRequest {
    private String name;
    private String email;
    private String phone;
    private String companyName;
}

