package com.labresource.platform.user.web;

import com.labresource.platform.user.Role;
import com.labresource.platform.user.User;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserResponse {

    private Long id;
    private Long institutionId;
    private String institutionName;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String status;
    private boolean isVerified;
    private List<String> roles;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse() {
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        if (user.getInstitution() != null) {
            res.setInstitutionId(user.getInstitution().getId());
            try {
                res.setInstitutionName(user.getInstitution().getName());
            } catch (Exception ignored) {
            }
        }
        if (user.getDepartment() != null) {
            res.setDepartmentId(user.getDepartment().getId());
            try {
                res.setDepartmentName(user.getDepartment().getName());
            } catch (Exception ignored) {
            }
        }
        res.setEmail(user.getEmail());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setPhone(user.getPhone());
        res.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        res.setVerified(user.isVerified());

        List<String> roleNames = Collections.emptyList();
        try {
            if (user.getRoles() != null) {
                roleNames = user.getRoles().stream()
                        .map(Role::getName)
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .toList();
            }
        } catch (Exception ignored) {
        }
        res.setRoles(roleNames);

        res.setLastLoginAt(user.getLastLoginAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
