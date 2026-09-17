package com.labresource.platform.notification.web;

public class BatchUpdateResponse {

    private int updatedCount;
    private String message;

    public BatchUpdateResponse() {
    }

    public BatchUpdateResponse(int updatedCount, String message) {
        this.updatedCount = updatedCount;
        this.message = message;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
