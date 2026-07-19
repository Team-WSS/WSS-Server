package org.websoso.WSSServer.feed.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.feed.report.domain.ReportedFeed;
import org.websoso.WSSServer.feed.report.exception.DuplicateReportException;
import org.websoso.WSSServer.feed.report.repository.ReportConstraintViolationDetector;
import org.websoso.WSSServer.feed.report.repository.ReportedCommentRepository;
import org.websoso.WSSServer.feed.report.repository.ReportedFeedRepository;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private ReportedCommentRepository reportedCommentRepository;

    @Mock
    private ReportedFeedRepository reportedFeedRepository;

    @Mock
    private ReportConstraintViolationDetector constraintViolationDetector;

    @Mock
    private Feed feed;

    @Mock
    private Comment comment;

    @Mock
    private User reporter;

    @DisplayName("신규 피드 신고를 저장하고 즉시 flush한다")
    @Test
    void savesFeedReport() {
        reportService.saveReportedFeed(feed, reporter, ReportedType.SPOILER);

        then(reportedFeedRepository).should().saveAndFlush(any(ReportedFeed.class));
    }

    @DisplayName("피드 중복 신고 제약조건 위반을 내부 중복 신고 예외로 변환한다")
    @Test
    void translatesDuplicateFeedReport() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        willThrow(exception).given(reportedFeedRepository).saveAndFlush(any(ReportedFeed.class));
        given(constraintViolationDetector.isDuplicateFeedReport(exception)).willReturn(true);

        assertThatThrownBy(() -> reportService.saveReportedFeed(feed, reporter, ReportedType.SPOILER))
                .isInstanceOf(DuplicateReportException.class)
                .extracting(throwable -> ((DuplicateReportException) throwable).getICustomError())
                .isEqualTo(ALREADY_REPORTED_FEED);
    }

    @DisplayName("중복 신고가 아닌 피드 무결성 예외는 원본 예외를 유지한다")
    @Test
    void rethrowsUnrelatedFeedConstraintViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unrelated");
        willThrow(exception).given(reportedFeedRepository).saveAndFlush(any(ReportedFeed.class));
        given(constraintViolationDetector.isDuplicateFeedReport(exception)).willReturn(false);

        assertThatThrownBy(() -> reportService.saveReportedFeed(feed, reporter, ReportedType.SPOILER))
                .isSameAs(exception);
    }

    @DisplayName("댓글 중복 신고 제약조건 위반을 내부 중복 신고 예외로 변환한다")
    @Test
    void translatesDuplicateCommentReport() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        willThrow(exception).given(reportedCommentRepository).saveAndFlush(any(ReportedComment.class));
        given(constraintViolationDetector.isDuplicateCommentReport(exception)).willReturn(true);

        assertThatThrownBy(() -> reportService.saveReportedComment(comment, reporter, ReportedType.IMPERTINENCE))
                .isInstanceOf(DuplicateReportException.class);
    }
}
