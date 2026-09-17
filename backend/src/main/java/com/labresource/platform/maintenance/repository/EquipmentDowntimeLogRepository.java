package com.labresource.platform.maintenance.repository;

import com.labresource.platform.maintenance.EquipmentDowntimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentDowntimeLogRepository extends JpaRepository<EquipmentDowntimeLog, Long> {

    List<EquipmentDowntimeLog> findByEquipmentId(Long equipmentId);

    List<EquipmentDowntimeLog> findByWorkOrderId(Long workOrderId);
}
