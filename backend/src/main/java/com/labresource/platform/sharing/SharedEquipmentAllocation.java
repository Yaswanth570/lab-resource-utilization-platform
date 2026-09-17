package com.labresource.platform.sharing;

import com.labresource.platform.equipment.Equipment;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "shared_equipment_allocations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_sea_agreement_equip", columnNames = {"sharing_agreement_id", "equipment_id"})
}, indexes = {
    @Index(name = "idx_sea_equip", columnList = "equipment_id")
})
public class SharedEquipmentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sharing_agreement_id", nullable = false)
    private ResourceSharingAgreement sharingAgreement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "custom_hourly_rate", precision = 10, scale = 2)
    private BigDecimal customHourlyRate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SharedEquipmentAllocation() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceSharingAgreement getSharingAgreement() {
        return sharingAgreement;
    }

    public void setSharingAgreement(ResourceSharingAgreement sharingAgreement) {
        this.sharingAgreement = sharingAgreement;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public BigDecimal getCustomHourlyRate() {
        return customHourlyRate;
    }

    public void setCustomHourlyRate(BigDecimal customHourlyRate) {
        this.customHourlyRate = customHourlyRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedEquipmentAllocation that = (SharedEquipmentAllocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
