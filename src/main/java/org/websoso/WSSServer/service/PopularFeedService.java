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
import org.websoso.WSSServer.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;

    public void createPopularFeed(Feed feed) {
        if (!popularFeedRepository.existsByFeed(feed)) {
            popularFeedRepository.save(PopularFeed.create(feed));
        }
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user) {
        List<PopularFeed> popularFeeds = findPopularFeeds(user);
        List<PopularFeedGetResponse> popularFeedGetResponses = getPopularFeedGetResponses(popularFeeds);
        return getPopularFeedsGetResponse(popularFeedGetResponses);
    }

    private static PopularFeedsGetResponse getPopularFeedsGetResponse(
            List<PopularFeedGetResponse> popularFeedGetResponses) {
        return new PopularFeedsGetResponse(popularFeedGetResponses);
    }

    private static List<PopularFeedGetResponse> getPopularFeedGetResponses(List<PopularFeed> popularFeeds) {
        return popularFeeds.stream()
                .map(popularFeed -> new PopularFeedGetResponse(
                        popularFeed.getFeed().getFeedId(),
                        popularFeed.getFeed().getFeedContent(),
                        popularFeed.getFeed().getLikes().size(),
                        popularFeed.getFeed().getComments().size(),
                        popularFeed.getFeed().getIsSpoiler()
                ))
                .toList();
    }

    private List<PopularFeed> findPopularFeeds(User user) {
        return popularFeedRepository.findTodayPopularFeeds(user.getUserId());
    }
}
