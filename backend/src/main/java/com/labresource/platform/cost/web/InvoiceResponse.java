package com.labresource.platform.cost.web;

import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long sharingAgreementId;
    private String sharingAgreementCode;
    private Long issuingInstitutionId;
    private String issuingInstitutionName;
    private Long billedInstitutionId;
    private String billedInstitutionName;
    private Long billedDepartmentId;
    private String billedDepartmentName;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private Instant issuedAt;
    private LocalDate dueDate;
    private Instant paidAt;
    private String paymentReference;
    private String notes;
    private int lineItemsCount;
    private List<InvoiceLineItemResponse> lineItems = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public InvoiceResponse() {
    }

    public static InvoiceResponse fromEntity(BillingInvoice inv) {
        return fromEntity(inv, null);
    }

    public static InvoiceResponse fromEntity(BillingInvoice inv, List<InvoiceLineItem> lines) {
        if (inv == null) return null;
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId());
        r.setInvoiceNumber(inv.getInvoiceNumber());

        if (inv.getSharingAgreement() != null) {
            r.setSharingAgreementId(inv.getSharingAgreement().getId());
            try {
                r.setSharingAgreementCode(inv.getSharingAgreement().getAgreementCode());
            } catch (Exception ignored) {}
        }

        if (inv.getIssuingInstitution() != null) {
            r.setIssuingInstitutionId(inv.getIssuingInstitution().getId());
            try {
                r.setIssuingInstitutionName(inv.getIssuingInstitution().getName());
            } catch (Exception ignored) {}
        }

        if (inv.getBilledInstitution() != null) {
            r.setBilledInstitutionId(inv.getBilledInstitution().getId());
            try {
                r.setBilledInstitutionName(inv.getBilledInstitution().getName());
            } catch (Exception ignored) {}
        }

        if (inv.getBilledDepartment() != null) {
            r.setBilledDepartmentId(inv.getBilledDepartment().getId());
            try {
                r.setBilledDepartmentName(inv.getBilledDepartment().getName());
            } catch (Exception ignored) {}
        }

        r.setBillingPeriodStart(inv.getBillingPeriodStart());
        r.setBillingPeriodEnd(inv.getBillingPeriodEnd());
        r.setSubtotalAmount(inv.getSubtotalAmount());
        r.setDiscountAmount(inv.getDiscountAmount());
        r.setTotalAmount(inv.getTotalAmount());
        r.setStatus(inv.getStatus());
        r.setIssuedAt(inv.getIssuedAt());
        r.setDueDate(inv.getDueDate());
        r.setPaidAt(inv.getPaidAt());
        r.setPaymentReference(inv.getPaymentReference());
        r.setNotes(inv.getNotes());
        r.setCreatedAt(inv.getCreatedAt());
        r.setUpdatedAt(inv.getUpdatedAt());

        if (lines != null) {
            r.setLineItems(lines.stream().map(InvoiceLineItemResponse::fromEntity).toList());
            r.setLineItemsCount(lines.size());
        }

        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getSharingAgreementId() {
        return sharingAgreementId;
    }

    public void setSharingAgreementId(Long sharingAgreementId) {
        this.sharingAgreementId = sharingAgreementId;
    }

    public String getSharingAgreementCode() {
        return sharingAgreementCode;
    }

    public void setSharingAgreementCode(String sharingAgreementCode) {
        this.sharingAgreementCode = sharingAgreementCode;
    }

    public Long getIssuingInstitutionId() {
        return issuingInstitutionId;
    }

    public void setIssuingInstitutionId(Long issuingInstitutionId) {
        this.issuingInstitutionId = issuingInstitutionId;
    }

    public String getIssuingInstitutionName() {
        return issuingInstitutionName;
    }

    public void setIssuingInstitutionName(String issuingInstitutionName) {
        this.issuingInstitutionName = issuingInstitutionName;
    }

    public Long getBilledInstitutionId() {
        return billedInstitutionId;
    }

    public void setBilledInstitutionId(Long billedInstitutionId) {
        this.billedInstitutionId = billedInstitutionId;
    }

    public String getBilledInstitutionName() {
        return billedInstitutionName;
    }

    public void setBilledInstitutionName(String billedInstitutionName) {
        this.billedInstitutionName = billedInstitutionName;
    }

    public Long getBilledDepartmentId() {
        return billedDepartmentId;
    }

    public void setBilledDepartmentId(Long billedDepartmentId) {
        this.billedDepartmentId = billedDepartmentId;
    }

    public String getBilledDepartmentName() {
        return billedDepartmentName;
    }

    public void setBilledDepartmentName(String billedDepartmentName) {
        this.billedDepartmentName = billedDepartmentName;
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

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getLineItemsCount() {
        return lineItemsCount;
    }

    public void setLineItemsCount(int lineItemsCount) {
        this.lineItemsCount = lineItemsCount;
    }

    public List<InvoiceLineItemResponse> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<InvoiceLineItemResponse> lineItems) {
        this.lineItems = lineItems;
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
