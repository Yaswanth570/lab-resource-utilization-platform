import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  User,
  ExternalLink,
  RefreshCw,
  AlertCircle,
  Play,
  Pause,
  RotateCcw,
  CheckCircle2,
  XCircle,
  UserCheck,
  DollarSign,
  AlertOctagon,
  FileText,
} from 'lucide-react';
import axios from 'axios';
import type {
  WorkOrderResponse,
  DowntimeLogResponse,
} from '../../types/maintenance';
import {
  getWorkOrderById,
  startWorkOrder,
  resumeWorkOrder,
  getDowntimeLogs,
} from '../../api/maintenance';
import { WorkOrderStatusBadge } from '../../components/maintenance/WorkOrderStatusBadge';
import { PriorityBadge } from '../../components/maintenance/PriorityBadge';
import { DowntimeReasonBadge } from '../../components/maintenance/DowntimeReasonBadge';

import { AssignTechnicianModal } from '../../components/maintenance/AssignTechnicianModal';
import { PauseWorkOrderModal } from '../../components/maintenance/PauseWorkOrderModal';
import { CompleteWorkOrderModal } from '../../components/maintenance/CompleteWorkOrderModal';
import { CancelWorkOrderModal } from '../../components/maintenance/CancelWorkOrderModal';
import { RecordDowntimeModal } from '../../components/maintenance/RecordDowntimeModal';

export const WorkOrderDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [workOrder, setWorkOrder] = useState<WorkOrderResponse | null>(null);
  const [relatedDowntime, setRelatedDowntime] = useState<DowntimeLogResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [isProcessingAction, setIsProcessingAction] = useState<boolean>(false);

  // Modals
  const [isAssignOpen, setIsAssignOpen] = useState<boolean>(false);
  const [isPauseOpen, setIsPauseOpen] = useState<boolean>(false);
  const [isCompleteOpen, setIsCompleteOpen] = useState<boolean>(false);
  const [isCancelOpen, setIsCancelOpen] = useState<boolean>(false);
  const [isRecordDowntimeOpen, setIsRecordDowntimeOpen] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  const loadData = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const woData = await getWorkOrderById(id);
      setWorkOrder(woData);

      // Load related downtime logs
      try {
        const dtList = await getDowntimeLogs({ workOrderId: Number(id) });
        setRelatedDowntime(dtList);
      } catch (err) {
        console.warn('Could not load related downtime logs:', err);
      }
    } catch (err: unknown) {
      console.error('Failed to load work order details:', err);
      setErrorMessage('Could not load work order. It may not exist or network is unavailable.');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Quick Start Action
  const handleStart = async () => {
    if (!workOrder) return;
    setIsProcessingAction(true);
    setErrorMessage(null);
    try {
      const updated = await startWorkOrder(workOrder.id);
      setWorkOrder(updated);
      showToast(`Work order started. Equipment operational status set to UNDER_MAINTENANCE.`);
    } catch (err: unknown) {
      console.error('Failed to start work order:', err);
      if (axios.isAxiosError(err)) {
        setErrorMessage(err.response?.data?.message || err.response?.data?.error || 'Failed to start work order.');
      } else {
        setErrorMessage('Failed to start work order.');
      }
    } finally {
      setIsProcessingAction(false);
    }
  };

  // Quick Resume Action
  const handleResume = async () => {
    if (!workOrder) return;
    setIsProcessingAction(true);
    setErrorMessage(null);
    try {
      const updated = await resumeWorkOrder(workOrder.id);
      setWorkOrder(updated);
      showToast(`Work order resumed.`);
    } catch (err: unknown) {
      console.error('Failed to resume work order:', err);
      if (axios.isAxiosError(err)) {
        setErrorMessage(err.response?.data?.message || err.response?.data?.error || 'Failed to resume work order.');
      } else {
        setErrorMessage('Failed to resume work order.');
      }
    } finally {
      setIsProcessingAction(false);
    }
  };

  if (isLoading) {
    return (
      <div className="p-16 text-center bg-slate-900 border border-slate-800 rounded-2xl">
        <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
        <p className="text-xs text-slate-400 mt-3">Loading work order details...</p>
      </div>
    );
  }

  if (errorMessage && !workOrder) {
    return (
      <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
        <h2 className="text-base font-semibold text-white">Work Order Not Found</h2>
        <p className="text-xs text-slate-400 max-w-md mx-auto">{errorMessage || 'Work order could not be located.'}</p>
        <div className="flex items-center justify-center gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/maintenance')}
            className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl"
          >
            Back to Maintenance
          </button>
          <button
            type="button"
            onClick={loadData}
            className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!workOrder) return null;

  return (
    <div className="space-y-6">
      {/* Toast */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-950/90 border border-emerald-500/40 text-emerald-200 rounded-xl shadow-2xl flex items-center gap-3 text-xs animate-in fade-in slide-in-from-top-3 duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Action Error Alert */}
      {errorMessage && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start justify-between gap-3 text-xs text-rose-300">
          <div className="flex items-start gap-2.5">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-rose-200">Action Failed</p>
              <p className="mt-0.5">{errorMessage}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setErrorMessage(null)}
            className="text-slate-400 hover:text-white text-xs font-semibold px-2 py-1 bg-slate-800 rounded-lg"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Navigation Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/maintenance')}
            className="p-2 text-slate-400 hover:text-white bg-slate-850 hover:bg-slate-800 border border-slate-700/80 rounded-xl transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl font-bold text-white font-mono">{workOrder.workOrderNumber}</h1>
              <WorkOrderStatusBadge status={workOrder.status} />
              <PriorityBadge priority={workOrder.priority} />
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Type: <strong className="text-slate-200">{workOrder.type}</strong> • Created{' '}
              {new Date(workOrder.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        {/* State-Valid Lifecycle Actions */}
        <div className="flex items-center gap-2">
          {workOrder.status === 'SCHEDULED' && (
            <button
              type="button"
              disabled={isProcessingAction}
              onClick={handleStart}
              className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 rounded-xl shadow-lg shadow-emerald-600/20 transition-all disabled:opacity-50"
            >
              <Play className="w-3.5 h-3.5" />
              <span>Start Work Order</span>
            </button>
          )}

          {workOrder.status === 'IN_PROGRESS' && (
            <>
              <button
                type="button"
                onClick={() => setIsPauseOpen(true)}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-xl transition-colors"
              >
                <Pause className="w-3.5 h-3.5" />
                <span>Pause (Parts)</span>
              </button>
              <button
                type="button"
                onClick={() => setIsCompleteOpen(true)}
                className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 rounded-xl shadow-lg shadow-emerald-600/20 transition-all"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Complete Order</span>
              </button>
            </>
          )}

          {workOrder.status === 'WAITING_FOR_PARTS' && (
            <>
              <button
                type="button"
                disabled={isProcessingAction}
                onClick={handleResume}
                className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-cyan-600 hover:bg-cyan-500 rounded-xl shadow-lg shadow-cyan-600/20 transition-all disabled:opacity-50"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Resume Work</span>
              </button>
              <button
                type="button"
                onClick={() => setIsCompleteOpen(true)}
                className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-500 rounded-xl shadow-lg shadow-emerald-600/20 transition-all"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Complete Order</span>
              </button>
            </>
          )}

          {workOrder.status !== 'COMPLETED' && workOrder.status !== 'CANCELLED' && (
            <>
              <button
                type="button"
                onClick={() => setIsAssignOpen(true)}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-300 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition-colors"
              >
                <UserCheck className="w-3.5 h-3.5" />
                <span>Assign Tech</span>
              </button>
              <button
                type="button"
                onClick={() => setIsCancelOpen(true)}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-rose-400 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 rounded-xl transition-colors"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Cancel</span>
              </button>
            </>
          )}
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Schedule, Summary, Costs, Downtime */}
        <div className="lg:col-span-2 space-y-6">
          {/* Execution & Times Summary */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-400" />
              Schedule & Execution Timeline
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl space-y-1.5">
                <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Scheduled Window</p>
                <p className="text-xs text-white">
                  Start: <span className="font-mono text-slate-300">{new Date(workOrder.scheduledStart).toLocaleString()}</span>
                </p>
                <p className="text-xs text-white">
                  End: <span className="font-mono text-slate-300">{new Date(workOrder.scheduledEnd).toLocaleString()}</span>
                </p>
              </div>

              <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl space-y-1.5">
                <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Actual Execution</p>
                <p className="text-xs text-white">
                  Started: {workOrder.actualStart ? (
                    <span className="font-mono text-amber-300">{new Date(workOrder.actualStart).toLocaleString()}</span>
                  ) : (
                    <span className="text-slate-500">Not started yet</span>
                  )}
                </p>
                <p className="text-xs text-white">
                  Ended: {workOrder.actualEnd ? (
                    <span className="font-mono text-emerald-300">{new Date(workOrder.actualEnd).toLocaleString()}</span>
                  ) : (
                    <span className="text-slate-500">Ongoing / Pending</span>
                  )}
                </p>
              </div>
            </div>
          </div>

          {/* Work Summary & Technical Findings */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <FileText className="w-4 h-4 text-indigo-400" />
              Technical Report & Resolution
            </h2>

            <div className="space-y-4">
              <div>
                <p className="text-xs font-semibold text-slate-300">Work Performed Summary</p>
                <p className="text-xs text-slate-300 bg-slate-850/60 border border-slate-800 p-3 rounded-xl mt-1.5 whitespace-pre-wrap leading-relaxed">
                  {workOrder.workPerformedSummary || 'No summary entered yet.'}
                </p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <p className="text-xs font-semibold text-slate-300">Root Cause of Failure</p>
                  <p className="text-xs text-slate-300 bg-slate-850/60 border border-slate-800 p-3 rounded-xl mt-1.5">
                    {workOrder.failureRootCause || 'Unspecified'}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-semibold text-slate-300">Resolution Notes</p>
                  <p className="text-xs text-slate-300 bg-slate-850/60 border border-slate-800 p-3 rounded-xl mt-1.5">
                    {workOrder.resolutionNotes || 'No resolution notes recorded.'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Financials & Labor Accounting */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <DollarSign className="w-4 h-4 text-emerald-400" />
              Cost & Labor Accounting
            </h2>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl">
                <p className="text-[11px] text-slate-400">Labor Hours</p>
                <p className="text-lg font-bold text-white mt-1 font-mono">
                  {workOrder.laborHours !== null && workOrder.laborHours !== undefined ? `${workOrder.laborHours}h` : '—'}
                </p>
              </div>
              <div className="p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl">
                <p className="text-[11px] text-slate-400">Labor Cost</p>
                <p className="text-lg font-bold text-slate-200 mt-1 font-mono">
                  {workOrder.laborCost !== null && workOrder.laborCost !== undefined ? `$${Number(workOrder.laborCost).toFixed(2)}` : '—'}
                </p>
              </div>
              <div className="p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl">
                <p className="text-[11px] text-slate-400">Parts Cost</p>
                <p className="text-lg font-bold text-slate-200 mt-1 font-mono">
                  {workOrder.partsCost !== null && workOrder.partsCost !== undefined ? `$${Number(workOrder.partsCost).toFixed(2)}` : '—'}
                </p>
              </div>
              <div className="p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl">
                <p className="text-[11px] text-emerald-400">Total Billed</p>
                <p className="text-lg font-bold text-emerald-300 mt-1 font-mono">
                  {workOrder.totalCost !== null && workOrder.totalCost !== undefined ? `$${Number(workOrder.totalCost).toFixed(2)}` : '—'}
                </p>
              </div>
            </div>
          </div>

          {/* Associated Downtime Logs */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                <AlertOctagon className="w-4 h-4 text-rose-400" />
                Equipment Downtime Incidents ({relatedDowntime.length})
              </h2>
              <button
                type="button"
                onClick={() => setIsRecordDowntimeOpen(true)}
                className="text-xs font-semibold text-rose-400 hover:text-rose-300 inline-flex items-center gap-1"
              >
                <AlertOctagon className="w-3 h-3" />
                <span>Log Downtime</span>
              </button>
            </div>

            {relatedDowntime.length === 0 ? (
              <p className="text-xs text-slate-500 italic">No equipment downtime intervals linked to this work order.</p>
            ) : (
              <div className="space-y-2.5">
                {relatedDowntime.map((dt) => (
                  <div
                    key={dt.id}
                    className="p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl flex items-center justify-between gap-4"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <DowntimeReasonBadge category={dt.reasonCategory} />
                        <span className="text-xs text-slate-400 font-mono">
                          {dt.durationMinutes ? `${dt.durationMinutes} mins` : dt.downtimeEnd ? 'Finished' : 'Ongoing'}
                        </span>
                      </div>
                      <p className="text-xs text-slate-300 mt-1">{dt.description}</p>
                    </div>

                    <div className="text-right text-[11px] font-mono text-slate-400">
                      {new Date(dt.downtimeStart).toLocaleDateString()}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Instrument & Request Connections */}
        <div className="space-y-6">
          {/* Equipment Connection */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white">Instrument Under Service</h2>
            {workOrder.equipmentId ? (
              <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl space-y-3">
                <div>
                  <p className="text-xs font-semibold text-white">{workOrder.equipmentName}</p>
                  <p className="text-[11px] text-slate-400 font-mono mt-0.5">ID #{workOrder.equipmentId}</p>
                </div>
                <Link
                  to={`/equipment/${workOrder.equipmentId}`}
                  className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-400 hover:text-indigo-300 hover:underline"
                >
                  <span>View Equipment Catalog Record</span>
                  <ExternalLink className="w-3 h-3" />
                </Link>
              </div>
            ) : (
              <p className="text-xs text-slate-500 italic">No instrument associated with this order.</p>
            )}
          </div>

          {/* Maintenance Request Connection */}
          {workOrder.maintenanceRequestId && (
            <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
              <h2 className="text-sm font-semibold text-white">Originating Request</h2>
              <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl space-y-3">
                <div>
                  <p className="text-xs font-semibold text-white font-mono">{workOrder.maintenanceRequestNumber}</p>
                  <p className="text-[11px] text-slate-400 font-mono mt-0.5">Request ID #{workOrder.maintenanceRequestId}</p>
                </div>
                <Link
                  to={`/maintenance/requests/${workOrder.maintenanceRequestId}`}
                  className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-400 hover:text-indigo-300 hover:underline"
                >
                  <span>Open Maintenance Request Record</span>
                  <ExternalLink className="w-3 h-3" />
                </Link>
              </div>
            </div>
          )}

          {/* Technician & Assignment Card */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4 text-xs">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-white">Technician Assignment</h2>
              {workOrder.status !== 'COMPLETED' && workOrder.status !== 'CANCELLED' && (
                <button
                  type="button"
                  onClick={() => setIsAssignOpen(true)}
                  className="text-xs font-semibold text-indigo-400 hover:underline"
                >
                  Change
                </button>
              )}
            </div>

            <div className="flex items-start gap-2.5 p-3.5 bg-slate-850/60 border border-slate-800 rounded-xl">
              <User className="w-4 h-4 text-indigo-400 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-white">{workOrder.assignedTechnicianName || 'Unassigned'}</p>
                {workOrder.assignedTechnicianId && (
                  <p className="text-[11px] text-slate-400 font-mono mt-0.5">
                    Technician User #{workOrder.assignedTechnicianId}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modals */}
      <AssignTechnicianModal
        isOpen={isAssignOpen}
        workOrder={workOrder}
        onClose={() => setIsAssignOpen(false)}
        onSuccess={(updated) => {
          setWorkOrder(updated);
          showToast(`Technician assigned to ${updated.workOrderNumber}.`);
        }}
      />

      <PauseWorkOrderModal
        isOpen={isPauseOpen}
        workOrder={workOrder}
        onClose={() => setIsPauseOpen(false)}
        onSuccess={(updated) => {
          setWorkOrder(updated);
          showToast(`Work order ${updated.workOrderNumber} paused.`);
        }}
      />

      <CompleteWorkOrderModal
        isOpen={isCompleteOpen}
        workOrder={workOrder}
        onClose={() => setIsCompleteOpen(false)}
        onSuccess={(updated) => {
          setWorkOrder(updated);
          showToast(`Work order ${updated.workOrderNumber} marked COMPLETED.`);
        }}
      />

      <CancelWorkOrderModal
        isOpen={isCancelOpen}
        workOrder={workOrder}
        onClose={() => setIsCancelOpen(false)}
        onSuccess={(updated) => {
          setWorkOrder(updated);
          showToast(`Work order ${updated.workOrderNumber} cancelled.`);
        }}
      />

      <RecordDowntimeModal
        isOpen={isRecordDowntimeOpen}
        initialEquipmentId={workOrder.equipmentId}
        initialWorkOrderId={workOrder.id}
        onClose={() => setIsRecordDowntimeOpen(false)}
        onSuccess={(created) => {
          setRelatedDowntime((prev) => [created, ...prev]);
          showToast(`Equipment downtime logged.`);
        }}
      />
    </div>
  );
};
