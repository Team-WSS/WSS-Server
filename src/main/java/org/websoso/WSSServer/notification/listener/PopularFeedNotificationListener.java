package org.websoso.WSSServer.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.event.FeedBecamePopularEvent;
import org.websoso.WSSServer.notification.application.NotificationSendApplication;

@Component
@RequiredArgsConstructor
public class PopularFeedNotificationListener {

    private final NotificationSendApplication notificationSendApplication;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FeedBecamePopularEvent event) {
        notificationSendApplication.sendFeedBecamePopularPushMessage(event.feedId());
    }

}
