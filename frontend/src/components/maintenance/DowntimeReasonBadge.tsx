import React from 'react';
import type { DowntimeReasonCategory } from '../../types/maintenance';
import { AlertOctagon, Wrench, ZapOff, ShieldAlert, Sliders } from 'lucide-react';

interface DowntimeReasonBadgeProps {
  category: DowntimeReasonCategory;
  className?: string;
}

export const DowntimeReasonBadge: React.FC<DowntimeReasonBadgeProps> = ({
  category,
  className = '',
}) => {
  const getConfig = () => {
    switch (category) {
      case 'UNSCHEDULED_BREAKDOWN':
        return {
          label: 'Unscheduled Breakdown',
          badgeClass: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
          icon: AlertOctagon,
        };
      case 'SCHEDULED_MAINTENANCE':
        return {
          label: 'Scheduled Maintenance',
          badgeClass: 'bg-sky-500/15 text-sky-300 border-sky-500/30',
          icon: Wrench,
        };
      case 'CALIBRATION':
        return {
          label: 'Calibration Service',
          badgeClass: 'bg-blue-500/15 text-blue-300 border-blue-500/30',
          icon: Sliders,
        };
      case 'FACILITY_OUTAGE':
        return {
          label: 'Facility Outage',
          badgeClass: 'bg-purple-500/15 text-purple-300 border-purple-500/30',
          icon: ZapOff,
        };
      case 'SAFETY_HOLD':
        return {
          label: 'Safety Hold',
          badgeClass: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
          icon: ShieldAlert,
        };
      default:
        return {
          label: category,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          icon: AlertOctagon,
        };
    }
  };

  const config = getConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded-full border ${config.badgeClass} ${className}`}
    >
      <Icon className="w-3.5 h-3.5" />
      <span>{config.label}</span>
    </span>
  );
};
