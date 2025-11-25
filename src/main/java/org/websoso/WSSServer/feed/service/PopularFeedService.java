package org.websoso.WSSServer.feed.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user) {
        Long currentUserId = Optional.ofNullable(user).map(User::getUserId).orElse(null);

        List<PopularFeed> popularFeeds = Optional.ofNullable(user).map(u -> findPopularFeedsWithUser(u.getUserId()))
                .orElseGet(this::findPopularFeedsWithoutUser);

        List<PopularFeedGetResponse> popularFeedGetResponses = mapToPopularFeedGetResponseList(popularFeeds,
                currentUserId);

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
        return popularFeeds.stream().filter(pf -> pf.getFeed().isVisibleTo(currentUserId))
                .map(PopularFeedGetResponse::of).toList();
    }
}
