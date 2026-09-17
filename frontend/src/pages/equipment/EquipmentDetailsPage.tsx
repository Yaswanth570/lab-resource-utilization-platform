import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Edit,
  RefreshCw,
  Plus,
  Trash2,
  DollarSign,
  Clock,
  Globe,
  Lock,
  AlertCircle,
  FileText,
  UserCheck,
  Sparkles,
} from 'lucide-react';
import type {
  EquipmentResponse,
  EquipmentSpecificationResponse,
  QualificationResponse,
} from '../../types/equipment';
import {
  getEquipmentById,
  getSpecifications,
  deleteSpecification,
  getQualifications,
} from '../../api/equipment';
import { EquipmentStatusBadge } from '../../components/equipment/EquipmentStatusBadge';
import { EquipmentFormModal } from '../../components/equipment/EquipmentFormModal';
import { StatusTransitionModal } from '../../components/equipment/StatusTransitionModal';
import { SpecificationModal } from '../../components/equipment/SpecificationModal';
import { useAuth } from '../../context/useAuth';
import { canManageEquipment, canUpdateEquipmentStatus } from '../../utils/rbac';

export const EquipmentDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [equipment, setEquipment] = useState<EquipmentResponse | null>(null);
  const [specifications, setSpecifications] = useState<EquipmentSpecificationResponse[]>([]);
  const [qualifications, setQualifications] = useState<QualificationResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Modals
  const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState<boolean>(false);
  const [isSpecModalOpen, setIsSpecModalOpen] = useState<boolean>(false);
  const [specToEdit, setSpecToEdit] = useState<EquipmentSpecificationResponse | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const loadData = useCallback(async () => {
    if (!id) return;
    setError(null);
    try {
      const [equipData, specsData, qualsData] = await Promise.all([
        getEquipmentById(id),
        getSpecifications(id).catch(() => []),
        getQualifications(id).catch(() => []),
      ]);
      setEquipment(equipData);
      setSpecifications(specsData);
      setQualifications(qualsData);
    } catch (err: unknown) {
      console.error('Failed to load equipment details:', err);
      setError('Equipment not found or failed to communicate with backend service.');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleDeleteSpec = async (specId: number) => {
    if (!confirm('Are you sure you want to delete this technical specification?')) return;
    try {
      await deleteSpecification(specId);
      setSpecifications((prev) => prev.filter((s) => s.id !== specId));
      showToast('Specification deleted successfully.');
    } catch (err: unknown) {
      console.error('Failed to delete specification:', err);
      alert('Failed to delete specification.');
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="p-8 rounded-2xl bg-slate-900 border border-slate-800 animate-pulse space-y-4">
          <div className="w-32 h-4 rounded-sm bg-slate-800" />
          <div className="w-64 h-8 rounded-sm bg-slate-800" />
          <div className="w-48 h-4 rounded-sm bg-slate-800/60" />
        </div>
      </div>
    );
  }

  if (error || !equipment) {
    return (
      <div className="max-w-xl mx-auto p-8 rounded-2xl bg-slate-900 border border-slate-800 text-center space-y-4">
        <AlertCircle className="w-12 h-12 text-rose-400 mx-auto" />
        <h2 className="text-lg font-bold text-white">Equipment Error</h2>
        <p className="text-xs text-slate-400">{error || 'Unable to locate equipment record.'}</p>
        <button
          type="button"
          onClick={() => navigate('/equipment')}
          className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-slate-800 hover:bg-slate-700 rounded-xl"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Equipment Catalog</span>
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Toast */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 bg-emerald-500/90 backdrop-blur-md text-white rounded-xl shadow-2xl border border-emerald-400/40 text-xs font-medium animate-in fade-in slide-in-from-bottom-5">
          <Sparkles className="w-4 h-4 text-emerald-200" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Navigation Breadcrumbs */}
      <div className="flex items-center gap-2 text-xs text-slate-400">
        <Link to="/equipment" className="hover:text-white flex items-center gap-1">
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Equipment Catalog</span>
        </Link>
        <span>/</span>
        <span className="text-slate-200 font-mono">{equipment.assetTag}</span>
      </div>

      {/* Main Header Dossier Card */}
      <div className="p-6 sm:p-8 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-6">
        <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                {equipment.name}
              </h1>
              <EquipmentStatusBadge status={equipment.status} />
            </div>

            <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
              <span className="font-mono bg-slate-950 px-2 py-0.5 rounded-md border border-slate-800 text-slate-300">
                Asset: {equipment.assetTag}
              </span>
              <span>•</span>
              <span>Serial: {equipment.serialNumber}</span>
              {equipment.modelNumber && (
                <>
                  <span>•</span>
                  <span>Model: {equipment.modelNumber}</span>
                </>
              )}
              {equipment.manufacturer && (
                <>
                  <span>•</span>
                  <span>Mfr: {equipment.manufacturer}</span>
                </>
              )}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5 shrink-0">
            {canUpdateEquipmentStatus(user?.roles) && (
              <button
                type="button"
                onClick={() => setIsStatusModalOpen(true)}
                className="flex items-center gap-2 px-3.5 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Change Status</span>
              </button>
            )}

            {canManageEquipment(user?.roles) && (
              <button
                type="button"
                onClick={() => setIsEditModalOpen(true)}
                className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl shadow-lg shadow-sky-600/20 transition-all"
              >
                <Edit className="w-3.5 h-3.5" />
                <span>Edit Instrument</span>
              </button>
            )}
          </div>
        </div>

        {/* Operational Status Reason Alert */}
        {equipment.operationalStatusReason && (
          <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs flex items-start gap-2.5">
            <AlertCircle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
            <div>
              <strong className="font-semibold">Operational Status Note:</strong>{' '}
              {equipment.operationalStatusReason}
            </div>
          </div>
        )}

        {/* Meta summary badges */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 border-t border-slate-800/80 text-xs">
          <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[11px] text-slate-500">Category</div>
            <div className="font-semibold text-slate-200 mt-0.5">
              {equipment.categoryName || 'General'}
            </div>
          </div>

          <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[11px] text-slate-500">Department / Inst</div>
            <div className="font-semibold text-slate-200 mt-0.5">
              Dept ID: {equipment.departmentId} (Inst: {equipment.institutionId})
            </div>
          </div>

          <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[11px] text-slate-500">Location</div>
            <div className="font-semibold text-slate-200 mt-0.5">
              {equipment.locationBuilding}, Rm {equipment.locationRoom}
            </div>
          </div>

          <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[11px] text-slate-500">Primary Manager</div>
            <div className="font-semibold text-slate-200 mt-0.5">
              {equipment.primaryLabManagerName || 'Unassigned'}
            </div>
          </div>
        </div>
      </div>

      {/* Grid: Operational Rules & Rates */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Booking & Operational Policy */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-sm font-semibold text-white border-b border-slate-800 pb-3">
            <Clock className="w-4 h-4 text-sky-400" />
            <span>Booking & Operating Policy</span>
          </div>

          <dl className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Min Duration</dt>
              <dd className="font-medium text-slate-200">
                {equipment.minBookingDurationMins ? `${equipment.minBookingDurationMins} mins` : '—'}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Max Duration</dt>
              <dd className="font-medium text-slate-200">
                {equipment.maxBookingDurationMins ? `${equipment.maxBookingDurationMins} mins` : '—'}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Buffer / Turnaround</dt>
              <dd className="font-medium text-slate-200">
                {equipment.bufferTimeMins ? `${equipment.bufferTimeMins} mins` : '—'}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Approval Required</dt>
              <dd className="font-medium">
                {equipment.requiresApproval ? (
                  <span className="text-amber-400">Manager approval required</span>
                ) : (
                  <span className="text-slate-400">Direct booking</span>
                )}
              </dd>
            </div>
            <div className="flex justify-between py-1">
              <dt className="text-slate-400">Training Certification</dt>
              <dd className="font-medium">
                {equipment.requiresTrainingCertification ? (
                  <span className="text-emerald-400">Certified users only</span>
                ) : (
                  <span className="text-slate-400">Open qualification</span>
                )}
              </dd>
            </div>
          </dl>
        </div>

        {/* Financial & Rates */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-sm font-semibold text-white border-b border-slate-800 pb-3">
            <DollarSign className="w-4 h-4 text-emerald-400" />
            <span>Financial & Rate Configuration</span>
          </div>

          <dl className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Internal Rate</dt>
              <dd className="font-semibold text-emerald-400">
                {equipment.hourlyRateInternal !== null ? `$${equipment.hourlyRateInternal} / hr` : 'Free of charge'}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">External Rate</dt>
              <dd className="font-semibold text-slate-200">
                {equipment.hourlyRateExternal !== null ? `$${equipment.hourlyRateExternal} / hr` : '—'}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">External Sharing Status</dt>
              <dd className="font-medium">
                {equipment.isShareableExternally ? (
                  <span className="inline-flex items-center gap-1 text-emerald-400">
                    <Globe className="w-3.5 h-3.5" /> Available for inter-institutional sharing
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-slate-400">
                    <Lock className="w-3.5 h-3.5" /> Restricted to host institution
                  </span>
                )}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Purchase Cost</dt>
              <dd className="font-medium text-slate-200">
                {equipment.purchaseCost !== null ? `$${equipment.purchaseCost}` : '—'}
              </dd>
            </div>
            <div className="flex justify-between py-1">
              <dt className="text-slate-400">Warranty Expiration</dt>
              <dd className="font-medium text-slate-200">
                {equipment.warrantyExpiryDate ? new Date(equipment.warrantyExpiryDate).toLocaleDateString() : '—'}
              </dd>
            </div>
          </dl>
        </div>
      </div>

      {/* Technical Specifications Section */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-md space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <FileText className="w-4 h-4 text-sky-400" />
            <h3 className="text-sm font-bold text-white">Technical Specifications</h3>
            <span className="px-2 py-0.5 text-[10px] font-semibold rounded-md bg-slate-800 text-slate-400">
              {specifications.length}
            </span>
          </div>
          {canManageEquipment(user?.roles) && (
            <button
              type="button"
              onClick={() => {
                setSpecToEdit(null);
                setIsSpecModalOpen(true);
              }}
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-lg transition-colors shadow-xs"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Specification</span>
            </button>
          )}
        </div>

        {specifications.length === 0 ? (
          <p className="text-xs text-slate-500 py-4 text-center">
            No technical specifications logged for this instrument yet.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <tr>
                  <th className="py-2.5 px-3">Parameter Name</th>
                  <th className="py-2.5 px-3">Value</th>
                  <th className="py-2.5 px-3">Unit</th>
                  <th className="py-2.5 px-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {specifications.map((sp) => (
                  <tr key={sp.id} className="hover:bg-slate-800/30">
                    <td className="py-2.5 px-3 font-medium text-white">{sp.specName}</td>
                    <td className="py-2.5 px-3 text-slate-200">{sp.specValue}</td>
                    <td className="py-2.5 px-3 text-slate-400">{sp.unit || '—'}</td>
                    <td className="py-2.5 px-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => {
                            setSpecToEdit(sp);
                            setIsSpecModalOpen(true);
                          }}
                          className="p-1 text-slate-400 hover:text-sky-400 rounded-md hover:bg-slate-800 transition-colors"
                          title="Edit specification"
                        >
                          <Edit className="w-3.5 h-3.5" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDeleteSpec(sp.id)}
                          className="p-1 text-slate-400 hover:text-rose-400 rounded-md hover:bg-slate-800 transition-colors"
                          title="Delete specification"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* User Training Qualifications Section (Read-Only) */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-md space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <UserCheck className="w-4 h-4 text-emerald-400" />
            <h3 className="text-sm font-bold text-white">Certified User Qualifications</h3>
            <span className="px-2 py-0.5 text-[10px] font-semibold rounded-md bg-slate-800 text-slate-400">
              {qualifications.length}
            </span>
          </div>
          <span className="text-[11px] text-slate-500">Read-only institutional records</span>
        </div>

        {qualifications.length === 0 ? (
          <p className="text-xs text-slate-500 py-4 text-center">
            No training qualification records active for this equipment.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <tr>
                  <th className="py-2.5 px-3">Certified User</th>
                  <th className="py-2.5 px-3">Email</th>
                  <th className="py-2.5 px-3">Certified Date</th>
                  <th className="py-2.5 px-3">Expiration Date</th>
                  <th className="py-2.5 px-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {qualifications.map((q) => (
                  <tr key={q.id} className="hover:bg-slate-800/30">
                    <td className="py-2.5 px-3 font-medium text-white">{q.userName}</td>
                    <td className="py-2.5 px-3 text-slate-400">{q.userEmail}</td>
                    <td className="py-2.5 px-3 text-slate-300">
                      {q.certifiedAt ? new Date(q.certifiedAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="py-2.5 px-3 text-slate-300">
                      {q.expiresAt ? new Date(q.expiresAt).toLocaleDateString() : 'Lifetime / No Expiry'}
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="px-2 py-0.5 text-[10px] font-medium rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {q.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal: Edit Equipment */}
      {isEditModalOpen && (
        <EquipmentFormModal
          isOpen={isEditModalOpen}
          equipmentToEdit={equipment}
          onClose={() => setIsEditModalOpen(false)}
          onSuccess={(updated) => {
            setEquipment(updated);
            showToast(`Instrument "${updated.assetTag}" updated successfully.`);
          }}
        />
      )}

      {/* Modal: Status Transition */}
      {isStatusModalOpen && (
        <StatusTransitionModal
          isOpen={isStatusModalOpen}
          equipment={equipment}
          onClose={() => setIsStatusModalOpen(false)}
          onSuccess={(updated) => {
            setEquipment(updated);
            showToast(`Status updated to ${updated.status}.`);
          }}
        />
      )}

      {/* Modal: Specification */}
      {isSpecModalOpen && (
        <SpecificationModal
          isOpen={isSpecModalOpen}
          equipmentId={equipment.id}
          specToEdit={specToEdit}
          onClose={() => {
            setIsSpecModalOpen(false);
            setSpecToEdit(null);
          }}
          onSuccess={(saved) => {
            if (specToEdit) {
              setSpecifications((prev) => prev.map((s) => (s.id === saved.id ? saved : s)));
              showToast('Specification updated successfully.');
            } else {
              setSpecifications((prev) => [...prev, saved]);
              showToast('Specification added successfully.');
            }
          }}
        />
      )}
    </div>
  );
};
