package com.labresource.platform.cost.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceRequest {

    private Long sharingAgreementId;

    private Long issuingInstitutionId;

    private Long billedInstitutionId;

    private Long billedDepartmentId;

    // Convenience aliases
    private Long institutionId;
    private Long departmentId;

    private String invoiceNumber;

    @NotNull(message = "Billing period start date is required")
    private LocalDate billingPeriodStart;

    @NotNull(message = "Billing period end date is required")
    private LocalDate billingPeriodEnd;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private LocalDate dueDate;

    private String notes;

    private List<Long> bookingIds = new ArrayList<>();

    public CreateInvoiceRequest() {
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
        if (this.issuingInstitutionId == null) {
            this.issuingInstitutionId = institutionId;
        }
        if (this.billedInstitutionId == null) {
            this.billedInstitutionId = institutionId;
        }
    }

    public Long getDepartmentId() {
        return departmentId != null ? departmentId : billedDepartmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
        if (this.billedDepartmentId == null) {
            this.billedDepartmentId = departmentId;
        }
    }

    public Long getSharingAgreementId() {
        return sharingAgreementId;
    }

    public void setSharingAgreementId(Long sharingAgreementId) {
        this.sharingAgreementId = sharingAgreementId;
    }

    public Long getIssuingInstitutionId() {
        return issuingInstitutionId;
    }

    public void setIssuingInstitutionId(Long issuingInstitutionId) {
        this.issuingInstitutionId = issuingInstitutionId;
    }

    public Long getBilledInstitutionId() {
        return billedInstitutionId;
    }

    public void setBilledInstitutionId(Long billedInstitutionId) {
        this.billedInstitutionId = billedInstitutionId;
    }

    public Long getBilledDepartmentId() {
        return billedDepartmentId;
    }

    public void setBilledDepartmentId(Long billedDepartmentId) {
        this.billedDepartmentId = billedDepartmentId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public void setBillingPeriodStart(LocalDate billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Long> getBookingIds() {
        return bookingIds;
    }

    public void setBookingIds(List<Long> bookingIds) {
        this.bookingIds = bookingIds;
    }
}
