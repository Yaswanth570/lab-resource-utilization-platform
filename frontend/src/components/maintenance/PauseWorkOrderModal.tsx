import React, { useState } from 'react';
import { X, PauseCircle, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type { WorkOrderResponse, PauseWorkOrderDto } from '../../types/maintenance';
import { pauseWorkOrder } from '../../api/maintenance';

interface PauseWorkOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: WorkOrderResponse) => void;
  workOrder: WorkOrderResponse | null;
}

export const PauseWorkOrderModal: React.FC<PauseWorkOrderModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  workOrder,
}) => {
  const [reason, setReason] = useState<string>('Waiting for replacement components and calibration standard.');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  React.useEffect(() => {
    if (isOpen) {
      setReason('Waiting for replacement components and calibration standard.');
      setErrorMessage(null);
    }
  }, [isOpen]);

  if (!isOpen || !workOrder) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      const payload: PauseWorkOrderDto = {
        reason: reason.trim() || undefined,
      };

      const updated = await pauseWorkOrder(workOrder.id, payload);
      onSuccess(updated);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to pause work order:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Failed to pause work order (HTTP ${err.response?.status})`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to pause work order.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div className="flex items-center gap-2 text-amber-400">
            <PauseCircle className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Pause Work Order</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error Alert */}
        {errorMessage && (
          <div className="mx-6 mt-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/50 space-y-1 text-xs text-slate-300">
            <div>Work Order: <strong className="text-white">{workOrder.workOrderNumber}</strong></div>
            <div>Equipment: <span className="text-slate-200">{workOrder.equipmentName}</span></div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Reason for Pause / Part Dependency
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Explain required parts, shipment lead time, or vendor assistance needed..."
              disabled={isSubmitting}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50"
            />
          </div>

          <p className="text-xs text-slate-400">
            This transitions the order to <strong className="text-amber-300">WAITING_FOR_PARTS</strong>. Equipment remains under maintenance.
          </p>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
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
              disabled={isSubmitting}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-amber-600 hover:bg-amber-500 rounded-xl shadow-lg shadow-amber-600/20 transition-colors disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Pausing...' : 'Confirm Pause'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
