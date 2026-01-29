package com.example.procurement.DTO;

import java.time.LocalDate;

import com.example.procurement.entity.Enum.MemberRole;

import lombok.Data;

@Data
public class SocietyMemberRequest {

    // Personal Information
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // Residence Details
    private String block;
    private String flatNumber;
    private String ownershipType;   // "OWNER" / "TENANT"
    private LocalDate moveInDate;

    // Verification (if tenant)
    private String propertyOwnerName;
    private String propertyOwnerContact;

    // Registration Reason
    private String reasonForRegistration;

    // Member Role (committee etc)
    private MemberRole role;

    private Long societyId;
}


