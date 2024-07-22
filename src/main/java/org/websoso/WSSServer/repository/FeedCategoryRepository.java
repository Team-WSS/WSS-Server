package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Category;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.FeedCategory;

@Repository
public interface FeedCategoryRepository extends JpaRepository<FeedCategory, Long> {

    Optional<List<FeedCategory>> findByFeed(Feed feed);

    void deleteByCategory(Category category);

    @Query(value = "SELECT fc.feed FROM FeedCategory fc "
            + "WHERE fc.category = ?1 "
            + "AND (?2 = 0 OR fc.feed.feedId < ?2) "
            + "AND fc.feed.isHidden = false "
            + "AND (?2 IS NULL "
            + "OR (fc.feed.user.userId NOT IN (SELECT b.blockingId FROM Block b WHERE b.blockedId = ?2)) "
            + "AND fc.feed.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = ?2)) "
            + "ORDER BY fc.feed.feedId DESC")
    Slice<Feed> findFeedsByCategory(Category category, Long lastFeedId, Long userId, PageRequest pageRequest);

}
