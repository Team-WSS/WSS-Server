package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedImageCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedImageUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.PopularFeedService;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final PopularFeedService popularFeedService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createFeed(@AuthenticationPrincipal User user,
                                           @Valid @RequestPart("feed") FeedCreateRequest request,
                                           @Valid @ModelAttribute FeedImageCreateRequest requestImage) {
        feedService.createFeed(user, request, requestImage);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<FeedGetResponse> getFeed(@AuthenticationPrincipal User user,
                                                   @PathVariable("feedId") Long feedId) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getFeedById(user, feedId));
    }

    @GetMapping
    public ResponseEntity<FeedsGetResponse> getFeeds(@AuthenticationPrincipal User user,
                                                     @RequestParam(value = "category", required = false) String category,
                                                     @RequestParam("lastFeedId") Long lastFeedId,
                                                     @RequestParam("size") int size) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getFeeds(user, category, lastFeedId, size));
    }

    @PutMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.domain.Feed))")
    public ResponseEntity<Void> updateFeed(@AuthenticationPrincipal User user,
                                           @PathVariable("feedId") Long feedId,
                                           @Valid @RequestPart("feed") FeedUpdateRequest request,
                                           @Valid @ModelAttribute FeedImageUpdateRequest requestImage) {
        feedService.updateFeed(feedId, request, requestImage);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.domain.Feed))")
    public ResponseEntity<Void> deleteFeed(@AuthenticationPrincipal User user,
                                           @PathVariable("feedId") Long feedId) {
        feedService.deleteFeed(feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/{feedId}/likes")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> likeFeed(@AuthenticationPrincipal User user,
                                         @PathVariable("feedId") Long feedId) {
        feedService.likeFeed(user, feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/likes")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> unLikeFeed(@AuthenticationPrincipal User user,
                                           @PathVariable("feedId") Long feedId) {
        feedService.unLikeFeed(user, feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularFeedsGetResponse> getPopularFeeds(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(popularFeedService.getPopularFeeds(user));
    }

    @GetMapping("/interest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterestFeedsGetResponse> getInterestFeeds(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getInterestFeeds(user));
    }

    @PostMapping("/{feedId}/comments")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> createComment(@AuthenticationPrincipal User user,
                                              @PathVariable("feedId") Long feedId,
                                              @Valid @RequestBody CommentCreateRequest request) {
        feedService.createComment(user, feedId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
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
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.domain.Feed))")
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
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.domain.Feed))")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal User user,
                                              @PathVariable("feedId") Long feedId,
                                              @PathVariable("commentId") Long commentId) {
        feedService.deleteComment(user, feedId, commentId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/{feedId}/spoiler")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportFeedSpoiler(@AuthenticationPrincipal User user,
                                                  @PathVariable("feedId") Long feedId) {
        feedService.reportFeed(user, feedId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/impertinence")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportedFeedImpertinence(@AuthenticationPrincipal User user,
                                                         @PathVariable("feedId") Long feedId) {
        feedService.reportFeed(user, feedId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/comments/{commentId}/spoiler")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> reportCommentSpoiler(@AuthenticationPrincipal User user,
                                                     @PathVariable("feedId") Long feedId,
                                                     @PathVariable("commentId") Long commentId) {
        feedService.reportComment(user, feedId, commentId, SPOILER);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PostMapping("/{feedId}/comments/{commentId}/impertinence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportCommentImpertinence(@AuthenticationPrincipal User user,
                                                          @PathVariable("feedId") Long feedId,
                                                          @PathVariable("commentId") Long commentId) {
        feedService.reportComment(user, feedId, commentId, IMPERTINENCE);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

}
