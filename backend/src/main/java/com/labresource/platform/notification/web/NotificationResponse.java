package com.labresource.platform.notification.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import org.hibernate.Hibernate;

import java.time.Instant;

public class NotificationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String title;
    private String message;
    private NotificationEventType eventType;
    private NotificationPriority priority;

    @JsonProperty("isRead")
    private boolean isRead;

    private Instant readAt;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Instant createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponse r = new NotificationResponse();
        r.setId(notification.getId());
        if (notification.getUser() != null) {
            r.setUserId(notification.getUser().getId());
            if (Hibernate.isInitialized(notification.getUser())) {
                try {
                    r.setUserEmail(notification.getUser().getEmail());
                } catch (Exception ignored) {
                }
            }
        }
        r.setTitle(notification.getTitle());
        r.setMessage(notification.getMessage());
        r.setEventType(notification.getEventType());
        r.setPriority(notification.getPriority());
        r.setRead(notification.isRead());
        r.setReadAt(notification.getReadAt());
        r.setRelatedEntityType(notification.getRelatedEntityType());
        r.setRelatedEntityId(notification.getRelatedEntityId());
        r.setCreatedAt(notification.getCreatedAt());
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    @JsonProperty("isRead")
    public boolean isRead() {
        return isRead;
    }

    @JsonProperty("isRead")
    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
