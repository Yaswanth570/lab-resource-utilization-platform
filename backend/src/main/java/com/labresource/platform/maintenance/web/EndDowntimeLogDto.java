package com.labresource.platform.maintenance.web;

import java.time.Instant;

public class EndDowntimeLogDto {

    private Instant downtimeEnd;
    private Integer durationMinutes;

    public EndDowntimeLogDto() {
    }

    public Instant getDowntimeEnd() {
        return downtimeEnd;
    }

    public void setDowntimeEnd(Instant downtimeEnd) {
        this.downtimeEnd = downtimeEnd;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
