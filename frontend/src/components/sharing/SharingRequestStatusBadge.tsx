import React from 'react';
import type { SharingRequestStatus } from '../../types/sharing';
import { Clock, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

interface SharingRequestStatusBadgeProps {
  status: SharingRequestStatus;
  className?: string;
  showIcon?: boolean;
}

export const SharingRequestStatusBadge: React.FC<SharingRequestStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getConfig = () => {
    switch (status) {
      case 'PENDING':
        return {
          label: 'Pending Review',
          badgeClass: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
          dotClass: 'bg-cyan-400',
          icon: Clock,
          pulse: true,
        };
      case 'APPROVED':
        return {
          label: 'Approved',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: CheckCircle2,
          pulse: false,
        };
      case 'REJECTED':
        return {
          label: 'Rejected',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          dotClass: 'bg-rose-400',
          icon: XCircle,
          pulse: false,
        };
      case 'EXPIRED':
        return {
          label: 'Expired',
          badgeClass: 'bg-slate-500/15 text-slate-400 border-slate-600/30',
          dotClass: 'bg-slate-400',
          icon: Clock,
          pulse: false,
        };
      case 'REVOKED':
        return {
          label: 'Revoked',
          badgeClass: 'bg-amber-500/15 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: AlertTriangle,
          pulse: false,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: Clock,
          pulse: false,
        };
    }
  };

  const config = getConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded-full border ${config.badgeClass} ${className}`}
    >
      {showIcon ? (
        <span className="relative flex items-center justify-center">
          {config.pulse && (
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-cyan-400 opacity-75" />
          )}
          <Icon className="w-3.5 h-3.5 relative" />
        </span>
      ) : (
        <span className={`w-1.5 h-1.5 rounded-full ${config.dotClass}`} />
      )}
      <span>{config.label}</span>
    </span>
  );
};
