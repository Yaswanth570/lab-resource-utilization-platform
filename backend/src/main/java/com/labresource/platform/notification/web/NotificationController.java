package com.labresource.platform.notification.web;

import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import com.labresource.platform.notification.service.NotificationService;
import com.labresource.platform.security.principal.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Long getRequiredCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BadCredentialsException("Authenticated user context is required"));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationEventType eventType,
            @RequestParam(required = false) NotificationPriority priority) {

        Long currentUserId = getRequiredCurrentUserId();
        List<Notification> list = notificationService.listUserNotifications(currentUserId, isRead, eventType, priority);
        List<NotificationResponse> response = list.stream().map(NotificationResponse::fromEntity).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        Long currentUserId = getRequiredCurrentUserId();
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        Long currentUserId = getRequiredCurrentUserId();
        Notification notification = notificationService.getNotificationById(id, currentUserId);
        return ResponseEntity.ok(NotificationResponse.fromEntity(notification));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        Long currentUserId = getRequiredCurrentUserId();
        Notification updated = notificationService.markAsRead(id, currentUserId);
        return ResponseEntity.ok(NotificationResponse.fromEntity(updated));
    }

    @PatchMapping("/{id}/unread")
    public ResponseEntity<NotificationResponse> markAsUnread(@PathVariable Long id) {
        Long currentUserId = getRequiredCurrentUserId();
        Notification updated = notificationService.markAsUnread(id, currentUserId);
        return ResponseEntity.ok(NotificationResponse.fromEntity(updated));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<BatchUpdateResponse> markAllAsRead() {
        Long currentUserId = getRequiredCurrentUserId();
        int count = notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(new BatchUpdateResponse(count, "Marked " + count + " notification(s) as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long currentUserId = getRequiredCurrentUserId();
        notificationService.deleteNotification(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        Long targetUserId = request.getUserId() != null ? request.getUserId() : getRequiredCurrentUserId();
        Notification created = notificationService.createNotification(
                targetUserId,
                request.getTitle(),
                request.getMessage(),
                request.getEventType(),
                request.getPriority(),
                request.getRelatedEntityType(),
                request.getRelatedEntityId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/notifications/" + created.getId()))
                .body(NotificationResponse.fromEntity(created));
    }
}
