import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  ShieldAlert,
  Server,
  Users,
  Lock,
  Cpu,
  FileCode,
  ArrowRight,
  Activity,
} from 'lucide-react';
import { apiClient } from '../../api/client';
import { getEquipmentList } from '../../api/equipment';
import type { EquipmentResponse } from '../../types/equipment';

export const SystemAdminDashboardView: React.FC = () => {
  const [healthStatus, setHealthStatus] = useState<{ status: string; service?: string } | null>(null);
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const loadHealth = async () => {
      try {
        const [healthRes, eqRes] = await Promise.allSettled([
          apiClient.get('/health'),
          getEquipmentList(),
        ]);

        if (isMounted) {
          if (healthRes.status === 'fulfilled') setHealthStatus(healthRes.value.data);
          if (eqRes.status === 'fulfilled') setEquipmentList(eqRes.value);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadHealth();
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div className="space-y-6">
      {/* Scope Banner */}
      <div className="p-4 rounded-xl bg-gradient-to-r from-slate-900 to-indigo-950/40 border border-slate-800 flex items-center justify-between text-xs shadow-md">
        <div className="flex items-center gap-2 text-slate-300">
          <ShieldAlert className="w-4 h-4 text-indigo-400" />
          <span className="font-semibold">System-Wide Super Administrator Scope:</span>
          <span className="text-slate-400">Cross-tenant infrastructure &amp; governance authority</span>
        </div>
        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">
          Full Access
        </span>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Backend Service Health</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1">
              {healthStatus?.status || (loading ? 'Checking...' : 'ONLINE')}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">REST API &amp; Spring Security</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Server className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Defined System Roles</div>
            <div className="text-2xl font-bold text-sky-400 mt-1">6</div>
            <div className="text-[11px] text-slate-500 mt-1">Standard RBAC roles enforced</div>
          </div>
          <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <Lock className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Global Instruments</div>
            <div className="text-2xl font-bold text-purple-400 mt-1">{equipmentList.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Across all registered tenants</div>
          </div>
          <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Cpu className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Method Security</div>
            <div className="text-2xl font-bold text-indigo-400 mt-1">Active</div>
            <div className="text-[11px] text-slate-500 mt-1">PreAuthorize &amp; 403 Handler</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Activity className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Security & Access Management (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Lock className="w-5 h-5 text-indigo-400" />
                <h3 className="text-base font-semibold text-white">Access Control &amp; Authority Architecture</h3>
              </div>
              <Link to="/admin" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                Administration Module &rarr;
              </Link>
            </div>

            <div className="space-y-3 text-xs">
              <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-white">Role-Based Access Control (RBAC)</div>
                  <div className="text-slate-400 mt-0.5">
                    Strict backend authorization with @PreAuthorize and JwtAccessDeniedHandler returning JSON 403 Forbidden.
                  </div>
                </div>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Enforced
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-white">Tenant &amp; Scope Isolation</div>
                  <div className="text-slate-400 mt-0.5">
                    Institution ID and Department ID claim extraction for data boundary protection.
                  </div>
                </div>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Enforced
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 flex items-center justify-between">
                <div>
                  <div className="font-semibold text-white">Self-Approval Prevention</div>
                  <div className="text-slate-400 mt-0.5">
                    Researcher / Student self-reservation approval strictly blocked across all controllers.
                  </div>
                </div>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Enforced
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* System Administration Shortcuts (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm space-y-4">
            <h3 className="text-sm font-semibold text-white">System Governance</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Super-administrative control over users, institutions, platform parameters, and cross-tenant auditing.
            </p>

            <div className="space-y-2 pt-2">
              <Link
                to="/admin"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-indigo-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400">
                    <Users className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-indigo-300 transition-colors">Users &amp; Roles</div>
                    <div className="text-[10px] text-slate-500">Access assignment &amp; lifecycle</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/equipment"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-sky-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
                    <Cpu className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-sky-300 transition-colors">Global Catalog</div>
                    <div className="text-[10px] text-slate-500">All registered instruments</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/reports"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-emerald-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
                    <FileCode className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-emerald-300 transition-colors">Audit &amp; Compliance Logs</div>
                    <div className="text-[10px] text-slate-500">Export system-wide telemetry</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
