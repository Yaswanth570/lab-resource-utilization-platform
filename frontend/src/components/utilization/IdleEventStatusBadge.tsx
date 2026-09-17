import React from 'react';
import type { IdleEventStatus } from '../../types/utilization';
import { AlertTriangle, CheckCircle2, Eye } from 'lucide-react';

interface IdleEventStatusBadgeProps {
  status: IdleEventStatus;
  className?: string;
  showIcon?: boolean;
}

export const IdleEventStatusBadge: React.FC<IdleEventStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'ONGOING':
        return {
          label: 'Ongoing Idle',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: AlertTriangle,
          pulse: true,
        };
      case 'RESOLVED':
        return {
          label: 'Resolved',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: CheckCircle2,
          pulse: false,
        };
      case 'ACKNOWLEDGED':
        return {
          label: 'Acknowledged',
          badgeClass: 'bg-sky-500/10 text-sky-400 border-sky-500/30',
          dotClass: 'bg-sky-400',
          icon: Eye,
          pulse: false,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: AlertTriangle,
          pulse: false,
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
        <span className="relative flex items-center justify-center">
          {config.pulse && (
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-amber-400 opacity-75" />
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
