package org.websoso.WSSServer.feed.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.application.FeedManagementApplication;
import org.websoso.WSSServer.user.event.WithdrawUserEvent;

@Component
@RequiredArgsConstructor
public class FeedUserToUnknownEventListener {

    private final FeedManagementApplication feedManagementApplication;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WithdrawUserEvent event) {
        feedManagementApplication.updateFeedWriterToUnknown(event.userId());
    }

}
