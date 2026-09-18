package com.labresource.platform.user.web;

import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.user.User;
import com.labresource.platform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (!SecurityUtils.isSystemAdmin()) {
            Long callerInstId = SecurityUtils.getCurrentInstitutionId().orElse(null);
            if (callerInstId != null && request.getInstitutionId() != null && !callerInstId.equals(request.getInstitutionId())) {
                throw new AccessDeniedException("Tenant isolation: Cannot create users outside your assigned institution");
            }
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        User created = userService.createUser(user, request.getInstitutionId(), request.getDepartmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(created));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (SecurityUtils.isResearcher() || SecurityUtils.isLabTechnician()) {
            if (currentUserId == null || !currentUserId.equals(id)) {
                throw new AccessDeniedException("Access denied: You can only view your own user profile");
            }
        }
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (SecurityUtils.isResearcher() || SecurityUtils.isLabTechnician()) {
            if (currentUserId == null || !currentUserId.equals(user.getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own user profile");
            }
        }
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new InvalidOperationException("No authenticated user in context"));
        User user = userService.getUserById(currentUserId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/users/me")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new InvalidOperationException("No authenticated user in context"));
        User updated = userService.updateUserProfile(currentUserId, request);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    @PatchMapping("/users/me")
    public ResponseEntity<UserResponse> patchCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return updateCurrentUserProfile(request);
    }

    @GetMapping("/institutions/{institutionId}/users")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD')")
    public ResponseEntity<List<UserResponse>> listUsersByInstitution(@PathVariable Long institutionId) {
        if (!SecurityUtils.isSystemAdmin()) {
            Long callerInstId = SecurityUtils.getCurrentInstitutionId().orElse(null);
            if (callerInstId != null && !callerInstId.equals(institutionId)) {
                throw new AccessDeniedException("Tenant isolation: Cannot view users of an external institution");
            }
        }
        List<User> list = userService.listUsersByInstitution(institutionId);
        return ResponseEntity.ok(list.stream().map(UserResponse::fromEntity).toList());
    }

    @GetMapping("/departments/{departmentId}/users")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR', 'ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD')")
    public ResponseEntity<List<UserResponse>> listUsersByDepartment(@PathVariable Long departmentId) {
        List<User> list = userService.listUsersByDepartment(departmentId);
        return ResponseEntity.ok(list.stream().map(UserResponse::fromEntity).toList());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        boolean isSelf = currentUserId != null && currentUserId.equals(id);
        boolean isAdmin = SecurityUtils.hasAnyRole("ROLE_INSTITUTION_ADMINISTRATOR", "ROLE_SYSTEM_ADMINISTRATOR");

        if (!isSelf && !isAdmin) {
            throw new AccessDeniedException("Access denied: You can only update your own user profile");
        }

        User updateData = new User();
        updateData.setFirstName(request.getFirstName());
        updateData.setLastName(request.getLastName());
        updateData.setPhone(request.getPhone());

        User updated = userService.updateUser(id, updateData, request.getDepartmentId());
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long id,
            @RequestBody AssignRoleRequest request) {
        User user;
        if (request.getRoleId() != null) {
            user = userService.assignRole(id, request.getRoleId());
        } else if (request.getRoleType() != null) {
            user = userService.assignRole(id, request.getRoleType());
        } else {
            throw new InvalidOperationException("Either roleId or roleType is required");
        }
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/users/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long id,
            @PathVariable Long roleId) {
        User user = userService.removeRole(id, roleId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
