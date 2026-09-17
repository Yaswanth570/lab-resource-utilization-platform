package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.analytics.service.AnalyticsService;
import com.labresource.platform.analytics.web.*;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}",
        "spring.datasource.password=${DB_PASSWORD:}"
})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AnalyticsService analyticsService;

    private String validToken;
    private final Long testUserId = 50L;
    private final Long testInstitutionId = 1L;

    @BeforeEach
    void setUp() {
        validToken = jwtService.generateToken(testUserId, "testuser@lab.edu", testInstitutionId, List.of("ROLE_LAB_MANAGER"));
    }

    @Test
    @DisplayName("GET /api/analytics/overview unauthenticated returns 401 Unauthorized")
    void testGetOverview_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/overview with valid token returns 200 OK and overview DTO")
    void testGetOverview_Success() throws Exception {
        AnalyticsOverviewResponse mockResponse = new AnalyticsOverviewResponse();
        mockResponse.setTotalBookings(42);
        mockResponse.setTotalUsageHours(BigDecimal.valueOf(128.50));
        mockResponse.setAverageUtilizationPercentage(BigDecimal.valueOf(64.25));
        mockResponse.setTotalCost(BigDecimal.valueOf(3200.00));
        mockResponse.setStartDate(LocalDate.of(2026, 8, 1));
        mockResponse.setEndDate(LocalDate.of(2026, 8, 31));

        when(analyticsService.getOverview(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + validToken)
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(42))
                .andExpect(jsonPath("$.totalUsageHours").value(128.50))
                .andExpect(jsonPath("$.averageUtilizationPercentage").value(64.25))
                .andExpect(jsonPath("$.totalCost").value(3200.00));
    }

    @Test
    @DisplayName("GET /api/analytics/overview with invalid date range returns 400 Bad Request")
    void testGetOverview_InvalidDateRange_Returns400() throws Exception {
        when(analyticsService.getOverview(eq(testInstitutionId), any(), any(), any()))
                .thenThrow(new InvalidOperationException("Start date cannot be after end date"));

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + validToken)
                        .param("startDate", "2026-09-10")
                        .param("endDate", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("GET /api/analytics/utilization returns 200 OK and utilization metrics")
    void testGetUtilization_Success() throws Exception {
        UtilizationAnalyticsResponse mockResponse = new UtilizationAnalyticsResponse();
        mockResponse.setOverallUtilizationPercentage(BigDecimal.valueOf(55.50));
        mockResponse.setTotalOperatingHours(BigDecimal.valueOf(500.00));
        mockResponse.setTotalUsageHours(BigDecimal.valueOf(277.50));

        when(analyticsService.getUtilizationAnalytics(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/utilization")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallUtilizationPercentage").value(55.50))
                .andExpect(jsonPath("$.totalOperatingHours").value(500.00))
                .andExpect(jsonPath("$.totalUsageHours").value(277.50));
    }

    @Test
    @DisplayName("GET /api/analytics/utilization/equipment/{id} returns 200 OK for valid equipment")
    void testGetEquipmentUtilization_Success() throws Exception {
        EquipmentUtilizationDetailResponse mockResponse = new EquipmentUtilizationDetailResponse();
        mockResponse.setEquipmentId(101L);
        mockResponse.setEquipmentName("Confocal Microscope");
        mockResponse.setUtilizationPercentage(BigDecimal.valueOf(72.00));

        when(analyticsService.getEquipmentUtilization(eq(101L), any(), any(), eq(testInstitutionId))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/utilization/equipment/101")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipmentId").value(101))
                .andExpect(jsonPath("$.equipmentName").value("Confocal Microscope"))
                .andExpect(jsonPath("$.utilizationPercentage").value(72.00));
    }

    @Test
    @DisplayName("GET /api/analytics/utilization/equipment/{id} returns 404 for nonexistent equipment")
    void testGetEquipmentUtilization_NotFound() throws Exception {
        when(analyticsService.getEquipmentUtilization(eq(999L), any(), any(), eq(testInstitutionId)))
                .thenThrow(new ResourceNotFoundException("Equipment", "id", 999L));

        mockMvc.perform(get("/api/analytics/utilization/equipment/999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/analytics/bookings returns 200 OK and booking analytics")
    void testGetBookings_Success() throws Exception {
        BookingAnalyticsResponse mockResponse = new BookingAnalyticsResponse();
        mockResponse.setTotalBookings(50);
        mockResponse.setCompletedBookings(40);
        mockResponse.setCancelledBookings(5);
        mockResponse.setNoShowBookings(5);

        when(analyticsService.getBookingAnalytics(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/bookings")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(50))
                .andExpect(jsonPath("$.completedBookings").value(40));
    }

    @Test
    @DisplayName("GET /api/analytics/maintenance returns 200 OK and maintenance analytics")
    void testGetMaintenance_Success() throws Exception {
        MaintenanceAnalyticsResponse mockResponse = new MaintenanceAnalyticsResponse();
        mockResponse.setTotalRequests(12);
        mockResponse.setTotalWorkOrders(8);
        mockResponse.setTotalDowntimeMinutes(720);
        mockResponse.setTotalDowntimeHours(BigDecimal.valueOf(12.00));

        when(analyticsService.getMaintenanceAnalytics(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/maintenance")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(12))
                .andExpect(jsonPath("$.totalWorkOrders").value(8))
                .andExpect(jsonPath("$.totalDowntimeMinutes").value(720));
    }

    @Test
    @DisplayName("GET /api/analytics/cost returns 200 OK and cost analytics")
    void testGetCost_Success() throws Exception {
        CostAnalyticsResponse mockResponse = new CostAnalyticsResponse();
        mockResponse.setTotalCost(BigDecimal.valueOf(5400.00));
        mockResponse.setUnbilledCost(BigDecimal.valueOf(1400.00));
        mockResponse.setSettledCost(BigDecimal.valueOf(4000.00));

        when(analyticsService.getCostAnalytics(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/cost")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(5400.00))
                .andExpect(jsonPath("$.unbilledCost").value(1400.00))
                .andExpect(jsonPath("$.settledCost").value(4000.00));
    }

    @Test
    @DisplayName("GET /api/analytics/equipment-performance returns 200 OK and rankings")
    void testGetEquipmentPerformance_Success() throws Exception {
        EquipmentPerformanceResponse mockResponse = new EquipmentPerformanceResponse();
        EquipmentPerformanceResponse.PerformanceItem item = new EquipmentPerformanceResponse.PerformanceItem();
        item.setEquipmentId(101L);
        item.setEquipmentName("Confocal Microscope");
        item.setUtilizationPercentage(BigDecimal.valueOf(88.00));
        mockResponse.setMostUtilized(List.of(item));

        when(analyticsService.getEquipmentPerformance(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/equipment-performance")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mostUtilized[0].equipmentId").value(101))
                .andExpect(jsonPath("$.mostUtilized[0].equipmentName").value("Confocal Microscope"));
    }

    @Test
    @DisplayName("GET /api/analytics/trends returns 200 OK and daily trend timeline")
    void testGetTrends_Success() throws Exception {
        TrendAnalyticsResponse mockResponse = new TrendAnalyticsResponse();
        TrendAnalyticsResponse.DailyTrendPoint point = new TrendAnalyticsResponse.DailyTrendPoint(
                LocalDate.of(2026, 8, 1), 5, 120, 0, BigDecimal.valueOf(250.00));
        mockResponse.setDailyTrends(List.of(point));

        when(analyticsService.getTrends(eq(testInstitutionId), any(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analytics/trends")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyTrends[0].date").value("2026-08-01"))
                .andExpect(jsonPath("$.dailyTrends[0].bookingCount").value(5))
                .andExpect(jsonPath("$.dailyTrends[0].usageMinutes").value(120))
                .andExpect(jsonPath("$.dailyTrends[0].cost").value(250.00));
    }
}
