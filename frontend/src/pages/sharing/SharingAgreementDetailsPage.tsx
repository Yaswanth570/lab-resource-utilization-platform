import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  FileText,
  ArrowLeft,
  Calendar,
  Building2,
  Clock,
  ExternalLink,
  RefreshCw,
  AlertCircle,
  Layers,
  CheckCircle2,
  XCircle,
  PauseCircle,
  PlayCircle,
  Plus,
  Tag,
  Percent,
} from 'lucide-react';
import type {
  ResourceSharingAgreementResponse,
  SharedEquipmentAllocationResponse,
  SharingAgreementStatus,
} from '../../types/sharing';
import type { InstitutionLookup } from '../../types/equipment';
import {
  getSharingAgreementById,
  updateAgreementStatus,
  getSharedAllocations,
  getInstitutions,
} from '../../api/sharing';
import { SharingAgreementStatusBadge } from '../../components/sharing/SharingAgreementStatusBadge';
import { CreateSharedAllocationModal } from '../../components/sharing/CreateSharedAllocationModal';

export const SharingAgreementDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [agreement, setAgreement] = useState<ResourceSharingAgreementResponse | null>(null);
  const [allocations, setAllocations] = useState<SharedEquipmentAllocationResponse[]>([]);
  const [institutions, setInstitutions] = useState<InstitutionLookup[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<boolean>(false);

  const [isAddAllocationOpen, setIsAddAllocationOpen] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  const loadData = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const [instList] = await Promise.all([
        getInstitutions().catch(() => []),
      ]);
      setInstitutions(instList);

      const agData = await getSharingAgreementById(id);
      setAgreement(agData);

      try {
        const allocList = await getSharedAllocations({ sharingAgreementId: Number(id) });
        setAllocations(allocList);
      } catch (err) {
        console.warn('Could not load allocations for agreement:', err);
      }
    } catch (err: unknown) {
      console.error('Failed to load agreement details:', err);
      setErrorMessage(
        'Could not load sharing agreement details. The backend REST endpoint may not be implemented yet or the agreement does not exist.'
      );
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleStatusChange = async (newStatus: SharingAgreementStatus) => {
    if (!agreement) return;
    setActionLoading(true);
    try {
      const updated = await updateAgreementStatus(agreement.id, newStatus);
      setAgreement(updated);
      showToast(`Agreement status updated to ${newStatus}`);
    } catch (err: unknown) {
      console.error('Failed to update status:', err);
      // If endpoint doesn't exist, provide honest feedback
      alert(
        `Unable to update agreement status to ${newStatus}. The backend status update endpoint (/api/sharing/agreements/:id/status) may not be implemented in the REST layer.`
      );
    } finally {
      setActionLoading(false);
    }
  };

  const getInstitutionName = (instId?: number | null) => {
    if (!instId) return 'N/A';
    const found = institutions.find((i) => i.id === instId);
    return found ? `${found.name} (${found.code})` : `Institution #${instId}`;
  };

  if (isLoading) {
    return (
      <div className="p-16 text-center bg-slate-900 border border-slate-800 rounded-2xl">
        <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
        <p className="text-xs text-slate-400 mt-3">Loading sharing agreement details...</p>
      </div>
    );
  }

  if (errorMessage || !agreement) {
    return (
      <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
        <h2 className="text-base font-semibold text-white">Sharing Agreement Unavailable</h2>
        <p className="text-xs text-slate-400 max-w-lg mx-auto">
          {errorMessage || 'Agreement details could not be found.'}
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
              <h1 className="text-xl font-bold text-white font-mono">{agreement.agreementCode}</h1>
              <SharingAgreementStatusBadge status={agreement.status} />
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Created on {new Date(agreement.createdAt).toLocaleDateString()} &middot; Last updated{' '}
              {new Date(agreement.updatedAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        {/* Lifecycle Action Buttons */}
        <div className="flex items-center gap-2 flex-wrap">
          {agreement.status === 'ACTIVE' && (
            <>
              <button
                type="button"
                disabled={actionLoading}
                onClick={() => handleStatusChange('SUSPENDED')}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-xl transition-colors disabled:opacity-50"
              >
                <PauseCircle className="w-3.5 h-3.5" />
                Suspend Agreement
              </button>
              <button
                type="button"
                disabled={actionLoading}
                onClick={() => handleStatusChange('TERMINATED')}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-rose-300 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 rounded-xl transition-colors disabled:opacity-50"
              >
                <XCircle className="w-3.5 h-3.5" />
                Terminate
              </button>
            </>
          )}

          {agreement.status === 'SUSPENDED' && (
            <>
              <button
                type="button"
                disabled={actionLoading}
                onClick={() => handleStatusChange('ACTIVE')}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-xl transition-colors disabled:opacity-50"
              >
                <PlayCircle className="w-3.5 h-3.5" />
                Reactivate Agreement
              </button>
              <button
                type="button"
                disabled={actionLoading}
                onClick={() => handleStatusChange('TERMINATED')}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-rose-300 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 rounded-xl transition-colors disabled:opacity-50"
              >
                <XCircle className="w-3.5 h-3.5" />
                Terminate
              </button>
            </>
          )}

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

      {/* Main Info Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Owner Institution */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-indigo-400" />
              Owner Institution
            </span>
            <span className="text-[10px] font-mono text-slate-500">ID #{agreement.ownerInstitutionId}</span>
          </div>
          <div className="text-sm font-semibold text-white truncate">
            {agreement.ownerInstitutionName || getInstitutionName(agreement.ownerInstitutionId)}
          </div>
        </div>

        {/* Requesting / Partner Institution */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-cyan-400" />
              Partner Institution
            </span>
            <span className="text-[10px] font-mono text-slate-500">ID #{agreement.requestingInstitutionId}</span>
          </div>
          <div className="text-sm font-semibold text-white truncate">
            {agreement.requestingInstitutionName || getInstitutionName(agreement.requestingInstitutionId)}
          </div>
        </div>

        {/* Billing Rate Multiplier */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Percent className="w-3.5 h-3.5 text-emerald-400" />
              Billing Multiplier
            </span>
            <span className="text-[10px] text-emerald-400 font-mono">External</span>
          </div>
          <div className="text-lg font-bold text-emerald-400 font-mono">
            {Number(agreement.billingRateMultiplier).toFixed(2)}x
          </div>
        </div>

        {/* Monthly Hours Cap */}
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span className="flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-amber-400" />
              Monthly Quota
            </span>
          </div>
          <div className="text-lg font-bold text-white font-mono">
            {agreement.maxMonthlyHours !== null && agreement.maxMonthlyHours !== undefined
              ? `${agreement.maxMonthlyHours} hrs`
              : 'Uncapped'}
          </div>
        </div>
      </div>

      {/* Validity Window Details & Contract Specs */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <h2 className="text-sm font-semibold text-white flex items-center gap-2">
            <FileText className="w-4 h-4 text-indigo-400" />
            Agreement Parameters & Timeline
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-3 bg-slate-850/60 border border-slate-800 rounded-lg">
              <span className="text-slate-400 block mb-1">Effective Start Date</span>
              <div className="flex items-center gap-2 text-white font-medium">
                <Calendar className="w-4 h-4 text-indigo-400" />
                <span>{agreement.startDate}</span>
              </div>
            </div>

            <div className="p-3 bg-slate-850/60 border border-slate-800 rounded-lg">
              <span className="text-slate-400 block mb-1">Expiration / End Date</span>
              <div className="flex items-center gap-2 text-white font-medium">
                <Calendar className="w-4 h-4 text-indigo-400" />
                <span>{agreement.endDate}</span>
              </div>
            </div>

            <div className="p-3 bg-slate-850/60 border border-slate-800 rounded-lg">
              <span className="text-slate-400 block mb-1">Associated Request ID</span>
              <span className="text-white font-mono">
                {agreement.sharingRequestId ? `#${agreement.sharingRequestId}` : 'Direct Administrative Agreement'}
              </span>
            </div>

            <div className="p-3 bg-slate-850/60 border border-slate-800 rounded-lg">
              <span className="text-slate-400 block mb-1">Internal Reference ID</span>
              <span className="text-white font-mono">PK #{agreement.id}</span>
            </div>
          </div>

          <div className="p-3 bg-indigo-950/20 border border-indigo-500/20 rounded-lg text-xs text-indigo-200 leading-relaxed">
            <p className="font-medium text-indigo-300 mb-1">Cross-Institutional Operating Policy</p>
            Bookings made by partner institution researchers under this agreement apply the{' '}
            <strong className="text-white">{Number(agreement.billingRateMultiplier).toFixed(2)}x</strong> billing rate
            multiplier against standard catalog base rates. Access is constrained to explicitly allocated equipment
            instruments.
          </div>
        </div>

        {/* Quick Summary / Status Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
          <h2 className="text-sm font-semibold text-white flex items-center gap-2">
            <Tag className="w-4 h-4 text-cyan-400" />
            Allocations Snapshot
          </h2>

          <div className="space-y-3 text-xs">
            <div className="flex items-center justify-between p-2.5 bg-slate-850/60 rounded-lg border border-slate-800">
              <span className="text-slate-400">Allocated Instruments</span>
              <span className="text-white font-bold font-mono">{allocations.length}</span>
            </div>
            <div className="flex items-center justify-between p-2.5 bg-slate-850/60 rounded-lg border border-slate-800">
              <span className="text-slate-400">Active Allocations</span>
              <span className="text-emerald-400 font-bold font-mono">
                {allocations.filter((a) => a.isActive).length}
              </span>
            </div>
            <div className="flex items-center justify-between p-2.5 bg-slate-850/60 rounded-lg border border-slate-800">
              <span className="text-slate-400">Agreement Lifecycle</span>
              <SharingAgreementStatusBadge status={agreement.status} />
            </div>
          </div>

          <button
            type="button"
            onClick={() => setIsAddAllocationOpen(true)}
            className="w-full inline-flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            Allocate Equipment
          </button>
        </div>
      </div>

      {/* Equipment Allocations Section */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-indigo-400" />
            <h2 className="text-sm font-semibold text-white">Covered Equipment Allocations</h2>
            <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded-full">
              {allocations.length}
            </span>
          </div>

          <button
            type="button"
            onClick={() => setIsAddAllocationOpen(true)}
            className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold text-indigo-300 hover:text-white bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/30 rounded-lg transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            New Allocation
          </button>
        </div>

        {allocations.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            <Layers className="w-8 h-8 text-slate-600 mx-auto mb-2" />
            <p className="font-medium text-slate-300">No equipment currently allocated under this agreement</p>
            <p className="text-[11px] text-slate-500 mt-1">
              Add an equipment allocation to make specific lab resources available to partner institutions.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-850/60 text-slate-400 border-b border-slate-800">
                  <th className="p-3 font-semibold">Allocation ID</th>
                  <th className="p-3 font-semibold">Equipment Instrument</th>
                  <th className="p-3 font-semibold">Asset Tag</th>
                  <th className="p-3 font-semibold">Custom Rate</th>
                  <th className="p-3 font-semibold">Allocation Status</th>
                  <th className="p-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {allocations.map((alloc) => (
                  <tr key={alloc.id} className="hover:bg-slate-850/40 transition-colors">
                    <td className="p-3 font-mono text-slate-300">#{alloc.id}</td>
                    <td className="p-3">
                      <Link
                        to={`/equipment/${alloc.equipmentId}`}
                        className="font-medium text-indigo-300 hover:text-indigo-200 hover:underline flex items-center gap-1"
                      >
                        {alloc.equipmentName || `Equipment #${alloc.equipmentId}`}
                        <ExternalLink className="w-3 h-3 text-slate-500" />
                      </Link>
                    </td>
                    <td className="p-3 font-mono text-slate-400">{alloc.equipmentAssetTag || 'N/A'}</td>
                    <td className="p-3 font-mono text-emerald-400">
                      {alloc.customHourlyRate !== null && alloc.customHourlyRate !== undefined
                        ? `$${Number(alloc.customHourlyRate).toFixed(2)}/hr`
                        : 'Standard Multiplier'}
                    </td>
                    <td className="p-3">
                      {alloc.isActive ? (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-500/10 text-slate-400 border border-slate-500/20">
                          Inactive
                        </span>
                      )}
                    </td>
                    <td className="p-3 text-right">
                      <Link
                        to={`/sharing/allocations/${alloc.id}`}
                        className="inline-flex items-center gap-1 px-2 py-1 text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg text-[11px] font-medium transition-colors"
                      >
                        Details
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

      {/* Add Allocation Modal */}
      <CreateSharedAllocationModal
        isOpen={isAddAllocationOpen}
        onClose={() => setIsAddAllocationOpen(false)}
        onSuccess={() => {
          setIsAddAllocationOpen(false);
          showToast('Equipment allocated to agreement successfully.');
          loadData();
        }}
        preselectedAgreementId={agreement.id}
      />
    </div>
  );
};
