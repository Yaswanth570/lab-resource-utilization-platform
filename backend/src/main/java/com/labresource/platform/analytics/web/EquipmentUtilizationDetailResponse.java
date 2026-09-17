package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EquipmentUtilizationDetailResponse {

    private Long equipmentId;
    private String equipmentName;
    private String modelNumber;
    private String serialNumber;
    private Long departmentId;
    private String departmentName;
    private long actualUsageMinutes;
    private BigDecimal actualUsageHours = BigDecimal.ZERO;
    private long operatingMinutes;
    private BigDecimal operatingHours = BigDecimal.ZERO;
    private BigDecimal utilizationPercentage = BigDecimal.ZERO;
    private long sessionCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<UtilizationAnalyticsResponse.DailyUtilizationPoint> dailyTrends = new ArrayList<>();

    public EquipmentUtilizationDetailResponse() {
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public long getActualUsageMinutes() {
        return actualUsageMinutes;
    }

    public void setActualUsageMinutes(long actualUsageMinutes) {
        this.actualUsageMinutes = actualUsageMinutes;
    }

    public BigDecimal getActualUsageHours() {
        return actualUsageHours;
    }

    public void setActualUsageHours(BigDecimal actualUsageHours) {
        this.actualUsageHours = actualUsageHours;
    }

    public long getOperatingMinutes() {
        return operatingMinutes;
    }

    public void setOperatingMinutes(long operatingMinutes) {
        this.operatingMinutes = operatingMinutes;
    }

    public BigDecimal getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(BigDecimal operatingHours) {
        this.operatingHours = operatingHours;
    }

    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<UtilizationAnalyticsResponse.DailyUtilizationPoint> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<UtilizationAnalyticsResponse.DailyUtilizationPoint> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }
}
