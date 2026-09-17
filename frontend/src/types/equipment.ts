export type EquipmentStatus =
  | 'AVAILABLE'
  | 'IN_USE'
  | 'UNDER_MAINTENANCE'
  | 'OUT_OF_SERVICE'
  | 'RETIRED';

export interface EquipmentResponse {
  id: number;
  institutionId: number;
  departmentId: number;
  categoryId: number;
  categoryName: string;
  primaryLabManagerId: number | null;
  primaryLabManagerName: string | null;
  name: string;
  assetTag: string;
  serialNumber: string;
  modelNumber: string | null;
  manufacturer: string | null;
  locationBuilding: string;
  locationRoom: string;
  status: EquipmentStatus;
  operationalStatusReason: string | null;
  isShareableExternally: boolean;
  hourlyRateInternal: number | null;
  hourlyRateExternal: number | null;
  minBookingDurationMins: number | null;
  maxBookingDurationMins: number | null;
  bufferTimeMins: number | null;
  requiresTrainingCertification: boolean;
  requiresApproval: boolean;
  purchaseDate: string | null;
  purchaseCost: number | null;
  warrantyExpiryDate: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface CreateEquipmentRequest {
  institutionId: number;
  departmentId: number;
  categoryId: number;
  primaryLabManagerId?: number | null;
  name: string;
  assetTag: string;
  serialNumber: string;
  modelNumber?: string | null;
  manufacturer?: string | null;
  locationBuilding: string;
  locationRoom: string;
  shareableExternally?: boolean;
  hourlyRateInternal?: number | null;
  hourlyRateExternal?: number | null;
  minBookingDurationMins?: number | null;
  maxBookingDurationMins?: number | null;
  bufferTimeMins?: number | null;
  requiresTrainingCertification?: boolean;
  requiresApproval?: boolean;
  purchaseDate?: string | null;
  purchaseCost?: number | null;
  warrantyExpiryDate?: string | null;
}

export interface UpdateEquipmentRequest {
  departmentId?: number;
  categoryId?: number;
  primaryLabManagerId?: number | null;
  name?: string;
  modelNumber?: string | null;
  manufacturer?: string | null;
  locationBuilding?: string;
  locationRoom?: string;
  shareableExternally?: boolean;
  hourlyRateInternal?: number | null;
  hourlyRateExternal?: number | null;
  minBookingDurationMins?: number | null;
  maxBookingDurationMins?: number | null;
  bufferTimeMins?: number | null;
  requiresTrainingCertification?: boolean;
  requiresApproval?: boolean;
  purchaseDate?: string | null;
  purchaseCost?: number | null;
  warrantyExpiryDate?: string | null;
}

export interface UpdateEquipmentStatusRequest {
  status: EquipmentStatus;
  reason?: string;
}

export interface EquipmentCategoryResponse {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EquipmentSpecificationResponse {
  id: number;
  equipmentId: number;
  specName: string;
  specValue: string;
  unit?: string | null;
}

export interface CreateEquipmentSpecificationRequest {
  specName: string;
  specValue: string;
  unit?: string | null;
}

export interface UpdateEquipmentSpecificationRequest {
  specName?: string;
  specValue?: string;
  unit?: string | null;
}

export interface QualificationResponse {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  equipmentId: number;
  equipmentName: string;
  certifiedByUserId?: number | null;
  certifiedByUserName?: string | null;
  certifiedAt?: string | null;
  expiresAt?: string | null;
  status: string;
  notes?: string | null;
}

export interface InstitutionLookup {
  id: number;
  name: string;
  code: string;
}

export interface DepartmentLookup {
  id: number;
  institutionId: number;
  name: string;
  code: string;
}
