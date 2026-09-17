import { apiClient } from './client';
import type {
  EquipmentResponse,
  CreateEquipmentRequest,
  UpdateEquipmentRequest,
  UpdateEquipmentStatusRequest,
  EquipmentCategoryResponse,
  EquipmentSpecificationResponse,
  CreateEquipmentSpecificationRequest,
  UpdateEquipmentSpecificationRequest,
  QualificationResponse,
  InstitutionLookup,
  DepartmentLookup,
} from '../types/equipment';

// Equipment Catalog CRUD
export const getEquipmentList = async (): Promise<EquipmentResponse[]> => {
  const response = await apiClient.get<EquipmentResponse[]>('/equipment');
  return response.data;
};

export const getEquipmentById = async (id: number | string): Promise<EquipmentResponse> => {
  const response = await apiClient.get<EquipmentResponse>(`/equipment/${id}`);
  return response.data;
};

export const createEquipment = async (data: CreateEquipmentRequest): Promise<EquipmentResponse> => {
  const response = await apiClient.post<EquipmentResponse>('/equipment', data);
  return response.data;
};

export const updateEquipment = async (
  id: number | string,
  data: UpdateEquipmentRequest
): Promise<EquipmentResponse> => {
  const response = await apiClient.put<EquipmentResponse>(`/equipment/${id}`, data);
  return response.data;
};

export const updateEquipmentStatus = async (
  id: number | string,
  data: UpdateEquipmentStatusRequest
): Promise<EquipmentResponse> => {
  const response = await apiClient.patch<EquipmentResponse>(`/equipment/${id}/status`, data);
  return response.data;
};

// Categories
export const getCategories = async (): Promise<EquipmentCategoryResponse[]> => {
  const response = await apiClient.get<EquipmentCategoryResponse[]>('/equipment-categories');
  return response.data;
};

// Technical Specifications
export const getSpecifications = async (
  equipmentId: number | string
): Promise<EquipmentSpecificationResponse[]> => {
  const response = await apiClient.get<EquipmentSpecificationResponse[]>(
    `/equipment/${equipmentId}/specifications`
  );
  return response.data;
};

export const createSpecification = async (
  equipmentId: number | string,
  data: CreateEquipmentSpecificationRequest
): Promise<EquipmentSpecificationResponse> => {
  const response = await apiClient.post<EquipmentSpecificationResponse>(
    `/equipment/${equipmentId}/specifications`,
    data
  );
  return response.data;
};

export const updateSpecification = async (
  id: number | string,
  data: UpdateEquipmentSpecificationRequest
): Promise<EquipmentSpecificationResponse> => {
  const response = await apiClient.put<EquipmentSpecificationResponse>(
    `/specifications/${id}`,
    data
  );
  return response.data;
};

export const deleteSpecification = async (id: number | string): Promise<void> => {
  await apiClient.delete(`/specifications/${id}`);
};

// Qualifications (Read-only on Equipment)
export const getQualifications = async (
  equipmentId: number | string
): Promise<QualificationResponse[]> => {
  const response = await apiClient.get<QualificationResponse[]>(
    `/qualifications?equipmentId=${equipmentId}`
  );
  return response.data;
};

// Lookups for Forms
export const getInstitutions = async (): Promise<InstitutionLookup[]> => {
  const response = await apiClient.get<InstitutionLookup[]>('/institutions');
  return response.data;
};

export const getDepartmentsByInstitution = async (
  institutionId: number | string
): Promise<DepartmentLookup[]> => {
  const response = await apiClient.get<DepartmentLookup[]>(
    `/institutions/${institutionId}/departments`
  );
  return response.data;
};
