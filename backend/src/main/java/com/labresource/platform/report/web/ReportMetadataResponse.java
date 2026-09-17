package com.labresource.platform.report.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportMetadataResponse {

    private String reportType;
    private String title;
    private LocalDateTime generatedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long institutionId;
    private Long departmentId;
    private int recordCount;

    public ReportMetadataResponse() {
    }

    public ReportMetadataResponse(String reportType, String title, LocalDateTime generatedAt,
                                  LocalDate startDate, LocalDate endDate,
                                  Long institutionId, Long departmentId, int recordCount) {
        this.reportType = reportType;
        this.title = title;
        this.generatedAt = generatedAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.institutionId = institutionId;
        this.departmentId = departmentId;
        this.recordCount = recordCount;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
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

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
}
