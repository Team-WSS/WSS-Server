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
            + "(?1 = 0 OR f.feedId < ?1) "
            + "AND f.isHidden = false "
            + "AND f.user.userId NOT IN (SELECT b.blockingId FROM Block b WHERE b.blockedId = ?2) "
            + "AND f.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = ?2) "
            + "ORDER BY f.feedId DESC")
    Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest);

}
