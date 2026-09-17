package com.labresource.platform.service;

import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.auth.AuthenticationService;
import com.labresource.platform.security.auth.AuthenticationServiceImpl;
import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Requirement A: Password hashing with BCrypt works")
    void testPasswordHashing() {
        String rawPassword = "SecurePassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("WrongPassword", encoded));
    }

    @Test
    @DisplayName("Requirement B & G & J: Correct credentials authenticates, sets lastLoginAt, returns JWT and authorities")
    void testSuccessfulAuthentication() {
        String rawPassword = "ValidPassword123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        Institution institution = new Institution();
        institution.setId(10L);

        Role role1 = new Role(UserRoleType.ROLE_RESEARCHER_STUDENT, "Researcher");
        role1.setId(1L);
        Role role2 = new Role(UserRoleType.ROLE_LAB_MANAGER, "Lab Manager");
        role2.setId(2L);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@lab.org");
        user.setPasswordHash(hashedPassword);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setStatus(UserStatus.ACTIVE);
        user.setInstitution(institution);
        user.setRoles(Set.of(role1, role2));

        when(userRepository.findByEmail("user@lab.org")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(eq(1L), eq("user@lab.org"), eq(10L), anyList()))
                .thenReturn("mock-jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = new LoginRequest("user@lab.org", rawPassword);
        LoginResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("user@lab.org", response.getEmail());
        assertEquals("Alice", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals(10L, response.getInstitutionId());
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains("ROLE_RESEARCHER_STUDENT"));
        assertTrue(response.getRoles().contains("ROLE_LAB_MANAGER"));

        // Verify lastLoginAt updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getLastLoginAt());
    }

    @Test
    @DisplayName("Requirement B (case insensitivity): Authenticates email regardless of case")
    void testEmailCaseInsensitive() {
        String rawPassword = "ValidPassword123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@lab.org");
        user.setPasswordHash(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("user@lab.org")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("token");

        LoginRequest request = new LoginRequest("  UsEr@LaB.oRg  ", rawPassword);
        LoginResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        verify(userRepository).findByEmail("user@lab.org");
    }

    @Test
    @DisplayName("Requirement C: Incorrect password is rejected with BadCredentialsException")
    void testIncorrectPasswordRejected() {
        String rawPassword = "CorrectPassword123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@lab.org");
        user.setPasswordHash(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("user@lab.org")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("user@lab.org", "WrongPassword");

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(request));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtService, never()).generateToken(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Requirement D: Nonexistent user is rejected with BadCredentialsException without disclosing existence")
    void testNonexistentUserRejected() {
        when(userRepository.findByEmail("nonexistent@lab.org")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent@lab.org", "SomePassword");

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    @DisplayName("Requirement E: Inactive user cannot authenticate (DisabledException)")
    void testInactiveUserRejected() {
        String rawPassword = "Password123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setId(2L);
        user.setEmail("inactive@lab.org");
        user.setPasswordHash(hashedPassword);
        user.setStatus(UserStatus.DEACTIVATED);

        when(userRepository.findByEmail("inactive@lab.org")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("inactive@lab.org", rawPassword);

        DisabledException ex = assertThrows(DisabledException.class,
                () -> authenticationService.authenticate(request));
        assertEquals("User account is not active", ex.getMessage());
    }

    @Test
    @DisplayName("Requirement F: Soft-deleted user cannot authenticate (DisabledException)")
    void testSoftDeletedUserRejected() {
        String rawPassword = "Password123!";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setId(3L);
        user.setEmail("deleted@lab.org");
        user.setPasswordHash(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeletedAt(Instant.now());

        when(userRepository.findByEmail("deleted@lab.org")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("deleted@lab.org", rawPassword);

        DisabledException ex = assertThrows(DisabledException.class,
                () -> authenticationService.authenticate(request));
        assertEquals("User account has been deactivated", ex.getMessage());
    }
}
