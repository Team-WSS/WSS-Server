package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovelAttractivePoint;

@Repository
public interface UserNovelAttractivePointRepository extends JpaRepository<UserNovelAttractivePoint, Long> {

    Integer countByUserNovel_NovelAndAttractivePoint_AttractivePointName(Novel novel, String attractivePoint);
}
