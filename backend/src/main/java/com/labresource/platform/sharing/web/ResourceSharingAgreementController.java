package com.labresource.platform.sharing.web;

import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharingAgreementStatus;
import com.labresource.platform.sharing.service.SharingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/sharing/agreements")
@PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class ResourceSharingAgreementController {

    private final SharingService sharingService;

    public ResourceSharingAgreementController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping
    public ResponseEntity<SharingAgreementResponse> createAgreement(@Valid @RequestBody CreateSharingAgreementRequest request) {
        ResourceSharingAgreement agreement = new ResourceSharingAgreement();
        agreement.setAgreementCode(request.getAgreementCode());
        agreement.setStartDate(request.getStartDate());
        agreement.setEndDate(request.getEndDate());
        agreement.setBillingRateMultiplier(request.getBillingRateMultiplier());
        agreement.setMaxMonthlyHours(request.getMaxMonthlyHours());
        agreement.setStatus(request.getStatus());

        ResourceSharingAgreement created = sharingService.createAgreement(
                agreement,
                request.getRequestingInstitutionId(),
                request.getOwnerInstitutionId(),
                request.getSharingRequestId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(SharingAgreementResponse.fromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<SharingAgreementResponse>> listAgreements(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long requestingInstitutionId,
            @RequestParam(required = false) Long ownerInstitutionId,
            @RequestParam(required = false) SharingAgreementStatus status) {

        List<ResourceSharingAgreement> list = sharingService.listAgreements(
                institutionId,
                requestingInstitutionId,
                ownerInstitutionId,
                status
        );

        return ResponseEntity.ok(list.stream().map(SharingAgreementResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharingAgreementResponse> getAgreementById(@PathVariable Long id) {
        ResourceSharingAgreement agreement = sharingService.getAgreementById(id);
        return ResponseEntity.ok(SharingAgreementResponse.fromEntity(agreement));
    }

    @GetMapping({"/code/{code}", "/number/{code}"})
    public ResponseEntity<SharingAgreementResponse> getAgreementByCode(@PathVariable String code) {
        ResourceSharingAgreement agreement = sharingService.getAgreementByCode(code);
        return ResponseEntity.ok(SharingAgreementResponse.fromEntity(agreement));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SharingAgreementResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSharingAgreementStatusRequest request) {

        Long operatorInstitutionId = SecurityUtils.getCurrentInstitutionId().orElse(null);
        ResourceSharingAgreement updated = sharingService.updateAgreementStatus(id, request.getStatus(), operatorInstitutionId);
        return ResponseEntity.ok(SharingAgreementResponse.fromEntity(updated));
    }
}
