package org.websoso.WSSServer.feed.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long>, FeedCustomRepository {

    Integer countByNovelId(Long novelId);

    List<Feed> findTop10ByNovelIdInOrderByFeedIdDesc(List<Long> novelIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.user.userId = -1 WHERE f.user.userId = :userId")
    void updateUserToUnknown(Long userId);

    List<Feed> findByUserUserIdAndIsHiddenFalseAndNovelIdIn(Long userId, List<Long> novelIds);

    List<Feed> findByUserUserIdAndIsHiddenFalseAndNovelIdInAndIsPublicTrueAndIsSpoilerFalse(Long userId,
                                                                                            List<Long> novelIds);
}
