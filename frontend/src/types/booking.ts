export type BookingStatus =
  | 'PENDING_APPROVAL'
  | 'CONFIRMED'
  | 'IN_USE'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export type BookingBillingStatus =
  | 'UNBILLED'
  | 'INVOICED'
  | 'SETTLED'
  | 'WAIVED';

export interface BookingResponse {
  id: number;
  bookingReference: string;
  equipmentId: number;
  equipmentName: string;
  userId: number;
  userName: string;
  userEmail: string;
  departmentId: number;
  departmentName: string;
  institutionId: number;
  institutionName: string;
  sharedAllocationId: number | null;
  startTime: string; // ISO-8601 string from backend Instant
  endTime: string;   // ISO-8601 string from backend Instant
  status: BookingStatus;
  billingStatus: BookingBillingStatus;
  purpose: string | null;
  projectCode: string | null;
  approvedByUserId: number | null;
  approvedByUserName: string | null;
  approvedAt: string | null;
  rejectionReason: string | null;
  cancellationReason: string | null;
  cancelledAt: string | null;
  cancelledByUserId: number | null;
  cancelledByUserName: string | null;
  isExternalBooking: boolean;
  baseHourlyRate: number | null;
  estimatedCost: number | null;
  actualCost: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateBookingRequest {
  equipmentId: number;
  userId?: number | null;
  departmentId?: number | null;
  institutionId?: number | null;
  sharedAllocationId?: number | null;
  startTime: string; // ISO-8601 string (e.g. 2026-09-10T10:00:00Z)
  endTime: string;   // ISO-8601 string (e.g. 2026-09-10T12:00:00Z)
  purpose?: string | null;
  projectCode?: string | null;
  isExternalBooking?: boolean | null;
  baseHourlyRate?: number | null;
}

export interface ConfirmBookingRequest {
  approvedByUserId?: number | null;
}

export interface CancelBookingRequest {
  cancelledByUserId?: number | null;
  cancellationReason?: string | null;
}

export interface BookingFilterParams {
  equipmentId?: number;
  userId?: number;
  institutionId?: number;
  departmentId?: number;
  status?: BookingStatus;
}
