package com.labresource.platform.booking.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final LocalTime OPERATING_WINDOW_START = LocalTime.of(8, 0);
    private static final LocalTime OPERATING_WINDOW_END = LocalTime.of(20, 0);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserEquipmentQualificationRepository qualificationRepository;
    private final SharedEquipmentAllocationRepository sharedAllocationRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              InstitutionRepository institutionRepository,
                              DepartmentRepository departmentRepository,
                              EquipmentRepository equipmentRepository,
                              UserEquipmentQualificationRepository qualificationRepository,
                              SharedEquipmentAllocationRepository sharedAllocationRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.qualificationRepository = qualificationRepository;
        this.sharedAllocationRepository = sharedAllocationRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(Booking booking, Long userId, Long equipmentId, Long departmentId, Long institutionId, Long sharedAllocationId) {
        if (booking == null) {
            throw new InvalidOperationException("Booking payload cannot be null");
        }
        if (userId == null) {
            throw new InvalidOperationException("User ID is required to create a booking");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to create a booking");
        }
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID is required to create a booking");
        }
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID is required to create a booking");
        }

        Instant startTime = booking.getStartTime();
        Instant endTime = booking.getEndTime();
        if (startTime == null || endTime == null) {
            throw new InvalidOperationException("Booking start time and end time are required and must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidOperationException("Booking start time must be before end time");
        }

        // Validate Operating Window: 08:00–20:00, Monday–Saturday (UTC)
        validateOperatingWindow(startTime, endTime);

        // Retrieve domain entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // Validate active / non-deleted resources
        if (!institution.isActive()) {
            throw new InvalidOperationException(String.format("Cannot create booking for inactive institution with id %d", institutionId));
        }
        if (!department.isActive()) {
            throw new InvalidOperationException(String.format("Cannot create booking for inactive department with id %d", departmentId));
        }
        if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException(String.format("Cannot create booking for deleted or inactive user with id %d", userId));
        }
        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot create booking for deleted equipment with id %d", equipmentId));
        }

        // Validate equipment operational status
        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new InvalidOperationException(String.format("Cannot book equipment %d with status RETIRED", equipmentId));
        }
        if (equipment.getStatus() == EquipmentStatus.OUT_OF_SERVICE) {
            throw new InvalidOperationException(String.format("Cannot book equipment %d with status OUT_OF_SERVICE", equipmentId));
        }
        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            throw new InvalidOperationException(String.format("Cannot book equipment %d with status UNDER_MAINTENANCE", equipmentId));
        }

        // Multi-tenant organizational consistency:
        // User.department.institution == User.institution
        // Booking.user.institution == Booking.institution
        if (!user.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException(String.format(
                    "User with id %d belongs to institution %d, not booking institution %d",
                    userId, user.getInstitution().getId(), institutionId));
        }

        // Booking.department.institution == Booking.institution
        if (!department.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException(String.format(
                    "Department with id %d belongs to institution %d, not booking institution %d",
                    departmentId, department.getInstitution().getId(), institutionId));
        }

        // Internal vs External Booking validation
        boolean isExternal = (sharedAllocationId != null) || booking.isExternalBooking();
        if (!isExternal) {
            // Internal booking: equipment must belong to the booking institution
            if (!equipment.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Equipment with id %d belongs to institution %d, not booking institution %d. External bookings require a shared equipment allocation.",
                        equipmentId, equipment.getInstitution().getId(), institutionId));
            }
            booking.setExternalBooking(false);
            if (booking.getBaseHourlyRate() == null) {
                booking.setBaseHourlyRate(equipment.getHourlyRateInternal() != null
                        ? equipment.getHourlyRateInternal()
                        : BigDecimal.ZERO);
            }
        } else {
            // External / Shared booking
            if (sharedAllocationId == null) {
                throw new InvalidOperationException("External bookings require a shared equipment allocation ID");
            }
            SharedEquipmentAllocation allocation = sharedAllocationRepository.findById(sharedAllocationId)
                    .orElseThrow(() -> new ResourceNotFoundException("SharedEquipmentAllocation", "id", sharedAllocationId));

            if (!allocation.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Shared equipment allocation with id %d is for equipment %d, not requested equipment %d",
                        sharedAllocationId, allocation.getEquipment().getId(), equipmentId));
            }
            if (!allocation.isActive()) {
                throw new InvalidOperationException(String.format(
                        "Shared equipment allocation with id %d is inactive", sharedAllocationId));
            }

            ResourceSharingAgreement agreement = allocation.getSharingAgreement();
            if (agreement == null) {
                throw new InvalidOperationException(String.format(
                        "Shared equipment allocation with id %d has no associated sharing agreement", sharedAllocationId));
            }
            if (agreement.getStatus() != SharingAgreementStatus.ACTIVE) {
                throw new InvalidOperationException(String.format(
                        "Sharing agreement %s is not ACTIVE (current status: %s)",
                        agreement.getAgreementCode(), agreement.getStatus()));
            }
            if (!agreement.getRequestingInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Sharing agreement %s does not authorize requesting institution %d (authorized: %d)",
                        agreement.getAgreementCode(), institutionId, agreement.getRequestingInstitution().getId()));
            }
            if (!agreement.getOwnerInstitution().getId().equals(equipment.getInstitution().getId())) {
                throw new InvalidOperationException(String.format(
                        "Sharing agreement owner institution %d does not match equipment institution %d",
                        agreement.getOwnerInstitution().getId(), equipment.getInstitution().getId()));
            }

            LocalDate bookingDate = startTime.atZone(ZoneOffset.UTC).toLocalDate();
            if (bookingDate.isBefore(agreement.getStartDate()) || bookingDate.isAfter(agreement.getEndDate())) {
                throw new InvalidOperationException(String.format(
                        "Booking date %s is outside sharing agreement %s active period (%s to %s)",
                        bookingDate, agreement.getAgreementCode(), agreement.getStartDate(), agreement.getEndDate()));
            }

            booking.setExternalBooking(true);
            booking.setSharedEquipmentAllocation(allocation);
            if (booking.getBaseHourlyRate() == null) {
                BigDecimal effectiveRate = allocation.getCustomHourlyRate() != null
                        ? allocation.getCustomHourlyRate()
                        : (equipment.getHourlyRateExternal() != null ? equipment.getHourlyRateExternal() : BigDecimal.ZERO);
                booking.setBaseHourlyRate(effectiveRate);
            }
        }

        // Equipment Qualification check
        if (equipment.isRequiresTrainingCertification()) {
            Optional<UserEquipmentQualification> qualOpt = qualificationRepository.findByUserIdAndEquipmentId(userId, equipmentId);
            if (qualOpt.isEmpty()) {
                throw new InvalidOperationException(String.format(
                        "User %d is not qualified to book equipment %d (training certification required)",
                        userId, equipmentId));
            }
            UserEquipmentQualification qual = qualOpt.get();
            if (qual.getStatus() != QualificationStatus.ACTIVE) {
                throw new InvalidOperationException(String.format(
                        "User %d qualification for equipment %d is not ACTIVE (status: %s)",
                        userId, equipmentId, qual.getStatus()));
            }
            if (qual.getExpiresAt() != null && qual.getExpiresAt().isBefore(Instant.now())) {
                throw new InvalidOperationException(String.format(
                        "User %d qualification for equipment %d expired at %s",
                        userId, equipmentId, qual.getExpiresAt()));
            }
        }

        // Overlapping Booking Prevention
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING_APPROVAL,
                BookingStatus.CONFIRMED,
                BookingStatus.IN_USE
        );
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(equipmentId, startTime, endTime, activeStatuses);
        boolean hasConflict = overlapping.stream().anyMatch(b -> !Objects.equals(b.getId(), booking.getId()));
        if (hasConflict) {
            throw new InvalidOperationException(String.format(
                    "Equipment %d is already booked for the requested time range (%s to %s)",
                    equipmentId, startTime, endTime));
        }

        // Booking Reference generation / uniqueness check
        if (booking.getBookingReference() == null || booking.getBookingReference().trim().isEmpty()) {
            booking.setBookingReference("BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        } else {
            String ref = booking.getBookingReference().trim();
            if (bookingRepository.existsByBookingReference(ref)) {
                throw new DuplicateResourceException("Booking", "bookingReference", ref);
            }
            booking.setBookingReference(ref);
        }

        // Calculate estimated cost
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (booking.getEstimatedCost() == null || booking.getEstimatedCost().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal hours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            BigDecimal rate = booking.getBaseHourlyRate() != null ? booking.getBaseHourlyRate() : BigDecimal.ZERO;
            booking.setEstimatedCost(rate.multiply(hours).setScale(2, RoundingMode.HALF_UP));
        }

        // Ensure default status and billing status
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING_APPROVAL);
        }
        if (booking.getBillingStatus() == null) {
            booking.setBillingStatus(BookingBillingStatus.UNBILLED);
        }
        if (booking.getPurpose() == null || booking.getPurpose().trim().isEmpty()) {
            booking.setPurpose("Standard laboratory reservation");
        }

        booking.setUser(user);
        booking.setInstitution(institution);
        booking.setDepartment(department);
        booking.setEquipment(equipment);

        return bookingRepository.save(booking);
    }

    private void validateOperatingWindow(Instant startTime, Instant endTime) {
        ZonedDateTime startZdt = startTime.atZone(ZoneOffset.UTC);
        ZonedDateTime endZdt = endTime.atZone(ZoneOffset.UTC);

        if (startZdt.getDayOfWeek() == DayOfWeek.SUNDAY || endZdt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new InvalidOperationException("Bookings are not permitted on Sundays. Operating window is Monday–Saturday, 08:00–20:00.");
        }

        if (!startZdt.toLocalDate().equals(endZdt.toLocalDate())) {
            throw new InvalidOperationException("Bookings cannot span across multiple calendar days. Operating window is Monday–Saturday, 08:00–20:00.");
        }

        LocalTime startTimeLocal = startZdt.toLocalTime();
        LocalTime endTimeLocal = endZdt.toLocalTime();

        if (startTimeLocal.isBefore(OPERATING_WINDOW_START) || endTimeLocal.isAfter(OPERATING_WINDOW_END)) {
            throw new InvalidOperationException(String.format(
                    "Booking time (%s to %s) must be within the operating window (08:00–20:00).",
                    startTimeLocal, endTimeLocal));
        }
    }

    @Override
    public Booking getBookingById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Booking ID cannot be null");
        }
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    @Override
    public Booking getBookingByReference(String bookingReference) {
        if (bookingReference == null || bookingReference.trim().isEmpty()) {
            throw new InvalidOperationException("Booking reference cannot be null or blank");
        }
        return bookingRepository.findByBookingReference(bookingReference.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", bookingReference.trim()));
    }

    @Override
    public List<Booking> listBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> listBookingsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return bookingRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public List<Booking> listBookingsByUser(Long userId) {
        if (userId == null) {
            throw new InvalidOperationException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public List<Booking> listBookingsByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return bookingRepository.findByInstitutionId(institutionId);
    }

    @Override
    public List<Booking> listBookingsByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return bookingRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Booking> listBookingsByStatus(BookingStatus status) {
        if (status == null) {
            throw new InvalidOperationException("Booking status cannot be null");
        }
        return bookingRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Booking confirmBooking(Long id, Long approvedByUserId) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.PENDING_APPROVAL) {
            throw new InvalidOperationException(String.format(
                    "Cannot confirm booking %d with status %s. Only PENDING_APPROVAL bookings can be confirmed.",
                    id, booking.getStatus()));
        }

        // Re-verify overlap against confirmed/in-use bookings prior to confirming
        List<BookingStatus> conflictingStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.IN_USE);
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                booking.getEquipment().getId(), booking.getStartTime(), booking.getEndTime(), conflictingStatuses);

        if (overlapping.stream().anyMatch(b -> !Objects.equals(b.getId(), id))) {
            throw new InvalidOperationException(String.format(
                    "Cannot confirm booking %d: conflicting confirmed or in-use booking exists for equipment %d in requested time range",
                    id, booking.getEquipment().getId()));
        }

        if (approvedByUserId != null) {
            User approver = userRepository.findById(approvedByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", approvedByUserId));
            booking.setApprovedByUser(approver);
        }

        booking.setApprovedAt(Instant.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long id, Long cancelledByUserId, String cancellationReason) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidOperationException(String.format("Cannot cancel an already COMPLETED booking with id %d", id));
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidOperationException(String.format("Cannot cancel an already CANCELLED booking with id %d", id));
        }
        if (booking.getStatus() == BookingStatus.IN_USE) {
            throw new InvalidOperationException(String.format("Cannot cancel an IN_USE booking with id %d. It must be completed.", id));
        }
        if (booking.getStatus() == BookingStatus.NO_SHOW) {
            throw new InvalidOperationException(String.format("Cannot cancel a NO_SHOW booking with id %d", id));
        }

        if (cancelledByUserId != null) {
            User canceller = userRepository.findById(cancelledByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", cancelledByUserId));
            booking.setCancelledByUser(canceller);
        }

        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(cancellationReason != null ? cancellationReason.trim() : null);
        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking startBooking(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidOperationException(String.format(
                    "Cannot start booking %d with status %s. Only CONFIRMED bookings can be started.",
                    id, booking.getStatus()));
        }

        Equipment equipment = booking.getEquipment();
        if (equipment.getStatus() == EquipmentStatus.RETIRED
                || equipment.getStatus() == EquipmentStatus.OUT_OF_SERVICE
                || equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            throw new InvalidOperationException(String.format(
                    "Cannot start booking on equipment %d with operational status %s",
                    equipment.getId(), equipment.getStatus()));
        }

        booking.setStatus(BookingStatus.IN_USE);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking completeBooking(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.IN_USE) {
            throw new InvalidOperationException(String.format(
                    "Cannot complete booking %d with status %s. Only IN_USE bookings can be completed.",
                    id, booking.getStatus()));
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking markNoShow(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidOperationException(String.format(
                    "Cannot mark booking %d as NO_SHOW with status %s. Only CONFIRMED bookings can be marked as NO_SHOW.",
                    id, booking.getStatus()));
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        return bookingRepository.save(booking);
    }
}
