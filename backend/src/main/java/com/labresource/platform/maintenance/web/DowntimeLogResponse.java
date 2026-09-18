package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.DowntimeReasonCategory;
import com.labresource.platform.maintenance.EquipmentDowntimeLog;

import java.time.Instant;

public class DowntimeLogResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private Long workOrderId;
    private String workOrderNumber;
    private DowntimeReasonCategory reasonCategory;
    private Instant downtimeStart;
    private Instant downtimeEnd;
    private Integer durationMinutes;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public DowntimeLogResponse() {
    }

    public static DowntimeLogResponse from(EquipmentDowntimeLog log) {
        if (log == null) {
            return null;
        }
        DowntimeLogResponse res = new DowntimeLogResponse();
        res.setId(log.getId());
        if (log.getEquipment() != null) {
            res.setEquipmentId(log.getEquipment().getId());
            try {
                res.setEquipmentName(log.getEquipment().getName());
            } catch (Exception ignored) {
            }
        }
        if (log.getWorkOrder() != null) {
            res.setWorkOrderId(log.getWorkOrder().getId());
            try {
                res.setWorkOrderNumber(log.getWorkOrder().getWorkOrderNumber());
            } catch (Exception ignored) {
            }
        }
        res.setReasonCategory(log.getReasonCategory());
        res.setDowntimeStart(log.getDowntimeStart());
        res.setDowntimeEnd(log.getDowntimeEnd());
        res.setDurationMinutes(log.getDurationMinutes());
        res.setDescription(log.getDescription());
        res.setCreatedAt(log.getCreatedAt());
        res.setUpdatedAt(log.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public DowntimeReasonCategory getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(DowntimeReasonCategory reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public Instant getDowntimeStart() {
        return downtimeStart;
    }

    public void setDowntimeStart(Instant downtimeStart) {
        this.downtimeStart = downtimeStart;
    }

    public Instant getDowntimeEnd() {
        return downtimeEnd;
    }

    public void setDowntimeEnd(Instant downtimeEnd) {
        this.downtimeEnd = downtimeEnd;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
