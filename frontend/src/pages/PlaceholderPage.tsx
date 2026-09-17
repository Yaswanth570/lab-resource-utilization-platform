import React from 'react';
import { Construction, CheckCircle2, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

interface PlaceholderPageProps {
  title: string;
  description: string;
  backendModule: string;
}

export const PlaceholderPage: React.FC<PlaceholderPageProps> = ({
  title,
  description,
  backendModule,
}) => {
  return (
    <div className="max-w-3xl mx-auto py-12 px-4 text-center">
      <div className="p-8 sm:p-12 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400 mb-6 shadow-inner">
          <Construction className="w-8 h-8" />
        </div>

        <h2 className="text-xl sm:text-2xl font-bold text-white tracking-tight">{title}</h2>
        <p className="mt-2 text-sm text-slate-400 max-w-lg mx-auto">{description}</p>

        {/* Backend Readiness Note */}
        <div className="mt-8 p-4 rounded-xl bg-slate-950 border border-slate-800/80 text-left max-w-md mx-auto">
          <div className="flex items-start gap-3">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <div className="text-xs">
              <div className="font-semibold text-slate-200">Backend API Services Active</div>
              <div className="text-slate-400 mt-0.5">
                The {backendModule} backend domain services, JPA entities, and REST controller endpoints have been completed and verified in Task 1E.7.
              </div>
            </div>
          </div>
        </div>

        <div className="mt-8">
          <Link
            to="/dashboard"
            className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg border border-slate-700 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Return to Dashboard</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
