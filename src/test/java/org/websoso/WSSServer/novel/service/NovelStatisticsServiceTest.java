package org.websoso.WSSServer.novel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.repository.NovelStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class NovelStatisticsServiceTest {

    @InjectMocks
    private NovelStatisticsService novelStatisticsService;

    @Mock
    private NovelStatisticsRepository novelStatisticsRepository;

    @DisplayName("변경 전후의 차이만 작품 통계에 반영한다")
    @Test
    void updatesStatisticsByDelta() {
        NovelStatisticsContribution before =
                new NovelStatisticsContribution(new BigDecimal("2.0"), 1L, 0L);
        NovelStatisticsContribution after =
                new NovelStatisticsContribution(new BigDecimal("4.0"), 1L, 1L);

        novelStatisticsService.updateByDelta(1L, before, after);

        then(novelStatisticsRepository).should().insertIfAbsent(1L);
        then(novelStatisticsRepository).should()
                .updateByDelta(1L, new BigDecimal("2.0"), 0L, 1L);
    }

    @DisplayName("통계 기여분에 변화가 없으면 UPDATE를 실행하지 않는다")
    @Test
    void skipsUpdateWhenDeltaIsZero() {
        NovelStatisticsContribution contribution =
                new NovelStatisticsContribution(new BigDecimal("4.0"), 1L, 1L);

        novelStatisticsService.updateByDelta(1L, contribution, contribution);

        then(novelStatisticsRepository).should(never()).insertIfAbsent(anyLong());
        then(novelStatisticsRepository).should(never())
                .updateByDelta(anyLong(), any(BigDecimal.class), anyLong(), anyLong());
    }

    @DisplayName("전체 작품 통계를 원본 서재 데이터 기준으로 보정한다")
    @Test
    void correctsAllStatistics() {
        given(novelStatisticsRepository.correctAll()).willReturn(10);

        int affectedRows = novelStatisticsService.correctAll();

        assertThat(affectedRows).isEqualTo(10);
        then(novelStatisticsRepository).should().correctAll();
    }
}
