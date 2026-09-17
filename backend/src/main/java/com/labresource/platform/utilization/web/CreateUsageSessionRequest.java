package com.labresource.platform.utilization.web;

import com.labresource.platform.utilization.SessionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateUsageSessionRequest {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long userId;
    private Long bookingId;
    private Long departmentId;
    private Long institutionId;
    private Instant checkedInAt;
    private Instant checkedOutAt;
    private Integer scheduledDurationMinutes;
    private Integer actualDurationMinutes;
    private SessionStatus sessionStatus;
    private String notes;

    public CreateUsageSessionRequest() {
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Instant checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(Instant checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public Integer getScheduledDurationMinutes() {
        return scheduledDurationMinutes;
    }

    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) {
        this.scheduledDurationMinutes = scheduledDurationMinutes;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
