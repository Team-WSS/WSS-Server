package org.websoso.WSSServer.repository;

import java.util.List;
import org.websoso.WSSServer.domain.PopularFeed;

public interface PopularFeedCustomRepository {

    List<PopularFeed> findTodayPopularFeeds(Long userId);
}
