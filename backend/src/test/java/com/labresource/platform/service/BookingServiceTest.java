package com.labresource.platform.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.booking.service.BookingServiceImpl;
import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.equipment.repository.UserEquipmentQualificationRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.sharing.ResourceSharingAgreement;
import com.labresource.platform.sharing.SharedEquipmentAllocation;
import com.labresource.platform.sharing.SharingAgreementStatus;
import com.labresource.platform.sharing.repository.SharedEquipmentAllocationRepository;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserEquipmentQualificationRepository qualificationRepository;

    @Mock
    private SharedEquipmentAllocationRepository sharedAllocationRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Institution institutionA;
    private Institution institutionB;
    private Department departmentA;
    private Department departmentB;
    private User userA;
    private Equipment equipmentA;
    private Equipment equipmentB;
    private Instant validStartTime;
    private Instant validEndTime;

    @BeforeEach
    void setUp() {
        institutionA = new Institution();
        institutionA.setId(1L);
        institutionA.setName("Institution A");
        institutionA.setCode("INST-A");
        institutionA.setActive(true);

        institutionB = new Institution();
        institutionB.setId(2L);
        institutionB.setName("Institution B");
        institutionB.setCode("INST-B");
        institutionB.setActive(true);

        departmentA = new Department();
        departmentA.setId(10L);
        departmentA.setName("Physics Dept");
        departmentA.setInstitution(institutionA);
        departmentA.setActive(true);

        departmentB = new Department();
        departmentB.setId(20L);
        departmentB.setName("Chemistry Dept");
        departmentB.setInstitution(institutionB);
        departmentB.setActive(true);

        userA = new User();
        userA.setId(100L);
        userA.setEmail("researcher@inst-a.edu");
        userA.setInstitution(institutionA);
        userA.setDepartment(departmentA);
        userA.setStatus(UserStatus.ACTIVE);

        equipmentA = new Equipment();
        equipmentA.setId(500L);
        equipmentA.setName("Electron Microscope");
        equipmentA.setAssetTag("EM-001");
        equipmentA.setInstitution(institutionA);
        equipmentA.setDepartment(departmentA);
        equipmentA.setStatus(EquipmentStatus.AVAILABLE);
        equipmentA.setHourlyRateInternal(new BigDecimal("50.00"));
        equipmentA.setHourlyRateExternal(new BigDecimal("90.00"));
        equipmentA.setRequiresTrainingCertification(false);

        equipmentB = new Equipment();
        equipmentB.setId(600L);
        equipmentB.setName("Spectrometer");
        equipmentB.setAssetTag("SPEC-001");
        equipmentB.setInstitution(institutionB);
        equipmentB.setDepartment(departmentB);
        equipmentB.setStatus(EquipmentStatus.AVAILABLE);
        equipmentB.setHourlyRateInternal(new BigDecimal("40.00"));
        equipmentB.setHourlyRateExternal(new BigDecimal("75.00"));
        equipmentB.setRequiresTrainingCertification(false);

        // Monday: 2026-09-14 09:00:00 to 11:00:00 UTC (operating window is Mon-Sat 08:00-20:00)
        validStartTime = Instant.parse("2026-09-14T09:00:00Z");
        validEndTime = Instant.parse("2026-09-14T11:00:00Z");
    }

    // -------------------------------------------------------------
    // Test 1: Successful Booking Creation
    // -------------------------------------------------------------
    @Test
    @DisplayName("1. Successful internal booking creation")
    void testSuccessfulBookingCreation() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);
        booking.setPurpose("Nano-material crystal imaging");

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1000L);
            return b;
        });

        Booking result = bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null);

        assertNotNull(result);
        assertEquals(1000L, result.getId());
        assertEquals(BookingStatus.PENDING_APPROVAL, result.getStatus());
        assertEquals(BookingBillingStatus.UNBILLED, result.getBillingStatus());
        assertFalse(result.isExternalBooking());
        assertEquals(new BigDecimal("50.00"), result.getBaseHourlyRate());
        assertEquals(new BigDecimal("100.00"), result.getEstimatedCost());
        assertNotNull(result.getBookingReference());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // -------------------------------------------------------------
    // Test 2: Invalid Start / End Time
    // -------------------------------------------------------------
    @Test
    @DisplayName("2. Invalid start/end time: start after end, equal times, null timestamps")
    void testInvalidStartEndTime() {
        Booking booking = new Booking();
        booking.setStartTime(validEndTime);
        booking.setEndTime(validStartTime); // start after end

        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        Booking bookingSameTime = new Booking();
        bookingSameTime.setStartTime(validStartTime);
        bookingSameTime.setEndTime(validStartTime); // start == end
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(bookingSameTime, 100L, 500L, 10L, 1L, null));

        Booking bookingNullTime = new Booking();
        bookingNullTime.setStartTime(null);
        bookingNullTime.setEndTime(validEndTime);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(bookingNullTime, 100L, 500L, 10L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 3: User Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("3. User not found throws ResourceNotFoundException")
    void testUserNotFound() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(booking, 999L, 500L, 10L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 4: Institution Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("4. Institution not found throws ResourceNotFoundException")
    void testInstitutionNotFound() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 999L, null));
    }

    // -------------------------------------------------------------
    // Test 5: Department Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("5. Department not found throws ResourceNotFoundException")
    void testDepartmentNotFound() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 999L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 6: Equipment Not Found
    // -------------------------------------------------------------
    @Test
    @DisplayName("6. Equipment not found throws ResourceNotFoundException")
    void testEquipmentNotFound() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(booking, 100L, 999L, 10L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 7: User / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("7. User / institution mismatch throws InvalidOperationException")
    void testUserInstitutionMismatch() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        // User belongs to institutionA (id=1), booking is for institutionB (id=2)
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(2L)).thenReturn(Optional.of(institutionB));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB));
        when(equipmentRepository.findById(600L)).thenReturn(Optional.of(equipmentB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 600L, 20L, 2L, null));
        assertTrue(ex.getMessage().contains("belongs to institution 1, not booking institution 2"));
    }

    // -------------------------------------------------------------
    // Test 8: Department / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("8. Department / institution mismatch throws InvalidOperationException")
    void testDepartmentInstitutionMismatch() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        // Department belongs to institutionB (id=2), booking is for institutionA (id=1)
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(20L)).thenReturn(Optional.of(departmentB));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 20L, 1L, null));
        assertTrue(ex.getMessage().contains("belongs to institution 2, not booking institution 1"));
    }

    // -------------------------------------------------------------
    // Test 9: Internal Equipment / Institution Mismatch
    // -------------------------------------------------------------
    @Test
    @DisplayName("9. Internal equipment / institution mismatch throws InvalidOperationException")
    void testInternalEquipmentInstitutionMismatch() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        // Equipment belongs to institutionB (id=2), booking is for institutionA (id=1) without shared allocation
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(600L)).thenReturn(Optional.of(equipmentB));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 600L, 10L, 1L, null));
        assertTrue(ex.getMessage().contains("External bookings require a shared equipment allocation"));
    }

    // -------------------------------------------------------------
    // Test 10: Retired Equipment Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("10. RETIRED equipment rejected")
    void testRetiredEquipmentRejected() {
        equipmentA.setStatus(EquipmentStatus.RETIRED);

        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
        assertTrue(ex.getMessage().contains("Cannot book equipment 500 with status RETIRED"));
    }

    // -------------------------------------------------------------
    // Test 11: Maintenance / Out-of-Service Equipment Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("11. OUT_OF_SERVICE and UNDER_MAINTENANCE equipment rejected")
    void testMaintenanceAndOutOfServiceEquipmentRejected() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));

        // OUT_OF_SERVICE test
        equipmentA.setStatus(EquipmentStatus.OUT_OF_SERVICE);
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        // UNDER_MAINTENANCE test
        equipmentA.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 12: Overlapping Booking Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("12. Overlapping booking rejected")
    void testOverlappingBookingRejected() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        Booking existing = new Booking();
        existing.setId(99L);
        existing.setEquipment(equipmentA);
        existing.setStatus(BookingStatus.CONFIRMED);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(List.of(existing));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
        assertTrue(ex.getMessage().contains("already booked for the requested time range"));
    }

    // -------------------------------------------------------------
    // Test 13: Non-Overlapping Booking Accepted
    // -------------------------------------------------------------
    @Test
    @DisplayName("13. Non-overlapping booking accepted")
    void testNonOverlappingBookingAccepted() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking saved = bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null);
        assertNotNull(saved);
        verify(bookingRepository, times(1)).save(booking);
    }

    // -------------------------------------------------------------
    // Test 14: Pending Booking Confirmation
    // -------------------------------------------------------------
    @Test
    @DisplayName("14. Pending booking confirmation (PENDING_APPROVAL -> CONFIRMED)")
    void testPendingBookingConfirmation() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setEquipment(equipmentA);
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);
        booking.setStatus(BookingStatus.PENDING_APPROVAL);

        User approver = new User();
        approver.setId(200L);
        approver.setEmail("labmanager@inst-a.edu");

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(200L)).thenReturn(Optional.of(approver));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking confirmed = bookingService.confirmBooking(10L, 200L);

        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertNotNull(confirmed.getApprovedAt());
        assertEquals(approver, confirmed.getApprovedByUser());
    }

    // -------------------------------------------------------------
    // Test 15: Invalid Confirmation Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("15. Invalid confirmation rejected for non-PENDING_APPROVAL booking")
    void testInvalidConfirmationRejected() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> bookingService.confirmBooking(10L, 200L));
        assertTrue(ex.getMessage().contains("Only PENDING_APPROVAL bookings can be confirmed"));
    }

    // -------------------------------------------------------------
    // Test 16: Cancellation of Pending and Confirmed Bookings
    // -------------------------------------------------------------
    @Test
    @DisplayName("16. Cancellation of PENDING_APPROVAL and CONFIRMED bookings")
    void testCancellation() {
        Booking pendingBooking = new Booking();
        pendingBooking.setId(10L);
        pendingBooking.setStatus(BookingStatus.PENDING_APPROVAL);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking cancelledPending = bookingService.cancelBooking(10L, "Sample prep failed");
        assertEquals(BookingStatus.CANCELLED, cancelledPending.getStatus());
        assertEquals("Sample prep failed", cancelledPending.getCancellationReason());
        assertNotNull(cancelledPending.getCancelledAt());

        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(11L);
        confirmedBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(11L)).thenReturn(Optional.of(confirmedBooking));
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        Booking cancelledConfirmed = bookingService.cancelBooking(11L, 100L, "Experiment rescheduled");
        assertEquals(BookingStatus.CANCELLED, cancelledConfirmed.getStatus());
        assertEquals(userA, cancelledConfirmed.getCancelledByUser());
    }

    // -------------------------------------------------------------
    // Test 17: Cancellation of Completed Booking Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("17. Cancellation of COMPLETED or already CANCELLED booking rejected")
    void testCancellationOfCompletedBookingRejected() {
        Booking completedBooking = new Booking();
        completedBooking.setId(20L);
        completedBooking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(20L)).thenReturn(Optional.of(completedBooking));

        assertThrows(InvalidOperationException.class,
                () -> bookingService.cancelBooking(20L, "Try to cancel"));

        Booking cancelledBooking = new Booking();
        cancelledBooking.setId(21L);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(21L)).thenReturn(Optional.of(cancelledBooking));

        assertThrows(InvalidOperationException.class,
                () -> bookingService.cancelBooking(21L, "Try to cancel again"));
    }

    // -------------------------------------------------------------
    // Test 18: Start Confirmed Booking
    // -------------------------------------------------------------
    @Test
    @DisplayName("18. Start confirmed booking (CONFIRMED -> IN_USE) and reject invalid starts")
    void testStartConfirmedBooking() {
        Booking booking = new Booking();
        booking.setId(30L);
        booking.setEquipment(equipmentA);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking inUse = bookingService.startBooking(30L);
        assertEquals(BookingStatus.IN_USE, inUse.getStatus());

        // Attempting to start a PENDING_APPROVAL booking must fail
        Booking pending = new Booking();
        pending.setId(31L);
        pending.setStatus(BookingStatus.PENDING_APPROVAL);
        when(bookingRepository.findById(31L)).thenReturn(Optional.of(pending));

        assertThrows(InvalidOperationException.class, () -> bookingService.startBooking(31L));
    }

    // -------------------------------------------------------------
    // Test 19: Complete In-Use Booking
    // -------------------------------------------------------------
    @Test
    @DisplayName("19. Complete in-use booking (IN_USE -> COMPLETED) and reject invalid completion")
    void testCompleteInUseBooking() {
        Booking booking = new Booking();
        booking.setId(40L);
        booking.setStatus(BookingStatus.IN_USE);

        when(bookingRepository.findById(40L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking completed = bookingService.completeBooking(40L);
        assertEquals(BookingStatus.COMPLETED, completed.getStatus());

        // Attempting to complete a CONFIRMED (not yet in use) booking must fail
        Booking confirmed = new Booking();
        confirmed.setId(41L);
        confirmed.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(41L)).thenReturn(Optional.of(confirmed));

        assertThrows(InvalidOperationException.class, () -> bookingService.completeBooking(41L));
    }

    // -------------------------------------------------------------
    // Test 20: No-Show Lifecycle Validation
    // -------------------------------------------------------------
    @Test
    @DisplayName("20. Mark no-show on CONFIRMED booking and reject invalid no-show transitions")
    void testNoShowLifecycleValidation() {
        Booking booking = new Booking();
        booking.setId(50L);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking noShow = bookingService.markNoShow(50L);
        assertEquals(BookingStatus.NO_SHOW, noShow.getStatus());

        // Attempting to mark NO_SHOW on IN_USE booking must fail
        Booking inUse = new Booking();
        inUse.setId(51L);
        inUse.setStatus(BookingStatus.IN_USE);
        when(bookingRepository.findById(51L)).thenReturn(Optional.of(inUse));

        assertThrows(InvalidOperationException.class, () -> bookingService.markNoShow(51L));
    }

    // -------------------------------------------------------------
    // Test 21: External / Shared Booking Validation
    // -------------------------------------------------------------
    @Test
    @DisplayName("21. External / shared booking validation (success and failure scenarios)")
    void testExternalSharedBookingValidation() {
        // Equipment B belongs to institution B
        // User A belongs to institution A
        // Agreement authorizes institution A to use equipment B from institution B
        ResourceSharingAgreement agreement = new ResourceSharingAgreement();
        agreement.setId(77L);
        agreement.setAgreementCode("AGR-MIT-STAN");
        agreement.setRequestingInstitution(institutionA);
        agreement.setOwnerInstitution(institutionB);
        agreement.setStatus(SharingAgreementStatus.ACTIVE);
        agreement.setStartDate(LocalDate.of(2026, 9, 1));
        agreement.setEndDate(LocalDate.of(2026, 9, 30));

        SharedEquipmentAllocation allocation = new SharedEquipmentAllocation();
        allocation.setId(700L);
        allocation.setEquipment(equipmentB);
        allocation.setSharingAgreement(agreement);
        allocation.setActive(true);
        allocation.setCustomHourlyRate(new BigDecimal("70.00"));

        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(600L)).thenReturn(Optional.of(equipmentB));
        when(sharedAllocationRepository.findById(700L)).thenReturn(Optional.of(allocation));
        when(bookingRepository.findOverlappingBookings(eq(600L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking externalBooking = bookingService.createBooking(booking, 100L, 600L, 10L, 1L, 700L);

        assertTrue(externalBooking.isExternalBooking());
        assertEquals(new BigDecimal("70.00"), externalBooking.getBaseHourlyRate());
        assertEquals(new BigDecimal("140.00"), externalBooking.getEstimatedCost());
        assertEquals(allocation, externalBooking.getSharedEquipmentAllocation());

        // Failure case: Inactive allocation
        allocation.setActive(false);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(new Booking(), 100L, 600L, 10L, 1L, 700L));

        // Failure case: Suspended agreement
        allocation.setActive(true);
        agreement.setStatus(SharingAgreementStatus.SUSPENDED);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(new Booking(), 100L, 600L, 10L, 1L, 700L));
    }

    // -------------------------------------------------------------
    // Test 22: Equipment Qualification Validation
    // -------------------------------------------------------------
    @Test
    @DisplayName("22. Qualification validation when equipment requires training certification")
    void testQualificationValidation() {
        equipmentA.setRequiresTrainingCertification(true);

        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));

        // Case A: No qualification record found
        when(qualificationRepository.findByUserIdAndEquipmentId(100L, 500L)).thenReturn(Optional.empty());
        InvalidOperationException exNoQual = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
        assertTrue(exNoQual.getMessage().contains("training certification required"));

        // Case B: Revoked qualification record
        UserEquipmentQualification revokedQual = new UserEquipmentQualification();
        revokedQual.setStatus(QualificationStatus.REVOKED);
        when(qualificationRepository.findByUserIdAndEquipmentId(100L, 500L)).thenReturn(Optional.of(revokedQual));
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        // Case C: Active and valid qualification record
        UserEquipmentQualification validQual = new UserEquipmentQualification();
        validQual.setStatus(QualificationStatus.ACTIVE);
        validQual.setExpiresAt(Instant.parse("2028-01-01T00:00:00Z"));
        when(qualificationRepository.findByUserIdAndEquipmentId(100L, 500L)).thenReturn(Optional.of(validQual));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking successful = bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null);
        assertNotNull(successful);
    }

    // -------------------------------------------------------------
    // Test 23: Operating Window Validation
    // -------------------------------------------------------------
    @Test
    @DisplayName("23. Operating-window validation (08:00–20:00, Monday–Saturday)")
    void testOperatingWindowValidation() {
        // Case A: Sunday booking rejected (2026-09-13 is Sunday)
        Booking sundayBooking = new Booking();
        sundayBooking.setStartTime(Instant.parse("2026-09-13T10:00:00Z"));
        sundayBooking.setEndTime(Instant.parse("2026-09-13T12:00:00Z"));
        InvalidOperationException exSunday = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(sundayBooking, 100L, 500L, 10L, 1L, null));
        assertTrue(exSunday.getMessage().contains("Sundays"));

        // Case B: Starts before 08:00 (Monday 07:00 to 09:00 UTC)
        Booking earlyBooking = new Booking();
        earlyBooking.setStartTime(Instant.parse("2026-09-14T07:00:00Z"));
        earlyBooking.setEndTime(Instant.parse("2026-09-14T09:00:00Z"));
        InvalidOperationException exEarly = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(earlyBooking, 100L, 500L, 10L, 1L, null));
        assertTrue(exEarly.getMessage().contains("operating window"));

        // Case C: Ends after 20:00 (Monday 19:00 to 21:00 UTC)
        Booking lateBooking = new Booking();
        lateBooking.setStartTime(Instant.parse("2026-09-14T19:00:00Z"));
        lateBooking.setEndTime(Instant.parse("2026-09-14T21:00:00Z"));
        InvalidOperationException exLate = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(lateBooking, 100L, 500L, 10L, 1L, null));
        assertTrue(exLate.getMessage().contains("operating window"));

        // Case D: Overnight booking spanning multiple days
        Booking multiDayBooking = new Booking();
        multiDayBooking.setStartTime(Instant.parse("2026-09-14T18:00:00Z"));
        multiDayBooking.setEndTime(Instant.parse("2026-09-15T09:00:00Z"));
        InvalidOperationException exMultiDay = assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(multiDayBooking, 100L, 500L, 10L, 1L, null));
        assertTrue(exMultiDay.getMessage().contains("multiple calendar days"));
    }

    // -------------------------------------------------------------
    // Test 24: Inactive / Deleted Resources Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("24. Inactive institution, inactive department, inactive/deleted user, deleted equipment rejected")
    void testInactiveAndDeletedResourcesRejected() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);

        // Inactive institution
        institutionA.setActive(false);
        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));

        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        // Inactive department
        institutionA.setActive(true);
        departmentA.setActive(false);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        // Deactivated user
        departmentA.setActive(true);
        userA.setStatus(UserStatus.DEACTIVATED);
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));

        // Soft deleted equipment
        userA.setStatus(UserStatus.ACTIVE);
        equipmentA.setDeletedAt(Instant.now());
        assertThrows(InvalidOperationException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
    }

    // -------------------------------------------------------------
    // Test 25: Query and Retrieval Methods
    // -------------------------------------------------------------
    @Test
    @DisplayName("25. Query and listing methods")
    void testQueryMethods() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingReference("BK-TEST-REF");

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertEquals(booking, bookingService.getBookingById(100L));

        when(bookingRepository.findByBookingReference("BK-TEST-REF")).thenReturn(Optional.of(booking));
        assertEquals(booking, bookingService.getBookingByReference("BK-TEST-REF"));

        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookings().size());

        when(equipmentRepository.existsById(500L)).thenReturn(true);
        when(bookingRepository.findByEquipmentId(500L)).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookingsByEquipment(500L).size());

        when(userRepository.existsById(100L)).thenReturn(true);
        when(bookingRepository.findByUserId(100L)).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookingsByUser(100L).size());

        when(institutionRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByInstitutionId(1L)).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookingsByInstitution(1L).size());

        when(departmentRepository.existsById(10L)).thenReturn(true);
        when(bookingRepository.findByDepartmentId(10L)).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookingsByDepartment(10L).size());

        when(bookingRepository.findByStatus(BookingStatus.CONFIRMED)).thenReturn(List.of(booking));
        assertEquals(1, bookingService.listBookingsByStatus(BookingStatus.CONFIRMED).size());
    }

    // -------------------------------------------------------------
    // Test 26: Duplicate Booking Reference Rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("26. Duplicate booking reference throws DuplicateResourceException")
    void testDuplicateBookingReference() {
        Booking booking = new Booking();
        booking.setStartTime(validStartTime);
        booking.setEndTime(validEndTime);
        booking.setBookingReference("BK-DUPLICATE");

        when(userRepository.findById(100L)).thenReturn(Optional.of(userA));
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institutionA));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(departmentA));
        when(equipmentRepository.findById(500L)).thenReturn(Optional.of(equipmentA));
        when(bookingRepository.findOverlappingBookings(eq(500L), eq(validStartTime), eq(validEndTime), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.existsByBookingReference("BK-DUPLICATE")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> bookingService.createBooking(booking, 100L, 500L, 10L, 1L, null));
    }
}
