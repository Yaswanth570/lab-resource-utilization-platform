package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.EquipmentSpecification;
import com.labresource.platform.equipment.service.EquipmentSpecificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class EquipmentSpecificationController {

    private final EquipmentSpecificationService specificationService;

    public EquipmentSpecificationController(EquipmentSpecificationService specificationService) {
        this.specificationService = specificationService;
    }

    @PostMapping("/equipment/{equipmentId}/specifications")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentSpecificationResponse> addSpecification(
            @PathVariable Long equipmentId,
            @Valid @RequestBody CreateEquipmentSpecificationRequest request) {
        EquipmentSpecification spec = new EquipmentSpecification();
        spec.setSpecName(request.getSpecName());
        spec.setSpecValue(request.getSpecValue());
        spec.setUnit(request.getUnit());

        EquipmentSpecification created = specificationService.addSpecification(equipmentId, spec);
        return ResponseEntity.status(HttpStatus.CREATED).body(EquipmentSpecificationResponse.fromEntity(created));
    }

    @GetMapping("/equipment/{equipmentId}/specifications")
    public ResponseEntity<List<EquipmentSpecificationResponse>> listSpecificationsByEquipment(
            @PathVariable Long equipmentId) {
        List<EquipmentSpecification> list = specificationService.listSpecificationsByEquipment(equipmentId);
        return ResponseEntity.ok(list.stream().map(EquipmentSpecificationResponse::fromEntity).toList());
    }

    @GetMapping("/specifications/{id}")
    public ResponseEntity<EquipmentSpecificationResponse> getSpecificationById(@PathVariable Long id) {
        EquipmentSpecification spec = specificationService.getSpecificationById(id);
        return ResponseEntity.ok(EquipmentSpecificationResponse.fromEntity(spec));
    }

    @PutMapping("/specifications/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentSpecificationResponse> updateSpecification(
            @PathVariable Long id,
            @RequestBody UpdateEquipmentSpecificationRequest request) {
        EquipmentSpecification updateData = new EquipmentSpecification();
        updateData.setSpecName(request.getSpecName());
        updateData.setSpecValue(request.getSpecValue());
        updateData.setUnit(request.getUnit());

        EquipmentSpecification updated = specificationService.updateSpecification(id, updateData);
        return ResponseEntity.ok(EquipmentSpecificationResponse.fromEntity(updated));
    }

    @DeleteMapping("/specifications/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deleteSpecification(@PathVariable Long id) {
        specificationService.deleteSpecification(id);
        return ResponseEntity.noContent().build();
    }
}
