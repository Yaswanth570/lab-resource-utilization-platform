package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.department.Department;
import com.labresource.platform.equipment.*;
import com.labresource.platform.equipment.service.EquipmentCategoryService;
import com.labresource.platform.equipment.service.EquipmentService;
import com.labresource.platform.equipment.service.EquipmentSpecificationService;
import com.labresource.platform.equipment.service.UserEquipmentQualificationService;
import com.labresource.platform.equipment.web.*;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.User;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private EquipmentCategoryService categoryService;

    @MockBean
    private EquipmentService equipmentService;

    @MockBean
    private EquipmentSpecificationService specService;

    @MockBean
    private UserEquipmentQualificationService qualificationService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "labmanager@lab.org", 10L, List.of("ROLE_LAB_MANAGER"));
    }

    @Test
    @DisplayName("POST /api/equipment-categories - Success returns 201 Created")
    void testCreateEquipmentCategory() throws Exception {
        CreateEquipmentCategoryRequest req = new CreateEquipmentCategoryRequest();
        req.setName("Spectrometer");
        req.setDescription("Optical analysis tools");

        EquipmentCategory cat = new EquipmentCategory();
        cat.setId(1L);
        cat.setName("Spectrometer");
        cat.setDescription("Optical analysis tools");

        when(categoryService.createCategory(any(EquipmentCategory.class))).thenReturn(cat);

        mockMvc.perform(post("/api/equipment-categories")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Spectrometer"));
    }

    @Test
    @DisplayName("POST /api/equipment - Success returns 201 Created")
    void testCreateEquipmentSuccess() throws Exception {
        CreateEquipmentRequest req = new CreateEquipmentRequest();
        req.setInstitutionId(10L);
        req.setDepartmentId(5L);
        req.setCategoryId(1L);
        req.setPrimaryLabManagerId(1L);
        req.setAssetTag("EQ-9999");
        req.setSerialNumber("SN-12345");
        req.setName("NMR Spectrometer 500MHz");
        req.setModelNumber("AVANCE-500");
        req.setManufacturer("Bruker");
        req.setLocationBuilding("Science Complex");
        req.setLocationRoom("Room 101");
        req.setRequiresTrainingCertification(true);
        req.setHourlyRateInternal(BigDecimal.valueOf(50.00));

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setAssetTag("EQ-9999");
        eq.setName("NMR Spectrometer 500MHz");
        eq.setStatus(EquipmentStatus.AVAILABLE);

        Institution inst = new Institution();
        inst.setId(10L);
        inst.setName("MIT");
        eq.setInstitution(inst);

        Department dept = new Department();
        dept.setId(5L);
        dept.setName("Chemistry");
        eq.setDepartment(dept);

        EquipmentCategory cat = new EquipmentCategory();
        cat.setId(1L);
        cat.setName("Spectrometer");
        eq.setCategory(cat);

        User mgr = new User();
        mgr.setId(1L);
        mgr.setFirstName("John");
        mgr.setLastName("Doe");
        eq.setPrimaryLabManager(mgr);

        when(equipmentService.createEquipment(any(Equipment.class), eq(10L), eq(5L), eq(1L), eq(1L))).thenReturn(eq);

        mockMvc.perform(post("/api/equipment")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.assetTag").value("EQ-9999"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.institutionId").value(10))
                .andExpect(jsonPath("$.departmentId").value(5))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/equipment/{id}/status - Update status returns 200 OK")
    void testUpdateEquipmentStatus() throws Exception {
        UpdateEquipmentStatusRequest req = new UpdateEquipmentStatusRequest();
        req.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        req.setReason("Scheduled calibration");

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("NMR");
        eq.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(equipmentService.updateOperationalStatus(eq(100L), eq(EquipmentStatus.UNDER_MAINTENANCE), any())).thenReturn(eq);

        mockMvc.perform(patch("/api/equipment/100/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("UNDER_MAINTENANCE"));
    }

    @Test
    @DisplayName("POST /api/qualifications - Success returns 201 Created")
    void testCreateQualification() throws Exception {
        CreateQualificationRequest req = new CreateQualificationRequest();
        req.setUserId(2L);
        req.setEquipmentId(100L);
        req.setCertifiedByUserId(1L);
        req.setNotes("Completed 4-hour hands-on training");

        UserEquipmentQualification q = new UserEquipmentQualification();
        q.setId(55L);
        q.setStatus(QualificationStatus.ACTIVE);
        q.setCertifiedAt(Instant.now());
        q.setNotes("Completed 4-hour hands-on training");

        User user = new User();
        user.setId(2L);
        user.setFirstName("Bob");
        user.setLastName("Student");
        user.setEmail("bob@lab.org");
        q.setUser(user);

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("NMR Spectrometer");
        q.setEquipment(eq);

        User certifier = new User();
        certifier.setId(1L);
        certifier.setFirstName("Tech");
        certifier.setLastName("Manager");
        q.setCertifiedBy(certifier);

        when(qualificationService.createQualification(any(UserEquipmentQualification.class), eq(2L), eq(100L), eq(1L)))
                .thenReturn(q);

        mockMvc.perform(post("/api/qualifications")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.equipmentId").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/qualifications/{id}/revoke - Revoke returns 204 No Content")
    void testRevokeQualification() throws Exception {
        RevokeQualificationRequest req = new RevokeQualificationRequest();
        req.setReason("Safety protocol breach");

        doNothing().when(qualificationService).revokeQualification(55L, "Safety protocol breach");

        mockMvc.perform(patch("/api/qualifications/55/revoke")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }
}
