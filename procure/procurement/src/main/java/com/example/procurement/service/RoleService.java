package com.example.procurement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.procurement.entity.Role;
import com.example.procurement.repository.RolesRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RolesRepo roleRepo;

    public Role create(Role role) {
        role.setId(null);
        return roleRepo.save(role);
    }

    public List<Role> getAll() {
        return roleRepo.findAll();
    }

    public Role getById(Long id) {
        return roleRepo.findById(id.intValue())
                .orElseThrow(() ->
                        new EntityNotFoundException("Role not found"));
    }
}

