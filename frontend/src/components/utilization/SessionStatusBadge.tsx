import React from 'react';
import type { SessionStatus } from '../../types/utilization';
import { Activity, CheckCheck, Clock, MinusCircle } from 'lucide-react';

interface SessionStatusBadgeProps {
  status: SessionStatus;
  className?: string;
  showIcon?: boolean;
}

export const SessionStatusBadge: React.FC<SessionStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'ACTIVE':
        return {
          label: 'Active Session',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: Activity,
          pulse: true,
        };
      case 'COMPLETED':
        return {
          label: 'Completed',
          badgeClass: 'bg-indigo-500/10 text-indigo-300 border-indigo-500/30',
          dotClass: 'bg-indigo-400',
          icon: CheckCheck,
          pulse: false,
        };
      case 'TERMINATED_EARLY':
        return {
          label: 'Terminated Early',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: Clock,
          pulse: false,
        };
      case 'AUTO_CLOSED':
        return {
          label: 'Auto-Closed',
          badgeClass: 'bg-slate-500/15 text-slate-400 border-slate-600/30',
          dotClass: 'bg-slate-400',
          icon: MinusCircle,
          pulse: false,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: Activity,
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
