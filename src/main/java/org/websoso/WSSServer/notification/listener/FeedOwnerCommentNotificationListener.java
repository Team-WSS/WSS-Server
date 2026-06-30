package org.websoso.WSSServer.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.comment.event.CommentCreatedEvent;
import org.websoso.WSSServer.notification.application.NotificationSendApplication;

@Component
@RequiredArgsConstructor
public class FeedOwnerCommentNotificationListener {

    private final NotificationSendApplication notificationSendApplication;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentCreatedEvent event) {
        notificationSendApplication.sendFeedOwnerCommentPushMessage(event.userId(), event.feedId());
    }

}
