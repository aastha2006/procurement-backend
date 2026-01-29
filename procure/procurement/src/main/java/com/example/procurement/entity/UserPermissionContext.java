package com.example.procurement.entity;

import java.util.List;

public class UserPermissionContext {

    private List<Integer> allowedEntityIds;
    private List<Integer> allowedCostCenterIds;
    private List<Integer> allowedLocationIds;
  

    public UserPermissionContext() {}

    public UserPermissionContext(List<Integer> allowedEntityIds,
                                 List<Integer> allowedCostCenterIds,
                                 List<Integer> allowedLocationIds) {
        this.allowedEntityIds = allowedEntityIds;
        this.allowedCostCenterIds = allowedCostCenterIds;
       
        this.allowedLocationIds = allowedLocationIds;
       
    }

    // Getters and Setters

    public List<Integer> getAllowedEntityIds() {
        return allowedEntityIds;
    }

    public void setAllowedDivisionIds(List<Integer> allowedEntityIds) {
        this.allowedEntityIds = allowedEntityIds;
    }

    public List<Integer> getAllowedCostCenterIds() {
        return allowedCostCenterIds;
    }

    public void setAllowedCostCenterIds(List<Integer> allowedCostCenterIds) {
        this.allowedCostCenterIds = allowedCostCenterIds;
    }

   

    public List<Integer> getAllowedLocationIds() {
        return allowedLocationIds;
    }

    public void setAllowedLocationIds(List<Integer> allowedLocationIds) {
        this.allowedLocationIds = allowedLocationIds;
    }

   
}

