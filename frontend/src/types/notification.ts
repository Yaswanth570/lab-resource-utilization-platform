export type NotificationEventType =
  | 'BOOKING_PENDING'
  | 'BOOKING_APPROVED'
  | 'BOOKING_REJECTED'
  | 'BOOKING_CANCELLED'
  | 'BOOKING_NO_SHOW'
  | 'WAITLIST_AVAILABLE'
  | 'MAINTENANCE_SCHEDULED'
  | 'WORK_ORDER_ASSIGNED'
  | 'CALIBRATION_EXPIRING_SOON'
  | 'CALIBRATION_EXPIRED'
  | 'CERTIFICATION_EXPIRING_SOON'
  | 'CERTIFICATION_EXPIRED'
  | 'SHARING_REQUEST_RECEIVED'
  | 'SHARING_REQUEST_APPROVED'
  | 'IDLE_ALERT'
  | 'INVOICE_GENERATED';

export type NotificationPriority = 'INFO' | 'WARNING' | 'URGENT' | 'CRITICAL';

export interface NotificationResponse {
  id: number;
  userId: number;
  userEmail?: string;
  title: string;
  message: string;
  eventType: NotificationEventType;
  priority: NotificationPriority;
  isRead: boolean;
  readAt?: string | null; // ISO-8601
  relatedEntityType?: string | null;
  relatedEntityId?: number | null;
  createdAt: string; // ISO-8601
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export interface BatchUpdateResponse {
  updatedCount: number;
  message: string;
}

export interface NotificationFilterParams {
  isRead?: boolean;
  eventType?: NotificationEventType;
  priority?: NotificationPriority;
}
