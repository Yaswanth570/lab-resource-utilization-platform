package com.labresource.platform.user.web;

import com.labresource.platform.user.Role;

import java.time.Instant;

public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Instant createdAt;

    public RoleResponse() {
    }

    public static RoleResponse fromEntity(Role role) {
        if (role == null) {
            return null;
        }
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setName(role.getName() != null ? role.getName().name() : null);
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
