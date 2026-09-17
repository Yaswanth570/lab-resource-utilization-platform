package com.labresource.platform.maintenance.web;

public class CancelWorkOrderDto {

    private String cancellationReason;

    public CancelWorkOrderDto() {
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
