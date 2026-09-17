import React from 'react';
import type { BookingBillingStatus } from '../../types/booking';
import { Clock, FileText, CheckCircle2, ShieldOff } from 'lucide-react';

interface BookingBillingStatusBadgeProps {
  status: BookingBillingStatus;
  className?: string;
  showIcon?: boolean;
}

export const BookingBillingStatusBadge: React.FC<BookingBillingStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getConfig = () => {
    switch (status) {
      case 'UNBILLED':
        return {
          label: 'Unbilled',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: Clock,
        };
      case 'INVOICED':
        return {
          label: 'Invoiced',
          badgeClass: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
          dotClass: 'bg-blue-400',
          icon: FileText,
        };
      case 'SETTLED':
        return {
          label: 'Settled',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: CheckCircle2,
        };
      case 'WAIVED':
        return {
          label: 'Waived',
          badgeClass: 'bg-purple-500/10 text-purple-400 border-purple-500/30',
          dotClass: 'bg-purple-400',
          icon: ShieldOff,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: Clock,
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
        <Icon className="w-3.5 h-3.5" />
      ) : (
        <span className={`w-1.5 h-1.5 rounded-full ${config.dotClass}`} />
      )}
      <span>{config.label}</span>
    </span>
  );
};
