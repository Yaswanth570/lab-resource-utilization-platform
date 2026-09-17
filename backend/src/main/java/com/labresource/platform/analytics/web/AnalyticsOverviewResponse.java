package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnalyticsOverviewResponse {

    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long noShowBookings;
    private BigDecimal totalUsageHours = BigDecimal.ZERO;
    private BigDecimal totalOperatingHours = BigDecimal.ZERO;
    private BigDecimal averageUtilizationPercentage = BigDecimal.ZERO;
    private BigDecimal totalDowntimeHours = BigDecimal.ZERO;
    private long downtimeIncidentCount;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal unbilledCost = BigDecimal.ZERO;
    private BigDecimal invoicedCost = BigDecimal.ZERO;
    private BigDecimal settledCost = BigDecimal.ZERO;
    private long activeEquipmentCount;
    private long maintenanceRequestCount;
    private long openWorkOrderCount;
    private LocalDate startDate;
    private LocalDate endDate;

    public AnalyticsOverviewResponse() {
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public long getNoShowBookings() {
        return noShowBookings;
    }

    public void setNoShowBookings(long noShowBookings) {
        this.noShowBookings = noShowBookings;
    }

    public BigDecimal getTotalUsageHours() {
        return totalUsageHours;
    }

    public void setTotalUsageHours(BigDecimal totalUsageHours) {
        this.totalUsageHours = totalUsageHours;
    }

    public BigDecimal getTotalOperatingHours() {
        return totalOperatingHours;
    }

    public void setTotalOperatingHours(BigDecimal totalOperatingHours) {
        this.totalOperatingHours = totalOperatingHours;
    }

    public BigDecimal getAverageUtilizationPercentage() {
        return averageUtilizationPercentage;
    }

    public void setAverageUtilizationPercentage(BigDecimal averageUtilizationPercentage) {
        this.averageUtilizationPercentage = averageUtilizationPercentage;
    }

    public BigDecimal getTotalDowntimeHours() {
        return totalDowntimeHours;
    }

    public void setTotalDowntimeHours(BigDecimal totalDowntimeHours) {
        this.totalDowntimeHours = totalDowntimeHours;
    }

    public long getDowntimeIncidentCount() {
        return downtimeIncidentCount;
    }

    public void setDowntimeIncidentCount(long downtimeIncidentCount) {
        this.downtimeIncidentCount = downtimeIncidentCount;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getUnbilledCost() {
        return unbilledCost;
    }

    public void setUnbilledCost(BigDecimal unbilledCost) {
        this.unbilledCost = unbilledCost;
    }

    public BigDecimal getInvoicedCost() {
        return invoicedCost;
    }

    public void setInvoicedCost(BigDecimal invoicedCost) {
        this.invoicedCost = invoicedCost;
    }

    public BigDecimal getSettledCost() {
        return settledCost;
    }

    public void setSettledCost(BigDecimal settledCost) {
        this.settledCost = settledCost;
    }

    public long getActiveEquipmentCount() {
        return activeEquipmentCount;
    }

    public void setActiveEquipmentCount(long activeEquipmentCount) {
        this.activeEquipmentCount = activeEquipmentCount;
    }

    public long getMaintenanceRequestCount() {
        return maintenanceRequestCount;
    }

    public void setMaintenanceRequestCount(long maintenanceRequestCount) {
        this.maintenanceRequestCount = maintenanceRequestCount;
    }

    public long getOpenWorkOrderCount() {
        return openWorkOrderCount;
    }

    public void setOpenWorkOrderCount(long openWorkOrderCount) {
        this.openWorkOrderCount = openWorkOrderCount;
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
}
