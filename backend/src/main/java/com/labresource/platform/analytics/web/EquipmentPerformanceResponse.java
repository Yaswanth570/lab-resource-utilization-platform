package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EquipmentPerformanceResponse {

    private List<PerformanceItem> mostUtilized = new ArrayList<>();
    private List<PerformanceItem> leastUtilized = new ArrayList<>();
    private List<PerformanceItem> highestDowntime = new ArrayList<>();
    private List<PerformanceItem> highestCost = new ArrayList<>();
    private List<PerformanceItem> highestBookingFrequency = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    public EquipmentPerformanceResponse() {
    }

    public List<PerformanceItem> getMostUtilized() {
        return mostUtilized;
    }

    public void setMostUtilized(List<PerformanceItem> mostUtilized) {
        this.mostUtilized = mostUtilized;
    }

    public List<PerformanceItem> getLeastUtilized() {
        return leastUtilized;
    }

    public void setLeastUtilized(List<PerformanceItem> leastUtilized) {
        this.leastUtilized = leastUtilized;
    }

    public List<PerformanceItem> getHighestDowntime() {
        return highestDowntime;
    }

    public void setHighestDowntime(List<PerformanceItem> highestDowntime) {
        this.highestDowntime = highestDowntime;
    }

    public List<PerformanceItem> getHighestCost() {
        return highestCost;
    }

    public void setHighestCost(List<PerformanceItem> highestCost) {
        this.highestCost = highestCost;
    }

    public List<PerformanceItem> getHighestBookingFrequency() {
        return highestBookingFrequency;
    }

    public void setHighestBookingFrequency(List<PerformanceItem> highestBookingFrequency) {
        this.highestBookingFrequency = highestBookingFrequency;
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

    public static class PerformanceItem {
        private Long equipmentId;
        private String equipmentName;
        private String departmentName;
        private BigDecimal metricValue = BigDecimal.ZERO;
        private String metricUnit; // e.g. "%", "hours", "minutes", "$", "bookings"
        private BigDecimal utilizationPercentage = BigDecimal.ZERO;
        private BigDecimal totalUsageHours = BigDecimal.ZERO;
        private BigDecimal totalDowntimeHours = BigDecimal.ZERO;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private long bookingCount;

        public PerformanceItem() {
        }

        public PerformanceItem(Long equipmentId, String equipmentName, String departmentName, BigDecimal metricValue, String metricUnit) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.departmentName = departmentName;
            this.metricValue = metricValue;
            this.metricUnit = metricUnit;
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

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public BigDecimal getMetricValue() {
            return metricValue;
        }

        public void setMetricValue(BigDecimal metricValue) {
            this.metricValue = metricValue;
        }

        public String getMetricUnit() {
            return metricUnit;
        }

        public void setMetricUnit(String metricUnit) {
            this.metricUnit = metricUnit;
        }

        public BigDecimal getUtilizationPercentage() {
            return utilizationPercentage;
        }

        public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
            this.utilizationPercentage = utilizationPercentage;
        }

        public BigDecimal getTotalUsageHours() {
            return totalUsageHours;
        }

        public void setTotalUsageHours(BigDecimal totalUsageHours) {
            this.totalUsageHours = totalUsageHours;
        }

        public BigDecimal getTotalDowntimeHours() {
            return totalDowntimeHours;
        }

        public void setTotalDowntimeHours(BigDecimal totalDowntimeHours) {
            this.totalDowntimeHours = totalDowntimeHours;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }
    }
}
