package org.websoso.WSSServer.repository;

import java.util.List;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.SortCriteria;

public interface FeedCustomRepository {

    List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds);

    List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size, boolean isVisible,
                                             boolean isUnVisible, SortCriteria sortCriteria);
}
