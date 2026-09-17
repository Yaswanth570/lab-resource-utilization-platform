package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import com.labresource.platform.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/maintenance/work-orders")
public class MaintenanceWorkOrderController {

    private final MaintenanceService maintenanceService;

    public MaintenanceWorkOrderController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> createWorkOrder(@Valid @RequestBody CreateWorkOrderDto dto) {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(dto.getType());
        if (dto.getPriority() != null) {
            wo.setPriority(dto.getPriority());
        }
        wo.setScheduledStart(dto.getScheduledStart());
        wo.setScheduledEnd(dto.getScheduledEnd());
        wo.setWorkOrderNumber(dto.getWorkOrderNumber());

        MaintenanceWorkOrder created = maintenanceService.createWorkOrder(
                wo,
                dto.getEquipmentId(),
                dto.getMaintenanceRequestId(),
                dto.getAssignedTechnicianId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(WorkOrderResponse.from(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> getWorkOrderById(@PathVariable Long id) {
        MaintenanceWorkOrder wo = maintenanceService.getWorkOrderById(id);
        return ResponseEntity.ok(WorkOrderResponse.from(wo));
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> getWorkOrderByNumber(@PathVariable String number) {
        MaintenanceWorkOrder wo = maintenanceService.getWorkOrderByNumber(number);
        return ResponseEntity.ok(WorkOrderResponse.from(wo));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<List<WorkOrderResponse>> listWorkOrders(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long maintenanceRequestId,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long departmentId) {

        List<MaintenanceWorkOrder> list;
        if (equipmentId != null) {
            list = maintenanceService.listWorkOrdersByEquipment(equipmentId);
        } else if (technicianId != null) {
            list = maintenanceService.listWorkOrdersByTechnician(technicianId);
        } else if (maintenanceRequestId != null) {
            list = maintenanceService.listWorkOrdersByRequest(maintenanceRequestId);
        } else if (status != null) {
            list = maintenanceService.listWorkOrdersByStatus(status);
        } else if (institutionId != null) {
            list = maintenanceService.listWorkOrdersByInstitution(institutionId);
        } else if (departmentId != null) {
            list = maintenanceService.listWorkOrdersByDepartment(departmentId);
        } else {
            list = maintenanceService.listWorkOrders();
        }

        List<WorkOrderResponse> response = list.stream()
                .map(WorkOrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianDto dto) {
        MaintenanceWorkOrder updated = maintenanceService.assignTechnician(id, dto.getTechnicianId());
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> startWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) StartWorkOrderDto dto) {
        Instant actualStart = (dto != null && dto.getActualStart() != null) ? dto.getActualStart() : Instant.now();
        MaintenanceWorkOrder updated = maintenanceService.startWorkOrder(id, actualStart);
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> pauseWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PauseWorkOrderDto dto) {
        String reason = (dto != null) ? dto.getReason() : "Waiting for parts";
        MaintenanceWorkOrder updated = maintenanceService.pauseWorkOrder(id, reason);
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> resumeWorkOrder(@PathVariable Long id) {
        MaintenanceWorkOrder updated = maintenanceService.resumeWorkOrder(id);
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> completeWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteWorkOrderDto dto) {
        Instant actualEnd = (dto != null && dto.getActualEnd() != null) ? dto.getActualEnd() : Instant.now();
        MaintenanceWorkOrder updated = maintenanceService.completeWorkOrder(
                id,
                actualEnd,
                (dto != null) ? dto.getLaborHours() : null,
                (dto != null) ? dto.getLaborCost() : null,
                (dto != null) ? dto.getPartsCost() : null,
                (dto != null) ? dto.getWorkPerformedSummary() : null,
                (dto != null) ? dto.getFailureRootCause() : null,
                (dto != null) ? dto.getResolutionNotes() : null
        );
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<WorkOrderResponse> cancelWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) CancelWorkOrderDto dto) {
        String reason = (dto != null) ? dto.getCancellationReason() : null;
        MaintenanceWorkOrder updated = maintenanceService.cancelWorkOrder(id, reason);
        return ResponseEntity.ok(WorkOrderResponse.from(updated));
    }
}
