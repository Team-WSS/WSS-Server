package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.NovelKeywords;

@Repository
public interface NovelKeywordsRepository extends JpaRepository<NovelKeywords, Long> {

    List<NovelKeywords> findByNovelIdAndUserId(Long novelId, Long userId);

}
