package com.labresource.platform.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labresource.platform.common.exception.InvalidOperationException;
import com.labresource.platform.common.exception.ResourceNotFoundException;
import com.labresource.platform.notification.Notification;
import com.labresource.platform.notification.NotificationEventType;
import com.labresource.platform.notification.NotificationPriority;
import com.labresource.platform.notification.service.NotificationService;
import com.labresource.platform.notification.web.CreateNotificationRequest;
import com.labresource.platform.security.jwt.JwtService;
import com.labresource.platform.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}",
        "spring.datasource.password=${DB_PASSWORD:}"
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private NotificationService notificationService;

    private String validToken;
    private final Long testUserId = 50L;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        validToken = jwtService.generateToken(testUserId, "testuser@lab.edu", 1L, List.of("ROLE_RESEARCHER"));

        User user = new User();
        user.setId(testUserId);
        user.setEmail("testuser@lab.edu");

        testNotification = new Notification();
        testNotification.setId(101L);
        testNotification.setUser(user);
        testNotification.setTitle("Reservation Confirmed");
        testNotification.setMessage("Your reservation for Electron Microscope has been confirmed.");
        testNotification.setEventType(NotificationEventType.BOOKING_APPROVED);
        testNotification.setPriority(NotificationPriority.INFO);
        testNotification.setRead(false);
        testNotification.setRelatedEntityType("BOOKING");
        testNotification.setRelatedEntityId(300L);
        testNotification.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("GET /api/notifications without token returns 401 Unauthorized")
    void listNotifications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/notifications with valid token returns 200 and list")
    void listNotifications_Authenticated_Returns200() throws Exception {
        when(notificationService.listUserNotifications(eq(testUserId), any(), any(), any()))
                .thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(101)))
                .andExpect(jsonPath("$[0].title", is("Reservation Confirmed")))
                .andExpect(jsonPath("$[0].eventType", is("BOOKING_APPROVED")))
                .andExpect(jsonPath("$[0].isRead", is(false)));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count returns 200 and count")
    void getUnreadCount_Returns200() throws Exception {
        when(notificationService.getUnreadCount(testUserId)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(3)));
    }

    @Test
    @DisplayName("GET /api/notifications/{id} returns 200 and notification details")
    void getNotificationById_Success_Returns200() throws Exception {
        when(notificationService.getNotificationById(101L, testUserId)).thenReturn(testNotification);

        mockMvc.perform(get("/api/notifications/101")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.title", is("Reservation Confirmed")))
                .andExpect(jsonPath("$.relatedEntityType", is("BOOKING")))
                .andExpect(jsonPath("$.relatedEntityId", is(300)));
    }

    @Test
    @DisplayName("GET /api/notifications/{id} returns 404 when notification does not exist")
    void getNotificationById_NotFound_Returns404() throws Exception {
        when(notificationService.getNotificationById(999L, testUserId))
                .thenThrow(new ResourceNotFoundException("Notification", "id", 999L));

        mockMvc.perform(get("/api/notifications/999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/notifications/{id} returns 400 when notification belongs to another user")
    void getNotificationById_OtherUser_Returns400() throws Exception {
        when(notificationService.getNotificationById(101L, testUserId))
                .thenThrow(new InvalidOperationException("Notification does not belong to authenticated user"));

        mockMvc.perform(get("/api/notifications/101")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not belong")));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read marks notification as read")
    void markAsRead_Success_Returns200() throws Exception {
        testNotification.setRead(true);
        testNotification.setReadAt(Instant.now());
        when(notificationService.markAsRead(101L, testUserId)).thenReturn(testNotification);

        mockMvc.perform(patch("/api/notifications/101/read")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.isRead", is(true)))
                .andExpect(jsonPath("$.readAt", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read returns 400 when notification belongs to another user")
    void markAsRead_OtherUser_Returns400() throws Exception {
        when(notificationService.markAsRead(101L, testUserId))
                .thenThrow(new InvalidOperationException("Notification does not belong to authenticated user"));

        mockMvc.perform(patch("/api/notifications/101/read")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/unread marks notification as unread")
    void markAsUnread_Success_Returns200() throws Exception {
        testNotification.setRead(false);
        testNotification.setReadAt(null);
        when(notificationService.markAsUnread(101L, testUserId)).thenReturn(testNotification);

        mockMvc.perform(patch("/api/notifications/101/unread")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.isRead", is(false)))
                .andExpect(jsonPath("$.readAt", nullValue()));
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all marks all notifications as read")
    void markAllAsRead_Success_Returns200() throws Exception {
        when(notificationService.markAllAsRead(testUserId)).thenReturn(4);

        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount", is(4)));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} deletes notification and returns 204 No Content")
    void deleteNotification_Success_Returns204() throws Exception {
        doNothing().when(notificationService).deleteNotification(101L, testUserId);

        mockMvc.perform(delete("/api/notifications/101")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} returns 400 when notification belongs to another user")
    void deleteNotification_OtherUser_Returns400() throws Exception {
        doThrow(new InvalidOperationException("Notification does not belong to authenticated user"))
                .when(notificationService).deleteNotification(101L, testUserId);

        mockMvc.perform(delete("/api/notifications/101")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/notifications creates new notification and returns 201 Created")
    void createNotification_Success_Returns201() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("New Alert");
        request.setMessage("A new system alert occurred.");
        request.setEventType(NotificationEventType.IDLE_ALERT);
        request.setPriority(NotificationPriority.WARNING);

        when(notificationService.createNotification(eq(testUserId), eq("New Alert"), eq("A new system alert occurred."),
                eq(NotificationEventType.IDLE_ALERT), eq(NotificationPriority.WARNING), any(), any()))
                .thenReturn(testNotification);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/notifications/")));
    }

    @Test
    @DisplayName("POST /api/notifications returns 400 on invalid payload (blank title)")
    void createNotification_InvalidPayload_Returns400() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle(""); // blank title
        request.setMessage("Valid message");
        request.setEventType(NotificationEventType.IDLE_ALERT);

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
