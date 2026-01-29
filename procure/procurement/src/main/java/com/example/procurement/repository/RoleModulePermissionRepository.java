package com.example.procurement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.procurement.entity.RoleModulePermission;

public interface RoleModulePermissionRepository
        extends JpaRepository<RoleModulePermission, Long> {

    List<RoleModulePermission> findByRoleId(Long roleId);

    @Modifying
@Query("delete from RoleModulePermission p where p.role.id = :roleId")
    void deleteByRoleId(Long roleId);
}

