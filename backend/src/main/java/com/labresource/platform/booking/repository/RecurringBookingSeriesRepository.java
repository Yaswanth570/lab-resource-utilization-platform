package com.labresource.platform.booking.repository;

import com.labresource.platform.booking.RecurringBookingSeries;
import com.labresource.platform.booking.SeriesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringBookingSeriesRepository extends JpaRepository<RecurringBookingSeries, Long> {

    List<RecurringBookingSeries> findByUserId(Long userId);

    List<RecurringBookingSeries> findByEquipmentId(Long equipmentId);

    List<RecurringBookingSeries> findByEquipmentIdAndStatus(Long equipmentId, SeriesStatus status);
}
