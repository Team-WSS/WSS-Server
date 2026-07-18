package org.websoso.WSSServer.feed.report.application;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.report.domain.ReportModerationAction;
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.feed.report.message.ReportMessageFormatter;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class ReportModerationApplication {

    private final FeedServiceImpl feedService;
    private final CommentServiceImpl commentService;
    private final ReportServiceImpl reportService;
    private final UserService userService;
    private final ReportMessageFormatter reportMessageFormatter;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = REQUIRES_NEW)
    public void moderate(FeedReportSavedEvent event) {
        Feed feed = feedService.getFeedOrException(event.feedId());
        User reporter = userService.getUserOrException(event.reporterId());
        int reportedCount = reportService.countByFeedAndReportedType(feed, event.reportedType());
        ReportModerationAction moderationAction = moderateFeed(event.feedId(), event.reportedType(), reportedCount);

        String content = reportMessageFormatter.formatFeedReportMessage(
                reporter,
                feed,
                event.reportedType(),
                reportedCount,
                moderationAction
        );
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void moderate(CommentReportSavedEvent event) {
        Comment comment = commentService.getCommentOrException(event.commentId());
        Feed feed = comment.getFeed();
        User reporter = userService.getUserOrException(event.reporterId());
        User commentWriter = userService.getUserOrException(comment.getUserId());
        int reportedCount = reportService.countByCommentAndReportedType(comment, event.reportedType());
        ReportModerationAction moderationAction = moderateComment(
                event.commentId(),
                event.reportedType(),
                reportedCount
        );

        String content = reportMessageFormatter.formatCommentReportMessage(
                reporter,
                feed,
                comment,
                commentWriter,
                event.reportedType(),
                reportedCount,
                moderationAction
        );
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

    private ReportModerationAction moderateFeed(Long feedId, ReportedType reportedType, int reportedCount) {
        if (!reportedType.isExceedingLimit(reportedCount)) {
            return ReportModerationAction.NONE;
        }

        if (reportedType.isSpoiler()) {
            feedService.markSpoiler(feedId);
            return ReportModerationAction.MARKED_AS_SPOILER;
        }

        if (reportedType.isImpertinence()) {
            feedService.hide(feedId);
            return ReportModerationAction.HIDDEN;
        }

        return ReportModerationAction.NONE;
    }

    private ReportModerationAction moderateComment(Long commentId, ReportedType reportedType, int reportedCount) {
        if (!reportedType.isExceedingLimit(reportedCount)) {
            return ReportModerationAction.NONE;
        }

        if (reportedType.isSpoiler()) {
            commentService.markSpoiler(commentId);
            return ReportModerationAction.MARKED_AS_SPOILER;
        }

        if (reportedType.isImpertinence()) {
            commentService.hide(commentId);
            return ReportModerationAction.HIDDEN;
        }

        return ReportModerationAction.NONE;
    }
}
