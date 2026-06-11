package org.websoso.WSSServer.feed.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.application.PopularFeedApplication;
import org.websoso.WSSServer.feed.event.PopularFeedCheckEvent;

@Component
@RequiredArgsConstructor
public class PopularFeedCheckEventListener {

    private final PopularFeedApplication popularFeedApplication;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PopularFeedCheckEvent event) {
        popularFeedApplication.checkAndRegister(event.feedId());
    }

}

