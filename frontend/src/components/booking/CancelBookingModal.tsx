import React, { useState } from 'react';
import { X, AlertTriangle, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type { BookingResponse } from '../../types/booking';
import { cancelBooking } from '../../api/booking';
import { useAuth } from '../../context/useAuth';

interface CancelBookingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: BookingResponse) => void;
  booking: BookingResponse | null;
}

export const CancelBookingModal: React.FC<CancelBookingModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  booking,
}) => {
  const { user } = useAuth();
  const [reason, setReason] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen || !booking) return null;

  const handleCancelSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      const cancelled = await cancelBooking(booking.id, {
        cancelledByUserId: user?.userId || null,
        cancellationReason: reason.trim() || undefined,
      });
      onSuccess(cancelled);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to cancel booking:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Cancellation failed with status ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to cancel the booking.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div className="flex items-center gap-2 text-rose-400">
            <AlertTriangle className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Cancel Reservation</h2>
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

        {/* Content */}
        <form onSubmit={handleCancelSubmit} className="p-6 space-y-4">
          <div className="text-xs text-slate-300 space-y-2">
            <p>
              Are you sure you want to cancel reservation <strong className="text-white font-mono">{booking.bookingReference}</strong>?
            </p>
            <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/50 space-y-1 text-slate-400">
              <div>Instrument: <strong className="text-slate-200">{booking.equipmentName}</strong></div>
              <div>Status: <span className="text-amber-400 font-medium">{booking.status}</span></div>
            </div>
            <p className="text-slate-400 text-[11px]">
              Once cancelled, the reserved slot will be freed in the schedule and the booking cannot be restarted.
            </p>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Reason for Cancellation (Optional)
            </label>
            <textarea
              rows={2}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Provide an optional cancellation explanation..."
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/40"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
            >
              Keep Reservation
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-500 rounded-xl shadow-lg shadow-rose-600/20 transition-colors disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Cancelling...' : 'Confirm Cancellation'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
