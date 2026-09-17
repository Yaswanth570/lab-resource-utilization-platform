import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  Handshake,
  Layers,
  CalendarCheck,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Plus,
  ArrowRight,
  ExternalLink,
  Building2,
  Info,
  CheckCircle2,
} from 'lucide-react';
import type {
  ResourceSharingAgreementResponse,
  SharingAgreementStatus,
  SharedEquipmentAllocationResponse,
} from '../../types/sharing';
import type { EquipmentResponse, InstitutionLookup } from '../../types/equipment';
import type { BookingResponse } from '../../types/booking';
import {
  getSharingAgreements,
  getSharedAllocations,
  getExternallyShareableEquipment,
  getExternalBookings,
  getInstitutions,
} from '../../api/sharing';
import { SharingAgreementStatusBadge } from '../../components/sharing/SharingAgreementStatusBadge';
import { BookingStatusBadge } from '../../components/booking/BookingStatusBadge';
import { CreateAgreementModal } from '../../components/sharing/CreateAgreementModal';
import { CreateSharedAllocationModal } from '../../components/sharing/CreateSharedAllocationModal';

type ActiveTab = 'agreements' | 'allocations' | 'bookings';

export const SharingPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ActiveTab>('allocations');

  // Shared Reference Data
  const [institutions, setInstitutions] = useState<InstitutionLookup[]>([]);
  const [shareableEquipment, setShareableEquipment] = useState<EquipmentResponse[]>([]);
  const [externalBookings, setExternalBookings] = useState<BookingResponse[]>([]);

  // Agreements State
  const [agreements, setAgreements] = useState<ResourceSharingAgreementResponse[]>([]);
  const [isLoadingAgreements, setIsLoadingAgreements] = useState<boolean>(false);
  const [agreementBackendMissing, setAgreementBackendMissing] = useState<boolean>(false);
  const [agreementSearch, setAgreementSearch] = useState<string>('');
  const [agreementStatusFilter, setAgreementStatusFilter] = useState<SharingAgreementStatus | 'ALL'>('ALL');

  // Allocations State
  const [allocations, setAllocations] = useState<SharedEquipmentAllocationResponse[]>([]);
  const [isLoadingAllocations, setIsLoadingAllocations] = useState<boolean>(false);
  const [allocationBackendMissing, setAllocationBackendMissing] = useState<boolean>(false);
  const [equipmentSearch, setEquipmentSearch] = useState<string>('');
  const [equipmentCategoryFilter, setEquipmentCategoryFilter] = useState<string>('ALL');

  // Booking Context State
  const [isLoadingBookings, setIsLoadingBookings] = useState<boolean>(false);
  const [bookingSearch, setBookingSearch] = useState<string>('');

  // Modals & Notifications
  const [isCreateAgreementOpen, setIsCreateAgreementOpen] = useState<boolean>(false);
  const [isCreateAllocationOpen, setIsCreateAllocationOpen] = useState<boolean>(false);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setSuccessToast(msg);
    setTimeout(() => setSuccessToast(null), 4000);
  };

  // --- Data Loading ---
  const loadReferenceData = useCallback(async () => {
    try {
      const instData = await getInstitutions();
      setInstitutions(instData);
    } catch (err) {
      console.warn('Could not load institution roster:', err);
    }

    try {
      const eqData = await getExternallyShareableEquipment();
      setShareableEquipment(eqData);
    } catch (err) {
      console.error('Failed to load externally shareable equipment:', err);
    }

    setIsLoadingBookings(true);
    try {
      const bData = await getExternalBookings();
      setExternalBookings(bData);
    } catch (err) {
      console.warn('Could not load external bookings:', err);
    } finally {
      setIsLoadingBookings(false);
    }
  }, []);

  const loadAgreements = useCallback(async () => {
    setIsLoadingAgreements(true);
    setAgreementBackendMissing(false);
    try {
      const data = await getSharingAgreements();
      setAgreements(data);
    } catch (err: unknown) {
      // Cleanly handle missing backend endpoint without breaking the application
      setAgreementBackendMissing(true);
    } finally {
      setIsLoadingAgreements(false);
    }
  }, []);

  const loadAllocations = useCallback(async () => {
    setIsLoadingAllocations(true);
    setAllocationBackendMissing(false);
    try {
      const data = await getSharedAllocations();
      setAllocations(data);
    } catch (err: unknown) {
      setAllocationBackendMissing(true);
    } finally {
      setIsLoadingAllocations(false);
    }
  }, []);

  useEffect(() => {
    loadReferenceData();
    loadAgreements();
    loadAllocations();
  }, [loadReferenceData, loadAgreements, loadAllocations]);

  // --- Filtering ---
  const filteredAgreements = useMemo(() => {
    return agreements.filter((ag) => {
      if (agreementStatusFilter !== 'ALL' && ag.status !== agreementStatusFilter) return false;
      if (agreementSearch.trim()) {
        const q = agreementSearch.toLowerCase();
        const matchCode = ag.agreementCode.toLowerCase().includes(q);
        const matchOwner = ag.ownerInstitutionName ? ag.ownerInstitutionName.toLowerCase().includes(q) : false;
        const matchReq = ag.requestingInstitutionName ? ag.requestingInstitutionName.toLowerCase().includes(q) : false;
        if (!matchCode && !matchOwner && !matchReq) return false;
      }
      return true;
    });
  }, [agreements, agreementStatusFilter, agreementSearch]);

  const filteredEquipment = useMemo(() => {
    return shareableEquipment.filter((eq) => {
      if (equipmentCategoryFilter !== 'ALL' && eq.categoryName !== equipmentCategoryFilter) return false;
      if (equipmentSearch.trim()) {
        const q = equipmentSearch.toLowerCase();
        const matchName = eq.name.toLowerCase().includes(q);
        const matchTag = eq.assetTag.toLowerCase().includes(q);
        const matchCat = eq.categoryName.toLowerCase().includes(q);
        if (!matchName && !matchTag && !matchCat) return false;
      }
      return true;
    });
  }, [shareableEquipment, equipmentCategoryFilter, equipmentSearch]);

  const categories = useMemo(() => {
    const set = new Set<string>();
    shareableEquipment.forEach((eq) => {
      if (eq.categoryName) set.add(eq.categoryName);
    });
    return Array.from(set);
  }, [shareableEquipment]);

  const filteredBookings = useMemo(() => {
    return externalBookings.filter((b) => {
      if (bookingSearch.trim()) {
        const q = bookingSearch.toLowerCase();
        const matchRef = b.bookingReference.toLowerCase().includes(q);
        const matchEq = b.equipmentName.toLowerCase().includes(q);
        const matchInst = b.institutionName ? b.institutionName.toLowerCase().includes(q) : false;
        const matchUser = b.userName.toLowerCase().includes(q);
        if (!matchRef && !matchEq && !matchInst && !matchUser) return false;
      }
      return true;
    });
  }, [externalBookings, bookingSearch]);

  const getInstitutionName = useCallback(
    (id?: number | null) => {
      if (!id) return 'External Partner';
      const inst = institutions.find((i) => i.id === id);
      return inst ? `${inst.name} (${inst.code})` : `Institution #${id}`;
    },
    [institutions]
  );

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed top-20 right-6 z-50 p-4 bg-emerald-950/90 border border-emerald-500/40 text-emerald-200 rounded-xl shadow-2xl flex items-center gap-3 text-xs animate-in fade-in slide-in-from-top-3 duration-200">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2.5">
            <Handshake className="w-7 h-7 text-indigo-400" />
            Resource Sharing Management
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Administer inter-institutional agreements, shared instrument quotas, and external reservation allocations.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => {
              loadReferenceData();
              loadAgreements();
              loadAllocations();
            }}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-850 hover:bg-slate-800 border border-slate-700/80 rounded-xl transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </button>

          {activeTab === 'agreements' && (
            <button
              type="button"
              onClick={() => setIsCreateAgreementOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>New Agreement</span>
            </button>
          )}

          {activeTab === 'allocations' && (
            <button
              type="button"
              onClick={() => setIsCreateAllocationOpen(true)}
              className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Allocate Instrument</span>
            </button>
          )}
        </div>
      </div>

      {/* Tabs Navigation */}
      <div className="flex border-b border-slate-800">
        <button
          type="button"
          onClick={() => setActiveTab('allocations')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'allocations'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Shared Equipment Allocations</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {shareableEquipment.length}
          </span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('bookings')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'bookings'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <CalendarCheck className="w-4 h-4" />
          <span>Shared Booking Context</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {externalBookings.length}
          </span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('agreements')}
          className={`flex items-center gap-2 px-5 py-3 text-xs font-semibold border-b-2 transition-all ${
            activeTab === 'agreements'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
          }`}
        >
          <Handshake className="w-4 h-4" />
          <span>Sharing Agreements</span>
          <span className="ml-1.5 px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
            {agreements.length}
          </span>
        </button>
      </div>

      {/* =========================================================
          TAB 1: SHARED EQUIPMENT ALLOCATIONS
      ========================================================= */}
      {activeTab === 'allocations' && (
        <div className="space-y-6">
          {/* Honest Metric KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-slate-400">Shareable Instruments</p>
              <p className="text-2xl font-bold text-white mt-1 font-mono">{shareableEquipment.length}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-emerald-400">Available For External Booking</p>
              <p className="text-2xl font-bold text-emerald-300 mt-1 font-mono">
                {shareableEquipment.filter((e) => e.status === 'AVAILABLE').length}
              </p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-indigo-400">Configured External Rates</p>
              <p className="text-2xl font-bold text-indigo-300 mt-1 font-mono">
                {shareableEquipment.filter((e) => e.hourlyRateExternal !== null).length}
              </p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-cyan-400">Allocated Instrument Records</p>
              <p className="text-2xl font-bold text-cyan-300 mt-1 font-mono">
                {isLoadingAllocations ? '...' : allocations.length}
              </p>
            </div>
          </div>

          {/* Allocation Endpoint Missing Banner */}
          {allocationBackendMissing && (
            <div className="p-4 bg-slate-850/80 border border-indigo-500/20 rounded-2xl flex items-start gap-3 text-xs text-slate-300">
              <Info className="w-5 h-5 text-indigo-400 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-white">External Sharing Inventory</p>
                <p className="mt-0.5 text-slate-400 leading-relaxed">
                  Displaying real platform instruments from the Equipment Catalog configured with external sharing eligibility (<code className="text-indigo-300">isShareableExternally = true</code>). When a dedicated allocation controller is enabled, custom partner quotas will appear here.
                </p>
              </div>
            </div>
          )}

          {/* Search & Filter */}
          <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl space-y-3">
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={equipmentSearch}
                  onChange={(e) => setEquipmentSearch(e.target.value)}
                  placeholder="Search shared instruments by name, asset tag, category..."
                  className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>

              <div className="flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4 text-slate-400 shrink-0" />
                <select
                  value={equipmentCategoryFilter}
                  onChange={(e) => setEquipmentCategoryFilter(e.target.value)}
                  className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                >
                  <option value="ALL">All Categories</option>
                  {categories.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Instruments Table */}
          {filteredEquipment.length === 0 ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
              <Layers className="w-10 h-10 text-slate-600 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No Shareable Equipment Found</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                No instruments in the catalog currently have external resource sharing enabled.
              </p>
              <Link
                to="/equipment"
                className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
              >
                <span>Browse Equipment Catalog</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">Instrument</th>
                      <th className="py-3.5 px-4">Asset Tag</th>
                      <th className="py-3.5 px-4">Category</th>
                      <th className="py-3.5 px-4">Operational Status</th>
                      <th className="py-3.5 px-4">External Rate</th>
                      <th className="py-3.5 px-4">Sharing Policy</th>
                      <th className="py-3.5 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredEquipment.map((eq) => (
                      <tr key={eq.id} className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3.5 px-4 font-medium text-white">
                          <Link
                            to={`/equipment/${eq.id}`}
                            className="text-indigo-400 hover:underline inline-flex items-center gap-1 font-semibold"
                          >
                            <span>{eq.name}</span>
                            <ExternalLink className="w-3 h-3 text-slate-500" />
                          </Link>
                        </td>
                        <td className="py-3.5 px-4 font-mono text-slate-400">{eq.assetTag}</td>
                        <td className="py-3.5 px-4 text-slate-300">{eq.categoryName}</td>
                        <td className="py-3.5 px-4">
                          <span
                            className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-mono text-[11px] ${
                              eq.status === 'AVAILABLE'
                                ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                                : eq.status === 'UNDER_MAINTENANCE'
                                ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30'
                                : 'bg-slate-500/10 text-slate-400 border border-slate-500/30'
                            }`}
                          >
                            {eq.status}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 font-mono font-medium text-emerald-300">
                          {eq.hourlyRateExternal !== null ? `$${eq.hourlyRateExternal.toFixed(2)}/hr` : '—'}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-indigo-300 bg-indigo-500/10 border border-indigo-500/30 px-2 py-0.5 rounded-md">
                            External Shareable
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <Link
                            to={`/equipment/${eq.id}`}
                            className="px-2.5 py-1 text-[11px] font-semibold text-slate-300 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors inline-flex items-center gap-1"
                          >
                            <span>View Details</span>
                            <ArrowRight className="w-3 h-3" />
                          </Link>
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

      {/* =========================================================
          TAB 2: SHARED BOOKING CONTEXT
      ========================================================= */}
      {activeTab === 'bookings' && (
        <div className="space-y-6">
          {/* Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-slate-400">Total External Bookings</p>
              <p className="text-2xl font-bold text-white mt-1 font-mono">{externalBookings.length}</p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-indigo-400">With Linked Allocation ID</p>
              <p className="text-2xl font-bold text-indigo-300 mt-1 font-mono">
                {externalBookings.filter((b) => b.sharedAllocationId !== null).length}
              </p>
            </div>
            <div className="p-4 bg-slate-850/60 border border-slate-800/80 rounded-2xl">
              <p className="text-xs text-emerald-400">Active / Confirmed</p>
              <p className="text-2xl font-bold text-emerald-300 mt-1 font-mono">
                {externalBookings.filter((b) => b.status === 'CONFIRMED' || b.status === 'IN_USE').length}
              </p>
            </div>
          </div>

          {/* Search */}
          <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={bookingSearch}
                onChange={(e) => setBookingSearch(e.target.value)}
                placeholder="Search external bookings by reference, instrument, partner institution..."
                className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
              />
            </div>
          </div>

          {/* Bookings Table */}
          {isLoadingBookings ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl">
              <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
              <p className="text-xs text-slate-400 mt-3">Loading external booking context...</p>
            </div>
          ) : filteredBookings.length === 0 ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
              <CalendarCheck className="w-10 h-10 text-slate-600 mx-auto" />
              <h3 className="text-sm font-semibold text-white">No External Reservations Logged</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                No external cross-institutional reservations currently exist in the platform.
              </p>
              <Link
                to="/bookings"
                className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
              >
                <span>View Booking Management</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">Booking Ref</th>
                      <th className="py-3.5 px-4">Instrument</th>
                      <th className="py-3.5 px-4">Requesting Institution</th>
                      <th className="py-3.5 px-4">Allocation Link</th>
                      <th className="py-3.5 px-4">Status</th>
                      <th className="py-3.5 px-4">Schedule</th>
                      <th className="py-3.5 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60 text-slate-300">
                    {filteredBookings.map((b) => (
                      <tr key={b.id} className="hover:bg-slate-800/30 transition-colors">
                        <td className="py-3.5 px-4 font-mono font-medium text-indigo-400">
                          <Link
                            to={`/bookings/${b.id}`}
                            className="hover:underline inline-flex items-center gap-1"
                          >
                            <span>{b.bookingReference}</span>
                            <ExternalLink className="w-3 h-3 text-slate-500" />
                          </Link>
                        </td>
                        <td className="py-3.5 px-4">
                          <Link
                            to={`/equipment/${b.equipmentId}`}
                            className="text-white hover:text-indigo-400 font-medium hover:underline inline-flex items-center gap-1"
                          >
                            <span>{b.equipmentName}</span>
                          </Link>
                        </td>
                        <td className="py-3.5 px-4 text-slate-300">
                          <div className="flex items-center gap-1.5">
                            <Building2 className="w-3.5 h-3.5 text-slate-500" />
                            <span>{b.institutionName || 'External Partner'}</span>
                          </div>
                        </td>
                        <td className="py-3.5 px-4 font-mono">
                          {b.sharedAllocationId ? (
                            <span className="text-indigo-300 font-semibold">Allocation #{b.sharedAllocationId}</span>
                          ) : (
                            <span className="text-slate-500">—</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <BookingStatusBadge status={b.status} />
                        </td>
                        <td className="py-3.5 px-4 text-slate-400 text-[11px] font-mono">
                          <div>{new Date(b.startTime).toLocaleDateString()}</div>
                          <div className="text-slate-500">
                            {new Date(b.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                            {new Date(b.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </div>
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <Link
                            to={`/bookings/${b.id}`}
                            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors inline-flex items-center"
                            title="Open Booking Details"
                          >
                            <ArrowRight className="w-4 h-4" />
                          </Link>
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

      {/* =========================================================
          TAB 3: SHARING AGREEMENTS
      ========================================================= */}
      {activeTab === 'agreements' && (
        <div className="space-y-6">
          {/* Missing Backend Capabilities Alert */}
          {agreementBackendMissing ? (
            <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
              <div className="flex items-start gap-3">
                <Info className="w-6 h-6 text-indigo-400 shrink-0 mt-0.5" />
                <div>
                  <h3 className="text-base font-semibold text-white">Sharing Agreement REST APIs Pending</h3>
                  <p className="text-xs text-slate-300 mt-1 leading-relaxed">
                    The backend JPA entities (<code className="text-indigo-300">ResourceSharingAgreement</code>, <code className="text-indigo-300">SharingAgreementStatus</code>) and repositories are fully defined, but dedicated REST endpoints (<code className="text-slate-200">/api/sharing/agreements</code>) were omitted during the initial REST API phase (Task 1E.7).
                  </p>
                  <p className="text-xs text-slate-400 mt-2">
                    In compliance with the project specifications, the frontend does not fabricate mock agreement records. You can manage existing shared resources in the <strong>Shared Equipment Allocations</strong> and <strong>Shared Booking Context</strong> tabs above.
                  </p>
                </div>
              </div>

              <div className="pt-2 flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => setIsCreateAgreementOpen(true)}
                  className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
                >
                  Draft New Agreement
                </button>
                <button
                  type="button"
                  onClick={loadAgreements}
                  className="px-4 py-2 text-xs font-semibold text-slate-300 bg-slate-800 hover:bg-slate-700 rounded-xl"
                >
                  Retry Connection
                </button>
              </div>
            </div>
          ) : isLoadingAgreements ? (
            <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl">
              <RefreshCw className="w-8 h-8 text-indigo-400 animate-spin mx-auto" />
              <p className="text-xs text-slate-400 mt-3">Loading sharing agreements...</p>
            </div>
          ) : (
            <div className="space-y-4">
              {/* Search & Filter Controls */}
              <div className="p-4 bg-slate-900 border border-slate-800/80 rounded-2xl space-y-3">
                <div className="flex flex-col sm:flex-row gap-3">
                  <div className="relative flex-1">
                    <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="text"
                      value={agreementSearch}
                      onChange={(e) => setAgreementSearch(e.target.value)}
                      placeholder="Search agreements by code or institution..."
                      className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                    />
                  </div>
                  <div className="flex items-center gap-2">
                    <SlidersHorizontal className="w-4 h-4 text-slate-400 shrink-0" />
                    <select
                      value={agreementStatusFilter}
                      onChange={(e) => setAgreementStatusFilter(e.target.value as SharingAgreementStatus | 'ALL')}
                      className="px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                    >
                      <option value="ALL">All Statuses</option>
                      <option value="ACTIVE">Active</option>
                      <option value="SUSPENDED">Suspended</option>
                      <option value="TERMINATED">Terminated</option>
                      <option value="EXPIRED">Expired</option>
                    </select>
                  </div>
                </div>
              </div>

              {filteredAgreements.length === 0 ? (
                <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl space-y-3">
                  <Handshake className="w-10 h-10 text-slate-600 mx-auto" />
                  <h3 className="text-sm font-semibold text-white">No Sharing Agreements Found</h3>
                  <p className="text-xs text-slate-400 max-w-sm mx-auto">
                    No formal cross-institutional sharing agreements have been entered into the system.
                  </p>
                  <button
                    type="button"
                    onClick={() => setIsCreateAgreementOpen(true)}
                    className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/20"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Create Agreement</span>
                  </button>
                </div>
              ) : (
                <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-850/80 border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider">
                        <tr>
                          <th className="py-3.5 px-4">Agreement Code</th>
                          <th className="py-3.5 px-4">Owner Institution</th>
                          <th className="py-3.5 px-4">Partner Institution</th>
                          <th className="py-3.5 px-4">Rate Multiplier</th>
                          <th className="py-3.5 px-4">Monthly Quota</th>
                          <th className="py-3.5 px-4">Status</th>
                          <th className="py-3.5 px-4">Validity</th>
                          <th className="py-3.5 px-4 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-800/60 text-slate-300">
                        {filteredAgreements.map((ag) => (
                          <tr key={ag.id} className="hover:bg-slate-800/30 transition-colors">
                            <td className="py-3.5 px-4 font-mono font-medium text-indigo-400">
                              <Link to={`/sharing/agreements/${ag.id}`} className="hover:underline">
                                {ag.agreementCode}
                              </Link>
                            </td>
                            <td className="py-3.5 px-4 text-white font-medium">
                              {ag.ownerInstitutionName || getInstitutionName(ag.ownerInstitutionId)}
                            </td>
                            <td className="py-3.5 px-4 text-slate-300">
                              {ag.requestingInstitutionName || getInstitutionName(ag.requestingInstitutionId)}
                            </td>
                        <td className="py-3.5 px-4 font-mono text-emerald-300">
                          {ag.billingRateMultiplier ? `${ag.billingRateMultiplier}x` : '1.0x'}
                        </td>
                        <td className="py-3.5 px-4 font-mono text-slate-300">
                          {ag.maxMonthlyHours ? `${ag.maxMonthlyHours} hrs/mo` : 'Unlimited'}
                        </td>
                        <td className="py-3.5 px-4">
                          <SharingAgreementStatusBadge status={ag.status} />
                        </td>
                        <td className="py-3.5 px-4 text-slate-400 font-mono text-[11px]">
                          {ag.startDate} to {ag.endDate}
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <Link
                            to={`/sharing/agreements/${ag.id}`}
                            className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors inline-flex items-center"
                            title="View Agreement Details"
                          >
                            <ArrowRight className="w-4 h-4" />
                          </Link>
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
        </div>
      )}

      {/* Modals */}
      <CreateAgreementModal
        isOpen={isCreateAgreementOpen}
        onClose={() => setIsCreateAgreementOpen(false)}
        onSuccess={(created) => {
          setAgreements((prev) => [created, ...prev]);
          showToast(`Agreement ${created.agreementCode} established.`);
        }}
      />

      <CreateSharedAllocationModal
        isOpen={isCreateAllocationOpen}
        availableAgreements={agreements}
        onClose={() => setIsCreateAllocationOpen(false)}
        onSuccess={(created) => {
          setAllocations((prev) => [created, ...prev]);
          showToast(`Instrument allocated successfully.`);
        }}
      />
    </div>
  );
};
