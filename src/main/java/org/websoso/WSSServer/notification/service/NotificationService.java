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
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.notification.controller.response.NotificationPageResponse;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.NotificationType;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.notification.repository.NotificationRepository;
import org.websoso.WSSServer.notification.repository.NotificationTypeRepository;
import org.websoso.WSSServer.notification.repository.ReadNotificationRepository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int NOTIFICATION_TITLE_MAX_LENGTH = 12;
    private static final int NOTIFICATION_TITLE_MIN_LENGTH = 0;

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final ReadNotificationRepository readNotificationRepository;

    /**
     * 알림을 생성 및 저장한다.
     *
     * @param feed 피드
     * @param novel 피드에 속한 소설 (없다면 null)
     * @param nickname 좋아요를 누른 사용자 닉네임
     * @param writerId 피드 작성자 ID
     */
    @Transactional
    public Notification createFeedLikedNotification(Feed feed, Novel novel, String nickname, Long writerId) {

        NotificationType notificationType = notificationTypeRepository.findByNotificationTypeName("좋아요");

        String notificationTitle = createNotificationTitle(feed, novel);

        String notificationBody = String.format("%s님이 내 글을 좋아해요.", nickname);

        Long feedId = feed.getFeedId();

        Notification notification = Notification.createFeedNotification(
                notificationTitle,
                notificationBody,
                writerId,
                feedId,
                notificationType
        );

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createBecamePopularFeedNotification(Feed feed, Novel novel) {

        NotificationType notificationType = notificationTypeRepository.findByNotificationTypeName("지금뜨는글");

        User feedOwner = feed.getUser();

        Long feedId = feed.getFeedId();

        String notificationTitle = "지금 뜨는 글 등극\uD83D\uDE4C";

        String notificationBody = createNotificationBody(feed, novel);

        Notification notification = Notification.createFeedNotification(
                notificationTitle,
                notificationBody,
                feedOwner.getUserId(),
                feedId,
                notificationType
        );

        return notificationRepository.save(notification);
    }

    /**
     * 읽지 않은 알림이 존재하는지 확인한다.
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 여부
     */
    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(Long userId) {
        return notificationRepository.existsUnreadNotifications(userId);
    }

    /**
     * 알림 목록을 조회한다.
     *
     * @param userId             사용자 ID
     * @param lastNotificationId 마지막으로 조회한 알림 ID
     * @param size               조회 사이즈
     * @return 알림 목록 (읽기 여부 포함)
     */
    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Long userId, Long lastNotificationId, int size) {

        Slice<ReadNotificationDto> sliceResult = notificationRepository.findNotifications(
                lastNotificationId,
                userId,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size)
        );

        return NotificationPageResponse.from(sliceResult);
    }

    /**
     * 알림 객체를 조회한다.
     *
     * @param userId         사용자 ID
     * @param notificationId 알림 ID
     * @return 알림 객체
     */
    @Transactional(readOnly = true)
    public Notification getNotification(Long userId, Long notificationId) {
        Notification notification = getNotificationOrException(notificationId);

        validateNotificationRecipient(userId, notification.getUserId());

        return notification;
    }

    /**
     * 상세 내역이 존재하는 공지 알림을 조회한다.
     *
     * @param notificationId 알림 ID
     * @return 알림 객체
     */
    @Transactional(readOnly = true)
    public Notification getNoticeNotification(Long notificationId) {
        Notification notification = getNotificationOrException(notificationId);

        validateNotificationType(notification.getNotificationType(), NOTICE);

        return notification;
    }

    /**
     * 알림을 읽기 상태로 전환한다.
     *
     * @param userId         사용자 ID
     * @param notificationId 알림 ID
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        readNotificationRepository.insertIgnoreReadNotification(
                notificationId,
                userId
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

    private String createNotificationTitle(Feed feed, Novel novel) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= NOTIFICATION_TITLE_MAX_LENGTH ? feedContent
                    : feedContent.substring(NOTIFICATION_TITLE_MIN_LENGTH, NOTIFICATION_TITLE_MAX_LENGTH);
            return "'" + feedContent + "...'";
        }

        return novel.getTitle();
    }

    private String createNotificationBody(Feed feed, Novel novel) {
        return String.format("내가 남긴 %s 글이 관심 받고 있어요!", generateNotificationBodyFragment(feed, novel));
    }

    private String generateNotificationBodyFragment(Feed feed, Novel novel) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= 12
                    ? feedContent
                    : feedContent.substring(0, 12);
            return "'" + feedContent + "...'";
        }

        return String.format("<%s>", novel.getTitle());
    }

}
