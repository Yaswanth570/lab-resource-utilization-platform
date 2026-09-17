import {
  LayoutDashboard,
  User,
  Cpu,
  CalendarDays,
  Activity,
  Wrench,
  Share2,
  Receipt,
  Bell,
  BarChart3,
  FileText,
  Shield,
  type LucideIcon,
} from 'lucide-react';
import { ROLES, hasRole } from '../utils/rbac';

export interface NavItemConfig {
  label: string;
  path: string;
  icon: LucideIcon;
}

// All valid application navigation items
export const ALL_NAV_ITEMS: Record<string, NavItemConfig> = {
  DASHBOARD: { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  PROFILE: { label: 'Profile', path: '/profile', icon: User },
  EQUIPMENT: { label: 'Equipment', path: '/equipment', icon: Cpu },
  BOOKINGS: { label: 'Bookings', path: '/bookings', icon: CalendarDays },
  UTILIZATION: { label: 'Utilization', path: '/utilization', icon: Activity },
  MAINTENANCE: { label: 'Maintenance', path: '/maintenance', icon: Wrench },
  SHARING: { label: 'Resource Sharing', path: '/sharing', icon: Share2 },
  COST: { label: 'Cost & Billing', path: '/cost', icon: Receipt },
  NOTIFICATIONS: { label: 'Notifications', path: '/notifications', icon: Bell },
  ANALYTICS: { label: 'Analytics', path: '/analytics', icon: BarChart3 },
  REPORTS: { label: 'Reports', path: '/reports', icon: FileText },
  ADMIN: { label: 'Administration', path: '/admin', icon: Shield },
};

/**
 * Returns the exact navigation items allowed for the user's primary/active role.
 * Strictly adheres to role specifications in Phase 4.
 */
export function getNavItemsForRoles(roles: string[] | undefined | null): NavItemConfig[] {
  if (!roles || roles.length === 0) {
    return [ALL_NAV_ITEMS.DASHBOARD, ALL_NAV_ITEMS.PROFILE];
  }

  // 1. SYSTEM ADMINISTRATOR
  if (hasRole(roles, ROLES.SYSTEM_ADMIN)) {
    return [
      ALL_NAV_ITEMS.DASHBOARD,
      ALL_NAV_ITEMS.ADMIN,
      ALL_NAV_ITEMS.EQUIPMENT,
      ALL_NAV_ITEMS.UTILIZATION,
      ALL_NAV_ITEMS.MAINTENANCE,
      ALL_NAV_ITEMS.COST,
      ALL_NAV_ITEMS.ANALYTICS,
      ALL_NAV_ITEMS.REPORTS,
      ALL_NAV_ITEMS.NOTIFICATIONS,
      ALL_NAV_ITEMS.PROFILE,
    ];
  }

  // 2. INSTITUTION ADMINISTRATOR
  if (hasRole(roles, ROLES.INSTITUTION_ADMIN)) {
    return [
      ALL_NAV_ITEMS.DASHBOARD,
      ALL_NAV_ITEMS.PROFILE,
      ALL_NAV_ITEMS.ADMIN,
      ALL_NAV_ITEMS.EQUIPMENT,
      ALL_NAV_ITEMS.UTILIZATION,
      ALL_NAV_ITEMS.SHARING,
      ALL_NAV_ITEMS.COST,
      ALL_NAV_ITEMS.ANALYTICS,
      ALL_NAV_ITEMS.REPORTS,
      ALL_NAV_ITEMS.NOTIFICATIONS,
    ];
  }

  // 3. DEPARTMENT HEAD
  if (hasRole(roles, ROLES.DEPARTMENT_HEAD)) {
    return [
      ALL_NAV_ITEMS.DASHBOARD,
      ALL_NAV_ITEMS.PROFILE,
      ALL_NAV_ITEMS.EQUIPMENT,
      ALL_NAV_ITEMS.BOOKINGS,
      ALL_NAV_ITEMS.UTILIZATION,
      ALL_NAV_ITEMS.SHARING,
      ALL_NAV_ITEMS.ANALYTICS,
      ALL_NAV_ITEMS.REPORTS,
      ALL_NAV_ITEMS.NOTIFICATIONS,
    ];
  }

  // 4. LAB MANAGER
  if (hasRole(roles, ROLES.LAB_MANAGER)) {
    return [
      ALL_NAV_ITEMS.DASHBOARD,
      ALL_NAV_ITEMS.PROFILE,
      ALL_NAV_ITEMS.EQUIPMENT,
      ALL_NAV_ITEMS.BOOKINGS,
      ALL_NAV_ITEMS.UTILIZATION,
      ALL_NAV_ITEMS.MAINTENANCE,
      ALL_NAV_ITEMS.SHARING,
      ALL_NAV_ITEMS.ANALYTICS,
      ALL_NAV_ITEMS.REPORTS,
      ALL_NAV_ITEMS.NOTIFICATIONS,
    ];
  }

  // 5. LAB TECHNICIAN
  if (hasRole(roles, ROLES.LAB_TECHNICIAN)) {
    return [
      ALL_NAV_ITEMS.DASHBOARD,
      ALL_NAV_ITEMS.PROFILE,
      ALL_NAV_ITEMS.EQUIPMENT,
      ALL_NAV_ITEMS.MAINTENANCE,
      ALL_NAV_ITEMS.NOTIFICATIONS,
    ];
  }

  // 6. RESEARCHER / STUDENT (Default)
  return [
    ALL_NAV_ITEMS.DASHBOARD,
    ALL_NAV_ITEMS.PROFILE,
    ALL_NAV_ITEMS.EQUIPMENT,
    ALL_NAV_ITEMS.BOOKINGS,
    ALL_NAV_ITEMS.NOTIFICATIONS,
  ];
}
