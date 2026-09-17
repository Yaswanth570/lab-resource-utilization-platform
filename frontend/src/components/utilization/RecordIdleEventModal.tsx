import React, { useState, useEffect } from 'react';
import { X, Loader2, AlertCircle, AlertTriangle, Cpu, Clock, Info } from 'lucide-react';
import axios from 'axios';
import type { IdleEventResponse, RecordIdleEventRequest, IdleDetectionSource } from '../../types/utilization';
import type { EquipmentResponse } from '../../types/equipment';
import { recordIdleEvent } from '../../api/utilization';
import { getEquipmentList } from '../../api/equipment';
import { useAuth } from '../../context/useAuth';

interface RecordIdleEventModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (event: IdleEventResponse) => void;
  preselectedEquipmentId?: number;
}

function getInitialDateTimeLocal(): string {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const hh = String(now.getHours()).padStart(2, '0');
  const min = String(now.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

export const RecordIdleEventModal: React.FC<RecordIdleEventModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  preselectedEquipmentId,
}) => {
  const { user } = useAuth();

  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [isLoadingEquipment, setIsLoadingEquipment] = useState<boolean>(true);

  // Form fields
  const [equipmentId, setEquipmentId] = useState<number | ''>(preselectedEquipmentId || '');
  const [detectionSource, setDetectionSource] = useState<IdleDetectionSource>('MANUAL_LAB_AUDIT');
  const [idleStartTime, setIdleStartTime] = useState<string>(getInitialDateTimeLocal);
  const [hasEnded, setHasEnded] = useState<boolean>(false);
  const [idleEndTime, setIdleEndTime] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [bookingId, setBookingId] = useState<string>('');
  const [usageSessionId, setUsageSessionId] = useState<string>('');

  // UI state
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;
    setIsLoadingEquipment(true);

    getEquipmentList()
      .then((data) => {
        if (!isMounted) return;
        const bookable = data.filter((e) => e.status !== 'RETIRED');
        setEquipmentList(bookable);
        if (!preselectedEquipmentId && bookable.length > 0) {
          setEquipmentId(bookable[0].id);
        }
      })
      .finally(() => {
        if (isMounted) setIsLoadingEquipment(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, preselectedEquipmentId]);

  useEffect(() => {
    if (isOpen) {
      if (preselectedEquipmentId) {
        setEquipmentId(preselectedEquipmentId);
      }
      setErrorMessage(null);
      setClientErrors({});
    }
  }, [isOpen, preselectedEquipmentId]);

  if (!isOpen) return null;

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!equipmentId) {
      errors.equipmentId = 'Equipment selection is required.';
    }

    if (!idleStartTime) {
      errors.idleStartTime = 'Idle start time is required.';
    }

    if (hasEnded && !idleEndTime) {
      errors.idleEndTime = 'Idle end time is required when marking resolved.';
    }

    if (hasEnded && idleStartTime && idleEndTime) {
      if (new Date(idleEndTime).getTime() <= new Date(idleStartTime).getTime()) {
        errors.idleEndTime = 'Idle end time must be after idle start time.';
      }
    }

    setClientErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!validateForm()) return;

    setIsSubmitting(true);

    try {
      const startIso = new Date(idleStartTime).toISOString();
      const endIso = hasEnded && idleEndTime ? new Date(idleEndTime).toISOString() : null;

      const payload: RecordIdleEventRequest = {
        equipmentId: Number(equipmentId),
        detectionSource,
        idleStartTime: startIso,
        idleEndTime: endIso,
        loggedByUserId: user?.userId || null,
        bookingId: bookingId ? Number(bookingId) : null,
        usageSessionId: usageSessionId ? Number(usageSessionId) : null,
        notes: notes.trim() || null,
      };

      const event = await recordIdleEvent(payload);
      onSuccess(event);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to record idle event:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Idle event creation failed with HTTP ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('An unexpected error occurred while logging the idle event.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div>
            <h2 className="text-base font-semibold text-white flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-amber-400" />
              Log Equipment Idle Event
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Record manual audit or no-show idle periods for lab instruments.
            </p>
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
          <div className="mx-6 mt-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2.5 text-xs text-rose-300">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-200">Rejection Notice</p>
              <p className="mt-0.5 whitespace-pre-wrap">{errorMessage}</p>
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Equipment Selector */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-slate-400" />
              Select Instrument <span className="text-rose-400">*</span>
            </label>
            {isLoadingEquipment ? (
              <div className="flex items-center gap-2 px-3 py-2 bg-slate-800 rounded-xl text-xs text-slate-400">
                <Loader2 className="w-4 h-4 animate-spin text-amber-400" />
                Loading instruments...
              </div>
            ) : (
              <select
                value={equipmentId}
                onChange={(e) => setEquipmentId(e.target.value ? Number(e.target.value) : '')}
                className={`w-full px-3 py-2 bg-slate-800/80 border ${
                  clientErrors.equipmentId ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50`}
              >
                <option value="">-- Choose Equipment --</option>
                {equipmentList.map((eq) => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} [{eq.assetTag}]
                  </option>
                ))}
              </select>
            )}
            {clientErrors.equipmentId && (
              <p className="text-xs text-rose-400">{clientErrors.equipmentId}</p>
            )}
          </div>

          {/* Detection Source */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Idle Detection Source
            </label>
            <select
              value={detectionSource}
              onChange={(e) => setDetectionSource(e.target.value as IdleDetectionSource)}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50"
            >
              <option value="MANUAL_LAB_AUDIT">Manual Laboratory Inspection / Audit</option>
              <option value="BOOKING_NO_SHOW">Booking No-Show Idle Detection</option>
              <option value="SCHEDULED_INSPECTION_CRON">Scheduled Inspection Task</option>
            </select>
          </div>

          {/* Idle Start Time */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              Idle Period Start Time <span className="text-rose-400">*</span>
            </label>
            <input
              type="datetime-local"
              value={idleStartTime}
              onChange={(e) => setIdleStartTime(e.target.value)}
              className={`w-full px-3 py-2 bg-slate-800/80 border ${
                clientErrors.idleStartTime ? 'border-rose-500' : 'border-slate-700'
              } rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50`}
            />
            {clientErrors.idleStartTime && (
              <p className="text-xs text-rose-400">{clientErrors.idleStartTime}</p>
            )}
          </div>

          {/* Already Ended Checkbox */}
          <div className="pt-1">
            <label className="flex items-center gap-2 cursor-pointer text-xs text-slate-300">
              <input
                type="checkbox"
                checked={hasEnded}
                onChange={(e) => setHasEnded(e.target.checked)}
                className="w-4 h-4 rounded border-slate-700 text-amber-500 focus:ring-amber-500/40 bg-slate-800"
              />
              <span>Idle period is already ended / resolved</span>
            </label>
          </div>

          {hasEnded && (
            <div className="space-y-1.5 p-3 bg-slate-800/40 rounded-xl border border-slate-700/50">
              <label className="block text-[11px] font-semibold text-slate-300">
                Idle Period End Time <span className="text-rose-400">*</span>
              </label>
              <input
                type="datetime-local"
                value={idleEndTime}
                onChange={(e) => setIdleEndTime(e.target.value)}
                className={`w-full px-3 py-1.5 bg-slate-800 border ${
                  clientErrors.idleEndTime ? 'border-rose-500' : 'border-slate-700'
                } rounded-lg text-xs text-white`}
              />
              {clientErrors.idleEndTime && (
                <p className="text-[11px] text-rose-400">{clientErrors.idleEndTime}</p>
              )}
            </div>
          )}

          {/* Optional Booking ID & Session ID */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="block text-[11px] font-semibold text-slate-300">
                Booking ID (Optional)
              </label>
              <input
                type="number"
                value={bookingId}
                onChange={(e) => setBookingId(e.target.value)}
                placeholder="e.g. 1"
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-xs text-white"
              />
            </div>

            <div className="space-y-1">
              <label className="block text-[11px] font-semibold text-slate-300">
                Usage Session ID (Optional)
              </label>
              <input
                type="number"
                value={usageSessionId}
                onChange={(e) => setUsageSessionId(e.target.value)}
                placeholder="e.g. 1"
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-xs text-white"
              />
            </div>
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Audit Notes / Observations
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Machine powered on but user absent during scheduled reservation window."
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50"
            />
          </div>

          <div className="flex items-start gap-2 p-2.5 bg-amber-500/10 border border-amber-500/20 rounded-xl text-[11px] text-amber-300">
            <Info className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
            <span>
              Idle events capture unutilized time during operating windows. Hardware telemetry is simulated via manual lab inspection logs.
            </span>
          </div>

          {/* Actions */}
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
              className="flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-400 hover:to-orange-500 rounded-xl shadow-lg shadow-amber-500/20 transition-all disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Logging Event...' : 'Record Idle Event'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
