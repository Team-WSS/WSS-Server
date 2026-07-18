package org.websoso.WSSServer.feed.report.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

@ExtendWith(MockitoExtension.class)
class ReportModerationApplicationTest {

    private static final Long REPORTER_ID = 1L;
    private static final Long FEED_ID = 10L;
    private static final Long COMMENT_ID = 20L;
    private static final Long COMMENT_WRITER_ID = 3L;

    @InjectMocks
    private ReportModerationApplication reportModerationApplication;

    @Mock
    private FeedServiceImpl feedService;

    @Mock
    private CommentServiceImpl commentService;

    @Mock
    private ReportServiceImpl reportService;

    @Mock
    private UserService userService;

    @Mock
    private ReportMessageFormatter reportMessageFormatter;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private User reporter;

    @Mock
    private User commentWriter;

    @Mock
    private Feed feed;

    @Mock
    private Comment comment;

    @DisplayName("커밋된 스포일러 신고가 임계치에 도달하면 피드를 스포일러 처리한다")
    @Test
    void marksFeedAsSpoilerAfterCommit() {
        FeedReportSavedEvent event = FeedReportSavedEvent.of(REPORTER_ID, FEED_ID, ReportedType.SPOILER);
        givenFeedModeration(event, 3, ReportModerationAction.MARKED_AS_SPOILER, "spoiler message");
        given(feedService.markSpoilerIfNotMarked(FEED_ID)).willReturn(true);

        reportModerationApplication.moderate(event);

        then(feedService).should().markSpoilerIfNotMarked(FEED_ID);
        then(feedService).should(never()).hideIfNotHidden(FEED_ID);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("spoiler message"));
    }

    @DisplayName("커밋된 부적절 신고가 임계치에 도달하면 피드를 숨김 처리한다")
    @Test
    void hidesFeedAfterCommit() {
        FeedReportSavedEvent event = FeedReportSavedEvent.of(REPORTER_ID, FEED_ID, ReportedType.IMPERTINENCE);
        givenFeedModeration(event, 3, ReportModerationAction.HIDDEN, "hidden message");
        given(feedService.hideIfNotHidden(FEED_ID)).willReturn(true);

        reportModerationApplication.moderate(event);

        then(feedService).should().hideIfNotHidden(FEED_ID);
        then(feedService).should(never()).markSpoilerIfNotMarked(FEED_ID);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("hidden message"));
    }

    @DisplayName("커밋된 댓글 신고가 임계치에 도달하면 댓글을 스포일러 처리한다")
    @Test
    void marksCommentAsSpoilerAfterCommit() {
        CommentReportSavedEvent event = CommentReportSavedEvent.of(REPORTER_ID, COMMENT_ID, ReportedType.SPOILER);
        givenCommentModeration(event, 3, ReportModerationAction.MARKED_AS_SPOILER, "comment message");
        given(commentService.markSpoilerIfNotMarked(COMMENT_ID)).willReturn(true);

        reportModerationApplication.moderate(event);

        then(commentService).should().markSpoilerIfNotMarked(COMMENT_ID);
        then(commentService).should(never()).hideIfNotHidden(COMMENT_ID);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("comment message"));
    }

    @DisplayName("커밋된 신고가 임계치 미만이면 상태를 변경하지 않는다")
    @Test
    void doesNotModerateBelowLimit() {
        FeedReportSavedEvent event = FeedReportSavedEvent.of(REPORTER_ID, FEED_ID, ReportedType.SPOILER);
        givenFeedModeration(event, 2, ReportModerationAction.NONE, "not moderated message");

        reportModerationApplication.moderate(event);

        then(feedService).should(never()).markSpoilerIfNotMarked(FEED_ID);
        then(feedService).should(never()).hideIfNotHidden(FEED_ID);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("not moderated message"));
    }

    @DisplayName("이미 처리된 피드가 임계치를 넘으면 중복 처리하지 않았음을 메시지에 반영한다")
    @Test
    void reportsAlreadyModeratedFeed() {
        FeedReportSavedEvent event = FeedReportSavedEvent.of(REPORTER_ID, FEED_ID, ReportedType.SPOILER);
        givenFeedModeration(event, 4, ReportModerationAction.ALREADY_MARKED_AS_SPOILER, "already moderated");
        given(feedService.markSpoilerIfNotMarked(FEED_ID)).willReturn(false);

        reportModerationApplication.moderate(event);

        then(feedService).should().markSpoilerIfNotMarked(FEED_ID);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("already moderated"));
    }

    private void givenFeedModeration(FeedReportSavedEvent event, int reportedCount,
                                     ReportModerationAction action, String message) {
        given(feedService.getFeedOrException(FEED_ID)).willReturn(feed);
        given(userService.getUserOrException(REPORTER_ID)).willReturn(reporter);
        given(reportService.countByFeedAndReportedType(feed, event.reportedType())).willReturn(reportedCount);
        given(reportMessageFormatter.formatFeedReportMessage(
                reporter,
                feed,
                event.reportedType(),
                reportedCount,
                action
        )).willReturn(message);
    }

    private void givenCommentModeration(CommentReportSavedEvent event, int reportedCount,
                                        ReportModerationAction action, String message) {
        given(comment.getUserId()).willReturn(COMMENT_WRITER_ID);
        given(comment.getFeed()).willReturn(feed);
        given(commentService.getCommentOrException(COMMENT_ID)).willReturn(comment);
        given(userService.getUserOrException(REPORTER_ID)).willReturn(reporter);
        given(userService.getUserOrException(COMMENT_WRITER_ID)).willReturn(commentWriter);
        given(reportService.countByCommentAndReportedType(comment, event.reportedType())).willReturn(reportedCount);
        given(reportMessageFormatter.formatCommentReportMessage(
                reporter,
                feed,
                comment,
                commentWriter,
                event.reportedType(),
                reportedCount,
                action
        )).willReturn(message);
    }
}
