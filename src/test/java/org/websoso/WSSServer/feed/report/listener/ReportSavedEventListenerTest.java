package org.websoso.WSSServer.feed.report.listener;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.report.application.ReportModerationApplication;
import org.websoso.WSSServer.feed.report.event.CommentReportSavedEvent;
import org.websoso.WSSServer.feed.report.event.FeedReportSavedEvent;

@ExtendWith(MockitoExtension.class)
class ReportSavedEventListenerTest {

    @InjectMocks
    private ReportSavedEventListener listener;

    @Mock
    private ReportModerationApplication reportModerationApplication;

    @DisplayName("피드 신고 저장 커밋 후 moderation을 요청한다")
    @Test
    void moderatesFeedReport() {
        FeedReportSavedEvent event = FeedReportSavedEvent.of(1L, 10L, ReportedType.SPOILER);

        listener.handle(event);

        then(reportModerationApplication).should().moderate(event);
    }

    @DisplayName("댓글 신고 저장 커밋 후 moderation을 요청한다")
    @Test
    void moderatesCommentReport() {
        CommentReportSavedEvent event = CommentReportSavedEvent.of(1L, 20L, ReportedType.IMPERTINENCE);

        listener.handle(event);

        then(reportModerationApplication).should().moderate(event);
    }
}
