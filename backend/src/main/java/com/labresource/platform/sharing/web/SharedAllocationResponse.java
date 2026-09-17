package com.labresource.platform.sharing.web;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharedEquipmentAllocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public class SharedAllocationResponse {

    private Long id;
    private Long sharingAgreementId;
    private String sharingAgreementCode;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentAssetTag;
    private String equipmentCategoryName;
    private EquipmentStatus equipmentStatus;
    private Long ownerInstitutionId;
    private String ownerInstitutionName;
    private Long requestingInstitutionId;
    private String requestingInstitutionName;
    private BigDecimal customHourlyRate;

    @JsonProperty("isActive")
    private boolean isActive;
    private Instant createdAt;

    public SharedAllocationResponse() {
    }

    public static SharedAllocationResponse fromEntity(SharedEquipmentAllocation alloc) {
        if (alloc == null) return null;
        SharedAllocationResponse r = new SharedAllocationResponse();
        r.setId(alloc.getId());
        r.setCustomHourlyRate(alloc.getCustomHourlyRate());
        r.setActive(alloc.isActive());
        r.setCreatedAt(alloc.getCreatedAt());

        ResourceSharingAgreement ag = alloc.getSharingAgreement();
        if (ag != null) {
            r.setSharingAgreementId(ag.getId());
            r.setSharingAgreementCode(ag.getAgreementCode());
            if (ag.getOwnerInstitution() != null) {
                r.setOwnerInstitutionId(ag.getOwnerInstitution().getId());
                try {
                    r.setOwnerInstitutionName(ag.getOwnerInstitution().getName());
                } catch (Exception ignored) {
                }
            }
            if (ag.getRequestingInstitution() != null) {
                r.setRequestingInstitutionId(ag.getRequestingInstitution().getId());
                try {
                    r.setRequestingInstitutionName(ag.getRequestingInstitution().getName());
                } catch (Exception ignored) {
                }
            }
        }

        Equipment eq = alloc.getEquipment();
        if (eq != null) {
            r.setEquipmentId(eq.getId());
            try {
                r.setEquipmentName(eq.getName());
                r.setEquipmentAssetTag(eq.getAssetTag());
                r.setEquipmentStatus(eq.getStatus());
                if (eq.getCategory() != null) {
                    r.setEquipmentCategoryName(eq.getCategory().getName());
                }
                if (r.getOwnerInstitutionId() == null && eq.getInstitution() != null) {
                    r.setOwnerInstitutionId(eq.getInstitution().getId());
                    r.setOwnerInstitutionName(eq.getInstitution().getName());
                }
            } catch (Exception ignored) {
            }
        }

        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSharingAgreementId() {
        return sharingAgreementId;
    }

    public void setSharingAgreementId(Long sharingAgreementId) {
        this.sharingAgreementId = sharingAgreementId;
    }

    public String getSharingAgreementCode() {
        return sharingAgreementCode;
    }

    public void setSharingAgreementCode(String sharingAgreementCode) {
        this.sharingAgreementCode = sharingAgreementCode;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentAssetTag() {
        return equipmentAssetTag;
    }

    public void setEquipmentAssetTag(String equipmentAssetTag) {
        this.equipmentAssetTag = equipmentAssetTag;
    }

    public String getEquipmentCategoryName() {
        return equipmentCategoryName;
    }

    public void setEquipmentCategoryName(String equipmentCategoryName) {
        this.equipmentCategoryName = equipmentCategoryName;
    }

    public EquipmentStatus getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public Long getOwnerInstitutionId() {
        return ownerInstitutionId;
    }

    public void setOwnerInstitutionId(Long ownerInstitutionId) {
        this.ownerInstitutionId = ownerInstitutionId;
    }

    public String getOwnerInstitutionName() {
        return ownerInstitutionName;
    }

    public void setOwnerInstitutionName(String ownerInstitutionName) {
        this.ownerInstitutionName = ownerInstitutionName;
    }

    public Long getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    public void setRequestingInstitutionId(Long requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    public String getRequestingInstitutionName() {
        return requestingInstitutionName;
    }

    public void setRequestingInstitutionName(String requestingInstitutionName) {
        this.requestingInstitutionName = requestingInstitutionName;
    }

    public BigDecimal getCustomHourlyRate() {
        return customHourlyRate;
    }

    public void setCustomHourlyRate(BigDecimal customHourlyRate) {
        this.customHourlyRate = customHourlyRate;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
