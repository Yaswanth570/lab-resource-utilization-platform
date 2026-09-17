package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.service.BookingService;
import com.labresource.platform.booking.web.CancelBookingRequest;
import com.labresource.platform.booking.web.ConfirmBookingRequest;
import com.labresource.platform.booking.web.CreateBookingRequest;
import com.labresource.platform.department.Department;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.User;
import com.labresource.platform.utilization.EquipmentIdleEvent;
import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.IdleDetectionSource;
import com.labresource.platform.utilization.IdleEventStatus;
import com.labresource.platform.utilization.SessionStatus;
import com.labresource.platform.utilization.service.UtilizationService;
import com.labresource.platform.utilization.web.CompleteUsageSessionRequest;
import com.labresource.platform.utilization.web.CreateUsageSessionRequest;
import com.labresource.platform.utilization.web.RecordIdleEventRequest;
import com.labresource.platform.utilization.web.ResolveIdleEventRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class BookingAndUtilizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UtilizationService utilizationService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer " + jwtService.generateToken(1L, "labmanager@lab.org", 10L, List.of("ROLE_LAB_MANAGER"));
    }

    @Test
    @DisplayName("POST /api/bookings - Success returns 201 Created")
    void testCreateBookingSuccess() throws Exception {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setEquipmentId(100L);
        req.setUserId(1L);
        req.setDepartmentId(5L);
        req.setInstitutionId(10L);
        req.setStartTime(Instant.parse("2026-09-15T09:00:00Z"));
        req.setEndTime(Instant.parse("2026-09-15T11:00:00Z"));
        req.setPurpose("Spectroscopy study");

        Booking b = new Booking();
        b.setId(200L);
        b.setBookingReference("BK-TESTREF123");
        b.setStartTime(req.getStartTime());
        b.setEndTime(req.getEndTime());
        b.setStatus(BookingStatus.PENDING_APPROVAL);
        b.setBillingStatus(BookingBillingStatus.UNBILLED);
        b.setPurpose("Spectroscopy study");

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("NMR");
        b.setEquipment(eq);

        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");
        user.setLastName("Researcher");
        b.setUser(user);

        when(bookingService.createBooking(any(Booking.class), eq(1L), eq(100L), eq(5L), eq(10L), eq(null))).thenReturn(b);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.bookingReference").value("BK-TESTREF123"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.equipmentId").value(100));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/confirm - Success confirms booking")
    void testConfirmBooking() throws Exception {
        ConfirmBookingRequest req = new ConfirmBookingRequest();
        req.setApprovedByUserId(1L);

        Booking b = new Booking();
        b.setId(200L);
        b.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.confirmBooking(200L, 1L)).thenReturn(b);

        mockMvc.perform(patch("/api/bookings/200/confirm")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/cancel - Success cancels booking")
    void testCancelBooking() throws Exception {
        CancelBookingRequest req = new CancelBookingRequest();
        req.setCancelledByUserId(1L);
        req.setCancellationReason("Schedule conflict");

        Booking b = new Booking();
        b.setId(200L);
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancellationReason("Schedule conflict");

        when(bookingService.cancelBooking(200L, 1L, "Schedule conflict")).thenReturn(b);

        mockMvc.perform(patch("/api/bookings/200/cancel")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Schedule conflict"));
    }

    @Test
    @DisplayName("POST /api/utilization/sessions - Success creates session")
    void testCreateUsageSession() throws Exception {
        CreateUsageSessionRequest req = new CreateUsageSessionRequest();
        req.setEquipmentId(100L);
        req.setUserId(1L);
        req.setCheckedInAt(Instant.parse("2026-09-15T09:05:00Z"));
        req.setScheduledDurationMinutes(120);

        EquipmentUsageSession s = new EquipmentUsageSession();
        s.setId(300L);
        s.setCheckedInAt(req.getCheckedInAt());
        s.setScheduledDurationMinutes(120);
        s.setSessionStatus(SessionStatus.ACTIVE);

        Equipment eq = new Equipment();
        eq.setId(100L);
        eq.setName("NMR");
        s.setEquipment(eq);

        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");
        s.setUser(user);

        when(utilizationService.createUsageSession(any(EquipmentUsageSession.class), eq(100L), eq(1L), eq(null), eq(null), eq(null)))
                .thenReturn(s);

        mockMvc.perform(post("/api/utilization/sessions")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.equipmentId").value(100))
                .andExpect(jsonPath("$.sessionStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/utilization/sessions/{id}/complete - Success completes session")
    void testCompleteUsageSession() throws Exception {
        CompleteUsageSessionRequest req = new CompleteUsageSessionRequest();
        req.setCheckedOutAt(Instant.parse("2026-09-15T11:00:00Z"));
        req.setNotes("Completed standard run");

        EquipmentUsageSession s = new EquipmentUsageSession();
        s.setId(300L);
        s.setSessionStatus(SessionStatus.COMPLETED);
        s.setActualDurationMinutes(115);
        s.setNotes("Completed standard run");

        when(utilizationService.completeUsageSession(eq(300L), any(Instant.class), eq("Completed standard run")))
                .thenReturn(s);

        mockMvc.perform(patch("/api/utilization/sessions/300/complete")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.sessionStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/utilization/rate - Calculates rate correctly")
    void testGetUtilizationRate() throws Exception {
        when(utilizationService.calculateEquipmentUtilizationPercentage(
                100L, LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30")))
                .thenReturn(new BigDecimal("78.50"));

        mockMvc.perform(get("/api/utilization/rate")
                        .param("equipmentId", "100")
                        .param("startDate", "2026-09-01")
                        .param("endDate", "2026-09-30")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipmentId").value(100))
                .andExpect(jsonPath("$.utilizationPercentage").value(78.50));
    }

    @Test
    @DisplayName("POST /api/utilization/idle-events - Records idle event")
    void testRecordIdleEvent() throws Exception {
        RecordIdleEventRequest req = new RecordIdleEventRequest();
        req.setEquipmentId(100L);
        req.setIdleStartTime(Instant.parse("2026-09-15T12:00:00Z"));
        req.setDetectionSource(IdleDetectionSource.MANUAL_LAB_AUDIT);

        EquipmentIdleEvent event = new EquipmentIdleEvent();
        event.setId(400L);
        event.setIdleStartTime(req.getIdleStartTime());
        event.setDetectionSource(IdleDetectionSource.MANUAL_LAB_AUDIT);
        event.setStatus(IdleEventStatus.ONGOING);

        Equipment eq = new Equipment();
        eq.setId(100L);
        event.setEquipment(eq);

        when(utilizationService.recordIdleEvent(any(EquipmentIdleEvent.class), eq(100L), eq(null), eq(null), eq(1L)))
                .thenReturn(event);

        mockMvc.perform(post("/api/utilization/idle-events")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(400))
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    @DisplayName("PATCH /api/utilization/idle-events/{id}/resolve - Resolves idle event")
    void testResolveIdleEvent() throws Exception {
        ResolveIdleEventRequest req = new ResolveIdleEventRequest();
        req.setIdleEndTime(Instant.parse("2026-09-15T12:30:00Z"));
        req.setNotes("Operator returned");

        EquipmentIdleEvent event = new EquipmentIdleEvent();
        event.setId(400L);
        event.setStatus(IdleEventStatus.RESOLVED);
        event.setIdleDurationMinutes(30);

        when(utilizationService.resolveIdleEvent(eq(400L), any(Instant.class), eq("Operator returned")))
                .thenReturn(event);

        mockMvc.perform(patch("/api/utilization/idle-events/400/resolve")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(400))
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
