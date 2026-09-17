package com.labresource.platform.cost.web;

import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.cost.service.CostService;
import com.labresource.platform.security.principal.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/cost")
@PreAuthorize("hasAnyRole('ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class CostController {

    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    // ==========================================
    // Usage Cost Endpoints
    // ==========================================

    @GetMapping("/usage")
    public ResponseEntity<List<UsageCostResponse>> listUsageCosts(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) BookingBillingStatus billingStatus) {

        Long institutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        List<UsageCostResponse> list = costService.listUsageCosts(institutionId, departmentId, equipmentId, billingStatus);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/usage/{bookingId}")
    public ResponseEntity<UsageCostResponse> getUsageCostByBookingId(@PathVariable Long bookingId) {
        UsageCostResponse res = costService.getUsageCostByBookingId(bookingId);
        return ResponseEntity.ok(res);
    }

    // ==========================================
    // Department Cost Summary Endpoints
    // ==========================================

    @GetMapping("/department")
    public ResponseEntity<List<DepartmentCostSummaryResponse>> listDepartmentCostSummaries() {
        Long institutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        List<DepartmentCostSummaryResponse> summaries = costService.listDepartmentCostSummaries(institutionId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<DepartmentCostSummaryResponse> getDepartmentCostSummary(@PathVariable Long departmentId) {
        Long institutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        DepartmentCostSummaryResponse summary = costService.getDepartmentCostSummary(departmentId, institutionId);
        return ResponseEntity.ok(summary);
    }
}
