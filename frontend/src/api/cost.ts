import { apiClient } from './client';
import type {
  UsageCostResponse,
  DepartmentCostSummaryResponse,
  InvoiceResponse,
  CreateInvoiceRequest,
  UpdateInvoiceStatusRequest,
  AddInvoiceLineRequest,
  InvoiceFilterParams,
  UsageCostFilterParams,
  InvoiceStatus,
} from '../types/cost';

// ==========================================
// 1. Cost & Usage Endpoints
// ==========================================

export const getUsageCosts = async (
  params?: UsageCostFilterParams
): Promise<UsageCostResponse[]> => {
  const response = await apiClient.get<UsageCostResponse[]>('/cost/usage', { params });
  return response.data;
};

export const getDepartmentCostSummary = async (): Promise<DepartmentCostSummaryResponse[]> => {
  const response = await apiClient.get<DepartmentCostSummaryResponse[]>('/cost/department');
  return response.data;
};

// ==========================================
// 2. Invoicing Endpoints
// ==========================================

export const getInvoices = async (
  params?: InvoiceFilterParams
): Promise<InvoiceResponse[]> => {
  const response = await apiClient.get<InvoiceResponse[]>('/cost/invoices', { params });
  return response.data;
};

export const getInvoiceById = async (id: number | string): Promise<InvoiceResponse> => {
  const response = await apiClient.get<InvoiceResponse>(`/cost/invoices/${id}`);
  return response.data;
};

export const createInvoice = async (
  data: CreateInvoiceRequest
): Promise<InvoiceResponse> => {
  const response = await apiClient.post<InvoiceResponse>('/cost/invoices', data);
  return response.data;
};

export const updateInvoiceStatus = async (
  id: number | string,
  status: InvoiceStatus
): Promise<InvoiceResponse> => {
  const payload: UpdateInvoiceStatusRequest = { status };
  const response = await apiClient.patch<InvoiceResponse>(
    `/cost/invoices/${id}/status`,
    payload
  );
  return response.data;
};

export const addInvoiceLine = async (
  id: number | string,
  data: AddInvoiceLineRequest
): Promise<InvoiceResponse> => {
  const response = await apiClient.post<InvoiceResponse>(
    `/cost/invoices/${id}/lines`,
    data
  );
  return response.data;
};

export const deleteDraftInvoice = async (id: number | string): Promise<void> => {
  await apiClient.delete(`/cost/invoices/${id}`);
};

// ==========================================
// 3. Platform Integration Helpers
// ==========================================

export const getUnbilledBookings = async (
  departmentId?: number
): Promise<UsageCostResponse[]> => {
  const response = await apiClient.get<UsageCostResponse[]>('/cost/usage', {
    params: {
      billingStatus: 'UNBILLED',
      departmentId,
    },
  });
  return response.data;
};
