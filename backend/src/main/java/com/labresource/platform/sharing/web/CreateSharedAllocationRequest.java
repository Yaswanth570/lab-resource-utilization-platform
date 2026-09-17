package com.labresource.platform.sharing.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateSharedAllocationRequest {

    @NotNull(message = "Sharing agreement ID is required")
    private Long sharingAgreementId;

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private BigDecimal customHourlyRate;

    @JsonProperty("isActive")
    private Boolean isActive;

    public CreateSharedAllocationRequest() {
    }

    public Long getSharingAgreementId() {
        return sharingAgreementId;
    }

    public void setSharingAgreementId(Long sharingAgreementId) {
        this.sharingAgreementId = sharingAgreementId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public BigDecimal getCustomHourlyRate() {
        return customHourlyRate;
    }

    public void setCustomHourlyRate(BigDecimal customHourlyRate) {
        this.customHourlyRate = customHourlyRate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
