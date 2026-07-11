package org.websoso.WSSServer.notification.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.policy.CommenterCommentNotificationPolicy;
import org.websoso.WSSServer.notification.policy.FeedOwnerCommentNotificationPolicy;
import org.websoso.WSSServer.notification.service.NotificationService;
import org.websoso.WSSServer.notification.service.PushNotificationService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class NotificationSendApplication {

    private final NotificationService notificationService;
    private final FeedServiceImpl feedService;
    private final NovelServiceImpl novelService;
    private final BlockService blockService;
    private final UserService userService;
    private final PushNotificationService pushNotificationService;
    private final FeedOwnerCommentNotificationPolicy feedOwnerCommentNotificationPolicy;
    private final CommenterCommentNotificationPolicy commenterCommentNotificationPolicy;

    // 피드 좋아요 푸시 메세지 전송
    @Transactional
    public void sendFeedLikedPushMessage(Long feedId, Long userId, Long writerId) {
        // 알림 발송자, 대상자가 서로 차단 상태인지 체크
        blockService.validateNotBlocked(userId, writerId);

        Feed feed = feedService.getFeedOrException(feedId);

        User liker = userService.getUserOrException(userId);

        Novel novel = novelService.findOptionalNovel(feed.getNovelId()).orElse(null);

        // Notification 엔티티 생성 및 저장
        Notification notification = notificationService.createFeedLikedNotification(feed, novel, liker.getNickname(), writerId);

        pushNotificationService.sendIfEnabled(writerId, notification);
    }

    // 인기글 등극 푸시 메세지 전송
    @Transactional
    public void sendFeedBecamePopularPushMessage(Long feedId) {

        Feed feed = feedService.getFeedOrException(feedId);

        Novel novel = novelService.findOptionalNovel(feed.getNovelId()).orElse(null);

        // Notification 엔티티 저장
        Notification notification = notificationService.createBecamePopularFeedNotification(feed, novel);

        pushNotificationService.sendIfEnabled(feed.getWriterId(), notification);
    }

    // 댓글 작성 시에 피드 작성자에게 알림 전송
    @Transactional
    public void sendFeedOwnerCommentPushMessage(Long userId, Long feedId) {

        Feed feed = feedService.getFeedOrException(feedId);

        User commentWriter = userService.getUserOrException(userId);

        Novel novel = novelService.findOptionalNovel(feed.getNovelId()).orElse(null);

        feedOwnerCommentNotificationPolicy.resolveRecipient(commentWriter, feed)
                .ifPresent(recipientId -> {

                    Notification notification = notificationService.createFeedOwnerCommentNotification(
                            feed,
                            novel,
                            commentWriter.getNickname(),
                            recipientId
                    );

                    pushNotificationService.sendIfEnabled(recipientId, notification);
                });
    }

    // 댓글을 작성한 피드에 다른 댓글이 작성되었을 때 알림 발송
    @Transactional
    public void sendCommenterCommentPushMessage(Long userId, Long feedId) {
        Feed feed = feedService.getFeedOrException(feedId);
        User commentWriter = userService.getUserOrException(userId);

        List<Long> recipientIds = commenterCommentNotificationPolicy.resolveRecipients(commentWriter, feed);
        if (recipientIds.isEmpty()) {
            return;
        }

        Novel novel = novelService.findOptionalNovel(feed.getNovelId()).orElse(null);
        recipientIds.forEach(recipientId -> {
            Notification notification = notificationService.createCommenterCommentNotification(
                    feed,
                    novel,
                    recipientId
            );

            pushNotificationService.sendIfEnabled(recipientId, notification);
        });
    }


}
