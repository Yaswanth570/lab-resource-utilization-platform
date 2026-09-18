package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.security.auth.AuthenticationServiceImpl;
import com.labresource.platform.security.dto.RegisterRequest;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.user.web.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;
    private AuthenticationServiceImpl authenticationService;

    private Institution mockInstitution;
    private Department mockDepartment;
    private Role mockStudentRole;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService,
                institutionRepository,
                departmentRepository,
                roleRepository
        );

        mockInstitution = new Institution();
        mockInstitution.setId(1L);
        mockInstitution.setName("Andhra Pradesh Institute of Technology");
        mockInstitution.setCode("APITR");
        mockInstitution.setActive(true);

        mockDepartment = new Department();
        mockDepartment.setId(10L);
        mockDepartment.setName("Computer Science & Engineering");
        mockDepartment.setCode("CSE");
        mockDepartment.setInstitution(mockInstitution);
        mockDepartment.setActive(true);

        mockStudentRole = new Role(UserRoleType.ROLE_RESEARCHER_STUDENT, "Student / Researcher standard role");
        mockStudentRole.setId(100L);
    }

    private RegisterRequest createValidRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setInstitutionId(1L);
        req.setDepartmentId(10L);
        req.setEmail("newuser@apitr.edu");
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");
        req.setFirstName("Ramesh");
        req.setLastName("Kumar");
        req.setPhone("+91 98480 12345");
        return req;
    }

    @Test
    @DisplayName("Requirement 1 & 2: Successful registration assigns default ROLE_RESEARCHER_STUDENT, hashes password, and sets status ACTIVE")
    void testSuccessfulRegistration() {
        RegisterRequest request = createValidRequest();

        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(mockInstitution));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(mockDepartment));
        when(roleRepository.findByName(UserRoleType.ROLE_RESEARCHER_STUDENT)).thenReturn(Optional.of(mockStudentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(501L);
            return u;
        });

        UserResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals("newuser@apitr.edu", response.getEmail());
        assertEquals("Ramesh", response.getFirstName());
        assertEquals("Kumar", response.getLastName());
        assertEquals("ACTIVE", response.getStatus());
        assertFalse(response.isVerified());
        assertTrue(response.getRoles().contains("ROLE_RESEARCHER_STUDENT"));

        // Verify captured entity
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();

        assertEquals("newuser@apitr.edu", captured.getEmail());
        assertEquals(UserStatus.ACTIVE, captured.getStatus());
        assertFalse(captured.isVerified());
        assertNotEquals("Password123!", captured.getPasswordHash());
        assertTrue(passwordEncoder.matches("Password123!", captured.getPasswordHash()));
        assertTrue(captured.getRoles().contains(mockStudentRole));
    }

    @Test
    @DisplayName("Requirement 3: Duplicate email throws DuplicateResourceException")
    void testDuplicateEmail_ThrowsDuplicateResourceException() {
        RegisterRequest request = createValidRequest();
        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 4: Mismatched confirmation password throws InvalidOperationException")
    void testMismatchedConfirmationPassword() {
        RegisterRequest request = createValidRequest();
        request.setConfirmPassword("DifferentPassword123!");

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () -> authenticationService.register(request));
        assertTrue(ex.getMessage().contains("Password and confirm password do not match"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 5: Weak passwords fail validation")
    void testWeakPassword_FailsComplexity() {
        // Less than 8 chars
        RegisterRequest reqTooShort = createValidRequest();
        reqTooShort.setPassword("Pass1!");
        reqTooShort.setConfirmPassword("Pass1!");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(reqTooShort));

        // No uppercase
        RegisterRequest reqNoUpper = createValidRequest();
        reqNoUpper.setPassword("password123!");
        reqNoUpper.setConfirmPassword("password123!");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(reqNoUpper));

        // No lowercase
        RegisterRequest reqNoLower = createValidRequest();
        reqNoLower.setPassword("PASSWORD123!");
        reqNoLower.setConfirmPassword("PASSWORD123!");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(reqNoLower));

        // No digit
        RegisterRequest reqNoDigit = createValidRequest();
        reqNoDigit.setPassword("Password!!!!");
        reqNoDigit.setConfirmPassword("Password!!!!");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(reqNoDigit));
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleType.class, names = {
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_INSTITUTION_ADMINISTRATOR",
            "ROLE_DEPARTMENT_HEAD",
            "ROLE_LAB_MANAGER",
            "ROLE_LAB_TECHNICIAN"
    })
    @DisplayName("Requirement 6: Attempted privileged role self-registration is strictly rejected")
    void testPrivilegedRoleRegistrationDenied(UserRoleType privilegedRole) {
        RegisterRequest request = createValidRequest();
        request.setRequestedRole(privilegedRole);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () -> authenticationService.register(request));
        assertTrue(ex.getMessage().contains("Self-registration with privileged role"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 7: Explicit requestedRole ROLE_RESEARCHER_STUDENT is accepted and assigned")
    void testExplicitResearcherStudentRoleAccepted() {
        RegisterRequest request = createValidRequest();
        request.setRequestedRole(UserRoleType.ROLE_RESEARCHER_STUDENT);

        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(mockInstitution));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(mockDepartment));
        when(roleRepository.findByName(UserRoleType.ROLE_RESEARCHER_STUDENT)).thenReturn(Optional.of(mockStudentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authenticationService.register(request);
        assertNotNull(response);
        assertTrue(response.getRoles().contains("ROLE_RESEARCHER_STUDENT"));
    }

    @Test
    @DisplayName("Requirement 8: Non-existent Institution throws ResourceNotFoundException")
    void testNonExistentInstitution() {
        RegisterRequest request = createValidRequest();
        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 9: Inactive Institution throws InvalidOperationException")
    void testInactiveInstitution() {
        mockInstitution.setActive(false);
        RegisterRequest request = createValidRequest();
        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(mockInstitution));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () -> authenticationService.register(request));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("Requirement 10: Department not belonging to Institution throws InvalidOperationException")
    void testDepartmentBelongsToDifferentInstitution() {
        Institution otherInstitution = new Institution();
        otherInstitution.setId(99L);
        mockDepartment.setInstitution(otherInstitution);

        RegisterRequest request = createValidRequest();
        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(mockInstitution));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(mockDepartment));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () -> authenticationService.register(request));
        assertTrue(ex.getMessage().contains("does not belong to institution"));
    }

    @Test
    @DisplayName("Requirement 11: Missing required identity/credential fields throw InvalidOperationException")
    void testMissingRequiredFields() {
        RegisterRequest req1 = createValidRequest();
        req1.setEmail(" ");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(req1));

        RegisterRequest req2 = createValidRequest();
        req2.setFirstName("");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(req2));

        RegisterRequest req3 = createValidRequest();
        req3.setLastName(null);
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(req3));

        RegisterRequest req4 = createValidRequest();
        req4.setPassword("");
        assertThrows(InvalidOperationException.class, () -> authenticationService.register(req4));
    }

    @Test
    @DisplayName("Requirement 12: Registration with 'Others' department succeeds with null department")
    void testRegistrationWithOtherDepartment_SucceedsWithNullDepartment() {
        RegisterRequest request = createValidRequest();
        request.setDepartmentId(null); // 'Others' selected

        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(mockInstitution));
        when(roleRepository.findByName(UserRoleType.ROLE_RESEARCHER_STUDENT)).thenReturn(Optional.of(mockStudentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authenticationService.register(request);
        assertNotNull(response);
        assertEquals(1L, response.getInstitutionId());
        assertNull(response.getDepartmentId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getDepartment());
        assertEquals(mockInstitution, captor.getValue().getInstitution());
    }

    @Test
    @DisplayName("Requirement 13: Registration with 'Others' institution succeeds with OTHER institution and null department")
    void testRegistrationWithOtherInstitution_SucceedsWithOtherInstitution() {
        RegisterRequest request = createValidRequest();
        request.setInstitutionId(null); // 'Others' selected
        request.setDepartmentId(null);  // 'Others' selected

        Institution otherInst = new Institution();
        otherInst.setId(999L);
        otherInst.setCode("OTHER");
        otherInst.setName("Other / Unlisted Institution");
        otherInst.setActive(true);

        when(userRepository.existsByEmail("newuser@apitr.edu")).thenReturn(false);
        when(institutionRepository.findByCode("OTHER")).thenReturn(Optional.of(otherInst));
        when(roleRepository.findByName(UserRoleType.ROLE_RESEARCHER_STUDENT)).thenReturn(Optional.of(mockStudentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authenticationService.register(request);
        assertNotNull(response);
        assertEquals(999L, response.getInstitutionId());
        assertNull(response.getDepartmentId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("OTHER", captor.getValue().getInstitution().getCode());
        assertNull(captor.getValue().getDepartment());
    }
}
