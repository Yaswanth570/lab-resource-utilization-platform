package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.common.web.GlobalExceptionHandler;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.service.DepartmentService;
import com.labresource.platform.department.web.CreateDepartmentRequest;
import com.labresource.platform.department.web.DepartmentController;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.service.InstitutionService;
import com.labresource.platform.institution.web.CreateInstitutionRequest;
import com.labresource.platform.institution.web.InstitutionController;
import com.labresource.platform.security.web.SecurityExceptionHandler;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InstitutionAndDepartmentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private InstitutionController institutionController;

    @InjectMocks
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(institutionController, departmentController)
                .setControllerAdvice(new GlobalExceptionHandler(), new SecurityExceptionHandler())
                .build();
    }

    // ==========================================
    // 1. INSTITUTION LIST LOADING TESTS
    // ==========================================

    @Test
    @DisplayName("GET /api/institutions - Success returns 200 OK with all institutions")
    void testListInstitutionsSuccess() throws Exception {
        Institution inst1 = new Institution();
        inst1.setId(1L);
        inst1.setCode("APITR");
        inst1.setName("Andhra Pradesh Institute of Technology & Research");
        inst1.setActive(true);

        Institution inst2 = new Institution();
        inst2.setId(2L);
        inst2.setCode("GRIU");
        inst2.setName("Global Research & Innovation University");
        inst2.setActive(true);

        when(institutionService.listInstitutions()).thenReturn(List.of(inst1, inst2));

        mockMvc.perform(get("/api/institutions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("APITR"))
                .andExpect(jsonPath("$[0].name").value("Andhra Pradesh Institute of Technology & Research"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("GRIU"));
    }

    @Test
    @DisplayName("GET /api/institutions?active=true - Success returns 200 OK with active institutions")
    void testListActiveInstitutionsSuccess() throws Exception {
        Institution inst1 = new Institution();
        inst1.setId(1L);
        inst1.setCode("APITR");
        inst1.setName("Andhra Pradesh Institute of Technology & Research");
        inst1.setActive(true);

        when(institutionService.listInstitutionsByActiveStatus(true)).thenReturn(List.of(inst1));

        mockMvc.perform(get("/api/institutions?active=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("APITR"));
    }

    @Test
    @DisplayName("GET /api/institutions/{id} - Success returns 200 OK")
    void testGetInstitutionByIdSuccess() throws Exception {
        Institution inst = new Institution();
        inst.setId(1L);
        inst.setCode("APITR");
        inst.setName("Andhra Pradesh Institute of Technology & Research");
        inst.setActive(true);

        when(institutionService.getInstitutionById(1L)).thenReturn(inst);

        mockMvc.perform(get("/api/institutions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("APITR"));
    }

    @Test
    @DisplayName("GET /api/institutions/{id} - Not Found returns 404")
    void testGetInstitutionNotFound() throws Exception {
        when(institutionService.getInstitutionById(999L))
                .thenThrow(new ResourceNotFoundException("Institution", "id", 999L));

        mockMvc.perform(get("/api/institutions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ==========================================
    // 2. DEPARTMENT LOADING BY INSTITUTION TESTS
    // ==========================================

    @Test
    @DisplayName("GET /api/institutions/{institutionId}/departments - Success returns 200 OK with departments filtered by institution")
    void testListDepartmentsByInstitutionSuccess() throws Exception {
        Institution inst = new Institution();
        inst.setId(1L);
        inst.setName("APITR");

        Department dept1 = new Department();
        dept1.setId(10L);
        dept1.setCode("CSE");
        dept1.setName("Computer Science & Engineering");
        dept1.setInstitution(inst);
        dept1.setActive(true);

        Department dept2 = new Department();
        dept2.setId(11L);
        dept2.setCode("ECE");
        dept2.setName("Electronics & Communication Engineering");
        dept2.setInstitution(inst);
        dept2.setActive(true);

        when(departmentService.listDepartmentsByInstitution(1L)).thenReturn(List.of(dept1, dept2));

        mockMvc.perform(get("/api/institutions/1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].code").value("CSE"))
                .andExpect(jsonPath("$[0].institutionId").value(1))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].code").value("ECE"))
                .andExpect(jsonPath("$[1].institutionId").value(1));
    }

    @Test
    @DisplayName("GET /api/institutions/{institutionId}/departments?active=true - Success returns active departments")
    void testListActiveDepartmentsByInstitutionSuccess() throws Exception {
        Institution inst = new Institution();
        inst.setId(1L);

        Department dept = new Department();
        dept.setId(10L);
        dept.setCode("CSE");
        dept.setName("Computer Science & Engineering");
        dept.setInstitution(inst);
        dept.setActive(true);

        when(departmentService.listActiveDepartmentsByInstitution(1L)).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/institutions/1/departments?active=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("CSE"));
    }

    @Test
    @DisplayName("GET /api/institutions/{institutionId}/departments - Invalid/nonexistent institution returns 404 Not Found")
    void testListDepartmentsNonexistentInstitutionReturns404() throws Exception {
        when(departmentService.listDepartmentsByInstitution(999L))
                .thenThrow(new ResourceNotFoundException("Institution", "id", 999L));

        mockMvc.perform(get("/api/institutions/999/departments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Institution not found with id: '999'"));
    }

    @Test
    @DisplayName("GET /api/institutions/{institutionId}/departments - Empty department result returns 200 OK with empty array")
    void testListDepartmentsEmptyResultReturns200() throws Exception {
        when(departmentService.listDepartmentsByInstitution(2L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/institutions/2/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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

        mockMvc.perform(get("/api/departments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.code").value("PHYSICS"))
                .andExpect(jsonPath("$.institutionId").value(10));
    }

    // ==========================================
    // 3. ADMINISTRATIVE MUTATION VALIDATION TESTS
    // ==========================================

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
        // Missing required fields

        mockMvc.perform(post("/api/institutions")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Institution already exists with code: 'DUP-CODE'"));
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.code").value("CS"))
                .andExpect(jsonPath("$.name").value("Computer Science"))
                .andExpect(jsonPath("$.institutionId").value(10));
    }
}
