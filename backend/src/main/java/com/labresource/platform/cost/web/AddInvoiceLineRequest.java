package com.labresource.platform.cost.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AddInvoiceLineRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @DecimalMin(value = "0.0", message = "Custom rate cannot be negative")
    private BigDecimal customRate;

    private String description;

    public AddInvoiceLineRequest() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getCustomRate() {
        return customRate;
    }

    public void setCustomRate(BigDecimal customRate) {
        this.customRate = customRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
