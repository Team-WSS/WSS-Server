package org.websoso.WSSServer.feed.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.feed.service.FeedService;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final FeedService feedService;

    @PostMapping("/{feedId}/comments")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> createComment(@AuthenticationPrincipal User user, @PathVariable("feedId") Long feedId,
                                              @Valid @RequestBody CommentCreateRequest request) {
        feedService.createComment(user, feedId, request);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/{feedId}/comments")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<CommentsGetResponse> getComments(@AuthenticationPrincipal User user,
                                                           @PathVariable("feedId") Long feedId) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getComments(user, feedId));
    }

    @PutMapping("/{feedId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user) "
            + "and @authorizationService.validate(#commentId, #user, T(org.websoso.WSSServer.feed.domain.Comment))")
    public ResponseEntity<Void> updateComment(@AuthenticationPrincipal User user,
                                              @PathVariable("feedId") Long feedId,
                                              @PathVariable("commentId") Long commentId,
                                              @Valid @RequestBody CommentUpdateRequest request) {
        feedService.updateComment(user, feedId, commentId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user) "
            + "and @authorizationService.validate(#commentId, #user, T(org.websoso.WSSServer.feed.domain.Comment))")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal User user,
                                              @PathVariable("feedId") Long feedId,
                                              @PathVariable("commentId") Long commentId) {
        feedService.deleteComment(user, feedId, commentId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
