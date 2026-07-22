package org.websoso.WSSServer.feed.report.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.report.application.ReportModerationApplication;
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;

@Component
@RequiredArgsConstructor
public class ReportSavedEventListener {

    private final ReportModerationApplication reportModerationApplication;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FeedReportSavedEvent event) {
        reportModerationApplication.moderateFeed(event.reporterId(), event.feedId(), event.reportedType());
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentReportSavedEvent event) {
        reportModerationApplication.moderateComment(event.reporterId(), event.commentId(), event.reportedType());
    }
}
