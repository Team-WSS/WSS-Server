package org.websoso.WSSServer.notification.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.NotificationApplication;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.notification.dto.NotificationGetResponse;
import org.websoso.WSSServer.notification.dto.NotificationsGetResponse;
import org.websoso.WSSServer.notification.dto.NotificationsReadStatusGetResponse;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplication notificationApplication;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationsGetResponse> getNotifications(@AuthenticationPrincipal User user,
                                                                     @RequestParam("lastNotificationId") Long lastNotificationId,
                                                                     @RequestParam("size") int size) {
        return ResponseEntity
                .status(OK)
                .body(notificationApplication.getNotifications(user, lastNotificationId, size));
    }

    @GetMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationGetResponse> getNotification(@AuthenticationPrincipal User user,
                                                                   @PathVariable("notificationId") Long notificationId) {
        return ResponseEntity
                .status(OK)
                .body(notificationApplication.getNotificationDetail(user, notificationId));
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationsReadStatusGetResponse> getNotificationStatus(
            @AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(notificationApplication.getReadStatus(user));
    }

    @PatchMapping("/{notificationId}/read-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createNotificationAsReadStatus(@AuthenticationPrincipal User user,
                                                         @PathVariable("notificationId") Long notificationId) {
        notificationApplication.updateNotificationReadStatus(user, notificationId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @Deprecated(since = "/status 전환 이후")
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationsReadStatusGetResponse> checkNotificationsReadStatusDeprecated(
            @AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(notificationApplication.getReadStatus(user));
    }

    @Deprecated(since = "/{notificationId}/read-status 전환 이후")
    @PostMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createNotificationAsRead(@AuthenticationPrincipal User user,
                                                         @PathVariable("notificationId") Long notificationId) {
        notificationApplication.updateNotificationReadStatus(user, notificationId);

        return ResponseEntity
                .status(CREATED)
                .build();
    }

}
