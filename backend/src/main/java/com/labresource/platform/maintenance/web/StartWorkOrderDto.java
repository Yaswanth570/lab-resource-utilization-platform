package com.labresource.platform.maintenance.web;

import java.time.Instant;

public class StartWorkOrderDto {

    private Instant actualStart;

    public StartWorkOrderDto() {
    }

    public Instant getActualStart() {
        return actualStart;
    }

    public void setActualStart(Instant actualStart) {
        this.actualStart = actualStart;
    }
}
