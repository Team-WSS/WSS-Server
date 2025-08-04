package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.SortCriteria;

public interface FeedCustomRepository {

    List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds);

    List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, Boolean isVisible,
                                             Boolean isUnVisible, SortCriteria sortCriteria, List<Genre> genres,
                                             Long visitorId);

    Slice<Feed> findRecommendedFeeds(Long lastFeedId, Long userId, PageRequest pageRequest, List<Genre> genres);

    Long countVisibleFeeds(User owner, Long lastFeedId, Boolean isVisible,
                           Boolean isUnVisible, List<Genre> genres,
                           Long visitorId);
}
