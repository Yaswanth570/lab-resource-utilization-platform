import React, { useState, useEffect } from 'react';
import { X, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type {
  EquipmentResponse,
  CreateEquipmentRequest,
  UpdateEquipmentRequest,
  EquipmentCategoryResponse,
  DepartmentLookup,
  InstitutionLookup,
} from '../../types/equipment';
import {
  createEquipment,
  updateEquipment,
  getCategories,
  getInstitutions,
  getDepartmentsByInstitution,
} from '../../api/equipment';
import { useAuth } from '../../context/useAuth';

interface EquipmentFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (saved: EquipmentResponse) => void;
  equipmentToEdit?: EquipmentResponse | null;
}

export const EquipmentFormModal: React.FC<EquipmentFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  equipmentToEdit,
}) => {
  const { user } = useAuth();
  const isEditing = Boolean(equipmentToEdit);

  // Reference data
  const [categories, setCategories] = useState<EquipmentCategoryResponse[]>([]);
  const [institutions, setInstitutions] = useState<InstitutionLookup[]>([]);
  const [departments, setDepartments] = useState<DepartmentLookup[]>([]);
  const [isLoadingRefData, setIsLoadingRefData] = useState<boolean>(false);

  // Form State
  const [institutionId, setInstitutionId] = useState<number>(user?.institutionId || 1);
  const [departmentId, setDepartmentId] = useState<number>(user?.departmentId || 1);
  const [categoryId, setCategoryId] = useState<number>(1);
  const [name, setName] = useState<string>('');
  const [assetTag, setAssetTag] = useState<string>('');
  const [serialNumber, setSerialNumber] = useState<string>('');
  const [modelNumber, setModelNumber] = useState<string>('');
  const [manufacturer, setManufacturer] = useState<string>('');
  const [locationBuilding, setLocationBuilding] = useState<string>('');
  const [locationRoom, setLocationRoom] = useState<string>('');

  // Flags & Rates
  const [shareableExternally, setShareableExternally] = useState<boolean>(false);
  const [requiresApproval, setRequiresApproval] = useState<boolean>(false);
  const [requiresTrainingCertification, setRequiresTrainingCertification] = useState<boolean>(false);
  const [hourlyRateInternal, setHourlyRateInternal] = useState<string>('');
  const [hourlyRateExternal, setHourlyRateExternal] = useState<string>('');
  const [minBookingDurationMins, setMinBookingDurationMins] = useState<string>('30');
  const [maxBookingDurationMins, setMaxBookingDurationMins] = useState<string>('480');
  const [bufferTimeMins, setBufferTimeMins] = useState<string>('15');
  const [purchaseCost, setPurchaseCost] = useState<string>('');
  const [purchaseDate, setPurchaseDate] = useState<string>('');
  const [warrantyExpiryDate, setWarrantyExpiryDate] = useState<string>('');

  // UI state
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  // Fetch categories & institutions/departments on mount
  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;

    Promise.all([
      getCategories().catch(() => []),
      getInstitutions().catch(() => []),
    ]).then(([cats, insts]) => {
      if (!isMounted) return;
      setCategories(cats);
      setInstitutions(insts);
      if (cats.length > 0 && !equipmentToEdit) {
        setCategoryId(cats[0].id);
      }
      setIsLoadingRefData(false);
    });

    return () => {
      isMounted = false;
    };
  }, [isOpen, equipmentToEdit]);

  // Fetch departments when institution changes
  useEffect(() => {
    if (!isOpen || !institutionId) return;

    let isMounted = true;
    getDepartmentsByInstitution(institutionId)
      .then((depts) => {
        if (!isMounted) return;
        setDepartments(depts);
        if (depts.length > 0 && !equipmentToEdit) {
          setDepartmentId(depts[0].id);
        }
      })
      .catch(() => {
        if (isMounted) setDepartments([]);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, institutionId, equipmentToEdit]);

  // Populate or reset form fields
  useEffect(() => {
    if (!isOpen) return;

    if (equipmentToEdit) {
      setInstitutionId(equipmentToEdit.institutionId);
      setDepartmentId(equipmentToEdit.departmentId);
      setCategoryId(equipmentToEdit.categoryId);
      setName(equipmentToEdit.name || '');
      setAssetTag(equipmentToEdit.assetTag || '');
      setSerialNumber(equipmentToEdit.serialNumber || '');
      setModelNumber(equipmentToEdit.modelNumber || '');
      setManufacturer(equipmentToEdit.manufacturer || '');
      setLocationBuilding(equipmentToEdit.locationBuilding || '');
      setLocationRoom(equipmentToEdit.locationRoom || '');
      setShareableExternally(equipmentToEdit.isShareableExternally);
      setRequiresApproval(equipmentToEdit.requiresApproval);
      setRequiresTrainingCertification(equipmentToEdit.requiresTrainingCertification);
      setHourlyRateInternal(
        equipmentToEdit.hourlyRateInternal !== null ? String(equipmentToEdit.hourlyRateInternal) : ''
      );
      setHourlyRateExternal(
        equipmentToEdit.hourlyRateExternal !== null ? String(equipmentToEdit.hourlyRateExternal) : ''
      );
      setMinBookingDurationMins(
        equipmentToEdit.minBookingDurationMins !== null
          ? String(equipmentToEdit.minBookingDurationMins)
          : '30'
      );
      setMaxBookingDurationMins(
        equipmentToEdit.maxBookingDurationMins !== null
          ? String(equipmentToEdit.maxBookingDurationMins)
          : '480'
      );
      setBufferTimeMins(
        equipmentToEdit.bufferTimeMins !== null ? String(equipmentToEdit.bufferTimeMins) : '15'
      );
      setPurchaseCost(
        equipmentToEdit.purchaseCost !== null ? String(equipmentToEdit.purchaseCost) : ''
      );
      setPurchaseDate(equipmentToEdit.purchaseDate || '');
      setWarrantyExpiryDate(equipmentToEdit.warrantyExpiryDate || '');
    } else {
      // Defaults for create
      setInstitutionId(user?.institutionId || 1);
      setDepartmentId(user?.departmentId || 1);
      setName('');
      setAssetTag('');
      setSerialNumber('');
      setModelNumber('');
      setManufacturer('');
      setLocationBuilding('');
      setLocationRoom('');
      setShareableExternally(false);
      setRequiresApproval(false);
      setRequiresTrainingCertification(false);
      setHourlyRateInternal('');
      setHourlyRateExternal('');
      setMinBookingDurationMins('30');
      setMaxBookingDurationMins('480');
      setBufferTimeMins('15');
      setPurchaseCost('');
      setPurchaseDate('');
      setWarrantyExpiryDate('');
    }
  }, [isOpen, equipmentToEdit, user]);

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!name.trim()) errors.name = 'Equipment name is required';
    if (!isEditing) {
      if (!assetTag.trim()) errors.assetTag = 'Asset tag is required';
      if (!serialNumber.trim()) errors.serialNumber = 'Serial number is required';
    }
    if (!locationBuilding.trim()) errors.locationBuilding = 'Building is required';
    if (!locationRoom.trim()) errors.locationRoom = 'Room is required';
    if (!categoryId) errors.categoryId = 'Category selection is required';
    if (!departmentId) errors.departmentId = 'Department selection is required';

    // Non-negative numeric validations
    if (hourlyRateInternal !== '' && Number(hourlyRateInternal) < 0) {
      errors.hourlyRateInternal = 'Hourly rate cannot be negative';
    }
    if (hourlyRateExternal !== '' && Number(hourlyRateExternal) < 0) {
      errors.hourlyRateExternal = 'External rate cannot be negative';
    }
    if (minBookingDurationMins !== '' && Number(minBookingDurationMins) < 1) {
      errors.minBookingDurationMins = 'Minimum duration must be at least 1 minute';
    }
    if (
      maxBookingDurationMins !== '' &&
      minBookingDurationMins !== '' &&
      Number(maxBookingDurationMins) < Number(minBookingDurationMins)
    ) {
      errors.maxBookingDurationMins = 'Max duration cannot be less than min duration';
    }
    if (bufferTimeMins !== '' && Number(bufferTimeMins) < 0) {
      errors.bufferTimeMins = 'Buffer time cannot be negative';
    }
    if (purchaseCost !== '' && Number(purchaseCost) < 0) {
      errors.purchaseCost = 'Purchase cost cannot be negative';
    }

    setClientErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      if (isEditing && equipmentToEdit) {
        const payload: UpdateEquipmentRequest = {
          departmentId: Number(departmentId),
          categoryId: Number(categoryId),
          name: name.trim(),
          modelNumber: modelNumber.trim() || null,
          manufacturer: manufacturer.trim() || null,
          locationBuilding: locationBuilding.trim(),
          locationRoom: locationRoom.trim(),
          shareableExternally,
          requiresApproval,
          requiresTrainingCertification,
          hourlyRateInternal: hourlyRateInternal !== '' ? Number(hourlyRateInternal) : null,
          hourlyRateExternal: hourlyRateExternal !== '' ? Number(hourlyRateExternal) : null,
          minBookingDurationMins:
            minBookingDurationMins !== '' ? Number(minBookingDurationMins) : null,
          maxBookingDurationMins:
            maxBookingDurationMins !== '' ? Number(maxBookingDurationMins) : null,
          bufferTimeMins: bufferTimeMins !== '' ? Number(bufferTimeMins) : null,
          purchaseCost: purchaseCost !== '' ? Number(purchaseCost) : null,
          purchaseDate: purchaseDate || null,
          warrantyExpiryDate: warrantyExpiryDate || null,
        };

        const updated = await updateEquipment(equipmentToEdit.id, payload);
        onSuccess(updated);
        onClose();
      } else {
        const payload: CreateEquipmentRequest = {
          institutionId: Number(institutionId),
          departmentId: Number(departmentId),
          categoryId: Number(categoryId),
          name: name.trim(),
          assetTag: assetTag.trim(),
          serialNumber: serialNumber.trim(),
          modelNumber: modelNumber.trim() || null,
          manufacturer: manufacturer.trim() || null,
          locationBuilding: locationBuilding.trim(),
          locationRoom: locationRoom.trim(),
          shareableExternally,
          requiresApproval,
          requiresTrainingCertification,
          hourlyRateInternal: hourlyRateInternal !== '' ? Number(hourlyRateInternal) : null,
          hourlyRateExternal: hourlyRateExternal !== '' ? Number(hourlyRateExternal) : null,
          minBookingDurationMins:
            minBookingDurationMins !== '' ? Number(minBookingDurationMins) : null,
          maxBookingDurationMins:
            maxBookingDurationMins !== '' ? Number(maxBookingDurationMins) : null,
          bufferTimeMins: bufferTimeMins !== '' ? Number(bufferTimeMins) : null,
          purchaseCost: purchaseCost !== '' ? Number(purchaseCost) : null,
          purchaseDate: purchaseDate || null,
          warrantyExpiryDate: warrantyExpiryDate || null,
        };

        const created = await createEquipment(payload);
        onSuccess(created);
        onClose();
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const apiMsg = err.response?.data?.message || err.response?.data?.error || err.message;
        setErrorMessage(apiMsg || 'Failed to save equipment. Please verify your inputs.');
      } else {
        setErrorMessage('An unexpected error occurred while saving equipment.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/70 backdrop-blur-xs overflow-y-auto">
      <div className="relative w-full max-w-3xl bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden my-8">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/80">
          <div>
            <h3 className="text-lg font-bold text-white tracking-tight">
              {isEditing ? `Edit Equipment: ${equipmentToEdit?.assetTag}` : 'Register New Equipment'}
            </h3>
            <p className="text-xs text-slate-400 mt-0.5">
              {isEditing
                ? 'Update instrument parameters and configuration.'
                : 'Enter laboratory instrument specifications and operational parameters.'}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1.5 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6 max-h-[80vh] overflow-y-auto">
          {errorMessage && (
            <div className="flex items-start gap-3 p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Section 1: Identification */}
          <div className="space-y-4">
            <h4 className="text-xs font-semibold text-sky-400 uppercase tracking-wider">
              1. Instrument Identification & Category
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="sm:col-span-2">
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Equipment Name <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Ultrafast Centrifuge Optima XPN-100"
                  className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors ${
                    clientErrors.name
                      ? 'border-rose-500/50 focus:ring-rose-500/30'
                      : 'border-slate-800 focus:ring-sky-500/30 focus:border-sky-500'
                  }`}
                />
                {clientErrors.name && (
                  <p className="mt-1 text-[11px] text-rose-400">{clientErrors.name}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Asset Tag <span className="text-rose-400">*</span>
                  {isEditing && (
                    <span className="text-[10px] text-slate-500 ml-1.5">(Immutable)</span>
                  )}
                </label>
                <input
                  type="text"
                  value={assetTag}
                  disabled={isEditing}
                  onChange={(e) => setAssetTag(e.target.value)}
                  placeholder="e.g. NSTI-EQ-0042"
                  className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors disabled:opacity-60 disabled:cursor-not-allowed ${
                    clientErrors.assetTag
                      ? 'border-rose-500/50 focus:ring-rose-500/30'
                      : 'border-slate-800 focus:ring-sky-500/30 focus:border-sky-500'
                  }`}
                />
                {clientErrors.assetTag && (
                  <p className="mt-1 text-[11px] text-rose-400">{clientErrors.assetTag}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Serial Number <span className="text-rose-400">*</span>
                  {isEditing && (
                    <span className="text-[10px] text-slate-500 ml-1.5">(Immutable)</span>
                  )}
                </label>
                <input
                  type="text"
                  value={serialNumber}
                  disabled={isEditing}
                  onChange={(e) => setSerialNumber(e.target.value)}
                  placeholder="e.g. SN-883921-A"
                  className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors disabled:opacity-60 disabled:cursor-not-allowed ${
                    clientErrors.serialNumber
                      ? 'border-rose-500/50 focus:ring-rose-500/30'
                      : 'border-slate-800 focus:ring-sky-500/30 focus:border-sky-500'
                  }`}
                />
                {clientErrors.serialNumber && (
                  <p className="mt-1 text-[11px] text-rose-400">{clientErrors.serialNumber}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Model Number
                </label>
                <input
                  type="text"
                  value={modelNumber}
                  onChange={(e) => setModelNumber(e.target.value)}
                  placeholder="e.g. Optima XPN-100"
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Manufacturer
                </label>
                <input
                  type="text"
                  value={manufacturer}
                  onChange={(e) => setManufacturer(e.target.value)}
                  placeholder="e.g. Beckman Coulter"
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Category <span className="text-rose-400">*</span>
                </label>
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(Number(e.target.value))}
                  disabled={isLoadingRefData}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
                >
                  {categories.length === 0 ? (
                    <option value={1}>General Laboratory Equipment</option>
                  ) : (
                    categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))
                  )}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Host Institution <span className="text-rose-400">*</span>
                  {isEditing && <span className="text-[10px] text-slate-500 ml-1.5">(Immutable)</span>}
                </label>
                <select
                  value={institutionId}
                  disabled={isEditing}
                  onChange={(e) => setInstitutionId(Number(e.target.value))}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors disabled:opacity-60"
                >
                  {institutions.length === 0 ? (
                    <option value={user?.institutionId || 1}>
                      Institution (ID: {user?.institutionId || 1})
                    </option>
                  ) : (
                    institutions.map((inst) => (
                      <option key={inst.id} value={inst.id}>
                        {inst.name} ({inst.code})
                      </option>
                    ))
                  )}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Department <span className="text-rose-400">*</span>
                </label>
                <select
                  value={departmentId}
                  onChange={(e) => setDepartmentId(Number(e.target.value))}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
                >
                  {departments.length === 0 ? (
                    <option value={user?.departmentId || 1}>
                      Current Department (ID: {user?.departmentId || 1})
                    </option>
                  ) : (
                    departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.code})
                      </option>
                    ))
                  )}
                </select>
              </div>
            </div>
          </div>

          {/* Section 2: Location */}
          <div className="space-y-4">
            <h4 className="text-xs font-semibold text-sky-400 uppercase tracking-wider">
              2. Laboratory Location
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Building <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={locationBuilding}
                  onChange={(e) => setLocationBuilding(e.target.value)}
                  placeholder="e.g. Life Sciences Center"
                  className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors ${
                    clientErrors.locationBuilding
                      ? 'border-rose-500/50 focus:ring-rose-500/30'
                      : 'border-slate-800 focus:ring-sky-500/30 focus:border-sky-500'
                  }`}
                />
                {clientErrors.locationBuilding && (
                  <p className="mt-1 text-[11px] text-rose-400">
                    {clientErrors.locationBuilding}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Room <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={locationRoom}
                  onChange={(e) => setLocationRoom(e.target.value)}
                  placeholder="e.g. Room 402-B"
                  className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors ${
                    clientErrors.locationRoom
                      ? 'border-rose-500/50 focus:ring-rose-500/30'
                      : 'border-slate-800 focus:ring-sky-500/30 focus:border-sky-500'
                  }`}
                />
                {clientErrors.locationRoom && (
                  <p className="mt-1 text-[11px] text-rose-400">{clientErrors.locationRoom}</p>
                )}
              </div>
            </div>
          </div>

          {/* Section 3: Booking Rules & Policy */}
          <div className="space-y-4">
            <h4 className="text-xs font-semibold text-sky-400 uppercase tracking-wider">
              3. Operational & Booking Policy
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Min Duration (mins)
                </label>
                <input
                  type="number"
                  min="1"
                  value={minBookingDurationMins}
                  onChange={(e) => setMinBookingDurationMins(e.target.value)}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
                {clientErrors.minBookingDurationMins && (
                  <p className="mt-1 text-[11px] text-rose-400">
                    {clientErrors.minBookingDurationMins}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Max Duration (mins)
                </label>
                <input
                  type="number"
                  min="1"
                  value={maxBookingDurationMins}
                  onChange={(e) => setMaxBookingDurationMins(e.target.value)}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
                {clientErrors.maxBookingDurationMins && (
                  <p className="mt-1 text-[11px] text-rose-400">
                    {clientErrors.maxBookingDurationMins}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Buffer Time (mins)
                </label>
                <input
                  type="number"
                  min="0"
                  value={bufferTimeMins}
                  onChange={(e) => setBufferTimeMins(e.target.value)}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
                {clientErrors.bufferTimeMins && (
                  <p className="mt-1 text-[11px] text-rose-400">{clientErrors.bufferTimeMins}</p>
                )}
              </div>
            </div>

            <div className="flex flex-wrap gap-6 pt-2">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={shareableExternally}
                  onChange={(e) => setShareableExternally(e.target.checked)}
                  className="w-4 h-4 rounded-sm border-slate-800 bg-slate-950 text-sky-500 focus:ring-sky-500/30"
                />
                <span className="text-xs text-slate-300 font-medium">
                  Shareable with external institutions
                </span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={requiresApproval}
                  onChange={(e) => setRequiresApproval(e.target.checked)}
                  className="w-4 h-4 rounded-sm border-slate-800 bg-slate-950 text-sky-500 focus:ring-sky-500/30"
                />
                <span className="text-xs text-slate-300 font-medium">
                  Requires manager approval for bookings
                </span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={requiresTrainingCertification}
                  onChange={(e) => setRequiresTrainingCertification(e.target.checked)}
                  className="w-4 h-4 rounded-sm border-slate-800 bg-slate-950 text-sky-500 focus:ring-sky-500/30"
                />
                <span className="text-xs text-slate-300 font-medium">
                  Requires user qualification certification
                </span>
              </label>
            </div>
          </div>

          {/* Section 4: Rates & Finance */}
          <div className="space-y-4">
            <h4 className="text-xs font-semibold text-sky-400 uppercase tracking-wider">
              4. Rates & Purchase Information
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Internal Rate ($ / hr)
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={hourlyRateInternal}
                  onChange={(e) => setHourlyRateInternal(e.target.value)}
                  placeholder="0.00"
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
                {clientErrors.hourlyRateInternal && (
                  <p className="mt-1 text-[11px] text-rose-400">
                    {clientErrors.hourlyRateInternal}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  External Rate ($ / hr)
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={hourlyRateExternal}
                  onChange={(e) => setHourlyRateExternal(e.target.value)}
                  placeholder="0.00"
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
                {clientErrors.hourlyRateExternal && (
                  <p className="mt-1 text-[11px] text-rose-400">
                    {clientErrors.hourlyRateExternal}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Purchase Cost ($)
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={purchaseCost}
                  onChange={(e) => setPurchaseCost(e.target.value)}
                  placeholder="0.00"
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Warranty Expiry Date
                </label>
                <input
                  type="date"
                  value={warrantyExpiryDate}
                  onChange={(e) => setWarrantyExpiryDate(e.target.value)}
                  className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30"
                />
              </div>
            </div>
          </div>

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl transition-colors shadow-md shadow-sky-600/20 disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Saving...</span>
                </>
              ) : (
                <span>{isEditing ? 'Save Changes' : 'Register Equipment'}</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
