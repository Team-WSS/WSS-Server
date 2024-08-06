package org.websoso.WSSServer.repository;

import java.util.List;
import org.websoso.WSSServer.domain.Feed;

public interface FeedCustomRepository {

    List<Feed> findPopularFeedsByNovelIds(List<Long> novelIds);
}
