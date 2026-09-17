export type SessionStatus =
  | 'ACTIVE'
  | 'COMPLETED'
  | 'TERMINATED_EARLY'
  | 'AUTO_CLOSED';

export type IdleEventStatus =
  | 'ONGOING'
  | 'RESOLVED'
  | 'ACKNOWLEDGED';

export type IdleDetectionSource =
  | 'SCHEDULED_INSPECTION_CRON'
  | 'BOOKING_NO_SHOW'
  | 'MANUAL_LAB_AUDIT';

export interface UsageSessionResponse {
  id: number;
  bookingId: number | null;
  bookingReference: string | null;
  equipmentId: number;
  equipmentName: string;
  userId: number;
  userName: string;
  userEmail: string;
  checkedInAt: string;  // ISO-8601 string
  checkedOutAt: string | null;
  actualDurationMinutes: number | null;
  scheduledDurationMinutes: number | null;
  sessionStatus: SessionStatus;
  notes: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateUsageSessionRequest {
  equipmentId: number;
  userId?: number | null;
  bookingId?: number | null;
  departmentId?: number | null;
  institutionId?: number | null;
  checkedInAt?: string | null;
  checkedOutAt?: string | null;
  scheduledDurationMinutes?: number | null;
  actualDurationMinutes?: number | null;
  sessionStatus?: SessionStatus | null;
  notes?: string | null;
}

export interface CompleteUsageSessionRequest {
  checkedOutAt?: string | null;
  notes?: string | null;
}

export interface UtilizationRateResponse {
  equipmentId: number;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  utilizationPercentage: number;
}

export interface IdleEventResponse {
  id: number;
  equipmentId: number;
  equipmentName: string;
  bookingId: number | null;
  usageSessionId: number | null;
  detectionSource: IdleDetectionSource;
  idleStartTime: string; // ISO-8601 string
  idleEndTime: string | null;
  idleDurationMinutes: number | null;
  status: IdleEventStatus;
  loggedByUserId: number | null;
  loggedByUserName: string | null;
  notes: string | null;
  createdAt: string | null;
}

export interface RecordIdleEventRequest {
  equipmentId: number;
  bookingId?: number | null;
  usageSessionId?: number | null;
  detectionSource?: IdleDetectionSource | null;
  idleStartTime: string; // ISO-8601 string
  idleEndTime?: string | null;
  idleDurationMinutes?: number | null;
  loggedByUserId?: number | null;
  notes?: string | null;
}

export interface ResolveIdleEventRequest {
  idleEndTime?: string | null;
  notes?: string | null;
}

export interface UsageSessionFilterParams {
  equipmentId?: number;
  userId?: number;
  institutionId?: number;
  departmentId?: number;
  status?: SessionStatus;
}

export interface IdleEventFilterParams {
  equipmentId?: number;
  status?: IdleEventStatus;
  bookingId?: number;
  usageSessionId?: number;
}
