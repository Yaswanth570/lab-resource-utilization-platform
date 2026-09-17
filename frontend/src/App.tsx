import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { PublicOnlyRoute } from './components/auth/PublicOnlyRoute';
import { RoleRoute } from './components/auth/RoleRoute';
import { ROLES } from './utils/rbac';
import { AppShell } from './components/layout/AppShell';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { PlaceholderPage } from './pages/PlaceholderPage';
import { EquipmentListPage } from './pages/equipment/EquipmentListPage';
import { EquipmentDetailsPage } from './pages/equipment/EquipmentDetailsPage';
import { BookingListPage } from './pages/booking/BookingListPage';
import { BookingDetailsPage } from './pages/booking/BookingDetailsPage';
import { UtilizationPage } from './pages/utilization/UtilizationPage';
import { UsageSessionDetailsPage } from './pages/utilization/UsageSessionDetailsPage';
import { MaintenancePage } from './pages/maintenance/MaintenancePage';
import { MaintenanceRequestDetailsPage } from './pages/maintenance/MaintenanceRequestDetailsPage';
import { WorkOrderDetailsPage } from './pages/maintenance/WorkOrderDetailsPage';
import { SharingPage } from './pages/sharing/SharingPage';
import { SharingAgreementDetailsPage } from './pages/sharing/SharingAgreementDetailsPage';
import { SharedAllocationDetailsPage } from './pages/sharing/SharedAllocationDetailsPage';
import { CostPage } from './pages/cost/CostPage';
import { InvoiceDetailsPage } from './pages/cost/InvoiceDetailsPage';
import { NotificationsPage } from './pages/notification/NotificationsPage';
import { AnalyticsPage } from './pages/analytics/AnalyticsPage';
import { ReportsPage } from './pages/reports/ReportsPage';
import { AccessDeniedPage } from './pages/AccessDeniedPage';

export function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Authentication Route */}
          <Route element={<PublicOnlyRoute />}>
            <Route path="/login" element={<LoginPage />} />
          </Route>

          {/* Protected Application Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppShell />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/access-denied" element={<AccessDeniedPage />} />

              {/* Equipment Module - Browsing available to all authenticated */}
              <Route path="/equipment" element={<EquipmentListPage />} />
              <Route path="/equipment/:id" element={<EquipmentDetailsPage />} />

              {/* Booking Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.RESEARCHER_STUDENT,
                      ROLES.LAB_MANAGER,
                      ROLES.DEPARTMENT_HEAD,
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/bookings" element={<BookingListPage />} />
                <Route path="/bookings/:id" element={<BookingDetailsPage />} />
              </Route>

              {/* Utilization Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.LAB_MANAGER,
                      ROLES.DEPARTMENT_HEAD,
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/utilization" element={<UtilizationPage />} />
                <Route path="/utilization/sessions/:id" element={<UsageSessionDetailsPage />} />
              </Route>

              {/* Maintenance Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.LAB_TECHNICIAN,
                      ROLES.LAB_MANAGER,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/maintenance" element={<MaintenancePage />} />
                <Route path="/maintenance/requests/:id" element={<MaintenanceRequestDetailsPage />} />
                <Route path="/maintenance/work-orders/:id" element={<WorkOrderDetailsPage />} />
              </Route>

              {/* Sharing Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.LAB_MANAGER,
                      ROLES.DEPARTMENT_HEAD,
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/sharing" element={<SharingPage />} />
                <Route path="/sharing/agreements/:id" element={<SharingAgreementDetailsPage />} />
                <Route path="/sharing/allocations/:id" element={<SharedAllocationDetailsPage />} />
              </Route>

              {/* Cost Management Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/cost" element={<CostPage />} />
                <Route path="/cost/invoices/:id" element={<InvoiceDetailsPage />} />
              </Route>

              {/* Analytics Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.LAB_MANAGER,
                      ROLES.DEPARTMENT_HEAD,
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/analytics" element={<AnalyticsPage />} />
              </Route>

              {/* Reports Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.LAB_MANAGER,
                      ROLES.DEPARTMENT_HEAD,
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route path="/reports" element={<ReportsPage />} />
              </Route>

              {/* Administration Module */}
              <Route
                element={
                  <RoleRoute
                    allowedRoles={[
                      ROLES.INSTITUTION_ADMIN,
                      ROLES.SYSTEM_ADMIN,
                    ]}
                  />
                }
              >
                <Route
                  path="/admin"
                  element={
                    <PlaceholderPage
                      title="Platform Administration"
                      description="Manage institutions, departments, user roles, system parameters, and operational windows."
                      backendModule="User, Role & Institution Administration"
                    />
                  }
                />
              </Route>
            </Route>
          </Route>

          {/* Catch-all Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
