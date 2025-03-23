package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.notification.NotificationCreateRequest;
import org.websoso.WSSServer.dto.notification.NotificationGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationsGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationsReadStatusGetResponse;
import org.websoso.WSSServer.service.NotificationService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<NotificationsGetResponse> getNotifications(@AuthenticationPrincipal User user,
                                                                     @RequestParam("lastNotificationId") Long lastNotificationId,
                                                                     @RequestParam("size") int size) {
        return ResponseEntity
                .status(OK)
                .body(notificationService.getNotifications(lastNotificationId, size, user));
    }

    @GetMapping("/unread")
    public ResponseEntity<NotificationsReadStatusGetResponse> checkNotificationsReadStatus(
            @AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(notificationService.checkNotificationsReadStatus(user));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationGetResponse> getNotification(@AuthenticationPrincipal User user,
                                                                   @PathVariable("notificationId") Long notificationId) {
        return ResponseEntity
                .status(OK)
                .body(notificationService.getNotification(user, notificationId));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> createNotificationAsRead(@AuthenticationPrincipal User user,
                                                         @PathVariable("notificationId") Long notificationId) {
        notificationService.createNotificationAsRead(user, notificationId);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping
    public ResponseEntity<Void> createNoticeNotification(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody NotificationCreateRequest notificationCreateRequest) {
        notificationService.createNoticeNotification(user, notificationCreateRequest);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
