package com.labresource.platform.analytics.web;

import com.labresource.platform.analytics.service.AnalyticsService;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.security.principal.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ==========================================
    // 1. Overview
    // ==========================================

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewResponse> getOverview(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        Long scopedDeptId = resolveScopedDepartmentId(departmentId);
        AnalyticsOverviewResponse response = analyticsService.getOverview(institutionId, scopedDeptId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. Utilization
    // ==========================================

    @GetMapping("/utilization")
    public ResponseEntity<UtilizationAnalyticsResponse> getUtilization(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        UtilizationAnalyticsResponse response = analyticsService.getUtilizationAnalytics(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/utilization/equipment/{equipmentId}")
    public ResponseEntity<EquipmentUtilizationDetailResponse> getEquipmentUtilization(
            @PathVariable Long equipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        EquipmentUtilizationDetailResponse response = analyticsService.getEquipmentUtilization(equipmentId, startDate, endDate, institutionId);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. Bookings
    // ==========================================

    @GetMapping("/bookings")
    public ResponseEntity<BookingAnalyticsResponse> getBookings(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        BookingAnalyticsResponse response = analyticsService.getBookingAnalytics(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 4. Maintenance & Downtime
    // ==========================================

    @GetMapping("/maintenance")
    public ResponseEntity<MaintenanceAnalyticsResponse> getMaintenance(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        MaintenanceAnalyticsResponse response = analyticsService.getMaintenanceAnalytics(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 5. Cost
    // ==========================================

    @GetMapping("/cost")
    public ResponseEntity<CostAnalyticsResponse> getCost(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        CostAnalyticsResponse response = analyticsService.getCostAnalytics(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 6. Equipment Performance
    // ==========================================

    @GetMapping("/equipment-performance")
    public ResponseEntity<EquipmentPerformanceResponse> getEquipmentPerformance(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        EquipmentPerformanceResponse response = analyticsService.getEquipmentPerformance(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 7. Trends
    // ==========================================

    @GetMapping("/trends")
    public ResponseEntity<TrendAnalyticsResponse> getTrends(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long institutionId = getAuthenticatedInstitutionId();
        TrendAnalyticsResponse response = analyticsService.getTrends(institutionId, departmentId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedInstitutionId() {
        return SecurityUtils.getCurrentInstitutionId()
                .orElseThrow(() -> new InvalidOperationException("No authenticated institution context"));
    }

    private Long resolveScopedDepartmentId(Long requestedDeptId) {
        if (SecurityUtils.isDepartmentHead() && !SecurityUtils.hasAnyRole("ROLE_INSTITUTION_ADMINISTRATOR", "ROLE_SYSTEM_ADMINISTRATOR")) {
            Long callerDeptId = SecurityUtils.getCurrentDepartmentId().orElse(null);
            if (callerDeptId != null) {
                if (requestedDeptId != null && !callerDeptId.equals(requestedDeptId)) {
                    throw new AccessDeniedException("Tenant isolation: Cannot view analytics of another department");
                }
                return callerDeptId;
            }
        }
        return requestedDeptId;
    }
}
