import { apiClient } from './client';
import type {
  ResourceSharingAgreementResponse,
  CreateSharingAgreementRequest,
  SharedEquipmentAllocationResponse,
  CreateSharedAllocationRequest,
  SharingAgreementStatus,
  SharingAgreementFilterParams,
  SharedAllocationFilterParams,
} from '../types/sharing';
import type { EquipmentResponse, InstitutionLookup } from '../types/equipment';
import type { BookingResponse } from '../types/booking';

// ==========================================
// 1. Sharing Agreements API
// ==========================================

export const getSharingAgreements = async (
  params?: SharingAgreementFilterParams
): Promise<ResourceSharingAgreementResponse[]> => {
  const response = await apiClient.get<ResourceSharingAgreementResponse[]>(
    '/sharing/agreements',
    { params }
  );
  return response.data;
};

export const getSharingAgreementById = async (
  id: number | string
): Promise<ResourceSharingAgreementResponse> => {
  const response = await apiClient.get<ResourceSharingAgreementResponse>(
    `/sharing/agreements/${id}`
  );
  return response.data;
};

export const createSharingAgreement = async (
  data: CreateSharingAgreementRequest
): Promise<ResourceSharingAgreementResponse> => {
  const response = await apiClient.post<ResourceSharingAgreementResponse>(
    '/sharing/agreements',
    data
  );
  return response.data;
};

export const updateAgreementStatus = async (
  id: number | string,
  status: SharingAgreementStatus
): Promise<ResourceSharingAgreementResponse> => {
  const response = await apiClient.patch<ResourceSharingAgreementResponse>(
    `/sharing/agreements/${id}/status`,
    { status }
  );
  return response.data;
};

// ==========================================
// 2. Shared Equipment Allocations API
// ==========================================

export const getSharedAllocations = async (
  params?: SharedAllocationFilterParams
): Promise<SharedEquipmentAllocationResponse[]> => {
  const response = await apiClient.get<SharedEquipmentAllocationResponse[]>(
    '/sharing/allocations',
    { params }
  );
  return response.data;
};

export const getSharedAllocationById = async (
  id: number | string
): Promise<SharedEquipmentAllocationResponse> => {
  const response = await apiClient.get<SharedEquipmentAllocationResponse>(
    `/sharing/allocations/${id}`
  );
  return response.data;
};

export const createSharedAllocation = async (
  data: CreateSharedAllocationRequest
): Promise<SharedEquipmentAllocationResponse> => {
  const response = await apiClient.post<SharedEquipmentAllocationResponse>(
    '/sharing/allocations',
    data
  );
  return response.data;
};

export const toggleAllocationActive = async (
  id: number | string,
  isActive: boolean
): Promise<SharedEquipmentAllocationResponse> => {
  const response = await apiClient.patch<SharedEquipmentAllocationResponse>(
    `/sharing/allocations/${id}/active`,
    { isActive }
  );
  return response.data;
};

// ==========================================
// 3. Integration with Existing Platform APIs
// ==========================================

export const getExternallyShareableEquipment = async (): Promise<EquipmentResponse[]> => {
  const response = await apiClient.get<EquipmentResponse[]>('/equipment');
  return response.data.filter((item) => item.isShareableExternally);
};

export const getExternalBookings = async (): Promise<BookingResponse[]> => {
  const response = await apiClient.get<BookingResponse[]>('/bookings');
  return response.data.filter(
    (b) => b.isExternalBooking || (b.sharedAllocationId !== null && b.sharedAllocationId !== undefined)
  );
};

export const getInstitutions = async (): Promise<InstitutionLookup[]> => {
  const response = await apiClient.get<InstitutionLookup[]>('/institutions');
  return response.data;
};
