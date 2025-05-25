package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long>, FeedCustomRepository {

    Integer countByNovelId(Long novelId);

    @Query(value = "SELECT f FROM Feed f WHERE "
            + "(:lastFeedId = 0 OR f.feedId < :lastFeedId) "
            + "AND f.isHidden = false "
            + "AND (:userId IS NULL "
            + "OR f.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = :userId)) "
            + "ORDER BY f.feedId DESC")
    Slice<Feed> findFeeds(Long lastFeedId, Long userId, PageRequest pageRequest);

    List<Feed> findTop10ByNovelIdInOrderByFeedIdDesc(List<Long> novelIds);

    @Query(value = "SELECT f FROM Feed f WHERE "
            + "(:lastFeedId = 0 OR f.feedId < :lastFeedId) "
            + "AND f.novelId = :novelId "
            + "AND f.isHidden = false "
            + "AND (:userId IS NULL "
            + "OR f.user.userId NOT IN (SELECT b.blockedId FROM Block b WHERE b.blockingId = :userId)) "
            + "ORDER BY f.feedId DESC")
    Slice<Feed> findFeedsByNovelId(Long novelId, Long lastFeedId, Long userId, PageRequest pageRequest);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.user.userId = -1 WHERE f.user.userId = :userId")
    void updateUserToUnknown(Long userId);

    List<Feed> findByUserUserIdAndNovelIdAndIsHiddenFalse(Long userId, Long novelId);

    List<Feed> findByUserUserIdAndNovelIdAndIsHiddenFalseAndIsPublicTrueAndIsSpoilerFalse(Long userId, Long novelId);
}
