package com.labresource.platform.institution.web;

import com.labresource.platform.institution.Institution;

import java.time.Instant;

public class InstitutionResponse {

    private Long id;
    private String name;
    private String code;
    private String domain;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String contactEmail;
    private String contactPhone;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public InstitutionResponse() {
    }

    public static InstitutionResponse fromEntity(Institution institution) {
        if (institution == null) {
            return null;
        }
        InstitutionResponse res = new InstitutionResponse();
        res.setId(institution.getId());
        res.setName(institution.getName());
        res.setCode(institution.getCode());
        res.setDomain(institution.getDomain());
        res.setAddressLine1(institution.getAddressLine1());
        res.setAddressLine2(institution.getAddressLine2());
        res.setCity(institution.getCity());
        res.setState(institution.getState());
        res.setCountry(institution.getCountry());
        res.setPostalCode(institution.getPostalCode());
        res.setContactEmail(institution.getContactEmail());
        res.setContactPhone(institution.getContactPhone());
        res.setActive(institution.isActive());
        res.setCreatedAt(institution.getCreatedAt());
        res.setUpdatedAt(institution.getUpdatedAt());
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
