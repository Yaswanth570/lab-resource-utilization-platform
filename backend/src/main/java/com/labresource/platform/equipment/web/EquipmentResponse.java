package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.Equipment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class EquipmentResponse {

    private Long id;
    private Long institutionId;
    private Long departmentId;
    private Long categoryId;
    private String categoryName;
    private Long primaryLabManagerId;
    private String primaryLabManagerName;
    private String name;
    private String assetTag;
    private String serialNumber;
    private String modelNumber;
    private String manufacturer;
    private String locationBuilding;
    private String locationRoom;
    private String status;
    private String operationalStatusReason;
    private boolean isShareableExternally;
    private BigDecimal hourlyRateInternal;
    private BigDecimal hourlyRateExternal;
    private Integer minBookingDurationMins;
    private Integer maxBookingDurationMins;
    private Integer bufferTimeMins;
    private boolean requiresTrainingCertification;
    private boolean requiresApproval;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private LocalDate warrantyExpiryDate;
    private Instant createdAt;
    private Instant updatedAt;

    public EquipmentResponse() {
    }

    public static EquipmentResponse fromEntity(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        EquipmentResponse res = new EquipmentResponse();
        res.setId(equipment.getId());
        res.setInstitutionId(equipment.getInstitution() != null ? equipment.getInstitution().getId() : null);
        res.setDepartmentId(equipment.getDepartment() != null ? equipment.getDepartment().getId() : null);

        if (equipment.getCategory() != null) {
            res.setCategoryId(equipment.getCategory().getId());
            try {
                res.setCategoryName(equipment.getCategory().getName());
            } catch (Exception ignored) {
            }
        }

        if (equipment.getPrimaryLabManager() != null) {
            res.setPrimaryLabManagerId(equipment.getPrimaryLabManager().getId());
            try {
                res.setPrimaryLabManagerName(equipment.getPrimaryLabManager().getFirstName() + " " + equipment.getPrimaryLabManager().getLastName());
            } catch (Exception ignored) {
            }
        }

        res.setName(equipment.getName());
        res.setAssetTag(equipment.getAssetTag());
        res.setSerialNumber(equipment.getSerialNumber());
        res.setModelNumber(equipment.getModelNumber());
        res.setManufacturer(equipment.getManufacturer());
        res.setLocationBuilding(equipment.getLocationBuilding());
        res.setLocationRoom(equipment.getLocationRoom());
        res.setStatus(equipment.getStatus() != null ? equipment.getStatus().name() : null);
        res.setOperationalStatusReason(equipment.getOperationalStatusReason());
        res.setShareableExternally(equipment.isShareableExternally());
        res.setHourlyRateInternal(equipment.getHourlyRateInternal());
        res.setHourlyRateExternal(equipment.getHourlyRateExternal());
        res.setMinBookingDurationMins(equipment.getMinBookingDurationMins());
        res.setMaxBookingDurationMins(equipment.getMaxBookingDurationMins());
        res.setBufferTimeMins(equipment.getBufferTimeMins());
        res.setRequiresTrainingCertification(equipment.isRequiresTrainingCertification());
        res.setRequiresApproval(equipment.isRequiresApproval());
        res.setPurchaseDate(equipment.getPurchaseDate());
        res.setPurchaseCost(equipment.getPurchaseCost());
        res.setWarrantyExpiryDate(equipment.getWarrantyExpiryDate());
        res.setCreatedAt(equipment.getCreatedAt());
        res.setUpdatedAt(equipment.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getPrimaryLabManagerId() {
        return primaryLabManagerId;
    }

    public void setPrimaryLabManagerId(Long primaryLabManagerId) {
        this.primaryLabManagerId = primaryLabManagerId;
    }

    public String getPrimaryLabManagerName() {
        return primaryLabManagerName;
    }

    public void setPrimaryLabManagerName(String primaryLabManagerName) {
        this.primaryLabManagerName = primaryLabManagerName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}
