import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import { useAuth } from '../context/useAuth';
import {
  getCurrentUserProfile,
  updateUserProfile,
  getInstitutions,
  getDepartmentsByInstitution,
  type PublicInstitutionLookup,
  type PublicDepartmentLookup,
} from '../api/client';
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
  Edit3,
  Save,
  X,
  Phone,
  Lock,
  AlertCircle,
  Info,
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user: authUser, updateUser } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Edit Mode State
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [editError, setEditError] = useState<string | null>(null);

  // Editable Form Fields
  const [editFirstName, setEditFirstName] = useState<string>('');
  const [editLastName, setEditLastName] = useState<string>('');
  const [editPhone, setEditPhone] = useState<string>('');
  const [editInstitutionId, setEditInstitutionId] = useState<string>('');
  const [editDepartmentId, setEditDepartmentId] = useState<string>('');

  // Institution & Department Options for Edit Mode
  const [institutions, setInstitutions] = useState<PublicInstitutionLookup[]>([]);
  const [departments, setDepartments] = useState<PublicDepartmentLookup[]>([]);
  const [isLoadingInstitutions, setIsLoadingInstitutions] = useState<boolean>(false);
  const [isLoadingDepartments, setIsLoadingDepartments] = useState<boolean>(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const fetchProfile = useCallback(async () => {
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
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  // Load institutions list for edit mode
  const fetchInstitutionsList = useCallback(async () => {
    setIsLoadingInstitutions(true);
    try {
      const data = await getInstitutions();
      setInstitutions(data || []);
    } catch (err) {
      console.error('Failed to load institutions for profile edit', err);
    } finally {
      setIsLoadingInstitutions(false);
    }
  }, []);

  // When edit mode is opened, initialize form fields from profile
  const handleStartEdit = () => {
    setEditError(null);
    setSuccessMessage(null);
    setFieldErrors({});

    const currentInstId = profile?.institutionId || authUser?.institutionId;
    const currentDeptId = profile?.departmentId !== undefined ? profile?.departmentId : authUser?.departmentId;

    setEditFirstName(profile?.firstName || authUser?.firstName || '');
    setEditLastName(profile?.lastName || authUser?.lastName || '');
    setEditPhone(profile?.phone || '');
    setEditInstitutionId(currentInstId ? String(currentInstId) : '');
    setEditDepartmentId(currentDeptId ? String(currentDeptId) : (currentDeptId === null ? 'OTHER' : ''));

    fetchInstitutionsList();
    setIsEditing(true);
  };

  // Load cascading departments when institution selection changes
  useEffect(() => {
    if (!isEditing || !editInstitutionId) {
      setDepartments([]);
      return;
    }

    if (editInstitutionId === 'OTHER') {
      setDepartments([]);
      setEditDepartmentId('OTHER');
      return;
    }

    let isMounted = true;
    const fetchDepts = async () => {
      setIsLoadingDepartments(true);
      try {
        const data = await getDepartmentsByInstitution(Number(editInstitutionId));
        if (isMounted) {
          const depts = data || [];
          setDepartments(depts);
          if (depts.length === 0) {
            setEditDepartmentId('OTHER');
          }
        }
      } catch (err) {
        console.error('Failed to load departments', err);
        if (isMounted) {
          setDepartments([]);
          setEditDepartmentId('OTHER');
        }
      } finally {
        if (isMounted) setIsLoadingDepartments(false);
      }
    };

    fetchDepts();
    return () => {
      isMounted = false;
    };
  }, [isEditing, editInstitutionId]);

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditError(null);
    setFieldErrors({});
  };

  const validateEditForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!editFirstName.trim()) {
      errors.firstName = 'First name is required';
    } else if (editFirstName.trim().length > 100) {
      errors.firstName = 'First name cannot exceed 100 characters';
    }

    if (!editLastName.trim()) {
      errors.lastName = 'Last name is required';
    } else if (editLastName.trim().length > 100) {
      errors.lastName = 'Last name cannot exceed 100 characters';
    }

    if (editPhone.trim()) {
      const phoneRegex = /^[+]?[0-9\s\-().]{7,30}$/;
      if (!phoneRegex.test(editPhone.trim())) {
        errors.phone = 'Please enter a valid phone number format (e.g. +1 555-123-4567)';
      }
    }

    if (!editInstitutionId) {
      errors.institutionId = 'Please select your institution';
    }

    if (!editDepartmentId) {
      errors.departmentId = 'Please select your department';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setEditError(null);
    setSuccessMessage(null);

    if (!validateEditForm()) {
      return;
    }

    setIsSaving(true);
    try {
      const resolvedInstitutionId =
        editInstitutionId === 'OTHER'
          ? (institutions.find((i) => i.code === 'OTHER')?.id ?? null)
          : Number(editInstitutionId);

      const resolvedDepartmentId =
        editDepartmentId === 'OTHER' || !editDepartmentId ? null : Number(editDepartmentId);

      const updated = await updateUserProfile({
        firstName: editFirstName.trim(),
        lastName: editLastName.trim(),
        phone: editPhone.trim() ? editPhone.trim() : undefined,
        institutionId: resolvedInstitutionId,
        departmentId: resolvedDepartmentId,
      });

      setProfile(updated);

      if (updateUser) {
        updateUser({
          firstName: updated.firstName,
          lastName: updated.lastName,
          departmentId: updated.departmentId,
        });
      }

      setIsEditing(false);
      setSuccessMessage('Profile details saved successfully!');
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        if (err.response?.data?.message) {
          setEditError(err.response.data.message);
        } else if (err.response?.status === 400) {
          setEditError('Invalid profile details. Please check all fields and try again.');
        } else if (err.response?.status === 403) {
          setEditError('Access denied: You are not authorized to perform this update.');
        } else {
          setEditError(err.message || 'Failed to update profile.');
        }
      } else {
        setEditError('An unexpected error occurred while saving your profile.');
      }
    } finally {
      setIsSaving(false);
    }
  };

  const formatRole = (role: string): string => {
    return role
      .replace(/^ROLE_/, '')
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
      .join(' ');
  };

  // Safe display values
  const firstName = profile?.firstName || authUser?.firstName || '—';
  const lastName = profile?.lastName || authUser?.lastName || '—';
  const email = profile?.email || authUser?.email || '—';
  const phone = profile?.phone || 'Not provided';
  const institutionId = profile?.institutionId || authUser?.institutionId || '—';
  const institutionName = profile?.institutionName;
  const departmentId = profile?.departmentId !== undefined ? profile?.departmentId : authUser?.departmentId;
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

          <div className="flex items-center gap-2">
            {!isEditing ? (
              <>
                <button
                  type="button"
                  onClick={handleStartEdit}
                  disabled={isLoading}
                  className="flex items-center gap-2 px-3.5 py-1.5 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-lg shadow-sm shadow-sky-600/30 transition-all disabled:opacity-50"
                  title="Edit profile details"
                >
                  <Edit3 className="w-3.5 h-3.5" />
                  <span>Edit Details</span>
                </button>
                <button
                  type="button"
                  onClick={fetchProfile}
                  disabled={isLoading}
                  className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors disabled:opacity-50"
                  title="Refresh profile details"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
                  <span>Refresh</span>
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={handleCancelEdit}
                disabled={isSaving}
                className="flex items-center gap-2 px-3.5 py-1.5 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors disabled:opacity-50"
              >
                <X className="w-3.5 h-3.5" />
                <span>Cancel</span>
              </button>
            )}
          </div>
        </div>

        {/* Global Notifications */}
        {successMessage && (
          <div className="mt-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{successMessage}</span>
          </div>
        )}

        {error && !isEditing && (
          <div className="mt-4 p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-amber-400 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* EDIT PROFILE VIEW */}
      {isEditing ? (
        <form onSubmit={handleSaveProfile} className="space-y-6">
          {editError && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-red-200">Unable to update profile</p>
                <p className="mt-0.5">{editError}</p>
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Editable Details Card */}
            <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-white">
                  <User className="w-4 h-4 text-sky-400" />
                  <span>Personal Information</span>
                </div>
                <span className="text-[11px] text-sky-400 font-medium">Editable</span>
              </div>

              <div className="space-y-3.5">
                {/* First Name */}
                <div>
                  <label htmlFor="editFirstName" className="block text-xs font-medium text-slate-300 mb-1">
                    First Name <span className="text-red-400">*</span>
                  </label>
                  <input
                    id="editFirstName"
                    type="text"
                    disabled={isSaving}
                    value={editFirstName}
                    onChange={(e) => {
                      setEditFirstName(e.target.value);
                      if (fieldErrors.firstName) {
                        setFieldErrors((prev) => {
                          const copy = { ...prev };
                          delete copy.firstName;
                          return copy;
                        });
                      }
                    }}
                    placeholder="First name"
                    className={`w-full px-3 py-2 text-xs bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                      fieldErrors.firstName ? 'border-red-500/60' : 'border-slate-800'
                    }`}
                  />
                  {fieldErrors.firstName && (
                    <p className="mt-1 text-[11px] text-red-400">{fieldErrors.firstName}</p>
                  )}
                </div>

                {/* Last Name */}
                <div>
                  <label htmlFor="editLastName" className="block text-xs font-medium text-slate-300 mb-1">
                    Last Name <span className="text-red-400">*</span>
                  </label>
                  <input
                    id="editLastName"
                    type="text"
                    disabled={isSaving}
                    value={editLastName}
                    onChange={(e) => {
                      setEditLastName(e.target.value);
                      if (fieldErrors.lastName) {
                        setFieldErrors((prev) => {
                          const copy = { ...prev };
                          delete copy.lastName;
                          return copy;
                        });
                      }
                    }}
                    placeholder="Last name"
                    className={`w-full px-3 py-2 text-xs bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                      fieldErrors.lastName ? 'border-red-500/60' : 'border-slate-800'
                    }`}
                  />
                  {fieldErrors.lastName && (
                    <p className="mt-1 text-[11px] text-red-400">{fieldErrors.lastName}</p>
                  )}
                </div>

                {/* Phone Number */}
                <div>
                  <label htmlFor="editPhone" className="block text-xs font-medium text-slate-300 mb-1">
                    Phone Number <span className="text-slate-500 font-normal">(Optional)</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Phone className="w-3.5 h-3.5" />
                    </div>
                    <input
                      id="editPhone"
                      type="tel"
                      autoComplete="tel"
                      disabled={isSaving}
                      value={editPhone}
                      onChange={(e) => {
                        setEditPhone(e.target.value);
                        if (fieldErrors.phone) {
                          setFieldErrors((prev) => {
                            const copy = { ...prev };
                            delete copy.phone;
                            return copy;
                          });
                        }
                      }}
                      placeholder="+91 98480 12345"
                      className={`w-full pl-9 pr-3 py-2 text-xs bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.phone ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                  </div>
                  {fieldErrors.phone && (
                    <p className="mt-1 text-[11px] text-red-400">{fieldErrors.phone}</p>
                  )}
                </div>

                {/* Institutional Email (Read-Only) */}
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label htmlFor="readOnlyEmail" className="block text-xs font-medium text-slate-400">
                      Institutional Email
                    </label>
                    <span className="text-[10px] text-slate-500 flex items-center gap-1">
                      <Lock className="w-2.5 h-2.5" /> Read-only
                    </span>
                  </div>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Mail className="w-3.5 h-3.5" />
                    </div>
                    <input
                      id="readOnlyEmail"
                      type="email"
                      readOnly
                      disabled
                      value={email}
                      className="w-full pl-9 pr-3 py-2 text-xs bg-slate-950/60 border border-slate-800/80 rounded-lg text-slate-400 cursor-not-allowed select-none"
                    />
                  </div>
                  <p className="mt-1 text-[10px] text-slate-500">
                    Email address is your verified platform login identity and cannot be altered directly.
                  </p>
                </div>
              </div>
            </div>

            {/* Institutional Affiliation & Organization Card */}
            <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-white">
                  <Building2 className="w-4 h-4 text-sky-400" />
                  <span>Institutional Affiliation</span>
                </div>
                <span className="text-[11px] text-sky-400 font-medium">Organization</span>
              </div>

              <div className="space-y-3.5">
                {/* Institution Selector */}
                <div>
                  <label htmlFor="editInstitution" className="block text-xs font-medium text-slate-300 mb-1">
                    Institution <span className="text-red-400">*</span>
                  </label>
                  <select
                    id="editInstitution"
                    disabled={isSaving || isLoadingInstitutions}
                    value={editInstitutionId}
                    onChange={(e) => {
                      setEditInstitutionId(e.target.value);
                      if (fieldErrors.institutionId) {
                        setFieldErrors((prev) => {
                          const copy = { ...prev };
                          delete copy.institutionId;
                          return copy;
                        });
                      }
                    }}
                    className={`w-full px-3 py-2 text-xs bg-slate-950 border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                      fieldErrors.institutionId ? 'border-red-500/60' : 'border-slate-800'
                    }`}
                  >
                    <option value="">Select Institution</option>
                    {institutions
                      .filter((inst) => inst.code !== 'OTHER')
                      .map((inst) => (
                        <option key={inst.id} value={inst.id}>
                          {inst.name} ({inst.code})
                        </option>
                      ))}
                    <option value="OTHER">Others (Unlisted Institution)</option>
                  </select>
                  {fieldErrors.institutionId && (
                    <p className="mt-1 text-[11px] text-red-400">{fieldErrors.institutionId}</p>
                  )}
                  <p className="mt-1 text-[10px] text-slate-500 flex items-center gap-1">
                    <Info className="w-3 h-3 text-slate-500 shrink-0" />
                    Multi-tenant rule: Inter-institutional transfers require institutional administrator approval.
                  </p>
                </div>

                {/* Department Selector */}
                <div>
                  <label htmlFor="editDepartment" className="block text-xs font-medium text-slate-300 mb-1">
                    Department <span className="text-red-400">*</span>
                  </label>
                  <select
                    id="editDepartment"
                    disabled={isSaving || isLoadingDepartments || !editInstitutionId}
                    value={editDepartmentId}
                    onChange={(e) => {
                      setEditDepartmentId(e.target.value);
                      if (fieldErrors.departmentId) {
                        setFieldErrors((prev) => {
                          const copy = { ...prev };
                          delete copy.departmentId;
                          return copy;
                        });
                      }
                    }}
                    className={`w-full px-3 py-2 text-xs bg-slate-950 border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                      fieldErrors.departmentId ? 'border-red-500/60' : 'border-slate-800'
                    }`}
                  >
                    {!editInstitutionId ? (
                      <option value="">Select an institution first</option>
                    ) : isLoadingDepartments ? (
                      <option value="">Loading departments...</option>
                    ) : (
                      <>
                        <option value="">Select Department</option>
                        {departments.map((dept) => (
                          <option key={dept.id} value={dept.id}>
                            {dept.name} ({dept.code})
                          </option>
                        ))}
                        <option value="OTHER">Others (General / Unassigned)</option>
                      </>
                    )}
                  </select>
                  {fieldErrors.departmentId && (
                    <p className="mt-1 text-[11px] text-red-400">{fieldErrors.departmentId}</p>
                  )}
                  {editInstitutionId && !isLoadingDepartments && departments.length === 0 && (
                    <p className="mt-1 text-[10px] text-sky-400">
                      No registered departments found for this institution. 'Others' is selected.
                    </p>
                  )}
                </div>

                {/* Read-Only Account Status & Verification Preview */}
                <div className="pt-2 border-t border-slate-800/60 grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <span className="block text-[10px] text-slate-500 uppercase tracking-wider font-semibold mb-0.5">
                      Account Status
                    </span>
                    <span className="inline-flex items-center gap-1.5 font-medium text-emerald-400">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                      {status}
                    </span>
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-500 uppercase tracking-wider font-semibold mb-0.5">
                      Verification
                    </span>
                    <span className="font-medium text-slate-300 flex items-center gap-1">
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
                    </span>
                  </div>
                </div>

                {/* Read-Only Role Preview */}
                <div className="pt-2 border-t border-slate-800/60">
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold">
                      Assigned Privilege
                    </span>
                    <span className="text-[10px] text-slate-500 flex items-center gap-1">
                      <Lock className="w-2.5 h-2.5" /> Managed by Admin
                    </span>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {roles.map((r) => (
                      <span
                        key={r}
                        className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-sky-500/10 border border-sky-500/20 text-sky-300 text-[11px] font-medium"
                      >
                        <ShieldCheck className="w-3 h-3 text-sky-400" />
                        <span>{formatRole(r)}</span>
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Form Action Controls */}
          <div className="flex items-center justify-end gap-3 p-4 rounded-xl bg-slate-900 border border-slate-800">
            <button
              type="button"
              onClick={handleCancelEdit}
              disabled={isSaving}
              className="px-4 py-2 text-xs font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-sky-600 hover:bg-sky-500 rounded-lg shadow-md shadow-sky-600/30 transition-all disabled:opacity-50"
            >
              {isSaving ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  <span>Saving Changes...</span>
                </>
              ) : (
                <>
                  <Save className="w-3.5 h-3.5" />
                  <span>Save Changes</span>
                </>
              )}
            </button>
          </div>
        </form>
      ) : (
        /* NORMAL VIEW PROFILE MODE */
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
                <dt className="text-slate-400">Phone Number</dt>
                <dd className="font-medium text-slate-200">{phone}</dd>
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
                    : 'Unassigned / General (Others)'}
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
      )}
    </div>
  );
};
