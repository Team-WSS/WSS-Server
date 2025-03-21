package org.websoso.WSSServer.service;

import static java.lang.Boolean.TRUE;
import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.FEED;
import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;
import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_ADMIN_ONLY;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_ALREADY_READ;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_NOTICE_TYPE;
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
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.Novel;
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
import org.websoso.WSSServer.notification.FCMService;
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
    private final FCMService fcmService;
    private final UserService userService;
    private final NovelService novelService;
    private final BlockService blockService;
    private final NotificationRepository notificationRepository;
    private final ReadNotificationRepository readNotificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final UserRepository userRepository;

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

    public void createNoticeNotification(User user, NotificationCreateRequest request) {
        validateAdminPrivilege(user);
        validateNoticeType(request.notificationTypeName());

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
    
    public void sendLikePushMessage(User liker, Feed feed) {
        User feedOwner = feed.getUser();
        if (liker.equals(feedOwner)) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("좋아요");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 수다글을 좋아해요.", liker.getNickname());
        Long feedId = feed.getFeedId();

        Notification notification = Notification.create(
                notificationTitle,
                notificationBody,
                null,
                feedOwner.getUserId(),
                feedId,
                notificationTypeComment
        );
        notificationRepository.save(notification);

        if (!TRUE.equals(feedOwner.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> feedOwnerDevices = feedOwner.getUserDevices();
        if (feedOwnerDevices.isEmpty()) {
            return;
        }

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notificationTitle,
                notificationBody,
                String.valueOf(feedId),
                "feedDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = feedOwnerDevices
                .stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();
        fcmService.sendMulticastPushMessage(
                targetFCMTokens,
                fcmMessageRequest
        );
    }

    public void sendCommentPushMessageToFeedOwner(User user, Feed feed) {
        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");
        User feedOwner = feed.getUser();

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = String.format("%s님이 내 수다글에 댓글을 남겼어요.", user.getNickname());
        Long feedId = feed.getFeedId();

        Notification notification = Notification.create(
                notificationTitle,
                notificationBody,
                null,
                feedOwner.getUserId(),
                feedId,
                notificationTypeComment
        );
        notificationRepository.save(notification);

        if (!TRUE.equals(feedOwner.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> feedOwnerDevices = feedOwner.getUserDevices();
        if (feedOwnerDevices.isEmpty()) {
            return;
        }

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notificationTitle,
                notificationBody,
                String.valueOf(feedId),
                "feedDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = feedOwnerDevices
                .stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();

        fcmService.sendMulticastPushMessage(
                targetFCMTokens,
                fcmMessageRequest
        );
    }

    public void sendCommentPushMessageToCommenters(User user, Feed feed) {
        User feedOwner = feed.getUser();
        List<User> commenters = feed.getComments()
                .stream()
                .map(Comment::getUserId)
                .filter(userId -> !userId.equals(user.getUserId()))
                .filter(userId -> !userId.equals(feedOwner.getUserId()))
                .filter(userId -> !blockService.isBlocked(userId, user.getUserId())
                        && !blockService.isBlocked(userId, feed.getUser().getUserId()))
                .distinct()
                .map(userService::getUserOrException)
                .toList();

        if (commenters.isEmpty()) {
            return;
        }

        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("댓글");

        String notificationTitle = createNotificationTitle(feed);
        String notificationBody = "내가 댓글 단 수다글에 또 다른 댓글이 달렸어요.";
        Long feedId = feed.getFeedId();

        commenters.forEach(commenter -> {
            Notification notification = Notification.create(
                    notificationTitle,
                    notificationBody,
                    null,
                    commenter.getUserId(),
                    feedId,
                    notificationTypeComment
            );
            notificationRepository.save(notification);

            if (!TRUE.equals(commenter.getIsPushEnabled())) {
                return;
            }

            List<UserDevice> commenterDevices = commenter.getUserDevices();
            if (commenterDevices.isEmpty()) {
                return;
            }

            List<String> targetFCMTokens = commenterDevices
                    .stream()
                    .map(UserDevice::getFcmToken)
                    .distinct()
                    .toList();

            FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                    notificationTitle,
                    notificationBody,
                    String.valueOf(feedId),
                    "feedDetail",
                    String.valueOf(notification.getNotificationId())
            );
            fcmService.sendMulticastPushMessage(
                    targetFCMTokens,
                    fcmMessageRequest
            );
        });
    }

    public void sendPopularFeedPushMessage(Feed feed) {
        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("지금뜨는수다글");

        User feedOwner = feed.getUser();
        Long feedId = feed.getFeedId();
        String notificationTitle = "지금 뜨는 수다글 등극\uD83D\uDE4C";
        String notificationBody = createNotificationBody(feed);

        Notification notification = Notification.create(
                notificationTitle,
                notificationBody,
                null,
                feedOwner.getUserId(),
                feedId,
                notificationTypeComment
        );
        notificationRepository.save(notification);

        if (!TRUE.equals(feedOwner.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> feedOwnerDevices = feedOwner.getUserDevices();
        if (feedOwnerDevices.isEmpty()) {
            return;
        }

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notificationTitle,
                notificationBody,
                String.valueOf(feedId),
                "feedDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = feedOwnerDevices
                .stream()
                .map(UserDevice::getFcmToken)
                .distinct()
                .toList();
        fcmService.sendMulticastPushMessage(
                targetFCMTokens,
                fcmMessageRequest
        );
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

    private void validateAdminPrivilege(User user) {
        if (user.getRole() != ADMIN) {
            throw new CustomNotificationException(NOTIFICATION_ADMIN_ONLY,
                    "User who tried to create, modify, or delete the notice is not an ADMIN.");
        }
    }

    private void validateNoticeType(String notificationTypeName) {
        if (!NotificationTypeGroup.isTypeInGroup(notificationTypeName, NOTICE)) {
            throw new CustomNotificationException(NOTIFICATION_NOT_NOTICE_TYPE,
                    "given notification type does not belong to the NOTICE category");
        }
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

        fcmService.sendMulticastPushMessage(targetFCMTokens, fcmMessageRequest);
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

    private String createNotificationBody(Feed feed) {
        return String.format("내가 남긴 %s 글이 관심 받고 있어요!", generateNotificationBodyFragment(feed));
    }

    private String generateNotificationBodyFragment(Feed feed) {
        if (feed.getNovelId() == null) {
            return truncateFeedContent(feed);
        }
        Novel novel = novelService.getNovelOrException(feed.getNovelId());
        return String.format("<%s>", novel.getTitle());
    }

    private String createNotificationTitle(Feed feed) {
        if (feed.getNovelId() == null) {
            return truncateFeedContent(feed);
        }
        Novel novel = novelService.getNovelOrException(feed.getNovelId());
        return novel.getTitle();
    }

    private String truncateFeedContent(Feed feed) {
        String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= 12
                    ? feedContent
                    : feedContent.substring(0, 12);
            return "'" + feedContent + "...'";
    }
}
