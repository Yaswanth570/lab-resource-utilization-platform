import React, { useState, useEffect } from 'react';
import { X, Hammer, Loader2, AlertCircle, Calendar } from 'lucide-react';
import axios from 'axios';
import type {
  WorkOrderResponse,
  CreateWorkOrderDto,
  WorkOrderType,
  MaintenancePriority,
  MaintenanceRequestResponse,
} from '../../types/maintenance';
import type { EquipmentResponse } from '../../types/equipment';
import { getEquipmentList } from '../../api/equipment';
import { createWorkOrder, getDepartmentUsers } from '../../api/maintenance';
import { useAuth } from '../../context/useAuth';
import type { UserProfileResponse } from '../../types/auth';

interface CreateWorkOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: WorkOrderResponse) => void;
  initialEquipmentId?: number;
  initialRequest?: MaintenanceRequestResponse | null;
}

function getInitialDateTimeLocal(offsetHours = 0): string {
  const d = new Date(Date.now() + offsetHours * 3600 * 1000);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

export const CreateWorkOrderModal: React.FC<CreateWorkOrderModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  initialEquipmentId,
  initialRequest,
}) => {
  const { user } = useAuth();
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [technicians, setTechnicians] = useState<UserProfileResponse[]>([]);
  const [loadingLookups, setLoadingLookups] = useState<boolean>(false);

  const [equipmentId, setEquipmentId] = useState<string>('');
  const [type, setType] = useState<WorkOrderType>('CORRECTIVE');
  const [priority, setPriority] = useState<MaintenancePriority>('MEDIUM');
  const [scheduledStart, setScheduledStart] = useState<string>(() => getInitialDateTimeLocal(1));
  const [scheduledEnd, setScheduledEnd] = useState<string>(() => getInitialDateTimeLocal(4));
  const [assignedTechnicianId, setAssignedTechnicianId] = useState<string>('');
  const [workOrderNumber, setWorkOrderNumber] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setLoadingLookups(true);

      const eqPromise = getEquipmentList();
      const techPromise = user?.departmentId ? getDepartmentUsers(user.departmentId) : Promise.resolve([]);

      Promise.allSettled([eqPromise, techPromise])
        .then(([eqRes, techRes]) => {
          if (eqRes.status === 'fulfilled') {
            setEquipmentList(eqRes.value);
            if (initialRequest?.equipmentId) {
              setEquipmentId(String(initialRequest.equipmentId));
            } else if (initialEquipmentId) {
              setEquipmentId(String(initialEquipmentId));
            } else if (eqRes.value.length > 0) {
              setEquipmentId(String(eqRes.value[0].id));
            }
          }
          if (techRes.status === 'fulfilled') {
            setTechnicians(techRes.value);
          }
          if (initialRequest) {
            setPriority(initialRequest.priority || 'MEDIUM');
            setType('CORRECTIVE');
          }
        })
        .finally(() => {
          setLoadingLookups(false);
        });
    }
  }, [isOpen, initialEquipmentId, initialRequest, user]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!equipmentId) {
      setErrorMessage('Please select an instrument / equipment.');
      return;
    }
    if (!scheduledStart || !scheduledEnd) {
      setErrorMessage('Scheduled start and end dates are required.');
      return;
    }

    const startInstant = new Date(scheduledStart).toISOString();
    const endInstant = new Date(scheduledEnd).toISOString();

    if (new Date(scheduledEnd).getTime() <= new Date(scheduledStart).getTime()) {
      setErrorMessage('Scheduled end time must be after scheduled start time.');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload: CreateWorkOrderDto = {
        equipmentId: Number(equipmentId),
        maintenanceRequestId: initialRequest ? initialRequest.id : undefined,
        assignedTechnicianId: assignedTechnicianId ? Number(assignedTechnicianId) : undefined,
        type,
        priority,
        scheduledStart: startInstant,
        scheduledEnd: endInstant,
        workOrderNumber: workOrderNumber.trim() || undefined,
      };

      const created = await createWorkOrder(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to create work order:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Failed to create work order (HTTP ${err.response?.status})`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to create work order.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90 shrink-0">
          <div className="flex items-center gap-2 text-indigo-400">
            <Hammer className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Create Work Order</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Form Body */}
        <div className="overflow-y-auto flex-1 p-6 space-y-4">
          {/* Linked Request banner */}
          {initialRequest && (
            <div className="p-3 bg-indigo-500/10 border border-indigo-500/30 rounded-xl space-y-1 text-xs text-indigo-300">
              <div>Linking to Request: <strong className="text-white">{initialRequest.requestNumber}</strong></div>
              <div>Issue: <span className="text-slate-200">{initialRequest.issueTitle}</span></div>
            </div>
          )}

          {/* Error Alert */}
          {errorMessage && (
            <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form id="create-work-order-form" onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Equipment / Instrument <span className="text-rose-400">*</span>
              </label>
              <select
                value={equipmentId}
                onChange={(e) => setEquipmentId(e.target.value)}
                disabled={loadingLookups || isSubmitting || !!initialRequest}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 disabled:opacity-60"
              >
                {loadingLookups ? (
                  <option value="">Loading equipment catalog...</option>
                ) : (
                  equipmentList.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.name} ({eq.assetTag}) — {eq.status}
                    </option>
                  ))
                )}
              </select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Work Order Type <span className="text-rose-400">*</span>
                </label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as WorkOrderType)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="CORRECTIVE">Corrective Repair</option>
                  <option value="PREVENTIVE">Preventive Maintenance</option>
                  <option value="EMERGENCY">Emergency Service</option>
                  <option value="OVERHAUL">Major Overhaul</option>
                  <option value="DECOMMISSION">Decommission Service</option>
                </select>
              </div>
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Priority Level <span className="text-rose-400">*</span>
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as MaintenancePriority)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  Scheduled Start <span className="text-rose-400">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={scheduledStart}
                  onChange={(e) => setScheduledStart(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  Scheduled End <span className="text-rose-400">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={scheduledEnd}
                  onChange={(e) => setScheduledEnd(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Assign Technician (Optional)
                </label>
                <select
                  value={assignedTechnicianId}
                  onChange={(e) => setAssignedTechnicianId(e.target.value)}
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="">-- Unassigned --</option>
                  {technicians.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.firstName} {t.lastName} ({t.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Custom WO # (Optional)
                </label>
                <input
                  type="text"
                  value={workOrderNumber}
                  onChange={(e) => setWorkOrderNumber(e.target.value)}
                  placeholder="Auto-generated if blank"
                  disabled={isSubmitting}
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>
          </form>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-800 bg-slate-900/90 shrink-0">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            form="create-work-order-form"
            disabled={isSubmitting || loadingLookups}
            className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-colors disabled:opacity-50"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            <span>{isSubmitting ? 'Creating...' : 'Create Work Order'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
