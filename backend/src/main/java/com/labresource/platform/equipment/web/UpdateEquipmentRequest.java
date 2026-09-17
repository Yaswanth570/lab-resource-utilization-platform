package com.labresource.platform.equipment.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateEquipmentRequest {

    private Long departmentId;
    private Long categoryId;
    private Long primaryLabManagerId;
    private String name;
    private String modelNumber;
    private String manufacturer;
    private String locationBuilding;
    private String locationRoom;
    private Boolean isShareableExternally;
    private BigDecimal hourlyRateInternal;
    private BigDecimal hourlyRateExternal;
    private Integer minBookingDurationMins;
    private Integer maxBookingDurationMins;
    private Integer bufferTimeMins;
    private Boolean requiresTrainingCertification;
    private Boolean requiresApproval;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private LocalDate warrantyExpiryDate;

    public UpdateEquipmentRequest() {
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

    public Long getPrimaryLabManagerId() {
        return primaryLabManagerId;
    }

    public void setPrimaryLabManagerId(Long primaryLabManagerId) {
        this.primaryLabManagerId = primaryLabManagerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getShareableExternally() {
        return isShareableExternally;
    }

    public void setShareableExternally(Boolean shareableExternally) {
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

    public Boolean getRequiresTrainingCertification() {
        return requiresTrainingCertification;
    }

    public void setRequiresTrainingCertification(Boolean requiresTrainingCertification) {
        this.requiresTrainingCertification = requiresTrainingCertification;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
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
}
