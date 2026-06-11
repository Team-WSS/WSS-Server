package org.websoso.WSSServer.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.UserDevice;
import org.websoso.WSSServer.notification.service.FcmService;
import org.websoso.WSSServer.notification.service.NotificationService;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

import java.util.List;

import static java.lang.Boolean.TRUE;

@Service
@RequiredArgsConstructor
public class NotificationSendApplication {

    private final NotificationService notificationService;
    private final FeedServiceImpl feedService;
    private final NovelServiceImpl novelService;
    private final BlockService blockService;
    private final UserService userService;
    private final UserDeviceService userDeviceService;
    private final FcmService fcmService;

    // 피드 좋아요 푸시 메세지 전송
    @Transactional
    public void sendFeedLikedPushMessage(Long feedId, Long userId, Long writerId) {
        // 알림 발송자, 대상자가 서로 차단 상태인지 체크
        blockService.validateNotBlocked(userId, writerId);

        Feed feed = feedService.getFeedOrException(feedId);

        User liker = userService.getUserOrException(userId);

        Novel novel;
        if (feed.getNovelId() == null) {
            novel = null;
        } else {
            novel = novelService.getNovelOrException(feed.getNovelId());
        }

        // Notification 엔티티 생성 및 저장
        Notification notification = notificationService.createFeedLikedNotification(feed, novel, liker.getNickname(), writerId);

        User target = userService.getUserOrException(writerId);

        // 알림 대상자의 알림 설정 여부
        if (!TRUE.equals(target.getIsPushEnabled())) {
            return;
        }

        // 알림 대상자에게 등록된 디바이스가 없다면 패스
        List<UserDevice> devices = userDeviceService.findUserDevices(target.getUserId());
        if (devices.isEmpty()) {
            return;
        }

        // FCM 푸시 알림 전송
        fcmService.sendPushFeedNotification(notification, devices);
    }

    // 인기글 등극 푸시 메세지 전송
    @Transactional
    public void sendFeedBecamePopularPushMessage(Long feedId) {

        Feed feed = feedService.getFeedOrException(feedId);

        Novel novel;
        if (feed.getNovelId() == null) {
            novel = null;
        } else {
            novel = novelService.getNovelOrException(feed.getNovelId());
        }

        // Notification 엔티티 저장
        Notification notification = notificationService.createBecamePopularFeedNotification(feed, novel);

        User target = userService.getUserOrException(feed.getWriterId());

        // 알림 대상자의 알림 설정 여부
        if (!TRUE.equals(target.getIsPushEnabled())) {
            return;
        }

        // 알림 대상자에게 등록된 디바이스가 없다면 패스
        List<UserDevice> devices = userDeviceService.findUserDevices(target.getUserId());
        if (devices.isEmpty()) {
            return;
        }

        // FCM 푸시 알림 전송
        fcmService.sendPushFeedNotification(notification, devices);
    }

}
