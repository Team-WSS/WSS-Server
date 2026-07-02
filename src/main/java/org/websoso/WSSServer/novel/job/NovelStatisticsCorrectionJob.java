package org.websoso.WSSServer.novel.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.novel.service.NovelStatisticsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NovelStatisticsCorrectionJob {

    private final NovelStatisticsService novelStatisticsService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void correct() {
        int affectedRows = novelStatisticsService.correctAll();
        log.info("novel statistics correction done. affectedRows={}", affectedRows);
    }
}
