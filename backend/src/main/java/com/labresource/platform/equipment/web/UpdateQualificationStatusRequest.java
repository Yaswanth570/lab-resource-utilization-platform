package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.QualificationStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateQualificationStatusRequest {

    @NotNull(message = "Status is required")
    private QualificationStatus status;

    private String notes;

    public UpdateQualificationStatusRequest() {
    }

    public QualificationStatus getStatus() {
        return status;
    }

    public void setStatus(QualificationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
