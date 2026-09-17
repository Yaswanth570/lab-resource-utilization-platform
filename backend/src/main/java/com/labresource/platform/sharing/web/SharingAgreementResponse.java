package com.labresource.platform.sharing.web;

import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharingAgreementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class SharingAgreementResponse {

    private Long id;
    private String agreementCode;
    private Long sharingRequestId;
    private Long requestingInstitutionId;
    private String requestingInstitutionName;
    private Long ownerInstitutionId;
    private String ownerInstitutionName;
    private BigDecimal billingRateMultiplier;
    private Integer maxMonthlyHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private SharingAgreementStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public SharingAgreementResponse() {
    }

    public static SharingAgreementResponse fromEntity(ResourceSharingAgreement a) {
        if (a == null) return null;
        SharingAgreementResponse r = new SharingAgreementResponse();
        r.setId(a.getId());
        r.setAgreementCode(a.getAgreementCode());
        if (a.getSharingRequest() != null) {
            r.setSharingRequestId(a.getSharingRequest().getId());
        }
        if (a.getRequestingInstitution() != null) {
            r.setRequestingInstitutionId(a.getRequestingInstitution().getId());
            try {
                r.setRequestingInstitutionName(a.getRequestingInstitution().getName());
            } catch (Exception ignored) {
            }
        }
        if (a.getOwnerInstitution() != null) {
            r.setOwnerInstitutionId(a.getOwnerInstitution().getId());
            try {
                r.setOwnerInstitutionName(a.getOwnerInstitution().getName());
            } catch (Exception ignored) {
            }
        }
        r.setBillingRateMultiplier(a.getBillingRateMultiplier());
        r.setMaxMonthlyHours(a.getMaxMonthlyHours());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        r.setStatus(a.getStatus());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgreementCode() {
        return agreementCode;
    }

    public void setAgreementCode(String agreementCode) {
        this.agreementCode = agreementCode;
    }

    public Long getSharingRequestId() {
        return sharingRequestId;
    }

    public void setSharingRequestId(Long sharingRequestId) {
        this.sharingRequestId = sharingRequestId;
    }

    public Long getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    public void setRequestingInstitutionId(Long requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    public String getRequestingInstitutionName() {
        return requestingInstitutionName;
    }

    public void setRequestingInstitutionName(String requestingInstitutionName) {
        this.requestingInstitutionName = requestingInstitutionName;
    }

    public Long getOwnerInstitutionId() {
        return ownerInstitutionId;
    }

    public void setOwnerInstitutionId(Long ownerInstitutionId) {
        this.ownerInstitutionId = ownerInstitutionId;
    }

    public String getOwnerInstitutionName() {
        return ownerInstitutionName;
    }

    public void setOwnerInstitutionName(String ownerInstitutionName) {
        this.ownerInstitutionName = ownerInstitutionName;
    }

    public BigDecimal getBillingRateMultiplier() {
        return billingRateMultiplier;
    }

    public void setBillingRateMultiplier(BigDecimal billingRateMultiplier) {
        this.billingRateMultiplier = billingRateMultiplier;
    }

    public Integer getMaxMonthlyHours() {
        return maxMonthlyHours;
    }

    public void setMaxMonthlyHours(Integer maxMonthlyHours) {
        this.maxMonthlyHours = maxMonthlyHours;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SharingAgreementStatus getStatus() {
        return status;
    }

    public void setStatus(SharingAgreementStatus status) {
        this.status = status;
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
