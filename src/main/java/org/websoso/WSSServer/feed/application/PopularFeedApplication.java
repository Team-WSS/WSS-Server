package org.websoso.WSSServer.feed.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.event.FeedBecamePopularEvent;
import org.websoso.WSSServer.feed.service.FeedLikeService;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.service.PopularFeedService;

@Service
@RequiredArgsConstructor
public class PopularFeedApplication {

    private static final int POPULAR_FEED_LIKE_THRESHOLD = 5;

    private final PopularFeedService popularFeedService;
    private final FeedServiceImpl feedService;
    private final FeedLikeService feedLikeService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void checkAndRegister(Long feedId) {

        Feed feed = feedService.getFeedOrException(feedId);

        if (!feed.containsNovel()) return;

        long likeCount = feedLikeService.countByFeedId(feedId);

        if (likeCount != POPULAR_FEED_LIKE_THRESHOLD) return;

        if (popularFeedService.existByFeed(feed)) return;

        popularFeedService.create(feed);

        // 지금 뜨는 글 선정 알림 전송
        eventPublisher.publishEvent(FeedBecamePopularEvent.of(feedId));

    }
}
