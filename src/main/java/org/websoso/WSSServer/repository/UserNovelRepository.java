package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.ReadStatus;

@Repository
public interface UserNovelRepository extends JpaRepository<UserNovel, Long> {

    Optional<UserNovel> findByNovelAndUser(Novel novel, User user);

    Integer countByNovelAndStatus(Novel novel, ReadStatus status);

    Integer countByNovelAndIsInterestTrue(Novel novel);

    @Query("SELECT SUM(un.userNovelRating) FROM UserNovel un WHERE un.novel = :novel")
    Float sumUserNovelRatingByNovel(Novel novel);

    Integer countByNovelAndUserNovelRatingNot(Novel novel, float ratingToExclude);

    //TODO QueryDSL로 수정하기
    @Query(value = "SELECT un.novel.novelId "
            + "FROM UserNovel un "
            + "WHERE (un.status = 'WATCHING' OR un.status = 'WATCHED' OR un.isInterest = true) "
            + "GROUP BY un.novel.novelId "
            + "ORDER BY COUNT(un) DESC")
    List<Long> findTodayPopularNovelsId(Pageable pageable);

}
