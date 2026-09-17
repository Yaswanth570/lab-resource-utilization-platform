import { apiClient } from './client';
import type {
  MaintenanceRequestResponse,
  CreateMaintenanceRequestDto,
  TriageMaintenanceRequestDto,
  RejectMaintenanceRequestDto,
  ResolveMaintenanceRequestDto,
  WorkOrderResponse,
  CreateWorkOrderDto,
  AssignTechnicianDto,
  StartWorkOrderDto,
  PauseWorkOrderDto,
  CompleteWorkOrderDto,
  CancelWorkOrderDto,
  DowntimeLogResponse,
  RecordDowntimeLogDto,
  EndDowntimeLogDto,
  MaintenanceRequestFilterParams,
  WorkOrderFilterParams,
  DowntimeFilterParams,
} from '../types/maintenance';
import type { UserProfileResponse } from '../types/auth';

// ==========================================
// 1. Maintenance Requests API
// ==========================================

export const getMaintenanceRequests = async (
  params?: MaintenanceRequestFilterParams
): Promise<MaintenanceRequestResponse[]> => {
  const response = await apiClient.get<MaintenanceRequestResponse[]>(
    '/maintenance/requests',
    { params }
  );
  return response.data;
};

export const getMaintenanceRequestById = async (
  id: number | string
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.get<MaintenanceRequestResponse>(
    `/maintenance/requests/${id}`
  );
  return response.data;
};

export const getMaintenanceRequestByNumber = async (
  requestNumber: string
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.get<MaintenanceRequestResponse>(
    `/maintenance/requests/number/${encodeURIComponent(requestNumber)}`
  );
  return response.data;
};

export const createMaintenanceRequest = async (
  data: CreateMaintenanceRequestDto
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.post<MaintenanceRequestResponse>(
    '/maintenance/requests',
    data
  );
  return response.data;
};

export const triageMaintenanceRequest = async (
  id: number | string,
  data?: TriageMaintenanceRequestDto
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.patch<MaintenanceRequestResponse>(
    `/maintenance/requests/${id}/triage`,
    data
  );
  return response.data;
};

export const rejectMaintenanceRequest = async (
  id: number | string,
  data: RejectMaintenanceRequestDto
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.patch<MaintenanceRequestResponse>(
    `/maintenance/requests/${id}/reject`,
    data
  );
  return response.data;
};

export const resolveMaintenanceRequest = async (
  id: number | string,
  data: ResolveMaintenanceRequestDto
): Promise<MaintenanceRequestResponse> => {
  const response = await apiClient.patch<MaintenanceRequestResponse>(
    `/maintenance/requests/${id}/resolve`,
    data
  );
  return response.data;
};

// ==========================================
// 2. Work Orders API
// ==========================================

export const getWorkOrders = async (
  params?: WorkOrderFilterParams
): Promise<WorkOrderResponse[]> => {
  const response = await apiClient.get<WorkOrderResponse[]>(
    '/maintenance/work-orders',
    { params }
  );
  return response.data;
};

export const getWorkOrderById = async (
  id: number | string
): Promise<WorkOrderResponse> => {
  const response = await apiClient.get<WorkOrderResponse>(
    `/maintenance/work-orders/${id}`
  );
  return response.data;
};

export const getWorkOrderByNumber = async (
  number: string
): Promise<WorkOrderResponse> => {
  const response = await apiClient.get<WorkOrderResponse>(
    `/maintenance/work-orders/number/${encodeURIComponent(number)}`
  );
  return response.data;
};

export const createWorkOrder = async (
  data: CreateWorkOrderDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.post<WorkOrderResponse>(
    '/maintenance/work-orders',
    data
  );
  return response.data;
};

export const assignTechnician = async (
  id: number | string,
  data: AssignTechnicianDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/assign`,
    data
  );
  return response.data;
};

export const startWorkOrder = async (
  id: number | string,
  data?: StartWorkOrderDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/start`,
    data
  );
  return response.data;
};

export const pauseWorkOrder = async (
  id: number | string,
  data?: PauseWorkOrderDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/pause`,
    data
  );
  return response.data;
};

export const resumeWorkOrder = async (
  id: number | string
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/resume`
  );
  return response.data;
};

export const completeWorkOrder = async (
  id: number | string,
  data?: CompleteWorkOrderDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/complete`,
    data
  );
  return response.data;
};

export const cancelWorkOrder = async (
  id: number | string,
  data?: CancelWorkOrderDto
): Promise<WorkOrderResponse> => {
  const response = await apiClient.patch<WorkOrderResponse>(
    `/maintenance/work-orders/${id}/cancel`,
    data
  );
  return response.data;
};

// ==========================================
// 3. Equipment Downtime Logs API
// ==========================================

export const getDowntimeLogs = async (
  params?: DowntimeFilterParams
): Promise<DowntimeLogResponse[]> => {
  const response = await apiClient.get<DowntimeLogResponse[]>(
    '/maintenance/downtime',
    { params }
  );
  return response.data;
};

export const getDowntimeLogById = async (
  id: number | string
): Promise<DowntimeLogResponse> => {
  const response = await apiClient.get<DowntimeLogResponse>(
    `/maintenance/downtime/${id}`
  );
  return response.data;
};

export const recordDowntimeLog = async (
  data: RecordDowntimeLogDto
): Promise<DowntimeLogResponse> => {
  const response = await apiClient.post<DowntimeLogResponse>(
    '/maintenance/downtime',
    data
  );
  return response.data;
};

export const endDowntimeLog = async (
  id: number | string,
  data?: EndDowntimeLogDto
): Promise<DowntimeLogResponse> => {
  const response = await apiClient.patch<DowntimeLogResponse>(
    `/maintenance/downtime/${id}/end`,
    data
  );
  return response.data;
};

// ==========================================
// 4. User / Technician Helpers
// ==========================================

export const getDepartmentUsers = async (
  departmentId: number | string
): Promise<UserProfileResponse[]> => {
  const response = await apiClient.get<UserProfileResponse[]>(
    `/departments/${departmentId}/users`
  );
  return response.data;
};

export const getInstitutionUsers = async (
  institutionId: number | string
): Promise<UserProfileResponse[]> => {
  const response = await apiClient.get<UserProfileResponse[]>(
    `/institutions/${institutionId}/users`
  );
  return response.data;
};
