package com.labresource.platform.booking.web;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.service.BookingService;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.security.principal.SecurityUtils;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Long userId = request.getUserId();
        if (userId == null || SecurityUtils.isResearcher()) {
            userId = SecurityUtils.getCurrentUserId().orElse(userId);
        }
        if (userId == null) {
            throw new InvalidOperationException("User ID is required to create a booking");
        }

        Long departmentId = request.getDepartmentId();
        Long institutionId = request.getInstitutionId();

        if (departmentId == null || institutionId == null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                if (institutionId == null && user.getInstitution() != null) {
                    institutionId = user.getInstitution().getId();
                }
                if (departmentId == null && user.getDepartment() != null) {
                    departmentId = user.getDepartment().getId();
                }
            }
        }

        Booking booking = new Booking();
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setProjectCode(request.getProjectCode());
        if (request.getIsExternalBooking() != null) {
            booking.setExternalBooking(request.getIsExternalBooking());
        }
        if (request.getBaseHourlyRate() != null) {
            booking.setBaseHourlyRate(request.getBaseHourlyRate());
        }

        Booking created = bookingService.createBooking(
                booking,
                userId,
                request.getEquipmentId(),
                departmentId,
                institutionId,
                request.getSharedAllocationId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            if (callerId == null || booking.getUser() == null || !callerId.equals(booking.getUser().getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own bookings");
            }
        }
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String reference) {
        Booking booking = bookingService.getBookingByReference(reference);
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            if (callerId == null || booking.getUser() == null || !callerId.equals(booking.getUser().getId())) {
                throw new AccessDeniedException("Access denied: You can only view your own bookings");
            }
        }
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> listBookings(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) BookingStatus status) {

        List<Booking> list;
        if (SecurityUtils.isResearcher()) {
            Long callerId = SecurityUtils.getCurrentUserId().orElse(null);
            list = bookingService.listBookingsByUser(callerId);
        } else if (SecurityUtils.isDepartmentHead() && !SecurityUtils.hasAnyRole("ROLE_SYSTEM_ADMINISTRATOR", "ROLE_INSTITUTION_ADMINISTRATOR", "ROLE_LAB_MANAGER")) {
            Long callerDeptId = SecurityUtils.getCurrentDepartmentId().orElse(departmentId);
            list = callerDeptId != null
                    ? bookingService.listBookingsByDepartment(callerDeptId)
                    : bookingService.listBookings();
        } else if (equipmentId != null) {
            list = bookingService.listBookingsByEquipment(equipmentId);
        } else if (userId != null) {
            list = bookingService.listBookingsByUser(userId);
        } else if (institutionId != null) {
            list = bookingService.listBookingsByInstitution(institutionId);
        } else if (departmentId != null) {
            list = bookingService.listBookingsByDepartment(departmentId);
        } else if (status != null) {
            list = bookingService.listBookingsByStatus(status);
        } else {
            list = bookingService.listBookings();
        }

        List<BookingResponse> response = list.stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmBookingRequest request) {

        Booking existing = bookingService.getBookingById(id);
        Long approverId = (request != null && request.getApprovedByUserId() != null)
                ? request.getApprovedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        // Disallow self-approval unless system administrator
        if (existing != null && approverId != null && existing.getUser() != null && approverId.equals(existing.getUser().getId()) && !SecurityUtils.isSystemAdmin()) {
            throw new AccessDeniedException("Self-approval forbidden: You cannot approve your own reservation request");
        }

        Booking confirmed = bookingService.confirmBooking(id, approverId);
        return ResponseEntity.ok(BookingResponse.from(confirmed));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {

        Booking booking = bookingService.getBookingById(id);
        Long cancellerId = (request != null && request.getCancelledByUserId() != null)
                ? request.getCancelledByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        if (SecurityUtils.isResearcher() && booking != null) {
            if (cancellerId == null || booking.getUser() == null || !cancellerId.equals(booking.getUser().getId())) {
                throw new AccessDeniedException("Access denied: You can only cancel your own bookings");
            }
        }

        String reason = (request != null) ? request.getCancellationReason() : null;
        Booking cancelled = bookingService.cancelBooking(id, cancellerId, reason);
        return ResponseEntity.ok(BookingResponse.from(cancelled));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_LAB_TECHNICIAN', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<BookingResponse> startBooking(@PathVariable Long id) {
        Booking started = bookingService.startBooking(id);
        return ResponseEntity.ok(BookingResponse.from(started));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_LAB_TECHNICIAN', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long id) {
        Booking completed = bookingService.completeBooking(id);
        return ResponseEntity.ok(BookingResponse.from(completed));
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ROLE_LAB_MANAGER', 'ROLE_LAB_TECHNICIAN', 'ROLE_DEPARTMENT_HEAD', 'ROLE_INSTITUTION_ADMINISTRATOR', 'ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable Long id) {
        Booking marked = bookingService.markNoShow(id);
        return ResponseEntity.ok(BookingResponse.from(marked));
    }
}
