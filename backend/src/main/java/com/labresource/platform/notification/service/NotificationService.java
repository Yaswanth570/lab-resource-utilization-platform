package com.labresource.platform.notification.service;

import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;

import java.util.List;

public interface NotificationService {

    List<Notification> listUserNotifications(Long userId, Boolean isRead, NotificationEventType eventType, NotificationPriority priority);

    long getUnreadCount(Long userId);

    Notification getNotificationById(Long id, Long userId);

    Notification markAsRead(Long id, Long userId);

    Notification markAsUnread(Long id, Long userId);

    int markAllAsRead(Long userId);

    void deleteNotification(Long id, Long userId);

    Notification createNotification(Long userId, String title, String message, NotificationEventType eventType, NotificationPriority priority, String relatedEntityType, Long relatedEntityId);
}
