import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Receipt,
  Building2,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Plus,
  ArrowRight,
  ExternalLink,
  CheckCircle2,
  Clock,
  ShieldCheck,
  AlertCircle,
  FileText,
  Trash2,
} from 'lucide-react';
import type {
  InvoiceResponse,
  UsageCostResponse,
  DepartmentCostSummaryResponse,
  InvoiceStatus,
} from '../../types/cost';
import type { BookingBillingStatus } from '../../types/booking';
import {
  getInvoices,
  getUsageCosts,
  getDepartmentCostSummary,
  deleteDraftInvoice,
} from '../../api/cost';
import { InvoiceStatusBadge } from '../../components/cost/InvoiceStatusBadge';
import { BookingBillingStatusBadge } from '../../components/cost/BookingBillingStatusBadge';
import { CreateInvoiceModal } from '../../components/cost/CreateInvoiceModal';

type ActiveTab = 'invoices' | 'usage' | 'departments';

export const CostPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ActiveTab>('invoices');

  // Invoices State
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [isLoadingInvoices, setIsLoadingInvoices] = useState<boolean>(false);
  const [invoiceSearch, setInvoiceSearch] = useState<string>('');
  const [invoiceStatusFilter, setInvoiceStatusFilter] = useState<InvoiceStatus | 'ALL'>('ALL');

  // Usage Costs State
  const [usageCosts, setUsageCosts] = useState<UsageCostResponse[]>([]);
  const [isLoadingUsage, setIsLoadingUsage] = useState<boolean>(false);
  const [usageSearch, setUsageSearch] = useState<string>('');
  const [usageStatusFilter, setUsageStatusFilter] = useState<BookingBillingStatus | 'ALL'>('ALL');

  // Department Summaries State
  const [departmentSummaries, setDepartmentSummaries] = useState<DepartmentCostSummaryResponse[]>([]);
  const [isLoadingDepartments, setIsLoadingDepartments] = useState<boolean>(false);
  const [deptSearch, setDeptSearch] = useState<string>('');

  // Modals & Feedback
  const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);
  const [successToast, setSuccessToast] = useState<string | null>(null);
  const [errorToast, setErrorToast] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const showToast = (msg: string, isError = false) => {
    if (isError) {
      setErrorToast(msg);
      setTimeout(() => setErrorToast(null), 5000);
    } else {
      setSuccessToast(msg);
      setTimeout(() => setSuccessToast(null), 4000);
    }
  };

  // --- Data Loading ---
  const loadInvoices = useCallback(async () => {
    setIsLoadingInvoices(true);
    try {
      const data = await getInvoices();
      setInvoices(data);
    } catch (err) {
      console.error('Failed to load billing invoices:', err);
    } finally {
      setIsLoadingInvoices(false);
    }
  }, []);

  const loadUsageCosts = useCallback(async () => {
    setIsLoadingUsage(true);
    try {
      const data = await getUsageCosts();
      setUsageCosts(data);
    } catch (err) {
      console.error('Failed to load usage costs:', err);
    } finally {
      setIsLoadingUsage(false);
    }
  }, []);

  const loadDepartmentSummaries = useCallback(async () => {
    setIsLoadingDepartments(true);
    try {
      const data = await getDepartmentCostSummary();
      setDepartmentSummaries(data);
    } catch (err) {
      console.error('Failed to load department summaries:', err);
    } finally {
      setIsLoadingDepartments(false);
    }
  }, []);

  const refreshAll = useCallback(() => {
    loadInvoices();
    loadUsageCosts();
    loadDepartmentSummaries();
  }, [loadInvoices, loadUsageCosts, loadDepartmentSummaries]);

  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  // Handle Draft Invoice Quick Deletion
  const handleDeleteDraft = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this draft invoice? Associated bookings will be reset to UNBILLED status.')) {
      return;
    }

    setDeletingId(id);
    try {
      await deleteDraftInvoice(id);
      showToast('Draft invoice deleted. Linked bookings reset to unbilled.');
      refreshAll();
    } catch (err: unknown) {
      console.error('Failed to delete draft invoice:', err);
      showToast('Failed to delete invoice. Only draft invoices can be removed.', true);
    } finally {
      setDeletingId(null);
    }
  };

  // --- Filtered Data ---
  const filteredInvoices = useMemo(() => {
    return invoices.filter((inv) => {
      const q = invoiceSearch.toLowerCase().trim();
      const matchesSearch =
        !q ||
        inv.invoiceNumber.toLowerCase().includes(q) ||
        (inv.departmentName && inv.departmentName.toLowerCase().includes(q));
      const matchesStatus =
        invoiceStatusFilter === 'ALL' || inv.status === invoiceStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [invoices, invoiceSearch, invoiceStatusFilter]);

  const filteredUsageCosts = useMemo(() => {
    return usageCosts.filter((u) => {
      const q = usageSearch.toLowerCase().trim();
      const matchesSearch =
        !q ||
        u.bookingReference.toLowerCase().includes(q) ||
        (u.equipmentName && u.equipmentName.toLowerCase().includes(q)) ||
        (u.userName && u.userName.toLowerCase().includes(q)) ||
        (u.departmentName && u.departmentName.toLowerCase().includes(q));
      const matchesStatus =
        usageStatusFilter === 'ALL' || u.billingStatus === usageStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [usageCosts, usageSearch, usageStatusFilter]);

  const filteredDepartments = useMemo(() => {
    return departmentSummaries.filter((d) => {
      const q = deptSearch.toLowerCase().trim();
      return !q || d.departmentName.toLowerCase().includes(q);
    });
  }, [departmentSummaries, deptSearch]);

  // --- Derived KPIs ---
  const totalInvoicedAmount = useMemo(() => {
    return invoices
      .filter((inv) => inv.status !== 'CANCELLED')
      .reduce((sum, inv) => sum + (inv.totalAmount || 0), 0);
  }, [invoices]);

  const totalSettledAmount = useMemo(() => {
    return invoices
      .filter((inv) => inv.status === 'SETTLED' || inv.status === 'PAID')
      .reduce((sum, inv) => sum + (inv.totalAmount || 0), 0);
  }, [invoices]);

  const unbilledValue = useMemo(() => {
    return usageCosts
      .filter((u) => u.billingStatus === 'UNBILLED')
      .reduce((sum, u) => sum + (u.totalCost || 0), 0);
  }, [usageCosts]);

  const activeInvoicesCount = useMemo(() => {
    return invoices.filter((inv) => inv.status === 'DRAFT' || inv.status === 'ISSUED').length;
  }, [invoices]);

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Toast Feedback */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 p-4 rounded-xl bg-emerald-500/90 text-white font-medium shadow-2xl backdrop-blur-md flex items-center gap-3 animate-in slide-in-from-bottom-5">
          <CheckCircle2 className="w-5 h-5 text-emerald-200 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}
      {errorToast && (
        <div className="fixed bottom-6 right-6 z-50 p-4 rounded-xl bg-rose-500/90 text-white font-medium shadow-2xl backdrop-blur-md flex items-center gap-3 animate-in slide-in-from-bottom-5">
          <AlertCircle className="w-5 h-5 text-rose-200 shrink-0" />
          <span>{errorToast}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/50 p-6 rounded-2xl border border-slate-800 backdrop-blur-md">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-gradient-to-br from-indigo-500/20 to-purple-500/20 border border-indigo-500/30 rounded-xl text-indigo-400">
            <Receipt className="w-7 h-7" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white tracking-tight">
              Cost & Billing Management
            </h1>
            <p className="text-sm text-slate-400 mt-0.5">
              Monitor equipment usage costs, audit departmental spend summaries, and manage billing invoices.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={refreshAll}
            className="p-2.5 rounded-xl border border-slate-700 bg-slate-800/80 text-slate-300 hover:text-white hover:bg-slate-700/80 transition flex items-center gap-2 text-sm font-medium"
            title="Refresh All Data"
          >
            <RefreshCw
              className={`w-4 h-4 ${
                isLoadingInvoices || isLoadingUsage || isLoadingDepartments ? 'animate-spin' : ''
              }`}
            />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white text-sm font-medium shadow-lg shadow-indigo-500/20 transition flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            <span>Generate Invoice</span>
          </button>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Total Invoiced</div>
            <div className="text-2xl font-bold text-white mt-1 font-mono">
              ${totalInvoicedAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Across all non-cancelled invoices</div>
          </div>
          <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
            <Receipt className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Unbilled Usage</div>
            <div className="text-2xl font-bold text-amber-400 mt-1 font-mono">
              ${unbilledValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Ready for invoice generation</div>
          </div>
          <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
            <Clock className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Settled Revenue</div>
            <div className="text-2xl font-bold text-emerald-400 mt-1 font-mono">
              ${totalSettledAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Paid and reconciled</div>
          </div>
          <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
            <ShieldCheck className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between">
          <div>
            <div className="text-xs font-medium text-slate-400">Active Invoices</div>
            <div className="text-2xl font-bold text-blue-400 mt-1 font-mono">
              {activeInvoicesCount}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Draft or pending settlement</div>
          </div>
          <div className="p-3 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400">
            <FileText className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Tabs Navigation */}
      <div className="border-b border-slate-800">
        <div className="flex space-x-8">
          <button
            onClick={() => setActiveTab('invoices')}
            className={`pb-4 text-sm font-medium transition flex items-center gap-2 border-b-2 ${
              activeTab === 'invoices'
                ? 'border-indigo-500 text-indigo-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Receipt className="w-4 h-4" />
            <span>Billing Invoices</span>
            <span className="ml-1.5 px-2 py-0.5 text-xs rounded-full bg-slate-800 text-slate-400">
              {invoices.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('usage')}
            className={`pb-4 text-sm font-medium transition flex items-center gap-2 border-b-2 ${
              activeTab === 'usage'
                ? 'border-indigo-500 text-indigo-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Clock className="w-4 h-4" />
            <span>Usage Costs</span>
            <span className="ml-1.5 px-2 py-0.5 text-xs rounded-full bg-slate-800 text-slate-400">
              {usageCosts.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('departments')}
            className={`pb-4 text-sm font-medium transition flex items-center gap-2 border-b-2 ${
              activeTab === 'departments'
                ? 'border-indigo-500 text-indigo-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Building2 className="w-4 h-4" />
            <span>Department Spend</span>
            <span className="ml-1.5 px-2 py-0.5 text-xs rounded-full bg-slate-800 text-slate-400">
              {departmentSummaries.length}
            </span>
          </button>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* TAB 1: BILLING INVOICES                                                    */}
      {/* ========================================================================= */}
      {activeTab === 'invoices' && (
        <div className="space-y-5">
          {/* Controls Bar */}
          <div className="flex flex-col sm:flex-row gap-3 justify-between items-stretch sm:items-center bg-slate-900/30 p-4 rounded-xl border border-slate-800/80">
            <div className="relative flex-1 max-w-md">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="text"
                placeholder="Search by invoice number or department..."
                value={invoiceSearch}
                onChange={(e) => setInvoiceSearch(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
              />
            </div>

            <div className="flex items-center gap-2">
              <SlidersHorizontal className="w-4 h-4 text-slate-500" />
              <select
                value={invoiceStatusFilter}
                onChange={(e) => setInvoiceStatusFilter(e.target.value as InvoiceStatus | 'ALL')}
                className="px-3 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-indigo-500 transition"
              >
                <option value="ALL">All Statuses</option>
                <option value="DRAFT">Draft</option>
                <option value="ISSUED">Issued</option>
                <option value="PAID">Paid</option>
                <option value="SETTLED">Settled</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>
          </div>

          {/* Invoices Table / List */}
          {isLoadingInvoices ? (
            <div className="py-16 text-center text-slate-400 flex flex-col items-center gap-3">
              <RefreshCw className="w-6 h-6 animate-spin text-indigo-400" />
              <span>Loading billing invoices...</span>
            </div>
          ) : filteredInvoices.length === 0 ? (
            <div className="py-16 text-center bg-slate-900/20 border border-dashed border-slate-800 rounded-2xl">
              <Receipt className="w-10 h-10 text-slate-600 mx-auto mb-3" />
              <div className="text-base font-medium text-slate-300">No invoices found</div>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                {invoices.length === 0
                  ? 'No billing invoices have been generated yet. Click "Generate Invoice" to create one.'
                  : 'No invoices match your current search and filter criteria.'}
              </p>
              {invoices.length === 0 && (
                <button
                  onClick={() => setIsCreateModalOpen(true)}
                  className="mt-4 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-medium transition"
                >
                  Generate First Invoice
                </button>
              )}
            </div>
          ) : (
            <div className="bg-slate-900/30 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-300">
                  <thead className="bg-slate-900/70 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    <tr>
                      <th className="px-6 py-4">Invoice #</th>
                      <th className="px-6 py-4">Department & Inst</th>
                      <th className="px-6 py-4">Billing Period</th>
                      <th className="px-6 py-4">Line Items</th>
                      <th className="px-6 py-4">Total Amount</th>
                      <th className="px-6 py-4">Status</th>
                      <th className="px-6 py-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {filteredInvoices.map((inv) => (
                      <tr
                        key={inv.id}
                        className="hover:bg-slate-800/40 transition group cursor-pointer"
                        onClick={() => window.location.assign(`/cost/invoices/${inv.id}`)}
                      >
                        <td className="px-6 py-4 font-mono font-bold text-indigo-400 group-hover:text-indigo-300">
                          <Link
                            to={`/cost/invoices/${inv.id}`}
                            className="hover:underline flex items-center gap-1.5"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <span>{inv.invoiceNumber}</span>
                            <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition" />
                          </Link>
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-medium text-white">{inv.departmentName || `Dept #${inv.departmentId}`}</div>
                          <div className="text-xs text-slate-500">{inv.institutionName || 'Default Institution'}</div>
                        </td>
                        <td className="px-6 py-4 text-xs font-mono text-slate-400">
                          {inv.billingPeriodStart} → {inv.billingPeriodEnd}
                        </td>
                        <td className="px-6 py-4">
                          <span className="px-2.5 py-0.5 rounded-full text-xs font-mono bg-slate-800 text-slate-300 border border-slate-700/60">
                            {inv.lineItems ? inv.lineItems.length : 0} items
                          </span>
                        </td>
                        <td className="px-6 py-4 font-mono font-bold text-white text-base">
                          ${(inv.totalAmount || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </td>
                        <td className="px-6 py-4">
                          <InvoiceStatusBadge status={inv.status} />
                        </td>
                        <td className="px-6 py-4 text-right">
                          <div className="flex items-center justify-end gap-2" onClick={(e) => e.stopPropagation()}>
                            {inv.status === 'DRAFT' && (
                              <button
                                onClick={(e) => handleDeleteDraft(inv.id, e)}
                                disabled={deletingId === inv.id}
                                className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition"
                                title="Delete Draft Invoice"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            )}
                            <Link
                              to={`/cost/invoices/${inv.id}`}
                              className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium transition flex items-center gap-1"
                            >
                              <span>Inspect</span>
                              <ArrowRight className="w-3 h-3" />
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

      {/* ========================================================================= */}
      {/* TAB 2: USAGE COSTS                                                        */}
      {/* ========================================================================= */}
      {activeTab === 'usage' && (
        <div className="space-y-5">
          {/* Controls Bar */}
          <div className="flex flex-col sm:flex-row gap-3 justify-between items-stretch sm:items-center bg-slate-900/30 p-4 rounded-xl border border-slate-800/80">
            <div className="relative flex-1 max-w-md">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="text"
                placeholder="Search by booking reference, equipment, or user..."
                value={usageSearch}
                onChange={(e) => setUsageSearch(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
              />
            </div>

            <div className="flex items-center gap-2">
              <SlidersHorizontal className="w-4 h-4 text-slate-500" />
              <select
                value={usageStatusFilter}
                onChange={(e) => setUsageStatusFilter(e.target.value as BookingBillingStatus | 'ALL')}
                className="px-3 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-slate-300 focus:outline-none focus:border-indigo-500 transition"
              >
                <option value="ALL">All Billing States</option>
                <option value="UNBILLED">Unbilled</option>
                <option value="INVOICED">Invoiced</option>
                <option value="SETTLED">Settled</option>
                <option value="WAIVED">Waived</option>
              </select>
            </div>
          </div>

          {isLoadingUsage ? (
            <div className="py-16 text-center text-slate-400 flex flex-col items-center gap-3">
              <RefreshCw className="w-6 h-6 animate-spin text-indigo-400" />
              <span>Loading usage costs...</span>
            </div>
          ) : filteredUsageCosts.length === 0 ? (
            <div className="py-16 text-center bg-slate-900/20 border border-dashed border-slate-800 rounded-2xl">
              <Clock className="w-10 h-10 text-slate-600 mx-auto mb-3" />
              <div className="text-base font-medium text-slate-300">No usage cost records found</div>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                No booking usage matches your filter criteria.
              </p>
            </div>
          ) : (
            <div className="bg-slate-900/30 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-slate-300">
                  <thead className="bg-slate-900/70 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                    <tr>
                      <th className="px-6 py-4">Booking Ref</th>
                      <th className="px-6 py-4">Equipment</th>
                      <th className="px-6 py-4">User / Dept</th>
                      <th className="px-6 py-4">Hours</th>
                      <th className="px-6 py-4">Rate</th>
                      <th className="px-6 py-4">Total Cost</th>
                      <th className="px-6 py-4">Billing Status</th>
                      <th className="px-6 py-4 text-right">Invoice Ref</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {filteredUsageCosts.map((u) => (
                      <tr key={u.bookingId} className="hover:bg-slate-800/40 transition">
                        <td className="px-6 py-4 font-mono font-semibold text-indigo-400">
                          {u.bookingReference}
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-medium text-white">{u.equipmentName || `Equipment #${u.equipmentId}`}</div>
                          <div className="text-xs text-slate-500 font-mono">
                            {u.startTime?.slice(0, 10)}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="font-medium text-slate-200">{u.userName || `User #${u.userId}`}</div>
                          <div className="text-xs text-slate-500">{u.departmentName || `Dept #${u.departmentId}`}</div>
                        </td>
                        <td className="px-6 py-4 font-mono text-slate-300">
                          {u.billableHours} hrs
                        </td>
                        <td className="px-6 py-4 font-mono text-slate-400">
                          ${u.hourlyRate?.toFixed(2)}/hr
                        </td>
                        <td className="px-6 py-4 font-mono font-bold text-white">
                          ${u.totalCost?.toFixed(2)}
                        </td>
                        <td className="px-6 py-4">
                          <BookingBillingStatusBadge status={u.billingStatus} />
                        </td>
                        <td className="px-6 py-4 text-right">
                          {u.invoiceId ? (
                            <Link
                              to={`/cost/invoices/${u.invoiceId}`}
                              className="text-xs font-mono text-indigo-400 hover:text-indigo-300 hover:underline flex items-center justify-end gap-1"
                            >
                              <span>{u.invoiceNumber || `INV-${u.invoiceId}`}</span>
                              <ExternalLink className="w-3 h-3" />
                            </Link>
                          ) : (
                            <span className="text-xs text-slate-600 font-mono">—</span>
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

      {/* ========================================================================= */}
      {/* TAB 3: DEPARTMENT SPEND                                                    */}
      {/* ========================================================================= */}
      {activeTab === 'departments' && (
        <div className="space-y-5">
          {/* Controls Bar */}
          <div className="flex flex-col sm:flex-row gap-3 justify-between items-stretch sm:items-center bg-slate-900/30 p-4 rounded-xl border border-slate-800/80">
            <div className="relative flex-1 max-w-md">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="text"
                placeholder="Search department summaries..."
                value={deptSearch}
                onChange={(e) => setDeptSearch(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-slate-800/60 border border-slate-700/80 rounded-xl text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
              />
            </div>
          </div>

          {isLoadingDepartments ? (
            <div className="py-16 text-center text-slate-400 flex flex-col items-center gap-3">
              <RefreshCw className="w-6 h-6 animate-spin text-indigo-400" />
              <span>Loading department spend summaries...</span>
            </div>
          ) : filteredDepartments.length === 0 ? (
            <div className="py-16 text-center bg-slate-900/20 border border-dashed border-slate-800 rounded-2xl">
              <Building2 className="w-10 h-10 text-slate-600 mx-auto mb-3" />
              <div className="text-base font-medium text-slate-300">No departmental spend recorded</div>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                No bookings or usage charges have been attributed to departments yet.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {filteredDepartments.map((dept) => (
                <div
                  key={dept.departmentId}
                  className="bg-slate-900/40 border border-slate-800 rounded-2xl p-6 hover:border-slate-700 transition flex flex-col justify-between shadow-lg"
                >
                  <div>
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <div className="p-2.5 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
                          <Building2 className="w-5 h-5" />
                        </div>
                        <div>
                          <h3 className="text-base font-semibold text-white tracking-tight">
                            {dept.departmentName}
                          </h3>
                          <span className="text-xs text-slate-500 font-mono">
                            Dept ID #{dept.departmentId}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xl font-bold font-mono text-white">
                          ${(dept.totalCost || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </div>
                        <div className="text-[11px] text-slate-500">Total Incurred</div>
                      </div>
                    </div>

                    <div className="mt-5 grid grid-cols-2 gap-3 pt-4 border-t border-slate-800/80 text-xs">
                      <div className="p-2.5 rounded-xl bg-slate-800/40 border border-slate-800">
                        <div className="text-slate-400">Bookings Count</div>
                        <div className="text-sm font-bold text-slate-200 mt-0.5 font-mono">
                          {dept.bookingCount}
                        </div>
                      </div>
                      <div className="p-2.5 rounded-xl bg-slate-800/40 border border-slate-800">
                        <div className="text-slate-400">Billable Hours</div>
                        <div className="text-sm font-bold text-slate-200 mt-0.5 font-mono">
                          {dept.totalHours} hrs
                        </div>
                      </div>
                    </div>

                    {/* Breakdown by Billing Status */}
                    <div className="mt-4 space-y-2 text-xs">
                      <div className="flex items-center justify-between text-slate-400">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-amber-400" />
                          Unbilled
                        </span>
                        <span className="font-mono font-medium text-slate-200">
                          ${(dept.unbilledCost || 0).toFixed(2)}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-slate-400">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-blue-400" />
                          Invoiced
                        </span>
                        <span className="font-mono font-medium text-slate-200">
                          ${(dept.invoicedCost || 0).toFixed(2)}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-slate-400">
                        <span className="flex items-center gap-1.5">
                          <span className="w-2 h-2 rounded-full bg-emerald-400" />
                          Settled
                        </span>
                        <span className="font-mono font-medium text-slate-200">
                          ${(dept.settledCost || 0).toFixed(2)}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-end">
                    <button
                      onClick={() => setIsCreateModalOpen(true)}
                      className="px-3.5 py-1.5 rounded-xl bg-indigo-600/20 hover:bg-indigo-600/30 text-indigo-300 border border-indigo-500/30 text-xs font-medium transition flex items-center gap-1.5"
                    >
                      <Receipt className="w-3.5 h-3.5" />
                      <span>Invoice Department</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Invoice Creation Modal */}
      <CreateInvoiceModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={(created) => {
          showToast(`Invoice ${created.invoiceNumber} created successfully.`);
          refreshAll();
        }}
      />
    </div>
  );
};
