package com.labresource.platform.department.web;

import com.labresource.platform.department.Department;
import com.labresource.platform.department.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        Department entity = new Department();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setBillingAccountCode(request.getBillingAccountCode());

        Department created = departmentService.createDepartment(request.getInstitutionId(), entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(DepartmentResponse.fromEntity(created));
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        Department dept = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(DepartmentResponse.fromEntity(dept));
    }

    @GetMapping("/institutions/{institutionId}/departments")
    public ResponseEntity<List<DepartmentResponse>> listDepartmentsByInstitution(
            @PathVariable Long institutionId,
            @RequestParam(required = false) Boolean active) {
        List<Department> list = (active != null && active)
                ? departmentService.listActiveDepartmentsByInstitution(institutionId)
                : departmentService.listDepartmentsByInstitution(institutionId);
        List<DepartmentResponse> response = list.stream().map(DepartmentResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        Department updateData = new Department();
        updateData.setName(request.getName());
        updateData.setCode(request.getCode());
        updateData.setBillingAccountCode(request.getBillingAccountCode());

        Department updated = departmentService.updateDepartment(id, updateData);
        return ResponseEntity.ok(DepartmentResponse.fromEntity(updated));
    }

    @PatchMapping("/departments/{id}/activate")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> activateDepartment(@PathVariable Long id) {
        departmentService.activateDepartment(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/departments/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable Long id) {
        departmentService.deactivateDepartment(id);
        return ResponseEntity.ok().build();
    }
}
