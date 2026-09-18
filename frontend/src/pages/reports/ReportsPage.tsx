import React, { useState, useEffect, useCallback } from 'react';
import {
  FileText,
  Download,
  Calendar,
  Building2,
  RefreshCw,
  AlertCircle,
  Activity,
  Wrench,
  DollarSign,
  Package,
  Layers,
} from 'lucide-react';
import {
  getUtilizationReport,
  getBookingReport,
  getMaintenanceReport,
  getCostReport,
  getEquipmentReport,
  getManagementReport,
  downloadReportCsv,
} from '../../api/reports';
import { getDepartmentCostSummary } from '../../api/cost';
import { useAuth } from '../../context/useAuth';
import { getDepartmentsByInstitution } from '../../api/equipment';
import type {
  ReportMetadataResponse,
  UtilizationReportResponse,
  BookingReportResponse,
  MaintenanceReportResponse,
  CostReportResponse,
  EquipmentInventoryReportResponse,
  ManagementSummaryReportResponse,
} from '../../types/report';

type ReportType = 'utilization' | 'bookings' | 'maintenance' | 'cost' | 'equipment' | 'management';

export const ReportsPage: React.FC = () => {
  const { user } = useAuth();

  // Tab State
  const [activeTab, setActiveTab] = useState<ReportType>('utilization');

  // Filter State
  const [startDate, setStartDate] = useState<string>(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState<string>(() => new Date().toISOString().split('T')[0]);
  const [departmentId, setDepartmentId] = useState<string>('');

  // Department dropdown options
  const [departments, setDepartments] = useState<{ id: number; name: string }[]>([]);

  // Report Data States
  const [utilizationData, setUtilizationData] = useState<UtilizationReportResponse | null>(null);
  const [bookingData, setBookingData] = useState<BookingReportResponse | null>(null);
  const [maintenanceData, setMaintenanceData] = useState<MaintenanceReportResponse | null>(null);
  const [costData, setCostData] = useState<CostReportResponse | null>(null);
  const [equipmentData, setEquipmentData] = useState<EquipmentInventoryReportResponse | null>(null);
  const [managementData, setManagementData] = useState<ManagementSummaryReportResponse | null>(null);

  // Loading & Error States
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isExporting, setIsExporting] = useState<boolean>(false);
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
        // Non-fatal if department list fails
      }
    };
    loadDepts();
  }, [user?.institutionId]);

  // Fetch Report based on active tab
  const fetchReport = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    const params = {
      startDate: startDate || undefined,
      endDate: endDate || undefined,
      departmentId: departmentId ? Number(departmentId) : undefined,
    };

    try {
      if (activeTab === 'utilization') {
        const data = await getUtilizationReport(params);
        setUtilizationData(data);
      } else if (activeTab === 'bookings') {
        const data = await getBookingReport(params);
        setBookingData(data);
      } else if (activeTab === 'maintenance') {
        const data = await getMaintenanceReport(params);
        setMaintenanceData(data);
      } else if (activeTab === 'cost') {
        const data = await getCostReport(params);
        setCostData(data);
      } else if (activeTab === 'equipment') {
        const data = await getEquipmentReport(params);
        setEquipmentData(data);
      } else if (activeTab === 'management') {
        const data = await getManagementReport(params);
        setManagementData(data);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to generate report';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [activeTab, startDate, endDate, departmentId]);

  useEffect(() => {
    fetchReport();
  }, [fetchReport]);

  // Handle Quick Date Presets
  const applyPreset = (days: number) => {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - days);
    setEndDate(end.toISOString().split('T')[0]);
    setStartDate(start.toISOString().split('T')[0]);
  };

  const applyThisMonth = () => {
    const now = new Date();
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
    setStartDate(firstDay.toISOString().split('T')[0]);
    setEndDate(now.toISOString().split('T')[0]);
  };

  // CSV Export Handler
  const handleExportCsv = async () => {
    setIsExporting(true);
    try {
      const params = {
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        departmentId: departmentId ? Number(departmentId) : undefined,
      };
      await downloadReportCsv(activeTab, params);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to export CSV';
      alert(`Export error: ${msg}`);
    } finally {
      setIsExporting(false);
    }
  };

  // Current active metadata
  const currentMetadata: ReportMetadataResponse | undefined =
    activeTab === 'utilization'
      ? utilizationData?.metadata
      : activeTab === 'bookings'
      ? bookingData?.metadata
      : activeTab === 'maintenance'
      ? maintenanceData?.metadata
      : activeTab === 'cost'
      ? costData?.metadata
      : activeTab === 'equipment'
      ? equipmentData?.metadata
      : managementData?.metadata;

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <FileText className="w-7 h-7 text-indigo-400" />
            Reports &amp; Compliance
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Formal presentation and exportable records generated from real operational and domain data.
          </p>
        </div>

        {/* Export Actions */}
        <div className="flex items-center gap-2.5">
          <button
            onClick={handleExportCsv}
            disabled={isExporting || isLoading}
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-xl text-xs font-semibold shadow-lg shadow-indigo-600/20 transition-all cursor-pointer"
          >
            <Download className="w-4 h-4" />
            {isExporting ? 'Exporting...' : 'Export CSV'}
          </button>
          <div className="relative group">
            <button
              disabled
              className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800/70 text-slate-500 border border-slate-700/60 rounded-xl text-xs font-semibold cursor-not-allowed"
              title="PDF export deferred per architectural specifications. Use CSV export."
            >
              <Download className="w-4 h-4" />
              Export PDF
            </button>
            <div className="absolute right-0 bottom-full mb-2 hidden group-hover:block w-64 p-2.5 bg-slate-800 border border-slate-700 text-slate-200 text-xs rounded-xl shadow-xl z-50">
              PDF export deferred per architecture; use CSV export.
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 overflow-x-auto">
        {[
          { id: 'utilization', label: 'Utilization', icon: Activity },
          { id: 'bookings', label: 'Bookings', icon: Calendar },
          { id: 'maintenance', label: 'Maintenance', icon: Wrench },
          { id: 'cost', label: 'Cost & Billing', icon: DollarSign },
          { id: 'equipment', label: 'Equipment Catalog', icon: Package },
          { id: 'management', label: 'Management Summary', icon: Layers },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as ReportType)}
              className={`flex items-center gap-2 px-4 py-3 text-xs font-semibold border-b-2 whitespace-nowrap transition-all ${
                isActive
                  ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5 font-semibold'
                  : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
              }`}
            >
              <Icon className={`w-4 h-4 ${isActive ? 'text-indigo-400' : 'text-slate-500'}`} />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800/80 shadow-sm space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          {/* Quick Date Presets */}
          <div className="flex items-center gap-1 bg-slate-800/90 p-1 rounded-xl border border-slate-700/60">
            <button
              onClick={() => applyPreset(7)}
              className="px-2.5 py-1 text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-700/80 rounded-lg transition-colors"
            >
              Last 7 Days
            </button>
            <button
              onClick={() => applyPreset(30)}
              className="px-2.5 py-1 text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-700/80 rounded-lg transition-colors"
            >
              Last 30 Days
            </button>
            <button
              onClick={() => applyPreset(90)}
              className="px-2.5 py-1 text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-700/80 rounded-lg transition-colors"
            >
              Last 90 Days
            </button>
            <button
              onClick={applyThisMonth}
              className="px-2.5 py-1 text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-700/80 rounded-lg transition-colors"
            >
              This Month
            </button>
          </div>

          {/* Refresh Action */}
          <button
            onClick={fetchReport}
            disabled={isLoading}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-300 bg-slate-850 hover:bg-slate-800 hover:text-white border border-slate-700/80 rounded-xl transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            Generate / Refresh
          </button>
        </div>

        {/* Custom Inputs */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2 border-t border-slate-800/80">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Start Date</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-3 py-1.5 text-xs bg-slate-800/80 border border-slate-700/80 rounded-xl text-white [color-scheme:dark] focus:ring-2 focus:ring-indigo-500/50 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-1.5 text-xs bg-slate-800/80 border border-slate-700/80 rounded-xl text-white [color-scheme:dark] focus:ring-2 focus:ring-indigo-500/50 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Department Filter</label>
            <div className="relative">
              <Building2 className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
              <select
                value={departmentId}
                onChange={(e) => setDepartmentId(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-800/80 border border-slate-700/80 rounded-xl text-white focus:ring-2 focus:ring-indigo-500/50 focus:outline-none appearance-none cursor-pointer"
              >
                <option value="" className="bg-slate-900 text-white">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id} className="bg-slate-900 text-white">
                    {d.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-rose-400 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-sm font-semibold text-rose-200">Report Generation Error</p>
            <p className="text-xs text-rose-300 mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Report Metadata Banner */}
      {currentMetadata && (
        <div className="bg-slate-850/60 border border-slate-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-300">
          <div className="space-y-1">
            <span className="font-semibold text-sm text-indigo-400">{currentMetadata.title}</span>
            <p className="text-slate-400">
              Period: <span className="font-medium text-slate-200">{currentMetadata.startDate}</span> to{' '}
              <span className="font-medium text-slate-200">{currentMetadata.endDate}</span>
              {currentMetadata.departmentId && ` • Department ID: ${currentMetadata.departmentId}`}
            </p>
          </div>
          <div className="flex items-center gap-6 text-right">
            <div>
              <span className="text-slate-400 block text-[11px]">Total Records</span>
              <span className="font-bold text-white text-sm font-mono">{currentMetadata.recordCount}</span>
            </div>
            <div>
              <span className="text-slate-400 block text-[11px]">Generated At</span>
              <span className="font-medium text-slate-300">
                {new Date(currentMetadata.generatedAt).toLocaleString()}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="space-y-4 py-8">
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((n) => (
              <div key={n} className="h-24 bg-slate-850/60 border border-slate-800/80 rounded-2xl animate-pulse" />
            ))}
          </div>
          <div className="h-64 bg-slate-850/60 border border-slate-800/80 rounded-2xl animate-pulse" />
        </div>
      )}

      {/* Report Content Panels */}
      {!isLoading && !error && (
        <>
          {/* TAB 1: UTILIZATION */}
          {activeTab === 'utilization' && utilizationData && (
            <div className="space-y-6">
              {/* Summary KPIs */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Overall Utilization</span>
                  <p className="text-2xl font-bold text-indigo-400 font-mono mt-1">
                    {utilizationData.summary.overallUtilizationPercentage}%
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Actual Usage</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">
                    {utilizationData.summary.totalUsageHours} <span className="text-xs font-normal text-slate-400 font-sans">hrs</span>
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Operating Window</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">
                    {utilizationData.summary.totalOperatingHours} <span className="text-xs font-normal text-slate-400 font-sans">hrs</span>
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Completed Sessions</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">
                    {utilizationData.summary.totalSessions}
                  </p>
                </div>
              </div>

              {/* Data Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Equipment Utilization Breakdown</h3>
                </div>
                {utilizationData.rows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No report data available for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4">Department</th>
                          <th className="py-3 px-4 text-right">Usage (Hours)</th>
                          <th className="py-3 px-4 text-right">Operating (Hours)</th>
                          <th className="py-3 px-4 text-right">Utilization (%)</th>
                          <th className="py-3 px-4 text-right">Sessions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {utilizationData.rows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-slate-400">{row.departmentName || 'N/A'}</td>
                            <td className="py-3 px-4 text-right font-mono">{row.actualUsageHours}</td>
                            <td className="py-3 px-4 text-right font-mono">{row.operatingHours}</td>
                            <td className="py-3 px-4 text-right font-semibold text-indigo-400 font-mono">
                              {row.utilizationPercentage}%
                            </td>
                            <td className="py-3 px-4 text-right font-mono">{row.sessionCount}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 2: BOOKINGS */}
          {activeTab === 'bookings' && bookingData && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Bookings</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">{bookingData.summary.totalBookings}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Completed</span>
                  <p className="text-2xl font-bold text-emerald-400 font-mono mt-1">{bookingData.summary.completedBookings}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Cancelled / No-Show</span>
                  <p className="text-2xl font-bold text-rose-400 font-mono mt-1">
                    {bookingData.summary.cancelledBookings + bookingData.summary.noShowBookings}
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Booked Hours</span>
                  <p className="text-2xl font-bold text-indigo-400 font-mono mt-1">
                    {bookingData.summary.totalBookedHours} <span className="text-xs font-normal text-slate-400 font-sans">hrs</span>
                  </p>
                </div>
              </div>

              {/* Status Distribution */}
              <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-sm">
                <h3 className="text-sm font-semibold text-white mb-3">Status Distribution</h3>
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
                  {Object.entries(bookingData.statusDistribution).map(([status, count]) => (
                    <div key={status} className="p-3 bg-slate-850/80 border border-slate-800/80 rounded-xl text-center">
                      <span className="text-xs font-medium text-slate-400">{status}</span>
                      <p className="text-lg font-bold text-white font-mono mt-0.5">{count}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Equipment Breakdown Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Equipment Booking Frequency</h3>
                </div>
                {bookingData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No report data available for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Equipment ID</th>
                          <th className="py-3 px-4">Equipment Name</th>
                          <th className="py-3 px-4 text-right">Booking Count</th>
                          <th className="py-3 px-4 text-right">Booked Hours</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {bookingData.equipmentRows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-mono text-xs text-indigo-400">#{row.equipmentId}</td>
                            <td className="py-3 px-4 font-medium text-white">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-right font-semibold text-white font-mono">{row.bookingCount}</td>
                            <td className="py-3 px-4 text-right text-slate-300 font-mono">{row.bookedHours} hrs</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 3: MAINTENANCE */}
          {activeTab === 'maintenance' && maintenanceData && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Maintenance Requests</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">{maintenanceData.summary.totalRequests}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Work Orders</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">{maintenanceData.summary.totalWorkOrders}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Downtime</span>
                  <p className="text-2xl font-bold text-amber-400 font-mono mt-1">
                    {maintenanceData.summary.totalDowntimeHours} <span className="text-xs font-normal text-slate-400 font-sans">hrs</span>
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Downtime Incidents</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">{maintenanceData.summary.downtimeIncidentCount}</p>
                </div>
              </div>

              {/* Downtime by Category */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Downtime by Reason Category</h3>
                </div>
                {maintenanceData.categoryRows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No downtime logged for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Category</th>
                          <th className="py-3 px-4 text-right">Incidents</th>
                          <th className="py-3 px-4 text-right">Duration (Hours)</th>
                          <th className="py-3 px-4 text-right">% of Total</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {maintenanceData.categoryRows.map((cat) => (
                          <tr key={cat.category} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{cat.category}</td>
                            <td className="py-3 px-4 text-right font-mono">{cat.incidentCount}</td>
                            <td className="py-3 px-4 text-right font-semibold text-amber-400 font-mono">{cat.durationHours}</td>
                            <td className="py-3 px-4 text-right text-slate-400 font-mono">{cat.percentageOfTotal}%</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              {/* Equipment Downtime Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Equipment Downtime Details</h3>
                </div>
                {maintenanceData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No equipment downtime records for this period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4 text-right">Incident Count</th>
                          <th className="py-3 px-4 text-right">Downtime (Hours)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {maintenanceData.equipmentRows.map((eq) => (
                          <tr key={eq.equipmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{eq.equipmentName}</td>
                            <td className="py-3 px-4 text-right font-mono">{eq.incidentCount}</td>
                            <td className="py-3 px-4 text-right font-semibold text-amber-400 font-mono">{eq.durationHours} hrs</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 4: COST & BILLING */}
          {activeTab === 'cost' && costData && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Expenditure</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">${costData.summary.totalCost.toFixed(2)}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Unbilled Costs</span>
                  <p className="text-2xl font-bold text-amber-400 font-mono mt-1">${costData.summary.unbilledCost.toFixed(2)}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Invoiced Costs</span>
                  <p className="text-2xl font-bold text-blue-400 font-mono mt-1">${costData.summary.invoicedCost.toFixed(2)}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Settled Costs</span>
                  <p className="text-2xl font-bold text-emerald-400 font-mono mt-1">${costData.summary.settledCost.toFixed(2)}</p>
                </div>
              </div>

              {/* Department Spending Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Department Cost Breakdown</h3>
                </div>
                {costData.departmentRows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No department cost records available.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Department</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                          <th className="py-3 px-4 text-right">Unbilled ($)</th>
                          <th className="py-3 px-4 text-right">Invoiced ($)</th>
                          <th className="py-3 px-4 text-right">Settled ($)</th>
                          <th className="py-3 px-4 text-right">Total ($)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {costData.departmentRows.map((d) => (
                          <tr key={d.departmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{d.departmentName}</td>
                            <td className="py-3 px-4 text-right font-mono">{d.bookingCount}</td>
                            <td className="py-3 px-4 text-right text-slate-400 font-mono">${d.unbilledCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right text-slate-400 font-mono">${d.invoicedCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right text-emerald-400 font-medium font-mono">${d.settledCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right font-bold text-white font-mono">${d.totalCost.toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              {/* Equipment Cost Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Equipment Usage Cost Breakdown</h3>
                </div>
                {costData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No equipment usage costs recorded.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4 text-right">Billable Hours</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                          <th className="py-3 px-4 text-right">Total Cost ($)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {costData.equipmentRows.map((eq) => (
                          <tr key={eq.equipmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{eq.equipmentName}</td>
                            <td className="py-3 px-4 text-right font-mono">{eq.billableHours} hrs</td>
                            <td className="py-3 px-4 text-right font-mono">{eq.bookingCount}</td>
                            <td className="py-3 px-4 text-right font-bold text-indigo-400 font-mono">${eq.totalCost.toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 5: EQUIPMENT INVENTORY */}
          {activeTab === 'equipment' && equipmentData && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Equipment</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">{equipmentData.summary.totalEquipment}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Operational</span>
                  <p className="text-2xl font-bold text-emerald-400 font-mono mt-1">{equipmentData.summary.operationalCount}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Under Maintenance</span>
                  <p className="text-2xl font-bold text-amber-400 font-mono mt-1">{equipmentData.summary.underMaintenanceCount}</p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Out of Service / Retired</span>
                  <p className="text-2xl font-bold text-rose-400 font-mono mt-1">{equipmentData.summary.decommissionedCount}</p>
                </div>
              </div>

              {/* Inventory Table */}
              <div className="bg-slate-900 rounded-2xl border border-slate-800 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-850/40">
                  <h3 className="text-sm font-semibold text-white">Equipment Inventory &amp; Performance Catalog</h3>
                </div>
                {equipmentData.rows.length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">
                    No equipment found for the selected department or institution.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-850/80 text-slate-400 font-semibold uppercase tracking-wider border-b border-slate-800">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4">Model &amp; Serial</th>
                          <th className="py-3 px-4">Department / Category</th>
                          <th className="py-3 px-4">Status</th>
                          <th className="py-3 px-4 text-right">Utilization (%)</th>
                          <th className="py-3 px-4 text-right">Downtime</th>
                          <th className="py-3 px-4 text-right">Expenditure</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {equipmentData.rows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-slate-800/40 transition-colors">
                            <td className="py-3 px-4 font-medium text-white">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-xs text-slate-500 font-mono">
                              {row.model || 'N/A'} / {row.serialNumber || 'N/A'}
                            </td>
                            <td className="py-3 px-4 text-slate-400">
                              {row.departmentName} • <span className="text-xs text-slate-500">{row.categoryName}</span>
                            </td>
                            <td className="py-3 px-4">
                              <span
                                className={`inline-block px-2.5 py-0.5 text-[11px] font-semibold rounded-lg ${
                                  row.status === 'AVAILABLE' || row.status === 'IN_USE'
                                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                                    : row.status === 'UNDER_MAINTENANCE'
                                    ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                                    : 'bg-slate-800 text-slate-400 border border-slate-700'
                                }`}
                              >
                                {row.status}
                              </span>
                            </td>
                            <td className="py-3 px-4 text-right font-medium text-indigo-400 font-mono">
                              {row.utilizationPercentage}%
                            </td>
                            <td className="py-3 px-4 text-right text-amber-400 font-mono">{row.downtimeHours} hrs</td>
                            <td className="py-3 px-4 text-right font-semibold text-emerald-400 font-mono">${row.totalCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right text-slate-300 font-mono">{row.bookingCount}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 6: MANAGEMENT SUMMARY */}
          {activeTab === 'management' && managementData && (
            <div className="space-y-6">
              {/* Executive KPIs */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Average Utilization</span>
                  <p className="text-2xl font-bold text-indigo-400 font-mono mt-1">
                    {managementData.kpis.averageUtilizationPercentage}%
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Completed Bookings</span>
                  <p className="text-2xl font-bold text-white font-mono mt-1">
                    {managementData.kpis.completedBookings} / {managementData.kpis.totalBookings}
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Downtime</span>
                  <p className="text-2xl font-bold text-amber-400 font-mono mt-1">
                    {managementData.kpis.totalDowntimeHours} <span className="text-xs font-normal text-slate-400 font-sans">hrs</span>
                  </p>
                </div>
                <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl shadow-sm">
                  <span className="text-xs font-medium text-slate-400">Total Expenditure</span>
                  <p className="text-2xl font-bold text-emerald-400 font-mono mt-1">
                    ${managementData.kpis.totalCost.toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Performance Rankings */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Top Utilized */}
                <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-white flex items-center gap-2">
                    <Activity className="w-4 h-4 text-indigo-400" />
                    Top Utilized Equipment
                  </h4>
                  {managementData.topUtilizedEquipment.length === 0 ? (
                    <p className="text-xs text-slate-400">No usage recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.topUtilizedEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2.5 bg-slate-850/80 border border-slate-800/80 rounded-xl">
                          <span className="font-medium text-slate-200 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-indigo-400 font-mono">{item.metricValue}%</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Highest Downtime */}
                <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-white flex items-center gap-2">
                    <Wrench className="w-4 h-4 text-amber-400" />
                    Highest Downtime
                  </h4>
                  {managementData.highestDowntimeEquipment.length === 0 ? (
                    <p className="text-xs text-slate-400">No downtime recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.highestDowntimeEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2.5 bg-slate-850/80 border border-slate-800/80 rounded-xl">
                          <span className="font-medium text-slate-200 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-amber-400 font-mono">{item.metricValue} hrs</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Highest Cost */}
                <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-white flex items-center gap-2">
                    <DollarSign className="w-4 h-4 text-emerald-400" />
                    Highest Expenditure
                  </h4>
                  {managementData.highestCostEquipment.length === 0 ? (
                    <p className="text-xs text-slate-400">No costs recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.highestCostEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2.5 bg-slate-850/80 border border-slate-800/80 rounded-xl">
                          <span className="font-medium text-slate-200 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-emerald-400 font-mono">${item.metricValue.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};
