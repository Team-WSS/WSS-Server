package org.websoso.WSSServer.feed.report.application;

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

    @Transactional
    public void moderateFeed(Long reporterId, Long feedId, ReportedType reportedType) {

        // 비동기 처리 시점의 피드와 신고자 조회
        Feed feed = feedService.getFeedOrException(feedId);
        User reporter = userService.getUserOrException(reporterId);

        // 신고 유형별 누적 신고 수 조회 및 자동 처리
        int reportedCount = reportService.countByFeedAndReportedType(feed, reportedType);
        ReportModerationAction moderationAction = applyFeedModeration(feedId, reportedType, reportedCount);

        // 신고 처리 결과를 포함한 운영 메시지 생성
        String content = reportMessageFormatter.formatFeedReportMessage(
                reporter,
                feed,
                reportedType,
                reportedCount,
                moderationAction
        );

        // 운영 메시지 전송 이벤트 발행
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

    @Transactional
    public void moderateComment(Long reporterId, Long commentId, ReportedType reportedType) {

        // 비동기 처리 시점의 댓글, 피드 및 사용자 조회
        Comment comment = commentService.getCommentOrException(commentId);
        Feed feed = comment.getFeed();
        User reporter = userService.getUserOrException(reporterId);
        User commentWriter = userService.getUserOrException(comment.getUserId());

        // 신고 유형별 누적 신고 수 조회 및 자동 처리
        int reportedCount = reportService.countByCommentAndReportedType(comment, reportedType);
        ReportModerationAction moderationAction = applyCommentModeration(
                commentId,
                reportedType,
                reportedCount
        );

        // 신고 처리 결과를 포함한 운영 메시지 생성
        String content = reportMessageFormatter.formatCommentReportMessage(
                reporter,
                feed,
                comment,
                commentWriter,
                reportedType,
                reportedCount,
                moderationAction
        );

        // 운영 메시지 전송 이벤트 발행
        eventPublisher.publishEvent(ReportMessageCreatedEvent.of(content));
    }

    private ReportModerationAction applyFeedModeration(Long feedId, ReportedType reportedType, int reportedCount) {

        // 신고 임계치 미만이면 상태를 변경하지 않음
        if (!reportedType.isExceedingLimit(reportedCount)) {
            return ReportModerationAction.NONE;
        }

        // 스포일러 신고인 경우 피드를 스포일러 처리
        if (reportedType.isSpoiler()) {
            return feedService.markSpoilerIfNotMarked(feedId)
                    ? ReportModerationAction.MARKED_AS_SPOILER
                    : ReportModerationAction.ALREADY_MARKED_AS_SPOILER;
        }

        // 부적절한 표현 신고인 경우 피드를 숨김 처리
        if (reportedType.isImpertinence()) {
            return feedService.hideIfNotHidden(feedId)
                    ? ReportModerationAction.HIDDEN
                    : ReportModerationAction.ALREADY_HIDDEN;
        }

        return ReportModerationAction.NONE;
    }

    private ReportModerationAction applyCommentModeration(Long commentId, ReportedType reportedType, int reportedCount) {

        // 신고 임계치 미만이면 상태를 변경하지 않음
        if (!reportedType.isExceedingLimit(reportedCount)) {
            return ReportModerationAction.NONE;
        }

        // 스포일러 신고인 경우 댓글을 스포일러 처리
        if (reportedType.isSpoiler()) {
            return commentService.markSpoilerIfNotMarked(commentId)
                    ? ReportModerationAction.MARKED_AS_SPOILER
                    : ReportModerationAction.ALREADY_MARKED_AS_SPOILER;
        }

        // 부적절한 표현 신고인 경우 댓글을 숨김 처리
        if (reportedType.isImpertinence()) {
            return commentService.hideIfNotHidden(commentId)
                    ? ReportModerationAction.HIDDEN
                    : ReportModerationAction.ALREADY_HIDDEN;
        }

        return ReportModerationAction.NONE;
    }
}
