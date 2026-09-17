package com.labresource.platform.utilization.web;

import java.time.Instant;

public class CompleteUsageSessionRequest {

    private Instant checkedOutAt;
    private String notes;

    public CompleteUsageSessionRequest() {
    }

    public Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(Instant checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
