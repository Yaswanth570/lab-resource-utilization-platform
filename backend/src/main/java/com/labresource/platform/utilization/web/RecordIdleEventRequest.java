package com.labresource.platform.utilization.web;

import com.labresource.platform.utilization.IdleDetectionSource;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class RecordIdleEventRequest {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long bookingId;
    private Long usageSessionId;
    private IdleDetectionSource detectionSource;

    @NotNull(message = "Idle start time is required")
    private Instant idleStartTime;

    private Instant idleEndTime;
    private Integer idleDurationMinutes;
    private Long loggedByUserId;
    private String notes;

    public RecordIdleEventRequest() {
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUsageSessionId() {
        return usageSessionId;
    }

    public void setUsageSessionId(Long usageSessionId) {
        this.usageSessionId = usageSessionId;
    }

    public IdleDetectionSource getDetectionSource() {
        return detectionSource;
    }

    public void setDetectionSource(IdleDetectionSource detectionSource) {
        this.detectionSource = detectionSource;
    }

    public Instant getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(Instant idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public Instant getIdleEndTime() {
        return idleEndTime;
    }

    public void setIdleEndTime(Instant idleEndTime) {
        this.idleEndTime = idleEndTime;
    }

    public Integer getIdleDurationMinutes() {
        return idleDurationMinutes;
    }

    public void setIdleDurationMinutes(Integer idleDurationMinutes) {
        this.idleDurationMinutes = idleDurationMinutes;
    }

    public Long getLoggedByUserId() {
        return loggedByUserId;
    }

    public void setLoggedByUserId(Long loggedByUserId) {
        this.loggedByUserId = loggedByUserId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
