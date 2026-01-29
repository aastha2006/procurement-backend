package com.example.procurement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.entity.RoleModulePermission;
import com.example.procurement.service.RolePermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService service;

    /**
     * 🔍 Load permissions (Edit Permissions popup)
     */
    @GetMapping
    public List<RoleModulePermission> getPermissions(
            @PathVariable Long roleId) {
        return service.getPermissions(roleId);
    }

    /**
     * 💾 Save permissions (Save Permissions button)
     */
    @PutMapping
    public ResponseEntity<Void> savePermissions(
            @PathVariable Long roleId,
            @RequestBody List<RoleModulePermission> permissions) {

        service.savePermissions(roleId, permissions);
        return ResponseEntity.ok().build();
    }
}

