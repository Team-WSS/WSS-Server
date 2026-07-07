package org.websoso.WSSServer.feed.report.application;

import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;
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
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.feed.report.exception.CustomReportException;
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.feed.report.message.ReportMessageFormatter;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.WSSServer.user.service.BlockService;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final CommentServiceImpl commentServiceImpl;
    private final ReportServiceImpl reportServiceImpl;
    private final BlockService blockService;
    private final UserService userService;
    private final ReportMessageFormatter reportMessageFormatter;
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

        int reportedCount = reportServiceImpl.countByFeedAndReportedType(feed, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            feed.hideFeed();
        }

        String content = reportMessageFormatter.formatFeedReportMessage(
                user,
                feed,
                reportedType,
                reportedCount,
                shouldHide
        );
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

    @Transactional
    public void reportComment(User user, Long feedId, Long commentId, ReportedType reportedType) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        Comment comment = commentServiceImpl.getCommentOrException(commentId);
        comment.validateBelongsTo(feed);
        User commentCreatedUser = userService.getUserOrException(comment.getUserId());

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

        int reportedCount = reportServiceImpl.countByCommentAndReportedType(comment, reportedType);
        boolean shouldHide = reportedType.isExceedingLimit(reportedCount);

        if (shouldHide) {
            if (reportedType.equals(SPOILER)) {
                comment.markSpoiler();
            } else if (reportedType.equals(IMPERTINENCE)) {
                comment.markHidden();
            }
        }

        String content = reportMessageFormatter.formatCommentReportMessage(
                user,
                feed,
                comment,
                reportedType,
                commentCreatedUser,
                reportedCount,
                shouldHide
        );
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

}
