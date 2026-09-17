package com.labresource.platform.booking.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingStatus;

import java.util.List;

public interface BookingService {

    /**
     * Creates a new booking with explicit entity identifiers and service-layer domain validation.
     */
    Booking createBooking(Booking booking, Long userId, Long equipmentId, Long departmentId, Long institutionId, Long sharedAllocationId);

    /**
     * Convenience overload that extracts entity identifiers from the provided Booking instance.
     */
    default Booking createBooking(Booking booking) {
        if (booking == null) {
            return createBooking(null, null, null, null, null, null);
        }
        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        Long equipmentId = booking.getEquipment() != null ? booking.getEquipment().getId() : null;
        Long departmentId = booking.getDepartment() != null ? booking.getDepartment().getId() : null;
        Long institutionId = booking.getInstitution() != null ? booking.getInstitution().getId() : null;
        Long sharedAllocationId = booking.getSharedEquipmentAllocation() != null ? booking.getSharedEquipmentAllocation().getId() : null;
        return createBooking(booking, userId, equipmentId, departmentId, institutionId, sharedAllocationId);
    }

    /**
     * Retrieves a booking by its primary key ID.
     */
    Booking getBookingById(Long id);

    /**
     * Retrieves a booking by its unique booking reference.
     */
    Booking getBookingByReference(String bookingReference);

    /**
     * Lists all bookings in the system.
     */
    List<Booking> listBookings();

    /**
     * Lists all bookings associated with the specified equipment.
     */
    List<Booking> listBookingsByEquipment(Long equipmentId);

    /**
     * Lists all bookings created by the specified user.
     */
    List<Booking> listBookingsByUser(Long userId);

    /**
     * Lists all bookings belonging to the specified institution.
     */
    List<Booking> listBookingsByInstitution(Long institutionId);

    /**
     * Lists all bookings belonging to the specified department.
     */
    List<Booking> listBookingsByDepartment(Long departmentId);

    /**
     * Lists all bookings currently in the specified status.
     */
    List<Booking> listBookingsByStatus(BookingStatus status);

    /**
     * Confirms a PENDING_APPROVAL booking with optional approver attribution.
     */
    Booking confirmBooking(Long id, Long approvedByUserId);

    /**
     * Confirms a PENDING_APPROVAL booking without explicit approver attribution.
     */
    default Booking confirmBooking(Long id) {
        return confirmBooking(id, null);
    }

    /**
     * Cancels an active booking with optional canceller attribution and cancellation reason.
     */
    Booking cancelBooking(Long id, Long cancelledByUserId, String cancellationReason);

    /**
     * Cancels an active booking without explicit canceller attribution.
     */
    default Booking cancelBooking(Long id, String cancellationReason) {
        return cancelBooking(id, null, cancellationReason);
    }

    /**
     * Transitions a CONFIRMED booking to IN_USE.
     */
    Booking startBooking(Long id);

    /**
     * Transitions an IN_USE booking to COMPLETED.
     */
    Booking completeBooking(Long id);

    /**
     * Marks a CONFIRMED booking as NO_SHOW.
     */
    Booking markNoShow(Long id);
}
