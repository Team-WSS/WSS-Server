package org.websoso.WSSServer.feed.report.application;

import static org.websoso.WSSServer.feed.report.exception.CustomReportError.SELF_COMMENT_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.SELF_FEED_REPORT_NOT_ALLOWED;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;
import org.websoso.WSSServer.feed.report.exception.CustomReportException;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final FeedServiceImpl feedService;
    private final CommentServiceImpl commentService;
    private final ReportServiceImpl reportService;
    private final BlockService blockService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reportFeed(User user, Long feedId, ReportedType reportedType) {
        Feed feed = feedService.getAccessFeedOrException(feedId, user.getUserId());

        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        if (feed.isMine(user.getUserId())) {
            throw new CustomReportException(SELF_FEED_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        reportService.saveReportedFeed(feed, user, reportedType);
        eventPublisher.publishEvent(FeedReportSavedEvent.of(user.getUserId(), feedId, reportedType));
    }

    @Transactional
    public void reportComment(User user, Long commentId, ReportedType reportedType) {
        Comment comment = commentService.getCommentOrException(commentId);
        Feed feed = comment.getFeed();

        feedService.validateAccess(feed, user.getUserId());
        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());
        if (!comment.getUserId().equals(feed.getWriterId())) {
            blockService.validateNotBlocked(user.getUserId(), comment.getUserId());
        }

        if (comment.isMine(user.getUserId())) {
            throw new CustomReportException(SELF_COMMENT_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        reportService.saveReportedComment(comment, user, reportedType);
        eventPublisher.publishEvent(CommentReportSavedEvent.of(user.getUserId(), commentId, reportedType));
    }

    @Deprecated(since = "POST /comments/{commentId}/spoiler 또는 /impertinence로 완벽 교체시")
    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        reportComment(user, commentId, reportedType);
    }
}
