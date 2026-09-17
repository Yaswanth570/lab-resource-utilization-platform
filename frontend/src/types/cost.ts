import type { BookingBillingStatus } from './booking';

export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PAID' | 'SETTLED' | 'CANCELLED';

export interface InvoiceLineItemResponse {
  id: number;
  invoiceId: number;
  bookingId?: number | null;
  bookingReference?: string | null;
  description: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  createdAt: string; // ISO-8601
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  departmentId: number;
  departmentName?: string;
  institutionId?: number | null;
  institutionName?: string | null;
  billingPeriodStart: string; // YYYY-MM-DD
  billingPeriodEnd: string;   // YYYY-MM-DD
  status: InvoiceStatus;
  totalAmount: number;
  issuedAt?: string | null;   // ISO-8601
  paidAt?: string | null;     // ISO-8601
  createdAt: string;          // ISO-8601
  lineItems: InvoiceLineItemResponse[];
}

export interface UsageCostResponse {
  bookingId: number;
  bookingReference: string;
  equipmentId: number;
  equipmentName?: string;
  userId: number;
  userName?: string;
  departmentId: number;
  departmentName?: string;
  startTime: string; // ISO-8601
  endTime: string;   // ISO-8601
  billableHours: number;
  hourlyRate: number;
  totalCost: number;
  billingStatus: BookingBillingStatus;
  invoiceId?: number | null;
  invoiceNumber?: string | null;
}

export interface DepartmentCostSummaryResponse {
  departmentId: number;
  departmentName: string;
  bookingCount: number;
  totalHours: number;
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
}

export interface CreateInvoiceRequest {
  departmentId: number;
  institutionId?: number | null;
  billingPeriodStart: string; // YYYY-MM-DD
  billingPeriodEnd: string;   // YYYY-MM-DD
  bookingIds?: number[];
}

export interface UpdateInvoiceStatusRequest {
  status: InvoiceStatus;
}

export interface AddInvoiceLineRequest {
  bookingId?: number | null;
  description?: string;
  quantity?: number;
  unitPrice?: number;
}

export interface InvoiceFilterParams {
  departmentId?: number;
  institutionId?: number;
  status?: InvoiceStatus;
}

export interface UsageCostFilterParams {
  departmentId?: number;
  userId?: number;
  equipmentId?: number;
  billingStatus?: BookingBillingStatus;
  startDate?: string;
  endDate?: string;
}
