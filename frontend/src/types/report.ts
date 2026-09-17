// ==========================================
// Reports Domain TypeScript Types
// ==========================================

export interface ReportFilterParams {
  departmentId?: number;
  startDate?: string; // YYYY-MM-DD
  endDate?: string;   // YYYY-MM-DD
}

export interface ReportMetadataResponse {
  reportType: string;
  title: string;
  generatedAt: string;
  startDate: string;
  endDate: string;
  institutionId: number;
  departmentId?: number;
  recordCount: number;
}

export interface ReportTypeDefinition {
  id: string;
  name: string;
  description: string;
  supportedFormats: string[];
}

// 1. Utilization Report
export interface UtilizationReportRow {
  equipmentId: number;
  equipmentName: string;
  departmentId?: number;
  departmentName?: string;
  actualUsageHours: number;
  operatingHours: number;
  utilizationPercentage: number;
  sessionCount: number;
}

export interface UtilizationSummary {
  overallUtilizationPercentage: number;
  totalOperatingHours: number;
  totalUsageHours: number;
  totalSessions: number;
  equipmentCount: number;
}

export interface UtilizationReportResponse {
  metadata: ReportMetadataResponse;
  summary: UtilizationSummary;
  rows: UtilizationReportRow[];
}

// 2. Booking Report
export interface BookingSummary {
  totalBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  noShowBookings: number;
  pendingBookings: number;
  inUseBookings: number;
  totalBookedHours: number;
  averageDurationMinutes: number;
}

export interface BookingEquipmentRow {
  equipmentId: number;
  equipmentName: string;
  bookingCount: number;
  bookedHours: number;
}

export interface BookingDailyRow {
  date: string;
  totalCount: number;
  completedCount: number;
  cancelledCount: number;
}

export interface BookingReportResponse {
  metadata: ReportMetadataResponse;
  summary: BookingSummary;
  statusDistribution: Record<string, number>;
  equipmentRows: BookingEquipmentRow[];
  dailyRows: BookingDailyRow[];
}

// 3. Maintenance Report
export interface MaintenanceSummary {
  totalRequests: number;
  totalWorkOrders: number;
  totalDowntimeHours: number;
  downtimeIncidentCount: number;
}

export interface MaintenanceCategoryRow {
  category: string;
  incidentCount: number;
  durationHours: number;
  percentageOfTotal: number;
}

export interface MaintenanceEquipmentRow {
  equipmentId: number;
  equipmentName: string;
  incidentCount: number;
  durationHours: number;
}

export interface MaintenanceReportResponse {
  metadata: ReportMetadataResponse;
  summary: MaintenanceSummary;
  requestStatusDistribution: Record<string, number>;
  workOrderStatusDistribution: Record<string, number>;
  categoryRows: MaintenanceCategoryRow[];
  equipmentRows: MaintenanceEquipmentRow[];
}

// 4. Cost Report
export interface CostSummary {
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  totalBookings: number;
}

export interface CostDepartmentRow {
  departmentId: number;
  departmentName: string;
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  bookingCount: number;
}

export interface CostEquipmentRow {
  equipmentId: number;
  equipmentName: string;
  totalCost: number;
  billableHours: number;
  bookingCount: number;
}

export interface CostReportResponse {
  metadata: ReportMetadataResponse;
  summary: CostSummary;
  departmentRows: CostDepartmentRow[];
  equipmentRows: CostEquipmentRow[];
}

// 5. Equipment Inventory Report
export interface EquipmentInventorySummary {
  totalEquipment: number;
  activeEquipment: number;
  operationalCount: number;
  underMaintenanceCount: number;
  decommissionedCount: number;
}

export interface EquipmentInventoryRow {
  equipmentId: number;
  equipmentName: string;
  model: string;
  serialNumber: string;
  departmentName: string;
  categoryName: string;
  status: string;
  utilizationPercentage: number;
  downtimeHours: number;
  totalCost: number;
  bookingCount: number;
}

export interface EquipmentInventoryReportResponse {
  metadata: ReportMetadataResponse;
  summary: EquipmentInventorySummary;
  rows: EquipmentInventoryRow[];
}

// 6. Management Summary Report
export interface ManagementKpiSummary {
  totalBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  noShowBookings: number;
  averageUtilizationPercentage: number;
  totalOperatingHours: number;
  totalUsageHours: number;
  totalDowntimeHours: number;
  downtimeIncidentCount: number;
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  activeEquipmentCount: number;
  maintenanceRequestCount: number;
  openWorkOrderCount: number;
}

export interface PerformanceItem {
  equipmentId: number;
  equipmentName: string;
  departmentName?: string;
  metricValue: number;
  metricUnit?: string;
  utilizationPercentage?: number;
  totalUsageHours?: number;
  totalDowntimeHours?: number;
  totalCost?: number;
  bookingCount?: number;
}

export interface ManagementSummaryReportResponse {
  metadata: ReportMetadataResponse;
  kpis: ManagementKpiSummary;
  topUtilizedEquipment: PerformanceItem[];
  highestDowntimeEquipment: PerformanceItem[];
  highestCostEquipment: PerformanceItem[];
}
