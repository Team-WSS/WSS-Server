package org.websoso.WSSServer.novel.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.novel.domain.NovelStatistics;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.repository.NovelStatisticsRepository;

@Service
@RequiredArgsConstructor
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;

    @Transactional(readOnly = true)
    public Map<Long, NovelStatistics> getStatisticsByNovelIds(List<Long> novelIds) {
        if (novelIds.isEmpty()) {
            return Map.of();
        }

        return novelStatisticsRepository.findAllById(novelIds).stream()
                .collect(Collectors.toMap(
                        NovelStatistics::getNovelId,
                        Function.identity()
                ));
    }

    @Transactional
    public void updateByDelta(Long novelId, NovelStatisticsContribution before,
                              NovelStatisticsContribution after) {
        BigDecimal ratingSumDelta = after.ratingSum().subtract(before.ratingSum());
        long ratingCountDelta = after.ratingCount() - before.ratingCount();
        long popularityDelta = after.popularity() - before.popularity();

        if (ratingSumDelta.signum() == 0
                && ratingCountDelta == 0
                && popularityDelta == 0) {
            return;
        }

        novelStatisticsRepository.insertIfAbsent(novelId);
        novelStatisticsRepository.updateByDelta(
                novelId,
                ratingSumDelta,
                ratingCountDelta,
                popularityDelta
        );
    }

    @Transactional
    public int correctAll() {
        return novelStatisticsRepository.correctAll();
    }
}
