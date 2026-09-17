package com.labresource.platform.maintenance.service;

import com.labresource.platform.maintenance.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface MaintenanceService {

    // ==========================================
    // Maintenance Request Operations
    // ==========================================

    /**
     * Submits a maintenance request for an equipment item with full tenant validation.
     */
    MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request, Long equipmentId, Long reportedByUserId, Long institutionId, Long departmentId);

    /**
     * Convenience overload without explicit institution/department overrides.
     */
    default MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request, Long equipmentId, Long reportedByUserId) {
        return createMaintenanceRequest(request, equipmentId, reportedByUserId, null, null);
    }

    /**
     * Retrieves a maintenance request by its ID.
     */
    MaintenanceRequest getMaintenanceRequestById(Long id);

    /**
     * Retrieves a maintenance request by its unique request number.
     */
    MaintenanceRequest getMaintenanceRequestByNumber(String requestNumber);

    /**
     * Lists all maintenance requests.
     */
    List<MaintenanceRequest> listMaintenanceRequests();

    /**
     * Lists all maintenance requests for a specific equipment item.
     */
    List<MaintenanceRequest> listMaintenanceRequestsByEquipment(Long equipmentId);

    /**
     * Lists all maintenance requests submitted by a specific user.
     */
    List<MaintenanceRequest> listMaintenanceRequestsByReportedUser(Long reportedByUserId);

    /**
     * Lists all maintenance requests in a given status.
     */
    List<MaintenanceRequest> listMaintenanceRequestsByStatus(MaintenanceRequestStatus status);

    /**
     * Lists all maintenance requests belonging to an institution.
     */
    List<MaintenanceRequest> listMaintenanceRequestsByInstitution(Long institutionId);

    /**
     * Lists all maintenance requests belonging to a department.
     */
    List<MaintenanceRequest> listMaintenanceRequestsByDepartment(Long departmentId);

    /**
     * Triages a submitted maintenance request, recording the triaging user, timestamp, and optional updated priority.
     */
    MaintenanceRequest triageMaintenanceRequest(Long id, Long triagedByUserId, MaintenancePriority updatedPriority);

    /**
     * Rejects a maintenance request with a reason.
     */
    MaintenanceRequest rejectMaintenanceRequest(Long id, Long triagedByUserId, String reason);

    /**
     * Resolves a maintenance request with resolution notes.
     */
    MaintenanceRequest resolveMaintenanceRequest(Long id, String resolutionNotes);

    // ==========================================
    // Maintenance Work Order Operations
    // ==========================================

    /**
     * Creates a maintenance work order, optionally linking to a maintenance request and assigning a technician.
     */
    MaintenanceWorkOrder createWorkOrder(MaintenanceWorkOrder workOrder, Long equipmentId, Long maintenanceRequestId, Long assignedTechnicianId);

    /**
     * Convenience overload for creating a standalone work order without a maintenance request.
     */
    default MaintenanceWorkOrder createWorkOrder(MaintenanceWorkOrder workOrder, Long equipmentId, Long assignedTechnicianId) {
        return createWorkOrder(workOrder, equipmentId, null, assignedTechnicianId);
    }

    /**
     * Retrieves a work order by its ID.
     */
    MaintenanceWorkOrder getWorkOrderById(Long id);

    /**
     * Retrieves a work order by its unique work order number.
     */
    MaintenanceWorkOrder getWorkOrderByNumber(String workOrderNumber);

    /**
     * Lists all maintenance work orders.
     */
    List<MaintenanceWorkOrder> listWorkOrders();

    /**
     * Lists all work orders for a specific equipment item.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByEquipment(Long equipmentId);

    /**
     * Lists all work orders assigned to a technician.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByTechnician(Long technicianId);

    /**
     * Lists all work orders associated with a maintenance request.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByRequest(Long maintenanceRequestId);

    /**
     * Lists all work orders with a specific status.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByStatus(WorkOrderStatus status);

    /**
     * Lists all work orders belonging to an institution.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByInstitution(Long institutionId);

    /**
     * Lists all work orders belonging to a department.
     */
    List<MaintenanceWorkOrder> listWorkOrdersByDepartment(Long departmentId);

    /**
     * Assigns or re-assigns a technician to a work order.
     */
    MaintenanceWorkOrder assignTechnician(Long workOrderId, Long technicianId);

    /**
     * Starts active work on a scheduled work order and sets equipment status to UNDER_MAINTENANCE.
     */
    MaintenanceWorkOrder startWorkOrder(Long workOrderId, Instant actualStart);

    /**
     * Pauses an in-progress work order (sets status to WAITING_FOR_PARTS).
     */
    MaintenanceWorkOrder pauseWorkOrder(Long workOrderId, String reason);

    /**
     * Resumes a paused work order (sets status back to IN_PROGRESS).
     */
    MaintenanceWorkOrder resumeWorkOrder(Long workOrderId);

    /**
     * Completes a work order, recording actual end time, labor/parts costs, notes, and restoring equipment to AVAILABLE.
     */
    MaintenanceWorkOrder completeWorkOrder(Long workOrderId, Instant actualEnd, BigDecimal laborHours, BigDecimal laborCost, BigDecimal partsCost, String summary, String rootCause, String resolutionNotes);

    /**
     * Cancels a work order and restores equipment status if applicable.
     */
    MaintenanceWorkOrder cancelWorkOrder(Long workOrderId, String cancellationReason);

    // ==========================================
    // Equipment Downtime Log Operations
    // ==========================================

    /**
     * Records an equipment downtime event, optionally linking to a work order.
     */
    EquipmentDowntimeLog recordDowntimeLog(EquipmentDowntimeLog log, Long equipmentId, Long workOrderId);

    /**
     * Convenience overload for recording downtime without a linked work order.
     */
    default EquipmentDowntimeLog recordDowntimeLog(EquipmentDowntimeLog log, Long equipmentId) {
        return recordDowntimeLog(log, equipmentId, null);
    }

    /**
     * Retrieves a downtime log by its ID.
     */
    EquipmentDowntimeLog getDowntimeLogById(Long id);

    /**
     * Lists all downtime logs.
     */
    List<EquipmentDowntimeLog> listDowntimeLogs();

    /**
     * Lists all downtime logs for an equipment item.
     */
    List<EquipmentDowntimeLog> listDowntimeLogsByEquipment(Long equipmentId);

    /**
     * Lists all downtime logs associated with a work order.
     */
    List<EquipmentDowntimeLog> listDowntimeLogsByWorkOrder(Long workOrderId);

    /**
     * Closes an active downtime log, recording the end time and calculating duration minutes.
     */
    EquipmentDowntimeLog endDowntimeLog(Long id, Instant downtimeEnd, Integer durationMinutes);
}
