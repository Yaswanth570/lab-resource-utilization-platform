package com.labresource.platform.report.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EquipmentInventoryReportResponse {

    private ReportMetadataResponse metadata;
    private EquipmentInventorySummary summary = new EquipmentInventorySummary();
    private List<EquipmentInventoryRow> rows = new ArrayList<>();

    public EquipmentInventoryReportResponse() {
    }

    public ReportMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(ReportMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public EquipmentInventorySummary getSummary() {
        return summary;
    }

    public void setSummary(EquipmentInventorySummary summary) {
        this.summary = summary;
    }

    public List<EquipmentInventoryRow> getRows() {
        return rows;
    }

    public void setRows(List<EquipmentInventoryRow> rows) {
        this.rows = rows;
    }

    public static class EquipmentInventorySummary {
        private long totalEquipment;
        private long activeEquipment;
        private long operationalCount;
        private long underMaintenanceCount;
        private long decommissionedCount;

        public EquipmentInventorySummary() {
        }

        public long getTotalEquipment() {
            return totalEquipment;
        }

        public void setTotalEquipment(long totalEquipment) {
            this.totalEquipment = totalEquipment;
        }

        public long getActiveEquipment() {
            return activeEquipment;
        }

        public void setActiveEquipment(long activeEquipment) {
            this.activeEquipment = activeEquipment;
        }

        public long getOperationalCount() {
            return operationalCount;
        }

        public void setOperationalCount(long operationalCount) {
            this.operationalCount = operationalCount;
        }

        public long getUnderMaintenanceCount() {
            return underMaintenanceCount;
        }

        public void setUnderMaintenanceCount(long underMaintenanceCount) {
            this.underMaintenanceCount = underMaintenanceCount;
        }

        public long getDecommissionedCount() {
            return decommissionedCount;
        }

        public void setDecommissionedCount(long decommissionedCount) {
            this.decommissionedCount = decommissionedCount;
        }
    }

    public static class EquipmentInventoryRow {
        private Long equipmentId;
        private String equipmentName;
        private String model;
        private String serialNumber;
        private String departmentName;
        private String categoryName;
        private String status;
        private BigDecimal utilizationPercentage = BigDecimal.ZERO;
        private BigDecimal downtimeHours = BigDecimal.ZERO;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private long bookingCount;

        public EquipmentInventoryRow() {
        }

        public EquipmentInventoryRow(Long equipmentId, String equipmentName, String model, String serialNumber,
                                     String departmentName, String categoryName, String status,
                                     BigDecimal utilizationPercentage, BigDecimal downtimeHours,
                                     BigDecimal totalCost, long bookingCount) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.model = model;
            this.serialNumber = serialNumber;
            this.departmentName = departmentName;
            this.categoryName = categoryName;
            this.status = status;
            this.utilizationPercentage = utilizationPercentage;
            this.downtimeHours = downtimeHours;
            this.totalCost = totalCost;
            this.bookingCount = bookingCount;
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

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public BigDecimal getUtilizationPercentage() {
            return utilizationPercentage;
        }

        public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
            this.utilizationPercentage = utilizationPercentage;
        }

        public BigDecimal getDowntimeHours() {
            return downtimeHours;
        }

        public void setDowntimeHours(BigDecimal downtimeHours) {
            this.downtimeHours = downtimeHours;
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
