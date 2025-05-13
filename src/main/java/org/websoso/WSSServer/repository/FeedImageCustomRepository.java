package org.websoso.WSSServer.repository;

import org.websoso.WSSServer.domain.FeedImage;

public interface FeedImageCustomRepository {
    FeedImage findThumbnailFeedImagwByFeedId(long feedId);
}
