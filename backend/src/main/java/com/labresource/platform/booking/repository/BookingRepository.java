package com.labresource.platform.booking.repository;

import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByEquipmentId(Long equipmentId);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByInstitutionId(Long institutionId);

    List<Booking> findByDepartmentId(Long departmentId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByBillingStatus(BookingBillingStatus billingStatus);

    List<Booking> findByRecurringSeriesId(Long recurringSeriesId);

    @Query("SELECT b FROM Booking b WHERE b.equipment.id = :equipmentId " +
           "AND b.status IN :statuses " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("equipmentId") Long equipmentId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("statuses") Collection<BookingStatus> statuses);
}
