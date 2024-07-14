package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelPlatform;

@Repository
public interface NovelPlatformRepository extends JpaRepository<NovelPlatform, Long> {

    List<NovelPlatform> findAllByNovel(Novel novel);
}
