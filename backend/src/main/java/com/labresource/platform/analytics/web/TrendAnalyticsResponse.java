package com.labresource.platform.analytics.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrendAnalyticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyTrendPoint> dailyTrends = new ArrayList<>();

    public TrendAnalyticsResponse() {
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

    public List<DailyTrendPoint> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<DailyTrendPoint> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    public static class DailyTrendPoint {
        private LocalDate date;
        private long bookingCount;
        private long usageMinutes;
        private BigDecimal usageHours = BigDecimal.ZERO;
        private long downtimeMinutes;
        private BigDecimal downtimeHours = BigDecimal.ZERO;
        private BigDecimal cost = BigDecimal.ZERO;

        public DailyTrendPoint() {
        }

        public DailyTrendPoint(LocalDate date, long bookingCount, long usageMinutes, long downtimeMinutes, BigDecimal cost) {
            this.date = date;
            this.bookingCount = bookingCount;
            this.usageMinutes = usageMinutes;
            this.usageHours = BigDecimal.valueOf(usageMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.downtimeMinutes = downtimeMinutes;
            this.downtimeHours = BigDecimal.valueOf(downtimeMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            this.cost = cost != null ? cost : BigDecimal.ZERO;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(long bookingCount) {
            this.bookingCount = bookingCount;
        }

        public long getUsageMinutes() {
            return usageMinutes;
        }

        public void setUsageMinutes(long usageMinutes) {
            this.usageMinutes = usageMinutes;
        }

        public BigDecimal getUsageHours() {
            return usageHours;
        }

        public void setUsageHours(BigDecimal usageHours) {
            this.usageHours = usageHours;
        }

        public long getDowntimeMinutes() {
            return downtimeMinutes;
        }

        public void setDowntimeMinutes(long downtimeMinutes) {
            this.downtimeMinutes = downtimeMinutes;
        }

        public BigDecimal getDowntimeHours() {
            return downtimeHours;
        }

        public void setDowntimeHours(BigDecimal downtimeHours) {
            this.downtimeHours = downtimeHours;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }
    }
}
