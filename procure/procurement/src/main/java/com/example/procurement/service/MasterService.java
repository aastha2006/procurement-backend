package com.example.procurement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.procurement.entity.Department;
import com.example.procurement.entity.Group;
import com.example.procurement.repository.DepartmentRepo;
import com.example.procurement.repository.GroupsRepo;
import com.example.procurement.repository.RolesRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MasterService {

    @Autowired
    private RolesRepo rolesRepo;

    @Autowired
    private DepartmentRepo deptRepo;

    @Autowired
    private GroupsRepo groupsRepo;

    public Department create(Department department) {

        if (deptRepo.existsByCode(department.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }

        // Ensure ID is not forced from request
        department.setId(null);

        return deptRepo.save(department);
    }

    /**
     * Get All Departments
     */
    public List<Department> getAll() {
        return deptRepo.findAll();
    }

    /**
     * Get Department by ID
     */
    public Department getById(Long id) {
        return deptRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id " + id));
    }

    public Group create(Group group) {

        if (groupsRepo.existsByCode(group.getCode())) {
            throw new IllegalArgumentException("Group code already exists");
        }

        // Avoid overriding existing record
        group.setId(null);

        return groupsRepo.save(group);
    }

    /**
     * Get All Groups
     */
    public List<Group> getAllGroup() {
        return groupsRepo.findAll();
    }

    /**
     * Get Group by ID
     */
    public Group getGroupById(Long id) {
        return groupsRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id " + id));
    }
}
