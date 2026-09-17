import React, { useState, useEffect } from 'react';
import { X, UserCheck, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type { WorkOrderResponse, AssignTechnicianDto } from '../../types/maintenance';
import type { UserProfileResponse } from '../../types/auth';
import { assignTechnician, getDepartmentUsers, getInstitutionUsers } from '../../api/maintenance';
import { useAuth } from '../../context/useAuth';

interface AssignTechnicianModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updated: WorkOrderResponse) => void;
  workOrder: WorkOrderResponse | null;
}

export const AssignTechnicianModal: React.FC<AssignTechnicianModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  workOrder,
}) => {
  const { user } = useAuth();
  const [users, setUsers] = useState<UserProfileResponse[]>([]);
  const [loadingUsers, setLoadingUsers] = useState<boolean>(false);
  const [selectedUserId, setSelectedUserId] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && workOrder) {
      setErrorMessage(null);
      setSelectedUserId(workOrder.assignedTechnicianId ? String(workOrder.assignedTechnicianId) : '');
      setLoadingUsers(true);

      const fetchPromise = user?.departmentId
        ? getDepartmentUsers(user.departmentId)
        : user?.institutionId
        ? getInstitutionUsers(user.institutionId)
        : Promise.resolve([]);

      fetchPromise
        .then((list) => {
          setUsers(list);
          if (!workOrder.assignedTechnicianId && list.length > 0) {
            setSelectedUserId(String(list[0].id));
          }
        })
        .catch((err) => {
          console.error('Failed to load technician users:', err);
          setErrorMessage('Could not load user list for assignment.');
        })
        .finally(() => {
          setLoadingUsers(false);
        });
    }
  }, [isOpen, workOrder, user]);

  if (!isOpen || !workOrder) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!selectedUserId) {
      setErrorMessage('Please select a technician.');
      return;
    }

    setIsSubmitting(true);

    try {
      const payload: AssignTechnicianDto = {
        technicianId: Number(selectedUserId),
      };

      const updated = await assignTechnician(workOrder.id, payload);
      onSuccess(updated);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to assign technician:', err);
      if (axios.isAxiosError(err)) {
        const backendMessage =
          err.response?.data?.message ||
          err.response?.data?.error ||
          (typeof err.response?.data === 'string' ? err.response?.data : null) ||
          `Assignment failed with HTTP ${err.response?.status}`;
        setErrorMessage(backendMessage);
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to assign technician.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/90">
          <div className="flex items-center gap-2 text-indigo-400">
            <UserCheck className="w-5 h-5" />
            <h2 className="text-base font-semibold text-white">Assign Technician</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error Alert */}
        {errorMessage && (
          <div className="mx-6 mt-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-start gap-2 text-xs text-rose-300">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/50 space-y-1 text-xs text-slate-300">
            <div>Work Order: <strong className="text-white">{workOrder.workOrderNumber}</strong></div>
            <div>Equipment: <span className="text-slate-200">{workOrder.equipmentName || `ID ${workOrder.equipmentId}`}</span></div>
            <div>Current Technician: <span className="text-indigo-300">{workOrder.assignedTechnicianName || 'Unassigned'}</span></div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Select Qualified Technician <span className="text-rose-400">*</span>
            </label>
            <select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              disabled={loadingUsers || isSubmitting}
              className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
            >
              {loadingUsers ? (
                <option value="">Loading users...</option>
              ) : (
                users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.firstName} {u.lastName} ({u.email})
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
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
              disabled={isSubmitting || loadingUsers}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-colors disabled:opacity-50"
            >
              {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>{isSubmitting ? 'Assigning...' : 'Assign Technician'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
