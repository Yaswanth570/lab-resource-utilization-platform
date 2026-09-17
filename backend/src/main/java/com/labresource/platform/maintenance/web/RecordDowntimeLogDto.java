package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.DowntimeReasonCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class RecordDowntimeLogDto {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long workOrderId;

    @NotNull(message = "Reason category is required")
    private DowntimeReasonCategory reasonCategory;

    @NotNull(message = "Downtime start time is required")
    private Instant downtimeStart;

    private Instant downtimeEnd;
    private Integer durationMinutes;

    @NotBlank(message = "Description is required")
    private String description;

    public RecordDowntimeLogDto() {
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
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
}
