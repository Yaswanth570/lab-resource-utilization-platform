package com.labresource.platform.user.web;

import com.labresource.platform.user.UserRoleType;

public class AssignRoleRequest {

    private Long roleId;
    private UserRoleType roleType;

    public AssignRoleRequest() {
    }

    public AssignRoleRequest(Long roleId) {
        this.roleId = roleId;
    }

    public AssignRoleRequest(UserRoleType roleType) {
        this.roleType = roleType;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public UserRoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(UserRoleType roleType) {
        this.roleType = roleType;
    }
}
