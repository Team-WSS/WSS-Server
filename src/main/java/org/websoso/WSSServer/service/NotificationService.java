package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.FEED;
import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_ALREADY_READ;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_READ_FORBIDDEN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_TYPE_INVALID;
import static org.websoso.WSSServer.exception.error.CustomNotificationTypeError.NOTIFICATION_TYPE_NOT_FOUND;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.dto.notification.NotificationCreateRequest;
import org.websoso.WSSServer.dto.notification.NotificationGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationInfo;
import org.websoso.WSSServer.dto.notification.NotificationsGetResponse;
import org.websoso.WSSServer.dto.notification.NotificationsReadStatusGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.exception.exception.CustomNotificationTypeException;
import org.websoso.WSSServer.notification.FCMClient;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.repository.ReadNotificationRepository;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private final NotificationRepository notificationRepository;
    private final ReadNotificationRepository readNotificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final UserRepository userRepository;
    private final FCMClient fcmClient;
    private final UserService userService;

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

    public void createNoticeNotification(User user, NotificationCreateRequest request) {
        Notification notification = notificationRepository.save(Notification.create(
                request.notificationTitle(),
                request.notificationBody(),
                request.notificationDetail(),
                request.userId(),
                null,
                getNotificationTypeOrException(request.notificationTypeName()))
        );

        sendNoticePushMessage(request.userId(), notification);
    }

    private NotificationType getNotificationTypeOrException(String notificationTypeName) {
        return notificationTypeRepository
                .findOptionalByNotificationTypeName(notificationTypeName)
                .orElseThrow(() -> new CustomNotificationTypeException(NOTIFICATION_TYPE_NOT_FOUND,
                        "notification type with the given name is not found"));
    }

    private void sendNoticePushMessage(Long userId, Notification notification) {
        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notification.getNotificationTitle(),
                notification.getNotificationBody(),
                "",
                "notificationDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = getTargetFCMTokens(userId);

        fcmClient.sendMulticastPushMessage(targetFCMTokens, fcmMessageRequest);
    }

    private List<String> getTargetFCMTokens(Long userId) {
        if (userId.equals(0L)) {
            return userRepository.findAllByIsPushEnabledTrue()
                    .stream()
                    .flatMap(user -> user.getUserDevices().stream())
                    .map(UserDevice::getFcmToken)
                    .distinct()
                    .toList();
        }
        return userService.getUserOrException(userId)
                .getUserDevices()
                .stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();
    }
}
