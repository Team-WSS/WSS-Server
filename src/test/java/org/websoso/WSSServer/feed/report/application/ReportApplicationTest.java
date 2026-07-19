package org.websoso.WSSServer.feed.report.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_FEED;

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
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;
import org.websoso.WSSServer.feed.report.exception.CustomReportException;
import org.websoso.WSSServer.feed.report.exception.DuplicateReportException;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@ExtendWith(MockitoExtension.class)
class ReportApplicationTest {

    private static final Long USER_ID = 1L;
    private static final Long FEED_ID = 10L;
    private static final Long FEED_WRITER_ID = 2L;
    private static final Long COMMENT_ID = 20L;
    private static final Long COMMENT_WRITER_ID = 3L;

    @InjectMocks
    private ReportApplication reportApplication;

    @Mock
    private FeedServiceImpl feedService;

    @Mock
    private CommentServiceImpl commentService;

    @Mock
    private ReportServiceImpl reportService;

    @Mock
    private BlockService blockService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private User reporter;

    @Mock
    private Feed feed;

    @Mock
    private Comment comment;

    @DisplayName("피드 신고를 저장하면 커밋 이후 처리 이벤트를 발행한다")
    @Test
    void reportsFeed() {
        givenFeedReport();

        reportApplication.reportFeed(reporter, FEED_ID, ReportedType.SPOILER);

        then(reportService).should().saveReportedFeed(feed, reporter, ReportedType.SPOILER);
        then(eventPublisher).should().publishEvent(
                FeedReportSavedEvent.of(USER_ID, FEED_ID, ReportedType.SPOILER)
        );
    }

    @DisplayName("중복 피드 신고 예외는 이벤트를 발행하지 않고 호출자에게 전파한다")
    @Test
    void propagatesDuplicateFeedReport() {
        givenFeedReport();
        DuplicateReportException exception = new DuplicateReportException(
                ALREADY_REPORTED_FEED,
                "already reported"
        );
        willThrow(exception).given(reportService).saveReportedFeed(feed, reporter, ReportedType.SPOILER);

        assertThatThrownBy(() -> reportApplication.reportFeed(reporter, FEED_ID, ReportedType.SPOILER))
                .isSameAs(exception);

        then(eventPublisher).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 신고는 피드·댓글 작성자 차단 검증 후 저장하고 이벤트를 발행한다")
    @Test
    void reportsComment() {
        given(reporter.getUserId()).willReturn(USER_ID);
        given(feed.getWriterId()).willReturn(FEED_WRITER_ID);
        given(comment.getUserId()).willReturn(COMMENT_WRITER_ID);
        given(comment.getFeed()).willReturn(feed);
        given(commentService.getCommentOrException(COMMENT_ID)).willReturn(comment);

        reportApplication.reportComment(reporter, COMMENT_ID, ReportedType.IMPERTINENCE);

        then(feedService).should().validateAccess(feed, USER_ID);
        then(blockService).should().validateNotBlocked(USER_ID, FEED_WRITER_ID);
        then(blockService).should().validateNotBlocked(USER_ID, COMMENT_WRITER_ID);
        then(reportService).should().saveReportedComment(comment, reporter, ReportedType.IMPERTINENCE);
        then(eventPublisher).should().publishEvent(
                CommentReportSavedEvent.of(USER_ID, COMMENT_ID, ReportedType.IMPERTINENCE)
        );
    }

    @DisplayName("자신이 작성한 댓글은 신고할 수 없다")
    @Test
    void rejectsOwnCommentReport() {
        given(reporter.getUserId()).willReturn(USER_ID);
        given(feed.getWriterId()).willReturn(FEED_WRITER_ID);
        given(comment.getUserId()).willReturn(USER_ID);
        given(comment.getFeed()).willReturn(feed);
        given(comment.isMine(USER_ID)).willReturn(true);
        given(commentService.getCommentOrException(COMMENT_ID)).willReturn(comment);

        assertThatThrownBy(() -> reportApplication.reportComment(reporter, COMMENT_ID, ReportedType.SPOILER))
                .isInstanceOf(CustomReportException.class);

        then(reportService).shouldHaveNoInteractions();
        then(eventPublisher).shouldHaveNoInteractions();
    }

    private void givenFeedReport() {
        given(reporter.getUserId()).willReturn(USER_ID);
        given(feed.getWriterId()).willReturn(FEED_WRITER_ID);
        given(feedService.getAccessFeedOrException(FEED_ID, USER_ID)).willReturn(feed);
    }
}
