package org.websoso.WSSServer.novel.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelPlatform;

@Repository
public interface NovelPlatformRepository extends JpaRepository<NovelPlatform, Long> {

    List<NovelPlatform> findAllByNovel(Novel novel);
}
