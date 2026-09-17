import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Layers,
  Cpu,
  CalendarDays,
  Activity,
  BarChart3,
  FileText,
  ArrowRight,
  CheckCircle2,
  Share2,
} from 'lucide-react';
import { getEquipmentList } from '../../api/equipment';
import { getBookings } from '../../api/booking';
import { getSharingAgreements } from '../../api/sharing';
import { useAuth } from '../../context/useAuth';
import type { EquipmentResponse } from '../../types/equipment';
import type { BookingResponse } from '../../types/booking';
import type { ResourceSharingAgreementResponse } from '../../types/sharing';

export const DepartmentHeadDashboardView: React.FC = () => {
  const { user } = useAuth();
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [sharingList, setSharingList] = useState<ResourceSharingAgreementResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const loadData = async () => {
      try {
        const [eqRes, bookRes, sharingRes] = await Promise.allSettled([
          getEquipmentList(),
          getBookings(),
          getSharingAgreements(),
        ]);

        if (isMounted) {
          if (eqRes.status === 'fulfilled') setEquipmentList(eqRes.value);
          if (bookRes.status === 'fulfilled') setBookings(bookRes.value);
          if (sharingRes.status === 'fulfilled') setSharingList(sharingRes.value);
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

  const availableEquipment = equipmentList.filter((eq) => eq.status === 'AVAILABLE');
  const activeBookings = bookings.filter(
    (b) => b.status === 'CONFIRMED' || b.status === 'IN_USE'
  );
  const pendingBookings = bookings.filter((b) => b.status === 'PENDING_APPROVAL');
  const completedBookings = bookings.filter((b) => b.status === 'COMPLETED');

  return (
    <div className="space-y-6">
      {/* Scope Banner */}
      <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between text-xs">
        <div className="flex items-center gap-2 text-slate-300">
          <Layers className="w-4 h-4 text-purple-400" />
          <span className="font-semibold">Department Operational Scope:</span>
          <span className="text-slate-400">
            {user?.departmentId ? `Department Unit #${user.departmentId}` : 'Assigned Department'}
          </span>
        </div>
        <span className="text-slate-500 font-mono">Scoped Analytics &amp; Reporting</span>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Department Equipment</div>
            <div className="text-2xl font-bold text-white mt-1">{equipmentList.length}</div>
            <div className="text-[11px] text-emerald-400 mt-1">{availableEquipment.length} online &amp; available</div>
          </div>
          <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <Cpu className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Active Bookings</div>
            <div className="text-2xl font-bold text-sky-400 mt-1">{activeBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">{pendingBookings.length} pending review</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <CalendarDays className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Completed Sessions</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1">{completedBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Research usage recorded</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Sharing Agreements</div>
            <div className="text-2xl font-bold text-purple-400 mt-1">{sharingList.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Inter-department allocations</div>
          </div>
          <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Share2 className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Department Equipment & Bookings (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Cpu className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Department Instruments Registry</h3>
              </div>
              <Link to="/equipment" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                View All &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-8 text-center text-slate-400 text-sm">Loading department resources...</div>
            ) : equipmentList.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-6">
                <p className="text-sm text-slate-400">No instruments currently assigned to this department unit.</p>
              </div>
            ) : (
              <div className="space-y-3">
                {equipmentList.slice(0, 5).map((eq) => (
                  <div
                    key={eq.id}
                    className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700/80 transition-all flex items-center justify-between text-xs"
                  >
                    <div>
                      <span className="font-semibold text-white">{eq.name}</span>
                      <div className="text-slate-400 text-[11px] mt-0.5 font-mono">
                        Tag: {eq.assetTag} &bull; {eq.locationBuilding} {eq.locationRoom}
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                          eq.status === 'AVAILABLE'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/30'
                        }`}
                      >
                        {eq.status}
                      </span>
                      <Link
                        to={`/equipment/${eq.id}`}
                        className="text-sky-400 hover:text-sky-300 font-medium"
                      >
                        Details &rarr;
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Analytics & Reports Shortcuts (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm space-y-4">
            <h3 className="text-sm font-semibold text-white">Department Intelligence</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Analyze booking demand, instrument utilization rates, and generate formal compliance reports for your department.
            </p>

            <div className="space-y-2 pt-2">
              <Link
                to="/analytics"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-sky-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
                    <BarChart3 className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-sky-300 transition-colors">Demand Analytics</div>
                    <div className="text-[10px] text-slate-500">Utilization &amp; trends</div>
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
                    <FileText className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-emerald-300 transition-colors">Department Reports</div>
                    <div className="text-[10px] text-slate-500">Export CSV / Audit logs</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/utilization"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-indigo-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400">
                    <Activity className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-indigo-300 transition-colors">Usage Sessions</div>
                    <div className="text-[10px] text-slate-500">Active telemetry &amp; idle logs</div>
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
