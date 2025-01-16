package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_READ_FORBIDDEN;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.notification.NotificationGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationInfo;
import org.websoso.WSSServer.dto.notification.NotificationsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.ReadNotificationRepository;

@Service
@AllArgsConstructor
@Transactional
public class NotificationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private final NotificationRepository notificationRepository;
    private final ReadNotificationRepository readNotificationRepository;

    @Transactional(readOnly = true)
    public Notification getNotificationOrException(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomNotificationException(NOTIFICATION_NOT_FOUND,
                        "notification with the given id is not found"));
    }

    private void validateNotificationRecipient(User user, Notification notification) {
        Long userId = user.getUserId();
        Long notificationUserId = notification.getUserId();
        if (notificationUserId == 0 || notificationUserId.equals(userId)) {
            return;
        }
        throw new CustomNotificationException(NOTIFICATION_READ_FORBIDDEN,
                "User does not have permission to access this notification.");
    }

    @Transactional(readOnly = true)
    public NotificationsGetResponse getNotifications(Long lastNotificationId, int size, User user) {
        Slice<Notification> notifications = notificationRepository.findNotifications(lastNotificationId,
                user.getUserId(), PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        Set<Notification> readNotifications = readNotificationRepository.findAllByUser(user).stream()
                .map(ReadNotification::getNotification)
                .collect(Collectors.toSet());

        List<NotificationInfo> notificationInfos = notifications.getContent().stream()
                .map(n -> NotificationInfo.of(n, readNotifications.contains(n)))
                .toList();

        return NotificationsGetResponse.of(notifications.hasNext(), notificationInfos);
    }

    @Transactional(readOnly = true)
    public NotificationGetResponse getNotification(User user, Long notificationId) {
        Notification notification = getNotificationOrException(notificationId);
        validateNotificationRecipient(user, notification);
        if (!readNotificationRepository.existsByUserAndNotification(user, notification)) {
            ReadNotification.create(notification, user);
        }
        return NotificationGetResponse.of(notification);
    }
}
