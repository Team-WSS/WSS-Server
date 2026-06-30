package org.websoso.WSSServer.library.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Repository
public interface UserNovelRepository extends JpaRepository<UserNovel, Long>, UserNovelCustomRepository {

    Optional<UserNovel> findByNovelAndUser(Novel novel, User user);

    Integer countByNovelAndStatus(Novel novel, ReadStatus status);

    Integer countByNovelAndIsInterestTrue(Novel novel);

    @Query("SELECT SUM(un.userNovelRating) FROM UserNovel un WHERE un.novel = :novel")
    Float sumUserNovelRatingByNovel(Novel novel);

    Integer countByNovelAndUserNovelRatingNot(Novel novel, float ratingToExclude);

    List<UserNovel> findByUserAndIsInterestTrue(User user);

    List<UserNovel> findUserNovelByUser(User user);

    Optional<UserNovel> findByNovel_NovelIdAndUser(Long novelId, User user);

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO user_novel (
            user_id, novel_id, is_interest, 
            user_novel_rating, status, 
            created_date, modified_date
        ) VALUES (
            :userId, :novelId, true, 
            :defaultRating, 
            :#{#defaultStatus?.name()},
            NOW(), NOW()
        )
        """, nativeQuery = true)
    int insertInterestIfAbsent(
            @Param("userId") Long userId,
            @Param("novelId") Long novelId,
            @Param("defaultRating") Float defaultRating,
            @Param("defaultStatus") ReadStatus defaultStatus
    );

    @Modifying
    @Query(value = """
        UPDATE novel n
        SET
            n.average_rating = CASE
                WHEN n.rating_count + :ratingCountDelta = 0 THEN 0
                ELSE CAST(ROUND(
                    (n.rating_sum + :ratingSumDelta)
                    / (n.rating_count + :ratingCountDelta),
                    3
                ) AS DECIMAL(4, 3))
            END,
            n.rating_sum = n.rating_sum + :ratingSumDelta,
            n.rating_count = n.rating_count + :ratingCountDelta,
            n.popularity = n.popularity + :popularityDelta
        WHERE n.novel_id = :novelId
        """, nativeQuery = true)
    void updateNovelStatisticsByDelta(
            @Param("novelId") Long novelId,
            @Param("ratingSumDelta") BigDecimal ratingSumDelta,
            @Param("ratingCountDelta") Long ratingCountDelta,
            @Param("popularityDelta") Long popularityDelta
    );

}
