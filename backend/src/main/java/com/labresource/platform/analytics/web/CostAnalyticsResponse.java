package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CostAnalyticsResponse {

    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal unbilledCost = BigDecimal.ZERO;
    private BigDecimal invoicedCost = BigDecimal.ZERO;
    private BigDecimal settledCost = BigDecimal.ZERO;
    private List<DepartmentCostMetric> departmentCosts = new ArrayList<>();
    private List<EquipmentCostMetric> equipmentCosts = new ArrayList<>();
    private List<DailyCostPoint> dailyCosts = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    public CostAnalyticsResponse() {
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

    public List<DepartmentCostMetric> getDepartmentCosts() {
        return departmentCosts;
    }

    public void setDepartmentCosts(List<DepartmentCostMetric> departmentCosts) {
        this.departmentCosts = departmentCosts;
    }

    public List<EquipmentCostMetric> getEquipmentCosts() {
        return equipmentCosts;
    }

    public void setEquipmentCosts(List<EquipmentCostMetric> equipmentCosts) {
        this.equipmentCosts = equipmentCosts;
    }

    public List<DailyCostPoint> getDailyCosts() {
        return dailyCosts;
    }

    public void setDailyCosts(List<DailyCostPoint> dailyCosts) {
        this.dailyCosts = dailyCosts;
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

    public static class DepartmentCostMetric {
        private Long departmentId;
        private String departmentName;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal unbilledCost = BigDecimal.ZERO;
        private BigDecimal invoicedCost = BigDecimal.ZERO;
        private BigDecimal settledCost = BigDecimal.ZERO;
        private long bookingCount;

        public DepartmentCostMetric() {
        }

        public DepartmentCostMetric(Long departmentId, String departmentName, BigDecimal totalCost, BigDecimal unbilledCost, BigDecimal invoicedCost, BigDecimal settledCost, long bookingCount) {
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

    public static class EquipmentCostMetric {
        private Long equipmentId;
        private String equipmentName;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private BigDecimal billableHours = BigDecimal.ZERO;
        private long bookingCount;

        public EquipmentCostMetric() {
        }

        public EquipmentCostMetric(Long equipmentId, String equipmentName, BigDecimal totalCost, BigDecimal billableHours, long bookingCount) {
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

    public static class DailyCostPoint {
        private LocalDate date;
        private BigDecimal cost = BigDecimal.ZERO;
        private long bookingCount;

        public DailyCostPoint() {
        }

        public DailyCostPoint(LocalDate date, BigDecimal cost, long bookingCount) {
            this.date = date;
            this.cost = cost;
            this.bookingCount = bookingCount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }
    }
}
