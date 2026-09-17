import React from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { hasAnyRole } from '../../utils/rbac';
import { AccessDeniedPage } from '../../pages/AccessDeniedPage';

interface RoleRouteProps {
  allowedRoles: string[];
}

export const RoleRoute: React.FC<RoleRouteProps> = ({ allowedRoles }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-8 h-8 border-2 border-sky-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const isAuthorized = hasAnyRole(user?.roles, allowedRoles);

  if (!isAuthorized) {
    return <AccessDeniedPage />;
  }

  return <Outlet />;
};
