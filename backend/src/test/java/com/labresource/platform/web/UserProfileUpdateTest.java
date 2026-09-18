package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.department.Department;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserRoleType;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.service.UserService;
import com.labresource.platform.user.web.UpdateUserProfileRequest;
import com.labresource.platform.user.web.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class UserProfileUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    private String userToken;
    private String otherUserToken;
    private Institution defaultInst;
    private Department defaultDept;

    @BeforeEach
    void setUp() {
        userToken = "Bearer " + jwtService.generateToken(1L, "student@univ.edu", 10L, List.of("ROLE_RESEARCHER_STUDENT"));
        otherUserToken = "Bearer " + jwtService.generateToken(2L, "other@univ.edu", 10L, List.of("ROLE_RESEARCHER_STUDENT"));

        defaultInst = new Institution();
        defaultInst.setId(10L);
        defaultInst.setName("National Science & Technology Institute");
        defaultInst.setCode("NSTI");

        defaultDept = new Department();
        defaultDept.setId(5L);
        defaultDept.setName("Biochemistry & Molecular Bio");
        defaultDept.setInstitution(defaultInst);
    }

    @Test
    @DisplayName("PUT /api/users/me - Successfully updates own profile details")
    void testUpdateOwnProfileSuccess() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Yaswanth");
        request.setLastName("Vankayalapati");
        request.setPhone("+91 98480 12345");
        request.setInstitutionId(10L);
        request.setDepartmentId(5L);

        User updated = new User();
        updated.setId(1L);
        updated.setEmail("student@univ.edu");
        updated.setFirstName("Yaswanth");
        updated.setLastName("Vankayalapati");
        updated.setPhone("+91 98480 12345");
        updated.setStatus(UserStatus.ACTIVE);
        updated.setInstitution(defaultInst);
        updated.setDepartment(defaultDept);
        updated.setRoles(Set.of(new Role(UserRoleType.ROLE_RESEARCHER_STUDENT, "Student")));

        when(userService.updateUserProfile(eq(1L), any(UpdateUserProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Yaswanth"))
                .andExpect(jsonPath("$.lastName").value("Vankayalapati"))
                .andExpect(jsonPath("$.phone").value("+91 98480 12345"))
                .andExpect(jsonPath("$.institutionId").value(10))
                .andExpect(jsonPath("$.institutionName").value("National Science & Technology Institute"))
                .andExpect(jsonPath("$.departmentId").value(5))
                .andExpect(jsonPath("$.departmentName").value("Biochemistry & Molecular Bio"))
                .andExpect(jsonPath("$.email").value("student@univ.edu"));
    }

    @Test
    @DisplayName("PATCH /api/users/me - Successfully patches own profile")
    void testPatchOwnProfileSuccess() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Yaswanth");
        request.setLastName("Updated");
        request.setPhone("+1 555-123-4567");

        User updated = new User();
        updated.setId(1L);
        updated.setEmail("student@univ.edu");
        updated.setFirstName("Yaswanth");
        updated.setLastName("Updated");
        updated.setPhone("+1 555-123-4567");
        updated.setStatus(UserStatus.ACTIVE);
        updated.setInstitution(defaultInst);

        when(userService.updateUserProfile(eq(1L), any(UpdateUserProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Yaswanth"))
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    @DisplayName("PUT /api/users/me - Unauthenticated request returns 401 Unauthorized")
    void testUpdateProfileUnauthenticated() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Yaswanth");
        request.setLastName("Vankayalapati");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/me - Blank first name returns 400 Bad Request")
    void testUpdateProfileBlankFirstName() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("");
        request.setLastName("Vankayalapati");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/me - Invalid phone format returns 400 Bad Request")
    void testUpdateProfileInvalidPhone() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Yaswanth");
        request.setLastName("Vankayalapati");
        request.setPhone("invalid-phone-abc@email.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/me - Cross-institution transfer rejection returns 400 Bad Request")
    void testUpdateProfileCrossInstitutionRejection() throws Exception {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFirstName("Yaswanth");
        request.setLastName("Vankayalapati");
        request.setInstitutionId(99L);

        when(userService.updateUserProfile(eq(1L), any(UpdateUserProfileRequest.class)))
                .thenThrow(new InvalidOperationException("Cross-institution user transfer is not permitted"));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Researcher cannot update another user profile (403 Forbidden)")
    void testCannotUpdateAnotherUserProfile() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Attacker");
        req.setLastName("Hacker");

        // User 2 attempts to call PUT /api/users/1
        mockMvc.perform(put("/api/users/1")
                        .header("Authorization", otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/me - Returns enriched institutionName and departmentName")
    void testGetCurrentUserProfileEnriched() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("student@univ.edu");
        user.setFirstName("Yaswanth");
        user.setLastName("Vankayalapati");
        user.setStatus(UserStatus.ACTIVE);
        user.setInstitution(defaultInst);
        user.setDepartment(defaultDept);
        user.setRoles(Set.of(new Role(UserRoleType.ROLE_RESEARCHER_STUDENT, "Student")));

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.institutionName").value("National Science & Technology Institute"))
                .andExpect(jsonPath("$.departmentName").value("Biochemistry & Molecular Bio"));
    }
}
