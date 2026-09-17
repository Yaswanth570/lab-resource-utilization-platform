package com.labresource.platform.security.jwt;

import java.util.List;

public interface JwtService {

    String generateToken(Long userId, String email, Long institutionId, List<String> roles);

    String generateToken(Long userId, String email, Long institutionId, Long departmentId, List<String> roles);

    boolean validateToken(String token);

    Long extractUserId(String token);

    String extractEmail(String token);

    List<String> extractRoles(String token);

    Long extractInstitutionId(String token);

    Long extractDepartmentId(String token);

    boolean isTokenExpired(String token);

    long getExpirationMs();
}
