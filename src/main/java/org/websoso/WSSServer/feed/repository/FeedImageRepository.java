package org.websoso.WSSServer.feed.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.feed.domain.FeedImage;

public interface FeedImageRepository extends JpaRepository<FeedImage, Long>, FeedImageCustomRepository {
    Integer countByFeedId(Long feedId);

    List<FeedImage> findByFeedIdIn(List<Long> feedIds);
}
