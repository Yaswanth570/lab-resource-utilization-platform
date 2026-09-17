package com.labresource.platform.report.service;

import com.labresource.platform.analytics.service.AnalyticsService;
import com.labresource.platform.analytics.web.*;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.report.web.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AnalyticsService analyticsService;
    private final EquipmentRepository equipmentRepository;
    private final DepartmentRepository departmentRepository;

    public ReportServiceImpl(AnalyticsService analyticsService,
                             EquipmentRepository equipmentRepository,
                             DepartmentRepository departmentRepository) {
        this.analyticsService = analyticsService;
        this.equipmentRepository = equipmentRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<ReportTypeDefinition> getAvailableReports() {
        return List.of(
                new ReportTypeDefinition("utilization", "Utilization Report",
                        "Equipment utilization rates, actual usage hours, operating hours, and session metrics.",
                        List.of("JSON", "CSV")),
                new ReportTypeDefinition("bookings", "Booking & Reservation Report",
                        "Booking volumes, status distributions, booked hours, equipment frequencies, and completion rates.",
                        List.of("JSON", "CSV")),
                new ReportTypeDefinition("maintenance", "Maintenance & Downtime Report",
                        "Maintenance requests, work order statuses, downtime incidents, and category distributions.",
                        List.of("JSON", "CSV")),
                new ReportTypeDefinition("cost", "Cost & Billing Report",
                        "Total equipment and department expenditure, unbilled, invoiced, and settled usage costs.",
                        List.of("JSON", "CSV")),
                new ReportTypeDefinition("equipment", "Equipment Inventory & Performance Report",
                        "Comprehensive equipment catalog with operational statuses, utilization, downtime, and cost.",
                        List.of("JSON", "CSV")),
                new ReportTypeDefinition("management", "Management Summary Report",
                        "Executive cross-domain summary uniting utilization, bookings, maintenance, and expenditure KPIs.",
                        List.of("JSON", "CSV"))
        );
    }

    @Override
    public UtilizationReportResponse generateUtilizationReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        UtilizationAnalyticsResponse analytics = analyticsService.getUtilizationAnalytics(institutionId, departmentId, range[0], range[1]);

        UtilizationReportResponse report = new UtilizationReportResponse();
        report.setMetadata(new ReportMetadataResponse("UTILIZATION", "Equipment Utilization Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId,
                analytics.getEquipmentMetrics() != null ? analytics.getEquipmentMetrics().size() : 0));

        UtilizationReportResponse.UtilizationSummary summary = report.getSummary();
        summary.setOverallUtilizationPercentage(analytics.getOverallUtilizationPercentage());
        summary.setTotalOperatingHours(analytics.getTotalOperatingHours());
        summary.setTotalUsageHours(analytics.getTotalUsageHours());
        summary.setTotalSessions(analytics.getTotalSessions());
        summary.setEquipmentCount(analytics.getEquipmentMetrics() != null ? analytics.getEquipmentMetrics().size() : 0);

        List<UtilizationReportResponse.UtilizationReportRow> rows = new ArrayList<>();
        if (analytics.getEquipmentMetrics() != null) {
            for (UtilizationAnalyticsResponse.EquipmentUtilizationMetric m : analytics.getEquipmentMetrics()) {
                rows.add(new UtilizationReportResponse.UtilizationReportRow(
                        m.getEquipmentId(),
                        m.getEquipmentName(),
                        m.getDepartmentId(),
                        m.getDepartmentName(),
                        m.getActualUsageHours(),
                        m.getOperatingHours(),
                        m.getUtilizationPercentage(),
                        m.getSessionCount()
                ));
            }
        }
        report.setRows(rows);
        return report;
    }

    @Override
    public BookingReportResponse generateBookingReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        BookingAnalyticsResponse analytics = analyticsService.getBookingAnalytics(institutionId, departmentId, range[0], range[1]);

        BookingReportResponse report = new BookingReportResponse();
        report.setMetadata(new ReportMetadataResponse("BOOKING", "Booking & Reservation Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId, (int) analytics.getTotalBookings()));

        BookingReportResponse.BookingSummary summary = report.getSummary();
        summary.setTotalBookings(analytics.getTotalBookings());
        summary.setCompletedBookings(analytics.getCompletedBookings());
        summary.setCancelledBookings(analytics.getCancelledBookings());
        summary.setNoShowBookings(analytics.getNoShowBookings());
        summary.setPendingBookings(analytics.getPendingBookings());
        summary.setInUseBookings(analytics.getInUseBookings());
        summary.setTotalBookedHours(analytics.getTotalBookedHours());
        summary.setAverageDurationMinutes(analytics.getAverageBookingDurationMinutes());

        report.setStatusDistribution(analytics.getStatusDistribution());

        List<BookingReportResponse.BookingEquipmentRow> equipRows = new ArrayList<>();
        if (analytics.getEquipmentFrequencies() != null) {
            for (BookingAnalyticsResponse.EquipmentBookingFrequency f : analytics.getEquipmentFrequencies()) {
                equipRows.add(new BookingReportResponse.BookingEquipmentRow(
                        f.getEquipmentId(),
                        f.getEquipmentName(),
                        f.getBookingCount(),
                        f.getBookedHours()
                ));
            }
        }
        report.setEquipmentRows(equipRows);

        List<BookingReportResponse.BookingDailyRow> dailyRows = new ArrayList<>();
        if (analytics.getDailyTrends() != null) {
            for (BookingAnalyticsResponse.DailyBookingPoint p : analytics.getDailyTrends()) {
                dailyRows.add(new BookingReportResponse.BookingDailyRow(
                        p.getDate(),
                        p.getTotalCount(),
                        p.getCompletedCount(),
                        p.getCancelledCount()
                ));
            }
        }
        report.setDailyRows(dailyRows);

        return report;
    }

    @Override
    public MaintenanceReportResponse generateMaintenanceReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        MaintenanceAnalyticsResponse analytics = analyticsService.getMaintenanceAnalytics(institutionId, departmentId, range[0], range[1]);

        MaintenanceReportResponse report = new MaintenanceReportResponse();
        int recordCount = analytics.getEquipmentDowntime() != null ? analytics.getEquipmentDowntime().size() : 0;
        report.setMetadata(new ReportMetadataResponse("MAINTENANCE", "Maintenance & Downtime Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId, recordCount));

        MaintenanceReportResponse.MaintenanceSummary summary = report.getSummary();
        summary.setTotalRequests(analytics.getTotalRequests());
        summary.setTotalWorkOrders(analytics.getTotalWorkOrders());
        summary.setTotalDowntimeHours(analytics.getTotalDowntimeHours());
        summary.setDowntimeIncidentCount(analytics.getDowntimeByCategory() != null
                ? analytics.getDowntimeByCategory().stream().mapToLong(MaintenanceAnalyticsResponse.DowntimeCategoryMetric::getIncidentCount).sum()
                : 0);

        report.setRequestStatusDistribution(analytics.getRequestStatusDistribution());
        report.setWorkOrderStatusDistribution(analytics.getWorkOrderStatusDistribution());

        List<MaintenanceReportResponse.MaintenanceCategoryRow> catRows = new ArrayList<>();
        if (analytics.getDowntimeByCategory() != null) {
            for (MaintenanceAnalyticsResponse.DowntimeCategoryMetric c : analytics.getDowntimeByCategory()) {
                catRows.add(new MaintenanceReportResponse.MaintenanceCategoryRow(
                        c.getCategory(),
                        c.getIncidentCount(),
                        c.getDurationHours(),
                        c.getPercentageOfTotal()
                ));
            }
        }
        report.setCategoryRows(catRows);

        List<MaintenanceReportResponse.MaintenanceEquipmentRow> equipRows = new ArrayList<>();
        if (analytics.getEquipmentDowntime() != null) {
            for (MaintenanceAnalyticsResponse.EquipmentDowntimeMetric e : analytics.getEquipmentDowntime()) {
                equipRows.add(new MaintenanceReportResponse.MaintenanceEquipmentRow(
                        e.getEquipmentId(),
                        e.getEquipmentName(),
                        e.getIncidentCount(),
                        e.getDurationHours()
                ));
            }
        }
        report.setEquipmentRows(equipRows);

        return report;
    }

    @Override
    public CostReportResponse generateCostReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        CostAnalyticsResponse analytics = analyticsService.getCostAnalytics(institutionId, departmentId, range[0], range[1]);

        CostReportResponse report = new CostReportResponse();
        int recordCount = analytics.getEquipmentCosts() != null ? analytics.getEquipmentCosts().size() : 0;
        report.setMetadata(new ReportMetadataResponse("COST", "Cost & Billing Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId, recordCount));

        CostReportResponse.CostSummary summary = report.getSummary();
        summary.setTotalCost(analytics.getTotalCost());
        summary.setUnbilledCost(analytics.getUnbilledCost());
        summary.setInvoicedCost(analytics.getInvoicedCost());
        summary.setSettledCost(analytics.getSettledCost());
        if (analytics.getEquipmentCosts() != null) {
            summary.setTotalBookings(analytics.getEquipmentCosts().stream().mapToLong(CostAnalyticsResponse.EquipmentCostMetric::getBookingCount).sum());
        }

        List<CostReportResponse.CostDepartmentRow> deptRows = new ArrayList<>();
        if (analytics.getDepartmentCosts() != null) {
            for (CostAnalyticsResponse.DepartmentCostMetric d : analytics.getDepartmentCosts()) {
                deptRows.add(new CostReportResponse.CostDepartmentRow(
                        d.getDepartmentId(),
                        d.getDepartmentName(),
                        d.getTotalCost(),
                        d.getUnbilledCost(),
                        d.getInvoicedCost(),
                        d.getSettledCost(),
                        d.getBookingCount()
                ));
            }
        }
        report.setDepartmentRows(deptRows);

        List<CostReportResponse.CostEquipmentRow> equipRows = new ArrayList<>();
        if (analytics.getEquipmentCosts() != null) {
            for (CostAnalyticsResponse.EquipmentCostMetric e : analytics.getEquipmentCosts()) {
                equipRows.add(new CostReportResponse.CostEquipmentRow(
                        e.getEquipmentId(),
                        e.getEquipmentName(),
                        e.getTotalCost(),
                        e.getBillableHours(),
                        e.getBookingCount()
                ));
            }
        }
        report.setEquipmentRows(equipRows);

        return report;
    }

    @Override
    public EquipmentInventoryReportResponse generateEquipmentReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        // Fetch equipment
        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);

        // Fetch domain metrics for enrichment
        UtilizationAnalyticsResponse utilData = analyticsService.getUtilizationAnalytics(institutionId, departmentId, range[0], range[1]);
        MaintenanceAnalyticsResponse maintData = analyticsService.getMaintenanceAnalytics(institutionId, departmentId, range[0], range[1]);
        CostAnalyticsResponse costData = analyticsService.getCostAnalytics(institutionId, departmentId, range[0], range[1]);
        BookingAnalyticsResponse bookingData = analyticsService.getBookingAnalytics(institutionId, departmentId, range[0], range[1]);

        Map<Long, BigDecimal> utilMap = new HashMap<>();
        if (utilData.getEquipmentMetrics() != null) {
            for (UtilizationAnalyticsResponse.EquipmentUtilizationMetric m : utilData.getEquipmentMetrics()) {
                utilMap.put(m.getEquipmentId(), m.getUtilizationPercentage());
            }
        }

        Map<Long, BigDecimal> downtimeMap = new HashMap<>();
        if (maintData.getEquipmentDowntime() != null) {
            for (MaintenanceAnalyticsResponse.EquipmentDowntimeMetric m : maintData.getEquipmentDowntime()) {
                downtimeMap.put(m.getEquipmentId(), m.getDurationHours());
            }
        }

        Map<Long, BigDecimal> costMap = new HashMap<>();
        if (costData.getEquipmentCosts() != null) {
            for (CostAnalyticsResponse.EquipmentCostMetric m : costData.getEquipmentCosts()) {
                costMap.put(m.getEquipmentId(), m.getTotalCost());
            }
        }

        Map<Long, Long> bookingMap = new HashMap<>();
        if (bookingData.getEquipmentFrequencies() != null) {
            for (BookingAnalyticsResponse.EquipmentBookingFrequency m : bookingData.getEquipmentFrequencies()) {
                bookingMap.put(m.getEquipmentId(), m.getBookingCount());
            }
        }

        EquipmentInventoryReportResponse report = new EquipmentInventoryReportResponse();
        report.setMetadata(new ReportMetadataResponse("EQUIPMENT", "Equipment Inventory & Performance Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId, equipmentList.size()));

        long operational = 0;
        long underMaint = 0;
        long decommissioned = 0;

        List<EquipmentInventoryReportResponse.EquipmentInventoryRow> rows = new ArrayList<>();
        for (Equipment eq : equipmentList) {
            if (eq.getStatus() == EquipmentStatus.AVAILABLE || eq.getStatus() == EquipmentStatus.IN_USE) {
                operational++;
            } else if (eq.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
                underMaint++;
            } else if (eq.getStatus() == EquipmentStatus.OUT_OF_SERVICE || eq.getStatus() == EquipmentStatus.RETIRED) {
                decommissioned++;
            }

            String deptName = eq.getDepartment() != null ? eq.getDepartment().getName() : "N/A";
            String catName = eq.getCategory() != null ? eq.getCategory().getName() : "N/A";

            rows.add(new EquipmentInventoryReportResponse.EquipmentInventoryRow(
                    eq.getId(),
                    eq.getName(),
                    eq.getModelNumber() != null ? eq.getModelNumber() : "",
                    eq.getSerialNumber() != null ? eq.getSerialNumber() : "",
                    deptName,
                    catName,
                    eq.getStatus().name(),
                    utilMap.getOrDefault(eq.getId(), BigDecimal.ZERO),
                    downtimeMap.getOrDefault(eq.getId(), BigDecimal.ZERO),
                    costMap.getOrDefault(eq.getId(), BigDecimal.ZERO),
                    bookingMap.getOrDefault(eq.getId(), 0L)
            ));
        }

        EquipmentInventoryReportResponse.EquipmentInventorySummary summary = report.getSummary();
        summary.setTotalEquipment(equipmentList.size());
        summary.setActiveEquipment(operational);
        summary.setOperationalCount(operational);
        summary.setUnderMaintenanceCount(underMaint);
        summary.setDecommissionedCount(decommissioned);

        report.setRows(rows);
        return report;
    }

    @Override
    public ManagementSummaryReportResponse generateManagementReport(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        validateDepartment(institutionId, departmentId);
        LocalDate[] range = resolveDateRange(startDate, endDate);

        AnalyticsOverviewResponse overview = analyticsService.getOverview(institutionId, departmentId, range[0], range[1]);
        EquipmentPerformanceResponse performance = analyticsService.getEquipmentPerformance(institutionId, departmentId, range[0], range[1]);

        ManagementSummaryReportResponse report = new ManagementSummaryReportResponse();
        report.setMetadata(new ReportMetadataResponse("MANAGEMENT", "Management Executive Summary Report",
                LocalDateTime.now(), range[0], range[1], institutionId, departmentId, (int) overview.getTotalBookings()));

        ManagementSummaryReportResponse.ManagementKpiSummary kpis = report.getKpis();
        kpis.setTotalBookings(overview.getTotalBookings());
        kpis.setCompletedBookings(overview.getCompletedBookings());
        kpis.setCancelledBookings(overview.getCancelledBookings());
        kpis.setNoShowBookings(overview.getNoShowBookings());
        kpis.setAverageUtilizationPercentage(overview.getAverageUtilizationPercentage());
        kpis.setTotalOperatingHours(overview.getTotalOperatingHours());
        kpis.setTotalUsageHours(overview.getTotalUsageHours());
        kpis.setTotalDowntimeHours(overview.getTotalDowntimeHours());
        kpis.setDowntimeIncidentCount(overview.getDowntimeIncidentCount());
        kpis.setTotalCost(overview.getTotalCost());
        kpis.setUnbilledCost(overview.getUnbilledCost());
        kpis.setInvoicedCost(overview.getInvoicedCost());
        kpis.setSettledCost(overview.getSettledCost());
        kpis.setActiveEquipmentCount(overview.getActiveEquipmentCount());
        kpis.setMaintenanceRequestCount(overview.getMaintenanceRequestCount());
        kpis.setOpenWorkOrderCount(overview.getOpenWorkOrderCount());

        report.setTopUtilizedEquipment(performance.getMostUtilized());
        report.setHighestDowntimeEquipment(performance.getHighestDowntime());
        report.setHighestCostEquipment(performance.getHighestCost());

        return report;
    }

    @Override
    public byte[] exportReportCsv(String reportType, Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        if (reportType == null || reportType.trim().isEmpty()) {
            throw new InvalidOperationException("Report type is required");
        }

        String typeLower = reportType.trim().toLowerCase();
        StringBuilder csv = new StringBuilder();

        switch (typeLower) {
            case "utilization": {
                UtilizationReportResponse rep = generateUtilizationReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("SUMMARY\n");
                csv.append("Overall Utilization (%),Total Operating Hours,Total Usage Hours,Total Sessions,Equipment Count\n");
                csv.append(String.format("%s,%s,%s,%d,%d\n\n",
                        rep.getSummary().getOverallUtilizationPercentage(),
                        rep.getSummary().getTotalOperatingHours(),
                        rep.getSummary().getTotalUsageHours(),
                        rep.getSummary().getTotalSessions(),
                        rep.getSummary().getEquipmentCount()));

                csv.append("EQUIPMENT UTILIZATION DETAILS\n");
                csv.append("Equipment ID,Equipment Name,Department,Actual Usage (Hours),Operating Hours,Utilization (%),Sessions\n");
                for (UtilizationReportResponse.UtilizationReportRow row : rep.getRows()) {
                    csv.append(String.format("%d,%s,%s,%s,%s,%s,%d\n",
                            row.getEquipmentId(),
                            escapeCsv(row.getEquipmentName()),
                            escapeCsv(row.getDepartmentName()),
                            row.getActualUsageHours(),
                            row.getOperatingHours(),
                            row.getUtilizationPercentage(),
                            row.getSessionCount()));
                }
                break;
            }
            case "bookings":
            case "booking": {
                BookingReportResponse rep = generateBookingReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("SUMMARY\n");
                csv.append("Total Bookings,Completed,Cancelled,No-Show,Pending,In Use,Total Booked Hours,Average Duration (Mins)\n");
                csv.append(String.format("%d,%d,%d,%d,%d,%d,%s,%s\n\n",
                        rep.getSummary().getTotalBookings(),
                        rep.getSummary().getCompletedBookings(),
                        rep.getSummary().getCancelledBookings(),
                        rep.getSummary().getNoShowBookings(),
                        rep.getSummary().getPendingBookings(),
                        rep.getSummary().getInUseBookings(),
                        rep.getSummary().getTotalBookedHours(),
                        rep.getSummary().getAverageDurationMinutes()));

                csv.append("EQUIPMENT BOOKING FREQUENCY\n");
                csv.append("Equipment ID,Equipment Name,Booking Count,Booked Hours\n");
                for (BookingReportResponse.BookingEquipmentRow row : rep.getEquipmentRows()) {
                    csv.append(String.format("%d,%s,%d,%s\n",
                            row.getEquipmentId(),
                            escapeCsv(row.getEquipmentName()),
                            row.getBookingCount(),
                            row.getBookedHours()));
                }
                break;
            }
            case "maintenance": {
                MaintenanceReportResponse rep = generateMaintenanceReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("SUMMARY\n");
                csv.append("Total Requests,Total Work Orders,Total Downtime (Hours),Downtime Incidents\n");
                csv.append(String.format("%d,%d,%s,%d\n\n",
                        rep.getSummary().getTotalRequests(),
                        rep.getSummary().getTotalWorkOrders(),
                        rep.getSummary().getTotalDowntimeHours(),
                        rep.getSummary().getDowntimeIncidentCount()));

                csv.append("DOWNTIME BY CATEGORY\n");
                csv.append("Category,Incident Count,Duration (Hours),Percentage of Total (%)\n");
                for (MaintenanceReportResponse.MaintenanceCategoryRow cat : rep.getCategoryRows()) {
                    csv.append(String.format("%s,%d,%s,%s\n",
                            escapeCsv(cat.getCategory()),
                            cat.getIncidentCount(),
                            cat.getDurationHours(),
                            cat.getPercentageOfTotal()));
                }
                csv.append("\nEQUIPMENT DOWNTIME DETAILS\n");
                csv.append("Equipment ID,Equipment Name,Incident Count,Downtime (Hours)\n");
                for (MaintenanceReportResponse.MaintenanceEquipmentRow row : rep.getEquipmentRows()) {
                    csv.append(String.format("%d,%s,%d,%s\n",
                            row.getEquipmentId(),
                            escapeCsv(row.getEquipmentName()),
                            row.getIncidentCount(),
                            row.getDurationHours()));
                }
                break;
            }
            case "cost": {
                CostReportResponse rep = generateCostReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("SUMMARY\n");
                csv.append("Total Cost ($),Unbilled Cost ($),Invoiced Cost ($),Settled Cost ($),Total Bookings\n");
                csv.append(String.format("%s,%s,%s,%s,%d\n\n",
                        rep.getSummary().getTotalCost(),
                        rep.getSummary().getUnbilledCost(),
                        rep.getSummary().getInvoicedCost(),
                        rep.getSummary().getSettledCost(),
                        rep.getSummary().getTotalBookings()));

                csv.append("DEPARTMENT COST BREAKDOWN\n");
                csv.append("Department ID,Department Name,Total Cost ($),Unbilled ($),Invoiced ($),Settled ($),Bookings\n");
                for (CostReportResponse.CostDepartmentRow row : rep.getDepartmentRows()) {
                    csv.append(String.format("%d,%s,%s,%s,%s,%s,%d\n",
                            row.getDepartmentId(),
                            escapeCsv(row.getDepartmentName()),
                            row.getTotalCost(),
                            row.getUnbilledCost(),
                            row.getInvoicedCost(),
                            row.getSettledCost(),
                            row.getBookingCount()));
                }
                csv.append("\nEQUIPMENT COST BREAKDOWN\n");
                csv.append("Equipment ID,Equipment Name,Total Cost ($),Billable Hours,Bookings\n");
                for (CostReportResponse.CostEquipmentRow row : rep.getEquipmentRows()) {
                    csv.append(String.format("%d,%s,%s,%s,%d\n",
                            row.getEquipmentId(),
                            escapeCsv(row.getEquipmentName()),
                            row.getTotalCost(),
                            row.getBillableHours(),
                            row.getBookingCount()));
                }
                break;
            }
            case "equipment": {
                EquipmentInventoryReportResponse rep = generateEquipmentReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("SUMMARY\n");
                csv.append("Total Equipment,Active,Operational,Under Maintenance,Decommissioned\n");
                csv.append(String.format("%d,%d,%d,%d,%d\n\n",
                        rep.getSummary().getTotalEquipment(),
                        rep.getSummary().getActiveEquipment(),
                        rep.getSummary().getOperationalCount(),
                        rep.getSummary().getUnderMaintenanceCount(),
                        rep.getSummary().getDecommissionedCount()));

                csv.append("EQUIPMENT INVENTORY DETAILS\n");
                csv.append("Equipment ID,Equipment Name,Model,Serial Number,Department,Category,Status,Utilization (%),Downtime (Hours),Cost ($),Bookings\n");
                for (EquipmentInventoryReportResponse.EquipmentInventoryRow row : rep.getRows()) {
                    csv.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d\n",
                            row.getEquipmentId(),
                            escapeCsv(row.getEquipmentName()),
                            escapeCsv(row.getModel()),
                            escapeCsv(row.getSerialNumber()),
                            escapeCsv(row.getDepartmentName()),
                            escapeCsv(row.getCategoryName()),
                            escapeCsv(row.getStatus()),
                            row.getUtilizationPercentage(),
                            row.getDowntimeHours(),
                            row.getTotalCost(),
                            row.getBookingCount()));
                }
                break;
            }
            case "management": {
                ManagementSummaryReportResponse rep = generateManagementReport(institutionId, departmentId, startDate, endDate);
                appendMetadataHeader(csv, rep.getMetadata());
                csv.append("EXECUTIVE KPIS\n");
                csv.append("Metric,Value\n");
                ManagementSummaryReportResponse.ManagementKpiSummary kpi = rep.getKpis();
                csv.append(String.format("Total Bookings,%d\n", kpi.getTotalBookings()));
                csv.append(String.format("Completed Bookings,%d\n", kpi.getCompletedBookings()));
                csv.append(String.format("Average Utilization (%%),%s\n", kpi.getAverageUtilizationPercentage()));
                csv.append(String.format("Total Usage Hours,%s\n", kpi.getTotalUsageHours()));
                csv.append(String.format("Total Operating Hours,%s\n", kpi.getTotalOperatingHours()));
                csv.append(String.format("Total Downtime Hours,%s\n", kpi.getTotalDowntimeHours()));
                csv.append(String.format("Downtime Incidents,%d\n", kpi.getDowntimeIncidentCount()));
                csv.append(String.format("Total Expenditure ($),%s\n", kpi.getTotalCost()));
                csv.append(String.format("Unbilled Expenditure ($),%s\n", kpi.getUnbilledCost()));
                csv.append(String.format("Settled Expenditure ($),%s\n", kpi.getSettledCost()));
                csv.append(String.format("Active Equipment Count,%d\n", kpi.getActiveEquipmentCount()));
                csv.append(String.format("Open Maintenance Work Orders,%d\n\n", kpi.getOpenWorkOrderCount()));

                csv.append("TOP UTILIZED EQUIPMENT\n");
                csv.append("Equipment ID,Equipment Name,Department,Utilization (%)\n");
                for (EquipmentPerformanceResponse.PerformanceItem item : rep.getTopUtilizedEquipment()) {
                    csv.append(String.format("%d,%s,%s,%s\n",
                            item.getEquipmentId(),
                            escapeCsv(item.getEquipmentName()),
                            escapeCsv(item.getDepartmentName()),
                            item.getMetricValue()));
                }

                csv.append("\nHIGHEST DOWNTIME EQUIPMENT\n");
                csv.append("Equipment ID,Equipment Name,Department,Downtime (Hours)\n");
                for (EquipmentPerformanceResponse.PerformanceItem item : rep.getHighestDowntimeEquipment()) {
                    csv.append(String.format("%d,%s,%s,%s\n",
                            item.getEquipmentId(),
                            escapeCsv(item.getEquipmentName()),
                            escapeCsv(item.getDepartmentName()),
                            item.getMetricValue()));
                }

                csv.append("\nHIGHEST COST EQUIPMENT\n");
                csv.append("Equipment ID,Equipment Name,Department,Total Cost ($)\n");
                for (EquipmentPerformanceResponse.PerformanceItem item : rep.getHighestCostEquipment()) {
                    csv.append(String.format("%d,%s,%s,%s\n",
                            item.getEquipmentId(),
                            escapeCsv(item.getEquipmentName()),
                            escapeCsv(item.getDepartmentName()),
                            item.getMetricValue()));
                }
                break;
            }
            default:
                throw new InvalidOperationException("Unsupported report type: " + reportType);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private void validateTenant(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Authenticated institution context is required");
        }
    }

    private void validateDepartment(Long institutionId, Long departmentId) {
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            if (!dept.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException("Department does not belong to authenticated user's institution");
            }
        }
    }

    private LocalDate[] resolveDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(30);

        if (start.isAfter(end)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
        return new LocalDate[]{start, end};
    }

    private List<Equipment> getFilteredEquipment(Long institutionId, Long departmentId) {
        List<Equipment> list;
        if (departmentId != null) {
            list = equipmentRepository.findByDepartmentId(departmentId);
        } else {
            list = equipmentRepository.findByInstitutionId(institutionId);
        }
        return list.stream()
                .filter(e -> e.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    private void appendMetadataHeader(StringBuilder csv, ReportMetadataResponse meta) {
        csv.append("# Report Title: ").append(meta.getTitle()).append("\n");
        csv.append("# Report Type: ").append(meta.getReportType()).append("\n");
        csv.append("# Generated At: ").append(meta.getGeneratedAt()).append("\n");
        csv.append("# Period: ").append(meta.getStartDate()).append(" to ").append(meta.getEndDate()).append("\n");
        csv.append("# Institution ID: ").append(meta.getInstitutionId()).append("\n");
        if (meta.getDepartmentId() != null) {
            csv.append("# Department ID: ").append(meta.getDepartmentId()).append("\n");
        }
        csv.append("# Total Records: ").append(meta.getRecordCount()).append("\n\n");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
