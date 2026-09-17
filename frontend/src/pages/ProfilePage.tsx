import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/useAuth';
import { getCurrentUserProfile } from '../api/client';
import type { UserProfileResponse } from '../types/auth';
import {
  User,
  Mail,
  Building2,
  ShieldCheck,
  Calendar,
  CheckCircle2,
  XCircle,
  RefreshCw,
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user: authUser } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    getCurrentUserProfile()
      .then((data) => {
        if (isMounted) {
          setProfile(data);
          setIsLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (isMounted) {
          console.warn('Unable to load full profile from /api/users/me; using session data fallback', err);
          setError('Unable to fetch live profile data from the server. Displaying cached session information.');
          setIsLoading(false);
        }
      });
    return () => {
      isMounted = false;
    };
  }, []);

  const handleRefresh = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getCurrentUserProfile();
      setProfile(data);
    } catch (err: unknown) {
      console.warn('Unable to load full profile from /api/users/me; using session data fallback', err);
      setError('Unable to fetch live profile data from the server. Displaying cached session information.');
    } finally {
      setIsLoading(false);
    }
  };

  const formatRole = (role: string): string => {
    return role
      .replace(/^ROLE_/, '')
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
      .join(' ');
  };

  // Safe fallback values if endpoint call fails or is in progress
  const firstName = profile?.firstName || authUser?.firstName || '—';
  const lastName = profile?.lastName || authUser?.lastName || '—';
  const email = profile?.email || authUser?.email || '—';
  const institutionId = profile?.institutionId || authUser?.institutionId || '—';
  const institutionName = profile?.institutionName;
  const departmentId = profile?.departmentId || authUser?.departmentId;
  const departmentName = profile?.departmentName;
  const roles = profile?.roles || authUser?.roles || [];
  const status = profile?.status || 'ACTIVE';
  const isVerified = profile?.verified ?? true;
  const createdAt = profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—';
  const lastLogin = profile?.lastLoginAt ? new Date(profile.lastLoginAt).toLocaleString() : 'Current Session';

  const initials = `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Profile Header Card */}
      <div className="p-6 sm:p-8 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="flex items-center gap-5">
            <div className="w-16 h-16 rounded-2xl bg-sky-600/20 border border-sky-500/40 text-sky-400 flex items-center justify-center text-xl font-bold shadow-lg shadow-sky-600/10">
              {initials}
            </div>
            <div>
              <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">
                {firstName} {lastName}
              </h2>
              <div className="flex items-center gap-2 mt-1 text-xs text-slate-400">
                <Mail className="w-3.5 h-3.5 text-slate-500" />
                <span>{email}</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={handleRefresh}
            disabled={isLoading}
            className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors disabled:opacity-50"
            title="Refresh profile details"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>

        {error && (
          <div className="mt-4 p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
            {error}
          </div>
        )}
      </div>

      {/* Account Details & Institutional Assignment */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Personal & Account Info */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-sm font-semibold text-white border-b border-slate-800 pb-3">
            <User className="w-4 h-4 text-sky-400" />
            <span>Account Details</span>
          </div>

          <dl className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">First Name</dt>
              <dd className="font-medium text-slate-200">{firstName}</dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Last Name</dt>
              <dd className="font-medium text-slate-200">{lastName}</dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Email Address</dt>
              <dd className="font-medium text-slate-200">{email}</dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Account Status</dt>
              <dd className="font-medium flex items-center gap-1.5">
                <span className="inline-block w-2 h-2 rounded-full bg-emerald-400" />
                <span className="text-emerald-400 uppercase text-[11px] font-semibold tracking-wider">
                  {status}
                </span>
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Verification</dt>
              <dd className="font-medium flex items-center gap-1 text-slate-200">
                {isVerified ? (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Verified</span>
                  </>
                ) : (
                  <>
                    <XCircle className="w-3.5 h-3.5 text-amber-400" />
                    <span>Pending</span>
                  </>
                )}
              </dd>
            </div>
            <div className="flex justify-between py-1">
              <dt className="text-slate-400">Member Since</dt>
              <dd className="font-medium text-slate-200 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-slate-500" />
                <span>{createdAt}</span>
              </dd>
            </div>
          </dl>
        </div>

        {/* Institutional Affiliation & Roles */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-sm font-semibold text-white border-b border-slate-800 pb-3">
            <Building2 className="w-4 h-4 text-sky-400" />
            <span>Institutional Affiliation</span>
          </div>

          <dl className="space-y-3 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Institution</dt>
              <dd className="font-medium text-slate-200">
                {institutionName ? `${institutionName} (#${institutionId})` : `Institution #${institutionId}`}
              </dd>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/60">
              <dt className="text-slate-400">Department</dt>
              <dd className="font-medium text-slate-200">
                {departmentName
                  ? `${departmentName} (#${departmentId})`
                  : departmentId
                  ? `Department #${departmentId}`
                  : 'Unassigned / General'}
              </dd>
            </div>
            <div className="py-1">
              <dt className="text-slate-400 mb-2">Granted Roles &amp; Permissions</dt>
              <dd className="flex flex-wrap gap-2">
                {roles.length > 0 ? (
                  roles.map((role) => (
                    <span
                      key={role}
                      className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-sky-500/10 border border-sky-500/30 text-sky-300 font-medium text-xs"
                    >
                      <ShieldCheck className="w-3.5 h-3.5 text-sky-400" />
                      <span>{formatRole(role)}</span>
                    </span>
                  ))
                ) : (
                  <span className="text-slate-500">No roles assigned</span>
                )}
              </dd>
            </div>
            <div className="flex justify-between pt-3 border-t border-slate-800/60">
              <dt className="text-slate-400">Last Authentication</dt>
              <dd className="font-medium text-slate-400 text-[11px]">{lastLogin}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  );
};
