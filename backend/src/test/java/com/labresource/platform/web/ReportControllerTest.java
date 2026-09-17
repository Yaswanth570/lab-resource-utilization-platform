package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.report.service.ReportService;
import com.labresource.platform.report.web.*;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ReportService reportService;

    private String validToken;
    private final Long testUserId = 50L;
    private final Long testInstitutionId = 1L;

    @BeforeEach
    void setUp() {
        validToken = jwtService.generateToken(testUserId, "testuser@lab.edu", testInstitutionId, List.of("ROLE_LAB_MANAGER"));
    }

    @Test
    @DisplayName("GET /api/reports unauthenticated returns 401 Unauthorized")
    void testListReports_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reports authenticated returns 200 with report definitions")
    void testListReports_Authenticated_Returns200() throws Exception {
        when(reportService.getAvailableReports()).thenReturn(List.of(
                new ReportTypeDefinition("utilization", "Utilization Report", "Desc", List.of("JSON", "CSV")),
                new ReportTypeDefinition("bookings", "Booking Report", "Desc", List.of("JSON", "CSV"))
        ));

        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("utilization")))
                .andExpect(jsonPath("$[0].name", is("Utilization Report")));
    }

    @Test
    @DisplayName("GET /api/reports/utilization returns 200 with UtilizationReportResponse")
    void testGetUtilizationReport() throws Exception {
        UtilizationReportResponse response = new UtilizationReportResponse();
        response.setMetadata(new ReportMetadataResponse("UTILIZATION", "Equipment Utilization Report",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 1));
        response.getSummary().setOverallUtilizationPercentage(new BigDecimal("65.50"));

        when(reportService.generateUtilizationReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/utilization")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("UTILIZATION")))
                .andExpect(jsonPath("$.summary.overallUtilizationPercentage", is(65.50)));
    }

    @Test
    @DisplayName("GET /api/reports/bookings returns 200 with BookingReportResponse")
    void testGetBookingReport() throws Exception {
        BookingReportResponse response = new BookingReportResponse();
        response.setMetadata(new ReportMetadataResponse("BOOKING", "Booking Report",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 10));
        response.getSummary().setTotalBookings(10);
        response.getSummary().setCompletedBookings(8);

        when(reportService.generateBookingReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/bookings")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("BOOKING")))
                .andExpect(jsonPath("$.summary.totalBookings", is(10)))
                .andExpect(jsonPath("$.summary.completedBookings", is(8)));
    }

    @Test
    @DisplayName("GET /api/reports/maintenance returns 200 with MaintenanceReportResponse")
    void testGetMaintenanceReport() throws Exception {
        MaintenanceReportResponse response = new MaintenanceReportResponse();
        response.setMetadata(new ReportMetadataResponse("MAINTENANCE", "Maintenance Report",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 2));
        response.getSummary().setTotalRequests(4);
        response.getSummary().setTotalDowntimeHours(new BigDecimal("12.50"));

        when(reportService.generateMaintenanceReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/maintenance")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("MAINTENANCE")))
                .andExpect(jsonPath("$.summary.totalRequests", is(4)))
                .andExpect(jsonPath("$.summary.totalDowntimeHours", is(12.50)));
    }

    @Test
    @DisplayName("GET /api/reports/cost returns 200 with CostReportResponse")
    void testGetCostReport() throws Exception {
        CostReportResponse response = new CostReportResponse();
        response.setMetadata(new ReportMetadataResponse("COST", "Cost Report",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 5));
        response.getSummary().setTotalCost(new BigDecimal("2500.00"));
        response.getSummary().setSettledCost(new BigDecimal("1800.00"));

        when(reportService.generateCostReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/cost")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("COST")))
                .andExpect(jsonPath("$.summary.totalCost", is(2500.00)))
                .andExpect(jsonPath("$.summary.settledCost", is(1800.00)));
    }

    @Test
    @DisplayName("GET /api/reports/equipment returns 200 with EquipmentInventoryReportResponse")
    void testGetEquipmentReport() throws Exception {
        EquipmentInventoryReportResponse response = new EquipmentInventoryReportResponse();
        response.setMetadata(new ReportMetadataResponse("EQUIPMENT", "Equipment Inventory",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 3));
        response.getSummary().setTotalEquipment(3);
        response.getSummary().setActiveEquipment(3);

        when(reportService.generateEquipmentReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/equipment")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("EQUIPMENT")))
                .andExpect(jsonPath("$.summary.totalEquipment", is(3)));
    }

    @Test
    @DisplayName("GET /api/reports/management returns 200 with ManagementSummaryReportResponse")
    void testGetManagementReport() throws Exception {
        ManagementSummaryReportResponse response = new ManagementSummaryReportResponse();
        response.setMetadata(new ReportMetadataResponse("MANAGEMENT", "Management Summary",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 50));
        response.getKpis().setTotalBookings(50);
        response.getKpis().setTotalCost(new BigDecimal("5000.00"));

        when(reportService.generateManagementReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/management")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("MANAGEMENT")))
                .andExpect(jsonPath("$.kpis.totalBookings", is(50)))
                .andExpect(jsonPath("$.kpis.totalCost", is(5000.00)));
    }

    @Test
    @DisplayName("GET /api/reports/utilization/preview returns 200 OK")
    void testGetUtilizationPreview() throws Exception {
        UtilizationReportResponse response = new UtilizationReportResponse();
        response.setMetadata(new ReportMetadataResponse("UTILIZATION", "Equipment Utilization Report",
                LocalDateTime.now(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), testInstitutionId, null, 0));

        when(reportService.generateUtilizationReport(eq(testInstitutionId), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/utilization/preview")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.reportType", is("UTILIZATION")));
    }

    @Test
    @DisplayName("GET /api/reports/utilization/csv returns 200 with text/csv and attachment header")
    void testExportCsv_Returns200() throws Exception {
        byte[] csvContent = "Equipment ID,Equipment Name\n1,Microscope\n".getBytes(StandardCharsets.UTF_8);

        when(reportService.exportReportCsv(eq("utilization"), eq(testInstitutionId), any(), any(), any()))
                .thenReturn(csvContent);

        mockMvc.perform(get("/api/reports/utilization/csv")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"utilization-report-")))
                .andExpect(content().bytes(csvContent));
    }

    @Test
    @DisplayName("GET /api/reports/utilization/pdf returns 400 with unsupported message")
    void testExportPdf_Returns400() throws Exception {
        mockMvc.perform(get("/api/reports/utilization/pdf")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("PDF export is unsupported")));
    }

    @Test
    @DisplayName("GET /api/reports/utilization with invalid date range returns 400 Bad Request")
    void testInvalidDateRange_Returns400() throws Exception {
        when(reportService.generateUtilizationReport(eq(testInstitutionId), any(), any(), any()))
                .thenThrow(new InvalidOperationException("Start date cannot be after end date"));

        mockMvc.perform(get("/api/reports/utilization")
                        .param("startDate", "2026-09-01")
                        .param("endDate", "2026-08-01")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Start date cannot be after end date")));
    }
}
