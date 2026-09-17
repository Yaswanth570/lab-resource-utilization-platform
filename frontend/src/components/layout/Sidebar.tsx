import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import {
  FlaskConical,
  X,
} from 'lucide-react';
import { notificationApi } from '../../api/notifications';
import { useAuth } from '../../context/useAuth';
import { getNavItemsForRoles } from '../../config/navigationConfig';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const navItems = getNavItemsForRoles(user?.roles);

  useEffect(() => {
    let isMounted = true;
    const fetchCount = async () => {
      try {
        const res = await notificationApi.getUnreadCount();
        if (isMounted) {
          setUnreadCount(res.unreadCount);
        }
      } catch {
        // Silently ignore failures in sidebar polling (e.g. unauthenticated)
      }
    };

    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);
  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar container */}
      <aside
        className={`fixed top-0 left-0 z-50 flex flex-col h-full w-64 bg-slate-900 border-r border-slate-800 transition-transform duration-200 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand header */}
        <div className="flex items-center justify-between h-16 px-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-9 h-9 rounded-lg bg-sky-600 text-white shadow-md shadow-sky-600/30">
              <FlaskConical className="w-5 h-5" />
            </div>
            <div>
              <span className="text-base font-semibold tracking-tight text-white">LabPlatform</span>
              <span className="block text-[10px] font-medium tracking-wider text-slate-400 uppercase">
                Resource Utilization
              </span>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-md text-slate-400 hover:text-white hover:bg-slate-800 lg:hidden"
            aria-label="Close menu"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation links */}
        <div className="flex-1 px-3 py-4 overflow-y-auto space-y-1">
          <div className="px-3 pb-2 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
            Navigation
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => onClose()}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                    isActive
                      ? 'bg-sky-600/20 text-sky-400 border-l-2 border-sky-500 font-semibold'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                  }`
                }
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span className="flex-1">{item.label}</span>
                {item.path === '/notifications' && unreadCount > 0 && (
                  <span className="px-1.5 py-0.5 text-[10px] font-bold rounded-full bg-sky-500 text-white leading-none">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </NavLink>
            );
          })}
        </div>

        {/* System footer */}
        <div className="p-4 border-t border-slate-800 text-[11px] text-slate-400 text-center">
          Enterprise Lab Platform &bull; v1.0
        </div>
      </aside>
    </>
  );
};
