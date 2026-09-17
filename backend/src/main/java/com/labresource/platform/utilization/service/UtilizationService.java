package com.labresource.platform.utilization.service;

import com.labresource.platform.utilization.EquipmentIdleEvent;
import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.IdleEventStatus;
import com.labresource.platform.utilization.SessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface UtilizationService {

    /**
     * Creates an equipment usage session with explicit domain validation and tenant verification.
     */
    EquipmentUsageSession createUsageSession(EquipmentUsageSession session, Long equipmentId, Long userId, Long bookingId, Long departmentId, Long institutionId);

    /**
     * Convenience overload for creating a usage session without explicit department/institution overrides.
     */
    default EquipmentUsageSession createUsageSession(EquipmentUsageSession session, Long equipmentId, Long userId, Long bookingId) {
        return createUsageSession(session, equipmentId, userId, bookingId, null, null);
    }

    /**
     * Retrieves an equipment usage session by its primary key ID.
     */
    EquipmentUsageSession getUsageSessionById(Long id);

    /**
     * Lists all equipment usage sessions.
     */
    List<EquipmentUsageSession> listUsageSessions();

    /**
     * Lists all equipment usage sessions for a specific equipment item.
     */
    List<EquipmentUsageSession> listUsageSessionsByEquipment(Long equipmentId);

    /**
     * Lists all equipment usage sessions initiated by a specific user.
     */
    List<EquipmentUsageSession> listUsageSessionsByUser(Long userId);

    /**
     * Lists all equipment usage sessions belonging to an institution.
     */
    List<EquipmentUsageSession> listUsageSessionsByInstitution(Long institutionId);

    /**
     * Lists all equipment usage sessions belonging to a department.
     */
    List<EquipmentUsageSession> listUsageSessionsByDepartment(Long departmentId);

    /**
     * Retrieves the usage session associated with a specific booking.
     */
    EquipmentUsageSession getUsageSessionByBooking(Long bookingId);

    /**
     * Lists all usage sessions in a given status.
     */
    List<EquipmentUsageSession> listUsageSessionsByStatus(SessionStatus status);

    /**
     * Lists usage sessions for an equipment item that overlap with the specified period.
     */
    List<EquipmentUsageSession> listUsageSessionsForPeriod(Long equipmentId, Instant startTime, Instant endTime);

    /**
     * Completes an active equipment usage session.
     */
    EquipmentUsageSession completeUsageSession(Long id, Instant checkedOutAt, String notes);

    /**
     * Terminates an active equipment usage session early.
     */
    EquipmentUsageSession terminateUsageSessionEarly(Long id, Instant checkedOutAt, String notes);

    /**
     * Calculates baseline equipment utilization percentage over an operating window (Mon–Sat 08:00–20:00).
     */
    BigDecimal calculateEquipmentUtilizationPercentage(Long equipmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Records an equipment idle event with domain validation and equipment consistency checks.
     */
    EquipmentIdleEvent recordIdleEvent(EquipmentIdleEvent idleEvent, Long equipmentId, Long bookingId, Long usageSessionId, Long loggedByUserId);

    /**
     * Retrieves an idle event by its primary key ID.
     */
    EquipmentIdleEvent getIdleEventById(Long id);

    /**
     * Lists all idle events for a specific equipment item.
     */
    List<EquipmentIdleEvent> listIdleEventsByEquipment(Long equipmentId);

    /**
     * Lists all idle events for an equipment item filtered by status.
     */
    List<EquipmentIdleEvent> listIdleEventsByStatus(Long equipmentId, IdleEventStatus status);

    /**
     * Lists all idle events associated with a booking.
     */
    List<EquipmentIdleEvent> listIdleEventsByBooking(Long bookingId);

    /**
     * Lists all idle events associated with a usage session.
     */
    List<EquipmentIdleEvent> listIdleEventsByUsageSession(Long usageSessionId);

    /**
     * Resolves an ongoing idle event, setting its end time, calculating duration, and marking status as RESOLVED.
     */
    EquipmentIdleEvent resolveIdleEvent(Long id, Instant idleEndTime, String notes);
}
