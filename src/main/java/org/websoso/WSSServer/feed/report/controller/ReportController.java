package org.websoso.WSSServer.feed.report.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.feed.report.application.ReportApplication;
import org.websoso.WSSServer.feed.report.exception.DuplicateReportException;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplication reportApplication;

    @PostMapping("/feeds/{feedId}/spoiler")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportFeedSpoiler(@AuthenticationPrincipal User user,
                                                  @PathVariable("feedId") Long feedId) {
        reportApplication.reportFeed(user, feedId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/feeds/{feedId}/impertinence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportedFeedImpertinence(@AuthenticationPrincipal User user,
                                                         @PathVariable("feedId") Long feedId) {
        reportApplication.reportFeed(user, feedId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/comments/{commentId}/spoiler")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentSpoiler(@AuthenticationPrincipal User user,
                                                     @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, commentId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/comments/{commentId}/impertinence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentImpertinence(@AuthenticationPrincipal User user,
                                                          @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, commentId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/feeds/{feedId}/comments/{commentId}/spoiler")
    @PreAuthorize("isAuthenticated()")
    @Deprecated(since = "POST /comments/{commentId}/spoiler로 완벽 교체시")
    public ResponseEntity<Void> reportCommentSpoiler(@AuthenticationPrincipal User user,
                                                     @PathVariable("feedId") Long feedId,
                                                     @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, feedId, commentId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/feeds/{feedId}/comments/{commentId}/impertinence")
    @PreAuthorize("isAuthenticated()")
    @Deprecated(since = "POST /comments/{commentId}/impertinence로 완벽 교체시")
    public ResponseEntity<Void> reportCommentImpertinence(@AuthenticationPrincipal User user,
                                                          @PathVariable("feedId") Long feedId,
                                                          @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, feedId, commentId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/feeds/{feedId}/spoiler/v2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportFeedSpoilerV2(@AuthenticationPrincipal User user,
                                                    @PathVariable("feedId") Long feedId) {
        reportIdempotently(() -> reportApplication.reportFeed(user, feedId, SPOILER));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/feeds/{feedId}/impertinence/v2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportFeedImpertinenceV2(@AuthenticationPrincipal User user,
                                                         @PathVariable("feedId") Long feedId) {
        reportIdempotently(() -> reportApplication.reportFeed(user, feedId, IMPERTINENCE));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/spoiler/v2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentSpoilerV2(@AuthenticationPrincipal User user,
                                                       @PathVariable("commentId") Long commentId) {
        reportIdempotently(() -> reportApplication.reportComment(user, commentId, SPOILER));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/impertinence/v2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentImpertinenceV2(@AuthenticationPrincipal User user,
                                                            @PathVariable("commentId") Long commentId) {
        reportIdempotently(() -> reportApplication.reportComment(user, commentId, IMPERTINENCE));
        return ResponseEntity.noContent().build();
    }

    private void reportIdempotently(Runnable reportAction) {
        try {
            reportAction.run();
        } catch (DuplicateReportException ignored) {
            // v2 API는 중복 신고를 이미 요청 목적이 달성된 것으로 처리한다.
        }
    }
}
