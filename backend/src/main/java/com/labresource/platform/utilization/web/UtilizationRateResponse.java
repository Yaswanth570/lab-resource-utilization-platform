package com.labresource.platform.utilization.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UtilizationRateResponse {

    private Long equipmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal utilizationPercentage;

    public UtilizationRateResponse() {
    }

    public UtilizationRateResponse(Long equipmentId, LocalDate startDate, LocalDate endDate, BigDecimal utilizationPercentage) {
        this.equipmentId = equipmentId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.utilizationPercentage = utilizationPercentage;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
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

    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}
