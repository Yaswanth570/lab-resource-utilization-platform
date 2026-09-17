import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  CalendarDays,
  Clock,
  CheckCircle2,
  ArrowRight,
  Cpu,
  Bell,
  Search,
  PlusCircle,
} from 'lucide-react';
import { getBookings } from '../../api/booking';
import { notificationApi } from '../../api/notifications';
import type { BookingResponse } from '../../types/booking';
import type { NotificationResponse } from '../../types/notification';

export const ResearcherDashboardView: React.FC = () => {
  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const loadData = async () => {
      try {
        const [bookingsRes, notifsRes] = await Promise.allSettled([
          getBookings(),
          notificationApi.getNotifications(),
        ]);

        if (isMounted) {
          if (bookingsRes.status === 'fulfilled') {
            setBookings(bookingsRes.value);
          }
          if (notifsRes.status === 'fulfilled') {
            setNotifications(notifsRes.value || []);
          }
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadData();
    return () => {
      isMounted = false;
    };
  }, []);

  const now = new Date();
  const upcomingBookings = bookings.filter(
    (b) =>
      (b.status === 'CONFIRMED' || b.status === 'IN_USE') &&
      new Date(b.endTime) >= now
  );
  const pendingBookings = bookings.filter((b) => b.status === 'PENDING_APPROVAL');
  const completedBookings = bookings.filter((b) => b.status === 'COMPLETED');

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            Confirmed
          </span>
        );
      case 'PENDING_APPROVAL':
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/10 border border-amber-500/30 text-amber-400">
            Pending Approval
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-sky-500/10 border border-sky-500/30 text-sky-400">
            Completed
          </span>
        );
      default:
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-slate-800 text-slate-400">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Quick Action Shortcuts */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Link
          to="/equipment"
          className="flex items-center justify-between p-5 rounded-2xl bg-gradient-to-r from-sky-950/40 to-slate-900 border border-sky-800/40 hover:border-sky-500/60 transition-all group shadow-lg"
        >
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20 group-hover:scale-105 transition-transform">
              <Search className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white group-hover:text-sky-300 transition-colors">
                Browse & Search Equipment
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Explore available lab instruments and check real-time availability
              </p>
            </div>
          </div>
          <ArrowRight className="w-5 h-5 text-slate-500 group-hover:text-sky-400 group-hover:translate-x-1 transition-all" />
        </Link>

        <Link
          to="/bookings"
          className="flex items-center justify-between p-5 rounded-2xl bg-gradient-to-r from-emerald-950/30 to-slate-900 border border-emerald-800/40 hover:border-emerald-500/60 transition-all group shadow-lg"
        >
          <div className="flex items-center gap-4">
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 group-hover:scale-105 transition-transform">
              <PlusCircle className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white group-hover:text-emerald-300 transition-colors">
                Create Reservation
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Book authorized operational time windows on calibrated resources
              </p>
            </div>
          </div>
          <ArrowRight className="w-5 h-5 text-slate-500 group-hover:text-emerald-400 group-hover:translate-x-1 transition-all" />
        </Link>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Upcoming Confirmed</div>
            <div className="text-2xl font-bold text-white mt-1">{upcomingBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Ready for your sessions</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <CalendarDays className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Pending Approval</div>
            <div className="text-2xl font-bold text-amber-400 mt-1">{pendingBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Under Lab Manager review</div>
          </div>
          <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Completed Sessions</div>
            <div className="text-2xl font-bold text-sky-400 mt-1">{completedBookings.length}</div>
            <div className="text-[11px] text-slate-500 mt-1">Total research sessions</div>
          </div>
          <div className="p-3 rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Two Column Layout: Upcoming/Pending Bookings & Notifications */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Bookings Column (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Upcoming Bookings */}
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <CalendarDays className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Upcoming Reservations</h3>
              </div>
              <Link to="/bookings" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                View All &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-8 text-center text-slate-400 text-sm">Loading your bookings...</div>
            ) : upcomingBookings.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-6">
                <Cpu className="w-8 h-8 text-slate-600 mx-auto mb-2" />
                <p className="text-sm text-slate-400 font-medium">No upcoming reservations</p>
                <p className="text-xs text-slate-500 mt-1">Explore equipment catalog to book lab resources</p>
              </div>
            ) : (
              <div className="space-y-3">
                {upcomingBookings.slice(0, 4).map((b) => (
                  <div
                    key={b.id}
                    className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700/80 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-white">
                          {b.equipmentName || `Equipment #${b.equipmentId}`}
                        </span>
                        {getStatusBadge(b.status)}
                      </div>
                      <div className="flex items-center gap-3 text-xs text-slate-400 mt-1.5 font-mono">
                        <span>Ref: {b.bookingReference}</span>
                        <span>&bull;</span>
                        <span>
                          {new Date(b.startTime).toLocaleDateString()} {new Date(b.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    </div>
                    <Link
                      to={`/bookings/${b.id}`}
                      className="inline-flex items-center justify-center px-3 py-1.5 rounded-lg bg-slate-800 text-slate-200 hover:text-white hover:bg-slate-700 text-xs font-medium transition-colors"
                    >
                      Details
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Pending Approval Bookings */}
          {pendingBookings.length > 0 && (
            <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <Clock className="w-5 h-5 text-amber-400" />
                  <h3 className="text-base font-semibold text-white">Pending Lab Manager Approval</h3>
                </div>
                <span className="text-xs text-amber-400/80 font-medium">
                  {pendingBookings.length} awaiting review
                </span>
              </div>

              <div className="space-y-3">
                {pendingBookings.slice(0, 3).map((b) => (
                  <div
                    key={b.id}
                    className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-slate-200">
                          {b.equipmentName || `Equipment #${b.equipmentId}`}
                        </span>
                        {getStatusBadge(b.status)}
                      </div>
                      <div className="text-xs text-slate-400 mt-1">
                        Purpose: {b.purpose || 'Research session'} &bull; Requested for {new Date(b.startTime).toLocaleDateString()}
                      </div>
                    </div>
                    <Link
                      to={`/bookings/${b.id}`}
                      className="inline-flex items-center justify-center px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800 text-xs font-medium transition-colors"
                    >
                      View Request
                    </Link>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Notifications Column (1 col) */}
        <div className="space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-sky-400" />
                <h3 className="text-base font-semibold text-white">Recent Alerts</h3>
              </div>
              <Link to="/notifications" className="text-xs text-sky-400 hover:text-sky-300 font-medium">
                View All &rarr;
              </Link>
            </div>

            {loading ? (
              <div className="py-6 text-center text-slate-400 text-sm">Loading alerts...</div>
            ) : notifications.length === 0 ? (
              <div className="py-8 text-center bg-slate-950/40 rounded-xl border border-slate-800/60 p-4">
                <CheckCircle2 className="w-6 h-6 text-emerald-500 mx-auto mb-1.5" />
                <p className="text-xs text-slate-400 font-medium">All caught up!</p>
                <p className="text-[11px] text-slate-500">No unread notifications</p>
              </div>
            ) : (
              <div className="space-y-3">
                {notifications.slice(0, 5).map((n) => (
                  <div
                    key={n.id}
                    className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700 transition-all text-xs"
                  >
                    <div className="font-semibold text-slate-200">{n.title}</div>
                    <p className="text-slate-400 mt-1 line-clamp-2">{n.message}</p>
                    <div className="text-[10px] text-slate-500 mt-2 font-mono">
                      {new Date(n.createdAt).toLocaleString()}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
