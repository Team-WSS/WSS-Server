package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.NOVEL_STATISTICS_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.NovelStatistics;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.NovelStatisticsRepository;

@Service
@RequiredArgsConstructor
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;
    private final NovelRepository novelRepository; // 나경이거 머지 되면 수정

    @Transactional
    public void increaseNovelFeedCount(Long novelId) {
        NovelStatistics novelStatistics = novelStatisticsRepository.findByNovelId(novelId)
                .orElseGet(() -> createNovelStatistics(novelId));

        novelStatistics.increaseNovelFeedCount();
    }

    @Transactional
    public NovelStatistics createNovelStatistics(Long novelId) {
        Novel novel = novelRepository.findById(novelId).get(); // 나경이거 머지 되면 수정

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
