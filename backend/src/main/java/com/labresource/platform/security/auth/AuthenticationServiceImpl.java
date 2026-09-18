package com.labresource.platform.security.auth;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.dto.RegisterRequest;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.RoleRepository;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.user.web.UserResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     InstitutionRepository institutionRepository,
                                     DepartmentRepository departmentRepository,
                                     RoleRepository roleRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.jwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
        this.institutionRepository = Objects.requireNonNull(institutionRepository, "institutionRepository must not be null");
        this.departmentRepository = Objects.requireNonNull(departmentRepository, "departmentRepository must not be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository must not be null");
    }

    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getDeletedAt() != null) {
            throw new DisabledException("User account has been deactivated");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("User account is not active");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                        .map(Role::getName)
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .toList()
                : Collections.emptyList();

        Long institutionId = user.getInstitution() != null ? user.getInstitution().getId() : null;
        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        String token = (departmentId != null)
                ? jwtService.generateToken(user.getId(), user.getEmail(), institutionId, departmentId, roles)
                : jwtService.generateToken(user.getId(), user.getEmail(), institutionId, roles);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                institutionId,
                departmentId,
                roles
        );
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new InvalidOperationException("Registration payload cannot be null");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidOperationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidOperationException("Password is required");
        }
        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Password and confirm password do not match");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidOperationException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new InvalidOperationException("Last name is required");
        }

        validatePasswordComplexity(request.getPassword());

        // Reject attempted privileged role self-assignment
        if (request.getRequestedRole() != null && request.getRequestedRole() != UserRoleType.ROLE_RESEARCHER_STUDENT) {
            throw new InvalidOperationException(String.format(
                    "Self-registration with privileged role '%s' is not permitted. Only Researcher / Student accounts can be self-registered.",
                    request.getRequestedRole()));
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        // Validate or resolve Institution ("Others" handled gracefully)
        Institution institution;
        if (request.getInstitutionId() == null || request.getInstitutionId() <= 0) {
            institution = getOrCreateOtherInstitution();
        } else {
            institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", request.getInstitutionId()));
            if (!institution.isActive()) {
                throw new InvalidOperationException("Selected institution is inactive");
            }
        }

        // Validate or resolve Department ("Others" handled gracefully)
        Department department = null;
        if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            if (!department.getInstitution().getId().equals(institution.getId())) {
                throw new InvalidOperationException(String.format(
                        "Department with id %d does not belong to institution with id %d",
                        request.getDepartmentId(), institution.getId()));
            }
            if (!department.isActive()) {
                throw new InvalidOperationException("Selected department is inactive");
            }
        }

        // Ensure default safest role
        Role studentRole = roleRepository.findByName(UserRoleType.ROLE_RESEARCHER_STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", UserRoleType.ROLE_RESEARCHER_STUDENT.name()));

        User user = new User();
        user.setInstitution(institution);
        user.setDepartment(department);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : null);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerified(false);
        user.setRoles(new HashSet<>(Set.of(studentRole)));

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    private Institution getOrCreateOtherInstitution() {
        return institutionRepository.findByCode("OTHER").orElseGet(() -> {
            Institution other = new Institution();
            other.setName("Other / Unlisted Institution");
            other.setCode("OTHER");
            other.setCountry("Other");
            other.setContactEmail("support@labresource.platform");
            other.setActive(true);
            return institutionRepository.save(other);
        });
    }

    private void validatePasswordComplexity(String password) {
        if (password.length() < 8) {
            throw new InvalidOperationException("Password must be at least 8 characters long");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new InvalidOperationException("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }
    }
}
