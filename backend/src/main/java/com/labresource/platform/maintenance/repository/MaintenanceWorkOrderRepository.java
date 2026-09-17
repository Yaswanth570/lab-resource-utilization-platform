package com.labresource.platform.maintenance.repository;

import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceWorkOrderRepository extends JpaRepository<MaintenanceWorkOrder, Long> {

    Optional<MaintenanceWorkOrder> findByWorkOrderNumber(String workOrderNumber);

    boolean existsByWorkOrderNumber(String workOrderNumber);

    List<MaintenanceWorkOrder> findByEquipmentId(Long equipmentId);

    List<MaintenanceWorkOrder> findByAssignedTechnicianId(Long assignedTechnicianId);

    List<MaintenanceWorkOrder> findByMaintenanceRequestId(Long maintenanceRequestId);

    List<MaintenanceWorkOrder> findByEquipmentIdAndStatus(Long equipmentId, WorkOrderStatus status);

    List<MaintenanceWorkOrder> findByAssignedTechnicianIdAndStatus(Long assignedTechnicianId, WorkOrderStatus status);
}
