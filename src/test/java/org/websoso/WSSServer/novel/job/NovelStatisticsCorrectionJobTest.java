package org.websoso.WSSServer.novel.job;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.novel.service.NovelStatisticsService;

@ExtendWith(MockitoExtension.class)
class NovelStatisticsCorrectionJobTest {

    @InjectMocks
    private NovelStatisticsCorrectionJob job;

    @Mock
    private NovelStatisticsService novelStatisticsService;

    @DisplayName("스케줄 실행 시 전체 작품 통계를 보정한다")
    @Test
    void correctsNovelStatistics() {
        job.correct();

        then(novelStatisticsService).should().correctAll();
    }
}
