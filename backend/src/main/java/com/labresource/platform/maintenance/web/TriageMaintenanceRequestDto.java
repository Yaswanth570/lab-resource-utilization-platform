package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenancePriority;

public class TriageMaintenanceRequestDto {

    private Long triagedByUserId;
    private MaintenancePriority priority;

    public TriageMaintenanceRequestDto() {
    }

    public Long getTriagedByUserId() {
        return triagedByUserId;
    }

    public void setTriagedByUserId(Long triagedByUserId) {
        this.triagedByUserId = triagedByUserId;
    }

    public MaintenancePriority getPriority() {
        return priority;
    }

    public void setPriority(MaintenancePriority priority) {
        this.priority = priority;
    }
}
