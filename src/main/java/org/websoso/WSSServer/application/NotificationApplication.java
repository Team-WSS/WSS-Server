package org.websoso.WSSServer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.dto.NotificationGetResponse;
import org.websoso.WSSServer.notification.dto.NotificationsGetResponse;
import org.websoso.WSSServer.notification.dto.NotificationsReadStatusGetResponse;
import org.websoso.WSSServer.notification.service.NotificationService;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationApplication {

    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public NotificationsReadStatusGetResponse getNotificationStatus(User user) {
        boolean hasUnreadNotifications = notificationService.hasUnreadNotifications(user);

        return NotificationsReadStatusGetResponse.of(hasUnreadNotifications);
    }

    @Transactional(readOnly = true)
    public NotificationsGetResponse getNotifications(User user, Long lastNotificationId, int size) {
        return notificationService.getNotifications(user, lastNotificationId, size);
    }

    @Transactional
    public NotificationGetResponse getNotificationDetail(User user, Long notificationId) {

        Notification notification = notificationService.getNotification(user, notificationId);

        notificationService.markAsRead(user, notification);

        return NotificationGetResponse.of(notification);
    }
}
