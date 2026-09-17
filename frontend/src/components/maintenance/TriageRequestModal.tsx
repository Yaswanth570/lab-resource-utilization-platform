import React, { useState } from 'react';
import { X, AlertTriangle, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type { MaintenanceRequestResponse, MaintenancePriority, TriageMaintenanceRequestDto } from '../../types/maintenance';
import { triageMaintenanceRequest } from '../../api/maintenance';

interface TriageRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: MaintenanceRequestResponse) => void;
  request: MaintenanceRequestResponse | null;
}

export const TriageRequestModal: React.FC<TriageRequestModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  request,
}) => {
  const [priority, setPriority] = useState<MaintenancePriority>(request?.priority || 'MEDIUM');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Sync state if request changes
  React.useEffect(() => {
    if (request) {
      setPriority(request.priority || 'MEDIUM');
      setErrorMessage(null);
    }
  }, [request]);

  if (!isOpen || !request) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      const payload: TriageMaintenanceRequestDto = {
        priority,
      };

      const updated = await triageMaintenanceRequest(request.id, payload);
      onSuccess(updated);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to triage request:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Triage failed with HTTP ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to triage maintenance request.');
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
          <div className="flex items-center gap-2 text-cyan-400">
            <AlertTriangle className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Triage Maintenance Request</h2>
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
            <div>Request: <strong className="text-white">{request.requestNumber}</strong></div>
            <div>Equipment: <span className="text-slate-200">{request.equipmentName || `ID ${request.equipmentId}`}</span></div>
            <div>Issue: <span className="text-slate-200 font-medium">{request.issueTitle}</span></div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Confirm / Adjust Priority Level
            </label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as MaintenancePriority)}
              disabled={isSubmitting}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-cyan-500/50"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>

          <p className="text-xs text-slate-400">
            Triaging moves this request into the <strong className="text-cyan-300">TRIAGED</strong> state and records your user as the triaging reviewer.
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
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-cyan-600 hover:bg-cyan-500 rounded-xl shadow-lg shadow-cyan-600/20 transition-colors disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Triaging...' : 'Confirm Triage'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
