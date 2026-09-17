package com.labresource.platform.utilization.service;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import com.labresource.platform.utilization.*;
import com.labresource.platform.utilization.repository.EquipmentIdleEventRepository;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UtilizationServiceImpl implements UtilizationService {

    private final EquipmentUsageSessionRepository sessionRepository;
    private final EquipmentIdleEventRepository idleEventRepository;
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;

    public UtilizationServiceImpl(EquipmentUsageSessionRepository sessionRepository,
                                  EquipmentIdleEventRepository idleEventRepository,
                                  EquipmentRepository equipmentRepository,
                                  BookingRepository bookingRepository,
                                  UserRepository userRepository,
                                  InstitutionRepository institutionRepository,
                                  DepartmentRepository departmentRepository) {
        this.sessionRepository = sessionRepository;
        this.idleEventRepository = idleEventRepository;
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public EquipmentUsageSession createUsageSession(EquipmentUsageSession session,
                                                    Long equipmentId,
                                                    Long userId,
                                                    Long bookingId,
                                                    Long departmentId,
                                                    Long institutionId) {
        if (session == null) {
            throw new InvalidOperationException("Usage session payload cannot be null");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to create a usage session");
        }
        if (userId == null) {
            throw new InvalidOperationException("User ID is required to create a usage session");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot record utilization for deleted equipment with id %d", equipmentId));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException(String.format("Cannot record utilization for deleted or inactive user with id %d", userId));
        }

        // Institution validation if explicitly provided
        if (institutionId != null) {
            Institution institution = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));
            if (!institution.isActive()) {
                throw new InvalidOperationException(String.format("Cannot record utilization for inactive institution with id %d", institutionId));
            }
            if (!user.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format("User %d does not belong to institution %d", userId, institutionId));
            }
        }

        // Department validation if explicitly provided
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            if (!department.isActive()) {
                throw new InvalidOperationException(String.format("Cannot record utilization for inactive department with id %d", departmentId));
            }
            if (institutionId != null && !department.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format("Department %d does not belong to institution %d", departmentId, institutionId));
            }
        }

        // Booking linkage validation
        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

            if (!booking.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Booking equipment %d does not match utilization equipment %d",
                        booking.getEquipment().getId(), equipmentId));
            }
            if (!booking.getUser().getId().equals(userId)) {
                throw new InvalidOperationException(String.format(
                        "Booking user %d does not match utilization user %d",
                        booking.getUser().getId(), userId));
            }
            if (!booking.getUser().getInstitution().getId().equals(user.getInstitution().getId())) {
                throw new InvalidOperationException(
                        "Booking user institution does not match utilization user institution");
            }
            if (institutionId != null && !booking.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(
                        "Booking institution does not match requested institution");
            }

            session.setBooking(booking);
        }

        // Multi-tenant consistency / Cross-institution rules
        if (booking == null) {
            // Ad-hoc manual utilization recording: User and Equipment must belong to the same institution
            if (!user.getInstitution().getId().equals(equipment.getInstitution().getId())) {
                throw new InvalidOperationException(String.format(
                        "Arbitrary cross-institution utilization rejected: User belongs to institution %d but equipment belongs to institution %d without an approved sharing booking",
                        user.getInstitution().getId(), equipment.getInstitution().getId()));
            }
            if (institutionId != null && !equipment.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format(
                        "Equipment %d belongs to institution %d, not requested institution %d",
                        equipmentId, equipment.getInstitution().getId(), institutionId));
            }
        } else {
            // If linked to an internal booking, equipment must belong to the booking institution
            if (!booking.isExternalBooking()) {
                if (!equipment.getInstitution().getId().equals(booking.getInstitution().getId())) {
                    throw new InvalidOperationException(
                            "Internal booking equipment institution does not match booking institution");
                }
                if (!user.getInstitution().getId().equals(equipment.getInstitution().getId())) {
                    throw new InvalidOperationException(
                            "User institution does not match internal booking equipment institution");
                }
            }
        }

        // Timestamp and duration validation
        Instant checkedInAt = session.getCheckedInAt();
        if (checkedInAt == null) {
            throw new InvalidOperationException("Check-in timestamp is required");
        }

        if (session.getActualDurationMinutes() != null && session.getActualDurationMinutes() <= 0) {
            throw new InvalidOperationException("Actual duration must be positive");
        }

        Instant checkedOutAt = session.getCheckedOutAt();
        if (checkedOutAt != null) {
            if (!checkedInAt.isBefore(checkedOutAt)) {
                throw new InvalidOperationException("Check-in time must be before check-out time");
            }
            long expectedDuration = Duration.between(checkedInAt, checkedOutAt).toMinutes();
            if (expectedDuration <= 0) {
                throw new InvalidOperationException("Calculated duration must be positive");
            }

            if (session.getActualDurationMinutes() != null) {
                if (session.getActualDurationMinutes().longValue() != expectedDuration) {
                    throw new InvalidOperationException(String.format(
                            "Explicit actual duration %d minutes does not match timestamp duration %d minutes",
                            session.getActualDurationMinutes(), expectedDuration));
                }
            } else {
                session.setActualDurationMinutes((int) expectedDuration);
            }

            if (session.getSessionStatus() == null || session.getSessionStatus() == SessionStatus.ACTIVE) {
                session.setSessionStatus(SessionStatus.COMPLETED);
            }
        }

        // Scheduled duration handling
        if (session.getScheduledDurationMinutes() != null) {
            if (session.getScheduledDurationMinutes() <= 0) {
                throw new InvalidOperationException("Scheduled duration must be positive");
            }
            if (booking != null && booking.getStartTime() != null && booking.getEndTime() != null) {
                long expectedScheduled = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
                if (session.getScheduledDurationMinutes().longValue() != expectedScheduled) {
                    throw new InvalidOperationException(String.format(
                            "Explicit scheduled duration %d minutes does not match booking duration %d minutes",
                            session.getScheduledDurationMinutes(), expectedScheduled));
                }
            }
        } else {
            if (booking != null && booking.getStartTime() != null && booking.getEndTime() != null) {
                long scheduled = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
                session.setScheduledDurationMinutes((int) Math.max(1, scheduled));
            } else if (session.getActualDurationMinutes() != null) {
                session.setScheduledDurationMinutes(session.getActualDurationMinutes());
            } else {
                throw new InvalidOperationException("Scheduled duration minutes is required");
            }
        }

        if (session.getSessionStatus() == null) {
            session.setSessionStatus(SessionStatus.ACTIVE);
        }

        session.setEquipment(equipment);
        session.setUser(user);

        return sessionRepository.save(session);
    }

    @Override
    public EquipmentUsageSession getUsageSessionById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Session ID cannot be null");
        }
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentUsageSession", "id", id));
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return sessionRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsByUser(Long userId) {
        if (userId == null) {
            throw new InvalidOperationException("User ID cannot be null");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return sessionRepository.findByUserId(userId);
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return sessionRepository.findAll().stream()
                .filter(s -> s.getEquipment().getInstitution().getId().equals(institutionId)
                        || s.getUser().getInstitution().getId().equals(institutionId))
                .toList();
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return sessionRepository.findAll().stream()
                .filter(s -> (s.getEquipment().getDepartment() != null && s.getEquipment().getDepartment().getId().equals(departmentId))
                        || (s.getUser().getDepartment() != null && s.getUser().getDepartment().getId().equals(departmentId)))
                .toList();
    }

    @Override
    public EquipmentUsageSession getUsageSessionByBooking(Long bookingId) {
        if (bookingId == null) {
            throw new InvalidOperationException("Booking ID cannot be null");
        }
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }
        return sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentUsageSession", "bookingId", bookingId));
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsByStatus(SessionStatus status) {
        if (status == null) {
            throw new InvalidOperationException("Session status cannot be null");
        }
        return sessionRepository.findAll().stream()
                .filter(s -> s.getSessionStatus() == status)
                .toList();
    }

    @Override
    public List<EquipmentUsageSession> listUsageSessionsForPeriod(Long equipmentId, Instant startTime, Instant endTime) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new InvalidOperationException("Start and end timestamps are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidOperationException("Start time must be before end time");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }

        return sessionRepository.findByEquipmentId(equipmentId).stream()
                .filter(s -> {
                    Instant in = s.getCheckedInAt();
                    Instant out = s.getCheckedOutAt() != null ? s.getCheckedOutAt() : in;
                    return in.isBefore(endTime) && out.isAfter(startTime);
                })
                .toList();
    }

    @Override
    @Transactional
    public EquipmentUsageSession completeUsageSession(Long id, Instant checkedOutAt, String notes) {
        EquipmentUsageSession session = getUsageSessionById(id);

        if (session.getSessionStatus() != SessionStatus.ACTIVE) {
            throw new InvalidOperationException(String.format(
                    "Cannot complete session %d with status %s. Only ACTIVE sessions can be completed.",
                    id, session.getSessionStatus()));
        }

        Instant out = checkedOutAt != null ? checkedOutAt : Instant.now();
        if (!session.getCheckedInAt().isBefore(out)) {
            throw new InvalidOperationException("Check-out time must be after check-in time");
        }

        session.setCheckedOutAt(out);
        int duration = (int) Duration.between(session.getCheckedInAt(), out).toMinutes();
        session.setActualDurationMinutes(Math.max(1, duration));
        session.setSessionStatus(SessionStatus.COMPLETED);

        if (notes != null && !notes.trim().isEmpty()) {
            session.setNotes(session.getNotes() != null
                    ? session.getNotes() + " | " + notes.trim()
                    : notes.trim());
        }

        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public EquipmentUsageSession terminateUsageSessionEarly(Long id, Instant checkedOutAt, String notes) {
        EquipmentUsageSession session = getUsageSessionById(id);

        if (session.getSessionStatus() != SessionStatus.ACTIVE) {
            throw new InvalidOperationException(String.format(
                    "Cannot terminate session %d with status %s. Only ACTIVE sessions can be terminated early.",
                    id, session.getSessionStatus()));
        }

        Instant out = checkedOutAt != null ? checkedOutAt : Instant.now();
        if (!session.getCheckedInAt().isBefore(out)) {
            throw new InvalidOperationException("Check-out time must be after check-in time");
        }

        session.setCheckedOutAt(out);
        int duration = (int) Duration.between(session.getCheckedInAt(), out).toMinutes();
        session.setActualDurationMinutes(Math.max(1, duration));
        session.setSessionStatus(SessionStatus.TERMINATED_EARLY);

        if (notes != null && !notes.trim().isEmpty()) {
            session.setNotes(session.getNotes() != null
                    ? session.getNotes() + " | " + notes.trim()
                    : notes.trim());
        }

        return sessionRepository.save(session);
    }

    @Override
    public BigDecimal calculateEquipmentUtilizationPercentage(Long equipmentId, LocalDate startDate, LocalDate endDate) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new InvalidOperationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }

        // Calculate available operating minutes in the window (08:00–20:00 = 12h = 720m, Monday–Saturday)
        long availableMinutes = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                availableMinutes += 720;
            }
        }

        if (availableMinutes == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Instant rangeStart = startDate.atTime(8, 0).toInstant(ZoneOffset.UTC);
        Instant rangeEnd = endDate.atTime(20, 0).toInstant(ZoneOffset.UTC);

        long usedMinutes = sessionRepository.findByEquipmentId(equipmentId).stream()
                .filter(s -> s.getSessionStatus() == SessionStatus.COMPLETED
                        || s.getSessionStatus() == SessionStatus.TERMINATED_EARLY
                        || s.getSessionStatus() == SessionStatus.AUTO_CLOSED)
                .filter(s -> s.getCheckedInAt().isBefore(rangeEnd)
                        && (s.getCheckedOutAt() == null || s.getCheckedOutAt().isAfter(rangeStart)))
                .mapToLong(s -> s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0)
                .sum();

        return BigDecimal.valueOf(usedMinutes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(availableMinutes), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public EquipmentIdleEvent recordIdleEvent(EquipmentIdleEvent idleEvent,
                                              Long equipmentId,
                                              Long bookingId,
                                              Long usageSessionId,
                                              Long loggedByUserId) {
        if (idleEvent == null) {
            throw new InvalidOperationException("Idle event payload cannot be null");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to record an idle event");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot record idle event for deleted equipment with id %d", equipmentId));
        }

        if (idleEvent.getIdleStartTime() == null) {
            throw new InvalidOperationException("Idle start time is required");
        }

        if (bookingId != null) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
            if (!booking.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Booking equipment %d does not match idle event equipment %d",
                        booking.getEquipment().getId(), equipmentId));
            }
            idleEvent.setBooking(booking);
        }

        if (usageSessionId != null) {
            EquipmentUsageSession session = sessionRepository.findById(usageSessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("EquipmentUsageSession", "id", usageSessionId));
            if (!session.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Usage session equipment %d does not match idle event equipment %d",
                        session.getEquipment().getId(), equipmentId));
            }
            idleEvent.setUsageSession(session);
        }

        if (loggedByUserId != null) {
            User loggedByUser = userRepository.findById(loggedByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", loggedByUserId));
            idleEvent.setLoggedByUser(loggedByUser);
        }

        if (idleEvent.getIdleEndTime() != null) {
            if (!idleEvent.getIdleStartTime().isBefore(idleEvent.getIdleEndTime())) {
                throw new InvalidOperationException("Idle start time must be before idle end time");
            }
            if (idleEvent.getIdleDurationMinutes() == null) {
                int duration = (int) Duration.between(idleEvent.getIdleStartTime(), idleEvent.getIdleEndTime()).toMinutes();
                idleEvent.setIdleDurationMinutes(Math.max(1, duration));
            }
            if (idleEvent.getIdleDurationMinutes() <= 0) {
                throw new InvalidOperationException("Idle duration must be positive");
            }
            if (idleEvent.getStatus() == null || idleEvent.getStatus() == IdleEventStatus.ONGOING) {
                idleEvent.setStatus(IdleEventStatus.RESOLVED);
            }
        }

        if (idleEvent.getDetectionSource() == null) {
            idleEvent.setDetectionSource(IdleDetectionSource.MANUAL_LAB_AUDIT);
        }
        if (idleEvent.getStatus() == null) {
            idleEvent.setStatus(IdleEventStatus.ONGOING);
        }

        idleEvent.setEquipment(equipment);

        return idleEventRepository.save(idleEvent);
    }

    @Override
    public EquipmentIdleEvent getIdleEventById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Idle event ID cannot be null");
        }
        return idleEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentIdleEvent", "id", id));
    }

    @Override
    public List<EquipmentIdleEvent> listIdleEventsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return idleEventRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public List<EquipmentIdleEvent> listIdleEventsByStatus(Long equipmentId, IdleEventStatus status) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (status == null) {
            throw new InvalidOperationException("Idle event status cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return idleEventRepository.findByEquipmentIdAndStatus(equipmentId, status);
    }

    @Override
    public List<EquipmentIdleEvent> listIdleEventsByBooking(Long bookingId) {
        if (bookingId == null) {
            throw new InvalidOperationException("Booking ID cannot be null");
        }
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }
        return idleEventRepository.findByBookingId(bookingId);
    }

    @Override
    public List<EquipmentIdleEvent> listIdleEventsByUsageSession(Long usageSessionId) {
        if (usageSessionId == null) {
            throw new InvalidOperationException("Usage session ID cannot be null");
        }
        if (!sessionRepository.existsById(usageSessionId)) {
            throw new ResourceNotFoundException("EquipmentUsageSession", "id", usageSessionId);
        }
        return idleEventRepository.findByUsageSessionId(usageSessionId);
    }

    @Override
    @Transactional
    public EquipmentIdleEvent resolveIdleEvent(Long id, Instant idleEndTime, String notes) {
        EquipmentIdleEvent event = getIdleEventById(id);

        if (event.getStatus() == IdleEventStatus.RESOLVED) {
            throw new InvalidOperationException(String.format("Idle event %d is already resolved", id));
        }

        Instant end = idleEndTime != null ? idleEndTime : Instant.now();
        if (!event.getIdleStartTime().isBefore(end)) {
            throw new InvalidOperationException("Idle end time must be after idle start time");
        }

        event.setIdleEndTime(end);
        int duration = (int) Duration.between(event.getIdleStartTime(), end).toMinutes();
        event.setIdleDurationMinutes(Math.max(1, duration));
        event.setStatus(IdleEventStatus.RESOLVED);

        if (notes != null && !notes.trim().isEmpty()) {
            event.setNotes(event.getNotes() != null
                    ? event.getNotes() + " | " + notes.trim()
                    : notes.trim());
        }

        return idleEventRepository.save(event);
    }
}
