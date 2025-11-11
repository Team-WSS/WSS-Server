package org.websoso.WSSServer.library.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;

@Repository
public interface UserNovelKeywordRepository extends JpaRepository<UserNovelKeyword, Long> {

    List<UserNovelKeyword> findAllByUserNovel_Novel(Novel novel);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserNovelKeyword un WHERE un.userNovel = :userNovel AND un.keyword IN :keywords")
    void deleteByKeywordsAndUserNovel(Set<Keyword> keywords, UserNovel userNovel);

}
