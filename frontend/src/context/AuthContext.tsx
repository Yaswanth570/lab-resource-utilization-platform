import React, { createContext, useState, useEffect, useCallback } from 'react';
import type { AuthContextType, AuthUser, LoginRequest } from '../types/auth';
import { loginApi, AUTH_STORAGE_KEY } from '../api/client';

const USER_STORAGE_KEY = 'lab_resource_auth_user';

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(AUTH_STORAGE_KEY));
  const [user, setUser] = useState<AuthUser | null>(() => {
    const saved = localStorage.getItem(USER_STORAGE_KEY);
    if (!saved) return null;
    try {
      return JSON.parse(saved) as AuthUser;
    } catch {
      return null;
    }
  });
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    setIsLoading(true);
    try {
      const response = await loginApi(request);
      const authUser: AuthUser = {
        userId: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        institutionId: response.institutionId,
        departmentId: response.departmentId,
        roles: response.roles,
      };

      localStorage.setItem(AUTH_STORAGE_KEY, response.accessToken);
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(authUser));

      setToken(response.accessToken);
      setUser(authUser);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => {
      logout();
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, [logout]);

  const updateUser = useCallback((updated: Partial<AuthUser>) => {
    setUser((prev) => {
      if (!prev) return null;
      const next = { ...prev, ...updated };
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: Boolean(token && user),
    isLoading,
    login,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
