package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
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
}
