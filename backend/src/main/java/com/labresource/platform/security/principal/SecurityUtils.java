package com.labresource.platform.security.principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }
        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }

    public static Optional<Long> getCurrentInstitutionId() {
        return getCurrentUser().map(UserPrincipal::getInstitutionId);
    }

    public static Optional<Long> getCurrentDepartmentId() {
        return getCurrentUser().map(UserPrincipal::getDepartmentId);
    }

    public static List<String> getCurrentUserRoles() {
        return getCurrentUser()
                .map(user -> user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public static boolean hasRole(String role) {
        if (role == null) return false;
        String formatted = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserRoles().contains(formatted);
    }

    public static boolean hasAnyRole(String... roles) {
        if (roles == null) return false;
        List<String> currentRoles = getCurrentUserRoles();
        for (String r : roles) {
            String formatted = r.startsWith("ROLE_") ? r : "ROLE_" + r;
            if (currentRoles.contains(formatted)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isResearcher() {
        return hasRole("ROLE_RESEARCHER_STUDENT");
    }

    public static boolean isLabTechnician() {
        return hasRole("ROLE_LAB_TECHNICIAN");
    }

    public static boolean isLabManager() {
        return hasRole("ROLE_LAB_MANAGER");
    }

    public static boolean isDepartmentHead() {
        return hasRole("ROLE_DEPARTMENT_HEAD");
    }

    public static boolean isInstitutionAdmin() {
        return hasRole("ROLE_INSTITUTION_ADMINISTRATOR");
    }

    public static boolean isSystemAdmin() {
        return hasRole("ROLE_SYSTEM_ADMINISTRATOR");
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);
    }
}
