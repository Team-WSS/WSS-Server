package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.NovelStatistics;

@Repository
public interface NovelStatisticsRepository extends JpaRepository<NovelStatistics, Long> {

    @Query("SELECT ns FROM NovelStatistics ns WHERE ns.novel.novelId = :novelId")
    Optional<NovelStatistics> findByNovelId(@Param("novelId") Long novelId);

}
