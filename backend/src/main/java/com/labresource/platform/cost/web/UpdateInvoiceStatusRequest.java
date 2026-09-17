package com.labresource.platform.cost.web;

import com.labresource.platform.cost.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateInvoiceStatusRequest {

    @NotNull(message = "Status is required")
    private InvoiceStatus status;

    private String paymentReference;

    public UpdateInvoiceStatusRequest() {
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
}
