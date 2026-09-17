package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenancePriority;
import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import com.labresource.platform.maintenance.WorkOrderType;

import java.math.BigDecimal;
import java.time.Instant;

public class WorkOrderResponse {

    private Long id;
    private String workOrderNumber;
    private Long maintenanceRequestId;
    private String maintenanceRequestNumber;
    private Long equipmentId;
    private String equipmentName;
    private Long assignedTechnicianId;
    private String assignedTechnicianName;
    private WorkOrderType type;
    private MaintenancePriority priority;
    private WorkOrderStatus status;
    private Instant scheduledStart;
    private Instant scheduledEnd;
    private Instant actualStart;
    private Instant actualEnd;
    private BigDecimal laborHours;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal totalCost;
    private String workPerformedSummary;
    private String failureRootCause;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkOrderResponse() {
    }

    public static WorkOrderResponse from(MaintenanceWorkOrder wo) {
        if (wo == null) {
            return null;
        }
        WorkOrderResponse res = new WorkOrderResponse();
        res.setId(wo.getId());
        res.setWorkOrderNumber(wo.getWorkOrderNumber());
        if (wo.getMaintenanceRequest() != null) {
            res.setMaintenanceRequestId(wo.getMaintenanceRequest().getId());
            try {
                res.setMaintenanceRequestNumber(wo.getMaintenanceRequest().getRequestNumber());
            } catch (Exception ignored) {}
        }
        if (wo.getEquipment() != null) {
            res.setEquipmentId(wo.getEquipment().getId());
            try {
                res.setEquipmentName(wo.getEquipment().getName());
            } catch (Exception ignored) {}
        }
        if (wo.getAssignedTechnician() != null) {
            res.setAssignedTechnicianId(wo.getAssignedTechnician().getId());
            try {
                res.setAssignedTechnicianName((wo.getAssignedTechnician().getFirstName() + " " + wo.getAssignedTechnician().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        res.setType(wo.getType());
        res.setPriority(wo.getPriority());
        res.setStatus(wo.getStatus());
        res.setScheduledStart(wo.getScheduledStart());
        res.setScheduledEnd(wo.getScheduledEnd());
        res.setActualStart(wo.getActualStart());
        res.setActualEnd(wo.getActualEnd());
        res.setLaborHours(wo.getLaborHours());
        res.setLaborCost(wo.getLaborCost());
        res.setPartsCost(wo.getPartsCost());
        res.setTotalCost(wo.getTotalCost());
        res.setWorkPerformedSummary(wo.getWorkPerformedSummary());
        res.setFailureRootCause(wo.getFailureRootCause());
        res.setResolutionNotes(wo.getResolutionNotes());
        res.setCreatedAt(wo.getCreatedAt());
        res.setUpdatedAt(wo.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public Long getMaintenanceRequestId() {
        return maintenanceRequestId;
    }

    public void setMaintenanceRequestId(Long maintenanceRequestId) {
        this.maintenanceRequestId = maintenanceRequestId;
    }

    public String getMaintenanceRequestNumber() {
        return maintenanceRequestNumber;
    }

    public void setMaintenanceRequestNumber(String maintenanceRequestNumber) {
        this.maintenanceRequestNumber = maintenanceRequestNumber;
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

    public Long getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Long assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public void setAssignedTechnicianName(String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
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

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
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

    public Instant getActualStart() {
        return actualStart;
    }

    public void setActualStart(Instant actualStart) {
        this.actualStart = actualStart;
    }

    public Instant getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(Instant actualEnd) {
        this.actualEnd = actualEnd;
    }

    public BigDecimal getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(BigDecimal laborHours) {
        this.laborHours = laborHours;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(BigDecimal partsCost) {
        this.partsCost = partsCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getWorkPerformedSummary() {
        return workPerformedSummary;
    }

    public void setWorkPerformedSummary(String workPerformedSummary) {
        this.workPerformedSummary = workPerformedSummary;
    }

    public String getFailureRootCause() {
        return failureRootCause;
    }

    public void setFailureRootCause(String failureRootCause) {
        this.failureRootCause = failureRootCause;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
