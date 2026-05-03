package org.websoso.WSSServer.recentsearch.job;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.recentsearch.service.RecentSearchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecentSearchCleanupJob {

    private static final int MAX_LOOP = 100;
    private static final int BATCH_SIZE = 1000;

    private final RecentSearchService service;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        long total = 0;

        for (int i = 0; i < MAX_LOOP; i++) {
            int deleted = service.deleteBatch(threshold, BATCH_SIZE);
            total += deleted;
            if (deleted < BATCH_SIZE) break;
        }

        log.info("recent_search cleanup done. threshold={}, deleted={}", threshold, total);
    }

}