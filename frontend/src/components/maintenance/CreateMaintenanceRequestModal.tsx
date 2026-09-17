import React, { useState, useEffect } from 'react';
import { X, Wrench, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type { MaintenanceRequestResponse, MaintenancePriority, CreateMaintenanceRequestDto } from '../../types/maintenance';
import type { EquipmentResponse } from '../../types/equipment';
import { getEquipmentList } from '../../api/equipment';
import { createMaintenanceRequest } from '../../api/maintenance';

interface CreateMaintenanceRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: MaintenanceRequestResponse) => void;
  initialEquipmentId?: number;
}

export const CreateMaintenanceRequestModal: React.FC<CreateMaintenanceRequestModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  initialEquipmentId,
}) => {
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [equipmentLoading, setEquipmentLoading] = useState<boolean>(false);

  const [equipmentId, setEquipmentId] = useState<string>(initialEquipmentId ? String(initialEquipmentId) : '');
  const [priority, setPriority] = useState<MaintenancePriority>('MEDIUM');
  const [issueTitle, setIssueTitle] = useState<string>('');
  const [issueDescription, setIssueDescription] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setEquipmentLoading(true);
      getEquipmentList()
        .then((items) => {
          setEquipmentList(items);
          if (initialEquipmentId) {
            setEquipmentId(String(initialEquipmentId));
          } else if (items.length > 0 && !equipmentId) {
            setEquipmentId(String(items[0].id));
          }
        })
        .catch((err) => {
          console.error('Failed to load equipment catalog:', err);
          setErrorMessage('Could not load equipment catalog for selection.');
        })
        .finally(() => {
          setEquipmentLoading(false);
        });
    }
  }, [isOpen, initialEquipmentId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!equipmentId) {
      setErrorMessage('Please select an equipment instrument.');
      return;
    }
    if (!issueTitle.trim()) {
      setErrorMessage('Issue title is required.');
      return;
    }
    if (!issueDescription.trim()) {
      setErrorMessage('Issue description is required.');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload: CreateMaintenanceRequestDto = {
        equipmentId: Number(equipmentId),
        priority,
        issueTitle: issueTitle.trim(),
        issueDescription: issueDescription.trim(),
      };

      const created = await createMaintenanceRequest(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to create maintenance request:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Submission failed with HTTP ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to submit maintenance request.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div className="flex items-center gap-2 text-indigo-400">
            <Wrench className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">New Maintenance Request</h2>
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
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Select Instrument / Equipment <span className="text-rose-400">*</span>
            </label>
            <select
              value={equipmentId}
              onChange={(e) => setEquipmentId(e.target.value)}
              disabled={equipmentLoading || isSubmitting}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
            >
              {equipmentLoading ? (
                <option value="">Loading equipment catalog...</option>
              ) : (
                equipmentList.map((eq) => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} ({eq.assetTag}) — {eq.status}
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Priority Level <span className="text-rose-400">*</span>
              </label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value as MaintenancePriority)}
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Issue Title <span className="text-rose-400">*</span>
              </label>
              <input
                type="text"
                value={issueTitle}
                onChange={(e) => setIssueTitle(e.target.value)}
                placeholder="e.g. Laser diode calibration error"
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Detailed Description <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={issueDescription}
              onChange={(e) => setIssueDescription(e.target.value)}
              placeholder="Describe symptoms, error codes, and operational impact observed..."
              disabled={isSubmitting}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
            />
          </div>

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
              disabled={isSubmitting || equipmentLoading}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-colors disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Submitting...' : 'Submit Request'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
