import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Wrench,
  Hammer,
  AlertOctagon,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Plus,
  ArrowRight,
  AlertCircle,
  Clock,
  User,
  ExternalLink,
  Play,
  Pause,
  RotateCcw,
  CheckCircle2,
  XCircle,
  UserCheck,
} from 'lucide-react';
import axios from 'axios';
import type {
  MaintenanceRequestResponse,
  MaintenanceRequestStatus,
  MaintenancePriority,
  WorkOrderResponse,
  WorkOrderStatus,
  WorkOrderType,
  DowntimeLogResponse,
  DowntimeReasonCategory,
} from '../../types/maintenance';
import type { EquipmentResponse } from '../../types/equipment';
import {
  getMaintenanceRequests,
  getWorkOrders,
  startWorkOrder,
  resumeWorkOrder,
  getDowntimeLogs,
} from '../../api/maintenance';
import { getEquipmentList } from '../../api/equipment';
import { MaintenanceRequestStatusBadge } from '../../components/maintenance/MaintenanceRequestStatusBadge';
import { WorkOrderStatusBadge } from '../../components/maintenance/WorkOrderStatusBadge';
import { PriorityBadge } from '../../components/maintenance/PriorityBadge';
import { DowntimeReasonBadge } from '../../components/maintenance/DowntimeReasonBadge';

import { CreateMaintenanceRequestModal } from '../../components/maintenance/CreateMaintenanceRequestModal';
import { TriageRequestModal } from '../../components/maintenance/TriageRequestModal';
import { RejectRequestModal } from '../../components/maintenance/RejectRequestModal';
import { ResolveRequestModal } from '../../components/maintenance/ResolveRequestModal';
import { CreateWorkOrderModal } from '../../components/maintenance/CreateWorkOrderModal';
import { AssignTechnicianModal } from '../../components/maintenance/AssignTechnicianModal';
import { PauseWorkOrderModal } from '../../components/maintenance/PauseWorkOrderModal';
import { CompleteWorkOrderModal } from '../../components/maintenance/CompleteWorkOrderModal';
import { CancelWorkOrderModal } from '../../components/maintenance/CancelWorkOrderModal';
import { RecordDowntimeModal } from '../../components/maintenance/RecordDowntimeModal';
import { EndDowntimeModal } from '../../components/maintenance/EndDowntimeModal';

type ActiveTab = 'requests' | 'work_orders' | 'downtime';

export const MaintenancePage: React.FC = () => {

  const [activeTab, setActiveTab] = useState<ActiveTab>('requests');

  // Shared Equipment lookup
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);

  // Requests State
  const [requests, setRequests] = useState<MaintenanceRequestResponse[]>([]);
  const [isLoadingRequests, setIsLoadingRequests] = useState<boolean>(true);
  const [requestSearch, setRequestSearch] = useState<string>('');
  const [requestStatusFilter, setRequestStatusFilter] = useState<MaintenanceRequestStatus | 'ALL'>('ALL');
  const [requestPriorityFilter, setRequestPriorityFilter] = useState<MaintenancePriority | 'ALL'>('ALL');
  const [requestEquipmentFilter, setRequestEquipmentFilter] = useState<number | 'ALL'>('ALL');

  // Work Orders State
  const [workOrders, setWorkOrders] = useState<WorkOrderResponse[]>([]);
  const [isLoadingWorkOrders, setIsLoadingWorkOrders] = useState<boolean>(true);
  const [woSearch, setWoSearch] = useState<string>('');
  const [woStatusFilter, setWoStatusFilter] = useState<WorkOrderStatus | 'ALL'>('ALL');
  const [woPriorityFilter, setWoPriorityFilter] = useState<MaintenancePriority | 'ALL'>('ALL');
  const [woTypeFilter, setWoTypeFilter] = useState<WorkOrderType | 'ALL'>('ALL');
  const [woEquipmentFilter, setWoEquipmentFilter] = useState<number | 'ALL'>('ALL');

  // Downtime State
  const [downtimeLogs, setDowntimeLogs] = useState<DowntimeLogResponse[]>([]);
  const [isLoadingDowntime, setIsLoadingDowntime] = useState<boolean>(true);
  const [downtimeSearch, setDowntimeSearch] = useState<string>('');
  const [downtimeReasonFilter, setDowntimeReasonFilter] = useState<DowntimeReasonCategory | 'ALL'>('ALL');
  const [downtimeEquipmentFilter, setDowntimeEquipmentFilter] = useState<number | 'ALL'>('ALL');
  const [downtimeActiveOnly, setDowntimeActiveOnly] = useState<boolean>(false);

  // Global Error & Toast
  const [actionError, setActionError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [processingActionId, setProcessingActionId] = useState<number | null>(null);

  // Modals state
  const [isCreateRequestOpen, setIsCreateRequestOpen] = useState<boolean>(false);
  const [triageRequestTarget, setTriageRequestTarget] = useState<MaintenanceRequestResponse | null>(null);
  const [rejectRequestTarget, setRejectRequestTarget] = useState<MaintenanceRequestResponse | null>(null);
  const [resolveRequestTarget, setResolveRequestTarget] = useState<MaintenanceRequestResponse | null>(null);

  const [isCreateWorkOrderOpen, setIsCreateWorkOrderOpen] = useState<boolean>(false);
  const [createWoInitialRequest, setCreateWoInitialRequest] = useState<MaintenanceRequestResponse | null>(null);
  const [assignTechnicianTarget, setAssignTechnicianTarget] = useState<WorkOrderResponse | null>(null);
  const [pauseWorkOrderTarget, setPauseWorkOrderTarget] = useState<WorkOrderResponse | null>(null);
  const [completeWorkOrderTarget, setCompleteWorkOrderTarget] = useState<WorkOrderResponse | null>(null);
  const [cancelWorkOrderTarget, setCancelWorkOrderTarget] = useState<WorkOrderResponse | null>(null);

  const [isRecordDowntimeOpen, setIsRecordDowntimeOpen] = useState<boolean>(false);
  const [endDowntimeTarget, setEndDowntimeTarget] = useState<DowntimeLogResponse | null>(null);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  // --- Data Loading ---
  const loadEquipment = useCallback(async () => {
    try {
      const items = await getEquipmentList();
      setEquipmentList(items);
    } catch (err) {
      console.error('Failed to load equipment:', err);
    }
  }, []);

  const loadRequests = useCallback(async () => {
    setIsLoadingRequests(true);
    setActionError(null);
    try {
      const data = await getMaintenanceRequests();
      setRequests(data);
    } catch (err: unknown) {
      console.error('Failed to load requests:', err);
      setActionError('Unable to load maintenance requests from server.');
    } finally {
      setIsLoadingRequests(false);
    }
  }, []);

  const loadWorkOrders = useCallback(async () => {
    setIsLoadingWorkOrders(true);
    setActionError(null);
    try {
      const data = await getWorkOrders();
      setWorkOrders(data);
    } catch (err: unknown) {
      console.error('Failed to load work orders:', err);
      setActionError('Unable to load work orders from server.');
    } finally {
      setIsLoadingWorkOrders(false);
    }
  }, []);

  const loadDowntime = useCallback(async () => {
    setIsLoadingDowntime(true);
    setActionError(null);
    try {
      const data = await getDowntimeLogs();
      setDowntimeLogs(data);
    } catch (err: unknown) {
      console.error('Failed to load downtime logs:', err);
      setActionError('Unable to load equipment downtime logs from server.');
    } finally {
      setIsLoadingDowntime(false);
    }
  }, []);

  useEffect(() => {
    loadEquipment();
    loadRequests();
    loadWorkOrders();
    loadDowntime();
  }, [loadEquipment, loadRequests, loadWorkOrders, loadDowntime]);

  // --- Inline Work Order Quick Actions ---
  const handleStartWorkOrder = async (wo: WorkOrderResponse) => {
    setProcessingActionId(wo.id);
    setActionError(null);
    try {
      const updated = await startWorkOrder(wo.id);
      setWorkOrders((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      showToast(`Work Order ${updated.workOrderNumber} started. Equipment is now UNDER_MAINTENANCE.`);
      loadEquipment();
    } catch (err: unknown) {
      console.error('Failed to start work order:', err);
      if (axios.isAxiosError(err)) {
        setActionError(err.response?.data?.message || err.response?.data?.error || 'Failed to start work order.');
      } else {
        setActionError('Failed to start work order.');
      }
    } finally {
      setProcessingActionId(null);
    }
  };

  const handleResumeWorkOrder = async (wo: WorkOrderResponse) => {
    setProcessingActionId(wo.id);
    setActionError(null);
    try {
      const updated = await resumeWorkOrder(wo.id);
      setWorkOrders((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      showToast(`Work Order ${updated.workOrderNumber} resumed.`);
    } catch (err: unknown) {
      console.error('Failed to resume work order:', err);
      if (axios.isAxiosError(err)) {
        setActionError(err.response?.data?.message || err.response?.data?.error || 'Failed to resume work order.');
      } else {
        setActionError('Failed to resume work order.');
      }
    } finally {
      setProcessingActionId(null);
    }
  };

  // --- Filtered Data ---
  const filteredRequests = useMemo(() => {
    return requests.filter((r) => {
      if (requestStatusFilter !== 'ALL' && r.status !== requestStatusFilter) return false;
      if (requestPriorityFilter !== 'ALL' && r.priority !== requestPriorityFilter) return false;
      if (requestEquipmentFilter !== 'ALL' && r.equipmentId !== requestEquipmentFilter) return false;
      if (requestSearch.trim()) {
        const q = requestSearch.toLowerCase();
        const matchTitle = r.issueTitle.toLowerCase().includes(q);
        const matchNum = r.requestNumber.toLowerCase().includes(q);
        const matchEq = r.equipmentName ? r.equipmentName.toLowerCase().includes(q) : false;
        const matchUser = r.reportedByUserName ? r.reportedByUserName.toLowerCase().includes(q) : false;
        if (!matchTitle && !matchNum && !matchEq && !matchUser) return false;
      }
      return true;
    });
  }, [requests, requestStatusFilter, requestPriorityFilter, requestEquipmentFilter, requestSearch]);

  const filteredWorkOrders = useMemo(() => {
    return workOrders.filter((wo) => {
      if (woStatusFilter !== 'ALL' && wo.status !== woStatusFilter) return false;
      if (woPriorityFilter !== 'ALL' && wo.priority !== woPriorityFilter) return false;
      if (woTypeFilter !== 'ALL' && wo.type !== woTypeFilter) return false;
      if (woEquipmentFilter !== 'ALL' && wo.equipmentId !== woEquipmentFilter) return false;
      if (woSearch.trim()) {
        const q = woSearch.toLowerCase();
        const matchNum = wo.workOrderNumber.toLowerCase().includes(q);
        const matchEq = wo.equipmentName ? wo.equipmentName.toLowerCase().includes(q) : false;
        const matchTech = wo.assignedTechnicianName ? wo.assignedTechnicianName.toLowerCase().includes(q) : false;
        const matchReq = wo.maintenanceRequestNumber ? wo.maintenanceRequestNumber.toLowerCase().includes(q) : false;
        if (!matchNum && !matchEq && !matchTech && !matchReq) return false;
      }
      return true;
    });
  }, [workOrders, woStatusFilter, woPriorityFilter, woTypeFilter, woEquipmentFilter, woSearch]);

  const filteredDowntime = useMemo(() => {
    return downtimeLogs.filter((d) => {
      if (downtimeReasonFilter !== 'ALL' && d.reasonCategory !== downtimeReasonFilter) return false;
      if (downtimeEquipmentFilter !== 'ALL' && d.equipmentId !== downtimeEquipmentFilter) return false;
      if (downtimeActiveOnly && d.downtimeEnd) return false;
      if (downtimeSearch.trim()) {
        const q = downtimeSearch.toLowerCase();
        const matchEq = d.equipmentName ? d.equipmentName.toLowerCase().includes(q) : false;
        const matchDesc = d.description ? d.description.toLowerCase().includes(q) : false;
        const matchWo = d.workOrderNumber ? d.workOrderNumber.toLowerCase().includes(q) : false;
        if (!matchEq && !matchDesc && !matchWo) return false;
      }
      return true;
    });
  }, [downtimeLogs, downtimeReasonFilter, downtimeEquipmentFilter, downtimeActiveOnly, downtimeSearch]);

  // Honest metrics
  const requestMetrics = useMemo(() => {
    const total = requests.length;
    const submitted = requests.filter((r) => r.status === 'SUBMITTED').length;
    const triaged = requests.filter((r) => r.status === 'TRIAGED').length;
    const woCreated = requests.filter((r) => r.status === 'WORK_ORDER_CREATED').length;
    const resolved = requests.filter((r) => r.status === 'RESOLVED').length;
    return { total, submitted, triaged, woCreated, resolved };
  }, [requests]);

  const woMetrics = useMemo(() => {
    const total = workOrders.length;
    const scheduled = workOrders.filter((w) => w.status === 'SCHEDULED').length;
    const inProgress = workOrders.filter((w) => w.status === 'IN_PROGRESS').length;
    const waiting = workOrders.filter((w) => w.status === 'WAITING_FOR_PARTS').length;
    const completed = workOrders.filter((w) => w.status === 'COMPLETED').length;
    return { total, scheduled, inProgress, waiting, completed };
  }, [workOrders]);

  const downtimeMetrics = useMemo(() => {
    const total = downtimeLogs.length;
    const active = downtimeLogs.filter((d) => !d.downtimeEnd).length;
    const totalMinutes = downtimeLogs.reduce((acc, curr) => acc + (curr.durationMinutes || 0), 0);
    return { total, active, totalMinutes };
  }, [downtimeLogs]);

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-950/90 border border-emerald-500/40 text-emerald-200 rounded-xl shadow-2xl flex items-center gap-3 text-xs animate-in fade-in slide-in-from-top-3 duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Global Action Error Alert */}
      {actionError && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start justify-between gap-3 text-xs text-rose-300">
          <div className="flex items-start gap-2.5">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-200">Maintenance Action Error</p>
              <p className="mt-0.5">{actionError}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setActionError(null)}
            className="text-slate-400 hover:text-white text-xs font-semibold px-2 py-1 bg-slate-800 rounded-lg"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2.5">
            <Wrench className="w-7 h-7 text-indigo-400" />
            Maintenance & Service Orders
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Coordinate equipment repair requests, manage lifecycle work orders, and log operational downtime.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => {
              loadRequests();
              loadWorkOrders();
              loadDowntime();
              loadEquipment();
            }}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-850 hover:bg-slate-800 border border-slate-700/80 rounded-xl transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </button>

          {activeTab === 'requests' && (
            <button
              type="button"
              onClick={() => setIsCreateRequestOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>New Request</span>
            </button>
          )}

          {activeTab === 'work_orders' && (
            <button
              type="button"
              onClick={() => {
                setCreateWoInitialRequest(null);
                setIsCreateWorkOrderOpen(true);
              }}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>New Work Order</span>
            </button>
          )}

          {activeTab === 'downtime' && (
            <button
              type="button"
              onClick={() => setIsRecordDowntimeOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-500 rounded-xl shadow-lg shadow-rose-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Log Downtime</span>
            </button>
          )}
        </div>
      </div>

      {/* Tabs Navigation */}
      <div className="flex border-b border-slate-800">
        <button
          type="button"
          onClick={() => setActiveTab('requests')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'requests'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <Wrench className="w-4 h-4" />
          <span>Maintenance Requests</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {requests.length}
          </span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('work_orders')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'work_orders'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <Hammer className="w-4 h-4" />
          <span>Work Orders</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {workOrders.length}
          </span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('downtime')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'downtime'
              ? 'border-rose-500 text-rose-400 bg-rose-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <AlertOctagon className="w-4 h-4" />
          <span>Downtime Records</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {downtimeLogs.length}
          </span>
        </button>
      </div>

      {/* =========================================================
          TAB 1: MAINTENANCE REQUESTS
      ========================================================= */}
      {activeTab === 'requests' && (
        <div className="space-y-6">
          {/* Honest Metric KPI Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-slate-400">Total Requests</p>
              <p className="text-2xl font-bold text-white mt-1 font-mono">{requestMetrics.total}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-amber-400">Submitted & Pending</p>
              <p className="text-2xl font-bold text-amber-300 mt-1 font-mono">{requestMetrics.submitted}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-cyan-400">Triaged / In Queue</p>
              <p className="text-2xl font-bold text-cyan-300 mt-1 font-mono">{requestMetrics.triaged}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-emerald-400">Resolved</p>
              <p className="text-2xl font-bold text-emerald-300 mt-1 font-mono">{requestMetrics.resolved}</p>
            </div>
          </div>

          {/* Search and Filters */}
          <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl space-y-3">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={requestSearch}
                  onChange={(e) => setRequestSearch(e.target.value)}
                  placeholder="Search by request #, issue title, instrument, reporter..."
                  className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>

              <div className="flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4 text-slate-400 shrink-0" />
                <select
                  value={requestStatusFilter}
                  onChange={(e) => setRequestStatusFilter(e.target.value as MaintenanceRequestStatus | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Statuses</option>
                  <option value="SUBMITTED">Submitted</option>
                  <option value="TRIAGED">Triaged</option>
                  <option value="WORK_ORDER_CREATED">Work Order Created</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="REJECTED">Rejected</option>
                </select>

                <select
                  value={requestPriorityFilter}
                  onChange={(e) => setRequestPriorityFilter(e.target.value as MaintenancePriority | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Priorities</option>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>

                <select
                  value={requestEquipmentFilter}
                  onChange={(e) => setRequestEquipmentFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 max-w-[200px]"
                >
                  <option value="ALL">All Instruments</option>
                  {equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Requests Content */}
          {isLoadingRequests ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl">
              <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
              <p className="text-xs text-slate-400 mt-3">Loading maintenance requests...</p>
            </div>
          ) : filteredRequests.length === 0 ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
              <Wrench className="w-10 h-10 text-slate-600 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Maintenance Requests Found</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                {requests.length === 0
                  ? 'No maintenance requests have been submitted yet. Report an equipment issue to start the triage workflow.'
                  : 'No requests matched your active search or filter criteria.'}
              </p>
              {requests.length === 0 && (
                <button
                  type="button"
                  onClick={() => setIsCreateRequestOpen(true)}
                  className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
                >
                  <Plus className="w-4 h-4" />
                  <span>Submit Maintenance Request</span>
                </button>
              )}
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">Request #</th>
                      <th className="py-3.5 px-4">Instrument</th>
                      <th className="py-3.5 px-4">Priority</th>
                      <th className="py-3.5 px-4">Issue Description</th>
                      <th className="py-3.5 px-4">Status</th>
                      <th className="py-3.5 px-4">Reported By</th>
                      <th className="py-3.5 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredRequests.map((req) => (
                      <tr key={req.id} className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3.5 px-4 font-mono font-medium text-indigo-400">
                          <Link
                            to={`/maintenance/requests/${req.id}`}
                            className="hover:underline inline-flex items-center gap-1"
                          >
                            <span>{req.requestNumber}</span>
                            <ExternalLink className="w-3 h-3 text-slate-500" />
                          </Link>
                        </td>
                        <td className="py-3.5 px-4">
                          {req.equipmentId ? (
                            <Link
                              to={`/equipment/${req.equipmentId}`}
                              className="text-white hover:text-indigo-400 font-medium hover:underline inline-flex items-center gap-1"
                            >
                              <span>{req.equipmentName || `Instrument #${req.equipmentId}`}</span>
                            </Link>
                          ) : (
                            <span className="text-slate-400">{req.equipmentName || '—'}</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <PriorityBadge priority={req.priority} />
                        </td>
                        <td className="py-3.5 px-4 max-w-xs">
                          <p className="font-medium text-white truncate">{req.issueTitle}</p>
                          <p className="text-slate-400 text-[11px] truncate mt-0.5">{req.issueDescription}</p>
                        </td>
                        <td className="py-3.5 px-4">
                          <MaintenanceRequestStatusBadge status={req.status} />
                        </td>
                        <td className="py-3.5 px-4 text-slate-400 text-[11px]">
                          <div>{req.reportedByUserName || 'Automated'}</div>
                          <div className="font-mono text-slate-500">{new Date(req.createdAt).toLocaleDateString()}</div>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-1.5">
                            {req.status === 'SUBMITTED' && (
                              <button
                                type="button"
                                onClick={() => setTriageRequestTarget(req)}
                                className="px-2.5 py-1 text-[11px] font-semibold text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-lg transition-colors"
                              >
                                Triage
                              </button>
                            )}

                            {(req.status === 'SUBMITTED' || req.status === 'TRIAGED') && (
                              <button
                                type="button"
                                onClick={() => {
                                  setCreateWoInitialRequest(req);
                                  setIsCreateWorkOrderOpen(true);
                                }}
                                className="px-2.5 py-1 text-[11px] font-semibold text-indigo-300 bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/30 rounded-lg transition-colors"
                              >
                                Create WO
                              </button>
                            )}

                            {(req.status === 'SUBMITTED' || req.status === 'TRIAGED') && (
                              <button
                                type="button"
                                onClick={() => setRejectRequestTarget(req)}
                                className="px-2 py-1 text-[11px] font-semibold text-rose-400 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 rounded-lg transition-colors"
                              >
                                Reject
                              </button>
                            )}

                            {req.status !== 'RESOLVED' && req.status !== 'REJECTED' && (
                              <button
                                type="button"
                                onClick={() => setResolveRequestTarget(req)}
                                className="px-2.5 py-1 text-[11px] font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors"
                              >
                                Resolve
                              </button>
                            )}

                            <Link
                              to={`/maintenance/requests/${req.id}`}
                              className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
                              title="View full request details"
                            >
                              <ArrowRight className="w-4 h-4" />
                            </Link>
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

      {/* =========================================================
          TAB 2: WORK ORDERS
      ========================================================= */}
      {activeTab === 'work_orders' && (
        <div className="space-y-6">
          {/* Honest Metric KPI Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-slate-400">Total Work Orders</p>
              <p className="text-2xl font-bold text-white mt-1 font-mono">{woMetrics.total}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-sky-400">Scheduled</p>
              <p className="text-2xl font-bold text-sky-300 mt-1 font-mono">{woMetrics.scheduled}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-amber-400">In Progress</p>
              <p className="text-2xl font-bold text-amber-300 mt-1 font-mono">{woMetrics.inProgress}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-emerald-400">Completed</p>
              <p className="text-2xl font-bold text-emerald-300 mt-1 font-mono">{woMetrics.completed}</p>
            </div>
          </div>

          {/* Work Orders Search and Filters */}
          <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl space-y-3">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={woSearch}
                  onChange={(e) => setWoSearch(e.target.value)}
                  placeholder="Search by WO #, instrument, technician, request #..."
                  className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>

              <div className="flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4 text-slate-400 shrink-0" />
                <select
                  value={woStatusFilter}
                  onChange={(e) => setWoStatusFilter(e.target.value as WorkOrderStatus | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Statuses</option>
                  <option value="SCHEDULED">Scheduled</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="WAITING_FOR_PARTS">Waiting for Parts</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>

                <select
                  value={woTypeFilter}
                  onChange={(e) => setWoTypeFilter(e.target.value as WorkOrderType | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Types</option>
                  <option value="CORRECTIVE">Corrective Repair</option>
                  <option value="PREVENTIVE">Preventive</option>
                  <option value="EMERGENCY">Emergency</option>
                  <option value="OVERHAUL">Overhaul</option>
                  <option value="DECOMMISSION">Decommission</option>
                </select>

                <select
                  value={woPriorityFilter}
                  onChange={(e) => setWoPriorityFilter(e.target.value as MaintenancePriority | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Priorities</option>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>

                <select
                  value={woEquipmentFilter}
                  onChange={(e) => setWoEquipmentFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 max-w-[200px]"
                >
                  <option value="ALL">All Instruments</option>
                  {equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Work Orders Content */}
          {isLoadingWorkOrders ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl">
              <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
              <p className="text-xs text-slate-400 mt-3">Loading work orders...</p>
            </div>
          ) : filteredWorkOrders.length === 0 ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
              <Hammer className="w-10 h-10 text-slate-600 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Work Orders Found</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                {workOrders.length === 0
                  ? 'No work orders currently exist. Schedule a preventative maintenance run or dispatch a repair order.'
                  : 'No work orders match the current filter selection.'}
              </p>
              {workOrders.length === 0 && (
                <button
                  type="button"
                  onClick={() => setIsCreateWorkOrderOpen(true)}
                  className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
                >
                  <Plus className="w-4 h-4" />
                  <span>Create Work Order</span>
                </button>
              )}
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">WO #</th>
                      <th className="py-3.5 px-4">Instrument</th>
                      <th className="py-3.5 px-4">Type & Priority</th>
                      <th className="py-3.5 px-4">Technician</th>
                      <th className="py-3.5 px-4">Status</th>
                      <th className="py-3.5 px-4">Schedule</th>
                      <th className="py-3.5 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredWorkOrders.map((wo) => (
                      <tr key={wo.id} className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3.5 px-4 font-mono font-medium text-indigo-400">
                          <Link
                            to={`/maintenance/work-orders/${wo.id}`}
                            className="hover:underline inline-flex items-center gap-1"
                          >
                            <span>{wo.workOrderNumber}</span>
                            <ExternalLink className="w-3 h-3 text-slate-500" />
                          </Link>
                          {wo.maintenanceRequestNumber && (
                            <div className="text-[10px] text-slate-500 font-normal">
                              Req:{' '}
                              <Link
                                to={`/maintenance/requests/${wo.maintenanceRequestId}`}
                                className="text-slate-400 hover:underline"
                              >
                                {wo.maintenanceRequestNumber}
                              </Link>
                            </div>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          {wo.equipmentId ? (
                            <Link
                              to={`/equipment/${wo.equipmentId}`}
                              className="text-white hover:text-indigo-400 font-medium hover:underline inline-flex items-center gap-1"
                            >
                              <span>{wo.equipmentName || `Instrument #${wo.equipmentId}`}</span>
                            </Link>
                          ) : (
                            <span className="text-slate-400">{wo.equipmentName || '—'}</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 space-y-1">
                          <div className="font-semibold text-white">{wo.type}</div>
                          <PriorityBadge priority={wo.priority} />
                        </td>
                        <td className="py-3.5 px-4">
                          <div className="flex items-center gap-1.5 text-slate-200 font-medium">
                            <User className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                            <span>{wo.assignedTechnicianName || 'Unassigned'}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4">
                          <WorkOrderStatusBadge status={wo.status} />
                        </td>
                        <td className="py-3.5 px-4 text-slate-400 text-[11px] font-mono">
                          <div>Start: {new Date(wo.scheduledStart).toLocaleDateString()}</div>
                          <div>End: {new Date(wo.scheduledEnd).toLocaleDateString()}</div>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-1.5">
                            {/* Actions valid strictly for current state */}
                            {wo.status === 'SCHEDULED' && (
                              <button
                                type="button"
                                disabled={processingActionId === wo.id}
                                onClick={() => handleStartWorkOrder(wo)}
                                className="px-2.5 py-1 text-[11px] font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                              >
                                <Play className="w-3 h-3" />
                                <span>Start</span>
                              </button>
                            )}

                            {wo.status === 'IN_PROGRESS' && (
                              <>
                                <button
                                  type="button"
                                  onClick={() => setPauseWorkOrderTarget(wo)}
                                  className="px-2 py-1 text-[11px] font-semibold text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                                >
                                  <Pause className="w-3 h-3" />
                                  <span>Pause</span>
                                </button>
                                <button
                                  type="button"
                                  onClick={() => setCompleteWorkOrderTarget(wo)}
                                  className="px-2.5 py-1 text-[11px] font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                                >
                                  <CheckCircle2 className="w-3 h-3" />
                                  <span>Complete</span>
                                </button>
                              </>
                            )}

                            {wo.status === 'WAITING_FOR_PARTS' && (
                              <>
                                <button
                                  type="button"
                                  disabled={processingActionId === wo.id}
                                  onClick={() => handleResumeWorkOrder(wo)}
                                  className="px-2.5 py-1 text-[11px] font-semibold text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                                >
                                  <RotateCcw className="w-3 h-3" />
                                  <span>Resume</span>
                                </button>
                                <button
                                  type="button"
                                  onClick={() => setCompleteWorkOrderTarget(wo)}
                                  className="px-2.5 py-1 text-[11px] font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                                >
                                  <CheckCircle2 className="w-3 h-3" />
                                  <span>Complete</span>
                                </button>
                              </>
                            )}

                            {wo.status !== 'COMPLETED' && wo.status !== 'CANCELLED' && (
                              <>
                                <button
                                  type="button"
                                  onClick={() => setAssignTechnicianTarget(wo)}
                                  className="p-1 text-slate-400 hover:text-indigo-400 rounded-lg hover:bg-slate-800 transition-colors"
                                  title="Reassign Technician"
                                >
                                  <UserCheck className="w-4 h-4" />
                                </button>
                                <button
                                  type="button"
                                  onClick={() => setCancelWorkOrderTarget(wo)}
                                  className="p-1 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-slate-800 transition-colors"
                                  title="Cancel Work Order"
                                >
                                  <XCircle className="w-4 h-4" />
                                </button>
                              </>
                            )}

                            <Link
                              to={`/maintenance/work-orders/${wo.id}`}
                              className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
                              title="View Work Order details"
                            >
                              <ArrowRight className="w-4 h-4" />
                            </Link>
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

      {/* =========================================================
          TAB 3: DOWNTIME RECORDS
      ========================================================= */}
      {activeTab === 'downtime' && (
        <div className="space-y-6">
          {/* Honest Metric KPI Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-slate-400">Total Downtime Logs</p>
              <p className="text-2xl font-bold text-white mt-1 font-mono">{downtimeMetrics.total}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-rose-400">Active Outages</p>
              <p className="text-2xl font-bold text-rose-300 mt-1 font-mono">{downtimeMetrics.active}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-amber-400">Logged Minutes</p>
              <p className="text-2xl font-bold text-amber-300 mt-1 font-mono">{downtimeMetrics.totalMinutes}m</p>
            </div>
          </div>

          {/* Downtime Search and Filters */}
          <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl space-y-3">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={downtimeSearch}
                  onChange={(e) => setDowntimeSearch(e.target.value)}
                  placeholder="Search by instrument, description, WO #..."
                  className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                />
              </div>

              <div className="flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4 text-slate-400 shrink-0" />
                <select
                  value={downtimeReasonFilter}
                  onChange={(e) => setDowntimeReasonFilter(e.target.value as DowntimeReasonCategory | 'ALL')}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50"
                >
                  <option value="ALL">All Categories</option>
                  <option value="UNSCHEDULED_BREAKDOWN">Unscheduled Breakdown</option>
                  <option value="SCHEDULED_MAINTENANCE">Scheduled Maintenance</option>
                  <option value="CALIBRATION">Calibration Service</option>
                  <option value="FACILITY_OUTAGE">Facility Outage</option>
                  <option value="SAFETY_HOLD">Safety Hold</option>
                </select>

                <select
                  value={downtimeEquipmentFilter}
                  onChange={(e) => setDowntimeEquipmentFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-rose-500/50 max-w-[200px]"
                >
                  <option value="ALL">All Instruments</option>
                  {equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name}
                    </option>
                  ))}
                </select>

                <label className="flex items-center gap-1.5 text-xs text-slate-300 font-medium cursor-pointer pl-2">
                  <input
                    type="checkbox"
                    checked={downtimeActiveOnly}
                    onChange={(e) => setDowntimeActiveOnly(e.target.checked)}
                    className="rounded bg-slate-800 border-slate-700 text-rose-600 focus:ring-rose-500/50"
                  />
                  <span>Ongoing Only</span>
                </label>
              </div>
            </div>
          </div>

          {/* Downtime Content */}
          {isLoadingDowntime ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl">
              <RefreshCw className="w-8 h-8 text-rose-400 animate-spin mx-auto" />
              <p className="text-xs text-slate-400 mt-3">Loading equipment downtime records...</p>
            </div>
          ) : filteredDowntime.length === 0 ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
              <AlertOctagon className="w-10 h-10 text-slate-600 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Downtime Logs Found</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                {downtimeLogs.length === 0
                  ? 'No downtime intervals recorded. Equipment availability operates at nominal capacity.'
                  : 'No downtime logs match the active filter parameters.'}
              </p>
              {downtimeLogs.length === 0 && (
                <button
                  type="button"
                  onClick={() => setIsRecordDowntimeOpen(true)}
                  className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-500 rounded-xl shadow-lg shadow-rose-600/20"
                >
                  <Plus className="w-4 h-4" />
                  <span>Log Downtime Incident</span>
                </button>
              )}
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">Instrument</th>
                      <th className="py-3.5 px-4">Reason Category</th>
                      <th className="py-3.5 px-4">Downtime Interval</th>
                      <th className="py-3.5 px-4">Duration</th>
                      <th className="py-3.5 px-4">WO Link</th>
                      <th className="py-3.5 px-4">Description</th>
                      <th className="py-3.5 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredDowntime.map((log) => (
                      <tr key={log.id} className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3.5 px-4">
                          {log.equipmentId ? (
                            <Link
                              to={`/equipment/${log.equipmentId}`}
                              className="text-white hover:text-indigo-400 font-medium hover:underline inline-flex items-center gap-1"
                            >
                              <span>{log.equipmentName || `Instrument #${log.equipmentId}`}</span>
                            </Link>
                          ) : (
                            <span className="text-slate-400">{log.equipmentName || '—'}</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <DowntimeReasonBadge category={log.reasonCategory} />
                        </td>
                        <td className="py-3.5 px-4 text-slate-400 text-[11px] font-mono">
                          <div>From: {new Date(log.downtimeStart).toLocaleString()}</div>
                          <div>To: {log.downtimeEnd ? new Date(log.downtimeEnd).toLocaleString() : <span className="text-rose-400 font-semibold">Active Outage</span>}</div>
                        </td>
                        <td className="py-3.5 px-4">
                          {log.durationMinutes !== null && log.durationMinutes !== undefined ? (
                            <span className="font-mono text-white font-medium">{log.durationMinutes} mins</span>
                          ) : log.downtimeEnd ? (
                            <span className="text-slate-400">Recorded</span>
                          ) : (
                            <span className="inline-flex items-center gap-1 text-rose-400 font-mono text-[11px]">
                              <span className="w-1.5 h-1.5 rounded-full bg-rose-400 animate-ping" />
                              Ongoing
                            </span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 font-mono">
                          {log.workOrderId ? (
                            <Link
                              to={`/maintenance/work-orders/${log.workOrderId}`}
                              className="text-indigo-400 hover:underline inline-flex items-center gap-1"
                            >
                              <span>{log.workOrderNumber || `WO #${log.workOrderId}`}</span>
                            </Link>
                          ) : (
                            <span className="text-slate-500">—</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 max-w-xs truncate text-slate-300" title={log.description}>
                          {log.description}
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          {!log.downtimeEnd && (
                            <button
                              type="button"
                              onClick={() => setEndDowntimeTarget(log)}
                              className="px-2.5 py-1 text-[11px] font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors inline-flex items-center gap-1"
                            >
                              <Clock className="w-3 h-3" />
                              <span>End Outage</span>
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

      {/* =========================================================
          MODALS
      ========================================================= */}
      <CreateMaintenanceRequestModal
        isOpen={isCreateRequestOpen}
        onClose={() => setIsCreateRequestOpen(false)}
        onSuccess={(created) => {
          setRequests((prev) => [created, ...prev]);
          showToast(`Request ${created.requestNumber} submitted successfully.`);
        }}
      />

      <TriageRequestModal
        isOpen={!!triageRequestTarget}
        request={triageRequestTarget}
        onClose={() => setTriageRequestTarget(null)}
        onSuccess={(updated) => {
          setRequests((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
          showToast(`Request ${updated.requestNumber} triaged.`);
        }}
      />

      <RejectRequestModal
        isOpen={!!rejectRequestTarget}
        request={rejectRequestTarget}
        onClose={() => setRejectRequestTarget(null)}
        onSuccess={(updated) => {
          setRequests((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
          showToast(`Request ${updated.requestNumber} rejected.`);
        }}
      />

      <ResolveRequestModal
        isOpen={!!resolveRequestTarget}
        request={resolveRequestTarget}
        onClose={() => setResolveRequestTarget(null)}
        onSuccess={(updated) => {
          setRequests((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
          showToast(`Request ${updated.requestNumber} resolved.`);
        }}
      />

      <CreateWorkOrderModal
        isOpen={isCreateWorkOrderOpen}
        initialRequest={createWoInitialRequest}
        onClose={() => {
          setIsCreateWorkOrderOpen(false);
          setCreateWoInitialRequest(null);
        }}
        onSuccess={(created) => {
          setWorkOrders((prev) => [created, ...prev]);
          showToast(`Work Order ${created.workOrderNumber} created.`);
          loadRequests(); // Refresh request status in case it moved to WORK_ORDER_CREATED
        }}
      />

      <AssignTechnicianModal
        isOpen={!!assignTechnicianTarget}
        workOrder={assignTechnicianTarget}
        onClose={() => setAssignTechnicianTarget(null)}
        onSuccess={(updated) => {
          setWorkOrders((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
          showToast(`Technician assigned to ${updated.workOrderNumber}.`);
        }}
      />

      <PauseWorkOrderModal
        isOpen={!!pauseWorkOrderTarget}
        workOrder={pauseWorkOrderTarget}
        onClose={() => setPauseWorkOrderTarget(null)}
        onSuccess={(updated) => {
          setWorkOrders((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
          showToast(`Work order ${updated.workOrderNumber} paused.`);
        }}
      />

      <CompleteWorkOrderModal
        isOpen={!!completeWorkOrderTarget}
        workOrder={completeWorkOrderTarget}
        onClose={() => setCompleteWorkOrderTarget(null)}
        onSuccess={(updated) => {
          setWorkOrders((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
          showToast(`Work order ${updated.workOrderNumber} marked COMPLETED.`);
          loadEquipment(); // Operational status restored
        }}
      />

      <CancelWorkOrderModal
        isOpen={!!cancelWorkOrderTarget}
        workOrder={cancelWorkOrderTarget}
        onClose={() => setCancelWorkOrderTarget(null)}
        onSuccess={(updated) => {
          setWorkOrders((prev) => prev.map((w) => (w.id === updated.id ? updated : w)));
          showToast(`Work order ${updated.workOrderNumber} cancelled.`);
          loadEquipment();
        }}
      />

      <RecordDowntimeModal
        isOpen={isRecordDowntimeOpen}
        onClose={() => setIsRecordDowntimeOpen(false)}
        onSuccess={(created) => {
          setDowntimeLogs((prev) => [created, ...prev]);
          showToast(`Equipment downtime recorded for ${created.equipmentName || `Equipment #${created.equipmentId}`}.`);
        }}
      />

      <EndDowntimeModal
        isOpen={!!endDowntimeTarget}
        downtimeLog={endDowntimeTarget}
        onClose={() => setEndDowntimeTarget(null)}
        onSuccess={(updated) => {
          setDowntimeLogs((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
          showToast(`Equipment downtime outage concluded.`);
        }}
      />
    </div>
  );
};
