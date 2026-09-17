import React, { useState, useEffect } from 'react';
import { X, AlertOctagon, Loader2, AlertCircle, Clock } from 'lucide-react';
import axios from 'axios';
import type {
  DowntimeLogResponse,
  RecordDowntimeLogDto,
  DowntimeReasonCategory,
  WorkOrderResponse,
} from '../../types/maintenance';
import type { EquipmentResponse } from '../../types/equipment';
import { getEquipmentList } from '../../api/equipment';
import { recordDowntimeLog, getWorkOrders } from '../../api/maintenance';

interface RecordDowntimeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: DowntimeLogResponse) => void;
  initialEquipmentId?: number;
  initialWorkOrderId?: number;
}

function getInitialDateTimeLocal(offsetMinutes = 0): string {
  const d = new Date(Date.now() + offsetMinutes * 60 * 1000);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

export const RecordDowntimeModal: React.FC<RecordDowntimeModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  initialEquipmentId,
  initialWorkOrderId,
}) => {
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrderResponse[]>([]);
  const [loadingLookups, setLoadingLookups] = useState<boolean>(false);

  const [equipmentId, setEquipmentId] = useState<string>('');
  const [workOrderId, setWorkOrderId] = useState<string>('');
  const [reasonCategory, setReasonCategory] = useState<DowntimeReasonCategory>('UNSCHEDULED_BREAKDOWN');
  const [downtimeStart, setDowntimeStart] = useState<string>(() => getInitialDateTimeLocal(-60));
  const [downtimeEnd, setDowntimeEnd] = useState<string>('');
  const [durationMinutes, setDurationMinutes] = useState<string>('');
  const [description, setDescription] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setLoadingLookups(true);

      const eqPromise = getEquipmentList();
      const woPromise = getWorkOrders();

      Promise.allSettled([eqPromise, woPromise])
        .then(([eqRes, woRes]) => {
          if (eqRes.status === 'fulfilled') {
            setEquipmentList(eqRes.value);
            if (initialEquipmentId) {
              setEquipmentId(String(initialEquipmentId));
            } else if (eqRes.value.length > 0) {
              setEquipmentId(String(eqRes.value[0].id));
            }
          }
          if (woRes.status === 'fulfilled') {
            setWorkOrders(woRes.value);
            if (initialWorkOrderId) {
              setWorkOrderId(String(initialWorkOrderId));
            }
          }
        })
        .finally(() => {
          setLoadingLookups(false);
        });
    }
  }, [isOpen, initialEquipmentId, initialWorkOrderId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!equipmentId) {
      setErrorMessage('Please select an equipment instrument.');
      return;
    }
    if (!downtimeStart) {
      setErrorMessage('Downtime start timestamp is required.');
      return;
    }
    if (!description.trim()) {
      setErrorMessage('A detailed description of the downtime incident is required.');
      return;
    }

    setIsSubmitting(true);

    try {
      const startIso = new Date(downtimeStart).toISOString();
      const endIso = downtimeEnd ? new Date(downtimeEnd).toISOString() : undefined;

      const payload: RecordDowntimeLogDto = {
        equipmentId: Number(equipmentId),
        workOrderId: workOrderId ? Number(workOrderId) : undefined,
        reasonCategory,
        downtimeStart: startIso,
        downtimeEnd: endIso,
        durationMinutes: durationMinutes ? Number(durationMinutes) : undefined,
        description: description.trim(),
      };

      const created = await recordDowntimeLog(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to record downtime:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Failed to record downtime (HTTP ${err.response?.status})`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to record downtime.');
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
          <div className="flex items-center gap-2 text-rose-400">
            <AlertOctagon className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Record Equipment Downtime</h2>
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
          {/* Error Alert */}
          {errorMessage && (
            <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form id="record-downtime-form" onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Equipment / Instrument <span className="text-rose-400">*</span>
              </label>
              <select
                value={equipmentId}
                onChange={(e) => setEquipmentId(e.target.value)}
                disabled={loadingLookups || isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
              >
                {loadingLookups ? (
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
                  Reason Category <span className="text-rose-400">*</span>
                </label>
                <select
                  value={reasonCategory}
                  onChange={(e) => setReasonCategory(e.target.value as DowntimeReasonCategory)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                >
                  <option value="UNSCHEDULED_BREAKDOWN">Unscheduled Breakdown</option>
                  <option value="SCHEDULED_MAINTENANCE">Scheduled Maintenance</option>
                  <option value="CALIBRATION">Calibration Service</option>
                  <option value="FACILITY_OUTAGE">Facility Outage</option>
                  <option value="SAFETY_HOLD">Safety Hold</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Associated Work Order (Optional)
                </label>
                <select
                  value={workOrderId}
                  onChange={(e) => setWorkOrderId(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                >
                  <option value="">-- None --</option>
                  {workOrders.map((wo) => (
                    <option key={wo.id} value={wo.id}>
                      {wo.workOrderNumber} ({wo.equipmentName} - {wo.status})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                  Downtime Start <span className="text-rose-400">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={downtimeStart}
                  onChange={(e) => setDowntimeStart(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                  Downtime End (Optional)
                </label>
                <input
                  type="datetime-local"
                  value={downtimeEnd}
                  onChange={(e) => setDowntimeEnd(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Duration in Minutes (Optional)
              </label>
              <input
                type="number"
                min="0"
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(e.target.value)}
                placeholder="Leave blank to calculate from start & end"
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Description of Outage / Failure <span className="text-rose-400">*</span>
              </label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Provide root failure observations, facility conditions, or safety interlock triggers..."
                disabled={isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
              />
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
            form="record-downtime-form"
            disabled={isSubmitting || loadingLookups}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-500 rounded-xl shadow-lg shadow-rose-600/20 transition-colors disabled:opacity-50"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            <span>{isSubmitting ? 'Recording...' : 'Record Downtime'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
