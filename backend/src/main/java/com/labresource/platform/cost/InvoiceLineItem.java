package com.labresource.platform.cost;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.utilization.EquipmentUsageSession;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "invoice_line_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_ili_booking", columnNames = {"booking_id"})
}, indexes = {
    @Index(name = "idx_ili_invoice", columnList = "invoice_id"),
    @Index(name = "idx_ili_equip", columnList = "equipment_id")
})
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private BillingInvoice invoice;

    /**
     * Unique mapping ensures a booking cannot appear on more than one invoice line item.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_session_id")
    private EquipmentUsageSession usageSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "billable_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal billableHours;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "total_line_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLineCost;

    @Column(name = "penalty_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public InvoiceLineItem() {
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

    public BillingInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(BillingInvoice invoice) {
        this.invoice = invoice;
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

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(BigDecimal billableHours) {
        this.billableHours = billableHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getTotalLineCost() {
        return totalLineCost;
    }

    public void setTotalLineCost(BigDecimal totalLineCost) {
        this.totalLineCost = totalLineCost;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
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
        InvoiceLineItem that = (InvoiceLineItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
