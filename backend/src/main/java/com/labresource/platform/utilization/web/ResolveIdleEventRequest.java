package com.labresource.platform.utilization.web;

import java.time.Instant;

public class ResolveIdleEventRequest {

    private Instant idleEndTime;
    private String notes;

    public ResolveIdleEventRequest() {
    }

    public Instant getIdleEndTime() {
        return idleEndTime;
    }

    public void setIdleEndTime(Instant idleEndTime) {
        this.idleEndTime = idleEndTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
