import { apiClient } from './client';
import type {
  AnalyticsFilterParams,
  AnalyticsOverviewResponse,
  UtilizationAnalyticsResponse,
  EquipmentUtilizationDetailResponse,
  BookingAnalyticsResponse,
  MaintenanceAnalyticsResponse,
  CostAnalyticsResponse,
  EquipmentPerformanceResponse,
  TrendAnalyticsResponse,
} from '../types/analytics';

// ==========================================
// Analytics REST API Client
// ==========================================

export const getOverview = async (
  params?: AnalyticsFilterParams
): Promise<AnalyticsOverviewResponse> => {
  const response = await apiClient.get<AnalyticsOverviewResponse>('/analytics/overview', { params });
  return response.data;
};

export const getUtilizationAnalytics = async (
  params?: AnalyticsFilterParams
): Promise<UtilizationAnalyticsResponse> => {
  const response = await apiClient.get<UtilizationAnalyticsResponse>('/analytics/utilization', { params });
  return response.data;
};

export const getEquipmentUtilization = async (
  equipmentId: number | string,
  params?: AnalyticsFilterParams
): Promise<EquipmentUtilizationDetailResponse> => {
  const response = await apiClient.get<EquipmentUtilizationDetailResponse>(
    `/analytics/utilization/equipment/${equipmentId}`,
    { params }
  );
  return response.data;
};

export const getBookingAnalytics = async (
  params?: AnalyticsFilterParams
): Promise<BookingAnalyticsResponse> => {
  const response = await apiClient.get<BookingAnalyticsResponse>('/analytics/bookings', { params });
  return response.data;
};

export const getMaintenanceAnalytics = async (
  params?: AnalyticsFilterParams
): Promise<MaintenanceAnalyticsResponse> => {
  const response = await apiClient.get<MaintenanceAnalyticsResponse>('/analytics/maintenance', { params });
  return response.data;
};

export const getCostAnalytics = async (
  params?: AnalyticsFilterParams
): Promise<CostAnalyticsResponse> => {
  const response = await apiClient.get<CostAnalyticsResponse>('/analytics/cost', { params });
  return response.data;
};

export const getEquipmentPerformance = async (
  params?: AnalyticsFilterParams
): Promise<EquipmentPerformanceResponse> => {
  const response = await apiClient.get<EquipmentPerformanceResponse>('/analytics/equipment-performance', { params });
  return response.data;
};

export const getTrends = async (
  params?: AnalyticsFilterParams
): Promise<TrendAnalyticsResponse> => {
  const response = await apiClient.get<TrendAnalyticsResponse>('/analytics/trends', { params });
  return response.data;
};

export const analyticsApi = {
  getOverview,
  getUtilizationAnalytics,
  getEquipmentUtilization,
  getBookingAnalytics,
  getMaintenanceAnalytics,
  getCostAnalytics,
  getEquipmentPerformance,
  getTrends,
};
