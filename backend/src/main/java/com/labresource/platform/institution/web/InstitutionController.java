package com.labresource.platform.institution.web;

import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InstitutionResponse> createInstitution(@Valid @RequestBody CreateInstitutionRequest request) {
        Institution entity = new Institution();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDomain(request.getDomain());
        entity.setAddressLine1(request.getAddressLine1());
        entity.setAddressLine2(request.getAddressLine2());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setCountry(request.getCountry());
        entity.setPostalCode(request.getPostalCode());
        entity.setContactEmail(request.getContactEmail());
        entity.setContactPhone(request.getContactPhone());

        Institution created = institutionService.createInstitution(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(InstitutionResponse.fromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> listInstitutions(
            @RequestParam(required = false) Boolean active) {
        List<Institution> list = (active != null)
                ? institutionService.listInstitutionsByActiveStatus(active)
                : institutionService.listInstitutions();
        List<InstitutionResponse> response = list.stream().map(InstitutionResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponse> getInstitutionById(@PathVariable Long id) {
        Institution institution = institutionService.getInstitutionById(id);
        return ResponseEntity.ok(InstitutionResponse.fromEntity(institution));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<InstitutionResponse> getInstitutionByCode(@PathVariable String code) {
        Institution institution = institutionService.getInstitutionByCode(code);
        return ResponseEntity.ok(InstitutionResponse.fromEntity(institution));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<InstitutionResponse> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInstitutionRequest request) {
        Institution updateData = new Institution();
        updateData.setName(request.getName());
        updateData.setCode(request.getCode());
        updateData.setDomain(request.getDomain());
        updateData.setAddressLine1(request.getAddressLine1());
        updateData.setAddressLine2(request.getAddressLine2());
        updateData.setCity(request.getCity());
        updateData.setState(request.getState());
        updateData.setCountry(request.getCountry());
        updateData.setPostalCode(request.getPostalCode());
        updateData.setContactEmail(request.getContactEmail());
        updateData.setContactPhone(request.getContactPhone());

        Institution updated = institutionService.updateInstitution(id, updateData);
        return ResponseEntity.ok(InstitutionResponse.fromEntity(updated));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> activateInstitution(@PathVariable Long id) {
        institutionService.activateInstitution(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deactivateInstitution(@PathVariable Long id) {
        institutionService.deactivateInstitution(id);
        return ResponseEntity.ok().build();
    }
}
