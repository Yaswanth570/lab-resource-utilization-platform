package com.labresource.platform.utilization.web;

import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.SessionStatus;

import java.time.Instant;

public class UsageSessionResponse {

    private Long id;
    private Long bookingId;
    private String bookingReference;
    private Long equipmentId;
    private String equipmentName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Instant checkedInAt;
    private Instant checkedOutAt;
    private Integer actualDurationMinutes;
    private Integer scheduledDurationMinutes;
    private SessionStatus sessionStatus;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public UsageSessionResponse() {
    }

    public static UsageSessionResponse from(EquipmentUsageSession s) {
        if (s == null) {
            return null;
        }
        UsageSessionResponse r = new UsageSessionResponse();
        r.setId(s.getId());
        if (s.getBooking() != null) {
            r.setBookingId(s.getBooking().getId());
            try {
                r.setBookingReference(s.getBooking().getBookingReference());
            } catch (Exception ignored) {}
        }
        if (s.getEquipment() != null) {
            r.setEquipmentId(s.getEquipment().getId());
            try {
                r.setEquipmentName(s.getEquipment().getName());
            } catch (Exception ignored) {}
        }
        if (s.getUser() != null) {
            r.setUserId(s.getUser().getId());
            try {
                r.setUserName((s.getUser().getFirstName() + " " + s.getUser().getLastName()).trim());
                r.setUserEmail(s.getUser().getEmail());
            } catch (Exception ignored) {}
        }
        r.setCheckedInAt(s.getCheckedInAt());
        r.setCheckedOutAt(s.getCheckedOutAt());
        r.setActualDurationMinutes(s.getActualDurationMinutes());
        r.setScheduledDurationMinutes(s.getScheduledDurationMinutes());
        r.setSessionStatus(s.getSessionStatus());
        r.setNotes(s.getNotes());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public Integer getScheduledDurationMinutes() {
        return scheduledDurationMinutes;
    }

    public void setScheduledDurationMinutes(Integer scheduledDurationMinutes) {
        this.scheduledDurationMinutes = scheduledDurationMinutes;
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
