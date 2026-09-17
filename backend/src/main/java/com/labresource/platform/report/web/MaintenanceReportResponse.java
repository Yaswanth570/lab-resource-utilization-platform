package com.labresource.platform.report.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceReportResponse {

    private ReportMetadataResponse metadata;
    private MaintenanceSummary summary = new MaintenanceSummary();
    private Map<String, Long> requestStatusDistribution = new LinkedHashMap<>();
    private Map<String, Long> workOrderStatusDistribution = new LinkedHashMap<>();
    private List<MaintenanceCategoryRow> categoryRows = new ArrayList<>();
    private List<MaintenanceEquipmentRow> equipmentRows = new ArrayList<>();

    public MaintenanceReportResponse() {
    }

    public ReportMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(ReportMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public MaintenanceSummary getSummary() {
        return summary;
    }

    public void setSummary(MaintenanceSummary summary) {
        this.summary = summary;
    }

    public Map<String, Long> getRequestStatusDistribution() {
        return requestStatusDistribution;
    }

    public void setRequestStatusDistribution(Map<String, Long> requestStatusDistribution) {
        this.requestStatusDistribution = requestStatusDistribution;
    }

    public Map<String, Long> getWorkOrderStatusDistribution() {
        return workOrderStatusDistribution;
    }

    public void setWorkOrderStatusDistribution(Map<String, Long> workOrderStatusDistribution) {
        this.workOrderStatusDistribution = workOrderStatusDistribution;
    }

    public List<MaintenanceCategoryRow> getCategoryRows() {
        return categoryRows;
    }

    public void setCategoryRows(List<MaintenanceCategoryRow> categoryRows) {
        this.categoryRows = categoryRows;
    }

    public List<MaintenanceEquipmentRow> getEquipmentRows() {
        return equipmentRows;
    }

    public void setEquipmentRows(List<MaintenanceEquipmentRow> equipmentRows) {
        this.equipmentRows = equipmentRows;
    }

    public static class MaintenanceSummary {
        private long totalRequests;
        private long totalWorkOrders;
        private BigDecimal totalDowntimeHours = BigDecimal.ZERO;
        private long downtimeIncidentCount;

        public MaintenanceSummary() {
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
    }

    public static class MaintenanceCategoryRow {
        private String category;
        private long incidentCount;
        private BigDecimal durationHours = BigDecimal.ZERO;
        private BigDecimal percentageOfTotal = BigDecimal.ZERO;

        public MaintenanceCategoryRow() {
        }

        public MaintenanceCategoryRow(String category, long incidentCount, BigDecimal durationHours, BigDecimal percentageOfTotal) {
            this.category = category;
            this.incidentCount = incidentCount;
            this.durationHours = durationHours;
            this.percentageOfTotal = percentageOfTotal;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getIncidentCount() {
            return incidentCount;
        }

        public void setIncidentCount(long incidentCount) {
            this.incidentCount = incidentCount;
        }

        public BigDecimal getDurationHours() {
            return durationHours;
        }

        public void setDurationHours(BigDecimal durationHours) {
            this.durationHours = durationHours;
        }

        public BigDecimal getPercentageOfTotal() {
            return percentageOfTotal;
        }

        public void setPercentageOfTotal(BigDecimal percentageOfTotal) {
            this.percentageOfTotal = percentageOfTotal;
        }
    }

    public static class MaintenanceEquipmentRow {
        private Long equipmentId;
        private String equipmentName;
        private long incidentCount;
        private BigDecimal durationHours = BigDecimal.ZERO;

        public MaintenanceEquipmentRow() {
        }

        public MaintenanceEquipmentRow(Long equipmentId, String equipmentName, long incidentCount, BigDecimal durationHours) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.incidentCount = incidentCount;
            this.durationHours = durationHours;
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

        public long getIncidentCount() {
            return incidentCount;
        }

        public void setIncidentCount(long incidentCount) {
            this.incidentCount = incidentCount;
        }

        public BigDecimal getDurationHours() {
            return durationHours;
        }

        public void setDurationHours(BigDecimal durationHours) {
            this.durationHours = durationHours;
        }
    }
}
