package org.websoso.WSSServer.novel.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelGenre;

@Repository
public interface NovelGenreRepository extends JpaRepository<NovelGenre, Long> {

    List<NovelGenre> findAllByNovel(Novel novel);
}
