package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenancePriority;
import com.labresource.platform.maintenance.MaintenanceRequest;
import com.labresource.platform.maintenance.MaintenanceRequestStatus;
import com.labresource.platform.maintenance.service.MaintenanceService;
import com.labresource.platform.security.principal.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/maintenance/requests")
public class MaintenanceRequestController {

    private final MaintenanceService maintenanceService;

    public MaintenanceRequestController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequestResponse> createMaintenanceRequest(@Valid @RequestBody CreateMaintenanceRequestDto dto) {
        Long reportedByUserId = dto.getReportedByUserId();
        if (reportedByUserId == null || SecurityUtils.isResearcher()) {
            reportedByUserId = SecurityUtils.getCurrentUserId().orElse(reportedByUserId);
        }

        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle(dto.getIssueTitle());
        req.setIssueDescription(dto.getIssueDescription());
        if (dto.getPriority() != null) {
            req.setPriority(dto.getPriority());
        }

        MaintenanceRequest created = maintenanceService.createMaintenanceRequest(
                req,
                dto.getEquipmentId(),
                reportedByUserId,
                dto.getInstitutionId(),
                dto.getDepartmentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(MaintenanceRequestResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponse> getRequestById(@PathVariable Long id) {
        MaintenanceRequest req = maintenanceService.getMaintenanceRequestById(id);
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            if (callerId == null || req.getReportedByUser() == null || !callerId.equals(req.getReportedByUser().getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own maintenance requests");
            }
        }
        return ResponseEntity.ok(MaintenanceRequestResponse.from(req));
    }

    @GetMapping("/number/{requestNumber}")
    public ResponseEntity<MaintenanceRequestResponse> getRequestByNumber(@PathVariable String requestNumber) {
        MaintenanceRequest req = maintenanceService.getMaintenanceRequestByNumber(requestNumber);
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            if (callerId == null || req.getReportedByUser() == null || !callerId.equals(req.getReportedByUser().getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own maintenance requests");
            }
        }
        return ResponseEntity.ok(MaintenanceRequestResponse.from(req));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceRequestResponse>> listRequests(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long reportedByUserId,
            @RequestParam(required = false) MaintenanceRequestStatus status,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long departmentId) {

        List<MaintenanceRequest> list;
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            list = maintenanceService.listMaintenanceRequestsByReportedUser(callerId);
        } else if (equipmentId != null) {
            list = maintenanceService.listMaintenanceRequestsByEquipment(equipmentId);
        } else if (reportedByUserId != null) {
            list = maintenanceService.listMaintenanceRequestsByReportedUser(reportedByUserId);
        } else if (status != null) {
            list = maintenanceService.listMaintenanceRequestsByStatus(status);
        } else if (institutionId != null) {
            list = maintenanceService.listMaintenanceRequestsByInstitution(institutionId);
        } else if (departmentId != null) {
            list = maintenanceService.listMaintenanceRequestsByDepartment(departmentId);
        } else {
            list = maintenanceService.listMaintenanceRequests();
        }

        List<MaintenanceRequestResponse> response = list.stream()
                .map(MaintenanceRequestResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/triage")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<MaintenanceRequestResponse> triageRequest(
            @PathVariable Long id,
            @RequestBody(required = false) TriageMaintenanceRequestDto dto) {
        Long triagedBy = (dto != null && dto.getTriagedByUserId() != null)
                ? dto.getTriagedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);
        MaintenancePriority priority = (dto != null) ? dto.getPriority() : null;

        MaintenanceRequest triaged = maintenanceService.triageMaintenanceRequest(id, triagedBy, priority);
        return ResponseEntity.ok(MaintenanceRequestResponse.from(triaged));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<MaintenanceRequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectMaintenanceRequestDto dto) {
        Long triagedBy = dto.getTriagedByUserId() != null
                ? dto.getTriagedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        MaintenanceRequest rejected = maintenanceService.rejectMaintenanceRequest(id, triagedBy, dto.getReason());
        return ResponseEntity.ok(MaintenanceRequestResponse.from(rejected));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<MaintenanceRequestResponse> resolveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ResolveMaintenanceRequestDto dto) {
        MaintenanceRequest resolved = maintenanceService.resolveMaintenanceRequest(id, dto.getResolutionNotes());
        return ResponseEntity.ok(MaintenanceRequestResponse.from(resolved));
    }
}
