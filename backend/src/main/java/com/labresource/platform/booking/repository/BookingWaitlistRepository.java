package com.labresource.platform.booking.repository;

import com.labresource.platform.booking.BookingWaitlist;
import com.labresource.platform.booking.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingWaitlistRepository extends JpaRepository<BookingWaitlist, Long> {

    List<BookingWaitlist> findByEquipmentId(Long equipmentId);

    List<BookingWaitlist> findByUserId(Long userId);

    List<BookingWaitlist> findByEquipmentIdAndStatusOrderByPositionAsc(Long equipmentId, WaitlistStatus status);
}
