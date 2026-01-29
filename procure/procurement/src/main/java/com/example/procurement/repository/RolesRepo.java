package com.example.procurement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.procurement.entity.Role;



@Repository
public interface RolesRepo extends JpaRepository<Role,Integer>
{
        List<Role> findByNameStartingWithIgnoreCase(String module);
}
