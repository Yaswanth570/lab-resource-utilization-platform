package com.labresource.platform.utilization.web;

import com.labresource.platform.utilization.EquipmentIdleEvent;
import com.labresource.platform.utilization.IdleDetectionSource;
import com.labresource.platform.utilization.IdleEventStatus;

import java.time.Instant;

public class IdleEventResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private Long bookingId;
    private Long usageSessionId;
    private IdleDetectionSource detectionSource;
    private Instant idleStartTime;
    private Instant idleEndTime;
    private Integer idleDurationMinutes;
    private IdleEventStatus status;
    private Long loggedByUserId;
    private String loggedByUserName;
    private String notes;
    private Instant createdAt;

    public IdleEventResponse() {
    }

    public static IdleEventResponse from(EquipmentIdleEvent e) {
        if (e == null) {
            return null;
        }
        IdleEventResponse r = new IdleEventResponse();
        r.setId(e.getId());
        if (e.getEquipment() != null) {
            r.setEquipmentId(e.getEquipment().getId());
            try {
                r.setEquipmentName(e.getEquipment().getName());
            } catch (Exception ignored) {}
        }
        if (e.getBooking() != null) {
            r.setBookingId(e.getBooking().getId());
        }
        if (e.getUsageSession() != null) {
            r.setUsageSessionId(e.getUsageSession().getId());
        }
        r.setDetectionSource(e.getDetectionSource());
        r.setIdleStartTime(e.getIdleStartTime());
        r.setIdleEndTime(e.getIdleEndTime());
        r.setIdleDurationMinutes(e.getIdleDurationMinutes());
        r.setStatus(e.getStatus());
        if (e.getLoggedByUser() != null) {
            r.setLoggedByUserId(e.getLoggedByUser().getId());
            try {
                r.setLoggedByUserName((e.getLoggedByUser().getFirstName() + " " + e.getLoggedByUser().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
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

    public IdleEventStatus getStatus() {
        return status;
    }

    public void setStatus(IdleEventStatus status) {
        this.status = status;
    }

    public Long getLoggedByUserId() {
        return loggedByUserId;
    }

    public void setLoggedByUserId(Long loggedByUserId) {
        this.loggedByUserId = loggedByUserId;
    }

    public String getLoggedByUserName() {
        return loggedByUserName;
    }

    public void setLoggedByUserName(String loggedByUserName) {
        this.loggedByUserName = loggedByUserName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
