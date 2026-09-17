import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { Menu, LogOut, ShieldCheck } from 'lucide-react';

interface HeaderProps {
  onMenuToggle: () => void;
}

const routeTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/profile': 'User Profile',
  '/equipment': 'Equipment Catalog',
  '/bookings': 'Resource Bookings',
  '/utilization': 'Utilization & Sessions',
  '/maintenance': 'Maintenance & Downtime',
  '/sharing': 'Resource Sharing Agreements',
  '/notifications': 'Notification Center',
  '/analytics': 'Utilization Analytics',
  '/reports': 'Compliance & Reports',
  '/admin': 'Platform Administration',
};

const formatRole = (role: string): string => {
  return role
    .replace(/^ROLE_/, '')
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
};

export const Header: React.FC<HeaderProps> = ({ onMenuToggle }) => {
  const location = useLocation();
  const { user, logout } = useAuth();

  const title = routeTitles[location.pathname] || 'Enterprise Laboratory';
  const primaryRole = user?.roles?.[0] ? formatRole(user.roles[0]) : 'User';
  const initials = user
    ? `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase()
    : 'U';

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between h-16 px-4 sm:px-6 bg-slate-900/90 backdrop-blur-md border-b border-slate-800">
      {/* Left section: mobile toggle + title */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuToggle}
          className="p-2 -ml-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 lg:hidden"
          aria-label="Open sidebar menu"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-base sm:text-lg font-semibold text-white tracking-tight">{title}</h1>
        </div>
      </div>

      {/* Right section: user profile summary + logout */}
      <div className="flex items-center gap-3 sm:gap-4">
        {user && (
          <div className="flex items-center gap-3 pl-2 pr-3 py-1.5 rounded-full bg-slate-800/80 border border-slate-700/60">
            <div className="flex items-center justify-center w-7 h-7 rounded-full bg-sky-600 text-white font-semibold text-xs shadow-inner">
              {initials}
            </div>
            <div className="hidden sm:block text-left">
              <div className="text-xs font-semibold text-slate-200 leading-tight">
                {user.firstName} {user.lastName}
              </div>
              <div className="flex items-center gap-1 text-[10px] text-sky-400">
                <ShieldCheck className="w-3 h-3 inline" />
                <span>{primaryRole}</span>
              </div>
            </div>
          </div>
        )}

        <button
          type="button"
          onClick={logout}
          className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-300 hover:text-red-400 bg-slate-800 hover:bg-red-500/10 border border-slate-700 hover:border-red-500/30 rounded-lg transition-colors"
          title="Sign out"
        >
          <LogOut className="w-4 h-4" />
          <span className="hidden sm:inline">Sign Out</span>
        </button>
      </div>
    </header>
  );
};
