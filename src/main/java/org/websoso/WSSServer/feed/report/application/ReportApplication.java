package org.websoso.WSSServer.feed.report.application;

import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_FEED;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.SELF_COMMENT_REPORT_NOT_ALLOWED;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.SELF_FEED_REPORT_NOT_ALLOWED;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.report.exception.CustomReportException;
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final CommentServiceImpl commentServiceImpl;
    private final ReportServiceImpl reportServiceImpl;
    private final BlockService blockService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reportFeed(User user, Long feedId, ReportedType reportedType) {

        // 차단한 경우 피드는 접근할 수 없다. (피드 정책)
        // 접근 가능한 피드인지 체크 및 피드 불러오기
        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        // 서로 차단 관계인지 체크한다.
        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        // 본인이 작성한 피드는 신고할 수 없다. (신고 정책)
        if (feed.isMine(user.getUserId())) {
            throw new CustomReportException(SELF_FEED_REPORT_NOT_ALLOWED, "cannot report own feed");
        }

        // 이미 신고한 피드는 또 신고할 수 없다. (신고 정책)
        if (reportServiceImpl.isExistsByFeedAndUserAndReportedType(feed, user, reportedType)) {
            throw new CustomReportException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        try {
            reportServiceImpl.saveReportedFeed(feed, user, reportedType);
        } catch (DataIntegrityViolationException e) {
            throw new CustomReportException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        eventPublisher.publishEvent(FeedReportSavedEvent.of(user.getUserId(), feedId, reportedType));
    }

    @Transactional
    public void reportComment(User user, Long commentId, ReportedType reportedType) {
        Comment comment = commentServiceImpl.getCommentOrException(commentId);
        Feed feed = comment.getFeed();

        feedServiceImpl.validateAccess(feed, user.getUserId());
        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());
        if (!comment.getUserId().equals(feed.getWriterId())) {
            blockService.validateNotBlocked(user.getUserId(), comment.getUserId());
        }

        if (comment.getUserId().equals(user.getUserId())) {
            throw new CustomReportException(SELF_COMMENT_REPORT_NOT_ALLOWED, "cannot report own comment");
        }

        if (reportServiceImpl.isExistsByCommentAndUserAndReportedType(comment, user, reportedType)) {
            throw new CustomReportException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        try {
            reportServiceImpl.saveReportedComment(comment, user, reportedType);
        } catch (DataIntegrityViolationException e) {
            throw new CustomReportException(ALREADY_REPORTED_COMMENT, "comment has already been reported by the user");
        }

        eventPublisher.publishEvent(CommentReportSavedEvent.of(user.getUserId(), commentId, reportedType));
    }

    @Deprecated(since = "POST /comments/{commentId}/spoiler 또는 /impertinence로 완벽 교체시")
    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        reportComment(user, commentId, reportedType);
    }

}
