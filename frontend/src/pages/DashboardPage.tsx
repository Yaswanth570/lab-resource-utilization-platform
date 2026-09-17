import React from 'react';
import { useAuth } from '../context/useAuth';
import {
  ShieldCheck,
  Building2,
  Layers,
  CheckCircle2,
  Clock,
} from 'lucide-react';
import {
  ROLES,
  hasRole,
} from '../utils/rbac';
import { ResearcherDashboardView } from '../components/dashboard/ResearcherDashboardView';
import { TechnicianDashboardView } from '../components/dashboard/TechnicianDashboardView';
import { LabManagerDashboardView } from '../components/dashboard/LabManagerDashboardView';
import { DepartmentHeadDashboardView } from '../components/dashboard/DepartmentHeadDashboardView';
import { InstitutionAdminDashboardView } from '../components/dashboard/InstitutionAdminDashboardView';
import { SystemAdminDashboardView } from '../components/dashboard/SystemAdminDashboardView';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  const formatRole = (role: string): string => {
    return role
      .replace(/^ROLE_/, '')
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  };

  const renderRoleDashboard = () => {
    const roles = user?.roles;
    if (hasRole(roles, ROLES.SYSTEM_ADMIN)) {
      return <SystemAdminDashboardView />;
    }
    if (hasRole(roles, ROLES.INSTITUTION_ADMIN)) {
      return <InstitutionAdminDashboardView />;
    }
    if (hasRole(roles, ROLES.DEPARTMENT_HEAD)) {
      return <DepartmentHeadDashboardView />;
    }
    if (hasRole(roles, ROLES.LAB_MANAGER)) {
      return <LabManagerDashboardView />;
    }
    if (hasRole(roles, ROLES.LAB_TECHNICIAN)) {
      return <TechnicianDashboardView />;
    }
    return <ResearcherDashboardView />;
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="p-6 sm:p-8 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900 to-sky-950/40 border border-slate-800 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-medium mb-3">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Active Session</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Welcome back, {user?.firstName}!
            </h2>
            <p className="mt-1 text-sm text-slate-400 max-w-2xl">
              Laboratory Resource Utilization &amp; Reservation Platform. Real-time instrument tracking, reservation governance, and operational analytics.
            </p>
          </div>

          <div className="flex flex-col items-start sm:items-end gap-1.5 text-xs text-slate-400">
            <div className="flex items-center gap-2">
              <Clock className="w-3.5 h-3.5 text-slate-500" />
              <span>Operating Window: 08:00 – 20:00 UTC</span>
            </div>
            <span className="text-[11px] text-slate-500">Monday – Saturday</span>
          </div>
        </div>

        {/* User context metadata cards */}
        <div className="mt-6 pt-6 border-t border-slate-800 grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400">Assigned Role</div>
              <div className="flex flex-wrap gap-1 mt-0.5">
                {user?.roles?.map((r) => (
                  <span
                    key={r}
                    className="inline-block text-xs font-semibold text-sky-300"
                  >
                    {formatRole(r)}
                  </span>
                )) || <span className="text-xs text-slate-400">User</span>}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
            <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400">
              <Building2 className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400">Institution Context</div>
              <div className="text-xs font-semibold text-slate-200">
                {user?.institutionId ? `Institution #${user.institutionId}` : 'Primary Academic Tenant'}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400">Department Unit</div>
              <div className="text-xs font-semibold text-slate-200">
                {user?.departmentId ? `Department #${user.departmentId}` : 'General Department'}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Role-Specific Dashboard Content */}
      {renderRoleDashboard()}
    </div>
  );
};

export default DashboardPage;
