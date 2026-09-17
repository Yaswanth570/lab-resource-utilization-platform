package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookingAnalyticsResponse {

    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long noShowBookings;
    private long pendingBookings;
    private long inUseBookings;
    private BigDecimal totalBookedHours = BigDecimal.ZERO;
    private BigDecimal averageBookingDurationMinutes = BigDecimal.ZERO;
    private Map<String, Long> statusDistribution = new LinkedHashMap<>();
    private List<DailyBookingPoint> dailyTrends = new ArrayList<>();
    private List<EquipmentBookingFrequency> equipmentFrequencies = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    public BookingAnalyticsResponse() {
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public long getNoShowBookings() {
        return noShowBookings;
    }

    public void setNoShowBookings(long noShowBookings) {
        this.noShowBookings = noShowBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public long getInUseBookings() {
        return inUseBookings;
    }

    public void setInUseBookings(long inUseBookings) {
        this.inUseBookings = inUseBookings;
    }

    public BigDecimal getTotalBookedHours() {
        return totalBookedHours;
    }

    public void setTotalBookedHours(BigDecimal totalBookedHours) {
        this.totalBookedHours = totalBookedHours;
    }

    public BigDecimal getAverageBookingDurationMinutes() {
        return averageBookingDurationMinutes;
    }

    public void setAverageBookingDurationMinutes(BigDecimal averageBookingDurationMinutes) {
        this.averageBookingDurationMinutes = averageBookingDurationMinutes;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<DailyBookingPoint> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<DailyBookingPoint> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    public List<EquipmentBookingFrequency> getEquipmentFrequencies() {
        return equipmentFrequencies;
    }

    public void setEquipmentFrequencies(List<EquipmentBookingFrequency> equipmentFrequencies) {
        this.equipmentFrequencies = equipmentFrequencies;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ==========================================
    // Nested Classes
    // ==========================================

    public static class DailyBookingPoint {
        private LocalDate date;
        private long totalCount;
        private long completedCount;
        private long cancelledCount;

        public DailyBookingPoint() {
        }

        public DailyBookingPoint(LocalDate date, long totalCount, long completedCount, long cancelledCount) {
            this.date = date;
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public long getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(long completedCount) {
            this.completedCount = completedCount;
        }

        public long getCancelledCount() {
            return cancelledCount;
        }

        public void setCancelledCount(long cancelledCount) {
            this.cancelledCount = cancelledCount;
        }
    }

    public static class EquipmentBookingFrequency {
        private Long equipmentId;
        private String equipmentName;
        private long bookingCount;
        private BigDecimal bookedHours = BigDecimal.ZERO;

        public EquipmentBookingFrequency() {
        }

        public EquipmentBookingFrequency(Long equipmentId, String equipmentName, long bookingCount, BigDecimal bookedHours) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.bookingCount = bookingCount;
            this.bookedHours = bookedHours;
        }

        public Long getEquipmentId() {
            return equipmentId;
        }

        public void setEquipmentId(Long equipmentId) {
            this.equipmentId = equipmentId;
        }

        public String getEquipmentName() {
            return equipmentName;
        }

        public void setEquipmentName(String equipmentName) {
            this.equipmentName = equipmentName;
        }

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }

        public BigDecimal getBookedHours() {
            return bookedHours;
        }

        public void setBookedHours(BigDecimal bookedHours) {
            this.bookedHours = bookedHours;
        }
    }
}
