package com.labresource.platform.maintenance.web;

import jakarta.validation.constraints.NotBlank;

public class ResolveMaintenanceRequestDto {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;

    public ResolveMaintenanceRequestDto() {
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
