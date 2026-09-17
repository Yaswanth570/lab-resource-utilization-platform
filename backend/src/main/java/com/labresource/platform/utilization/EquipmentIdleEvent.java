package com.labresource.platform.utilization;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "equipment_idle_events", indexes = {
    @Index(name = "idx_ie_equip_dates", columnList = "equipment_id, idle_start_time"),
    @Index(name = "idx_ie_status", columnList = "status")
})
public class EquipmentIdleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_session_id")
    private EquipmentUsageSession usageSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_source", nullable = false, length = 50)
    private IdleDetectionSource detectionSource;

    @Column(name = "idle_start_time", nullable = false)
    private Instant idleStartTime;

    @Column(name = "idle_end_time")
    private Instant idleEndTime;

    @Column(name = "idle_duration_minutes")
    private Integer idleDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IdleEventStatus status = IdleEventStatus.ONGOING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by_user_id")
    private User loggedByUser;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public EquipmentIdleEvent() {
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

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public EquipmentUsageSession getUsageSession() {
        return usageSession;
    }

    public void setUsageSession(EquipmentUsageSession usageSession) {
        this.usageSession = usageSession;
    }

    public IdleDetectionSource getDetectionSource() {
        return detectionSource;
    }

    public void setDetectionSource(IdleDetectionSource detectionSource) {
        this.detectionSource = detectionSource;
    }

    public Instant getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(Instant idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public Instant getIdleEndTime() {
        return idleEndTime;
    }

    public void setIdleEndTime(Instant idleEndTime) {
        this.idleEndTime = idleEndTime;
    }

    public Integer getIdleDurationMinutes() {
        return idleDurationMinutes;
    }

    public void setIdleDurationMinutes(Integer idleDurationMinutes) {
        this.idleDurationMinutes = idleDurationMinutes;
    }

    public IdleEventStatus getStatus() {
        return status;
    }

    public void setStatus(IdleEventStatus status) {
        this.status = status;
    }

    public User getLoggedByUser() {
        return loggedByUser;
    }

    public void setLoggedByUser(User loggedByUser) {
        this.loggedByUser = loggedByUser;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        EquipmentIdleEvent that = (EquipmentIdleEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
