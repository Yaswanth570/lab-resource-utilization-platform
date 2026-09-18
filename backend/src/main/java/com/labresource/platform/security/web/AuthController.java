package com.labresource.platform.security.web;

import com.labresource.platform.department.service.DepartmentService;
import com.labresource.platform.department.web.DepartmentResponse;
import com.labresource.platform.institution.service.InstitutionService;
import com.labresource.platform.institution.web.InstitutionResponse;
import com.labresource.platform.security.auth.AuthenticationService;
import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.dto.RegisterRequest;
import com.labresource.platform.user.web.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final InstitutionService institutionService;
    private final DepartmentService departmentService;

    public AuthController(AuthenticationService authenticationService,
                          InstitutionService institutionService,
                          DepartmentService departmentService) {
        this.authenticationService = authenticationService;
        this.institutionService = institutionService;
        this.departmentService = departmentService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/institutions")
    public ResponseEntity<List<InstitutionResponse>> getRegistrationInstitutions() {
        List<InstitutionResponse> institutions = institutionService.listInstitutionsByActiveStatus(true)
                .stream()
                .map(InstitutionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(institutions);
    }

    @GetMapping("/institutions/{institutionId}/departments")
    public ResponseEntity<List<DepartmentResponse>> getRegistrationDepartments(@PathVariable Long institutionId) {
        List<DepartmentResponse> departments = departmentService.listActiveDepartmentsByInstitution(institutionId)
                .stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(departments);
    }
}
