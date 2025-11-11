package org.websoso.WSSServer.feed.repository;

import java.util.List;
import org.websoso.WSSServer.feed.domain.PopularFeed;

public interface PopularFeedCustomRepository {

    List<PopularFeed> findTodayPopularFeeds(Long userId);
}
