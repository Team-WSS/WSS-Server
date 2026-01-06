package org.websoso.WSSServer.library.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;

@Repository
public interface UserNovelAttractivePointRepository extends JpaRepository<UserNovelAttractivePoint, Long> {

    Integer countByUserNovel_NovelAndAttractivePoint_AttractivePointName(Novel novel, String attractivePoint);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserNovelAttractivePoint un WHERE un.userNovel = :userNovel AND un.attractivePoint IN :attractivePoints")
    void deleteByAttractivePointsAndUserNovel(Set<AttractivePoint> attractivePoints, UserNovel userNovel);

}
