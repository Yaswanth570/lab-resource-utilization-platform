import { apiClient } from './client';
import type {
  NotificationResponse,
  UnreadCountResponse,
  BatchUpdateResponse,
  NotificationFilterParams,
} from '../types/notification';

// ==========================================
// Notification REST Endpoints
// ==========================================

export const getNotifications = async (
  params?: NotificationFilterParams
): Promise<NotificationResponse[]> => {
  const response = await apiClient.get<NotificationResponse[]>('/notifications', { params });
  return response.data;
};

export const getUnreadCount = async (): Promise<UnreadCountResponse> => {
  const response = await apiClient.get<UnreadCountResponse>('/notifications/unread-count');
  return response.data;
};

export const getNotificationById = async (
  id: number | string
): Promise<NotificationResponse> => {
  const response = await apiClient.get<NotificationResponse>(`/notifications/${id}`);
  return response.data;
};

export const markAsRead = async (
  id: number | string
): Promise<NotificationResponse> => {
  const response = await apiClient.patch<NotificationResponse>(`/notifications/${id}/read`);
  return response.data;
};

export const markAsUnread = async (
  id: number | string
): Promise<NotificationResponse> => {
  const response = await apiClient.patch<NotificationResponse>(`/notifications/${id}/unread`);
  return response.data;
};

export const markAllAsRead = async (): Promise<BatchUpdateResponse> => {
  const response = await apiClient.patch<BatchUpdateResponse>('/notifications/read-all');
  return response.data;
};

export const deleteNotification = async (
  id: number | string
): Promise<void> => {
  await apiClient.delete(`/notifications/${id}`);
};

export const notificationApi = {
  getNotifications,
  getUnreadCount,
  getNotificationById,
  markAsRead,
  markAsUnread,
  markAllAsRead,
  deleteNotification,
};
