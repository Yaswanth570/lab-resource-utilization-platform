package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.sharing.*;
import com.labresource.platform.sharing.service.SharingService;
import com.labresource.platform.sharing.web.CreateSharedAllocationRequest;
import com.labresource.platform.sharing.web.CreateSharingAgreementRequest;
import com.labresource.platform.sharing.web.UpdateSharedAllocationStatusRequest;
import com.labresource.platform.sharing.web.UpdateSharingAgreementStatusRequest;
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
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}",
        "spring.datasource.password=${DB_PASSWORD:}"
})
class SharingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private SharingService sharingService;

    private String validToken;
    private Institution ownerInst;
    private Institution reqInst;
    private ResourceSharingAgreement testAgreement;
    private Equipment testEquipment;
    private SharedEquipmentAllocation testAllocation;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "labmanager@nsti.edu", 10L, List.of("ROLE_LAB_MANAGER"));

        ownerInst = new Institution();
        ownerInst.setId(10L);
        ownerInst.setName("National Science & Tech Institute");
        ownerInst.setCode("NSTI");

        reqInst = new Institution();
        reqInst.setId(20L);
        reqInst.setName("Apex Research University");
        reqInst.setCode("ARU");

        testAgreement = new ResourceSharingAgreement();
        testAgreement.setId(100L);
        testAgreement.setAgreementCode("AGR-2026-001");
        testAgreement.setOwnerInstitution(ownerInst);
        testAgreement.setRequestingInstitution(reqInst);
        testAgreement.setStartDate(LocalDate.of(2026, 1, 1));
        testAgreement.setEndDate(LocalDate.of(2026, 12, 31));
        testAgreement.setBillingRateMultiplier(new BigDecimal("1.25"));
        testAgreement.setMaxMonthlyHours(120);
        testAgreement.setStatus(SharingAgreementStatus.ACTIVE);
        testAgreement.setCreatedAt(Instant.now());
        testAgreement.setUpdatedAt(Instant.now());

        testEquipment = new Equipment();
        testEquipment.setId(500L);
        testEquipment.setName("High-Resolution TEM");
        testEquipment.setAssetTag("TEM-001");
        testEquipment.setInstitution(ownerInst);
        testEquipment.setShareableExternally(true);
        testEquipment.setStatus(EquipmentStatus.AVAILABLE);

        testAllocation = new SharedEquipmentAllocation();
        testAllocation.setId(200L);
        testAllocation.setSharingAgreement(testAgreement);
        testAllocation.setEquipment(testEquipment);
        testAllocation.setCustomHourlyRate(new BigDecimal("150.00"));
        testAllocation.setActive(true);
        testAllocation.setCreatedAt(Instant.now());
    }

    // ==========================================
    // Requirement A: Unauthenticated request -> 401
    // ==========================================
    @Test
    @DisplayName("A & O: Unauthenticated requests are rejected with 401")
    void testUnauthenticatedRequestsRejected() throws Exception {
        mockMvc.perform(get("/api/sharing/agreements"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/sharing/allocations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/sharing/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // Requirement B: Authenticated list agreements -> 200
    // ==========================================
    @Test
    @DisplayName("B: Authenticated agreement list returns 200 and DTO array")
    void testListAgreementsSuccess() throws Exception {
        when(sharingService.listAgreements(any(), any(), any(), any())).thenReturn(List.of(testAgreement));

        mockMvc.perform(get("/api/sharing/agreements")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].agreementCode").value("AGR-2026-001"))
                .andExpect(jsonPath("$[0].ownerInstitutionName").value("National Science & Tech Institute"))
                .andExpect(jsonPath("$[0].billingRateMultiplier").value(1.25))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    // ==========================================
    // Requirement C: Create valid agreement -> 201
    // ==========================================
    @Test
    @DisplayName("C & P: Create valid agreement returns 201 with properly mapped DTO")
    void testCreateValidAgreement() throws Exception {
        CreateSharingAgreementRequest req = new CreateSharingAgreementRequest();
        req.setAgreementCode("AGR-2026-002");
        req.setRequestingInstitutionId(20L);
        req.setOwnerInstitutionId(10L);
        req.setStartDate(LocalDate.of(2026, 2, 1));
        req.setEndDate(LocalDate.of(2026, 11, 30));
        req.setBillingRateMultiplier(new BigDecimal("1.50"));
        req.setMaxMonthlyHours(80);

        when(sharingService.createAgreement(any(), eq(20L), eq(10L), any())).thenReturn(testAgreement);

        mockMvc.perform(post("/api/sharing/agreements")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.agreementCode").value("AGR-2026-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ==========================================
    // Requirement D: Invalid agreement -> 400
    // ==========================================
    @Test
    @DisplayName("D: Invalid agreement payload triggers validation rejection (400)")
    void testInvalidAgreementValidation() throws Exception {
        CreateSharingAgreementRequest invalidReq = new CreateSharingAgreementRequest();
        // Missing ownerInstitutionId, requestingInstitutionId, and dates

        mockMvc.perform(post("/api/sharing/agreements")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // Requirement E: Agreement detail -> 200
    // ==========================================
    @Test
    @DisplayName("E: Agreement detail returns 200 by ID and code")
    void testGetAgreementDetail() throws Exception {
        when(sharingService.getAgreementById(100L)).thenReturn(testAgreement);
        when(sharingService.getAgreementByCode("AGR-2026-001")).thenReturn(testAgreement);

        mockMvc.perform(get("/api/sharing/agreements/100")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.agreementCode").value("AGR-2026-001"));

        mockMvc.perform(get("/api/sharing/agreements/code/AGR-2026-001")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));

        when(sharingService.getAgreementById(999L))
                .thenThrow(new ResourceNotFoundException("ResourceSharingAgreement", "id", 999L));

        mockMvc.perform(get("/api/sharing/agreements/999")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // Requirement F: Agreement status transition -> 200
    // ==========================================
    @Test
    @DisplayName("F: Agreement status transition returns 200 with updated status")
    void testAgreementStatusTransition() throws Exception {
        ResourceSharingAgreement suspended = new ResourceSharingAgreement();
        suspended.setId(100L);
        suspended.setAgreementCode("AGR-2026-001");
        suspended.setStatus(SharingAgreementStatus.SUSPENDED);

        when(sharingService.updateAgreementStatus(eq(100L), eq(SharingAgreementStatus.SUSPENDED), any()))
                .thenReturn(suspended);

        UpdateSharingAgreementStatusRequest updateReq = new UpdateSharingAgreementStatusRequest(SharingAgreementStatus.SUSPENDED);

        mockMvc.perform(patch("/api/sharing/agreements/100/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    // ==========================================
    // Requirement G: Invalid agreement transition -> 400
    // ==========================================
    @Test
    @DisplayName("G: Invalid agreement status transition rejected with 400")
    void testInvalidAgreementTransition() throws Exception {
        when(sharingService.updateAgreementStatus(eq(100L), any(), any()))
                .thenThrow(new InvalidOperationException("Terminated agreements cannot change status"));

        UpdateSharingAgreementStatusRequest updateReq = new UpdateSharingAgreementStatusRequest(SharingAgreementStatus.ACTIVE);

        mockMvc.perform(patch("/api/sharing/agreements/100/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Terminated agreements cannot change status")));
    }

    // ==========================================
    // Requirement H: Create valid shared allocation -> 201
    // ==========================================
    @Test
    @DisplayName("H: Create valid shared allocation returns 201 Created")
    void testCreateValidSharedAllocation() throws Exception {
        CreateSharedAllocationRequest req = new CreateSharedAllocationRequest();
        req.setSharingAgreementId(100L);
        req.setEquipmentId(500L);
        req.setCustomHourlyRate(new BigDecimal("150.00"));
        req.setIsActive(true);

        when(sharingService.createAllocation(eq(100L), eq(500L), any(), eq(true), any()))
                .thenReturn(testAllocation);

        mockMvc.perform(post("/api/sharing/allocations")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.sharingAgreementId").value(100))
                .andExpect(jsonPath("$.equipmentId").value(500))
                .andExpect(jsonPath("$.customHourlyRate").value(150.00))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    // ==========================================
    // Requirement I: Allocation detail -> 200
    // ==========================================
    @Test
    @DisplayName("I: Allocation detail returns 200 with complete metadata")
    void testGetAllocationDetail() throws Exception {
        when(sharingService.getAllocationById(200L)).thenReturn(testAllocation);

        mockMvc.perform(get("/api/sharing/allocations/200")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.equipmentName").value("High-Resolution TEM"))
                .andExpect(jsonPath("$.equipmentAssetTag").value("TEM-001"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    // ==========================================
    // Requirements J, K, L, M: Domain integrity violations rejected
    // ==========================================
    @Test
    @DisplayName("J & K: Non-shareable equipment allocation rejected with 400")
    void testNonShareableEquipmentRejected() throws Exception {
        when(sharingService.createAllocation(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidOperationException("Equipment 'Private Laser' is not configured as externally shareable"));

        CreateSharedAllocationRequest req = new CreateSharedAllocationRequest();
        req.setSharingAgreementId(100L);
        req.setEquipmentId(501L);

        mockMvc.perform(post("/api/sharing/allocations")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not configured as externally shareable")));
    }

    @Test
    @DisplayName("L: Inactive agreement allocation rejected with 400")
    void testInactiveAgreementAllocationRejected() throws Exception {
        when(sharingService.createAllocation(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidOperationException("Cannot allocate equipment to agreement that is not ACTIVE"));

        CreateSharedAllocationRequest req = new CreateSharedAllocationRequest();
        req.setSharingAgreementId(100L);
        req.setEquipmentId(500L);

        mockMvc.perform(post("/api/sharing/allocations")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not ACTIVE")));
    }

    @Test
    @DisplayName("M: Cross-institution mismatch rejected with 400")
    void testCrossInstitutionViolationRejected() throws Exception {
        when(sharingService.createAllocation(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidOperationException("Equipment institution #30 does not match agreement owner institution #10"));

        CreateSharedAllocationRequest req = new CreateSharedAllocationRequest();
        req.setSharingAgreementId(100L);
        req.setEquipmentId(500L);

        mockMvc.perform(post("/api/sharing/allocations")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("does not match agreement owner institution")));
    }

    // ==========================================
    // Requirement N: Allocation status transition -> 200
    // ==========================================
    @Test
    @DisplayName("N: Allocation active toggle via /active and /status returns 200")
    void testAllocationStatusTransition() throws Exception {
        SharedEquipmentAllocation deactivated = new SharedEquipmentAllocation();
        deactivated.setId(200L);
        deactivated.setSharingAgreement(testAgreement);
        deactivated.setEquipment(testEquipment);
        deactivated.setActive(false);

        when(sharingService.updateAllocationActive(eq(200L), eq(false), any()))
                .thenReturn(deactivated);

        UpdateSharedAllocationStatusRequest toggleReq = new UpdateSharedAllocationStatusRequest(false);

        mockMvc.perform(patch("/api/sharing/allocations/200/active")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toggleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(patch("/api/sharing/allocations/200/status")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toggleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    // ==========================================
    // Requirement Q: Duplicate allocation rejected -> 409 Conflict
    // ==========================================
    @Test
    @DisplayName("Duplicate allocation rejected with 409 Conflict")
    void testDuplicateAllocationRejected() throws Exception {
        when(sharingService.createAllocation(any(), any(), any(), any(), any()))
                .thenThrow(new DuplicateResourceException("Equipment is already allocated under agreement"));

        CreateSharedAllocationRequest req = new CreateSharedAllocationRequest();
        req.setSharingAgreementId(100L);
        req.setEquipmentId(500L);

        mockMvc.perform(post("/api/sharing/allocations")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already allocated")));
    }
}
