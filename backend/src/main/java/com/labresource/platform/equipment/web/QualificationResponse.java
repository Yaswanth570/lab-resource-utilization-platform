package com.labresource.platform.equipment.web;

import com.labresource.platform.equipment.QualificationStatus;
import com.labresource.platform.equipment.UserEquipmentQualification;

import java.time.Instant;

public class QualificationResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long equipmentId;
    private String equipmentName;
    private Long certifiedByUserId;
    private String certifiedByUserName;
    private Instant certifiedAt;
    private Instant expiresAt;
    private QualificationStatus status;
    private String notes;

    public QualificationResponse() {
    }

    public static QualificationResponse from(UserEquipmentQualification q) {
        if (q == null) {
            return null;
        }
        QualificationResponse r = new QualificationResponse();
        r.setId(q.getId());
        if (q.getUser() != null) {
            r.setUserId(q.getUser().getId());
            r.setUserName((q.getUser().getFirstName() + " " + q.getUser().getLastName()).trim());
            r.setUserEmail(q.getUser().getEmail());
        }
        if (q.getEquipment() != null) {
            r.setEquipmentId(q.getEquipment().getId());
            r.setEquipmentName(q.getEquipment().getName());
        }
        if (q.getCertifiedBy() != null) {
            r.setCertifiedByUserId(q.getCertifiedBy().getId());
            r.setCertifiedByUserName((q.getCertifiedBy().getFirstName() + " " + q.getCertifiedBy().getLastName()).trim());
        }
        r.setCertifiedAt(q.getCertifiedAt());
        r.setExpiresAt(q.getExpiresAt());
        r.setStatus(q.getStatus());
        r.setNotes(q.getNotes());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public Long getCertifiedByUserId() {
        return certifiedByUserId;
    }

    public void setCertifiedByUserId(Long certifiedByUserId) {
        this.certifiedByUserId = certifiedByUserId;
    }

    public String getCertifiedByUserName() {
        return certifiedByUserName;
    }

    public void setCertifiedByUserName(String certifiedByUserName) {
        this.certifiedByUserName = certifiedByUserName;
    }

    public Instant getCertifiedAt() {
        return certifiedAt;
    }

    public void setCertifiedAt(Instant certifiedAt) {
        this.certifiedAt = certifiedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
