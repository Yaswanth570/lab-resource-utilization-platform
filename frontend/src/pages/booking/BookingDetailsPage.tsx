import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Calendar,
  Clock,
  Building2,
  User,
  ArrowLeft,
  AlertCircle,
  Sparkles,
  Check,
  Play,
  CheckCheck,
  UserX,
  XCircle,
  Cpu,
  DollarSign,
  History,
  FileText,
  ShieldCheck,
  AlertTriangle,
  Loader2,
} from 'lucide-react';
import type { BookingResponse } from '../../types/booking';
import {
  getBookingById,
  confirmBooking,
  startBooking,
  completeBooking,
  markNoShow,
} from '../../api/booking';
import { BookingStatusBadge } from '../../components/booking/BookingStatusBadge';
import { CancelBookingModal } from '../../components/booking/CancelBookingModal';
import { useAuth } from '../../context/useAuth';
import { canApproveBooking, isHigherStaff } from '../../utils/rbac';

export const BookingDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [booking, setBooking] = useState<BookingResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [isCancelModalOpen, setIsCancelModalOpen] = useState<boolean>(false);

  const fetchBooking = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getBookingById(id);
      setBooking(data);
    } catch (err: unknown) {
      console.error('Failed to load booking details:', err);
      setError('Unable to load reservation details. The record may not exist or network connection failed.');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchBooking();
  }, [fetchBooking]);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => {
      setSuccessToast(null);
    }, 4000);
  };

  // Lifecycle Actions
  const handleConfirm = async () => {
    if (!booking) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await confirmBooking(booking.id);
      setBooking(updated);
      showToast('Reservation confirmed successfully.');
    } catch (err: unknown) {
      console.error('Failed to confirm reservation:', err);
      const msg = err instanceof Error ? err.message : 'Confirmation rejected by backend.';
      setActionError(msg);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleStart = async () => {
    if (!booking) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await startBooking(booking.id);
      setBooking(updated);
      showToast('Reservation started. Equipment status is now IN_USE.');
    } catch (err: unknown) {
      console.error('Failed to start reservation:', err);
      const msg = err instanceof Error ? err.message : 'Session start rejected by backend.';
      setActionError(msg);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleComplete = async () => {
    if (!booking) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await completeBooking(booking.id);
      setBooking(updated);
      showToast('Reservation session completed.');
    } catch (err: unknown) {
      console.error('Failed to complete reservation:', err);
      const msg = err instanceof Error ? err.message : 'Session completion rejected by backend.';
      setActionError(msg);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleMarkNoShow = async () => {
    if (!booking) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await markNoShow(booking.id);
      setBooking(updated);
      showToast('Reservation marked as NO_SHOW.');
    } catch (err: unknown) {
      console.error('Failed to mark reservation as no-show:', err);
      const msg = err instanceof Error ? err.message : 'Operation rejected by backend.';
      setActionError(msg);
    } finally {
      setIsProcessing(false);
    }
  };

  const formatDateTime = (isoString?: string | null) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleString(undefined, {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
      });
    } catch {
      return isoString;
    }
  };

  const formatDuration = (startIso?: string, endIso?: string) => {
    if (!startIso || !endIso) return '—';
    const start = new Date(startIso).getTime();
    const end = new Date(endIso).getTime();
    const diffMins = Math.round((end - start) / (1000 * 60));
    if (diffMins <= 0) return '—';
    const hours = Math.floor(diffMins / 60);
    const mins = diffMins % 60;
    if (hours === 0) return `${mins} minutes`;
    if (mins === 0) return `${hours} hours`;
    return `${hours} hours, ${mins} minutes`;
  };

  if (isLoading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center text-slate-400 space-y-3">
        <Loader2 className="w-8 h-8 animate-spin text-sky-400" />
        <p className="text-xs">Loading reservation record...</p>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="max-w-2xl mx-auto py-12 px-4 space-y-4 text-center">
        <div className="p-8 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
          <h2 className="text-base font-semibold text-white">Record Not Found</h2>
          <p className="text-xs text-slate-400">{error || 'Booking could not be loaded.'}</p>
          <button
            type="button"
            onClick={() => navigate('/bookings')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Reservations</span>
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 bg-emerald-500/90 backdrop-blur-md text-white rounded-xl shadow-2xl border border-emerald-400/40 text-xs font-medium animate-in fade-in slide-in-from-bottom-5">
          <Sparkles className="w-4 h-4 text-emerald-200" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Action Error Alert */}
      {actionError && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start justify-between gap-3 text-xs text-rose-300">
          <div className="flex items-start gap-2.5">
            <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-200">Operation Transition Rejected</p>
              <p className="mt-0.5 whitespace-pre-wrap">{actionError}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setActionError(null)}
            className="text-rose-400 hover:text-white"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Breadcrumb Navigation */}
      <div className="flex items-center gap-2 text-xs text-slate-400">
        <Link to="/bookings" className="hover:text-white transition-colors flex items-center gap-1">
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Reservations</span>
        </Link>
        <span>/</span>
        <span className="text-slate-200 font-mono">{booking.bookingReference}</span>
      </div>

      {/* Page Header with Status & Action Bar */}
      <div className="p-6 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-xl sm:text-2xl font-mono font-bold text-white tracking-tight">
              {booking.bookingReference}
            </span>
            <BookingStatusBadge status={booking.status} />
            {booking.isExternalBooking && (
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
                External Sharing Allocation
              </span>
            )}
          </div>
          <p className="text-xs text-slate-400 mt-1.5 flex items-center gap-2">
            <span>Created on {formatDateTime(booking.createdAt)}</span>
            {booking.updatedAt && <span>• Last modified {formatDateTime(booking.updatedAt)}</span>}
          </p>
        </div>

        {/* Dynamic Action Buttons Appropriate for Current Status */}
        <div className="flex flex-wrap items-center gap-2.5">
          {booking.status === 'PENDING_APPROVAL' && (
            <>
              {canApproveBooking(user?.roles, booking.userId, user?.userId) && (
                <button
                  type="button"
                  onClick={handleConfirm}
                  disabled={isProcessing}
                  className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-sky-500/20 transition-all disabled:opacity-50"
                >
                  {isProcessing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4" />}
                  <span>Confirm Booking</span>
                </button>
              )}
              <button
                type="button"
                onClick={() => setIsCancelModalOpen(true)}
                disabled={isProcessing}
                className="flex items-center gap-2 px-4 py-2 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-xl text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <XCircle className="w-4 h-4" />
                <span>Cancel</span>
              </button>
            </>
          )}

          {booking.status === 'CONFIRMED' && (
            <>
              {isHigherStaff(user?.roles) && (
                <>
                  <button
                    type="button"
                    onClick={handleStart}
                    disabled={isProcessing}
                    className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-500/20 transition-all disabled:opacity-50"
                  >
                    {isProcessing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4" />}
                    <span>Start Session (In-Use)</span>
                  </button>
                  <button
                    type="button"
                    onClick={handleMarkNoShow}
                    disabled={isProcessing}
                    className="flex items-center gap-2 px-3.5 py-2 bg-orange-500/10 hover:bg-orange-500/20 text-orange-400 border border-orange-500/30 rounded-xl text-xs font-semibold transition-colors disabled:opacity-50"
                  >
                    <UserX className="w-4 h-4" />
                    <span>Mark No-Show</span>
                  </button>
                </>
              )}
              <button
                type="button"
                onClick={() => setIsCancelModalOpen(true)}
                disabled={isProcessing}
                className="flex items-center gap-2 px-3.5 py-2 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-xl text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <XCircle className="w-4 h-4" />
                <span>Cancel</span>
              </button>
            </>
          )}

          {booking.status === 'IN_USE' && (
            <button
              type="button"
              onClick={handleComplete}
              disabled={isProcessing}
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-400 hover:to-purple-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-indigo-500/20 transition-all disabled:opacity-50"
            >
              {isProcessing ? <Loader2 className="w-4 h-4 animate-spin" /> : <CheckCheck className="w-4 h-4" />}
              <span>Complete Reservation</span>
            </button>
          )}

          {(booking.status === 'COMPLETED' || booking.status === 'CANCELLED' || booking.status === 'NO_SHOW') && (
            <span className="text-xs text-slate-400 bg-slate-800/80 px-3 py-1.5 rounded-xl border border-slate-700/60">
              Terminal State: No further lifecycle actions permitted
            </span>
          )}
        </div>
      </div>

      {/* Grid Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Columns: Details & Timing */}
        <div className="lg:col-span-2 space-y-6">
          {/* Reservation Schedule Section */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <Calendar className="w-4 h-4 text-sky-400" />
              Reservation Timing & Schedule
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="p-4 bg-slate-800/50 border border-slate-700/40 rounded-xl space-y-1">
                <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  Start Time (UTC)
                </span>
                <p className="text-sm font-medium text-white">{formatDateTime(booking.startTime)}</p>
              </div>

              <div className="p-4 bg-slate-800/50 border border-slate-700/40 rounded-xl space-y-1">
                <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  End Time (UTC)
                </span>
                <p className="text-sm font-medium text-white">{formatDateTime(booking.endTime)}</p>
              </div>
            </div>

            <div className="p-3 bg-slate-800/30 rounded-xl border border-slate-700/30 flex items-center justify-between text-xs text-slate-300">
              <span className="flex items-center gap-1.5">
                <Clock className="w-4 h-4 text-slate-400" />
                Scheduled Duration:
              </span>
              <strong className="text-white font-mono">
                {formatDuration(booking.startTime, booking.endTime)}
              </strong>
            </div>
          </div>

          {/* Instrument Information */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                <Cpu className="w-4 h-4 text-indigo-400" />
                Reserved Instrument
              </h3>
              {booking.equipmentId && (
                <Link
                  to={`/equipment/${booking.equipmentId}`}
                  className="text-xs text-sky-400 hover:text-sky-300 underline font-medium"
                >
                  View Equipment Page →
                </Link>
              )}
            </div>

            <div className="p-4 bg-slate-800/40 border border-slate-700/40 rounded-xl space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-base font-semibold text-white">{booking.equipmentName}</span>
                <span className="px-2 py-0.5 rounded text-xs font-mono bg-slate-700 text-sky-300">
                  ID: {booking.equipmentId}
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Instrument assigned to this reservation. Operating conditions and sensor monitoring are active.
              </p>
            </div>
          </div>

          {/* Protocol Notes & Purpose */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <FileText className="w-4 h-4 text-amber-400" />
              Purpose & Protocol Notes
            </h3>

            <div className="space-y-3">
              <div>
                <label className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">
                  Reservation Purpose
                </label>
                <p className="text-xs text-slate-200 mt-1 p-3 bg-slate-800/40 rounded-xl border border-slate-700/40">
                  {booking.purpose || 'Standard laboratory reservation'}
                </p>
              </div>

              {booking.projectCode && (
                <div>
                  <label className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">
                    Project / Grant Identifier
                  </label>
                  <p className="text-xs text-slate-200 mt-1 font-mono">{booking.projectCode}</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: User, Org, Cost, Lifecycle History */}
        <div className="space-y-6">
          {/* User & Organization Details */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <User className="w-4 h-4 text-emerald-400" />
              Researcher & Tenant
            </h3>

            <div className="space-y-3 text-xs">
              <div className="pb-3 border-b border-slate-800">
                <span className="text-[11px] text-slate-400 uppercase tracking-wider block">Researcher</span>
                <p className="text-slate-200 font-medium mt-0.5">{booking.userName || '—'}</p>
                <p className="text-slate-400 text-[11px]">{booking.userEmail || '—'}</p>
              </div>

              <div className="pb-3 border-b border-slate-800">
                <span className="text-[11px] text-slate-400 uppercase tracking-wider block">Department</span>
                <p className="text-slate-200 font-medium mt-0.5 flex items-center gap-1.5">
                  <Building2 className="w-3.5 h-3.5 text-slate-400" />
                  {booking.departmentName || `Department #${booking.departmentId}`}
                </p>
              </div>

              <div>
                <span className="text-[11px] text-slate-400 uppercase tracking-wider block">Institution</span>
                <p className="text-slate-200 font-medium mt-0.5">
                  {booking.institutionName || `Institution #${booking.institutionId}`}
                </p>
              </div>
            </div>
          </div>

          {/* Cost & Billing */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <DollarSign className="w-4 h-4 text-emerald-400" />
              Cost & Billing Assessment
            </h3>

            <div className="space-y-2 text-xs">
              <div className="flex justify-between py-1.5 border-b border-slate-800">
                <span className="text-slate-400">Billing Status</span>
                <span className="font-semibold text-slate-200">{booking.billingStatus || 'UNBILLED'}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-slate-800">
                <span className="text-slate-400">Base Hourly Rate</span>
                <span className="font-mono text-slate-200">
                  {booking.baseHourlyRate != null ? `$${booking.baseHourlyRate.toFixed(2)} / hr` : 'Free / Standard'}
                </span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-slate-800">
                <span className="text-slate-400">Estimated Cost</span>
                <span className="font-mono text-sky-400 font-semibold">
                  {booking.estimatedCost != null ? `$${booking.estimatedCost.toFixed(2)}` : '$0.00'}
                </span>
              </div>
              {booking.actualCost != null && (
                <div className="flex justify-between py-1.5">
                  <span className="text-slate-400">Actual Billed Cost</span>
                  <span className="font-mono text-emerald-400 font-bold">
                    ${booking.actualCost.toFixed(2)}
                  </span>
                </div>
              )}
            </div>
          </div>

          {/* Lifecycle & Audit History */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <History className="w-4 h-4 text-purple-400" />
              Lifecycle Audit Events
            </h3>

            <div className="space-y-3 text-xs">
              {booking.approvedAt && (
                <div className="p-3 bg-sky-500/10 border border-sky-500/20 rounded-xl space-y-1">
                  <div className="flex items-center gap-1.5 text-sky-400 font-semibold">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    <span>Approved</span>
                  </div>
                  <p className="text-slate-300">
                    By: {booking.approvedByUserName || `User #${booking.approvedByUserId}`}
                  </p>
                  <p className="text-[11px] text-slate-400">{formatDateTime(booking.approvedAt)}</p>
                </div>
              )}

              {booking.cancelledAt && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl space-y-1">
                  <div className="flex items-center gap-1.5 text-rose-400 font-semibold">
                    <XCircle className="w-3.5 h-3.5" />
                    <span>Cancelled</span>
                  </div>
                  <p className="text-slate-300">
                    By: {booking.cancelledByUserName || `User #${booking.cancelledByUserId || 'System'}`}
                  </p>
                  <p className="text-[11px] text-slate-400">{formatDateTime(booking.cancelledAt)}</p>
                  {booking.cancellationReason && (
                    <p className="text-[11px] text-rose-300 italic mt-1">
                      "{booking.cancellationReason}"
                    </p>
                  )}
                </div>
              )}

              {booking.rejectionReason && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl space-y-1">
                  <span className="text-rose-400 font-semibold">Rejection Note</span>
                  <p className="text-slate-300">{booking.rejectionReason}</p>
                </div>
              )}

              {!booking.approvedAt && !booking.cancelledAt && !booking.rejectionReason && (
                <p className="text-slate-400 text-xs italic">
                  No subsequent lifecycle transitions logged yet.
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Cancel Confirmation Modal */}
      <CancelBookingModal
        isOpen={isCancelModalOpen}
        booking={booking}
        onClose={() => setIsCancelModalOpen(false)}
        onSuccess={(updated) => {
          showToast(`Reservation ${updated.bookingReference} cancelled.`);
          setBooking(updated);
        }}
      />
    </div>
  );
};
