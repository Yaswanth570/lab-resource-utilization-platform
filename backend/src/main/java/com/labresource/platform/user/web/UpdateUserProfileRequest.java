package com.labresource.platform.user.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    @Pattern(regexp = "^$|^[+]?[0-9\\s\\-().]{7,30}$", message = "Phone number must be a valid phone format or empty")
    private String phone;

    private Long institutionId;

    private Long departmentId;

    public UpdateUserProfileRequest() {
    }

    public UpdateUserProfileRequest(String firstName, String lastName, String phone, Long institutionId, Long departmentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.institutionId = institutionId;
        this.departmentId = departmentId;
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

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
