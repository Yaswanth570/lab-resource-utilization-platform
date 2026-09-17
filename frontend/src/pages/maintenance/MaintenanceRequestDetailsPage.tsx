import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Wrench,
  ArrowLeft,
  Calendar,
  User,
  Clock,
  ExternalLink,
  RefreshCw,
  AlertCircle,
  Hammer,
  CheckCircle2,
  XCircle,
  AlertTriangle,
} from 'lucide-react';
import type {
  MaintenanceRequestResponse,
  WorkOrderResponse,
} from '../../types/maintenance';
import {
  getMaintenanceRequestById,
  getWorkOrders,
} from '../../api/maintenance';
import { MaintenanceRequestStatusBadge } from '../../components/maintenance/MaintenanceRequestStatusBadge';
import { PriorityBadge } from '../../components/maintenance/PriorityBadge';
import { WorkOrderStatusBadge } from '../../components/maintenance/WorkOrderStatusBadge';

import { TriageRequestModal } from '../../components/maintenance/TriageRequestModal';
import { RejectRequestModal } from '../../components/maintenance/RejectRequestModal';
import { ResolveRequestModal } from '../../components/maintenance/ResolveRequestModal';
import { CreateWorkOrderModal } from '../../components/maintenance/CreateWorkOrderModal';

export const MaintenanceRequestDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [request, setRequest] = useState<MaintenanceRequestResponse | null>(null);
  const [relatedWorkOrders, setRelatedWorkOrders] = useState<WorkOrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  // Modals
  const [isTriageOpen, setIsTriageOpen] = useState<boolean>(false);
  const [isRejectOpen, setIsRejectOpen] = useState<boolean>(false);
  const [isResolveOpen, setIsResolveOpen] = useState<boolean>(false);
  const [isCreateWoOpen, setIsCreateWoOpen] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  const loadData = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const reqData = await getMaintenanceRequestById(id);
      setRequest(reqData);

      // Load related work orders for this request
      try {
        const woList = await getWorkOrders({ maintenanceRequestId: Number(id) });
        setRelatedWorkOrders(woList);
      } catch (err) {
        console.warn('Could not load related work orders:', err);
      }
    } catch (err: unknown) {
      console.error('Failed to load request details:', err);
      setErrorMessage('Could not load maintenance request. It may not exist or network is unavailable.');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (isLoading) {
    return (
      <div className="p-16 text-center bg-slate-900 border border-slate-800 rounded-2xl">
        <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
        <p className="text-xs text-slate-400 mt-3">Loading request details...</p>
      </div>
    );
  }

  if (errorMessage || !request) {
    return (
      <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
        <h2 className="text-base font-semibold text-white">Maintenance Request Not Found</h2>
        <p className="text-xs text-slate-400 max-w-md mx-auto">{errorMessage || 'Request could not be located.'}</p>
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

  return (
    <div className="space-y-6">
      {/* Toast */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-950/90 border border-emerald-500/40 text-emerald-200 rounded-xl shadow-2xl flex items-center gap-3 text-xs animate-in fade-in slide-in-from-top-3 duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successToast}</span>
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
              <h1 className="text-xl font-bold text-white font-mono">{request.requestNumber}</h1>
              <MaintenanceRequestStatusBadge status={request.status} />
              <PriorityBadge priority={request.priority} />
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Submitted {new Date(request.createdAt).toLocaleString()}
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          {request.status === 'SUBMITTED' && (
            <button
              type="button"
              onClick={() => setIsTriageOpen(true)}
              className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-xl transition-colors"
            >
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Triage Request</span>
            </button>
          )}

          {(request.status === 'SUBMITTED' || request.status === 'TRIAGED') && (
            <>
              <button
                type="button"
                onClick={() => setIsCreateWoOpen(true)}
                className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-all"
              >
                <Hammer className="w-3.5 h-3.5" />
                <span>Create Work Order</span>
              </button>
              <button
                type="button"
                onClick={() => setIsRejectOpen(true)}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-rose-400 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 rounded-xl transition-colors"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Reject</span>
              </button>
            </>
          )}

          {request.status !== 'RESOLVED' && request.status !== 'REJECTED' && (
            <button
              type="button"
              onClick={() => setIsResolveOpen(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-xl transition-colors"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Resolve</span>
            </button>
          )}
        </div>
      </div>

      {/* Main Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Issue & Description */}
        <div className="lg:col-span-2 space-y-6">
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <Wrench className="w-4 h-4 text-indigo-400" />
              Issue Summary
            </h2>
            <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl">
              <p className="text-sm font-semibold text-white">{request.issueTitle}</p>
              <p className="text-xs text-slate-300 whitespace-pre-wrap mt-2 leading-relaxed">
                {request.issueDescription}
              </p>
            </div>
          </div>

          {/* Associated Work Orders */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-white flex items-center gap-2">
                <Hammer className="w-4 h-4 text-indigo-400" />
                Associated Work Orders ({relatedWorkOrders.length})
              </h2>
              {(request.status === 'SUBMITTED' || request.status === 'TRIAGED') && (
                <button
                  type="button"
                  onClick={() => setIsCreateWoOpen(true)}
                  className="text-xs font-semibold text-indigo-400 hover:text-indigo-300 inline-flex items-center gap-1"
                >
                  <Hammer className="w-3 h-3" />
                  <span>New Work Order</span>
                </button>
              )}
            </div>

            {relatedWorkOrders.length === 0 ? (
              <p className="text-xs text-slate-500 italic">No work orders linked to this maintenance request yet.</p>
            ) : (
              <div className="space-y-2.5">
                {relatedWorkOrders.map((wo) => (
                  <div
                    key={wo.id}
                    className="p-3.5 bg-slate-850/60 border border-slate-800 hover:border-slate-700/80 rounded-xl flex items-center justify-between gap-4 transition-colors"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <Link
                          to={`/maintenance/work-orders/${wo.id}`}
                          className="font-mono text-xs font-semibold text-indigo-400 hover:underline"
                        >
                          {wo.workOrderNumber}
                        </Link>
                        <WorkOrderStatusBadge status={wo.status} />
                      </div>
                      <p className="text-xs text-slate-400 mt-1">
                        Type: <span className="text-slate-200">{wo.type}</span> • Technician:{' '}
                        <span className="text-slate-200">{wo.assignedTechnicianName || 'Unassigned'}</span>
                      </p>
                    </div>

                    <Link
                      to={`/maintenance/work-orders/${wo.id}`}
                      className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
                      title="View Work Order"
                    >
                      <ExternalLink className="w-4 h-4" />
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Metadata & Equipment */}
        <div className="space-y-6">
          {/* Equipment Reference Card */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
            <h2 className="text-sm font-semibold text-white">Target Equipment</h2>
            {request.equipmentId ? (
              <div className="p-4 bg-slate-850/60 border border-slate-800 rounded-xl space-y-3">
                <div>
                  <p className="text-xs font-semibold text-white">{request.equipmentName || 'Equipment Instrument'}</p>
                  <p className="text-[11px] text-slate-400 font-mono mt-0.5">ID #{request.equipmentId}</p>
                </div>
                <Link
                  to={`/equipment/${request.equipmentId}`}
                  className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-400 hover:text-indigo-300 hover:underline"
                >
                  <span>View Equipment Catalog Record</span>
                  <ExternalLink className="w-3 h-3" />
                </Link>
              </div>
            ) : (
              <p className="text-xs text-slate-500 italic">No equipment associated with this record.</p>
            )}
          </div>

          {/* Audit & Triage Trail */}
          <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4 text-xs">
            <h2 className="text-sm font-semibold text-white">Audit & Review Trail</h2>
            <div className="space-y-3 text-slate-300">
              <div className="flex items-start gap-2.5">
                <User className="w-4 h-4 text-slate-500 shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-slate-200">Reported By</p>
                  <p className="text-slate-400">{request.reportedByUserName || 'System User'}</p>
                  {request.reportedByUserId && (
                    <p className="text-[11px] text-slate-500 font-mono">User ID #{request.reportedByUserId}</p>
                  )}
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <Clock className="w-4 h-4 text-slate-500 shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-slate-200">Submission Timestamp</p>
                  <p className="text-slate-400 font-mono">{new Date(request.createdAt).toLocaleString()}</p>
                </div>
              </div>

              {request.triagedByUserName && (
                <div className="flex items-start gap-2.5 pt-2 border-t border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold text-cyan-300">Triaged By</p>
                    <p className="text-slate-300">{request.triagedByUserName}</p>
                    {request.triagedAt && (
                      <p className="text-[11px] text-slate-500 font-mono">
                        {new Date(request.triagedAt).toLocaleString()}
                      </p>
                    )}
                  </div>
                </div>
              )}

              <div className="flex items-start gap-2.5 pt-2 border-t border-slate-800">
                <Calendar className="w-4 h-4 text-slate-500 shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-slate-200">Last Modified</p>
                  <p className="text-slate-400 font-mono">{new Date(request.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modals */}
      <TriageRequestModal
        isOpen={isTriageOpen}
        request={request}
        onClose={() => setIsTriageOpen(false)}
        onSuccess={(updated) => {
          setRequest(updated);
          showToast(`Request ${updated.requestNumber} triaged.`);
        }}
      />

      <RejectRequestModal
        isOpen={isRejectOpen}
        request={request}
        onClose={() => setIsRejectOpen(false)}
        onSuccess={(updated) => {
          setRequest(updated);
          showToast(`Request ${updated.requestNumber} rejected.`);
        }}
      />

      <ResolveRequestModal
        isOpen={isResolveOpen}
        request={request}
        onClose={() => setIsResolveOpen(false)}
        onSuccess={(updated) => {
          setRequest(updated);
          showToast(`Request ${updated.requestNumber} marked resolved.`);
        }}
      />

      <CreateWorkOrderModal
        isOpen={isCreateWoOpen}
        initialRequest={request}
        onClose={() => setIsCreateWoOpen(false)}
        onSuccess={(created) => {
          setRelatedWorkOrders((prev) => [created, ...prev]);
          showToast(`Work Order ${created.workOrderNumber} created.`);
          loadData();
        }}
      />
    </div>
  );
};
