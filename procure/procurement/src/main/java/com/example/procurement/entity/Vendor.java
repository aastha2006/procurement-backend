package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String fullName;
     private String companyName;

    private String gst;
    private String pan;
    @Column(name = "bank_account")
    private String bankAccount;
    @Column(name = "bank_ifsc")
    private String bankIfsc;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;

    private boolean verified;
    @Enumerated(EnumType.STRING)
    private VendorStatus status = VendorStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime invitedOn;
}
