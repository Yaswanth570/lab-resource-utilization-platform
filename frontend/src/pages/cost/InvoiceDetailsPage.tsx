import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Receipt,
  Calendar,
  Building2,
  CheckCircle2,
  AlertCircle,
  Plus,
  Send,
  ShieldCheck,
  XCircle,
  Trash2,
  Loader2,
  DollarSign,
  FileText,
} from 'lucide-react';
import axios from 'axios';
import type { InvoiceResponse, AddInvoiceLineRequest, UsageCostResponse } from '../../types/cost';
import {
  getInvoiceById,
  updateInvoiceStatus,
  addInvoiceLine,
  deleteDraftInvoice,
  getUnbilledBookings,
} from '../../api/cost';
import { InvoiceStatusBadge } from '../../components/cost/InvoiceStatusBadge';

export const InvoiceDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [invoice, setInvoice] = useState<InvoiceResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [isActionLoading, setIsActionLoading] = useState<boolean>(false);

  // Add Line Modal State
  const [isAddLineOpen, setIsAddLineOpen] = useState<boolean>(false);
  const [lineDesc, setLineDesc] = useState<string>('');
  const [lineQty, setLineQty] = useState<string>('1');
  const [linePrice, setLinePrice] = useState<string>('50.00');
  const [selectedBookingId, setSelectedBookingId] = useState<string>('');
  const [availableBookings, setAvailableBookings] = useState<UsageCostResponse[]>([]);
  const [loadingBookings, setLoadingBookings] = useState<boolean>(false);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  const loadInvoice = useCallback(async () => {
    if (!id) return;
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getInvoiceById(id);
      setInvoice(data);
    } catch (err: unknown) {
      console.error('Failed to load invoice details:', err);
      let msg = 'Failed to load invoice. It may not exist or has been deleted.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadInvoice();
  }, [loadInvoice]);

  // Load unbilled bookings when opening add line modal
  const handleOpenAddLine = async () => {
    if (!invoice) return;
    setIsAddLineOpen(true);
    setLoadingBookings(true);
    try {
      const bks = await getUnbilledBookings(invoice.departmentId);
      setAvailableBookings(bks);
    } catch (err) {
      console.warn('Could not load unbilled bookings for modal:', err);
      setAvailableBookings([]);
    } finally {
      setLoadingBookings(false);
    }
  };

  // Status transitions
  const handleIssueInvoice = async () => {
    if (!id) return;
    setIsActionLoading(true);
    try {
      const updated = await updateInvoiceStatus(id, 'ISSUED');
      setInvoice(updated);
      showToast(`Invoice ${updated.invoiceNumber} has been issued successfully.`);
    } catch (err: unknown) {
      console.error('Failed to issue invoice:', err);
      let msg = 'Failed to issue invoice.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      alert(msg);
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleSettleInvoice = async () => {
    if (!id) return;
    if (!window.confirm('Mark this invoice as SETTLED? All linked bookings will also transition to SETTLED.')) {
      return;
    }
    setIsActionLoading(true);
    try {
      const updated = await updateInvoiceStatus(id, 'SETTLED');
      setInvoice(updated);
      showToast(`Invoice ${updated.invoiceNumber} and associated bookings are now SETTLED.`);
    } catch (err: unknown) {
      console.error('Failed to settle invoice:', err);
      let msg = 'Failed to settle invoice.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      alert(msg);
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleCancelInvoice = async () => {
    if (!id) return;
    if (!window.confirm('Are you sure you want to CANCEL this invoice?')) {
      return;
    }
    setIsActionLoading(true);
    try {
      const updated = await updateInvoiceStatus(id, 'CANCELLED');
      setInvoice(updated);
      showToast(`Invoice ${updated.invoiceNumber} has been cancelled.`);
    } catch (err: unknown) {
      console.error('Failed to cancel invoice:', err);
      let msg = 'Failed to cancel invoice.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      alert(msg);
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleDeleteDraft = async () => {
    if (!id) return;
    if (!window.confirm('Delete this draft invoice? All associated bookings will be returned to UNBILLED status.')) {
      return;
    }
    setIsActionLoading(true);
    try {
      await deleteDraftInvoice(id);
      alert('Draft invoice deleted. Linked bookings reset to UNBILLED.');
      navigate('/cost');
    } catch (err: unknown) {
      console.error('Failed to delete draft invoice:', err);
      let msg = 'Failed to delete draft invoice.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      alert(msg);
      setIsActionLoading(false);
    }
  };

  const handleAddLineSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !lineDesc.trim()) return;

    setIsActionLoading(true);
    try {
      const payload: AddInvoiceLineRequest = {
        description: lineDesc.trim(),
        quantity: parseFloat(lineQty) || 1,
        unitPrice: parseFloat(linePrice) || 0,
        bookingId: selectedBookingId ? Number(selectedBookingId) : undefined,
      };

      const updated = await addInvoiceLine(id, payload);
      setInvoice(updated);
      setIsAddLineOpen(false);
      setLineDesc('');
      setLineQty('1');
      setLinePrice('50.00');
      setSelectedBookingId('');
      showToast('Line item added successfully.');
    } catch (err: unknown) {
      console.error('Failed to add line item:', err);
      let msg = 'Failed to add line item.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        msg = err.response.data.message;
      }
      alert(msg);
    } finally {
      setIsActionLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="py-24 text-center text-slate-400 flex flex-col items-center gap-3">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-400" />
        <span>Loading invoice details...</span>
      </div>
    );
  }

  if (errorMessage || !invoice) {
    return (
      <div className="space-y-6 max-w-2xl mx-auto py-12">
        <div className="p-6 bg-rose-500/10 border border-rose-500/20 rounded-2xl text-center">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto mb-3" />
          <h2 className="text-lg font-bold text-white">Invoice Not Found</h2>
          <p className="text-sm text-rose-300 mt-1">{errorMessage || 'Unable to retrieve invoice.'}</p>
          <Link
            to="/cost"
            className="mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-medium transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Cost & Billing</span>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Toast Feedback */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 p-4 rounded-xl bg-emerald-500/90 text-white font-medium shadow-2xl backdrop-blur-md flex items-center gap-3 animate-in slide-in-from-bottom-5">
          <CheckCircle2 className="w-5 h-5 text-emerald-200 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Back navigation */}
      <div>
        <Link
          to="/cost"
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-400 hover:text-white transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Cost & Billing Management</span>
        </Link>
      </div>

      {/* Main Header Banner */}
      <div className="bg-slate-900/60 border border-slate-800 p-6 md:p-8 rounded-2xl backdrop-blur-md">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="p-3.5 bg-gradient-to-br from-indigo-500/20 to-purple-500/20 border border-indigo-500/30 rounded-2xl text-indigo-400">
              <Receipt className="w-8 h-8" />
            </div>
            <div>
              <div className="flex items-center gap-3 flex-wrap">
                <h1 className="text-2xl md:text-3xl font-bold font-mono text-white tracking-tight">
                  {invoice.invoiceNumber}
                </h1>
                <InvoiceStatusBadge status={invoice.status} />
              </div>
              <p className="text-xs text-slate-400 mt-1 flex items-center gap-2 flex-wrap">
                <span>Created {invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString() : 'N/A'}</span>
                {invoice.issuedAt && (
                  <>
                    <span>•</span>
                    <span className="text-blue-400">Issued {new Date(invoice.issuedAt).toLocaleDateString()}</span>
                  </>
                )}
                {invoice.paidAt && (
                  <>
                    <span>•</span>
                    <span className="text-emerald-400">Settled {new Date(invoice.paidAt).toLocaleDateString()}</span>
                  </>
                )}
              </p>
            </div>
          </div>

          {/* Action Lifecycle Buttons */}
          <div className="flex items-center gap-2.5 flex-wrap">
            {invoice.status === 'DRAFT' && (
              <>
                <button
                  onClick={handleOpenAddLine}
                  disabled={isActionLoading}
                  className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium border border-slate-700 transition flex items-center gap-1.5"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add Line</span>
                </button>
                <button
                  onClick={handleIssueInvoice}
                  disabled={isActionLoading}
                  className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-medium shadow-lg shadow-blue-500/20 transition flex items-center gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Issue Invoice</span>
                </button>
                <button
                  onClick={handleDeleteDraft}
                  disabled={isActionLoading}
                  className="p-2 rounded-xl text-rose-400 hover:bg-rose-500/10 border border-rose-500/20 transition"
                  title="Delete Draft"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </>
            )}

            {invoice.status === 'ISSUED' && (
              <>
                <button
                  onClick={handleSettleInvoice}
                  disabled={isActionLoading}
                  className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium shadow-lg shadow-emerald-500/20 transition flex items-center gap-1.5"
                >
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Mark Settled</span>
                </button>
                <button
                  onClick={handleCancelInvoice}
                  disabled={isActionLoading}
                  className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-rose-950/40 text-slate-400 hover:text-rose-400 border border-slate-700 hover:border-rose-500/30 text-xs font-medium transition flex items-center gap-1.5"
                >
                  <XCircle className="w-3.5 h-3.5" />
                  <span>Cancel Invoice</span>
                </button>
              </>
            )}

            {invoice.status === 'SETTLED' && (
              <span className="px-3.5 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-medium flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4" />
                <span>Invoice Fully Settled</span>
              </span>
            )}
          </div>
        </div>

        {/* Metadata Details Grid */}
        <div className="mt-8 pt-6 border-t border-slate-800/80 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
          <div className="p-3.5 rounded-xl bg-slate-800/40 border border-slate-800">
            <span className="text-slate-400 block mb-1 flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-slate-500" />
              Billed Department
            </span>
            <div className="text-sm font-semibold text-white">
              {invoice.departmentName || `Department #${invoice.departmentId}`}
            </div>
            <div className="text-[11px] text-slate-500">{invoice.institutionName || 'Default Institution'}</div>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/40 border border-slate-800">
            <span className="text-slate-400 block mb-1 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-slate-500" />
              Billing Period
            </span>
            <div className="text-sm font-mono font-medium text-slate-200">
              {invoice.billingPeriodStart} → {invoice.billingPeriodEnd}
            </div>
            <div className="text-[11px] text-slate-500">Service Coverage Window</div>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/40 border border-slate-800">
            <span className="text-slate-400 block mb-1 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-slate-500" />
              Line Item Count
            </span>
            <div className="text-sm font-mono font-semibold text-white">
              {invoice.lineItems ? invoice.lineItems.length : 0} items
            </div>
            <div className="text-[11px] text-slate-500">
              {invoice.status === 'DRAFT' ? 'Editable until issued' : 'Immutable'}
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-indigo-950/30 border border-indigo-500/30">
            <span className="text-indigo-300 block mb-1 flex items-center gap-1.5">
              <DollarSign className="w-3.5 h-3.5 text-indigo-400" />
              Total Invoice Amount
            </span>
            <div className="text-lg font-bold font-mono text-indigo-200">
              ${(invoice.totalAmount || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <div className="text-[11px] text-indigo-400/80">Inclusive of all billable items</div>
          </div>
        </div>
      </div>

      {/* Invoice Line Items Table */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-white">Invoice Line Items</h2>
            <p className="text-xs text-slate-400">
              Detailed breakdown of usage sessions, equipment hours, and attached billables
            </p>
          </div>
          {invoice.status === 'DRAFT' && (
            <button
              onClick={handleOpenAddLine}
              className="px-3 py-1.5 rounded-xl bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border border-indigo-500/30 text-xs font-medium transition flex items-center gap-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Custom Line</span>
            </button>
          )}
        </div>

        {invoice.lineItems.length === 0 ? (
          <div className="py-12 text-center bg-slate-900/30 border border-dashed border-slate-800 rounded-2xl text-slate-400 text-xs">
            No line items attached to this invoice yet. Click &quot;Add Custom Line&quot; to add billables.
          </div>
        ) : (
          <div className="bg-slate-900/40 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-300">
                <thead className="bg-slate-900/70 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <tr>
                    <th className="px-6 py-4 w-12">#</th>
                    <th className="px-6 py-4">Description</th>
                    <th className="px-6 py-4">Booking Ref</th>
                    <th className="px-6 py-4 text-right">Quantity</th>
                    <th className="px-6 py-4 text-right">Unit Price</th>
                    <th className="px-6 py-4 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {invoice.lineItems.map((line, idx) => (
                    <tr key={line.id || idx} className="hover:bg-slate-800/30 transition">
                      <td className="px-6 py-4 font-mono text-xs text-slate-500">
                        {idx + 1}
                      </td>
                      <td className="px-6 py-4 font-medium text-white">
                        {line.description}
                      </td>
                      <td className="px-6 py-4 font-mono text-xs text-indigo-400">
                        {line.bookingReference ? (
                          <span className="px-2 py-0.5 rounded-md bg-indigo-500/10 border border-indigo-500/20">
                            {line.bookingReference}
                          </span>
                        ) : (
                          <span className="text-slate-600">—</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-right font-mono text-slate-300">
                        {line.quantity}
                      </td>
                      <td className="px-6 py-4 text-right font-mono text-slate-400">
                        ${line.unitPrice?.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-right font-mono font-bold text-white">
                        ${line.totalAmount?.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-slate-900/80 border-t border-slate-800">
                  <tr>
                    <td colSpan={5} className="px-6 py-4 text-right text-xs font-bold text-slate-400 uppercase tracking-wider">
                      Invoice Grand Total
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-lg text-indigo-300">
                      ${(invoice.totalAmount || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Add Line Item Modal */}
      {isAddLineOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg shadow-2xl p-6 space-y-5 animate-in fade-in duration-200">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <h3 className="text-base font-semibold text-white">Add Line Item</h3>
              <button
                onClick={() => setIsAddLineOpen(false)}
                className="text-slate-400 hover:text-white text-xs"
              >
                Cancel
              </button>
            </div>

            <form onSubmit={handleAddLineSubmit} className="space-y-4 text-sm">
              {/* Optional Unbilled Booking Picker */}
              {availableBookings.length > 0 && (
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">
                    Link to Unbilled Booking (Optional)
                  </label>
                  <select
                    value={selectedBookingId}
                    onChange={(e) => {
                      const bId = e.target.value;
                      setSelectedBookingId(bId);
                      if (bId) {
                        const found = availableBookings.find((b) => String(b.bookingId) === bId);
                        if (found) {
                          setLineDesc(`Lab usage for ${found.equipmentName || 'Equipment'}`);
                          setLinePrice(String(found.hourlyRate || 50));
                        }
                      }
                    }}
                    disabled={loadingBookings}
                    className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-none focus:border-indigo-500"
                  >
                    <option value="">No linked booking (Custom charge)</option>
                    {availableBookings.map((b) => (
                      <option key={b.bookingId} value={b.bookingId}>
                        {b.bookingReference} — {b.equipmentName} (${(b.totalCost || 0).toFixed(2)})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">
                  Item Description <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={lineDesc}
                  onChange={(e) => setLineDesc(e.target.value)}
                  placeholder="e.g., Spectrometer analysis hours"
                  required
                  className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">
                    Quantity / Hours <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={lineQty}
                    onChange={(e) => setLineQty(e.target.value)}
                    required
                    className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">
                    Unit Price ($) <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={linePrice}
                    onChange={(e) => setLinePrice(e.target.value)}
                    required
                    className="w-full px-3 py-2 bg-slate-800/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsAddLineOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isActionLoading || !lineDesc.trim()}
                  className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium shadow-lg transition disabled:opacity-50"
                >
                  {isActionLoading ? 'Adding...' : 'Add Line Item'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
