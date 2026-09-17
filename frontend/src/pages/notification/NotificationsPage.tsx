import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Bell,
  CheckCheck,
  RefreshCw,
  Search,
  SlidersHorizontal,
  MailCheck,
  Mail,
  Trash2,
  ExternalLink,
  CheckCircle2,
  AlertCircle,
  Clock,
  ShieldAlert,
  Inbox,
} from 'lucide-react';
import type {
  NotificationResponse,
  NotificationPriority,
} from '../../types/notification';
import {
  getNotifications,
  markAsRead,
  markAsUnread,
  markAllAsRead,
  deleteNotification,
} from '../../api/notifications';
import { NotificationPriorityBadge } from '../../components/notification/NotificationPriorityBadge';
import { NotificationTypeBadge } from '../../components/notification/NotificationTypeBadge';

type StatusFilter = 'ALL' | 'UNREAD' | 'READ';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Filters State
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<NotificationPriority | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Toast / Feedback State
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [isMarkingAll, setIsMarkingAll] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const loadNotifications = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getNotifications();
      setNotifications(data);
    } catch (err) {
      console.error('Failed to load notifications:', err);
      setErrorMessage('Could not load notifications. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadNotifications();
  }, [loadNotifications]);

  // Actions
  const handleToggleRead = async (notif: NotificationResponse, e: React.MouseEvent) => {
    e.stopPropagation();
    setActionLoadingId(notif.id);
    try {
      if (notif.isRead) {
        const updated = await markAsUnread(notif.id);
        setNotifications((prev) => prev.map((n) => (n.id === notif.id ? updated : n)));
        showToast('Marked as unread.');
      } else {
        const updated = await markAsRead(notif.id);
        setNotifications((prev) => prev.map((n) => (n.id === notif.id ? updated : n)));
        showToast('Marked as read.');
      }
    } catch (err) {
      console.error('Failed to update read status:', err);
      showToast('Failed to update status.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleMarkAllRead = async () => {
    setIsMarkingAll(true);
    try {
      const res = await markAllAsRead();
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, isRead: true, readAt: new Date().toISOString() }))
      );
      showToast(`Marked ${res.updatedCount} notification(s) as read.`);
    } catch (err) {
      console.error('Failed to mark all as read:', err);
      showToast('Failed to mark all as read.');
    } finally {
      setIsMarkingAll(false);
    }
  };

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    setActionLoadingId(id);
    try {
      await deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      showToast('Notification deleted.');
    } catch (err) {
      console.error('Failed to delete notification:', err);
      showToast('Failed to delete notification.');
    } finally {
      setActionLoadingId(null);
    }
  };

  // Helper for deep-linking
  const getRelatedEntityLink = (type?: string | null, id?: number | null) => {
    if (!type || !id) return null;
    switch (type.toUpperCase()) {
      case 'BOOKING':
        return { path: `/bookings/${id}`, label: `View Booking #${id}` };
      case 'EQUIPMENT':
        return { path: `/equipment/${id}`, label: `View Equipment #${id}` };
      case 'MAINTENANCE_REQUEST':
        return { path: `/maintenance/requests/${id}`, label: `View Request #${id}` };
      case 'WORK_ORDER':
        return { path: `/maintenance/work-orders/${id}`, label: `View Work Order #${id}` };
      case 'SHARING_AGREEMENT':
        return { path: `/sharing/agreements/${id}`, label: `View Agreement #${id}` };
      case 'INVOICE':
        return { path: `/cost/invoices/${id}`, label: `View Invoice #${id}` };
      default:
        return null;
    }
  };

  // Filtered List
  const filteredNotifications = useMemo(() => {
    return notifications.filter((n) => {
      // Status Filter
      if (statusFilter === 'UNREAD' && n.isRead) return false;
      if (statusFilter === 'READ' && !n.isRead) return false;

      // Priority Filter
      if (priorityFilter !== 'ALL' && n.priority !== priorityFilter) return false;

      // Search Query Filter
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesTitle = n.title?.toLowerCase().includes(q);
        const matchesMsg = n.message?.toLowerCase().includes(q);
        if (!matchesTitle && !matchesMsg) return false;
      }

      return true;
    });
  }, [notifications, statusFilter, priorityFilter, searchQuery]);

  // Derived Counts
  const unreadCount = useMemo(() => notifications.filter((n) => !n.isRead).length, [notifications]);
  const highPriorityCount = useMemo(
    () => notifications.filter((n) => n.priority === 'CRITICAL' || n.priority === 'URGENT').length,
    [notifications]
  );

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 p-4 rounded-xl bg-slate-800/95 text-white font-medium shadow-2xl border border-slate-700/80 backdrop-blur-md flex items-center gap-3 animate-in slide-in-from-bottom-5">
          <CheckCircle2 className="w-5 h-5 text-indigo-400 shrink-0" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/50 p-6 rounded-2xl border border-slate-800 backdrop-blur-md">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-gradient-to-br from-indigo-500/20 to-purple-500/20 border border-indigo-500/30 rounded-xl text-indigo-400">
            <Bell className="w-7 h-7" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white tracking-tight">
              Notification Center
            </h1>
            <p className="text-sm text-slate-400 mt-0.5">
              System alerts, reservations, maintenance schedules, and billing notices.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={loadNotifications}
            className="p-2.5 rounded-xl border border-slate-700 bg-slate-800/80 text-slate-300 hover:text-white hover:bg-slate-700/80 transition flex items-center gap-2 text-sm font-medium"
            title="Refresh Notifications"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              disabled={isMarkingAll}
              className="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium shadow-lg shadow-indigo-500/20 transition flex items-center gap-2 disabled:opacity-50"
            >
              <CheckCheck className="w-4 h-4" />
              <span>Mark All Read</span>
            </button>
          )}
        </div>
      </div>

      {/* KPI Count Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Unread Notices</div>
            <div className="text-2xl font-bold text-indigo-400 mt-1 font-mono">
              {unreadCount}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Pending user review</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
            <Mail className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Total Inbox</div>
            <div className="text-2xl font-bold text-white mt-1 font-mono">
              {notifications.length}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">All recorded notifications</div>
          </div>
          <div className="p-3 rounded-xl bg-slate-800 border border-slate-700 text-slate-300">
            <Inbox className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Urgent & Critical</div>
            <div className="text-2xl font-bold text-rose-400 mt-1 font-mono">
              {highPriorityCount}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Action required notices</div>
          </div>
          <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
            <ShieldAlert className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Controls & Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-3 justify-between items-stretch sm:items-center bg-slate-900/30 p-4 rounded-xl border border-slate-800/80">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            placeholder="Search notifications by keyword..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          {/* Status Tabs */}
          <div className="flex bg-slate-800/60 p-1 rounded-xl border border-slate-700/80 text-xs">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-3 py-1.5 rounded-lg transition font-medium ${
                statusFilter === 'ALL'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setStatusFilter('UNREAD')}
              className={`px-3 py-1.5 rounded-lg transition font-medium ${
                statusFilter === 'UNREAD'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Unread ({unreadCount})
            </button>
            <button
              onClick={() => setStatusFilter('READ')}
              className={`px-3 py-1.5 rounded-lg transition font-medium ${
                statusFilter === 'READ'
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Read
            </button>
          </div>

          {/* Priority Filter */}
          <div className="flex items-center gap-1.5">
            <SlidersHorizontal className="w-4 h-4 text-slate-500" />
            <select
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value as NotificationPriority | 'ALL')}
              className="px-3 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-xs text-slate-300 focus:outline-none focus:border-indigo-500 transition"
            >
              <option value="ALL">All Priorities</option>
              <option value="INFO">Info</option>
              <option value="WARNING">Warning</option>
              <option value="URGENT">Urgent</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>
        </div>
      </div>

      {/* Notifications List */}
      {errorMessage && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{errorMessage}</span>
          </div>
          <button
            onClick={loadNotifications}
            className="px-3 py-1 bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 rounded-lg text-xs font-medium"
          >
            Retry
          </button>
        </div>
      )}

      {isLoading ? (
        <div className="py-20 text-center text-slate-400 flex flex-col items-center gap-3">
          <RefreshCw className="w-6 h-6 animate-spin text-indigo-400" />
          <span>Loading notifications...</span>
        </div>
      ) : filteredNotifications.length === 0 ? (
        <div className="py-20 text-center bg-slate-900/20 border border-dashed border-slate-800 rounded-2xl p-8">
          <div className="w-12 h-12 rounded-full bg-slate-800/80 border border-slate-700 flex items-center justify-center text-slate-500 mx-auto mb-4">
            <Inbox className="w-6 h-6" />
          </div>
          <div className="text-base font-semibold text-slate-200">
            {notifications.length === 0 ? "You're all caught up!" : 'No matching notifications'}
          </div>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            {notifications.length === 0
              ? 'No system alerts or notifications have been posted to your account. Booking confirmations, maintenance updates, and billing invoices will appear here.'
              : 'No notifications matched your active status, priority, or search filters.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredNotifications.map((notif) => {
            const relatedLink = getRelatedEntityLink(notif.relatedEntityType, notif.relatedEntityId);
            return (
              <div
                key={notif.id}
                className={`p-5 rounded-2xl border transition relative flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-md ${
                  notif.isRead
                    ? 'bg-slate-900/30 border-slate-800/80 hover:bg-slate-800/30 text-slate-300'
                    : 'bg-slate-900/80 border-indigo-500/40 hover:bg-slate-800/60 text-white shadow-indigo-500/5 ring-1 ring-indigo-500/20'
                }`}
              >
                {/* Left details */}
                <div className="flex items-start gap-4">
                  {/* Unread indicator dot */}
                  <div className="mt-1 shrink-0">
                    {!notif.isRead ? (
                      <span className="relative flex h-2.5 w-2.5">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75" />
                        <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-indigo-500" />
                      </span>
                    ) : (
                      <span className="inline-flex rounded-full h-2.5 w-2.5 bg-slate-700" />
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <div className="flex items-center gap-2 flex-wrap">
                      <NotificationTypeBadge eventType={notif.eventType} />
                      <NotificationPriorityBadge priority={notif.priority} />
                      <span className="text-xs text-slate-500 flex items-center gap-1 font-mono">
                        <Clock className="w-3 h-3" />
                        {notif.createdAt ? new Date(notif.createdAt).toLocaleString() : 'N/A'}
                      </span>
                    </div>

                    <h3 className="text-sm font-semibold tracking-tight">
                      {notif.title}
                    </h3>
                    <p className="text-xs text-slate-400 leading-relaxed max-w-2xl">
                      {notif.message}
                    </p>

                    {relatedLink && (
                      <div className="pt-1">
                        <Link
                          to={relatedLink.path}
                          className="inline-flex items-center gap-1 text-xs font-medium text-indigo-400 hover:text-indigo-300 hover:underline"
                        >
                          <span>{relatedLink.label}</span>
                          <ExternalLink className="w-3 h-3" />
                        </Link>
                      </div>
                    )}
                  </div>
                </div>

                {/* Right action controls */}
                <div className="flex items-center justify-end gap-2 shrink-0 sm:border-l sm:border-slate-800 sm:pl-4">
                  <button
                    onClick={(e) => handleToggleRead(notif, e)}
                    disabled={actionLoadingId === notif.id}
                    className={`p-2 rounded-xl border text-xs font-medium transition flex items-center gap-1.5 ${
                      notif.isRead
                        ? 'border-slate-700 text-slate-400 hover:text-white hover:bg-slate-800'
                        : 'border-indigo-500/30 bg-indigo-600/10 text-indigo-300 hover:bg-indigo-600/20'
                    }`}
                    title={notif.isRead ? 'Mark as Unread' : 'Mark as Read'}
                  >
                    {notif.isRead ? (
                      <>
                        <Mail className="w-4 h-4" />
                        <span className="hidden md:inline">Mark Unread</span>
                      </>
                    ) : (
                      <>
                        <MailCheck className="w-4 h-4" />
                        <span className="hidden md:inline">Mark Read</span>
                      </>
                    )}
                  </button>

                  <button
                    onClick={(e) => handleDelete(notif.id, e)}
                    disabled={actionLoadingId === notif.id}
                    className="p-2 rounded-xl border border-slate-700/80 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 hover:border-rose-500/20 transition"
                    title="Delete Notification"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
