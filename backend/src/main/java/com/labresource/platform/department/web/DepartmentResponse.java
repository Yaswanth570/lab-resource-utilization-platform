package com.labresource.platform.department.web;

import com.labresource.platform.department.Department;

import java.time.Instant;

public class DepartmentResponse {

    private Long id;
    private Long institutionId;
    private String name;
    private String code;
    private Long headUserId;
    private String billingAccountCode;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public DepartmentResponse() {
    }

    public static DepartmentResponse fromEntity(Department dept) {
        if (dept == null) {
            return null;
        }
        DepartmentResponse res = new DepartmentResponse();
        res.setId(dept.getId());
        res.setInstitutionId(dept.getInstitution() != null ? dept.getInstitution().getId() : null);
        res.setName(dept.getName());
        res.setCode(dept.getCode());
        res.setHeadUserId(dept.getHeadUser() != null ? dept.getHeadUser().getId() : null);
        res.setBillingAccountCode(dept.getBillingAccountCode());
        res.setActive(dept.isActive());
        res.setCreatedAt(dept.getCreatedAt());
        res.setUpdatedAt(dept.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getHeadUserId() {
        return headUserId;
    }

    public void setHeadUserId(Long headUserId) {
        this.headUserId = headUserId;
    }

    public String getBillingAccountCode() {
        return billingAccountCode;
    }

    public void setBillingAccountCode(String billingAccountCode) {
        this.billingAccountCode = billingAccountCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
