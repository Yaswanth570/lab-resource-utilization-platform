import React, { useState } from 'react';
import { X, AlertTriangle, Loader2, RefreshCw } from 'lucide-react';
import axios from 'axios';
import type { EquipmentResponse, EquipmentStatus } from '../../types/equipment';
import { updateEquipmentStatus } from '../../api/equipment';
import { EquipmentStatusBadge } from './EquipmentStatusBadge';

interface StatusTransitionModalProps {
  isOpen: boolean;
  onClose: () => void;
  equipment: EquipmentResponse;
  onSuccess: (updated: EquipmentResponse) => void;
}

const ALL_STATUSES: EquipmentStatus[] = [
  'AVAILABLE',
  'IN_USE',
  'UNDER_MAINTENANCE',
  'OUT_OF_SERVICE',
  'RETIRED',
];

export const StatusTransitionModal: React.FC<StatusTransitionModalProps> = ({
  isOpen,
  onClose,
  equipment,
  onSuccess,
}) => {
  const [selectedStatus, setSelectedStatus] = useState<EquipmentStatus>(equipment.status);
  const [reason, setReason] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const isSevere = selectedStatus === 'OUT_OF_SERVICE' || selectedStatus === 'RETIRED';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedStatus === equipment.status) {
      onClose();
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const updated = await updateEquipmentStatus(equipment.id, {
        status: selectedStatus,
        reason: reason.trim() || undefined,
      });
      onSuccess(updated);
      onClose();
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const msg = err.response?.data?.message || err.response?.data?.error || err.message;
        setErrorMessage(msg || 'Failed to update equipment operational status.');
      } else {
        setErrorMessage('An unexpected error occurred while transitioning status.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/70 backdrop-blur-xs">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
              <RefreshCw className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white tracking-tight">
                Update Operational Status
              </h3>
              <p className="text-[11px] text-slate-400">
                {equipment.name} ({equipment.assetTag})
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {errorMessage && (
            <div className="flex items-start gap-2.5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs">
              <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">
              Current Status
            </label>
            <div>
              <EquipmentStatusBadge status={equipment.status} />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Target Status <span className="text-rose-400">*</span>
            </label>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value as EquipmentStatus)}
              className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 transition-colors"
            >
              {ALL_STATUSES.map((st) => (
                <option key={st} value={st}>
                  {st.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>

          {isSevere && (
            <div className="flex items-start gap-2.5 p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
              <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
              <span>
                Caution: Setting equipment to{' '}
                <strong className="font-semibold">{selectedStatus.replace(/_/g, ' ')}</strong> will
                prevent any new bookings or usage sessions.
              </span>
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Transition Reason / Log Notes
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Scheduled sensor calibration and diagnostic test run"
              className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 transition-colors"
            />
          </div>

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-3.5 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || selectedStatus === equipment.status}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl transition-colors disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  <span>Updating...</span>
                </>
              ) : (
                <span>Confirm Status Change</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
