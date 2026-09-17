package com.labresource.platform.equipment.web;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateQualificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long certifiedByUserId;
    private Instant expiresAt;
    private String notes;

    public CreateQualificationRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getCertifiedByUserId() {
        return certifiedByUserId;
    }

    public void setCertifiedByUserId(Long certifiedByUserId) {
        this.certifiedByUserId = certifiedByUserId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
