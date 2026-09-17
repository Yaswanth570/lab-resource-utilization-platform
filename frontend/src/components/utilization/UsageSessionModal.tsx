import React, { useState, useEffect } from 'react';
import { X, Loader2, AlertCircle, Clock, Cpu, Calendar, Info } from 'lucide-react';
import axios from 'axios';
import type { UsageSessionResponse, CreateUsageSessionRequest } from '../../types/utilization';
import type { EquipmentResponse } from '../../types/equipment';
import type { BookingResponse } from '../../types/booking';
import { createUsageSession } from '../../api/utilization';
import { getEquipmentList } from '../../api/equipment';
import { getBookings } from '../../api/booking';
import { useAuth } from '../../context/useAuth';

interface UsageSessionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (session: UsageSessionResponse) => void;
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

export const UsageSessionModal: React.FC<UsageSessionModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  preselectedEquipmentId,
}) => {
  const { user } = useAuth();

  // Equipment & Booking lookup
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [userBookings, setUserBookings] = useState<BookingResponse[]>([]);
  const [isLoadingLookups, setIsLoadingLookups] = useState<boolean>(true);

  // Form Fields
  const [equipmentId, setEquipmentId] = useState<number | ''>(preselectedEquipmentId || '');
  const [bookingId, setBookingId] = useState<number | ''>('');
  const [checkedInAt, setCheckedInAt] = useState<string>(getInitialDateTimeLocal);
  const [includeCheckOut, setIncludeCheckOut] = useState<boolean>(false);
  const [checkedOutAt, setCheckedOutAt] = useState<string>('');
  const [scheduledDuration, setScheduledDuration] = useState<string>('120');
  const [actualDuration, setActualDuration] = useState<string>('');
  const [notes, setNotes] = useState<string>('');

  // UI state
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;
    setIsLoadingLookups(true);

    Promise.all([
      getEquipmentList().catch(() => []),
      getBookings({ userId: user?.userId }).catch(() => []),
    ])
      .then(([eqs, bks]) => {
        if (!isMounted) return;
        setEquipmentList(eqs.filter((e) => e.status !== 'RETIRED'));
        setUserBookings(bks);
        if (!preselectedEquipmentId && eqs.length > 0) {
          setEquipmentId(eqs[0].id);
        }
      })
      .finally(() => {
        if (isMounted) setIsLoadingLookups(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, preselectedEquipmentId, user?.userId]);

  // Sync preselected equipment when modal opens
  useEffect(() => {
    if (isOpen) {
      if (preselectedEquipmentId) {
        setEquipmentId(preselectedEquipmentId);
      }
      setErrorMessage(null);
      setClientErrors({});
    }
  }, [isOpen, preselectedEquipmentId]);

  // When user selects a booking, auto-fill equipment and scheduled duration
  const handleBookingChange = (bIdStr: string) => {
    if (!bIdStr) {
      setBookingId('');
      return;
    }
    const bId = Number(bIdStr);
    setBookingId(bId);

    const match = userBookings.find((b) => b.id === bId);
    if (match) {
      setEquipmentId(match.equipmentId);
      if (match.startTime && match.endTime) {
        const start = new Date(match.startTime).getTime();
        const end = new Date(match.endTime).getTime();
        const diffMins = Math.round((end - start) / (1000 * 60));
        if (diffMins > 0) {
          setScheduledDuration(String(diffMins));
        }
      }
    }
  };

  if (!isOpen) return null;

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!equipmentId) {
      errors.equipmentId = 'Please select an instrument.';
    }

    if (!checkedInAt) {
      errors.checkedInAt = 'Check-in timestamp is required.';
    }

    if (includeCheckOut && !checkedOutAt) {
      errors.checkedOutAt = 'Check-out timestamp is required when recording completed session.';
    }

    if (includeCheckOut && checkedInAt && checkedOutAt) {
      const inTime = new Date(checkedInAt).getTime();
      const outTime = new Date(checkedOutAt).getTime();
      if (outTime <= inTime) {
        errors.checkedOutAt = 'Check-out time must be strictly after check-in time.';
      }
    }

    if (scheduledDuration && Number(scheduledDuration) <= 0) {
      errors.scheduledDuration = 'Scheduled duration must be greater than zero.';
    }

    if (actualDuration && Number(actualDuration) <= 0) {
      errors.actualDuration = 'Actual duration must be greater than zero.';
    }

    setClientErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const inIso = new Date(checkedInAt).toISOString();
      const outIso = includeCheckOut && checkedOutAt ? new Date(checkedOutAt).toISOString() : null;

      const payload: CreateUsageSessionRequest = {
        equipmentId: Number(equipmentId),
        userId: user?.userId || null,
        bookingId: bookingId ? Number(bookingId) : null,
        checkedInAt: inIso,
        checkedOutAt: outIso,
        scheduledDurationMinutes: scheduledDuration ? Number(scheduledDuration) : null,
        actualDurationMinutes: actualDuration ? Number(actualDuration) : null,
        notes: notes.trim() || null,
      };

      const session = await createUsageSession(payload);
      onSuccess(session);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to create usage session:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Session creation rejected with HTTP ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('An unexpected error occurred while creating the usage session.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-xl bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div>
            <h2 className="text-base font-semibold text-white flex items-center gap-2">
              <Clock className="w-5 h-5 text-emerald-400" />
              Record Equipment Usage Session
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Log instrument check-in, duration, and optional booking linkage.
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
              <p className="font-semibold text-rose-200">Backend Validation Error</p>
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
              Select Equipment <span className="text-rose-400">*</span>
            </label>
            {isLoadingLookups ? (
              <div className="flex items-center gap-2 px-3 py-2 bg-slate-800 rounded-xl text-xs text-slate-400">
                <Loader2 className="w-4 h-4 animate-spin text-emerald-400" />
                Loading instruments...
              </div>
            ) : (
              <select
                value={equipmentId}
                onChange={(e) => setEquipmentId(e.target.value ? Number(e.target.value) : '')}
                className={`w-full px-3 py-2 bg-slate-800/80 border ${
                  clientErrors.equipmentId ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50`}
              >
                <option value="">-- Choose Equipment --</option>
                {equipmentList.map((eq) => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} [{eq.assetTag}] ({eq.status})
                  </option>
                ))}
              </select>
            )}
            {clientErrors.equipmentId && (
              <p className="text-xs text-rose-400">{clientErrors.equipmentId}</p>
            )}
          </div>

          {/* Optional Linked Booking */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-slate-400" />
              Link to Existing Reservation (Optional)
            </label>
            <select
              value={bookingId}
              onChange={(e) => handleBookingChange(e.target.value)}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="">-- Ad-Hoc Usage (No Reservation) --</option>
              {userBookings.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.bookingReference} — {b.equipmentName} ({b.status})
                </option>
              ))}
            </select>
            <p className="text-[11px] text-slate-400">
              Linking a reservation associates usage sessions with scheduled slots.
            </p>
          </div>

          {/* Check-In Time */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              Check-In Time <span className="text-rose-400">*</span>
            </label>
            <input
              type="datetime-local"
              value={checkedInAt}
              onChange={(e) => setCheckedInAt(e.target.value)}
              className={`w-full px-3 py-2 bg-slate-800/80 border ${
                clientErrors.checkedInAt ? 'border-rose-500' : 'border-slate-700'
              } rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50`}
            />
            {clientErrors.checkedInAt && (
              <p className="text-xs text-rose-400">{clientErrors.checkedInAt}</p>
            )}
          </div>

          {/* Check-Out Toggle */}
          <div className="pt-1">
            <label className="flex items-center gap-2 cursor-pointer text-xs text-slate-300">
              <input
                type="checkbox"
                checked={includeCheckOut}
                onChange={(e) => setIncludeCheckOut(e.target.checked)}
                className="w-4 h-4 rounded border-slate-700 text-emerald-500 focus:ring-emerald-500/40 bg-slate-800"
              />
              <span>Record as already checked-out / completed</span>
            </label>
          </div>

          {/* Check-Out Timestamp & Actual Duration (if enabled) */}
          {includeCheckOut && (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 p-3 bg-slate-800/40 rounded-xl border border-slate-700/50 animate-in fade-in duration-150">
              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-slate-300">
                  Check-Out Time <span className="text-rose-400">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={checkedOutAt}
                  onChange={(e) => setCheckedOutAt(e.target.value)}
                  className={`w-full px-3 py-1.5 bg-slate-800 border ${
                    clientErrors.checkedOutAt ? 'border-rose-500' : 'border-slate-700'
                  } rounded-lg text-xs text-white`}
                />
                {clientErrors.checkedOutAt && (
                  <p className="text-[11px] text-rose-400">{clientErrors.checkedOutAt}</p>
                )}
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-slate-300">
                  Actual Duration (Mins)
                </label>
                <input
                  type="number"
                  placeholder="Auto-calculated if blank"
                  value={actualDuration}
                  onChange={(e) => setActualDuration(e.target.value)}
                  className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-xs text-white"
                />
                <p className="text-[10px] text-slate-400">
                  Leave blank to auto-calculate from timestamps.
                </p>
              </div>
            </div>
          )}

          {/* Scheduled Duration */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Scheduled Duration (Minutes)
            </label>
            <input
              type="number"
              value={scheduledDuration}
              onChange={(e) => setScheduledDuration(e.target.value)}
              placeholder="e.g. 120"
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Session Observations / Notes
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Standard protocol completed without abnormalities."
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
          </div>

          {/* Notice */}
          <div className="flex items-start gap-2 p-2.5 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-[11px] text-emerald-300">
            <Info className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <span>
              Backend validation verifies that actual duration matches timestamps and links to valid reservations.
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
              className="flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 rounded-xl shadow-lg shadow-emerald-500/20 transition-all disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Recording Session...' : 'Record Usage Session'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
