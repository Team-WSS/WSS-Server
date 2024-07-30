package org.websoso.WSSServer.repository;

import java.util.List;
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

    List<FeedCategory> findByFeed(Feed feed);

    void deleteByCategory(Category category);

    @Query(value = "SELECT fc.feed FROM FeedCategory fc "
            + "WHERE fc.category = :category "
            + "AND (:lastFeedId = 0 OR fc.feed.feedId < :lastFeedId) "
            + "AND fc.feed.isHidden = false "
            + "AND (:lastFeedId IS NULL "
            + "OR (fc.feed.user.userId NOT IN (SELECT b.blockingId FROM Block b WHERE b.blockedId = :userId)) "
            + "AND fc.feed.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = :userId)) "
            + "ORDER BY fc.feed.feedId DESC")
    Slice<Feed> findFeedsByCategory(Category category, Long lastFeedId, Long userId, PageRequest pageRequest);

}
