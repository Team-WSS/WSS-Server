package org.websoso.WSSServer.repository;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;

@Repository
public interface NovelStatisticsRepository extends JpaRepository<NovelStatistics, Long> {
    Optional<NovelStatistics> findByNovel(Novel novel);

    default NovelStatistics findByNovelOrThrow(Novel novel) {
        return findByNovel(novel).orElseThrow(() -> new EntityNotFoundException(""));
    }
}
