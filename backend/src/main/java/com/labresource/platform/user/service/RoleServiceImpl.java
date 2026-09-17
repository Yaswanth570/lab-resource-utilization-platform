package com.labresource.platform.user.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRoleById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Role ID cannot be null");
        }
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    public Role getRoleByName(UserRoleType name) {
        if (name == null) {
            throw new InvalidOperationException("Role name cannot be null");
        }
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name.name()));
    }

    @Override
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public Role createRole(Role role) {
        if (role == null) {
            throw new InvalidOperationException("Role payload cannot be null");
        }
        if (role.getName() == null) {
            throw new InvalidOperationException("Role name is required");
        }
        if (roleRepository.existsByName(role.getName())) {
            throw new DuplicateResourceException("Role", "name", role.getName().name());
        }
        return roleRepository.save(role);
    }
}
