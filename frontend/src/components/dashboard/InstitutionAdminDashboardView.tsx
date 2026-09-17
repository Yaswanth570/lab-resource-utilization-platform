import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Building2,
  Cpu,
  Receipt,
  Share2,
  FileText,
  Shield,
  Activity,
  ArrowRight,
} from 'lucide-react';
import { getEquipmentList } from '../../api/equipment';
import { getBookings } from '../../api/booking';
import { getSharingAgreements } from '../../api/sharing';
import { useAuth } from '../../context/useAuth';
import type { EquipmentResponse } from '../../types/equipment';
import type { BookingResponse } from '../../types/booking';
import type { ResourceSharingAgreementResponse } from '../../types/sharing';

export const InstitutionAdminDashboardView: React.FC = () => {
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
  const externalShareable = equipmentList.filter((eq) => eq.isShareableExternally);

  return (
    <div className="space-y-6">
      {/* Scope Banner */}
      <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between text-xs">
        <div className="flex items-center gap-2 text-slate-300">
          <Building2 className="w-4 h-4 text-sky-400" />
          <span className="font-semibold">Institution Governance Scope:</span>
          <span className="text-slate-400">
            {user?.institutionId ? `Institution Tenant #${user.institutionId}` : 'Primary Academic Institution'}
          </span>
        </div>
        <span className="text-slate-500 font-mono">Institutional Administration</span>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Total Instruments</div>
            <div className="text-2xl font-bold text-white mt-1">{equipmentList.length}</div>
            <div className="text-[11px] text-emerald-400 mt-1">{availableEquipment.length} operational</div>
          </div>
          <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <Cpu className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Total Reservations</div>
            <div className="text-2xl font-bold text-indigo-400 mt-1">{bookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Recorded platform bookings</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Activity className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Inter-Lab Sharing</div>
            <div className="text-2xl font-bold text-purple-400 mt-1">{sharingList.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">{externalShareable.length} shareable instruments</div>
          </div>
          <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
            <Share2 className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Billing &amp; Invoicing</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1">Active</div>
            <div className="text-[11px] text-slate-500 mt-1">Department chargeback ready</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Receipt className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Institutional Overview (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Cpu className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Institution Equipment Registry</h3>
              </div>
              <Link to="/equipment" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                Manage Equipment &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-8 text-center text-slate-400 text-sm">Loading registry...</div>
            ) : equipmentList.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-6">
                <p className="text-sm text-slate-400">No equipment currently registered in this institution.</p>
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
                        Tag: {eq.assetTag} &bull; Model: {eq.modelNumber || 'N/A'}
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
                      <Link to={`/equipment/${eq.id}`} className="text-sky-400 hover:text-sky-300 font-medium">
                        View &rarr;
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Administration Links (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm space-y-4">
            <h3 className="text-sm font-semibold text-white">Institutional Administration</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Oversee departmental chargeback, inter-institutional resource sharing agreements, and system access policies.
            </p>

            <div className="space-y-2 pt-2">
              <Link
                to="/cost"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-emerald-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
                    <Receipt className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-emerald-300 transition-colors">Cost &amp; Billing</div>
                    <div className="text-[10px] text-slate-500">Invoicing &amp; department chargeback</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/sharing"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-purple-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400">
                    <Share2 className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-purple-300 transition-colors">Resource Sharing</div>
                    <div className="text-[10px] text-slate-500">Agreements &amp; quotas</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/admin"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-sky-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
                    <Shield className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-sky-300 transition-colors">Platform Administration</div>
                    <div className="text-[10px] text-slate-500">Departments &amp; users</div>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:translate-x-1 transition-all" />
              </Link>

              <Link
                to="/reports"
                className="flex items-center justify-between p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-indigo-800/60 group text-xs font-medium text-slate-200 transition-all"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400">
                    <FileText className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-white group-hover:text-indigo-300 transition-colors">Institutional Reports</div>
                    <div className="text-[10px] text-slate-500">Export audit &amp; utilization files</div>
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
