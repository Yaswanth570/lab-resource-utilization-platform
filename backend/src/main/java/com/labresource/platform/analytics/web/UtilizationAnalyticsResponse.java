package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilizationAnalyticsResponse {

    private BigDecimal overallUtilizationPercentage = BigDecimal.ZERO;
    private BigDecimal totalOperatingHours = BigDecimal.ZERO;
    private BigDecimal totalUsageHours = BigDecimal.ZERO;
    private long totalSessions;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<EquipmentUtilizationMetric> equipmentMetrics = new ArrayList<>();
    private List<DailyUtilizationPoint> dailyTrends = new ArrayList<>();

    public UtilizationAnalyticsResponse() {
    }

    public BigDecimal getOverallUtilizationPercentage() {
        return overallUtilizationPercentage;
    }

    public void setOverallUtilizationPercentage(BigDecimal overallUtilizationPercentage) {
        this.overallUtilizationPercentage = overallUtilizationPercentage;
    }

    public BigDecimal getTotalOperatingHours() {
        return totalOperatingHours;
    }

    public void setTotalOperatingHours(BigDecimal totalOperatingHours) {
        this.totalOperatingHours = totalOperatingHours;
    }

    public BigDecimal getTotalUsageHours() {
        return totalUsageHours;
    }

    public void setTotalUsageHours(BigDecimal totalUsageHours) {
        this.totalUsageHours = totalUsageHours;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
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

    public List<EquipmentUtilizationMetric> getEquipmentMetrics() {
        return equipmentMetrics;
    }

    public void setEquipmentMetrics(List<EquipmentUtilizationMetric> equipmentMetrics) {
        this.equipmentMetrics = equipmentMetrics;
    }

    public List<DailyUtilizationPoint> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<DailyUtilizationPoint> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    // ==========================================
    // Nested Metric Classes
    // ==========================================

    public static class EquipmentUtilizationMetric {
        private Long equipmentId;
        private String equipmentName;
        private Long departmentId;
        private String departmentName;
        private long actualUsageMinutes;
        private BigDecimal actualUsageHours = BigDecimal.ZERO;
        private long operatingMinutes;
        private BigDecimal operatingHours = BigDecimal.ZERO;
        private BigDecimal utilizationPercentage = BigDecimal.ZERO;
        private long sessionCount;

        public EquipmentUtilizationMetric() {
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
    }

    public static class DailyUtilizationPoint {
        private LocalDate date;
        private long actualUsageMinutes;
        private BigDecimal actualUsageHours = BigDecimal.ZERO;
        private long operatingMinutes;
        private BigDecimal operatingHours = BigDecimal.ZERO;
        private BigDecimal utilizationPercentage = BigDecimal.ZERO;
        private long sessionCount;

        public DailyUtilizationPoint() {
        }

        public DailyUtilizationPoint(LocalDate date, long actualUsageMinutes, long operatingMinutes, BigDecimal utilizationPercentage, long sessionCount) {
            this.date = date;
            this.actualUsageMinutes = actualUsageMinutes;
            this.actualUsageHours = BigDecimal.valueOf(actualUsageMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.operatingMinutes = operatingMinutes;
            this.operatingHours = BigDecimal.valueOf(operatingMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.utilizationPercentage = utilizationPercentage;
            this.sessionCount = sessionCount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
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
    }
}
