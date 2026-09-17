package com.labresource.platform.report.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CostReportResponse {

    private ReportMetadataResponse metadata;
    private CostSummary summary = new CostSummary();
    private List<CostDepartmentRow> departmentRows = new ArrayList<>();
    private List<CostEquipmentRow> equipmentRows = new ArrayList<>();

    public CostReportResponse() {
    }

    public ReportMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(ReportMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public CostSummary getSummary() {
        return summary;
    }

    public void setSummary(CostSummary summary) {
        this.summary = summary;
    }

    public List<CostDepartmentRow> getDepartmentRows() {
        return departmentRows;
    }

    public void setDepartmentRows(List<CostDepartmentRow> departmentRows) {
        this.departmentRows = departmentRows;
    }

    public List<CostEquipmentRow> getEquipmentRows() {
        return equipmentRows;
    }

    public void setEquipmentRows(List<CostEquipmentRow> equipmentRows) {
        this.equipmentRows = equipmentRows;
    }

    public static class CostSummary {
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal unbilledCost = BigDecimal.ZERO;
        private BigDecimal invoicedCost = BigDecimal.ZERO;
        private BigDecimal settledCost = BigDecimal.ZERO;
        private long totalBookings;

        public CostSummary() {
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

        public long getTotalBookings() {
            return totalBookings;
        }

        public void setTotalBookings(long totalBookings) {
            this.totalBookings = totalBookings;
        }
    }

    public static class CostDepartmentRow {
        private Long departmentId;
        private String departmentName;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal unbilledCost = BigDecimal.ZERO;
        private BigDecimal invoicedCost = BigDecimal.ZERO;
        private BigDecimal settledCost = BigDecimal.ZERO;
        private long bookingCount;

        public CostDepartmentRow() {
        }

        public CostDepartmentRow(Long departmentId, String departmentName, BigDecimal totalCost,
                                 BigDecimal unbilledCost, BigDecimal invoicedCost, BigDecimal settledCost,
                                 long bookingCount) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.totalCost = totalCost;
            this.unbilledCost = unbilledCost;
            this.invoicedCost = invoicedCost;
            this.settledCost = settledCost;
            this.bookingCount = bookingCount;
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

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }
    }

    public static class CostEquipmentRow {
        private Long equipmentId;
        private String equipmentName;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal billableHours = BigDecimal.ZERO;
        private long bookingCount;

        public CostEquipmentRow() {
        }

        public CostEquipmentRow(Long equipmentId, String equipmentName, BigDecimal totalCost,
                                BigDecimal billableHours, long bookingCount) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.totalCost = totalCost;
            this.billableHours = billableHours;
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

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public BigDecimal getBillableHours() {
            return billableHours;
        }

        public void setBillableHours(BigDecimal billableHours) {
            this.billableHours = billableHours;
        }

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }
    }
}
