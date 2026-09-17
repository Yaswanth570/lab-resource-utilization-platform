package com.labresource.platform.sharing.web;

import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.sharing.SharedEquipmentAllocation;
import com.labresource.platform.sharing.service.SharingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/sharing/allocations")
@PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class SharedEquipmentAllocationController {

    private final SharingService sharingService;

    public SharedEquipmentAllocationController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping
    public ResponseEntity<SharedAllocationResponse> createAllocation(@Valid @RequestBody CreateSharedAllocationRequest request) {
        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);

        SharedEquipmentAllocation created = sharingService.createAllocation(
                request.getSharingAgreementId(),
                request.getEquipmentId(),
                request.getCustomHourlyRate(),
                request.getIsActive(),
                operatorInstitutionId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(SharedAllocationResponse.fromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<SharedAllocationResponse>> listAllocations(
            @RequestParam(required = false) Long sharingAgreementId,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long institutionId) {

        List<SharedEquipmentAllocation> list = sharingService.listAllocations(
                sharingAgreementId,
                equipmentId,
                isActive,
                institutionId
        );

        return ResponseEntity.ok(list.stream().map(SharedAllocationResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharedAllocationResponse> getAllocationById(@PathVariable Long id) {
        SharedEquipmentAllocation allocation = sharingService.getAllocationById(id);
        return ResponseEntity.ok(SharedAllocationResponse.fromEntity(allocation));
    }

    @PatchMapping({"/{id}/active", "/{id}/status"})
    public ResponseEntity<SharedAllocationResponse> updateActive(
            @PathVariable Long id,
            @RequestBody UpdateSharedAllocationStatusRequest request) {

        if (request.getIsActive() == null) {
            throw new InvalidOperationException("Field 'isActive' or 'status' is required");
        }

        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        SharedEquipmentAllocation updated = sharingService.updateAllocationActive(id, request.getIsActive(), operatorInstitutionId);
        return ResponseEntity.ok(SharedAllocationResponse.fromEntity(updated));
    }
}
