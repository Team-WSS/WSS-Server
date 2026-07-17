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
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.feed.report.message.ReportMessageFormatter;
import org.websoso.WSSServer.feed.report.service.ReportServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

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

    @DisplayName("댓글 신고 시 애플리케이션 계층에서 피드 접근 가능 여부를 검증한다")
    @Test
    void validatesFeedAccessWhenReportingComment() {
        given(reporter.getUserId()).willReturn(USER_ID);
        given(feed.getWriterId()).willReturn(FEED_WRITER_ID);
        given(comment.getUserId()).willReturn(COMMENT_WRITER_ID);
        given(feedService.getAccessFeedOrException(FEED_ID, USER_ID)).willReturn(feed);
        given(commentService.getCommentOrException(COMMENT_ID)).willReturn(comment);
        given(userService.getUserOrException(COMMENT_WRITER_ID)).willReturn(commentWriter);
        given(reportService.isExistsByCommentAndUserAndReportedType(comment, reporter, ReportedType.SPOILER))
                .willReturn(false);
        given(reportService.countByCommentAndReportedType(comment, ReportedType.SPOILER)).willReturn(1);
        given(reportMessageFormatter.formatCommentReportMessage(
                reporter,
                feed,
                comment,
                ReportedType.SPOILER,
                commentWriter,
                1,
                false
        )).willReturn("report message");

        reportApplication.reportComment(reporter, FEED_ID, COMMENT_ID, ReportedType.SPOILER);

        then(feedService).should().getAccessFeedOrException(FEED_ID, USER_ID);
        then(feedService).should(never()).getFeedOrException(FEED_ID);
        then(blockService).should().validateNotBlocked(USER_ID, FEED_WRITER_ID);
        then(comment).should().validateBelongsTo(feed);
        then(eventPublisher).should().publishEvent(ReportMessageCreatedEvent.of("report message"));
    }
}
