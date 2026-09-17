package com.labresource.platform.equipment;

import com.labresource.platform.department.Department;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "equipment", uniqueConstraints = {
    @UniqueConstraint(name = "uq_eq_asset_tag", columnNames = {"asset_tag"}),
    @UniqueConstraint(name = "uq_eq_serial_number", columnNames = {"serial_number"}),
    @UniqueConstraint(name = "uq_eq_id_institution", columnNames = {"id", "institution_id"})
}, indexes = {
    @Index(name = "idx_eq_search", columnList = "institution_id, department_id, category_id, status"),
    @Index(name = "idx_eq_shareable", columnList = "is_shareable_externally"),
    @Index(name = "idx_eq_deleted_at", columnList = "deleted_at")
})
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private EquipmentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_lab_manager_id")
    private User primaryLabManager;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "asset_tag", nullable = false, unique = true, length = 100)
    private String assetTag;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(name = "manufacturer", length = 150)
    private String manufacturer;

    @Column(name = "location_building", nullable = false, length = 100)
    private String locationBuilding;

    @Column(name = "location_room", nullable = false, length = 50)
    private String locationRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(name = "operational_status_reason", length = 255)
    private String operationalStatusReason;

    @Column(name = "is_shareable_externally", nullable = false)
    private boolean isShareableExternally = false;

    @Column(name = "hourly_rate_internal", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRateInternal = BigDecimal.ZERO;

    @Column(name = "hourly_rate_external", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRateExternal = BigDecimal.ZERO;

    @Column(name = "min_booking_duration_mins", nullable = false)
    private Integer minBookingDurationMins = 30;

    @Column(name = "max_booking_duration_mins", nullable = false)
    private Integer maxBookingDurationMins = 480;

    @Column(name = "buffer_time_mins", nullable = false)
    private Integer bufferTimeMins = 15;

    @Column(name = "requires_training_certification", nullable = false)
    private boolean requiresTrainingCertification = false;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval = false;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 12, scale = 2)
    private BigDecimal purchaseCost;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Equipment() {
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public void setCategory(EquipmentCategory category) {
        this.category = category;
    }

    public User getPrimaryLabManager() {
        return primaryLabManager;
    }

    public void setPrimaryLabManager(User primaryLabManager) {
        this.primaryLabManager = primaryLabManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getLocationBuilding() {
        return locationBuilding;
    }

    public void setLocationBuilding(String locationBuilding) {
        this.locationBuilding = locationBuilding;
    }

    public String getLocationRoom() {
        return locationRoom;
    }

    public void setLocationRoom(String locationRoom) {
        this.locationRoom = locationRoom;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public String getOperationalStatusReason() {
        return operationalStatusReason;
    }

    public void setOperationalStatusReason(String operationalStatusReason) {
        this.operationalStatusReason = operationalStatusReason;
    }

    public boolean isShareableExternally() {
        return isShareableExternally;
    }

    public void setShareableExternally(boolean shareableExternally) {
        isShareableExternally = shareableExternally;
    }

    public BigDecimal getHourlyRateInternal() {
        return hourlyRateInternal;
    }

    public void setHourlyRateInternal(BigDecimal hourlyRateInternal) {
        this.hourlyRateInternal = hourlyRateInternal;
    }

    public BigDecimal getHourlyRateExternal() {
        return hourlyRateExternal;
    }

    public void setHourlyRateExternal(BigDecimal hourlyRateExternal) {
        this.hourlyRateExternal = hourlyRateExternal;
    }

    public Integer getMinBookingDurationMins() {
        return minBookingDurationMins;
    }

    public void setMinBookingDurationMins(Integer minBookingDurationMins) {
        this.minBookingDurationMins = minBookingDurationMins;
    }

    public Integer getMaxBookingDurationMins() {
        return maxBookingDurationMins;
    }

    public void setMaxBookingDurationMins(Integer maxBookingDurationMins) {
        this.maxBookingDurationMins = maxBookingDurationMins;
    }

    public Integer getBufferTimeMins() {
        return bufferTimeMins;
    }

    public void setBufferTimeMins(Integer bufferTimeMins) {
        this.bufferTimeMins = bufferTimeMins;
    }

    public boolean isRequiresTrainingCertification() {
        return requiresTrainingCertification;
    }

    public void setRequiresTrainingCertification(boolean requiresTrainingCertification) {
        this.requiresTrainingCertification = requiresTrainingCertification;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(BigDecimal purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(id, equipment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
