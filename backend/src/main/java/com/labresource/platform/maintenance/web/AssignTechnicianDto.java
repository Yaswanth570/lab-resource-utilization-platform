package com.labresource.platform.maintenance.web;

import jakarta.validation.constraints.NotNull;

public class AssignTechnicianDto {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    public AssignTechnicianDto() {
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
