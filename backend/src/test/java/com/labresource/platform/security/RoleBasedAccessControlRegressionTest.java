package com.labresource.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class RoleBasedAccessControlRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper to generate token with department
    private String tokenFor(Long userId, String email, Long instId, Long deptId, String role) {
        return jwtService.generateToken(userId, email, instId, deptId, List.of(role));
    }

    // ==========================================
    // RESEARCHER / STUDENT RBAC TESTS
    // ==========================================

    @Test
    @DisplayName("DENY: Researcher cannot create equipment (POST /api/equipment -> 403)")
    void researcherCannotCreateEquipment() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");
        String validPayload = """
            {
                "institutionId": 1,
                "departmentId": 1,
                "categoryId": 1,
                "name": "Super Microscope",
                "assetTag": "ASSET-999",
                "serialNumber": "SN-999",
                "locationBuilding": "Science Hall",
                "locationRoom": "Room 101"
            }
            """;

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Researcher cannot deactivate equipment (PATCH /api/equipment/1/deactivate -> 403)")
    void researcherCannotDeactivateEquipment() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");

        mockMvc.perform(patch("/api/equipment/1/deactivate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Researcher cannot confirm/approve booking (PATCH /api/bookings/1/confirm -> 403)")
    void researcherCannotConfirmBooking() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");

        mockMvc.perform(patch("/api/bookings/1/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Researcher cannot list roles (GET /api/roles -> 403)")
    void researcherCannotListRoles() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Researcher cannot create department (POST /api/departments -> 403)")
    void researcherCannotCreateDepartment() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");
        String validPayload = """
            {
                "institutionId": 1,
                "name": "New Department",
                "code": "DEPT-NEW",
                "billingAccountCode": "BILL-001"
            }
            """;

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Researcher cannot access analytics overview (GET /api/analytics/overview -> 403)")
    void researcherCannotAccessAnalytics() throws Exception {
        String token = tokenFor(101L, "student@lab.org", 1L, 1L, "ROLE_RESEARCHER_STUDENT");

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // LAB TECHNICIAN RBAC TESTS
    // ==========================================

    @Test
    @DisplayName("DENY: Technician cannot manage users (POST /api/users -> 403)")
    void technicianCannotManageUsers() throws Exception {
        String token = tokenFor(102L, "tech@lab.org", 1L, 1L, "ROLE_LAB_TECHNICIAN");
        String validPayload = """
            {
                "institutionId": 1,
                "departmentId": 1,
                "email": "newuser@lab.org",
                "password": "Password123!",
                "firstName": "New",
                "lastName": "User"
            }
            """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Technician cannot approve bookings (PATCH /api/bookings/1/confirm -> 403)")
    void technicianCannotApproveBooking() throws Exception {
        String token = tokenFor(102L, "tech@lab.org", 1L, 1L, "ROLE_LAB_TECHNICIAN");

        mockMvc.perform(patch("/api/bookings/1/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Technician cannot access cost management (GET /api/cost/usage -> 403)")
    void technicianCannotAccessCostUsage() throws Exception {
        String token = tokenFor(102L, "tech@lab.org", 1L, 1L, "ROLE_LAB_TECHNICIAN");

        mockMvc.perform(get("/api/cost/usage")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // DEPARTMENT HEAD RBAC TESTS
    // ==========================================

    @Test
    @DisplayName("DENY: Department Head cannot manage institutions (POST /api/institutions -> 403)")
    void departmentHeadCannotManageInstitutions() throws Exception {
        String token = tokenFor(103L, "depthead@lab.org", 1L, 2L, "ROLE_DEPARTMENT_HEAD");
        String validPayload = """
            {
                "name": "New University",
                "code": "UNIV-NEW",
                "country": "USA",
                "contactEmail": "admin@univ.edu"
            }
            """;

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DENY: Department Head cannot manage system roles (GET /api/roles -> 403)")
    void departmentHeadCannotManageRoles() throws Exception {
        String token = tokenFor(103L, "depthead@lab.org", 1L, 2L, "ROLE_DEPARTMENT_HEAD");

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // INSTITUTION ADMINISTRATOR RBAC TESTS
    // ==========================================

    @Test
    @DisplayName("DENY: Institution Admin cannot create other institutions (POST /api/institutions -> 403)")
    void instAdminCannotManageInstitutions() throws Exception {
        String token = tokenFor(104L, "instadmin@lab.org", 1L, null, "ROLE_INSTITUTION_ADMINISTRATOR");
        String validPayload = """
            {
                "name": "External University",
                "code": "UNIV-EXT",
                "country": "USA",
                "contactEmail": "admin@external.edu"
            }
            """;

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // TAMPERED / MISSING TOKEN
    // ==========================================

    @Test
    @DisplayName("DENY: Missing token returns 401 Unauthorized")
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DENY: Tampered JWT returns 401 Unauthorized")
    void tamperedJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/equipment")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.fake.signature"))
                .andExpect(status().isUnauthorized());
    }
}
