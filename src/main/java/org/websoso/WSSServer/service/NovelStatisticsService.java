package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.NOVEL_STATISTICS_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;

@Service
@RequiredArgsConstructor
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;

    protected NovelStatistics getNovelStatisticsOrException(Novel novel) {
        return novelStatisticsRepository.findByNovel(novel).orElseThrow(
                () -> new InvalidNovelStatisticsException(NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics with the given novel is not found"));
    }
}