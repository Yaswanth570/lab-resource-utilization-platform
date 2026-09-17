import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Activity,
  Clock,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Plus,
  ArrowRight,
  AlertCircle,
  Sparkles,
  AlertTriangle,
  Percent,
} from 'lucide-react';
import type {
  UsageSessionResponse,
  SessionStatus,
  UtilizationRateResponse,
  IdleEventResponse,
  IdleEventStatus,
} from '../../types/utilization';
import type { EquipmentResponse } from '../../types/equipment';
import {
  getUsageSessions,
  completeUsageSession,
  terminateUsageSessionEarly,
  getUtilizationRate,
  getIdleEvents,
} from '../../api/utilization';
import { getEquipmentList } from '../../api/equipment';
import { SessionStatusBadge } from '../../components/utilization/SessionStatusBadge';
import { IdleEventStatusBadge } from '../../components/utilization/IdleEventStatusBadge';
import { UsageSessionModal } from '../../components/utilization/UsageSessionModal';
import { RecordIdleEventModal } from '../../components/utilization/RecordIdleEventModal';
import { ResolveIdleEventModal } from '../../components/utilization/ResolveIdleEventModal';
import { useAuth } from '../../context/useAuth';

type ActiveTab = 'sessions' | 'rates' | 'idle';

const SESSION_STATUS_FILTERS: { label: string; value: SessionStatus | 'ALL' }[] = [
  { label: 'All Statuses', value: 'ALL' },
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Terminated Early', value: 'TERMINATED_EARLY' },
  { label: 'Auto-Closed', value: 'AUTO_CLOSED' },
];

export const UtilizationPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [activeTab, setActiveTab] = useState<ActiveTab>('sessions');

  // Equipment List Reference
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);

  // Usage Sessions State
  const [sessions, setSessions] = useState<UsageSessionResponse[]>([]);
  const [isLoadingSessions, setIsLoadingSessions] = useState<boolean>(true);
  const [sessionSearch, setSessionSearch] = useState<string>('');
  const [sessionStatusFilter, setSessionStatusFilter] = useState<SessionStatus | 'ALL'>('ALL');
  const [sessionScope, setSessionScope] = useState<'my' | 'all'>('all');

  // Modals & Action States
  const [isRecordSessionModalOpen, setIsRecordSessionModalOpen] = useState<boolean>(false);
  const [isRecordIdleModalOpen, setIsRecordIdleModalOpen] = useState<boolean>(false);
  const [idleEventToResolve, setIdleEventToResolve] = useState<IdleEventResponse | null>(null);
  const [processingId, setProcessingId] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  // Utilization Rate Calculation Form State
  const [rateEquipmentId, setRateEquipmentId] = useState<number | ''>('');
  const [rateStartDate, setRateStartDate] = useState<string>(() => {
    const d = new Date();
    d.setDate(1); // 1st of current month
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    return `${yyyy}-${mm}-01`;
  });
  const [rateEndDate, setRateEndDate] = useState<string>(() => {
    const d = new Date();
    d.setMonth(d.getMonth() + 1, 0); // End of current month
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  });
  const [utilizationRateData, setUtilizationRateData] = useState<UtilizationRateResponse | null>(null);
  const [isLoadingRate, setIsLoadingRate] = useState<boolean>(false);
  const [rateError, setRateError] = useState<string | null>(null);

  // Idle Events State
  const [idleEvents, setIdleEvents] = useState<IdleEventResponse[]>([]);
  const [isLoadingIdle, setIsLoadingIdle] = useState<boolean>(false);
  const [idleEquipmentId, setIdleEquipmentId] = useState<number | 'ALL'>('ALL');
  const [idleStatusFilter, setIdleStatusFilter] = useState<IdleEventStatus | 'ALL'>('ALL');

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => {
      setSuccessToast(null);
    }, 4000);
  };

  // Fetch Equipment Reference
  useEffect(() => {
    getEquipmentList()
      .then((data) => {
        const bookable = data.filter((e) => e.status !== 'RETIRED');
        setEquipmentList(bookable);
        if (bookable.length > 0) {
          setRateEquipmentId(bookable[0].id);
        }
      })
      .catch((err) => console.error('Failed to load equipment list:', err));
  }, []);

  // Fetch Usage Sessions
  const fetchSessions = useCallback(async () => {
    setIsLoadingSessions(true);
    try {
      const params = sessionScope === 'my' && user?.userId ? { userId: user.userId } : {};
      const data = await getUsageSessions(params);
      setSessions(data);
    } catch (err) {
      console.error('Failed to load usage sessions:', err);
    } finally {
      setIsLoadingSessions(false);
    }
  }, [sessionScope, user]);

  useEffect(() => {
    if (activeTab === 'sessions') {
      fetchSessions();
    }
  }, [activeTab, fetchSessions]);

  // Fetch Idle Events
  const fetchIdleEvents = useCallback(async () => {
    setIsLoadingIdle(true);
    try {
      const equipId = idleEquipmentId !== 'ALL' ? idleEquipmentId : equipmentList[0]?.id;
      if (!equipId) {
        setIdleEvents([]);
        return;
      }
      const params = idleStatusFilter !== 'ALL'
        ? { equipmentId: equipId, status: idleStatusFilter }
        : { equipmentId: equipId };
      const data = await getIdleEvents(params);
      setIdleEvents(data);
    } catch (err) {
      console.error('Failed to load idle events:', err);
    } finally {
      setIsLoadingIdle(false);
    }
  }, [idleEquipmentId, idleStatusFilter, equipmentList]);

  useEffect(() => {
    if (activeTab === 'idle') {
      fetchIdleEvents();
    }
  }, [activeTab, fetchIdleEvents]);

  // Handle Complete Session Quick Action
  const handleCompleteSession = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    setProcessingId(id);
    setActionError(null);
    try {
      const completed = await completeUsageSession(id);
      setSessions((prev) => prev.map((s) => (s.id === id ? completed : s)));
      showToast('Usage session marked as completed.');
    } catch (err: unknown) {
      console.error('Failed to complete session:', err);
      setActionError(err instanceof Error ? err.message : 'Failed to complete session.');
    } finally {
      setProcessingId(null);
    }
  };

  // Handle Terminate Session Early
  const handleTerminateEarly = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    setProcessingId(id);
    setActionError(null);
    try {
      const terminated = await terminateUsageSessionEarly(id);
      setSessions((prev) => prev.map((s) => (s.id === id ? terminated : s)));
      showToast('Usage session terminated early.');
    } catch (err: unknown) {
      console.error('Failed to terminate session:', err);
      setActionError(err instanceof Error ? err.message : 'Failed to terminate session.');
    } finally {
      setProcessingId(null);
    }
  };

  // Query Utilization Rate
  const handleQueryUtilizationRate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rateEquipmentId) return;
    setIsLoadingRate(true);
    setRateError(null);
    try {
      const res = await getUtilizationRate(rateEquipmentId, rateStartDate, rateEndDate);
      setUtilizationRateData(res);
    } catch (err: unknown) {
      console.error('Failed to query utilization rate:', err);
      setRateError(err instanceof Error ? err.message : 'Failed to compute utilization rate from backend.');
    } finally {
      setIsLoadingRate(false);
    }
  };

  // Client-side filtering of usage sessions
  const filteredSessions = useMemo(() => {
    return sessions.filter((s) => {
      if (sessionStatusFilter !== 'ALL' && s.sessionStatus !== sessionStatusFilter) {
        return false;
      }
      if (sessionSearch.trim()) {
        const query = sessionSearch.trim().toLowerCase();
        const matchesEq = s.equipmentName?.toLowerCase().includes(query);
        const matchesRef = s.bookingReference?.toLowerCase().includes(query);
        const matchesUser = s.userName?.toLowerCase().includes(query);
        const matchesNotes = s.notes?.toLowerCase().includes(query);
        return matchesEq || matchesRef || matchesUser || matchesNotes;
      }
      return true;
    });
  }, [sessions, sessionStatusFilter, sessionSearch]);

  const formatDateTime = (isoString?: string | null) => {
    if (!isoString) return '—';
    try {
      return new Date(isoString).toLocaleString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
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
              Utilization & Session Tracking
            </h1>
            <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-slate-800 text-emerald-400 border border-slate-700/60">
              Active Monitoring
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Instrument check-in/out records, operating window metrics, and laboratory idle event logs.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          {activeTab === 'sessions' && (
            <button
              type="button"
              onClick={() => setIsRecordSessionModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-500/20 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Record Usage Session</span>
            </button>
          )}

          {activeTab === 'idle' && (
            <button
              type="button"
              onClick={() => setIsRecordIdleModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-400 hover:to-orange-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-amber-500/20 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Log Idle Event</span>
            </button>
          )}
        </div>
      </div>

      {/* Tabs Navigation */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-1">
        <button
          type="button"
          onClick={() => setActiveTab('sessions')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            activeTab === 'sessions'
              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Activity className="w-4 h-4" />
          <span>Usage Sessions ({sessions.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('rates')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            activeTab === 'rates'
              ? 'bg-sky-500/10 text-sky-400 border border-sky-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Percent className="w-4 h-4" />
          <span>Equipment Utilization Rate</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('idle')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-colors ${
            activeTab === 'idle'
              ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <AlertTriangle className="w-4 h-4" />
          <span>Idle Events ({idleEvents.length})</span>
        </button>
      </div>

      {/* TAB 1: USAGE SESSIONS */}
      {activeTab === 'sessions' && (
        <div className="space-y-4">
          {/* Filters Bar */}
          <div className="p-4 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl flex flex-col md:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="text"
                placeholder="Search instrument, booking reference, researcher, notes..."
                value={sessionSearch}
                onChange={(e) => setSessionSearch(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div className="inline-flex rounded-xl bg-slate-800/90 p-1 border border-slate-700/60 shrink-0">
              <button
                type="button"
                onClick={() => setSessionScope('all')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  sessionScope === 'all'
                    ? 'bg-emerald-500 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                All Sessions
              </button>
              <button
                type="button"
                onClick={() => setSessionScope('my')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  sessionScope === 'my'
                    ? 'bg-emerald-500 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                My Sessions
              </button>
            </div>

            <div className="relative shrink-0">
              <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
              <select
                value={sessionStatusFilter}
                onChange={(e) => setSessionStatusFilter(e.target.value as SessionStatus | 'ALL')}
                className="pl-9 pr-8 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 appearance-none cursor-pointer"
              >
                {SESSION_STATUS_FILTERS.map((s) => (
                  <option key={s.value} value={s.value}>
                    {s.label}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              onClick={() => fetchSessions()}
              disabled={isLoadingSessions}
              className="p-2 text-slate-400 hover:text-white bg-slate-800 rounded-xl border border-slate-700/60 transition-colors"
              title="Refresh sessions"
            >
              <RefreshCw className={`w-4 h-4 ${isLoadingSessions ? 'animate-spin text-emerald-400' : ''}`} />
            </button>
          </div>

          {/* Sessions List */}
          {isLoadingSessions ? (
            <div className="py-20 flex flex-col items-center justify-center text-slate-400 space-y-3">
              <RefreshCw className="w-8 h-8 animate-spin text-emerald-400" />
              <p className="text-xs">Loading equipment usage sessions...</p>
            </div>
          ) : filteredSessions.length === 0 ? (
            <div className="py-16 px-4 bg-slate-900/40 border border-dashed border-slate-800 rounded-2xl text-center space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800/80 flex items-center justify-center mx-auto text-slate-400">
                <Clock className="w-6 h-6 text-emerald-400/80" />
              </div>
              <div className="space-y-1">
                <h3 className="text-sm font-semibold text-white">No usage sessions recorded</h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto">
                  {sessionSearch || sessionStatusFilter !== 'ALL'
                    ? 'No sessions match your search or status criteria.'
                    : 'Check in to an instrument or link to a reservation to record your first usage session.'}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setIsRecordSessionModalOpen(true)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
              >
                <Plus className="w-4 h-4 text-emerald-400" />
                <span>Record First Session</span>
              </button>
            </div>
          ) : (
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-300">
                  <thead className="bg-slate-800/60 text-slate-400 border-b border-slate-800 font-semibold">
                    <tr>
                      <th className="px-5 py-3.5">Session ID</th>
                      <th className="px-5 py-3.5">Instrument</th>
                      <th className="px-5 py-3.5">Linked Booking</th>
                      <th className="px-5 py-3.5">Check-In / Out (UTC)</th>
                      <th className="px-5 py-3.5">Duration</th>
                      <th className="px-5 py-3.5">Status</th>
                      <th className="px-5 py-3.5 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {filteredSessions.map((s) => (
                      <tr
                        key={s.id}
                        onClick={() => navigate(`/utilization/sessions/${s.id}`)}
                        className="hover:bg-slate-800/40 transition-colors cursor-pointer group"
                      >
                        <td className="px-5 py-4 font-mono text-xs font-semibold text-emerald-400 group-hover:underline">
                          SES-{s.id}
                        </td>
                        <td className="px-5 py-4">
                          <div className="font-medium text-white">{s.equipmentName}</div>
                          <div className="text-[11px] text-slate-400">{s.userName}</div>
                        </td>
                        <td className="px-5 py-4">
                          {s.bookingReference ? (
                            <span className="font-mono text-sky-400 font-medium">
                              {s.bookingReference}
                            </span>
                          ) : (
                            <span className="text-slate-500 italic">Ad-hoc (No reservation)</span>
                          )}
                        </td>
                        <td className="px-5 py-4 space-y-0.5">
                          <div className="text-slate-200">In: {formatDateTime(s.checkedInAt)}</div>
                          <div className="text-slate-400 text-[11px]">
                            Out: {s.checkedOutAt ? formatDateTime(s.checkedOutAt) : 'In progress...'}
                          </div>
                        </td>
                        <td className="px-5 py-4 font-mono text-slate-300">
                          {s.actualDurationMinutes != null
                            ? `${s.actualDurationMinutes}m actual`
                            : 'Active'}
                          {s.scheduledDurationMinutes && (
                            <span className="block text-[11px] text-slate-500">
                              (sched: {s.scheduledDurationMinutes}m)
                            </span>
                          )}
                        </td>
                        <td className="px-5 py-4">
                          <SessionStatusBadge status={s.sessionStatus} />
                        </td>
                        <td className="px-5 py-4 text-right" onClick={(e) => e.stopPropagation()}>
                          <div className="flex items-center justify-end gap-1.5">
                            {s.sessionStatus === 'ACTIVE' && (
                              <>
                                <button
                                  type="button"
                                  onClick={(e) => handleCompleteSession(e, s.id)}
                                  disabled={processingId === s.id}
                                  className="px-2.5 py-1 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 rounded-lg border border-emerald-500/30 text-[11px] font-medium transition-colors"
                                  title="Complete session now"
                                >
                                  Complete
                                </button>
                                <button
                                  type="button"
                                  onClick={(e) => handleTerminateEarly(e, s.id)}
                                  disabled={processingId === s.id}
                                  className="px-2.5 py-1 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 rounded-lg border border-amber-500/30 text-[11px] font-medium transition-colors"
                                  title="Terminate early"
                                >
                                  Early Stop
                                </button>
                              </>
                            )}
                            <button
                              type="button"
                              onClick={() => navigate(`/utilization/sessions/${s.id}`)}
                              className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                              title="View details"
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
          )}
        </div>
      )}

      {/* TAB 2: EQUIPMENT UTILIZATION RATE */}
      {activeTab === 'rates' && (
        <div className="space-y-6">
          {/* Query Form */}
          <div className="p-6 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl space-y-4">
            <div>
              <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                <Percent className="w-4 h-4 text-sky-400" />
                Authoritative Operating Window Utilization Rate
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Calculates actual utilization percentage strictly based on Monday–Saturday, 08:00–20:00 UTC (720 operating mins/day) and completed session durations.
              </p>
            </div>

            <form onSubmit={handleQueryUtilizationRate} className="grid grid-cols-1 sm:grid-cols-4 gap-4 items-end">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">Select Instrument</label>
                <select
                  value={rateEquipmentId}
                  onChange={(e) => setRateEquipmentId(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50"
                >
                  <option value="">-- Choose Equipment --</option>
                  {equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name} [{eq.assetTag}]
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">Start Date</label>
                <input
                  type="date"
                  value={rateStartDate}
                  onChange={(e) => setRateStartDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">End Date</label>
                <input
                  type="date"
                  value={rateEndDate}
                  onChange={(e) => setRateEndDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-sky-500/50"
                />
              </div>

              <div>
                <button
                  type="submit"
                  disabled={isLoadingRate || !rateEquipmentId}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-sky-600/20 transition-colors disabled:opacity-50"
                >
                  {isLoadingRate ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Percent className="w-4 h-4" />}
                  <span>Calculate Rate</span>
                </button>
              </div>
            </form>
          </div>

          {/* Query Results */}
          {rateError && (
            <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start gap-2.5 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{rateError}</span>
            </div>
          )}

          {utilizationRateData ? (
            <div className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl space-y-4 animate-in fade-in duration-200">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4">
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block">
                    Calculated Utilization Percentage
                  </span>
                  <div className="text-3xl sm:text-4xl font-mono font-bold text-emerald-400 mt-1">
                    {utilizationRateData.utilizationPercentage}%
                  </div>
                </div>

                <div className="text-right text-xs text-slate-400 space-y-1">
                  <div>Period: <strong className="text-slate-200">{utilizationRateData.startDate}</strong> to <strong className="text-slate-200">{utilizationRateData.endDate}</strong></div>
                  <div>Instrument ID: <span className="font-mono text-sky-400">#{utilizationRateData.equipmentId}</span></div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs text-slate-400">
                <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 space-y-1">
                  <span className="font-semibold text-slate-300 block">Authoritative Calculation</span>
                  <p className="text-[11px]">
                    Values are directly returned by the backend algorithm without client-side modifications. Operating windows exclude Sundays.
                  </p>
                </div>
                <div className="p-3.5 bg-slate-800/40 rounded-xl border border-slate-700/40 space-y-1">
                  <span className="font-semibold text-slate-300 block">Baseline Parameters</span>
                  <p className="text-[11px]">
                    Operating window: 08:00 – 20:00 UTC (12 hours / 720 minutes per day, Monday through Saturday).
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="py-12 px-4 bg-slate-900/30 border border-dashed border-slate-800 rounded-2xl text-center space-y-2">
              <Percent className="w-8 h-8 text-slate-500 mx-auto" />
              <p className="text-xs text-slate-400">
                Select an instrument and calendar window above to compute the utilization rate from the backend service.
              </p>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: IDLE EVENTS */}
      {activeTab === 'idle' && (
        <div className="space-y-4">
          {/* Filters Bar */}
          <div className="p-4 bg-slate-900/80 backdrop-blur-md border border-slate-800 rounded-2xl flex flex-col md:flex-row gap-3">
            <div className="flex-1">
              <select
                value={idleEquipmentId}
                onChange={(e) => setIdleEquipmentId(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50"
              >
                {equipmentList.map((eq) => (
                  <option key={eq.id} value={eq.id}>
                    Filter by: {eq.name} [{eq.assetTag}]
                  </option>
                ))}
              </select>
            </div>

            <div className="relative shrink-0">
              <select
                value={idleStatusFilter}
                onChange={(e) => setIdleStatusFilter(e.target.value as IdleEventStatus | 'ALL')}
                className="px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-amber-500/50"
              >
                <option value="ALL">All Event Statuses</option>
                <option value="ONGOING">Ongoing</option>
                <option value="RESOLVED">Resolved</option>
                <option value="ACKNOWLEDGED">Acknowledged</option>
              </select>
            </div>

            <button
              type="button"
              onClick={() => fetchIdleEvents()}
              disabled={isLoadingIdle}
              className="p-2 text-slate-400 hover:text-white bg-slate-800 rounded-xl border border-slate-700/60 transition-colors"
              title="Refresh idle events"
            >
              <RefreshCw className={`w-4 h-4 ${isLoadingIdle ? 'animate-spin text-amber-400' : ''}`} />
            </button>
          </div>

          {/* Idle Events List */}
          {isLoadingIdle ? (
            <div className="py-20 flex flex-col items-center justify-center text-slate-400 space-y-3">
              <RefreshCw className="w-8 h-8 animate-spin text-amber-400" />
              <p className="text-xs">Loading idle events...</p>
            </div>
          ) : idleEvents.length === 0 ? (
            <div className="py-16 px-4 bg-slate-900/40 border border-dashed border-slate-800 rounded-2xl text-center space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800/80 flex items-center justify-center mx-auto text-slate-400">
                <AlertTriangle className="w-6 h-6 text-amber-400/80" />
              </div>
              <div className="space-y-1">
                <h3 className="text-sm font-semibold text-white">No idle events logged</h3>
                <p className="text-xs text-slate-400 max-w-sm mx-auto">
                  There are no manual audit or no-show idle events recorded for this instrument.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setIsRecordIdleModalOpen(true)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white rounded-xl text-xs font-medium transition-colors"
              >
                <Plus className="w-4 h-4 text-amber-400" />
                <span>Log Idle Event</span>
              </button>
            </div>
          ) : (
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-300">
                  <thead className="bg-slate-800/60 text-slate-400 border-b border-slate-800 font-semibold">
                    <tr>
                      <th className="px-5 py-3.5">Event ID</th>
                      <th className="px-5 py-3.5">Instrument</th>
                      <th className="px-5 py-3.5">Detection Source</th>
                      <th className="px-5 py-3.5">Idle Timing (UTC)</th>
                      <th className="px-5 py-3.5">Duration</th>
                      <th className="px-5 py-3.5">Status</th>
                      <th className="px-5 py-3.5 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {idleEvents.map((ev) => (
                      <tr key={ev.id} className="hover:bg-slate-800/40 transition-colors">
                        <td className="px-5 py-4 font-mono text-xs font-semibold text-amber-400">
                          IDLE-{ev.id}
                        </td>
                        <td className="px-5 py-4">
                          <div className="font-medium text-white">{ev.equipmentName}</div>
                          {ev.notes && <div className="text-[11px] text-slate-400 line-clamp-1">{ev.notes}</div>}
                        </td>
                        <td className="px-5 py-4">
                          <span className="px-2 py-0.5 rounded text-[11px] font-mono bg-slate-800 text-slate-300 border border-slate-700">
                            {ev.detectionSource}
                          </span>
                        </td>
                        <td className="px-5 py-4 space-y-0.5">
                          <div>From: {formatDateTime(ev.idleStartTime)}</div>
                          <div className="text-slate-400 text-[11px]">
                            To: {ev.idleEndTime ? formatDateTime(ev.idleEndTime) : 'Ongoing...'}
                          </div>
                        </td>
                        <td className="px-5 py-4 font-mono text-slate-300">
                          {ev.idleDurationMinutes != null ? `${ev.idleDurationMinutes}m` : '—'}
                        </td>
                        <td className="px-5 py-4">
                          <IdleEventStatusBadge status={ev.status} />
                        </td>
                        <td className="px-5 py-4 text-right">
                          {ev.status === 'ONGOING' && (
                            <button
                              type="button"
                              onClick={() => setIdleEventToResolve(ev)}
                              className="px-2.5 py-1 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 rounded-lg border border-emerald-500/30 text-[11px] font-medium transition-colors"
                            >
                              Resolve
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Record Usage Session Modal */}
      <UsageSessionModal
        isOpen={isRecordSessionModalOpen}
        onClose={() => setIsRecordSessionModalOpen(false)}
        onSuccess={(created) => {
          showToast(`Usage session SES-${created.id} recorded successfully.`);
          fetchSessions();
        }}
      />

      {/* Record Idle Event Modal */}
      <RecordIdleEventModal
        isOpen={isRecordIdleModalOpen}
        onClose={() => setIsRecordIdleModalOpen(false)}
        onSuccess={(created) => {
          showToast(`Idle event IDLE-${created.id} logged.`);
          fetchIdleEvents();
        }}
      />

      {/* Resolve Idle Event Modal */}
      <ResolveIdleEventModal
        isOpen={Boolean(idleEventToResolve)}
        idleEvent={idleEventToResolve}
        onClose={() => setIdleEventToResolve(null)}
        onSuccess={(updated) => {
          showToast(`Idle event IDLE-${updated.id} marked as resolved.`);
          setIdleEvents((prev) => prev.map((e) => (e.id === updated.id ? updated : e)));
        }}
      />
    </div>
  );
};
