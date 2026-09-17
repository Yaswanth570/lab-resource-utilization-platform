package com.labresource.platform.utilization.repository;

import com.labresource.platform.utilization.EquipmentIdleEvent;
import com.labresource.platform.utilization.IdleEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentIdleEventRepository extends JpaRepository<EquipmentIdleEvent, Long> {

    List<EquipmentIdleEvent> findByEquipmentId(Long equipmentId);

    List<EquipmentIdleEvent> findByEquipmentIdAndStatus(Long equipmentId, IdleEventStatus status);

    List<EquipmentIdleEvent> findByBookingId(Long bookingId);

    List<EquipmentIdleEvent> findByUsageSessionId(Long usageSessionId);
}
