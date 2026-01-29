package com.example.procurement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.procurement.entity.Role;
import com.example.procurement.entity.RoleModulePermission;
import com.example.procurement.repository.RoleModulePermissionRepository;
import com.example.procurement.repository.RolesRepo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService {

    private final RolesRepo roleRepo;
    private final RoleModulePermissionRepository permissionRepo;

    /**
     * Save / Update module permissions for a role
     */
    public void savePermissions(Long roleId,
                                List<RoleModulePermission> permissions) {

        Role role = roleRepo.findById(roleId.intValue())
                .orElseThrow(() ->
                        new EntityNotFoundException("Role not found"));

        // Remove existing permissions
        permissionRepo.deleteByRoleId(roleId);

        // Attach role & save new permissions
        for (RoleModulePermission p : permissions) {
            p.setId(null);
            p.setRole(role);
            permissionRepo.save(p);
        }
    }

    /**
     * Get permissions for role
     */
    public List<RoleModulePermission> getPermissions(Long roleId) {
        return permissionRepo.findByRoleId(roleId);
    }
}

