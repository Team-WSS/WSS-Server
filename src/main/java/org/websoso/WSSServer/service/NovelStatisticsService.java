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
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;
    private final NovelService novelService;

    @Transactional
    public void increaseNovelFeedCount(Long novelId) {
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovelId(novelId)
                .orElseGet(() -> createNovelStatistics(novelId));

        novelStatistics.increaseNovelFeedCount();
    }

    @Transactional
    public NovelStatistics createNovelStatistics(Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);

        return novelStatisticsRepository.save(
                NovelStatistics.builder()
                        .novel(novel)
                        .build()
        );
    }

    @Transactional
    public void decreaseNovelFeedCount(Long novelId) {
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovelId(novelId)
                .orElseThrow(() -> new InvalidNovelStatisticsException(NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics not found"));

        novelStatistics.decreaseNovelFeedCount();
    }

    protected NovelStatistics getNovelStatisticsOrException(Novel novel) {
        return novelStatisticsRepository.findByNovel(novel).orElseThrow(
                () -> new InvalidNovelStatisticsException(NOVEL_STATISTICS_NOT_FOUND,
                        "novel statistics with the given novel is not found"));
    }

}
