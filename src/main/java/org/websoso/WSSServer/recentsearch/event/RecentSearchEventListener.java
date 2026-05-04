package org.websoso.WSSServer.recentsearch.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.recentsearch.service.RecentSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecentSearchEventListener {

    private final RecentSearchService recentSearchService;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NovelSearchedEvent event) {
        try {
            recentSearchService.add(event.userId(), event.keyword());
        } catch (Exception e) {
            log.warn("recent search save failed. userId={}, keyword={}",
                    event.userId(), event.keyword(), e);
        }
    }
}

