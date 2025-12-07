package org.websoso.WSSServer.feed.controller;

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
import org.websoso.WSSServer.application.ReportApplication;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplication reportApplication;

    @PostMapping("/{feedId}/spoiler")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportFeedSpoiler(@AuthenticationPrincipal User user,
                                                  @PathVariable("feedId") Long feedId) {
        reportApplication.reportFeed(user, feedId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/impertinence")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportedFeedImpertinence(@AuthenticationPrincipal User user,
                                                         @PathVariable("feedId") Long feedId) {
        reportApplication.reportFeed(user, feedId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/comments/{commentId}/spoiler")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportCommentSpoiler(@AuthenticationPrincipal User user,
                                                     @PathVariable("feedId") Long feedId,
                                                     @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, feedId, commentId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/comments/{commentId}/impertinence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentImpertinence(@AuthenticationPrincipal User user,
                                                          @PathVariable("feedId") Long feedId,
                                                          @PathVariable("commentId") Long commentId) {
        reportApplication.reportComment(user, feedId, commentId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
