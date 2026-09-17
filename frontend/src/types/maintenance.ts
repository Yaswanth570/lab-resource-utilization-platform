export type MaintenanceRequestStatus =
  | 'SUBMITTED'
  | 'TRIAGED'
  | 'WORK_ORDER_CREATED'
  | 'RESOLVED'
  | 'REJECTED';

export type WorkOrderStatus =
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'WAITING_FOR_PARTS'
  | 'COMPLETED'
  | 'CANCELLED';

export type MaintenancePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type WorkOrderType =
  | 'CORRECTIVE'
  | 'PREVENTIVE'
  | 'EMERGENCY'
  | 'OVERHAUL'
  | 'DECOMMISSION';

export type DowntimeReasonCategory =
  | 'UNSCHEDULED_BREAKDOWN'
  | 'SCHEDULED_MAINTENANCE'
  | 'CALIBRATION'
  | 'FACILITY_OUTAGE'
  | 'SAFETY_HOLD';

export interface MaintenanceRequestResponse {
  id: number;
  requestNumber: string;
  equipmentId?: number;
  equipmentName?: string;
  reportedByUserId?: number;
  reportedByUserName?: string;
  priority: MaintenancePriority;
  issueTitle: string;
  issueDescription: string;
  status: MaintenanceRequestStatus;
  triagedByUserId?: number;
  triagedByUserName?: string;
  triagedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMaintenanceRequestDto {
  equipmentId: number;
  reportedByUserId?: number;
  institutionId?: number;
  departmentId?: number;
  priority?: MaintenancePriority;
  issueTitle: string;
  issueDescription: string;
}

export interface TriageMaintenanceRequestDto {
  triagedByUserId?: number;
  priority?: MaintenancePriority;
}

export interface RejectMaintenanceRequestDto {
  triagedByUserId?: number;
  reason: string;
}

export interface ResolveMaintenanceRequestDto {
  resolutionNotes: string;
}

export interface WorkOrderResponse {
  id: number;
  workOrderNumber: string;
  maintenanceRequestId?: number;
  maintenanceRequestNumber?: string;
  equipmentId?: number;
  equipmentName?: string;
  assignedTechnicianId?: number;
  assignedTechnicianName?: string;
  type: WorkOrderType;
  priority: MaintenancePriority;
  status: WorkOrderStatus;
  scheduledStart: string;
  scheduledEnd: string;
  actualStart?: string;
  actualEnd?: string;
  laborHours?: number;
  laborCost?: number;
  partsCost?: number;
  totalCost?: number;
  workPerformedSummary?: string;
  failureRootCause?: string;
  resolutionNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWorkOrderDto {
  equipmentId: number;
  maintenanceRequestId?: number;
  assignedTechnicianId?: number;
  type: WorkOrderType;
  priority?: MaintenancePriority;
  scheduledStart: string;
  scheduledEnd: string;
  workOrderNumber?: string;
}

export interface AssignTechnicianDto {
  technicianId: number;
}

export interface StartWorkOrderDto {
  actualStart?: string;
}

export interface PauseWorkOrderDto {
  reason?: string;
}

export interface CompleteWorkOrderDto {
  actualEnd?: string;
  laborHours?: number;
  laborCost?: number;
  partsCost?: number;
  workPerformedSummary?: string;
  failureRootCause?: string;
  resolutionNotes?: string;
}

export interface CancelWorkOrderDto {
  cancellationReason?: string;
}

export interface DowntimeLogResponse {
  id: number;
  equipmentId?: number;
  equipmentName?: string;
  workOrderId?: number;
  workOrderNumber?: string;
  reasonCategory: DowntimeReasonCategory;
  downtimeStart: string;
  downtimeEnd?: string;
  durationMinutes?: number;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface RecordDowntimeLogDto {
  equipmentId: number;
  workOrderId?: number;
  reasonCategory: DowntimeReasonCategory;
  downtimeStart: string;
  downtimeEnd?: string;
  durationMinutes?: number;
  description: string;
}

export interface EndDowntimeLogDto {
  downtimeEnd?: string;
  durationMinutes?: number;
}

export interface MaintenanceRequestFilterParams {
  equipmentId?: number;
  reportedByUserId?: number;
  status?: MaintenanceRequestStatus;
  institutionId?: number;
  departmentId?: number;
}

export interface WorkOrderFilterParams {
  equipmentId?: number;
  technicianId?: number;
  maintenanceRequestId?: number;
  status?: WorkOrderStatus;
  institutionId?: number;
  departmentId?: number;
}

export interface DowntimeFilterParams {
  equipmentId?: number;
  workOrderId?: number;
}
