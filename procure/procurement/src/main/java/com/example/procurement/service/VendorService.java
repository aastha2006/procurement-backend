package com.example.procurement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.procurement.config.PasswordGenerator;
import com.example.procurement.entity.AppUser;
import com.example.procurement.entity.Group;
import com.example.procurement.entity.Vendor;
import com.example.procurement.entity.VendorStatus;
import com.example.procurement.repository.GroupsRepo;
import com.example.procurement.repository.UserLoginRepo;
import com.example.procurement.repository.VendorRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class VendorService {

    @Autowired
    PasswordGenerator passwordGenerator;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    GroupsRepo groupRepo;
    @Autowired
    UserLoginRepo userLoginRepo;

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public Vendor createVendor(Vendor vendor) {
        vendor.setCreatedAt(LocalDateTime.now());
        if (vendor.getStatus() == null)
            vendor.setStatus(VendorStatus.PENDING);
        return vendorRepository.save(vendor);
    }

    public Vendor approveVendor(Long id) {
        Vendor v = vendorRepository.findById(id).orElseThrow(() -> new RuntimeException("Vendor not found"));
        v.setStatus(VendorStatus.APPROVED);
        Vendor savedVendor = vendorRepository.save(v);

        // Check if login already exists
        if (userLoginRepo.findByUsernameAndLoginType(v.getEmail(), "Supplier") == null) {
            try {
                // Create User Login
                AppUser newVendor = new AppUser();
                newVendor.setEmail(v.getEmail());
                newVendor.setIdVen(v);
                newVendor.setLoginType("Supplier");
                newVendor.setUsername(v.getEmail());
                newVendor.setEnabled(true);
                newVendor.setDt_enable(LocalDate.now());

                // Set default password "vendor123" for testing/production stability
                newVendor.setPassword(passwordEncoder.encode("vendor123"));

                // Assign "Supplier" Group (create if missing to avoid blocking)
                Group grp = groupRepo.findFirstByName("Supplier")
                        .orElseGet(() -> {
                            Group g = new Group();
                            g.setName("Supplier");
                            return groupRepo.save(g);
                        });
                newVendor.setIdGroup(grp);

                userLoginRepo.save(newVendor);
                System.out.println("Created Login for Vendor: " + v.getEmail());
            } catch (Exception e) {
                System.err.println("Error creating login for vendor: " + e.getMessage());
                // Don't fail the approval if login creation fails, but log it
            }
        }

        return savedVendor;
    }

    public Page<Vendor> getApprovedVendors(int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return vendorRepository.findByStatus(VendorStatus.APPROVED, pageable);
    }

    public Page<Vendor> getpendingVendors(int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return vendorRepository.findByStatus(VendorStatus.PENDING, pageable);
    }

    @Transactional
    public Vendor saveSignupSupplier(Vendor vendorSignup) {
        // 1️⃣ Save Vendor
        Vendor savedVendor = vendorRepository.save(vendorSignup);

        // 2️⃣ Generate secure password
        String securePassword = passwordGenerator.createSecurePassword();
        if (securePassword == null || securePassword.isEmpty()) {
            throw new IllegalStateException("Failed to generate secure password");
        }

        // 3️⃣ Create User Login
        AppUser newVendor = new AppUser();
        newVendor.setEmail(savedVendor.getEmail());
        newVendor.setIdVen(savedVendor);
        newVendor.setLoginType("Supplier");
        // newVendor.setTypePassword("Temporary");
        newVendor.setUsername(savedVendor.getEmail());
        newVendor.setEnabled(true);
        newVendor.setPassword(passwordEncoder.encode(securePassword));
        newVendor.setDt_enable(LocalDate.now());
        System.out.println(securePassword);
        // 4️⃣ Assign Group
        Group grp = groupRepo.findFirstByName("Supplier")
                .orElseThrow(() -> new EntityNotFoundException("Supplier group not found"));
        newVendor.setIdGroup(grp);

        // 5️⃣ Save Login
        userLoginRepo.save(newVendor);

        // Details vendetails = new Details();
        // vendetails.setName(savedVendor.getName());
        // vendetails.setRecipient(savedVendor.getEmail());
        // // 6️⃣ Send Email with plain password (not encoded)
        // sendEmailToVendorCred(
        // vendetails,
        // newVendor.getUsername(),
        // securePassword
        // );

        return savedVendor;
    }

    public Vendor getVendor(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

}
