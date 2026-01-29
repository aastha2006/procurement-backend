package com.example.procurement.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.procurement.DTO.InviteSupplierRequest;
import com.example.procurement.entity.Vendor;
import com.example.procurement.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierInviteService {

    private final VendorRepository vendorRepo;
    //private final EmailService emailService;

    public void sendInvitation(InviteSupplierRequest invite) {
        // Check if already exists
        Optional<Vendor> existing = vendorRepo.findByEmail(invite.getEmail());
        if (existing.isPresent()) return;

        // Create temp vendor record
        Vendor vendor = new Vendor();
        vendor.setCompanyName(invite.getCompanyName());
        vendor.setFullName(invite.getName());
        vendor.setEmail(invite.getEmail());
        vendor.setPhone(invite.getPhone());
        vendor.setVerified(false);
        vendor.setInvitedOn(LocalDateTime.now());

        vendorRepo.save(vendor);

        // Send invite email
        String link = "https://yourapp.com/vendor/register?email=" + invite.getEmail();
        String subject = "Invitation to Register as Supplier";
        String message = "Hello " + invite.getName() + ",\n\n"
                + "You have been invited to submit a quotation for our procurement system. "
                + "Please register using the following link:\n" + link;

       // emailService.sendEmail(invite.getEmail(), subject, message);
    }
}

