import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Check,
  Cpu,
  Wrench,
  Share2,
  Bell,
  Clock,
  Activity,
  ArrowRight,
  ShieldCheck,
} from 'lucide-react';
import { getBookings, confirmBooking } from '../../api/booking';
import { getEquipmentList } from '../../api/equipment';
import { getWorkOrders } from '../../api/maintenance';
import { getSharingAgreements } from '../../api/sharing';
import { notificationApi } from '../../api/notifications';
import type { BookingResponse } from '../../types/booking';
import type { EquipmentResponse } from '../../types/equipment';
import type { WorkOrderResponse } from '../../types/maintenance';
import type { ResourceSharingAgreementResponse } from '../../types/sharing';
import type { NotificationResponse } from '../../types/notification';

export const LabManagerDashboardView: React.FC = () => {
  const [pendingBookings, setPendingBookings] = useState<BookingResponse[]>([]);
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrderResponse[]>([]);
  const [sharingAgreements, setSharingAgreements] = useState<ResourceSharingAgreementResponse[]>([]);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionInProgress, setActionInProgress] = useState<number | null>(null);
  const [actionFeedback, setActionFeedback] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      const [bookingsRes, eqRes, woRes, sharingRes, notifsRes] = await Promise.allSettled([
        getBookings(),
        getEquipmentList(),
        getWorkOrders(),
        getSharingAgreements(),
        notificationApi.getNotifications(),
      ]);

      if (bookingsRes.status === 'fulfilled') {
        setPendingBookings(bookingsRes.value.filter((b) => b.status === 'PENDING_APPROVAL'));
      }
      if (eqRes.status === 'fulfilled') setEquipmentList(eqRes.value);
      if (woRes.status === 'fulfilled') setWorkOrders(woRes.value);
      if (sharingRes.status === 'fulfilled') setSharingAgreements(sharingRes.value);
      if (notifsRes.status === 'fulfilled') setNotifications(notifsRes.value || []);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleApprove = async (id: number) => {
    setActionInProgress(id);
    setActionFeedback(null);
    try {
      await confirmBooking(id);
      setActionFeedback(`Reservation #${id} confirmed successfully.`);
      await loadData();
    } catch (err: any) {
      setActionFeedback(err.response?.data?.message || `Failed to approve reservation #${id}`);
    } finally {
      setActionInProgress(null);
    }
  };

  const availableEquipment = equipmentList.filter((eq) => eq.status === 'AVAILABLE');
  const maintenanceEquipment = equipmentList.filter((eq) => eq.status === 'UNDER_MAINTENANCE');
  const activeWorkOrders = workOrders.filter(
    (wo) => wo.status === 'IN_PROGRESS' || wo.status === 'SCHEDULED' || wo.status === 'WAITING_FOR_PARTS'
  );

  return (
    <div className="space-y-6">
      {/* Feedback banner */}
      {actionFeedback && (
        <div className="p-4 rounded-xl bg-sky-950/50 border border-sky-800 text-sky-300 text-sm flex items-center justify-between">
          <span>{actionFeedback}</span>
          <button
            type="button"
            onClick={() => setActionFeedback(null)}
            className="text-sky-400 hover:text-white text-xs"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Operational Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Pending Approvals</div>
            <div className="text-2xl font-bold text-amber-400 mt-1">{pendingBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Awaiting reservation review</div>
          </div>
          <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Equipment Available</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1">
              {availableEquipment.length} <span className="text-sm font-normal text-slate-500">/ {equipmentList.length}</span>
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Operational lab instruments</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Cpu className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Active Maintenance</div>
            <div className="text-2xl font-bold text-indigo-400 mt-1">
              {activeWorkOrders.length}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">{maintenanceEquipment.length} instruments offline</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Wrench className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Sharing Agreements</div>
            <div className="text-2xl font-bold text-purple-400 mt-1">{sharingAgreements.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Inter-lab resource sharing</div>
          </div>
          <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Share2 className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Pending Approvals & Operations (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Pending Approval Table */}
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-amber-400" />
                <h3 className="text-base font-semibold text-white">Pending Reservation Approvals</h3>
              </div>
              <Link to="/bookings" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                All Bookings &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-8 text-center text-slate-400 text-sm">Loading requests...</div>
            ) : pendingBookings.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-6">
                <Check className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                <p className="text-sm text-slate-300 font-medium">All reservations are up to date</p>
                <p className="text-xs text-slate-500 mt-1">No pending researcher reservation requests</p>
              </div>
            ) : (
              <div className="space-y-3">
                {pendingBookings.slice(0, 5).map((b) => (
                  <div
                    key={b.id}
                    className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700/80 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-white">
                          {b.equipmentName || `Equipment #${b.equipmentId}`}
                        </span>
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                          Pending Approval
                        </span>
                      </div>
                      <div className="text-xs text-slate-300 mt-1">
                        Requested by User #{b.userId} &bull; Ref: <span className="font-mono text-slate-400">{b.bookingReference}</span>
                      </div>
                      <div className="text-[11px] text-slate-500 mt-0.5 font-mono">
                        {new Date(b.startTime).toLocaleDateString()} {new Date(b.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} &ndash; {new Date(b.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <Link
                        to={`/bookings/${b.id}`}
                        className="px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800 text-xs font-medium transition-colors"
                      >
                        Review
                      </Link>
                      <button
                        type="button"
                        onClick={() => handleApprove(b.id)}
                        disabled={actionInProgress === b.id}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-medium transition-colors shadow-sm"
                      >
                        <Check className="w-3.5 h-3.5" />
                        {actionInProgress === b.id ? 'Approving...' : 'Approve'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Equipment Status Summary */}
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Cpu className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Equipment Operational Status</h3>
              </div>
              <Link to="/equipment" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                Equipment Catalog &rarr;
              </Link>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80">
                <div className="text-xs text-slate-400">Total Registered</div>
                <div className="text-xl font-bold text-white mt-1">{equipmentList.length}</div>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/60 border border-emerald-900/30">
                <div className="text-xs text-emerald-400">Available</div>
                <div className="text-xl font-bold text-emerald-400 mt-1">{availableEquipment.length}</div>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/60 border border-amber-900/30">
                <div className="text-xs text-amber-400">In Maintenance</div>
                <div className="text-xl font-bold text-amber-400 mt-1">{maintenanceEquipment.length}</div>
              </div>
              <div className="p-3.5 rounded-xl bg-slate-950/60 border border-sky-900/30">
                <div className="text-xs text-sky-400">Shareable</div>
                <div className="text-xl font-bold text-sky-400 mt-1">
                  {equipmentList.filter((e) => e.isShareableExternally).length}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Shortcuts & Alerts Column (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Lab Notifications</h3>
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

          {/* Quick Management Shortcuts */}
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm space-y-3">
            <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Management Modules
            </h4>
            <div className="space-y-2">
              <Link
                to="/utilization"
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 text-xs font-medium text-slate-200 hover:text-white transition-all"
              >
                <div className="flex items-center gap-2.5">
                  <Activity className="w-4 h-4 text-sky-400" />
                  <span>Instrument Utilization</span>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500" />
              </Link>

              <Link
                to="/maintenance"
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 text-xs font-medium text-slate-200 hover:text-white transition-all"
              >
                <div className="flex items-center gap-2.5">
                  <Wrench className="w-4 h-4 text-indigo-400" />
                  <span>Maintenance &amp; Work Orders</span>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500" />
              </Link>

              <Link
                to="/sharing"
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 text-xs font-medium text-slate-200 hover:text-white transition-all"
              >
                <div className="flex items-center gap-2.5">
                  <Share2 className="w-4 h-4 text-purple-400" />
                  <span>Resource Sharing</span>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500" />
              </Link>

              <Link
                to="/analytics"
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 text-xs font-medium text-slate-200 hover:text-white transition-all"
              >
                <div className="flex items-center gap-2.5">
                  <Activity className="w-4 h-4 text-emerald-400" />
                  <span>Operational Analytics</span>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500" />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
