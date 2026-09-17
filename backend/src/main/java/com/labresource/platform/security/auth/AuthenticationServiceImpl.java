package com.labresource.platform.security.auth;

import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.jwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
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
}
