package com.example.procurement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.procurement.entity.Department;

public interface DepartmentRepo extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    boolean existsByCode(String code);

    Optional<Department> findById(Long id);

}
