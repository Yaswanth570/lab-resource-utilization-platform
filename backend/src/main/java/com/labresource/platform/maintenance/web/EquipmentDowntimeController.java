package com.labresource.platform.maintenance.web;

import com.labresource.platform.maintenance.EquipmentDowntimeLog;
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
@RequestMapping("/api/maintenance/downtime")
public class EquipmentDowntimeController {

    private final MaintenanceService maintenanceService;

    public EquipmentDowntimeController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<DowntimeLogResponse> recordDowntimeLog(@Valid @RequestBody RecordDowntimeLogDto dto) {
        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setReasonCategory(dto.getReasonCategory());
        log.setDowntimeStart(dto.getDowntimeStart());
        log.setDowntimeEnd(dto.getDowntimeEnd());
        log.setDurationMinutes(dto.getDurationMinutes());
        log.setDescription(dto.getDescription());

        EquipmentDowntimeLog created = maintenanceService.recordDowntimeLog(
                log,
                dto.getEquipmentId(),
                dto.getWorkOrderId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(DowntimeLogResponse.from(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<DowntimeLogResponse> getDowntimeLogById(@PathVariable Long id) {
        EquipmentDowntimeLog log = maintenanceService.getDowntimeLogById(id);
        return ResponseEntity.ok(DowntimeLogResponse.from(log));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<List<DowntimeLogResponse>> listDowntimeLogs(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long workOrderId) {

        List<EquipmentDowntimeLog> list;
        if (equipmentId != null) {
            list = maintenanceService.listDowntimeLogsByEquipment(equipmentId);
        } else if (workOrderId != null) {
            list = maintenanceService.listDowntimeLogsByWorkOrder(workOrderId);
        } else {
            list = maintenanceService.listDowntimeLogs();
        }

        List<DowntimeLogResponse> response = list.stream()
                .map(DowntimeLogResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<DowntimeLogResponse> endDowntimeLog(
            @PathVariable Long id,
            @RequestBody(required = false) EndDowntimeLogDto dto) {
        Instant end = (dto != null && dto.getDowntimeEnd() != null) ? dto.getDowntimeEnd() : Instant.now();
        Integer duration = (dto != null) ? dto.getDurationMinutes() : null;

        EquipmentDowntimeLog ended = maintenanceService.endDowntimeLog(id, end, duration);
        return ResponseEntity.ok(DowntimeLogResponse.from(ended));
    }
}
