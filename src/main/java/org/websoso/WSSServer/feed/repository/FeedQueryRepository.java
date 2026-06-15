package org.websoso.WSSServer.feed.repository;

import java.util.List;

public interface FeedQueryRepository {

    List<FeedInfoRow> findFeedInfoRows(List<Long> feedIds, Long userId);

    List<UserFeedInfoRow> findUserFeedInfoRows(List<Long> feedIds, Long visitorId);

    List<PopularFeedInfoRow> findPopularFeedInfoRows(List<Long> blockedUserIds, int size);

}
