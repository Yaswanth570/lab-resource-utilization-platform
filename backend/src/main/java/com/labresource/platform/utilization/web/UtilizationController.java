package com.labresource.platform.utilization.web;

import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.utilization.EquipmentIdleEvent;
import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.IdleEventStatus;
import com.labresource.platform.utilization.SessionStatus;
import com.labresource.platform.utilization.service.UtilizationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/utilization")
@PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class UtilizationController {

    private final UtilizationService utilizationService;

    public UtilizationController(UtilizationService utilizationService) {
        this.utilizationService = utilizationService;
    }

    // --- Usage Sessions ---

    @PostMapping("/sessions")
    public ResponseEntity<UsageSessionResponse> createUsageSession(@Valid @RequestBody CreateUsageSessionRequest request) {
        Long userId = request.getUserId();
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId().orElse(null);
        }

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(request.getCheckedInAt() != null ? request.getCheckedInAt() : Instant.now());
        session.setCheckedOutAt(request.getCheckedOutAt());
        session.setScheduledDurationMinutes(request.getScheduledDurationMinutes());
        session.setActualDurationMinutes(request.getActualDurationMinutes());
        if (request.getSessionStatus() != null) {
            session.setSessionStatus(request.getSessionStatus());
        }
        session.setNotes(request.getNotes());

        EquipmentUsageSession created = utilizationService.createUsageSession(
                session,
                request.getEquipmentId(),
                userId,
                request.getBookingId(),
                request.getDepartmentId(),
                request.getInstitutionId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(UsageSessionResponse.from(created));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<UsageSessionResponse> getUsageSessionById(@PathVariable Long id) {
        EquipmentUsageSession session = utilizationService.getUsageSessionById(id);
        return ResponseEntity.ok(UsageSessionResponse.from(session));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UsageSessionResponse>> listUsageSessions(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) SessionStatus status) {

        List<EquipmentUsageSession> list;
        if (equipmentId != null) {
            list = utilizationService.listUsageSessionsByEquipment(equipmentId);
        } else if (userId != null) {
            list = utilizationService.listUsageSessionsByUser(userId);
        } else if (institutionId != null) {
            list = utilizationService.listUsageSessionsByInstitution(institutionId);
        } else if (departmentId != null) {
            list = utilizationService.listUsageSessionsByDepartment(departmentId);
        } else if (status != null) {
            list = utilizationService.listUsageSessionsByStatus(status);
        } else {
            list = utilizationService.listUsageSessions();
        }

        List<UsageSessionResponse> response = list.stream()
                .map(UsageSessionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/booking/{bookingId}")
    public ResponseEntity<UsageSessionResponse> getUsageSessionByBooking(@PathVariable Long bookingId) {
        EquipmentUsageSession session = utilizationService.getUsageSessionByBooking(bookingId);
        return ResponseEntity.ok(UsageSessionResponse.from(session));
    }

    @PatchMapping("/sessions/{id}/complete")
    public ResponseEntity<UsageSessionResponse> completeUsageSession(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteUsageSessionRequest request) {
        Instant checkedOutAt = (request != null) ? request.getCheckedOutAt() : null;
        String notes = (request != null) ? request.getNotes() : null;
        EquipmentUsageSession completed = utilizationService.completeUsageSession(id, checkedOutAt, notes);
        return ResponseEntity.ok(UsageSessionResponse.from(completed));
    }

    @PatchMapping("/sessions/{id}/terminate-early")
    public ResponseEntity<UsageSessionResponse> terminateUsageSessionEarly(
            @PathVariable Long id,
            @RequestBody(required = false) CompleteUsageSessionRequest request) {
        Instant checkedOutAt = (request != null) ? request.getCheckedOutAt() : null;
        String notes = (request != null) ? request.getNotes() : null;
        EquipmentUsageSession terminated = utilizationService.terminateUsageSessionEarly(id, checkedOutAt, notes);
        return ResponseEntity.ok(UsageSessionResponse.from(terminated));
    }

    // --- Utilization Rate Calculation ---

    @GetMapping("/rate")
    public ResponseEntity<UtilizationRateResponse> getUtilizationRate(
            @RequestParam Long equipmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal percentage = utilizationService.calculateEquipmentUtilizationPercentage(equipmentId, startDate, endDate);
        return ResponseEntity.ok(new UtilizationRateResponse(equipmentId, startDate, endDate, percentage));
    }

    // --- Idle Events ---

    @PostMapping("/idle-events")
    public ResponseEntity<IdleEventResponse> recordIdleEvent(@Valid @RequestBody RecordIdleEventRequest request) {
        Long loggedByUserId = request.getLoggedByUserId();
        if (loggedByUserId == null) {
            loggedByUserId = SecurityUtils.getCurrentUserId().orElse(null);
        }

        EquipmentIdleEvent event = new EquipmentIdleEvent();
        event.setDetectionSource(request.getDetectionSource());
        event.setIdleStartTime(request.getIdleStartTime());
        event.setIdleEndTime(request.getIdleEndTime());
        event.setIdleDurationMinutes(request.getIdleDurationMinutes());
        event.setNotes(request.getNotes());

        EquipmentIdleEvent recorded = utilizationService.recordIdleEvent(
                event,
                request.getEquipmentId(),
                request.getBookingId(),
                request.getUsageSessionId(),
                loggedByUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(IdleEventResponse.from(recorded));
    }

    @GetMapping("/idle-events/{id}")
    public ResponseEntity<IdleEventResponse> getIdleEventById(@PathVariable Long id) {
        EquipmentIdleEvent event = utilizationService.getIdleEventById(id);
        return ResponseEntity.ok(IdleEventResponse.from(event));
    }

    @GetMapping("/idle-events")
    public ResponseEntity<List<IdleEventResponse>> listIdleEvents(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) IdleEventStatus status,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long usageSessionId) {

        List<EquipmentIdleEvent> list;
        if (equipmentId != null && status != null) {
            list = utilizationService.listIdleEventsByStatus(equipmentId, status);
        } else if (equipmentId != null) {
            list = utilizationService.listIdleEventsByEquipment(equipmentId);
        } else if (bookingId != null) {
            list = utilizationService.listIdleEventsByBooking(bookingId);
        } else if (usageSessionId != null) {
            list = utilizationService.listIdleEventsByUsageSession(usageSessionId);
        } else {
            list = List.of();
        }

        List<IdleEventResponse> response = list.stream()
                .map(IdleEventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/idle-events/{id}/resolve")
    public ResponseEntity<IdleEventResponse> resolveIdleEvent(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveIdleEventRequest request) {
        Instant endTime = (request != null) ? request.getIdleEndTime() : null;
        String notes = (request != null) ? request.getNotes() : null;
        EquipmentIdleEvent resolved = utilizationService.resolveIdleEvent(id, endTime, notes);
        return ResponseEntity.ok(IdleEventResponse.from(resolved));
    }
}
