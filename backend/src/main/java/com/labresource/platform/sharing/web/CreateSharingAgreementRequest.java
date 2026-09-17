package com.labresource.platform.sharing.web;

import com.labresource.platform.sharing.SharingAgreementStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateSharingAgreementRequest {

    private Long sharingRequestId;
    private String agreementCode;

    @NotNull(message = "Requesting institution ID is required")
    private Long requestingInstitutionId;

    @NotNull(message = "Owner institution ID is required")
    private Long ownerInstitutionId;

    @DecimalMin(value = "0.01", message = "Billing rate multiplier must be at least 0.01")
    private BigDecimal billingRateMultiplier;

    private Integer maxMonthlyHours;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private SharingAgreementStatus status;

    public CreateSharingAgreementRequest() {
    }

    public Long getSharingRequestId() {
        return sharingRequestId;
    }

    public void setSharingRequestId(Long sharingRequestId) {
        this.sharingRequestId = sharingRequestId;
    }

    public String getAgreementCode() {
        return agreementCode;
    }

    public void setAgreementCode(String agreementCode) {
        this.agreementCode = agreementCode;
    }

    public Long getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    public void setRequestingInstitutionId(Long requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    public Long getOwnerInstitutionId() {
        return ownerInstitutionId;
    }

    public void setOwnerInstitutionId(Long ownerInstitutionId) {
        this.ownerInstitutionId = ownerInstitutionId;
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
}
