import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  FlaskConical,
  Eye,
  EyeOff,
  Lock,
  Mail,
  User as UserIcon,
  Phone,
  Building2,
  GraduationCap,
  ShieldCheck,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Check,
  X
} from 'lucide-react';
import axios from 'axios';
import {
  registerApi,
  getPublicInstitutions,
  getPublicDepartments,
  type PublicInstitutionLookup,
  type PublicDepartmentLookup
} from '../api/client';
import type { RegisterResponse } from '../types/auth';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();

  // Form Fields
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [institutionId, setInstitutionId] = useState<number | 'OTHER' | ''>('');
  const [departmentId, setDepartmentId] = useState<number | 'OTHER' | ''>('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [termsAccepted, setTermsAccepted] = useState(false);

  // Visibility Toggles
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Dynamic Lookups
  const [institutions, setInstitutions] = useState<PublicInstitutionLookup[]>([]);
  const [departments, setDepartments] = useState<PublicDepartmentLookup[]>([]);
  const [isLoadingInstitutions, setIsLoadingInstitutions] = useState(true);
  const [isLoadingDepartments, setIsLoadingDepartments] = useState(false);
  const [institutionLoadError, setInstitutionLoadError] = useState<string | null>(null);
  const [departmentLoadError, setDepartmentLoadError] = useState<string | null>(null);

  // State & Validation
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [successData, setSuccessData] = useState<RegisterResponse | null>(null);

  // Password rules
  const hasMinLength = password.length >= 8;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumber = /\d/.test(password);
  const isPasswordStrong = hasMinLength && hasUpperCase && hasLowerCase && hasNumber;

  // Load institutions on mount with retry capability
  const fetchInstitutions = useCallback(async () => {
    setIsLoadingInstitutions(true);
    setInstitutionLoadError(null);
    try {
      const data = await getPublicInstitutions();
      setInstitutions(data);
      if (!data || data.length === 0) {
        setInstitutionLoadError('No active institutions found in the database.');
      }
    } catch (err) {
      console.error('Failed to load institutions for registration', err);
      setInstitutionLoadError('Unable to load institutions from server.');
    } finally {
      setIsLoadingInstitutions(false);
    }
  }, []);

  useEffect(() => {
    fetchInstitutions();
  }, [fetchInstitutions]);

  // Load departments when institution changes
  useEffect(() => {
    if (!institutionId) {
      setDepartments([]);
      setDepartmentId('');
      setDepartmentLoadError(null);
      return;
    }

    if (institutionId === 'OTHER') {
      setDepartments([]);
      setDepartmentId('OTHER');
      setDepartmentLoadError(null);
      return;
    }

    let isMounted = true;
    const fetchDepartments = async () => {
      setIsLoadingDepartments(true);
      setDepartmentLoadError(null);
      try {
        const data = await getPublicDepartments(Number(institutionId));
        if (isMounted) {
          const depts = data || [];
          setDepartments(depts);
          setDepartmentLoadError(null);
          if (depts.length === 0) {
            setDepartmentId('OTHER');
          } else {
            setDepartmentId('');
          }
        }
      } catch (err) {
        console.error('Failed to load departments for institution', err);
        if (isMounted) {
          setDepartments([]);
          setDepartmentId('OTHER');
          setDepartmentLoadError(null);
        }
      } finally {
        if (isMounted) setIsLoadingDepartments(false);
      }
    };

    fetchDepartments();
    return () => {
      isMounted = false;
    };
  }, [institutionId]);

  const clearFieldError = (field: string) => {
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const copy = { ...prev };
        delete copy[field];
        return copy;
      });
    }
  };

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!firstName.trim()) {
      errors.firstName = 'First name is required';
    }

    if (!lastName.trim()) {
      errors.lastName = 'Last name is required';
    }

    if (!email.trim()) {
      errors.email = 'Institutional email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (!institutionId) {
      errors.institutionId = 'Please select your institution';
    }

    if (!departmentId) {
      errors.departmentId = 'Please select your department';
    }

    if (!password) {
      errors.password = 'Password is required';
    } else if (!isPasswordStrong) {
      errors.password = 'Password does not meet the complexity requirements';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Confirmation password is required';
    } else if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    if (!termsAccepted) {
      errors.termsAccepted = 'You must acknowledge and accept the laboratory usage policies';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      const resolvedInstitutionId =
        institutionId === 'OTHER'
          ? (institutions.find((i) => i.code === 'OTHER')?.id ?? null)
          : Number(institutionId);

      const resolvedDepartmentId =
        departmentId === 'OTHER' ? null : Number(departmentId);

      const response = await registerApi({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        phone: phone.trim() ? phone.trim() : undefined,
        institutionId: resolvedInstitutionId,
        departmentId: resolvedDepartmentId,
        password,
        confirmPassword,
        requestedRole: 'ROLE_RESEARCHER_STUDENT'
      });

      setSuccessData(response);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        if (err.code === 'ECONNABORTED') {
          setErrorMessage('Registration request timed out. Please verify the backend server is running and try again.');
        } else if (err.response?.status === 409) {
          setErrorMessage(
            err.response.data?.message ||
            'An account with this email address already exists. Please sign in or use a different email.'
          );
        } else if (err.response?.data?.message) {
          setErrorMessage(err.response.data.message);
        } else if (err.response?.data?.error) {
          setErrorMessage(String(err.response.data.error));
        } else if (err.response?.status === 400) {
          setErrorMessage('Invalid registration request. Please check all fields.');
        } else if (err.response?.status === 403) {
          setErrorMessage(
            err.response.data?.message ||
            'Access denied. Please check your network connection or refresh the page.'
          );
        } else if (err.code === 'ERR_NETWORK') {
          setErrorMessage('Unable to connect to the backend server. Please ensure the service is running.');
        } else {
          setErrorMessage(err.message || 'An unexpected error occurred during account creation. Please try again.');
        }
      } else {
        setErrorMessage('Failed to complete registration. Please try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center px-4 py-12">
      {/* Platform Branding */}
      <div className="text-center mb-8 max-w-md">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-sky-600/20 border border-sky-500/40 text-sky-400 mb-4 shadow-lg shadow-sky-600/10">
          <FlaskConical className="w-8 h-8" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
          Lab Resource Platform
        </h1>
        <p className="mt-2 text-sm text-slate-400">
          Enterprise University Laboratory Resource &amp; Utilization System
        </p>
      </div>

      {/* Main Registration Card */}
      <div className="w-full max-w-xl bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 shadow-2xl">
        {successData ? (
          /* Successful Registration State */
          <div className="text-center py-4 space-y-6">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 shadow-lg shadow-emerald-500/10">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <div>
              <h2 className="text-xl font-bold text-white tracking-tight">
                Account Successfully Created!
              </h2>
              <p className="text-xs text-slate-400 mt-2 max-w-md mx-auto">
                Welcome, <span className="font-semibold text-white">{successData.firstName} {successData.lastName}</span>! Your institutional profile has been registered with standard <span className="text-sky-400 font-medium">Researcher / Student</span> privileges.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-950/80 border border-slate-800 text-left space-y-2 text-xs">
              <div className="flex justify-between py-1 border-b border-slate-800/80">
                <span className="text-slate-400">Registered Email:</span>
                <span className="text-white font-medium">{successData.email}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-800/80">
                <span className="text-slate-400">Assigned Role:</span>
                <span className="inline-flex items-center gap-1 text-emerald-400 font-medium">
                  <ShieldCheck className="w-3.5 h-3.5" /> Researcher / Student
                </span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-400">Account Status:</span>
                <span className="text-emerald-400 font-medium">Active &amp; Ready</span>
              </div>
            </div>

            <div className="pt-2">
              <button
                type="button"
                onClick={() => navigate('/login', { state: { registeredEmail: successData.email } })}
                className="w-full py-2.5 px-4 bg-sky-600 hover:bg-sky-500 text-white text-sm font-semibold rounded-lg shadow-md shadow-sky-600/30 transition-colors focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-900"
              >
                Proceed to Sign In
              </button>
            </div>
          </div>
        ) : (
          /* Registration Form */
          <>
            <div className="mb-6">
              <h2 className="text-lg font-semibold text-white tracking-tight">Create an Account</h2>
              <p className="text-xs text-slate-400 mt-1">
                Register for laboratory equipment access, reservation privileges, and utilization telemetry.
              </p>
            </div>

            {/* Error Alert Banner */}
            {errorMessage && (
              <div
                className="mb-5 p-3.5 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-3 text-red-300 text-xs leading-relaxed"
                role="alert"
              >
                <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                <span>{errorMessage}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              {/* Full Name Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {/* First Name */}
                <div>
                  <label htmlFor="firstName" className="block text-xs font-medium text-slate-300 mb-1.5">
                    First Name <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <UserIcon className="w-4 h-4" />
                    </div>
                    <input
                      id="firstName"
                      type="text"
                      disabled={isSubmitting}
                      value={firstName}
                      onChange={(e) => {
                        setFirstName(e.target.value);
                        clearFieldError('firstName');
                      }}
                      placeholder="e.g. Ramesh"
                      className={`w-full pl-9 pr-3 py-2 text-sm bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.firstName ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                  </div>
                  {fieldErrors.firstName && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.firstName}</p>
                  )}
                </div>

                {/* Last Name */}
                <div>
                  <label htmlFor="lastName" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Last Name <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <UserIcon className="w-4 h-4" />
                    </div>
                    <input
                      id="lastName"
                      type="text"
                      disabled={isSubmitting}
                      value={lastName}
                      onChange={(e) => {
                        setLastName(e.target.value);
                        clearFieldError('lastName');
                      }}
                      placeholder="e.g. Kumar"
                      className={`w-full pl-9 pr-3 py-2 text-sm bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.lastName ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                  </div>
                  {fieldErrors.lastName && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.lastName}</p>
                  )}
                </div>
              </div>

              {/* Email & Phone Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {/* Institutional Email */}
                <div>
                  <label htmlFor="email" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Institutional Email <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Mail className="w-4 h-4" />
                    </div>
                    <input
                      id="email"
                      type="email"
                      autoComplete="email"
                      disabled={isSubmitting}
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value);
                        clearFieldError('email');
                      }}
                      placeholder="user@apitr.edu"
                      className={`w-full pl-9 pr-3 py-2 text-sm bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.email ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                  </div>
                  {fieldErrors.email && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.email}</p>
                  )}
                </div>

                {/* Phone Number */}
                <div>
                  <label htmlFor="phone" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Phone Number <span className="text-slate-500 font-normal">(Optional)</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Phone className="w-4 h-4" />
                    </div>
                    <input
                      id="phone"
                      type="tel"
                      autoComplete="tel"
                      disabled={isSubmitting}
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+91 98480 12345"
                      className="w-full pl-9 pr-3 py-2 text-sm bg-slate-950 border border-slate-800 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50"
                    />
                  </div>
                </div>
              </div>

              {/* Institution & Department Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {/* Institution Selector */}
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label htmlFor="institution" className="block text-xs font-medium text-slate-300">
                      Institution <span className="text-red-400">*</span>
                    </label>
                    {institutionLoadError && (
                      <button
                        type="button"
                        onClick={fetchInstitutions}
                        className="text-[11px] text-sky-400 hover:text-sky-300 underline font-medium"
                      >
                        Retry
                      </button>
                    )}
                  </div>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Building2 className="w-4 h-4" />
                    </div>
                    <select
                      id="institution"
                      disabled={isSubmitting || isLoadingInstitutions}
                      value={institutionId}
                      onChange={(e) => {
                        const val = e.target.value;
                        setInstitutionId(val === 'OTHER' ? 'OTHER' : val ? Number(val) : '');
                        clearFieldError('institutionId');
                      }}
                      className={`w-full pl-9 pr-8 py-2 text-sm bg-slate-950 border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 appearance-none ${
                        fieldErrors.institutionId ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    >
                      <option value="">
                        {isLoadingInstitutions
                          ? 'Loading institutions...'
                          : institutions.length === 0
                          ? 'No institutions available'
                          : 'Select Institution'}
                      </option>
                      {institutions
                        .filter((inst) => inst.code !== 'OTHER')
                        .map((inst) => (
                          <option key={inst.id} value={inst.id}>
                            {inst.name} ({inst.code})
                          </option>
                        ))}
                      <option value="OTHER">Others</option>
                    </select>
                  </div>
                  {fieldErrors.institutionId && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.institutionId}</p>
                  )}
                  {institutionLoadError && (
                    <p className="mt-1 text-[11px] text-amber-400/90">{institutionLoadError}</p>
                  )}
                </div>

                {/* Department Selector */}
                <div>
                  <label htmlFor="department" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Department <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <GraduationCap className="w-4 h-4" />
                    </div>
                    <select
                      id="department"
                      disabled={isSubmitting || !institutionId || isLoadingDepartments}
                      value={departmentId}
                      onChange={(e) => {
                        const val = e.target.value;
                        setDepartmentId(val === 'OTHER' ? 'OTHER' : val ? Number(val) : '');
                        clearFieldError('departmentId');
                      }}
                      className={`w-full pl-9 pr-8 py-2 text-sm bg-slate-950 border rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 appearance-none ${
                        fieldErrors.departmentId ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    >
                      <option value="">
                        {!institutionId
                          ? 'Select institution first'
                          : isLoadingDepartments
                          ? 'Loading departments...'
                          : 'Select Department'}
                      </option>
                      {departments
                        .filter((dept) => dept.code !== 'OTHER')
                        .map((dept) => (
                          <option key={dept.id} value={dept.id}>
                            {dept.name} ({dept.code})
                          </option>
                        ))}
                      {institutionId && <option value="OTHER">Others</option>}
                    </select>
                  </div>
                  {fieldErrors.departmentId && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.departmentId}</p>
                  )}
                  {institutionId && !isLoadingDepartments && departments.length === 0 && institutionId !== 'OTHER' && (
                    <p className="mt-1 text-[11px] text-sky-400/90 font-medium">
                      No departments registered for this institution. "Others" has been selected so you can proceed.
                    </p>
                  )}
                  {departmentLoadError && (
                    <p className="mt-1 text-[11px] text-amber-400/90">{departmentLoadError}</p>
                  )}
                </div>
              </div>

              {/* Account Type / Requested Role Banner */}
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800/80 flex items-start gap-3">
                <ShieldCheck className="w-5 h-5 text-sky-400 shrink-0 mt-0.5" />
                <div className="text-xs">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-200">Account Privilege:</span>
                    <span className="px-2 py-0.5 rounded-md bg-sky-500/10 text-sky-400 border border-sky-500/20 text-[11px] font-medium">
                      Researcher / Student
                    </span>
                  </div>
                  <p className="text-slate-400 mt-1 text-[11px] leading-relaxed">
                    Public self-registration automatically grants standard researcher/student reservation privileges. Laboratory technician, lab manager, or institutional administrative rights require verification and assignment by an authorized administrator.
                  </p>
                </div>
              </div>

              {/* Password Fields Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {/* Password */}
                <div>
                  <label htmlFor="password" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Password <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Lock className="w-4 h-4" />
                    </div>
                    <input
                      id="password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      disabled={isSubmitting}
                      value={password}
                      onChange={(e) => {
                        setPassword(e.target.value);
                        clearFieldError('password');
                      }}
                      placeholder="At least 8 characters"
                      className={`w-full pl-9 pr-10 py-2 text-sm bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.password ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                    <button
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-200 transition-colors"
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                  {fieldErrors.password && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.password}</p>
                  )}
                </div>

                {/* Confirm Password */}
                <div>
                  <label htmlFor="confirmPassword" className="block text-xs font-medium text-slate-300 mb-1.5">
                    Confirm Password <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Lock className="w-4 h-4" />
                    </div>
                    <input
                      id="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      disabled={isSubmitting}
                      value={confirmPassword}
                      onChange={(e) => {
                        setConfirmPassword(e.target.value);
                        clearFieldError('confirmPassword');
                      }}
                      placeholder="Repeat password"
                      className={`w-full pl-9 pr-10 py-2 text-sm bg-slate-950 border rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-transparent transition-colors disabled:opacity-50 ${
                        fieldErrors.confirmPassword ? 'border-red-500/60' : 'border-slate-800'
                      }`}
                    />
                    <button
                      type="button"
                      disabled={isSubmitting}
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-200 transition-colors"
                      aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                    >
                      {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                  {fieldErrors.confirmPassword && (
                    <p className="mt-1 text-xs text-red-400">{fieldErrors.confirmPassword}</p>
                  )}
                </div>
              </div>

              {/* Password Complexity Checklist */}
              {password.length > 0 && (
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 text-[11px] space-y-1.5">
                  <span className="text-slate-400 font-medium block mb-1">Password Requirements:</span>
                  <div className="grid grid-cols-2 gap-1.5 text-slate-400">
                    <div className={`flex items-center gap-1.5 ${hasMinLength ? 'text-emerald-400' : 'text-slate-500'}`}>
                      {hasMinLength ? <Check className="w-3.5 h-3.5" /> : <X className="w-3.5 h-3.5" />}
                      <span>At least 8 characters</span>
                    </div>
                    <div className={`flex items-center gap-1.5 ${hasUpperCase ? 'text-emerald-400' : 'text-slate-500'}`}>
                      {hasUpperCase ? <Check className="w-3.5 h-3.5" /> : <X className="w-3.5 h-3.5" />}
                      <span>1 uppercase letter</span>
                    </div>
                    <div className={`flex items-center gap-1.5 ${hasLowerCase ? 'text-emerald-400' : 'text-slate-500'}`}>
                      {hasLowerCase ? <Check className="w-3.5 h-3.5" /> : <X className="w-3.5 h-3.5" />}
                      <span>1 lowercase letter</span>
                    </div>
                    <div className={`flex items-center gap-1.5 ${hasNumber ? 'text-emerald-400' : 'text-slate-500'}`}>
                      {hasNumber ? <Check className="w-3.5 h-3.5" /> : <X className="w-3.5 h-3.5" />}
                      <span>1 number (0-9)</span>
                    </div>
                  </div>
                </div>
              )}

              {/* Terms / Consent Checkbox */}
              <div>
                <label className="flex items-start gap-2.5 cursor-pointer mt-1">
                  <input
                    type="checkbox"
                    checked={termsAccepted}
                    disabled={isSubmitting}
                    onChange={(e) => {
                      setTermsAccepted(e.target.checked);
                      clearFieldError('termsAccepted');
                    }}
                    className="mt-0.5 rounded border-slate-700 bg-slate-950 text-sky-600 focus:ring-sky-500 focus:ring-offset-slate-900"
                  />
                  <span className="text-xs text-slate-400 leading-relaxed select-none">
                    I acknowledge and agree to institutional laboratory safety guidelines, equipment operational protocols, and user terms of service.
                  </span>
                </label>
                {fieldErrors.termsAccepted && (
                  <p className="mt-1 text-xs text-red-400">{fieldErrors.termsAccepted}</p>
                )}
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full mt-3 py-2.5 px-4 bg-sky-600 hover:bg-sky-500 text-white text-sm font-semibold rounded-lg shadow-md shadow-sky-600/30 transition-colors focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    <span>Creating Account...</span>
                  </>
                ) : (
                  <span>Complete Registration</span>
                )}
              </button>
            </form>

            {/* Back to Login Link */}
            <div className="mt-6 pt-5 border-t border-slate-800 text-center">
              <p className="text-xs text-slate-400">
                Already registered?{' '}
                <Link to="/login" className="text-sky-400 hover:text-sky-300 font-semibold transition-colors">
                  Sign In to Platform
                </Link>
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
