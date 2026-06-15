package org.websoso.WSSServer.feed.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndFeed(Long userId, Feed feed);

    boolean existsByUserIdAndFeed(Long userId, Feed feed);

    @Query("SELECT l.feed.feedId FROM Like l WHERE l.userId = :userId AND l.feed.feedId IN :feedIds")
    List<Long> findLikedFeedIds(Long userId, List<Long> feedIds);

    @Query("""
            SELECT l.feed.feedId AS feedId, COUNT(l.likeId) AS count
            FROM Like l
            WHERE l.feed.feedId IN :feedIds
            GROUP BY l.feed.feedId
            """)
    List<FeedCountProjection> countByFeedIds(List<Long> feedIds);

    List<Like> findByFeedFeedIdIn(List<Long> feedIds);

    long countByFeed(Feed feed);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.feed.feedId = :feedId")
    void deleteByFeedId(Long feedId);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.userId = :userId AND l.feed = :feed")
    void deleteByUserIdAndFeed(Long userId, Feed feed);

    Long feed(Feed feed);

    long countByFeed_FeedId(Long feedFeedId);
}
