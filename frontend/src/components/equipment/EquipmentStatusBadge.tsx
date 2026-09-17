import React from 'react';
import type { EquipmentStatus } from '../../types/equipment';
import { CheckCircle2, Clock, AlertTriangle, XCircle, Archive } from 'lucide-react';

interface EquipmentStatusBadgeProps {
  status: EquipmentStatus;
  className?: string;
  showIcon?: boolean;
}

export const EquipmentStatusBadge: React.FC<EquipmentStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'AVAILABLE':
        return {
          label: 'Available',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: CheckCircle2,
        };
      case 'IN_USE':
        return {
          label: 'In Use',
          badgeClass: 'bg-sky-500/10 text-sky-400 border-sky-500/30',
          dotClass: 'bg-sky-400',
          icon: Clock,
        };
      case 'UNDER_MAINTENANCE':
        return {
          label: 'Under Maintenance',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: AlertTriangle,
        };
      case 'OUT_OF_SERVICE':
        return {
          label: 'Out of Service',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          dotClass: 'bg-rose-400',
          icon: XCircle,
        };
      case 'RETIRED':
        return {
          label: 'Retired',
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: Archive,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: CheckCircle2,
        };
    }
  };

  const config = getStatusConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded-full border ${config.badgeClass} ${className}`}
    >
      {showIcon ? (
        <Icon className="w-3.5 h-3.5" />
      ) : (
        <span className={`w-1.5 h-1.5 rounded-full ${config.dotClass}`} />
      )}
      <span>{config.label}</span>
    </span>
  );
};
