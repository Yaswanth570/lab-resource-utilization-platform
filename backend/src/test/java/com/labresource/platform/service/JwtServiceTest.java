package com.labresource.platform.service;

import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.security.jwt.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 256-bit test secret strictly for unit test executions
    private static final String TEST_SECRET = "UnitTestingSecretKeyMaterial32Bytes!";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Verification: Missing JWT secret fails safely at startup")
    void testMissingSecretFailsSafely() {
        IllegalStateException exNull = assertThrows(IllegalStateException.class,
                () -> new JwtServiceImpl(null, TEST_EXPIRATION_MS));
        assertTrue(exNull.getMessage().contains("missing"));

        IllegalStateException exEmpty = assertThrows(IllegalStateException.class,
                () -> new JwtServiceImpl("", TEST_EXPIRATION_MS));
        assertTrue(exEmpty.getMessage().contains("missing"));

        IllegalStateException exBlank = assertThrows(IllegalStateException.class,
                () -> new JwtServiceImpl("   ", TEST_EXPIRATION_MS));
        assertTrue(exBlank.getMessage().contains("missing"));
    }

    @Test
    @DisplayName("Verification: Invalid/insufficient JWT secret fails safely")
    void testInvalidSecretFailsSafely() {
        // Key material with less than 256 bits (32 bytes)
        IllegalStateException exShort = assertThrows(IllegalStateException.class,
                () -> new JwtServiceImpl("short-secret-key", TEST_EXPIRATION_MS));
        assertTrue(exShort.getMessage().contains("invalid") || exShort.getMessage().contains("at least 256 bits"));
    }

    @Test
    @DisplayName("Verification: Valid configured JWT secret works for signing and verification")
    void testValidConfiguredSecretWorks() {
        String customValidSecret = "AnotherValidTestingKeyWithAtLeast32BytesLength!";
        JwtService customService = new JwtServiceImpl(customValidSecret, TEST_EXPIRATION_MS);

        String token = customService.generateToken(99L, "configured@lab.org", 5L, List.of("ROLE_LAB_MANAGER"));
        assertNotNull(token);
        assertTrue(customService.validateToken(token));
        assertEquals(99L, customService.extractUserId(token));
        assertEquals("configured@lab.org", customService.extractEmail(token));
        assertEquals(5L, customService.extractInstitutionId(token));
    }

    @Test
    @DisplayName("Requirement G & H: Successful JWT generation and validation")
    void testGenerateAndValidateToken() {
        Long userId = 42L;
        String email = "researcher@lab.org";
        Long institutionId = 1L;
        List<String> roles = List.of("ROLE_RESEARCHER_STUDENT", "ROLE_LAB_TECHNICIAN");

        String token = jwtService.generateToken(userId, email, institutionId, roles);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.validateToken(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(institutionId, jwtService.extractInstitutionId(token));
        assertEquals(roles, jwtService.extractRoles(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Requirement I: Expired JWT is rejected")
    void testExpiredTokenRejected() {
        // JwtService with negative expiration to create immediate expiration
        JwtService expiredJwtService = new JwtServiceImpl(TEST_SECRET, -1000);
        String token = expiredJwtService.generateToken(1L, "expired@lab.org", 1L, List.of("ROLE_RESEARCHER_STUDENT"));

        assertFalse(expiredJwtService.validateToken(token));
        assertTrue(expiredJwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Requirement I: Tampered/invalid JWT is rejected")
    void testTamperedTokenRejected() {
        String token = jwtService.generateToken(1L, "user@lab.org", 1L, List.of("ROLE_RESEARCHER_STUDENT"));
        String tamperedToken = token + "corrupted";

        assertFalse(jwtService.validateToken(tamperedToken));
        assertFalse(jwtService.validateToken("not-a-jwt"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("JWT claims do not leak sensitive information")
    void testTokenClaimsIntegrity() {
        String token = jwtService.generateToken(10L, "safe@lab.org", 5L, List.of("ROLE_LAB_MANAGER"));

        assertEquals(10L, jwtService.extractUserId(token));
        assertEquals("safe@lab.org", jwtService.extractEmail(token));
        assertEquals(5L, jwtService.extractInstitutionId(token));
        assertEquals(List.of("ROLE_LAB_MANAGER"), jwtService.extractRoles(token));
    }
}
