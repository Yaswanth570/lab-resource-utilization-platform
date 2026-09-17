package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.user.service.UserServiceImpl;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Institution institutionA;
    private Institution institutionB;
    private Department departmentA;
    private Department departmentB;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        institutionA = new Institution();
        institutionA.setId(1L);
        institutionA.setName("MIT");

        institutionB = new Institution();
        institutionB.setId(2L);
        institutionB.setName("Harvard");

        departmentA = new Department();
        departmentA.setId(10L);
        departmentA.setName("Computer Science");
        departmentA.setInstitution(institutionA);

        departmentB = new Department();
        departmentB.setId(20L);
        departmentB.setName("Medical Sciences");
        departmentB.setInstitution(institutionB);

        user = new User();
        user.setId(100L);
        user.setEmail("alice@mit.edu");
        user.setFirstName("Alice");
        user.setLastName("Smith");

        role = new Role(UserRoleType.ROLE_RESEARCHER_STUDENT, "Researcher Student");
        role.setId(5L);
    }

    @Test
    void createUser_underValidInstitutionWithoutDept_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(userRepository.existsByEmail("alice@mit.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user, 1L, null);

        assertNotNull(created);
        assertEquals("alice@mit.edu", created.getEmail());
        assertEquals(institutionA, created.getInstitution());
        assertNull(created.getDepartment());
        assertEquals(UserStatus.ACTIVE, created.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_withValidDepartmentInSameInstitution_success() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(userRepository.existsByEmail("alice@mit.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user, 1L, 10L);

        assertNotNull(created);
        assertEquals(institutionA, created.getInstitution());
        assertEquals(departmentA, created.getDepartment());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_nonexistentInstitution_throwsException() {
        when(institutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(user, 99L, null));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_crossInstitutionDepartment_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        // departmentB belongs to institutionB (id 2)
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> userService.createUser(user, 1L, 20L));
        assertTrue(ex.getMessage().contains("does not belong to institution"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(userRepository.existsByEmail("alice@mit.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(user, 1L, null));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void assignRole_success() {
        user.setInstitution(institutionA);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.assignRole(100L, 5L);

        assertNotNull(updated);
        assertTrue(updated.getRoles().contains(role));
        verify(userRepository).save(user);
    }

    @Test
    void removeRole_success() {
        user.setInstitution(institutionA);
        user.getRoles().add(role);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.removeRole(100L, 5L);

        assertNotNull(updated);
        assertFalse(updated.getRoles().contains(role));
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_success() {
        user.setInstitution(institutionA);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deactivateUser(100L);

        assertEquals(UserStatus.DEACTIVATED, user.getStatus());
        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
    }
}
