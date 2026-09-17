package com.labresource.platform.report.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UtilizationReportResponse {

    private ReportMetadataResponse metadata;
    private UtilizationSummary summary = new UtilizationSummary();
    private List<UtilizationReportRow> rows = new ArrayList<>();

    public UtilizationReportResponse() {
    }

    public ReportMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(ReportMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public UtilizationSummary getSummary() {
        return summary;
    }

    public void setSummary(UtilizationSummary summary) {
        this.summary = summary;
    }

    public List<UtilizationReportRow> getRows() {
        return rows;
    }

    public void setRows(List<UtilizationReportRow> rows) {
        this.rows = rows;
    }

    public static class UtilizationSummary {
        private BigDecimal overallUtilizationPercentage = BigDecimal.ZERO;
        private BigDecimal totalOperatingHours = BigDecimal.ZERO;
        private BigDecimal totalUsageHours = BigDecimal.ZERO;
        private long totalSessions;
        private long equipmentCount;

        public UtilizationSummary() {
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

        public long getEquipmentCount() {
            return equipmentCount;
        }

        public void setEquipmentCount(long equipmentCount) {
            this.equipmentCount = equipmentCount;
        }
    }

    public static class UtilizationReportRow {
        private Long equipmentId;
        private String equipmentName;
        private Long departmentId;
        private String departmentName;
        private BigDecimal actualUsageHours = BigDecimal.ZERO;
        private BigDecimal operatingHours = BigDecimal.ZERO;
        private BigDecimal utilizationPercentage = BigDecimal.ZERO;
        private long sessionCount;

        public UtilizationReportRow() {
        }

        public UtilizationReportRow(Long equipmentId, String equipmentName, Long departmentId, String departmentName,
                                    BigDecimal actualUsageHours, BigDecimal operatingHours,
                                    BigDecimal utilizationPercentage, long sessionCount) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.actualUsageHours = actualUsageHours;
            this.operatingHours = operatingHours;
            this.utilizationPercentage = utilizationPercentage;
            this.sessionCount = sessionCount;
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

        public BigDecimal getActualUsageHours() {
            return actualUsageHours;
        }

        public void setActualUsageHours(BigDecimal actualUsageHours) {
            this.actualUsageHours = actualUsageHours;
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
