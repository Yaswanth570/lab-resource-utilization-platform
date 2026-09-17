import React from 'react';
import type { NotificationEventType } from '../../types/notification';
import {
  CalendarDays,
  CheckCircle2,
  XCircle,
  Clock,
  Wrench,
  Gauge,
  Award,
  Share2,
  Activity,
  Receipt,
} from 'lucide-react';

interface NotificationTypeBadgeProps {
  eventType: NotificationEventType;
  className?: string;
}

export const NotificationTypeBadge: React.FC<NotificationTypeBadgeProps> = ({
  eventType,
  className = '',
}) => {
  const getConfig = () => {
    switch (eventType) {
      case 'BOOKING_APPROVED':
        return {
          label: 'Booking Approved',
          badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
          icon: CheckCircle2,
        };
      case 'BOOKING_REJECTED':
        return {
          label: 'Booking Rejected',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          icon: XCircle,
        };
      case 'BOOKING_CANCELLED':
        return {
          label: 'Booking Cancelled',
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-600/30',
          icon: XCircle,
        };
      case 'BOOKING_NO_SHOW':
        return {
          label: 'Booking No-Show',
          badgeClass: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
          icon: AlertCircleIcon,
        };
      case 'BOOKING_PENDING':
        return {
          label: 'Booking Pending',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          icon: Clock,
        };
      case 'WAITLIST_AVAILABLE':
        return {
          label: 'Slot Available',
          badgeClass: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
          icon: CalendarDays,
        };
      case 'MAINTENANCE_SCHEDULED':
      case 'WORK_ORDER_ASSIGNED':
        return {
          label: 'Maintenance',
          badgeClass: 'bg-orange-500/10 text-orange-400 border-orange-500/30',
          icon: Wrench,
        };
      case 'CALIBRATION_EXPIRING_SOON':
      case 'CALIBRATION_EXPIRED':
        return {
          label: 'Calibration',
          badgeClass: 'bg-purple-500/10 text-purple-400 border-purple-500/30',
          icon: Gauge,
        };
      case 'CERTIFICATION_EXPIRING_SOON':
      case 'CERTIFICATION_EXPIRED':
        return {
          label: 'Qualification',
          badgeClass: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
          icon: Award,
        };
      case 'SHARING_REQUEST_RECEIVED':
      case 'SHARING_REQUEST_APPROVED':
        return {
          label: 'Resource Sharing',
          badgeClass: 'bg-teal-500/10 text-teal-400 border-teal-500/30',
          icon: Share2,
        };
      case 'IDLE_ALERT':
        return {
          label: 'Idle Alert',
          badgeClass: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
          icon: Activity,
        };
      case 'INVOICE_GENERATED':
        return {
          label: 'Billing Invoice',
          badgeClass: 'bg-sky-500/10 text-sky-400 border-sky-500/30',
          icon: Receipt,
        };
      default:
        return {
          label: eventType,
          badgeClass: 'bg-slate-500/10 text-slate-400 border-slate-500/30',
          icon: Clock,
        };
    }
  };

  const AlertCircleIcon = Clock; // fallback safe
  const config = getConfig();
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 text-xs font-medium rounded-md border ${config.badgeClass} ${className}`}
    >
      <Icon className="w-3.5 h-3.5" />
      <span>{config.label}</span>
    </span>
  );
};
