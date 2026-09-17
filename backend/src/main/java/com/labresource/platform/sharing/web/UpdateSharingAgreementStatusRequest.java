package com.labresource.platform.sharing.web;

import com.labresource.platform.sharing.SharingAgreementStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateSharingAgreementStatusRequest {

    @NotNull(message = "Status is required")
    private SharingAgreementStatus status;

    public UpdateSharingAgreementStatusRequest() {
    }

    public UpdateSharingAgreementStatusRequest(SharingAgreementStatus status) {
        this.status = status;
    }

    public SharingAgreementStatus getStatus() {
        return status;
    }

    public void setStatus(SharingAgreementStatus status) {
        this.status = status;
    }
}
