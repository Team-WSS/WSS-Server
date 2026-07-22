package org.websoso.WSSServer.feed.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_FEED;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.report.application.ReportApplication;
import org.websoso.WSSServer.feed.report.exception.DuplicateReportException;
import org.websoso.WSSServer.user.domain.User;

class ReportControllerTest {

    private final ReportApplication reportApplication = Mockito.mock(ReportApplication.class);
    private final ReportController reportController = new ReportController(reportApplication);
    private final User reporter = Mockito.mock(User.class);

    @DisplayName("기존 피드 신고 API는 201 Created를 반환한다")
    @Test
    void returnsCreatedForLegacyFeedReport() {
        ResponseEntity<Void> response = reportController.reportFeedSpoiler(reporter, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(reportApplication).should().reportFeed(reporter, 1L, ReportedType.SPOILER);
    }

    @DisplayName("기존 댓글 신고 API는 201 Created를 반환한다")
    @Test
    void returnsCreatedForLegacyCommentReport() {
        ResponseEntity<Void> response = reportController.reportCommentSpoiler(reporter, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(reportApplication).should().reportComment(reporter, 1L, ReportedType.SPOILER);
    }

    @DisplayName("v2 피드 신고 API는 204 No Content를 반환한다")
    @Test
    void returnsNoContentForV2FeedReport() {
        ResponseEntity<Void> response = reportController.reportFeedSpoilerV2(reporter, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(reportApplication).should().reportFeed(reporter, 1L, ReportedType.SPOILER);
    }

    @DisplayName("v2 댓글 신고 API는 204 No Content를 반환한다")
    @Test
    void returnsNoContentForV2CommentReport() {
        ResponseEntity<Void> response = reportController.reportCommentSpoilerV2(reporter, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(reportApplication).should().reportComment(reporter, 1L, ReportedType.SPOILER);
    }

    @DisplayName("v2 피드 신고 API는 중복 신고도 204 No Content를 반환한다")
    @Test
    void returnsNoContentForDuplicateV2FeedReport() {
        willThrow(new DuplicateReportException(ALREADY_REPORTED_FEED, "already reported"))
                .given(reportApplication).reportFeed(reporter, 1L, ReportedType.SPOILER);

        ResponseEntity<Void> response = reportController.reportFeedSpoilerV2(reporter, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
