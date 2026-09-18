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

export interface RegisterRequest {
  institutionId?: number | null;
  departmentId?: number | null;
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phone?: string;
  requestedRole?: string;
}

export interface RegisterResponse {
  id: number;
  institutionId: number;
  departmentId?: number | null;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  status: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
  verified: boolean;
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

export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  phone?: string;
  institutionId?: number | null;
  departmentId?: number | null;
}

export interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  updateUser?: (updated: Partial<AuthUser>) => void;
}
