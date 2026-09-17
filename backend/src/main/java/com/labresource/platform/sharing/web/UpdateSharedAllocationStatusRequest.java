package com.labresource.platform.sharing.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateSharedAllocationStatusRequest {

    @JsonProperty("isActive")
    private Boolean isActive;
    private String status;

    public UpdateSharedAllocationStatusRequest() {
    }

    public UpdateSharedAllocationStatusRequest(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsActive() {
        if (isActive != null) {
            return isActive;
        }
        if (status != null) {
            return "ACTIVE".equalsIgnoreCase(status) || "TRUE".equalsIgnoreCase(status);
        }
        return null;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
