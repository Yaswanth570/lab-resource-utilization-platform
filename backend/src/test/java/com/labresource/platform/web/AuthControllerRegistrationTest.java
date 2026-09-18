package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.web.GlobalExceptionHandler;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.service.DepartmentService;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.service.InstitutionService;
import com.labresource.platform.security.auth.AuthenticationService;
import com.labresource.platform.security.dto.RegisterRequest;
import com.labresource.platform.security.web.AuthController;
import com.labresource.platform.security.web.SecurityExceptionHandler;
import com.labresource.platform.user.web.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerRegistrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler(), new SecurityExceptionHandler())
                .build();
    }

    private RegisterRequest createValidPayload() {
        RegisterRequest req = new RegisterRequest();
        req.setInstitutionId(1L);
        req.setDepartmentId(10L);
        req.setEmail("researcher@apitr.edu");
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");
        req.setFirstName("Anjali");
        req.setLastName("Rao");
        req.setPhone("+91 98480 66666");
        return req;
    }

    @Test
    @DisplayName("POST /api/auth/register - Success returns 201 Created and UserResponse without passwordHash")
    void testRegisterSuccessReturns201() throws Exception {
        RegisterRequest request = createValidPayload();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(101L);
        userResponse.setInstitutionId(1L);
        userResponse.setDepartmentId(10L);
        userResponse.setEmail("researcher@apitr.edu");
        userResponse.setFirstName("Anjali");
        userResponse.setLastName("Rao");
        userResponse.setStatus("ACTIVE");
        userResponse.setRoles(List.of("ROLE_RESEARCHER_STUDENT"));

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.email").value("researcher@apitr.edu"))
                .andExpect(jsonPath("$.firstName").value("Anjali"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_RESEARCHER_STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register - Duplicate email returns 409 Conflict")
    void testRegisterDuplicateEmailReturns409() throws Exception {
        RegisterRequest request = createValidPayload();

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User", "email", "researcher@apitr.edu"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: 'researcher@apitr.edu'"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation failure (invalid email, blank fields) returns 400 Bad Request")
    void testRegisterValidationFailureReturns400() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("not-an-email");
        invalidRequest.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/auth/institutions - Returns active institutions list")
    void testGetRegistrationInstitutionsReturns200() throws Exception {
        Institution inst = new Institution();
        inst.setId(1L);
        inst.setName("APITR");
        inst.setCode("APITR");
        inst.setActive(true);

        when(institutionService.listInstitutionsByActiveStatus(true)).thenReturn(List.of(inst));

        mockMvc.perform(get("/api/auth/institutions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("APITR"));
    }

    @Test
    @DisplayName("GET /api/auth/institutions/{id}/departments - Returns active departments list")
    void testGetRegistrationDepartmentsReturns200() throws Exception {
        Institution inst = new Institution();
        inst.setId(1L);

        Department dept = new Department();
        dept.setId(10L);
        dept.setName("Computer Science & Engineering");
        dept.setCode("CSE");
        dept.setInstitution(inst);
        dept.setActive(true);

        when(departmentService.listActiveDepartmentsByInstitution(1L)).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/auth/institutions/1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Computer Science & Engineering"));
    }
}
