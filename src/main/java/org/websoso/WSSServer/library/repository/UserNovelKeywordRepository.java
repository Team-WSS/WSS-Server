package org.websoso.WSSServer.library.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovelKeyword;

@Repository
public interface UserNovelKeywordRepository extends JpaRepository<UserNovelKeyword, Long>,
        UserNovelKeywordCustomRepository {

    List<UserNovelKeyword> findAllByUserNovel_Novel(Novel novel);
}
