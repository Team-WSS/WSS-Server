package org.websoso.WSSServer.library.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;

public interface UserNovelKeywordCustomRepository {

    List<Keyword> findTopKeywordsByCount(Pageable pageable);

    List<Keyword> findKeywordsByUserIdOrderByCountDesc(Long userId);

    void deleteByKeywordsAndUserNovel(Set<Keyword> keywords, UserNovel userNovel);
}
