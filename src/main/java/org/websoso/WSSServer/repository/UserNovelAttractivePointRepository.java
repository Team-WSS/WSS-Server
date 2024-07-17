package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;

@Repository
public interface UserNovelAttractivePointRepository extends JpaRepository<UserNovelAttractivePoint, Long> {

    List<UserNovelAttractivePoint> findAllByUserNovel(UserNovel userNovel);

    Integer countByUserNovel_NovelAndAttractivePoint_AttractivePointName(Novel novel, String attractivePoint);

    void deleteByAttractivePointAndUserNovel(AttractivePoint attractivePoint, UserNovel userNovel);

}
