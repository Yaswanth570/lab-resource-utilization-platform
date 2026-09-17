package com.labresource.platform.analytics.service;

import com.labresource.platform.analytics.web.*;

import java.time.LocalDate;

public interface AnalyticsService {

    /**
     * Retrieves top-level overview KPIs for the institution within the specified date range.
     */
    AnalyticsOverviewResponse getOverview(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves overall and per-equipment utilization metrics within the specified date range.
     */
    UtilizationAnalyticsResponse getUtilizationAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves detailed utilization metrics for a specific equipment item.
     */
    EquipmentUtilizationDetailResponse getEquipmentUtilization(Long equipmentId, LocalDate startDate, LocalDate endDate, Long institutionId);

    /**
     * Retrieves booking analytics, status distributions, and equipment frequencies.
     */
    BookingAnalyticsResponse getBookingAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves maintenance analytics, request/work order breakdowns, and downtime by category.
     */
    MaintenanceAnalyticsResponse getMaintenanceAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves cost analytics, department spending, and top equipment costs.
     */
    CostAnalyticsResponse getCostAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves ranked equipment leaderboards across utilization, downtime, cost, and bookings.
     */
    EquipmentPerformanceResponse getEquipmentPerformance(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves unified multi-metric chronological daily trends.
     */
    TrendAnalyticsResponse getTrends(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate);
}
