package com.labresource.platform.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.cost.BillingInvoice;
import com.labresource.platform.cost.InvoiceLineItem;
import com.labresource.platform.cost.InvoiceStatus;
import com.labresource.platform.cost.repository.BillingInvoiceRepository;
import com.labresource.platform.cost.repository.InvoiceLineItemRepository;
import com.labresource.platform.cost.service.CostServiceImpl;
import com.labresource.platform.cost.web.DepartmentCostSummaryResponse;
import com.labresource.platform.cost.web.UsageCostResponse;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.sharing.repository.ResourceSharingAgreementRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostServiceTest {

    @Mock
    private BillingInvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineItemRepository lineItemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ResourceSharingAgreementRepository sharingAgreementRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private CostServiceImpl costService;

    private Institution inst1;
    private Department dept1;
    private Equipment equip1;
    private Booking booking1;
    private BillingInvoice draftInvoice;

    @BeforeEach
    void setUp() {
        inst1 = new Institution();
        inst1.setId(1L);
        inst1.setName("Apex Institute of Technology");

        dept1 = new Department();
        dept1.setId(10L);
        dept1.setName("Bioengineering");
        dept1.setInstitution(inst1);

        equip1 = new Equipment();
        equip1.setId(100L);
        equip1.setName("Electron Microscope");
        equip1.setHourlyRateInternal(BigDecimal.valueOf(50.00));
        equip1.setHourlyRateExternal(BigDecimal.valueOf(100.00));
        equip1.setInstitution(inst1);
        equip1.setDepartment(dept1);

        booking1 = new Booking();
        booking1.setId(500L);
        booking1.setBookingReference("BK-2026-001");
        booking1.setInstitution(inst1);
        booking1.setDepartment(dept1);
        booking1.setEquipment(equip1);
        booking1.setBillingStatus(BookingBillingStatus.UNBILLED);
        booking1.setStartTime(Instant.parse("2026-05-01T10:00:00Z"));
        booking1.setEndTime(Instant.parse("2026-05-01T12:00:00Z"));
        booking1.setBaseHourlyRate(BigDecimal.valueOf(50.00));

        draftInvoice = new BillingInvoice();
        draftInvoice.setId(1L);
        draftInvoice.setInvoiceNumber("INV-TEST-001");
        draftInvoice.setIssuingInstitution(inst1);
        draftInvoice.setBilledInstitution(inst1);
        draftInvoice.setBilledDepartment(dept1);
        draftInvoice.setBillingPeriodStart(LocalDate.parse("2026-05-01"));
        draftInvoice.setBillingPeriodEnd(LocalDate.parse("2026-05-31"));
        draftInvoice.setStatus(InvoiceStatus.DRAFT);
        draftInvoice.setSubtotalAmount(BigDecimal.ZERO);
        draftInvoice.setTotalAmount(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Create invoice with unbilled bookings succeeds and updates booking to INVOICED")
    void testCreateInvoiceSuccessWithUnbilledBookings() {
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(inst1));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept1));
        when(invoiceRepository.save(any(BillingInvoice.class))).thenAnswer(invocation -> {
            BillingInvoice bi = invocation.getArgument(0);
            if (bi.getId() == null) bi.setId(1L);
            return bi;
        });
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking1));
        when(lineItemRepository.existsByBookingId(500L)).thenReturn(false);

        BillingInvoice created = costService.createInvoice(
                draftInvoice,
                List.of(500L),
                1L,
                1L,
                10L,
                null,
                1L
        );

        assertNotNull(created);
        assertEquals(InvoiceStatus.DRAFT, created.getStatus());
        assertEquals(BookingBillingStatus.INVOICED, booking1.getBillingStatus());
        verify(lineItemRepository, times(1)).save(any(InvoiceLineItem.class));
        verify(bookingRepository, times(1)).save(booking1);
    }

    @Test
    @DisplayName("Create invoice with already invoiced booking throws InvalidOperationException")
    void testCreateInvoiceWithAlreadyInvoicedBookingThrowsException() {
        booking1.setBillingStatus(BookingBillingStatus.INVOICED);

        when(institutionRepository.findById(1L)).thenReturn(Optional.of(inst1));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept1));
        when(invoiceRepository.save(any(BillingInvoice.class))).thenAnswer(invocation -> {
            BillingInvoice bi = invocation.getArgument(0);
            bi.setId(1L);
            return bi;
        });
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking1));

        assertThrows(InvalidOperationException.class, () ->
                costService.createInvoice(draftInvoice, List.of(500L), 1L, 1L, 10L, null, 1L));
    }

    @Test
    @DisplayName("Delete draft invoice resets associated bookings from INVOICED back to UNBILLED")
    void testDeleteDraftInvoiceResetsBookingToUnbilled() {
        booking1.setBillingStatus(BookingBillingStatus.INVOICED);

        InvoiceLineItem line = new InvoiceLineItem();
        line.setId(99L);
        line.setInvoice(draftInvoice);
        line.setBooking(booking1);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
        when(lineItemRepository.findByInvoiceId(1L)).thenReturn(List.of(line));

        costService.deleteInvoice(1L, 1L);

        assertEquals(BookingBillingStatus.UNBILLED, booking1.getBillingStatus());
        verify(bookingRepository, times(1)).save(booking1);
        verify(lineItemRepository, times(1)).delete(line);
        verify(invoiceRepository, times(1)).delete(draftInvoice);
    }

    @Test
    @DisplayName("Attempt to delete non-draft invoice throws InvalidOperationException")
    void testDeleteIssuedInvoiceThrowsException() {
        draftInvoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));

        assertThrows(InvalidOperationException.class, () ->
                costService.deleteInvoice(1L, 1L));

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Attempt to add line item to non-draft invoice throws InvalidOperationException")
    void testAddLineItemToIssuedInvoiceThrowsException() {
        draftInvoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));

        assertThrows(InvalidOperationException.class, () ->
                costService.addLineItem(1L, 500L, null, null, 1L));

        verify(lineItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Status transition DRAFT to ISSUED sets issuedAt timestamp")
    void testUpdateInvoiceStatusDraftToIssuedSetsIssuedAt() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
        when(invoiceRepository.save(any(BillingInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BillingInvoice updated = costService.updateInvoiceStatus(1L, InvoiceStatus.ISSUED, null, 1L);

        assertEquals(InvoiceStatus.ISSUED, updated.getStatus());
        assertNotNull(updated.getIssuedAt());
    }

    @Test
    @DisplayName("Status transition ISSUED to SETTLED updates bookings to SETTLED")
    void testUpdateInvoiceStatusIssuedToSettledSetsBookingsToSettled() {
        draftInvoice.setStatus(InvoiceStatus.ISSUED);
        draftInvoice.setIssuedAt(Instant.now());

        booking1.setBillingStatus(BookingBillingStatus.INVOICED);
        InvoiceLineItem line = new InvoiceLineItem();
        line.setInvoice(draftInvoice);
        line.setBooking(booking1);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
        when(lineItemRepository.findByInvoiceId(1L)).thenReturn(List.of(line));
        when(invoiceRepository.save(any(BillingInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BillingInvoice updated = costService.updateInvoiceStatus(1L, InvoiceStatus.SETTLED, "WIRE-REC-9988", 1L);

        assertEquals(InvoiceStatus.SETTLED, updated.getStatus());
        assertNotNull(updated.getPaidAt());
        assertEquals("WIRE-REC-9988", updated.getPaymentReference());
        assertEquals(BookingBillingStatus.SETTLED, booking1.getBillingStatus());
        verify(bookingRepository, times(1)).save(booking1);
    }

    @Test
    @DisplayName("Terminal status transition from SETTLED throws InvalidOperationException")
    void testTerminalStatusTransitionThrowsException() {
        draftInvoice.setStatus(InvoiceStatus.SETTLED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));

        assertThrows(InvalidOperationException.class, () ->
                costService.updateInvoiceStatus(1L, InvoiceStatus.ISSUED, null, 1L));
    }

    @Test
    @DisplayName("Department cost summary aggregates unbilled, invoiced, and settled amounts")
    void testCalculateDepartmentCostSummary() {
        Booking invoicedBooking = new Booking();
        invoicedBooking.setId(501L);
        invoicedBooking.setDepartment(dept1);
        invoicedBooking.setBillingStatus(BookingBillingStatus.INVOICED);
        invoicedBooking.setStartTime(Instant.parse("2026-05-02T10:00:00Z"));
        invoicedBooking.setEndTime(Instant.parse("2026-05-02T11:00:00Z"));
        invoicedBooking.setBaseHourlyRate(BigDecimal.valueOf(60.00));

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept1));
        when(bookingRepository.findByDepartmentId(10L)).thenReturn(List.of(booking1, invoicedBooking));

        DepartmentCostSummaryResponse summary = costService.getDepartmentCostSummary(10L, 1L);

        assertNotNull(summary);
        assertEquals(10L, summary.getDepartmentId());
        assertEquals(2, summary.getTotalBookingsCount());
        assertEquals(1, summary.getUnbilledBookingsCount());
        // booking1: 2 hrs * $50 = $100 unbilled
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), summary.getUnbilledCost());
        // invoicedBooking: 1 hr * $60 = $60 invoiced
        assertEquals(BigDecimal.valueOf(60.00).setScale(2), summary.getInvoicedCost());
        assertEquals(BigDecimal.valueOf(160.00).setScale(2), summary.getTotalCost());
    }

    @Test
    @DisplayName("Usage cost calculation derives duration hours and total cost")
    void testUsageCostCalculation() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking1));
        when(lineItemRepository.findByBookingId(500L)).thenReturn(Optional.empty());

        UsageCostResponse res = costService.getUsageCostByBookingId(500L);

        assertNotNull(res);
        assertEquals("BK-2026-001", res.getBookingReference());
        assertEquals(BigDecimal.valueOf(2.00).setScale(2), res.getBillableHours());
        assertEquals(BigDecimal.valueOf(50.00), res.getHourlyRate());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), res.getTotalCost());
        assertEquals(BookingBillingStatus.UNBILLED, res.getBillingStatus());
    }
}
