package com.labresource.platform.analytics.service;

import com.labresource.platform.analytics.web.*;
import com.labresource.platform.booking.Booking;
import com.labresource.platform.booking.BookingBillingStatus;
import com.labresource.platform.booking.BookingStatus;
import com.labresource.platform.booking.repository.BookingRepository;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.cost.service.CostService;
import com.labresource.platform.cost.web.UsageCostResponse;
import com.labresource.platform.department.Department;
import com.labresource.platform.department.repository.DepartmentRepository;
import com.labresource.platform.equipment.Equipment;
import com.labresource.platform.equipment.EquipmentStatus;
import com.labresource.platform.equipment.repository.EquipmentRepository;
import com.labresource.platform.maintenance.DowntimeReasonCategory;
import com.labresource.platform.maintenance.EquipmentDowntimeLog;
import com.labresource.platform.maintenance.MaintenanceRequest;
import com.labresource.platform.maintenance.MaintenanceWorkOrder;
import com.labresource.platform.maintenance.WorkOrderStatus;
import com.labresource.platform.maintenance.repository.EquipmentDowntimeLogRepository;
import com.labresource.platform.maintenance.repository.MaintenanceRequestRepository;
import com.labresource.platform.maintenance.repository.MaintenanceWorkOrderRepository;
import com.labresource.platform.utilization.EquipmentUsageSession;
import com.labresource.platform.utilization.SessionStatus;
import com.labresource.platform.utilization.repository.EquipmentUsageSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;
    private final EquipmentUsageSessionRepository sessionRepository;
    private final EquipmentDowntimeLogRepository downtimeLogRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final DepartmentRepository departmentRepository;
    private final CostService costService;

    public AnalyticsServiceImpl(EquipmentRepository equipmentRepository,
                                BookingRepository bookingRepository,
                                EquipmentUsageSessionRepository sessionRepository,
                                EquipmentDowntimeLogRepository downtimeLogRepository,
                                MaintenanceRequestRepository maintenanceRequestRepository,
                                MaintenanceWorkOrderRepository workOrderRepository,
                                DepartmentRepository departmentRepository,
                                CostService costService) {
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.downtimeLogRepository = downtimeLogRepository;
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.departmentRepository = departmentRepository;
        this.costService = costService;
    }

    // ==========================================
    // 1. Overview
    // ==========================================

    @Override
    public AnalyticsOverviewResponse getOverview(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);
        List<Booking> bookings = getFilteredBookings(institutionId, departmentId, startInstant, endInstant);

        AnalyticsOverviewResponse res = new AnalyticsOverviewResponse();
        res.setStartDate(start);
        res.setEndDate(end);
        res.setActiveEquipmentCount(equipmentList.stream().filter(e -> e.getStatus() == EquipmentStatus.AVAILABLE || e.getStatus() == EquipmentStatus.IN_USE).count());

        // Bookings KPIs
        res.setTotalBookings(bookings.size());
        res.setCompletedBookings(bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count());
        res.setCancelledBookings(bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count());
        res.setNoShowBookings(bookings.stream().filter(b -> b.getStatus() == BookingStatus.NO_SHOW).count());

        // Utilization KPIs
        long operatingMinutesPerEquip = calculateOperatingMinutes(start, end);
        long totalOperatingMinutes = operatingMinutesPerEquip * equipmentList.size();
        res.setTotalOperatingHours(toHours(totalOperatingMinutes));

        long totalUsageMinutes = 0;
        for (Equipment eq : equipmentList) {
            List<EquipmentUsageSession> sessions = getFilteredSessions(eq.getId(), startInstant, endInstant);
            totalUsageMinutes += sessions.stream()
                    .mapToLong(s -> s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0)
                    .sum();
        }
        res.setTotalUsageHours(toHours(totalUsageMinutes));

        if (totalOperatingMinutes > 0) {
            BigDecimal util = BigDecimal.valueOf(Math.min(totalUsageMinutes, totalOperatingMinutes))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOperatingMinutes), 2, RoundingMode.HALF_UP);
            res.setAverageUtilizationPercentage(util);
        }

        // Downtime KPIs
        long totalDowntimeMinutes = 0;
        long downtimeIncidents = 0;
        for (Equipment eq : equipmentList) {
            List<EquipmentDowntimeLog> logs = getFilteredDowntimeLogs(eq.getId(), startInstant, endInstant);
            downtimeIncidents += logs.size();
            totalDowntimeMinutes += logs.stream()
                    .mapToLong(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : 0)
                    .sum();
        }
        res.setTotalDowntimeHours(toHours(totalDowntimeMinutes));
        res.setDowntimeIncidentCount(downtimeIncidents);

        // Maintenance KPIs
        long maintenanceReqCount = 0;
        long openWorkOrders = 0;
        for (Equipment eq : equipmentList) {
            List<MaintenanceRequest> reqs = maintenanceRequestRepository.findByEquipmentId(eq.getId()).stream()
                    .filter(r -> !r.getCreatedAt().isBefore(startInstant) && !r.getCreatedAt().isAfter(endInstant))
                    .toList();
            maintenanceReqCount += reqs.size();

            List<MaintenanceWorkOrder> wos = workOrderRepository.findByEquipmentId(eq.getId()).stream()
                    .filter(w -> w.getStatus() == WorkOrderStatus.SCHEDULED || w.getStatus() == WorkOrderStatus.IN_PROGRESS || w.getStatus() == WorkOrderStatus.WAITING_FOR_PARTS)
                    .toList();
            openWorkOrders += wos.size();
        }
        res.setMaintenanceRequestCount(maintenanceReqCount);
        res.setOpenWorkOrderCount(openWorkOrders);

        // Cost KPIs
        List<UsageCostResponse> usageCosts = getFilteredUsageCosts(institutionId, departmentId, startInstant, endInstant);
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal unbilled = BigDecimal.ZERO;
        BigDecimal invoiced = BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;

        for (UsageCostResponse uc : usageCosts) {
            BigDecimal amt = uc.getTotalCost() != null ? uc.getTotalCost() : BigDecimal.ZERO;
            totalCost = totalCost.add(amt);
            if (uc.getBillingStatus() == BookingBillingStatus.UNBILLED) {
                unbilled = unbilled.add(amt);
            } else if (uc.getBillingStatus() == BookingBillingStatus.INVOICED) {
                invoiced = invoiced.add(amt);
            } else if (uc.getBillingStatus() == BookingBillingStatus.SETTLED) {
                settled = settled.add(amt);
            }
        }
        res.setTotalCost(totalCost);
        res.setUnbilledCost(unbilled);
        res.setInvoicedCost(invoiced);
        res.setSettledCost(settled);

        return res;
    }

    // ==========================================
    // 2. Utilization Analytics
    // ==========================================

    @Override
    public UtilizationAnalyticsResponse getUtilizationAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);
        long operatingMinutesPerEquip = calculateOperatingMinutes(start, end);
        long totalOperatingMinutes = operatingMinutesPerEquip * equipmentList.size();

        UtilizationAnalyticsResponse response = new UtilizationAnalyticsResponse();
        response.setStartDate(start);
        response.setEndDate(end);
        response.setTotalOperatingHours(toHours(totalOperatingMinutes));

        long totalUsageMinutes = 0;
        long totalSessions = 0;
        List<UtilizationAnalyticsResponse.EquipmentUtilizationMetric> equipMetrics = new ArrayList<>();

        Map<LocalDate, Long> dailyUsageMap = new HashMap<>();
        Map<LocalDate, Long> dailySessionCountMap = new HashMap<>();

        for (Equipment eq : equipmentList) {
            List<EquipmentUsageSession> sessions = getFilteredSessions(eq.getId(), startInstant, endInstant);
            long equipUsageMinutes = sessions.stream()
                    .mapToLong(s -> s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0)
                    .sum();
            totalUsageMinutes += equipUsageMinutes;
            totalSessions += sessions.size();

            BigDecimal utilPct = BigDecimal.ZERO;
            if (operatingMinutesPerEquip > 0) {
                utilPct = BigDecimal.valueOf(Math.min(equipUsageMinutes, operatingMinutesPerEquip))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(operatingMinutesPerEquip), 2, RoundingMode.HALF_UP);
            }

            UtilizationAnalyticsResponse.EquipmentUtilizationMetric em = new UtilizationAnalyticsResponse.EquipmentUtilizationMetric();
            em.setEquipmentId(eq.getId());
            em.setEquipmentName(eq.getName());
            if (eq.getDepartment() != null) {
                em.setDepartmentId(eq.getDepartment().getId());
                em.setDepartmentName(eq.getDepartment().getName());
            }
            em.setActualUsageMinutes(equipUsageMinutes);
            em.setActualUsageHours(toHours(equipUsageMinutes));
            em.setOperatingMinutes(operatingMinutesPerEquip);
            em.setOperatingHours(toHours(operatingMinutesPerEquip));
            em.setUtilizationPercentage(utilPct);
            em.setSessionCount(sessions.size());
            equipMetrics.add(em);

            // Group by date for daily trends
            for (EquipmentUsageSession s : sessions) {
                LocalDate sessionDate = s.getCheckedInAt().atZone(ZoneOffset.UTC).toLocalDate();
                if (!sessionDate.isBefore(start) && !sessionDate.isAfter(end)) {
                    long dMins = s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0;
                    dailyUsageMap.merge(sessionDate, dMins, Long::sum);
                    dailySessionCountMap.merge(sessionDate, 1L, Long::sum);
                }
            }
        }

        // Sort equipment metrics by utilization desc
        equipMetrics.sort((a, b) -> b.getUtilizationPercentage().compareTo(a.getUtilizationPercentage()));
        response.setEquipmentMetrics(equipMetrics);
        response.setTotalUsageHours(toHours(totalUsageMinutes));
        response.setTotalSessions(totalSessions);

        if (totalOperatingMinutes > 0) {
            BigDecimal overallUtil = BigDecimal.valueOf(Math.min(totalUsageMinutes, totalOperatingMinutes))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOperatingMinutes), 2, RoundingMode.HALF_UP);
            response.setOverallUtilizationPercentage(overallUtil);
        }

        // Generate daily trends
        List<UtilizationAnalyticsResponse.DailyUtilizationPoint> dailyTrends = new ArrayList<>();
        long dailyOperatingMinutesAllEquip = equipmentList.size() * 720L;

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean isSunday = d.getDayOfWeek() == DayOfWeek.SUNDAY;
            long dayOperating = isSunday ? 0 : dailyOperatingMinutesAllEquip;
            long dayUsage = dailyUsageMap.getOrDefault(d, 0L);
            long daySessions = dailySessionCountMap.getOrDefault(d, 0L);

            BigDecimal dayUtil = BigDecimal.ZERO;
            if (dayOperating > 0) {
                dayUtil = BigDecimal.valueOf(Math.min(dayUsage, dayOperating))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(dayOperating), 2, RoundingMode.HALF_UP);
            }
            dailyTrends.add(new UtilizationAnalyticsResponse.DailyUtilizationPoint(d, dayUsage, dayOperating, dayUtil, daySessions));
        }
        response.setDailyTrends(dailyTrends);

        return response;
    }

    // ==========================================
    // 3. Equipment Utilization (Single)
    // ==========================================

    @Override
    public EquipmentUtilizationDetailResponse getEquipmentUtilization(Long equipmentId, LocalDate startDate, LocalDate endDate, Long institutionId) {
        validateTenant(institutionId);
        if (equipmentId == null) {
            throw new InvalidOperationException("Equipment ID cannot be null");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        if (!equipment.getInstitution().getId().equals(institutionId)) {
            throw new InvalidOperationException("Equipment does not belong to authenticated user's institution");
        }

        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        long operatingMinutes = calculateOperatingMinutes(start, end);
        List<EquipmentUsageSession> sessions = getFilteredSessions(equipmentId, startInstant, endInstant);

        long usageMinutes = sessions.stream()
                .mapToLong(s -> s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0)
                .sum();

        BigDecimal util = BigDecimal.ZERO;
        if (operatingMinutes > 0) {
            util = BigDecimal.valueOf(Math.min(usageMinutes, operatingMinutes))
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(operatingMinutes), 2, RoundingMode.HALF_UP);
        }

        EquipmentUtilizationDetailResponse res = new EquipmentUtilizationDetailResponse();
        res.setEquipmentId(equipment.getId());
        res.setEquipmentName(equipment.getName());
        res.setModelNumber(equipment.getModelNumber());
        res.setSerialNumber(equipment.getSerialNumber());
        if (equipment.getDepartment() != null) {
            res.setDepartmentId(equipment.getDepartment().getId());
            res.setDepartmentName(equipment.getDepartment().getName());
        }
        res.setActualUsageMinutes(usageMinutes);
        res.setActualUsageHours(toHours(usageMinutes));
        res.setOperatingMinutes(operatingMinutes);
        res.setOperatingHours(toHours(operatingMinutes));
        res.setUtilizationPercentage(util);
        res.setSessionCount(sessions.size());
        res.setStartDate(start);
        res.setEndDate(end);

        // Daily trends for this single equipment
        Map<LocalDate, Long> dayUsageMap = new HashMap<>();
        Map<LocalDate, Long> daySessionMap = new HashMap<>();
        for (EquipmentUsageSession s : sessions) {
            LocalDate d = s.getCheckedInAt().atZone(ZoneOffset.UTC).toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                dayUsageMap.merge(d, s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0L, Long::sum);
                daySessionMap.merge(d, 1L, Long::sum);
            }
        }

        List<UtilizationAnalyticsResponse.DailyUtilizationPoint> dailyTrends = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean isSunday = d.getDayOfWeek() == DayOfWeek.SUNDAY;
            long dayOp = isSunday ? 0 : 720L;
            long dayUs = dayUsageMap.getOrDefault(d, 0L);
            long daySc = daySessionMap.getOrDefault(d, 0L);
            BigDecimal dayUt = BigDecimal.ZERO;
            if (dayOp > 0) {
                dayUt = BigDecimal.valueOf(Math.min(dayUs, dayOp))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(dayOp), 2, RoundingMode.HALF_UP);
            }
            dailyTrends.add(new UtilizationAnalyticsResponse.DailyUtilizationPoint(d, dayUs, dayOp, dayUt, daySc));
        }
        res.setDailyTrends(dailyTrends);

        return res;
    }

    // ==========================================
    // 4. Booking Analytics
    // ==========================================

    @Override
    public BookingAnalyticsResponse getBookingAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Booking> bookings = getFilteredBookings(institutionId, departmentId, startInstant, endInstant);

        BookingAnalyticsResponse res = new BookingAnalyticsResponse();
        res.setStartDate(start);
        res.setEndDate(end);
        res.setTotalBookings(bookings.size());

        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            statusDist.put(status.name(), 0L);
        }

        long totalBookedMinutes = 0;
        Map<LocalDate, long[]> dailyBookingMap = new HashMap<>(); // [total, completed, cancelled]
        Map<Long, BookingAnalyticsResponse.EquipmentBookingFrequency> equipFreqMap = new HashMap<>();

        for (Booking b : bookings) {
            String sName = b.getStatus().name();
            statusDist.put(sName, statusDist.getOrDefault(sName, 0L) + 1);

            long durationMins = Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();
            totalBookedMinutes += Math.max(0, durationMins);

            // Daily tracking
            LocalDate bDate = b.getStartTime().atZone(ZoneOffset.UTC).toLocalDate();
            if (!bDate.isBefore(start) && !bDate.isAfter(end)) {
                long[] counts = dailyBookingMap.computeIfAbsent(bDate, k -> new long[3]);
                counts[0]++; // total
                if (b.getStatus() == BookingStatus.COMPLETED) {
                    counts[1]++;
                } else if (b.getStatus() == BookingStatus.CANCELLED) {
                    counts[2]++;
                }
            }

            // Equipment frequency
            if (b.getEquipment() != null) {
                Long eqId = b.getEquipment().getId();
                String eqName = b.getEquipment().getName();
                BookingAnalyticsResponse.EquipmentBookingFrequency freq = equipFreqMap.computeIfAbsent(
                        eqId, id -> new BookingAnalyticsResponse.EquipmentBookingFrequency(id, eqName, 0, BigDecimal.ZERO));
                freq.setBookingCount(freq.getBookingCount() + 1);
                freq.setBookedHours(freq.getBookedHours().add(toHours(durationMins)));
            }
        }

        res.setCompletedBookings(statusDist.getOrDefault(BookingStatus.COMPLETED.name(), 0L));
        res.setCancelledBookings(statusDist.getOrDefault(BookingStatus.CANCELLED.name(), 0L));
        res.setNoShowBookings(statusDist.getOrDefault(BookingStatus.NO_SHOW.name(), 0L));
        res.setPendingBookings(statusDist.getOrDefault(BookingStatus.PENDING_APPROVAL.name(), 0L));
        res.setInUseBookings(statusDist.getOrDefault(BookingStatus.IN_USE.name(), 0L));
        res.setStatusDistribution(statusDist);
        res.setTotalBookedHours(toHours(totalBookedMinutes));

        if (!bookings.isEmpty()) {
            res.setAverageBookingDurationMinutes(BigDecimal.valueOf(totalBookedMinutes)
                    .divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP));
        }

        // Daily trends
        List<BookingAnalyticsResponse.DailyBookingPoint> dailyTrends = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long[] counts = dailyBookingMap.getOrDefault(d, new long[3]);
            dailyTrends.add(new BookingAnalyticsResponse.DailyBookingPoint(d, counts[0], counts[1], counts[2]));
        }
        res.setDailyTrends(dailyTrends);

        // Equipment frequencies sorted by booking count desc
        List<BookingAnalyticsResponse.EquipmentBookingFrequency> freqList = new ArrayList<>(equipFreqMap.values());
        freqList.sort((a, b) -> Long.compare(b.getBookingCount(), a.getBookingCount()));
        res.setEquipmentFrequencies(freqList);

        return res;
    }

    // ==========================================
    // 5. Maintenance Analytics
    // ==========================================

    @Override
    public MaintenanceAnalyticsResponse getMaintenanceAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);

        MaintenanceAnalyticsResponse res = new MaintenanceAnalyticsResponse();
        res.setStartDate(start);
        res.setEndDate(end);

        Map<String, Long> reqStatusDist = new LinkedHashMap<>();
        Map<String, Long> reqPriorityDist = new LinkedHashMap<>();
        Map<String, Long> woStatusDist = new LinkedHashMap<>();
        Map<DowntimeReasonCategory, long[]> categoryMap = new EnumMap<>(DowntimeReasonCategory.class); // [durationMins, incidents]
        Map<Long, MaintenanceAnalyticsResponse.EquipmentDowntimeMetric> equipDowntimeMap = new HashMap<>();

        long totalRequests = 0;
        long totalWorkOrders = 0;
        long totalDowntimeMinutes = 0;

        for (Equipment eq : equipmentList) {
            // Maintenance Requests
            List<MaintenanceRequest> reqs = maintenanceRequestRepository.findByEquipmentId(eq.getId()).stream()
                    .filter(r -> !r.getCreatedAt().isBefore(startInstant) && !r.getCreatedAt().isAfter(endInstant))
                    .toList();
            totalRequests += reqs.size();
            for (MaintenanceRequest r : reqs) {
                reqStatusDist.merge(r.getStatus().name(), 1L, Long::sum);
                reqPriorityDist.merge(r.getPriority().name(), 1L, Long::sum);
            }

            // Work Orders
            List<MaintenanceWorkOrder> wos = workOrderRepository.findByEquipmentId(eq.getId()).stream()
                    .filter(w -> !w.getCreatedAt().isBefore(startInstant) && !w.getCreatedAt().isAfter(endInstant))
                    .toList();
            totalWorkOrders += wos.size();
            for (MaintenanceWorkOrder w : wos) {
                woStatusDist.merge(w.getStatus().name(), 1L, Long::sum);
            }

            // Downtime Logs
            List<EquipmentDowntimeLog> logs = getFilteredDowntimeLogs(eq.getId(), startInstant, endInstant);
            for (EquipmentDowntimeLog log : logs) {
                long dMins = log.getDurationMinutes() != null ? log.getDurationMinutes() : 0;
                totalDowntimeMinutes += dMins;

                long[] catCounts = categoryMap.computeIfAbsent(log.getReasonCategory(), k -> new long[2]);
                catCounts[0] += dMins;
                catCounts[1]++;

                MaintenanceAnalyticsResponse.EquipmentDowntimeMetric eqMetric = equipDowntimeMap.computeIfAbsent(
                        eq.getId(), id -> new MaintenanceAnalyticsResponse.EquipmentDowntimeMetric(id, eq.getName(), 0, 0));
                eqMetric.setDurationMinutes(eqMetric.getDurationMinutes() + dMins);
                eqMetric.setDurationHours(toHours(eqMetric.getDurationMinutes()));
                eqMetric.setIncidentCount(eqMetric.getIncidentCount() + 1);
            }
        }

        res.setTotalRequests(totalRequests);
        res.setTotalWorkOrders(totalWorkOrders);
        res.setTotalDowntimeMinutes(totalDowntimeMinutes);
        res.setTotalDowntimeHours(toHours(totalDowntimeMinutes));
        res.setRequestStatusDistribution(reqStatusDist);
        res.setRequestPriorityDistribution(reqPriorityDist);
        res.setWorkOrderStatusDistribution(woStatusDist);

        // Downtime by Category
        List<MaintenanceAnalyticsResponse.DowntimeCategoryMetric> catMetrics = new ArrayList<>();
        for (Map.Entry<DowntimeReasonCategory, long[]> entry : categoryMap.entrySet()) {
            long mins = entry.getValue()[0];
            long incidents = entry.getValue()[1];
            BigDecimal pct = BigDecimal.ZERO;
            if (totalDowntimeMinutes > 0) {
                pct = BigDecimal.valueOf(mins)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalDowntimeMinutes), 2, RoundingMode.HALF_UP);
            }
            catMetrics.add(new MaintenanceAnalyticsResponse.DowntimeCategoryMetric(entry.getKey().name(), mins, incidents, pct));
        }
        catMetrics.sort((a, b) -> Long.compare(b.getDurationMinutes(), a.getDurationMinutes()));
        res.setDowntimeByCategory(catMetrics);

        // Downtime by Equipment
        List<MaintenanceAnalyticsResponse.EquipmentDowntimeMetric> equipDowntimes = new ArrayList<>(equipDowntimeMap.values());
        equipDowntimes.sort((a, b) -> Long.compare(b.getDurationMinutes(), a.getDurationMinutes()));
        res.setEquipmentDowntime(equipDowntimes);

        return res;
    }

    // ==========================================
    // 6. Cost Analytics
    // ==========================================

    @Override
    public CostAnalyticsResponse getCostAnalytics(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<UsageCostResponse> usageCosts = getFilteredUsageCosts(institutionId, departmentId, startInstant, endInstant);

        CostAnalyticsResponse res = new CostAnalyticsResponse();
        res.setStartDate(start);
        res.setEndDate(end);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal unbilled = BigDecimal.ZERO;
        BigDecimal invoiced = BigDecimal.ZERO;
        BigDecimal settled = BigDecimal.ZERO;

        Map<Long, CostAnalyticsResponse.DepartmentCostMetric> deptCostMap = new HashMap<>();
        Map<Long, CostAnalyticsResponse.EquipmentCostMetric> equipCostMap = new HashMap<>();
        Map<LocalDate, BigDecimal> dailyCostMap = new HashMap<>();
        Map<LocalDate, Long> dailyBookingCountMap = new HashMap<>();

        for (UsageCostResponse uc : usageCosts) {
            BigDecimal amt = uc.getTotalCost() != null ? uc.getTotalCost() : BigDecimal.ZERO;
            total = total.add(amt);

            if (uc.getBillingStatus() == BookingBillingStatus.UNBILLED) {
                unbilled = unbilled.add(amt);
            } else if (uc.getBillingStatus() == BookingBillingStatus.INVOICED) {
                invoiced = invoiced.add(amt);
            } else if (uc.getBillingStatus() == BookingBillingStatus.SETTLED) {
                settled = settled.add(amt);
            }

            // Department aggregations
            if (uc.getDepartmentId() != null) {
                CostAnalyticsResponse.DepartmentCostMetric dm = deptCostMap.computeIfAbsent(
                        uc.getDepartmentId(), id -> new CostAnalyticsResponse.DepartmentCostMetric(
                                id, uc.getDepartmentName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0));
                dm.setTotalCost(dm.getTotalCost().add(amt));
                dm.setBookingCount(dm.getBookingCount() + 1);
                if (uc.getBillingStatus() == BookingBillingStatus.UNBILLED) {
                    dm.setUnbilledCost(dm.getUnbilledCost().add(amt));
                } else if (uc.getBillingStatus() == BookingBillingStatus.INVOICED) {
                    dm.setInvoicedCost(dm.getInvoicedCost().add(amt));
                } else if (uc.getBillingStatus() == BookingBillingStatus.SETTLED) {
                    dm.setSettledCost(dm.getSettledCost().add(amt));
                }
            }

            // Equipment aggregations
            if (uc.getEquipmentId() != null) {
                CostAnalyticsResponse.EquipmentCostMetric em = equipCostMap.computeIfAbsent(
                        uc.getEquipmentId(), id -> new CostAnalyticsResponse.EquipmentCostMetric(
                                id, uc.getEquipmentName(), BigDecimal.ZERO, BigDecimal.ZERO, 0));
                em.setTotalCost(em.getTotalCost().add(amt));
                em.setBillableHours(em.getBillableHours().add(uc.getBillableHours() != null ? uc.getBillableHours() : BigDecimal.ZERO));
                em.setBookingCount(em.getBookingCount() + 1);
            }

            // Daily cost point
            if (uc.getStartTime() != null) {
                LocalDate d = uc.getStartTime().atZone(ZoneOffset.UTC).toLocalDate();
                if (!d.isBefore(start) && !d.isAfter(end)) {
                    dailyCostMap.merge(d, amt, BigDecimal::add);
                    dailyBookingCountMap.merge(d, 1L, Long::sum);
                }
            }
        }

        res.setTotalCost(total);
        res.setUnbilledCost(unbilled);
        res.setInvoicedCost(invoiced);
        res.setSettledCost(settled);

        List<CostAnalyticsResponse.DepartmentCostMetric> deptList = new ArrayList<>(deptCostMap.values());
        deptList.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        res.setDepartmentCosts(deptList);

        List<CostAnalyticsResponse.EquipmentCostMetric> equipList = new ArrayList<>(equipCostMap.values());
        equipList.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        res.setEquipmentCosts(equipList);

        List<CostAnalyticsResponse.DailyCostPoint> dailyList = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            BigDecimal c = dailyCostMap.getOrDefault(d, BigDecimal.ZERO);
            long count = dailyBookingCountMap.getOrDefault(d, 0L);
            dailyList.add(new CostAnalyticsResponse.DailyCostPoint(d, c, count));
        }
        res.setDailyCosts(dailyList);

        return res;
    }

    // ==========================================
    // 7. Equipment Performance Leaderboard
    // ==========================================

    @Override
    public EquipmentPerformanceResponse getEquipmentPerformance(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);
        long operatingMinutesPerEquip = calculateOperatingMinutes(start, end);

        List<EquipmentPerformanceResponse.PerformanceItem> allItems = new ArrayList<>();

        for (Equipment eq : equipmentList) {
            EquipmentPerformanceResponse.PerformanceItem item = new EquipmentPerformanceResponse.PerformanceItem();
            item.setEquipmentId(eq.getId());
            item.setEquipmentName(eq.getName());
            if (eq.getDepartment() != null) {
                item.setDepartmentName(eq.getDepartment().getName());
            }

            // Usage & Utilization
            List<EquipmentUsageSession> sessions = getFilteredSessions(eq.getId(), startInstant, endInstant);
            long usageMins = sessions.stream()
                    .mapToLong(s -> s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0)
                    .sum();
            item.setTotalUsageHours(toHours(usageMins));

            BigDecimal util = BigDecimal.ZERO;
            if (operatingMinutesPerEquip > 0) {
                util = BigDecimal.valueOf(Math.min(usageMins, operatingMinutesPerEquip))
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(operatingMinutesPerEquip), 2, RoundingMode.HALF_UP);
            }
            item.setUtilizationPercentage(util);

            // Downtime
            List<EquipmentDowntimeLog> logs = getFilteredDowntimeLogs(eq.getId(), startInstant, endInstant);
            long dtMins = logs.stream()
                    .mapToLong(l -> l.getDurationMinutes() != null ? l.getDurationMinutes() : 0)
                    .sum();
            item.setTotalDowntimeHours(toHours(dtMins));

            // Bookings
            List<Booking> bookings = bookingRepository.findByEquipmentId(eq.getId()).stream()
                    .filter(b -> b.getStartTime().isBefore(endInstant) && b.getEndTime().isAfter(startInstant))
                    .toList();
            item.setBookingCount(bookings.size());

            // Cost
            List<UsageCostResponse> costs = costService.listUsageCosts(institutionId, null, eq.getId(), null).stream()
                    .filter(c -> c.getStartTime() != null && c.getStartTime().isBefore(endInstant) && (c.getEndTime() == null || c.getEndTime().isAfter(startInstant)))
                    .toList();
            BigDecimal totalEqCost = costs.stream()
                    .map(c -> c.getTotalCost() != null ? c.getTotalCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setTotalCost(totalEqCost);

            allItems.add(item);
        }

        EquipmentPerformanceResponse response = new EquipmentPerformanceResponse();
        response.setStartDate(start);
        response.setEndDate(end);

        // Most Utilized (desc)
        List<EquipmentPerformanceResponse.PerformanceItem> mostUtil = new ArrayList<>(allItems);
        mostUtil.sort((a, b) -> b.getUtilizationPercentage().compareTo(a.getUtilizationPercentage()));
        for (EquipmentPerformanceResponse.PerformanceItem it : mostUtil) {
            it.setMetricValue(it.getUtilizationPercentage());
            it.setMetricUnit("%");
        }
        response.setMostUtilized(mostUtil.stream().limit(10).toList());

        // Least Utilized (asc)
        List<EquipmentPerformanceResponse.PerformanceItem> leastUtil = new ArrayList<>(allItems);
        leastUtil.sort((a, b) -> a.getUtilizationPercentage().compareTo(b.getUtilizationPercentage()));
        for (EquipmentPerformanceResponse.PerformanceItem it : leastUtil) {
            it.setMetricValue(it.getUtilizationPercentage());
            it.setMetricUnit("%");
        }
        response.setLeastUtilized(leastUtil.stream().limit(10).toList());

        // Highest Downtime (desc)
        List<EquipmentPerformanceResponse.PerformanceItem> highDt = new ArrayList<>(allItems);
        highDt.sort((a, b) -> b.getTotalDowntimeHours().compareTo(a.getTotalDowntimeHours()));
        for (EquipmentPerformanceResponse.PerformanceItem it : highDt) {
            it.setMetricValue(it.getTotalDowntimeHours());
            it.setMetricUnit("hours");
        }
        response.setHighestDowntime(highDt.stream().limit(10).toList());

        // Highest Cost (desc)
        List<EquipmentPerformanceResponse.PerformanceItem> highCost = new ArrayList<>(allItems);
        highCost.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        for (EquipmentPerformanceResponse.PerformanceItem it : highCost) {
            it.setMetricValue(it.getTotalCost());
            it.setMetricUnit("$");
        }
        response.setHighestCost(highCost.stream().limit(10).toList());

        // Highest Booking Frequency (desc)
        List<EquipmentPerformanceResponse.PerformanceItem> highBk = new ArrayList<>(allItems);
        highBk.sort((a, b) -> Long.compare(b.getBookingCount(), a.getBookingCount()));
        for (EquipmentPerformanceResponse.PerformanceItem it : highBk) {
            it.setMetricValue(BigDecimal.valueOf(it.getBookingCount()));
            it.setMetricUnit("bookings");
        }
        response.setHighestBookingFrequency(highBk.stream().limit(10).toList());

        return response;
    }

    // ==========================================
    // 8. Unified Daily Trends
    // ==========================================

    @Override
    public TrendAnalyticsResponse getTrends(Long institutionId, Long departmentId, LocalDate startDate, LocalDate endDate) {
        validateTenant(institutionId);
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Equipment> equipmentList = getFilteredEquipment(institutionId, departmentId);
        List<Booking> bookings = getFilteredBookings(institutionId, departmentId, startInstant, endInstant);
        List<UsageCostResponse> usageCosts = getFilteredUsageCosts(institutionId, departmentId, startInstant, endInstant);

        Map<LocalDate, Long> dayBookingMap = new HashMap<>();
        for (Booking b : bookings) {
            LocalDate d = b.getStartTime().atZone(ZoneOffset.UTC).toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                dayBookingMap.merge(d, 1L, Long::sum);
            }
        }

        Map<LocalDate, Long> dayUsageMap = new HashMap<>();
        Map<LocalDate, Long> dayDowntimeMap = new HashMap<>();
        for (Equipment eq : equipmentList) {
            List<EquipmentUsageSession> sessions = getFilteredSessions(eq.getId(), startInstant, endInstant);
            for (EquipmentUsageSession s : sessions) {
                LocalDate d = s.getCheckedInAt().atZone(ZoneOffset.UTC).toLocalDate();
                if (!d.isBefore(start) && !d.isAfter(end)) {
                    dayUsageMap.merge(d, s.getActualDurationMinutes() != null ? s.getActualDurationMinutes() : 0L, Long::sum);
                }
            }

            List<EquipmentDowntimeLog> logs = getFilteredDowntimeLogs(eq.getId(), startInstant, endInstant);
            for (EquipmentDowntimeLog log : logs) {
                LocalDate d = log.getDowntimeStart().atZone(ZoneOffset.UTC).toLocalDate();
                if (!d.isBefore(start) && !d.isAfter(end)) {
                    dayDowntimeMap.merge(d, log.getDurationMinutes() != null ? log.getDurationMinutes() : 0L, Long::sum);
                }
            }
        }

        Map<LocalDate, BigDecimal> dayCostMap = new HashMap<>();
        for (UsageCostResponse uc : usageCosts) {
            if (uc.getStartTime() != null) {
                LocalDate d = uc.getStartTime().atZone(ZoneOffset.UTC).toLocalDate();
                if (!d.isBefore(start) && !d.isAfter(end)) {
                    dayCostMap.merge(d, uc.getTotalCost() != null ? uc.getTotalCost() : BigDecimal.ZERO, BigDecimal::add);
                }
            }
        }

        List<TrendAnalyticsResponse.DailyTrendPoint> points = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long bCount = dayBookingMap.getOrDefault(d, 0L);
            long uMins = dayUsageMap.getOrDefault(d, 0L);
            long dtMins = dayDowntimeMap.getOrDefault(d, 0L);
            BigDecimal cost = dayCostMap.getOrDefault(d, BigDecimal.ZERO);
            points.add(new TrendAnalyticsResponse.DailyTrendPoint(d, bCount, uMins, dtMins, cost));
        }

        TrendAnalyticsResponse response = new TrendAnalyticsResponse();
        response.setStartDate(start);
        response.setEndDate(end);
        response.setDailyTrends(points);
        return response;
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private void validateTenant(Long institutionId) {
        if (institutionId == null) {
            throw new InvalidOperationException("Authenticated institution context is required");
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
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            if (!dept.getInstitution().getId().equals(institutionId)) {
                throw new InvalidOperationException("Department does not belong to authenticated user's institution");
            }
            list = equipmentRepository.findByDepartmentId(departmentId);
        } else {
            list = equipmentRepository.findByInstitutionId(institutionId);
        }
        return list.stream()
                .filter(e -> e.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    private List<Booking> getFilteredBookings(Long institutionId, Long departmentId, Instant startInstant, Instant endInstant) {
        List<Booking> raw;
        if (departmentId != null) {
            raw = bookingRepository.findByDepartmentId(departmentId);
        } else {
            raw = bookingRepository.findByInstitutionId(institutionId);
        }
        return raw.stream()
                .filter(b -> b.getStartTime().isBefore(endInstant) && b.getEndTime().isAfter(startInstant))
                .collect(Collectors.toList());
    }

    private List<EquipmentUsageSession> getFilteredSessions(Long equipmentId, Instant startInstant, Instant endInstant) {
        return sessionRepository.findByEquipmentId(equipmentId).stream()
                .filter(s -> s.getSessionStatus() == SessionStatus.COMPLETED
                        || s.getSessionStatus() == SessionStatus.TERMINATED_EARLY
                        || s.getSessionStatus() == SessionStatus.AUTO_CLOSED)
                .filter(s -> s.getCheckedInAt().isBefore(endInstant)
                        && (s.getCheckedOutAt() == null || s.getCheckedOutAt().isAfter(startInstant)))
                .collect(Collectors.toList());
    }

    private List<EquipmentDowntimeLog> getFilteredDowntimeLogs(Long equipmentId, Instant startInstant, Instant endInstant) {
        return downtimeLogRepository.findByEquipmentId(equipmentId).stream()
                .filter(l -> l.getDowntimeStart().isBefore(endInstant)
                        && (l.getDowntimeEnd() == null || l.getDowntimeEnd().isAfter(startInstant)))
                .collect(Collectors.toList());
    }

    private List<UsageCostResponse> getFilteredUsageCosts(Long institutionId, Long departmentId, Instant startInstant, Instant endInstant) {
        List<UsageCostResponse> raw = costService.listUsageCosts(institutionId, departmentId, null, null);
        return raw.stream()
                .filter(u -> u.getStartTime() != null
                        && u.getStartTime().isBefore(endInstant)
                        && (u.getEndTime() == null || u.getEndTime().isAfter(startInstant)))
                .collect(Collectors.toList());
    }

    private long calculateOperatingMinutes(LocalDate startDate, LocalDate endDate) {
        long minutes = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                minutes += 720; // 08:00 to 20:00 = 12 hours = 720 minutes
            }
        }
        return minutes;
    }

    private BigDecimal toHours(long minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
