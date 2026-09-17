package com.labresource.platform.calibration;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "equipment_calibrations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cal_ref", columnNames = {"calibration_reference"})
}, indexes = {
    @Index(name = "idx_cal_expiry", columnList = "equipment_id, next_due_date, status")
})
public class EquipmentCalibration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calibration_reference", nullable = false, unique = true, length = 50)
    private String calibrationReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id")
    private User performedByUser;

    @Column(name = "external_vendor_name", length = 150)
    private String externalVendorName;

    @Column(name = "calibration_date", nullable = false)
    private LocalDate calibrationDate;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(name = "frequency_months", nullable = false)
    private Integer frequencyMonths = 12;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 30)
    private CalibrationResult result;

    @Column(name = "calibration_standard", length = 100)
    private String calibrationStandard;

    @Column(name = "measured_deviation", length = 100)
    private String measuredDeviation;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "certificate_document_url", length = 500)
    private String certificateDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CalibrationStatus status = CalibrationStatus.VALID;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EquipmentCalibration() {
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

    public String getCalibrationReference() {
        return calibrationReference;
    }

    public void setCalibrationReference(String calibrationReference) {
        this.calibrationReference = calibrationReference;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public User getPerformedByUser() {
        return performedByUser;
    }

    public void setPerformedByUser(User performedByUser) {
        this.performedByUser = performedByUser;
    }

    public String getExternalVendorName() {
        return externalVendorName;
    }

    public void setExternalVendorName(String externalVendorName) {
        this.externalVendorName = externalVendorName;
    }

    public LocalDate getCalibrationDate() {
        return calibrationDate;
    }

    public void setCalibrationDate(LocalDate calibrationDate) {
        this.calibrationDate = calibrationDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public Integer getFrequencyMonths() {
        return frequencyMonths;
    }

    public void setFrequencyMonths(Integer frequencyMonths) {
        this.frequencyMonths = frequencyMonths;
    }

    public CalibrationResult getResult() {
        return result;
    }

    public void setResult(CalibrationResult result) {
        this.result = result;
    }

    public String getCalibrationStandard() {
        return calibrationStandard;
    }

    public void setCalibrationStandard(String calibrationStandard) {
        this.calibrationStandard = calibrationStandard;
    }

    public String getMeasuredDeviation() {
        return measuredDeviation;
    }

    public void setMeasuredDeviation(String measuredDeviation) {
        this.measuredDeviation = measuredDeviation;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getCertificateDocumentUrl() {
        return certificateDocumentUrl;
    }

    public void setCertificateDocumentUrl(String certificateDocumentUrl) {
        this.certificateDocumentUrl = certificateDocumentUrl;
    }

    public CalibrationStatus getStatus() {
        return status;
    }

    public void setStatus(CalibrationStatus status) {
        this.status = status;
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
        EquipmentCalibration that = (EquipmentCalibration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
