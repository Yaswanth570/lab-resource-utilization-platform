// ==========================================
// Analytics Domain TypeScript Types
// ==========================================

export interface AnalyticsFilterParams {
  departmentId?: number;
  startDate?: string; // YYYY-MM-DD
  endDate?: string;   // YYYY-MM-DD
}

// 1. Overview
export interface AnalyticsOverviewResponse {
  totalBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  noShowBookings: number;
  totalUsageHours: number;
  totalOperatingHours: number;
  averageUtilizationPercentage: number;
  totalDowntimeHours: number;
  downtimeIncidentCount: number;
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  activeEquipmentCount: number;
  maintenanceRequestCount: number;
  openWorkOrderCount: number;
  startDate?: string;
  endDate?: string;
}

// 2. Utilization
export interface EquipmentUtilizationMetric {
  equipmentId: number;
  equipmentName: string;
  departmentId?: number;
  departmentName?: string;
  actualUsageMinutes: number;
  actualUsageHours: number;
  operatingMinutes: number;
  operatingHours: number;
  utilizationPercentage: number;
  sessionCount: number;
}

export interface DailyUtilizationPoint {
  date: string;
  actualUsageMinutes: number;
  actualUsageHours: number;
  operatingMinutes: number;
  operatingHours: number;
  utilizationPercentage: number;
  sessionCount: number;
}

export interface UtilizationAnalyticsResponse {
  overallUtilizationPercentage: number;
  totalOperatingHours: number;
  totalUsageHours: number;
  totalSessions: number;
  startDate?: string;
  endDate?: string;
  equipmentMetrics: EquipmentUtilizationMetric[];
  dailyTrends: DailyUtilizationPoint[];
}

export interface EquipmentUtilizationDetailResponse {
  equipmentId: number;
  equipmentName: string;
  modelNumber?: string;
  serialNumber?: string;
  departmentId?: number;
  departmentName?: string;
  actualUsageMinutes: number;
  actualUsageHours: number;
  operatingMinutes: number;
  operatingHours: number;
  utilizationPercentage: number;
  sessionCount: number;
  startDate?: string;
  endDate?: string;
  dailyTrends: DailyUtilizationPoint[];
}

// 3. Bookings
export interface DailyBookingPoint {
  date: string;
  totalCount: number;
  completedCount: number;
  cancelledCount: number;
}

export interface EquipmentBookingFrequency {
  equipmentId: number;
  equipmentName: string;
  bookingCount: number;
  bookedHours: number;
}

export interface BookingAnalyticsResponse {
  totalBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  noShowBookings: number;
  pendingBookings: number;
  inUseBookings: number;
  totalBookedHours: number;
  averageBookingDurationMinutes: number;
  statusDistribution: Record<string, number>;
  dailyTrends: DailyBookingPoint[];
  equipmentFrequencies: EquipmentBookingFrequency[];
  startDate?: string;
  endDate?: string;
}

// 4. Maintenance
export interface DowntimeCategoryMetric {
  category: string;
  durationMinutes: number;
  durationHours: number;
  incidentCount: number;
  percentageOfTotal: number;
}

export interface EquipmentDowntimeMetric {
  equipmentId: number;
  equipmentName: string;
  durationMinutes: number;
  durationHours: number;
  incidentCount: number;
}

export interface MaintenanceAnalyticsResponse {
  totalRequests: number;
  totalWorkOrders: number;
  totalDowntimeMinutes: number;
  totalDowntimeHours: number;
  requestStatusDistribution: Record<string, number>;
  requestPriorityDistribution: Record<string, number>;
  workOrderStatusDistribution: Record<string, number>;
  downtimeByCategory: DowntimeCategoryMetric[];
  equipmentDowntime: EquipmentDowntimeMetric[];
  startDate?: string;
  endDate?: string;
}

// 5. Cost
export interface DepartmentCostMetric {
  departmentId: number;
  departmentName: string;
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  bookingCount: number;
}

export interface EquipmentCostMetric {
  equipmentId: number;
  equipmentName: string;
  totalCost: number;
  billableHours: number;
  bookingCount: number;
}

export interface DailyCostPoint {
  date: string;
  cost: number;
  bookingCount: number;
}

export interface CostAnalyticsResponse {
  totalCost: number;
  unbilledCost: number;
  invoicedCost: number;
  settledCost: number;
  departmentCosts: DepartmentCostMetric[];
  equipmentCosts: EquipmentCostMetric[];
  dailyCosts: DailyCostPoint[];
  startDate?: string;
  endDate?: string;
}

// 6. Equipment Performance
export interface PerformanceItem {
  equipmentId: number;
  equipmentName: string;
  departmentName?: string;
  metricValue: number;
  metricUnit: string;
  utilizationPercentage: number;
  totalUsageHours: number;
  totalDowntimeHours: number;
  totalCost: number;
  bookingCount: number;
}

export interface EquipmentPerformanceResponse {
  mostUtilized: PerformanceItem[];
  leastUtilized: PerformanceItem[];
  highestDowntime: PerformanceItem[];
  highestCost: PerformanceItem[];
  highestBookingFrequency: PerformanceItem[];
  startDate?: string;
  endDate?: string;
}

// 7. Trends
export interface DailyTrendPoint {
  date: string;
  bookingCount: number;
  usageMinutes: number;
  usageHours: number;
  downtimeMinutes: number;
  downtimeHours: number;
  cost: number;
}

export interface TrendAnalyticsResponse {
  startDate?: string;
  endDate?: string;
  dailyTrends: DailyTrendPoint[];
}
