package com.labresource.platform.service;

import com.labresource.platform.analytics.service.AnalyticsServiceImpl;
import com.labresource.platform.analytics.web.*;
import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.cost.service.CostService;
import com.labresource.platform.cost.web.UsageCostResponse;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.maintenance.DowntimeReasonCategory;
import com.labresource.platform.maintenance.EquipmentDowntimeLog;
import com.labresource.platform.maintenance.MaintenanceRequest;
import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import com.labresource.platform.maintenance.repository.EquipmentDowntimeLogRepository;
import com.labresource.platform.maintenance.repository.MaintenanceRequestRepository;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.SessionStatus;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EquipmentUsageSessionRepository sessionRepository;

    @Mock
    private EquipmentDowntimeLogRepository downtimeLogRepository;

    @Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Mock
    private MaintenanceWorkOrderRepository workOrderRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CostService costService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Institution institution;
    private Department department;
    private Equipment equipment1;
    private Equipment equipment2;

    @BeforeEach
    void setUp() {
        institution = new Institution();
        institution.setId(1L);
        institution.setName("Test Institution");

        department = new Department();
        department.setId(10L);
        department.setName("Biotech Department");
        department.setInstitution(institution);

        equipment1 = new Equipment();
        equipment1.setId(101L);
        equipment1.setName("Confocal Microscope");
        equipment1.setStatus(EquipmentStatus.AVAILABLE);
        equipment1.setInstitution(institution);
        equipment1.setDepartment(department);

        equipment2 = new Equipment();
        equipment2.setId(102L);
        equipment2.setName("Mass Spectrometer");
        equipment2.setStatus(EquipmentStatus.IN_USE);
        equipment2.setInstitution(institution);
        equipment2.setDepartment(department);
    }

    @Test
    @DisplayName("Tenant validation: null institutionId throws InvalidOperationException")
    void testTenantValidation_NullInstitutionThrowsException() {
        assertThrows(InvalidOperationException.class, () ->
                analyticsService.getOverview(null, null, LocalDate.now().minusDays(7), LocalDate.now()));
    }

    @Test
    @DisplayName("Date range validation: startDate after endDate throws InvalidOperationException")
    void testDateValidation_StartAfterEndThrowsException() {
        LocalDate start = LocalDate.of(2026, 9, 10);
        LocalDate end = LocalDate.of(2026, 9, 1);
        assertThrows(InvalidOperationException.class, () ->
                analyticsService.getOverview(1L, null, start, end));
    }

    @Test
    @DisplayName("Overview: Returns calculated overview KPIs correctly with mock data")
    void testGetOverview_SuccessWithData() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7); // 7 days: exactly 6 non-Sunday days = 6 * 720 = 4320 mins/equip

        when(equipmentRepository.findByInstitutionId(1L)).thenReturn(List.of(equipment1, equipment2));

        // Bookings
        Booking b1 = new Booking();
        b1.setId(201L);
        b1.setStatus(BookingStatus.COMPLETED);
        b1.setStartTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        b1.setEndTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(7200));

        Booking b2 = new Booking();
        b2.setId(202L);
        b2.setStatus(BookingStatus.CANCELLED);
        b2.setStartTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(10000));
        b2.setEndTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(14000));

        when(bookingRepository.findByInstitutionId(1L)).thenReturn(List.of(b1, b2));

        // Usage Sessions for equipment1
        EquipmentUsageSession s1 = new EquipmentUsageSession();
        s1.setId(301L);
        s1.setEquipment(equipment1);
        s1.setSessionStatus(SessionStatus.COMPLETED);
        s1.setCheckedInAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        s1.setCheckedOutAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(7200));
        s1.setActualDurationMinutes(60);
        when(sessionRepository.findByEquipmentId(101L)).thenReturn(List.of(s1));
        when(sessionRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());

        // Downtime for equipment2
        EquipmentDowntimeLog dt = new EquipmentDowntimeLog();
        dt.setId(401L);
        dt.setEquipment(equipment2);
        dt.setReasonCategory(DowntimeReasonCategory.SCHEDULED_MAINTENANCE);
        dt.setDowntimeStart(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        dt.setDowntimeEnd(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(10800));
        dt.setDurationMinutes(120);
        when(downtimeLogRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(downtimeLogRepository.findByEquipmentId(102L)).thenReturn(List.of(dt));

        // Maintenance Request & Work Order
        when(maintenanceRequestRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(maintenanceRequestRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());
        when(workOrderRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(workOrderRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());

        // Costs
        UsageCostResponse uc1 = new UsageCostResponse();
        uc1.setTotalCost(BigDecimal.valueOf(150.00));
        uc1.setBillingStatus(BookingBillingStatus.UNBILLED);
        uc1.setStartTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        uc1.setEndTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(7200));

        when(costService.listUsageCosts(1L, null, null, null)).thenReturn(List.of(uc1));

        AnalyticsOverviewResponse res = analyticsService.getOverview(1L, null, start, end);

        assertNotNull(res);
        assertEquals(2, res.getTotalBookings());
        assertEquals(1, res.getCompletedBookings());
        assertEquals(1, res.getCancelledBookings());
        assertEquals(0, res.getNoShowBookings());
        assertEquals(2, res.getActiveEquipmentCount());
        assertEquals(BigDecimal.valueOf(1.00).setScale(2), res.getTotalUsageHours());
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), res.getTotalDowntimeHours());
        assertEquals(1, res.getDowntimeIncidentCount());
        assertEquals(BigDecimal.valueOf(150.00), res.getTotalCost());
        assertEquals(BigDecimal.valueOf(150.00), res.getUnbilledCost());
    }

    @Test
    @DisplayName("Overview: Empty data returns zeroed metrics safely without division-by-zero")
    void testGetOverview_EmptyData_ZeroSafe() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        when(equipmentRepository.findByInstitutionId(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByInstitutionId(1L)).thenReturn(Collections.emptyList());
        when(costService.listUsageCosts(1L, null, null, null)).thenReturn(Collections.emptyList());

        AnalyticsOverviewResponse res = analyticsService.getOverview(1L, null, start, end);

        assertNotNull(res);
        assertEquals(0, res.getTotalBookings());
        assertEquals(BigDecimal.ZERO, res.getAverageUtilizationPercentage());
        assertEquals(BigDecimal.ZERO.setScale(2), res.getTotalOperatingHours());
        assertEquals(BigDecimal.ZERO.setScale(2), res.getTotalUsageHours());
        assertEquals(BigDecimal.ZERO, res.getTotalCost());
    }

    @Test
    @DisplayName("Utilization: Calculates per-equipment and overall utilization with daily trends")
    void testGetUtilizationAnalytics_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        when(equipmentRepository.findByInstitutionId(1L)).thenReturn(List.of(equipment1));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setEquipment(equipment1);
        session.setSessionStatus(SessionStatus.COMPLETED);
        session.setCheckedInAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        session.setCheckedOutAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(7200));
        session.setActualDurationMinutes(120);

        when(sessionRepository.findByEquipmentId(101L)).thenReturn(List.of(session));

        UtilizationAnalyticsResponse res = analyticsService.getUtilizationAnalytics(1L, null, start, end);

        assertNotNull(res);
        assertEquals(1, res.getTotalSessions());
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), res.getTotalUsageHours());
        assertEquals(1, res.getEquipmentMetrics().size());
        assertEquals(101L, res.getEquipmentMetrics().get(0).getEquipmentId());
        assertEquals(7, res.getDailyTrends().size());
    }

    @Test
    @DisplayName("Single Equipment Utilization: Valid equipment returns detailed response")
    void testGetEquipmentUtilization_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        when(equipmentRepository.findById(101L)).thenReturn(Optional.of(equipment1));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setEquipment(equipment1);
        session.setSessionStatus(SessionStatus.COMPLETED);
        session.setCheckedInAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        session.setActualDurationMinutes(180);

        when(sessionRepository.findByEquipmentId(101L)).thenReturn(List.of(session));

        EquipmentUtilizationDetailResponse res = analyticsService.getEquipmentUtilization(101L, start, end, 1L);

        assertNotNull(res);
        assertEquals(101L, res.getEquipmentId());
        assertEquals("Confocal Microscope", res.getEquipmentName());
        assertEquals(180, res.getActualUsageMinutes());
        assertEquals(BigDecimal.valueOf(3.00).setScale(2), res.getActualUsageHours());
    }

    @Test
    @DisplayName("Single Equipment Utilization: Nonexistent equipment throws ResourceNotFoundException")
    void testGetEquipmentUtilization_NonexistentThrowsNotFound() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                analyticsService.getEquipmentUtilization(999L, LocalDate.now().minusDays(7), LocalDate.now(), 1L));
    }

    @Test
    @DisplayName("Single Equipment Utilization: Cross-institution access throws InvalidOperationException")
    void testGetEquipmentUtilization_CrossInstitutionThrowsException() {
        Institution otherInst = new Institution();
        otherInst.setId(99L);
        Equipment otherEquipment = new Equipment();
        otherEquipment.setId(200L);
        otherEquipment.setInstitution(otherInst);

        when(equipmentRepository.findById(200L)).thenReturn(Optional.of(otherEquipment));

        assertThrows(InvalidOperationException.class, () ->
                analyticsService.getEquipmentUtilization(200L, LocalDate.now().minusDays(7), LocalDate.now(), 1L));
    }

    @Test
    @DisplayName("Booking Analytics: Aggregates status distribution and equipment frequencies")
    void testGetBookingAnalytics_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        Booking b = new Booking();
        b.setId(501L);
        b.setEquipment(equipment1);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setStartTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        b.setEndTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(10800)); // 2 hours = 120 mins

        when(bookingRepository.findByInstitutionId(1L)).thenReturn(List.of(b));

        BookingAnalyticsResponse res = analyticsService.getBookingAnalytics(1L, null, start, end);

        assertNotNull(res);
        assertEquals(1, res.getTotalBookings());
        assertEquals(1, res.getStatusDistribution().get(BookingStatus.CONFIRMED.name()));
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), res.getTotalBookedHours());
        assertEquals(1, res.getEquipmentFrequencies().size());
        assertEquals(101L, res.getEquipmentFrequencies().get(0).getEquipmentId());
    }

    @Test
    @DisplayName("Maintenance Analytics: Aggregates downtime reason categories and equipment rankings")
    void testGetMaintenanceAnalytics_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        when(equipmentRepository.findByInstitutionId(1L)).thenReturn(List.of(equipment1));
        when(maintenanceRequestRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(workOrderRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());

        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setEquipment(equipment1);
        log.setReasonCategory(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN);
        log.setDowntimeStart(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        log.setDurationMinutes(240);

        when(downtimeLogRepository.findByEquipmentId(101L)).thenReturn(List.of(log));

        MaintenanceAnalyticsResponse res = analyticsService.getMaintenanceAnalytics(1L, null, start, end);

        assertNotNull(res);
        assertEquals(240, res.getTotalDowntimeMinutes());
        assertEquals(BigDecimal.valueOf(4.00).setScale(2), res.getTotalDowntimeHours());
        assertEquals(1, res.getDowntimeByCategory().size());
        assertEquals(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN.name(), res.getDowntimeByCategory().get(0).getCategory());
        assertEquals(1, res.getEquipmentDowntime().size());
    }

    @Test
    @DisplayName("Cost Analytics: Aggregates department spending and top equipment costs")
    void testGetCostAnalytics_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        UsageCostResponse uc = new UsageCostResponse();
        uc.setBookingId(1L);
        uc.setDepartmentId(10L);
        uc.setDepartmentName("Biotech");
        uc.setEquipmentId(101L);
        uc.setEquipmentName("Confocal Microscope");
        uc.setTotalCost(BigDecimal.valueOf(250.00));
        uc.setBillingStatus(BookingBillingStatus.SETTLED);
        uc.setStartTime(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));

        when(costService.listUsageCosts(1L, null, null, null)).thenReturn(List.of(uc));

        CostAnalyticsResponse res = analyticsService.getCostAnalytics(1L, null, start, end);

        assertNotNull(res);
        assertEquals(BigDecimal.valueOf(250.00), res.getTotalCost());
        assertEquals(BigDecimal.valueOf(250.00), res.getSettledCost());
        assertEquals(1, res.getDepartmentCosts().size());
        assertEquals("Biotech", res.getDepartmentCosts().get(0).getDepartmentName());
        assertEquals(1, res.getEquipmentCosts().size());
        assertEquals("Confocal Microscope", res.getEquipmentCosts().get(0).getEquipmentName());
    }

    @Test
    @DisplayName("Equipment Performance: Correctly orders leaderboards")
    void testGetEquipmentPerformance_Success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 7);

        when(equipmentRepository.findByInstitutionId(1L)).thenReturn(List.of(equipment1, equipment2));

        // equipment1 has session (high util), equipment2 has none
        EquipmentUsageSession s1 = new EquipmentUsageSession();
        s1.setEquipment(equipment1);
        s1.setSessionStatus(SessionStatus.COMPLETED);
        s1.setCheckedInAt(start.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600));
        s1.setActualDurationMinutes(300);

        when(sessionRepository.findByEquipmentId(101L)).thenReturn(List.of(s1));
        when(sessionRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());
        when(downtimeLogRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(downtimeLogRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByEquipmentId(101L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByEquipmentId(102L)).thenReturn(Collections.emptyList());
        when(costService.listUsageCosts(eq(1L), any(), eq(101L), any())).thenReturn(Collections.emptyList());
        when(costService.listUsageCosts(eq(1L), any(), eq(102L), any())).thenReturn(Collections.emptyList());

        EquipmentPerformanceResponse res = analyticsService.getEquipmentPerformance(1L, null, start, end);

        assertNotNull(res);
        assertFalse(res.getMostUtilized().isEmpty());
        assertEquals(101L, res.getMostUtilized().get(0).getEquipmentId());
        assertFalse(res.getLeastUtilized().isEmpty());
        assertEquals(102L, res.getLeastUtilized().get(0).getEquipmentId());
    }

    @Test
    @DisplayName("Department validation: Department belonging to different institution throws InvalidOperationException")
    void testDepartmentFiltering_CrossInstitutionDepartmentThrows() {
        Institution otherInst = new Institution();
        otherInst.setId(999L);
        Department otherDept = new Department();
        otherDept.setId(88L);
        otherDept.setInstitution(otherInst);

        when(departmentRepository.findById(88L)).thenReturn(Optional.of(otherDept));

        assertThrows(InvalidOperationException.class, () ->
                analyticsService.getOverview(1L, 88L, LocalDate.now().minusDays(7), LocalDate.now()));
    }
}
