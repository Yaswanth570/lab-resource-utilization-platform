package com.labresource.platform.sharing;

import com.labresource.platform.institution.Institution;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "resource_sharing_agreements", uniqueConstraints = {
    @UniqueConstraint(name = "uq_sa_code", columnNames = {"agreement_code"}),
    @UniqueConstraint(name = "uq_sa_request_id", columnNames = {"sharing_request_id"})
}, indexes = {
    @Index(name = "idx_sa_institutions", columnList = "requesting_institution_id, owner_institution_id, status")
})
public class ResourceSharingAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sharing_request_id", nullable = false, unique = true)
    private ResourceSharingRequest sharingRequest;

    @Column(name = "agreement_code", nullable = false, unique = true, length = 50)
    private String agreementCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requesting_institution_id", nullable = false)
    private Institution requestingInstitution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_institution_id", nullable = false)
    private Institution ownerInstitution;

    @Column(name = "billing_rate_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal billingRateMultiplier = BigDecimal.ONE;

    @Column(name = "max_monthly_hours")
    private Integer maxMonthlyHours;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SharingAgreementStatus status = SharingAgreementStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ResourceSharingAgreement() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceSharingRequest getSharingRequest() {
        return sharingRequest;
    }

    public void setSharingRequest(ResourceSharingRequest sharingRequest) {
        this.sharingRequest = sharingRequest;
    }

    public String getAgreementCode() {
        return agreementCode;
    }

    public void setAgreementCode(String agreementCode) {
        this.agreementCode = agreementCode;
    }

    public Institution getRequestingInstitution() {
        return requestingInstitution;
    }

    public void setRequestingInstitution(Institution requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    public Institution getOwnerInstitution() {
        return ownerInstitution;
    }

    public void setOwnerInstitution(Institution ownerInstitution) {
        this.ownerInstitution = ownerInstitution;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceSharingAgreement that = (ResourceSharingAgreement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
