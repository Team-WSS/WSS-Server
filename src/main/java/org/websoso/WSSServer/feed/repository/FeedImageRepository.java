package org.websoso.WSSServer.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.feed.domain.FeedImage;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {
    Integer countByFeedId(Long feedId);
}
