import { apiClient } from './client';
import type {
  UsageSessionResponse,
  CreateUsageSessionRequest,
  CompleteUsageSessionRequest,
  UtilizationRateResponse,
  IdleEventResponse,
  RecordIdleEventRequest,
  ResolveIdleEventRequest,
  UsageSessionFilterParams,
  IdleEventFilterParams,
} from '../types/utilization';

// --- Usage Sessions ---

export const getUsageSessions = async (
  params?: UsageSessionFilterParams
): Promise<UsageSessionResponse[]> => {
  const response = await apiClient.get<UsageSessionResponse[]>(
    '/utilization/sessions',
    { params }
  );
  return response.data;
};

export const getUsageSessionById = async (
  id: number | string
): Promise<UsageSessionResponse> => {
  const response = await apiClient.get<UsageSessionResponse>(
    `/utilization/sessions/${id}`
  );
  return response.data;
};

export const getUsageSessionByBooking = async (
  bookingId: number | string
): Promise<UsageSessionResponse> => {
  const response = await apiClient.get<UsageSessionResponse>(
    `/utilization/sessions/booking/${bookingId}`
  );
  return response.data;
};

export const createUsageSession = async (
  data: CreateUsageSessionRequest
): Promise<UsageSessionResponse> => {
  const response = await apiClient.post<UsageSessionResponse>(
    '/utilization/sessions',
    data
  );
  return response.data;
};

export const completeUsageSession = async (
  id: number | string,
  data?: CompleteUsageSessionRequest
): Promise<UsageSessionResponse> => {
  const response = await apiClient.patch<UsageSessionResponse>(
    `/utilization/sessions/${id}/complete`,
    data
  );
  return response.data;
};

export const terminateUsageSessionEarly = async (
  id: number | string,
  data?: CompleteUsageSessionRequest
): Promise<UsageSessionResponse> => {
  const response = await apiClient.patch<UsageSessionResponse>(
    `/utilization/sessions/${id}/terminate-early`,
    data
  );
  return response.data;
};

// --- Utilization Rate Calculation ---

export const getUtilizationRate = async (
  equipmentId: number | string,
  startDate: string,
  endDate: string
): Promise<UtilizationRateResponse> => {
  const response = await apiClient.get<UtilizationRateResponse>(
    '/utilization/rate',
    {
      params: {
        equipmentId,
        startDate,
        endDate,
      },
    }
  );
  return response.data;
};

// --- Idle Events ---

export const getIdleEvents = async (
  params?: IdleEventFilterParams
): Promise<IdleEventResponse[]> => {
  const response = await apiClient.get<IdleEventResponse[]>(
    '/utilization/idle-events',
    { params }
  );
  return response.data;
};

export const getIdleEventById = async (
  id: number | string
): Promise<IdleEventResponse> => {
  const response = await apiClient.get<IdleEventResponse>(
    `/utilization/idle-events/${id}`
  );
  return response.data;
};

export const recordIdleEvent = async (
  data: RecordIdleEventRequest
): Promise<IdleEventResponse> => {
  const response = await apiClient.post<IdleEventResponse>(
    '/utilization/idle-events',
    data
  );
  return response.data;
};

export const resolveIdleEvent = async (
  id: number | string,
  data?: ResolveIdleEventRequest
): Promise<IdleEventResponse> => {
  const response = await apiClient.patch<IdleEventResponse>(
    `/utilization/idle-events/${id}/resolve`,
    data
  );
  return response.data;
};
