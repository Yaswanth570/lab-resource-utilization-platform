package com.labresource.platform.booking.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class CreateBookingRequest {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    private Long userId;
    private Long departmentId;
    private Long institutionId;
    private Long sharedAllocationId;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    private String purpose;
    private String projectCode;
    private Boolean isExternalBooking;
    private BigDecimal baseHourlyRate;

    public CreateBookingRequest() {
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

    public Long getSharedAllocationId() {
        return sharedAllocationId;
    }

    public void setSharedAllocationId(Long sharedAllocationId) {
        this.sharedAllocationId = sharedAllocationId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public Boolean getIsExternalBooking() {
        return isExternalBooking;
    }

    public void setIsExternalBooking(Boolean isExternalBooking) {
        this.isExternalBooking = isExternalBooking;
    }

    public BigDecimal getBaseHourlyRate() {
        return baseHourlyRate;
    }

    public void setBaseHourlyRate(BigDecimal baseHourlyRate) {
        this.baseHourlyRate = baseHourlyRate;
    }
}
