package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.websoso.WSSServer.domain.FeedImage;

public interface FeedImageCustomRepository {
    Optional<FeedImage> findThumbnailFeedImageByFeedId(long feedId);
}
