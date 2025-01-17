package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
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
    public ResponseEntity<NotificationsGetResponse> getNotifications(Principal principal,
                                                                     @RequestParam("lastNotificationId") Long lastNotificationId,
                                                                     @RequestParam("size") int size) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(notificationService.getNotifications(lastNotificationId, size, user));
    }

    @GetMapping("/unread")
    public ResponseEntity<NotificationsReadStatusGetResponse> checkNotificationsReadStatus(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(notificationService.checkNotificationsReadStatus(user));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationGetResponse> getNotification(Principal principal,
                                                                   @PathVariable("notificationId") Long notificationId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(notificationService.getNotification(user, notificationId));
    }
}
