import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Layers,
  ArrowLeft,
  Calendar,
  DollarSign,
  Building2,
  ExternalLink,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  XCircle,
  ToggleLeft,
  ToggleRight,
  Bookmark,
  Cpu,
  ShieldCheck,
} from 'lucide-react';
import type {
  SharedEquipmentAllocationResponse,
  ResourceSharingAgreementResponse,
} from '../../types/sharing';
import type { BookingResponse } from '../../types/booking';
import {
  getSharedAllocationById,
  toggleAllocationActive,
  getSharingAgreementById,
  getExternalBookings,
} from '../../api/sharing';
import { BookingStatusBadge } from '../../components/booking/BookingStatusBadge';

export const SharedAllocationDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [allocation, setAllocation] = useState<SharedEquipmentAllocationResponse | null>(null);
  const [agreement, setAgreement] = useState<ResourceSharingAgreementResponse | null>(null);
  const [relatedBookings, setRelatedBookings] = useState<BookingResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  const loadData = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const allocData = await getSharedAllocationById(id);
      setAllocation(allocData);

      // Fetch parent agreement if present
      if (allocData.sharingAgreementId) {
        try {
          const agData = await getSharingAgreementById(allocData.sharingAgreementId);
          setAgreement(agData);
        } catch (err) {
          console.warn('Could not load parent agreement details:', err);
        }
      }

      // Fetch related external bookings for this allocation
      try {
        const extBookings = await getExternalBookings();
        const allocBookings = extBookings.filter(
          (b) => b.sharedAllocationId === Number(id) || b.equipmentId === allocData.equipmentId
        );
        setRelatedBookings(allocBookings);
      } catch (err) {
        console.warn('Could not load related external bookings:', err);
      }
    } catch (err: unknown) {
      console.error('Failed to load shared allocation:', err);
      setErrorMessage(
        'Could not load equipment allocation details. The backend REST endpoint may not be implemented yet or the allocation record does not exist.'
      );
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleToggleActive = async () => {
    if (!allocation) return;
    setActionLoading(true);
    try {
      const targetState = !allocation.isActive;
      const updated = await toggleAllocationActive(allocation.id, targetState);
      setAllocation(updated);
      showToast(`Allocation marked as ${targetState ? 'ACTIVE' : 'INACTIVE'}`);
    } catch (err: unknown) {
      console.error('Failed to toggle allocation active state:', err);
      alert(
        'Unable to update allocation active status. The backend status update endpoint (/api/sharing/allocations/:id/active) may not be implemented in the REST layer.'
      );
    } finally {
      setActionLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="p-16 text-center bg-slate-900 border border-slate-800 rounded-2xl">
        <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
        <p className="text-xs text-slate-400 mt-3">Loading allocation details...</p>
      </div>
    );
  }

  if (errorMessage || !allocation) {
    return (
      <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
        <h2 className="text-base font-semibold text-white">Allocation Record Unavailable</h2>
        <p className="text-xs text-slate-400 max-w-lg mx-auto">
          {errorMessage || 'Equipment allocation record could not be found.'}
        </p>
        <div className="flex items-center justify-center gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/sharing')}
            className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
          >
            Back to Sharing Management
          </button>
          <button
            type="button"
            onClick={loadData}
            className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl transition-colors"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-950/90 border border-emerald-500/40 text-emerald-200 rounded-xl shadow-2xl flex items-center gap-3 text-xs animate-in fade-in slide-in-from-top-3 duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Navigation Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/sharing')}
            className="p-2 text-slate-400 hover:text-white bg-slate-850 hover:bg-slate-800 border border-slate-700/80 rounded-xl transition-colors"
            title="Back to Sharing"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h1 className="text-xl font-bold text-white flex items-center gap-2">
                <Layers className="w-5 h-5 text-indigo-400" />
                <span>Allocation #{allocation.id}</span>
              </h1>
              {allocation.isActive ? (
                <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  Active Allocation
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">
                  <XCircle className="w-3.5 h-3.5" />
                  Suspended / Inactive
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Assigned to Agreement{' '}
              <Link
                to={`/sharing/agreements/${allocation.sharingAgreementId}`}
                className="text-indigo-400 hover:text-indigo-300 font-mono hover:underline"
              >
                #{allocation.sharingAgreementId}
              </Link>{' '}
              &middot; Created {new Date(allocation.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            disabled={actionLoading}
            onClick={handleToggleActive}
            className={`inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold rounded-xl border transition-colors disabled:opacity-50 ${
              allocation.isActive
                ? 'text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border-amber-500/30'
                : 'text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border-emerald-500/30'
            }`}
          >
            {allocation.isActive ? (
              <>
                <ToggleLeft className="w-4 h-4" />
                Deactivate Allocation
              </>
            ) : (
              <>
                <ToggleRight className="w-4 h-4" />
                Activate Allocation
              </>
            )}
          </button>

          <button
            type="button"
            onClick={loadData}
            className="p-2 text-slate-400 hover:text-white bg-slate-850 hover:bg-slate-800 border border-slate-700/80 rounded-xl transition-colors"
            title="Refresh Data"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Info Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Allocated Equipment */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-indigo-400" />
              Equipment Instrument
            </span>
            <span className="text-[10px] font-mono text-slate-500">ID #{allocation.equipmentId}</span>
          </div>
          <Link
            to={`/equipment/${allocation.equipmentId}`}
            className="text-sm font-semibold text-white hover:text-indigo-300 flex items-center gap-1.5 truncate"
          >
            <span className="truncate">{allocation.equipmentName || `Equipment #${allocation.equipmentId}`}</span>
            <ExternalLink className="w-3 h-3 text-slate-500 shrink-0" />
          </Link>
          <div className="text-[11px] font-mono text-slate-400 mt-0.5">
            Tag: {allocation.equipmentAssetTag || 'N/A'}
          </div>
        </div>

        {/* Parent Agreement */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-cyan-400" />
              Parent Agreement
            </span>
            <span className="text-[10px] font-mono text-slate-500">ID #{allocation.sharingAgreementId}</span>
          </div>
          <Link
            to={`/sharing/agreements/${allocation.sharingAgreementId}`}
            className="text-sm font-semibold text-cyan-300 hover:text-cyan-200 flex items-center gap-1.5 truncate font-mono"
          >
            <span>{allocation.sharingAgreementCode || `AGR-${allocation.sharingAgreementId}`}</span>
            <ExternalLink className="w-3 h-3 text-slate-500 shrink-0" />
          </Link>
          <div className="text-[11px] text-slate-400 mt-0.5">
            Multiplier: {agreement ? `${Number(agreement.billingRateMultiplier).toFixed(2)}x` : 'Custom'}
          </div>
        </div>

        {/* Hourly Rate Override */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <DollarSign className="w-3.5 h-3.5 text-emerald-400" />
              Billing Model
            </span>
          </div>
          <div className="text-base font-bold text-emerald-400 font-mono">
            {allocation.customHourlyRate !== null && allocation.customHourlyRate !== undefined
              ? `$${Number(allocation.customHourlyRate).toFixed(2)}/hr (Custom)`
              : 'Agreement Multiplier'}
          </div>
          <div className="text-[11px] text-slate-500 mt-0.5">
            {allocation.customHourlyRate ? 'Custom rate overrides multiplier' : 'Calculated against base fee'}
          </div>
        </div>

        {/* Sharing Policy Status */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-indigo-400" />
              External Availability
            </span>
          </div>
          <div className="text-sm font-bold text-white">
            {allocation.isActive ? 'Bookable Externally' : 'Suspended by Host'}
          </div>
          <div className="text-[11px] text-slate-500 mt-0.5">
            {allocation.isActive ? 'Eligible partner researchers may reserve' : 'External reservations blocked'}
          </div>
        </div>
      </div>

      {/* Cross-Module Linkages: Related External Bookings */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Bookmark className="w-4 h-4 text-indigo-400" />
            <h2 className="text-sm font-semibold text-white">Bookings Utilizing This Resource Allocation</h2>
            <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded-full">
              {relatedBookings.length}
            </span>
          </div>
        </div>

        {relatedBookings.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            <Bookmark className="w-8 h-8 text-slate-600 mx-auto mb-2" />
            <p className="font-medium text-slate-300">No active external reservations for this allocation</p>
            <p className="text-[11px] text-slate-500 mt-1">
              External researchers from partner institutions with active sharing agreements will have bookings
              associated here.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-850/60 text-slate-400 border-b border-slate-800">
                  <th className="p-3 font-semibold">Booking Ref</th>
                  <th className="p-3 font-semibold">User ID</th>
                  <th className="p-3 font-semibold">Start Time</th>
                  <th className="p-3 font-semibold">End Time</th>
                  <th className="p-3 font-semibold">Status</th>
                  <th className="p-3 font-semibold text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {relatedBookings.map((b) => (
                  <tr key={b.id} className="hover:bg-slate-850/40 transition-colors">
                    <td className="p-3 font-mono text-indigo-300 font-medium">
                      {b.bookingReference || `#${b.id}`}
                    </td>
                    <td className="p-3 font-mono text-slate-300">User #{b.userId}</td>
                    <td className="p-3 text-slate-300">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-slate-500" />
                        {new Date(b.startTime).toLocaleString()}
                      </div>
                    </td>
                    <td className="p-3 text-slate-300">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-slate-500" />
                        {new Date(b.endTime).toLocaleString()}
                      </div>
                    </td>
                    <td className="p-3">
                      <BookingStatusBadge status={b.status} />
                    </td>
                    <td className="p-3 text-right">
                      <Link
                        to={`/bookings/${b.id}`}
                        className="inline-flex items-center gap-1 px-2 py-1 text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg text-[11px] font-medium transition-colors"
                      >
                        Inspect
                        <ExternalLink className="w-3 h-3 text-slate-400" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
