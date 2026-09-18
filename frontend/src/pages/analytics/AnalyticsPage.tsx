import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart3,
  Activity,
  CalendarDays,
  Clock,
  Wrench,
  Receipt,
  Cpu,
  CheckCircle2,
  AlertTriangle,
  RotateCw,
  TrendingUp,
  Building2,
  Layers,
  ChevronRight,
  SlidersHorizontal,
} from 'lucide-react';
import {
  getOverview,
  getUtilizationAnalytics,
  getBookingAnalytics,
  getMaintenanceAnalytics,
  getCostAnalytics,
  getEquipmentPerformance,
  getTrends,
} from '../../api/analytics';
import { getDepartmentCostSummary } from '../../api/cost';
import { useAuth } from '../../context/useAuth';
import { getDepartmentsByInstitution } from '../../api/equipment';
import type {
  AnalyticsOverviewResponse,
  UtilizationAnalyticsResponse,
  BookingAnalyticsResponse,
  MaintenanceAnalyticsResponse,
  CostAnalyticsResponse,
  EquipmentPerformanceResponse,
  TrendAnalyticsResponse,
} from '../../types/analytics';

type ActiveTab = 'overview' | 'utilization' | 'bookings' | 'maintenance' | 'cost' | 'performance';

export const AnalyticsPage: React.FC = () => {
  const { user } = useAuth();

  // Tab State
  const [activeTab, setActiveTab] = useState<ActiveTab>('overview');

  // Filters State
  const [startDate, setStartDate] = useState<string>(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState<string>(() => new Date().toISOString().split('T')[0]);
  const [departmentId, setDepartmentId] = useState<string>('');

  // Department List for Filter
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);

  // Analytics Data States
  const [overview, setOverview] = useState<AnalyticsOverviewResponse | null>(null);
  const [utilization, setUtilization] = useState<UtilizationAnalyticsResponse | null>(null);
  const [bookings, setBookings] = useState<BookingAnalyticsResponse | null>(null);
  const [maintenance, setMaintenance] = useState<MaintenanceAnalyticsResponse | null>(null);
  const [cost, setCost] = useState<CostAnalyticsResponse | null>(null);
  const [performance, setPerformance] = useState<EquipmentPerformanceResponse | null>(null);
  const [trends, setTrends] = useState<TrendAnalyticsResponse | null>(null);

  // Status States
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Load Department Options
  useEffect(() => {
    const loadDepts = async () => {
      if (user?.institutionId) {
        try {
          const depts = await getDepartmentsByInstitution(user.institutionId);
          if (depts.length > 0) {
            setDepartments(
              depts.map((d) => ({
                id: d.id,
                name: `${d.name} (${d.code})`,
              }))
            );
            return;
          }
        } catch {
          // Fall back to cost summary if institution lookup fails
        }
      }

      try {
        const summaries = await getDepartmentCostSummary();
        setDepartments(
          summaries.map((d) => ({
            id: d.departmentId,
            name: d.departmentName,
          }))
        );
      } catch {
        // Non-fatal if department list fails to load
      }
    };
    loadDepts();
  }, [user?.institutionId]);

  // Fetch Analytics Data
  const fetchAnalytics = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    const params = {
      startDate: startDate || undefined,
      endDate: endDate || undefined,
      departmentId: departmentId ? Number(departmentId) : undefined,
    };

    try {
      const [ov, ut, bk, mt, ct, pf, tr] = await Promise.all([
        getOverview(params),
        getUtilizationAnalytics(params),
        getBookingAnalytics(params),
        getMaintenanceAnalytics(params),
        getCostAnalytics(params),
        getEquipmentPerformance(params),
        getTrends(params),
      ]);

      setOverview(ov);
      setUtilization(ut);
      setBookings(bk);
      setMaintenance(mt);
      setCost(ct);
      setPerformance(pf);
      setTrends(tr);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load analytics records';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [startDate, endDate, departmentId]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  // Preset Date Handlers
  const handlePreset = (days: number) => {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - days);
    setStartDate(start.toISOString().split('T')[0]);
    setEndDate(end.toISOString().split('T')[0]);
  };

  const handleThisMonth = () => {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    setStartDate(start.toISOString().split('T')[0]);
    setEndDate(now.toISOString().split('T')[0]);
  };

  // Completion rate calculation
  const completionRate =
    overview && overview.totalBookings > 0
      ? Math.round((overview.completedBookings / overview.totalBookings) * 100)
      : 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-sky-500" />
            Analytics & Utilization Insights
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Operational intelligence, equipment uptime, booking trends, and financial summaries derived directly from domain data.
          </p>
        </div>
        <button
          onClick={fetchAnalytics}
          disabled={isLoading}
          className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm font-medium rounded-lg border border-slate-700 transition disabled:opacity-50"
        >
          <RotateCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh Data
        </button>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4">
          {/* Presets */}
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1 mr-1">
              <SlidersHorizontal className="w-3.5 h-3.5" />
              Presets:
            </span>
            <button
              onClick={() => handlePreset(7)}
              className="px-2.5 py-1 text-xs font-medium rounded-md bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition"
            >
              Last 7 Days
            </button>
            <button
              onClick={() => handlePreset(30)}
              className="px-2.5 py-1 text-xs font-medium rounded-md bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition"
            >
              Last 30 Days
            </button>
            <button
              onClick={() => handlePreset(90)}
              className="px-2.5 py-1 text-xs font-medium rounded-md bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition"
            >
              Last 90 Days
            </button>
            <button
              onClick={handleThisMonth}
              className="px-2.5 py-1 text-xs font-medium rounded-md bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition"
            >
              This Month
            </button>
          </div>

          {/* Custom Date Inputs & Department Filter */}
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-2 text-xs text-slate-400">
              <span>From:</span>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="px-2.5 py-1 text-xs rounded-md bg-slate-800 border border-slate-700 text-white focus:outline-none focus:border-sky-500"
              />
            </div>
            <div className="flex items-center gap-2 text-xs text-slate-400">
              <span>To:</span>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="px-2.5 py-1 text-xs rounded-md bg-slate-800 border border-slate-700 text-white focus:outline-none focus:border-sky-500"
              />
            </div>
            {departments.length > 0 && (
              <div className="flex items-center gap-2 text-xs text-slate-400">
                <Building2 className="w-3.5 h-3.5" />
                <select
                  value={departmentId}
                  onChange={(e) => setDepartmentId(e.target.value)}
                  className="px-2.5 py-1 text-xs rounded-md bg-slate-800 border border-slate-700 text-white focus:outline-none focus:border-sky-500"
                >
                  <option value="">All Departments</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400 text-sm flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
          <button
            onClick={fetchAnalytics}
            className="text-xs font-semibold underline hover:text-rose-300"
          >
            Try Again
          </button>
        </div>
      )}

      {/* Top Overview KPI Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Total Bookings */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-xl shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Bookings Volume</span>
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
              <CalendarDays className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold text-white">
              {isLoading ? '...' : (overview?.totalBookings ?? 0).toLocaleString()}
            </span>
          </div>
          <div className="mt-2 text-xs text-slate-400 flex items-center justify-between">
            <span>Completed: <strong className="text-slate-200">{overview?.completedBookings ?? 0}</strong></span>
            <span className="text-emerald-400 font-semibold">{completionRate}% fulfillment</span>
          </div>
        </div>

        {/* Utilization */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-xl shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Avg. Utilization</span>
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
              <Activity className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-emerald-400">
              {isLoading ? '...' : `${overview?.averageUtilizationPercentage ?? 0}%`}
            </span>
            <span className="text-xs text-slate-400">of operating window</span>
          </div>
          <div className="mt-2 text-xs text-slate-400 flex items-center justify-between">
            <span>Used: <strong className="text-slate-200">{overview?.totalUsageHours ?? 0}h</strong></span>
            <span>Operating: <strong className="text-slate-200">{overview?.totalOperatingHours ?? 0}h</strong></span>
          </div>
        </div>

        {/* Downtime */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-xl shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Total Downtime</span>
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-400">
              <Wrench className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold text-white">
              {isLoading ? '...' : `${overview?.totalDowntimeHours ?? 0}h`}
            </span>
          </div>
          <div className="mt-2 text-xs text-slate-400 flex items-center justify-between">
            <span>Incidents: <strong className="text-slate-200">{overview?.downtimeIncidentCount ?? 0}</strong></span>
            <span>Open Orders: <strong className="text-amber-400">{overview?.openWorkOrderCount ?? 0}</strong></span>
          </div>
        </div>

        {/* Cost & Billing */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-xl shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Resource Spend</span>
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400">
              <Receipt className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl font-bold text-white">
              {isLoading ? '...' : `$${Number(overview?.totalCost ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
            </span>
          </div>
          <div className="mt-2 text-xs text-slate-400 flex items-center justify-between">
            <span>Settled: <strong className="text-emerald-400">${Number(overview?.settledCost ?? 0).toFixed(0)}</strong></span>
            <span>Unbilled: <strong className="text-amber-400">${Number(overview?.unbilledCost ?? 0).toFixed(0)}</strong></span>
          </div>
        </div>
      </div>

      {/* Tabs Bar */}
      <div className="border-b border-slate-800">
        <nav className="flex space-x-2 overflow-x-auto pb-px">
          {[
            { id: 'overview', label: 'Overview & Trends', icon: TrendingUp },
            { id: 'utilization', label: 'Equipment Utilization', icon: Activity },
            { id: 'bookings', label: 'Bookings Volume', icon: CalendarDays },
            { id: 'maintenance', label: 'Maintenance & Downtime', icon: Wrench },
            { id: 'cost', label: 'Cost & Spending', icon: Receipt },
            { id: 'performance', label: 'Equipment Rankings', icon: Layers },
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as ActiveTab)}
                className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 whitespace-nowrap transition-colors ${
                  isActive
                    ? 'border-sky-500 text-sky-400 font-semibold'
                    : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
                }`}
              >
                <Icon className="w-4 h-4" />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* TAB 1: OVERVIEW & TRENDS */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          {/* Daily Trend Timeline */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-base font-semibold text-white flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-sky-400" />
                  Daily Activity Timeline
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Day-by-day operational trends across bookings, usage, downtime, and spend.
                </p>
              </div>
            </div>

            {isLoading ? (
              <div className="h-48 flex items-center justify-center text-slate-500 text-sm">
                Loading timeline records...
              </div>
            ) : !trends || trends.dailyTrends.length === 0 ? (
              <div className="h-48 flex flex-col items-center justify-center text-slate-500 text-sm">
                <Clock className="w-8 h-8 mb-2 opacity-50" />
                No daily activity recorded for this period.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Date</th>
                      <th className="py-2.5 px-3">Bookings</th>
                      <th className="py-2.5 px-3">Usage Hours</th>
                      <th className="py-2.5 px-3">Downtime Hours</th>
                      <th className="py-2.5 px-3 text-right">Daily Cost</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {trends.dailyTrends.map((pt) => (
                      <tr key={pt.date} className="hover:bg-slate-800/40 transition">
                        <td className="py-2.5 px-3 font-medium text-slate-200">{pt.date}</td>
                        <td className="py-2.5 px-3 text-slate-300">
                          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                            pt.bookingCount > 0 ? 'bg-sky-500/10 text-sky-400' : 'text-slate-500'
                          }`}>
                            {pt.bookingCount}
                          </span>
                        </td>
                        <td className="py-2.5 px-3 text-slate-300">{pt.usageHours}h</td>
                        <td className="py-2.5 px-3">
                          <span className={pt.downtimeHours > 0 ? 'text-amber-400 font-semibold' : 'text-slate-500'}>
                            {pt.downtimeHours}h
                          </span>
                        </td>
                        <td className="py-2.5 px-3 text-right font-medium text-slate-200">
                          ${Number(pt.cost).toFixed(2)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Highlights Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
              <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Active Catalog Footprint
              </h3>
              <div className="flex items-center gap-3 mt-3">
                <Cpu className="w-8 h-8 text-sky-400" />
                <div>
                  <span className="text-2xl font-bold text-white">
                    {overview?.activeEquipmentCount ?? 0}
                  </span>
                  <span className="block text-xs text-slate-400">Instruments available / in-use</span>
                </div>
              </div>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
              <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Maintenance Activity
              </h3>
              <div className="flex items-center gap-3 mt-3">
                <Wrench className="w-8 h-8 text-amber-400" />
                <div>
                  <span className="text-2xl font-bold text-white">
                    {overview?.maintenanceRequestCount ?? 0}
                  </span>
                  <span className="block text-xs text-slate-400">Total requests logged in range</span>
                </div>
              </div>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
              <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Settled Invoicing Rate
              </h3>
              <div className="flex items-center gap-3 mt-3">
                <Receipt className="w-8 h-8 text-emerald-400" />
                <div>
                  <span className="text-2xl font-bold text-emerald-400">
                    {overview && overview.totalCost > 0
                      ? Math.round((overview.settledCost / overview.totalCost) * 100)
                      : 0}%
                  </span>
                  <span className="block text-xs text-slate-400">Reconciled cost settlement</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: EQUIPMENT UTILIZATION */}
      {activeTab === 'utilization' && (
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-4">
              <div>
                <h2 className="text-base font-semibold text-white flex items-center gap-2">
                  <Activity className="w-5 h-5 text-emerald-400" />
                  Instrument Utilization Breakdown
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Calculated against standard operating window (Mon–Sat 08:00–20:00 = 720 operating min/day, Sundays excluded).
                </p>
              </div>
              <div className="text-xs text-slate-400">
                Total Sessions: <strong className="text-white">{utilization?.totalSessions ?? 0}</strong>
              </div>
            </div>

            {isLoading ? (
              <div className="h-48 flex items-center justify-center text-slate-500 text-sm">
                Calculating utilization rates...
              </div>
            ) : !utilization || utilization.equipmentMetrics.length === 0 ? (
              <div className="h-48 flex flex-col items-center justify-center text-slate-500 text-sm">
                <Cpu className="w-8 h-8 mb-2 opacity-50" />
                No equipment records found for this period.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Equipment</th>
                      <th className="py-2.5 px-3">Department</th>
                      <th className="py-2.5 px-3">Usage Hours</th>
                      <th className="py-2.5 px-3">Operating Hours</th>
                      <th className="py-2.5 px-3">Sessions</th>
                      <th className="py-2.5 px-3 w-48">Utilization %</th>
                      <th className="py-2.5 px-3 text-right">Details</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {utilization.equipmentMetrics.map((item) => (
                      <tr key={item.equipmentId} className="hover:bg-slate-800/40 transition">
                        <td className="py-3 px-3 font-medium text-white">{item.equipmentName}</td>
                        <td className="py-3 px-3 text-slate-400">{item.departmentName || '—'}</td>
                        <td className="py-3 px-3 text-slate-200 font-semibold">{item.actualUsageHours}h</td>
                        <td className="py-3 px-3 text-slate-400">{item.operatingHours}h</td>
                        <td className="py-3 px-3 text-slate-300">{item.sessionCount}</td>
                        <td className="py-3 px-3">
                          <div className="flex items-center gap-2">
                            <div className="w-24 bg-slate-800 rounded-full h-2 overflow-hidden">
                              <div
                                className={`h-full rounded-full ${
                                  item.utilizationPercentage >= 70
                                    ? 'bg-emerald-500'
                                    : item.utilizationPercentage >= 30
                                    ? 'bg-sky-500'
                                    : 'bg-slate-600'
                                }`}
                                style={{ width: `${Math.min(100, item.utilizationPercentage)}%` }}
                              />
                            </div>
                            <span className="font-semibold text-slate-200">
                              {item.utilizationPercentage}%
                            </span>
                          </div>
                        </td>
                        <td className="py-3 px-3 text-right">
                          <Link
                            to={`/equipment/${item.equipmentId}`}
                            className="text-sky-400 hover:text-sky-300 inline-flex items-center gap-1 font-medium"
                          >
                            View <ChevronRight className="w-3.5 h-3.5" />
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 3: BOOKINGS */}
      {activeTab === 'bookings' && (
        <div className="space-y-6">
          {/* Status Distribution Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {bookings &&
              Object.entries(bookings.statusDistribution).map(([status, count]) => (
                <div key={status} className="p-4 bg-slate-900 border border-slate-800 rounded-xl">
                  <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block">
                    {status.replace(/_/g, ' ')}
                  </span>
                  <span className="text-xl font-bold text-white mt-1 block">
                    {count.toLocaleString()}
                  </span>
                </div>
              ))}
          </div>

          {/* Top Booked Equipment Table */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-base font-semibold text-white mb-3 flex items-center gap-2">
              <CalendarDays className="w-5 h-5 text-sky-400" />
              Equipment Booking Frequency
            </h3>
            {isLoading ? (
              <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                Loading booking records...
              </div>
            ) : !bookings || bookings.equipmentFrequencies.length === 0 ? (
              <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                No bookings found for the selected period.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Equipment</th>
                      <th className="py-2.5 px-3">Booking Count</th>
                      <th className="py-2.5 px-3">Total Booked Hours</th>
                      <th className="py-2.5 px-3 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {bookings.equipmentFrequencies.map((f) => (
                      <tr key={f.equipmentId} className="hover:bg-slate-800/40 transition">
                        <td className="py-2.5 px-3 font-medium text-white">{f.equipmentName}</td>
                        <td className="py-2.5 px-3 text-slate-300 font-semibold">{f.bookingCount}</td>
                        <td className="py-2.5 px-3 text-slate-300">{f.bookedHours}h</td>
                        <td className="py-2.5 px-3 text-right">
                          <Link
                            to={`/equipment/${f.equipmentId}`}
                            className="text-sky-400 hover:text-sky-300 inline-flex items-center gap-1"
                          >
                            Catalog <ChevronRight className="w-3.5 h-3.5" />
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 4: MAINTENANCE & DOWNTIME */}
      {activeTab === 'maintenance' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Downtime by Reason Category */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
              <h3 className="text-base font-semibold text-white mb-3 flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-amber-400" />
                Downtime by Reason Category
              </h3>
              {isLoading ? (
                <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                  Calculating downtime categories...
                </div>
              ) : !maintenance || maintenance.downtimeByCategory.length === 0 ? (
                <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                  Zero downtime incidents recorded in this window.
                </div>
              ) : (
                <div className="space-y-3">
                  {maintenance.downtimeByCategory.map((cat) => (
                    <div key={cat.category} className="space-y-1">
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-medium text-slate-300">
                          {cat.category.replace(/_/g, ' ')}
                        </span>
                        <span className="text-slate-400">
                          {cat.durationHours}h ({cat.percentageOfTotal}%) &bull; {cat.incidentCount} incident(s)
                        </span>
                      </div>
                      <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                        <div
                          className="bg-amber-500 h-full rounded-full"
                          style={{ width: `${Math.min(100, cat.percentageOfTotal)}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Equipment Downtime Impact */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
              <h3 className="text-base font-semibold text-white mb-3 flex items-center gap-2">
                <Wrench className="w-5 h-5 text-rose-400" />
                Equipment Downtime Impact
              </h3>
              {isLoading ? (
                <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                  Loading equipment downtime logs...
                </div>
              ) : !maintenance || maintenance.equipmentDowntime.length === 0 ? (
                <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                  All equipment operational with zero logged downtime.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                      <tr>
                        <th className="py-2 px-3">Equipment</th>
                        <th className="py-2 px-3">Incidents</th>
                        <th className="py-2 px-3 text-right">Downtime Duration</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/60">
                      {maintenance.equipmentDowntime.map((eq) => (
                        <tr key={eq.equipmentId} className="hover:bg-slate-800/40 transition">
                          <td className="py-2.5 px-3 font-medium text-white">{eq.equipmentName}</td>
                          <td className="py-2.5 px-3 text-slate-400">{eq.incidentCount}</td>
                          <td className="py-2.5 px-3 text-right font-semibold text-rose-400">{eq.durationHours}h</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* TAB 5: COST & SPENDING */}
      {activeTab === 'cost' && (
        <div className="space-y-6">
          {/* Department Cost Table */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-base font-semibold text-white mb-3 flex items-center gap-2">
              <Building2 className="w-5 h-5 text-sky-400" />
              Department Resource Spend
            </h3>
            {isLoading ? (
              <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                Aggregating department spending...
              </div>
            ) : !cost || cost.departmentCosts.length === 0 ? (
              <div className="h-32 flex items-center justify-center text-slate-500 text-sm">
                No departmental usage costs recorded for this window.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Department</th>
                      <th className="py-2.5 px-3">Bookings</th>
                      <th className="py-2.5 px-3">Total Cost</th>
                      <th className="py-2.5 px-3">Unbilled</th>
                      <th className="py-2.5 px-3">Invoiced</th>
                      <th className="py-2.5 px-3 text-right">Settled</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {cost.departmentCosts.map((d) => (
                      <tr key={d.departmentId} className="hover:bg-slate-800/40 transition">
                        <td className="py-2.5 px-3 font-semibold text-white">{d.departmentName}</td>
                        <td className="py-2.5 px-3 text-slate-300">{d.bookingCount}</td>
                        <td className="py-2.5 px-3 font-bold text-white">${Number(d.totalCost).toFixed(2)}</td>
                        <td className="py-2.5 px-3 text-amber-400">${Number(d.unbilledCost).toFixed(2)}</td>
                        <td className="py-2.5 px-3 text-sky-400">${Number(d.invoicedCost).toFixed(2)}</td>
                        <td className="py-2.5 px-3 text-right text-emerald-400 font-medium">${Number(d.settledCost).toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 6: EQUIPMENT RANKINGS */}
      {activeTab === 'performance' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Most Utilized */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-emerald-400 uppercase tracking-wider mb-3 flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" /> Top Utilized Instruments
            </h3>
            {!performance || performance.mostUtilized.length === 0 ? (
              <p className="text-xs text-slate-500 py-4">No data available.</p>
            ) : (
              <div className="space-y-2.5">
                {performance.mostUtilized.map((item, idx) => (
                  <div key={item.equipmentId} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-800/50 text-xs">
                    <div className="flex items-center gap-2.5">
                      <span className="w-5 h-5 flex items-center justify-center rounded-full bg-emerald-500/20 text-emerald-400 font-bold text-[10px]">
                        {idx + 1}
                      </span>
                      <span className="font-medium text-white">{item.equipmentName}</span>
                    </div>
                    <span className="font-bold text-emerald-400">{item.metricValue}%</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Highest Downtime */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-rose-400 uppercase tracking-wider mb-3 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4" /> Highest Downtime Instruments
            </h3>
            {!performance || performance.highestDowntime.length === 0 ? (
              <p className="text-xs text-slate-500 py-4">No downtime recorded.</p>
            ) : (
              <div className="space-y-2.5">
                {performance.highestDowntime.map((item, idx) => (
                  <div key={item.equipmentId} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-800/50 text-xs">
                    <div className="flex items-center gap-2.5">
                      <span className="w-5 h-5 flex items-center justify-center rounded-full bg-rose-500/20 text-rose-400 font-bold text-[10px]">
                        {idx + 1}
                      </span>
                      <span className="font-medium text-white">{item.equipmentName}</span>
                    </div>
                    <span className="font-bold text-rose-400">{item.metricValue} hours</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Highest Spend */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-purple-400 uppercase tracking-wider mb-3 flex items-center gap-2">
              <Receipt className="w-4 h-4" /> Highest Spend Equipment
            </h3>
            {!performance || performance.highestCost.length === 0 ? (
              <p className="text-xs text-slate-500 py-4">No cost records available.</p>
            ) : (
              <div className="space-y-2.5">
                {performance.highestCost.map((item, idx) => (
                  <div key={item.equipmentId} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-800/50 text-xs">
                    <div className="flex items-center gap-2.5">
                      <span className="w-5 h-5 flex items-center justify-center rounded-full bg-purple-500/20 text-purple-400 font-bold text-[10px]">
                        {idx + 1}
                      </span>
                      <span className="font-medium text-white">{item.equipmentName}</span>
                    </div>
                    <span className="font-bold text-purple-400">${Number(item.metricValue).toFixed(2)}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Highest Bookings */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
            <h3 className="text-sm font-semibold text-sky-400 uppercase tracking-wider mb-3 flex items-center gap-2">
              <CalendarDays className="w-4 h-4" /> Most Reserved Instruments
            </h3>
            {!performance || performance.highestBookingFrequency.length === 0 ? (
              <p className="text-xs text-slate-500 py-4">No bookings recorded.</p>
            ) : (
              <div className="space-y-2.5">
                {performance.highestBookingFrequency.map((item, idx) => (
                  <div key={item.equipmentId} className="flex items-center justify-between p-2.5 rounded-lg bg-slate-800/50 text-xs">
                    <div className="flex items-center gap-2.5">
                      <span className="w-5 h-5 flex items-center justify-center rounded-full bg-sky-500/20 text-sky-400 font-bold text-[10px]">
                        {idx + 1}
                      </span>
                      <span className="font-medium text-white">{item.equipmentName}</span>
                    </div>
                    <span className="font-bold text-sky-400">{item.metricValue} bookings</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
