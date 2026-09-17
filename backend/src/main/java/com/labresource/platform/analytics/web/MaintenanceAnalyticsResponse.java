package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceAnalyticsResponse {

    private long totalRequests;
    private long totalWorkOrders;
    private long totalDowntimeMinutes;
    private BigDecimal totalDowntimeHours = BigDecimal.ZERO;
    private Map<String, Long> requestStatusDistribution = new LinkedHashMap<>();
    private Map<String, Long> requestPriorityDistribution = new LinkedHashMap<>();
    private Map<String, Long> workOrderStatusDistribution = new LinkedHashMap<>();
    private List<DowntimeCategoryMetric> downtimeByCategory = new ArrayList<>();
    private List<EquipmentDowntimeMetric> equipmentDowntime = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    public MaintenanceAnalyticsResponse() {
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getTotalWorkOrders() {
        return totalWorkOrders;
    }

    public void setTotalWorkOrders(long totalWorkOrders) {
        this.totalWorkOrders = totalWorkOrders;
    }

    public long getTotalDowntimeMinutes() {
        return totalDowntimeMinutes;
    }

    public void setTotalDowntimeMinutes(long totalDowntimeMinutes) {
        this.totalDowntimeMinutes = totalDowntimeMinutes;
    }

    public BigDecimal getTotalDowntimeHours() {
        return totalDowntimeHours;
    }

    public void setTotalDowntimeHours(BigDecimal totalDowntimeHours) {
        this.totalDowntimeHours = totalDowntimeHours;
    }

    public Map<String, Long> getRequestStatusDistribution() {
        return requestStatusDistribution;
    }

    public void setRequestStatusDistribution(Map<String, Long> requestStatusDistribution) {
        this.requestStatusDistribution = requestStatusDistribution;
    }

    public Map<String, Long> getRequestPriorityDistribution() {
        return requestPriorityDistribution;
    }

    public void setRequestPriorityDistribution(Map<String, Long> requestPriorityDistribution) {
        this.requestPriorityDistribution = requestPriorityDistribution;
    }

    public Map<String, Long> getWorkOrderStatusDistribution() {
        return workOrderStatusDistribution;
    }

    public void setWorkOrderStatusDistribution(Map<String, Long> workOrderStatusDistribution) {
        this.workOrderStatusDistribution = workOrderStatusDistribution;
    }

    public List<DowntimeCategoryMetric> getDowntimeByCategory() {
        return downtimeByCategory;
    }

    public void setDowntimeByCategory(List<DowntimeCategoryMetric> downtimeByCategory) {
        this.downtimeByCategory = downtimeByCategory;
    }

    public List<EquipmentDowntimeMetric> getEquipmentDowntime() {
        return equipmentDowntime;
    }

    public void setEquipmentDowntime(List<EquipmentDowntimeMetric> equipmentDowntime) {
        this.equipmentDowntime = equipmentDowntime;
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

    // ==========================================
    // Nested Classes
    // ==========================================

    public static class DowntimeCategoryMetric {
        private String category;
        private long durationMinutes;
        private BigDecimal durationHours = BigDecimal.ZERO;
        private long incidentCount;
        private BigDecimal percentageOfTotal = BigDecimal.ZERO;

        public DowntimeCategoryMetric() {
        }

        public DowntimeCategoryMetric(String category, long durationMinutes, long incidentCount, BigDecimal percentageOfTotal) {
            this.category = category;
            this.durationMinutes = durationMinutes;
            this.durationHours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.incidentCount = incidentCount;
            this.percentageOfTotal = percentageOfTotal;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(long durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public BigDecimal getDurationHours() {
            return durationHours;
        }

        public void setDurationHours(BigDecimal durationHours) {
            this.durationHours = durationHours;
        }

        public long getIncidentCount() {
            return incidentCount;
        }

        public void setIncidentCount(long incidentCount) {
            this.incidentCount = incidentCount;
        }

        public BigDecimal getPercentageOfTotal() {
            return percentageOfTotal;
        }

        public void setPercentageOfTotal(BigDecimal percentageOfTotal) {
            this.percentageOfTotal = percentageOfTotal;
        }
    }

    public static class EquipmentDowntimeMetric {
        private Long equipmentId;
        private String equipmentName;
        private long durationMinutes;
        private BigDecimal durationHours = BigDecimal.ZERO;
        private long incidentCount;

        public EquipmentDowntimeMetric() {
        }

        public EquipmentDowntimeMetric(Long equipmentId, String equipmentName, long durationMinutes, long incidentCount) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.durationMinutes = durationMinutes;
            this.durationHours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.incidentCount = incidentCount;
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

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(long durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public BigDecimal getDurationHours() {
            return durationHours;
        }

        public void setDurationHours(BigDecimal durationHours) {
            this.durationHours = durationHours;
        }

        public long getIncidentCount() {
            return incidentCount;
        }

        public void setIncidentCount(long incidentCount) {
            this.incidentCount = incidentCount;
        }
    }
}
