import React, { useState, useEffect } from 'react';
import { 
  Server, 
  Activity, 
  Database, 
  Layers, 
  CheckCircle2, 
  AlertCircle, 
  RefreshCw, 
  Shield, 
  Cpu 
} from 'lucide-react';
import { getHealth, type HealthResponse } from '../api/client';

interface ModuleCardProps {
  name: string;
  category: 'core' | 'resource' | 'operations' | 'finance' | 'insights';
  description: string;
}

const MODULES: ModuleCardProps[] = [
  { name: 'auth', category: 'core', description: 'Authentication, JWT validation & token lifecycle' },
  { name: 'user', category: 'core', description: 'User roles, permissions & researcher identities' },
  { name: 'institution', category: 'core', description: 'Universities, research centers & partner organizations' },
  { name: 'department', category: 'core', description: 'Academic faculties, lab divisions & hierarchies' },
  { name: 'equipment', category: 'resource', description: 'Catalog, hardware specs, availability & operating status' },
  { name: 'booking', category: 'resource', description: 'Time-slot reservation & automated waitlist queuing' },
  { name: 'utilization', category: 'resource', description: 'Real-time telemetry, session logging & idle detection' },
  { name: 'sharing', category: 'operations', description: 'Cross-institutional sharing agreements & access governance' },
  { name: 'maintenance', category: 'operations', description: 'Preventive schedules, service tickets & downtime logs' },
  { name: 'calibration', category: 'operations', description: 'Precision certification, audit trails & standards compliance' },
  { name: 'cost', category: 'finance', description: 'Tiered rate calculations & internal chargeback billing' },
  { name: 'notification', category: 'insights', description: 'Multi-channel event alerts (email, SMS & push)' },
  { name: 'analytics', category: 'insights', description: 'Utilization heatmaps, efficiency KPIs & telemetry charts' },
  { name: 'report', category: 'insights', description: 'Institutional compliance reporting & audit exports' },
];

export const FoundationOverview: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [latency, setLatency] = useState<number | null>(null);
  const [lastChecked, setLastChecked] = useState<Date | null>(null);

  const checkHealthStatus = async () => {
    setLoading(true);
    setError(null);
    const start = performance.now();
    try {
      const data = await getHealth();
      const elapsed = Math.round(performance.now() - start);
      setHealth(data);
      setLatency(elapsed);
      setLastChecked(new Date());
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Unable to connect to backend';
      setError(errorMessage);
      setHealth(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkHealthStatus();
  }, []);

  return (
    <div className="min-h-screen bg-[#090d16] text-slate-100 flex flex-col selection:bg-cyan-500/20 selection:text-cyan-200">
      {/* Top Header / Brand Bar */}
      <header className="border-b border-slate-800/80 bg-[#0d1322]/80 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-600 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
              <Cpu className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="font-bold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                Lab Resource Utilization Platform
              </div>
              <div className="text-xs text-slate-400 font-mono">Architectural Foundation • v0.1.0</div>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2 px-3 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs font-mono">
              <span className={`w-2 h-2 rounded-full ${health?.status === 'UP' ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400'}`}></span>
              <span className="text-slate-300">
                Backend: {loading ? 'Checking...' : health?.status === 'UP' ? 'Connected (UP)' : 'Offline / Standby'}
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {/* Foundation Hero Banner */}
        <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 via-[#0e1628] to-[#0a1120] border border-slate-800/80 p-8 shadow-2xl">
          <div className="absolute -right-16 -bottom-16 w-80 h-80 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>
          <div className="absolute right-1/4 -top-16 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>

          <div className="relative z-10 max-w-3xl space-y-4">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-cyan-950/60 border border-cyan-800/50 text-cyan-400 text-xs font-medium tracking-wide uppercase">
              <span>Phase 1</span>
              <span>•</span>
              <span>Project Foundation & Architecture</span>
            </div>

            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
              Enterprise Resource Platform for Research & Laboratory Networks
            </h1>

            <p className="text-slate-400 text-base leading-relaxed">
              Clean modular monolith architecture connecting academic institutions, laboratory faculties, 
              shared equipment reservation, precision telemetry, and cross-organization chargebacks.
            </p>
          </div>
        </section>

        {/* Live System Health & Backend Verifier */}
        <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 rounded-xl bg-[#0e1526]/90 border border-slate-800/80 p-6 backdrop-blur shadow-lg">
            <div className="flex items-center justify-between pb-4 mb-4 border-b border-slate-800">
              <div className="flex items-center space-x-3">
                <div className="p-2 rounded-lg bg-cyan-950/70 border border-cyan-800/40 text-cyan-400">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-white">API Health & Connectivity</h2>
                  <p className="text-xs text-slate-400">Verifying live Spring Boot endpoint: <code className="text-cyan-300 font-mono">GET /api/health</code></p>
                </div>
              </div>

              <button
                onClick={checkHealthStatus}
                disabled={loading}
                className="inline-flex items-center space-x-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition disabled:opacity-50"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
                <span>Test Endpoint</span>
              </button>
            </div>

            {loading ? (
              <div className="py-8 flex flex-col items-center justify-center space-y-3">
                <RefreshCw className="w-8 h-8 text-cyan-400 animate-spin" />
                <div className="text-sm text-slate-400 font-mono">Querying backend service status...</div>
              </div>
            ) : health ? (
              <div className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="p-4 rounded-lg bg-emerald-950/20 border border-emerald-800/30">
                    <div className="text-xs font-mono uppercase text-emerald-400 font-medium">Status</div>
                    <div className="text-2xl font-bold text-emerald-300 flex items-center space-x-2 mt-1">
                      <CheckCircle2 className="w-6 h-6" />
                      <span>{health.status}</span>
                    </div>
                  </div>

                  <div className="p-4 rounded-lg bg-slate-900/60 border border-slate-800">
                    <div className="text-xs font-mono uppercase text-slate-400 font-medium">Roundtrip Latency</div>
                    <div className="text-2xl font-bold text-white mt-1 font-mono">
                      {latency} <span className="text-sm font-normal text-slate-400">ms</span>
                    </div>
                  </div>

                  <div className="p-4 rounded-lg bg-slate-900/60 border border-slate-800">
                    <div className="text-xs font-mono uppercase text-slate-400 font-medium">Verified Timestamp</div>
                    <div className="text-sm font-mono text-slate-300 mt-2 truncate">
                      {lastChecked?.toLocaleTimeString() || 'N/A'}
                    </div>
                  </div>
                </div>

                <div className="p-3 rounded-lg bg-slate-950/80 border border-slate-800/80 font-mono text-xs text-slate-300 space-y-1">
                  <div className="text-slate-500">// Response Payload</div>
                  <pre className="text-emerald-400 font-semibold">{JSON.stringify(health, null, 2)}</pre>
                </div>
              </div>
            ) : (
              <div className="p-5 rounded-lg bg-amber-950/20 border border-amber-800/40 text-amber-300 space-y-2">
                <div className="flex items-center space-x-2 font-medium">
                  <AlertCircle className="w-5 h-5 text-amber-400" />
                  <span>Backend Not Responding on http://localhost:8080</span>
                </div>
                <p className="text-xs text-amber-200/80">
                  Ensure the Spring Boot backend service is running (<code className="font-mono bg-amber-900/40 px-1 py-0.5 rounded">./mvnw spring-boot:run</code>)
                  and the database or Hikari connection pool is configured.
                </p>
                {error && <div className="text-xs font-mono text-amber-400/90 pt-1">Error: {error}</div>}
              </div>
            )}
          </div>

          {/* Technical Specs Card */}
          <div className="rounded-xl bg-[#0e1526]/90 border border-slate-800/80 p-6 backdrop-blur shadow-lg flex flex-col justify-between">
            <div>
              <div className="flex items-center space-x-3 pb-4 mb-4 border-b border-slate-800">
                <div className="p-2 rounded-lg bg-blue-950/70 border border-blue-800/40 text-blue-400">
                  <Server className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-white">Stack Architecture</h2>
                  <p className="text-xs text-slate-400">Foundation specifications</p>
                </div>
              </div>

              <div className="space-y-3 text-xs font-mono">
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Backend Runtime</span>
                  <span className="text-white font-medium">Java 21 (LTS)</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Framework</span>
                  <span className="text-white font-medium">Spring Boot 3.4.3</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Build Tool</span>
                  <span className="text-white font-medium">Maven 3.9.16 (Wrapper)</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Database Engine</span>
                  <span className="text-white font-medium">MySQL 8.0 (JPA/Hibernate)</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Frontend Client</span>
                  <span className="text-white font-medium">React 19 + TypeScript</span>
                </div>
                <div className="flex justify-between items-center py-1 border-b border-slate-800/50">
                  <span className="text-slate-400">Bundler & Styling</span>
                  <span className="text-white font-medium">Vite + Tailwind CSS v4</span>
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
              <span className="flex items-center space-x-1.5">
                <Shield className="w-3.5 h-3.5 text-cyan-400" />
                <span>Stateless Security</span>
              </span>
              <span className="flex items-center space-x-1.5">
                <Database className="w-3.5 h-3.5 text-blue-400" />
                <span>Env-Configured DB</span>
              </span>
            </div>
          </div>
        </section>

        {/* Modular Monolith Architecture Blueprint */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Layers className="w-5 h-5 text-cyan-400" />
              <h2 className="text-xl font-bold text-white">Modular Monolith Blueprint</h2>
            </div>
            <span className="text-xs font-mono px-2.5 py-1 rounded bg-slate-800 text-slate-300 border border-slate-700">
              14 Domain Modules Scaffolded
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {MODULES.map((mod) => (
              <div 
                key={mod.name}
                className="group relative rounded-xl bg-[#0c1220] border border-slate-800 p-4 transition-all duration-200 hover:border-cyan-700/60 hover:bg-[#0f1729]"
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="font-mono text-sm font-semibold text-white group-hover:text-cyan-300 transition">
                    /{mod.name}
                  </span>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-900 border border-slate-800 text-slate-400">
                    Foundation
                  </span>
                </div>
                <p className="text-xs text-slate-400 leading-snug">
                  {mod.description}
                </p>
              </div>
            ))}
          </div>
        </section>

      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-[#090d16] py-6 text-center text-xs text-slate-500 font-mono">
        Lab Resource Utilization Platform • Foundation Phase • Modular Monolith Architecture
      </footer>
    </div>
  );
};
