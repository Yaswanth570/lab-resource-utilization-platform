package com.labresource.platform.cost.web;

import com.labresource.platform.cost.InvoiceLineItem;

import java.math.BigDecimal;
import java.time.Instant;

public class InvoiceLineItemResponse {

    private Long id;
    private Long invoiceId;
    private Long bookingId;
    private String bookingReference;
    private Long usageSessionId;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentAssetTag;
    private String description;
    private BigDecimal billableHours;
    private BigDecimal hourlyRate;
    private BigDecimal totalLineCost;
    private BigDecimal penaltyAmount;
    private Instant createdAt;

    public InvoiceLineItemResponse() {
    }

    public static InvoiceLineItemResponse fromEntity(InvoiceLineItem item) {
        if (item == null) return null;
        InvoiceLineItemResponse r = new InvoiceLineItemResponse();
        r.setId(item.getId());
        if (item.getInvoice() != null) {
            r.setInvoiceId(item.getInvoice().getId());
        }
        if (item.getBooking() != null) {
            r.setBookingId(item.getBooking().getId());
            try {
                r.setBookingReference(item.getBooking().getBookingReference());
            } catch (Exception ignored) {}
        }
        if (item.getUsageSession() != null) {
            r.setUsageSessionId(item.getUsageSession().getId());
        }
        if (item.getEquipment() != null) {
            r.setEquipmentId(item.getEquipment().getId());
            try {
                r.setEquipmentName(item.getEquipment().getName());
                r.setEquipmentAssetTag(item.getEquipment().getAssetTag());
            } catch (Exception ignored) {}
        }
        r.setDescription(item.getDescription());
        r.setBillableHours(item.getBillableHours());
        r.setHourlyRate(item.getHourlyRate());
        r.setTotalLineCost(item.getTotalLineCost());
        r.setPenaltyAmount(item.getPenaltyAmount());
        r.setCreatedAt(item.getCreatedAt());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
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

    public Long getUsageSessionId() {
        return usageSessionId;
    }

    public void setUsageSessionId(Long usageSessionId) {
        this.usageSessionId = usageSessionId;
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

    public String getEquipmentAssetTag() {
        return equipmentAssetTag;
    }

    public void setEquipmentAssetTag(String equipmentAssetTag) {
        this.equipmentAssetTag = equipmentAssetTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(BigDecimal billableHours) {
        this.billableHours = billableHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getTotalLineCost() {
        return totalLineCost;
    }

    public void setTotalLineCost(BigDecimal totalLineCost) {
        this.totalLineCost = totalLineCost;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
