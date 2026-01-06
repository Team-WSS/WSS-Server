package org.websoso.WSSServer.feed.repository;

import java.util.Optional;
import org.websoso.WSSServer.feed.domain.FeedImage;

public interface FeedImageCustomRepository {
    Optional<FeedImage> findThumbnailFeedImageByFeedId(long feedId);
}
