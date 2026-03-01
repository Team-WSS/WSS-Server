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
public class NotificationApplication {

    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public NotificationsReadStatusGetResponse getReadStatus(User user) {

        boolean hasUnreadNotifications = notificationService.hasUnreadNotifications(user.getUserId());

        return NotificationsReadStatusGetResponse.of(hasUnreadNotifications);
    }

    @Transactional(readOnly = true)
    public NotificationsGetResponse getNotifications(User user, Long lastNotificationId, int size) {
        return notificationService.getNotifications(user.getUserId(), lastNotificationId, size);
    }

    @Transactional
    public NotificationGetResponse getNotificationDetail(User user, Long notificationId) {

        Notification notification = notificationService.getNoticeNotification(notificationId);

        notificationService.markAsRead(user.getUserId(), notification.getNotificationId());

        return NotificationGetResponse.of(notification);
    }

    @Transactional
    public void updateNotificationReadStatus(User user, Long notificationId) {

        Long userId = user.getUserId();

        Notification notification = notificationService.getNotification(userId, notificationId);

        notificationService.markAsRead(userId, notification.getNotificationId());
    }

}
