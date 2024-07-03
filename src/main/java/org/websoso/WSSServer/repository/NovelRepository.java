package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    @Query(value = "SELECT n.* FROM novel n JOIN (SELECT un.novel_id FROM user_novel un GROUP BY un.novel_id ORDER BY MAX(un.created_date) DESC LIMIT 10) latest_un ON n.novel_id = latest_un.novel_id", nativeQuery = true)
    List<Novel> findSosoPick();
}
