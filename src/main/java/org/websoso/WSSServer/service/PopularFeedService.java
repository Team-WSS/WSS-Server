package org.websoso.WSSServer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.PopularFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.notification.FCMService;
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
        }
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
