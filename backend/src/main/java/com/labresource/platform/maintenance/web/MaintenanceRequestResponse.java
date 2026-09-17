package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenancePriority;
import com.labresource.platform.maintenance.MaintenanceRequest;
import com.labresource.platform.maintenance.MaintenanceRequestStatus;

import java.time.Instant;

public class MaintenanceRequestResponse {

    private Long id;
    private String requestNumber;
    private Long equipmentId;
    private String equipmentName;
    private Long reportedByUserId;
    private String reportedByUserName;
    private MaintenancePriority priority;
    private String issueTitle;
    private String issueDescription;
    private MaintenanceRequestStatus status;
    private Long triagedByUserId;
    private String triagedByUserName;
    private Instant triagedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public MaintenanceRequestResponse() {
    }

    public static MaintenanceRequestResponse from(MaintenanceRequest r) {
        if (r == null) {
            return null;
        }
        MaintenanceRequestResponse res = new MaintenanceRequestResponse();
        res.setId(r.getId());
        res.setRequestNumber(r.getRequestNumber());
        if (r.getEquipment() != null) {
            res.setEquipmentId(r.getEquipment().getId());
            try {
                res.setEquipmentName(r.getEquipment().getName());
            } catch (Exception ignored) {}
        }
        if (r.getReportedByUser() != null) {
            res.setReportedByUserId(r.getReportedByUser().getId());
            try {
                res.setReportedByUserName((r.getReportedByUser().getFirstName() + " " + r.getReportedByUser().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        res.setPriority(r.getPriority());
        res.setIssueTitle(r.getIssueTitle());
        res.setIssueDescription(r.getIssueDescription());
        res.setStatus(r.getStatus());
        if (r.getTriagedByUser() != null) {
            res.setTriagedByUserId(r.getTriagedByUser().getId());
            try {
                res.setTriagedByUserName((r.getTriagedByUser().getFirstName() + " " + r.getTriagedByUser().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        res.setTriagedAt(r.getTriagedAt());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
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

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(Long reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getReportedByUserName() {
        return reportedByUserName;
    }

    public void setReportedByUserName(String reportedByUserName) {
        this.reportedByUserName = reportedByUserName;
    }

    public MaintenancePriority getPriority() {
        return priority;
    }

    public void setPriority(MaintenancePriority priority) {
        this.priority = priority;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public MaintenanceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceRequestStatus status) {
        this.status = status;
    }

    public Long getTriagedByUserId() {
        return triagedByUserId;
    }

    public void setTriagedByUserId(Long triagedByUserId) {
        this.triagedByUserId = triagedByUserId;
    }

    public String getTriagedByUserName() {
        return triagedByUserName;
    }

    public void setTriagedByUserName(String triagedByUserName) {
        this.triagedByUserName = triagedByUserName;
    }

    public Instant getTriagedAt() {
        return triagedAt;
    }

    public void setTriagedAt(Instant triagedAt) {
        this.triagedAt = triagedAt;
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
