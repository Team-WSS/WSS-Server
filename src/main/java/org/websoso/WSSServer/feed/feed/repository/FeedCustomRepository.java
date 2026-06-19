package org.websoso.WSSServer.feed.feed.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.SortCriteria;

public interface FeedCustomRepository {

    List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds);

    Slice<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, Boolean isVisible,
                                              Boolean isUnVisible, SortCriteria sortCriteria, List<Genre> genres,
                                              Long visitorId, boolean includeEtc);

    Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Long> blockedUserIds);

    Slice<Feed> findRecommendedFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Genre> genres,
                                     List<Long> blockedUserIds);

    Slice<Feed> findInterestedNovelFeeds(Long lastFeedId, Long userId, PageRequest pageRequest,
                                         List<Long> blockedUserIds);

    Long countVisibleFeeds(User owner, Boolean isVisible,
                           Boolean isUnVisible, List<Genre> genres,
                           Long visitorId, boolean includeEtc);

    Slice<Feed> findFeedsByNovelId(Long novelId, Long lastFeedId, Long userId, PageRequest pageRequest);
}
