package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    Integer countByNovelId(Long novelId);

    @Query("SELECT f FROM Feed f WHERE f.novelId IN :novelIds ORDER BY SIZE(f.likes) DESC")
    List<Feed> findPopularFeedsByNovelIds(@Param("novelIds") List<Long> novelIds);
}
