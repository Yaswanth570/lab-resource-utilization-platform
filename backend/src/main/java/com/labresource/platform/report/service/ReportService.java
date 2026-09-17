package com.labresource.platform.report.service;

import com.labresource.platform.report.web.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * Lists available report definitions with their metadata and supported formats.
     */
    List<ReportTypeDefinition> getAvailableReports();

    /**
     * Generates an Equipment Utilization Report for the institution.
     */
    UtilizationReportResponse generateUtilizationReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Generates a Booking & Reservation Report for the institution.
     */
    BookingReportResponse generateBookingReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Generates a Maintenance & Downtime Report for the institution.
     */
    MaintenanceReportResponse generateMaintenanceReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Generates a Cost & Billing Report for the institution.
     */
    CostReportResponse generateCostReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Generates a Comprehensive Equipment Inventory & Performance Report.
     */
    EquipmentInventoryReportResponse generateEquipmentReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Generates a Management Executive Summary Report uniting all domains.
     */
    ManagementSummaryReportResponse generateManagementReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Exports the requested report type as an RFC 4180 compliant CSV byte array.
     */
    byte[] exportReportCsv(String reportType, Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);
}
