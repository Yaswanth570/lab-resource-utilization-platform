import axios from 'axios';
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, UserProfileResponse, UpdateProfilePayload } from '../types/auth';

export interface HealthResponse {
  status: string;
  service: string;
}

import type { InstitutionLookup, DepartmentLookup } from '../types/equipment';

export type PublicInstitutionLookup = InstitutionLookup;
export type PublicDepartmentLookup = DepartmentLookup;
export type { InstitutionLookup, DepartmentLookup };

const AUTH_STORAGE_KEY = 'lab_resource_auth_token';

const getBaseUrl = (): string => {
  let url = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() || '/api';
  if (url.endsWith('/')) {
    url = url.slice(0, -1);
  }
  if ((url.startsWith('http://') || url.startsWith('https://')) && !url.endsWith('/api')) {
    url = `${url}/api`;
  }
  return url;
};

export const apiClient = axios.create({
  baseURL: getBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach Authorization header if token exists
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(AUTH_STORAGE_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401 Unauthorized centrally
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Don't trigger auto-logout if the 401 was from public endpoints (auth, institutions, health)
      const url = error.config?.url || '';
      const isPublicRequest =
        url.includes('/auth/') ||
        url.includes('/institutions') ||
        url.includes('/health');
      if (!isPublicRequest) {
        localStorage.removeItem(AUTH_STORAGE_KEY);
        localStorage.removeItem('lab_resource_auth_user');
        window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      }
    }
    return Promise.reject(error);
  }
);

export const getHealth = async (): Promise<HealthResponse> => {
  const response = await apiClient.get<HealthResponse>('/health');
  return response.data;
};

export const loginApi = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
  return response.data;
};

export const registerApi = async (data: RegisterRequest): Promise<RegisterResponse> => {
  const response = await apiClient.post<RegisterResponse>('/auth/register', data);
  return response.data;
};

export const getInstitutions = async (): Promise<PublicInstitutionLookup[]> => {
  // Strategy 1: Try canonical /institutions?active=true
  try {
    const res = await apiClient.get<PublicInstitutionLookup[]>('/institutions', {
      params: { active: true },
    });
    if (Array.isArray(res.data)) {
      return res.data;
    }
  } catch (err) {
    console.warn('GET /institutions failed, attempting /auth/institutions fallback...', err);
  }

  // Strategy 2: Fallback to /auth/institutions
  try {
    const authRes = await apiClient.get<PublicInstitutionLookup[]>('/auth/institutions');
    if (Array.isArray(authRes.data)) {
      return authRes.data;
    }
  } catch (authErr) {
    console.warn('All institution retrieval endpoints failed:', authErr);
  }

  return [];
};

export const getDepartmentsByInstitution = async (
  institutionId: number | string
): Promise<PublicDepartmentLookup[]> => {
  if (!institutionId) return [];

  // Strategy 1: Try canonical /institutions/{id}/departments?active=true
  try {
    const res = await apiClient.get<PublicDepartmentLookup[]>(
      `/institutions/${institutionId}/departments`,
      { params: { active: true } }
    );
    if (Array.isArray(res.data)) {
      return res.data;
    }
  } catch (err) {
    console.warn(
      `GET /institutions/${institutionId}/departments failed, attempting /auth fallback...`,
      err
    );
  }

  // Strategy 2: Fallback to /auth/institutions/{id}/departments
  try {
    const authRes = await apiClient.get<PublicDepartmentLookup[]>(
      `/auth/institutions/${institutionId}/departments`
    );
    if (Array.isArray(authRes.data)) {
      return authRes.data;
    }
  } catch (authErr) {
    console.warn(
      `Department fallback endpoint not available for institution ${institutionId}:`,
      authErr
    );
  }

  return [];
};

// Public aliases
export const getPublicInstitutions = getInstitutions;
export const getPublicDepartments = getDepartmentsByInstitution;

export const getCurrentUserProfile = async (): Promise<UserProfileResponse> => {
  const response = await apiClient.get<UserProfileResponse>('/users/me');
  return response.data;
};

export const updateUserProfile = async (
  payload: UpdateProfilePayload
): Promise<UserProfileResponse> => {
  const response = await apiClient.put<UserProfileResponse>('/users/me', payload);
  return response.data;
};

export { AUTH_STORAGE_KEY };
export default apiClient;
