package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.FEED;
import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_ALREADY_READ;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_READ_FORBIDDEN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_TYPE_INVALID;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.dto.notification.NotificationGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationInfo;
import org.websoso.WSSServer.dto.notification.NotificationsGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationsReadStatusGetResponse;
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
    public NotificationsReadStatusGetResponse checkNotificationsReadStatus(User user) {
        Boolean hasUnreadNotifications = notificationRepository.existsUnreadNotifications(
                Set.of(0L, user.getUserId()), user);
        return NotificationsReadStatusGetResponse.of(hasUnreadNotifications);
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

    public NotificationGetResponse getNotification(User user, Long notificationId) {
        Notification notification = getAndValidateNotification(user, notificationId, NOTICE);
        if (!readNotificationRepository.existsByUserAndNotification(user, notification)) {
            readNotificationRepository.save(ReadNotification.create(notification, user));
        }
        return NotificationGetResponse.of(notification);
    }

    public void createNotificationAsRead(User user, Long notificationId) {
        Notification notification = getAndValidateNotification(user, notificationId, FEED);
        checkIfNotificationAlreadyRead(user, notification);
        readNotificationRepository.save(ReadNotification.create(notification, user));
    }

    private Notification getAndValidateNotification(User user, Long notificationId,
                                                    NotificationTypeGroup notificationTypeGroup) {
        Notification notification = getNotificationOrException(notificationId);
        validateNotification(notification.getNotificationType(), notificationTypeGroup);
        validateNotificationRecipient(user.getUserId(), notification.getUserId());
        return notification;
    }

    private Notification getNotificationOrException(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomNotificationException(NOTIFICATION_NOT_FOUND,
                        "notification with the given id is not found"));
    }

    private void validateNotification(NotificationType notificationType, NotificationTypeGroup notificationTypeGroup) {
        if (NotificationTypeGroup.isTypeInGroup(notificationType.getNotificationTypeName(), notificationTypeGroup)) {
            return;
        }
        throw new CustomNotificationException(NOTIFICATION_TYPE_INVALID, "notification type is incorrect");
    }

    private void validateNotificationRecipient(Long userId, Long recipientUserId) {
        if (recipientUserId == 0 || recipientUserId.equals(userId)) {
            return;
        }
        throw new CustomNotificationException(NOTIFICATION_READ_FORBIDDEN,
                "User does not have permission to access this notification.");
    }

    private void checkIfNotificationAlreadyRead(User user, Notification notification) {
        if (readNotificationRepository.existsByUserAndNotification(user, notification)) {
            throw new CustomNotificationException(NOTIFICATION_ALREADY_READ,
                    "notifications that the user has already read");
        }
    }
}
