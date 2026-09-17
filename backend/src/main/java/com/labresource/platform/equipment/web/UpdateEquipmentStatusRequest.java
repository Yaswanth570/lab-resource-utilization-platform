package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateEquipmentStatusRequest {

    @NotNull(message = "Equipment status is required")
    private EquipmentStatus status;

    private String reason;

    public UpdateEquipmentStatusRequest() {
    }

    public UpdateEquipmentStatusRequest(EquipmentStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
