package com.labresource.platform.maintenance.web;

import jakarta.validation.constraints.NotBlank;

public class RejectMaintenanceRequestDto {

    private Long triagedByUserId;

    @NotBlank(message = "Rejection reason is required")
    private String reason;

    public RejectMaintenanceRequestDto() {
    }

    public Long getTriagedByUserId() {
        return triagedByUserId;
    }

    public void setTriagedByUserId(Long triagedByUserId) {
        this.triagedByUserId = triagedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
