package org.websoso.WSSServer.feed.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.event.FeedLikedEvent;
import org.websoso.WSSServer.feed.event.PopularFeedCheckEvent;
import org.websoso.WSSServer.feed.service.FeedLikeService;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class FeedLikeApplication {

    private final FeedServiceImpl feedService;
    private final FeedLikeService feedLikeService;
    private final BlockService blockService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(User user, Long feedId) {

        // 접근 가능한 피드인지 체크하고 조회
        Feed feed = feedService.getAccessFeedOrException(feedId, user.getUserId());

        // 작성자와 본인이 차단 관계인지 체크
        blockService.validateNotBlocked(feed.getWriterId(),user.getUserId());

        // 피드 좋아요 처리 (이미 좋아요 되어있는 경우, 패스)
        if (!feedLikeService.create(user.getUserId(), feed)) {
            return;
        }

        // 좋아요 알림 발행
        eventPublisher.publishEvent(
                FeedLikedEvent.of(
                        user.getUserId(),
                        feed.getFeedId(),
                        feed.getWriterId()
                )
        );

        // 인기 피드 발행
        eventPublisher.publishEvent(
                PopularFeedCheckEvent.of(feed.getFeedId())
        );

    }
}
