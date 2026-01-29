package com.example.procurement.entity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        // this.createdBy = // You can set createdBy here if you have user context
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
        // this.updatedBy = // You can set updatedBy here if you have user context
    }

    // Getters and Setters
    public Integer getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }
    public Integer getUpdatedBy() {
        return updatedBy;
    }
    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
}
