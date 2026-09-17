package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.service.DepartmentService;
import com.labresource.platform.department.web.CreateDepartmentRequest;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.service.InstitutionService;
import com.labresource.platform.institution.web.CreateInstitutionRequest;
import com.labresource.platform.security.jwt.JwtService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class InstitutionAndDepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private InstitutionService institutionService;

    @MockBean
    private DepartmentService departmentService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "admin@univ.edu", 10L, List.of("ROLE_SYSTEM_ADMINISTRATOR", "ROLE_INSTITUTION_ADMINISTRATOR"));
    }

    @Test
    @DisplayName("POST /api/institutions - Success returns 201 Created")
    void testCreateInstitutionSuccess() throws Exception {
        CreateInstitutionRequest req = new CreateInstitutionRequest();
        req.setCode("INST-MIT");
        req.setName("MIT");
        req.setCountry("USA");
        req.setContactEmail("admin@mit.edu");

        Institution created = new Institution();
        created.setId(10L);
        created.setCode("INST-MIT");
        created.setName("MIT");
        created.setCountry("USA");
        created.setContactEmail("admin@mit.edu");
        created.setActive(true);

        when(institutionService.createInstitution(any(Institution.class))).thenReturn(created);

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("INST-MIT"))
                .andExpect(jsonPath("$.name").value("MIT"));
    }

    @Test
    @DisplayName("POST /api/institutions - Validation failure returns 400 Bad Request")
    void testCreateInstitutionValidationFailure() throws Exception {
        CreateInstitutionRequest req = new CreateInstitutionRequest();
        // missing required code, name, country, contactEmail

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/institutions - Duplicate code returns 409 Conflict")
    void testCreateInstitutionDuplicateCode() throws Exception {
        CreateInstitutionRequest req = new CreateInstitutionRequest();
        req.setCode("DUP-CODE");
        req.setName("Duplicate Institute");
        req.setCountry("USA");
        req.setContactEmail("dup@lab.org");

        when(institutionService.createInstitution(any(Institution.class)))
                .thenThrow(new DuplicateResourceException("Institution", "code", "DUP-CODE"));

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Institution already exists with code: 'DUP-CODE'"));
    }

    @Test
    @DisplayName("GET /api/institutions/{id} - Not Found returns 404")
    void testGetInstitutionNotFound() throws Exception {
        when(institutionService.getInstitutionById(999L))
                .thenThrow(new ResourceNotFoundException("Institution", "id", 999L));

        mockMvc.perform(get("/api/institutions/999")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/departments - Success returns 201 Created")
    void testCreateDepartmentSuccess() throws Exception {
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setInstitutionId(10L);
        req.setCode("CS");
        req.setName("Computer Science");

        Department created = new Department();
        created.setId(5L);
        created.setCode("CS");
        created.setName("Computer Science");
        Institution inst = new Institution();
        inst.setId(10L);
        inst.setName("MIT");
        created.setInstitution(inst);

        when(departmentService.createDepartment(eq(10L), any(Department.class))).thenReturn(created);

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.code").value("CS"))
                .andExpect(jsonPath("$.name").value("Computer Science"))
                .andExpect(jsonPath("$.institutionId").value(10));
    }

    @Test
    @DisplayName("GET /api/departments/{id} - Success returns 200 OK")
    void testGetDepartmentById() throws Exception {
        Department dept = new Department();
        dept.setId(5L);
        dept.setCode("PHYSICS");
        dept.setName("Physics Dept");
        Institution inst = new Institution();
        inst.setId(10L);
        dept.setInstitution(inst);

        when(departmentService.getDepartmentById(5L)).thenReturn(dept);

        mockMvc.perform(get("/api/departments/5")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.code").value("PHYSICS"));
    }
}
