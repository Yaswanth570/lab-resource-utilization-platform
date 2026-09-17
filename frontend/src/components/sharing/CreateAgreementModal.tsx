import React, { useState, useEffect } from 'react';
import { X, Handshake, Loader2, AlertCircle, Calendar } from 'lucide-react';
import axios from 'axios';
import type {
  ResourceSharingAgreementResponse,
  CreateSharingAgreementRequest,
} from '../../types/sharing';
import type { InstitutionLookup } from '../../types/equipment';
import { getInstitutions, createSharingAgreement } from '../../api/sharing';

interface CreateAgreementModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: ResourceSharingAgreementResponse) => void;
}

function getDefaultDates(): { start: string; end: string } {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const nextYear = yyyy + 1;
  return {
    start: `${yyyy}-${mm}-${dd}`,
    end: `${nextYear}-${mm}-${dd}`,
  };
}

export const CreateAgreementModal: React.FC<CreateAgreementModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [institutions, setInstitutions] = useState<InstitutionLookup[]>([]);
  const [loadingInstitutions, setLoadingInstitutions] = useState<boolean>(false);

  const [ownerInstitutionId, setOwnerInstitutionId] = useState<string>('');
  const [requestingInstitutionId, setRequestingInstitutionId] = useState<string>('');
  const [agreementCode, setAgreementCode] = useState<string>('');
  const [billingMultiplier, setBillingMultiplier] = useState<string>('1.00');
  const [maxMonthlyHours, setMaxMonthlyHours] = useState<string>('40');
  const [startDate, setStartDate] = useState<string>(() => getDefaultDates().start);
  const [endDate, setEndDate] = useState<string>(() => getDefaultDates().end);

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setLoadingInstitutions(true);
      getInstitutions()
        .then((items) => {
          setInstitutions(items);
          if (items.length > 0) {
            setOwnerInstitutionId(String(items[0].id));
            if (items.length > 1) {
              setRequestingInstitutionId(String(items[1].id));
            } else {
              setRequestingInstitutionId(String(items[0].id));
            }
          }
        })
        .catch((err) => {
          console.error('Failed to load institutions:', err);
          setErrorMessage('Could not load institution roster.');
        })
        .finally(() => {
          setLoadingInstitutions(false);
        });
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!ownerInstitutionId || !requestingInstitutionId) {
      setErrorMessage('Both owner and partner institutions must be selected.');
      return;
    }

    if (ownerInstitutionId === requestingInstitutionId) {
      setErrorMessage('Owner institution and requesting partner institution must be distinct entities.');
      return;
    }

    if (!startDate || !endDate) {
      setErrorMessage('Start date and end date are required.');
      return;
    }

    if (new Date(endDate) <= new Date(startDate)) {
      setErrorMessage('Agreement end date must be after the start date.');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload: CreateSharingAgreementRequest = {
        agreementCode: agreementCode.trim() || undefined,
        ownerInstitutionId: Number(ownerInstitutionId),
        requestingInstitutionId: Number(requestingInstitutionId),
        billingRateMultiplier: billingMultiplier ? Number(billingMultiplier) : 1.0,
        maxMonthlyHours: maxMonthlyHours ? Number(maxMonthlyHours) : undefined,
        startDate,
        endDate,
      };

      const created = await createSharingAgreement(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to create agreement:', err);
      if (axios.isAxiosError(err)) {
        if (err.response?.status === 404) {
          setErrorMessage('Backend sharing agreement endpoint is not yet provisioned. Skipping submission.');
        } else {
          setErrorMessage(
            err.response?.data?.message ||
            err.response?.data?.error ||
            `Failed to create agreement (HTTP ${err.response?.status})`
          );
        }
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to create agreement.');
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
            <Handshake className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">New Resource Sharing Agreement</h2>
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

          <form id="create-agreement-form" onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Owner Institution <span className="text-rose-400">*</span>
                </label>
                <select
                  value={ownerInstitutionId}
                  onChange={(e) => setOwnerInstitutionId(e.target.value)}
                  disabled={loadingInstitutions || isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  {loadingInstitutions ? (
                    <option value="">Loading institutions...</option>
                  ) : (
                    institutions.map((inst) => (
                      <option key={inst.id} value={inst.id}>
                        {inst.name} ({inst.code})
                      </option>
                    ))
                  )}
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Partner / Requesting Institution <span className="text-rose-400">*</span>
                </label>
                <select
                  value={requestingInstitutionId}
                  onChange={(e) => setRequestingInstitutionId(e.target.value)}
                  disabled={loadingInstitutions || isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  {loadingInstitutions ? (
                    <option value="">Loading institutions...</option>
                  ) : (
                    institutions.map((inst) => (
                      <option key={inst.id} value={inst.id}>
                        {inst.name} ({inst.code})
                      </option>
                    ))
                  )}
                </select>
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Agreement Code / Reference (Optional)
              </label>
              <input
                type="text"
                value={agreementCode}
                onChange={(e) => setAgreementCode(e.target.value)}
                placeholder="e.g. SA-2026-NSTI-IITB-01 (Auto-generated if blank)"
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Billing Rate Multiplier
                </label>
                <input
                  type="number"
                  step="0.05"
                  min="0.1"
                  max="10.0"
                  value={billingMultiplier}
                  onChange={(e) => setBillingMultiplier(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Max Monthly Hours Quota
                </label>
                <input
                  type="number"
                  min="1"
                  value={maxMonthlyHours}
                  onChange={(e) => setMaxMonthlyHours(e.target.value)}
                  placeholder="e.g. 40 hours"
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  Start Date <span className="text-rose-400">*</span>
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  End Date <span className="text-rose-400">*</span>
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
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
            form="create-agreement-form"
            disabled={isSubmitting || loadingInstitutions}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-colors disabled:opacity-50"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            <span>{isSubmitting ? 'Creating...' : 'Establish Agreement'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
