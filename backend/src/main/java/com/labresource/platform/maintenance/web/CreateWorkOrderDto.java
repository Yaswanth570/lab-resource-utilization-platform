package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenancePriority;
import com.labresource.platform.maintenance.WorkOrderType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateWorkOrderDto {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long maintenanceRequestId;
    private Long assignedTechnicianId;

    @NotNull(message = "Work order type is required")
    private WorkOrderType type;

    private MaintenancePriority priority;

    @NotNull(message = "Scheduled start time is required")
    private Instant scheduledStart;

    @NotNull(message = "Scheduled end time is required")
    private Instant scheduledEnd;

    private String workOrderNumber;

    public CreateWorkOrderDto() {
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getMaintenanceRequestId() {
        return maintenanceRequestId;
    }

    public void setMaintenanceRequestId(Long maintenanceRequestId) {
        this.maintenanceRequestId = maintenanceRequestId;
    }

    public Long getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Long assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public WorkOrderType getType() {
        return type;
    }

    public void setType(WorkOrderType type) {
        this.type = type;
    }

    public MaintenancePriority getPriority() {
        return priority;
    }

    public void setPriority(MaintenancePriority priority) {
        this.priority = priority;
    }

    public Instant getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(Instant scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public Instant getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(Instant scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }
}
