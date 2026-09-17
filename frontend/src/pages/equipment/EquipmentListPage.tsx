import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus,
  Search,
  SlidersHorizontal,
  RefreshCw,
  Cpu,
  Layers,
  Building2,
  DollarSign,
  ArrowRight,
  Globe,
  Lock,
  AlertCircle,
  Sparkles,
  ChevronRight,
} from 'lucide-react';
import type {
  EquipmentResponse,
  EquipmentStatus,
  EquipmentCategoryResponse,
} from '../../types/equipment';
import { getEquipmentList, getCategories } from '../../api/equipment';
import { EquipmentStatusBadge } from '../../components/equipment/EquipmentStatusBadge';
import { EquipmentFormModal } from '../../components/equipment/EquipmentFormModal';
import { StatusTransitionModal } from '../../components/equipment/StatusTransitionModal';
import { useAuth } from '../../context/useAuth';
import { canManageEquipment, canUpdateEquipmentStatus } from '../../utils/rbac';

const STATUS_FILTERS: { label: string; value: EquipmentStatus | 'ALL' }[] = [
  { label: 'All Statuses', value: 'ALL' },
  { label: 'Available', value: 'AVAILABLE' },
  { label: 'In Use', value: 'IN_USE' },
  { label: 'Under Maintenance', value: 'UNDER_MAINTENANCE' },
  { label: 'Out of Service', value: 'OUT_OF_SERVICE' },
  { label: 'Retired', value: 'RETIRED' },
];

export const EquipmentListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  // Data state
  const [equipmentList, setEquipmentList] = useState<EquipmentResponse[]>([]);
  const [categories, setCategories] = useState<EquipmentCategoryResponse[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successToast, setSuccessToast] = useState<string | null>(null);

  // Filters state
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<EquipmentStatus | 'ALL'>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<number | 'ALL'>('ALL');

  // Modals state
  const [isAddModalOpen, setIsAddModalOpen] = useState<boolean>(false);
  const [statusModalEquipment, setStatusModalEquipment] = useState<EquipmentResponse | null>(null);

  const fetchEquipment = useCallback(async () => {
    setError(null);
    try {
      const [equipData, catData] = await Promise.all([
        getEquipmentList(),
        getCategories().catch(() => []),
      ]);
      setEquipmentList(equipData);
      setCategories(catData);
    } catch (err: unknown) {
      console.error('Failed to load equipment list:', err);
      setError('Failed to load equipment catalog from backend service.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEquipment();
  }, [fetchEquipment]);

  // Filtered Equipment list using client-side matching
  const filteredEquipment = useMemo(() => {
    return equipmentList.filter((item) => {
      // Status match
      if (statusFilter !== 'ALL' && item.status !== statusFilter) {
        return false;
      }

      // Category match
      if (categoryFilter !== 'ALL' && item.categoryId !== categoryFilter) {
        return false;
      }

      // Search match (name, assetTag, serialNumber)
      if (searchQuery.trim()) {
        const query = searchQuery.trim().toLowerCase();
        const matchesName = item.name?.toLowerCase().includes(query);
        const matchesTag = item.assetTag?.toLowerCase().includes(query);
        const matchesSerial = item.serialNumber?.toLowerCase().includes(query);
        const matchesModel = item.modelNumber?.toLowerCase().includes(query);
        return matchesName || matchesTag || matchesSerial || matchesModel;
      }

      return true;
    });
  }, [equipmentList, statusFilter, categoryFilter, searchQuery]);

  const showToast = (message: string) => {
    setSuccessToast(message);
    setTimeout(() => {
      setSuccessToast(null);
    }, 4000);
  };

  const handleEquipmentCreated = (saved: EquipmentResponse) => {
    showToast(`Instrument "${saved.name}" (${saved.assetTag}) created successfully.`);
    fetchEquipment();
  };

  const handleStatusUpdated = (updated: EquipmentResponse) => {
    showToast(`Status for "${updated.assetTag}" updated to ${updated.status}.`);
    setEquipmentList((prev) => prev.map((eq) => (eq.id === updated.id ? updated : eq)));
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Toast Notification */}
      {successToast && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 bg-emerald-500/90 backdrop-blur-md text-white rounded-xl shadow-2xl border border-emerald-400/40 text-xs font-medium animate-in fade-in slide-in-from-bottom-5">
          <Sparkles className="w-4 h-4 text-emerald-200" />
          <span>{successToast}</span>
        </div>
      )}

      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
              Equipment Catalog
            </h1>
            <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-slate-800 text-sky-400 border border-slate-700/60">
              {filteredEquipment.length} {filteredEquipment.length === 1 ? 'item' : 'items'}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Registered instruments, technical parameters, and operational status tracking.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={fetchEquipment}
            disabled={isLoading}
            className="flex items-center gap-2 px-3 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-900 border border-slate-800 rounded-xl hover:bg-slate-800 transition-colors disabled:opacity-50"
            title="Refresh list"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          {canManageEquipment(user?.roles) && (
            <button
              type="button"
              onClick={() => setIsAddModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl shadow-lg shadow-sky-600/20 transition-all hover:shadow-sky-600/30"
            >
              <Plus className="w-4 h-4" />
              <span>Add Equipment</span>
            </button>
          )}
        </div>
      </div>

      {/* Filters & Search Toolbar */}
      <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 shadow-md space-y-3">
        <div className="flex flex-col md:flex-row gap-3">
          {/* Search bar */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by equipment name, asset tag, serial, or model..."
              className="w-full pl-9 pr-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[10px] text-slate-400 hover:text-white px-1.5 py-0.5 rounded-sm bg-slate-800"
              >
                Clear
              </button>
            )}
          </div>

          {/* Category Dropdown */}
          <div className="w-full md:w-56">
            <select
              value={categoryFilter}
              onChange={(e) =>
                setCategoryFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))
              }
              className="w-full px-3.5 py-2 text-xs text-white bg-slate-950 border border-slate-800 rounded-xl focus:outline-hidden focus:ring-2 focus:ring-sky-500/30 focus:border-sky-500 transition-colors"
            >
              <option value="ALL">All Categories</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Status Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 pt-1 scrollbar-none">
          <span className="text-[11px] font-medium text-slate-500 mr-1.5 flex items-center gap-1 shrink-0">
            <SlidersHorizontal className="w-3 h-3" /> Status:
          </span>
          {STATUS_FILTERS.map((filter) => {
            const isActive = statusFilter === filter.value;
            return (
              <button
                key={filter.value}
                type="button"
                onClick={() => setStatusFilter(filter.value)}
                className={`px-3 py-1 text-xs font-medium rounded-lg border transition-colors shrink-0 ${
                  isActive
                    ? 'bg-sky-600/20 text-sky-300 border-sky-500/40 shadow-xs'
                    : 'bg-slate-950 text-slate-400 border-slate-800 hover:text-slate-200 hover:border-slate-700'
                }`}
              >
                {filter.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Error State */}
      {error && (
        <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-300 flex items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2.5">
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
            <span>{error}</span>
          </div>
          <button
            type="button"
            onClick={fetchEquipment}
            className="px-3 py-1 rounded-lg bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 transition-colors"
          >
            Retry
          </button>
        </div>
      )}

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="space-y-3">
          {[1, 2, 3, 4].map((n) => (
            <div
              key={n}
              className="p-5 rounded-2xl bg-slate-900 border border-slate-800 animate-pulse flex items-center justify-between"
            >
              <div className="space-y-2.5">
                <div className="w-48 h-4 rounded-sm bg-slate-800" />
                <div className="w-32 h-3 rounded-sm bg-slate-800/60" />
              </div>
              <div className="w-24 h-6 rounded-full bg-slate-800" />
            </div>
          ))}
        </div>
      )}

      {/* Empty State: Zero equipment registered */}
      {!isLoading && !error && equipmentList.length === 0 && (
        <div className="p-12 text-center rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-4">
          <div className="w-16 h-16 mx-auto rounded-2xl bg-slate-800/80 border border-slate-700 flex items-center justify-center text-slate-400">
            <Cpu className="w-8 h-8" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white tracking-tight">
              No equipment registered
            </h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto mt-1">
              There are currently no instruments registered in this platform instance.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsAddModalOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-xl transition-colors shadow-md shadow-sky-600/20"
          >
            <Plus className="w-4 h-4" />
            <span>Register First Instrument</span>
          </button>
        </div>
      )}

      {/* Empty State: Filters yielded zero matches */}
      {!isLoading && !error && equipmentList.length > 0 && filteredEquipment.length === 0 && (
        <div className="p-10 text-center rounded-2xl bg-slate-900 border border-slate-800 space-y-3">
          <div className="w-12 h-12 mx-auto rounded-xl bg-slate-800 flex items-center justify-center text-slate-400">
            <Search className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-semibold text-white">No equipment matching filters</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">
            Try adjusting your search query, status selector, or category filter to discover
            registered instruments.
          </p>
          <button
            type="button"
            onClick={() => {
              setSearchQuery('');
              setStatusFilter('ALL');
              setCategoryFilter('ALL');
            }}
            className="px-3.5 py-1.5 text-xs text-sky-400 hover:text-sky-300 font-medium"
          >
            Reset all filters
          </button>
        </div>
      )}

      {/* Equipment Table (Desktop) */}
      {!isLoading && !error && filteredEquipment.length > 0 && (
        <>
          <div className="hidden lg:block overflow-hidden rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
            <table className="w-full text-left text-xs text-slate-300 border-collapse">
              <thead className="bg-slate-950/80 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">Instrument / Asset</th>
                  <th className="py-3.5 px-4">Category & Location</th>
                  <th className="py-3.5 px-4">Operational Status</th>
                  <th className="py-3.5 px-4">Rates ($/hr)</th>
                  <th className="py-3.5 px-4">Sharing & Access</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {filteredEquipment.map((eq) => (
                  <tr
                    key={eq.id}
                    onClick={() => navigate(`/equipment/${eq.id}`)}
                    className="hover:bg-slate-800/40 transition-colors cursor-pointer group"
                  >
                    {/* Instrument / Asset */}
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white group-hover:text-sky-400 transition-colors">
                        {eq.name}
                      </div>
                      <div className="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
                        <span className="font-mono bg-slate-950 px-1.5 py-0.5 rounded-sm border border-slate-800 text-slate-300">
                          {eq.assetTag}
                        </span>
                        <span>•</span>
                        <span>SN: {eq.serialNumber}</span>
                      </div>
                    </td>

                    {/* Category & Location */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-1.5 text-slate-200">
                        <Layers className="w-3.5 h-3.5 text-sky-400" />
                        <span>{eq.categoryName || 'General'}</span>
                      </div>
                      <div className="flex items-center gap-1.5 mt-1 text-[11px] text-slate-400">
                        <Building2 className="w-3 h-3 text-slate-500" />
                        <span>
                          {eq.locationBuilding}, Rm {eq.locationRoom}
                        </span>
                      </div>
                    </td>

                    {/* Operational Status */}
                    <td className="py-3.5 px-4">
                      <EquipmentStatusBadge status={eq.status} />
                    </td>

                    {/* Rates */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-1 text-slate-200">
                        <DollarSign className="w-3 h-3 text-emerald-400" />
                        <span>
                          Int: {eq.hourlyRateInternal !== null ? `$${eq.hourlyRateInternal}` : '—'}
                        </span>
                      </div>
                      <div className="text-[11px] text-slate-400 mt-0.5 pl-4">
                        Ext: {eq.hourlyRateExternal !== null ? `$${eq.hourlyRateExternal}` : '—'}
                      </div>
                    </td>

                    {/* Sharing & Access */}
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        {eq.isShareableExternally ? (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-medium rounded-md bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                            <Globe className="w-3 h-3" /> Shared
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-medium rounded-md bg-slate-800 text-slate-400 border border-slate-700">
                            <Lock className="w-3 h-3" /> Internal
                          </span>
                        )}
                        {eq.requiresApproval && (
                          <span className="text-[10px] text-amber-400 font-medium">Approval</span>
                        )}
                      </div>
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {canUpdateEquipmentStatus(user?.roles) && (
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              setStatusModalEquipment(eq);
                            }}
                            className="px-2.5 py-1 text-[11px] font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors border border-slate-700/60"
                          >
                            Status
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/equipment/${eq.id}`);
                          }}
                          className="p-1 text-slate-400 hover:text-sky-400 rounded-lg hover:bg-slate-800 transition-colors"
                        >
                          <ChevronRight className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Equipment Cards (Mobile / Tablet) */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:hidden gap-4">
            {filteredEquipment.map((eq) => (
              <div
                key={eq.id}
                onClick={() => navigate(`/equipment/${eq.id}`)}
                className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-md hover:border-slate-700 transition-all cursor-pointer space-y-3"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="text-sm font-bold text-white tracking-tight">{eq.name}</h3>
                    <div className="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
                      <span className="font-mono bg-slate-950 px-1.5 py-0.5 rounded-sm border border-slate-800 text-slate-300">
                        {eq.assetTag}
                      </span>
                      <span>•</span>
                      <span>SN: {eq.serialNumber}</span>
                    </div>
                  </div>
                  <EquipmentStatusBadge status={eq.status} />
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs pt-1 border-t border-slate-800/60">
                  <div className="space-y-1">
                    <div className="text-[11px] text-slate-500">Category & Location</div>
                    <div className="font-medium text-slate-200 truncate">
                      {eq.categoryName || 'General'}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {eq.locationBuilding}, Rm {eq.locationRoom}
                    </div>
                  </div>

                  <div className="space-y-1">
                    <div className="text-[11px] text-slate-500">Hourly Rates</div>
                    <div className="font-medium text-slate-200">
                      Int: {eq.hourlyRateInternal !== null ? `$${eq.hourlyRateInternal}/hr` : '—'}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      Ext: {eq.hourlyRateExternal !== null ? `$${eq.hourlyRateExternal}/hr` : '—'}
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 text-xs">
                  <div className="flex items-center gap-2">
                    {eq.isShareableExternally ? (
                      <span className="text-[10px] text-emerald-400 flex items-center gap-1">
                        <Globe className="w-3 h-3" /> Shared
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-500 flex items-center gap-1">
                        <Lock className="w-3 h-3" /> Internal
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    {canUpdateEquipmentStatus(user?.roles) && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setStatusModalEquipment(eq);
                        }}
                        className="px-2.5 py-1 text-[11px] font-medium text-slate-300 bg-slate-800 rounded-lg hover:bg-slate-700"
                      >
                        Status
                      </button>
                    )}
                    <span className="text-sky-400 font-medium text-[11px] flex items-center gap-1">
                      Details <ArrowRight className="w-3 h-3" />
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* Modal: Add Equipment */}
      {isAddModalOpen && (
        <EquipmentFormModal
          isOpen={isAddModalOpen}
          onClose={() => setIsAddModalOpen(false)}
          onSuccess={handleEquipmentCreated}
        />
      )}

      {/* Modal: Status Transition */}
      {statusModalEquipment && (
        <StatusTransitionModal
          isOpen={Boolean(statusModalEquipment)}
          equipment={statusModalEquipment}
          onClose={() => setStatusModalEquipment(null)}
          onSuccess={handleStatusUpdated}
        />
      )}
    </div>
  );
};
