package com.labresource.platform.report.web;

import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.report.service.ReportService;
import com.labresource.platform.security.principal.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Lists all available report types and supported formats.
     */
    @GetMapping
    public ResponseEntity<List<ReportTypeDefinition>> listReports() {
        return ResponseEntity.ok(reportService.getAvailableReports());
    }

    /**
     * Utilization Report
     */
    @GetMapping({"/utilization", "/utilization/preview"})
    public ResponseEntity<UtilizationReportResponse> getUtilizationReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        UtilizationReportResponse report = reportService.generateUtilizationReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Booking & Reservation Report
     */
    @GetMapping({"/bookings", "/bookings/preview"})
    public ResponseEntity<BookingReportResponse> getBookingReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        BookingReportResponse report = reportService.generateBookingReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Maintenance & Downtime Report
     */
    @GetMapping({"/maintenance", "/maintenance/preview"})
    public ResponseEntity<MaintenanceReportResponse> getMaintenanceReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        MaintenanceReportResponse report = reportService.generateMaintenanceReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Cost & Billing Report
     */
    @GetMapping({"/cost", "/cost/preview"})
    public ResponseEntity<CostReportResponse> getCostReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        CostReportResponse report = reportService.generateCostReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Equipment Inventory & Performance Report
     */
    @GetMapping({"/equipment", "/equipment/preview"})
    public ResponseEntity<EquipmentInventoryReportResponse> getEquipmentReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        EquipmentInventoryReportResponse report = reportService.generateEquipmentReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Management Executive Summary Report
     */
    @GetMapping({"/management", "/management/preview"})
    public ResponseEntity<ManagementSummaryReportResponse> getManagementReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        ManagementSummaryReportResponse report = reportService.generateManagementReport(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Generic dynamic endpoint for reports by path variable
     */
    @GetMapping({"/{type}", "/{type}/preview"})
    public ResponseEntity<?> getReportByType(
            @PathVariable String type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        switch (type.trim().toLowerCase()) {
            case "utilization":
                return ResponseEntity.ok(reportService.generateUtilizationReport(institutionId, departmentId, startDate, endDate));
            case "bookings":
            case "booking":
                return ResponseEntity.ok(reportService.generateBookingReport(institutionId, departmentId, startDate, endDate));
            case "maintenance":
                return ResponseEntity.ok(reportService.generateMaintenanceReport(institutionId, departmentId, startDate, endDate));
            case "cost":
                return ResponseEntity.ok(reportService.generateCostReport(institutionId, departmentId, startDate, endDate));
            case "equipment":
                return ResponseEntity.ok(reportService.generateEquipmentReport(institutionId, departmentId, startDate, endDate));
            case "management":
                return ResponseEntity.ok(reportService.generateManagementReport(institutionId, departmentId, startDate, endDate));
            default:
                throw new InvalidOperationException("Unsupported report type: " + type);
        }
    }

    /**
     * CSV Export endpoint
     */
    @GetMapping("/{type}/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable String type,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        byte[] csvBytes = reportService.exportReportCsv(type, institutionId, departmentId, startDate, endDate);

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);
        String filename = String.format("%s-report-%s-to-%s.csv", type.trim().toLowerCase(), start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    /**
     * PDF Export endpoint (Deferred / Unsupported)
     */
    @GetMapping("/{type}/pdf")
    public ResponseEntity<Void> exportPdf(@PathVariable String type) {
        throw new InvalidOperationException("PDF export is unsupported. PDF generation dependencies are deferred. Please export as CSV.");
    }

    private Long getAuthenticatedInstitutionId() {
        return SecurityUtils.getCurrentInstitutionId()
                .orElseThrow(() -> new InvalidOperationException("No authenticated institution context"));
    }
}
