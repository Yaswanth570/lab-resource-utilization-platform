package com.labresource.platform.maintenance.service;

import com.labresource.platform.common.exception.DuplicateResourceException;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.institution.Institution;
import com.labresource.platform.institution.repository.InstitutionRepository;
import com.labresource.platform.maintenance.*;
import com.labresource.platform.maintenance.repository.EquipmentDowntimeLogRepository;
import com.labresource.platform.maintenance.repository.MaintenanceRequestRepository;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.UserStatus;
import com.labresource.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRequestRepository requestRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final EquipmentDowntimeLogRepository downtimeLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;

    public MaintenanceServiceImpl(MaintenanceRequestRepository requestRepository,
                                  MaintenanceWorkOrderRepository workOrderRepository,
                                  EquipmentDowntimeLogRepository downtimeLogRepository,
                                  EquipmentRepository equipmentRepository,
                                  UserRepository userRepository,
                                  InstitutionRepository institutionRepository,
                                  DepartmentRepository departmentRepository) {
        this.requestRepository = requestRepository;
        this.workOrderRepository = workOrderRepository;
        this.downtimeLogRepository = downtimeLogRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
    }

    // ==========================================
    // Maintenance Request Operations
    // ==========================================

    @Override
    @Transactional
    public MaintenanceRequest createMaintenanceRequest(MaintenanceRequest request,
                                                       Long equipmentId,
                                                       Long reportedByUserId,
                                                       Long institutionId,
                                                       Long departmentId) {
        if (request == null) {
            throw new InvalidOperationException("Maintenance request payload cannot be null");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to create a maintenance request");
        }
        if (reportedByUserId == null) {
            throw new InvalidOperationException("Reported by user ID is required to create a maintenance request");
        }
        if (request.getIssueTitle() == null || request.getIssueTitle().trim().isEmpty()) {
            throw new InvalidOperationException("Issue title is required");
        }
        if (request.getIssueDescription() == null || request.getIssueDescription().trim().isEmpty()) {
            throw new InvalidOperationException("Issue description is required");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot report maintenance on deleted equipment with id %d", equipmentId));
        }

        User reportedByUser = userRepository.findById(reportedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reportedByUserId));

        if (reportedByUser.getDeletedAt() != null || reportedByUser.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException(String.format("Cannot report maintenance by inactive or deleted user with id %d", reportedByUserId));
        }

        // Validate tenant alignment between user and equipment
        if (!reportedByUser.getInstitution().getId().equals(equipment.getInstitution().getId())) {
            throw new InvalidOperationException(String.format(
                    "User institution %d does not match equipment institution %d",
                    reportedByUser.getInstitution().getId(), equipment.getInstitution().getId()));
        }

        // Validate explicit institution if provided
        if (institutionId != null) {
            Institution institution = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Institution", "id", institutionId));
            if (!institution.isActive()) {
                throw new InvalidOperationException(String.format("Institution %d is inactive", institutionId));
            }
            if (!equipment.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException(String.format("Equipment %d does not belong to institution %d", equipmentId, institutionId));
            }
        }

        // Validate explicit department if provided
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            if (!department.isActive()) {
                throw new InvalidOperationException(String.format("Department %d is inactive", departmentId));
            }
            if (!department.getInstitution().getId().equals(equipment.getInstitution().getId())) {
                throw new InvalidOperationException(String.format("Department %d does not belong to equipment institution %d", departmentId, equipment.getInstitution().getId()));
            }
        }

        // Validate or generate requestNumber
        if (request.getRequestNumber() != null && !request.getRequestNumber().trim().isEmpty()) {
            String reqNum = request.getRequestNumber().trim();
            if (requestRepository.existsByRequestNumber(reqNum)) {
                throw new DuplicateResourceException("MaintenanceRequest", "requestNumber", reqNum);
            }
            request.setRequestNumber(reqNum);
        } else {
            request.setRequestNumber(generateRequestNumber());
        }

        if (request.getPriority() == null) {
            request.setPriority(MaintenancePriority.MEDIUM);
        }

        request.setStatus(MaintenanceRequestStatus.SUBMITTED);
        request.setEquipment(equipment);
        request.setReportedByUser(reportedByUser);

        return requestRepository.save(request);
    }

    @Override
    public MaintenanceRequest getMaintenanceRequestById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Maintenance request ID cannot be null");
        }
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", "id", id));
    }

    @Override
    public MaintenanceRequest getMaintenanceRequestByNumber(String requestNumber) {
        if (requestNumber == null || requestNumber.trim().isEmpty()) {
            throw new InvalidOperationException("Request number cannot be null or empty");
        }
        return requestRepository.findByRequestNumber(requestNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", "requestNumber", requestNumber.trim()));
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequests() {
        return requestRepository.findAll();
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequestsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return requestRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequestsByReportedUser(Long reportedByUserId) {
        if (reportedByUserId == null) {
            throw new InvalidOperationException("User ID cannot be null");
        }
        if (!userRepository.existsById(reportedByUserId)) {
            throw new ResourceNotFoundException("User", "id", reportedByUserId);
        }
        return requestRepository.findByReportedByUserId(reportedByUserId);
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequestsByStatus(MaintenanceRequestStatus status) {
        if (status == null) {
            throw new InvalidOperationException("Status cannot be null");
        }
        return requestRepository.findByStatus(status);
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequestsByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return requestRepository.findAll().stream()
                .filter(r -> r.getEquipment().getInstitution().getId().equals(institutionId))
                .toList();
    }

    @Override
    public List<MaintenanceRequest> listMaintenanceRequestsByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return requestRepository.findAll().stream()
                .filter(r -> r.getEquipment().getDepartment() != null
                        && r.getEquipment().getDepartment().getId().equals(departmentId))
                .toList();
    }

    @Override
    @Transactional
    public MaintenanceRequest triageMaintenanceRequest(Long id, Long triagedByUserId, MaintenancePriority updatedPriority) {
        MaintenanceRequest request = getMaintenanceRequestById(id);

        if (request.getStatus() != MaintenanceRequestStatus.SUBMITTED) {
            throw new InvalidOperationException(String.format(
                    "Cannot triage maintenance request %d with status %s. Only SUBMITTED requests can be triaged.",
                    id, request.getStatus()));
        }

        if (triagedByUserId == null) {
            throw new InvalidOperationException("Triaged by user ID is required");
        }

        User triagedByUser = userRepository.findById(triagedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", triagedByUserId));

        if (triagedByUser.getDeletedAt() != null || triagedByUser.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException(String.format("User %d is inactive or deleted", triagedByUserId));
        }

        if (!triagedByUser.getInstitution().getId().equals(request.getEquipment().getInstitution().getId())) {
            throw new InvalidOperationException(String.format(
                    "Triaging user institution %d does not match equipment institution %d",
                    triagedByUser.getInstitution().getId(), request.getEquipment().getInstitution().getId()));
        }

        request.setTriagedByUser(triagedByUser);
        request.setTriagedAt(Instant.now());
        request.setStatus(MaintenanceRequestStatus.TRIAGED);

        if (updatedPriority != null) {
            request.setPriority(updatedPriority);
        }

        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public MaintenanceRequest rejectMaintenanceRequest(Long id, Long triagedByUserId, String reason) {
        MaintenanceRequest request = getMaintenanceRequestById(id);

        if (request.getStatus() == MaintenanceRequestStatus.RESOLVED
                || request.getStatus() == MaintenanceRequestStatus.REJECTED
                || request.getStatus() == MaintenanceRequestStatus.WORK_ORDER_CREATED) {
            throw new InvalidOperationException(String.format(
                    "Cannot reject maintenance request %d with status %s", id, request.getStatus()));
        }

        if (triagedByUserId != null) {
            User triagedByUser = userRepository.findById(triagedByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", triagedByUserId));
            if (!triagedByUser.getInstitution().getId().equals(request.getEquipment().getInstitution().getId())) {
                throw new InvalidOperationException(String.format(
                        "Triaging user institution %d does not match equipment institution %d",
                        triagedByUser.getInstitution().getId(), request.getEquipment().getInstitution().getId()));
            }
            request.setTriagedByUser(triagedByUser);
            request.setTriagedAt(Instant.now());
        }

        request.setStatus(MaintenanceRequestStatus.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            request.setIssueDescription(request.getIssueDescription() + " | Rejection reason: " + reason.trim());
        }

        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public MaintenanceRequest resolveMaintenanceRequest(Long id, String resolutionNotes) {
        MaintenanceRequest request = getMaintenanceRequestById(id);

        if (request.getStatus() == MaintenanceRequestStatus.RESOLVED) {
            throw new InvalidOperationException(String.format("Maintenance request %d is already resolved", id));
        }
        if (request.getStatus() == MaintenanceRequestStatus.REJECTED) {
            throw new InvalidOperationException(String.format("Cannot resolve rejected maintenance request %d", id));
        }

        request.setStatus(MaintenanceRequestStatus.RESOLVED);
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            request.setIssueDescription(request.getIssueDescription() + " | Resolution: " + resolutionNotes.trim());
        }

        return requestRepository.save(request);
    }

    // ==========================================
    // Maintenance Work Order Operations
    // ==========================================

    @Override
    @Transactional
    public MaintenanceWorkOrder createWorkOrder(MaintenanceWorkOrder workOrder,
                                                Long equipmentId,
                                                Long maintenanceRequestId,
                                                Long assignedTechnicianId) {
        if (workOrder == null) {
            throw new InvalidOperationException("Work order payload cannot be null");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to create a work order");
        }
        if (workOrder.getType() == null) {
            throw new InvalidOperationException("Work order type is required");
        }
        if (workOrder.getScheduledStart() == null || workOrder.getScheduledEnd() == null) {
            throw new InvalidOperationException("Scheduled start and end timestamps are required");
        }
        if (!workOrder.getScheduledStart().isBefore(workOrder.getScheduledEnd())) {
            throw new InvalidOperationException("Scheduled start time must be before scheduled end time");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot create work order for deleted equipment with id %d", equipmentId));
        }
        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new InvalidOperationException(String.format("Cannot create work order for RETIRED equipment with id %d", equipmentId));
        }

        // Maintenance request linkage
        MaintenanceRequest maintenanceRequest = null;
        if (maintenanceRequestId != null) {
            maintenanceRequest = requestRepository.findById(maintenanceRequestId)
                    .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", "id", maintenanceRequestId));

            if (!maintenanceRequest.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Work order equipment %d does not match maintenance request equipment %d",
                        equipmentId, maintenanceRequest.getEquipment().getId()));
            }

            if (maintenanceRequest.getStatus() == MaintenanceRequestStatus.RESOLVED
                    || maintenanceRequest.getStatus() == MaintenanceRequestStatus.REJECTED) {
                throw new InvalidOperationException(String.format(
                        "Cannot link work order to maintenance request %d with status %s",
                        maintenanceRequestId, maintenanceRequest.getStatus()));
            }

            maintenanceRequest.setStatus(MaintenanceRequestStatus.WORK_ORDER_CREATED);
            requestRepository.save(maintenanceRequest);
            workOrder.setMaintenanceRequest(maintenanceRequest);
        }

        // Assigned technician validation
        if (assignedTechnicianId != null) {
            User technician = userRepository.findById(assignedTechnicianId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedTechnicianId));
            if (technician.getDeletedAt() != null || technician.getStatus() != UserStatus.ACTIVE) {
                throw new InvalidOperationException(String.format("Technician %d is inactive or deleted", assignedTechnicianId));
            }
            if (!technician.getInstitution().getId().equals(equipment.getInstitution().getId())) {
                throw new InvalidOperationException(String.format(
                        "Assigned technician institution %d does not match equipment institution %d",
                        technician.getInstitution().getId(), equipment.getInstitution().getId()));
            }
            workOrder.setAssignedTechnician(technician);
        }

        // Validate or generate workOrderNumber
        if (workOrder.getWorkOrderNumber() != null && !workOrder.getWorkOrderNumber().trim().isEmpty()) {
            String woNum = workOrder.getWorkOrderNumber().trim();
            if (workOrderRepository.existsByWorkOrderNumber(woNum)) {
                throw new DuplicateResourceException("MaintenanceWorkOrder", "workOrderNumber", woNum);
            }
            workOrder.setWorkOrderNumber(woNum);
        } else {
            workOrder.setWorkOrderNumber(generateWorkOrderNumber());
        }

        if (workOrder.getPriority() == null) {
            workOrder.setPriority(maintenanceRequest != null ? maintenanceRequest.getPriority() : MaintenancePriority.MEDIUM);
        }

        workOrder.setStatus(WorkOrderStatus.SCHEDULED);
        workOrder.setEquipment(equipment);

        if (workOrder.getLaborHours() == null) {
            workOrder.setLaborHours(BigDecimal.ZERO);
        }
        if (workOrder.getLaborCost() == null) {
            workOrder.setLaborCost(BigDecimal.ZERO);
        }
        if (workOrder.getPartsCost() == null) {
            workOrder.setPartsCost(BigDecimal.ZERO);
        }
        if (workOrder.getTotalCost() == null) {
            workOrder.setTotalCost(workOrder.getLaborCost().add(workOrder.getPartsCost()));
        }

        return workOrderRepository.save(workOrder);
    }

    @Override
    public MaintenanceWorkOrder getWorkOrderById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Work order ID cannot be null");
        }
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWorkOrder", "id", id));
    }

    @Override
    public MaintenanceWorkOrder getWorkOrderByNumber(String workOrderNumber) {
        if (workOrderNumber == null || workOrderNumber.trim().isEmpty()) {
            throw new InvalidOperationException("Work order number cannot be null or empty");
        }
        return workOrderRepository.findByWorkOrderNumber(workOrderNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWorkOrder", "workOrderNumber", workOrderNumber.trim()));
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrders() {
        return workOrderRepository.findAll();
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        return workOrderRepository.findByEquipmentId(equipmentId);
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByTechnician(Long technicianId) {
        if (technicianId == null) {
            throw new InvalidOperationException("Technician ID cannot be null");
        }
        if (!userRepository.existsById(technicianId)) {
            throw new ResourceNotFoundException("User", "id", technicianId);
        }
        return workOrderRepository.findByAssignedTechnicianId(technicianId);
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByRequest(Long maintenanceRequestId) {
        if (maintenanceRequestId == null) {
            throw new InvalidOperationException("Maintenance request ID cannot be null");
        }
        if (!requestRepository.existsById(maintenanceRequestId)) {
            throw new ResourceNotFoundException("MaintenanceRequest", "id", maintenanceRequestId);
        }
        return workOrderRepository.findByMaintenanceRequestId(maintenanceRequestId);
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByStatus(WorkOrderStatus status) {
        if (status == null) {
            throw new InvalidOperationException("Status cannot be null");
        }
        return workOrderRepository.findAll().stream()
                .filter(wo -> wo.getStatus() == status)
                .toList();
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByInstitution(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Institution ID cannot be null");
        }
        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", "id", institutionId);
        }
        return workOrderRepository.findAll().stream()
                .filter(wo -> wo.getEquipment().getInstitution().getId().equals(institutionId))
                .toList();
    }

    @Override
    public List<MaintenanceWorkOrder> listWorkOrdersByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new InvalidOperationException("Department ID cannot be null");
        }
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }
        return workOrderRepository.findAll().stream()
                .filter(wo -> wo.getEquipment().getDepartment() != null
                        && wo.getEquipment().getDepartment().getId().equals(departmentId))
                .toList();
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder assignTechnician(Long workOrderId, Long technicianId) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new InvalidOperationException(String.format(
                    "Cannot assign technician to work order %d with status %s", workOrderId, workOrder.getStatus()));
        }

        if (technicianId == null) {
            workOrder.setAssignedTechnician(null);
            return workOrderRepository.save(workOrder);
        }

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", technicianId));

        if (technician.getDeletedAt() != null || technician.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException(String.format("Technician %d is inactive or deleted", technicianId));
        }

        if (!technician.getInstitution().getId().equals(workOrder.getEquipment().getInstitution().getId())) {
            throw new InvalidOperationException(String.format(
                    "Technician institution %d does not match equipment institution %d",
                    technician.getInstitution().getId(), workOrder.getEquipment().getInstitution().getId()));
        }

        workOrder.setAssignedTechnician(technician);
        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder startWorkOrder(Long workOrderId, Instant actualStart) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.SCHEDULED) {
            throw new InvalidOperationException(String.format(
                    "Cannot start work order %d with status %s. Only SCHEDULED work orders can be started.",
                    workOrderId, workOrder.getStatus()));
        }

        Equipment equipment = workOrder.getEquipment();
        if (equipment.getStatus() == EquipmentStatus.RETIRED) {
            throw new InvalidOperationException(String.format("Cannot start maintenance on RETIRED equipment with id %d", equipment.getId()));
        }

        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setActualStart(actualStart != null ? actualStart : Instant.now());

        // Preserve pre-maintenance status if not already under maintenance
        if (equipment.getStatus() != EquipmentStatus.UNDER_MAINTENANCE) {
            equipment.setOperationalStatusReason("PREV_STATUS:" + equipment.getStatus().name());
        }

        // Equipment status side effect: transition to UNDER_MAINTENANCE
        equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        equipmentRepository.save(equipment);

        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder pauseWorkOrder(Long workOrderId, String reason) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new InvalidOperationException(String.format(
                    "Cannot pause work order %d with status %s. Only IN_PROGRESS work orders can be paused.",
                    workOrderId, workOrder.getStatus()));
        }

        workOrder.setStatus(WorkOrderStatus.WAITING_FOR_PARTS);
        if (reason != null && !reason.trim().isEmpty()) {
            workOrder.setResolutionNotes(workOrder.getResolutionNotes() != null
                    ? workOrder.getResolutionNotes() + " | Paused: " + reason.trim()
                    : "Paused: " + reason.trim());
        }

        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder resumeWorkOrder(Long workOrderId) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.WAITING_FOR_PARTS) {
            throw new InvalidOperationException(String.format(
                    "Cannot resume work order %d with status %s. Only WAITING_FOR_PARTS work orders can be resumed.",
                    workOrderId, workOrder.getStatus()));
        }

        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder completeWorkOrder(Long workOrderId,
                                                  Instant actualEnd,
                                                  BigDecimal laborHours,
                                                  BigDecimal laborCost,
                                                  BigDecimal partsCost,
                                                  String summary,
                                                  String rootCause,
                                                  String resolutionNotes) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new InvalidOperationException(String.format(
                    "Cannot complete work order %d with status %s. Only IN_PROGRESS work orders can be completed.",
                    workOrderId, workOrder.getStatus()));
        }

        Instant end = actualEnd != null ? actualEnd : Instant.now();
        if (workOrder.getActualStart() != null && !workOrder.getActualStart().isBefore(end)) {
            throw new InvalidOperationException("Actual completion end time must be after actual start time");
        }

        if (laborHours != null) {
            if (laborHours.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Labor hours cannot be negative");
            }
            workOrder.setLaborHours(laborHours);
        }

        if (laborCost != null) {
            if (laborCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Labor cost cannot be negative");
            }
            workOrder.setLaborCost(laborCost);
        }

        if (partsCost != null) {
            if (partsCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Parts cost cannot be negative");
            }
            workOrder.setPartsCost(partsCost);
        }

        BigDecimal total = workOrder.getLaborCost().add(workOrder.getPartsCost());
        workOrder.setTotalCost(total);

        if (summary != null && !summary.trim().isEmpty()) {
            workOrder.setWorkPerformedSummary(summary.trim());
        }
        if (rootCause != null && !rootCause.trim().isEmpty()) {
            workOrder.setFailureRootCause(rootCause.trim());
        }
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            workOrder.setResolutionNotes(workOrder.getResolutionNotes() != null
                    ? workOrder.getResolutionNotes() + " | " + resolutionNotes.trim()
                    : resolutionNotes.trim());
        }

        workOrder.setActualEnd(end);
        workOrder.setStatus(WorkOrderStatus.COMPLETED);

        // Equipment status side effect: return to AVAILABLE if no other active (IN_PROGRESS or WAITING_FOR_PARTS) work orders
        Equipment equipment = workOrder.getEquipment();
        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            boolean hasOtherActiveWorkOrders = workOrderRepository.findByEquipmentId(equipment.getId())
                    .stream()
                    .filter(wo -> !wo.getId().equals(workOrderId))
                    .anyMatch(wo -> wo.getStatus() == WorkOrderStatus.IN_PROGRESS
                            || wo.getStatus() == WorkOrderStatus.WAITING_FOR_PARTS);

            if (!hasOtherActiveWorkOrders && equipment.getStatus() != EquipmentStatus.RETIRED) {
                equipment.setStatus(EquipmentStatus.AVAILABLE);
                equipment.setOperationalStatusReason(null);
                equipmentRepository.save(equipment);
            }
        }

        // Linked maintenance request resolution: ONLY when every linked work order is terminal (COMPLETED or CANCELLED)
        if (workOrder.getMaintenanceRequest() != null
                && workOrder.getMaintenanceRequest().getStatus() == MaintenanceRequestStatus.WORK_ORDER_CREATED) {
            MaintenanceRequest request = workOrder.getMaintenanceRequest();
            List<MaintenanceWorkOrder> linkedOrders = workOrderRepository.findByMaintenanceRequestId(request.getId());
            boolean allTerminal = linkedOrders.isEmpty() || linkedOrders.stream()
                    .allMatch(wo -> {
                        WorkOrderStatus s = (wo.getId() != null && wo.getId().equals(workOrderId)) ? WorkOrderStatus.COMPLETED : wo.getStatus();
                        return s == WorkOrderStatus.COMPLETED || s == WorkOrderStatus.CANCELLED;
                    });

            if (allTerminal) {
                request.setStatus(MaintenanceRequestStatus.RESOLVED);
                requestRepository.save(request);
            }
        }

        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public MaintenanceWorkOrder cancelWorkOrder(Long workOrderId, String cancellationReason) {
        MaintenanceWorkOrder workOrder = getWorkOrderById(workOrderId);

        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED || workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new InvalidOperationException(String.format(
                    "Cannot cancel work order %d with status %s", workOrderId, workOrder.getStatus()));
        }

        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        if (cancellationReason != null && !cancellationReason.trim().isEmpty()) {
            workOrder.setResolutionNotes(workOrder.getResolutionNotes() != null
                    ? workOrder.getResolutionNotes() + " | Cancelled: " + cancellationReason.trim()
                    : "Cancelled: " + cancellationReason.trim());
        }

        // Equipment status side effect: if equipment was UNDER_MAINTENANCE, check if it can leave UNDER_MAINTENANCE
        Equipment equipment = workOrder.getEquipment();
        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            boolean hasOtherActiveWorkOrders = workOrderRepository.findByEquipmentId(equipment.getId())
                    .stream()
                    .filter(wo -> !wo.getId().equals(workOrderId))
                    .anyMatch(wo -> wo.getStatus() == WorkOrderStatus.IN_PROGRESS
                            || wo.getStatus() == WorkOrderStatus.WAITING_FOR_PARTS);

            if (!hasOtherActiveWorkOrders && equipment.getStatus() != EquipmentStatus.RETIRED) {
                // If equipment was OUT_OF_SERVICE prior to maintenance, or linked to an unresolved maintenance request, it remains OUT_OF_SERVICE
                if (equipment.getOperationalStatusReason() != null
                        && equipment.getOperationalStatusReason().contains("PREV_STATUS:OUT_OF_SERVICE")) {
                    equipment.setStatus(EquipmentStatus.OUT_OF_SERVICE);
                } else if (workOrder.getMaintenanceRequest() != null
                        && workOrder.getMaintenanceRequest().getStatus() != MaintenanceRequestStatus.RESOLVED) {
                    equipment.setStatus(EquipmentStatus.OUT_OF_SERVICE);
                } else if (equipment.getOperationalStatusReason() != null
                        && equipment.getOperationalStatusReason().contains("PREV_STATUS:AVAILABLE")) {
                    equipment.setStatus(EquipmentStatus.AVAILABLE);
                } else {
                    equipment.setStatus(EquipmentStatus.OUT_OF_SERVICE);
                }
                equipmentRepository.save(equipment);
            }
        }

        // Linked maintenance request check: ensure cancellation does not leave request stranded if all work orders are terminal
        if (workOrder.getMaintenanceRequest() != null
                && workOrder.getMaintenanceRequest().getStatus() == MaintenanceRequestStatus.WORK_ORDER_CREATED) {
            MaintenanceRequest request = workOrder.getMaintenanceRequest();
            List<MaintenanceWorkOrder> linkedOrders = workOrderRepository.findByMaintenanceRequestId(request.getId());
            boolean allTerminal = linkedOrders.isEmpty() || linkedOrders.stream()
                    .allMatch(wo -> {
                        WorkOrderStatus s = (wo.getId() != null && wo.getId().equals(workOrderId)) ? WorkOrderStatus.CANCELLED : wo.getStatus();
                        return s == WorkOrderStatus.COMPLETED || s == WorkOrderStatus.CANCELLED;
                    });

            if (allTerminal) {
                request.setStatus(MaintenanceRequestStatus.RESOLVED);
                requestRepository.save(request);
            }
        }

        return workOrderRepository.save(workOrder);
    }

    // ==========================================
    // Equipment Downtime Log Operations
    // ==========================================

    @Override
    @Transactional
    public EquipmentDowntimeLog recordDowntimeLog(EquipmentDowntimeLog log, Long equipmentId, Long workOrderId) {
        if (log == null) {
            throw new InvalidOperationException("Downtime log payload cannot be null");
        }
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID is required to record downtime log");
        }
        if (log.getDowntimeStart() == null) {
            throw new InvalidOperationException("Downtime start timestamp is required");
        }
        if (log.getReasonCategory() == null) {
            throw new InvalidOperationException("Reason category is required");
        }
        if (log.getDescription() == null || log.getDescription().trim().isEmpty()) {
            throw new InvalidOperationException("Description is required");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (equipment.getDeletedAt() != null) {
            throw new InvalidOperationException(String.format("Cannot record downtime on deleted equipment with id %d", equipmentId));
        }

        if (workOrderId != null) {
            MaintenanceWorkOrder workOrder = workOrderRepository.findById(workOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWorkOrder", "id", workOrderId));
            if (!workOrder.getEquipment().getId().equals(equipmentId)) {
                throw new InvalidOperationException(String.format(
                        "Downtime log equipment %d does not match work order equipment %d",
                        equipmentId, workOrder.getEquipment().getId()));
            }
            log.setWorkOrder(workOrder);
        }

        if (log.getDowntimeEnd() != null) {
            if (!log.getDowntimeStart().isBefore(log.getDowntimeEnd())) {
                throw new InvalidOperationException("Downtime start must be before downtime end");
            }
            long minutes = Duration.between(log.getDowntimeStart(), log.getDowntimeEnd()).toMinutes();
            if (log.getDurationMinutes() != null) {
                if (log.getDurationMinutes() <= 0) {
                    throw new InvalidOperationException("Duration minutes must be positive");
                }
                if (log.getDurationMinutes().longValue() != minutes) {
                    throw new InvalidOperationException(String.format(
                            "Explicit duration %d minutes does not match timestamp duration %d minutes",
                            log.getDurationMinutes(), minutes));
                }
            } else {
                log.setDurationMinutes((int) Math.max(1, minutes));
            }
        }

        log.setEquipment(equipment);
        return downtimeLogRepository.save(log);
    }

    @Override
    public EquipmentDowntimeLog getDowntimeLogById(Long id) {
        if (id == null) {
            throw new InvalidOperationException("Downtime log ID cannot be null");
        }
        EquipmentDowntimeLog log = downtimeLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentDowntimeLog", "id", id));
        initializeDowntimeLog(log);
        return log;
    }

    @Override
    public List<EquipmentDowntimeLog> listDowntimeLogs() {
        List<EquipmentDowntimeLog> list = downtimeLogRepository.findAll();
        list.forEach(this::initializeDowntimeLog);
        return list;
    }

    @Override
    public List<EquipmentDowntimeLog> listDowntimeLogsByEquipment(Long equipmentId) {
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        List<EquipmentDowntimeLog> list = downtimeLogRepository.findByEquipmentId(equipmentId);
        list.forEach(this::initializeDowntimeLog);
        return list;
    }

    @Override
    public List<EquipmentDowntimeLog> listDowntimeLogsByWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            throw new InvalidOperationException("Work order ID cannot be null");
        }
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new ResourceNotFoundException("MaintenanceWorkOrder", "id", workOrderId);
        }
        List<EquipmentDowntimeLog> list = downtimeLogRepository.findByWorkOrderId(workOrderId);
        list.forEach(this::initializeDowntimeLog);
        return list;
    }

    private void initializeDowntimeLog(EquipmentDowntimeLog log) {
        if (log != null) {
            if (log.getEquipment() != null) {
                try {
                    log.getEquipment().getName();
                } catch (Exception ignored) {
                }
            }
            if (log.getWorkOrder() != null) {
                try {
                    log.getWorkOrder().getWorkOrderNumber();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    @Transactional
    public EquipmentDowntimeLog endDowntimeLog(Long id, Instant downtimeEnd, Integer durationMinutes) {
        EquipmentDowntimeLog log = getDowntimeLogById(id);

        Instant end = downtimeEnd != null ? downtimeEnd : Instant.now();
        if (!log.getDowntimeStart().isBefore(end)) {
            throw new InvalidOperationException("Downtime end time must be after downtime start time");
        }

        long minutes = Duration.between(log.getDowntimeStart(), end).toMinutes();
        if (durationMinutes != null) {
            if (durationMinutes <= 0) {
                throw new InvalidOperationException("Duration minutes must be positive");
            }
            if (durationMinutes.longValue() != minutes) {
                throw new InvalidOperationException(String.format(
                        "Explicit duration %d minutes does not match timestamp duration %d minutes",
                        durationMinutes, minutes));
            }
            log.setDurationMinutes(durationMinutes);
        } else {
            log.setDurationMinutes((int) Math.max(1, minutes));
        }

        log.setDowntimeEnd(end);
        return downtimeLogRepository.save(log);
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private String generateRequestNumber() {
        return "MR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateWorkOrderNumber() {
        return "WO-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
