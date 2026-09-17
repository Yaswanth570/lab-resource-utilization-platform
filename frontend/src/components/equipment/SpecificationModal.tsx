import React, { useState, useEffect } from 'react';
import { X, Loader2, AlertCircle } from 'lucide-react';
import axios from 'axios';
import type {
  EquipmentSpecificationResponse,
  CreateEquipmentSpecificationRequest,
  UpdateEquipmentSpecificationRequest,
} from '../../types/equipment';
import { createSpecification, updateSpecification } from '../../api/equipment';

interface SpecificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  equipmentId: number;
  specToEdit?: EquipmentSpecificationResponse | null;
  onSuccess: (saved: EquipmentSpecificationResponse) => void;
}

export const SpecificationModal: React.FC<SpecificationModalProps> = ({
  isOpen,
  onClose,
  equipmentId,
  specToEdit,
  onSuccess,
}) => {
  const isEditing = Boolean(specToEdit);
  const [specName, setSpecName] = useState<string>('');
  const [specValue, setSpecValue] = useState<string>('');
  const [unit, setUnit] = useState<string>('');

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isOpen) {
      if (specToEdit) {
        setSpecName(specToEdit.specName || '');
        setSpecValue(specToEdit.specValue || '');
        setUnit(specToEdit.unit || '');
      } else {
        setSpecName('');
        setSpecValue('');
        setUnit('');
      }
    }
  }, [isOpen, specToEdit]);

  if (!isOpen) return null;

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!specName.trim()) errs.specName = 'Specification name is required';
    if (!specValue.trim()) errs.specValue = 'Specification value is required';
    setClientErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      if (isEditing && specToEdit) {
        const payload: UpdateEquipmentSpecificationRequest = {
          specName: specName.trim(),
          specValue: specValue.trim(),
          unit: unit.trim() || undefined,
        };
        const updated = await updateSpecification(specToEdit.id, payload);
        onSuccess(updated);
        onClose();
      } else {
        const payload: CreateEquipmentSpecificationRequest = {
          specName: specName.trim(),
          specValue: specValue.trim(),
          unit: unit.trim() || undefined,
        };
        const created = await createSpecification(equipmentId, payload);
        onSuccess(created);
        onClose();
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const msg = err.response?.data?.message || err.response?.data?.error || err.message;
        setErrorMessage(msg || 'Failed to save specification.');
      } else {
        setErrorMessage('An unexpected error occurred while saving specification.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/70 backdrop-blur-xs">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
          <div>
            <h3 className="text-sm font-bold text-white tracking-tight">
              {isEditing ? 'Edit Specification' : 'Add Equipment Specification'}
            </h3>
            <p className="text-[11px] text-slate-400">
              Define technical parameter, measurement, or capacity.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {errorMessage && (
            <div className="flex items-start gap-2.5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Parameter Name <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              value={specName}
              onChange={(e) => setSpecName(e.target.value)}
              placeholder="e.g. Max Speed, Wavelength, Laser Power"
              className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors ${
                clientErrors.specName
                  ? 'border-rose-500/50 focus:ring-rose-500/30'
                  : 'border-slate-800 focus:ring-sky-500/30'
              }`}
            />
            {clientErrors.specName && (
              <p className="mt-1 text-[11px] text-rose-400">{clientErrors.specName}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Parameter Value <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              value={specValue}
              onChange={(e) => setSpecValue(e.target.value)}
              placeholder="e.g. 100000, 200-900, 50"
              className={`w-full px-3.5 py-2 text-xs text-white bg-slate-950 border rounded-xl focus:outline-hidden focus:ring-2 transition-colors ${
                clientErrors.specValue
                  ? 'border-rose-500/50 focus:ring-rose-500/30'
                  : 'border-slate-800 focus:ring-sky-500/30'
              }`}
            />
            {clientErrors.specValue && (
              <p className="mt-1 text-[11px] text-rose-400">{clientErrors.specValue}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Unit of Measure <span className="text-slate-500 text-[11px]">(Optional)</span>
            </label>
            <input
              type="text"
              value={unit}
              onChange={(e) => setUnit(e.target.value)}
              placeholder="e.g. RPM, nm, mW, °C"
              className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 transition-colors"
            />
          </div>

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-3.5 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl transition-colors disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  <span>Saving...</span>
                </>
              ) : (
                <span>{isEditing ? 'Update Specification' : 'Add Specification'}</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
