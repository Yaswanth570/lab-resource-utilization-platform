package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/equipment")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setAssetTag(request.getAssetTag());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setModelNumber(request.getModelNumber());
        equipment.setManufacturer(request.getManufacturer());
        equipment.setLocationBuilding(request.getLocationBuilding());
        equipment.setLocationRoom(request.getLocationRoom());

        if (request.getShareableExternally() != null) equipment.setShareableExternally(request.getShareableExternally());
        if (request.getHourlyRateInternal() != null) equipment.setHourlyRateInternal(request.getHourlyRateInternal());
        if (request.getHourlyRateExternal() != null) equipment.setHourlyRateExternal(request.getHourlyRateExternal());
        if (request.getMinBookingDurationMins() != null) equipment.setMinBookingDurationMins(request.getMinBookingDurationMins());
        if (request.getMaxBookingDurationMins() != null) equipment.setMaxBookingDurationMins(request.getMaxBookingDurationMins());
        if (request.getBufferTimeMins() != null) equipment.setBufferTimeMins(request.getBufferTimeMins());
        if (request.getRequiresTrainingCertification() != null) equipment.setRequiresTrainingCertification(request.getRequiresTrainingCertification());
        if (request.getRequiresApproval() != null) equipment.setRequiresApproval(request.getRequiresApproval());
        if (request.getPurchaseDate() != null) equipment.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchaseCost() != null) equipment.setPurchaseCost(request.getPurchaseCost());
        if (request.getWarrantyExpiryDate() != null) equipment.setWarrantyExpiryDate(request.getWarrantyExpiryDate());

        Equipment created = equipmentService.createEquipment(
                equipment,
                request.getInstitutionId(),
                request.getDepartmentId(),
                request.getCategoryId(),
                request.getPrimaryLabManagerId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(EquipmentResponse.fromEntity(created));
    }

    @GetMapping("/equipment")
    public ResponseEntity<List<EquipmentResponse>> listEquipment() {
        List<Equipment> list = equipmentService.listEquipment();
        return ResponseEntity.ok(list.stream().map(EquipmentResponse::fromEntity).toList());
    }

    @GetMapping("/equipment/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        Equipment equipment = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(EquipmentResponse.fromEntity(equipment));
    }

    @GetMapping("/equipment/asset-tag/{assetTag}")
    public ResponseEntity<EquipmentResponse> getEquipmentByAssetTag(@PathVariable String assetTag) {
        Equipment equipment = equipmentService.getEquipmentByAssetTag(assetTag);
        return ResponseEntity.ok(EquipmentResponse.fromEntity(equipment));
    }

    @GetMapping("/institutions/{institutionId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> listEquipmentByInstitution(
            @PathVariable Long institutionId,
            @RequestParam(required = false) EquipmentStatus status) {
        List<Equipment> list = (status != null)
                ? equipmentService.listEquipmentByInstitutionAndStatus(institutionId, status)
                : equipmentService.listEquipmentByInstitution(institutionId);
        return ResponseEntity.ok(list.stream().map(EquipmentResponse::fromEntity).toList());
    }

    @GetMapping("/departments/{departmentId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> listEquipmentByDepartment(@PathVariable Long departmentId) {
        List<Equipment> list = equipmentService.listEquipmentByDepartment(departmentId);
        return ResponseEntity.ok(list.stream().map(EquipmentResponse::fromEntity).toList());
    }

    @GetMapping("/equipment-categories/{categoryId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> listEquipmentByCategory(@PathVariable Long categoryId) {
        List<Equipment> list = equipmentService.listEquipmentByCategory(categoryId);
        return ResponseEntity.ok(list.stream().map(EquipmentResponse::fromEntity).toList());
    }

    @PutMapping("/equipment/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long id,
            @RequestBody UpdateEquipmentRequest request) {
        Equipment updateData = new Equipment();
        updateData.setName(request.getName());
        updateData.setModelNumber(request.getModelNumber());
        updateData.setManufacturer(request.getManufacturer());
        updateData.setLocationBuilding(request.getLocationBuilding());
        updateData.setLocationRoom(request.getLocationRoom());

        if (request.getShareableExternally() != null) updateData.setShareableExternally(request.getShareableExternally());
        if (request.getHourlyRateInternal() != null) updateData.setHourlyRateInternal(request.getHourlyRateInternal());
        if (request.getHourlyRateExternal() != null) updateData.setHourlyRateExternal(request.getHourlyRateExternal());
        if (request.getMinBookingDurationMins() != null) updateData.setMinBookingDurationMins(request.getMinBookingDurationMins());
        if (request.getMaxBookingDurationMins() != null) updateData.setMaxBookingDurationMins(request.getMaxBookingDurationMins());
        if (request.getBufferTimeMins() != null) updateData.setBufferTimeMins(request.getBufferTimeMins());
        if (request.getRequiresTrainingCertification() != null) updateData.setRequiresTrainingCertification(request.getRequiresTrainingCertification());
        if (request.getRequiresApproval() != null) updateData.setRequiresApproval(request.getRequiresApproval());
        if (request.getPurchaseDate() != null) updateData.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchaseCost() != null) updateData.setPurchaseCost(request.getPurchaseCost());
        if (request.getWarrantyExpiryDate() != null) updateData.setWarrantyExpiryDate(request.getWarrantyExpiryDate());

        Equipment updated = equipmentService.updateEquipment(
                id,
                updateData,
                request.getDepartmentId(),
                request.getCategoryId(),
                request.getPrimaryLabManagerId()
        );
        return ResponseEntity.ok(EquipmentResponse.fromEntity(updated));
    }

    @PatchMapping("/equipment/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN', 'ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentResponse> updateOperationalStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentStatusRequest request) {
        Equipment updated = equipmentService.updateOperationalStatus(id, request.getStatus(), request.getReason());
        return ResponseEntity.ok(EquipmentResponse.fromEntity(updated));
    }

    @PatchMapping("/equipment/{id}/activate")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> activateEquipment(@PathVariable Long id) {
        equipmentService.activateEquipment(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/equipment/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deactivateEquipment(@PathVariable Long id) {
        equipmentService.deactivateEquipment(id);
        return ResponseEntity.ok().build();
    }
}
