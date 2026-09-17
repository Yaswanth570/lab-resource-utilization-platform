import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Calendar,
  Plus,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Clock,
  Building2,
  Sparkles,
  ArrowRight,
  AlertCircle,
  Play,
  XCircle,
  Check,
  CheckCheck,
} from 'lucide-react';
import type { BookingResponse, BookingStatus } from '../../types/booking';
import {
  getBookings,
  confirmBooking,
  startBooking,
  completeBooking,
} from '../../api/booking';
import { BookingStatusBadge } from '../../components/booking/BookingStatusBadge';
import { BookingFormModal } from '../../components/booking/BookingFormModal';
import { CancelBookingModal } from '../../components/booking/CancelBookingModal';
import { useAuth } from '../../context/useAuth';
import { canApproveBooking, isResearcher } from '../../utils/rbac';

const STATUS_FILTERS: { label: string; value: BookingStatus | 'ALL' }[] = [
  { label: 'All Statuses', value: 'ALL' },
  { label: 'Pending Approval', value: 'PENDING_APPROVAL' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'In Use', value: 'IN_USE' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
  { label: 'No-Show', value: 'NO_SHOW' },
];

export const BookingListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  // Data State
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  // Filters State
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<BookingStatus | 'ALL'>('ALL');
  const [viewScope, setViewScope] = useState<'my' | 'all'>('my');

  // Modals State
  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
  const [bookingToCancel, setBookingToCancel] = useState<BookingResponse | null>(null);
  const [processingId, setProcessingId] = useState<number | null>(null);

  const fetchBookings = useCallback(async () => {
    setError(null);
    try {
      // If user toggles 'my', query by user ID; otherwise fetch all
      const params = viewScope === 'my' && user?.userId ? { userId: user.userId } : {};
      const data = await getBookings(params);
      setBookings(data);
    } catch (err: unknown) {
      console.error('Failed to load bookings:', err);
      setError('Unable to load bookings from the backend service.');
    } finally {
      setIsLoading(false);
    }
  }, [viewScope, user]);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  const showToast = (message: string) => {
    setSuccessToast(message);
    setTimeout(() => {
      setSuccessToast(null);
    }, 4000);
  };

  // Lifecycle Quick-Actions
  const handleConfirm = async (e: React.MouseEvent, id: number, refNum: string) => {
    e.stopPropagation();
    setActionError(null);
    setProcessingId(id);
    try {
      const updated = await confirmBooking(id);
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)));
      showToast(`Reservation ${refNum} confirmed successfully.`);
    } catch (err: unknown) {
      console.error('Failed to confirm booking:', err);
      const msg = err instanceof Error ? err.message : 'Failed to confirm booking.';
      setActionError(msg);
    } finally {
      setProcessingId(null);
    }
  };

  const handleStart = async (e: React.MouseEvent, id: number, refNum: string) => {
    e.stopPropagation();
    setActionError(null);
    setProcessingId(id);
    try {
      const updated = await startBooking(id);
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)));
      showToast(`Reservation ${refNum} started. Equipment is now IN_USE.`);
    } catch (err: unknown) {
      console.error('Failed to start booking:', err);
      const msg = err instanceof Error ? err.message : 'Failed to start booking.';
      setActionError(msg);
    } finally {
      setProcessingId(null);
    }
  };

  const handleComplete = async (e: React.MouseEvent, id: number, refNum: string) => {
    e.stopPropagation();
    setActionError(null);
    setProcessingId(id);
    try {
      const updated = await completeBooking(id);
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)));
      showToast(`Reservation ${refNum} marked as COMPLETED.`);
    } catch (err: unknown) {
      console.error('Failed to complete booking:', err);
      const msg = err instanceof Error ? err.message : 'Failed to complete booking.';
      setActionError(msg);
    } finally {
      setProcessingId(null);
    }
  };

  // Client-side search and filtering
  const filteredBookings = useMemo(() => {
    return bookings.filter((item) => {
      // Status match
      if (statusFilter !== 'ALL' && item.status !== statusFilter) {
        return false;
      }

      // Search match: booking reference, equipment name, purpose
      if (searchQuery.trim()) {
        const query = searchQuery.trim().toLowerCase();
        const matchesRef = item.bookingReference?.toLowerCase().includes(query);
        const matchesEquip = item.equipmentName?.toLowerCase().includes(query);
        const matchesPurpose = item.purpose?.toLowerCase().includes(query);
        const matchesUser = item.userName?.toLowerCase().includes(query);
        return matchesRef || matchesEquip || matchesPurpose || matchesUser;
      }

      return true;
    });
  }, [bookings, statusFilter, searchQuery]);

  const formatDateTime = (isoString?: string | null) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleString(undefined, {
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
    if (!startIso || !endIso) return null;
    const start = new Date(startIso).getTime();
    const end = new Date(endIso).getTime();
    const diffMins = Math.round((end - start) / (1000 * 60));
    if (diffMins <= 0) return null;
    const hours = Math.floor(diffMins / 60);
    const mins = diffMins % 60;
    if (hours === 0) return `${mins}m`;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}m`;
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 bg-emerald-500/90 backdrop-blur-md text-white rounded-xl shadow-2xl border border-emerald-400/40 text-xs font-medium animate-in fade-in slide-in-from-bottom-5">
          <Sparkles className="w-4 h-4 text-emerald-200" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Action Error Banner */}
      {actionError && (
        <div className="p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start justify-between gap-3 text-xs text-rose-300">
          <div className="flex items-start gap-2">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-200">Operation Error</p>
              <p className="mt-0.5">{actionError}</p>
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

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
              Resource Reservations
            </h1>
            <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-slate-800 text-sky-400 border border-slate-700/60">
              {filteredBookings.length} {filteredBookings.length === 1 ? 'reservation' : 'reservations'}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Track equipment booking schedules, status approvals, and laboratory reservations.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={() => fetchBookings()}
            disabled={isLoading}
            className="p-2.5 text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl border border-slate-700/60 transition-colors"
            title="Refresh bookings"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-sky-400' : ''}`} />
          </button>

          <button
            type="button"
            onClick={() => setIsAddModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-sky-500/20 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>New Booking</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl space-y-4">
        <div className="flex flex-col md:flex-row gap-3">
          {/* Search Input */}
          <div className="relative flex-1">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search reference, equipment, user, or protocol..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/50"
            />
          </div>

          {/* Scope Toggle: My Bookings vs All Bookings */}
          {!isResearcher(user?.roles) && (
            <div className="inline-flex rounded-xl bg-slate-800/90 p-1 border border-slate-700/60 shrink-0">
              <button
                type="button"
                onClick={() => setViewScope('my')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  viewScope === 'my'
                    ? 'bg-sky-500 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                My Bookings
              </button>
              <button
                type="button"
                onClick={() => setViewScope('all')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  viewScope === 'all'
                    ? 'bg-sky-500 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                All Bookings
              </button>
            </div>
          )}

          {/* Status Filter */}
          <div className="relative shrink-0">
            <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as BookingStatus | 'ALL')}
              className="pl-9 pr-8 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50 appearance-none cursor-pointer"
            >
              {STATUS_FILTERS.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      {isLoading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400 space-y-3">
          <RefreshCw className="w-8 h-8 animate-spin text-sky-400" />
          <p className="text-xs">Loading resource reservations...</p>
        </div>
      ) : error ? (
        <div className="p-6 bg-rose-500/10 border border-rose-500/20 rounded-2xl text-center space-y-3">
          <AlertCircle className="w-8 h-8 text-rose-400 mx-auto" />
          <p className="text-sm font-semibold text-rose-200">{error}</p>
          <button
            type="button"
            onClick={() => fetchBookings()}
            className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-medium rounded-xl transition-colors"
          >
            Retry Connection
          </button>
        </div>
      ) : filteredBookings.length === 0 ? (
        /* Honest Empty State */
        <div className="py-16 px-4 bg-slate-900/40 border border-dashed border-slate-800 rounded-2xl text-center space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-slate-800/80 flex items-center justify-center mx-auto text-slate-400">
            <Calendar className="w-6 h-6 text-sky-400/80" />
          </div>
          <div className="space-y-1">
            <h3 className="text-sm font-semibold text-white">No bookings registered</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              {searchQuery || statusFilter !== 'ALL'
                ? 'No reservations match the specified search query or filter status.'
                : 'There are no active or past resource reservations in the system.'}
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsAddModalOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
          >
            <Plus className="w-4 h-4 text-sky-400" />
            <span>Create First Booking</span>
          </button>
        </div>
      ) : (
        <>
          {/* Desktop Table View */}
          <div className="hidden md:block bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="bg-slate-800/60 text-slate-400 border-b border-slate-800 font-semibold">
                  <tr>
                    <th className="px-5 py-3.5">Reference</th>
                    <th className="px-5 py-3.5">Instrument / Equipment</th>
                    <th className="px-5 py-3.5">Schedule (UTC)</th>
                    <th className="px-5 py-3.5">User & Dept</th>
                    <th className="px-5 py-3.5">Status</th>
                    <th className="px-5 py-3.5 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {filteredBookings.map((b) => (
                    <tr
                      key={b.id}
                      onClick={() => navigate(`/bookings/${b.id}`)}
                      className="hover:bg-slate-800/40 transition-colors cursor-pointer group"
                    >
                      {/* Reference */}
                      <td className="px-5 py-4">
                        <span className="font-mono text-xs font-bold text-sky-400 group-hover:underline">
                          {b.bookingReference}
                        </span>
                        {b.isExternalBooking && (
                          <span className="block mt-0.5 text-[10px] text-amber-400 font-medium">
                            External Allocation
                          </span>
                        )}
                      </td>

                      {/* Equipment */}
                      <td className="px-5 py-4">
                        <div className="font-medium text-white">{b.equipmentName}</div>
                        {b.purpose && (
                          <div className="text-[11px] text-slate-400 line-clamp-1 max-w-xs mt-0.5">
                            {b.purpose}
                          </div>
                        )}
                      </td>

                      {/* Schedule */}
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-1.5 text-slate-200">
                          <Clock className="w-3.5 h-3.5 text-slate-400" />
                          <span>{formatDateTime(b.startTime)}</span>
                        </div>
                        <div className="text-[11px] text-slate-400 mt-0.5 flex items-center gap-1">
                          <span>to {formatDateTime(b.endTime)}</span>
                          {formatDuration(b.startTime, b.endTime) && (
                            <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-400 font-mono text-[10px]">
                              ({formatDuration(b.startTime, b.endTime)})
                            </span>
                          )}
                        </div>
                      </td>

                      {/* User & Dept */}
                      <td className="px-5 py-4">
                        <div className="text-slate-200">{b.userName || '—'}</div>
                        <div className="text-[11px] text-slate-400 flex items-center gap-1 mt-0.5">
                          <Building2 className="w-3 h-3 text-slate-500" />
                          <span>{b.departmentName || '—'}</span>
                        </div>
                      </td>

                      {/* Status */}
                      <td className="px-5 py-4">
                        <BookingStatusBadge status={b.status} />
                      </td>

                      {/* Actions */}
                      <td className="px-5 py-4 text-right" onClick={(e) => e.stopPropagation()}>
                        <div className="flex items-center justify-end gap-1.5">
                          {/* Confirm action for PENDING_APPROVAL */}
                          {b.status === 'PENDING_APPROVAL' && canApproveBooking(user?.roles, b.userId, user?.userId) && (
                            <button
                              type="button"
                              onClick={(e) => handleConfirm(e, b.id, b.bookingReference)}
                              disabled={processingId === b.id}
                              className="px-2.5 py-1 bg-sky-500/10 hover:bg-sky-500/20 text-sky-400 rounded-lg border border-sky-500/30 text-[11px] font-medium transition-colors flex items-center gap-1"
                              title="Confirm reservation"
                            >
                              <Check className="w-3 h-3" />
                              <span>Confirm</span>
                            </button>
                          )}

                          {/* Start action for CONFIRMED */}
                          {b.status === 'CONFIRMED' && (
                            <button
                              type="button"
                              onClick={(e) => handleStart(e, b.id, b.bookingReference)}
                              disabled={processingId === b.id}
                              className="px-2.5 py-1 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 rounded-lg border border-emerald-500/30 text-[11px] font-medium transition-colors flex items-center gap-1"
                              title="Start session"
                            >
                              <Play className="w-3 h-3" />
                              <span>Start</span>
                            </button>
                          )}

                          {/* Complete action for IN_USE */}
                          {b.status === 'IN_USE' && (
                            <button
                              type="button"
                              onClick={(e) => handleComplete(e, b.id, b.bookingReference)}
                              disabled={processingId === b.id}
                              className="px-2.5 py-1 bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-300 rounded-lg border border-indigo-500/30 text-[11px] font-medium transition-colors flex items-center gap-1"
                              title="Complete session"
                            >
                              <CheckCheck className="w-3 h-3" />
                              <span>Complete</span>
                            </button>
                          )}

                          {/* Cancel action for PENDING_APPROVAL and CONFIRMED */}
                          {(b.status === 'PENDING_APPROVAL' || b.status === 'CONFIRMED') && (
                            <button
                              type="button"
                              onClick={() => setBookingToCancel(b)}
                              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
                              title="Cancel reservation"
                            >
                              <XCircle className="w-4 h-4" />
                            </button>
                          )}

                          {/* Details link */}
                          <button
                            type="button"
                            onClick={() => navigate(`/bookings/${b.id}`)}
                            className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                            title="View reservation details"
                          >
                            <ArrowRight className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Mobile Card Layout */}
          <div className="grid grid-cols-1 gap-3.5 md:hidden">
            {filteredBookings.map((b) => (
              <div
                key={b.id}
                onClick={() => navigate(`/bookings/${b.id}`)}
                className="p-4 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-3 active:scale-[0.99] transition-transform cursor-pointer"
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <span className="font-mono text-xs font-bold text-sky-400">
                      {b.bookingReference}
                    </span>
                    <h3 className="text-sm font-semibold text-white mt-0.5">{b.equipmentName}</h3>
                  </div>
                  <BookingStatusBadge status={b.status} />
                </div>

                <div className="text-xs text-slate-300 space-y-1 bg-slate-800/40 p-2.5 rounded-xl border border-slate-700/30">
                  <div className="flex items-center gap-1.5 text-slate-300">
                    <Clock className="w-3.5 h-3.5 text-slate-400" />
                    <span>{formatDateTime(b.startTime)}</span>
                  </div>
                  <div className="text-[11px] text-slate-400 pl-5">
                    to {formatDateTime(b.endTime)}
                  </div>
                </div>

                <div className="flex items-center justify-between pt-1 text-xs text-slate-400">
                  <span>{b.userName || 'User'}</span>
                  <div className="flex items-center gap-1 text-sky-400 font-medium">
                    <span>View Details</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* New Booking Modal */}
      <BookingFormModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSuccess={(created) => {
          showToast(`Reservation ${created.bookingReference} created successfully.`);
          fetchBookings();
        }}
      />

      {/* Cancel Confirmation Modal */}
      <CancelBookingModal
        isOpen={Boolean(bookingToCancel)}
        booking={bookingToCancel}
        onClose={() => setBookingToCancel(null)}
        onSuccess={(updated) => {
          showToast(`Reservation ${updated.bookingReference} cancelled.`);
          setBookings((prev) => prev.map((b) => (b.id === updated.id ? updated : b)));
        }}
      />
    </div>
  );
};
