package com.labresource.platform.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.maintenance.*;
import com.labresource.platform.maintenance.repository.EquipmentDowntimeLogRepository;
import com.labresource.platform.maintenance.repository.MaintenanceRequestRepository;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.maintenance.service.MaintenanceServiceImpl;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRequestRepository requestRepository;

    @Mock
    private MaintenanceWorkOrderRepository workOrderRepository;

    @Mock
    private EquipmentDowntimeLogRepository downtimeLogRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private MaintenanceServiceImpl maintenanceService;

    private Institution institutionA;
    private Institution institutionB;
    private Department departmentA;
    private Department departmentB;
    private User userA;
    private User technicianA;
    private User technicianB;
    private Equipment equipmentA;
    private Equipment equipmentRetired;

    @BeforeEach
    void setUp() {
        institutionA = new Institution();
        institutionA.setId(1L);
        institutionA.setName("Institution A");
        institutionA.setActive(true);

        institutionB = new Institution();
        institutionB.setId(2L);
        institutionB.setName("Institution B");
        institutionB.setActive(true);

        departmentA = new Department();
        departmentA.setId(10L);
        departmentA.setName("Physics");
        departmentA.setInstitution(institutionA);
        departmentA.setActive(true);

        departmentB = new Department();
        departmentB.setId(20L);
        departmentB.setName("Chemistry");
        departmentB.setInstitution(institutionB);
        departmentB.setActive(true);

        userA = new User();
        userA.setId(100L);
        userA.setEmail("userA@inst-a.edu");
        userA.setInstitution(institutionA);
        userA.setDepartment(departmentA);
        userA.setStatus(UserStatus.ACTIVE);

        technicianA = new User();
        technicianA.setId(101L);
        technicianA.setEmail("techA@inst-a.edu");
        technicianA.setInstitution(institutionA);
        technicianA.setDepartment(departmentA);
        technicianA.setStatus(UserStatus.ACTIVE);

        technicianB = new User();
        technicianB.setId(201L);
        technicianB.setEmail("techB@inst-b.edu");
        technicianB.setInstitution(institutionB);
        technicianB.setDepartment(departmentB);
        technicianB.setStatus(UserStatus.ACTIVE);

        equipmentA = new Equipment();
        equipmentA.setId(500L);
        equipmentA.setName("Electron Microscope");
        equipmentA.setInstitution(institutionA);
        equipmentA.setDepartment(departmentA);
        equipmentA.setStatus(EquipmentStatus.AVAILABLE);

        equipmentRetired = new Equipment();
        equipmentRetired.setId(501L);
        equipmentRetired.setName("Decommissioned Centrifuge");
        equipmentRetired.setInstitution(institutionA);
        equipmentRetired.setDepartment(departmentA);
        equipmentRetired.setStatus(EquipmentStatus.RETIRED);
    }

    // ==========================================
    // MAINTENANCE REQUEST TESTS (1 - 10)
    // ==========================================

    @Test
    @DisplayName("1. Successful maintenance request creation")
    void testSuccessfulRequestCreation() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Lens calibration drift");
        req.setIssueDescription("Images are blurry at 1000x magnification");
        req.setPriority(MaintenancePriority.HIGH);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> {
            MaintenanceRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        MaintenanceRequest result = maintenanceService.createMaintenanceRequest(req, 500L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(equipmentA, result.getEquipment());
        assertEquals(userA, result.getReportedByUser());
        assertEquals(MaintenanceRequestStatus.SUBMITTED, result.getStatus());
        assertEquals(MaintenancePriority.HIGH, result.getPriority());
        assertNotNull(result.getRequestNumber());
        assertTrue(result.getRequestNumber().startsWith("MR-"));
        verify(requestRepository, times(1)).save(any(MaintenanceRequest.class));
    }

    @Test
    @DisplayName("2. Equipment not found throws ResourceNotFoundException")
    void testRequestEquipmentNotFound() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Sample stage error");
        req.setIssueDescription("Stage not moving along Z axis");

        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 999L, 100L));
    }

    @Test
    @DisplayName("3. Requester not found throws ResourceNotFoundException")
    void testRequestRequesterNotFound() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Vacuum seal leak");
        req.setIssueDescription("Chamber pressure dropping");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 999L));
    }

    @Test
    @DisplayName("4. Institution not found throws ResourceNotFoundException")
    void testRequestInstitutionNotFound() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Power supply fluctuation");
        req.setIssueDescription("Intermittent shutdown");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 100L, 999L, null));
    }

    @Test
    @DisplayName("5. Department not found throws ResourceNotFoundException")
    void testRequestDepartmentNotFound() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Cooling fan failure");
        req.setIssueDescription("High temperature alarm");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 100L, null, 999L));
    }

    @Test
    @DisplayName("6. Equipment / institution mismatch throws InvalidOperationException")
    void testRequestEquipmentInstitutionMismatch() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Optics alignment");
        req.setIssueDescription("Beam deflected");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA)); // inst A (id=1)
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(2L)).thenReturn(Optional.of(institutionB)); // inst B (id=2)

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 100L, 2L, null));
        assertTrue(ex.getMessage().contains("does not belong to institution 2"));
    }

    @Test
    @DisplayName("7. User / institution mismatch throws InvalidOperationException")
    void testRequestUserInstitutionMismatch() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Detector noise");
        req.setIssueDescription("High background noise");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA)); // inst A
        when(userRepository.findById(201L)).thenReturn(Optional.of(technicianB)); // inst B

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 201L));
        assertTrue(ex.getMessage().contains("does not match equipment institution"));
    }

    @Test
    @DisplayName("8. Department / institution mismatch throws InvalidOperationException")
    void testRequestDepartmentInstitutionMismatch() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setIssueTitle("Software crash");
        req.setIssueDescription("Acquisition freeze");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA)); // inst A
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB)); // inst B

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createMaintenanceRequest(req, 500L, 100L, null, 20L));
        assertTrue(ex.getMessage().contains("does not belong to equipment institution"));
    }

    @Test
    @DisplayName("9. Invalid request status transitions rejected")
    void testInvalidRequestStatusTransition() {
        MaintenanceRequest resolvedReq = new MaintenanceRequest();
        resolvedReq.setId(10L);
        resolvedReq.setEquipment(equipmentA);
        resolvedReq.setStatus(MaintenanceRequestStatus.RESOLVED);

        when(requestRepository.findById(10L)).thenReturn(Optional.of(resolvedReq));

        // Triaging an already resolved request is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.triageMaintenanceRequest(10L, 101L, MaintenancePriority.HIGH));

        // Rejecting an already resolved request is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.rejectMaintenanceRequest(10L, 101L, "Cannot reject resolved"));

        // Resolving an already resolved request is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.resolveMaintenanceRequest(10L, "Already done"));
    }

    @Test
    @DisplayName("10. Request triage and rejection lifecycle behavior")
    void testRequestTriageAndRejectionLifecycle() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(5L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.SUBMITTED);
        req.setIssueDescription("Sample holder jammed");

        when(requestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findById(101L)).thenReturn(Optional.of(technicianA));
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        // Valid triage
        MaintenanceRequest triaged = maintenanceService.triageMaintenanceRequest(5L, 101L, MaintenancePriority.CRITICAL);
        assertEquals(MaintenanceRequestStatus.TRIAGED, triaged.getStatus());
        assertEquals(technicianA, triaged.getTriagedByUser());
        assertEquals(MaintenancePriority.CRITICAL, triaged.getPriority());
        assertNotNull(triaged.getTriagedAt());

        // Valid rejection from TRIAGED
        when(requestRepository.findById(5L)).thenReturn(Optional.of(triaged));
        MaintenanceRequest rejected = maintenanceService.rejectMaintenanceRequest(5L, 101L, "User error; unjammed without tools");
        assertEquals(MaintenanceRequestStatus.REJECTED, rejected.getStatus());
        assertTrue(rejected.getIssueDescription().contains("Rejection reason"));
    }

    // ==========================================
    // WORK ORDER TESTS (11 - 20)
    // ==========================================

    @Test
    @DisplayName("11. Successful work-order creation")
    void testSuccessfulWorkOrderCreation() {
        Instant start = Instant.parse("2026-09-15T09:00:00Z");
        Instant end = Instant.parse("2026-09-15T12:00:00Z");

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.CORRECTIVE);
        wo.setScheduledStart(start);
        wo.setScheduledEnd(end);

        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(20L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.TRIAGED);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(requestRepository.findById(20L)).thenReturn(Optional.of(req));
        when(userRepository.findById(101L)).thenReturn(Optional.of(technicianA));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> {
            MaintenanceWorkOrder w = inv.getArgument(0);
            w.setId(77L);
            return w;
        });

        MaintenanceWorkOrder result = maintenanceService.createWorkOrder(wo, 500L, 20L, 101L);

        assertNotNull(result);
        assertEquals(77L, result.getId());
        assertEquals(equipmentA, result.getEquipment());
        assertEquals(req, result.getMaintenanceRequest());
        assertEquals(technicianA, result.getAssignedTechnician());
        assertEquals(WorkOrderStatus.SCHEDULED, result.getStatus());
        assertEquals(MaintenanceRequestStatus.WORK_ORDER_CREATED, req.getStatus());
        assertNotNull(result.getWorkOrderNumber());
        assertTrue(result.getWorkOrderNumber().startsWith("WO-"));
    }

    @Test
    @DisplayName("12. Maintenance request not found throws ResourceNotFoundException")
    void testWorkOrderRequestNotFound() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.PREVENTIVE);
        wo.setScheduledStart(Instant.parse("2026-09-15T09:00:00Z"));
        wo.setScheduledEnd(Instant.parse("2026-09-15T12:00:00Z"));

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createWorkOrder(wo, 500L, 999L, null));
    }

    @Test
    @DisplayName("13. Equipment mismatch between request and work order throws InvalidOperationException")
    void testWorkOrderEquipmentMismatch() {
        Equipment equipmentOther = new Equipment();
        equipmentOther.setId(600L);
        equipmentOther.setInstitution(institutionA);

        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(30L);
        req.setEquipment(equipmentOther); // equipment is 600L
        req.setStatus(MaintenanceRequestStatus.TRIAGED);

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.CORRECTIVE);
        wo.setScheduledStart(Instant.parse("2026-09-15T09:00:00Z"));
        wo.setScheduledEnd(Instant.parse("2026-09-15T12:00:00Z"));

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA)); // work order targets 500L
        when(requestRepository.findById(30L)).thenReturn(Optional.of(req));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createWorkOrder(wo, 500L, 30L, null));
        assertTrue(ex.getMessage().contains("does not match maintenance request equipment"));
    }

    @Test
    @DisplayName("14. Technician not found throws ResourceNotFoundException")
    void testWorkOrderTechnicianNotFound() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.PREVENTIVE);
        wo.setScheduledStart(Instant.parse("2026-09-15T09:00:00Z"));
        wo.setScheduledEnd(Instant.parse("2026-09-15T12:00:00Z"));

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createWorkOrder(wo, 500L, null, 999L));
    }

    @Test
    @DisplayName("15. Technician / institution mismatch throws InvalidOperationException")
    void testWorkOrderTechnicianInstitutionMismatch() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.CORRECTIVE);
        wo.setScheduledStart(Instant.parse("2026-09-15T09:00:00Z"));
        wo.setScheduledEnd(Instant.parse("2026-09-15T12:00:00Z"));

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA)); // inst A
        when(userRepository.findById(201L)).thenReturn(Optional.of(technicianB)); // inst B

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createWorkOrder(wo, 500L, null, 201L));
        assertTrue(ex.getMessage().contains("does not match equipment institution"));
    }

    @Test
    @DisplayName("16. Invalid work-order transitions rejected")
    void testInvalidWorkOrderTransition() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(40L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.COMPLETED);

        when(workOrderRepository.findById(40L)).thenReturn(Optional.of(wo));

        // Starting an already completed work order is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.startWorkOrder(40L, Instant.now()));

        // Completing an already completed work order is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.completeWorkOrder(40L, Instant.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null));

        // Cancelling an already completed work order is rejected
        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.cancelWorkOrder(40L, "Late cancel"));
    }

    @Test
    @DisplayName("17. Valid technician assignment and re-assignment")
    void testValidTechnicianAssignment() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(50L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(50L)).thenReturn(Optional.of(wo));
        when(userRepository.findById(101L)).thenReturn(Optional.of(technicianA));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceWorkOrder updated = maintenanceService.assignTechnician(50L, 101L);
        assertEquals(technicianA, updated.getAssignedTechnician());

        // Un-assigning technician
        MaintenanceWorkOrder unassigned = maintenanceService.assignTechnician(50L, null);
        assertNull(unassigned.getAssignedTechnician());
    }

    @Test
    @DisplayName("18. Valid start work order transitions status and equipment")
    void testValidStartWorkOrder() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(60L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(60L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant actualStart = Instant.parse("2026-09-15T09:30:00Z");
        MaintenanceWorkOrder started = maintenanceService.startWorkOrder(60L, actualStart);

        assertEquals(WorkOrderStatus.IN_PROGRESS, started.getStatus());
        assertEquals(actualStart, started.getActualStart());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentA.getStatus());
        verify(equipmentRepository, times(1)).save(equipmentA);
    }

    @Test
    @DisplayName("19. Valid completion updates costs, resolves request, and restores equipment")
    void testValidCompleteWorkOrder() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(15L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.WORK_ORDER_CREATED);

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(70L);
        wo.setEquipment(equipmentA);
        wo.setMaintenanceRequest(req);
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo.setActualStart(Instant.parse("2026-09-15T09:00:00Z"));

        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(workOrderRepository.findById(70L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo));
        when(workOrderRepository.findByMaintenanceRequestId(15L)).thenReturn(List.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant actualEnd = Instant.parse("2026-09-15T11:30:00Z");
        MaintenanceWorkOrder completed = maintenanceService.completeWorkOrder(
                70L,
                actualEnd,
                BigDecimal.valueOf(2.5),
                BigDecimal.valueOf(250.0),
                BigDecimal.valueOf(50.0),
                "Replaced optical aperture",
                "Wear and tear",
                "All diagnostics passed");

        assertEquals(WorkOrderStatus.COMPLETED, completed.getStatus());
        assertEquals(actualEnd, completed.getActualEnd());
        assertEquals(BigDecimal.valueOf(2.5), completed.getLaborHours());
        assertEquals(BigDecimal.valueOf(250.0), completed.getLaborCost());
        assertEquals(BigDecimal.valueOf(50.0), completed.getPartsCost());
        assertEquals(BigDecimal.valueOf(300.0), completed.getTotalCost());
        assertEquals(EquipmentStatus.AVAILABLE, equipmentA.getStatus());
        assertEquals(MaintenanceRequestStatus.RESOLVED, req.getStatus());
        verify(equipmentRepository, times(1)).save(equipmentA);
        verify(requestRepository, times(1)).save(req);
    }

    @Test
    @DisplayName("20. Invalid completion timestamps (actualEnd before actualStart) rejected")
    void testInvalidCompletionTimestamps() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(80L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo.setActualStart(Instant.parse("2026-09-15T10:00:00Z"));

        when(workOrderRepository.findById(80L)).thenReturn(Optional.of(wo));

        Instant invalidEnd = Instant.parse("2026-09-15T09:00:00Z"); // before actualStart

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.completeWorkOrder(80L, invalidEnd, null, null, null, null, null, null));
        assertTrue(ex.getMessage().contains("Actual completion end time must be after actual start time"));
    }

    // ==========================================
    // DOWNTIME TESTS (21 - 23)
    // ==========================================

    @Test
    @DisplayName("21. Valid downtime behavior with duration calculation")
    void testValidDowntimeBehavior() {
        Instant start = Instant.parse("2026-09-15T08:00:00Z");
        Instant end = Instant.parse("2026-09-15T10:00:00Z");

        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setReasonCategory(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN);
        log.setDescription("Power surge protection tripped");
        log.setDowntimeStart(start);
        log.setDowntimeEnd(end);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(downtimeLogRepository.save(any(EquipmentDowntimeLog.class))).thenAnswer(inv -> {
            EquipmentDowntimeLog l = inv.getArgument(0);
            l.setId(100L);
            return l;
        });

        EquipmentDowntimeLog result = maintenanceService.recordDowntimeLog(log, 500L);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(120, result.getDurationMinutes());
        assertEquals(DowntimeReasonCategory.UNSCHEDULED_BREAKDOWN, result.getReasonCategory());
    }

    @Test
    @DisplayName("22. Invalid downtime timestamps (end before start) rejected")
    void testInvalidDowntimeTimestamps() {
        EquipmentDowntimeLog log = new EquipmentDowntimeLog();
        log.setReasonCategory(DowntimeReasonCategory.FACILITY_OUTAGE);
        log.setDescription("Cooling chiller failure");
        log.setDowntimeStart(Instant.parse("2026-09-15T10:00:00Z"));
        log.setDowntimeEnd(Instant.parse("2026-09-15T09:00:00Z")); // end before start

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));

        assertThrows(InvalidOperationException.class,
                () -> maintenanceService.recordDowntimeLog(log, 500L));
    }

    @Test
    @DisplayName("23. Historical maintenance records are preserved without physical deletion")
    void testHistoricalMaintenancePreserved() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(1L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.RESOLVED);

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(2L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.COMPLETED);

        EquipmentDowntimeLog dt = new EquipmentDowntimeLog();
        dt.setId(3L);
        dt.setEquipment(equipmentA);

        when(requestRepository.findAll()).thenReturn(List.of(req));
        when(workOrderRepository.findAll()).thenReturn(List.of(wo));
        when(downtimeLogRepository.findAll()).thenReturn(List.of(dt));

        assertEquals(1, maintenanceService.listMaintenanceRequests().size());
        assertEquals(1, maintenanceService.listWorkOrders().size());
        assertEquals(1, maintenanceService.listDowntimeLogs().size());
        // Verify historical entities are retrieved cleanly
        assertEquals(1L, maintenanceService.listMaintenanceRequests().get(0).getId());
        assertEquals(2L, maintenanceService.listWorkOrders().get(0).getId());
        assertEquals(3L, maintenanceService.listDowntimeLogs().get(0).getId());
    }

    // ==========================================
    // EQUIPMENT STATUS TESTS (24 - 25)
    // ==========================================

    @Test
    @DisplayName("24. Equipment status side effects: UNDER_MAINTENANCE on start, AVAILABLE on complete/cancel")
    void testEquipmentStatusSideEffects() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(90L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(90L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // Start work order -> equipment status becomes UNDER_MAINTENANCE
        maintenanceService.startWorkOrder(90L, Instant.now());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentA.getStatus());

        // Cancel work order when no other in-progress work orders exist -> equipment status returns to AVAILABLE
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo));
        maintenanceService.cancelWorkOrder(90L, "Parts obsolete");
        assertEquals(EquipmentStatus.AVAILABLE, equipmentA.getStatus());
    }

    @Test
    @DisplayName("25. RETIRED equipment is never silently reactivated or scheduled for maintenance")
    void testRetiredEquipmentNeverReactivated() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setType(WorkOrderType.CORRECTIVE);
        wo.setScheduledStart(Instant.parse("2026-09-15T09:00:00Z"));
        wo.setScheduledEnd(Instant.parse("2026-09-15T12:00:00Z"));

        when(equipmentRepository.findById(501L)).thenReturn(Optional.of(equipmentRetired)); // RETIRED

        // Cannot create work order on RETIRED equipment
        InvalidOperationException exCreate = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.createWorkOrder(wo, 501L, null, null));
        assertTrue(exCreate.getMessage().contains("Cannot create work order for RETIRED equipment"));

        // Cannot start work order if equipment was marked RETIRED
        MaintenanceWorkOrder woRetired = new MaintenanceWorkOrder();
        woRetired.setId(95L);
        woRetired.setEquipment(equipmentRetired);
        woRetired.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(95L)).thenReturn(Optional.of(woRetired));
        InvalidOperationException exStart = assertThrows(InvalidOperationException.class,
                () -> maintenanceService.startWorkOrder(95L, Instant.now()));
        assertTrue(exStart.getMessage().contains("Cannot start maintenance on RETIRED equipment"));
        assertEquals(EquipmentStatus.RETIRED, equipmentRetired.getStatus());
    }

    // ==========================================
    // FOCUSED LIFECYCLE REGRESSION TESTS (A - G)
    // ==========================================

    @Test
    @DisplayName("A. AVAILABLE equipment: start maintenance -> complete -> AVAILABLE")
    void testAvailableEquipmentStartCompleteAvailable() {
        equipmentA.setStatus(EquipmentStatus.AVAILABLE);

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(101L);
        wo.setEquipment(equipmentA);
        wo.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(101L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // Start maintenance -> becomes UNDER_MAINTENANCE
        maintenanceService.startWorkOrder(101L, Instant.now());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentA.getStatus());

        // Complete maintenance -> returns to AVAILABLE
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo));
        maintenanceService.completeWorkOrder(101L, Instant.now().plusSeconds(3600), BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, "Done", null, null);
        assertEquals(EquipmentStatus.AVAILABLE, equipmentA.getStatus());
        verify(equipmentRepository, times(2)).save(equipmentA);
    }

    @Test
    @DisplayName("B. OUT_OF_SERVICE equipment: maintenance -> cancel -> remains OUT_OF_SERVICE")
    void testOutOfServiceEquipmentMaintenanceCancelRemainsOutOfService() {
        Equipment equipmentOOS = new Equipment();
        equipmentOOS.setId(502L);
        equipmentOOS.setName("Broken Spectrometer");
        equipmentOOS.setInstitution(institutionA);
        equipmentOOS.setStatus(EquipmentStatus.OUT_OF_SERVICE);

        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(102L);
        wo.setEquipment(equipmentOOS);
        wo.setStatus(WorkOrderStatus.SCHEDULED);

        when(workOrderRepository.findById(102L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // Start maintenance -> becomes UNDER_MAINTENANCE, records PREV_STATUS:OUT_OF_SERVICE
        maintenanceService.startWorkOrder(102L, Instant.now());
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentOOS.getStatus());

        // Cancel maintenance -> must NOT resurrect to AVAILABLE; remains OUT_OF_SERVICE
        when(workOrderRepository.findByEquipmentId(502L)).thenReturn(List.of(wo));
        maintenanceService.cancelWorkOrder(102L, "Cannot obtain parts; repair aborted");
        assertEquals(EquipmentStatus.OUT_OF_SERVICE, equipmentOOS.getStatus());
        verify(equipmentRepository, times(2)).save(equipmentOOS);
    }

    @Test
    @DisplayName("C. Multiple work orders: WO1 completes while WO2 is WAITING_FOR_PARTS -> remains UNDER_MAINTENANCE")
    void testMultipleWorkOrdersCompletesWhileOtherWaitingForPartsRemainsUnderMaintenance() {
        MaintenanceWorkOrder wo1 = new MaintenanceWorkOrder();
        wo1.setId(103L);
        wo1.setEquipment(equipmentA);
        wo1.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo1.setActualStart(Instant.now().minusSeconds(3600));

        MaintenanceWorkOrder wo2 = new MaintenanceWorkOrder();
        wo2.setId(104L);
        wo2.setEquipment(equipmentA);
        wo2.setStatus(WorkOrderStatus.WAITING_FOR_PARTS);

        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(workOrderRepository.findById(103L)).thenReturn(Optional.of(wo1));
        // Active work orders list includes wo2 in WAITING_FOR_PARTS
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.completeWorkOrder(103L, Instant.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);

        assertEquals(WorkOrderStatus.COMPLETED, wo1.getStatus());
        // Equipment MUST remain UNDER_MAINTENANCE because wo2 is WAITING_FOR_PARTS
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentA.getStatus());
    }

    @Test
    @DisplayName("D. Multiple work orders: WO1 completes while WO2 is IN_PROGRESS -> remains UNDER_MAINTENANCE")
    void testMultipleWorkOrdersCompletesWhileOtherInProgressRemainsUnderMaintenance() {
        MaintenanceWorkOrder wo1 = new MaintenanceWorkOrder();
        wo1.setId(105L);
        wo1.setEquipment(equipmentA);
        wo1.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo1.setActualStart(Instant.now().minusSeconds(3600));

        MaintenanceWorkOrder wo2 = new MaintenanceWorkOrder();
        wo2.setId(106L);
        wo2.setEquipment(equipmentA);
        wo2.setStatus(WorkOrderStatus.IN_PROGRESS);

        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(workOrderRepository.findById(105L)).thenReturn(Optional.of(wo1));
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.completeWorkOrder(105L, Instant.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);

        assertEquals(WorkOrderStatus.COMPLETED, wo1.getStatus());
        // Equipment MUST remain UNDER_MAINTENANCE because wo2 is IN_PROGRESS
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipmentA.getStatus());
    }

    @Test
    @DisplayName("E. Request with multiple work orders: WO1 completes while WO2 is active -> request does NOT become RESOLVED")
    void testRequestWithMultipleWorkOrdersNotResolvedWhileAnotherActive() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(301L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.WORK_ORDER_CREATED);

        MaintenanceWorkOrder wo1 = new MaintenanceWorkOrder();
        wo1.setId(107L);
        wo1.setEquipment(equipmentA);
        wo1.setMaintenanceRequest(req);
        wo1.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo1.setActualStart(Instant.now().minusSeconds(3600));

        MaintenanceWorkOrder wo2 = new MaintenanceWorkOrder();
        wo2.setId(108L);
        wo2.setEquipment(equipmentA);
        wo2.setMaintenanceRequest(req);
        wo2.setStatus(WorkOrderStatus.SCHEDULED); // active sibling

        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(workOrderRepository.findById(107L)).thenReturn(Optional.of(wo1));
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.findByMaintenanceRequestId(301L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.completeWorkOrder(107L, Instant.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);

        assertEquals(WorkOrderStatus.COMPLETED, wo1.getStatus());
        // Request must NOT become RESOLVED while wo2 is still SCHEDULED
        assertEquals(MaintenanceRequestStatus.WORK_ORDER_CREATED, req.getStatus());
        verify(requestRepository, never()).save(req);
    }

    @Test
    @DisplayName("F. Request with multiple work orders: all work orders COMPLETED/CANCELLED -> request becomes RESOLVED")
    void testRequestWithMultipleWorkOrdersBecomesResolvedWhenAllTerminal() {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setId(302L);
        req.setEquipment(equipmentA);
        req.setStatus(MaintenanceRequestStatus.WORK_ORDER_CREATED);

        MaintenanceWorkOrder wo1 = new MaintenanceWorkOrder();
        wo1.setId(109L);
        wo1.setEquipment(equipmentA);
        wo1.setMaintenanceRequest(req);
        wo1.setStatus(WorkOrderStatus.COMPLETED);

        MaintenanceWorkOrder wo2 = new MaintenanceWorkOrder();
        wo2.setId(110L);
        wo2.setEquipment(equipmentA);
        wo2.setMaintenanceRequest(req);
        wo2.setStatus(WorkOrderStatus.IN_PROGRESS);
        wo2.setActualStart(Instant.now().minusSeconds(1800));

        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);

        when(workOrderRepository.findById(110L)).thenReturn(Optional.of(wo2));
        when(workOrderRepository.findByEquipmentId(500L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.findByMaintenanceRequestId(302L)).thenReturn(List.of(wo1, wo2));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // When wo2 completes, all work orders [wo1=COMPLETED, wo2=COMPLETED] are terminal
        maintenanceService.completeWorkOrder(110L, Instant.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);

        assertEquals(WorkOrderStatus.COMPLETED, wo2.getStatus());
        assertEquals(MaintenanceRequestStatus.RESOLVED, req.getStatus());
        verify(requestRepository, times(1)).save(req);
    }

    @Test
    @DisplayName("G. RETIRED equipment: must never become AVAILABLE through maintenance cancellation/completion")
    void testRetiredEquipmentNeverBecomesAvailableThroughCancellationOrCompletion() {
        MaintenanceWorkOrder wo = new MaintenanceWorkOrder();
        wo.setId(111L);
        wo.setEquipment(equipmentRetired); // RETIRED
        wo.setStatus(WorkOrderStatus.IN_PROGRESS);

        when(workOrderRepository.findById(111L)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(MaintenanceWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // Cancellation on retired equipment
        maintenanceService.cancelWorkOrder(111L, "Scrapped");
        assertEquals(EquipmentStatus.RETIRED, equipmentRetired.getStatus());
        verify(equipmentRepository, never()).save(equipmentRetired);
    }
}
