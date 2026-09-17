import React, { useState, useEffect, useMemo } from 'react';
import { X, Loader2, AlertCircle, Calendar, Clock, Info, ShieldAlert } from 'lucide-react';
import axios from 'axios';
import type { BookingResponse, CreateBookingRequest } from '../../types/booking';
import type { EquipmentResponse } from '../../types/equipment';
import { createBooking } from '../../api/booking';
import { getEquipmentList } from '../../api/equipment';
import { useAuth } from '../../context/useAuth';

interface BookingFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (booking: BookingResponse) => void;
  preselectedEquipmentId?: number;
}

function getInitialBookingDate(): string {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  if (tomorrow.getDay() === 0) {
    tomorrow.setDate(tomorrow.getDate() + 1);
  }
  const yyyy = tomorrow.getFullYear();
  const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
  const dd = String(tomorrow.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

export const BookingFormModal: React.FC<BookingFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  preselectedEquipmentId,
}) => {
  const { user } = useAuth();

  // Reference equipment data
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [isLoadingEquipment, setIsLoadingEquipment] = useState<boolean>(true);

  // Form Fields
  const [equipmentId, setEquipmentId] = useState<number | ''>(preselectedEquipmentId || '');
  const [startDate, setStartDate] = useState<string>(getInitialBookingDate);
  const [startTime, setStartTime] = useState<string>('09:00');
  const [endTime, setEndTime] = useState<string>('11:00');
  const [purpose, setPurpose] = useState<string>('Standard laboratory reservation');
  const [projectCode, setProjectCode] = useState<string>('');

  // UI state
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  // Sync preselected equipment and clear errors when modal opens
  useEffect(() => {
    if (isOpen && preselectedEquipmentId) {
      setEquipmentId(preselectedEquipmentId);
    }
    if (isOpen) {
      setErrorMessage(null);
      setClientErrors({});
    }
  }, [isOpen, preselectedEquipmentId]);

  // Fetch real equipment catalog
  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;
    setIsLoadingEquipment(true);

    getEquipmentList()
      .then((data) => {
        if (!isMounted) return;
        // Exclude retired equipment as normally bookable
        const bookable = data.filter((eq) => eq.status !== 'RETIRED');
        setEquipmentList(bookable);
        if (!preselectedEquipmentId && bookable.length > 0) {
          setEquipmentId(bookable[0].id);
        }
      })
      .catch((err) => {
        console.error('Failed to load equipment list:', err);
        if (isMounted) {
          setErrorMessage('Unable to load equipment list from backend.');
        }
      })
      .finally(() => {
        if (isMounted) setIsLoadingEquipment(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, preselectedEquipmentId]);

  // Find currently selected equipment details
  const selectedEquipment = useMemo(() => {
    return equipmentList.find((eq) => eq.id === Number(equipmentId)) || null;
  }, [equipmentList, equipmentId]);

  if (!isOpen) return null;

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!equipmentId) {
      errors.equipmentId = 'Please select an instrument.';
    }

    if (!startDate) {
      errors.startDate = 'Reservation date is required.';
    }

    if (!startTime) {
      errors.startTime = 'Start time is required.';
    }

    if (!endTime) {
      errors.endTime = 'End time is required.';
    }

    if (startTime && endTime && startTime >= endTime) {
      errors.endTime = 'End time must be strictly after start time.';
    }

    if (startDate) {
      const selected = new Date(`${startDate}T00:00:00`);
      // Sunday is 0
      if (selected.getUTCDay() === 0) {
        errors.startDate = 'Operating window does not permit reservations on Sundays.';
      }
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
      // Construct UTC ISO strings matching backend Instant expectations
      const startDateTime = new Date(`${startDate}T${startTime}:00Z`);
      const endDateTime = new Date(`${startDate}T${endTime}:00Z`);

      const payload: CreateBookingRequest = {
        equipmentId: Number(equipmentId),
        userId: user?.userId || null,
        departmentId: user?.departmentId || null,
        institutionId: user?.institutionId || null,
        startTime: startDateTime.toISOString(),
        endTime: endDateTime.toISOString(),
        purpose: purpose.trim() || 'Standard laboratory reservation',
        projectCode: projectCode.trim() || null,
      };

      const created = await createBooking(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Create booking failed:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Request failed with status code ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('An unexpected error occurred while creating the reservation.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden p-4 bg-black/70 backdrop-blur-sm">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div>
            <h2 className="text-lg font-semibold text-white tracking-tight flex items-center gap-2">
              <Calendar className="w-5 h-5 text-sky-400" />
              New Resource Reservation
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Book equipment time slots in compliance with laboratory operational windows.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error Banner */}
        {errorMessage && (
          <div className="mx-6 mt-4 p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div className="text-xs text-rose-300">
              <p className="font-semibold text-rose-200">Reservation Rejected by Scheduling Engine</p>
              <p className="mt-0.5 whitespace-pre-wrap">{errorMessage}</p>
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Equipment Selection */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Select Instrument / Equipment <span className="text-rose-400">*</span>
            </label>
            {isLoadingEquipment ? (
              <div className="flex items-center gap-2 px-3 py-2 bg-slate-800/60 rounded-xl border border-slate-700 text-xs text-slate-400">
                <Loader2 className="w-4 h-4 animate-spin text-sky-400" />
                Loading instrument catalog...
              </div>
            ) : (
              <select
                value={equipmentId}
                onChange={(e) => setEquipmentId(e.target.value ? Number(e.target.value) : '')}
                className={`w-full px-3.5 py-2.5 bg-slate-800/80 border ${
                  clientErrors.equipmentId ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50`}
              >
                <option value="">-- Choose Equipment --</option>
                {equipmentList.map((eq) => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} [{eq.assetTag}] — {eq.categoryName} ({eq.status})
                  </option>
                ))}
              </select>
            )}
            {clientErrors.equipmentId && (
              <p className="text-xs text-rose-400">{clientErrors.equipmentId}</p>
            )}

            {/* Selected Equipment Snapshot */}
            {selectedEquipment && (
              <div className="mt-2 p-3 bg-slate-800/50 border border-slate-700/50 rounded-xl text-xs space-y-1.5">
                <div className="flex items-center justify-between text-slate-300 font-medium">
                  <span>{selectedEquipment.name}</span>
                  <span className="px-2 py-0.5 rounded text-[11px] bg-slate-700/80 text-sky-300 font-mono">
                    {selectedEquipment.assetTag}
                  </span>
                </div>
                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-400">
                  <span>Category: {selectedEquipment.categoryName}</span>
                  <span>
                    Location: {selectedEquipment.locationBuilding || 'Main'} - Room {selectedEquipment.locationRoom || 'Lab'}
                  </span>
                  <span>Status: <strong className="text-slate-300">{selectedEquipment.status}</strong></span>
                </div>
              </div>
            )}

            {/* Authoritative Availability Disclaimer */}
            <div className="flex items-start gap-2 p-2.5 bg-sky-500/10 border border-sky-500/20 rounded-xl text-xs text-sky-300">
              <Info className="w-4 h-4 text-sky-400 shrink-0 mt-0.5" />
              <span>
                <strong>Notice:</strong> Equipment operational status does not imply time slot availability.
                Slot overlap and qualifications are authoritatively validated by the backend scheduling engine upon submission.
              </span>
            </div>
          </div>

          {/* Date & Time Selection */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                Date <span className="text-rose-400">*</span>
              </label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className={`w-full px-3 py-2 bg-slate-800/80 border ${
                  clientErrors.startDate ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50`}
              />
              {clientErrors.startDate && (
                <p className="text-xs text-rose-400">{clientErrors.startDate}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                Start Time (UTC) <span className="text-rose-400">*</span>
              </label>
              <input
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className={`w-full px-3 py-2 bg-slate-800/80 border ${
                  clientErrors.startTime ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50`}
              />
              {clientErrors.startTime && (
                <p className="text-xs text-rose-400">{clientErrors.startTime}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                End Time (UTC) <span className="text-rose-400">*</span>
              </label>
              <input
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className={`w-full px-3 py-2 bg-slate-800/80 border ${
                  clientErrors.endTime ? 'border-rose-500' : 'border-slate-700'
                } rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50`}
              />
              {clientErrors.endTime && (
                <p className="text-xs text-rose-400">{clientErrors.endTime}</p>
              )}
            </div>
          </div>

          {/* Operating Window Note */}
          <div className="flex items-center gap-2 text-xs text-slate-400 bg-slate-800/30 px-3 py-2 rounded-lg border border-slate-700/40">
            <ShieldAlert className="w-3.5 h-3.5 text-amber-400 shrink-0" />
            <span>
              Operating Window: <strong>08:00 – 20:00 UTC</strong>, Monday through Saturday. Single calendar day reservations only.
            </span>
          </div>

          {/* Purpose & Project Code */}
          <div className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Reservation Purpose / Protocol Notes
              </label>
              <textarea
                rows={2}
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                placeholder="Describe your experimental protocol or purpose..."
                className="w-full px-3.5 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Project / Grant Code (Optional)
              </label>
              <input
                type="text"
                value={projectCode}
                onChange={(e) => setProjectCode(e.target.value)}
                placeholder="e.g. PRJ-2026-BIO"
                className="w-full px-3.5 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50"
              />
            </div>
          </div>

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
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
              className="flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 rounded-xl shadow-lg shadow-sky-500/20 transition-all disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{isSubmitting ? 'Requesting Reservation...' : 'Confirm & Request Booking'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
