package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Map;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;

public interface FeedCustomRepository {

    Map<Long, Feed> findPopularFeedsByNovelIds(User user, List<Long> novelIds);

    List<Feed> findFeedsByNoOffsetPagination(User owner, Long lastFeedId, int size);
}
