package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.NOVEL_STATISTICS_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;

    public void increaseNovelFeedCount(Novel novel) {
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovel(novel)
                .orElseGet(() -> createNovelStatistics(novel));

        novelStatistics.increaseNovelFeedCount();
    }

    public NovelStatistics createNovelStatistics(Novel novel) {
        return novelStatisticsRepository.save(
                NovelStatistics.builder()
                        .novel(novel)
                        .build()
        );
    }

    public void decreaseNovelFeedCount(Novel novel) {
        NovelStatistics novelStatistics = getNovelStatisticsOrException(novel);

        novelStatistics.decreaseNovelFeedCount();
    }

    @Transactional(readOnly = true)
    protected NovelStatistics getNovelStatisticsOrException(Novel novel) {
        return novelStatisticsRepository.findByNovel(novel).orElseThrow(
                () -> new InvalidNovelStatisticsException(NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics with the given novel is not found"));
    }

}
