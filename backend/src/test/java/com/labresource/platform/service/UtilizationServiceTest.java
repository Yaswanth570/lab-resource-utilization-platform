package com.labresource.platform.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.utilization.*;
import com.labresource.platform.utilization.repository.EquipmentIdleEventRepository;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import com.labresource.platform.utilization.service.UtilizationServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizationServiceTest {

    @Mock
    private EquipmentUsageSessionRepository sessionRepository;

    @Mock
    private EquipmentIdleEventRepository idleEventRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private UtilizationServiceImpl utilizationService;

    private Institution institutionA;
    private Institution institutionB;
    private Department departmentA;
    private Department departmentB;
    private User userA;
    private User userB;
    private Equipment equipmentA;
    private Equipment equipmentB;
    private Booking bookingA;
    private Instant baseCheckIn;
    private Instant baseCheckOut;

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

        userB = new User();
        userB.setId(200L);
        userB.setEmail("userB@inst-b.edu");
        userB.setInstitution(institutionB);
        userB.setStatus(UserStatus.ACTIVE);

        equipmentA = new Equipment();
        equipmentA.setId(500L);
        equipmentA.setName("NMR Spectrometer");
        equipmentA.setInstitution(institutionA);
        equipmentA.setDepartment(departmentA);
        equipmentA.setStatus(EquipmentStatus.AVAILABLE);

        equipmentB = new Equipment();
        equipmentB.setId(600L);
        equipmentB.setName("Mass Spectrometer");
        equipmentB.setInstitution(institutionB);
        equipmentB.setStatus(EquipmentStatus.AVAILABLE);

        bookingA = new Booking();
        bookingA.setId(1000L);
        bookingA.setEquipment(equipmentA);
        bookingA.setUser(userA);
        bookingA.setInstitution(institutionA);
        bookingA.setStartTime(Instant.parse("2026-09-14T09:00:00Z"));
        bookingA.setEndTime(Instant.parse("2026-09-14T11:00:00Z"));
        bookingA.setExternalBooking(false);

        baseCheckIn = Instant.parse("2026-09-14T09:05:00Z");
        baseCheckOut = Instant.parse("2026-09-14T10:55:00Z");
    }

    // -------------------------------------------------------------
    // Test 1: Successful Utilization Creation (with and without booking)
    // -------------------------------------------------------------
    @Test
    @DisplayName("1. Successful utilization session creation")
    void testSuccessfulUtilizationCreation() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);
        session.setCheckedOutAt(baseCheckOut);
        session.setNotes("Clean experiment run");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(1000L)).thenReturn(Optional.of(bookingA));
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> {
            EquipmentUsageSession s = inv.getArgument(0);
            s.setId(50L);
            return s;
        });

        EquipmentUsageSession result = utilizationService.createUsageSession(session, 500L, 100L, 1000L);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(equipmentA, result.getEquipment());
        assertEquals(userA, result.getUser());
        assertEquals(bookingA, result.getBooking());
        assertEquals(SessionStatus.COMPLETED, result.getSessionStatus());
        assertEquals(110, result.getActualDurationMinutes());
        assertEquals(120, result.getScheduledDurationMinutes());
        verify(sessionRepository, times(1)).save(any(EquipmentUsageSession.class));
    }

    // -------------------------------------------------------------
    // Test 2: Equipment Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("2. Equipment not found throws ResourceNotFoundException")
    void testEquipmentNotFound() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> utilizationService.createUsageSession(session, 999L, 100L, null));
    }

    // -------------------------------------------------------------
    // Test 3: User Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("3. User not found throws ResourceNotFoundException")
    void testUserNotFound() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> utilizationService.createUsageSession(session, 500L, 999L, null));
    }

    // -------------------------------------------------------------
    // Test 4: Institution Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("4. Institution not found throws ResourceNotFoundException")
    void testInstitutionNotFound() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null, null, 999L));
    }

    // -------------------------------------------------------------
    // Test 5: Department Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("5. Department not found throws ResourceNotFoundException")
    void testDepartmentNotFound() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null, 999L, null));
    }

    // -------------------------------------------------------------
    // Test 6: Booking Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("6. Booking not found throws ResourceNotFoundException")
    void testBookingNotFound() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, 999L));
    }

    // -------------------------------------------------------------
    // Test 7: Invalid Start / End Time
    // -------------------------------------------------------------
    @Test
    @DisplayName("7. Invalid check-in / check-out timestamps rejected")
    void testInvalidStartEndTime() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckOut);
        session.setCheckedOutAt(baseCheckIn); // checkIn after checkOut

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));

        assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null));

        EquipmentUsageSession nullCheckIn = new EquipmentUsageSession();
        nullCheckIn.setCheckedInAt(null);
        assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(nullCheckIn, 500L, 100L, null));
    }

    // -------------------------------------------------------------
    // Test 8: Zero / Negative Duration Rejection
    // -------------------------------------------------------------
    @Test
    @DisplayName("8. Zero or negative duration rejected")
    void testZeroOrNegativeDurationRejection() {
        EquipmentUsageSession sessionZeroDuration = new EquipmentUsageSession();
        sessionZeroDuration.setCheckedInAt(baseCheckIn);
        sessionZeroDuration.setCheckedOutAt(baseCheckOut);
        sessionZeroDuration.setActualDurationMinutes(0); // non-positive duration

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));

        assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionZeroDuration, 500L, 100L, null));

        EquipmentUsageSession sessionNegativeSched = new EquipmentUsageSession();
        sessionNegativeSched.setCheckedInAt(baseCheckIn);
        sessionNegativeSched.setCheckedOutAt(baseCheckOut);
        sessionNegativeSched.setScheduledDurationMinutes(-10); // negative scheduled

        assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionNegativeSched, 500L, 100L, null));
    }

    // -------------------------------------------------------------
    // Test 9: Booking / Equipment Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("9. Booking equipment mismatch throws InvalidOperationException")
    void testBookingEquipmentMismatch() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        // Booking is for equipmentA (500L), but session request targets equipmentB (600L)
        when(equipmentRepository.findById(600L)).thenReturn(Optional.of(equipmentB));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(1000L)).thenReturn(Optional.of(bookingA));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 600L, 100L, 1000L));
        assertTrue(ex.getMessage().contains("does not match utilization equipment"));
    }

    // -------------------------------------------------------------
    // Test 10: User / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("10. User / institution mismatch throws InvalidOperationException")
    void testUserInstitutionMismatch() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        // User belongs to institutionA (id=1), explicit institution is institutionB (id=2)
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(2L)).thenReturn(Optional.of(institutionB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null, null, 2L));
        assertTrue(ex.getMessage().contains("does not belong to institution 2"));
    }

    // -------------------------------------------------------------
    // Test 11: Department / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("11. Department / institution mismatch throws InvalidOperationException")
    void testDepartmentInstitutionMismatch() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        // Department B belongs to institutionB (id=2), explicit institution is institutionA (id=1), user from inst A
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null, 20L, 1L));
        assertTrue(ex.getMessage().contains("does not belong to institution 1"));
    }

    // -------------------------------------------------------------
    // Test 12: Equipment / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("12. Equipment / institution mismatch throws InvalidOperationException")
    void testEquipmentInstitutionMismatch() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        // Booking specifies equipment B (institution B), but internal booking institution is institution A
        Booking internalBookingWrongInst = new Booking();
        internalBookingWrongInst.setId(2000L);
        internalBookingWrongInst.setEquipment(equipmentB);
        internalBookingWrongInst.setUser(userA);
        internalBookingWrongInst.setInstitution(institutionA);
        internalBookingWrongInst.setExternalBooking(false);

        when(equipmentRepository.findById(600L)).thenReturn(Optional.of(equipmentB));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(2000L)).thenReturn(Optional.of(internalBookingWrongInst));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 600L, 100L, 2000L));
        assertTrue(ex.getMessage().contains("equipment institution does not match booking institution"));
    }

    // -------------------------------------------------------------
    // Test 13: Cross-Institution Utilization Rejection
    // -------------------------------------------------------------
    @Test
    @DisplayName("13. Arbitrary cross-institution utilization without external booking rejected")
    void testCrossInstitutionUtilizationRejection() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(baseCheckIn);

        // User B (institution B) attempts to record utilization on Equipment A (institution A) without a booking
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(200L)).thenReturn(Optional.of(userB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 200L, null));
        assertTrue(ex.getMessage().contains("Arbitrary cross-institution utilization rejected"));
    }

    // -------------------------------------------------------------
    // Test 14: Successful Retrieval by Equipment
    // -------------------------------------------------------------
    @Test
    @DisplayName("14. Successful retrieval by equipment")
    void testRetrievalByEquipment() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setId(10L);
        session.setEquipment(equipmentA);

        when(equipmentRepository.existsById(500L)).thenReturn(true);
        when(sessionRepository.findByEquipmentId(500L)).thenReturn(List.of(session));

        List<EquipmentUsageSession> list = utilizationService.listUsageSessionsByEquipment(500L);
        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).getId());
    }

    // -------------------------------------------------------------
    // Test 15: Successful Retrieval by User
    // -------------------------------------------------------------
    @Test
    @DisplayName("15. Successful retrieval by user")
    void testRetrievalByUser() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setId(10L);
        session.setUser(userA);

        when(userRepository.existsById(100L)).thenReturn(true);
        when(sessionRepository.findByUserId(100L)).thenReturn(List.of(session));

        List<EquipmentUsageSession> list = utilizationService.listUsageSessionsByUser(100L);
        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).getId());
    }

    // -------------------------------------------------------------
    // Test 16: Successful Retrieval by Booking
    // -------------------------------------------------------------
    @Test
    @DisplayName("16. Successful retrieval by booking")
    void testRetrievalByBooking() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setId(10L);
        session.setBooking(bookingA);

        when(bookingRepository.existsById(1000L)).thenReturn(true);
        when(sessionRepository.findByBookingId(1000L)).thenReturn(Optional.of(session));

        EquipmentUsageSession result = utilizationService.getUsageSessionByBooking(1000L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    // -------------------------------------------------------------
    // Test 17: Successful Period Query
    // -------------------------------------------------------------
    @Test
    @DisplayName("17. Successful period query for equipment usage sessions")
    void testPeriodQuery() {
        EquipmentUsageSession session1 = new EquipmentUsageSession();
        session1.setId(1L);
        session1.setCheckedInAt(Instant.parse("2026-09-14T09:00:00Z"));
        session1.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));

        EquipmentUsageSession session2 = new EquipmentUsageSession();
        session2.setId(2L);
        session2.setCheckedInAt(Instant.parse("2026-09-14T14:00:00Z"));
        session2.setCheckedOutAt(Instant.parse("2026-09-14T16:00:00Z"));

        when(equipmentRepository.existsById(500L)).thenReturn(true);
        when(sessionRepository.findByEquipmentId(500L)).thenReturn(List.of(session1, session2));

        // Query between 08:00 and 12:00 -> should return session1 only
        List<EquipmentUsageSession> results = utilizationService.listUsageSessionsForPeriod(
                500L,
                Instant.parse("2026-09-14T08:00:00Z"),
                Instant.parse("2026-09-14T12:00:00Z"));

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    // -------------------------------------------------------------
    // Test 18: Idle / Usage Validation & Resolution
    // -------------------------------------------------------------
    @Test
    @DisplayName("18. Idle event recording and resolution lifecycle")
    void testIdleEventLifecycle() {
        EquipmentIdleEvent idleEvent = new EquipmentIdleEvent();
        idleEvent.setIdleStartTime(Instant.parse("2026-09-14T11:00:00Z"));
        idleEvent.setNotes("Operator stepped away");

        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(idleEventRepository.save(any(EquipmentIdleEvent.class))).thenAnswer(inv -> {
            EquipmentIdleEvent e = inv.getArgument(0);
            e.setId(88L);
            return e;
        });

        EquipmentIdleEvent recorded = utilizationService.recordIdleEvent(idleEvent, 500L, null, null, null);
        assertNotNull(recorded);
        assertEquals(88L, recorded.getId());
        assertEquals(IdleEventStatus.ONGOING, recorded.getStatus());
        assertEquals(IdleDetectionSource.MANUAL_LAB_AUDIT, recorded.getDetectionSource());

        // Resolve idle event
        when(idleEventRepository.findById(88L)).thenReturn(Optional.of(recorded));
        Instant resolveTime = Instant.parse("2026-09-14T11:45:00Z");
        EquipmentIdleEvent resolved = utilizationService.resolveIdleEvent(88L, resolveTime, "Operator returned");

        assertEquals(IdleEventStatus.RESOLVED, resolved.getStatus());
        assertEquals(45, resolved.getIdleDurationMinutes());
        assertEquals(resolveTime, resolved.getIdleEndTime());
    }

    // -------------------------------------------------------------
    // Test 19: Historical Records Preservation (No Deletion)
    // -------------------------------------------------------------
    @Test
    @DisplayName("19. Historical records are preserved (session completion and retrieval)")
    void testHistoricalRecordsPreservation() {
        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setId(99L);
        session.setCheckedInAt(baseCheckIn);
        session.setSessionStatus(SessionStatus.ACTIVE);

        when(sessionRepository.findById(99L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentUsageSession completed = utilizationService.completeUsageSession(99L, baseCheckOut, "Routine completion");
        assertEquals(SessionStatus.COMPLETED, completed.getSessionStatus());
        assertNotNull(completed.getCheckedOutAt());
        assertNotNull(completed.getActualDurationMinutes());

        // Verify that list and get methods return the persisted historical entity
        when(sessionRepository.findAll()).thenReturn(List.of(completed));
        List<EquipmentUsageSession> all = utilizationService.listUsageSessions();
        assertEquals(1, all.size());
        assertEquals(99L, all.get(0).getId());
    }

    // -------------------------------------------------------------
    // Test 20: Baseline Operating-Window Utilization Calculation
    // -------------------------------------------------------------
    @Test
    @DisplayName("20. Baseline operating-window utilization calculation (08:00–20:00, Mon–Sat)")
    void testCalculateEquipmentUtilizationPercentage() {
        // Monday 2026-09-14 to Wednesday 2026-09-16 (3 working days = 3 * 720m = 2160 available minutes)
        LocalDate startDate = LocalDate.of(2026, 9, 14);
        LocalDate endDate = LocalDate.of(2026, 9, 16);

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setId(1L);
        session.setEquipment(equipmentA);
        session.setCheckedInAt(Instant.parse("2026-09-14T09:00:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T15:00:00Z"));
        session.setActualDurationMinutes(360); // 6 hours = 360 minutes
        session.setSessionStatus(SessionStatus.COMPLETED);

        when(equipmentRepository.existsById(500L)).thenReturn(true);
        when(sessionRepository.findByEquipmentId(500L)).thenReturn(List.of(session));

        BigDecimal percentage = utilizationService.calculateEquipmentUtilizationPercentage(500L, startDate, endDate);

        // 360 / 2160 = 16.67%
        assertEquals(new BigDecimal("16.67"), percentage);
    }

    // -------------------------------------------------------------
    // Test 21: Multiple usage sessions referencing the same booking are allowed
    // -------------------------------------------------------------
    @Test
    @DisplayName("21. Multiple usage sessions referencing the same booking are allowed")
    void testMultipleUsageSessionsForSameBookingAllowed() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(1000L)).thenReturn(Optional.of(bookingA));
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> inv.getArgument(0));

        // First session under bookingA
        EquipmentUsageSession session1 = new EquipmentUsageSession();
        session1.setCheckedInAt(Instant.parse("2026-09-14T09:00:00Z"));
        session1.setCheckedOutAt(Instant.parse("2026-09-14T09:45:00Z"));
        EquipmentUsageSession result1 = utilizationService.createUsageSession(session1, 500L, 100L, 1000L);

        // Second session under the exact same bookingA
        EquipmentUsageSession session2 = new EquipmentUsageSession();
        session2.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        session2.setCheckedOutAt(Instant.parse("2026-09-14T10:45:00Z"));
        EquipmentUsageSession result2 = utilizationService.createUsageSession(session2, 500L, 100L, 1000L);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(bookingA, result1.getBooking());
        assertEquals(bookingA, result2.getBooking());
        verify(sessionRepository, times(2)).save(any(EquipmentUsageSession.class));
    }

    // -------------------------------------------------------------
    // Test 22: 10:00-11:00 with actualDurationMinutes=500 is rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("22. 10:00-11:00 with actualDurationMinutes=500 is rejected")
    void testContradictoryActualDurationRejected() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        session.setActualDurationMinutes(500); // 500 contradicts 60 min timestamp duration

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, null));
        assertTrue(ex.getMessage().contains("Explicit actual duration 500 minutes does not match timestamp duration 60 minutes"));
    }

    // -------------------------------------------------------------
    // Test 23: Correct explicit duration is accepted
    // -------------------------------------------------------------
    @Test
    @DisplayName("23. Correct explicit duration is accepted")
    void testCorrectExplicitActualDurationAccepted() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        session.setActualDurationMinutes(60); // exactly matches 10:00 to 11:00

        EquipmentUsageSession result = utilizationService.createUsageSession(session, 500L, 100L, null);
        assertNotNull(result);
        assertEquals(60, result.getActualDurationMinutes());
        assertEquals(60, result.getScheduledDurationMinutes());
        assertEquals(SessionStatus.COMPLETED, result.getSessionStatus());
    }

    // -------------------------------------------------------------
    // Test 24: Omitted duration is correctly calculated
    // -------------------------------------------------------------
    @Test
    @DisplayName("24. Omitted duration is correctly calculated from timestamps")
    void testOmittedDurationAutoCalculated() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        session.setActualDurationMinutes(null); // omitted

        EquipmentUsageSession result = utilizationService.createUsageSession(session, 500L, 100L, null);
        assertNotNull(result);
        assertEquals(60, result.getActualDurationMinutes());
    }

    // -------------------------------------------------------------
    // Test 25: Zero duration remains rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("25. Zero duration remains rejected")
    void testZeroDurationRejected() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));

        // Explicit 0 actual duration
        EquipmentUsageSession sessionZero = new EquipmentUsageSession();
        sessionZero.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        sessionZero.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        sessionZero.setActualDurationMinutes(0);

        InvalidOperationException exZero = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionZero, 500L, 100L, null));
        assertTrue(exZero.getMessage().contains("Actual duration must be positive"));

        // Timestamp difference less than 1 minute (0 minutes calculated)
        EquipmentUsageSession sessionZeroCalc = new EquipmentUsageSession();
        sessionZeroCalc.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        sessionZeroCalc.setCheckedOutAt(Instant.parse("2026-09-14T10:00:30Z")); // 30s difference = 0 minutes

        InvalidOperationException exCalc = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionZeroCalc, 500L, 100L, null));
        assertTrue(exCalc.getMessage().contains("Calculated duration must be positive"));
    }

    // -------------------------------------------------------------
    // Test 26: Negative duration remains rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("26. Negative duration remains rejected")
    void testNegativeDurationRejected() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));

        // Negative actual duration
        EquipmentUsageSession sessionNegActual = new EquipmentUsageSession();
        sessionNegActual.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        sessionNegActual.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        sessionNegActual.setActualDurationMinutes(-15);

        InvalidOperationException exNegActual = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionNegActual, 500L, 100L, null));
        assertTrue(exNegActual.getMessage().contains("Actual duration must be positive"));

        // Negative scheduled duration
        EquipmentUsageSession sessionNegSched = new EquipmentUsageSession();
        sessionNegSched.setCheckedInAt(Instant.parse("2026-09-14T10:00:00Z"));
        sessionNegSched.setCheckedOutAt(Instant.parse("2026-09-14T11:00:00Z"));
        sessionNegSched.setScheduledDurationMinutes(-10);

        InvalidOperationException exNegSched = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(sessionNegSched, 500L, 100L, null));
        assertTrue(exNegSched.getMessage().contains("Scheduled duration must be positive"));
    }

    // -------------------------------------------------------------
    // Test 27: Inconsistent scheduled duration against booking is rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("27. Inconsistent scheduled duration against booking is rejected")
    void testInconsistentScheduledDurationAgainstBookingRejected() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(1000L)).thenReturn(Optional.of(bookingA)); // 09:00 to 11:00 = 120 min

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(Instant.parse("2026-09-14T09:05:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T10:55:00Z"));
        session.setScheduledDurationMinutes(180); // 180 != 120 min booking duration

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> utilizationService.createUsageSession(session, 500L, 100L, 1000L));
        assertTrue(ex.getMessage().contains("Explicit scheduled duration 180 minutes does not match booking duration 120 minutes"));
    }

    // -------------------------------------------------------------
    // Test 28: Valid scheduled duration is accepted
    // -------------------------------------------------------------
    @Test
    @DisplayName("28. Valid scheduled duration is accepted")
    void testValidScheduledDurationAccepted() {
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(bookingRepository.findById(1000L)).thenReturn(Optional.of(bookingA)); // 09:00 to 11:00 = 120 min
        when(sessionRepository.save(any(EquipmentUsageSession.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentUsageSession session = new EquipmentUsageSession();
        session.setCheckedInAt(Instant.parse("2026-09-14T09:05:00Z"));
        session.setCheckedOutAt(Instant.parse("2026-09-14T10:55:00Z"));
        session.setScheduledDurationMinutes(120); // exactly matches 120 min booking

        EquipmentUsageSession result = utilizationService.createUsageSession(session, 500L, 100L, 1000L);
        assertNotNull(result);
        assertEquals(120, result.getScheduledDurationMinutes());
        assertEquals(110, result.getActualDurationMinutes());
    }
}
