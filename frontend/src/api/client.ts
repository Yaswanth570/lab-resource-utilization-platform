import axios from 'axios';
import type { LoginRequest, LoginResponse, UserProfileResponse } from '../types/auth';

export interface HealthResponse {
  status: string;
  service: string;
}

const AUTH_STORAGE_KEY = 'lab_resource_auth_token';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
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
      // Don't trigger auto-logout if the 401 was from the login endpoint itself
      const isLoginRequest = error.config?.url?.includes('/auth/login');
      if (!isLoginRequest) {
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

export const getCurrentUserProfile = async (): Promise<UserProfileResponse> => {
  const response = await apiClient.get<UserProfileResponse>('/users/me');
  return response.data;
};

export { AUTH_STORAGE_KEY };
export default apiClient;
