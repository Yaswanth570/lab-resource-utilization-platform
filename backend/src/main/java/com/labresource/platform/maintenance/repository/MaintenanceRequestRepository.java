package com.labresource.platform.maintenance.repository;

import com.labresource.platform.maintenance.MaintenancePriority;
import com.labresource.platform.maintenance.MaintenanceRequest;
import com.labresource.platform.maintenance.MaintenanceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    Optional<MaintenanceRequest> findByRequestNumber(String requestNumber);

    boolean existsByRequestNumber(String requestNumber);

    List<MaintenanceRequest> findByEquipmentId(Long equipmentId);

    List<MaintenanceRequest> findByReportedByUserId(Long reportedByUserId);

    List<MaintenanceRequest> findByStatus(MaintenanceRequestStatus status);

    List<MaintenanceRequest> findByEquipmentIdAndStatus(Long equipmentId, MaintenanceRequestStatus status);

    List<MaintenanceRequest> findByEquipmentIdAndPriority(Long equipmentId, MaintenancePriority priority);
}
