package com.labresource.platform.sharing;

import com.labresource.platform.department.Department;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "resource_sharing_requests", indexes = {
    @Index(name = "idx_sr_pair", columnList = "requesting_institution_id, owner_institution_id, status")
})
public class ResourceSharingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requesting_institution_id", nullable = false)
    private Institution requestingInstitution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_institution_id", nullable = false)
    private Institution ownerInstitution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_equipment_id")
    private Equipment targetEquipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id")
    private Department targetDepartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SharingRequestStatus status = SharingRequestStatus.PENDING;

    @Column(name = "request_title", nullable = false, length = 200)
    private String requestTitle;

    @Column(name = "justification", nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(name = "requested_start_date", nullable = false)
    private LocalDate requestedStartDate;

    @Column(name = "requested_end_date", nullable = false)
    private LocalDate requestedEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedByUser;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ResourceSharingRequest() {
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

    public User getRequestedByUser() {
        return requestedByUser;
    }

    public void setRequestedByUser(User requestedByUser) {
        this.requestedByUser = requestedByUser;
    }

    public Equipment getTargetEquipment() {
        return targetEquipment;
    }

    public void setTargetEquipment(Equipment targetEquipment) {
        this.targetEquipment = targetEquipment;
    }

    public Department getTargetDepartment() {
        return targetDepartment;
    }

    public void setTargetDepartment(Department targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public SharingRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SharingRequestStatus status) {
        this.status = status;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }

    public void setRequestedStartDate(LocalDate requestedStartDate) {
        this.requestedStartDate = requestedStartDate;
    }

    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }

    public void setRequestedEndDate(LocalDate requestedEndDate) {
        this.requestedEndDate = requestedEndDate;
    }

    public User getReviewedByUser() {
        return reviewedByUser;
    }

    public void setReviewedByUser(User reviewedByUser) {
        this.reviewedByUser = reviewedByUser;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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
        ResourceSharingRequest that = (ResourceSharingRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
