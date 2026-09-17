export interface AuthUser {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  institutionId?: number | null;
  departmentId?: number | null;
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  institutionId?: number | null;
  departmentId?: number | null;
  roles: string[];
}

export interface UserProfileResponse {
  id: number;
  institutionId: number;
  institutionName?: string;
  departmentId?: number | null;
  departmentName?: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  status: string;
  roles: string[];
  lastLoginAt?: string | null;
  createdAt: string;
  updatedAt: string;
  verified: boolean;
}

export interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
}
