package com.labresource.platform.cost.web;

import java.math.BigDecimal;

public class DepartmentCostSummaryResponse {

    private Long departmentId;
    private String departmentName;
    private Long institutionId;
    private String institutionName;
    private int totalBookingsCount;
    private int unbilledBookingsCount;
    private BigDecimal unbilledCost = BigDecimal.ZERO;
    private BigDecimal invoicedCost = BigDecimal.ZERO;
    private BigDecimal settledCost = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;

    public DepartmentCostSummaryResponse() {
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

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public int getTotalBookingsCount() {
        return totalBookingsCount;
    }

    public void setTotalBookingsCount(int totalBookingsCount) {
        this.totalBookingsCount = totalBookingsCount;
    }

    public int getUnbilledBookingsCount() {
        return unbilledBookingsCount;
    }

    public void setUnbilledBookingsCount(int unbilledBookingsCount) {
        this.unbilledBookingsCount = unbilledBookingsCount;
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
