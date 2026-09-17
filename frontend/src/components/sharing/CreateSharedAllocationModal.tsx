import React, { useState, useEffect } from 'react';
import { X, Layers, Loader2, AlertCircle, DollarSign } from 'lucide-react';
import axios from 'axios';
import type {
  SharedEquipmentAllocationResponse,
  CreateSharedAllocationRequest,
  ResourceSharingAgreementResponse,
} from '../../types/sharing';
import type { EquipmentResponse } from '../../types/equipment';
import { getExternallyShareableEquipment, createSharedAllocation } from '../../api/sharing';

interface CreateSharedAllocationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: SharedEquipmentAllocationResponse) => void;
  availableAgreements?: ResourceSharingAgreementResponse[];
  preselectedAgreementId?: number;
}

export const CreateSharedAllocationModal: React.FC<CreateSharedAllocationModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  availableAgreements = [],
  preselectedAgreementId,
}) => {
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [loadingEquipment, setLoadingEquipment] = useState<boolean>(false);

  const [agreementId, setAgreementId] = useState<string>(preselectedAgreementId ? String(preselectedAgreementId) : '');
  const [equipmentId, setEquipmentId] = useState<string>('');
  const [customHourlyRate, setCustomHourlyRate] = useState<string>('');
  const [isActive, setIsActive] = useState<boolean>(true);

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setLoadingEquipment(true);
      if (preselectedAgreementId) {
        setAgreementId(String(preselectedAgreementId));
      } else if (availableAgreements.length > 0 && !agreementId) {
        setAgreementId(String(availableAgreements[0].id));
      }

      getExternallyShareableEquipment()
        .then((items) => {
          setEquipmentList(items);
          if (items.length > 0) {
            setEquipmentId(String(items[0].id));
            if (items[0].hourlyRateExternal !== null && items[0].hourlyRateExternal !== undefined) {
              setCustomHourlyRate(String(items[0].hourlyRateExternal));
            }
          }
        })
        .catch((err) => {
          console.error('Failed to load shareable equipment:', err);
          setErrorMessage('Could not load externally shareable equipment.');
        })
        .finally(() => {
          setLoadingEquipment(false);
        });
    }
  }, [isOpen, preselectedAgreementId, availableAgreements]);

  const handleEquipmentChange = (selectedId: string) => {
    setEquipmentId(selectedId);
    const found = equipmentList.find((e) => String(e.id) === selectedId);
    if (found && found.hourlyRateExternal !== null && found.hourlyRateExternal !== undefined) {
      setCustomHourlyRate(String(found.hourlyRateExternal));
    }
  };

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!agreementId) {
      setErrorMessage('A valid sharing agreement reference is required.');
      return;
    }

    if (!equipmentId) {
      setErrorMessage('Please select an externally shareable instrument.');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload: CreateSharedAllocationRequest = {
        sharingAgreementId: Number(agreementId),
        equipmentId: Number(equipmentId),
        customHourlyRate: customHourlyRate ? Number(customHourlyRate) : undefined,
        isActive,
      };

      const created = await createSharedAllocation(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to allocate shared equipment:', err);
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 404) {
          setErrorMessage('Backend shared allocation endpoint is not yet provisioned. Skipping submission.');
        } else {
          setErrorMessage(
            err.response?.data?.message ||
            err.response?.data?.error ||
            `Failed to create allocation (HTTP ${err.response?.status})`
          );
        }
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to create shared allocation.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90 shrink-0">
          <div className="flex items-center gap-2 text-indigo-400">
            <Layers className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Allocate Shared Instrument</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Body */}
        <div className="overflow-y-auto flex-1 p-6 space-y-4">
          {/* Error Alert */}
          {errorMessage && (
            <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form id="create-allocation-form" onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Resource Sharing Agreement <span className="text-rose-400">*</span>
              </label>
              {availableAgreements.length > 0 ? (
                <select
                  value={agreementId}
                  onChange={(e) => setAgreementId(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  {availableAgreements.map((ag) => (
                    <option key={ag.id} value={ag.id}>
                      {ag.agreementCode} ({ag.ownerInstitutionName || 'Owner'} ↔ {ag.requestingInstitutionName || 'Partner'})
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  type="number"
                  value={agreementId}
                  onChange={(e) => setAgreementId(e.target.value)}
                  placeholder="Enter active sharing agreement ID"
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 font-mono"
                />
              )}
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Externally Shareable Equipment <span className="text-rose-400">*</span>
              </label>
              <select
                value={equipmentId}
                onChange={(e) => handleEquipmentChange(e.target.value)}
                disabled={loadingEquipment || isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              >
                {loadingEquipment ? (
                  <option value="">Loading equipment catalog...</option>
                ) : equipmentList.length === 0 ? (
                  <option value="">No instruments flagged for external sharing</option>
                ) : (
                  equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name} ({eq.assetTag}) — Ext Rate: ${eq.hourlyRateExternal ?? 'N/A'}/hr
                    </option>
                  ))
                )}
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1">
                <DollarSign className="w-3.5 h-3.5 text-emerald-400" />
                Custom Hourly Rate Override ($) (Optional)
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={customHourlyRate}
                onChange={(e) => setCustomHourlyRate(e.target.value)}
                placeholder="Leave blank to use default external catalog rate"
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              />
            </div>

            <div className="pt-2">
              <label className="flex items-center gap-2 text-xs text-slate-300 font-medium cursor-pointer">
                <input
                  type="checkbox"
                  checked={isActive}
                  onChange={(e) => setIsActive(e.target.checked)}
                  className="rounded bg-slate-800 border-slate-700 text-indigo-600 focus:ring-indigo-500/50"
                />
                <span>Allocation is active immediately upon creation</span>
              </label>
            </div>
          </form>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-800 bg-slate-900/90 shrink-0">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            form="create-allocation-form"
            disabled={isSubmitting || loadingEquipment}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-colors disabled:opacity-50"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            <span>{isSubmitting ? 'Allocating...' : 'Confirm Allocation'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
