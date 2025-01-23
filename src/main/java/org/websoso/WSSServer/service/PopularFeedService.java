package org.websoso.WSSServer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.PopularFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.notification.FCMService;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;
    private final NovelService novelService;
    private final FCMService fcmService;

    public void createPopularFeed(Feed feed) {
        if (!popularFeedRepository.existsByFeed(feed)) {
            popularFeedRepository.save(PopularFeed.create(feed));

            sendPopularFeedPushMessage(feed);
        }
    }

    private void sendPopularFeedPushMessage(Feed feed) {
        Long feedId = feed.getFeedId();
        String notificationTitle = "지금 뜨는 수다글 등극\uD83D\uDE4C";
        String notificationBody = createNotificationBody(feed);
        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notificationTitle,
                notificationBody,
                String.valueOf(feedId),
        );

        List<String> targetFCMTokens = feed.getUser()
                .getUserDevices()
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
            return feedContent.length() <= 12
                    ? feedContent
                    : "'" + feedContent.substring(0, 12) + "...'";
        }
        Novel novel = novelService.getNovelOrException(feed.getNovelId());
        return String.format("<%s>", novel.getTitle());
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user) {
        List<PopularFeed> popularFeeds = findPopularFeeds(user);
        List<PopularFeedGetResponse> popularFeedGetResponses = mapToPopularFeedGetResponseList(popularFeeds);
        return new PopularFeedsGetResponse(popularFeedGetResponses);
    }

    private List<PopularFeed> findPopularFeeds(User user) {
        if (user == null) {
            return popularFeedRepository.findTop9ByOrderByPopularFeedIdDesc();
        }
        return popularFeedRepository.findTodayPopularFeeds(user.getUserId());
    }

    private static List<PopularFeedGetResponse> mapToPopularFeedGetResponseList(List<PopularFeed> popularFeeds) {
        return popularFeeds.stream()
                .map(PopularFeedGetResponse::of)
                .toList();
    }
}
