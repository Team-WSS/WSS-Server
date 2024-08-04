package org.websoso.WSSServer.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Integer countByNovelId(Long novelId);

    @Query(value = "SELECT f FROM Feed f WHERE "
            + "(:lastFeedId = 0 OR f.feedId < :lastFeedId) "
            + "AND f.isHidden = false "
            + "AND (:userId IS NULL "
            + "OR (f.user.userId NOT IN (SELECT b.blockingId FROM Block b WHERE b.blockedId = :userId)) "
            + "AND f.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = :userId)) "
            + "ORDER BY f.feedId DESC")
    Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest);

}
