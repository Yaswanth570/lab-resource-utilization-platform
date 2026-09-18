import React, { useState, useEffect, useMemo } from 'react';
import { X, Receipt, Loader2, AlertCircle, Calendar, Building2, CheckSquare, Square, DollarSign } from 'lucide-react';
import axios from 'axios';
import type { InvoiceResponse, CreateInvoiceRequest, UsageCostResponse } from '../../types/cost';
import type { InstitutionLookup, DepartmentLookup } from '../../types/equipment';
import { getInstitutions, getDepartmentsByInstitution } from '../../api/equipment';
import { getUnbilledBookings, createInvoice } from '../../api/cost';

interface CreateInvoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (created: InvoiceResponse) => void;
}

function getDefaultPeriod(): { start: string; end: string } {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  // First day of current month
  const start = `${yyyy}-${mm}-01`;
  const end = `${yyyy}-${mm}-${dd}`;
  return { start, end };
}

export const CreateInvoiceModal: React.FC<CreateInvoiceModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [institutions, setInstitutions] = useState<InstitutionLookup[]>([]);
  const [departments, setDepartments] = useState<DepartmentLookup[]>([]);
  const [unbilledBookings, setUnbilledBookings] = useState<UsageCostResponse[]>([]);

  const [loadingLookups, setLoadingLookups] = useState<boolean>(false);
  const [loadingBookings, setLoadingBookings] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [selectedInstitutionId, setSelectedInstitutionId] = useState<string>('');
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<string>('');
  const [periodStart, setPeriodStart] = useState<string>(() => getDefaultPeriod().start);
  const [periodEnd, setPeriodEnd] = useState<string>(() => getDefaultPeriod().end);
  const [selectedBookingIds, setSelectedBookingIds] = useState<number[]>([]);

  // Load institutions on modal open
  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setSelectedBookingIds([]);
      setLoadingLookups(true);
      getInstitutions()
        .then((insts) => {
          setInstitutions(insts);
          if (insts.length > 0) {
            setSelectedInstitutionId(String(insts[0].id));
          }
        })
        .catch((err) => {
          console.error('Failed to load institutions:', err);
          setErrorMessage('Failed to load institutions.');
        })
        .finally(() => {
          setLoadingLookups(false);
        });
    }
  }, [isOpen]);

  // Load departments when institution changes
  useEffect(() => {
    if (selectedInstitutionId) {
      getDepartmentsByInstitution(selectedInstitutionId)
        .then((depts) => {
          setDepartments(depts);
          if (depts.length > 0) {
            setSelectedDepartmentId(String(depts[0].id));
          } else {
            setSelectedDepartmentId('');
          }
        })
        .catch((err) => {
          console.error('Failed to load departments:', err);
          setDepartments([]);
          setSelectedDepartmentId('');
        });
    } else {
      setDepartments([]);
      setSelectedDepartmentId('');
    }
  }, [selectedInstitutionId]);

  // Load unbilled bookings when department changes
  useEffect(() => {
    if (selectedDepartmentId) {
      setLoadingBookings(true);
      setSelectedBookingIds([]);
      getUnbilledBookings(Number(selectedDepartmentId))
        .then((bks) => {
          setUnbilledBookings(bks);
        })
        .catch((err) => {
          console.error('Failed to load unbilled bookings:', err);
          setUnbilledBookings([]);
        })
        .finally(() => {
          setLoadingBookings(false);
        });
    } else {
      setUnbilledBookings([]);
      setSelectedBookingIds([]);
    }
  }, [selectedDepartmentId]);

  const toggleBooking = (id: number) => {
    setSelectedBookingIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const toggleAllBookings = () => {
    if (selectedBookingIds.length === unbilledBookings.length) {
      setSelectedBookingIds([]);
    } else {
      setSelectedBookingIds(unbilledBookings.map((b) => b.bookingId));
    }
  };

  const estimatedTotal = useMemo(() => {
    return unbilledBookings
      .filter((b) => selectedBookingIds.includes(b.bookingId))
      .reduce((sum, b) => sum + (b.totalCost || 0), 0);
  }, [unbilledBookings, selectedBookingIds]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!selectedDepartmentId) {
      setErrorMessage('Please select a department for the invoice.');
      return;
    }

    if (!periodStart || !periodEnd) {
      setErrorMessage('Please provide both billing start and end dates.');
      return;
    }

    if (new Date(periodEnd) < new Date(periodStart)) {
      setErrorMessage('Billing period end date cannot be earlier than start date.');
      return;
    }

    setIsSubmitting(true);
    try {
      const payload: CreateInvoiceRequest = {
        departmentId: Number(selectedDepartmentId),
        institutionId: selectedInstitutionId ? Number(selectedInstitutionId) : undefined,
        billingPeriodStart: periodStart,
        billingPeriodEnd: periodEnd,
        bookingIds: selectedBookingIds.length > 0 ? selectedBookingIds : undefined,
      };

      const created = await createInvoice(payload);
      onSuccess(created);
      onClose();
    } catch (err: unknown) {
      console.error('Failed to create invoice:', err);
      let msg = 'Failed to generate invoice. Please try again.';
      if (axios.isAxiosError(err)) {
        msg = err.response?.data?.message || err.response?.data?.error || msg;
      }
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden my-8 animate-in fade-in duration-200">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/50">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-gradient-to-br from-indigo-500/20 to-purple-500/20 border border-indigo-500/30 text-indigo-400">
              <Receipt className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-white">Generate Department Invoice</h2>
              <p className="text-xs text-slate-400">
                Create a draft billing invoice and bundle unbilled usage bookings
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {errorMessage && (
            <div className="p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-start gap-2.5">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Institution & Department Pickers */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 flex items-center gap-1.5">
                <Building2 className="w-3.5 h-3.5 text-slate-500" />
                Institution
              </label>
              <select
                value={selectedInstitutionId}
                onChange={(e) => setSelectedInstitutionId(e.target.value)}
                disabled={loadingLookups || isSubmitting}
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500"
              >
                {loadingLookups ? (
                  <option value="">Loading institutions...</option>
                ) : institutions.length === 0 ? (
                  <option value="">No institutions available</option>
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
              <label className="block text-xs font-medium text-slate-400 mb-1.5">
                Target Department <span className="text-rose-400">*</span>
              </label>
              <select
                value={selectedDepartmentId}
                onChange={(e) => setSelectedDepartmentId(e.target.value)}
                disabled={loadingLookups || departments.length === 0 || isSubmitting}
                required
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500"
              >
                {departments.length === 0 ? (
                  <option value="">No departments available</option>
                ) : (
                  departments.map((dept) => (
                    <option key={dept.id} value={dept.id}>
                      {dept.name} ({dept.code})
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          {/* Billing Period */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-slate-500" />
                Billing Period Start <span className="text-rose-400">*</span>
              </label>
              <input
                type="date"
                value={periodStart}
                onChange={(e) => setPeriodStart(e.target.value)}
                disabled={isSubmitting}
                required
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-slate-500" />
                Billing Period End <span className="text-rose-400">*</span>
              </label>
              <input
                type="date"
                value={periodEnd}
                onChange={(e) => setPeriodEnd(e.target.value)}
                disabled={isSubmitting}
                required
                className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-sm text-white focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          {/* Unbilled Bookings Selection */}
          <div className="pt-2 border-t border-slate-800">
            <div className="flex items-center justify-between mb-2.5">
              <div>
                <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                  Select Unbilled Bookings ({unbilledBookings.length} available)
                </span>
                <p className="text-xs text-slate-500">
                  Selected bookings will automatically transition to INVOICED on invoice creation
                </p>
              </div>
              {unbilledBookings.length > 0 && (
                <button
                  type="button"
                  onClick={toggleAllBookings}
                  className="text-xs text-indigo-400 hover:text-indigo-300 font-medium"
                >
                  {selectedBookingIds.length === unbilledBookings.length ? 'Deselect All' : 'Select All'}
                </button>
              )}
            </div>

            {loadingBookings ? (
              <div className="py-8 flex flex-col items-center justify-center gap-2 text-slate-400">
                <Loader2 className="w-5 h-5 animate-spin text-indigo-400" />
                <span className="text-xs">Fetching department unbilled bookings...</span>
              </div>
            ) : unbilledBookings.length === 0 ? (
              <div className="py-6 px-4 rounded-xl bg-slate-800/30 border border-slate-800/80 text-center text-xs text-slate-400">
                No unbilled bookings found for this department. You can still generate a draft invoice and attach custom line items later.
              </div>
            ) : (
              <div className="max-h-56 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
                {unbilledBookings.map((b) => {
                  const isSelected = selectedBookingIds.includes(b.bookingId);
                  const approxCost = b.totalCost || 0;
                  return (
                    <div
                      key={b.bookingId}
                      onClick={() => toggleBooking(b.bookingId)}
                      className={`p-3 rounded-xl border transition cursor-pointer flex items-center justify-between ${
                        isSelected
                          ? 'bg-indigo-500/10 border-indigo-500/30 text-white'
                          : 'bg-slate-800/40 border-slate-800 text-slate-300 hover:bg-slate-800/80'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="text-indigo-400">
                          {isSelected ? <CheckSquare className="w-4 h-4" /> : <Square className="w-4 h-4 text-slate-500" />}
                        </div>
                        <div>
                          <div className="text-xs font-semibold font-mono text-indigo-300">
                            {b.bookingReference}
                          </div>
                          <div className="text-xs text-slate-400 truncate max-w-xs">
                            {b.equipmentName} • {b.userName || 'User'}
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xs font-bold text-slate-200">
                          ${approxCost.toFixed(2)}
                        </div>
                        <div className="text-[10px] text-slate-500 font-mono">
                          {b.startTime?.slice(0, 10)}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Estimated Total Preview */}
            {selectedBookingIds.length > 0 && (
              <div className="mt-3 p-3 rounded-xl bg-indigo-950/40 border border-indigo-500/30 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs text-indigo-300">
                  <DollarSign className="w-4 h-4 text-indigo-400" />
                  <span>Estimated Total for {selectedBookingIds.length} booking(s):</span>
                </div>
                <div className="text-sm font-bold text-indigo-200 font-mono">
                  ${estimatedTotal.toFixed(2)}
                </div>
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 rounded-xl text-sm font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !selectedDepartmentId}
              className="px-5 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-purple-600 text-white hover:from-indigo-600 hover:to-purple-700 shadow-lg shadow-indigo-500/20 transition flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Creating Draft...</span>
                </>
              ) : (
                <>
                  <Receipt className="w-4 h-4" />
                  <span>Create Invoice</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
