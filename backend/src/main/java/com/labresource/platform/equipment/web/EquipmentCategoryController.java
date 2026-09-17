package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.EquipmentCategory;
import com.labresource.platform.equipment.service.EquipmentCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/equipment-categories")
public class EquipmentCategoryController {

    private final EquipmentCategoryService categoryService;

    public EquipmentCategoryController(EquipmentCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentCategoryResponse> createCategory(@Valid @RequestBody CreateEquipmentCategoryRequest request) {
        EquipmentCategory category = new EquipmentCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        EquipmentCategory created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(EquipmentCategoryResponse.fromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentCategoryResponse>> listCategories() {
        List<EquipmentCategory> list = categoryService.listCategories();
        return ResponseEntity.ok(list.stream().map(EquipmentCategoryResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentCategoryResponse> getCategoryById(@PathVariable Long id) {
        EquipmentCategory category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(EquipmentCategoryResponse.fromEntity(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<EquipmentCategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateEquipmentCategoryRequest request) {
        EquipmentCategory updateData = new EquipmentCategory();
        updateData.setName(request.getName());
        updateData.setDescription(request.getDescription());

        EquipmentCategory updated = categoryService.updateCategory(id, updateData);
        return ResponseEntity.ok(EquipmentCategoryResponse.fromEntity(updated));
    }
}
