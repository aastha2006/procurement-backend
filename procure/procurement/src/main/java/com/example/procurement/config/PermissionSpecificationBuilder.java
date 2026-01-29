package com.example.procurement.config;

import jakarta.persistence.criteria.*;


import org.springframework.data.jpa.domain.Specification;

import com.example.procurement.entity.UserPermissionContext;

import java.util.ArrayList;
import java.util.List;

public class PermissionSpecificationBuilder {

    public static <T> Specification<T> buildFilters(UserPermissionContext context, Class<T> entityClass) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Entity
            if (context.getAllowedEntityIds() != null && !context.getAllowedEntityIds().isEmpty()
                    && hasAttribute(root, "entities")) {
                predicates.add(root.get("entities").get("id").in(context.getAllowedEntityIds()));

                       
            }

            // Cost Center
            if (context.getAllowedCostCenterIds() != null && !context.getAllowedCostCenterIds().isEmpty()
                    && hasAttribute(root, "costCenter")) {
                predicates.add(root.get("costCenter").get("id").in(context.getAllowedCostCenterIds()));
            }

            // Department
            // if (context.getAllowedDepartmentIds() != null && !context.getAllowedDepartmentIds().isEmpty()
            //         && hasAttribute(root, "department")) {
            //     predicates.add(root.get("department").get("id").in(context.getAllowedDepartmentIds()));
            // }

            // Location
            if (context.getAllowedLocationIds() != null && !context.getAllowedLocationIds().isEmpty()
                    && hasAttribute(root, "location")) {
                predicates.add(root.get("location").get("id").in(context.getAllowedLocationIds()));
            }

            // ItemGroup
            // if (context.getAllowedItemGroupIds() != null && !context.getAllowedItemGroupIds().isEmpty()
            //         && hasAttribute(root, "itemGroup")) {
            //     predicates.add(root.get("itemGroup").get("id").in(context.getAllowedItemGroupIds()));
            // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Utility method to check if the entity has a given attribute
    private static boolean hasAttribute(Root<?> root, String attributeName) {
        try {
            root.get(attributeName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

