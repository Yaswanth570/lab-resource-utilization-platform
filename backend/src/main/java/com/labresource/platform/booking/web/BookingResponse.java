package com.labresource.platform.booking.web;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class BookingResponse {

    private Long id;
    private String bookingReference;
    private Long equipmentId;
    private String equipmentName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long departmentId;
    private String departmentName;
    private Long institutionId;
    private String institutionName;
    private Long sharedAllocationId;
    private Instant startTime;
    private Instant endTime;
    private BookingStatus status;
    private BookingBillingStatus billingStatus;
    private String purpose;
    private String projectCode;
    private Long approvedByUserId;
    private String approvedByUserName;
    private Instant approvedAt;
    private String rejectionReason;
    private String cancellationReason;
    private Instant cancelledAt;
    private Long cancelledByUserId;
    private String cancelledByUserName;
    private boolean isExternalBooking;
    private BigDecimal baseHourlyRate;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private Instant createdAt;
    private Instant updatedAt;

    public BookingResponse() {
    }

    public static BookingResponse from(Booking b) {
        if (b == null) {
            return null;
        }
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setBookingReference(b.getBookingReference());
        if (b.getEquipment() != null) {
            r.setEquipmentId(b.getEquipment().getId());
            try {
                r.setEquipmentName(b.getEquipment().getName());
            } catch (Exception ignored) {}
        }
        if (b.getUser() != null) {
            r.setUserId(b.getUser().getId());
            try {
                r.setUserName((b.getUser().getFirstName() + " " + b.getUser().getLastName()).trim());
                r.setUserEmail(b.getUser().getEmail());
            } catch (Exception ignored) {}
        }
        if (b.getDepartment() != null) {
            r.setDepartmentId(b.getDepartment().getId());
            try {
                r.setDepartmentName(b.getDepartment().getName());
            } catch (Exception ignored) {}
        }
        if (b.getInstitution() != null) {
            r.setInstitutionId(b.getInstitution().getId());
            try {
                r.setInstitutionName(b.getInstitution().getName());
            } catch (Exception ignored) {}
        }
        if (b.getSharedEquipmentAllocation() != null) {
            r.setSharedAllocationId(b.getSharedEquipmentAllocation().getId());
        }
        r.setStartTime(b.getStartTime());
        r.setEndTime(b.getEndTime());
        r.setStatus(b.getStatus());
        r.setBillingStatus(b.getBillingStatus());
        r.setPurpose(b.getPurpose());
        r.setProjectCode(b.getProjectCode());
        if (b.getApprovedByUser() != null) {
            r.setApprovedByUserId(b.getApprovedByUser().getId());
            try {
                r.setApprovedByUserName((b.getApprovedByUser().getFirstName() + " " + b.getApprovedByUser().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        r.setApprovedAt(b.getApprovedAt());
        r.setRejectionReason(b.getRejectionReason());
        r.setCancellationReason(b.getCancellationReason());
        r.setCancelledAt(b.getCancelledAt());
        if (b.getCancelledByUser() != null) {
            r.setCancelledByUserId(b.getCancelledByUser().getId());
            try {
                r.setCancelledByUserName((b.getCancelledByUser().getFirstName() + " " + b.getCancelledByUser().getLastName()).trim());
            } catch (Exception ignored) {}
        }
        r.setExternalBooking(b.isExternalBooking());
        r.setBaseHourlyRate(b.getBaseHourlyRate());
        r.setEstimatedCost(b.getEstimatedCost());
        r.setActualCost(b.getActualCost());
        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BookingBillingStatus getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(BookingBillingStatus billingStatus) {
        this.billingStatus = billingStatus;
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

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }

    public String getApprovedByUserName() {
        return approvedByUserName;
    }

    public void setApprovedByUserName(String approvedByUserName) {
        this.approvedByUserName = approvedByUserName;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Long getCancelledByUserId() {
        return cancelledByUserId;
    }

    public void setCancelledByUserId(Long cancelledByUserId) {
        this.cancelledByUserId = cancelledByUserId;
    }

    public String getCancelledByUserName() {
        return cancelledByUserName;
    }

    public void setCancelledByUserName(String cancelledByUserName) {
        this.cancelledByUserName = cancelledByUserName;
    }

    public boolean isExternalBooking() {
        return isExternalBooking;
    }

    public void setExternalBooking(boolean externalBooking) {
        isExternalBooking = externalBooking;
    }

    public BigDecimal getBaseHourlyRate() {
        return baseHourlyRate;
    }

    public void setBaseHourlyRate(BigDecimal baseHourlyRate) {
        this.baseHourlyRate = baseHourlyRate;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
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
