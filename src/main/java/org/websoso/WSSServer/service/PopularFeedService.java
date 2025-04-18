package org.websoso.WSSServer.service;

import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.NotificationType;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.PopularFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.notification.FCMService;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.NotificationTypeRepository;
import org.websoso.WSSServer.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;
    private final NovelService novelService;
    private final FCMService fcmService;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;

    public void createPopularFeed(Feed feed) {
        if (!popularFeedRepository.existsByFeed(feed)) {
            popularFeedRepository.save(PopularFeed.create(feed));

            sendPopularFeedPushMessage(feed);
        }
    }

    private void sendPopularFeedPushMessage(Feed feed) {
        NotificationType notificationTypeComment = notificationTypeRepository.findByNotificationTypeName("지금뜨는글");

        User feedOwner = feed.getUser();
        Long feedId = feed.getFeedId();
        String notificationTitle = "지금 뜨는 글 등극\uD83D\uDE4C";
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
                .toList();
        fcmService.sendMulticastPushMessage(
                targetFCMTokens,
                fcmMessageRequest
        );
    }

    private String createNotificationBody(Feed feed) {
        return String.format("내가 남긴 %s 글이 관심 받고 있어요!", generateNotificationBodyFragment(feed));
    }

    private String generateNotificationBodyFragment(Feed feed) {
        if (feed.getNovelId() == null) {
            String feedContent = feed.getFeedContent();
            feedContent = feedContent.length() <= 12
                    ? feedContent
                    : feedContent.substring(0, 12);
            return "'" + feedContent + "...'";
        }
        Novel novel = novelService.getNovelOrException(feed.getNovelId());
        return String.format("<%s>", novel.getTitle());
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user) {
        Long currentUserId = Optional.ofNullable(user)
                .map(User::getUserId)
                .orElse(null);

        List<PopularFeed> popularFeeds = Optional.ofNullable(user)
                .map(u -> findPopularFeedsWithUser(u.getUserId()))
                .orElseGet(this::findPopularFeedsWithoutUser);

        List<PopularFeedGetResponse> popularFeedGetResponses =
                mapToPopularFeedGetResponseList(popularFeeds, currentUserId);

        return new PopularFeedsGetResponse(popularFeedGetResponses);
    }

    private List<PopularFeed> findPopularFeedsWithUser(Long userId) {
        return popularFeedRepository.findTodayPopularFeeds(userId);
    }

    private List<PopularFeed> findPopularFeedsWithoutUser() {
        return popularFeedRepository.findTop9ByOrderByPopularFeedIdDesc();
    }

    private static List<PopularFeedGetResponse> mapToPopularFeedGetResponseList(List<PopularFeed> popularFeeds,
                                                                                Long currentUserId) {
        return popularFeeds.stream()
                .filter(pf -> pf.getFeed().isVisibleTo(currentUserId))
                .map(PopularFeedGetResponse::of)
                .toList();
    }

    private static boolean isVisibleToUser(Feed feed, Long currentUserId) {
        return feed.getIsPublic() || feed.getWriterId().equals(currentUserId);
    }
}
