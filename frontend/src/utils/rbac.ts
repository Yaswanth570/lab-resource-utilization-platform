export const ROLES = {
  RESEARCHER_STUDENT: 'ROLE_RESEARCHER_STUDENT',
  LAB_TECHNICIAN: 'ROLE_LAB_TECHNICIAN',
  LAB_MANAGER: 'ROLE_LAB_MANAGER',
  DEPARTMENT_HEAD: 'ROLE_DEPARTMENT_HEAD',
  INSTITUTION_ADMIN: 'ROLE_INSTITUTION_ADMINISTRATOR',
  SYSTEM_ADMIN: 'ROLE_SYSTEM_ADMINISTRATOR',
} as const;

export type UserRole = (typeof ROLES)[keyof typeof ROLES];

export function hasRole(roles: string[] | undefined | null, role: string): boolean {
  if (!roles) return false;
  return roles.includes(role);
}

export function hasAnyRole(roles: string[] | undefined | null, targetRoles: string[]): boolean {
  if (!roles) return false;
  return targetRoles.some((r) => roles.includes(r));
}

export function isResearcher(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.RESEARCHER_STUDENT) && !isHigherStaff(roles);
}

export function isTechnician(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.LAB_TECHNICIAN);
}

export function isLabManager(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.LAB_MANAGER);
}

export function isDepartmentHead(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.DEPARTMENT_HEAD);
}

export function isInstitutionAdmin(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.INSTITUTION_ADMIN);
}

export function isSystemAdmin(roles: string[] | undefined | null): boolean {
  return hasRole(roles, ROLES.SYSTEM_ADMIN);
}

export function isHigherStaff(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [
    ROLES.LAB_TECHNICIAN,
    ROLES.LAB_MANAGER,
    ROLES.DEPARTMENT_HEAD,
    ROLES.INSTITUTION_ADMIN,
    ROLES.SYSTEM_ADMIN,
  ]);
}

// Action-level permission gates

export function canApproveBooking(
  roles: string[] | undefined | null,
  bookingUserId?: number,
  currentUserId?: number
): boolean {
  // Researcher/Technician can NEVER approve bookings
  if (!hasAnyRole(roles, [ROLES.LAB_MANAGER, ROLES.DEPARTMENT_HEAD, ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN])) {
    return false;
  }
  // Self-approval is forbidden unless system administrator
  if (bookingUserId && currentUserId && bookingUserId === currentUserId && !hasRole(roles, ROLES.SYSTEM_ADMIN)) {
    return false;
  }
  return true;
}

export function canManageEquipment(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.LAB_MANAGER, ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canUpdateEquipmentStatus(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.LAB_TECHNICIAN, ROLES.LAB_MANAGER, ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canManageUsers(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canViewCosts(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.DEPARTMENT_HEAD, ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canManageCosts(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canManageMaintenance(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [ROLES.LAB_TECHNICIAN, ROLES.LAB_MANAGER, ROLES.INSTITUTION_ADMIN, ROLES.SYSTEM_ADMIN]);
}

export function canViewReports(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [
    ROLES.LAB_MANAGER,
    ROLES.DEPARTMENT_HEAD,
    ROLES.INSTITUTION_ADMIN,
    ROLES.SYSTEM_ADMIN,
  ]);
}

export function canViewAnalytics(roles: string[] | undefined | null): boolean {
  return hasAnyRole(roles, [
    ROLES.LAB_MANAGER,
    ROLES.DEPARTMENT_HEAD,
    ROLES.INSTITUTION_ADMIN,
    ROLES.SYSTEM_ADMIN,
  ]);
}
