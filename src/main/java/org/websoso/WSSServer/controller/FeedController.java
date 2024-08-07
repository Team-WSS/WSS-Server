package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.dto.comment.CommentUpdateRequest;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.PopularFeedService;
import org.websoso.WSSServer.service.UserService;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;
    private final PopularFeedService popularFeedService;

    @PostMapping
    public ResponseEntity<Void> createFeed(Principal principal,
                                           @Valid @RequestBody FeedCreateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.createFeed(user, request);

        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PutMapping("/{feedId}")
    public ResponseEntity<Void> updateFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId,
                                           @Valid @RequestBody FeedUpdateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.updateFeed(user, feedId, request);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.deleteFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/{feedId}/likes")
    public ResponseEntity<Void> likeFeed(Principal principal,
                                         @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.likeFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/likes")
    public ResponseEntity<Void> unLikeFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.unLikeFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedGetResponse> getFeed(Principal principal,
                                                   @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(feedService.getFeedById(user, feedId));
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularFeedsGetResponse> getPopularFeeds(Principal principal) {
        User user = principal == null ?
                null :
                userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(popularFeedService.getPopularFeeds(user));
    }

    @GetMapping
    public ResponseEntity<FeedsGetResponse> getFeeds(Principal principal,
                                                     @RequestParam("category") String category,
                                                     @RequestParam("lastFeedId") Long lastFeedId,
                                                     @RequestParam("size") int size) {
        User user = principal == null ? null : userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(feedService.getFeeds(user, category, lastFeedId, size));
    }

    @PostMapping("/{feedId}/comments")
    public ResponseEntity<Void> createComment(Principal principal,
                                              @PathVariable("feedId") Long feedId,
                                              @Valid @RequestBody CommentCreateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.createComment(user, feedId, request);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PutMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(Principal principal,
                                              @PathVariable("feedId") Long feedId,
                                              @PathVariable("commentId") Long commentId,
                                              @Valid @RequestBody CommentUpdateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.updateComment(user, feedId, commentId, request);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(Principal principal,
                                              @PathVariable("feedId") Long feedId,
                                              @PathVariable("commentId") Long commentId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.deleteComment(user, feedId, commentId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentsGetResponse> getComments(Principal principal,
                                                           @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(feedService.getComments(user, feedId));
    }

}
