import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Wrench,
  AlertTriangle,
  Clock,
  CheckCircle2,
  Bell,
  ArrowRight,
  ShieldAlert,
} from 'lucide-react';
import { getWorkOrders, getDowntimeLogs } from '../../api/maintenance';
import { getEquipmentList } from '../../api/equipment';
import { notificationApi } from '../../api/notifications';
import { useAuth } from '../../context/useAuth';
import type { WorkOrderResponse, DowntimeLogResponse } from '../../types/maintenance';
import type { EquipmentResponse } from '../../types/equipment';
import type { NotificationResponse } from '../../types/notification';

export const TechnicianDashboardView: React.FC = () => {
  const { user } = useAuth();
  const [workOrders, setWorkOrders] = useState<WorkOrderResponse[]>([]);
  const [downtimes, setDowntimes] = useState<DowntimeLogResponse[]>([]);
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const loadData = async () => {
      try {
        const [woRes, downRes, eqRes, notifsRes] = await Promise.allSettled([
          getWorkOrders(),
          getDowntimeLogs(),
          getEquipmentList(),
          notificationApi.getNotifications(),
        ]);

        if (isMounted) {
          if (woRes.status === 'fulfilled') setWorkOrders(woRes.value);
          if (downRes.status === 'fulfilled') setDowntimes(downRes.value);
          if (eqRes.status === 'fulfilled') setEquipmentList(eqRes.value);
          if (notifsRes.status === 'fulfilled') setNotifications(notifsRes.value || []);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadData();
    return () => {
      isMounted = false;
    };
  }, []);

  const assignedWorkOrders = workOrders.filter(
    (wo) => wo.assignedTechnicianId === user?.userId
  );
  const activeWorkOrders = workOrders.filter(
    (wo) => wo.status === 'IN_PROGRESS' || wo.status === 'SCHEDULED' || wo.status === 'WAITING_FOR_PARTS'
  );
  const equipmentRequiringAttention = equipmentList.filter(
    (eq) => eq.status === 'UNDER_MAINTENANCE' || eq.status === 'OUT_OF_SERVICE'
  );
  const activeDowntimes = downtimes.filter((dt) => !dt.downtimeEnd);

  return (
    <div className="space-y-6">
      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Assigned Work Orders</div>
            <div className="text-2xl font-bold text-sky-400 mt-1">{assignedWorkOrders.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Directly assigned to you</div>
          </div>
          <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <Wrench className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Active Work Orders</div>
            <div className="text-2xl font-bold text-indigo-400 mt-1">{activeWorkOrders.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Under technician handling</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Attention Required</div>
            <div className="text-2xl font-bold text-amber-400 mt-1">{equipmentRequiringAttention.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Under repair or out of service</div>
          </div>
          <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <AlertTriangle className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Active Downtime Events</div>
            <div className="text-2xl font-bold text-rose-400 mt-1">{activeDowntimes.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Unresolved downtime logs</div>
          </div>
          <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400 border border-rose-500/20">
            <ShieldAlert className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Work Orders Column (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Wrench className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Assigned &amp; Active Work Orders</h3>
              </div>
              <Link to="/maintenance" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                View All &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-8 text-center text-slate-400 text-sm">Loading work orders...</div>
            ) : activeWorkOrders.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-6">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                <p className="text-sm text-slate-300 font-medium">No active maintenance work orders</p>
                <p className="text-xs text-slate-500 mt-1">All scheduled and assigned tasks are up to date</p>
              </div>
            ) : (
              <div className="space-y-3">
                {activeWorkOrders.slice(0, 5).map((wo) => (
                  <div
                    key={wo.id}
                    className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700/80 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-white font-mono">
                          {wo.workOrderNumber}
                        </span>
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-sky-500/10 text-sky-400 border border-sky-500/20">
                          {wo.status}
                        </span>
                      </div>
                      <div className="text-xs text-slate-300 mt-1">
                        {wo.workPerformedSummary || `Maintenance Service Action (${wo.type})`}
                      </div>
                      <div className="text-[11px] text-slate-500 mt-1">
                        Equipment #{wo.equipmentId} &bull; Priority: {wo.priority || 'MEDIUM'}
                      </div>
                    </div>
                    <Link
                      to={`/maintenance/work-orders/${wo.id}`}
                      className="inline-flex items-center justify-center px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-medium transition-colors shrink-0"
                    >
                      Update Work &rarr;
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Equipment Requiring Attention */}
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-amber-400" />
                <h3 className="text-base font-semibold text-white">Equipment Requiring Attention</h3>
              </div>
              <Link to="/equipment" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                Equipment Catalog &rarr;
              </Link>
            </div>

            {equipmentRequiringAttention.length === 0 ? (
              <div className="py-6 text-center text-slate-400 text-xs">
                All laboratory equipment is currently operational.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {equipmentRequiringAttention.slice(0, 4).map((eq) => (
                  <div
                    key={eq.id}
                    className="p-3.5 rounded-xl bg-slate-950/60 border border-amber-900/30 text-xs"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-white">{eq.name}</span>
                      <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-500/10 text-amber-400">
                        {eq.status}
                      </span>
                    </div>
                    <div className="text-slate-400 mt-1 font-mono text-[11px]">
                      Tag: {eq.assetTag} &bull; {eq.locationBuilding} {eq.locationRoom}
                    </div>
                    <div className="mt-2.5 pt-2 border-t border-slate-800 flex justify-end">
                      <Link
                        to={`/equipment/${eq.id}`}
                        className="text-sky-400 hover:text-sky-300 text-[11px] font-medium"
                      >
                        Inspect Resource &rarr;
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Notifications & Downtime Column (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Maintenance Alerts</h3>
              </div>
              <Link to="/notifications" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                All Alerts &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-6 text-center text-slate-400 text-sm">Loading alerts...</div>
            ) : notifications.length === 0 ? (
              <div className="py-6 text-center text-slate-500 text-xs">No active alerts</div>
            ) : (
              <div className="space-y-3">
                {notifications.slice(0, 4).map((n) => (
                  <div
                    key={n.id}
                    className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/80 text-xs"
                  >
                    <div className="font-semibold text-slate-200">{n.title}</div>
                    <p className="text-slate-400 mt-1 text-[11px] line-clamp-2">{n.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Quick Navigation Card */}
          <div className="p-5 rounded-2xl bg-gradient-to-br from-slate-900 to-sky-950/30 border border-slate-800 text-xs space-y-2">
            <span className="text-[11px] uppercase tracking-wider font-semibold text-slate-400">
              Technician Workflow
            </span>
            <p className="text-slate-400">
              Update work orders as soon as service actions begin. Record downtime immediately upon taking an instrument offline.
            </p>
            <div className="pt-2">
              <Link
                to="/maintenance"
                className="inline-flex items-center gap-1.5 text-sky-400 hover:text-sky-300 font-medium"
              >
                Open Maintenance Board <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
