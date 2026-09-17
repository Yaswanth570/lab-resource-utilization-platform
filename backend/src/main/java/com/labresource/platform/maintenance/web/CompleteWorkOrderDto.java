package com.labresource.platform.maintenance.web;

import java.math.BigDecimal;
import java.time.Instant;

public class CompleteWorkOrderDto {

    private Instant actualEnd;
    private BigDecimal laborHours;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private String workPerformedSummary;
    private String failureRootCause;
    private String resolutionNotes;

    public CompleteWorkOrderDto() {
    }

    public Instant getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(Instant actualEnd) {
        this.actualEnd = actualEnd;
    }

    public BigDecimal getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(BigDecimal laborHours) {
        this.laborHours = laborHours;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(BigDecimal partsCost) {
        this.partsCost = partsCost;
    }

    public String getWorkPerformedSummary() {
        return workPerformedSummary;
    }

    public void setWorkPerformedSummary(String workPerformedSummary) {
        this.workPerformedSummary = workPerformedSummary;
    }

    public String getFailureRootCause() {
        return failureRootCause;
    }

    public void setFailureRootCause(String failureRootCause) {
        this.failureRootCause = failureRootCause;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
