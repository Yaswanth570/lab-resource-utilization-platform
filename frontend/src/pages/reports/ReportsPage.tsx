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
  }, []);

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
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight flex items-center gap-2">
            <FileText className="w-7 h-7 text-indigo-600" />
            Reports & Compliance
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Formal presentation and exportable records generated from real operational and domain data.
          </p>
        </div>

        {/* Export Actions */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCsv}
            disabled={isExporting || isLoading}
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-lg text-sm font-medium shadow-sm transition-colors"
          >
            <Download className="w-4 h-4" />
            {isExporting ? 'Exporting...' : 'Export CSV'}
          </button>
          <div className="relative group">
            <button
              disabled
              className="inline-flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-400 border border-gray-200 rounded-lg text-sm font-medium cursor-not-allowed"
              title="PDF export deferred per architectural specifications. Use CSV export."
            >
              <Download className="w-4 h-4" />
              Export PDF
            </button>
            <div className="absolute right-0 bottom-full mb-2 hidden group-hover:block w-60 p-2 bg-gray-900 text-white text-xs rounded shadow-lg z-50">
              PDF export deferred per architecture; use CSV export.
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="flex space-x-6 overflow-x-auto pb-px">
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
                className={`flex items-center gap-2 pb-3 text-sm font-medium border-b-2 whitespace-nowrap transition-colors ${
                  isActive
                    ? 'border-indigo-600 text-indigo-600 font-semibold'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-indigo-600' : 'text-gray-400'}`} />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-4">
          {/* Quick Date Presets */}
          <div className="flex items-center gap-1 bg-gray-50 p-1 rounded-lg border border-gray-200">
            <button
              onClick={() => applyPreset(7)}
              className="px-2.5 py-1 text-xs font-medium text-gray-600 hover:text-gray-900 hover:bg-white rounded transition-colors"
            >
              Last 7 Days
            </button>
            <button
              onClick={() => applyPreset(30)}
              className="px-2.5 py-1 text-xs font-medium text-gray-600 hover:text-gray-900 hover:bg-white rounded transition-colors"
            >
              Last 30 Days
            </button>
            <button
              onClick={() => applyPreset(90)}
              className="px-2.5 py-1 text-xs font-medium text-gray-600 hover:text-gray-900 hover:bg-white rounded transition-colors"
            >
              Last 90 Days
            </button>
            <button
              onClick={applyThisMonth}
              className="px-2.5 py-1 text-xs font-medium text-gray-600 hover:text-gray-900 hover:bg-white rounded transition-colors"
            >
              This Month
            </button>
          </div>

          {/* Refresh Action */}
          <button
            onClick={fetchReport}
            disabled={isLoading}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            Generate / Refresh
          </button>
        </div>

        {/* Custom Inputs */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2 border-t border-gray-100">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Start Date</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-3 py-1.5 text-sm bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-1.5 text-sm bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Department Filter</label>
            <div className="relative">
              <Building2 className="w-4 h-4 text-gray-400 absolute left-3 top-2.5 pointer-events-none" />
              <select
                value={departmentId}
                onChange={(e) => setDepartmentId(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-sm bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none appearance-none"
              >
                <option value="">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
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
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-sm font-semibold text-red-900">Report Generation Error</p>
            <p className="text-xs text-red-700 mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Report Metadata Banner */}
      {currentMetadata && (
        <div className="bg-indigo-50/70 border border-indigo-100 rounded-xl p-4 flex flex-wrap items-center justify-between gap-4 text-xs text-indigo-950">
          <div className="space-y-1">
            <span className="font-semibold text-sm text-indigo-900">{currentMetadata.title}</span>
            <p className="text-indigo-700">
              Period: <span className="font-medium">{currentMetadata.startDate}</span> to{' '}
              <span className="font-medium">{currentMetadata.endDate}</span>
              {currentMetadata.departmentId && ` • Department ID: ${currentMetadata.departmentId}`}
            </p>
          </div>
          <div className="flex items-center gap-4 text-right">
            <div>
              <span className="text-gray-500 block">Total Records</span>
              <span className="font-bold text-gray-900 text-sm">{currentMetadata.recordCount}</span>
            </div>
            <div>
              <span className="text-gray-500 block">Generated At</span>
              <span className="font-medium text-gray-700">
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
              <div key={n} className="h-24 bg-gray-100 rounded-xl animate-pulse" />
            ))}
          </div>
          <div className="h-64 bg-gray-100 rounded-xl animate-pulse" />
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Overall Utilization</span>
                  <p className="text-2xl font-bold text-indigo-600 mt-1">
                    {utilizationData.summary.overallUtilizationPercentage}%
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Actual Usage</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">
                    {utilizationData.summary.totalUsageHours} <span className="text-xs font-normal text-gray-500">hrs</span>
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Operating Window</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">
                    {utilizationData.summary.totalOperatingHours} <span className="text-xs font-normal text-gray-500">hrs</span>
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Completed Sessions</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">
                    {utilizationData.summary.totalSessions}
                  </p>
                </div>
              </div>

              {/* Data Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Equipment Utilization Breakdown</h3>
                </div>
                {utilizationData.rows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No report data available for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4">Department</th>
                          <th className="py-3 px-4 text-right">Usage (Hours)</th>
                          <th className="py-3 px-4 text-right">Operating (Hours)</th>
                          <th className="py-3 px-4 text-right">Utilization (%)</th>
                          <th className="py-3 px-4 text-right">Sessions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {utilizationData.rows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-gray-600">{row.departmentName || 'N/A'}</td>
                            <td className="py-3 px-4 text-right">{row.actualUsageHours}</td>
                            <td className="py-3 px-4 text-right">{row.operatingHours}</td>
                            <td className="py-3 px-4 text-right font-semibold text-indigo-600">
                              {row.utilizationPercentage}%
                            </td>
                            <td className="py-3 px-4 text-right">{row.sessionCount}</td>
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Bookings</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{bookingData.summary.totalBookings}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Completed</span>
                  <p className="text-2xl font-bold text-emerald-600 mt-1">{bookingData.summary.completedBookings}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Cancelled / No-Show</span>
                  <p className="text-2xl font-bold text-rose-600 mt-1">
                    {bookingData.summary.cancelledBookings + bookingData.summary.noShowBookings}
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Booked Hours</span>
                  <p className="text-2xl font-bold text-indigo-600 mt-1">
                    {bookingData.summary.totalBookedHours} <span className="text-xs font-normal text-gray-500">hrs</span>
                  </p>
                </div>
              </div>

              {/* Status Distribution */}
              <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm">
                <h3 className="text-sm font-semibold text-gray-800 mb-3">Status Distribution</h3>
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
                  {Object.entries(bookingData.statusDistribution).map(([status, count]) => (
                    <div key={status} className="p-3 bg-gray-50 rounded-lg text-center">
                      <span className="text-xs font-medium text-gray-500">{status}</span>
                      <p className="text-lg font-bold text-gray-900 mt-0.5">{count}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Equipment Breakdown Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Equipment Booking Frequency</h3>
                </div>
                {bookingData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No report data available for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Equipment ID</th>
                          <th className="py-3 px-4">Equipment Name</th>
                          <th className="py-3 px-4 text-right">Booking Count</th>
                          <th className="py-3 px-4 text-right">Booked Hours</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {bookingData.equipmentRows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-mono text-xs text-gray-500">#{row.equipmentId}</td>
                            <td className="py-3 px-4 font-medium text-gray-900">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-right font-semibold text-gray-900">{row.bookingCount}</td>
                            <td className="py-3 px-4 text-right">{row.bookedHours} hrs</td>
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Maintenance Requests</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{maintenanceData.summary.totalRequests}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Work Orders</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{maintenanceData.summary.totalWorkOrders}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Downtime</span>
                  <p className="text-2xl font-bold text-amber-600 mt-1">
                    {maintenanceData.summary.totalDowntimeHours} <span className="text-xs font-normal text-gray-500">hrs</span>
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Downtime Incidents</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{maintenanceData.summary.downtimeIncidentCount}</p>
                </div>
              </div>

              {/* Downtime by Category */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Downtime by Reason Category</h3>
                </div>
                {maintenanceData.categoryRows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No downtime logged for the selected period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Category</th>
                          <th className="py-3 px-4 text-right">Incidents</th>
                          <th className="py-3 px-4 text-right">Duration (Hours)</th>
                          <th className="py-3 px-4 text-right">% of Total</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {maintenanceData.categoryRows.map((cat) => (
                          <tr key={cat.category} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{cat.category}</td>
                            <td className="py-3 px-4 text-right">{cat.incidentCount}</td>
                            <td className="py-3 px-4 text-right font-semibold">{cat.durationHours}</td>
                            <td className="py-3 px-4 text-right text-gray-600">{cat.percentageOfTotal}%</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              {/* Equipment Downtime Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Equipment Downtime Details</h3>
                </div>
                {maintenanceData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No equipment downtime records for this period.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4 text-right">Incident Count</th>
                          <th className="py-3 px-4 text-right">Downtime (Hours)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {maintenanceData.equipmentRows.map((eq) => (
                          <tr key={eq.equipmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{eq.equipmentName}</td>
                            <td className="py-3 px-4 text-right">{eq.incidentCount}</td>
                            <td className="py-3 px-4 text-right font-semibold text-amber-600">{eq.durationHours} hrs</td>
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Expenditure</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">${costData.summary.totalCost.toFixed(2)}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Unbilled Costs</span>
                  <p className="text-2xl font-bold text-amber-600 mt-1">${costData.summary.unbilledCost.toFixed(2)}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Invoiced Costs</span>
                  <p className="text-2xl font-bold text-blue-600 mt-1">${costData.summary.invoicedCost.toFixed(2)}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Settled Costs</span>
                  <p className="text-2xl font-bold text-emerald-600 mt-1">${costData.summary.settledCost.toFixed(2)}</p>
                </div>
              </div>

              {/* Department Spending Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Department Cost Breakdown</h3>
                </div>
                {costData.departmentRows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No department cost records available.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Department</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                          <th className="py-3 px-4 text-right">Unbilled ($)</th>
                          <th className="py-3 px-4 text-right">Invoiced ($)</th>
                          <th className="py-3 px-4 text-right">Settled ($)</th>
                          <th className="py-3 px-4 text-right">Total ($)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {costData.departmentRows.map((d) => (
                          <tr key={d.departmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{d.departmentName}</td>
                            <td className="py-3 px-4 text-right">{d.bookingCount}</td>
                            <td className="py-3 px-4 text-right text-gray-600">${d.unbilledCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right text-gray-600">${d.invoicedCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right text-emerald-600 font-medium">${d.settledCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right font-bold text-gray-900">${d.totalCost.toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              {/* Equipment Cost Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Equipment Usage Cost Breakdown</h3>
                </div>
                {costData.equipmentRows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No equipment usage costs recorded.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4 text-right">Billable Hours</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                          <th className="py-3 px-4 text-right">Total Cost ($)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {costData.equipmentRows.map((eq) => (
                          <tr key={eq.equipmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{eq.equipmentName}</td>
                            <td className="py-3 px-4 text-right">{eq.billableHours} hrs</td>
                            <td className="py-3 px-4 text-right">{eq.bookingCount}</td>
                            <td className="py-3 px-4 text-right font-bold text-indigo-600">${eq.totalCost.toFixed(2)}</td>
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Equipment</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{equipmentData.summary.totalEquipment}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Operational</span>
                  <p className="text-2xl font-bold text-emerald-600 mt-1">{equipmentData.summary.operationalCount}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Under Maintenance</span>
                  <p className="text-2xl font-bold text-amber-600 mt-1">{equipmentData.summary.underMaintenanceCount}</p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Out of Service / Retired</span>
                  <p className="text-2xl font-bold text-rose-600 mt-1">{equipmentData.summary.decommissionedCount}</p>
                </div>
              </div>

              {/* Inventory Table */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                  <h3 className="text-base font-semibold text-gray-900">Equipment Inventory & Performance Catalog</h3>
                </div>
                {equipmentData.rows.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-sm">
                    No equipment found for the selected department or institution.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-gray-50 text-xs font-semibold text-gray-500 border-b border-gray-200">
                          <th className="py-3 px-4">Equipment</th>
                          <th className="py-3 px-4">Model & Serial</th>
                          <th className="py-3 px-4">Department / Category</th>
                          <th className="py-3 px-4">Status</th>
                          <th className="py-3 px-4 text-right">Utilization (%)</th>
                          <th className="py-3 px-4 text-right">Downtime</th>
                          <th className="py-3 px-4 text-right">Expenditure</th>
                          <th className="py-3 px-4 text-right">Bookings</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
                        {equipmentData.rows.map((row) => (
                          <tr key={row.equipmentId} className="hover:bg-gray-50 transition-colors">
                            <td className="py-3 px-4 font-medium text-gray-900">{row.equipmentName}</td>
                            <td className="py-3 px-4 text-xs text-gray-500 font-mono">
                              {row.model || 'N/A'} / {row.serialNumber || 'N/A'}
                            </td>
                            <td className="py-3 px-4 text-gray-600">
                              {row.departmentName} • <span className="text-xs text-gray-400">{row.categoryName}</span>
                            </td>
                            <td className="py-3 px-4">
                              <span
                                className={`inline-block px-2 py-0.5 text-xs font-semibold rounded ${
                                  row.status === 'AVAILABLE' || row.status === 'IN_USE'
                                    ? 'bg-emerald-100 text-emerald-800'
                                    : row.status === 'UNDER_MAINTENANCE'
                                    ? 'bg-amber-100 text-amber-800'
                                    : 'bg-gray-100 text-gray-700'
                                }`}
                              >
                                {row.status}
                              </span>
                            </td>
                            <td className="py-3 px-4 text-right font-medium text-indigo-600">
                              {row.utilizationPercentage}%
                            </td>
                            <td className="py-3 px-4 text-right text-amber-700">{row.downtimeHours} hrs</td>
                            <td className="py-3 px-4 text-right font-semibold">${row.totalCost.toFixed(2)}</td>
                            <td className="py-3 px-4 text-right">{row.bookingCount}</td>
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
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Average Utilization</span>
                  <p className="text-2xl font-bold text-indigo-600 mt-1">
                    {managementData.kpis.averageUtilizationPercentage}%
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Completed Bookings</span>
                  <p className="text-2xl font-bold text-gray-900 mt-1">
                    {managementData.kpis.completedBookings} / {managementData.kpis.totalBookings}
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Downtime</span>
                  <p className="text-2xl font-bold text-amber-600 mt-1">
                    {managementData.kpis.totalDowntimeHours} <span className="text-xs font-normal text-gray-500">hrs</span>
                  </p>
                </div>
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                  <span className="text-xs font-medium text-gray-500">Total Expenditure</span>
                  <p className="text-2xl font-bold text-emerald-600 mt-1">
                    ${managementData.kpis.totalCost.toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Performance Rankings */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Top Utilized */}
                <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-gray-900 flex items-center gap-2">
                    <Activity className="w-4 h-4 text-indigo-600" />
                    Top Utilized Equipment
                  </h4>
                  {managementData.topUtilizedEquipment.length === 0 ? (
                    <p className="text-xs text-gray-500">No usage recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.topUtilizedEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2 bg-gray-50 rounded">
                          <span className="font-medium text-gray-800 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-indigo-600">{item.metricValue}%</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Highest Downtime */}
                <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-gray-900 flex items-center gap-2">
                    <Wrench className="w-4 h-4 text-amber-600" />
                    Highest Downtime
                  </h4>
                  {managementData.highestDowntimeEquipment.length === 0 ? (
                    <p className="text-xs text-gray-500">No downtime recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.highestDowntimeEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2 bg-gray-50 rounded">
                          <span className="font-medium text-gray-800 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-amber-700">{item.metricValue} hrs</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Highest Cost */}
                <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm space-y-3">
                  <h4 className="text-sm font-semibold text-gray-900 flex items-center gap-2">
                    <DollarSign className="w-4 h-4 text-emerald-600" />
                    Highest Expenditure
                  </h4>
                  {managementData.highestCostEquipment.length === 0 ? (
                    <p className="text-xs text-gray-500">No costs recorded.</p>
                  ) : (
                    <div className="space-y-2">
                      {managementData.highestCostEquipment.slice(0, 5).map((item) => (
                        <div key={item.equipmentId} className="flex justify-between items-center text-xs p-2 bg-gray-50 rounded">
                          <span className="font-medium text-gray-800 truncate pr-2">{item.equipmentName}</span>
                          <span className="font-bold text-emerald-600">${item.metricValue.toFixed(2)}</span>
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
