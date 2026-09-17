import React, { useState } from 'react';
import { X, CheckCircle2, Loader2, AlertCircle, Clock, DollarSign } from 'lucide-react';
import axios from 'axios';
import type { WorkOrderResponse, CompleteWorkOrderDto } from '../../types/maintenance';
import { completeWorkOrder } from '../../api/maintenance';

interface CompleteWorkOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: WorkOrderResponse) => void;
  workOrder: WorkOrderResponse | null;
}

function getInitialDateTimeLocal(): string {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

export const CompleteWorkOrderModal: React.FC<CompleteWorkOrderModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  workOrder,
}) => {
  const [actualEnd, setActualEnd] = useState<string>(getInitialDateTimeLocal);
  const [laborHours, setLaborHours] = useState<string>('2.5');
  const [laborCost, setLaborCost] = useState<string>('150.00');
  const [partsCost, setPartsCost] = useState<string>('0.00');
  const [workPerformedSummary, setWorkPerformedSummary] = useState<string>('');
  const [failureRootCause, setFailureRootCause] = useState<string>('');
  const [resolutionNotes, setResolutionNotes] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  React.useEffect(() => {
    if (isOpen) {
      setActualEnd(getInitialDateTimeLocal());
      setLaborHours('2.5');
      setLaborCost('150.00');
      setPartsCost('0.00');
      setWorkPerformedSummary('');
      setFailureRootCause('');
      setResolutionNotes('');
      setErrorMessage(null);
    }
  }, [isOpen]);

  if (!isOpen || !workOrder) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    setIsSubmitting(true);

    try {
      const endIso = actualEnd ? new Date(actualEnd).toISOString() : undefined;
      const payload: CompleteWorkOrderDto = {
        actualEnd: endIso,
        laborHours: laborHours ? Number(laborHours) : undefined,
        laborCost: laborCost ? Number(laborCost) : undefined,
        partsCost: partsCost ? Number(partsCost) : undefined,
        workPerformedSummary: workPerformedSummary.trim() || undefined,
        failureRootCause: failureRootCause.trim() || undefined,
        resolutionNotes: resolutionNotes.trim() || undefined,
      };

      const updated = await completeWorkOrder(workOrder.id, payload);
      onSuccess(updated);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to complete work order:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Failed to complete work order (HTTP ${err.response?.status})`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to complete work order.');
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
          <div className="flex items-center gap-2 text-emerald-400">
            <CheckCircle2 className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Complete Work Order</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Form Body */}
        <div className="overflow-y-auto flex-1 p-6 space-y-4">
          <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/50 space-y-1 text-xs text-slate-300">
            <div>Work Order: <strong className="text-white">{workOrder.workOrderNumber}</strong></div>
            <div>Equipment: <span className="text-slate-200">{workOrder.equipmentName}</span></div>
            <div>Started At: <span className="text-amber-400 font-mono">{workOrder.actualStart ? new Date(workOrder.actualStart).toLocaleString() : 'N/A'}</span></div>
          </div>

          {/* Error Alert */}
          {errorMessage && (
            <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form id="complete-work-order-form" onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                Actual Completion Timestamp
              </label>
              <input
                type="datetime-local"
                value={actualEnd}
                onChange={(e) => setActualEnd(e.target.value)}
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Labor Hours
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  value={laborHours}
                  onChange={(e) => setLaborHours(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                />
              </div>
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1">
                  <DollarSign className="w-3 h-3 text-slate-400" /> Labor Cost ($)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={laborCost}
                  onChange={(e) => setLaborCost(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                />
              </div>
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1">
                  <DollarSign className="w-3 h-3 text-slate-400" /> Parts Cost ($)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={partsCost}
                  onChange={(e) => setPartsCost(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Work Performed Summary
              </label>
              <textarea
                rows={2}
                value={workPerformedSummary}
                onChange={(e) => setWorkPerformedSummary(e.target.value)}
                placeholder="Describe procedures, replacements, testing protocols performed..."
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Failure Root Cause
              </label>
              <input
                type="text"
                value={failureRootCause}
                onChange={(e) => setFailureRootCause(e.target.value)}
                placeholder="e.g. Component thermal fatigue, lens dust accumulation"
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Resolution & Validation Notes
              </label>
              <textarea
                rows={2}
                value={resolutionNotes}
                onChange={(e) => setResolutionNotes(e.target.value)}
                placeholder="Final inspection results and readiness verification..."
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <p className="text-xs text-slate-400">
              Completing this work order automatically restores equipment operational status if no other active maintenance work orders exist.
            </p>
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
            form="complete-work-order-form"
            disabled={isSubmitting}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 rounded-xl shadow-lg shadow-emerald-600/20 transition-colors disabled:opacity-50"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            <span>{isSubmitting ? 'Completing...' : 'Mark Completed'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
