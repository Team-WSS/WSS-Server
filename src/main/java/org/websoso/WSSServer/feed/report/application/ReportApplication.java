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
        Long reporterId = user.getUserId();

        // 접근 가능한 피드인지 체크하고 조회
        Feed feed = feedService.getAccessFeedOrException(feedId, reporterId);

        // 본인이 작성한 피드인지 체크
        if (feed.isWrittenBy(reporterId)) {
            throw new CustomReportException(SELF_FEED_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        // 신고자와 피드 작성자가 차단 관계인지 체크
        blockService.validateNotBlocked(reporterId, feed.getWriterId());

        // 피드 신고 저장
        reportService.saveReportedFeed(feed, user, reportedType);

        // 신고 집계 및 자동 처리를 위한 이벤트 발행
        eventPublisher.publishEvent(FeedReportSavedEvent.of(reporterId, feedId, reportedType));
    }

    @Transactional
    public void reportComment(User user, Long commentId, ReportedType reportedType) {
        Long reporterId = user.getUserId();

        // commentId로 댓글과 소속 피드 조회
        Comment comment = commentService.getCommentOrException(commentId);
        Feed feed = comment.getFeed();
        Long feedWriterId = feed.getWriterId();

        // 소속 피드에 접근 가능한지 체크
        feedService.validateAccess(feed, reporterId);

        // 본인이 작성한 댓글인지 체크
        if (comment.isWrittenBy(reporterId)) {
            throw new CustomReportException(SELF_COMMENT_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        // 신고자와 피드 작성자 및 댓글 작성자가 차단 관계인지 체크
        blockService.validateNotBlocked(reporterId, feedWriterId);
        boolean isCommentWrittenByFeedWriter = comment.isWrittenBy(feedWriterId);
        if (!isCommentWrittenByFeedWriter) {
            blockService.validateNotBlocked(reporterId, comment.getUserId());
        }

        // 댓글 신고 저장
        reportService.saveReportedComment(comment, user, reportedType);

        // 신고 집계 및 자동 처리를 위한 이벤트 발행
        eventPublisher.publishEvent(CommentReportSavedEvent.of(reporterId, commentId, reportedType));
    }

    @Deprecated(since = "POST /comments/{commentId}/spoiler 또는 /impertinence로 완벽 교체시")
    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {

        // 기존 API 호환을 위해 feedId를 제외한 댓글 신고 로직으로 위임
        reportComment(user, commentId, reportedType);
    }
}
