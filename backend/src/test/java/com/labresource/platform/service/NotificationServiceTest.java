package com.labresource.platform.service;

import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import com.labresource.platform.notification.repository.NotificationRepository;
import com.labresource.platform.notification.service.NotificationServiceImpl;
import com.labresource.platform.user.User;
import com.labresource.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user1;
    private User user2;
    private Notification notification1;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(10L);
        user1.setEmail("user10@lab.edu");

        user2 = new User();
        user2.setId(20L);
        user2.setEmail("user20@lab.edu");

        notification1 = new Notification();
        notification1.setId(100L);
        notification1.setUser(user1);
        notification1.setTitle("Booking Approved");
        notification1.setMessage("Your booking for Spectrometer has been approved.");
        notification1.setEventType(NotificationEventType.BOOKING_APPROVED);
        notification1.setPriority(NotificationPriority.INFO);
        notification1.setRead(false);
        notification1.setCreatedAt(Instant.now().minusSeconds(3600));
    }

    @Test
    @DisplayName("listUserNotifications returns user notifications sorted")
    void listUserNotifications_Success() {
        when(notificationRepository.findByUserId(10L)).thenReturn(List.of(notification1));

        List<Notification> result = notificationService.listUserNotifications(10L, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Booking Approved", result.get(0).getTitle());
        verify(notificationRepository).findByUserId(10L);
    }

    @Test
    @DisplayName("listUserNotifications filters by isRead flag")
    void listUserNotifications_FilterIsRead() {
        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(10L, false))
                .thenReturn(List.of(notification1));

        List<Notification> result = notificationService.listUserNotifications(10L, false, null, null);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
        verify(notificationRepository).findByUserIdAndIsReadOrderByCreatedAtDesc(10L, false);
    }

    @Test
    @DisplayName("listUserNotifications filters by eventType and priority")
    void listUserNotifications_FilterEventTypeAndPriority() {
        when(notificationRepository.findByUserId(10L)).thenReturn(List.of(notification1));

        List<Notification> match = notificationService.listUserNotifications(
                10L, null, NotificationEventType.BOOKING_APPROVED, NotificationPriority.INFO);
        assertEquals(1, match.size());

        List<Notification> mismatch = notificationService.listUserNotifications(
                10L, null, NotificationEventType.MAINTENANCE_SCHEDULED, NotificationPriority.INFO);
        assertTrue(mismatch.isEmpty());
    }

    @Test
    @DisplayName("getUnreadCount returns correct count from repository")
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(10L);

        assertEquals(5L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(10L);
    }

    @Test
    @DisplayName("getNotificationById returns notification when owned by authenticated user")
    void getNotificationById_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        Notification result = notificationService.getNotificationById(100L, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(10L, result.getUser().getId());
    }

    @Test
    @DisplayName("getNotificationById throws InvalidOperationException when owned by another user")
    void getNotificationById_OtherUserThrows() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        assertThrows(InvalidOperationException.class, () ->
                notificationService.getNotificationById(100L, 20L));
    }

    @Test
    @DisplayName("getNotificationById throws ResourceNotFoundException when ID not found")
    void getNotificationById_NotFoundThrows() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.getNotificationById(999L, 10L));
    }

    @Test
    @DisplayName("markAsRead updates read status and timestamp")
    void markAsRead_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.markAsRead(100L, 10L);

        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());
        verify(notificationRepository).save(notification1);
    }

    @Test
    @DisplayName("markAsRead is idempotent when notification is already read")
    void markAsRead_Idempotent() {
        Instant initialReadAt = Instant.now().minusSeconds(60);
        notification1.setRead(true);
        notification1.setReadAt(initialReadAt);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        Notification result = notificationService.markAsRead(100L, 10L);

        assertTrue(result.isRead());
        assertEquals(initialReadAt, result.getReadAt());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsRead throws InvalidOperationException if user is not the owner")
    void markAsRead_OtherUserThrows() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        assertThrows(InvalidOperationException.class, () ->
                notificationService.markAsRead(100L, 20L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsUnread resets read flag and timestamp")
    void markAsUnread_Success() {
        notification1.setRead(true);
        notification1.setReadAt(Instant.now());
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.markAsUnread(100L, 10L);

        assertFalse(result.isRead());
        assertNull(result.getReadAt());
        verify(notificationRepository).save(notification1);
    }

    @Test
    @DisplayName("markAllAsRead marks all unread notifications of user")
    void markAllAsRead_Success() {
        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(10L, false))
                .thenReturn(List.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        int count = notificationService.markAllAsRead(10L);

        assertEquals(1, count);
        assertTrue(notification1.isRead());
        assertNotNull(notification1.getReadAt());
        verify(notificationRepository).save(notification1);
    }

    @Test
    @DisplayName("deleteNotification deletes owned notification")
    void deleteNotification_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        notificationService.deleteNotification(100L, 10L);

        verify(notificationRepository).delete(notification1);
    }

    @Test
    @DisplayName("deleteNotification throws InvalidOperationException for another user's notification")
    void deleteNotification_OtherUserThrows() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification1));

        assertThrows(InvalidOperationException.class, () ->
                notificationService.deleteNotification(100L, 20L));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("createNotification creates and saves valid notification")
    void createNotification_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(200L);
            return n;
        });

        Notification created = notificationService.createNotification(
                10L,
                "New Maintenance",
                "Routine checkup scheduled",
                NotificationEventType.MAINTENANCE_SCHEDULED,
                NotificationPriority.WARNING,
                "MAINTENANCE_REQUEST",
                55L
        );

        assertNotNull(created);
        assertEquals(200L, created.getId());
        assertEquals("New Maintenance", created.getTitle());
        assertEquals(NotificationEventType.MAINTENANCE_SCHEDULED, created.getEventType());
        assertEquals(NotificationPriority.WARNING, created.getPriority());
        assertEquals("MAINTENANCE_REQUEST", created.getRelatedEntityType());
        assertEquals(55L, created.getRelatedEntityId());
        assertFalse(created.isRead());
        verify(notificationRepository).save(any(Notification.class));
    }
}
