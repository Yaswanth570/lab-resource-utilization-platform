import React from 'react';
import type { NotificationPriority } from '../../types/notification';
import { Info, AlertTriangle, AlertOctagon, Flame } from 'lucide-react';

interface NotificationPriorityBadgeProps {
  priority: NotificationPriority;
  className?: string;
  showIcon?: boolean;
}

export const NotificationPriorityBadge: React.FC<NotificationPriorityBadgeProps> = ({
  priority,
  className = '',
  showIcon = true,
}) => {
  const getConfig = () => {
    switch (priority) {
      case 'INFO':
        return {
          label: 'Info',
          badgeClass: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
          dotClass: 'bg-blue-400',
          icon: Info,
          pulse: false,
        };
      case 'WARNING':
        return {
          label: 'Warning',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: AlertTriangle,
          pulse: false,
        };
      case 'URGENT':
        return {
          label: 'Urgent',
          badgeClass: 'bg-orange-500/10 text-orange-400 border-orange-500/30',
          dotClass: 'bg-orange-400',
          icon: AlertOctagon,
          pulse: true,
        };
      case 'CRITICAL':
        return {
          label: 'Critical',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          dotClass: 'bg-rose-400',
          icon: Flame,
          pulse: true,
        };
      default:
        return {
          label: priority,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: Info,
          pulse: false,
        };
    }
  };

  const config = getConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 text-xs font-medium rounded-full border ${config.badgeClass} ${className}`}
    >
      {showIcon ? (
        <span className="relative flex items-center justify-center">
          {config.pulse && (
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-rose-400 opacity-75" />
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
