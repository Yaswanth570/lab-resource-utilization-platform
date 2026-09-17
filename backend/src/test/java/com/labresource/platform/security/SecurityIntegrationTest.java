package com.labresource.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.security.auth.AuthenticationService;
import com.labresource.platform.security.dto.LoginRequest;
import com.labresource.platform.security.dto.LoginResponse;
import com.labresource.platform.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Requirement K: /api/health remains publicly accessible without authentication")
    void testHealthEndpointPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Lab Resource Utilization Platform"));
    }

    @Test
    @DisplayName("Requirement L: Protected endpoint without token returns 401 Unauthorized")
    void testProtectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/protected-resource"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Requirement L: Protected endpoint with valid Bearer token passes security filter")
    void testProtectedEndpointWithValidTokenPassesFilter() throws Exception {
        String token = jwtService.generateToken(1L, "researcher@lab.org", 10L, List.of("ROLE_RESEARCHER_STUDENT"));

        // With valid token, the request passes through the JwtAuthenticationFilter.
        // Even if no controller handles /api/protected-resource (yielding 404), it is NOT rejected with 401 Unauthorized.
        mockMvc.perform(get("/api/protected-resource")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()); // 404 means authenticated and dispatched to MVC
    }

    @Test
    @DisplayName("Protected endpoint with invalid Bearer token returns 401 Unauthorized")
    void testProtectedEndpointWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/protected-resource")
                        .header("Authorization", "Bearer invalid-tampered-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 OK and JWT response")
    void testLoginSuccess() throws Exception {
        LoginResponse mockResponse = new LoginResponse(
                "mock-valid-jwt-token",
                "Bearer",
                86400000L,
                1L,
                "user@lab.org",
                "Alice",
                "Smith",
                10L,
                5L,
                List.of("ROLE_RESEARCHER_STUDENT")
        );

        when(authenticationService.authenticate(any(LoginRequest.class))).thenReturn(mockResponse);

        LoginRequest request = new LoginRequest("user@lab.org", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-valid-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("user@lab.org"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.institutionId").value(10))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_RESEARCHER_STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials returns 401 Unauthorized")
    void testLoginFailure() throws Exception {
        when(authenticationService.authenticate(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        LoginRequest request = new LoginRequest("user@lab.org", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login with blank payload returns 400 Bad Request")
    void testLoginValidationFailure() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("CORS: Allows requests from production Vercel frontend")
    void testCorsAllowedOriginVercel() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://frontend-beta-flax-4nvh1p8p6p.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://frontend-beta-flax-4nvh1p8p6p.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS: Allows requests from localhost:5173 for local development")
    void testCorsAllowedOriginLocalhost() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS: Rejects requests from unauthorized origin")
    void testCorsDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://unauthorized-domain.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
