import React from 'react';
import type { MaintenancePriority } from '../../types/maintenance';
import { Flame, AlertCircle, ArrowUp, ArrowDown } from 'lucide-react';

interface PriorityBadgeProps {
  priority: MaintenancePriority;
  className?: string;
  showIcon?: boolean;
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({
  priority,
  className = '',
  showIcon = true,
}) => {
  const getPriorityConfig = () => {
    switch (priority) {
      case 'CRITICAL':
        return {
          label: 'Critical',
          badgeClass: 'bg-red-500/15 text-red-400 border-red-500/40',
          dotClass: 'bg-red-400',
          icon: Flame,
          pulse: true,
        };
      case 'HIGH':
        return {
          label: 'High',
          badgeClass: 'bg-amber-500/15 text-amber-300 border-amber-500/40',
          dotClass: 'bg-amber-400',
          icon: AlertCircle,
          pulse: false,
        };
      case 'MEDIUM':
        return {
          label: 'Medium',
          badgeClass: 'bg-blue-500/15 text-blue-300 border-blue-500/30',
          dotClass: 'bg-blue-400',
          icon: ArrowUp,
          pulse: false,
        };
      case 'LOW':
        return {
          label: 'Low',
          badgeClass: 'bg-slate-500/15 text-slate-300 border-slate-600/30',
          dotClass: 'bg-slate-400',
          icon: ArrowDown,
          pulse: false,
        };
      default:
        return {
          label: priority,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: AlertCircle,
          pulse: false,
        };
    }
  };

  const config = getPriorityConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 text-xs font-semibold rounded-full border ${config.badgeClass} ${className}`}
    >
      {showIcon && (
        <span className="relative flex items-center justify-center">
          {config.pulse && (
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-red-400 opacity-75" />
          )}
          <Icon className="w-3.5 h-3.5 relative" />
        </span>
      )}
      <span>{config.label}</span>
    </span>
  );
};
