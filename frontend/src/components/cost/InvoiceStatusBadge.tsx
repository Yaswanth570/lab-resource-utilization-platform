import React from 'react';
import type { InvoiceStatus } from '../../types/cost';
import { FileEdit, Send, CheckCircle2, ShieldCheck, XCircle } from 'lucide-react';

interface InvoiceStatusBadgeProps {
  status: InvoiceStatus;
  className?: string;
  showIcon?: boolean;
}

export const InvoiceStatusBadge: React.FC<InvoiceStatusBadgeProps> = ({
  status,
  className = '',
  showIcon = true,
}) => {
  const getConfig = () => {
    switch (status) {
      case 'DRAFT':
        return {
          label: 'Draft',
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-600/30',
          dotClass: 'bg-slate-400',
          icon: FileEdit,
          pulse: false,
        };
      case 'ISSUED':
        return {
          label: 'Issued',
          badgeClass: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
          dotClass: 'bg-blue-400',
          icon: Send,
          pulse: true,
        };
      case 'PAID':
        return {
          label: 'Paid',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          dotClass: 'bg-amber-400',
          icon: CheckCircle2,
          pulse: false,
        };
      case 'SETTLED':
        return {
          label: 'Settled',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          dotClass: 'bg-emerald-400',
          icon: ShieldCheck,
          pulse: false,
        };
      case 'CANCELLED':
        return {
          label: 'Cancelled',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          dotClass: 'bg-rose-400',
          icon: XCircle,
          pulse: false,
        };
      default:
        return {
          label: status,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          dotClass: 'bg-slate-400',
          icon: FileEdit,
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
            <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-blue-400 opacity-75" />
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
