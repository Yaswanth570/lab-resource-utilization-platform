package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.maintenance.*;
import com.labresource.platform.maintenance.service.MaintenanceService;
import com.labresource.platform.maintenance.web.*;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private MaintenanceService maintenanceService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "staff@lab.org", 10L, List.of("ROLE_LAB_MANAGER", "ROLE_LAB_TECHNICIAN"));
    }

    @Test
    @DisplayName("POST /api/maintenance/requests - Success creates request")
    void testCreateMaintenanceRequest() throws Exception {
        CreateMaintenanceRequestDto dto = new CreateMaintenanceRequestDto();
        dto.setEquipmentId(100L);
        dto.setReportedByUserId(1L);
        dto.setIssueTitle("Laser alignment off");
        dto.setIssueDescription("Calibration laser needs adjustment");
        dto.setPriority(MaintenancePriority.HIGH);

        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(500L);
        req.setRequestNumber("MR-2026-001");
        req.setIssueTitle(dto.getIssueTitle());
        req.setIssueDescription(dto.getIssueDescription());
        req.setPriority(MaintenancePriority.HIGH);
        req.setStatus(MaintenanceRequestStatus.SUBMITTED);

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("Laser Cutter");
        req.setEquipment(eq);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Tech");
        req.setReportedByUser(user);

        when(maintenanceService.createMaintenanceRequest(any(MaintenanceRequest.class), eq(100L), eq(1L), eq(null), eq(null)))
                .thenReturn(req);

        mockMvc.perform(post("/api/maintenance/requests")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.requestNumber").value("MR-2026-001"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("PATCH /api/maintenance/requests/{id}/triage - Triage request")
    void testTriageRequest() throws Exception {
        TriageMaintenanceRequestDto dto = new TriageMaintenanceRequestDto();
        dto.setPriority(MaintenancePriority.CRITICAL);

        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(500L);
        req.setStatus(MaintenanceRequestStatus.TRIAGED);
        req.setPriority(MaintenancePriority.CRITICAL);

        when(maintenanceService.triageMaintenanceRequest(eq(500L), eq(1L), eq(MaintenancePriority.CRITICAL)))
                .thenReturn(req);

        mockMvc.perform(patch("/api/maintenance/requests/500/triage")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.status").value("TRIAGED"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"));
    }

    @Test
    @DisplayName("POST /api/maintenance/work-orders - Success creates work order")
    void testCreateWorkOrder() throws Exception {
        CreateWorkOrderDto dto = new CreateWorkOrderDto();
        dto.setEquipmentId(100L);
        dto.setType(WorkOrderType.CORRECTIVE);
        dto.setScheduledStart(Instant.parse("2026-09-16T08:00:00Z"));
        dto.setScheduledEnd(Instant.parse("2026-09-16T12:00:00Z"));

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(600L);
        wo.setWorkOrderNumber("WO-2026-001");
        wo.setType(WorkOrderType.CORRECTIVE);
        wo.setStatus(WorkOrderStatus.SCHEDULED);
        wo.setScheduledStart(dto.getScheduledStart());
        wo.setScheduledEnd(dto.getScheduledEnd());

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("Laser Cutter");
        wo.setEquipment(eq);

        when(maintenanceService.createWorkOrder(any(MaintenanceWorkOrder.class), eq(100L), eq(null), eq(null)))
                .thenReturn(wo);

        mockMvc.perform(post("/api/maintenance/work-orders")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(600))
                .andExpect(jsonPath("$.workOrderNumber").value("WO-2026-001"))
                .andExpect(jsonPath("$.type").value("CORRECTIVE"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("PATCH /api/maintenance/work-orders/{id}/start - Starts work order")
    void testStartWorkOrder() throws Exception {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(600L);
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);

        when(maintenanceService.startWorkOrder(eq(600L), any(Instant.class))).thenReturn(wo);

        mockMvc.perform(patch("/api/maintenance/work-orders/600/start")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(600))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/maintenance/work-orders/{id}/complete - Completes work order")
    void testCompleteWorkOrder() throws Exception {
        CompleteWorkOrderDto dto = new CompleteWorkOrderDto();
        dto.setLaborHours(BigDecimal.valueOf(3.5));
        dto.setLaborCost(BigDecimal.valueOf(175.00));
        dto.setPartsCost(BigDecimal.valueOf(50.00));
        dto.setWorkPerformedSummary("Realignment completed");

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(600L);
        wo.setStatus(WorkOrderStatus.COMPLETED);
        wo.setLaborHours(BigDecimal.valueOf(3.5));
        wo.setTotalCost(BigDecimal.valueOf(225.00));

        when(maintenanceService.completeWorkOrder(
                eq(600L), any(Instant.class), eq(BigDecimal.valueOf(3.5)), eq(BigDecimal.valueOf(175.00)),
                eq(BigDecimal.valueOf(50.00)), eq("Realignment completed"), eq(null), eq(null)))
                .thenReturn(wo);

        mockMvc.perform(patch("/api/maintenance/work-orders/600/complete")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(600))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalCost").value(225.00));
    }

    @Test
    @DisplayName("POST /api/maintenance/downtime - Success records downtime")
    void testRecordDowntime() throws Exception {
        RecordDowntimeLogDto dto = new RecordDowntimeLogDto();
        dto.setEquipmentId(100L);
        dto.setReasonCategory(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN);
        dto.setDowntimeStart(Instant.parse("2026-09-16T08:00:00Z"));
        dto.setDescription("Cooling system leak");

        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setId(700L);
        log.setReasonCategory(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN);
        log.setDowntimeStart(dto.getDowntimeStart());
        log.setDescription("Cooling system leak");

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("Laser Cutter");
        log.setEquipment(eq);

        when(maintenanceService.recordDowntimeLog(any(EquipmentDowntimeLog.class), eq(100L), eq(null)))
                .thenReturn(log);

        mockMvc.perform(post("/api/maintenance/downtime")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(700))
                .andExpect(jsonPath("$.reasonCategory").value("UNSCHEDULED_BREAKDOWN"))
                .andExpect(jsonPath("$.description").value("Cooling system leak"));
    }

    @Test
    @DisplayName("PATCH /api/maintenance/downtime/{id}/end - Ends downtime")
    void testEndDowntime() throws Exception {
        EndDowntimeLogDto dto = new EndDowntimeLogDto();
        dto.setDowntimeEnd(Instant.parse("2026-09-16T10:00:00Z"));
        dto.setDurationMinutes(120);

        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setId(700L);
        log.setDowntimeEnd(dto.getDowntimeEnd());
        log.setDurationMinutes(120);

        when(maintenanceService.endDowntimeLog(eq(700L), any(Instant.class), eq(120)))
                .thenReturn(log);

        mockMvc.perform(patch("/api/maintenance/downtime/700/end")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(700))
                .andExpect(jsonPath("$.durationMinutes").value(120));
    }
}
