import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Clock,
  Cpu,
  Calendar,
  User,
  ArrowLeft,
  AlertCircle,
  Sparkles,
  CheckCheck,
  AlertTriangle,
  FileText,
  Loader2,
} from 'lucide-react';
import type { UsageSessionResponse } from '../../types/utilization';
import {
  getUsageSessionById,
  completeUsageSession,
  terminateUsageSessionEarly,
} from '../../api/utilization';
import { SessionStatusBadge } from '../../components/utilization/SessionStatusBadge';

export const UsageSessionDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [session, setSession] = useState<UsageSessionResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  const fetchSession = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getUsageSessionById(id);
      setSession(data);
    } catch (err) {
      console.error('Failed to load session details:', err);
      setError('Unable to load usage session details from backend.');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchSession();
  }, [fetchSession]);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => {
      setSuccessToast(null);
    }, 4000);
  };

  const handleComplete = async () => {
    if (!session) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await completeUsageSession(session.id);
      setSession(updated);
      showToast('Session marked as completed.');
    } catch (err: unknown) {
      console.error('Failed to complete session:', err);
      setActionError(err instanceof Error ? err.message : 'Failed to complete session.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleTerminateEarly = async () => {
    if (!session) return;
    setIsProcessing(true);
    setActionError(null);
    try {
      const updated = await terminateUsageSessionEarly(session.id);
      setSession(updated);
      showToast('Session terminated early.');
    } catch (err: unknown) {
      console.error('Failed to terminate session:', err);
      setActionError(err instanceof Error ? err.message : 'Failed to terminate session.');
    } finally {
      setIsProcessing(false);
    }
  };

  const formatDateTime = (isoString?: string | null) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleString(undefined, {
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

  if (isLoading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center text-slate-400 space-y-3">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-400" />
        <p className="text-xs">Loading usage session record...</p>
      </div>
    );
  }

  if (error || !session) {
    return (
      <div className="max-w-2xl mx-auto py-12 px-4 space-y-4 text-center">
        <div className="p-8 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
          <h2 className="text-base font-semibold text-white">Record Not Found</h2>
          <p className="text-xs text-slate-400">{error || 'Session could not be located.'}</p>
          <button
            type="button"
            onClick={() => navigate('/utilization')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Utilization</span>
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
              <p className="font-semibold text-rose-200">Operation Error</p>
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
        <Link to="/utilization" className="hover:text-white transition-colors flex items-center gap-1">
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Utilization</span>
        </Link>
        <span>/</span>
        <span className="text-slate-200 font-mono">SES-{session.id}</span>
      </div>

      {/* Header with Status & Actions */}
      <div className="p-6 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <span className="text-xl sm:text-2xl font-mono font-bold text-white tracking-tight">
              Usage Session #{session.id}
            </span>
            <SessionStatusBadge status={session.sessionStatus} />
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Instrument check-in recorded on {formatDateTime(session.createdAt)}
          </p>
        </div>

        {session.sessionStatus === 'ACTIVE' && (
          <div className="flex items-center gap-2.5">
            <button
              type="button"
              onClick={handleComplete}
              disabled={isProcessing}
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-500/20 transition-all disabled:opacity-50"
            >
              {isProcessing ? <Loader2 className="w-4 h-4 animate-spin" /> : <CheckCheck className="w-4 h-4" />}
              <span>Complete Session</span>
            </button>

            <button
              type="button"
              onClick={handleTerminateEarly}
              disabled={isProcessing}
              className="flex items-center gap-2 px-4 py-2 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-xl text-xs font-semibold transition-colors disabled:opacity-50"
            >
              <Clock className="w-4 h-4" />
              <span>Terminate Early</span>
            </button>
          </div>
        )}
      </div>

      {/* Grid Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Columns */}
        <div className="lg:col-span-2 space-y-6">
          {/* Timing & Duration */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <Clock className="w-4 h-4 text-emerald-400" />
              Check-In / Out Timestamps & Duration
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="p-4 bg-slate-800/50 border border-slate-700/40 rounded-xl space-y-1">
                <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  Check-In Timestamp (UTC)
                </span>
                <p className="text-sm font-medium text-white">{formatDateTime(session.checkedInAt)}</p>
              </div>

              <div className="p-4 bg-slate-800/50 border border-slate-700/40 rounded-xl space-y-1">
                <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  Check-Out Timestamp (UTC)
                </span>
                <p className="text-sm font-medium text-white">
                  {session.checkedOutAt ? formatDateTime(session.checkedOutAt) : 'Session actively ongoing'}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="p-3 bg-slate-800/30 rounded-xl border border-slate-700/30 flex items-center justify-between text-xs text-slate-300">
                <span>Actual Operating Duration:</span>
                <strong className="text-emerald-400 font-mono text-sm">
                  {session.actualDurationMinutes != null ? `${session.actualDurationMinutes} mins` : 'In progress'}
                </strong>
              </div>

              <div className="p-3 bg-slate-800/30 rounded-xl border border-slate-700/30 flex items-center justify-between text-xs text-slate-300">
                <span>Scheduled Duration:</span>
                <strong className="text-white font-mono text-sm">
                  {session.scheduledDurationMinutes != null ? `${session.scheduledDurationMinutes} mins` : '—'}
                </strong>
              </div>
            </div>
          </div>

          {/* Observations & Notes */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <FileText className="w-4 h-4 text-amber-400" />
              Observations & Audit Notes
            </h3>

            <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 text-xs text-slate-200">
              {session.notes || 'No observations or audit notes logged for this session.'}
            </div>
          </div>
        </div>

        {/* Right Column */}
        <div className="space-y-6">
          {/* Equipment Reference */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-3">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <Cpu className="w-4 h-4 text-sky-400" />
              Instrument Used
            </h3>

            <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 space-y-2 text-xs">
              <div className="font-semibold text-white">{session.equipmentName}</div>
              <div className="text-slate-400">ID: #{session.equipmentId}</div>
              <Link
                to={`/equipment/${session.equipmentId}`}
                className="text-sky-400 hover:text-sky-300 underline block font-medium mt-1"
              >
                View Equipment Details →
              </Link>
            </div>
          </div>

          {/* Linked Reservation */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-3">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-400" />
              Linked Reservation
            </h3>

            <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 space-y-2 text-xs">
              {session.bookingId ? (
                <>
                  <div className="font-mono font-bold text-sky-400">{session.bookingReference}</div>
                  <div className="text-slate-400">Booking ID: #{session.bookingId}</div>
                  <Link
                    to={`/bookings/${session.bookingId}`}
                    className="text-sky-400 hover:text-sky-300 underline block font-medium mt-1"
                  >
                    View Booking Record →
                  </Link>
                </>
              ) : (
                <div className="text-slate-400 italic">
                  Ad-hoc usage session recorded without a prior scheduled reservation.
                </div>
              )}
            </div>
          </div>

          {/* User Info */}
          <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-3">
            <h3 className="text-sm font-semibold text-white flex items-center gap-2">
              <User className="w-4 h-4 text-emerald-400" />
              Attributed Researcher
            </h3>

            <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 space-y-1 text-xs">
              <div className="font-medium text-white">{session.userName}</div>
              <div className="text-slate-400">{session.userEmail}</div>
              <div className="text-slate-500 text-[11px]">User ID: #{session.userId}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
