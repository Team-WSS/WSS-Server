package org.websoso.WSSServer.feed.feed.repository;

import org.websoso.WSSServer.feed.feed.repository.projection.FeedInfoRow;
import org.websoso.WSSServer.feed.feed.repository.projection.PopularFeedInfoRow;
import org.websoso.WSSServer.feed.feed.repository.projection.UserFeedInfoRow;

import java.util.List;

public interface FeedQueryRepository {

    List<FeedInfoRow> findFeedInfoRows(List<Long> feedIds, Long userId);

    List<UserFeedInfoRow> findUserFeedInfoRows(List<Long> feedIds, Long visitorId);

    List<PopularFeedInfoRow> findPopularFeedInfoRowsByFeedIds(List<Long> feedIds, Long userId);

}
