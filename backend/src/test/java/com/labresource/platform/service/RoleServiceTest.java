package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.service.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role(UserRoleType.ROLE_LAB_MANAGER, "Lab Manager Role");
        role.setId(1L);
    }

    @Test
    void getRoleById_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        Role found = roleService.getRoleById(1L);

        assertNotNull(found);
        assertEquals(UserRoleType.ROLE_LAB_MANAGER, found.getName());
    }

    @Test
    void getRoleById_notFound_throwsException() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(99L));
    }

    @Test
    void getRoleByName_success() {
        when(roleRepository.findByName(UserRoleType.ROLE_LAB_MANAGER)).thenReturn(Optional.of(role));

        Role found = roleService.getRoleByName(UserRoleType.ROLE_LAB_MANAGER);

        assertNotNull(found);
        assertEquals(UserRoleType.ROLE_LAB_MANAGER, found.getName());
    }

    @Test
    void createRole_duplicateName_throwsException() {
        when(roleRepository.existsByName(UserRoleType.ROLE_LAB_MANAGER)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> roleService.createRole(role));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_success() {
        when(roleRepository.existsByName(UserRoleType.ROLE_LAB_MANAGER)).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role created = roleService.createRole(role);

        assertNotNull(created);
        assertEquals(UserRoleType.ROLE_LAB_MANAGER, created.getName());
        verify(roleRepository).save(role);
    }
}
