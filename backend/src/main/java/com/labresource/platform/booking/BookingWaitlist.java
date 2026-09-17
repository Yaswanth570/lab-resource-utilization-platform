package com.labresource.platform.booking;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "booking_waitlists", indexes = {
    @Index(name = "idx_wl_queue", columnList = "equipment_id, desired_start_time, status, position")
})
public class BookingWaitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "desired_start_time", nullable = false)
    private Instant desiredStartTime;

    @Column(name = "desired_end_time", nullable = false)
    private Instant desiredEndTime;

    @Column(name = "position", nullable = false)
    private Integer position = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "offer_expiry_time")
    private Instant offerExpiryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_booking_id")
    private Booking convertedBooking;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BookingWaitlist() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getDesiredStartTime() {
        return desiredStartTime;
    }

    public void setDesiredStartTime(Instant desiredStartTime) {
        this.desiredStartTime = desiredStartTime;
    }

    public Instant getDesiredEndTime() {
        return desiredEndTime;
    }

    public void setDesiredEndTime(Instant desiredEndTime) {
        this.desiredEndTime = desiredEndTime;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public Instant getOfferExpiryTime() {
        return offerExpiryTime;
    }

    public void setOfferExpiryTime(Instant offerExpiryTime) {
        this.offerExpiryTime = offerExpiryTime;
    }

    public Booking getConvertedBooking() {
        return convertedBooking;
    }

    public void setConvertedBooking(Booking convertedBooking) {
        this.convertedBooking = convertedBooking;
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
        BookingWaitlist that = (BookingWaitlist) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
