package com.labresource.platform.utilization.repository;

import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentUsageSessionRepository extends JpaRepository<EquipmentUsageSession, Long> {

    List<EquipmentUsageSession> findByEquipmentId(Long equipmentId);

    List<EquipmentUsageSession> findByUserId(Long userId);

    Optional<EquipmentUsageSession> findByBookingId(Long bookingId);

    List<EquipmentUsageSession> findByEquipmentIdAndSessionStatus(Long equipmentId, SessionStatus sessionStatus);
}
