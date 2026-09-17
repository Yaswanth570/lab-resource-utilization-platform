package com.labresource.platform.user.web;

import com.labresource.platform.user.Role;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyRole('ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> listRoles() {
        List<Role> list = roleService.listRoles();
        return ResponseEntity.ok(list.stream().map(RoleResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable UserRoleType name) {
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }
}
