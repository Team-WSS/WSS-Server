package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    @Query("SELECT n FROM Novel n JOIN UserNovel un ON n.novelId = un.novel.novelId " +
            "GROUP BY n.novelId " +
            "ORDER BY MAX(un.createdDate) DESC")
    Page<Novel> findSosoPick(Pageable pageable);

    @Query("SELECT n FROM Novel n " +
            "JOIN n.novelGenres ng " +
            "WHERE (:genres IS NULL OR ng.genre IN :genres) " +//TODO genres 리스트 여러개일때 오류
            "AND (:isCompleted IS NULL OR n.isCompleted = :isCompleted) " +
            "AND (:novelRating IS NULL OR " +
            "(SELECT AVG(un.userNovelRating) FROM UserNovel un WHERE un.novel = n AND un.userNovelRating <> 0) >= :novelRating) " +//TODO 반올림 확인
            "AND (:keywords IS NULL OR " +
            "(SELECT COUNT(unk.keyword) FROM UserNovelKeyword unk WHERE unk.userNovel.novel = n AND unk.keyword IN :keywords GROUP BY unk.userNovel.novel) = :keywordsSize) " +
            "ORDER BY (SELECT COUNT(un) FROM UserNovel un WHERE un.novel = n AND (un.isInterest = true OR un.status <> 'QUIT')) DESC")//TODO
    Page<Novel> findFilteredNovels(Pageable pageable, List<Genre> genres, Boolean isCompleted, Float novelRating,
                                   List<Keyword> keywords, Integer keywordsSize);

}
