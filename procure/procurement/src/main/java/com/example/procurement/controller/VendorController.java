package com.example.procurement.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.entity.Vendor;
import com.example.procurement.service.VendorService;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    private final VendorService vendorService;
    public VendorController(VendorService vendorService) { this.vendorService = vendorService; }

    @PostMapping
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        return ResponseEntity.ok(vendorService.createVendor(vendor));
    }
@GetMapping("/approved")
public ResponseEntity<Page<Vendor>> approvedVendors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize
) {
    return ResponseEntity.ok(vendorService.getApprovedVendors(page, pageSize));
}
@GetMapping("/pending")
public ResponseEntity<Page<Vendor>> pendingVendors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize
) {
    return ResponseEntity.ok(vendorService.getpendingVendors(page, pageSize));
}

    @PostMapping("/{id}/approve")
    public ResponseEntity<Vendor> approve(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.approveVendor(id));
    }

    @GetMapping("/{id}")
public ResponseEntity<Vendor> showVendor(
        @PathVariable Long id
) {
    return ResponseEntity.ok(vendorService.getVendor(id));
}
}
