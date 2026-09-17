package com.labresource.platform.report.web;

import java.util.List;

public class ReportTypeDefinition {

    private String id;
    private String name;
    private String description;
    private List<String> supportedFormats;

    public ReportTypeDefinition() {
    }

    public ReportTypeDefinition(String id, String name, String description, List<String> supportedFormats) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.supportedFormats = supportedFormats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(List<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }
}
