package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelKeyword;

@Repository
public interface NovelKeywordRepository extends JpaRepository<NovelKeyword, Long> {

    List<NovelKeyword> findAllByNovelAndUserId(Novel novel, Long userId);

    List<NovelKeyword> findAllByNovel(Novel novel);

}
