package com.labresource.platform.booking.web;

public class ConfirmBookingRequest {

    private Long approvedByUserId;

    public ConfirmBookingRequest() {
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
}
