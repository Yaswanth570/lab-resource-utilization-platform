package com.labresource.platform.maintenance;

import com.labresource.platform.equipment.Equipment;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "equipment_downtime_logs", indexes = {
    @Index(name = "idx_dt_equip_window", columnList = "equipment_id, downtime_start, downtime_end")
})
public class EquipmentDowntimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private MaintenanceWorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", nullable = false, length = 50)
    private DowntimeReasonCategory reasonCategory;

    @Column(name = "downtime_start", nullable = false)
    private Instant downtimeStart;

    @Column(name = "downtime_end")
    private Instant downtimeEnd;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EquipmentDowntimeLog() {
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

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public MaintenanceWorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(MaintenanceWorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public DowntimeReasonCategory getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(DowntimeReasonCategory reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public Instant getDowntimeStart() {
        return downtimeStart;
    }

    public void setDowntimeStart(Instant downtimeStart) {
        this.downtimeStart = downtimeStart;
    }

    public Instant getDowntimeEnd() {
        return downtimeEnd;
    }

    public void setDowntimeEnd(Instant downtimeEnd) {
        this.downtimeEnd = downtimeEnd;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        EquipmentDowntimeLog that = (EquipmentDowntimeLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
