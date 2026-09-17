package com.labresource.platform.notification.service;

import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import com.labresource.platform.notification.repository.NotificationRepository;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listUserNotifications(Long userId, Boolean isRead, NotificationEventType eventType, NotificationPriority priority) {
        if (userId == null) {
            throw new InvalidOperationException("User ID is required to fetch notifications");
        }

        List<Notification> list;
        if (isRead != null) {
            list = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        } else {
            list = notificationRepository.findByUserId(userId).stream()
                    .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .toList();
        }

        return list.stream()
                .filter(n -> eventType == null || n.getEventType() == eventType)
                .filter(n -> priority == null || n.getPriority() == priority)
                .map(this::initializeNotification)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (userId != null && (notification.getUser() == null || !Objects.equals(notification.getUser().getId(), userId))) {
            throw new InvalidOperationException("Notification does not belong to authenticated user");
        }

        return initializeNotification(notification);
    }

    @Override
    public Notification markAsRead(Long id, Long userId) {
        Notification notification = getNotificationById(id, userId);

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return initializeNotification(notification);
    }

    @Override
    public Notification markAsUnread(Long id, Long userId) {
        Notification notification = getNotificationById(id, userId);

        if (notification.isRead()) {
            notification.setRead(false);
            notification.setReadAt(null);
            notification = notificationRepository.save(notification);
        }

        return initializeNotification(notification);
    }

    @Override
    public int markAllAsRead(Long userId) {
        if (userId == null) {
            throw new InvalidOperationException("User ID is required to mark notifications as read");
        }

        List<Notification> unreadList = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        Instant now = Instant.now();
        for (Notification n : unreadList) {
            n.setRead(true);
            n.setReadAt(now);
            notificationRepository.save(n);
        }

        return unreadList.size();
    }

    @Override
    public void deleteNotification(Long id, Long userId) {
        Notification notification = getNotificationById(id, userId);
        notificationRepository.delete(notification);
    }

    @Override
    public Notification createNotification(Long userId, String title, String message,
                                           NotificationEventType eventType, NotificationPriority priority,
                                           String relatedEntityType, Long relatedEntityId) {
        if (userId == null) {
            throw new InvalidOperationException("Target user ID is required to create a notification");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidOperationException("Notification title is required");
        }
        if (message == null || message.isBlank()) {
            throw new InvalidOperationException("Notification message is required");
        }
        if (eventType == null) {
            throw new InvalidOperationException("Notification event type is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title.trim());
        notification.setMessage(message.trim());
        notification.setEventType(eventType);
        notification.setPriority(priority != null ? priority : NotificationPriority.INFO);
        notification.setRead(false);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setCreatedAt(Instant.now());

        Notification saved = notificationRepository.save(notification);
        return initializeNotification(saved);
    }

    private Notification initializeNotification(Notification n) {
        if (n.getUser() != null) {
            Hibernate.initialize(n.getUser());
        }
        return n;
    }
}
