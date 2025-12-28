package org.websoso.WSSServer.library.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Modifying(clearAutomatically = true)
    @Query(value = """
        INSERT INTO user_novel (
            user_id, novel_id, is_interest, 
            user_novel_rating, status, 
            created_date, modified_date
        ) VALUES (
            :userId, :novelId, true, 
            :defaultRating, 
            :#{#defaultStatus?.name()},
            NOW(), NOW()
        )
        ON DUPLICATE KEY UPDATE
            is_interest = true,
            modified_date = NOW()
        """, nativeQuery = true)
    void upsertInterest(
            @Param("userId") Long userId,
            @Param("novelId") Long novelId,
            @Param("defaultRating") Float defaultRating,
            @Param("defaultStatus") ReadStatus defaultStatus
    );

}
