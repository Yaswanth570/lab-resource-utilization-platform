package com.labresource.platform.service;

import com.labresource.platform.analytics.service.AnalyticsService;
import com.labresource.platform.analytics.web.*;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentCategory;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.report.service.ReportServiceImpl;
import com.labresource.platform.report.web.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private final Long institutionId = 1L;
    private final Long foreignInstitutionId = 2L;
    private final Long departmentId = 10L;
    private final LocalDate startDate = LocalDate.of(2026, 8, 1);
    private final LocalDate endDate = LocalDate.of(2026, 8, 31);

    private Institution institution;
    private Department department;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        institution = new Institution();
        institution.setId(institutionId);
        institution.setName("Test University");

        department = new Department();
        department.setId(departmentId);
        department.setName("Physics Dept");
        department.setInstitution(institution);

        EquipmentCategory category = new EquipmentCategory();
        category.setId(100L);
        category.setName("Spectroscopy");

        equipment = new Equipment();
        equipment.setId(500L);
        equipment.setName("Mass Spectrometer 3000");
        equipment.setModelNumber("MS-3000");
        equipment.setSerialNumber("SN-98765");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setInstitution(institution);
        equipment.setDepartment(department);
        equipment.setCategory(category);
    }

    @Test
    @DisplayName("getAvailableReports returns all 6 supported report types")
    void testGetAvailableReports() {
        List<ReportTypeDefinition> reports = reportService.getAvailableReports();
        assertEquals(6, reports.size());
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("utilization")));
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("bookings")));
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("maintenance")));
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("cost")));
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("equipment")));
        assertTrue(reports.stream().anyMatch(r -> r.getId().equals("management")));
    }

    @Test
    @DisplayName("generateUtilizationReport reuses AnalyticsService and populates identical summary and rows")
    void testGenerateUtilizationReport() {
        UtilizationAnalyticsResponse analytics = new UtilizationAnalyticsResponse();
        analytics.setOverallUtilizationPercentage(new BigDecimal("45.50"));
        analytics.setTotalOperatingHours(new BigDecimal("360.00"));
        analytics.setTotalUsageHours(new BigDecimal("163.80"));
        analytics.setTotalSessions(12);

        UtilizationAnalyticsResponse.EquipmentUtilizationMetric metric = new UtilizationAnalyticsResponse.EquipmentUtilizationMetric();
        metric.setEquipmentId(500L);
        metric.setEquipmentName("Mass Spectrometer 3000");
        metric.setDepartmentId(10L);
        metric.setDepartmentName("Physics Dept");
        metric.setActualUsageHours(new BigDecimal("163.80"));
        metric.setOperatingHours(new BigDecimal("360.00"));
        metric.setUtilizationPercentage(new BigDecimal("45.50"));
        metric.setSessionCount(12);
        analytics.setEquipmentMetrics(List.of(metric));

        when(analyticsService.getUtilizationAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(analytics);

        UtilizationReportResponse report = reportService.generateUtilizationReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("UTILIZATION", report.getMetadata().getReportType());
        assertEquals(new BigDecimal("45.50"), report.getSummary().getOverallUtilizationPercentage());
        assertEquals(new BigDecimal("360.00"), report.getSummary().getTotalOperatingHours());
        assertEquals(new BigDecimal("163.80"), report.getSummary().getTotalUsageHours());
        assertEquals(12, report.getSummary().getTotalSessions());
        assertEquals(1, report.getRows().size());

        UtilizationReportResponse.UtilizationReportRow row = report.getRows().get(0);
        assertEquals(500L, row.getEquipmentId());
        assertEquals("Mass Spectrometer 3000", row.getEquipmentName());
        assertEquals(new BigDecimal("45.50"), row.getUtilizationPercentage());
    }

    @Test
    @DisplayName("generateBookingReport matches AnalyticsService values identically")
    void testGenerateBookingReport() {
        BookingAnalyticsResponse analytics = new BookingAnalyticsResponse();
        analytics.setTotalBookings(25);
        analytics.setCompletedBookings(20);
        analytics.setCancelledBookings(3);
        analytics.setNoShowBookings(2);
        analytics.setTotalBookedHours(new BigDecimal("50.00"));
        analytics.setAverageBookingDurationMinutes(new BigDecimal("120.00"));
        analytics.setStatusDistribution(Map.of("COMPLETED", 20L, "CANCELLED", 3L, "NO_SHOW", 2L));

        BookingAnalyticsResponse.EquipmentBookingFrequency freq =
                new BookingAnalyticsResponse.EquipmentBookingFrequency(500L, "Mass Spectrometer 3000", 25, new BigDecimal("50.00"));
        analytics.setEquipmentFrequencies(List.of(freq));

        when(analyticsService.getBookingAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(analytics);

        BookingReportResponse report = reportService.generateBookingReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("BOOKING", report.getMetadata().getReportType());
        assertEquals(25, report.getSummary().getTotalBookings());
        assertEquals(20, report.getSummary().getCompletedBookings());
        assertEquals(3, report.getSummary().getCancelledBookings());
        assertEquals(new BigDecimal("50.00"), report.getSummary().getTotalBookedHours());
        assertEquals(1, report.getEquipmentRows().size());
        assertEquals(500L, report.getEquipmentRows().get(0).getEquipmentId());
    }

    @Test
    @DisplayName("generateMaintenanceReport matches AnalyticsService values identically")
    void testGenerateMaintenanceReport() {
        MaintenanceAnalyticsResponse analytics = new MaintenanceAnalyticsResponse();
        analytics.setTotalRequests(5);
        analytics.setTotalWorkOrders(4);
        analytics.setTotalDowntimeHours(new BigDecimal("18.50"));
        analytics.setRequestStatusDistribution(Map.of("SUBMITTED", 1L, "COMPLETED", 4L));

        MaintenanceAnalyticsResponse.DowntimeCategoryMetric cat =
                new MaintenanceAnalyticsResponse.DowntimeCategoryMetric("SCHEDULED_PREVENTIVE", 600, 2, new BigDecimal("54.05"));
        analytics.setDowntimeByCategory(List.of(cat));

        MaintenanceAnalyticsResponse.EquipmentDowntimeMetric eqMetric =
                new MaintenanceAnalyticsResponse.EquipmentDowntimeMetric(500L, "Mass Spectrometer 3000", 1110, 3);
        analytics.setEquipmentDowntime(List.of(eqMetric));

        when(analyticsService.getMaintenanceAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(analytics);

        MaintenanceReportResponse report = reportService.generateMaintenanceReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("MAINTENANCE", report.getMetadata().getReportType());
        assertEquals(5, report.getSummary().getTotalRequests());
        assertEquals(4, report.getSummary().getTotalWorkOrders());
        assertEquals(new BigDecimal("18.50"), report.getSummary().getTotalDowntimeHours());
        assertEquals(1, report.getCategoryRows().size());
        assertEquals("SCHEDULED_PREVENTIVE", report.getCategoryRows().get(0).getCategory());
        assertEquals(1, report.getEquipmentRows().size());
        assertEquals(500L, report.getEquipmentRows().get(0).getEquipmentId());
    }

    @Test
    @DisplayName("generateCostReport matches AnalyticsService expenditure totals identically")
    void testGenerateCostReport() {
        CostAnalyticsResponse analytics = new CostAnalyticsResponse();
        analytics.setTotalCost(new BigDecimal("1250.00"));
        analytics.setUnbilledCost(new BigDecimal("250.00"));
        analytics.setInvoicedCost(new BigDecimal("500.00"));
        analytics.setSettledCost(new BigDecimal("500.00"));

        CostAnalyticsResponse.DepartmentCostMetric deptMetric =
                new CostAnalyticsResponse.DepartmentCostMetric(10L, "Physics Dept", new BigDecimal("1250.00"),
                        new BigDecimal("250.00"), new BigDecimal("500.00"), new BigDecimal("500.00"), 10);
        analytics.setDepartmentCosts(List.of(deptMetric));

        CostAnalyticsResponse.EquipmentCostMetric eqCost =
                new CostAnalyticsResponse.EquipmentCostMetric(500L, "Mass Spectrometer 3000", new BigDecimal("1250.00"),
                        new BigDecimal("25.00"), 10);
        analytics.setEquipmentCosts(List.of(eqCost));

        when(analyticsService.getCostAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(analytics);

        CostReportResponse report = reportService.generateCostReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("COST", report.getMetadata().getReportType());
        assertEquals(new BigDecimal("1250.00"), report.getSummary().getTotalCost());
        assertEquals(new BigDecimal("250.00"), report.getSummary().getUnbilledCost());
        assertEquals(new BigDecimal("500.00"), report.getSummary().getInvoicedCost());
        assertEquals(new BigDecimal("500.00"), report.getSummary().getSettledCost());
        assertEquals(1, report.getDepartmentRows().size());
        assertEquals(1, report.getEquipmentRows().size());
    }

    @Test
    @DisplayName("generateEquipmentReport aggregates inventory and performance metrics")
    void testGenerateEquipmentReport() {
        when(equipmentRepository.findByInstitutionId(institutionId)).thenReturn(List.of(equipment));

        UtilizationAnalyticsResponse utilData = new UtilizationAnalyticsResponse();
        UtilizationAnalyticsResponse.EquipmentUtilizationMetric uMetric = new UtilizationAnalyticsResponse.EquipmentUtilizationMetric();
        uMetric.setEquipmentId(500L);
        uMetric.setUtilizationPercentage(new BigDecimal("60.00"));
        utilData.setEquipmentMetrics(List.of(uMetric));
        when(analyticsService.getUtilizationAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(utilData);

        MaintenanceAnalyticsResponse maintData = new MaintenanceAnalyticsResponse();
        MaintenanceAnalyticsResponse.EquipmentDowntimeMetric dMetric = new MaintenanceAnalyticsResponse.EquipmentDowntimeMetric(500L, "Mass Spectrometer 3000", 300, 1);
        maintData.setEquipmentDowntime(List.of(dMetric));
        when(analyticsService.getMaintenanceAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(maintData);

        CostAnalyticsResponse costData = new CostAnalyticsResponse();
        CostAnalyticsResponse.EquipmentCostMetric cMetric = new CostAnalyticsResponse.EquipmentCostMetric(500L, "Mass Spectrometer 3000", new BigDecimal("800.00"), new BigDecimal("10.00"), 5);
        costData.setEquipmentCosts(List.of(cMetric));
        when(analyticsService.getCostAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(costData);

        BookingAnalyticsResponse bookingData = new BookingAnalyticsResponse();
        BookingAnalyticsResponse.EquipmentBookingFrequency bMetric = new BookingAnalyticsResponse.EquipmentBookingFrequency(500L, "Mass Spectrometer 3000", 5, new BigDecimal("10.00"));
        bookingData.setEquipmentFrequencies(List.of(bMetric));
        when(analyticsService.getBookingAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(bookingData);

        EquipmentInventoryReportResponse report = reportService.generateEquipmentReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("EQUIPMENT", report.getMetadata().getReportType());
        assertEquals(1, report.getSummary().getTotalEquipment());
        assertEquals(1, report.getSummary().getActiveEquipment());
        assertEquals(1, report.getRows().size());

        EquipmentInventoryReportResponse.EquipmentInventoryRow row = report.getRows().get(0);
        assertEquals(500L, row.getEquipmentId());
        assertEquals("Mass Spectrometer 3000", row.getEquipmentName());
        assertEquals("Physics Dept", row.getDepartmentName());
        assertEquals("Spectroscopy", row.getCategoryName());
        assertEquals(new BigDecimal("60.00"), row.getUtilizationPercentage());
        assertEquals(new BigDecimal("5.00"), row.getDowntimeHours()); // 300 mins = 5.00 hrs
        assertEquals(new BigDecimal("800.00"), row.getTotalCost());
        assertEquals(5, row.getBookingCount());
    }

    @Test
    @DisplayName("generateManagementReport aggregates executive summary KPIs")
    void testGenerateManagementReport() {
        AnalyticsOverviewResponse overview = new AnalyticsOverviewResponse();
        overview.setTotalBookings(50);
        overview.setCompletedBookings(45);
        overview.setAverageUtilizationPercentage(new BigDecimal("72.50"));
        overview.setTotalOperatingHours(new BigDecimal("1000.00"));
        overview.setTotalUsageHours(new BigDecimal("725.00"));
        overview.setTotalDowntimeHours(new BigDecimal("15.00"));
        overview.setDowntimeIncidentCount(2);
        overview.setTotalCost(new BigDecimal("5400.00"));
        overview.setUnbilledCost(new BigDecimal("400.00"));
        overview.setSettledCost(new BigDecimal("5000.00"));
        overview.setActiveEquipmentCount(10);
        overview.setOpenWorkOrderCount(1);

        EquipmentPerformanceResponse perf = new EquipmentPerformanceResponse();
        EquipmentPerformanceResponse.PerformanceItem topUtil = new EquipmentPerformanceResponse.PerformanceItem(500L, "Mass Spectrometer 3000", "Physics", new BigDecimal("72.50"), "%");
        perf.setMostUtilized(List.of(topUtil));

        when(analyticsService.getOverview(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(overview);
        when(analyticsService.getEquipmentPerformance(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(perf);

        ManagementSummaryReportResponse report = reportService.generateManagementReport(institutionId, null, startDate, endDate);

        assertNotNull(report);
        assertEquals("MANAGEMENT", report.getMetadata().getReportType());
        assertEquals(50, report.getKpis().getTotalBookings());
        assertEquals(new BigDecimal("72.50"), report.getKpis().getAverageUtilizationPercentage());
        assertEquals(new BigDecimal("5400.00"), report.getKpis().getTotalCost());
        assertEquals(1, report.getTopUtilizedEquipment().size());
    }

    @Test
    @DisplayName("exportReportCsv generates valid RFC 4180 CSV bytes for utilization report")
    void testExportReportCsv_Utilization() {
        UtilizationAnalyticsResponse analytics = new UtilizationAnalyticsResponse();
        analytics.setOverallUtilizationPercentage(new BigDecimal("50.00"));
        analytics.setTotalOperatingHours(new BigDecimal("100.00"));
        analytics.setTotalUsageHours(new BigDecimal("50.00"));
        analytics.setTotalSessions(5);

        UtilizationAnalyticsResponse.EquipmentUtilizationMetric metric = new UtilizationAnalyticsResponse.EquipmentUtilizationMetric();
        metric.setEquipmentId(500L);
        metric.setEquipmentName("Mass Spectrometer, Model 3000"); // has comma -> needs quotes
        metric.setDepartmentId(10L);
        metric.setDepartmentName("Physics, Dept");
        metric.setActualUsageHours(new BigDecimal("50.00"));
        metric.setOperatingHours(new BigDecimal("100.00"));
        metric.setUtilizationPercentage(new BigDecimal("50.00"));
        metric.setSessionCount(5);
        analytics.setEquipmentMetrics(List.of(metric));

        when(analyticsService.getUtilizationAnalytics(eq(institutionId), eq(null), eq(startDate), eq(endDate)))
                .thenReturn(analytics);

        byte[] csvBytes = reportService.exportReportCsv("utilization", institutionId, null, startDate, endDate);

        assertNotNull(csvBytes);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("# Report Title: Equipment Utilization Report"));
        assertTrue(csvContent.contains("# Report Type: UTILIZATION"));
        assertTrue(csvContent.contains("Equipment ID,Equipment Name,Department,Actual Usage (Hours),Operating Hours,Utilization (%),Sessions"));
        assertTrue(csvContent.contains("\"Mass Spectrometer, Model 3000\""));
        assertTrue(csvContent.contains("\"Physics, Dept\""));
    }

    @Test
    @DisplayName("exportReportCsv throws InvalidOperationException for unsupported type")
    void testExportReportCsv_UnsupportedType() {
        assertThrows(InvalidOperationException.class, () ->
                reportService.exportReportCsv("nonexistent_type", institutionId, null, startDate, endDate));
    }

    @Test
    @DisplayName("Inverted date range throws InvalidOperationException")
    void testInvertedDateRange_ThrowsException() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 8, 1);

        assertThrows(InvalidOperationException.class, () ->
                reportService.generateUtilizationReport(institutionId, null, start, end));
    }

    @Test
    @DisplayName("Foreign department throws InvalidOperationException")
    void testForeignDepartment_ThrowsException() {
        Department foreignDept = new Department();
        foreignDept.setId(999L);
        Institution foreignInst = new Institution();
        foreignInst.setId(foreignInstitutionId);
        foreignDept.setInstitution(foreignInst);

        when(departmentRepository.findById(999L)).thenReturn(Optional.of(foreignDept));

        assertThrows(InvalidOperationException.class, () ->
                reportService.generateUtilizationReport(institutionId, 999L, startDate, endDate));
    }
}
