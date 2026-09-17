package com.labresource.platform.report.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookingReportResponse {

    private ReportMetadataResponse metadata;
    private BookingSummary summary = new BookingSummary();
    private Map<String, Long> statusDistribution = new LinkedHashMap<>();
    private List<BookingEquipmentRow> equipmentRows = new ArrayList<>();
    private List<BookingDailyRow> dailyRows = new ArrayList<>();

    public BookingReportResponse() {
    }

    public ReportMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(ReportMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public BookingSummary getSummary() {
        return summary;
    }

    public void setSummary(BookingSummary summary) {
        this.summary = summary;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<BookingEquipmentRow> getEquipmentRows() {
        return equipmentRows;
    }

    public void setEquipmentRows(List<BookingEquipmentRow> equipmentRows) {
        this.equipmentRows = equipmentRows;
    }

    public List<BookingDailyRow> getDailyRows() {
        return dailyRows;
    }

    public void setDailyRows(List<BookingDailyRow> dailyRows) {
        this.dailyRows = dailyRows;
    }

    public static class BookingSummary {
        private long totalBookings;
        private long completedBookings;
        private long cancelledBookings;
        private long noShowBookings;
        private long pendingBookings;
        private long inUseBookings;
        private BigDecimal totalBookedHours = BigDecimal.ZERO;
        private BigDecimal averageDurationMinutes = BigDecimal.ZERO;

        public BookingSummary() {
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

        public BigDecimal getAverageDurationMinutes() {
            return averageDurationMinutes;
        }

        public void setAverageDurationMinutes(BigDecimal averageDurationMinutes) {
            this.averageDurationMinutes = averageDurationMinutes;
        }
    }

    public static class BookingEquipmentRow {
        private Long equipmentId;
        private String equipmentName;
        private long bookingCount;
        private BigDecimal bookedHours = BigDecimal.ZERO;

        public BookingEquipmentRow() {
        }

        public BookingEquipmentRow(Long equipmentId, String equipmentName, long bookingCount, BigDecimal bookedHours) {
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

    public static class BookingDailyRow {
        private LocalDate date;
        private long totalCount;
        private long completedCount;
        private long cancelledCount;

        public BookingDailyRow() {
        }

        public BookingDailyRow(LocalDate date, long totalCount, long completedCount, long cancelledCount) {
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
}
