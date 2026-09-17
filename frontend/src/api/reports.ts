import { apiClient } from './client';
import type {
  ReportFilterParams,
  ReportTypeDefinition,
  UtilizationReportResponse,
  BookingReportResponse,
  MaintenanceReportResponse,
  CostReportResponse,
  EquipmentInventoryReportResponse,
  ManagementSummaryReportResponse,
} from '../types/report';

// ==========================================
// Reports REST API Client
// ==========================================

export const listReports = async (): Promise<ReportTypeDefinition[]> => {
  const response = await apiClient.get<ReportTypeDefinition[]>('/reports');
  return response.data;
};

export const getUtilizationReport = async (
  params?: ReportFilterParams
): Promise<UtilizationReportResponse> => {
  const response = await apiClient.get<UtilizationReportResponse>('/reports/utilization', { params });
  return response.data;
};

export const getBookingReport = async (
  params?: ReportFilterParams
): Promise<BookingReportResponse> => {
  const response = await apiClient.get<BookingReportResponse>('/reports/bookings', { params });
  return response.data;
};

export const getMaintenanceReport = async (
  params?: ReportFilterParams
): Promise<MaintenanceReportResponse> => {
  const response = await apiClient.get<MaintenanceReportResponse>('/reports/maintenance', { params });
  return response.data;
};

export const getCostReport = async (
  params?: ReportFilterParams
): Promise<CostReportResponse> => {
  const response = await apiClient.get<CostReportResponse>('/reports/cost', { params });
  return response.data;
};

export const getEquipmentReport = async (
  params?: ReportFilterParams
): Promise<EquipmentInventoryReportResponse> => {
  const response = await apiClient.get<EquipmentInventoryReportResponse>('/reports/equipment', { params });
  return response.data;
};

export const getManagementReport = async (
  params?: ReportFilterParams
): Promise<ManagementSummaryReportResponse> => {
  const response = await apiClient.get<ManagementSummaryReportResponse>('/reports/management', { params });
  return response.data;
};

/**
 * Downloads the report CSV export directly from the backend.
 */
export const downloadReportCsv = async (
  reportType: string,
  params?: ReportFilterParams
): Promise<void> => {
  const response = await apiClient.get(`/reports/${reportType}/csv`, {
    params,
    responseType: 'blob',
  });

  // Extract filename from Content-Disposition header if available
  const disposition = response.headers['content-disposition'];
  let filename = `${reportType}-report.csv`;
  if (disposition && disposition.indexOf('filename=') !== -1) {
    const filenameMatch = disposition.match(/filename="?([^";]+)"?/);
    if (filenameMatch && filenameMatch[1]) {
      filename = filenameMatch[1];
    }
  }

  // Create blob link to download
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

export const reportsApi = {
  listReports,
  getUtilizationReport,
  getBookingReport,
  getMaintenanceReport,
  getCostReport,
  getEquipmentReport,
  getManagementReport,
  downloadReportCsv,
};
