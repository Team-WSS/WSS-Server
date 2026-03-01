package org.websoso.WSSServer.notification.service;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_READ_FORBIDDEN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_TYPE_INVALID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.NotificationType;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.notification.dto.NotificationsGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.notification.repository.NotificationRepository;
import org.websoso.WSSServer.notification.repository.ReadNotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;

    private final NotificationRepository notificationRepository;
    private final ReadNotificationRepository readNotificationRepository;

    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(User user) {
        return notificationRepository.existsUnreadNotifications(user.getUserId());
    }

    @Transactional(readOnly = true)
    public NotificationsGetResponse getNotifications(User user, Long lastNotificationId, int size) {

        Slice<ReadNotificationDto> sliceResult = notificationRepository.findNotifications(
                lastNotificationId,
                user.getUserId(),
                PageRequest.of(DEFAULT_PAGE_NUMBER, size)
        );

        return NotificationsGetResponse.from(sliceResult);
    }

    @Transactional(readOnly = true)
    public Notification getNotification(User user, Long notificationId) {
        Notification notification = getNotificationOrException(notificationId);

        validateNotificationRecipient(user.getUserId(), notification.getUserId());

        return notification;
    }

    @Transactional(readOnly = true)
    public Notification getNoticeNotification(User user, Long notificationId) {
        Notification notification = getNotificationOrException(notificationId);

        validateNotificationType(notification.getNotificationType(), NOTICE);

        return notification;
    }

    @Transactional
    public void markAsRead(User user, Notification notification) {
        readNotificationRepository.insertIgnoreReadNotification(
                notification.getNotificationId(),
                user.getUserId()
        );
    }

    private Notification getNotificationOrException(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomNotificationException(NOTIFICATION_NOT_FOUND,
                        "notification with the given id is not found"));
    }

    private void validateNotificationType(NotificationType notificationType, NotificationTypeGroup notificationTypeGroup) {
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

}
