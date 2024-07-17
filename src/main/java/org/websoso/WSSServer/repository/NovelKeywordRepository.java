package org.websoso.WSSServer.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelKeyword;

@Repository
public interface NovelKeywordRepository extends JpaRepository<NovelKeyword, Long> {

    List<NovelKeyword> findAllByNovelAndUserId(Novel novel, Long userId);

    List<NovelKeyword> findAllByNovel(Novel novel);

    @Modifying
    @Transactional
    @Query("DELETE FROM NovelKeyword nk WHERE nk.keyword IN :keywords AND nk.novel = :novel AND nk.userId = :userId")
    void deleteByKeywordsAndNovelAAndUserId(Set<Keyword> keywords, Novel novel, Long userId);

}
