package org.websoso.WSSServer.feed.feed.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.feed.application.PopularFeedApplication;
import org.websoso.WSSServer.feed.feed.event.PopularFeedCheckEvent;

@Component
@RequiredArgsConstructor
public class PopularFeedCheckEventListener {

    private final PopularFeedApplication popularFeedApplication;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PopularFeedCheckEvent event) {
        popularFeedApplication.checkAndRegister(event.feedId());
    }

}

