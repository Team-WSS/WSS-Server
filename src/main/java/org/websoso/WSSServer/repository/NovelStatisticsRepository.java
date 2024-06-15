package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;

@Repository
public interface NovelStatisticsRepository extends JpaRepository<NovelStatistics, Long> {
    
    Optional<NovelStatistics> findByNovel(Novel novel);

}
