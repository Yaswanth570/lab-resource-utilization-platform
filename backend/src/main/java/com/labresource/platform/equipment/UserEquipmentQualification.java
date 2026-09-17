package com.labresource.platform.equipment;

import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_equipment_qualifications", uniqueConstraints = {
    @UniqueConstraint(name = "uq_user_equip_qual", columnNames = {"user_id", "equipment_id"})
}, indexes = {
    @Index(name = "idx_ueq_lookup", columnList = "user_id, equipment_id, status")
})
public class UserEquipmentQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certified_by_user_id", nullable = false)
    private User certifiedBy;

    @Column(name = "certified_at", nullable = false)
    private Instant certifiedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QualificationStatus status = QualificationStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public UserEquipmentQualification() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.certifiedAt == null) {
            this.certifiedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getCertifiedBy() {
        return certifiedBy;
    }

    public void setCertifiedBy(User certifiedBy) {
        this.certifiedBy = certifiedBy;
    }

    public Instant getCertifiedAt() {
        return certifiedAt;
    }

    public void setCertifiedAt(Instant certifiedAt) {
        this.certifiedAt = certifiedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public QualificationStatus getStatus() {
        return status;
    }

    public void setStatus(QualificationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEquipmentQualification that = (UserEquipmentQualification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
