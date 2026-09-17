import React from 'react';
import type { SharingAgreementStatus } from '../../types/sharing';
import { CheckCircle2, PauseCircle, XCircle, Clock } from 'lucide-react';

interface SharingAgreementStatusBadgeProps {
  status: SharingAgreementStatus;
  className?: string;
  showIcon?: boolean;
}

export const SharingAgreementStatusBadge: React.FC<SharingAgreementStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getConfig = () => {
    switch (status) {
      case 'ACTIVE':
        return {
          label: 'Active Agreement',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: CheckCircle2,
          pulse: true,
        };
      case 'SUSPENDED':
        return {
          label: 'Suspended',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: PauseCircle,
          pulse: false,
        };
      case 'TERMINATED':
        return {
          label: 'Terminated',
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
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: CheckCircle2,
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
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-emerald-400 opacity-75" />
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
