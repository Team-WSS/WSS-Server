package org.websoso.WSSServer.novel.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.repository.NovelStatisticsRepository;

@Service
@RequiredArgsConstructor
public class NovelStatisticsService {

    private final NovelStatisticsRepository novelStatisticsRepository;

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
