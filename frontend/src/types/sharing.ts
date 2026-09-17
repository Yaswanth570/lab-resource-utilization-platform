import type { EquipmentStatus } from './equipment';

export type SharingAgreementStatus = 'ACTIVE' | 'SUSPENDED' | 'TERMINATED' | 'EXPIRED';

export type SharingRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXPIRED' | 'REVOKED';

export interface ResourceSharingAgreementResponse {
  id: number;
  agreementCode: string;
  sharingRequestId?: number | null;
  requestingInstitutionId: number;
  requestingInstitutionName?: string;
  ownerInstitutionId: number;
  ownerInstitutionName?: string;
  billingRateMultiplier: number;
  maxMonthlyHours?: number | null;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  status: SharingAgreementStatus;
  createdAt: string; // ISO-8601
  updatedAt: string; // ISO-8601
}

export interface CreateSharingAgreementRequest {
  sharingRequestId?: number | null;
  agreementCode?: string;
  requestingInstitutionId: number;
  ownerInstitutionId: number;
  billingRateMultiplier?: number;
  maxMonthlyHours?: number | null;
  startDate: string;
  endDate: string;
  status?: SharingAgreementStatus;
}

export interface SharedEquipmentAllocationResponse {
  id: number;
  sharingAgreementId: number;
  sharingAgreementCode?: string;
  equipmentId: number;
  equipmentName?: string;
  equipmentAssetTag?: string;
  equipmentCategoryName?: string;
  equipmentStatus?: EquipmentStatus;
  ownerInstitutionId?: number;
  ownerInstitutionName?: string;
  requestingInstitutionId?: number;
  requestingInstitutionName?: string;
  customHourlyRate?: number | null;
  isActive: boolean;
  createdAt: string; // ISO-8601
}

export interface CreateSharedAllocationRequest {
  sharingAgreementId: number;
  equipmentId: number;
  customHourlyRate?: number | null;
  isActive?: boolean;
}

export interface ResourceSharingRequestResponse {
  id: number;
  requestingInstitutionId: number;
  requestingInstitutionName?: string;
  ownerInstitutionId: number;
  ownerInstitutionName?: string;
  requestedByUserId: number;
  requestedByUserName?: string;
  status: SharingRequestStatus;
  requestedStartDate: string;
  requestedEndDate: string;
  purpose: string;
  reviewedByUserId?: number | null;
  reviewedByUserName?: string | null;
  reviewedAt?: string | null;
  rejectionReason?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SharingAgreementFilterParams {
  status?: SharingAgreementStatus;
  ownerInstitutionId?: number;
  requestingInstitutionId?: number;
}

export interface SharedAllocationFilterParams {
  sharingAgreementId?: number;
  equipmentId?: number;
  isActive?: boolean;
}
