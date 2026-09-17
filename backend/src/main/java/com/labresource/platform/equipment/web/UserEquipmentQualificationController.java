package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.UserEquipmentQualification;
import com.labresource.platform.equipment.service.UserEquipmentQualificationService;
import com.labresource.platform.security.principal.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qualifications")
public class UserEquipmentQualificationController {

    private final UserEquipmentQualificationService qualificationService;

    public UserEquipmentQualificationController(UserEquipmentQualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @PostMapping
    public ResponseEntity<QualificationResponse> createQualification(@Valid @RequestBody CreateQualificationRequest request) {
        UserEquipmentQualification q = new UserEquipmentQualification();
        q.setExpiresAt(request.getExpiresAt());
        q.setNotes(request.getNotes());

        Long certifierId = request.getCertifiedByUserId();
        if (certifierId == null) {
            certifierId = SecurityUtils.getCurrentUserId().orElse(null);
        }

        UserEquipmentQualification created = qualificationService.createQualification(
                q,
                request.getUserId(),
                request.getEquipmentId(),
                certifierId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(QualificationResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualificationResponse> getQualificationById(@PathVariable Long id) {
        UserEquipmentQualification q = qualificationService.getQualificationById(id);
        return ResponseEntity.ok(QualificationResponse.from(q));
    }

    @GetMapping
    public ResponseEntity<List<QualificationResponse>> listQualifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long equipmentId) {
        List<UserEquipmentQualification> list;
        if (userId != null) {
            list = qualificationService.listQualificationsByUser(userId);
        } else if (equipmentId != null) {
            list = qualificationService.listQualificationsByEquipment(equipmentId);
        } else {
            // default to current user's qualifications if authenticated
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            if (currentUserId != null) {
                list = qualificationService.listQualificationsByUser(currentUserId);
            } else {
                list = List.of();
            }
        }
        List<QualificationResponse> response = list.stream()
                .map(QualificationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QualificationResponse>> getByUserId(@PathVariable Long userId) {
        List<QualificationResponse> response = qualificationService.listQualificationsByUser(userId).stream()
                .map(QualificationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<QualificationResponse>> getByEquipmentId(@PathVariable Long equipmentId) {
        List<QualificationResponse> response = qualificationService.listQualificationsByEquipment(equipmentId).stream()
                .map(QualificationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<QualificationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQualificationStatusRequest request) {
        UserEquipmentQualification updated = qualificationService.updateQualificationStatus(
                id, request.getStatus(), request.getNotes()
        );
        return ResponseEntity.ok(QualificationResponse.from(updated));
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeQualification(
            @PathVariable Long id,
            @RequestBody(required = false) RevokeQualificationRequest request) {
        String reason = (request != null) ? request.getReason() : "Revoked by administrator";
        qualificationService.revokeQualification(id, reason);
        return ResponseEntity.noContent().build();
    }
}
