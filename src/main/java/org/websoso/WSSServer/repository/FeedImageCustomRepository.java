package org.websoso.WSSServer.repository;

public interface FeedImageCustomRepository {
    FeedImageSummary findFeedThumbnailAndImageCountByFeedId(long feedId);
}
